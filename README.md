# Кодекс жизни / Kodeks Zhizni

Личный кодекс жизненных норм: законы-статьи, поиск, меры пресечения и ежедневные напоминания.

**Package:** `org.ivanivorontsov.kodekszhizni`  
**Version:** 1.2.0 (versionCode 3)  
**Stack:** Kotlin · Jetpack Compose (Material 3) · Room · WorkManager · DataStore · AlarmManager

---

## Установка (RU)

1. Откройте [Actions](https://github.com/ivanivorontsov-sudo/kodeks-zhizni/actions) → последний успешный workflow **Build APK**.
2. Скачайте артефакт **kodeks-zhizni-apk** и распакуйте `app-debug.apk`.
3. На Android включите установку из неизвестных источников для файлового менеджера/браузера.
4. Установите APK. При первом запуске разрешите уведомления (Android 13+).
5. Для точного «звонка» по окончании меры: в настройках системы разрешите приложению **точные будильники** (Exact alarms) при запросе ОС.

Локальная сборка:

```bash
export JAVA_HOME=/home/box/jdk-17
export ANDROID_HOME=/home/box/Android/Sdk
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Требуются JDK 17+ и Android SDK (compileSdk 35).

---

## Install (EN)

1. Open [Actions](https://github.com/ivanivorontsov-sudo/kodeks-zhizni/actions) → latest green **Build APK** run.
2. Download artifact **kodeks-zhizni-apk**, unzip `app-debug.apk`.
3. Allow installs from unknown sources, then install.
4. Grant notification permission on first launch (Android 13+).
5. Allow **exact alarms** for the measure-end ringtone if the OS prompts.

```bash
./gradlew assembleDebug
```

---

## Возможности / Features (1.2.0)

| RU | EN |
|----|----|
| Мера сохраняется как **Ожидает (ARMED)** — таймер не стартует при создании | Measure saved as ARMED; timer does not start on create |
| Кнопка **«Нарушение»** увеличивает счётчик и запускает ACTIVE | **Violation** button increments count and starts ACTIVE |
| Пока ACTIVE: «Ещё нарушение» только ++count, таймер не сбрасывается | While ACTIVE: secondary button only ++count |
| После COMPLETED/STOPPED: «Нарушение» снова запускает период | After done: Violation can start a new ACTIVE period |
| По окончании меры — **звонок** (ringtone) + полноэкран «Мера окончена» | At endAt: looping ringtone + full-screen dismiss UI |
| Фильтры: Все / Ожидают / Активные / Завершённые | Filters: All / Pending / Active / Done |
| Расширенный текст «Страшный кодекс» (психология, здоровье, карьера, отношения) | Expanded scary non-compliance consequences text |
| Законы с **блокировкой 30 дней**; меры привязаны к закону | 30-day law lock; measures require linked law |

### Миграция Room 2 → 3

- Добавляется `measures.violationCount INTEGER NOT NULL DEFAULT 0`.
- Новый статус `ARMED` (хранится как TEXT через TypeConverter) — схема status не меняется.
- Существующие ACTIVE/COMPLETED/STOPPED строки сохраняются; пользовательские законы не удаляются.

### Миграция Room 1 → 2

- `articles.lockedUntil`; удаление `isSeed`; `measures.articleId`.

---

## Звонок по окончании меры / Measure-end ringing

1. Когда мера становится **ACTIVE** с `endAt`, планируются:
   - **AlarmManager** `setExactAndAllowWhileIdle` (точный будильник);
   - запасной **WorkManager** OneTime с той же задержкой.
2. По срабатыванию: статус → COMPLETED, играет `RingtoneManager` TYPE_RINGTONE (loop), high-priority notification с **full-screen intent** и Activity «Мера окончена».
3. Кнопка **«Сбросить / Понял»** останавливает рингтон и закрывает экран.
4. При старте приложения и после BOOT_COMPLETED: просроченные ACTIVE завершаются (со звонком) и будильники ACTIVE пересоздаются.

### Разрешения (targetSdk 35)

| Permission | Зачем |
|------------|-------|
| `POST_NOTIFICATIONS` | Канал окончания меры и советы |
| `USE_FULL_SCREEN_INTENT` | Полноэкран при окончании меры |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Точный будильник на `endAt` |
| `RECEIVE_BOOT_COMPLETED` | Перепланировать будильники после перезагрузки |
| `VIBRATE` / `WAKE_LOCK` | Вибрация канала / удержание при аларме |

На Android 12+ пользователь может отозвать точные будильники в настройках системы — тогда сработает WorkManager (менее точно в Doze).

---

## CI

При push в `main` и по `workflow_dispatch` собирается debug APK и публикуется как артефакт `kodeks-zhizni-apk` (без секретов).

---

## Автор

Ivan Vorontsov
