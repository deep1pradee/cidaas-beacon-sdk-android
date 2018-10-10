package cidaasbeaconsdk.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SharedPref {
    // Shared Preferences
    SharedPreferences preferences;
    // Editor for Shared preferences
    SharedPreferences.Editor editor;
    //Application context
    Context context;
    private static final String PREFS_NAME = "cidaasPrefsFile";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_SUB = "sub";
    private static final String KEY_LOC_ID = "location_ids";
    private static final String KEY_SESSION_ID = "session_ids";

    static SharedPref sharedPref;

    private SharedPref(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.apply();
    }

    public static SharedPref getSharedPrefInstance(Context context) {
        if (sharedPref == null)
            sharedPref = new SharedPref(context);
        return sharedPref;
    }

    public void setLat(String lat) {
        editor.putString(KEY_LAT, lat);
        editor.apply();
    }

    public void setLon(String lon) {
        editor.putString(KEY_LON, lon);
        editor.apply();
    }

    public String getLat() {
        return preferences.getString(KEY_LAT, "");
    }

    public String getLon() {
        return preferences.getString(KEY_LON, "");
    }

    public void setSub(String sub) {
        editor.putString(KEY_USER_SUB, sub);
        editor.apply();

    }

    public void setAccessToken(String access_token) {
        editor.putString(KEY_ACCESS_TOKEN, access_token);
        editor.apply();
    }

    public String getSub() {
        return preferences.getString(KEY_USER_SUB, "");
    }

    public String getAccessToken() {
        return preferences.getString(KEY_ACCESS_TOKEN, "");
    }

    public void setLocationIds(String locationId) {
        Set<String> locIds = getLocationIds();
        locIds.add(locationId);
        editor.putStringSet(KEY_LOC_ID, locIds);
        editor.apply();
    }

    public void removeLocationId() {
        editor.putStringSet(KEY_LOC_ID, new HashSet<String>());
        editor.apply();
    }

    public void removeSessionId() {
        try {
            editor.putString(KEY_SESSION_ID, "");
            editor.apply();
        } catch (Exception ex) {

        }

    }

    public Set<String> getLocationIds() {
        return preferences.getStringSet(KEY_LOC_ID, new HashSet<String>());
    }

    public String getSessionId() {
        return preferences.getString(KEY_SESSION_ID, "");
    }

    public void setSessionId(String uniqueId) {
        editor.putString(KEY_SESSION_ID, uniqueId);
        editor.apply();
    }
}
