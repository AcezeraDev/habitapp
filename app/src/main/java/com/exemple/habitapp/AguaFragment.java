package com.exemple.habitapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.exemple.habitapp.databinding.FragmentAguaBinding;

import java.util.Locale;

public class AguaFragment extends Fragment {

    private FragmentAguaBinding binding;
    private AguaViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAguaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AguaViewModel.class);

        configurarObservadores();
        configurarBotoes();
    }

    private void configurarObservadores() {

        viewModel.getLitros().observe(getViewLifecycleOwner(), litros -> {

            double meta = viewModel.getMeta().getValue() != null
                    ? viewModel.getMeta().getValue()
                    : 2.0;

            // litros exibidos
            String textoLitros = String.format(Locale.getDefault(), "%.2f L", litros);
            binding.txtAgua.setText(textoLitros);

            // progresso
            binding.progressoAgua.setProgressCompat((int) (litros * 100), true);

            // 🔥 quanto falta
            double faltaMl = Math.max(0, (meta - litros) * 1000);

            if (faltaMl == 0) {
                binding.txtFaltam.setText("Meta atingida! 🎉");
            } else {
                // 🔥 troca ponto por vírgula
                String texto = String.format(Locale.getDefault(),
                        "Faltam %,.0f ml", faltaMl);

                binding.txtFaltam.setText(texto.replace(",", "."));
            }
        });

        viewModel.getMeta().observe(getViewLifecycleOwner(), meta -> {

            String textoMeta = String.format(Locale.getDefault(), "Meta: %.2f L", meta);
            binding.txtMetaLabel.setText(textoMeta);

            binding.progressoAgua.setMax((int) (meta * 100));

            if (binding.editMeta.getText().toString().isEmpty()) {
                binding.editMeta.setText(String.valueOf(meta));
            }
        });
    }

    private void configurarBotoes() {

        // ➕ adicionar água (AGORA COM INPUT PERSONALIZADO)
        binding.btnAdd.setOnClickListener(v -> {

            String input = binding.editQuantidade.getText().toString();

            if (!input.isEmpty()) {

                try {
                    double quantidadeMl = Double.parseDouble(input);

                    double atual = viewModel.getLitros().getValue() != null ? viewModel.getLitros().getValue() : 0.0;
                    double meta = viewModel.getMeta().getValue() != null ? viewModel.getMeta().getValue() : 2.0;

                    if (atual >= meta) {
                        Toast.makeText(requireContext(), "Meta já atingida! 🌊", Toast.LENGTH_SHORT).show();
                    } else {
                        viewModel.adicionarAgua(quantidadeMl);

                        Toast.makeText(requireContext(), "+" + input + "ml adicionados!", Toast.LENGTH_SHORT).show();

                        binding.editQuantidade.setText("");
                        binding.editQuantidade.clearFocus();
                    }

                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Número inválido", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(requireContext(), "Digite a quantidade em ml", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔄 reset
        binding.btnReset.setOnClickListener(v -> viewModel.resetarAgua());

        // 🎯 salvar meta
        binding.layoutMetaInput.setEndIconOnClickListener(v -> {

            String input = binding.editMeta.getText().toString();

            if (!input.isEmpty()) {
                try {
                    double novaMeta = Double.parseDouble(input);

                    viewModel.salvarNovaMeta(novaMeta);

                    binding.editMeta.clearFocus();

                    Toast.makeText(requireContext(), "Nova meta salva! 🎯", Toast.LENGTH_SHORT).show();

                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Número inválido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}