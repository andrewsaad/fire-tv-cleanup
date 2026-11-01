package com.gathin.tvhome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Which packages the home row shows, with no Android dependencies so it can be unit tested. Each config.xml
 * dock_apps item is one tile: a package name, or alternatives separated by "|" (e.g. a modified build first, then
 * the store app), of which the first installed one is used. Items with nothing installed are skipped.
 */
final class DockApps {

    private DockApps() {
    }

    static List<String> resolve(List<String> configured, Collection<String> installed) {
        Set<String> out = new LinkedHashSet<>();
        for (String raw : configured) {
            if (raw == null) continue;
            for (String alt : raw.split("\\|")) {
                String pkg = alt.trim();
                if (!pkg.isEmpty() && installed.contains(pkg)) {
                    out.add(pkg);
                    break;
                }
            }
        }
        return new ArrayList<>(out);
    }

    /** The packages on the row: {@link #resolve}, capped at the row's column count. */
    static List<String> row(List<String> configured, Collection<String> installed, int columns) {
        List<String> all = resolve(configured, installed);
        return new ArrayList<>(all.subList(0, Math.min(columns, all.size())));
    }

    /** Every package an item names, alternatives included. */
    static List<String> packages(List<String> configured) {
        List<String> out = new ArrayList<>();
        for (String raw : configured) {
            if (raw == null) continue;
            for (String alt : raw.split("\\|")) if (!alt.trim().isEmpty()) out.add(alt.trim());
        }
        return out;
    }
}
