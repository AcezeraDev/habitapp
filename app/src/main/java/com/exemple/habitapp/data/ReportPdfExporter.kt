package com.exemple.habitapp.data

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportPdfExporter {
    @Throws(IOException::class)
    fun exportWeeklyReport(context: Context, state: HabitDashboardState, uri: Uri) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        var y = 54
        paint.textSize = 26f
        paint.isFakeBoldText = true
        canvas.drawText("HabitApp - Relatório semanal", 40f, y.toFloat(), paint)

        y += 32
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Gerado em ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}", 40f, y.toFloat(), paint)

        y += 42
        listOf(
            "Score hoje: ${state.score}%",
            "Média semanal: ${state.weeklyAverage}%",
            "Sequência: ${state.streak} dias",
            "Nível XP: ${state.xpLevel} (${state.xp} XP)",
            "Água total registrada: ${state.totalWaterMl} ml",
            "Foco total registrado: ${state.totalFocusMinutes} min",
        ).forEach {
            drawLine(canvas, paint, it, y)
            y += 24
        }

        y += 18
        paint.isFakeBoldText = true
        drawLine(canvas, paint, "Últimos 7 dias", y)
        paint.isFakeBoldText = false
        state.weekScores.forEachIndexed { index, score ->
            y += 24
            drawLine(canvas, paint, "${if (index == state.weekScores.lastIndex) "Hoje" else "-${state.weekScores.lastIndex - index}d"}: $score%", y)
        }

        y += 42
        paint.isFakeBoldText = true
        drawLine(canvas, paint, "Dica da próxima semana", y)
        paint.isFakeBoldText = false
        y += 24
        drawLine(canvas, paint, smartTip(state), y)

        document.finishPage(page)
        try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                document.writeTo(output)
            } ?: throw IOException("Sem acesso ao arquivo.")
        } finally {
            document.close()
        }
    }

    private fun drawLine(canvas: Canvas, paint: Paint, text: String, y: Int) {
        canvas.drawText(text.take(92), 40f, y.toFloat(), paint)
    }

    private fun smartTip(state: HabitDashboardState): String {
        return when {
            state.waterPercent < state.focusPercent -> "Priorize hidratação cedo para subir o score sem depender do fim do dia."
            state.focusPercent < state.waterPercent -> "Agende um bloco curto de foco antes das distrações do dia."
            else -> "Mantenha metas pequenas e repetíveis para proteger a sequência."
        }
    }
}
