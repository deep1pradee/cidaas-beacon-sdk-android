package cidaasbeaconsdk.Helper;

/**
 * Created by Suprada on 08-Dec-17.
 */

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.FileHandler;


public class Logger {

    public static  FileHandler logger = null;
    private static String filename = "cidaas_sdk_log";

    static boolean isExternalStorageAvailable = false;
    static boolean isExternalStorageWriteable = false;
    static String state = Environment.getExternalStorageState();

    public static void addRecordToLog(String message) {

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            isExternalStorageAvailable = isExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            isExternalStorageAvailable = true;
            isExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            isExternalStorageAvailable = isExternalStorageWriteable = false;
        }

        File dir = new File("/sdcard/Files/");
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if(!dir.exists()) {
                //Log.d("Dir created ", "Dir created ");
                dir.mkdirs();
            }

            File logFile = new File("/sdcard/Files/"+filename+".txt");

            if (!logFile.exists())  {
                try  {
                   // Log.d("File created ", "File created ");
                    logFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));

                buf.write(message + "\r\n");
                //buf.append(message);
                buf.newLine();
                buf.flush();
                buf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
