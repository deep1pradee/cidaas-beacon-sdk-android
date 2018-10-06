package cidaasbeaconsdk.Service;


import cidaasbeaconsdk.Entity.BeaconEmitRequest;
import cidaasbeaconsdk.Entity.CategoryResponseEntity;
import cidaasbeaconsdk.Entity.LocationRequest;
import cidaasbeaconsdk.Entity.Result;
import retrofit2.http.Url;

public interface ServiceModel {
/*    Observable<ResponseBody> updateBeacon(BeaconEmitRequest beaconEmitRequest,String url);

    Observable<ResponseBody> updateLocation(DeviceLocation locationEmitRequest, String url);

    @GET
    Observable<CategoryResponseEntity> getDefaultConfig(@Url String url, Result<CategoryResponseEntity> result);*/
    public void getDefaultConfig(@Url String url, Result<CategoryResponseEntity> responseEntityResult);
    public void updateBeacon(String access_token,BeaconEmitRequest beaconEmitRequest, String url);
    public void updateLocation(String access_token, LocationRequest deviceLocation, String url);

}
