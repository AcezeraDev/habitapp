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

    private static final String[] STATUS_LABELS = {"Todos", "Pendentes", "Concluídos"};
    private static final String[] STATUS_VALUES = {"Todos", "Pendentes", "Concluidos"};
    private static final String[] CATEGORY_LABELS = {"Todas", "Rotina", "Saúde", "Foco", "Movimento", "Sono"};
    private static final String[] CATEGORY_FILTER_VALUES = {"Todas", "Rotina", "Saude", "Foco", "Movimento", "Sono"};
    private static final String[] FREQUENCY_LABELS = {"Todas", "Diário", "Dias úteis", "3x semana", "Semanal"};
    private static final String[] FREQUENCY_FILTER_VALUES = {"Todas", "Diario", "Dias uteis", "3x semana", "Semanal"};
    private static final String[] CATEGORY_VALUES = {"Rotina", "Saude", "Foco", "Movimento", "Sono"};
    private static final String[] FREQUENCY_VALUES = {"Diario", "Dias uteis", "3x semana", "Semanal"};
    private static final String[] COLORS = {"Azul", "Agua", "Verde", "Roxo", "Amarelo", "Coral"};
    private static final String[] ICONS = {"Estudo", "Livro", "Agua", "Movimento", "Foco", "Sono", "Sol", "Lua", "Fogo", "Trofeu", "Historico", "Perfil"};

    private SharedPreferences prefs;
    private LinearLayout listLayout;
    private LinearLayout emptyLayout;
    private TextView summary;
    private View rootView;
    private String statusFilter = STATUS_VALUES[0];
    private String categoryFilter = CATEGORY_FILTER_VALUES[0];
    private String frequencyFilter = FREQUENCY_FILTER_VALUES[0];

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

        setupChips(view.findViewById(R.id.chipStatusGroup), STATUS_LABELS, STATUS_VALUES, value -> {
            statusFilter = value;
            renderHabits();
        });
        setupChips(view.findViewById(R.id.chipCategoryGroup), CATEGORY_LABELS, CATEGORY_FILTER_VALUES, value -> {
            categoryFilter = value;
            renderHabits();
        });
        setupChips(view.findViewById(R.id.chipFrequencyGroup), FREQUENCY_LABELS, FREQUENCY_FILTER_VALUES, value -> {
            frequencyFilter = value;
            renderHabits();
        });

        view.findViewById(R.id.btnCreateHabit).setOnClickListener(v -> showHabitDialog(null));
        view.findViewById(R.id.btnCreateHabitEmpty).setOnClickListener(v -> showHabitDialog(null));

        renderHabits();
        UiAnimator.enter(view);
    }

    private void setupChips(ChipGroup group, String[] labels, String[] values, FilterListener listener) {
        group.removeAllViews();
        for (int i = 0; i < values.length; i++) {
            String label = labels[i];
            String value = values[i];
            Chip chip = new Chip(requireContext());
            chip.setText(label);
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
        summary.setText(completed + " de " + habits.size() + " hábitos concluídos hoje | " + HabitStore.getStreak(prefs) + " dias fortes");

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
            TextView emptyFiltered = HabitUi.text(requireContext(), "Nenhum hábito aparece com esses filtros.", 14, R.color.muted, false);
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
                    FeedbackHelper.snack(rootView, !done ? "Hábito concluído." : "Hábito reaberto.");
                    UiAnimator.complete(v);
                    renderHabits();
                },
                v -> showHabitDialog(habit),
                v -> confirmDelete(habit),
                v -> HabitDetailDialog.show(requireContext(), prefs, rootView, habit, this::renderHabits)
        );
        HabitUi.addWithBottomMargin(listLayout, card, 12);
        UiAnimator.enterDelayed(card, Math.min(index, 8) * 40L);
    }

    private MaterialButton iconAction(int iconRes, int tintRes, String label) {
        MaterialButton button = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText("");
        button.setContentDescription(label);
        HabitUi.tooltip(button, label);
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
                isEditing ? "Editar hábito" : "Novo hábito",
                "Defina nome, cor, ícone e frequência sem sair do fluxo.",
                R.drawable.ic_nav_goals,
                R.color.primary
        ));

        TextInputLayout nameLayout = input(form, "Nome do hábito", isEditing ? editing.name : "");
        TextInputLayout descriptionLayout = input(form, "Descrição", isEditing ? editing.description : "");
        TextInputLayout timeLayout = input(form, "Horário", isEditing ? editing.time : "");
        Spinner category = spinner(form, "Categoria", CATEGORY_VALUES, isEditing ? editing.category : "Rotina");
        Spinner frequency = spinner(form, "Frequência", FREQUENCY_VALUES, isEditing ? editing.frequency : "Diario");
        Spinner color = spinner(form, "Cor", COLORS, isEditing ? editing.colorName : "Azul");
        Spinner icon = spinner(form, "Ícone", ICONS, isEditing ? editing.iconName : "Estudo");
        if (!isEditing) {
            addTemplates(form, nameLayout, descriptionLayout, timeLayout, category, frequency, color, icon);
        }

        SwitchMaterial reminder = new SwitchMaterial(requireContext());
        reminder.setText("Ativar lembrete deste hábito");
        reminder.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink));
        reminder.setPadding(HabitUi.dp(requireContext(), 12), HabitUi.dp(requireContext(), 8), HabitUi.dp(requireContext(), 12), HabitUi.dp(requireContext(), 8));
        reminder.setBackground(HabitUi.rounded(requireContext(), R.color.primary_soft, R.color.line, 1, 18));
        reminder.setChecked(isEditing && editing.reminder);
        form.addView(reminder);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEditing ? "Editar hábito" : "Criar hábito")
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
                nameLayout.setError("Esse hábito já existe.");
                return;
            }

            String description = text(descriptionLayout);
            if (TextUtils.isEmpty(description)) {
                description = HabitStore.buildSuggestedRecord(name).description;
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
            FeedbackHelper.snack(rootView, isEditing ? "Hábito atualizado." : "Hábito criado.");
            renderHabits();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void addTemplates(LinearLayout parent, TextInputLayout nameLayout, TextInputLayout descriptionLayout, TextInputLayout timeLayout,
                              Spinner category, Spinner frequency, Spinner color, Spinner icon) {
        TextView label = HabitUi.text(requireContext(), "Modelos inteligentes", 12, R.color.muted, true);
        label.setPadding(0, HabitUi.dp(requireContext(), 4), 0, HabitUi.dp(requireContext(), 8));
        parent.addView(label);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 0, 0, HabitUi.dp(requireContext(), 8));
        parent.addView(row);

        row.addView(templateButton("Beber água", R.drawable.ic_premium_drop, R.color.water,
                nameLayout, descriptionLayout, timeLayout, category, frequency, color, icon));
        row.addView(templateButton("Estudar 30 min", R.drawable.ic_nav_focus, R.color.study,
                nameLayout, descriptionLayout, timeLayout, category, frequency, color, icon));
        row.addView(templateButton("Fazer exercício", R.drawable.ic_premium_dumbbell, R.color.coral,
                nameLayout, descriptionLayout, timeLayout, category, frequency, color, icon));
        row.addView(templateButton("Dormir cedo", R.drawable.ic_premium_sleep, R.color.success,
                nameLayout, descriptionLayout, timeLayout, category, frequency, color, icon));
        row.addView(templateButton("Ler 10 páginas", R.drawable.ic_premium_book, R.color.primary,
                nameLayout, descriptionLayout, timeLayout, category, frequency, color, icon));
    }

    private MaterialButton templateButton(String title, int iconRes, int colorRes, TextInputLayout nameLayout,
                                          TextInputLayout descriptionLayout, TextInputLayout timeLayout,
                                          Spinner category, Spinner frequency, Spinner color, Spinner icon) {
        MaterialButton button = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText(title);
        button.setAllCaps(false);
        button.setIconResource(iconRes);
        button.setIconTint(HabitUi.isPremiumIcon(iconRes) ? null : ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorRes)));
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink));
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), softForColor(colorRes))));
        button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorRes)));
        button.setCornerRadius(HabitUi.dp(requireContext(), 18));
        HabitUi.press(button);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HabitUi.dp(requireContext(), 48));
        params.setMargins(0, 0, 0, HabitUi.dp(requireContext(), 8));
        button.setLayoutParams(params);
        button.setOnClickListener(v -> applyTemplate(title, nameLayout, descriptionLayout, timeLayout, category, frequency, color, icon));
        return button;
    }

    private void applyTemplate(String title, TextInputLayout nameLayout, TextInputLayout descriptionLayout, TextInputLayout timeLayout,
                               Spinner category, Spinner frequency, Spinner color, Spinner icon) {
        HabitRecord suggestion = HabitStore.buildSuggestedRecord(title);
        setText(nameLayout, suggestion.name);
        setText(descriptionLayout, suggestion.description);
        setText(timeLayout, suggestion.time);
        selectSpinner(category, suggestion.category);
        selectSpinner(frequency, suggestion.frequency);
        selectSpinner(color, suggestion.colorName);
        selectSpinner(icon, suggestion.iconName);
    }

    private void setText(TextInputLayout layout, String value) {
        if (layout.getEditText() != null) {
            layout.getEditText().setText(value);
        }
    }

    private void selectSpinner(Spinner spinner, String value) {
        if (spinner.getAdapter() == null) return;
        for (int position = 0; position < spinner.getAdapter().getCount(); position++) {
            Object item = spinner.getAdapter().getItem(position);
            if (value.equals(String.valueOf(item))) {
                spinner.setSelection(position);
                return;
            }
        }
    }

    private TextInputLayout input(LinearLayout parent, String hint, String value) {
        TextInputLayout layout = new TextInputLayout(requireContext());
        layout.setHint(hint);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        float radius = HabitUi.dp(requireContext(), 18);
        layout.setBoxCornerRadii(radius, radius, radius, radius);

        TextInputEditText editText = new TextInputEditText(requireContext());
        editText.setSingleLine(!"Descrição".equals(hint));
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
                .setTitle("Excluir hábito")
                .setMessage("Remover \"" + habit.name + "\" da sua rotina?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    HabitStore.removeHabitRecord(prefs, habit.name);
                    FeedbackHelper.snack(rootView, "Hábito removido.");
                    renderHabits();
                })
                .show();
    }

    private interface FilterListener {
        void onSelected(String value);
    }
}
