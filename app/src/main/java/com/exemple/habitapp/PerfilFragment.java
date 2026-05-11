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

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PerfilFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView txtTitulo;
    private TextView txtSubtitulo;
    private TextView txtNivel;
    private TextView txtStats;
    private TextView txtResumo;
    private TextInputEditText inputNome;
    private TextInputEditText inputObjetivo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);

        txtTitulo = view.findViewById(R.id.txtPerfilTitulo);
        txtSubtitulo = view.findViewById(R.id.txtPerfilSubtitulo);
        txtNivel = view.findViewById(R.id.txtPerfilNivel);
        txtStats = view.findViewById(R.id.txtPerfilStats);
        txtResumo = view.findViewById(R.id.txtPerfilResumo);
        inputNome = view.findViewById(R.id.inputPerfilNome);
        inputObjetivo = view.findViewById(R.id.inputPerfilObjetivo);
        MaterialButton btnSalvar = view.findViewById(R.id.btnSalvarPerfil);

        inputNome.setText(prefs.getString("nome_usuario", "Guerreiro"));
        inputObjetivo.setText(prefs.getString("objetivo_principal", "Mais disciplina"));
        btnSalvar.setOnClickListener(v -> salvarPerfil());

        atualizarTela();
        return view;
    }

    private void salvarPerfil() {
        String nome = inputNome.getText() != null ? inputNome.getText().toString().trim() : "";
        String objetivo = inputObjetivo.getText() != null ? inputObjetivo.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nome)) nome = "Guerreiro";
        if (TextUtils.isEmpty(objetivo)) objetivo = "Mais disciplina";

        prefs.edit()
                .putString("nome_usuario", nome)
                .putString("objetivo_principal", objetivo)
                .apply();

        atualizarTela();
        Toast.makeText(getContext(), "Perfil salvo.", Toast.LENGTH_SHORT).show();
    }

    private void atualizarTela() {
        String nome = prefs.getString("nome_usuario", "Guerreiro");
        String objetivo = prefs.getString("objetivo_principal", "Mais disciplina");
        int streak = HabitStore.getStreak(prefs);
        int media = HabitStore.getWeeklyAverage(prefs);
        int aguaTotal = prefs.getInt("total_agua_ml_registrado", 0);
        int focoTotal = prefs.getInt("total_foco_min_registrado", 0);

        txtTitulo.setText("Perfil de " + nome);
        txtSubtitulo.setText(objetivo);
        txtNivel.setText("Nivel " + HabitStore.getLevelName(prefs));
        txtStats.setText("Streak " + streak + (streak == 1 ? " dia" : " dias") + " | media " + media + "%");
        txtResumo.setText("Total registrado: " + aguaTotal + " ml de agua e " + focoTotal + " min de foco. Seu objetivo atual e: " + objetivo + ".");
    }
}
