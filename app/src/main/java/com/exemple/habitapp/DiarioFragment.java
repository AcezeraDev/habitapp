package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class DiarioFragment extends Fragment {

    private SharedPreferences prefs;
    private TextInputEditText input;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feature_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);

        ((TextView) view.findViewById(R.id.txtFeatureTitle)).setText("Diario rapido");
        ((TextView) view.findViewById(R.id.txtFeatureSubtitle)).setText("Registre uma nota curta sobre seu dia.");
        ((TextView) view.findViewById(R.id.txtFeatureHeroTitle)).setText("Reflexao de hoje");
        ((TextView) view.findViewById(R.id.txtFeatureHeroSubtitle)).setText(buildSummary());
        ((LinearProgressIndicator) view.findViewById(R.id.progressFeatureHero)).setProgressCompat(HabitStore.getTodayScore(prefs), true);

        LinearLayout list = view.findViewById(R.id.layoutFeatureContent);
        list.removeAllViews();

        TextInputLayout inputLayout = new TextInputLayout(requireContext());
        inputLayout.setHint("Como foi o dia?");
        input = new TextInputEditText(requireContext());
        input.setMinLines(4);
        input.setMaxLines(6);
        input.setText(prefs.getString(key(), ""));
        inputLayout.addView(input);
        list.addView(inputLayout);

        for (int offset = 0; offset > -5; offset--) {
            String note = prefs.getString("diary_" + HabitStore.dayKey(offset), "");
            if (!TextUtils.isEmpty(note)) {
                FeatureUi.addCard(requireContext(), list, offset == 0 ? "Hoje" : offset + "d", note, -1, R.color.primary);
            }
        }

        MaterialButton primary = view.findViewById(R.id.btnFeaturePrimary);
        primary.setText("Salvar diario");
        primary.setOnClickListener(v -> save(view));
        view.findViewById(R.id.btnFeatureSecondary).setVisibility(View.GONE);
    }

    private void save(View view) {
        String text = input.getText() != null ? input.getText().toString().trim() : "";
        prefs.edit().putString(key(), text).apply();
        FeedbackHelper.success(requireContext());
        Toast.makeText(requireContext(), "Diario salvo.", Toast.LENGTH_SHORT).show();
        onViewCreated(view, null);
    }

    private String key() {
        return "diary_" + HabitStore.todayKey();
    }

    private String buildSummary() {
        int mood = prefs.getInt("mood_" + HabitStore.todayKey(), -1);
        int energy = prefs.getInt("energy_" + HabitStore.todayKey(), -1);
        return "Humor " + label(mood) + " | energia " + label(energy) + " | score " + HabitStore.getTodayScore(prefs) + "%";
    }

    private String label(int value) {
        if (value == 0) return "baixo";
        if (value == 1) return "ok";
        if (value == 2) return "alto";
        return "sem registro";
    }
}
