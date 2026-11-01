package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WeatherTest {

    private static final String SAMPLE = "{\"latitude\":51.51,\"longitude\":-0.13,"
            + "\"current_units\":{\"time\":\"iso8601\",\"temperature_2m\":\"°C\",\"weather_code\":\"wmo code\",\"is_day\":\"\"},"
            + "\"current\":{\"time\":\"2026-11-01T05:00\",\"interval\":900,\"temperature_2m\":16.2,\"weather_code\":61,\"is_day\":0}}";

    @Test
    public void parsesCurrentValues() {
        Weather w = Weather.parse(SAMPLE);
        assertEquals(16.2, w.tempC, 1e-9);
        assertEquals(61, w.code);
        assertFalse(w.day);
    }

    @Test
    public void negativeTemperatureAndMissingIsDay() {
        Weather w = Weather.parse("{\"current\":{\"temperature_2m\":-3.5,\"weather_code\":71}}");
        assertEquals(-3.5, w.tempC, 1e-9);
        assertTrue(w.day);
    }

    @Test
    public void missingOrGarbledGivesNull() {
        assertNull(Weather.parse(null));
        assertNull(Weather.parse(""));
        assertNull(Weather.parse("<html>503</html>"));
        assertNull(Weather.parse("{\"current\":{\"weather_code\":3}}"));
        assertNull(Weather.parse("{\"current_units\":{\"temperature_2m\":\"°C\",\"weather_code\":\"wmo code\"}}"));
    }

    @Test
    public void formatsBothUnits() {
        assertEquals("🌧️ Rain · 61°F / 16°C", new Weather(16.2, 61, false).text());
        assertEquals("☀️ Sunny · 68°F / 20°C", new Weather(20.0, 0, true).text());
        assertEquals("❄️ Snow · 26°F / -3°C", new Weather(-3.4, 73, true).text());
    }

    @Test
    public void unknownCodeShowsTemperatureOnly() {
        assertEquals("50°F / 10°C", new Weather(10.0, 42, true).text());
    }

    @Test
    public void labels() {
        assertEquals("Clear", Weather.label(0, false));
        assertEquals("Partly cloudy", Weather.label(2, true));
        assertEquals("Cloudy", Weather.label(3, true));
        assertEquals("Fog", Weather.label(48, true));
        assertEquals("Drizzle", Weather.label(55, true));
        assertEquals("Rain", Weather.label(81, true));
        assertEquals("Snow", Weather.label(86, true));
        assertEquals("Storm", Weather.label(95, true));
        assertEquals("", Weather.label(100, true));
    }

    @Test
    public void urlNeedsLocation() {
        assertEquals("", Weather.url("", "-0.13"));
        assertEquals("", Weather.url(null, null));
        assertEquals("https://api.open-meteo.com/v1/forecast?latitude=51.51&longitude=-0.13"
                + "&current=temperature_2m,weather_code,is_day", Weather.url(" 51.51", "-0.13 "));
    }
}
