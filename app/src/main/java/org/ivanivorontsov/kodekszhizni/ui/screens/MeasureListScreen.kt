package org.ivanivorontsov.kodekszhizni.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.ivanivorontsov.kodekszhizni.data.Article
import org.ivanivorontsov.kodekszhizni.data.Measure
import org.ivanivorontsov.kodekszhizni.data.MeasurePresets
import org.ivanivorontsov.kodekszhizni.data.MeasureStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasureListScreen(vm: MeasureViewModel) {
    val all by vm.measures.collectAsState()
    val articles by vm.articles.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var showNeedLaw by remember { mutableStateOf(false) }
    // 0 all, 1 armed/pending, 2 active, 3 done
    var filter by remember { mutableIntStateOf(0) }
    val startError by vm.startError.collectAsState()
    val toast by vm.toast.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(toast) {
        toast?.let {
            snackbar.showSnackbar(it)
            vm.clearToast()
        }
    }

    val filtered = when (filter) {
        1 -> all.filter { it.status == MeasureStatus.ARMED }
        2 -> all.filter { it.status == MeasureStatus.ACTIVE }
        3 -> all.filter {
            it.status == MeasureStatus.COMPLETED || it.status == MeasureStatus.STOPPED
        }
        else -> all
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Меры пресечения") }) },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (articles.isEmpty()) {
                    showNeedLaw = true
                } else {
                    showCreate = true
                }
            }) {
                Icon(Icons.Default.Add, "Новая мера")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filter == 0, onClick = { filter = 0 }, label = { Text("Все") })
                FilterChip(selected = filter == 1, onClick = { filter = 1 }, label = { Text("Ожидают") })
                FilterChip(selected = filter == 2, onClick = { filter = 2 }, label = { Text("Активные") })
                FilterChip(selected = filter == 3, onClick = { filter = 3 }, label = { Text("Завершённые") })
            }
            LazyColumn(
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { m ->
                    MeasureCard(
                        measure = m,
                        articleTitle = vm.articleTitle(m.articleId, articles),
                        onViolation = { vm.recordViolation(m.id) },
                        onComplete = { vm.stop(m.id, completed = true) },
                        onStop = { vm.stop(m.id, completed = false) },
                        onDelete = { vm.delete(m.id) }
                    )
                }
            }
        }
    }

    if (showNeedLaw) {
        AlertDialog(
            onDismissRequest = { showNeedLaw = false },
            title = { Text("Сначала создайте закон") },
            text = {
                Text(
                    "Мера пресечения должна быть привязана к закону кодекса. " +
                        "Перейдите во вкладку «Кодекс», добавьте закон, затем вернитесь сюда."
                )
            },
            confirmButton = {
                TextButton(onClick = { showNeedLaw = false }) { Text("Понятно") }
            }
        )
    }

    if (showCreate) {
        CreateMeasureDialog(
            articles = articles,
            onDismiss = { showCreate = false },
            onCreate = { typeId, title, desc, hours, daysUntil, articleId ->
                val endAbs = daysUntil?.let {
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(it)
                }
                vm.arm(typeId, title, desc, hours, endAbs, articleId)
                showCreate = false
            }
        )
    }

    startError?.let { msg ->
        AlertDialog(
            onDismissRequest = { vm.clearStartError() },
            title = { Text("Нельзя создать меру") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { vm.clearStartError() }) { Text("Понятно") }
            }
        )
    }
}

@Composable
private fun MeasureCard(
    measure: Measure,
    articleTitle: String,
    onViolation: () -> Unit,
    onComplete: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit
) {
    val fmt = remember { SimpleDateFormat("d MMM, HH:mm", Locale("ru")) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(MeasurePresets.label(measure.type)) })
                AssistChip(onClick = {}, label = { Text(statusLabel(measure.status)) })
            }
            Text(measure.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "Закон: $articleTitle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Нарушений: ${measure.violationCount}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (measure.description.isNotBlank()) {
                Text(measure.description, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                buildString {
                    when (measure.status) {
                        MeasureStatus.ARMED -> {
                            append("Ожидает нарушения")
                            if (measure.durationHours > 0) {
                                append(" · план ${measure.durationHours} ч")
                            } else {
                                append(" · без срока (до остановки)")
                            }
                        }
                        MeasureStatus.ACTIVE -> {
                            append("Старт: ${fmt.format(Date(measure.startedAt))}")
                            measure.endAt?.let { append(" · до ${fmt.format(Date(it))}") }
                                ?: append(" · без срока")
                            if (measure.durationHours > 0) append(" · ${measure.durationHours} ч")
                        }
                        else -> {
                            measure.completedAt?.let {
                                append("Завершена: ${fmt.format(Date(it))}")
                            }
                            if (measure.durationHours > 0) append(" · план ${measure.durationHours} ч")
                        }
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            when (measure.status) {
                MeasureStatus.ARMED -> {
                    Button(
                        onClick = onViolation,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Нарушение", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onDelete) { Text("Удалить") }
                }
                MeasureStatus.ACTIVE -> {
                    OutlinedButton(
                        onClick = onViolation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ещё нарушение")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onComplete) { Text("Выполнено") }
                        TextButton(onClick = onStop) { Text("Остановить") }
                    }
                }
                MeasureStatus.COMPLETED, MeasureStatus.STOPPED -> {
                    Button(
                        onClick = onViolation,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Нарушение — запустить снова", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onDelete) { Text("Удалить") }
                }
            }
        }
    }
}

@Composable
private fun CreateMeasureDialog(
    articles: List<Article>,
    onDismiss: () -> Unit,
    onCreate: (typeId: String, title: String, desc: String, hours: Long, days: Long?, articleId: Long) -> Unit
) {
    var typeId by remember { mutableStateOf(MeasurePresets.ALL.first().id) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var hoursText by remember { mutableStateOf("24") }
    var daysText by remember { mutableStateOf("") }
    var articleId by remember { mutableLongStateOf(articles.firstOrNull()?.id ?: 0L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая мера") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Мера сохранится в ожидании. Таймер запустится только после кнопки «Нарушение».",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text("Тип меры", style = MaterialTheme.typography.labelLarge)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MeasurePresets.ALL.forEach { preset ->
                        FilterChip(
                            selected = typeId == preset.id,
                            onClick = {
                                typeId = preset.id
                                if (title.isBlank() || MeasurePresets.ALL.any { it.title == title }) {
                                    title = preset.title
                                }
                                if (desc.isBlank() || MeasurePresets.ALL.any { it.description == desc }) {
                                    desc = preset.description
                                }
                            },
                            label = {
                                Text(preset.title, style = MaterialTheme.typography.labelSmall)
                            }
                        )
                    }
                }
                Text("Закон кодекса", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    articles.forEach { a ->
                        FilterChip(
                            selected = articleId == a.id,
                            onClick = { articleId = a.id },
                            label = { Text(a.title, maxLines = 1) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { hoursText = it.filter { c -> c.isDigit() } },
                    label = { Text("Плановая длительность (часы)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it.filter { c -> c.isDigit() } },
                    label = { Text("Или план: N дней (в часы)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (articleId <= 0L) return@TextButton
                    val hours = hoursText.toLongOrNull() ?: 0L
                    val days = daysText.toLongOrNull()
                    onCreate(typeId, title, desc, hours, days, articleId)
                },
                enabled = articleId > 0L
            ) { Text("В ожидание") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private fun statusLabel(s: MeasureStatus) = when (s) {
    MeasureStatus.ARMED -> "Ожидает"
    MeasureStatus.ACTIVE -> "Активна"
    MeasureStatus.COMPLETED -> "Выполнена"
    MeasureStatus.STOPPED -> "Остановлена"
}
