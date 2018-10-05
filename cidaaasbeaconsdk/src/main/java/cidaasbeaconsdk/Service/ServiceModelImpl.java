package cidaasbeaconsdk.Service;


import cidaasbeaconsdk.Entity.BeaconEmitRequest;
import cidaasbeaconsdk.Entity.CategoryResponseEntity;
import cidaasbeaconsdk.Entity.ErrorEntity;
import cidaasbeaconsdk.Entity.Result;
import cidaasbeaconsdk.Helper.BeaconHelper;
import cidaasbeaconsdk.SDKEntity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static cidaasbeaconsdk.Helper.BeaconHelper.BEACON_EMIT_SERVICE;
import static cidaasbeaconsdk.Helper.BeaconHelper.CONTENT_TYPE_JSON;

public class ServiceModelImpl implements ServiceModel {
    @Override
    public void getDefaultConfig(String url, final Result<CategoryResponseEntity> responseEntityResult) {
        try {
            Services services = new Services();
            IService iService = services.createClient(SDKEntity.SDKEntityInstance.getBaseUrl());
            iService.getDefaultConfig(SDKEntity.SDKEntityInstance.getBaseUrl() + BeaconHelper.DEFAULT_CONFIG_SERVICE)
                    .enqueue(new Callback<CategoryResponseEntity>() {
                        @Override
                        public void onResponse(Call<CategoryResponseEntity> call, Response<CategoryResponseEntity> response) {
                            if (response.isSuccessful()) {
                                responseEntityResult.onSuccess(response.body());
                            } else {
                                responseEntityResult.onSuccess(BeaconHelper.getUUID());
                            }
                        }

                        @Override
                        public void onFailure(Call<CategoryResponseEntity> call, Throwable t) {
                            responseEntityResult.onSuccess(BeaconHelper.getUUID());
                        }
                    });
        } catch (Exception ex) {
            ErrorEntity errorEntity = new ErrorEntity();
            errorEntity.setStatus(500);
            errorEntity.setSuccess(false);
            errorEntity.setMessage(ex.getMessage());
            responseEntityResult.onError(errorEntity);
        }

    }

    @Override
    public void updateBeacon(String access_token,BeaconEmitRequest beacon, String url) {

        Services services = new Services();
        IService iService = services.createClient(url);
        iService.beaconEmit(url + BEACON_EMIT_SERVICE, CONTENT_TYPE_JSON,access_token, beacon).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Timber.d(response.isSuccessful() + " Response ");
                }
                // responseBodyResult.onSuccess(response.body());
                else {
                    Timber.d(response.code() + " Response failed code");
                    ErrorEntity errorEntity = new ErrorEntity();
                    errorEntity.setStatus(500);
                    errorEntity.setSuccess(false);
                    errorEntity.setMessage("");
                    // responseBodyResult.onError(errorEntity);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ErrorEntity errorEntity = new ErrorEntity();
                errorEntity.setStatus(500);
                errorEntity.setSuccess(false);
                errorEntity.setMessage(t.getMessage());
                Timber.d(t.getMessage() + " Response failed code");
                // responseBodyResult.onError(errorEntity);
            }
        });
    }
   /* @Override
    public Observable<ResponseBody> updateBeacon(final BeaconEmitRequest beaconEmitRequest, final String url) {
        Callable<ResponseBody> callable = new Callable<ResponseBody>() {
            @Override
            public ResponseBody call() throws Exception {

                return null;
            }
        };
        return makeObservable(callable).subscribeOn(Schedulers.computation());
    }

    @Override
    public Observable<ResponseBody> updateLocation(final DeviceLocation locationEmitRequest, final String url) {
        Callable<ResponseBody> callable = new Callable<ResponseBody>() {
            @Override
            public ResponseBody call() throws Exception {
                Services services = new Services();
                IService iService = services.createClient(url);
                iService.locationEmit(url + LOCATION_EMIT_SERVICE, CONTENT_TYPE_JSON, locationEmitRequest).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
                return null;
            }
        };
        return makeObservable(callable).subscribeOn(Schedulers.computation());
    }

    @Override
    public Observable<CategoryResponseEntity> getDefaultConfig(final String url, final Result<CategoryResponseEntity> result) {
       *//* final Callable<CategoryResponseEntity> callable = new Callable<CategoryResponseEntity>() {
            @Override
            public CategoryResponseEntity call() throws Exception {*//*
                Services services = new Services();
                IService iService = services.createClient(url);
                iService.getDefaultConfig(url+DEFAULT_CONFIG_SERVICE).enqueue(new Callback<CategoryResponseEntity>() {
                    @Override
                    public void onResponse(Call<CategoryResponseEntity> call, Response<CategoryResponseEntity> response) {
                        if (response.isSuccessful()) {
                            result.onSuccess(response.body());
                        }
                        else
                        {
                            ErrorEntity errorEntity = new ErrorEntity();
                            errorEntity.setStatus(response.code());
                            errorEntity.setSuccess(false);
                            errorEntity.setMessage(response.errorBody().byteStream().toString());
                            result.onError(errorEntity);
                        }
                    }

                    @Override
                    public void onFailure(Call<CategoryResponseEntity> call, Throwable t) {
                        ErrorEntity errorEntity = new ErrorEntity();
                        errorEntity.setStatus(500);
                        errorEntity.setSuccess(false);
                        errorEntity.setMessage(t.getMessage());
                        result.onError(errorEntity);
                    }
                });
                return null;
           *//* }
        };
        return makeObservable(callable).subscribeOn(Schedulers.computation());*//*
    }

    @Override
    public Observable<CategoryResponseEntity> getDefaultConfig(String url) {
        return null;
    }*/
}
