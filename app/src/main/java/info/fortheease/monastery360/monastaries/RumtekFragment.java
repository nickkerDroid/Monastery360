package info.fortheease.monastery360.monastaries;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

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
import info.fortheease.monastery360.databinding.FragmentRumtekBinding; // Corrected binding class

public class RumtekFragment extends Fragment implements TextToSpeech.OnInitListener {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private CarouselAdapter adapter;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;
    private TextToSpeech textToSpeech;
    private boolean ttsInitialized = false;
    private final String UTTERANCE_ID = "UniqueIDFragmentRumtek"; // Unique ID for fragment
    private Locale currentLocale = Locale.US;

    private FragmentRumtekBinding binding; // Corrected binding class
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRumtekBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btn360.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PanoramaActivity.class);
            intent.putExtra("panoramic", "rumtek");
            startActivity(intent);
        });
        binding.btnPlayAudio.setOnClickListener(v -> toggleAudio());
        binding.btnOpenMap.setOnClickListener(v -> openMapsIntent());

        binding.btnLangEnglish.setOnClickListener(v -> setTtsLanguage(Locale.US));
        binding.btnLangHindi.setOnClickListener(v -> setTtsLanguage(new Locale("hi", "IN")));
        binding.btnLangNepali.setOnClickListener(v -> setTtsLanguage(new Locale("ne", "NP")));

        viewPager = binding.viewPager; // Use binding
        dotsLayout = binding.dotsLayout; // Use binding

        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.rumtek1);
        images.add(R.drawable.rumtek2);
        images.add(R.drawable.rumtek3);

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

        binding.btnDirections.setOnClickListener(v -> openDirectionsIntent());
    }

    private void openDirectionsIntent() {
        if (getContext() == null) return;
        double latitude = 27.28886;
        double longitude = 88.56146;
        String uriString = "https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude;
        Uri gmmIntentUri = Uri.parse(uriString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            startActivity(browserIntent);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            setTtsLanguage(Locale.US);
            if (ttsInitialized && textToSpeech != null) { // Ensure textToSpeech is not null
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
            if (getContext() != null) {
                Toast.makeText(getContext(), "TTS Initialization failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getStringForLocale(int resId, Locale locale) {
        if (getContext() == null) return ""; // Or handle error appropriately
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        return requireContext().createConfigurationContext(config).getString(resId);
    }

    private void setTtsLanguage(Locale locale) {
        if (textToSpeech == null || getContext() == null) return;
        currentLocale = locale;
        int result = textToSpeech.setLanguage(locale);
        updateButtonBackgrounds(locale);

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS", "Language not supported: " + locale.getDisplayName());
            Toast.makeText(getContext(), "Language not supported: " + locale.getDisplayName(), Toast.LENGTH_SHORT).show();
            if (!locale.equals(Locale.US)) {
                setTtsLanguage(Locale.US);
            }
            ttsInitialized = false;
        } else {
            ttsInitialized = true;
            Log.i("TTS", "Language set to: " + locale.getDisplayName());
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
                if (binding != null) binding.btnPlayAudio.setText(getString(R.string.play_audio));
            }
        }
    }

    private void updateButtonBackgrounds(Locale selectedLocale) {
        if (getContext() == null || binding == null) return;
        // Using a simpler background update, assuming you have selector drawables or will set them up.
        // Or, use Color.parseColor if you want specific hex colors.
        binding.btnLangEnglish.setBackgroundColor(selectedLocale.equals(Locale.US) ? ContextCompat.getColor(requireContext(), R.color.black) : Color.GRAY);
        binding.btnLangHindi.setBackgroundColor(selectedLocale.equals(new Locale("hi", "IN")) ? ContextCompat.getColor(requireContext(), R.color.black) : Color.GRAY);
        binding.btnLangNepali.setBackgroundColor(selectedLocale.equals(new Locale("ne", "NP")) ? ContextCompat.getColor(requireContext(), R.color.black) : Color.GRAY);
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
            Log.e("TTS", "TTS not initialized, or context/binding is null.");
            if (getContext() != null) Toast.makeText(getContext(), "TTS not ready. Please select a language.", Toast.LENGTH_SHORT).show();
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
            // onDone will handle text update
        } else {
            String textToSpeak = getStringForLocale(R.string.perched_on_a_hill_just_23_km_from_gangtok_rumtek_monastery_is_sikkim_s_largest_and_most_famous_monastery_known_as_the_seat_of_the_karma_kagyu_lineage_of_tibetan_buddhism_visitors_are_welcomed_by_its_stunning_tibetan_architecture_vibrant_murals_and_the_golden_stupa_enshrined_with_precious_relics_the_serene_surroundings_and_panoramic_views_of_the_himalayas_make_it_a_peaceful_retreat_you_can_witness_daily_chanting_rituals_explore_the_monastery_courtyard_and_enjoy_quiet_moments_perfect_for_meditation_or_photography_best_time_to_visit_morning_prayers_or_late_afternoon_for_sunset_views_tip_wear_modest_clothing_and_comfortable_shoes_as_the_monastery_is_on_a_short_uphill_walk, currentLocale);
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params);
            binding.btnPlayAudio.setText("Pause Audio"); // Or R.string.pause_audio if you have it
        }
    }

    // getResIdByName is not used here but kept for consistency if needed later.
    private int getResIdByName(String name, String folder) {
        if (name == null || getContext() == null) return 0;
        Resources res = getResources();
        return res.getIdentifier(name, folder, requireContext().getPackageName());
    }

    private void openMapsIntent() {
        if (getContext() == null) return;
        String uri = "geo:" + 27.28886 + "," + 88.56146 + "?q=" + 27.28886 + "," + 88.56146 + "(" + Uri.encode("Rumtek Monastery") + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + 27.28886 + "," + 88.56146));
            startActivity(i);
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
