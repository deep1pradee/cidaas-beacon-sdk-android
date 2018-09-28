package cidaasbeaconsdk;

import cidaasbeaconsdk.Entity.Beacon;

public interface BeaconEvents {
    public void didEnterRegion(Beacon beacon);

    public void didExitRegion(Beacon beacon);

    public void didBeaconsInRange(Beacon beacon);
}
