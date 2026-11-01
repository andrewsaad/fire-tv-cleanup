package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The page behind the More tile: the apps in config.xml (more_apps) that are installed, in that order, then
 * Developer options. Same wide tiles as the home row; Back returns home.
 */
public class MoreActivity extends Activity {

    private static final int SIDE_MARGIN_DP = 56, TILE_GAP_DP = 10;

    private static final class Item {
        final String label;
        final Drawable banner;
        final Drawable icon;
        final Intent intent;

        Item(String label, Drawable banner, Drawable icon, Intent intent) {
            this.label = label;
            this.banner = banner;
            this.icon = icon;
            this.intent = intent;
        }
    }

    private final List<Item> items = new ArrayList<>();
    private RecyclerView grid;
    private int columns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        // As many across as the home row holds (its apps plus the More tile), so the tiles match in size.
        columns = Math.max(1, getResources().getInteger(R.integer.columns)) + 1;
        grid = findViewById(R.id.moreGrid);
        grid.setLayoutManager(new GridLayoutManager(this, columns));
        grid.setItemAnimator(null);
        grid.setAdapter(new Tiles());
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
        grid.post(() -> {
            if (getCurrentFocus() != null && grid.findContainingViewHolder(getCurrentFocus()) != null) return;
            RecyclerView.ViewHolder vh = grid.findViewHolderForAdapterPosition(0);
            if (vh != null) vh.itemView.findViewById(R.id.card).requestFocus();
        });
    }

    private void reload() {
        PackageManager pm = getPackageManager();
        List<String> configured = Arrays.asList(getResources().getStringArray(R.array.more_apps));
        Set<String> installed = new HashSet<>();
        for (String pkg : configured) if (launchIntent(pm, pkg.trim()) != null) installed.add(pkg.trim());
        // An app already on the home row (e.g. Plex where fewer apps are installed) is not repeated here.
        List<String> dock = Arrays.asList(getResources().getStringArray(R.array.dock_apps));
        Set<String> dockInstalled = new HashSet<>();
        for (String pkg : DockApps.packages(dock)) if (launchIntent(pm, pkg) != null) dockInstalled.add(pkg);
        installed.removeAll(DockApps.row(dock, dockInstalled, getResources().getInteger(R.integer.columns)));
        items.clear();
        for (String key : MoreApps.entries(configured, installed)) {
            if (MoreApps.DEVELOPER_OPTIONS.equals(key)) {
                items.add(new Item(getString(R.string.developer_options), null, getDrawable(R.drawable.ic_settings),
                        developerOptionsIntent(pm)));
                continue;
            }
            Intent launch = launchIntent(pm, key);
            if (launch == null) continue;   // uninstalled since the check above
            String label = key;
            Drawable icon = null;
            try {
                ApplicationInfo ai = pm.getApplicationInfo(key, 0);
                label = String.valueOf(pm.getApplicationLabel(ai));
                icon = pm.getApplicationIcon(ai);
            } catch (PackageManager.NameNotFoundException ignored) {
                // keep the package name as the label
            }
            Drawable banner = launch.getComponent() != null ? AppAdapter.banner(pm, launch.getComponent()) : null;
            items.add(new Item(label, banner, icon, launch));
        }
        RecyclerView.Adapter<?> a = grid.getAdapter();
        if (a != null) a.notifyDataSetChanged();
    }

    /** The TV launch intent, else the phone one, else null (not installed or not launchable). */
    static Intent launchIntent(PackageManager pm, String pkg) {
        if (pkg.isEmpty()) return null;
        Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
        return i != null ? i : pm.getLaunchIntentForPackage(pkg);
    }

    /** Developer options, or the main Settings screen when this TV has no such page. */
    private static Intent developerOptionsIntent(PackageManager pm) {
        Intent dev = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        return dev.resolveActivity(pm) != null ? dev : new Intent(Settings.ACTION_SETTINGS);
    }

    private void open(Item item) {
        Intent i = new Intent(item.intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Can't open " + item.label, Toast.LENGTH_SHORT).show();
        }
    }

    private final class Tiles extends RecyclerView.Adapter<Tiles.Holder> {

        final class Holder extends RecyclerView.ViewHolder {
            final View card;
            final ImageView banner;
            final ImageView icon;
            final TextView name;
            final GradientDrawable ring = new GradientDrawable();

            Holder(View v) {
                super(v);
                card = v.findViewById(R.id.card);
                banner = v.findViewById(R.id.banner);
                icon = v.findViewById(R.id.icon);
                name = v.findViewById(R.id.name);
                float d = v.getResources().getDisplayMetrics().density;
                // Squircle-ish corners like the home tiles; the focus ring follows the same radius.
                card.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        float radius = Math.min(view.getWidth(), view.getHeight()) * AppAdapter.CORNER_RATIO;
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                    }
                });
                card.setClipToOutline(true);
                // Not in getOutline: changing the ring invalidates the card, which asks for the outline again.
                card.addOnLayoutChangeListener((view, l, t, r, b, ol, ot, or, ob) ->
                        ring.setCornerRadius(Math.min(r - l, b - t) * AppAdapter.CORNER_RATIO));
                ring.setStroke(Math.round(3 * d), 0xF5FFFFFF);
                card.setOnFocusChangeListener((view, focused) -> {
                    float s = focused ? AppAdapter.FOCUS_SCALE : 1f;
                    view.animate().scaleX(s).scaleY(s)
                            .setDuration(focused ? 260 : 180)
                            .setInterpolator(new OvershootInterpolator(focused ? 1.6f : 0f))
                            .start();
                    view.setForeground(focused ? ring : null);
                    name.animate().alpha(focused ? 1f : 0.6f).translationY(focused ? 6 * d : 0f).setDuration(220).start();
                });
            }
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_more, parent, false);
            // Same sizing as the home row (AppAdapter.onCreateViewHolder).
            float d = v.getResources().getDisplayMetrics().density;
            int cell = (v.getResources().getDisplayMetrics().widthPixels - Math.round(2 * SIDE_MARGIN_DP * d)) / columns;
            ViewGroup.LayoutParams lp = v.findViewById(R.id.card).getLayoutParams();
            lp.width = cell - Math.round(2 * TILE_GAP_DP * d);
            lp.height = lp.width * 9 / 16;
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Item item = items.get(position);
            h.name.setText(item.label);
            boolean art = item.banner != null;
            h.banner.setImageDrawable(item.banner);
            h.banner.setVisibility(art ? View.VISIBLE : View.GONE);
            h.icon.setImageDrawable(art ? null : item.icon);
            h.icon.setVisibility(art ? View.GONE : View.VISIBLE);
            h.card.setOnClickListener(v -> open(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
