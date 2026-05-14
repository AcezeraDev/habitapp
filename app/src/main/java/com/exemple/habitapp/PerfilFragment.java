package com.exemple.habitapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PerfilFragment extends Fragment {

    private SharedPreferences prefs;
    private ActivityResultLauncher<String> avatarLauncher;
    private ImageView imgAvatar;
    private TextView txtTitulo;
    private TextView txtSubtitulo;
    private TextView txtEmail;
    private TextView txtBadges;
    private TextView txtNivel;
    private TextView txtStats;
    private TextView txtResumo;
    private TextInputEditText inputNome;
    private TextInputEditText inputEmail;
    private TextInputEditText inputObjetivo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        avatarLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                salvarAvatar(uri);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);

        imgAvatar = view.findViewById(R.id.imgPerfilAvatar);
        txtTitulo = view.findViewById(R.id.txtPerfilTitulo);
        txtSubtitulo = view.findViewById(R.id.txtPerfilSubtitulo);
        txtEmail = view.findViewById(R.id.txtPerfilEmail);
        txtBadges = view.findViewById(R.id.txtPerfilBadges);
        txtNivel = view.findViewById(R.id.txtPerfilNivel);
        txtStats = view.findViewById(R.id.txtPerfilStats);
        txtResumo = view.findViewById(R.id.txtPerfilResumo);
        inputNome = view.findViewById(R.id.inputPerfilNome);
        inputEmail = view.findViewById(R.id.inputPerfilEmail);
        inputObjetivo = view.findViewById(R.id.inputPerfilObjetivo);
        MaterialButton btnSalvar = view.findViewById(R.id.btnSalvarPerfil);
        MaterialButton btnTrocarFoto = view.findViewById(R.id.btnTrocarFotoPerfil);
        MaterialButton btnRemoverFoto = view.findViewById(R.id.btnRemoverFotoPerfil);
        MaterialButton btnSair = view.findViewById(R.id.btnSairPerfil);

        inputNome.setText(prefs.getString("nome_usuario", "Guerreiro"));
        inputEmail.setText(prefs.getString("email_usuario", ""));
        inputObjetivo.setText(prefs.getString("objetivo_principal", "Mais disciplina"));
        btnSalvar.setOnClickListener(v -> salvarPerfil());
        btnTrocarFoto.setOnClickListener(v -> avatarLauncher.launch("image/*"));
        btnRemoverFoto.setOnClickListener(v -> removerAvatar());
        btnSair.setOnClickListener(v -> sairDaConta());

        carregarAvatar();
        atualizarTela();
        UiAnimator.enter(view);
        return view;
    }

    private void salvarPerfil() {
        String nome = inputNome.getText() != null ? inputNome.getText().toString().trim() : "";
        String email = inputEmail.getText() != null ? inputEmail.getText().toString().trim() : "";
        String objetivo = inputObjetivo.getText() != null ? inputObjetivo.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nome)) nome = "Guerreiro";
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Digite um e-mail válido.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(objetivo)) objetivo = "Mais disciplina";

        prefs.edit()
                .putString("nome_usuario", nome)
                .putString("email_usuario", email)
                .putString("objetivo_principal", objetivo)
                .apply();

        atualizarTela();
        Toast.makeText(getContext(), "Perfil salvo.", Toast.LENGTH_SHORT).show();
    }

    private void atualizarTela() {
        String nome = prefs.getString("nome_usuario", "Guerreiro");
        String email = prefs.getString("email_usuario", "");
        String objetivo = prefs.getString("objetivo_principal", "Mais disciplina");
        int streak = HabitStore.getStreak(prefs);
        int media = HabitStore.getWeeklyAverage(prefs);
        int aguaTotal = prefs.getInt("total_agua_ml_registrado", 0);
        int focoTotal = prefs.getInt("total_foco_min_registrado", 0);
        int medalhas = AchievementEngine.getUnlockedCount(AchievementEngine.getAchievements(prefs));

        txtTitulo.setText("Perfil de " + nome);
        txtSubtitulo.setText(objetivo);
        txtEmail.setText(TextUtils.isEmpty(email) ? "Sem e-mail cadastrado" : email);
        txtBadges.setText(medalhas + (medalhas == 1 ? " medalha liberada" : " medalhas liberadas"));
        txtNivel.setText("Nível " + HabitStore.getLevelName(prefs));
        txtStats.setText("Sequência " + streak + (streak == 1 ? " dia" : " dias") + " | média " + media + "%");
        txtResumo.setText("Total registrado: " + aguaTotal + " ml de água e " + focoTotal + " min de foco. Seu objetivo atual é: " + objetivo + ".");
    }

    private void salvarAvatar(Uri uri) {
        File arquivo = getAvatarFile();
        try (InputStream input = requireContext().getContentResolver().openInputStream(uri);
             OutputStream output = new FileOutputStream(arquivo)) {
            if (input == null) {
                Toast.makeText(getContext(), "Não consegui abrir essa imagem.", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            Bitmap avatar = decodeSampledBitmap(arquivo.getAbsolutePath(), 320, 320);
            if (avatar == null) {
                if (arquivo.exists()) arquivo.delete();
                Toast.makeText(getContext(), "Escolha uma imagem válida.", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putString("perfil_foto_path", arquivo.getAbsolutePath())
                    .apply();
            carregarAvatar();
            Toast.makeText(getContext(), "Foto do perfil atualizada.", Toast.LENGTH_SHORT).show();
        } catch (IOException | SecurityException ex) {
            Toast.makeText(getContext(), "Não consegui salvar a foto.", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarAvatar() {
        String caminho = prefs != null ? prefs.getString("perfil_foto_path", "") : "";
        if (!TextUtils.isEmpty(caminho)) {
            Bitmap avatar = decodeSampledBitmap(caminho, 320, 320);
            if (avatar != null) {
                imgAvatar.setPadding(0, 0, 0, 0);
                imgAvatar.clearColorFilter();
                imgAvatar.setImageBitmap(avatar);
                return;
            }
        }

        imgAvatar.setPadding(dp(18), dp(18), dp(18), dp(18));
        imgAvatar.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary));
        imgAvatar.setImageResource(R.drawable.ic_nav_profile);
    }

    private void removerAvatar() {
        File arquivo = getAvatarFile();
        if (arquivo.exists()) {
            arquivo.delete();
        }

        prefs.edit().remove("perfil_foto_path").apply();
        carregarAvatar();
        Toast.makeText(getContext(), "Foto removida.", Toast.LENGTH_SHORT).show();
    }

    private void sairDaConta() {
        prefs.edit().putBoolean("perfil_logado", false).apply();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private File getAvatarFile() {
        return new File(requireContext().getFilesDir(), "profile_avatar.img");
    }

    private Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds, reqWidth, reqHeight);
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
