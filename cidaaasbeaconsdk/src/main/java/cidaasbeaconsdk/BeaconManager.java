package cidaasbeaconsdk;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Collection;
import java.util.List;

import cidaasbeaconsdk.Entity.BeaconModel;
import cidaasbeaconsdk.Entity.Proximity;
import cidaasbeaconsdk.Helper.BeaconHelper;


public class BeaconManager {
    static Context mContext;
    static BeaconHelper beaconHelper;
    String TAG = "main";
    static BeaconEvents mBeaconEvents;
    static org.altbeacon.beacon.BeaconManager beaconManager;
    static BeaconConsumer beaconConsumer;
    static Intent serviceIntent;

    public void registerEvents(BeaconEvents beaconEvents) {
        mBeaconEvents = beaconEvents;
    }

    public BeaconManager(Context context) {
        mContext = context;
        beaconHelper = new BeaconHelper();
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(mContext);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));


    }

    private void setUpBootStrapNotifier(Region region) {
        BootstrapNotifier bootstrapNotifier = new BootstrapNotifier() {
            @Override
            public Context getApplicationContext() {
                return mContext;
            }

            @Override
            public void didEnterRegion(Region region) {
                if (mBeaconEvents != null)
                    mBeaconEvents.didEnterRegion(setBeacon(null, region));
            }

            @Override
            public void didExitRegion(Region region) {
                if (mBeaconEvents != null)
                    mBeaconEvents.didExitRegion(setBeacon(null, region));
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.d("mainactivity", "didDetermineStateForRegion: ");
            }
        };
        RegionBootstrap regionBootstrap = new RegionBootstrap(bootstrapNotifier, region);
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

    private void bindService(final Region region) {
        beaconConsumer = new BeaconConsumer() {
            @Override
            public void onBeaconServiceConnect() {
                beaconManager.addRangeNotifier(new RangeNotifier() {
                    @Override
                    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                        if (beacons.size() > 0) {

                            Beacon firstBeacon = beacons.iterator().next();
                            if (mBeaconEvents != null)
                                mBeaconEvents.didBeaconsInRange(setBeacon(firstBeacon, null));
                            Log.d("beacon", "didRangeBeaconsInRegion: " + firstBeacon.getBluetoothAddress() + " distance " + firstBeacon.getDistance());
                        }
                    }
                });
                try {

                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                }
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

    public void unbind() {
        if (beaconManager != null)
            beaconManager.unbind(beaconConsumer);
    }

    public void setUpBackgroundMode(boolean isBackgroundMode) {
        if (beaconManager != null && beaconManager.isBound(beaconConsumer))
            beaconManager.setBackgroundMode(isBackgroundMode);
    }

    @NonNull
    private static cidaasbeaconsdk.Entity.Beacon setBeacon(Beacon mBeacon, Region region) {

        cidaasbeaconsdk.Entity.Beacon beacon = new cidaasbeaconsdk.Entity.Beacon();
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
        } else if (region != null) {
            Log.d("Manager", "setBeacon: " + region.toString());
        }
        return beacon;
    }

    public void setExpirationMilliseconds(long milliseconds) {
        RangedBeacon.setSampleExpirationMilliseconds(milliseconds);
    }

    public void startBeaconMonitoring(final List<BeaconModel> beaconList) {
        beaconConsumer = new BeaconConsumer() {

            @Override
            public void onBeaconServiceConnect() {
                beaconManager.addMonitorNotifier(new MonitorNotifier() {
                    @Override
                    public void didEnterRegion(Region region) {
                        if (mBeaconEvents != null)
                            mBeaconEvents.didEnterRegion(setBeacon(null, region));
                    }

                    @Override
                    public void didExitRegion(Region region) {
                        if (mBeaconEvents != null)
                            mBeaconEvents.didExitRegion(setBeacon(null, region));
                    }

                    @Override
                    public void didDetermineStateForRegion(int i, Region region) {
                        if (mBeaconEvents != null)
                            mBeaconEvents.didExitRegion(setBeacon(null, region));
                    }
                });
                for (int i = 0; i < beaconList.size(); i++) {
                    Region region = new Region(beaconList.get(i).getName(),
                            Identifier.parse(beaconList.get(i).getUuid()), null, null);
                    try {
                        beaconManager.startMonitoringBeaconsInRegion(region);
                    } catch (Exception ex) {

                    }
                }
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

    }

    public List<BeaconModel> getBeaconUUIDs() {
        return beaconHelper.getUUID();
    }

    public void startBeaconRanging(List<BeaconModel> beaconList) {
        for (int i = 0; i < beaconList.size(); i++) {
            Region region = new Region(beaconList.get(i).getName(),
                    Identifier.parse(beaconList.get(i).getUuid()), null, null);
            bindService(region);
        }
    }
}
