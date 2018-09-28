package widaas.cidaas_beacon_sdk_android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import cidaasbeaconsdk.BeaconEvents;
import cidaasbeaconsdk.BeaconManager;
import cidaasbeaconsdk.Entity.Beacon;
import cidaasbeaconsdk.Entity.BeaconModel;

public class MainActivity extends AppCompatActivity implements BeaconEvents {
    BeaconManager beaconMonitor;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconMonitor = new BeaconManager(this);
        BeaconManager.registerEvents(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
        List<BeaconModel> model = BeaconManager.getBeaconUUIDs();

        if (model != null && model.size() > 0) {
            for (int i = 0; i < model.size(); i++) {
                Log.d("MainActivity", "onCreate: " + model.get(i));

            }
        }
        BeaconManager.startMonitoringBeacons(model);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, yay! Start the Bluetooth device scan.
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                }
            }
        }
    }

    @Override
    public void didEnterRegion(Beacon beacon) {
        Log.d("MainActivity", "didEnterRegion: "+ beacon.getUuid());
        Toast.makeText(this, " didEnterRegion ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didExitRegion(Beacon beacon) {
        Log.d("MainActivity", "didExitRegion: " + beacon.getUuid());
        Toast.makeText(this, " didExitRegion ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didBeaconsInRange(Beacon beacon) {
        Log.d("MainActivity ", "didBeaconsInRange: " + beacon.getUuid() + " minor" + beacon.getMinor());
        Toast.makeText(this, " didBeaconsInRange ", Toast.LENGTH_SHORT).show();
    }
}
