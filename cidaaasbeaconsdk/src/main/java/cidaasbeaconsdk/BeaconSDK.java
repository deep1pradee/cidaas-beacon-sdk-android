package cidaasbeaconsdk;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.provider.Settings;
import android.support.annotation.NonNull;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;

import java.util.Collection;
import java.util.List;

import cidaasbeaconsdk.Entity.BeaconEmitRequest;
import cidaasbeaconsdk.Entity.BeaconEvents;
import cidaasbeaconsdk.Entity.CategoryResponse;
import cidaasbeaconsdk.Entity.CategoryResponseEntity;
import cidaasbeaconsdk.Entity.DeviceLocation;
import cidaasbeaconsdk.Entity.ErrorEntity;
import cidaasbeaconsdk.Entity.Geo;
import cidaasbeaconsdk.Entity.Proximity;
import cidaasbeaconsdk.Entity.Result;
import cidaasbeaconsdk.Helper.BeaconHelper;
import cidaasbeaconsdk.Helper.SharedPref;
import cidaasbeaconsdk.Service.ServiceModel;
import cidaasbeaconsdk.Service.ServiceModelImpl;
import timber.log.Timber;

import static cidaasbeaconsdk.Helper.SharedPref.getSharedPrefInstance;


public class BeaconSDK {
    static Context mContext;
    static BeaconHelper beaconHelper;
    String TAG = "main";
    static BeaconEvents mBeaconEvents;
    org.altbeacon.beacon.BeaconManager beaconManager;
    static BeaconConsumer beaconConsumer;
    static Intent serviceIntent;
    private AssetManager assetManager;
    private String configurationFileName;
    SharedPref sharedPref;

    public void registerEvents(BeaconEvents beaconEvents) {
        mBeaconEvents = beaconEvents;
    }

    static BeaconSDK manager;
    ServiceModel serviceModel;

    public static BeaconSDK getInstance(Context context) {

        if (manager == null) {
            manager = new BeaconSDK(context);
        }
        return manager;
    }

    private BeaconSDK(Context context) {
        mContext = context;
        beaconHelper = new BeaconHelper();
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(mContext);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        serviceModel = new ServiceModelImpl();
        sharedPref = getSharedPrefInstance(mContext);

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
        if (beaconManager != null)
            beaconManager.unbind(beaconConsumer);
    }

    public void setUpBackgroundMode(boolean isBackgroundMode) {
        if (beaconManager != null && beaconManager.isBound(beaconConsumer))
            beaconManager.setBackgroundMode(isBackgroundMode);
    }

    @NonNull
    private static cidaasbeaconsdk.Entity.BeaconResult setBeacon(Beacon mBeacon, Region region) {

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
            Timber.d("Manager", "setBeacon: " + region.toString());
        }
        return beacon;
    }

    public void setExpirationMilliseconds(long milliseconds) {
        RangedBeacon.setSampleExpirationMilliseconds(milliseconds);
    }

    public void startBeaconMonitoring(final List<CategoryResponse> beaconList) {
        if (SDKEntity.SDKEntityInstance != null && SDKEntity.SDKEntityInstance.getBaseUrl() != null &&
                !SDKEntity.SDKEntityInstance.getBaseUrl().equals("")) {
            if (beaconManager != null && beaconManager.isBound(beaconConsumer)) {
                addMonitoringNotifier(beaconList);
                Timber.d(TAG, "startBeaconMonitoring: is bound");
            } else {
                Timber.d(TAG, "startBeaconMonitoring: not bound");
                setUpConsumer(beaconList);
            }
        } else {
            if (mBeaconEvents != null) {
                ErrorEntity errorEntity = new ErrorEntity();
                errorEntity.setStatus(417);
                errorEntity.setSuccess(false);
                errorEntity.setMessage("No Base URL");
                mBeaconEvents.onError(errorEntity);
            }
        }


    }

    private void setUpConsumer(final List<CategoryResponse> beaconList) {
        beaconConsumer = new BeaconConsumer() {

            @Override
            public void onBeaconServiceConnect() {
                addMonitoringNotifier(beaconList);
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
        assetManager = asset;
        configurationFileName = fileName;
        SDKEntity.SDKEntityInstance.readInputs(assetManager, configurationFileName, mContext);
    }

    public void setURL(String baseURL) {
        SDKEntity.SDKEntityInstance.setBaseUrl(baseURL);

    }


    private void addMonitoringNotifier(List<CategoryResponse> beaconList) {
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
                            BeaconEmitRequest beaconEmitRequest = setUpRequestEntity(firstBeacon, android_id);
                            serviceModel.updateBeacon(beaconEmitRequest, SDKEntity.SDKEntityInstance.getBaseUrl());
                        }
                    }
                }
            });
            for (int i = 0; i < beaconList.size(); i++) {
                Identifier id1 = null;
                if (beaconList.get(i).getUniqueId() != null && beaconList.get(i).getUniqueId().length != 0) {
                    for (int j = 0; j < beaconList.get(i).getUniqueId().length; j++) {
                        id1 = Identifier.parse(beaconList.get(i).getUniqueId()[j]);
                        Region region = new Region(beaconList.get(i).getVendor() + j, id1, null, null);
                        try {
                            beaconManager.startMonitoringBeaconsInRegion(region);
                            beaconManager.startRangingBeaconsInRegion(region);
                        } catch (Exception ex) {
                            Timber.d("startMonitoringBeaconsInRegion" + ex.getMessage());
                        }
                    }


                }

            }
        } catch (Exception ex) {
            Timber.d(ex.getMessage());
        }

    }

    @NonNull
    private BeaconEmitRequest setUpRequestEntity(Beacon firstBeacon, String android_id) {
        BeaconEmitRequest beaconEmitRequest = new BeaconEmitRequest();
        beaconEmitRequest.setDeviceId(android_id);
        beaconEmitRequest.setDistance(String.valueOf(firstBeacon.getDistance()));
        Geo geo = new Geo();
        geo.setLatitude(sharedPref.getLat());
        geo.setLongitude(sharedPref.getLon());
        beaconEmitRequest.setGeo(geo);
        DeviceLocation deviceLocation = new DeviceLocation();
        deviceLocation.setGeo(geo);
        deviceLocation.setMajor(firstBeacon.getId2().toString());
        deviceLocation.setMinor(firstBeacon.getId3().toString());
        deviceLocation.setUniqueId(firstBeacon.getId1().toString());
        beaconEmitRequest.setBeacon(deviceLocation);
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


}
