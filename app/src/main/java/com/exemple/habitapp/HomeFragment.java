package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String CUSTOM_HABITS_KEY = "custom_habits";

    private SharedPreferences prefs;
    private ActivityResultLauncher<Void> cameraLauncher;
    private LinearLayout layoutGaleria;
    private LinearLayout layoutHabitosExtras;
    private LinearLayout layoutWeekBarsHome;
    private TextInputEditText inputNovoHabito;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        rootView = view;

        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);
        layoutGaleria = view.findViewById(R.id.layoutGaleria);
        layoutHabitosExtras = view.findViewById(R.id.layoutHabitosExtras);
        layoutWeekBarsHome = view.findViewById(R.id.layoutWeekBarsHome);
        inputNovoHabito = view.findViewById(R.id.inputNovoHabito);

        configurarAcoesRapidas(view);
        configurarChecklist(view);
        configurarCheckin(view);
        configurarHabitosExtras(view);
        configurarCamera(view);
        atualizarResumo(view);
        atualizarGaleria();
        renderHabitosExtras();
        renderWeekBars(layoutWeekBarsHome);

        return view;
    }

    private void configurarCamera(View view) {
        MaterialButton btnFoto = view.findViewById(R.id.btnFotoDia);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                result -> {
                    if (result != null) {
                        salvarFotoDoDia(result);
                        atualizarResumo(rootView);
                        atualizarGaleria();
                    } else {
                        Toast.makeText(getContext(), "Nao foi possivel tirar a foto.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnFoto.setOnClickListener(v -> {
            if (jaTirouFotoHoje()) {
                Toast.makeText(getContext(), "Voce ja registrou a foto de hoje.", Toast.LENGTH_SHORT).show();
            } else {
                cameraLauncher.launch(null);
            }
        });
    }

    private void atualizarResumo(View view) {
        TextView txtGreeting = view.findViewById(R.id.txtGreeting);
        TextView txtScore = view.findViewById(R.id.txtScore);
        TextView txtScoreLabel = view.findViewById(R.id.txtScoreLabel);
        TextView txtAguaHome = view.findViewById(R.id.txtAguaHome);
        TextView txtAguaFalta = view.findViewById(R.id.txtAguaFalta);
        TextView txtEstudosHome = view.findViewById(R.id.txtEstudosHome);
        TextView txtEstudosFalta = view.findViewById(R.id.txtEstudosFalta);
        TextView txtChecklistHome = view.findViewById(R.id.txtChecklistHome);
        TextView txtChallengeStatus = view.findViewById(R.id.txtChallengeStatus);
        TextView txtPlanoTitulo = view.findViewById(R.id.txtPlanoTitulo);
        TextView txtPlanoDescricao = view.findViewById(R.id.txtPlanoDescricao);
        TextView txtCheckinResumo = view.findViewById(R.id.txtCheckinResumo);
        TextView txtHabitosResumo = view.findViewById(R.id.txtHabitosResumo);
        TextView txtStreakHome = view.findViewById(R.id.txtStreakHome);
        TextView txtMediaHome = view.findViewById(R.id.txtMediaHome);
        TextView txtNivelHome = view.findViewById(R.id.txtNivelHome);
        LinearProgressIndicator progressDaily = view.findViewById(R.id.progressDaily);
        LinearProgressIndicator progressAguaHome = view.findViewById(R.id.progressAguaHome);
        LinearProgressIndicator progressEstudosHome = view.findViewById(R.id.progressEstudosHome);
        LinearProgressIndicator progressChecklistHome = view.findViewById(R.id.progressChecklistHome);

        double aguaLitros = prefs.getFloat("agua_litros", 0f);
        double metaLitros = prefs.getFloat("meta_litros", 2.0f);
        int estudosFeitos = prefs.getInt("estudos_concluidos_min", 0);
        int metaEstudos = prefs.getInt("meta_estudos_min", 60);
        int diaAtual = prefs.getInt("challenge_day", 1);
        int metaDias = prefs.getInt("challenge_goal_days", 30);
        String nome = prefs.getString("nome_usuario", "Guerreiro");

        int aguaMl = (int) Math.round(aguaLitros * 1000);
        int metaMl = (int) Math.round(metaLitros * 1000);
        int faltaAgua = Math.max(0, metaMl - aguaMl);
        int faltaEstudos = Math.max(0, metaEstudos - estudosFeitos);
        int aguaPercent = calcularPercentual(aguaMl, metaMl);
        int estudoPercent = calcularPercentual(estudosFeitos, metaEstudos);
        int checklistConcluido = getChecklistConcluido();
        int checklistPercent = calcularPercentual(checklistConcluido, 3);
        List<String> habitos = getCustomHabits();
        int habitosConcluidos = getHabitosExtrasConcluidos(habitos);
        int habitosPercent = habitos.isEmpty() ? 100 : calcularPercentual(habitosConcluidos, habitos.size());
        int score = HabitStore.getTodayScore(prefs);
        int streak = HabitStore.getStreak(prefs);
        int weeklyAverage = HabitStore.getWeeklyAverage(prefs);

        txtGreeting.setText(getSaudacao() + ", " + nome);
        txtScore.setText(score + "%");
        txtScoreLabel.setText(score >= 100 ? "Dia fechado com consistencia" : "Proxima meta: " + proximaAcao(faltaAgua, faltaEstudos));
        txtAguaHome.setText(aguaMl + " / " + metaMl + " ml");
        txtAguaFalta.setText(faltaAgua == 0 ? "Meta de agua concluida" : "Faltam " + faltaAgua + " ml");
        txtEstudosHome.setText(estudosFeitos + " / " + metaEstudos + " min");
        txtEstudosFalta.setText(faltaEstudos == 0 ? "Meta de foco concluida" : "Faltam " + faltaEstudos + " min");
        txtChecklistHome.setText(checklistConcluido + " de 3 concluidos");
        txtChallengeStatus.setText("Dia " + Math.min(diaAtual, metaDias) + " de " + metaDias);
        txtPlanoTitulo.setText("Plano inteligente para " + getMelhorHorario());
        txtPlanoDescricao.setText(criarPlanoDoDia(faltaAgua, faltaEstudos, checklistConcluido));
        txtCheckinResumo.setText("Humor: " + labelNivel(getMood()) + "  |  Energia: " + labelNivel(getEnergy()));
        txtHabitosResumo.setText(habitos.isEmpty()
                ? "Crie habitos pequenos para acompanhar hoje."
                : habitosConcluidos + " de " + habitos.size() + " habitos extras concluidos");
        txtStreakHome.setText(streak + (streak == 1 ? " dia\nstreak" : " dias\nstreak"));
        txtMediaHome.setText(weeklyAverage + "%\nmedia");
        txtNivelHome.setText(HabitStore.getLevelName(prefs) + "\nnivel");

        progressDaily.setProgressCompat(score, true);
        progressAguaHome.setProgressCompat(aguaPercent, true);
        progressEstudosHome.setProgressCompat(estudoPercent, true);
        progressChecklistHome.setProgressCompat(checklistPercent, true);
    }

    private void configurarAcoesRapidas(View view) {
        MaterialButton btnQuickWater = view.findViewById(R.id.btnQuickWater);
        MaterialButton btnQuickFocus = view.findViewById(R.id.btnQuickFocus);

        btnQuickWater.setOnClickListener(v -> {
            double atual = prefs.getFloat("agua_litros", 0f);
            double meta = prefs.getFloat("meta_litros", 2.0f);
            double novoValor = Math.min(meta, atual + 0.25);
            int adicionadoMl = (int) Math.round(Math.max(0, novoValor - atual) * 1000);

            if (adicionadoMl == 0) {
                Toast.makeText(getContext(), "Meta de agua ja concluida.", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit().putFloat("agua_litros", (float) novoValor).apply();
            registrarAgua(adicionadoMl);
            HabitStore.saveTodaySnapshot(prefs);
            atualizarResumo(rootView);
            renderWeekBars(layoutWeekBarsHome);
            Toast.makeText(getContext(), "+" + adicionadoMl + " ml adicionados.", Toast.LENGTH_SHORT).show();
        });

        btnQuickFocus.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(R.id.estudos);
            }
        });
    }

    private void configurarChecklist(View view) {
        CheckBox checkPlanejamento = view.findViewById(R.id.checkPlanejamento);
        CheckBox checkTreino = view.findViewById(R.id.checkTreino);
        CheckBox checkSono = view.findViewById(R.id.checkSono);
        long hoje = getTodayKey();

        checkPlanejamento.setChecked(prefs.getBoolean("check_planejamento_" + hoje, false));
        checkTreino.setChecked(prefs.getBoolean("check_treino_" + hoje, false));
        checkSono.setChecked(prefs.getBoolean("check_sono_" + hoje, false));

        checkPlanejamento.setOnCheckedChangeListener((buttonView, isChecked) -> salvarChecklist("check_planejamento_", isChecked));
        checkTreino.setOnCheckedChangeListener((buttonView, isChecked) -> salvarChecklist("check_treino_", isChecked));
        checkSono.setOnCheckedChangeListener((buttonView, isChecked) -> salvarChecklist("check_sono_", isChecked));
    }

    private void configurarCheckin(View view) {
        MaterialButton btnMoodLow = view.findViewById(R.id.btnMoodLow);
        MaterialButton btnMoodOk = view.findViewById(R.id.btnMoodOk);
        MaterialButton btnMoodHigh = view.findViewById(R.id.btnMoodHigh);
        MaterialButton btnEnergyLow = view.findViewById(R.id.btnEnergyLow);
        MaterialButton btnEnergyOk = view.findViewById(R.id.btnEnergyOk);
        MaterialButton btnEnergyHigh = view.findViewById(R.id.btnEnergyHigh);

        btnMoodLow.setOnClickListener(v -> salvarNivel("mood_", 0));
        btnMoodOk.setOnClickListener(v -> salvarNivel("mood_", 1));
        btnMoodHigh.setOnClickListener(v -> salvarNivel("mood_", 2));
        btnEnergyLow.setOnClickListener(v -> salvarNivel("energy_", 0));
        btnEnergyOk.setOnClickListener(v -> salvarNivel("energy_", 1));
        btnEnergyHigh.setOnClickListener(v -> salvarNivel("energy_", 2));

        atualizarGrupoCheckin(btnMoodLow, btnMoodOk, btnMoodHigh, getMood());
        atualizarGrupoCheckin(btnEnergyLow, btnEnergyOk, btnEnergyHigh, getEnergy());
    }

    private void configurarHabitosExtras(View view) {
        MaterialButton btnAddHabito = view.findViewById(R.id.btnAddHabito);

        btnAddHabito.setOnClickListener(v -> {
            String novoHabito = inputNovoHabito.getText() != null
                    ? inputNovoHabito.getText().toString().trim()
                    : "";

            if (TextUtils.isEmpty(novoHabito)) {
                Toast.makeText(getContext(), "Digite um habito para adicionar.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> habitos = getCustomHabits();
            if (habitos.contains(novoHabito)) {
                Toast.makeText(getContext(), "Esse habito ja existe.", Toast.LENGTH_SHORT).show();
                return;
            }

            habitos.add(novoHabito.replace("\n", " ").replace("|", "/"));
            salvarCustomHabits(habitos);
            inputNovoHabito.setText("");
            HabitStore.saveTodaySnapshot(prefs);
            renderHabitosExtras();
            atualizarResumo(rootView);
            renderWeekBars(layoutWeekBarsHome);
        });
    }

    private void renderHabitosExtras() {
        if (layoutHabitosExtras == null) return;

        layoutHabitosExtras.removeAllViews();
        List<String> habitos = getCustomHabits();

        if (habitos.isEmpty()) {
            TextView vazio = new TextView(getContext());
            vazio.setText("Nenhum habito extra ainda.");
            vazio.setTextColor(0xFF667085);
            vazio.setTextSize(14f);
            layoutHabitosExtras.addView(vazio);
            return;
        }

        for (String habito : habitos) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(habito);
            checkBox.setTextColor(0xFF101828);
            checkBox.setTextSize(15f);
            checkBox.setChecked(isHabitoConcluido(habito));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(getHabitoKey(habito), isChecked).apply();
                HabitStore.saveTodaySnapshot(prefs);
                atualizarResumo(rootView);
                renderWeekBars(layoutWeekBarsHome);
            });
            layoutHabitosExtras.addView(checkBox);
        }
    }

    private void renderWeekBars(LinearLayout layout) {
        if (layout == null) return;

        layout.removeAllViews();
        int[] scores = HabitStore.getWeekScores(prefs);

        for (int i = 0; i < scores.length; i++) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 4, 0, 4);

            TextView label = new TextView(requireContext());
            label.setText(i == 6 ? "Hoje" : "-" + (6 - i) + "d");
            label.setTextColor(0xFF667085);
            label.setTextSize(12f);
            row.addView(label, new LinearLayout.LayoutParams(44, ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearProgressIndicator bar = new LinearProgressIndicator(requireContext());
            bar.setMax(100);
            bar.setProgressCompat(scores[i], false);
            bar.setIndicatorColor(0xFF165DFF);
            bar.setTrackColor(0xFFD9E4F2);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(bar, barParams);

            TextView value = new TextView(requireContext());
            value.setText(scores[i] + "%");
            value.setGravity(Gravity.END);
            value.setTextColor(0xFF101828);
            value.setTextSize(12f);
            row.addView(value, new LinearLayout.LayoutParams(48, ViewGroup.LayoutParams.WRAP_CONTENT));

            layout.addView(row);
        }
    }

    private void salvarChecklist(String chave, boolean marcado) {
        prefs.edit().putBoolean(chave + getTodayKey(), marcado).apply();
        HabitStore.saveTodaySnapshot(prefs);
        atualizarResumo(rootView);
        renderWeekBars(layoutWeekBarsHome);
    }

    private void salvarNivel(String prefixo, int nivel) {
        prefs.edit().putInt(prefixo + getTodayKey(), nivel).apply();
        configurarCheckin(rootView);
        HabitStore.saveTodaySnapshot(prefs);
        atualizarResumo(rootView);
        renderWeekBars(layoutWeekBarsHome);
    }

    private int getChecklistConcluido() {
        long hoje = getTodayKey();
        int total = 0;
        if (prefs.getBoolean("check_planejamento_" + hoje, false)) total++;
        if (prefs.getBoolean("check_treino_" + hoje, false)) total++;
        if (prefs.getBoolean("check_sono_" + hoje, false)) total++;
        return total;
    }

    private int getHabitosExtrasConcluidos(List<String> habitos) {
        int total = 0;
        for (String habito : habitos) {
            if (isHabitoConcluido(habito)) total++;
        }
        return total;
    }

    private List<String> getCustomHabits() {
        String salvos = prefs.getString(CUSTOM_HABITS_KEY, "");
        List<String> habitos = new ArrayList<>();
        if (TextUtils.isEmpty(salvos)) return habitos;

        String[] partes = salvos.split("\\|");
        for (String parte : partes) {
            if (!TextUtils.isEmpty(parte.trim())) {
                habitos.add(parte.trim());
            }
        }
        return habitos;
    }

    private void salvarCustomHabits(List<String> habitos) {
        prefs.edit().putString(CUSTOM_HABITS_KEY, TextUtils.join("|", habitos)).apply();
    }

    private boolean isHabitoConcluido(String habito) {
        return prefs.getBoolean(getHabitoKey(habito), false);
    }

    private String getHabitoKey(String habito) {
        return "custom_habit_" + getTodayKey() + "_" + habito.hashCode();
    }

    private int getMood() {
        return prefs.getInt("mood_" + getTodayKey(), -1);
    }

    private int getEnergy() {
        return prefs.getInt("energy_" + getTodayKey(), -1);
    }

    private void atualizarGrupoCheckin(MaterialButton baixo, MaterialButton medio, MaterialButton alto, int nivel) {
        baixo.setAlpha(nivel == 0 ? 1f : 0.55f);
        medio.setAlpha(nivel == 1 ? 1f : 0.55f);
        alto.setAlpha(nivel == 2 ? 1f : 0.55f);
        baixo.setStrokeWidth(nivel == 0 ? 3 : 1);
        medio.setStrokeWidth(nivel == 1 ? 3 : 1);
        alto.setStrokeWidth(nivel == 2 ? 3 : 1);
    }

    private int calcularPercentual(double atual, double meta) {
        if (meta <= 0) return 0;
        return (int) Math.max(0, Math.min(100, Math.round((atual / meta) * 100)));
    }

    private String proximaAcao(int faltaAgua, int faltaEstudos) {
        if (faltaAgua > 0) return "beber agua";
        if (faltaEstudos > 0) return "abrir foco";
        if (getChecklistConcluido() < 3) return "finalizar checklist";
        if (getCustomHabits().size() > getHabitosExtrasConcluidos(getCustomHabits())) return "habito extra";
        return "registrar progresso";
    }

    private String criarPlanoDoDia(int faltaAgua, int faltaEstudos, int checklistConcluido) {
        String objetivo = prefs.getString("objetivo_principal", "Mais disciplina");
        String rotina = prefs.getString("ritmo_rotina", "Equilibrada");

        if (faltaAgua > 0) {
            return objetivo + ": beba 250 ml agora e mantenha a rotina " + rotina.toLowerCase(Locale.ROOT) + ".";
        }

        if (faltaEstudos > 0) {
            return objetivo + ": abra uma sessao curta de foco e proteja esse bloco.";
        }

        if (checklistConcluido < 3) {
            return objetivo + ": feche o checklist para consolidar o dia.";
        }

        return "Dia muito bem encaminhado. Use o restante para manter o basico simples.";
    }

    private String getMelhorHorario() {
        return prefs.getString("melhor_horario", "hoje").toLowerCase(Locale.ROOT);
    }

    private String labelNivel(int nivel) {
        if (nivel == 0) return "baixo";
        if (nivel == 1) return "ok";
        if (nivel == 2) return "alto";
        return "sem registro";
    }

    private String getSaudacao() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Bom dia";
        if (hour < 18) return "Boa tarde";
        return "Boa noite";
    }

    private boolean jaTirouFotoHoje() {
        return getTodayKey() == prefs.getLong("ultimo_dia_foto", -1);
    }

    private void salvarFotoDoDia(Bitmap bitmap) {
        long hoje = getTodayKey();
        int diaAtual = prefs.getInt("challenge_day", 1);

        File pasta = new File(requireContext().getFilesDir(), "shape_challenge");
        if (!pasta.exists()) pasta.mkdirs();

        File arquivo = new File(pasta, "shape_day_" + diaAtual + ".jpg");

        try (FileOutputStream out = new FileOutputStream(arquivo)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();

            prefs.edit()
                    .putString("foto_dia_" + diaAtual, arquivo.getAbsolutePath())
                    .putLong("ultimo_dia_foto", hoje)
                    .putInt("challenge_day", diaAtual + 1)
                    .apply();

            Toast.makeText(getContext(), "Foto do dia " + diaAtual + " salva.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Erro ao salvar a foto.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void registrarAgua(int quantidadeMl) {
        String chave = "agua_log_" + getTodayKey();
        String horario = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String entrada = horario + " - " + quantidadeMl + " ml";
        String logAtual = prefs.getString(chave, "");
        String novoLog = TextUtils.isEmpty(logAtual) ? entrada : logAtual + "|" + entrada;
        int totalRegistrado = prefs.getInt("total_agua_ml_registrado", 0) + quantidadeMl;
        prefs.edit()
                .putString(chave, novoLog)
                .putInt("total_agua_ml_registrado", totalRegistrado)
                .apply();
    }

    private long getTodayKey() {
        return System.currentTimeMillis() / (1000L * 60 * 60 * 24);
    }

    private void atualizarGaleria() {
        if (layoutGaleria == null) return;

        layoutGaleria.removeAllViews();
        int diaAtual = prefs.getInt("challenge_day", 1);

        for (int dia = 1; dia < diaAtual; dia++) {
            String caminhoFoto = prefs.getString("foto_dia_" + dia, null);

            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setPadding(8, 8, 8, 8);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(132, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 12, 0);
            card.setLayoutParams(cardParams);

            ImageView imagem = new ImageView(getContext());
            imagem.setLayoutParams(new LinearLayout.LayoutParams(116, 116));
            imagem.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (caminhoFoto != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(caminhoFoto);
                if (bitmap != null) {
                    imagem.setImageBitmap(bitmap);
                } else {
                    imagem.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } else {
                imagem.setImageResource(android.R.drawable.ic_menu_report_image);
            }

            TextView textoDia = new TextView(getContext());
            textoDia.setText("Dia " + dia);
            textoDia.setTextSize(12f);
            textoDia.setTextColor(0xFFFFFFFF);
            textoDia.setPadding(0, 8, 0, 0);
            textoDia.setGravity(Gravity.CENTER);

            card.addView(imagem);
            card.addView(textoDia);
            layoutGaleria.addView(card);
        }

        if (diaAtual == 1) {
            TextView vazio = new TextView(getContext());
            vazio.setText("Nenhuma foto registrada ainda.");
            vazio.setTextSize(14f);
            vazio.setTextColor(0xFFFFF3E0);
            vazio.setPadding(0, 8, 0, 8);
            layoutGaleria.addView(vazio);
        }
    }
}
