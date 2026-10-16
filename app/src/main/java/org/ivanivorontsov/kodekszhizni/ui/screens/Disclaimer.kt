package org.ivanivorontsov.kodekszhizni.ui.screens

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

object Disclaimer {
    const val TITLE = "Важно"
    const val TEXT = "«Кодекс жизни» — личный дневник норм и самодисциплины. Приложение не является юридической консультацией. Тексты, «статьи» и «меры пресечения» в нём не имеют юридической силы и не связаны с законодательством. По правовым вопросам сверяйтесь с официальными источниками (например, pravo.gov.ru) и обращайтесь к юристу."
    private const val PREFS = "disclaimer"
    private const val KEY = "accepted_v1"

    fun isAccepted(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false)

    fun accept(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, true).apply()
    }
}

@Composable
fun FirstRunDisclaimer() {
    val context = LocalContext.current
    var show by remember { mutableStateOf(!Disclaimer.isAccepted(context)) }
    if (show) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(Disclaimer.TITLE) },
            text = { Text(Disclaimer.TEXT) },
            confirmButton = {
                TextButton(onClick = {
                    Disclaimer.accept(context)
                    show = false
                }) { Text("Понятно") }
            }
        )
    }
}
