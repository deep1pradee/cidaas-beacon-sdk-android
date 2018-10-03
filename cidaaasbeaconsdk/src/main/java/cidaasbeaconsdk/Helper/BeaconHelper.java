package cidaasbeaconsdk.Helper;

import java.util.ArrayList;
import java.util.List;

import cidaasbeaconsdk.Entity.BeaconEntity;

public class BeaconHelper {

    public List<BeaconEntity> getUUID() {
        List<BeaconEntity> beaconList = new ArrayList<>();
        beaconList.add(new BeaconEntity("ufo1", "4b54504c-5546-4f00-0000-000000000001"));
        beaconList.add(new BeaconEntity("ufo2", "4b54504c-5546-4f00-0000-000000000002"));
        beaconList.add(new BeaconEntity("Estimote", "b9407f30-f5f8-466e-aff9-25556b57fe6d"));
        beaconList.add(new BeaconEntity("RadiusNetwork", "2f234454-cf6d-4a0f-adf2-f4911ba9ffa6"));
        beaconList.add(new BeaconEntity("AirLocate", "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0"));
        beaconList.add(new BeaconEntity("WeChat1", "FDA50693-A4E2-4FB1-AFCF-C6EB07647825"));
        beaconList.add(new BeaconEntity("WeChat2", "AB8190D5-D11E-4941-ACC4-42F30510B408"));
        return beaconList;
    }
}
