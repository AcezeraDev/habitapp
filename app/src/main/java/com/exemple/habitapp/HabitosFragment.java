package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class HabitosFragment extends Fragment {

    private static final String[] STATUS = {"Todos", "Pendentes", "Concluidos"};
    private static final String[] CATEGORIES = {"Todas", "Rotina", "Saude", "Foco", "Movimento", "Sono"};
    private static final String[] FREQUENCIES = {"Todas", "Diario", "Dias uteis", "3x semana", "Semanal"};
    private static final String[] CATEGORY_VALUES = {"Rotina", "Saude", "Foco", "Movimento", "Sono"};
    private static final String[] FREQUENCY_VALUES = {"Diario", "Dias uteis", "3x semana", "Semanal"};
    private static final String[] COLORS = {"Azul", "Agua", "Verde", "Roxo", "Amarelo", "Coral"};
    private static final String[] ICONS = {"Estudo", "Agua", "Movimento", "Foco", "Sono", "Sol", "Lua", "Fogo", "Trofeu", "Historico", "Perfil"};

    private SharedPreferences prefs;
    private LinearLayout listLayout;
    private LinearLayout emptyLayout;
    private TextView summary;
    private View rootView;
    private String statusFilter = STATUS[0];
    private String categoryFilter = CATEGORIES[0];
    private String frequencyFilter = FREQUENCIES[0];

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habitos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);

        summary = view.findViewById(R.id.txtHabitSummary);
        listLayout = view.findViewById(R.id.layoutHabitList);
        emptyLayout = view.findViewById(R.id.layoutHabitEmpty);

        setupChips(view.findViewById(R.id.chipStatusGroup), STATUS, value -> {
            statusFilter = value;
            renderHabits();
        });
        setupChips(view.findViewById(R.id.chipCategoryGroup), CATEGORIES, value -> {
            categoryFilter = value;
            renderHabits();
        });
        setupChips(view.findViewById(R.id.chipFrequencyGroup), FREQUENCIES, value -> {
            frequencyFilter = value;
            renderHabits();
        });

        view.findViewById(R.id.btnCreateHabit).setOnClickListener(v -> showHabitDialog(null));
        view.findViewById(R.id.btnCreateHabitEmpty).setOnClickListener(v -> showHabitDialog(null));

        renderHabits();
        UiAnimator.enter(view);
    }

    private void setupChips(ChipGroup group, String[] values, FilterListener listener) {
        group.removeAllViews();
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            Chip chip = new Chip(requireContext());
            chip.setText(value);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_checked},
                    new int[]{}
            };
            chip.setTextColor(new ColorStateList(states, new int[]{
                    ContextCompat.getColor(requireContext(), R.color.primary),
                    ContextCompat.getColor(requireContext(), R.color.ink)
            }));
            chip.setChipBackgroundColor(new ColorStateList(states, new int[]{
                    ContextCompat.getColor(requireContext(), R.color.primary_soft),
                    ContextCompat.getColor(requireContext(), R.color.surface_soft)
            }));
            chip.setChipStrokeColor(new ColorStateList(states, new int[]{
                    ContextCompat.getColor(requireContext(), R.color.primary),
                    ContextCompat.getColor(requireContext(), R.color.line)
            }));
            chip.setChipStrokeWidth(HabitUi.dp(requireContext(), 1));
            chip.setOnClickListener(v -> {
                for (int index = 0; index < group.getChildCount(); index++) {
                    View child = group.getChildAt(index);
                    if (child instanceof Chip) {
                        ((Chip) child).setChecked(child == v);
                    }
                }
                listener.onSelected(value);
            });
            group.addView(chip);
        }
    }

    private void renderHabits() {
        List<HabitRecord> habits = HabitStore.getHabitRecords(prefs);
        int completed = 0;
        for (HabitRecord habit : habits) {
            if (HabitStore.isHabitDoneToday(prefs, habit.name)) completed++;
        }
        summary.setText(completed + " de " + habits.size() + " habitos concluidos hoje | " + HabitStore.getStreak(prefs) + " dias fortes");

        listLayout.removeAllViews();
        int visible = 0;

        for (HabitRecord habit : habits) {
            if (!matchesFilters(habit)) continue;
            addHabitCard(habit, visible);
            visible++;
        }

        boolean hasNoHabits = habits.isEmpty();
        emptyLayout.setVisibility(hasNoHabits ? View.VISIBLE : View.GONE);

        if (!hasNoHabits && visible == 0) {
            TextView emptyFiltered = HabitUi.text(requireContext(), "Nenhum habito aparece com esses filtros.", 14, R.color.muted, false);
            emptyFiltered.setGravity(Gravity.CENTER);
            emptyFiltered.setPadding(0, HabitUi.dp(requireContext(), 18), 0, HabitUi.dp(requireContext(), 18));
            listLayout.addView(emptyFiltered);
        }
    }

    private boolean matchesFilters(HabitRecord habit) {
        boolean done = HabitStore.isHabitDoneToday(prefs, habit.name);
        if ("Pendentes".equals(statusFilter) && done) return false;
        if ("Concluidos".equals(statusFilter) && !done) return false;
        if (!"Todas".equals(categoryFilter) && !categoryFilter.equals(habit.category)) return false;
        return "Todas".equals(frequencyFilter) || frequencyFilter.equals(habit.frequency);
    }

    private void addHabitCard(HabitRecord habit, int index) {
        boolean done = HabitStore.isHabitDoneToday(prefs, habit.name);
        com.google.android.material.card.MaterialCardView card = HabitComponents.habitCard(
                requireContext(),
                habit,
                done,
                HabitStore.getHabitStreak(prefs, habit.name),
                v -> {
                    HabitStore.setHabitDoneToday(prefs, habit.name, !done);
                    FeedbackHelper.success(requireContext());
                    if (!done) CelebrationView.burst(rootView);
                    FeedbackHelper.snack(rootView, !done ? "Habito concluido." : "Habito reaberto.");
                    UiAnimator.complete(v);
                    renderHabits();
                },
                v -> showHabitDialog(habit),
                v -> confirmDelete(habit)
        );
        HabitUi.addWithBottomMargin(listLayout, card, 12);
        UiAnimator.enterDelayed(card, Math.min(index, 8) * 40L);
    }

    private MaterialButton iconAction(int iconRes, int tintRes, String label) {
        MaterialButton button = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText("");
        button.setContentDescription(label);
        button.setTooltipText(label);
        button.setIconResource(iconRes);
        button.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), tintRes)));
        button.setIconPadding(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setCornerRadius(HabitUi.dp(requireContext(), 8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(HabitUi.dp(requireContext(), 48), HabitUi.dp(requireContext(), 48));
        params.setMargins(HabitUi.dp(requireContext(), 8), 0, 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private int softForColor(int colorRes) {
        if (colorRes == R.color.water) return R.color.water_soft;
        if (colorRes == R.color.study) return R.color.study_soft;
        if (colorRes == R.color.success) return R.color.success_soft;
        if (colorRes == R.color.coral || colorRes == R.color.warning) return R.color.coral_soft;
        return R.color.primary_soft;
    }

    private void showHabitDialog(HabitRecord editing) {
        boolean isEditing = editing != null;
        ScrollView scroll = new ScrollView(requireContext());
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = HabitUi.dp(requireContext(), 20);
        form.setPadding(padding, padding, padding, padding);
        form.setBackground(HabitUi.rounded(requireContext(), R.color.surface, R.color.line, 1, 24));
        scroll.addView(form);

        form.addView(HabitComponents.sectionTitle(
                requireContext(),
                isEditing ? "Editar habito" : "Novo habito",
                "Defina nome, cor, icone e frequencia sem sair do fluxo.",
                R.drawable.ic_nav_goals,
                R.color.primary
        ));

        TextInputLayout nameLayout = input(form, "Nome do habito", isEditing ? editing.name : "");
        TextInputLayout descriptionLayout = input(form, "Descricao", isEditing ? editing.description : "");
        TextInputLayout timeLayout = input(form, "Horario", isEditing ? editing.time : "");
        Spinner category = spinner(form, "Categoria", CATEGORY_VALUES, isEditing ? editing.category : "Rotina");
        Spinner frequency = spinner(form, "Frequencia", FREQUENCY_VALUES, isEditing ? editing.frequency : "Diario");
        Spinner color = spinner(form, "Cor", COLORS, isEditing ? editing.colorName : "Azul");
        Spinner icon = spinner(form, "Icone", ICONS, isEditing ? editing.iconName : "Estudo");

        SwitchMaterial reminder = new SwitchMaterial(requireContext());
        reminder.setText("Ativar lembrete deste habito");
        reminder.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink));
        reminder.setPadding(HabitUi.dp(requireContext(), 12), HabitUi.dp(requireContext(), 8), HabitUi.dp(requireContext(), 12), HabitUi.dp(requireContext(), 8));
        reminder.setBackground(HabitUi.rounded(requireContext(), R.color.primary_soft, R.color.line, 1, 18));
        reminder.setChecked(isEditing && editing.reminder);
        form.addView(reminder);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEditing ? "Editar habito" : "Criar habito")
                .setView(scroll)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = text(nameLayout);
            String oldName = isEditing ? editing.name : null;
            if (TextUtils.isEmpty(name)) {
                nameLayout.setError("Informe um nome.");
                return;
            }

            name = HabitStore.sanitizeHabitName(name);
            List<String> existing = HabitStore.getCustomHabits(prefs);
            if ((oldName == null || !oldName.equals(name)) && existing.contains(name)) {
                nameLayout.setError("Esse habito ja existe.");
                return;
            }

            String description = text(descriptionLayout);
            if (TextUtils.isEmpty(description)) {
                description = "Uma acao pequena para manter consistencia hoje.";
            }

            HabitRecord record = new HabitRecord(
                    name,
                    description,
                    value(category),
                    value(frequency),
                    text(timeLayout),
                    reminder.isChecked(),
                    value(color),
                    value(icon)
            );
            HabitStore.saveHabitRecord(prefs, oldName, record);
            FeedbackHelper.snack(rootView, isEditing ? "Habito atualizado." : "Habito criado.");
            renderHabits();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private TextInputLayout input(LinearLayout parent, String hint, String value) {
        TextInputLayout layout = new TextInputLayout(requireContext());
        layout.setHint(hint);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        float radius = HabitUi.dp(requireContext(), 18);
        layout.setBoxCornerRadii(radius, radius, radius, radius);

        TextInputEditText editText = new TextInputEditText(requireContext());
        editText.setSingleLine(!"Descricao".equals(hint));
        editText.setText(value);
        layout.addView(editText);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, HabitUi.dp(requireContext(), 12));
        parent.addView(layout, params);
        return layout;
    }

    private Spinner spinner(LinearLayout parent, String label, String[] values, String selected) {
        TextView text = HabitUi.text(requireContext(), label, 12, R.color.muted, true);
        parent.addView(text);

        Spinner spinner = new Spinner(requireContext());
        spinner.setPadding(HabitUi.dp(requireContext(), 10), HabitUi.dp(requireContext(), 8), HabitUi.dp(requireContext(), 10), HabitUi.dp(requireContext(), 8));
        spinner.setBackground(HabitUi.rounded(requireContext(), R.color.surface_tint, R.color.line, 1, 18));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, values);
        spinner.setAdapter(adapter);
        spinner.setSelection(Math.max(0, adapter.getPosition(selected)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, HabitUi.dp(requireContext(), 12));
        parent.addView(spinner, params);
        return spinner;
    }

    private String text(TextInputLayout layout) {
        if (layout.getEditText() == null || layout.getEditText().getText() == null) return "";
        return layout.getEditText().getText().toString().trim();
    }

    private String value(Spinner spinner) {
        Object selected = spinner.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    private void confirmDelete(HabitRecord habit) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir habito")
                .setMessage("Remover \"" + habit.name + "\" da sua rotina?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    HabitStore.removeHabitRecord(prefs, habit.name);
                    FeedbackHelper.snack(rootView, "Habito removido.");
                    renderHabits();
                })
                .show();
    }

    private interface FilterListener {
        void onSelected(String value);
    }
}
