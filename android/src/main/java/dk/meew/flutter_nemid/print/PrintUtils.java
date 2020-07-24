package dk.meew.flutter_nemid.print;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;

/*
 * Class for separate instantiation of API level dependent Print functionality
 * Printing is not available in API levels lower than 19.
 * In order to support printing on >=19, while not using this functionality in API level<16,
 * the functionality is split into a separate class which is instantiated runtime, only when
 * running the correct API level.
 */

public class PrintUtils {

    @TargetApi(19)
    public void createWebPrintJob(WebView webView, Activity activity) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = "NemID";
        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
    }
}
