package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** One dock_apps list serves every TV; check each TV's row (installed apps in list order, at most "columns"). */
public class DockAppsConfigTest {

    private static Document config() throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new File("src/main/res/values/config.xml"));
    }

    /** What AppAdapter.reload shows: pinned apps that are installed, in pinned order, capped at columns. */
    private static List<String> row(List<String> installed) throws Exception {
        Document doc = config();
        List<String> pinned = new ArrayList<>();
        NodeList arrays = doc.getElementsByTagName("string-array");
        for (int i = 0; i < arrays.getLength(); i++) {
            Element a = (Element) arrays.item(i);
            if (!"dock_apps".equals(a.getAttribute("name"))) continue;
            NodeList items = a.getElementsByTagName("item");
            for (int j = 0; j < items.getLength(); j++) pinned.add(items.item(j).getTextContent().trim());
        }
        int columns = Integer.parseInt(doc.getElementsByTagName("integer").item(0).getTextContent().trim());
        List<String> out = new ArrayList<>();
        for (String p : pinned) if (installed.contains(p) && out.size() < columns) out.add(p);
        return out;
    }

    @Test
    public void fireTvShowsPlexYouTubeSmartersGenral() throws Exception {
        assertEquals(Arrays.asList("com.plexapp.android", "com.amazon.firetv.youtube", "com.nst.iptvsmarterstvbox", "com.genraltv.app"),
                row(Arrays.asList("com.genraltv.app", "com.nst.iptvsmarterstvbox", "com.plexapp.android", "com.amazon.firetv.youtube", "com.netflix.ninja")));
    }

    @Test
    public void googleTvShowsStreamVaultPlexYouTubeShahid() throws Exception {
        assertEquals(Arrays.asList("com.streamvault.app", "com.plexapp.android", "com.google.android.youtube.tv", "net.mbc.shahidTV"),
                row(Arrays.asList("net.mbc.shahidTV", "com.ottplay.ottplay", "com.streamvault.app", "com.plexapp.android",
                        "com.google.android.youtube.tv", "com.netflix.ninja")));
    }

    @Test
    public void googleTvWithoutStreamVaultSkipsTelevizo() throws Exception {
        // Televizo now lives on the More page only.
        assertEquals(Arrays.asList("com.plexapp.android", "com.google.android.youtube.tv", "net.mbc.shahidTV"),
                row(Arrays.asList("net.mbc.shahidTV", "com.ottplay.ottplay", "com.plexapp.android", "com.google.android.youtube.tv")));
    }
}
