package info.fortheease.monastery360;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.utils.PLUtils;

import java.util.Objects;

public class PanoramaActivity extends AppCompatActivity {

    private PLManager plManager;
    int resID = R.drawable.rumtek_panorama;
    GyroPanoramaController gyroController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panorama);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        if (intent != null) {
            String panoramic = intent.getStringExtra("panoramic");
            if (Objects.equals(panoramic, "rumtek")) {
                resID = R.drawable.rumtek_panorama;
            } else if (Objects.equals(panoramic, "lachung")) {
                resID = R.drawable.lachung_panorama;
            } else if (Objects.equals(panoramic, "enchey")) {
                resID = R.drawable.enchey_panorama;
            } else if (Objects.equals(panoramic, "phodong")) {
                resID = R.drawable.phodong_panorama;
            } else if (Objects.equals(panoramic, "dubdi")) {
                resID = R.drawable.dubdi_panorama;
            }
        }
        ViewGroup container = (ViewGroup) findViewById(R.id.main);
        plManager = new PLManager(this);
        plManager.setContentView(container);
        plManager.onCreate();

        PLSphericalPanorama panorama = new PLSphericalPanorama();
        panorama.getCamera().lookAt(30.0f, 90.0f); // initial pitch/yaw
        // load bitmap from res/raw
        PLImage image = new PLImage(PLUtils.getBitmap(this, resID), false);
        panorama.setImage(image);

        plManager.setPanorama(panorama);

        gyroController = new GyroPanoramaController(this, plManager);
        gyroController.start();
    }

    private void showInfoPopup(String title, String description) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override protected void onResume() { super.onResume(); plManager.onResume(); }
    @Override protected void onPause()  { plManager.onPause(); super.onPause(); }
    @Override protected void onDestroy(){ plManager.onDestroy(); gyroController.stop(); super.onDestroy();  }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return plManager.onTouchEvent(event);
    }
}