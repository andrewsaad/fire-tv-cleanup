package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class DockAppsTest {

    @Test
    public void firstInstalledAlternativeWins() {
        assertEquals(Collections.singletonList("a.debug"),
                DockApps.resolve(Collections.singletonList("a.debug|a"), Arrays.asList("a", "a.debug")));
        assertEquals(Collections.singletonList("a"),
                DockApps.resolve(Collections.singletonList("a.debug|a"), Collections.singletonList("a")));
    }

    @Test
    public void itemsWithNothingInstalledAreSkippedAndOrderIsKept() {
        assertEquals(Arrays.asList("c", "a"),
                DockApps.resolve(Arrays.asList("c", "b1|b2", "a"), Arrays.asList("a", "c", "zzz")));
    }

    @Test
    public void rowIsCappedAtColumns() {
        assertEquals(Arrays.asList("a", "b"), DockApps.row(Arrays.asList("a", "b", "c"), Arrays.asList("a", "b", "c"), 2));
        assertEquals(Collections.singletonList("a"), DockApps.row(Arrays.asList("a", "b"), Collections.singletonList("a"), 4));
    }

    @Test
    public void packagesListsEveryAlternative() {
        assertEquals(Arrays.asList("a.debug", "a", "b"), DockApps.packages(Arrays.asList("a.debug | a", "b", "", null)));
    }

    @Test
    public void spacesBlanksAndDuplicatesAreIgnored() {
        assertEquals(Arrays.asList("a", "b"),
                DockApps.resolve(Arrays.asList(" a ", "", "x | b", "a", null), Arrays.asList("a", "b")));
    }
}
