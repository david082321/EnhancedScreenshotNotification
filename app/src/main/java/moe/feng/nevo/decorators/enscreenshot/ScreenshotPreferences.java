package moe.feng.nevo.decorators.enscreenshot;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.LocaleList;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import moe.feng.nevo.decorators.enscreenshot.utils.FormatUtils;
import net.grandcentrix.tray.TrayPreferences;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.Optional;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ScreenshotPreferences {

    public static final int SHARE_EVOLVE_TYPE_NONE = 0;
    public static final int SHARE_EVOLVE_TYPE_DISMISS_AFTER_SHARING = 1;
    public static final int SHARE_EVOLVE_TYPE_DELETE_SCREENSHOT = 2;

    @IntDef({ SHARE_EVOLVE_TYPE_NONE,
            SHARE_EVOLVE_TYPE_DISMISS_AFTER_SHARING, SHARE_EVOLVE_TYPE_DELETE_SCREENSHOT })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShareEvolveType {}

    public static final int PREVIEW_TYPE_NONE = 0;
    public static final int PREVIEW_TYPE_PIP = 1;
    public static final int PREVIEW_TYPE_ARISU = 2;

    @IntDef({ PREVIEW_TYPE_NONE, PREVIEW_TYPE_PIP, PREVIEW_TYPE_ARISU })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PreviewType {}

    private static final String PREF_NAME = "screenshot";

    private static final String KEY_SCREENSHOT_PATH = "screenshot_path";
    private static final String KEY_PREFERRED_EDITOR_COMPONENT = "preferred_component";
    private static final String KEY_SHARE_EVOLVE_TYPE = "share_evolve_type";
    private static final String KEY_EDIT_ACTION_TEXT_FORMAT = "edit_action_text_format";
    private static final String KEY_SHOW_SCREENSHOTS_COUNT = "show_screenshots_count";
    private static final String KEY_SHOW_SCREENSHOT_DETAILS = "show_screenshot_details";
    private static final String KEY_PREVIEW_FLOATING_WINDOW_TYPE = "preview_floating_window_type";
    private static final String KEY_REPLACE_NOTIFICATION_WITH_PREVIEW = "replace_notification_with_preview";
    private static final String KEY_DETECT_BARCODE = "detect_barcode";

    private static final File DEFAULT_SCREENSHOT_PATH =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Screenshots");

    private static final ComponentName LAUNCH_COMPONENT_NAME =
            ComponentName.createRelative(BuildConfig.APPLICATION_ID, ".LaunchActivity");

    private final TrayPreferences mPreferences;
    private final PackageManager mPackageManager;

    public ScreenshotPreferences(@NonNull Context context) {
        mPreferences = new TrayPreferences(context, PREF_NAME, 1);
        mPackageManager = context.getPackageManager();
    }

    @NonNull
    public String getScreenshotPath() {
        return Optional.ofNullable(mPreferences.getString(KEY_SCREENSHOT_PATH, null))
                .orElse(DEFAULT_SCREENSHOT_PATH.getAbsolutePath());
    }

    public Optional<ComponentName> getPreferredEditorComponentName() {
        final String value = mPreferences.getString(KEY_PREFERRED_EDITOR_COMPONENT, null);
        if (TextUtils.isEmpty(value)) {
            return Optional.empty();
        }
        return Optional.ofNullable(ComponentName.unflattenFromString(value));
    }

    public boolean isPreferredEditorAvailable() {
        final Optional<ComponentName> cn = getPreferredEditorComponentName();
        if (cn.isPresent()) {
            try {
                if (Objects.equals(
                        mPackageManager.getPackageInfo(
                                cn.get().getPackageName(), 0).packageName,
                        cn.get().getPackageName())) {
                    return true;
                }
            } catch (PackageManager.NameNotFoundException ignored) {

            }
        }
        return false;
    }

    public Optional<CharSequence> getPreferredEditorTitle() {
        final Optional<ComponentName> cn = getPreferredEditorComponentName();
        if (cn.isPresent()) {
            try {
                return Optional.of(
                        mPackageManager.getActivityInfo(cn.get(), PackageManager.GET_META_DATA)
                                .loadLabel(mPackageManager));
            } catch (PackageManager.NameNotFoundException ignored) {

            }
        }
        return Optional.empty();
    }

    public Optional<Drawable> getPreferredEditorIcon() {
        final Optional<ComponentName> cn = getPreferredEditorComponentName();
        if (cn.isPresent()) {
            try {
                return Optional.of(
                        mPackageManager.getActivityInfo(cn.get(), PackageManager.GET_META_DATA)
                                .loadUnbadgedIcon(mPackageManager));
            } catch (PackageManager.NameNotFoundException ignored) {

            }
        }
        return Optional.empty();
    }

    public boolean isHideLauncherIcon() {
        return mPackageManager.getComponentEnabledSetting(LAUNCH_COMPONENT_NAME)
                == PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }

    @ShareEvolveType
    public int getShareEvolveType() {
        return mPreferences.getInt(KEY_SHARE_EVOLVE_TYPE, SHARE_EVOLVE_TYPE_NONE);
    }

    @NonNull
    public String getEditActionTextFormat() {
        return Optional.ofNullable(mPreferences.getString(KEY_EDIT_ACTION_TEXT_FORMAT, null))
                .orElseGet(() -> FormatUtils.getEditActionTextFormats(LocaleList.getDefault()).second.get(1));
    }

    public boolean isShowScreenshotsCount() {
        return mPreferences.getBoolean(KEY_SHOW_SCREENSHOTS_COUNT, false);
    }

    public boolean isShowScreenshotDetails() {
        return mPreferences.getBoolean(KEY_SHOW_SCREENSHOT_DETAILS, false);
    }

    @PreviewType
    public int getPreviewType() {
        return mPreferences.getInt(KEY_PREVIEW_FLOATING_WINDOW_TYPE, PREVIEW_TYPE_NONE);
    }

    public boolean isReplaceNotificationWithPreview() {
        return mPreferences.getBoolean(KEY_REPLACE_NOTIFICATION_WITH_PREVIEW, false);
    }

    public boolean shouldDetectBarcode() {
        return mPreferences.getBoolean(KEY_DETECT_BARCODE, false);
    }

    public void setScreenshotPath(@Nullable String screenshotPath) {
        if (screenshotPath == null) {
            mPreferences.remove(KEY_SCREENSHOT_PATH);
        } else {
            mPreferences.put(KEY_SCREENSHOT_PATH, screenshotPath);
        }
    }

    public void setPreferredEditorComponentName(@Nullable ComponentName componentName) {
        if (componentName == null) {
            mPreferences.remove(KEY_PREFERRED_EDITOR_COMPONENT);
        } else {
            mPreferences.put(KEY_PREFERRED_EDITOR_COMPONENT, componentName.flattenToString());
        }
    }

    public void setHideLauncherIcon(boolean shouldHide) {
        mPackageManager.setComponentEnabledSetting(
                LAUNCH_COMPONENT_NAME,
                shouldHide ?
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void setShareEvolveType(@ShareEvolveType int type) {
        mPreferences.put(KEY_SHARE_EVOLVE_TYPE, type);
    }

    public void setEditActionTextFormat(@NonNull String format) {
        mPreferences.put(KEY_EDIT_ACTION_TEXT_FORMAT, format);
    }

    public void setShowScreenshotsCount(boolean bool) {
        mPreferences.put(KEY_SHOW_SCREENSHOTS_COUNT, bool);
    }

    public void setShowScreenshotDetails(boolean bool) {
        mPreferences.put(KEY_SHOW_SCREENSHOT_DETAILS, bool);
    }

    public void setPreviewType(@PreviewType int type) {
        mPreferences.put(KEY_PREVIEW_FLOATING_WINDOW_TYPE, type);
    }

    public void setReplaceNotificationWithPreview(boolean bool) {
        mPreferences.put(KEY_REPLACE_NOTIFICATION_WITH_PREVIEW, bool);
    }

    public void setDetectBarcode(boolean bool) {
        mPreferences.put(KEY_DETECT_BARCODE, bool);
    }
}
