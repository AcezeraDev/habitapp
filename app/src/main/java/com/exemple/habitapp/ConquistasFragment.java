package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class ConquistasFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conquistas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        List<Achievement> achievements = AchievementEngine.getAchievements(prefs);
        int unlocked = AchievementEngine.getUnlockedCount(achievements);
        int overallProgress = HabitStore.percent(unlocked, achievements.size());

        TextView resumo = view.findViewById(R.id.txtConquistasResumo);
        TextView nivel = view.findViewById(R.id.txtConquistasNivel);
        TextView proxima = view.findViewById(R.id.txtConquistasProxima);
        LinearProgressIndicator progress = view.findViewById(R.id.progressConquistas);
        LinearLayout list = view.findViewById(R.id.layoutConquistasLista);

        resumo.setText("Medalhas liberadas por agua, foco, rotina e sequencia.");
        nivel.setText(unlocked + " de " + achievements.size() + " medalhas");
        progress.setProgressCompat(overallProgress, true);
        proxima.setText(nextAchievement(achievements));

        list.removeAllViews();
        for (Achievement achievement : achievements) {
            addAchievementCard(list, achievement);
        }
    }

    private String nextAchievement(List<Achievement> achievements) {
        Achievement best = null;
        for (Achievement achievement : achievements) {
            if (achievement.unlocked) continue;
            if (best == null || achievement.progress > best.progress) {
                best = achievement;
            }
        }
        return best == null ? "Todas as conquistas principais foram liberadas." : "Proxima: " + best.title + " (" + best.progress + "%)";
    }

    private void addAchievementCard(LinearLayout parent, Achievement achievement) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface));
        card.setRadius(dp(8));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(ContextCompat.getColor(requireContext(), achievement.unlocked ? achievement.colorRes : R.color.line));

        LinearLayout content = new LinearLayout(requireContext());
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setPadding(dp(14), dp(14), dp(14), dp(14));

        TextView badge = new TextView(requireContext());
        badge.setText(achievement.unlocked ? "OK" : achievement.progress + "%");
        badge.setGravity(Gravity.CENTER);
        badge.setTextSize(12f);
        badge.setTypeface(badge.getTypeface(), Typeface.BOLD);
        badge.setTextColor(ContextCompat.getColor(requireContext(), achievement.unlocked ? R.color.white : achievement.colorRes));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setShape(GradientDrawable.OVAL);
        badgeBg.setColor(ContextCompat.getColor(requireContext(), achievement.unlocked ? achievement.colorRes : R.color.surface_soft));
        badge.setBackground(badgeBg);
        content.addView(badge, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout texts = new LinearLayout(requireContext());
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(14), 0, 0, 0);

        TextView title = new TextView(requireContext());
        title.setText(achievement.title);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink));
        title.setTextSize(16f);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        texts.addView(title);

        TextView subtitle = new TextView(requireContext());
        subtitle.setText(achievement.subtitle);
        subtitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        subtitle.setTextSize(13f);
        subtitle.setPadding(0, dp(3), 0, dp(8));
        texts.addView(subtitle);

        LinearProgressIndicator bar = new LinearProgressIndicator(requireContext());
        bar.setMax(100);
        bar.setProgressCompat(achievement.progress, false);
        bar.setIndicatorColor(ContextCompat.getColor(requireContext(), achievement.colorRes));
        bar.setTrackColor(ContextCompat.getColor(requireContext(), R.color.line));
        bar.setTrackThickness(dp(6));
        bar.setTrackCornerRadius(dp(8));
        bar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), achievement.colorRes)));
        texts.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(content);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(10));
        parent.addView(card, params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
