package com.gathin.tvhome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PhotoDreamTest {

    @Test
    public void fireOs6KeepsSystemFont() {
        assertFalse(PhotoDream.hasFontResources(25));
    }

    @Test
    public void android8AndUpUseOpenSans() {
        assertTrue(PhotoDream.hasFontResources(26));
        assertTrue(PhotoDream.hasFontResources(34));
    }
}
