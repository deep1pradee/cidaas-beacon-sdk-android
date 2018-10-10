package cidaasbeaconsdk.Helper;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.FileHandler;

import cidaasbeaconsdk.BeaconSDK;
import timber.log.Timber;

/**
 * Created by Suprada on 08-Dec-17.
 */

public class Logger {

    /* public static  FileHandler logger = null;
     private static String filename = "cidaas_sdk_log";

     static boolean isExternalStorageAvailable = false;
     static boolean isExternalStorageWriteable = false;
     static String state = Environment.getExternalStorageState();

     public static void addRecordToLog(String message) {
         File sddir = Environment.getExternalStorageDirectory();
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

         File logFileDir=new File(sddir,"cidaas-beacon/");
         if (Environment.MEDIA_MOUNTED.equals(state)) {
             if(!logFileDir.exists()) {
                 //Log.d("Dir created ", "Dir created ");
                 logFileDir.mkdirs();
             }

             File logFile = new File(logFileDir,filename+".txt");

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
     }*/
    public static FileHandler logger = null;
    private static String filename = "beacon_log";

    //Variables to Check permission
    static boolean isExternalStorageAvailable = false;
    static boolean isExternalStorageWriteable = false;


    static String state = Environment.getExternalStorageState();

    //Shared Instances
    public static Logger shared;


    public static Logger getShared() {
        if (shared == null) {
            shared = new Logger();
        }
        return shared;
    }


    //Add records to a log file
    public static void addRecordToLog(String message) {
        try {
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
            if (BeaconSDK.isLogEnable) {
                File sddir = Environment.getExternalStorageDirectory();
                File logFileDir = new File(sddir, "cidaasbeacon/");
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    if (!logFileDir.exists()) {

                        logFileDir.mkdirs();
                    }

                    File logFile = new File(logFileDir, filename + ".txt");

                    if (!logFile.exists()) {
                        try {
                            // Log.d("File created ", "File created ");
                            logFile.createNewFile();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Timber.d(e.getMessage());
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
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Timber.d(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }


        } catch (Exception e) {

            Timber.d(e.getMessage());   //todo Handle Exception
        }
    }
}
