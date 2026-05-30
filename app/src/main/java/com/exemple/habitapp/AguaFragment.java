package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.exemple.habitapp.databinding.FragmentAguaBinding;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AguaFragment extends Fragment {

    private FragmentAguaBinding binding;
    private AguaViewModel viewModel;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAguaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);
        viewModel = new ViewModelProvider(this).get(AguaViewModel.class);

        configurarObservadores();
        configurarBotoes();
        renderHistoricoAgua();
        renderHistoricoSemanalAgua();
        UiAnimator.enter(view);
    }

    private void configurarObservadores() {
        viewModel.getLitros().observe(getViewLifecycleOwner(), litros -> atualizarTela());
        viewModel.getMeta().observe(getViewLifecycleOwner(), meta -> {
            binding.progressoAgua.setMax(100);
            if (!binding.editMeta.hasFocus()) {
                binding.editMeta.setText(String.valueOf((int) Math.round(meta * 1000)));
            }
            atualizarTela();
        });
    }

    private void atualizarTela() {
        double litros = viewModel.getLitros().getValue() != null ? viewModel.getLitros().getValue() : 0.0;
        double meta = viewModel.getMeta().getValue() != null ? viewModel.getMeta().getValue() : 2.0;
        int aguaMl = (int) Math.round(litros * 1000);
        int metaMl = Math.max(1, (int) Math.round(meta * 1000));
        int faltaMl = Math.max(0, metaMl - aguaMl);
        int coposRestantes = (int) Math.ceil(faltaMl / 250.0);

        binding.txtAgua.setText(String.format(Locale.getDefault(), "%.2f L", litros));
        binding.txtMetaLabel.setText(String.format(Locale.getDefault(), "Meta: %d ml", metaMl));
        binding.progressoAgua.setMax(100);
        UiAnimator.animateProgress(binding.progressoAgua, HabitStore.percent(aguaMl, metaMl));
        binding.txtCoposRestantes.setText(coposRestantes == 0
                ? "Nenhum copo restante"
                : coposRestantes + (coposRestantes == 1 ? " copo" : " copos") + " de 250 ml restantes");

        if (faltaMl == 0) {
            binding.txtFaltam.setText("Meta atingida. Excelente consistência.");
            binding.txtDicaAgua.setText("Dica: mantenha o ritmo amanhã com pequenos registros ao longo do dia.");
            binding.txtProximaAgua.setText("Próximo lembrete sugerido: meta fechada");
            binding.txtRitmoAgua.setText("Ritmo de hoje: completo.");
        } else {
            binding.txtFaltam.setText(String.format(Locale.getDefault(), "Faltam %.0f ml", faltaMl));
            binding.txtDicaAgua.setText(faltaMl <= 500
                    ? "Dica: você está perto. Mais um copo pode fechar a meta."
                    : "Dica: registre pequenas doses para não depender de um grande volume no fim do dia.");
            binding.txtProximaAgua.setText("Próximo lembrete sugerido: em 90 min");
            binding.txtRitmoAgua.setText(criarRitmoHidratacao(litros, meta));
        }
    }

    private void configurarBotoes() {
        binding.btnWater100.setOnClickListener(v -> adicionarQuantidade(100));
        binding.btnWater250.setOnClickListener(v -> adicionarQuantidade(250));
        binding.btnWater500.setOnClickListener(v -> adicionarQuantidade(500));
        binding.btnUndoWater.setOnClickListener(v -> desfazerUltimoRegistro());

        binding.btnAdd.setOnClickListener(v -> {
            String input = binding.editQuantidade.getText() != null
                    ? binding.editQuantidade.getText().toString()
                    : "";

            if (input.isEmpty()) {
                Toast.makeText(requireContext(), "Digite a quantidade em ml.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                adicionarQuantidade(parseDecimal(input));
                binding.editQuantidade.setText("");
                binding.editQuantidade.clearFocus();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Número inválido.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnReset.setOnClickListener(v -> {
            viewModel.resetarAgua();
            prefs.edit().remove(getLogKey()).apply();
            HabitStore.saveTodaySnapshot(prefs);
            renderHistoricoAgua();
            renderHistoricoSemanalAgua();
            Toast.makeText(requireContext(), "Água zerada para hoje.", Toast.LENGTH_SHORT).show();
        });

        binding.layoutMetaInput.setEndIconOnClickListener(v -> {
            String input = binding.editMeta.getText() != null
                    ? binding.editMeta.getText().toString()
                    : "";

            if (input.isEmpty()) return;

            try {
                int novaMetaMl = (int) Math.round(parseDecimal(input));
                if (novaMetaMl < 500 || novaMetaMl > 8000) {
                    Toast.makeText(requireContext(), "Defina uma meta entre 500 ml e 8000 ml.", Toast.LENGTH_SHORT).show();
                    return;
                }

                viewModel.salvarNovaMeta(novaMetaMl / 1000.0);
                HabitStore.saveTodaySnapshot(prefs);
                renderHistoricoSemanalAgua();
                binding.editMeta.setText(String.valueOf(novaMetaMl));
                binding.editMeta.clearFocus();
                Toast.makeText(requireContext(), "Nova meta salva.", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Número inválido.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adicionarQuantidade(double quantidadeMl) {
        double atual = viewModel.getLitros().getValue() != null ? viewModel.getLitros().getValue() : 0.0;
        double meta = viewModel.getMeta().getValue() != null ? viewModel.getMeta().getValue() : 2.0;

        if (quantidadeMl <= 0) {
            Toast.makeText(requireContext(), "Informe um valor maior que zero.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (atual >= meta) {
            Toast.makeText(requireContext(), "Meta já atingida.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean metaAtingidaAgora = atual < meta && atual + (quantidadeMl / 1000.0) >= meta;
        int registradoMl = (int) Math.round(Math.min(quantidadeMl, (meta - atual) * 1000));
        viewModel.adicionarAgua(quantidadeMl);
        registrarAgua(registradoMl);
        HabitStore.saveTodaySnapshot(prefs);
        renderHistoricoAgua();
        renderHistoricoSemanalAgua();
        UiAnimator.pulse(binding.txtAgua);

        if (metaAtingidaAgora) {
            FeedbackHelper.success(requireContext());
            CelebrationView.burst(binding.getRoot());
            Toast.makeText(requireContext(), "Meta de água atingida. Excelente ritmo.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "+" + registradoMl + " ml adicionados.", Toast.LENGTH_SHORT).show();
        }
    }

    private String criarRitmoHidratacao(double litros, double meta) {
        double percentual = meta <= 0 ? 0 : litros / meta;
        if (percentual < 0.33) return "Ritmo de hoje: comece com 2 copos nas próximas horas.";
        if (percentual < 0.66) return "Ritmo de hoje: você está no meio do caminho.";
        return "Ritmo de hoje: falta pouco para fechar.";
    }

    private void registrarAgua(int quantidadeMl) {
        String horario = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String entrada = horario + " - " + quantidadeMl + " ml";
        String logAtual = prefs.getString(getLogKey(), "");
        String novoLog = TextUtils.isEmpty(logAtual) ? entrada : logAtual + "|" + entrada;
        int totalRegistrado = prefs.getInt("total_agua_ml_registrado", 0) + quantidadeMl;

        prefs.edit()
                .putString(getLogKey(), novoLog)
                .putInt("total_agua_ml_registrado", totalRegistrado)
                .apply();
    }

    private void desfazerUltimoRegistro() {
        String logAtual = prefs.getString(getLogKey(), "");
        if (TextUtils.isEmpty(logAtual)) {
            Toast.makeText(requireContext(), "Não há registro para desfazer.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] entradas = logAtual.split("\\|");
        String ultimaEntrada = entradas[entradas.length - 1];
        int ultimoMl = extrairMl(ultimaEntrada);

        if (ultimoMl <= 0) {
            Toast.makeText(requireContext(), "Não consegui identificar o último registro.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder novoLog = new StringBuilder();
        for (int i = 0; i < entradas.length - 1; i++) {
            if (i > 0) novoLog.append("|");
            novoLog.append(entradas[i]);
        }

        int aguaAtualMl = HabitStore.getAguaMl(prefs);
        int novoAguaMl = Math.max(0, aguaAtualMl - ultimoMl);
        int totalRegistrado = Math.max(0, prefs.getInt("total_agua_ml_registrado", 0) - ultimoMl);

        prefs.edit()
                .putString(getLogKey(), novoLog.toString())
                .putInt("total_agua_ml_registrado", totalRegistrado)
                .apply();

        viewModel.definirAguaMl(novoAguaMl);
        HabitStore.saveTodaySnapshot(prefs);
        renderHistoricoAgua();
        renderHistoricoSemanalAgua();
        UiAnimator.pulse(binding.txtAgua);
        Toast.makeText(requireContext(), "-" + ultimoMl + " ml removidos.", Toast.LENGTH_SHORT).show();
    }

    private int extrairMl(String entrada) {
        if (TextUtils.isEmpty(entrada)) return 0;
        String[] partes = entrada.split("-");
        if (partes.length == 0) return 0;

        String quantidade = partes[partes.length - 1].replace("ml", "").trim();
        try {
            return Math.max(0, (int) Math.round(parseDecimal(quantidade)));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void renderHistoricoAgua() {
        if (binding == null) return;

        binding.layoutHistoricoAgua.removeAllViews();
        String log = prefs.getString(getLogKey(), "");

        if (TextUtils.isEmpty(log)) {
            adicionarLinhaHistorico("Nenhum registro de água hoje.");
            return;
        }

        String[] entradas = log.split("\\|");
        for (int i = entradas.length - 1; i >= 0; i--) {
            adicionarLinhaHistorico(entradas[i]);
        }
    }

    private void renderHistoricoSemanalAgua() {
        if (binding == null) return;

        binding.layoutHistoricoSemanalAgua.removeAllViews();
        int metaMl = Math.max(1, HabitStore.getMetaAguaMl(prefs));

        for (int offset = 0; offset > -7; offset--) {
            long day = HabitStore.dayKey(offset);
            int aguaMl = offset == 0 ? HabitStore.getAguaMl(prefs) : prefs.getInt("agua_ml_day_" + day, 0);
            adicionarLinhaHistoricoSemanal(formatDayLabel(offset), aguaMl, HabitStore.percent(aguaMl, metaMl));
        }
    }

    private void adicionarLinhaHistoricoSemanal(String label, int aguaMl, int percentual) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView day = new TextView(requireContext());
        day.setText(label);
        day.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink));
        day.setTextSize(13f);
        day.setGravity(Gravity.START);
        row.addView(day, new LinearLayout.LayoutParams(HabitUi.dp(requireContext(), 62), ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearProgressIndicator progress = new LinearProgressIndicator(requireContext());
        progress.setMax(100);
        progress.setProgressCompat(percentual, false);
        progress.setIndicatorColor(ContextCompat.getColor(requireContext(), percentual >= 100 ? R.color.success : R.color.water));
        progress.setTrackColor(ContextCompat.getColor(requireContext(), R.color.line));
        progress.setTrackThickness(HabitUi.dp(requireContext(), 8));
        progress.setTrackCornerRadius(HabitUi.dp(requireContext(), 8));
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        progressParams.setMargins(HabitUi.dp(requireContext(), 8), 0, HabitUi.dp(requireContext(), 8), 0);
        row.addView(progress, progressParams);

        TextView amount = new TextView(requireContext());
        amount.setText(aguaMl + " ml");
        amount.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        amount.setTextSize(13f);
        amount.setGravity(Gravity.END);
        row.addView(amount, new LinearLayout.LayoutParams(HabitUi.dp(requireContext(), 76), ViewGroup.LayoutParams.WRAP_CONTENT));

        binding.layoutHistoricoSemanalAgua.addView(row);
    }

    private String formatDayLabel(int offset) {
        if (offset == 0) return "Hoje";
        if (offset == -1) return "Ontem";

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, offset);
        return new SimpleDateFormat("dd/MM", Locale.getDefault()).format(calendar.getTime());
    }

    private void adicionarLinhaHistorico(String texto) {
        TextView linha = new TextView(requireContext());
        linha.setText(texto);
        linha.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        linha.setTextSize(14f);
        linha.setPadding(0, 6, 0, 6);
        binding.layoutHistoricoAgua.addView(linha);
    }

    private String getLogKey() {
        return "agua_log_" + HabitStore.todayKey();
    }

    private double parseDecimal(String input) {
        return Double.parseDouble(input.trim().replace(",", "."));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
