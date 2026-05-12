package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.util.List;

public class MissoesFragment extends Fragment {

    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feature_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        render(view);
    }

    private void render(View view) {
        List<Mission> missions = MissionEngine.getDailyMissions(prefs);
        int done = MissionEngine.getCompletedCount(missions);
        int level = XpEngine.getLevel(prefs);

        ((TextView) view.findViewById(R.id.txtFeatureTitle)).setText("Missoes diarias");
        ((TextView) view.findViewById(R.id.txtFeatureSubtitle)).setText("Tarefas pequenas que viram XP e deixam o dia andando.");
        ((TextView) view.findViewById(R.id.txtFeatureHeroTitle)).setText("Nivel " + level + " | " + XpEngine.getBaseXp(prefs) + " XP");
        ((TextView) view.findViewById(R.id.txtFeatureHeroSubtitle)).setText(done + " de " + missions.size() + " missoes completas. Proxima: " + MissionEngine.getNextMissionTitle(missions));
        ((LinearProgressIndicator) view.findViewById(R.id.progressFeatureHero)).setProgressCompat(XpEngine.getLevelProgress(prefs), true);

        LinearLayout list = view.findViewById(R.id.layoutFeatureContent);
        list.removeAllViews();
        for (Mission mission : missions) {
            FeatureUi.addCard(requireContext(), list, mission.title + " +" + mission.xpReward + " XP", mission.subtitle, mission.progress, mission.isComplete() ? R.color.success : R.color.primary);
        }

        MaterialButton primary = view.findViewById(R.id.btnFeaturePrimary);
        primary.setText(XpEngine.hasClaimedToday(prefs) ? "XP de hoje ja resgatado" : "Resgatar XP das missoes");
        primary.setEnabled(!XpEngine.hasClaimedToday(prefs));
        primary.setOnClickListener(v -> {
            int xp = XpEngine.claimDailyMissions(prefs);
            if (xp > 0) {
                FeedbackHelper.success(requireContext());
                Toast.makeText(requireContext(), "+" + xp + " XP adicionados.", Toast.LENGTH_SHORT).show();
                render(view);
            } else {
                Toast.makeText(requireContext(), "Complete uma missao antes de resgatar XP.", Toast.LENGTH_SHORT).show();
            }
        });

        MaterialButton secondary = view.findViewById(R.id.btnFeatureSecondary);
        secondary.setText("Abrir painel de hoje");
        secondary.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(R.id.home);
            }
        });
    }
}
