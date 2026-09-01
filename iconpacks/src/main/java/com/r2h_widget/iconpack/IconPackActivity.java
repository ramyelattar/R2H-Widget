package com.r2h_widget.iconpack;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The companion is intentionally launchable because Nothing Launcher uses its
 * launcher-activity inventory to discover external icon packs. Keep this
 * screen lightweight and branded; it is not a second copy of the R2H app.
 */
public final class IconPackActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        String packName = getString(getResources().getIdentifier(
                "icon_pack_name",
                "string",
                getPackageName()
        ));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(56, 80, 56, 56);
        root.setBackgroundColor(Color.rgb(7, 8, 12));

        TextView title = text(packName, 30, Color.WHITE);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView subtitle = text("R2H Icon Pack", 18, Color.rgb(180, 184, 198));
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = 20;
        root.addView(subtitle, subtitleParams);

        TextView managed = text("Managed by R2H", 15, Color.rgb(135, 141, 158));
        LinearLayout.LayoutParams managedParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        managedParams.topMargin = 10;
        root.addView(managed, managedParams);

        Button openR2h = new Button(this);
        openR2h.setText("Open R2H");
        openR2h.setOnClickListener(view -> openR2h());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.topMargin = 48;
        root.addView(openR2h, buttonParams);

        Button openSettings = new Button(this);
        openSettings.setText("Open Icon Pack Settings");
        openSettings.setOnClickListener(view -> openIconPackSettings());
        root.addView(openSettings, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        setContentView(root);
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private void openR2h() {
        Intent main = new Intent();
        main.setComponent(new ComponentName("com.r2h_widget", "com.r2h_widget.MainActivity"));
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(main);
    }

    private void openIconPackSettings() {
        Intent settings = new Intent("com.nothing.launcher.icon_pack_picker")
                .setPackage("com.nothing.launcher");
        if (getPackageManager().resolveActivity(settings, 0) == null) {
            Toast.makeText(this, "Open Home > Customise > Icon Pack", Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(settings);
    }
}
