package javax.obex.utils;

import android.util.Log;
import android.webkit.MimeTypeMap;

public class Utility {

    private static String TAG = "Utility";

    public static String getMimeType(String filename) {
        String extension = GetFileExtension(filename);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String type = map.getMimeTypeFromExtension(extension);
        String mimeType = null;
        Log.d(TAG, "Mimetype guessed from extension " + extension + " is " + type);

        if (type != null) {
            mimeType = type;
        } else {
            if (mimeType == null) {
                if (true) {
                    Log.d(TAG, "Can't get mimetype");
                }
            }
        }
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase();
        }

        return mimeType;
    }

    public static String GetFileExtension(String hint) {
        // TODO: Implement this method
        int dotIndex = hint.lastIndexOf(".");
        if (dotIndex > 0) {
            return hint.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

}
