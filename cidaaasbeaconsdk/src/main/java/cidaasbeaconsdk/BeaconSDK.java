package cidaasbeaconsdk;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import cidaasbeaconsdk.Entity.BeaconEmitRequest;
import cidaasbeaconsdk.Entity.BeaconEvents;
import cidaasbeaconsdk.Entity.CategoryResponse;
import cidaasbeaconsdk.Entity.CategoryResponseEntity;
import cidaasbeaconsdk.Entity.DeviceLocation;
import cidaasbeaconsdk.Entity.ErrorEntity;
import cidaasbeaconsdk.Entity.Geo;
import cidaasbeaconsdk.Entity.LOcationCordinates;
import cidaasbeaconsdk.Entity.Proximity;
import cidaasbeaconsdk.Entity.ProximityListRequest;
import cidaasbeaconsdk.Entity.RegionCallBack;
import cidaasbeaconsdk.Entity.Result;
import cidaasbeaconsdk.Helper.BeaconHelper;
import cidaasbeaconsdk.Helper.Logger;
import cidaasbeaconsdk.Helper.SharedPref;
import cidaasbeaconsdk.Service.GeofenceTransitionsIntentService;
import cidaasbeaconsdk.Service.ServiceModel;
import cidaasbeaconsdk.Service.ServiceModelImpl;
import i.widaas.cidaaasbeaconsdk.BuildConfig;
import okhttp3.Request;

import static cidaasbeaconsdk.Helper.SharedPref.getSharedPrefInstance;


public class BeaconSDK {
    static Context mContext;
    static BeaconHelper beaconHelper;
    private ArrayList<Geofence> mGeofenceList;
    String TAG = "main";
    static BeaconEvents mBeaconEvents;
    org.altbeacon.beacon.BeaconManager beaconManager;
    static BeaconConsumer beaconConsumer;
    static Intent serviceIntent;
    private AssetManager assetManager;
    private String configurationFileName;
    SharedPref sharedPref;
    Logger logger;
    com.google.android.gms.location.LocationListener locationListener;
    /*12.919471096259466, */
    //   double currentLatitude = 12.919564999999999, currentLongitude = 77.6683352;
    double currentLatitude = 0, currentLongitude = 0;
    // double defaultLat = 12.919592, defaultLon = 77.668214;
    //   double defaultLat = 12.9075669, defaultLon = 77.5618457;
    //   double currentLatitude = 12.919523752209402, currentLongitude = 77.6682609109338;
    LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    Intent intent;
    private boolean isExcecute = false;


    public void registerEvents(BeaconEvents beaconEvents) {
        mBeaconEvents = beaconEvents;
    }

    static BeaconSDK manager;
    static ServiceModel serviceModel;

    public static BeaconSDK getInstance(Context context) {

        if (manager == null) {
            manager = new BeaconSDK(context);
        }
        return manager;
    }

    private BeaconSDK(Context context) {
        ErrorEntity errorEntity;
        if (context != null) {
            mContext = context;
            beaconHelper = new BeaconHelper();
            beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(mContext);
            beaconManager.getBeaconParsers().add(new BeaconParser()
                    .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
            serviceModel = new ServiceModelImpl();
            sharedPref = getSharedPrefInstance(mContext);
            logger = Logger.getShared();
            GeofenceTransitionsIntentService.regionCallBack = new RegionCallBack() {
                @Override
                public void OnEntered(String[] triggeringIds) {
                    sharedPref.setSessionId(UUID.randomUUID().toString());
                    for (int i = 0; i < triggeringIds.length; i++) {
                        sharedPref.setLocationIds(triggeringIds[i]);
                    }
                    mBeaconEvents.didEnterGeoRegion();
                    StartLocEmitService("STARTED");
                    resumeLocationUpdates();
                    // startGeoFencing();
                    logger.addRecordToLog("Size of existing geo fences " + getGeofencingRequest().getGeofences().size());
                    logger.addRecordToLog("STARTED " + triggeringIds.length);
                }

                @Override
                public void OnExited() {
                    sharedPref.removeLocationId();
                    sharedPref.removeSessionId();
                    mBeaconEvents.didExitGeoRegion();
                    StartLocEmitService("ENDED");
                    logger.addRecordToLog("ENDED ");
                }
            };
        } else {
            if (mBeaconEvents != null) {
                errorEntity = new ErrorEntity();
                errorEntity.setStatus(417);
                errorEntity.setSuccess(false);
                errorEntity.setMessage("Please provide activity context ");
                mBeaconEvents.onError(errorEntity);
            }
        }


    }

    private boolean isGPSON() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled) {
            return false;
        } else return true;
    }

    private boolean isBTOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                return false;
            } else return true;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void setUpLocationUpdates(LOcationCordinates data) {
        mGeofenceList = new ArrayList<Geofence>();

        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resp == ConnectionResult.SUCCESS) {
            logger.addRecordToLog("Google api client connecction success " + resp);
            initGoogleAPIClient();
            if (data != null) {
                logger.addRecordToLog("location coordinates call data is not null");
                createGeofences(data);
            }

        } else {
            logger.addRecordToLog("Your Device doesn't support Google Play Services.");
        }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds
    }

    public static String getAppLabel(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    public static String createCustomUserAgent(Request originalRequest) {
        // App name can be also retrieved programmatically, but no need to do it for this sample needs
        String ua = getAppLabel(mContext);
        String baseUa = System.getProperty("http.agent");
        if (baseUa != null) {
            ua = ua + "/" + BuildConfig.VERSION_NAME + " " + baseUa;
        }
        return ua;
    }

    // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
    // find a different type of beacon, you must specify the byte layout for that beacon's
    // advertisement with a line like below.  The example shows how to find a beacon with the
    // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
    // layout expression for other beacon types, do a web search for "setBeaconLayout"
    // including the quotes.
    public void setBeaconLayout(String beaconLayout) {
        //"m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(beaconLayout));
    }


    public void unbind() {
        try {
            if (beaconManager != null)
                beaconManager.unbind(beaconConsumer);
            if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        } catch (Exception ex) {
            logger.addRecordToLog("unbind exception " + ex.getMessage());
        }

    }

    public void setUpBackgroundMode(boolean isBackgroundMode) {
        if (beaconManager != null && beaconManager.isBound(beaconConsumer))
            beaconManager.setBackgroundMode(isBackgroundMode);
    }

    private void resumeLocationUpdates() {
        logger.addRecordToLog("RESUMING LOCATION UPDATES");
        if (mGoogleApiClient != null && mLocationRequest != null && locationListener != null)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationListener);
    }

    @NonNull
    private cidaasbeaconsdk.Entity.BeaconResult setBeacon(Beacon mBeacon, Region region) {

        cidaasbeaconsdk.Entity.BeaconResult beacon = new cidaasbeaconsdk.Entity.BeaconResult();
        if (mBeacon != null) {
            beacon.setUuid(mBeacon.getId1().toUuid().toString());
            beacon.setBluetoothAddress(mBeacon.getBluetoothAddress());
            beacon.setMajor(mBeacon.getId2().toString());
            beacon.setMinor(mBeacon.getId3().toString());
            if (mBeacon.getDistance() < 0.5)
                beacon.setProximity(Proximity.IMMEDIATE);
            else if (mBeacon.getDistance() <= 3.0)
                beacon.setProximity(Proximity.NEAR);
            else if (mBeacon.getDistance() > 3) {
                beacon.setProximity(Proximity.FAR);
            } else {
                beacon.setProximity(Proximity.UNKNOWN);
            }
            beacon.setDistance(mBeacon.getDistance());
        } else if (region != null) {
            beacon.setUuid(region.getId1().toUuid().toString());
            if (region.getId2() != null)
                beacon.setMajor(region.getId2().toString());
            if (region.getId3() != null)
                beacon.setMinor(region.getId3().toString());
            // logger.addRecordToLog("Manager  setBeacon: " + region.toString());
        }
        return beacon;
    }

    public void setExpirationMilliseconds(long milliseconds) {
        RangedBeacon.setSampleExpirationMilliseconds(milliseconds);
    }

    public void startBeaconMonitoringAndRanging(final List<CategoryResponse> beaconList, final String sub, final String access_token) {
        ErrorEntity errorEntity;

        if (/*isBTOn() && */isNetworkConnected() && isGPSON()) {
            sharedPref.setAccessToken(access_token);
            sharedPref.setSub(sub);

            if (SDKEntity.SDKEntityInstance != null && SDKEntity.SDKEntityInstance.getBaseUrl() != null &&
                    !SDKEntity.SDKEntityInstance.getBaseUrl().equals("")) {

                serviceModel.getProximityList(access_token, getProximityReq(), SDKEntity.SDKEntityInstance.getBaseUrl(), new Result<LOcationCordinates>() {
                    @Override
                    public void onSuccess(LOcationCordinates result) {
                        setUpLocationUpdates(result);
                    }

                    @Override
                    public void onError(ErrorEntity errorEntity) {
                        if (mBeaconEvents != null)
                            mBeaconEvents.onError(errorEntity);
                    }
                });
                if (beaconList != null && beaconList.size() != 0) {
                    setBeaconList(beaconList, sub, access_token);
                } else {
                    getBeaconUUIDs(new Result<CategoryResponseEntity>() {
                        @Override
                        public void onSuccess(CategoryResponseEntity result) {
                            setBeaconList(beaconList, sub, access_token);
                        }

                        @Override
                        public void onError(ErrorEntity errorEntity) {
                            if (mBeaconEvents != null) {
                                errorEntity = new ErrorEntity();
                                errorEntity.setStatus(417);
                                errorEntity.setSuccess(false);
                                errorEntity.setMessage("No Base URL");
                                mBeaconEvents.onError(errorEntity);
                            }
                        }
                    });
                }

            } else {
                if (mBeaconEvents != null) {
                    errorEntity = new ErrorEntity();
                    errorEntity.setStatus(417);
                    errorEntity.setSuccess(false);
                    errorEntity.setMessage("No Base URL");
                    mBeaconEvents.onError(errorEntity);
                }
            }
        } else {
            if (mBeaconEvents != null) {
                errorEntity = new ErrorEntity();
                errorEntity.setStatus(417);
                errorEntity.setSuccess(false);
                logger.addRecordToLog("Please make sure you enabledPlease make sure you enabled your Internet / GPS / Bluetooth");
                errorEntity.setMessage("Please make sure you enabled your Internet / GPS / Bluetooth");
                mBeaconEvents.onError(errorEntity);
            }
        }


    }

    private ProximityListRequest getProximityReq() {
        ProximityListRequest proximityListRequest = new ProximityListRequest();
        proximityListRequest.setSkip(0);
        proximityListRequest.setTake(100);
        return proximityListRequest;
    }

    private void setBeaconList(List<CategoryResponse> beaconList, String sub, String access_token) {
        if (beaconManager != null && beaconManager.isBound(beaconConsumer)) {
            addMonitoringNotifier(beaconList, sub, access_token);
            //  logger.addRecordToLog("startBeaconMonitoring: is bound");
        } else {
            //  logger.addRecordToLog("startBeaconMonitoring: not bound");
            setUpConsumer(beaconList, sub, access_token);
        }
    }

    private void setUpConsumer(final List<CategoryResponse> beaconList, final String sub, final String access_token) {
        beaconConsumer = new BeaconConsumer() {

            @Override
            public void onBeaconServiceConnect() {
                addMonitoringNotifier(beaconList, sub, access_token);
            }

            @Override
            public Context getApplicationContext() {
                return mContext;
            }

            @Override
            public void unbindService(ServiceConnection serviceConnection) {
                mContext.unbindService(serviceConnection);
                mContext.stopService(serviceIntent);
            }

            @Override
            public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
                serviceIntent = intent;
                mContext.startService(intent);
                return mContext.bindService(intent, serviceConnection, i);

            }
        };
        beaconManager.bind(beaconConsumer);
    }


    public void setURLFile(AssetManager asset, String fileName) {
        logger.addRecordToLog("file name :" + fileName);
        assetManager = asset;
        configurationFileName = fileName;
        SDKEntity.SDKEntityInstance.readInputs(assetManager, configurationFileName, mContext);
    }

    public void setURL(String baseURL) {
        SDKEntity.SDKEntityInstance.setBaseUrl(baseURL);

    }


    private void addMonitoringNotifier(List<CategoryResponse> beaconList, final String sub, final String access_token) {
        try {
            beaconManager.addMonitorNotifier(new MonitorNotifier() {
                @Override
                public void didEnterRegion(Region region) {
                    if (mBeaconEvents != null) {
                        mBeaconEvents.didEnterRegion(setBeacon(null, region));
                    }
                }

                @Override
                public void didExitRegion(Region region) {
                    if (mBeaconEvents != null)
                        mBeaconEvents.didExitRegion(setBeacon(null, region));
                }

                @Override
                public void didDetermineStateForRegion(int i, Region region) {
                /* int INSIDE = 1;
                   int OUTSIDE = 0;*/
                    if (mBeaconEvents != null) {
                        mBeaconEvents.didDetermineStateForRegion(i, setBeacon(null, region));
                    }
                }
            });
            beaconManager.addRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    if (beacons.size() > 0) {
                        Beacon firstBeacon = beacons.iterator().next();
                        if (mBeaconEvents != null) {
                            mBeaconEvents.didBeaconsInRange(setBeacon(firstBeacon, null));
                            String android_id = Settings.Secure.getString(mContext.getContentResolver(),
                                    Settings.Secure.ANDROID_ID);
                            BeaconEmitRequest beaconEmitRequest = setUpRequestEntity(firstBeacon, android_id, sub);


                            final Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                public void run() {
                                    handler.postDelayed(this, 6000);
                                    isExcecute = true;
                                }
                            };
                            runnable.run();
                            if (isExcecute) {
                                //   logger.addRecordToLog("beacon emit called " + SDKEntity.SDKEntityInstance.getBaseUrl());
                                isExcecute = false;
                                serviceModel.updateBeacon(access_token, beaconEmitRequest, SDKEntity.SDKEntityInstance.getBaseUrl());
                            }
                        }
                    }
                }
            });
            for (int i = 0; i < beaconList.size(); i++) {
                Identifier id1 = null;
                if (beaconList.get(i).getUniqueId() != null && beaconList.get(i).getUniqueId().length != 0) {
                    for (int j = 0; j < beaconList.get(i).getUniqueId().length; j++) {
                        id1 = Identifier.parse(beaconList.get(i).getUniqueId()[j]);
                        Region region = new Region(UUID.randomUUID().toString(), id1, null, null);
                        try {
                            //    logger.addRecordToLog("region while registering " + region.toString());
                            beaconManager.startMonitoringBeaconsInRegion(region);
                            beaconManager.startRangingBeaconsInRegion(region);
                        } catch (Exception ex) {
                            //  logger.addRecordToLog("startMonitoringBeaconsInRegion" + ex.getMessage());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.addRecordToLog(ex.getMessage());
        }

    }

    @NonNull
    private BeaconEmitRequest setUpRequestEntity(Beacon firstBeacon, String android_id, String sub) {
        BeaconEmitRequest beaconEmitRequest = new BeaconEmitRequest();
        beaconEmitRequest.setDeviceId(android_id);
        beaconEmitRequest.setSub(sub);
        beaconEmitRequest.setDistance(String.valueOf(firstBeacon.getDistance()));
        Geo geo = new Geo();
        geo.setCoordinates(new String[]{String.valueOf(currentLongitude), String.valueOf(currentLatitude)});
        beaconEmitRequest.setGeo(geo);
        DeviceLocation deviceLocation = new DeviceLocation();
        // deviceLocation.setGeo(geo);
        deviceLocation.setMajor(firstBeacon.getId2().toString());
        deviceLocation.setMinor(firstBeacon.getId3().toString());
        deviceLocation.setUniqueId(firstBeacon.getId1().toString());
        beaconEmitRequest.setBeacon(deviceLocation);
        try {
            //  logger.addRecordToLog(new ObjectMapper().writeValueAsString(beaconEmitRequest));
        } catch (Exception e) {
            //  logger.addRecordToLog(e.getMessage());
        }
        return beaconEmitRequest;
    }


    public void getBeaconUUIDs(Result<CategoryResponseEntity> responseEntityResult) {
        try {
            if (SDKEntity.SDKEntityInstance != null && SDKEntity.SDKEntityInstance.getBaseUrl() != null && !SDKEntity.SDKEntityInstance.getBaseUrl().equals("")) {
                serviceModel.getDefaultConfig(SDKEntity.SDKEntityInstance.getBaseUrl(), responseEntityResult);
            } else
                responseEntityResult.onSuccess(BeaconHelper.getUUID());
        } catch (Exception ex) {
            ErrorEntity errorEntity = new ErrorEntity();
            errorEntity.setStatus(500);
            errorEntity.setSuccess(false);
            errorEntity.setMessage(ex.getMessage());
            responseEntityResult.onError(errorEntity);
        }


    }


    public void initGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionAddListener)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        mGoogleApiClient.connect();
    }


    public void StartLocEmitService(String status) {
        logger.addRecordToLog("StartLocEmitService: " + status);
        serviceModel.updateLocation(sharedPref.getAccessToken(), getLocationRequest(currentLatitude, currentLongitude, status), SDKEntity.SDKEntityInstance.getBaseUrl());
    }

    private GoogleApiClient.ConnectionCallbacks connectionAddListener =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    try {
                        Log.i(TAG, "onConnected");

                        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        locationListener = new com.google.android.gms.location.LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                currentLatitude = location.getLatitude();
                                currentLongitude = location.getLongitude();
                                if (sharedPref.getSessionId() != null && !sharedPref.getSessionId().equalsIgnoreCase("")) {
                                    StartLocEmitService("IN_PROGRESS");
                                }
                                logger.addRecordToLog("onLocationChanged " + location.getLatitude() + " " + location.getLongitude());
                            }
                        };
                        // resumeLocationUpdates();
                        if (location == null) {
                            resumeLocationUpdates();
                            // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationListener);
                        } else {
                            //If everything went fine lets get latitude and longitude
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            logger.addRecordToLog(currentLatitude + " WORKS " + currentLongitude);

                            //createGeofences(currentLatitude, currentLongitude);
                            //registerGeofences(mGeofenceList);
                        }
                        sharedPref.setLat(String.valueOf(currentLatitude));
                        sharedPref.setLon(String.valueOf(currentLongitude));
                        logger.addRecordToLog("lat " + currentLatitude + " lon " + currentLongitude);
                        try {
                            startGeoFencing();
                        } catch (SecurityException securityException) {
                            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                            logger.addRecordToLog("Error " + securityException.getMessage());
                        }
                    } catch (Exception ex) {
                        logger.addRecordToLog(ex.getMessage());
                    }

                }

                @Override
                public void onConnectionSuspended(int i) {
                    try {
                        if (mGoogleApiClient != null && locationListener != null)
                            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);

                    } catch (Exception ex) {
                        logger.addRecordToLog("onConnectionSuspended: " + ex.getMessage());
                    }

                    logger.addRecordToLog("onConnectionSuspended");

                }
            };

    private void startGeoFencing() {
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {

            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    logger.addRecordToLog("Saving Geofence");

                } else {
                    logger.addRecordToLog("Registering geofence failed: " + status.getStatusMessage() +
                            " : " + status.getStatusCode());
                }
            }
        });
    }

    /* private float getDistance(Location location) {
         Location newLoc = new Location("loc");
         newLoc.setLatitude(defaultLat);
         newLoc.setLongitude(defaultLon);
         return location.distanceTo(newLoc);
     }*/
    public void stopMonitoringAndRanging() {
        Collection<Region> monitoringList = beaconManager.getMonitoredRegions();
        if (monitoringList != null && monitoringList.size() > 0) {
            try {
                Iterator<Region> iterator = monitoringList.iterator();
                // while loop
                while (iterator.hasNext()) {
                    beaconManager.stopMonitoringBeaconsInRegion(iterator.next());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collection<Region> rangedRegionList = beaconManager.getRangedRegions();
        if (rangedRegionList != null && rangedRegionList.size() > 0) {
            try {
                Iterator<Region> iterator = rangedRegionList.iterator();
                // while loop
                while (iterator.hasNext()) {
                    beaconManager.stopRangingBeaconsInRegion(iterator.next());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (GeofenceTransitionsIntentService.regionCallBack != null) {
            logger.addRecordToLog("Manually stopped monitoring & ranging !! ");
            GeofenceTransitionsIntentService.regionCallBack.OnExited();
        }

    }

    private cidaasbeaconsdk.Entity.LocationRequest getLocationRequest(double currentLatitude, double currentLongitude, String status) {
        Set<String> list = sharedPref.getLocationIds();
        Geo geo = new Geo();
        geo.setCoordinates(new String[]{String.valueOf(currentLongitude), String.valueOf(currentLatitude)});
        cidaasbeaconsdk.Entity.LocationRequest deviceLocation = new cidaasbeaconsdk.Entity.LocationRequest();
        String android_id = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        deviceLocation.setDeviceId(android_id);
        deviceLocation.setGeo(geo);
        deviceLocation.setStatus(status);
        if (list != null) {
            String[] array = new String[list.size()];
            // deviceLocation.setLocationIds(array);
        } else if (GeofenceTransitionsIntentService.list != null && GeofenceTransitionsIntentService.list.length > 0) {
            String[] array = new String[GeofenceTransitionsIntentService.list.length];
            //deviceLocation.setLocationIds(array);
        }
        deviceLocation.setSessionId(sharedPref.getSessionId());
        deviceLocation.setSub(sharedPref.getSub());
        //once ended remove all the ids from shared preference
        if (status.equalsIgnoreCase("ENDED")) {
            sharedPref.removeLocationId();
            GeofenceTransitionsIntentService.list = new String[0];
            if (mGoogleApiClient != null && locationListener != null)
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
        }
        return deviceLocation;
    }

    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    logger.addRecordToLog("onConnectionFailed");
                }
            };

    /**
     * Create a Geofence list
     */
    public void createGeofences(LOcationCordinates data) {
        logger.addRecordToLog("createGeofences: called ");
        if (data.getData() != null) {
            for (int i = 0; i < data.getData().length; i++) {
                try {

                    Geofence fence = new Geofence.Builder()
                            .setRequestId(data.getData()[i].getLocationId())
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                            .setCircularRegion(Double.parseDouble(data.getData()[i].getCoordinates()[1]),
                                    Double.parseDouble(data.getData()[i].getCoordinates()[0]), data.getData()[i].getRadius())
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .build();
                    logger.addRecordToLog("createGeofences: fence lat " + data.getData()[i].getCoordinates()[1] + " lon " + data.getData()[i].getCoordinates()[0]);
                    mGeofenceList.add(fence);

                } catch (Exception e) {
                    logger.addRecordToLog(e.getMessage());
                }

            }
        }


    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }


}
