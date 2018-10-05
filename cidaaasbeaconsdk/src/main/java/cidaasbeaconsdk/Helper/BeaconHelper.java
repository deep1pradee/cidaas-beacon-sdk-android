package cidaasbeaconsdk.Helper;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cidaasbeaconsdk.Entity.BeaconEntity;
import cidaasbeaconsdk.Entity.CategoryResponse;
import cidaasbeaconsdk.Entity.CategoryResponseEntity;

public class BeaconHelper {

    private String baseURL;


    public static CategoryResponseEntity getUUID() {

        CategoryResponseEntity categoryResponseEntity = new CategoryResponseEntity();
        List<CategoryResponse> categoryResponseList = new ArrayList<>();
        CategoryResponse categoryResponse;
        try {
            for (int i = 0; i < getBeaconEntities().size(); i++) {
                categoryResponse = new CategoryResponse();
                categoryResponse.setUniqueId(getBeaconEntities().get(0).getUuid());
                categoryResponse.setVendor(getBeaconEntities().get(0).getName());
                categoryResponseList.add(categoryResponse);
            }
            categoryResponseEntity.setData(categoryResponseList);
        } catch (Exception ex) {
            return categoryResponseEntity;
        }


        return categoryResponseEntity;
    }

    @NonNull
    private static List<BeaconEntity> getBeaconEntities() {
        List<BeaconEntity> beaconList = new ArrayList<>();
        beaconList.add(new BeaconEntity("ufo", new String[]{"4b54504c-5546-4f00-0000-000000000001", "4b54504c-5546-4f00-0000-000000000002"}));
        beaconList.add(new BeaconEntity("Estimote", new String[]{"b9407f30-f5f8-466e-aff9-25556b57fe6d"}));
        beaconList.add(new BeaconEntity("RadiusNetwork", new String[]{"2f234454-cf6d-4a0f-adf2-f4911ba9ffa6"}));
        beaconList.add(new BeaconEntity("AirLocate", new String[]{"E2C56DB5-DFFB-48D2-B060-D0F5A71096E0"}));
        beaconList.add(new BeaconEntity("WeChat1", new String[]{"FDA50693-A4E2-4FB1-AFCF-C6EB07647825"}));
        beaconList.add(new BeaconEntity("WeChat2", new String[]{"AB8190D5-D11E-4941-ACC4-42F30510B408"}));
        return beaconList;
    }

    public static final String BEACON_EMIT_SERVICE = "/access-control-srv/notification/beaconemit";
    public static final String LOCATION_EMIT_SERVICE = "/access-control-srv/notification/locationchange";
    public static final String DEFAULT_CONFIG_SERVICE = "/access-control-srv/devices/beacons/configs";
    public static final String CONTENT_TYPE_JSON = "application/json";

}
