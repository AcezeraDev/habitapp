package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarioFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);
        HabitStore.saveTodaySnapshot(prefs);

        TextView title = view.findViewById(R.id.txtCalendarioTitulo);
        TextView summary = view.findViewById(R.id.txtCalendarioResumo);
        TextView insight = view.findViewById(R.id.txtCalendarioInsight);
        LinearLayout weekdays = view.findViewById(R.id.layoutCalendarioSemana);
        GridLayout grid = view.findViewById(R.id.layoutCalendarioGrid);

        Calendar calendar = Calendar.getInstance();
        String month = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.getTime());
        title.setText(capitalize(month));

        int strongDays = countStrongDays(prefs, calendar);
        summary.setText(strongDays + " dias fortes neste mes.");
        insight.setText(strongDays >= 7
                ? "Voce ja criou uma base boa neste mes. Agora a meta e proteger a sequencia."
                : "Escolha uma meta pequena para hoje e transforme o calendario aos poucos.");

        renderWeekdays(weekdays);
        renderMonth(grid, prefs, calendar);
    }

    private int countStrongDays(SharedPreferences prefs, Calendar now) {
        Calendar cursor = (Calendar) now.clone();
        int maxDay = cursor.getActualMaximum(Calendar.DAY_OF_MONTH);
        int today = now.get(Calendar.DAY_OF_MONTH);
        int strong = 0;

        for (int day = 1; day <= Math.min(today, maxDay); day++) {
            cursor.set(Calendar.DAY_OF_MONTH, day);
            long key = normalizedDayKey(cursor);
            if (HabitStore.getScoreForDay(prefs, key) >= 80) {
                strong++;
            }
        }
        return strong;
    }

    private void renderWeekdays(LinearLayout parent) {
        parent.removeAllViews();
        String[] labels = {"D", "S", "T", "Q", "Q", "S", "S"};
        for (String label : labels) {
            TextView view = new TextView(requireContext());
            view.setText(label);
            view.setGravity(Gravity.CENTER);
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
            view.setTextSize(12f);
            view.setTypeface(view.getTypeface(), Typeface.BOLD);
            parent.addView(view, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
    }

    private void renderMonth(GridLayout grid, SharedPreferences prefs, Calendar now) {
        grid.removeAllViews();
        Calendar cursor = (Calendar) now.clone();
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cursor.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDay = cursor.getActualMaximum(Calendar.DAY_OF_MONTH);
        int today = now.get(Calendar.DAY_OF_MONTH);

        for (int index = 0; index < 42; index++) {
            int dayNumber = index - firstDayOfWeek + 1;
            TextView day = createDayView();

            if (dayNumber < 1 || dayNumber > maxDay) {
                day.setText("");
                day.setVisibility(View.INVISIBLE);
            } else {
                cursor.set(Calendar.DAY_OF_MONTH, dayNumber);
                int score = dayNumber == today ? HabitStore.getTodayScore(prefs) : HabitStore.getScoreForDay(prefs, normalizedDayKey(cursor));
                day.setText(String.valueOf(dayNumber));
                day.setTextColor(ContextCompat.getColor(requireContext(), score >= 80 ? R.color.white : R.color.ink));
                day.setBackground(dayBackground(score, dayNumber == today));
                day.setContentDescription("Dia " + dayNumber + ", score " + score + "%");
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dp(46);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(dp(3), dp(3), dp(3), dp(3));
            grid.addView(day, params);
        }
    }

    private TextView createDayView() {
        TextView view = new TextView(requireContext());
        view.setGravity(Gravity.CENTER);
        view.setTextSize(14f);
        view.setTypeface(view.getTypeface(), Typeface.BOLD);
        return view;
    }

    private GradientDrawable dayBackground(int score, boolean today) {
        int color;
        if (score >= 80) {
            color = ContextCompat.getColor(requireContext(), R.color.success);
        } else if (score > 0) {
            color = ContextCompat.getColor(requireContext(), R.color.primary);
        } else {
            color = ContextCompat.getColor(requireContext(), R.color.surface_soft);
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(8));
        if (today) {
            drawable.setStroke(dp(2), ContextCompat.getColor(requireContext(), R.color.coral));
        }
        return drawable;
    }

    private long normalizedDayKey(Calendar calendar) {
        Calendar normalized = (Calendar) calendar.clone();
        normalized.set(Calendar.HOUR_OF_DAY, 0);
        normalized.set(Calendar.MINUTE, 0);
        normalized.set(Calendar.SECOND, 0);
        normalized.set(Calendar.MILLISECOND, 0);
        return normalized.getTimeInMillis() / (1000L * 60L * 60L * 24L);
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) return "";
        return value.substring(0, 1).toUpperCase(Locale.getDefault()) + value.substring(1);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
