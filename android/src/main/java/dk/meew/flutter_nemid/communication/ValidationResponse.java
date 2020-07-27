package dk.meew.flutter_nemid.communication;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import dk.meew.flutter_nemid.utilities.Base64;
import dk.meew.flutter_nemid.utilities.StringHelper;

/**
 * Utility class for with parsing capability for validation results received from the SP backend.
 */
public class ValidationResponse {
	public static Map<String, String> parse(String value) {
		Map<String, String> result = new HashMap<>();
		StringTokenizer str = new StringTokenizer(value.trim(), ";", false);
		while (str.hasMoreTokens()) {
			String token = str.nextToken();
			int i = token.indexOf("=");
			if (i == -1) {
				throw new RuntimeException("Parameters not separated by =: " + token + "\nComplete value from server:\n" + value);
			}
			String key = token.substring(0, i);
			String encodedValue = token.substring(i + 1);
			String v = encodedValue.length() == 0 ? "" : StringHelper.toUtf8String(Base64.decode(encodedValue));

			result.put(key, v);
		}
		return result;
	}
}
