package cidaasbeaconsdk.Helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    // Shared Preferences
    SharedPreferences preferences;
    // Editor for Shared preferences
    SharedPreferences.Editor editor;
    //Application context
    Context context;
    private static final String PREFS_NAME = "cidaasPrefsFile";
    private static final String KEY_LAT = "";
    private static final String KEY_LON = "";

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

    public void setLatLon(String lat, String lon) {
        editor.putString(KEY_LAT, lat);
        editor.putString(KEY_LON, lon);
        editor.apply();
    }

    public String getLat() {
        return preferences.getString(KEY_LAT, "");
    }

    public String getLon() {
        return preferences.getString(KEY_LON, "");
    }

}
