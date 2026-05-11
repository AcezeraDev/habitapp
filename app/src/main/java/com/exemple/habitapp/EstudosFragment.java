package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

public class EstudosFragment extends Fragment {

    CountDownTimer timer;
    boolean rodando = false;

    long tempoTotal = 25 * 60 * 1000L;
    long tempoRestante = tempoTotal;

    CircularProgressIndicator progressIndicator;
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_estudos, container, false);

        TextView txt = view.findViewById(R.id.txtTempo);
        MaterialButton btnStart = view.findViewById(R.id.btnStart);
        MaterialButton btnReset = view.findViewById(R.id.btnReset);
        TextInputEditText inputTempo = view.findViewById(R.id.inputTempo);

        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        int focoMinutos = prefs.getInt("foco_minutos", 25);
        tempoTotal = focoMinutos * 60 * 1000L;
        tempoRestante = tempoTotal;
        inputTempo.setText(String.valueOf(focoMinutos));

        progressIndicator = view.findViewById(R.id.progressTimer);

        atualizarTempo(txt);

        btnStart.setOnClickListener(v -> {

            if (!rodando) {

                String valor = inputTempo.getText() != null
                        ? inputTempo.getText().toString()
                        : "";

                if (!TextUtils.isEmpty(valor)) {
                    try {
                        int minutos = Integer.parseInt(valor);

                        if (minutos <= 0) {
                            Toast.makeText(getContext(),
                                    "Tempo inválido!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (tempoRestante == tempoTotal) {
                            tempoTotal = minutos * 60 * 1000L;
                            tempoRestante = tempoTotal;
                        }

                    } catch (Exception e) {
                        Toast.makeText(getContext(),
                                "Só números!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                rodando = true;

                timer = new CountDownTimer(tempoRestante, 500) { // 🔥 mais fluido
                    @Override
                    public void onTick(long ms) {
                        tempoRestante = ms;
                        atualizarTempo(txt);
                    }

                    @Override
                    public void onFinish() {
                        txt.setText("Fim! 🎉");
                        rodando = false;
                        btnStart.setText("Iniciar");
                        salvarEstudoConcluido();

                        if (progressIndicator != null) {
                            progressIndicator.setProgressCompat(0, true);
                        }
                    }
                }.start();

                btnStart.setText("Pausar");

            } else {
                if (timer != null) timer.cancel();
                rodando = false;
                btnStart.setText("Continuar");
            }
        });

        btnReset.setOnClickListener(v -> {
            if (timer != null) timer.cancel();

            tempoRestante = tempoTotal;
            rodando = false;

            atualizarTempo(txt);
            btnStart.setText("Iniciar");
        });

        return view;
    }

    private void salvarEstudoConcluido() {
        int minutosSessao = (int) (tempoTotal / 60000L);
        int total = prefs.getInt("estudos_concluidos_min", 0) + minutosSessao;
        prefs.edit().putInt("estudos_concluidos_min", total).apply();
    }

    private void atualizarTempo(TextView txt) {

        int totalSeg = (int) (tempoRestante / 1000);
        int min = totalSeg / 60;
        int seg = totalSeg % 60;

        txt.setText(String.format("%02d:%02d", min, seg));

        if (progressIndicator != null && tempoTotal > 0) {
            int progresso = (int) ((tempoRestante * 100) / tempoTotal);

            // 🔥 versão suave (menos lag)
            progressIndicator.setProgressCompat(progresso, true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
    }
}
