package info.fortheease.monastery360;

import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import info.fortheease.monastery360.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity {


    ActivityDetailBinding binding;
    MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btn360.setOnClickListener(v -> {
            startActivity(new Intent(DetailActivity.this, PanoramaActivity.class));
        });
        binding.btnPlayAudio.setOnClickListener(v -> toggleAudio());
        binding.btnOpenMap.setOnClickListener(v -> openMapsIntent());
    }
    private void toggleAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            binding.btnPlayAudio.setText("Play Audio Guide");
            return;
        }
        if (mediaPlayer == null) {
            int audioRes = getResIdByName("audio_name", "raw");
            if (audioRes == 0) return;
            mediaPlayer = MediaPlayer.create(this, audioRes);
            mediaPlayer.setOnCompletionListener(mp -> {
                binding.btnPlayAudio.setText("Play Audio Guide");
                mediaPlayer.release();
                mediaPlayer = null;
            });
        }
        mediaPlayer.start();
        binding.btnPlayAudio.setText("Pause Audio");
    }
    private int getResIdByName(String name, String folder) {
        if (name == null) return 0;
        Resources res = getResources();
        return res.getIdentifier(name, folder, getPackageName());
    }

    private void openMapsIntent() {
        String uri = "geo:" + 27.3382 + "," + 88.6170 + "?q=" + 27.3382 + "," + 88.6170 + "(" + Uri.encode("Rumtek Monastery") + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps"); // prefer Google Maps app
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // fallback to browser maps
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + 27.3382 + "," + 88.6170));
            startActivity(i);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}