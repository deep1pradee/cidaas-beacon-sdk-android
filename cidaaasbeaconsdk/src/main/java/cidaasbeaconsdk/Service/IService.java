package cidaasbeaconsdk.Service;

import cidaasbeaconsdk.Entity.BeaconEmitRequest;
import cidaasbeaconsdk.Entity.CategoryResponseEntity;
import cidaasbeaconsdk.Entity.LocationRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface IService {
    @POST
    Call<ResponseBody> beaconEmit(@Url String url,
                                  @Header("Content-Type") String content_type,
                                  @Header("access_token") String acess_token,
                                  @Body BeaconEmitRequest request);

    @POST
    Call<ResponseBody> locationEmit(@Url String url,
                                    @Header("Content-Type") String content_type,
                                    @Header("access_token") String acess_token,
                                    @Body LocationRequest request);

    @GET
    Call<CategoryResponseEntity> getDefaultConfig(@Url String url);
}
