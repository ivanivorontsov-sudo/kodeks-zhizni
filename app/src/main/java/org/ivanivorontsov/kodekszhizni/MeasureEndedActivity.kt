package org.ivanivorontsov.kodekszhizni

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ivanivorontsov.kodekszhizni.ui.theme.KodeksTheme
import org.ivanivorontsov.kodekszhizni.worker.MeasureEndNotifier
import org.ivanivorontsov.kodekszhizni.worker.RingtonePlayer

/**
 * Full-screen «Мера окончена» with looping ringtone until the user dismisses.
 */
class MeasureEndedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val measureId = intent.getLongExtra(EXTRA_MEASURE_ID, -1L)
        val measureTitle = intent.getStringExtra(EXTRA_MEASURE_TITLE).orEmpty()
        val lawTitle = intent.getStringExtra(EXTRA_LAW_TITLE).orEmpty()
        val violationCount = intent.getIntExtra(EXTRA_VIOLATION_COUNT, 0)

        if (!RingtonePlayer.isPlaying()) {
            RingtonePlayer.start(this)
        }

        setContent {
            KodeksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Мера окончена",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            measureTitle.ifBlank { "Мера пресечения" },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Закон: ${lawTitle.ifBlank { "—" }}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Нарушений: $violationCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = { dismiss(measureId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Сбросить / Понял", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }

    private fun dismiss(measureId: Long) {
        RingtonePlayer.stop()
        if (measureId >= 0L) {
            MeasureEndNotifier.cancelNotification(this, measureId)
        }
        finish()
    }

    override fun onDestroy() {
        // Keep ringing if user leaves without pressing dismiss (e.g. Home);
        // only stop on explicit dismiss. If finishing after dismiss, already stopped.
        super.onDestroy()
    }

    companion object {
        const val EXTRA_MEASURE_ID = "measure_id"
        const val EXTRA_MEASURE_TITLE = "measure_title"
        const val EXTRA_LAW_TITLE = "law_title"
        const val EXTRA_VIOLATION_COUNT = "violation_count"
    }
}
