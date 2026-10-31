package com.gathin.tvhome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Which entries the More page shows, with no Android dependencies so it can be unit tested: the packages in
 * config.xml (more_apps) that are installed, in that order, each once, then the fixed Developer options entry.
 */
final class MoreApps {

    /** Not a package: the Developer options entry, always last. */
    static final String DEVELOPER_OPTIONS = "#developer_options";

    private MoreApps() {
    }

    static List<String> entries(List<String> configured, Collection<String> installed) {
        Set<String> out = new LinkedHashSet<>();
        for (String raw : configured) {
            String pkg = raw == null ? "" : raw.trim();
            if (!pkg.isEmpty() && installed.contains(pkg)) out.add(pkg);
        }
        out.remove(DEVELOPER_OPTIONS);
        List<String> list = new ArrayList<>(out);
        list.add(DEVELOPER_OPTIONS);
        return list;
    }
}
