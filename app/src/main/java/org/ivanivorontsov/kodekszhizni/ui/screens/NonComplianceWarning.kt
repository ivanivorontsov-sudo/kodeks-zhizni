package org.ivanivorontsov.kodekszhizni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun NonComplianceWarningDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        title = {
            Text(
                "Страшный кодекс",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Почему страшно не выполнять выводы своего кодекса",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Кодекс — это не декоративные слова на экране. Это договор с единственным " +
                        "человеком, от которого вы никуда не уйдёте: с собой. Когда вы формулируете " +
                        "закон и тут же его нарушаете, вы не «чуть-чуть расслабились». Вы учите " +
                        "собственную волю тому, что ваши решения ничего не значат."
                )
                Text(
                    "Сначала это кажется мелочью. Один пропуск. Одна «особая» ситуация. Один " +
                        "вечер, когда «можно исключение». Но психика не ведёт бухгалтерию оправданий — " +
                        "она запоминает факт: я сказал и не сделал. И каждый такой факт тихо " +
                        "подтачивает самодоверие. Вы начинаете меньше верить своим планам. " +
                        "Потом — своим обещаниям. Потом — вообще своей способности изменить жизнь."
                )
                Text(
                    "Страшно не в том, что мир вас накажет. Страшно в том, что вы сами " +
                        "перестаёте быть надёжным союзником для себя. Внутренний голос, который " +
                        "раньше говорил «мы доведём», начинает шептать «зачем стараться, всё равно " +
                        "сорвёмся». И вот уже кодекс превращается в музей благих намерений: красиво, " +
                        "пусто, стыдно открывать."
                )
                Text(
                    "Хаос приходит не взрывом, а привычкой. Без исполнения законов день " +
                        "рассыпается на импульсы: лента вместо сна, сладкое вместо решения, откладывание " +
                        "вместо действия. Вы снова и снова обещаете себе «с понедельника» — и каждый " +
                        "сорванный понедельник делает следующий ещё менее убедительным. Так рождается " +
                        "усталость не от работы, а от собственной лжи себе."
                )
                Text(
                    "Самое тяжёлое последствие — размытая идентичность. Человек, который держит " +
                        "слово перед собой, чувствует стержень. Человек, который коллекционирует " +
                        "невыполненные выводы, живёт в тумане: хочет уважать себя, но не находит " +
                        "доказательств. Внешние цели становятся декорацией, а внутри растёт тревога: " +
                        "«Если я не могу выполнить даже свои правила — кем я вообще являюсь?»"
                )
                Text(
                    "Поэтому кодекс требует серьёзности. Не жестокости к себе — серьёзности. " +
                        "Лучше один честный закон, который вы реально соблюдаете, чем десяток громких " +
                        "формулировок, которые вы научились игнорировать. Невыполнение не делает вас " +
                        "«свободным». Оно делает вас непредсказуемым для самого себя — а это и есть " +
                        "настоящий страх: потерять внутреннюю опору и смотреть, как жизнь снова " +
                        "управляется настроением, лентой и случайностью."
                )
                Text(
                    "Прочитайте. Запомните тяжесть этой цены. И если принимаете закон — принимайте " +
                        "его как обязательство, а не как красивую запись. Страшно не кодекс. " +
                        "Страшно жить так, будто ваши собственные выводы можно выбросить без последствий.",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Я понял") }
        }
    )
}
