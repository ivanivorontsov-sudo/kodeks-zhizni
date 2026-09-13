package org.ivanivorontsov.kodekszhizni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ivanivorontsov.kodekszhizni.data.Article
import org.ivanivorontsov.kodekszhizni.data.computeLockedUntil
import org.ivanivorontsov.kodekszhizni.data.daysLeft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleEditScreen(
    articleId: Long,
    vm: ArticleViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var createdAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lockedUntil by remember { mutableLongStateOf(0L) }
    var isSeed by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(articleId == 0L) }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmCreate by remember { mutableStateOf(false) }
    val saveError by vm.saveError.collectAsState()
    val fmt = remember { SimpleDateFormat("d MMM yyyy, HH:mm", Locale("ru")) }
    val now = System.currentTimeMillis()
    val locked = articleId != 0L && lockedUntil > 0L && now < lockedUntil
    val daysRemaining = if (locked) {
        Article(
            title = "",
            body = "",
            createdAt = createdAt,
            lockedUntil = lockedUntil
        ).daysLeft(now)
    } else 0

    LaunchedEffect(articleId) {
        vm.clearSaveError()
        if (articleId != 0L) {
            vm.load(articleId)?.let { a ->
                title = a.title
                body = a.body
                tags = a.tags
                category = a.category
                createdAt = a.createdAt
                lockedUntil = a.lockedUntil
                isSeed = a.isSeed
            }
        }
        loaded = true
    }

    fun doSave() {
        val ts = System.currentTimeMillis()
        val created = if (articleId == 0L) ts else createdAt
        vm.save(
            Article(
                id = articleId,
                title = title.trim().ifBlank { "Без названия" },
                body = body.trim(),
                tags = tags.trim(),
                category = category.trim(),
                createdAt = created,
                updatedAt = ts,
                isSeed = false,
                lockedUntil = if (articleId == 0L) computeLockedUntil(created) else lockedUntil
            )
        ) { onBack() }
    }

    fun attemptSave() {
        if (locked) return
        if (articleId == 0L) {
            confirmCreate = true
        } else {
            doSave()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            articleId == 0L -> "Новый закон"
                            locked -> "Закон (только чтение)"
                            else -> "Закон"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (articleId != 0L && !locked) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Default.Delete, "Удалить")
                        }
                    }
                    if (!locked) {
                        IconButton(onClick = { attemptSave() }) {
                            Icon(Icons.Default.Save, "Сохранить")
                        }
                    } else {
                        IconButton(onClick = { /* locked */ }, enabled = false) {
                            Icon(Icons.Default.Lock, "Заблокировано")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (!loaded) return@Scaffold
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (locked) {
                Text(
                    "🔒 Закон заблокирован на 30 дней после создания.\n" +
                        "Разблокировка: ${fmt.format(Date(lockedUntil))} " +
                        "(осталось $daysRemaining дн.).\n" +
                        "Просмотр разрешён; правка и удаление недоступны.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            OutlinedTextField(
                value = title,
                onValueChange = { if (!locked) title = it },
                label = { Text("Заголовок") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = locked,
                enabled = !locked
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { if (!locked) category = it },
                    label = { Text("Категория") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    readOnly = locked,
                    enabled = !locked
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { if (!locked) tags = it },
                    label = { Text("Теги") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("сон,фокус") },
                    readOnly = locked,
                    enabled = !locked
                )
            }
            OutlinedTextField(
                value = body,
                onValueChange = { if (!locked) body = it },
                label = { Text("Текст (Markdown)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                minLines = 12,
                readOnly = locked,
                enabled = !locked
            )
            if (articleId != 0L) {
                Text(
                    buildString {
                        append("Создано: ${fmt.format(Date(createdAt))}")
                        if (lockedUntil > 0L) {
                            append(" · блокировка до ${fmt.format(Date(lockedUntil))}")
                        }
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            if (!locked) {
                Text(
                    "Подсказка: используйте ## заголовки и **жирный** для структуры. " +
                        "После сохранения закон нельзя править и удалять 30 дней.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        }
    }

    if (confirmCreate) {
        AlertDialog(
            onDismissRequest = { confirmCreate = false },
            title = { Text("Серьёзное обязательство") },
            text = {
                Text(
                    "Вы добавляете закон в свой кодекс. Это серьёзное обязательство:\n\n" +
                        "• в течение 30 дней закон нельзя будет изменить или удалить;\n" +
                        "• к закону можно привязать меру пресечения;\n" +
                        "• просмотр всегда доступен.\n\n" +
                        "Невыполнение выводов кодекса разрушает самодоверие и волю " +
                        "(см. «Страшный кодекс» на главном экране).\n\n" +
                        "Подтвердите, что готовы принять этот закон."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmCreate = false
                    doSave()
                }) { Text("Принимаю закон") }
            },
            dismissButton = {
                TextButton(onClick = { confirmCreate = false }) { Text("Отмена") }
            }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить закон?") },
            text = { Text("Действие необратимо.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.delete(articleId, onBack)
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отмена") }
            }
        )
    }

    saveError?.let { msg ->
        AlertDialog(
            onDismissRequest = { vm.clearSaveError() },
            title = { Text("Нельзя выполнить") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { vm.clearSaveError() }) { Text("Понятно") }
            }
        )
    }
}
