package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class ConfiguracoesFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView txtSummary;
    private TextView txtThemeSummary;
    private SwitchMaterial switchWater;
    private SwitchMaterial switchFocus;
    private SwitchMaterial switchRoutine;
    private SwitchMaterial switchDarkMode;
    private TextInputEditText inputWaterStart;
    private TextInputEditText inputWaterInterval;
    private TextInputEditText inputFocusTime;
    private TextInputEditText inputRoutineTime;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuracoes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        txtSummary = view.findViewById(R.id.txtReminderSummary);
        txtThemeSummary = view.findViewById(R.id.txtThemeSummary);
        switchWater = view.findViewById(R.id.switchWaterReminder);
        switchFocus = view.findViewById(R.id.switchFocusReminder);
        switchRoutine = view.findViewById(R.id.switchRoutineReminder);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        inputWaterStart = view.findViewById(R.id.inputWaterStart);
        inputWaterInterval = view.findViewById(R.id.inputWaterInterval);
        inputFocusTime = view.findViewById(R.id.inputFocusTime);
        inputRoutineTime = view.findViewById(R.id.inputRoutineTime);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveReminders);
        MaterialButton btnTest = view.findViewById(R.id.btnTestReminder);

        loadSavedValues();
        btnSave.setOnClickListener(v -> saveValues());
        btnTest.setOnClickListener(v -> {
            NotificationHelper.showReminder(requireContext(), ReminderScheduler.TYPE_WATER);
            Toast.makeText(requireContext(), "Notificacao de teste enviada.", Toast.LENGTH_SHORT).show();
        });
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeController.setDarkMode(requireContext(), isChecked);
            updateThemeSummary();
            Toast.makeText(requireContext(), isChecked ? "Modo escuro ativado." : "Modo claro ativado.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadSavedValues() {
        switchWater.setChecked(prefs.getBoolean(ReminderScheduler.PREF_WATER_ENABLED, true));
        switchFocus.setChecked(prefs.getBoolean(ReminderScheduler.PREF_FOCUS_ENABLED, true));
        switchRoutine.setChecked(prefs.getBoolean(ReminderScheduler.PREF_ROUTINE_ENABLED, true));
        switchDarkMode.setChecked(ThemeController.isDarkMode(requireContext()));
        inputWaterStart.setText(formatTime(
                prefs.getInt(ReminderScheduler.PREF_WATER_START_HOUR, 10),
                prefs.getInt(ReminderScheduler.PREF_WATER_START_MINUTE, 0)
        ));
        inputWaterInterval.setText(String.valueOf(prefs.getInt(ReminderScheduler.PREF_WATER_INTERVAL_HOURS, 2)));
        inputFocusTime.setText(formatTime(
                prefs.getInt(ReminderScheduler.PREF_FOCUS_HOUR, 16),
                prefs.getInt(ReminderScheduler.PREF_FOCUS_MINUTE, 30)
        ));
        inputRoutineTime.setText(formatTime(
                prefs.getInt(ReminderScheduler.PREF_ROUTINE_HOUR, 21),
                prefs.getInt(ReminderScheduler.PREF_ROUTINE_MINUTE, 0)
        ));
        updateSummary();
        updateThemeSummary();
    }

    private void saveValues() {
        int[] waterTime = parseTime(inputWaterStart);
        int[] focusTime = parseTime(inputFocusTime);
        int[] routineTime = parseTime(inputRoutineTime);
        Integer interval = parseInterval(inputWaterInterval);

        if (waterTime == null || focusTime == null || routineTime == null || interval == null) {
            Toast.makeText(requireContext(), "Revise horarios e intervalo.", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit()
                .putBoolean(ReminderScheduler.PREF_WATER_ENABLED, switchWater.isChecked())
                .putBoolean(ReminderScheduler.PREF_FOCUS_ENABLED, switchFocus.isChecked())
                .putBoolean(ReminderScheduler.PREF_ROUTINE_ENABLED, switchRoutine.isChecked())
                .putInt(ReminderScheduler.PREF_WATER_START_HOUR, waterTime[0])
                .putInt(ReminderScheduler.PREF_WATER_START_MINUTE, waterTime[1])
                .putInt(ReminderScheduler.PREF_WATER_INTERVAL_HOURS, interval)
                .putInt(ReminderScheduler.PREF_FOCUS_HOUR, focusTime[0])
                .putInt(ReminderScheduler.PREF_FOCUS_MINUTE, focusTime[1])
                .putInt(ReminderScheduler.PREF_ROUTINE_HOUR, routineTime[0])
                .putInt(ReminderScheduler.PREF_ROUTINE_MINUTE, routineTime[1])
                .apply();

        ReminderScheduler.scheduleDefaultReminders(requireContext());
        updateSummary();
        Toast.makeText(requireContext(), "Lembretes salvos.", Toast.LENGTH_SHORT).show();
    }

    private void updateSummary() {
        String water = switchWater.isChecked() ? "agua a partir de " + text(inputWaterStart) : "agua desligada";
        String focus = switchFocus.isChecked() ? "foco as " + text(inputFocusTime) : "foco desligado";
        String routine = switchRoutine.isChecked() ? "rotina as " + text(inputRoutineTime) : "rotina desligada";
        txtSummary.setText(water + " | " + focus + " | " + routine);
    }

    private void updateThemeSummary() {
        txtThemeSummary.setText(switchDarkMode.isChecked()
                ? "Tema escuro ativo para reduzir brilho e deixar o app mais confortavel."
                : "Tema claro ativo para uso durante o dia.");
    }

    private int[] parseTime(TextInputEditText input) {
        String value = text(input);
        if (TextUtils.isEmpty(value) || !value.contains(":")) return null;
        String[] parts = value.split(":");
        if (parts.length != 2) return null;

        try {
            int hour = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;
            return new int[]{hour, minute};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInterval(TextInputEditText input) {
        try {
            int interval = Integer.parseInt(text(input));
            if (interval < 1 || interval > 8) return null;
            return interval;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    private String text(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }
}
