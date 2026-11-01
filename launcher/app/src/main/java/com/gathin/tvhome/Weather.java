package com.gathin.tvhome;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Current weather from Open-Meteo (free, no key), formatted for the home screen: "🌧 Rain · 61°F / 16°C".
 * Pure Java (no android.*) so JUnit runs it on the JVM; MainActivity does the network call.
 */
final class Weather {

    /** Refresh at most this often. */
    static final long REFRESH_MS = 30 * 60_000L;

    final double tempC;
    final int code;
    final boolean day;

    Weather(double tempC, int code, boolean day) {
        this.tempC = tempC;
        this.code = code;
        this.day = day;
    }

    /** Empty when no location is configured (weather stays hidden). */
    static String url(String lat, String lon) {
        if (lat == null || lon == null || lat.trim().isEmpty() || lon.trim().isEmpty()) return "";
        return "https://api.open-meteo.com/v1/forecast?latitude=" + lat.trim() + "&longitude=" + lon.trim()
                + "&current=temperature_2m,weather_code,is_day";
    }

    // The "current" object, then each field inside it ("current_units" holds strings, so it never matches a number).
    private static final Pattern CURRENT = Pattern.compile("\"current\"\\s*:\\s*\\{([^}]*)\\}");

    /** Parses Open-Meteo's response; null when the current values are missing. */
    static Weather parse(String json) {
        if (json == null) return null;
        Matcher m = CURRENT.matcher(json);
        if (!m.find()) return null;
        String cur = m.group(1);
        Double temp = number(cur, "temperature_2m");
        Double code = number(cur, "weather_code");
        if (temp == null || code == null) return null;
        Double isDay = number(cur, "is_day");
        return new Weather(temp, code.intValue(), isDay == null || isDay != 0);
    }

    private static Double number(String obj, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(obj);
        return m.find() ? Double.valueOf(m.group(1)) : null;
    }

    /** WMO weather code → word (https://open-meteo.com/en/docs, "WMO Weather interpretation codes"). */
    static String label(int code, boolean day) {
        if (code == 0) return day ? "Sunny" : "Clear";
        if (code <= 2) return "Partly cloudy";
        if (code == 3) return "Cloudy";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if ((code >= 61 && code <= 67) || (code >= 80 && code <= 82)) return "Rain";
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) return "Snow";
        if (code >= 95 && code <= 99) return "Storm";
        return "";
    }

    static String icon(int code, boolean day) {
        if (code == 0) return day ? "☀️" : "🌙";          // ☀️ / 🌙
        if (code <= 2) return day ? "⛅" : "☁️";               // ⛅ / ☁️
        if (code == 3) return "☁️";                                // ☁️
        if (code == 45 || code == 48) return "🌫️";           // 🌫️
        if (code >= 51 && code <= 67 || code >= 80 && code <= 82) return "🌧️"; // 🌧️
        if (code >= 71 && code <= 77 || code == 85 || code == 86) return "❄️";      // ❄️
        if (code >= 95 && code <= 99) return "⛈️";                 // ⛈️
        return "";
    }

    /** "🌧️ Rain · 61°F / 16°C"; unknown codes show only the temperature. */
    String text() {
        long f = Math.round(tempC * 9 / 5 + 32);
        long c = Math.round(tempC);
        String temps = String.format(Locale.US, "%d°F / %d°C", f, c);
        String word = label(code, day);
        return word.isEmpty() ? temps : icon(code, day) + " " + word + " · " + temps;
    }
}
