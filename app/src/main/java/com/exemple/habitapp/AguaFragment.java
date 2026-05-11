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
import androidx.lifecycle.ViewModelProvider;

import com.exemple.habitapp.databinding.FragmentAguaBinding;

import java.text.SimpleDateFormat;
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
        viewModel = new ViewModelProvider(this).get(AguaViewModel.class);

        configurarObservadores();
        configurarBotoes();
        renderHistoricoAgua();
    }

    private void configurarObservadores() {
        viewModel.getLitros().observe(getViewLifecycleOwner(), litros -> atualizarTela());
        viewModel.getMeta().observe(getViewLifecycleOwner(), meta -> {
            binding.progressoAgua.setMax((int) (meta * 100));
            if (binding.editMeta.getText() == null || binding.editMeta.getText().toString().isEmpty()) {
                binding.editMeta.setText(String.valueOf(meta));
            }
            atualizarTela();
        });
    }

    private void atualizarTela() {
        double litros = viewModel.getLitros().getValue() != null ? viewModel.getLitros().getValue() : 0.0;
        double meta = viewModel.getMeta().getValue() != null ? viewModel.getMeta().getValue() : 2.0;
        double faltaMl = Math.max(0, (meta - litros) * 1000);
        int coposRestantes = (int) Math.ceil(faltaMl / 250.0);

        binding.txtAgua.setText(String.format(Locale.getDefault(), "%.2f L", litros));
        binding.txtMetaLabel.setText(String.format(Locale.getDefault(), "Meta: %.2f L", meta));
        binding.progressoAgua.setMax((int) (meta * 100));
        binding.progressoAgua.setProgressCompat((int) (litros * 100), true);
        binding.txtCoposRestantes.setText(coposRestantes == 0
                ? "Nenhum copo restante"
                : coposRestantes + (coposRestantes == 1 ? " copo" : " copos") + " de 250 ml restantes");

        if (faltaMl == 0) {
            binding.txtFaltam.setText("Meta atingida. Excelente consistencia.");
            binding.txtDicaAgua.setText("Dica: mantenha o ritmo amanha com pequenos registros ao longo do dia.");
            binding.txtProximaAgua.setText("Proximo lembrete sugerido: meta fechada");
            binding.txtRitmoAgua.setText("Ritmo de hoje: completo.");
        } else {
            binding.txtFaltam.setText(String.format(Locale.getDefault(), "Faltam %.0f ml", faltaMl));
            binding.txtDicaAgua.setText(faltaMl <= 500
                    ? "Dica: voce esta perto. Mais um copo pode fechar a meta."
                    : "Dica: registre pequenas doses para nao depender de um grande volume no fim do dia.");
            binding.txtProximaAgua.setText("Proximo lembrete sugerido: em 90 min");
            binding.txtRitmoAgua.setText(criarRitmoHidratacao(litros, meta));
        }
    }

    private void configurarBotoes() {
        binding.btnWater250.setOnClickListener(v -> adicionarQuantidade(250));
        binding.btnWater500.setOnClickListener(v -> adicionarQuantidade(500));
        binding.btnWater750.setOnClickListener(v -> adicionarQuantidade(750));

        binding.btnAdd.setOnClickListener(v -> {
            String input = binding.editQuantidade.getText() != null
                    ? binding.editQuantidade.getText().toString()
                    : "";

            if (input.isEmpty()) {
                Toast.makeText(requireContext(), "Digite a quantidade em ml.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                adicionarQuantidade(Double.parseDouble(input));
                binding.editQuantidade.setText("");
                binding.editQuantidade.clearFocus();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Numero invalido.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnReset.setOnClickListener(v -> {
            viewModel.resetarAgua();
            prefs.edit().remove(getLogKey()).apply();
            renderHistoricoAgua();
            Toast.makeText(requireContext(), "Agua zerada para hoje.", Toast.LENGTH_SHORT).show();
        });

        binding.layoutMetaInput.setEndIconOnClickListener(v -> {
            String input = binding.editMeta.getText() != null
                    ? binding.editMeta.getText().toString()
                    : "";

            if (input.isEmpty()) return;

            try {
                double novaMeta = Double.parseDouble(input);
                viewModel.salvarNovaMeta(novaMeta);
                binding.editMeta.clearFocus();
                Toast.makeText(requireContext(), "Nova meta salva.", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Numero invalido.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Meta ja atingida.", Toast.LENGTH_SHORT).show();
            return;
        }

        int registradoMl = (int) Math.round(Math.min(quantidadeMl, (meta - atual) * 1000));
        viewModel.adicionarAgua(quantidadeMl);
        registrarAgua(registradoMl);
        renderHistoricoAgua();
        Toast.makeText(requireContext(), "+" + registradoMl + " ml adicionados.", Toast.LENGTH_SHORT).show();
    }

    private String criarRitmoHidratacao(double litros, double meta) {
        double percentual = meta <= 0 ? 0 : litros / meta;
        if (percentual < 0.33) return "Ritmo de hoje: comece com 2 copos nas proximas horas.";
        if (percentual < 0.66) return "Ritmo de hoje: voce esta no meio do caminho.";
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

    private void renderHistoricoAgua() {
        if (binding == null) return;

        binding.layoutHistoricoAgua.removeAllViews();
        String log = prefs.getString(getLogKey(), "");

        if (TextUtils.isEmpty(log)) {
            adicionarLinhaHistorico("Nenhum registro de agua hoje.");
            return;
        }

        String[] entradas = log.split("\\|");
        for (int i = entradas.length - 1; i >= 0; i--) {
            adicionarLinhaHistorico(entradas[i]);
        }
    }

    private void adicionarLinhaHistorico(String texto) {
        TextView linha = new TextView(requireContext());
        linha.setText(texto);
        linha.setTextColor(0xFF64748B);
        linha.setTextSize(14f);
        linha.setPadding(0, 6, 0, 6);
        binding.layoutHistoricoAgua.addView(linha);
    }

    private String getLogKey() {
        return "agua_log_" + getTodayKey();
    }

    private long getTodayKey() {
        return System.currentTimeMillis() / (1000L * 60 * 60 * 24);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
