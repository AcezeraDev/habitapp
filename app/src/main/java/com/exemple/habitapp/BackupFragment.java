package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class BackupFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView txtStatus;
    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String[]> importLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                uri -> {
                    if (uri != null) exportBackup(uri);
                }
        );

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) importBackup(uri);
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_backup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        txtStatus = view.findViewById(R.id.txtBackupStatus);
        TextView resumo = view.findViewById(R.id.txtBackupResumo);
        TextView detalhes = view.findViewById(R.id.txtBackupDetalhes);
        MaterialButton btnExport = view.findViewById(R.id.btnExportBackup);
        MaterialButton btnCloud = view.findViewById(R.id.btnCloudBackup);
        MaterialButton btnImport = view.findViewById(R.id.btnImportBackup);

        int medalhas = AchievementEngine.getUnlockedCount(AchievementEngine.getAchievements(prefs));
        resumo.setText("Ha " + prefs.getAll().size() + " registros salvos no HabitApp.");
        detalhes.setText("Inclui perfil, metas, " + medalhas + " medalhas, calendario, historico e lembretes.");
        btnExport.setOnClickListener(v -> exportLauncher.launch(defaultFileName()));
        btnCloud.setOnClickListener(v -> exportLauncher.launch(cloudFileName()));
        btnImport.setOnClickListener(v -> importLauncher.launch(new String[]{"application/json", "text/*"}));
    }

    private void exportBackup(Uri uri) {
        try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
            if (out == null) throw new IOException("Sem acesso ao arquivo.");
            out.write(buildBackupJson().toString(2).getBytes(StandardCharsets.UTF_8));
            txtStatus.setText("Backup exportado com sucesso.");
            Toast.makeText(requireContext(), "Backup exportado.", Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            txtStatus.setText("Nao foi possivel exportar o backup.");
            Toast.makeText(requireContext(), "Erro ao exportar backup.", Toast.LENGTH_SHORT).show();
        }
    }

    private void importBackup(Uri uri) {
        try {
            String json = readText(uri);
            JSONObject root = new JSONObject(json);
            JSONObject values = root.getJSONObject("values");
            SharedPreferences.Editor editor = prefs.edit().clear();

            Iterator<String> keys = values.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject item = values.getJSONObject(key);
                String type = item.getString("type");

                if ("String".equals(type)) {
                    editor.putString(key, item.optString("value", ""));
                } else if ("Integer".equals(type)) {
                    editor.putInt(key, item.getInt("value"));
                } else if ("Long".equals(type)) {
                    editor.putLong(key, item.getLong("value"));
                } else if ("Float".equals(type)) {
                    editor.putFloat(key, (float) item.getDouble("value"));
                } else if ("Boolean".equals(type)) {
                    editor.putBoolean(key, item.getBoolean("value"));
                }
            }

            editor.apply();
            HabitStore.ensureToday(prefs);
            ReminderScheduler.scheduleDefaultReminders(requireContext());
            HabitWidgetProvider.updateAll(requireContext());
            txtStatus.setText("Backup restaurado. Abra as telas para ver os dados atualizados.");
            Toast.makeText(requireContext(), "Backup restaurado.", Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            txtStatus.setText("Nao foi possivel restaurar esse arquivo.");
            Toast.makeText(requireContext(), "Erro ao restaurar backup.", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject buildBackupJson() throws JSONException {
        JSONObject root = new JSONObject();
        JSONObject values = new JSONObject();

        root.put("app", "HabitApp");
        root.put("version", 1);
        root.put("app_version", BuildConfig.VERSION_NAME);
        root.put("exported_at", System.currentTimeMillis());

        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;

            JSONObject item = new JSONObject();
            item.put("type", value.getClass().getSimpleName());
            item.put("value", value);
            values.put(entry.getKey(), item);
        }

        root.put("values", values);
        return root;
    }

    private String readText(Uri uri) throws IOException {
        StringBuilder builder = new StringBuilder();
        InputStream in = requireContext().getContentResolver().openInputStream(uri);
        if (in == null) throw new IOException("Arquivo indisponivel.");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private String defaultFileName() {
        String stamp = new SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.getDefault()).format(new Date());
        return "habitapp-backup-" + stamp + ".json";
    }

    private String cloudFileName() {
        String stamp = new SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.getDefault()).format(new Date());
        return "habitapp-cloud-backup-" + stamp + ".json";
    }
}
