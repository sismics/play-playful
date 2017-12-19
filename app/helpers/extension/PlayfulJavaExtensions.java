package helpers.extension;

import helpers.locale.LocaleUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.templates.JavaExtensions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author jtremeaux
 */
public class PlayfulJavaExtensions extends JavaExtensions {
    public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String ISO8601_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern(ISO8601);

    public static final DateTimeFormatter DATE_TIME_MS_FORMATTER = DateTimeFormat.forPattern(ISO8601_MS);

    /**
     * Formats a date / time to ISO format (e.g. 2015-01-01T00:00:00Z).
     *
     * @param date Date to format
     * @return Formatted date
     */
    public static String formatDateTimeIsoMs(Date date) {
        if (date == null) {
            return null;
        }

        return new DateTime(date).withZone(DateTimeZone.UTC).toString();
    }

    public static String formatDateTimeIso(Date date) {
        if (date == null) {
            return null;
        }

        return DATE_TIME_FORMATTER.withZone(DateTimeZone.UTC).print(date.getTime());
    }

    public static Date parseDateTimeIso(String value) {
        if (value == null) {
            return null;
        }
        try {
            DateTime date = DATE_TIME_FORMATTER.withZoneUTC().parseDateTime(value);
            return date.toDate();
        } catch (Exception e) {
            return null;
        }
    }

    public static Date parseDateTimeIsoMs(String value) {
        if (value == null) {
            return null;
        }
        try {
            DateTime date = DATE_TIME_MS_FORMATTER.withZoneUTC().parseDateTime(value);
            return date.toDate();
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatDoubleUs(Double value) {
        return String.format(Locale.US, "%.8f", value);
    }

    public static Date truncateSecondOfMinute(Date date) {
        if (date == null) {
            return null;
        }
        return new DateTime(date).withSecondOfMinute(0).toDate();
    }

    public static String formatPrice(BigDecimal price, Locale locale) {
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        format.setRoundingMode(RoundingMode.HALF_EVEN);
        return format.format(price);
    }

    public static String formatWithoutTrailingZero(Double value, Locale locale) {
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(1);
        format.setRoundingMode(RoundingMode.HALF_EVEN);
        return format.format(value);
    }

    public static BigDecimal fromCents(Long cents) {
        return BigDecimal.valueOf(cents)
                .divideToIntegralValue(BigDecimal.valueOf(100));
    }

    /**
     * Convert a timestamp (in seconds) from a GMT date to a local Java date.
     *
     * @param date The date to convert
     * @return The date
     */
    public static Date getDate(Long date) {
        return new LocalDateTime(date * 1000)
                .toDateTime(DateTimeZone.UTC)
                .toDate();
    }

    public static String formatDateLong(Date date, Locale locale) {
        return new DateTime(date).toString(DateTimeFormat.patternForStyle("L-", locale), locale);
    }
    
    public static String formatFileSize(Long value, Locale locale) {
        if (value == null) {
            return "";
        }
        String[] units = {"B", "kB", "MB", "GB", "TB", "PB"};
        int unit = (int) (Math.floor(Math.log(value) / Math.log(1000)));
        return String.format(locale, "%.1f", value / Math.pow(1000, unit)) +  ' ' + units[unit];

    }

    public static String countryName(String countryCode, Locale locale) {
        return LocaleUtil.getCountryName(countryCode, locale);
    }
}
