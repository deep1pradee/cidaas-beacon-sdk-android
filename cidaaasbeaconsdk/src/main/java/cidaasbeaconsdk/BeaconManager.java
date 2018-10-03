package cidaasbeaconsdk;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.annotation.NonNull;

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
import java.util.List;

import cidaasbeaconsdk.Entity.BeaconEntity;
import cidaasbeaconsdk.Entity.Proximity;
import cidaasbeaconsdk.Helper.BeaconHelper;
import timber.log.Timber;


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
    private static cidaasbeaconsdk.Entity.BeaconEntity setBeacon(Beacon mBeacon, Region region) {

        cidaasbeaconsdk.Entity.BeaconEntity beacon = new cidaasbeaconsdk.Entity.BeaconEntity();
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

    public void startBeaconMonitoring(BeaconEntity beaconModel) {
        List<BeaconEntity> beaconModelList = new ArrayList<>();
        beaconModelList.add(beaconModel);
        if (beaconManager != null && beaconManager.isBound(beaconConsumer)) {
            addMonitoringNotifier(beaconModelList);
            Timber.d(TAG, "startBeaconMonitoring: is bound");
        } else {
            Timber.d(TAG, "startBeaconMonitoring: not bound");
            setUpConsumer(beaconModelList);
        }
    }

    public void startBeaconMonitoring(final List<BeaconEntity> beaconList) {
        if (beaconManager != null && beaconManager.isBound(beaconConsumer)) {
            addMonitoringNotifier(beaconList);
            Timber.d(TAG, "startBeaconMonitoring: is bound");
        } else {
            Timber.d(TAG, "startBeaconMonitoring: not bound");
            setUpConsumer(beaconList);
        }


    }

    private void setUpConsumer(final List<BeaconEntity> beaconList) {
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

    private void addMonitoringNotifier(List<BeaconEntity> beaconList) {
        try {
            beaconManager.addMonitorNotifier(new MonitorNotifier() {
                @Override
                public void didEnterRegion(Region region) {
                    if (mBeaconEvents != null) {
                        mBeaconEvents.didEnterRegion(setBeacon(null, region));
                        startRangingBeacons(region);
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
            for (int i = 0; i < beaconList.size(); i++) {
                Identifier id1 = null, id2 = null, id3 = null;
                id1 = Identifier.parse(beaconList.get(i).getUuid());
                if (beaconList.get(i).getMajor() != null)
                    id2 = Identifier.parse(beaconList.get(i).getMajor().toString());
                if (beaconList.get(i).getMinor() != null)
                    id3 = Identifier.parse(beaconList.get(i).getMinor().toString());
                Region region = new Region(beaconList.get(i).getName(), id1, id2
                        , id3);
                try {
                    beaconManager.startMonitoringBeaconsInRegion(region);
                } catch (Exception ex) {
                    Timber.d("startMonitoringBeaconsInRegion" + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            Timber.d(ex.getMessage());
        }

    }

    public void startRangingBeacons(Region region) {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Beacon firstBeacon = beacons.iterator().next();
                    if (mBeaconEvents != null)
                        mBeaconEvents.didBeaconsInRange(setBeacon(firstBeacon, null));
                    Timber.d("didRangeBeaconsInRegion: " + firstBeacon.getBluetoothAddress() + " distance " + firstBeacon.getDistance());
                }
            }
        });
        try {
//new Region(beaconEntity.getName(),Identifier.parse(beaconEntity.getUuid()),Identifier.parse(beaconEntity.getMajor()),Identifier.parse(beaconEntity.getMinor()))
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
        }
    }

    public List<BeaconEntity> getBeaconUUIDs() {
        return beaconHelper.getUUID();
    }


}
