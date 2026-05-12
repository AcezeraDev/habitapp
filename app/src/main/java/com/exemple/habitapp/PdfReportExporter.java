package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class PdfReportExporter {

    private PdfReportExporter() {
    }

    public static void exportWeeklyReport(Context context, SharedPreferences prefs, Uri uri) throws IOException {
        HabitStore.ensureToday(prefs);
        HabitStore.saveTodaySnapshot(prefs);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int y = 54;
        paint.setTextSize(26f);
        paint.setFakeBoldText(true);
        canvas.drawText("HabitApp - Relatorio semanal", 40, y, paint);

        y += 32;
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        canvas.drawText("Gerado em " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()), 40, y, paint);

        y += 42;
        drawLine(canvas, paint, "Score hoje: " + HabitStore.getTodayScore(prefs) + "%", y);
        y += 24;
        drawLine(canvas, paint, "Media semanal: " + HabitStore.getWeeklyAverage(prefs) + "%", y);
        y += 24;
        drawLine(canvas, paint, "Sequencia: " + HabitStore.getStreak(prefs) + " dias", y);
        y += 24;
        drawLine(canvas, paint, "Nivel XP: " + XpEngine.getLevel(prefs) + " (" + XpEngine.getBaseXp(prefs) + " XP)", y);
        y += 24;
        drawLine(canvas, paint, "Agua total registrada: " + prefs.getInt("total_agua_ml_registrado", 0) + " ml", y);
        y += 24;
        drawLine(canvas, paint, "Foco total registrado: " + prefs.getInt("total_foco_min_registrado", 0) + " min", y);

        y += 42;
        paint.setFakeBoldText(true);
        drawLine(canvas, paint, "Ultimos 7 dias", y);
        paint.setFakeBoldText(false);
        int[] scores = HabitStore.getWeekScores(prefs);
        for (int i = 0; i < scores.length; i++) {
            y += 24;
            drawLine(canvas, paint, (i == 6 ? "Hoje" : "-" + (6 - i) + "d") + ": " + scores[i] + "%", y);
        }

        y += 42;
        paint.setFakeBoldText(true);
        drawLine(canvas, paint, "Dica da proxima semana", y);
        paint.setFakeBoldText(false);
        y += 24;
        drawLine(canvas, paint, buildTip(prefs), y);

        document.finishPage(page);

        try (OutputStream output = context.getContentResolver().openOutputStream(uri)) {
            if (output == null) throw new IOException("Sem acesso ao arquivo.");
            document.writeTo(output);
        } finally {
            document.close();
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, String text, int y) {
        canvas.drawText(text, 40, y, paint);
    }

    private static String buildTip(SharedPreferences prefs) {
        int agua = HabitStore.percent(HabitStore.getAguaMl(prefs), HabitStore.getMetaAguaMl(prefs));
        int foco = HabitStore.percent(prefs.getInt("estudos_concluidos_min", 0), prefs.getInt("meta_estudos_min", 60));
        if (agua < foco) return "Priorize hidratacao cedo para subir o score sem depender do fim do dia.";
        if (foco < agua) return "Agende um bloco curto de foco antes das distrações do dia.";
        return "Mantenha metas pequenas e repetiveis para proteger a sequencia.";
    }
}
