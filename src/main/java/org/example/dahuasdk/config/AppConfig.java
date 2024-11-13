package org.example.dahuasdk.config;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    private static OkHttpClient.Builder okHttpClientBuilder(int connectTimeout, int readTimeout, int writeTimeout) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(500);
        dispatcher.setMaxRequestsPerHost(500);

        ConnectionPool connectionPool = new ConnectionPool(100, 5, TimeUnit.MINUTES);

        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .connectionPool(connectionPool);
    }

    @Bean
    public OkHttpClient dahuaHttpClient(DahuaProperties properties) {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Credentials(properties.getUsername(), properties.getPassword()));
        final Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();

        return okHttpClientBuilder(properties.getConnectTimeout(), properties.getReadTimeout(), properties.getWriteTimeout())
                .authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                .addInterceptor(new AuthenticationCacheInterceptor(authCache))
                .build();
    }

    @Bean
    public OkHttpClient vhrHttpClient(VhrProperties properties) {
        return okHttpClientBuilder(properties.getConnectTimeout(), properties.getReadTimeout(), properties.getWriteTimeout())
                .build();
    }
}
