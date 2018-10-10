package cidaasbeaconsdk.Service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cidaasbeaconsdk.Entity.BeaconEmitRequest;
import cidaasbeaconsdk.Entity.CategoryResponseEntity;
import cidaasbeaconsdk.Entity.ErrorEntity;
import cidaasbeaconsdk.Entity.LOcationCordinates;
import cidaasbeaconsdk.Entity.LocationRequest;
import cidaasbeaconsdk.Entity.ProximityListReponse;
import cidaasbeaconsdk.Entity.ProximityListRequest;
import cidaasbeaconsdk.Entity.Result;
import cidaasbeaconsdk.Helper.BeaconHelper;
import cidaasbeaconsdk.Helper.Logger;
import cidaasbeaconsdk.SDKEntity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cidaasbeaconsdk.Helper.BeaconHelper.BEACON_EMIT_SERVICE;
import static cidaasbeaconsdk.Helper.BeaconHelper.CONTENT_TYPE_JSON;
import static cidaasbeaconsdk.Helper.BeaconHelper.LOCATION_EMIT_SERVICE;
import static cidaasbeaconsdk.Helper.BeaconHelper.LOCATION_LIST_SERVICE;

public class ServiceModelImpl implements ServiceModel {
    Logger logger;

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
                                logger.addRecordToLog("getDefaultConfig " + response.isSuccessful());
                                responseEntityResult.onSuccess(response.body());
                            } else {
                                logger.addRecordToLog("getDefaultConfig " + response.isSuccessful() + " code " + response.code());
                                responseEntityResult.onSuccess(BeaconHelper.getUUID());
                            }
                        }

                        @Override
                        public void onFailure(Call<CategoryResponseEntity> call, Throwable t) {
                            responseEntityResult.onSuccess(BeaconHelper.getUUID());
                            logger.addRecordToLog("getDefaultConfig " + t.getMessage());
                        }
                    });
        } catch (Exception ex) {
            logger.addRecordToLog(ex.getMessage());
            ErrorEntity errorEntity = new ErrorEntity();
            errorEntity.setStatus(500);
            errorEntity.setSuccess(false);
            errorEntity.setMessage(ex.getMessage());
            responseEntityResult.onError(errorEntity);
        }

    }

    @Override
    public void updateBeacon(String access_token, BeaconEmitRequest beacon, String url) {

        Services services = new Services();
        IService iService = services.createClient(url);
        iService.beaconEmit(url + BEACON_EMIT_SERVICE, CONTENT_TYPE_JSON, access_token, beacon).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    logger.addRecordToLog(response.isSuccessful() + " updateBeacon Response ");
                }
                // responseBodyResult.onSuccess(response.body());
                else {
                    logger.addRecordToLog(response.code() + "updateBeacon Response failed code");
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
                logger.addRecordToLog(t.getMessage() + " updateBeacon Response failed code");
                // responseBodyResult.onError(errorEntity);
            }
        });
    }

    @Override
    public void updateLocation(String access_token, LocationRequest deviceLocation, String url) {
        Services services = new Services();
        IService iService = services.createClient(url);
        if(deviceLocation!=null&&deviceLocation.getSessionId()!=null&&!deviceLocation.getSessionId().equalsIgnoreCase(""))
        {
            try {
                logger.addRecordToLog("Location update REquest :"+new ObjectMapper().writeValueAsString(deviceLocation));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            iService.locationEmit(url + LOCATION_EMIT_SERVICE, CONTENT_TYPE_JSON, access_token, deviceLocation)
                    .enqueue(new Callback<ResponseBody>() {
                                 @Override
                                 public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                     if (response.isSuccessful())
                                         logger.addRecordToLog(response.isSuccessful() + " update location REsponse");
                                     else
                                         logger.addRecordToLog(response.code() + " update location failed");
                                 }

                                 @Override
                                 public void onFailure(Call<ResponseBody> call, Throwable t) {
                                     logger.addRecordToLog(t.getMessage() + "update location REsponse");
                                 }
                             }
                    );
        }
        else
        {
            logger.addRecordToLog("No session id");
        }

    }

    @Override
    public void getProximityList(String access_token, ProximityListRequest proximityListRequest, String url, final Result<LOcationCordinates> responseEntityResult) {
        Services services = new Services();
        IService iService = services.createClient(url);
        iService.getProximitylist(url + LOCATION_LIST_SERVICE, CONTENT_TYPE_JSON, access_token, proximityListRequest)
                .enqueue(new Callback<ProximityListReponse>() {
                    @Override
                    public void onResponse(Call<ProximityListReponse> call, Response<ProximityListReponse> response) {
                        if (response.isSuccessful()) {
                            logger.addRecordToLog(response.isSuccessful() + " getProximityList REsponse");
                            if (response.body() != null && response.body().getData() != null)
                                responseEntityResult.onSuccess(response.body().getData());
                        }
                    }

                    @Override
                    public void onFailure(Call<ProximityListReponse> call, Throwable t) {
                        ErrorEntity errorEntity = new ErrorEntity();
                        errorEntity.setStatus(500);
                        errorEntity.setSuccess(false);
                        errorEntity.setMessage("");
                        responseEntityResult.onError(errorEntity);
                        logger.addRecordToLog(t.getMessage() + " getProximityList REsponse");

                    }
                });


    }

}
