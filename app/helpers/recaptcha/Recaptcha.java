package helpers.recaptcha;

import com.google.gson.Gson;
import com.sismics.sapparot.function.CheckedConsumer;
import com.sismics.sapparot.function.CheckedFunction;
import com.sismics.sapparot.okhttp.OkHttpHelper;
import helpers.recaptcha.model.RecaptchaVerifyResponse;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import play.Play;

/**
 * @author jtremeaux
 */
public class Recaptcha {
    private static Recaptcha instance;

    private OkHttpClient client;

    public static Recaptcha get() {
        if (instance == null) {
            instance = new Recaptcha();
        }
        return instance;
    }

    public Recaptcha() {
        client = createClient();
    }

    public boolean verifyAndValidate(String response) {
        RecaptchaVerifyResponse verifyResponse = verify(response);
        return verifyResponse.success;
    }

    public RecaptchaVerifyResponse verify(String response) {
        if (isMock()) {
            RecaptchaVerifyResponse verifyResponse = new RecaptchaVerifyResponse();
            verifyResponse.success = true;
            return verifyResponse;
        }
        Request request = new Request.Builder()
                .url("https://www.google.com/recaptcha/api/siteverify")
                .post((new FormBody.Builder()
                        .add("secret", getSecretKey())
                        .add("response", response)
                        .build()))
                .build();
        return execute(request, (r) -> new Gson().fromJson(r.body().string(), RecaptchaVerifyResponse.class),
        (r) -> {
            throw new RuntimeException("Error getting recaptcha response, response was: " + r.body().string());
        });
    }

    private boolean isMock() {
        return Boolean.parseBoolean(Play.configuration.getProperty("recaptcha.mock", "false"));
    }

    private static OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    public String getSecretKey() {
        return Play.configuration.getProperty("recaptcha.secret_key");
    }

    public <T> T execute(Request request, CheckedFunction<Response, T> onSuccess, CheckedConsumer<Response> onFailure) {
        return OkHttpHelper.execute(client, request, onSuccess, onFailure);
    }
}
