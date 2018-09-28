package cidaasbeaconsdk.Helper;

import java.util.ArrayList;
import java.util.List;

import cidaasbeaconsdk.Entity.BeaconModel;

public class BeaconHelper {

    public List<BeaconModel> getUUID() {
        List<BeaconModel> beaconList = new ArrayList<>();
        beaconList.add(new BeaconModel("ufo1", "4b54504c-5546-4f00-0000-000000000001"));
        beaconList.add(new BeaconModel("ufo2", "4b54504c-5546-4f00-0000-000000000002"));
        beaconList.add(new BeaconModel("Estimote", "b9407f30-f5f8-466e-aff9-25556b57fe6d"));
        beaconList.add(new BeaconModel("RadiusNetwork", "2f234454-cf6d-4a0f-adf2-f4911ba9ffa6"));
        beaconList.add(new BeaconModel("AirLocate", "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0"));
        beaconList.add(new BeaconModel("WeChat1", "FDA50693-A4E2-4FB1-AFCF-C6EB07647825"));
        beaconList.add(new BeaconModel("WeChat2", "AB8190D5-D11E-4941-ACC4-42F30510B408"));
        beaconList.add(new BeaconModel("UUID", "74278BDA-B644-4520-8F0C-720EAF059935"));
        return beaconList;
    }
}
