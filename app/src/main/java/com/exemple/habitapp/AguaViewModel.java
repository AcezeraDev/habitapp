package com.exemple.habitapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AguaViewModel extends AndroidViewModel {

    private final SharedPreferences prefs;
    private final MutableLiveData<Double> litrosLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> metaLiveData = new MutableLiveData<>();

    public AguaViewModel(@NonNull Application application) {
        super(application);
        prefs = application.getSharedPreferences("habit_data", Context.MODE_PRIVATE);

        litrosLiveData.setValue((double) prefs.getFloat("agua_litros", 0f));
        metaLiveData.setValue((double) prefs.getFloat("meta_litros", 2.0f)); // Padrão 2L
    }

    public LiveData<Double> getLitros() { return litrosLiveData; }
    public LiveData<Double> getMeta() { return metaLiveData; }

    public void adicionarAgua(double quantidadeMl) {
        double atual = (litrosLiveData.getValue() != null) ? litrosLiveData.getValue() : 0.0;
        double metaAtual = (metaLiveData.getValue() != null) ? metaLiveData.getValue() : 2.0;

        double quantidadeLitros = quantidadeMl / 1000.0;
        double novoValor = atual + quantidadeLitros;

        if (novoValor > metaAtual) {
            novoValor = metaAtual;
        }

        salvarLitros(novoValor);
    }

    public void salvarNovaMeta(double novaMetaLitros) {
        if (novaMetaLitros > 0) {
            prefs.edit().putFloat("meta_litros", (float) novaMetaLitros).apply();
            metaLiveData.setValue(novaMetaLitros);
        }
    }

    public void resetarAgua() { salvarLitros(0.0); }

    private void salvarLitros(double valor) {
        prefs.edit().putFloat("agua_litros", (float) valor).apply();
        litrosLiveData.setValue(valor);
    }
}