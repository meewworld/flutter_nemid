package dk.meew.flutter_nemid.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitHelper {

    private static final int DEFAULT_READ_TIMEOUT_SECONDS = 30;

    private static final int CONNECT_TIME_OUT = 15;

    private static final int WRITE_TIME_OUT = 15;

    private static Map<String, Retrofit> retrofitInstances = new HashMap<>();

    private static NonPersistentCookieJar nonPersistentCookieJar;

    public static Retrofit getRetrofitForBaseUrl(String baseUrl) {
        return getRetrofitForBaseUrl(baseUrl, DEFAULT_READ_TIMEOUT_SECONDS);
    }

    /**
     * Retrieves a shared Retrofit instance for the given <code>baseUrl</code>.
     * The retrofit instance has logging enabled.
     *
     * @param baseUrl           rest base url.
     * @return                  a new Retrofit instance.
     */
    private static Retrofit getRetrofitForBaseUrl(String baseUrl, long readTimeout) {
        Retrofit retrofit = retrofitInstances.get(baseUrl);

        if (retrofit == null) {
            // Setup logging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Build http client
            nonPersistentCookieJar = new NonPersistentCookieJar();
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                    .cookieJar(nonPersistentCookieJar)
                    .addInterceptor(logging)
                    .build();

            // Build shared retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(client)
                    .build();

            retrofitInstances.put(baseUrl, retrofit);
        }

        return retrofit;
    }

    public static void clearCookies() {
        nonPersistentCookieJar.clear();
    }

    /**
     * A simple non-persistent cookie jar for OkHttp / Retrofit.
     * Keeps cookies in memory only.
     */
    private static class NonPersistentCookieJar implements CookieJar {
        private final Set<Cookie> cookieStore = new LinkedHashSet<>();

        @Override
        public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.addAll(cookies);
        }

        @Override
        public synchronized List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> matchingCookies = new ArrayList<>();
            Iterator<Cookie> it = cookieStore.iterator();
            while (it.hasNext()) {
                Cookie cookie = it.next();
                if (cookie.expiresAt() < System.currentTimeMillis()) {
                    it.remove();
                } else if (cookie.matches(url)) {
                    matchingCookies.add(cookie);
                }
            }
            return matchingCookies;
        }

        public void clear() {
            cookieStore.clear();
        }
    }
}
