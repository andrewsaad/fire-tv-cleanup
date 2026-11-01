package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MoreAppsTest {

    private static final String DEV = MoreApps.DEVELOPER_OPTIONS;

    @Test
    public void developerOptionsIsAlwaysPresentAndLast() {
        assertEquals(Collections.singletonList(DEV), MoreApps.entries(Collections.emptyList(), Collections.emptySet()));
        assertEquals(Arrays.asList("org.xbmc.kodi", DEV),
                MoreApps.entries(Arrays.asList("org.xbmc.kodi"), Arrays.asList("org.xbmc.kodi")));
    }

    @Test
    public void missingAppsAreSkippedAndOrderIsKept() {
        assertEquals(Arrays.asList("c", "a", DEV),
                MoreApps.entries(Arrays.asList("c", "b", "a"), Arrays.asList("a", "c", "zzz")));
    }

    @Test
    public void duplicatesAppearOnce() {
        assertEquals(Arrays.asList("a", "b", DEV),
                MoreApps.entries(Arrays.asList("a", " b ", "a", "b", DEV), Arrays.asList("a", "b", DEV)));
    }

    /** The shipped more_apps list: Google TV with Netflix, Kodi, Downloader and our IPTV app installed. */
    @Test
    public void configuredListOnAGoogleTv() throws Exception {
        List<String> configured = configArray("more_apps");
        assertTrue(configured.contains("com.iptvapp"));
        assertEquals(Arrays.asList("com.netflix.ninja", "org.xbmc.kodi", "com.esaba.downloader", "com.iptvapp", DEV),
                MoreApps.entries(configured, Arrays.asList("com.iptvapp", "com.esaba.downloader", "org.xbmc.kodi", "com.netflix.ninja")));
    }

    /** Televizo left the home row for StreamVault; it heads the More page where installed. */
    @Test
    public void televizoIsFirstOnMorePage() throws Exception {
        assertEquals(Arrays.asList("com.ottplay.ottplay", "com.netflix.ninja", DEV),
                MoreApps.entries(configArray("more_apps"), Arrays.asList("com.netflix.ninja", "com.ottplay.ottplay")));
    }

    /** Plex left the main Google TV row for Jellyfin; it heads the More page. */
    @Test
    public void plexIsFirstOnMorePage() throws Exception {
        assertEquals(Arrays.asList("com.plexapp.android", "com.ottplay.ottplay", "org.xbmc.kodi", DEV),
                MoreApps.entries(configArray("more_apps"), Arrays.asList("org.xbmc.kodi", "com.ottplay.ottplay", "com.plexapp.android")));
    }

    private static List<String> configArray(String name) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new File("src/main/res/values/config.xml"));
        List<String> out = new ArrayList<>();
        NodeList arrays = doc.getElementsByTagName("string-array");
        for (int i = 0; i < arrays.getLength(); i++) {
            Element a = (Element) arrays.item(i);
            if (!name.equals(a.getAttribute("name"))) continue;
            NodeList items = a.getElementsByTagName("item");
            for (int j = 0; j < items.getLength(); j++) out.add(items.item(j).getTextContent().trim());
        }
        return out;
    }
}
