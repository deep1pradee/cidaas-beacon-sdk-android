package cidaasbeaconsdk;

import cidaasbeaconsdk.Entity.BeaconEntity;

public interface BeaconEvents {
    public void didEnterRegion(BeaconEntity beacon);

    public void didExitRegion(BeaconEntity beacon);

    public void didBeaconsInRange(BeaconEntity beacon);

    /* int INSIDE = 1;
        int OUTSIDE = 0;*/
    void didDetermineStateForRegion(int var1, BeaconEntity beacon);

}
