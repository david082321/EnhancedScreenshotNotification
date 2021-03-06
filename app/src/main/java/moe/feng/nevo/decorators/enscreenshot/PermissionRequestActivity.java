package moe.feng.nevo.decorators.enscreenshot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.Optional;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class PermissionRequestActivity extends Activity {

    public static final String TAG = "PermissionRequest";

    public static final String EXTRA_PERMISSION_TYPE =
            BuildConfig.APPLICATION_ID + ".extra.PERMISSION_TYPE";

    public static final int TYPE_STORAGE = 1;

    private static final int REQUEST_CODE_PERMISSION = 1000;

    private static final SparseArray<String[]> PERMISSIONS_MAP = new SparseArray<>();

    static {
        PERMISSIONS_MAP.append(TYPE_STORAGE, new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int type = Optional.ofNullable(getIntent())
                .map(intent -> intent.getIntExtra(EXTRA_PERMISSION_TYPE, 0))
                .orElse(0);
        if (type <= 0) {
            Log.wtf(TAG, "Request type should be a positive number.");
            finish();
            return;
        }

        requestPermissions(PERMISSIONS_MAP.get(type), REQUEST_CODE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Arrays.stream(grantResults).allMatch(i -> i == PackageManager.PERMISSION_GRANTED)) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    public static boolean checkIfPermissionTypeGranted(@NonNull Context context, int type) {
        for (String permission : PERMISSIONS_MAP.get(type)) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
