package dk.meew.flutter_nemid.utilities;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;

/*
This class is implemented to support the NemIdWebview to fix problems in the four-digit password
case. When the client is using the four password boxes for password input, it must be possible to
delete characters in previous boxes. However, in API levels lower than 22 on some devices, this
can be problematic, as the KEYCODE_DEL (backspace) keyevent is not always emitted when pressing backspace
in an empty input field. This class implements a fix found in:
http://stackoverflow.com/questions/18581636/android-cannot-capture-backspace-delete-press-in-soft-keyboard
 */
public class InputConnectionForBackspaceIssues extends BaseInputConnection {

    public InputConnectionForBackspaceIssues(View targetView, boolean fullEditor) {
        super(targetView, fullEditor);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        if ((Build.VERSION.SDK_INT >= 14) && (beforeLength == 1 && afterLength == 0)) {
            return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                    && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        } else {
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

}
