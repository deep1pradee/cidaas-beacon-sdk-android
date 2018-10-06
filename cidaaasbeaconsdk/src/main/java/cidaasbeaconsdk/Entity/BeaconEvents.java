package cidaasbeaconsdk.Entity;


public interface BeaconEvents {
    public void didEnterRegion(BeaconResult beacon);

    public void didExitRegion(BeaconResult beacon);

    public void didBeaconsInRange(BeaconResult beacon);

    /* int INSIDE = 1;
        int OUTSIDE = 0;*/
    void didDetermineStateForRegion(int var1, BeaconResult beacon);

    void onError(ErrorEntity errorEntity);
    public void didEnterGeoRegion();
    public void didExitGeoRegion();
}
