package cidaasbeaconsdk.Service;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import cidaasbeaconsdk.Entity.ErrorEntity;
import cidaasbeaconsdk.Entity.RegionCallBack;
import cidaasbeaconsdk.Helper.Logger;

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitions";
    public static String[] list = new String[0];
    public static RegionCallBack regionCallBack;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    static List<Geofence> triggeringGeofences = new ArrayList<>();
    Logger logger;

    @Override
    protected void onHandleIntent(Intent intent) {
        logger.addRecordToLog("onHandleIntent");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            //String errorMessage = GeofenceErrorMessages.getErrorString(this,
            //      geofencingEvent.getErrorCode());
            logger.addRecordToLog("Goefencing Error " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.setStatus(417);
        errorEntity.setSuccess(false);
        logger.addRecordToLog("geofenceTransition = " + geofenceTransition + " Enter : " + Geofence.GEOFENCE_TRANSITION_ENTER + "Exit : " + Geofence.GEOFENCE_TRANSITION_EXIT);
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            logger.addRecordToLog("onHandleIntent: Entered Entered the Location " + triggeringGeofences.size());
            /*errorEntity.setMessage("Entered the Location");
            BeaconSDK.mBeaconEvents.onError(errorEntity);
            BeaconSDK.mBeaconEvents.didEnterGeoRegion();*/
            if (triggeringGeofences != null && triggeringGeofences.size() > 0) {
                list = new String[triggeringGeofences.size()];
                for (int i = 0; i < triggeringGeofences.size(); i++) {
                    list[i] = triggeringGeofences.get(i).getRequestId();
                }
            }
            if (regionCallBack != null) {
                regionCallBack.OnEntered(getTriggeringIds());

            }


        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            logger.addRecordToLog( "Showing Notification...");
            logger.addRecordToLog("Exited Exited the Location");
           /* errorEntity.setMessage("Exited the Location");
            BeaconSDK.mBeaconEvents.onError(errorEntity);
            BeaconSDK.mBeaconEvents.didExitGeoRegion();*/
            if (regionCallBack != null) {
                regionCallBack.OnExited();
            }
        } else {
            logger.addRecordToLog("Error: ");
            // Log the error.
            errorEntity.setMessage("Error");
            // BeaconSDK.mBeaconEvents.onError(errorEntity);
            logger.addRecordToLog("Error");
            logger.addRecordToLog( "Error ");
        }
    }

    public static String[] getTriggeringIds() {

        return list;

    }

}
