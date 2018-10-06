package cidaasbeaconsdk.Service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import cidaasbeaconsdk.BeaconSDK;
import cidaasbeaconsdk.Entity.ErrorEntity;
import cidaasbeaconsdk.Entity.RegionCallBack;
import timber.log.Timber;

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitions";
    public static String[] list = new String[0];
    public static RegionCallBack regionCallBack;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    static List<Geofence> triggeringGeofences = new ArrayList<>();

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            //String errorMessage = GeofenceErrorMessages.getErrorString(this,
            //      geofencingEvent.getErrorCode());
            Log.e(TAG, "Goefencing Error " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.setStatus(417);
        errorEntity.setSuccess(false);
        Log.i(TAG, "geofenceTransition = " + geofenceTransition + " Enter : " + Geofence.GEOFENCE_TRANSITION_ENTER + "Exit : " + Geofence.GEOFENCE_TRANSITION_EXIT);
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            Log.d(TAG, "onHandleIntent: Entered Entered the Location " + triggeringGeofences.size());
            /*errorEntity.setMessage("Entered the Location");
            BeaconSDK.mBeaconEvents.onError(errorEntity);
            BeaconSDK.mBeaconEvents.didEnterGeoRegion();*/
            if (regionCallBack != null) {
                regionCallBack.OnEntered(getTriggeringIds());
            }
            if (triggeringGeofences != null) {
                for (int i = 0; i < triggeringGeofences.size(); i++) {
                    list[i] = triggeringGeofences.get(i).getRequestId();
                }
            }

        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d(TAG, "Showing Notification...");
            Timber.d("Exited", "Exited the Location");
           /* errorEntity.setMessage("Exited the Location");
            BeaconSDK.mBeaconEvents.onError(errorEntity);
            BeaconSDK.mBeaconEvents.didExitGeoRegion();*/
            if (regionCallBack != null) {
                regionCallBack.OnExited();
            }
        } else {
            Log.d(TAG, "Error: ");
            // Log the error.
            errorEntity.setMessage("Error");
            BeaconSDK.mBeaconEvents.onError(errorEntity);
            Timber.d("Error", "Error");
            Log.e(TAG, "Error ");
        }
    }

    public static String[] getTriggeringIds() {

        return list;

    }

}
