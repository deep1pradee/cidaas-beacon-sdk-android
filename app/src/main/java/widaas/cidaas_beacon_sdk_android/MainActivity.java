package widaas.cidaas_beacon_sdk_android;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cidaasbeaconsdk.BeaconSDK;
import cidaasbeaconsdk.Entity.BeaconEvents;
import cidaasbeaconsdk.Entity.BeaconResult;
import cidaasbeaconsdk.Entity.CategoryResponse;
import cidaasbeaconsdk.Entity.CategoryResponseEntity;
import cidaasbeaconsdk.Entity.ErrorEntity;
import cidaasbeaconsdk.Entity.Result;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements BeaconEvents {
    BeaconSDK beaconMonitor;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    TextView txtview;
    private List<CategoryResponse> model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtview = (TextView) findViewById(R.id.txtview);
        beaconMonitor = BeaconSDK.getInstance(this);
        beaconMonitor.registerEvents(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
        beaconMonitor.setURLFile(getAssets(), "properties.xml");
        beaconMonitor.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");

        beaconMonitor.getBeaconUUIDs(new Result<CategoryResponseEntity>() {
            @Override
            public void onSuccess(CategoryResponseEntity result) {
                model=result.getData();
                beaconMonitor.startBeaconMonitoring(model);

            }

            @Override
            public void onError(ErrorEntity errorEntity) {
                Toast.makeText(MainActivity.this, errorEntity.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        if (model != null && model.size() > 0) {
            for (int i = 0; i < model.size(); i++) {
                Timber.d("MainActivity", "onCreate: " + model.get(i));

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconMonitor.unbind();
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconMonitor.setUpBackgroundMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconMonitor.setUpBackgroundMode(false);
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

    private void sendNotification(String message) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Beacon Reference Application")
                        .setContentText(message)
                        .setSmallIcon(R.mipmap.ic_launcher);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void didEnterRegion(BeaconResult beacon) {
        sendNotification("Enter " + beacon.getUuid() + " major " + beacon.getMajor() + " minor " + beacon.getMinor());
        txtview.setText("didEnterRegion");
        Log.d("MainActivity", "didEnterRegion: " + beacon.getUuid() + " major " + beacon.getMajor() + " minor " + beacon.getMinor());
        Toast.makeText(this, " didEnterRegion ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didExitRegion(BeaconResult beacon) {
        sendNotification("Exit " + beacon.getUuid() + " major " + beacon.getMajor() + " minor " + beacon.getMinor());
        txtview.setText("didExitRegion");
        Log.d("MainActivity", "didExitRegion: " + beacon.getUuid() + " major " + beacon.getMajor() + " minor " + beacon.getMinor());
        Toast.makeText(this, " didExitRegion ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didBeaconsInRange(BeaconResult beacon) {
        //  txtview.setText("didBeaconsInRange");
        Log.d("MainActivity ", "didBeaconsInRange: " + beacon.getUuid() + " major " + beacon.getMajor() + " minor " + beacon.getMinor() + " distance " + beacon.getDistance());
        // beaconMonitor.startBeaconMonitoring(beacon);
        //  Toast.makeText(this, " didBeaconsInRange ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didDetermineStateForRegion(int var1, BeaconResult var2) {
        txtview.setText("didDetermineStateForRegion");
        Log.d("MAinActivity", "didDetermineStateForRegion: ");
        Toast.makeText(this, "Beacon state changed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(ErrorEntity errorEntity) {
        txtview.setText(errorEntity.getMessage());
        Log.d("MAinActivity", "onError: ");
        Toast.makeText(this, "errorEntity " + errorEntity.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
