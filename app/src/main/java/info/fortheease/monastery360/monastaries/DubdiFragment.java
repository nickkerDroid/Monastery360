package info.fortheease.monastery360.monastaries;

import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import info.fortheease.monastery360.PanoramaActivity;
import info.fortheease.monastery360.R;
import info.fortheease.monastery360.adapters.CarouselAdapter;
import info.fortheease.monastery360.databinding.FragmentDubdiBinding; // Corrected binding class

public class DubdiFragment extends Fragment implements TextToSpeech.OnInitListener {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private CarouselAdapter adapter;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;
    private TextToSpeech textToSpeech;
    private boolean ttsInitialized = false;
    private final String UTTERANCE_ID = "UniqueIDFragmentDubdi"; // Unique ID for fragment

    private FragmentDubdiBinding binding; // Corrected binding class
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDubdiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btn360.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PanoramaActivity.class);
            intent.putExtra("panoramic", "dubdi");
            startActivity(intent);
        });
        binding.btnPlayAudio.setOnClickListener(v -> toggleAudio());
        binding.btnOpenMap.setOnClickListener(v -> openMapsIntent());

        viewPager = binding.viewPager; // Use binding
        dotsLayout = binding.dotsLayout; // Use binding

        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.dubdi1);
        images.add(R.drawable.dubdi2);
        images.add(R.drawable.dubdi3);
        images.add(R.drawable.dubdi4);
        images.add(R.drawable.dubdi5);

        adapter = new CarouselAdapter(requireContext(), images);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);

        setupDots(images.size());
        setCurrentDot(0);

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setCurrentDot(position);
            }
        };
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        textToSpeech = new TextToSpeech(requireContext(), this);
    }

    private void setupDots(int count) {
        if (dotsLayout == null || getContext() == null) return;
        dotsLayout.removeAllViews();
        int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            params.setMargins(sizeInPx / 2, 0, sizeInPx / 2, 0);
            dot.setLayoutParams(params);
            dot.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dot_inactive));
            dotsLayout.addView(dot);
        }
    }

    private void setCurrentDot(int index) {
        if (dotsLayout == null || getContext() == null) return;
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            ImageView dot = (ImageView) dotsLayout.getChildAt(i);
            if (i == index)
                dot.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dot_active));
            else
                dot.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dot_inactive));
        }
    }

    private void toggleAudio() {
        if (!ttsInitialized || textToSpeech == null || getContext() == null || binding == null) {
            Log.e("TTS", "TTS not initialized or context/binding is null.");
            return;
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            binding.btnPlayAudio.setText(getString(R.string.play_audio));
        }

        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        } else {
            String textToSpeak = getString(R.string.perched_on_a_hill_above_yuksom_in_west_sikkim_dubdi_monastery_1701_is_the_oldest_monastery_in_the_state_and_a_key_landmark_in_sikkim_s_buddhist_history_surrounded_by_dense_forests_and_offering_stunning_views_of_the_mountains_it_is_a_peaceful_destination_for_both_spiritual_seekers_and_nature_lovers_the_monastery_houses_ancient_manuscripts_rare_buddhist_relics_and_traditional_paintings);
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params);
            binding.btnPlayAudio.setText("Pause Audio");
        }
    }

    private void openMapsIntent() {
        if (getContext() == null) return;
        String uri = "geo:" + 27.36671 + "," + 88.23007 + "?q=" + 27.36671 + "," + 88.23007 + "(" + Uri.encode("Dubdi Monastery") + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + 27.36671 + "," + 88.23007));
            startActivity(i);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported");
            } else {
                ttsInitialized = true;
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {}

                    @Override
                    public void onDone(String utteranceId) {
                        if (utteranceId.equals(UTTERANCE_ID) && getActivity() != null && binding != null) {
                            getActivity().runOnUiThread(() -> binding.btnPlayAudio.setText(getString(R.string.play_audio)));
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if (getActivity() != null && binding != null) {
                             getActivity().runOnUiThread(() -> binding.btnPlayAudio.setText(getString(R.string.play_audio)));
                        }
                    }
                });
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pageChangeCallback != null && viewPager != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            ttsInitialized = false;
        }
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        binding = null; // Release binding
    }
}
