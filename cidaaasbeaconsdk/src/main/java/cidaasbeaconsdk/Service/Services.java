package cidaasbeaconsdk.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cidaasbeaconsdk.BeaconSDK;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import timber.log.Timber;

public class Services  {
    String HEADER_USER_AGENT = "User-Agent";

    public IService createClient(String baseURL) {
        OkHttpClient okHttpClient = null;
        IService authService = null;
        final String HEADER_USER_AGENT = "User-Agent";
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(100, TimeUnit.SECONDS)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        Request requestWithUserAgent = originalRequest.newBuilder()
                                .header(HEADER_USER_AGENT, BeaconSDK.createCustomUserAgent(originalRequest))
                                .build();
                        for (int i = 0; i < requestWithUserAgent.headers().size(); i++) {
                            Timber.d("User-Agent : "+String.format("%s: %s", requestWithUserAgent.headers().name(i), requestWithUserAgent.headers().value(i)));
                        }

                        return chain.proceed(requestWithUserAgent);
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(okHttpClient)
                .build();
        authService = retrofit.create(IService.class);
        return authService;
    }

}
