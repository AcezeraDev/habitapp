package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RelatorioFragment extends Fragment {

    private SharedPreferences prefs;
    private ActivityResultLauncher<String> pdfLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pdfLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
            if (uri != null) exportPdf(uri);
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feature_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);
        HabitStore.saveTodaySnapshot(prefs);

        int bestScore = 0;
        int worstScore = 101;
        int bestOffset = 0;
        int weakOffset = 0;
        for (int offset = 0; offset >= -6; offset--) {
            int score = HabitStore.getScoreForDay(prefs, HabitStore.dayKey(offset));
            if (score > bestScore) {
                bestScore = score;
                bestOffset = offset;
            }
            if (score < worstScore) {
                worstScore = score;
                weakOffset = offset;
            }
        }

        ((TextView) view.findViewById(R.id.txtFeatureTitle)).setText("Relatorio semanal");
        ((TextView) view.findViewById(R.id.txtFeatureSubtitle)).setText("Um resumo automatico para entender sua semana.");
        ((TextView) view.findViewById(R.id.txtFeatureHeroTitle)).setText(HabitStore.getWeeklyAverage(prefs) + "% media");
        ((TextView) view.findViewById(R.id.txtFeatureHeroSubtitle)).setText("Melhor dia: " + labelDay(bestOffset) + " (" + bestScore + "%). Ponto de ajuste: " + labelDay(weakOffset) + " (" + worstScore + "%).");
        ((LinearProgressIndicator) view.findViewById(R.id.progressFeatureHero)).setProgressCompat(HabitStore.getWeeklyAverage(prefs), true);

        LinearLayout list = view.findViewById(R.id.layoutFeatureContent);
        list.removeAllViews();
        FeatureUi.addCard(requireContext(), list, "Ranking pessoal", "Melhor score " + bestScore + "% | menor score " + worstScore + "% | sequencia " + HabitStore.getStreak(prefs) + " dias.", -1, R.color.primary);
        FeatureUi.addCard(requireContext(), list, "Totais", prefs.getInt("total_agua_ml_registrado", 0) + " ml de agua | " + prefs.getInt("total_foco_min_registrado", 0) + " min de foco.", -1, R.color.water);
        FeatureUi.addCard(requireContext(), list, "Dica inteligente", smartTip(), -1, R.color.success);

        MaterialButton primary = view.findViewById(R.id.btnFeaturePrimary);
        primary.setText("Exportar PDF");
        primary.setOnClickListener(v -> pdfLauncher.launch(fileName()));

        MaterialButton secondary = view.findViewById(R.id.btnFeatureSecondary);
        secondary.setText("Abrir estatisticas");
        secondary.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(R.id.estatisticas);
            }
        });
    }

    private void exportPdf(Uri uri) {
        try {
            PdfReportExporter.exportWeeklyReport(requireContext(), prefs, uri);
            FeedbackHelper.success(requireContext());
            Toast.makeText(requireContext(), "PDF exportado.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Nao consegui exportar o PDF.", Toast.LENGTH_SHORT).show();
        }
    }

    private String smartTip() {
        int agua = HabitStore.percent(HabitStore.getAguaMl(prefs), HabitStore.getMetaAguaMl(prefs));
        int foco = HabitStore.percent(prefs.getInt("estudos_concluidos_min", 0), prefs.getInt("meta_estudos_min", 60));
        if (agua < foco) return "A agua esta puxando o score para baixo. Feche 500 ml mais cedo.";
        if (foco < agua) return "O foco esta atrasado. Use uma sessao curta antes de abrir outras tarefas.";
        return "A semana esta equilibrada. Proteja o basico e suba a consistencia.";
    }

    private String labelDay(int offset) {
        if (offset == 0) return "Hoje";
        if (offset == -1) return "Ontem";
        return offset + "d";
    }

    private String fileName() {
        String stamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return "habitapp-relatorio-" + stamp + ".pdf";
    }
}
