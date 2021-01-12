package helpers.recaptcha.model;

import java.util.Date;

/**
 * @author jtremeaux
 */
public class RecaptchaVerifyResponse {
    public boolean success;

    public double score;

    public String action;

    public Date challenge_ts;

    public String hostname;
}
