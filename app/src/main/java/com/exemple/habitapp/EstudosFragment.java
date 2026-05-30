package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EstudosFragment extends Fragment {

    private static final String PREF_FOCUS_RUNNING = "focus_timer_running";
    private static final String PREF_FOCUS_TOTAL_MS = "focus_timer_total_ms";
    private static final String PREF_FOCUS_REMAINING_MS = "focus_timer_remaining_ms";
    private static final String PREF_FOCUS_END_AT_MS = "focus_timer_end_at_ms";
    private static final int MIN_FOCUS_MINUTES = 5;
    private static final int MAX_FOCUS_MINUTES = 180;

    private CountDownTimer timer;
    private boolean rodando = false;

    private long tempoTotal = 25 * 60 * 1000L;
    private long tempoRestante = tempoTotal;

    private CircularProgressIndicator progressIndicator;
    private LinearProgressIndicator progressFocoDia;
    private SharedPreferences prefs;
    private TextView txtTempo;
    private TextView txtSessaoStatus;
    private TextView txtFocoProgresso;
    private TextInputEditText inputTempo;
    private MaterialButton btnStart;
    private LinearLayout layoutHistoricoFoco;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estudos, container, false);

        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);

        txtTempo = view.findViewById(R.id.txtTempo);
        txtSessaoStatus = view.findViewById(R.id.txtSessaoStatus);
        txtFocoProgresso = view.findViewById(R.id.txtFocoProgresso);
        btnStart = view.findViewById(R.id.btnStart);
        MaterialButton btnReset = view.findViewById(R.id.btnReset);
        MaterialButton btnQuickStudy = view.findViewById(R.id.btnQuickStudy);
        MaterialButton btnPomodoro = view.findViewById(R.id.btnPomodoro);
        MaterialButton btnDeepWork = view.findViewById(R.id.btnDeepWork);
        MaterialButton btnAddFocus10 = view.findViewById(R.id.btnAddFocus10);
        inputTempo = view.findViewById(R.id.inputTempo);
        progressIndicator = view.findViewById(R.id.progressTimer);
        progressFocoDia = view.findViewById(R.id.progressFocoDia);
        layoutHistoricoFoco = view.findViewById(R.id.layoutHistoricoFoco);

        int focoMinutos = prefs.getInt("foco_minutos", 25);
        restaurarEstadoTimer(focoMinutos);

        btnQuickStudy.setOnClickListener(v -> configurarDuracao(15));
        btnPomodoro.setOnClickListener(v -> configurarDuracao(25));
        btnDeepWork.setOnClickListener(v -> configurarDuracao(45));
        btnAddFocus10.setOnClickListener(v -> adicionarMinutosManuais(10));
        btnStart.setOnClickListener(v -> alternarTimer());
        btnReset.setOnClickListener(v -> resetarTimer());

        atualizarTempo();
        atualizarStatus();
        renderHistoricoFoco();
        UiAnimator.enter(view);
        return view;
    }

    private void configurarDuracao(int minutos) {
        if (rodando) {
            Toast.makeText(getContext(), "Pause a sessão antes de trocar o tempo.", Toast.LENGTH_SHORT).show();
            return;
        }

        minutos = limitarDuracao(minutos);
        if (timer != null) timer.cancel();
        tempoTotal = minutos * 60 * 1000L;
        tempoRestante = tempoTotal;
        prefs.edit().putInt("foco_minutos", minutos).apply();
        limparEstadoTimer();
        btnStart.setText("Iniciar");
        inputTempo.setText(String.valueOf(minutos));
        atualizarTempo();
        atualizarStatus();
    }

    private void alternarTimer() {
        if (!rodando) {
            if (tempoRestante <= 0) {
                tempoRestante = tempoTotal;
            }
            if (!atualizarDuracaoPeloInput()) return;

            rodando = true;
            btnStart.setText("Pausar");
            txtSessaoStatus.setText("Sessão em andamento. Mantenha uma única prioridade.");
            persistirEstadoTimer(true);
            iniciarContagem();
        } else {
            if (timer != null) timer.cancel();
            rodando = false;
            btnStart.setText("Continuar");
            txtSessaoStatus.setText("Pausado. Volte quando estiver pronto.");
            persistirEstadoTimer(false);
        }
    }

    private void restaurarEstadoTimer(int fallbackMinutos) {
        long fallbackMs = limitarDuracao(fallbackMinutos) * 60 * 1000L;
        tempoTotal = prefs.getLong(PREF_FOCUS_TOTAL_MS, fallbackMs);
        tempoTotal = limitarDuracao((int) Math.max(1L, tempoTotal / 60000L)) * 60 * 1000L;
        tempoRestante = prefs.getLong(PREF_FOCUS_REMAINING_MS, tempoTotal);
        tempoRestante = Math.max(0L, Math.min(tempoRestante, tempoTotal));
        inputTempo.setText(String.valueOf(Math.max(1, tempoTotal / 60000L)));

        if (prefs.getBoolean(PREF_FOCUS_RUNNING, false)) {
            long endAt = prefs.getLong(PREF_FOCUS_END_AT_MS, 0L);
            tempoRestante = endAt > 0L ? Math.max(0L, endAt - System.currentTimeMillis()) : tempoRestante;

            if (tempoRestante <= 0L) {
                concluirSessao();
                return;
            }

            rodando = true;
            btnStart.setText("Pausar");
            txtSessaoStatus.setText("Sessão em andamento. Mantenha uma única prioridade.");
            iniciarContagem();
        } else {
            rodando = false;
            btnStart.setText(tempoRestante < tempoTotal ? "Continuar" : "Iniciar");
        }

        atualizarTempo();
        atualizarStatus();
    }

    private void iniciarContagem() {
        if (timer != null) timer.cancel();

        if (tempoRestante <= 0L) {
            concluirSessao();
            return;
        }

        timer = new CountDownTimer(tempoRestante, 500) {
            @Override
            public void onTick(long ms) {
                tempoRestante = ms;
                atualizarTempo();
            }

            @Override
            public void onFinish() {
                tempoRestante = 0L;
                concluirSessao();
            }
        }.start();
    }

    private void concluirSessao() {
        if (timer != null) timer.cancel();
        rodando = false;
        btnStart.setText("Iniciar");
        limparEstadoTimer();
        salvarEstudoConcluido();
        atualizarTempo();
        atualizarStatus();
        renderHistoricoFoco();
        Toast.makeText(getContext(), "Sessão concluída.", Toast.LENGTH_SHORT).show();
    }

    private void persistirEstadoTimer(boolean running) {
        SharedPreferences.Editor editor = prefs.edit()
                .putBoolean(PREF_FOCUS_RUNNING, running)
                .putLong(PREF_FOCUS_TOTAL_MS, tempoTotal)
                .putLong(PREF_FOCUS_REMAINING_MS, tempoRestante);

        if (running) {
            editor.putLong(PREF_FOCUS_END_AT_MS, System.currentTimeMillis() + tempoRestante);
        } else {
            editor.remove(PREF_FOCUS_END_AT_MS);
        }

        editor.apply();
    }

    private void limparEstadoTimer() {
        prefs.edit()
                .remove(PREF_FOCUS_RUNNING)
                .remove(PREF_FOCUS_TOTAL_MS)
                .remove(PREF_FOCUS_REMAINING_MS)
                .remove(PREF_FOCUS_END_AT_MS)
                .apply();
    }

    private boolean atualizarDuracaoPeloInput() {
        String valor = inputTempo.getText() != null ? inputTempo.getText().toString() : "";

        if (TextUtils.isEmpty(valor)) return true;

        try {
            int minutos = Integer.parseInt(valor);
            if (minutos < MIN_FOCUS_MINUTES || minutos > MAX_FOCUS_MINUTES) {
                Toast.makeText(getContext(), "Use um tempo entre 5 e 180 minutos.", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (tempoRestante == tempoTotal) {
                tempoTotal = minutos * 60 * 1000L;
                tempoRestante = tempoTotal;
                prefs.edit().putInt("foco_minutos", minutos).apply();
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Use apenas numeros.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private int limitarDuracao(int minutos) {
        return Math.max(MIN_FOCUS_MINUTES, Math.min(MAX_FOCUS_MINUTES, minutos));
    }

    private void resetarTimer() {
        if (timer != null) timer.cancel();
        rodando = false;
        btnStart.setText("Iniciar");
        tempoRestante = tempoTotal;
        limparEstadoTimer();
        atualizarTempo();
        atualizarStatus();
    }

    private void adicionarMinutosManuais(int minutos) {
        int total = prefs.getInt("estudos_concluidos_min", 0) + minutos;
        int totalGeral = prefs.getInt("total_foco_min_registrado", 0) + minutos;

        prefs.edit()
                .putInt("estudos_concluidos_min", total)
                .putInt("total_foco_min_registrado", totalGeral)
                .apply();

        registrarFoco(minutos, "Manual");
        HabitStore.saveTodaySnapshot(prefs);
        atualizarStatus();
        renderHistoricoFoco();
        Toast.makeText(getContext(), "+" + minutos + " min registrados.", Toast.LENGTH_SHORT).show();
    }

    private void salvarEstudoConcluido() {
        int minutosSessao = (int) (tempoTotal / 60000L);
        int total = prefs.getInt("estudos_concluidos_min", 0) + minutosSessao;
        int sessoes = prefs.getInt("sessoes_foco_concluidas", 0) + 1;
        int totalGeral = prefs.getInt("total_foco_min_registrado", 0) + minutosSessao;
        prefs.edit()
                .putInt("estudos_concluidos_min", total)
                .putInt("sessoes_foco_concluidas", sessoes)
                .putInt("total_foco_min_registrado", totalGeral)
                .apply();

        registrarFoco(minutosSessao, "Sessão concluída");
        HabitStore.saveTodaySnapshot(prefs);
    }

    private void atualizarTempo() {
        int totalSeg = (int) (tempoRestante / 1000);
        int min = totalSeg / 60;
        int seg = totalSeg % 60;

        txtTempo.setText(String.format(Locale.getDefault(), "%02d:%02d", min, seg));

        if (progressIndicator != null && tempoTotal > 0) {
            int progresso = (int) (((tempoTotal - tempoRestante) * 100) / tempoTotal);
            UiAnimator.animateProgress(progressIndicator, progresso);
        }
    }

    private void atualizarStatus() {
        int estudos = prefs.getInt("estudos_concluidos_min", 0);
        int meta = prefs.getInt("meta_estudos_min", 60);
        int sessoes = prefs.getInt("sessoes_foco_concluidas", 0);
        int percentual = percentual(estudos, meta);

        txtSessaoStatus.setText("Hoje: " + estudos + "/" + meta + " min em " + sessoes + " sessões.");
        txtFocoProgresso.setText(estudos + " / " + meta + " min  |  " + percentual + "% da meta");
        UiAnimator.animateProgress(progressFocoDia, percentual);
    }

    private void registrarFoco(int minutos, String origem) {
        String horario = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String entrada = horario + " - " + origem + " - " + minutos + " min";
        String logAtual = prefs.getString(getLogKey(), "");
        String novoLog = TextUtils.isEmpty(logAtual) ? entrada : logAtual + "|" + entrada;
        prefs.edit().putString(getLogKey(), novoLog).apply();
    }

    private void renderHistoricoFoco() {
        layoutHistoricoFoco.removeAllViews();
        String log = prefs.getString(getLogKey(), "");

        if (TextUtils.isEmpty(log)) {
            adicionarLinhaHistorico("Nenhuma sessão registrada hoje.");
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
        linha.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        linha.setTextSize(14f);
        linha.setPadding(0, 6, 0, 6);
        layoutHistoricoFoco.addView(linha);
    }

    private int percentual(double atual, double meta) {
        if (meta <= 0) return 0;
        return (int) Math.max(0, Math.min(100, Math.round((atual / meta) * 100)));
    }

    private String getLogKey() {
        return "focus_log_" + HabitStore.todayKey();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rodando && prefs != null) {
            persistirEstadoTimer(true);
        }
        if (timer != null) timer.cancel();
    }
}
