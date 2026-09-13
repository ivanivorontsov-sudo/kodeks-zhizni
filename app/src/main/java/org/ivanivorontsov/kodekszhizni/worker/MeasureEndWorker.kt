package org.ivanivorontsov.kodekszhizni.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MeasureEndWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val measureId = inputData.getLong(KEY_MEASURE_ID, -1L)
        if (measureId < 0L) return Result.failure()
        MeasureEndCompleter.completeAndRing(applicationContext, measureId)
        return Result.success()
    }

    companion object {
        const val KEY_MEASURE_ID = "measure_id"
    }
}
