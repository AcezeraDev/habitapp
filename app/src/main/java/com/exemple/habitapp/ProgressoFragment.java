package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_progresso, container, false);

        TextView txtResumo = view.findViewById(R.id.txtResumo);
        TextView txtEstudos = view.findViewById(R.id.txtEstudos);
        ProgressBar progress = view.findViewById(R.id.progressBar);

        SharedPreferences prefs = getActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);

        double aguaLitros = prefs.getFloat("agua_litros", 0f);
        double metaLitros = prefs.getFloat("meta_litros", 2.0f);
        int estudos = prefs.getInt("estudos_concluidos_min", 0);
        int metaEstudos = prefs.getInt("meta_estudos_min", 60);

        int aguaMl = (int) Math.round(aguaLitros * 1000);
        int metaMl = (int) Math.round(metaLitros * 1000);

        txtResumo.setText(aguaMl + " ml / " + metaMl + " ml");
        txtEstudos.setText(estudos + " min / " + metaEstudos + " min");

        progress.setMax(Math.max(metaMl, 1));
        progress.setProgress(Math.min(aguaMl, metaMl));

        return view;
    }
}
