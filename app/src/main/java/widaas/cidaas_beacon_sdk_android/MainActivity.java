package widaas.cidaas_beacon_sdk_android;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
    private String sub="1d693bd7-263d-4dd4-94e0-708fbab2ab7f";
    private String access_token="eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwMjM2ZWZiLWRlMjEtNDI5Mi04ZDRlLTRmZGIxNjhhZDg4ZSJ9.eyJzaWQiOiI2MjM1ZjVkNi1mYTIyLTQxYTctYWRhMS05OGNhZTllZGViMDAiLCJzdWIiOiIxZDY5M2JkNy0yNjNkLTRkZDQtOTRlMC03MDhmYmFiMmFiN2YiLCJpc3ViIjoiMzdjOGM2MzMtYWVkNC00ZmY1LWExMDUtYTNiY2VkYmEyNGE1IiwiYXVkIjoiNGQ1ZTZlMjAtOTM0Ny00MjU1LTk3OTAtNWI3MTk2ODQzMTAzIiwiaWF0IjoxNTM5MDgwNTkyLCJhdXRoX3RpbWUiOjE1MzkwODA1OTIsImlzcyI6Imh0dHBzOi8vbmlnaHRseWJ1aWxkLmNpZGFhcy5kZSIsImp0aSI6IjA4NjkzN2I3LTIwZDAtNGJlYS04NzViLTIxZGJlYTY0M2U2MSIsIm5vbmNlIjoiMTIzNDUiLCJzY29wZXMiOlsib3BlbmlkIl0sInJvbGVzIjpbIlVTRVIiXSwiZ3JvdXBzIjpbeyJncm91cElkIjoiQ0lEQUFTX0FETUlOUyIsInJvbGVzIjpbIlNFQ09OREFSWV9BRE1JTiJdfV0sImV4cCI6MTUzOTE2Njk5MiwiZW1haWwiOiJzdXByYWRhcmFvQGdtYWlsLmNvbSIsImdpdmVuX25hbWUiOiJza3JhbyJ9.jp1CVhbKcsJZ1eU2dLhGc_lHJHaHPZypuJzkoG8kNKP2pNZp1mCTFSbSF2CXMDF0m1w0ur4KCWu2_cxmSPTI46fMpaAynA1JTyo5AZBJy4iKRomITu3bKQA-eDCZcrlGnjjgUp9_C_S5emFlFxG8Bc2HbgHtCesw78SCsWa74Dg";
    private static final int REQUEST = 112;

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
                beaconMonitor.startBeaconMonitoringAndRanging(model,sub,access_token);
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
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
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
                    if (Build.VERSION.SDK_INT >= 23) {
                        String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        if (!hasPermissions(this, PERMISSIONS)) {
                            ActivityCompat.requestPermissions((Activity) this, PERMISSIONS, REQUEST );
                        }
                    }
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                    if (Build.VERSION.SDK_INT >= 23) {
                        String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        if (!hasPermissions(this, PERMISSIONS)) {
                            ActivityCompat.requestPermissions((Activity) this, PERMISSIONS, REQUEST );
                        }
                    }
                }
            }
            break;
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                } else {
                    Toast.makeText(this, "The app was not allowed to write in your storage", Toast.LENGTH_LONG).show();
                }
            }
            break;
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
        //sendNotification("Enter " + beacon.getUuid() + " major " + beacon.getMajor() + " minor " + beacon.getMinor());
        txtview.setText("didEnterRegion");
        Log.d("MainActivity", "didEnterRegion: " + beacon.getUuid() + " major " + beacon.getMajor() + " minor " + beacon.getMinor());
      //  Toast.makeText(this, " didEnterRegion ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didExitRegion(BeaconResult beacon) {
       // sendNotification("Exit " + beacon.getUuid() + " major " + beacon.getMajor() + " minor " + beacon.getMinor());
        txtview.setText("didExitRegion");
        Log.d("MainActivity", "didExitRegion: " + beacon.getUuid() + " major " + beacon.getMajor() + " minor " + beacon.getMinor());
      //  Toast.makeText(this, " didExitRegion ", Toast.LENGTH_SHORT).show();
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
      //  Toast.makeText(this, "Beacon state changed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(ErrorEntity errorEntity) {
        //txtview.setText(errorEntity.getMessage());
        Log.d("MAinActivity", "onError: "+errorEntity.getMessage());
        Toast.makeText(this, "errorEntity " + errorEntity.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didEnterGeoRegion() {
        sendNotification("Enter geo region");
//        txtview.setText("Enter geo region ");
        Toast.makeText(this, "Enter geo region ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didExitGeoRegion() {
        sendNotification("Exited geo region");
       // txtview.setText("Exited geo region");
        Toast.makeText(this, "Exited geo region ", Toast.LENGTH_SHORT).show();

    }

}
