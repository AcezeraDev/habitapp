package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HomeFragment extends Fragment {

    private SharedPreferences prefs;
    private ActivityResultLauncher<Void> cameraLauncher;
    private LinearLayout layoutGaleria;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);

        Button btnFoto = view.findViewById(R.id.btnFotoDia);
        layoutGaleria = view.findViewById(R.id.layoutGaleria);

        atualizarResumo(view);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                result -> {
                    if (result != null) {
                        salvarFotoDoDia(result);
                        atualizarGaleria();
                    } else {
                        Toast.makeText(getContext(), "Não foi possível tirar a foto.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnFoto.setOnClickListener(v -> {
            if (jaTirouFotoHoje()) {
                Toast.makeText(getContext(), "Você já tirou foto hoje!", Toast.LENGTH_SHORT).show();
            } else {
                cameraLauncher.launch(null);
            }
        });

        atualizarGaleria();
        return view;
    }

    private void atualizarResumo(View view) {
        TextView txtAguaHome = view.findViewById(R.id.txtAguaHome);
        TextView txtAguaFalta = view.findViewById(R.id.txtAguaFalta);
        TextView txtEstudosHome = view.findViewById(R.id.txtEstudosHome);
        TextView txtEstudosFalta = view.findViewById(R.id.txtEstudosFalta);

        double aguaLitros = prefs.getFloat("agua_litros", 0f);
        double metaLitros = prefs.getFloat("meta_litros", 2.0f);
        int estudosFeitos = prefs.getInt("estudos_concluidos_min", 0);
        int metaEstudos = prefs.getInt("meta_estudos_min", 60);

        int aguaMl = (int) Math.round(aguaLitros * 1000);
        int metaMl = (int) Math.round(metaLitros * 1000);
        int faltaAgua = Math.max(0, metaMl - aguaMl);
        int faltaEstudos = Math.max(0, metaEstudos - estudosFeitos);

        txtAguaHome.setText(aguaMl + " ml / " + metaMl + " ml");
        txtAguaFalta.setText("Faltam: " + faltaAgua + " ml");
        txtEstudosHome.setText(estudosFeitos + " min / " + metaEstudos + " min");
        txtEstudosFalta.setText("Faltam: " + faltaEstudos + " min");
    }

    private boolean jaTirouFotoHoje() {
        long hoje = System.currentTimeMillis() / (1000L * 60 * 60 * 24);
        long salvo = prefs.getLong("ultimo_dia_foto", -1);
        return hoje == salvo;
    }

    private void salvarFotoDoDia(Bitmap bitmap) {
        long hoje = System.currentTimeMillis() / (1000L * 60 * 60 * 24);
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

            Toast.makeText(getContext(), "Foto do dia " + diaAtual + " salva! 💪", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Erro ao salvar a foto.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void atualizarGaleria() {
        if (layoutGaleria == null) return;

        layoutGaleria.removeAllViews();

        TextView titulo = new TextView(getContext());
        titulo.setText("📅 Suas fotos do desafio");
        titulo.setTextSize(14f);
        titulo.setTextColor(0xFF38BDF8);
        titulo.setPadding(0, 16, 0, 12);
        layoutGaleria.addView(titulo);

        int diaAtual = prefs.getInt("challenge_day", 1);

        for (int dia = 1; dia < diaAtual; dia++) {
            String caminhoFoto = prefs.getString("foto_dia_" + dia, null);

            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(16, 16, 16, 16);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardParams);

            card.setBackgroundColor(0xFF1E293B);

            ImageView imagem = new ImageView(getContext());
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(140, 140);
            imagem.setLayoutParams(imgParams);
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
            textoDia.setTextSize(18f);
            textoDia.setTextColor(0xFFFFFFFF);
            textoDia.setPadding(24, 0, 0, 0);

            card.addView(imagem);
            card.addView(textoDia);

            layoutGaleria.addView(card);
        }

        if (diaAtual == 1) {
            TextView vazio = new TextView(getContext());
            vazio.setText("Nenhuma foto tirada ainda.");
            vazio.setTextSize(16f);
            vazio.setTextColor(0xFF64748B);
            vazio.setPadding(0, 8, 0, 8);
            layoutGaleria.addView(vazio);
        }
    }
}
