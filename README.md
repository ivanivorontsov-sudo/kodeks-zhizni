# Кодекс жизни / Kodeks Zhizni

Личный кодекс жизненных норм: статьи-уроки, поиск, меры пресечения и ежедневные напоминания.

**Package:** `org.ivanivorontsov.kodekszhizni`  
**Stack:** Kotlin · Jetpack Compose (Material 3) · Room · WorkManager · DataStore

---

## Установка (RU)

1. Откройте [Actions](https://github.com/ivanivorontsov-sudo/kodeks-zhizni/actions) → последний успешный workflow **Build APK**.
2. Скачайте артефакт **kodeks-zhizni-apk** и распакуйте `app-debug.apk`.
3. На Android включите установку из неизвестных источников для файлового менеджера/браузера.
4. Установите APK. При первом запуске разрешите уведомления (Android 13+).

Локальная сборка:

```bash
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

```bash
./gradlew assembleDebug
```

---

## Возможности / Features

| RU | EN |
|----|----|
| Редактор статей (создание, правка, удаление): заголовок, Markdown-текст, теги/категории, метки времени | Full article editor with markdown body, tags/categories, timestamps |
| Поиск по названию, тексту и тегам | Search across title, body, tags |
| Room / SQLite хранилище | Room / SQLite persistence |
| Меры пресечения: цифровой детокс, ранний подъём, без соцсетей, своя мера + длительность; активные/завершённые | Restraint measures: detox, early wake, no social, custom + duration |
| ~60 исходных русских статей-уроков при первом запуске | ~60 original Russian life-lesson articles on first launch |
| Уведомления 3× в день + ежемесячный «пересмотри нормы» (WorkManager) | 3× daily rotating tips + monthly norms review |
| Настройка времени и переключатели | Configurable times and toggles |
| Material 3 Compose, minSdk 26, targetSdk 35 | Material 3 Compose UI |

---

## CI

При push в `main` и по `workflow_dispatch` собирается debug APK и публикуется как артефакт `kodeks-zhizni-apk` (без секретов).

---

## Автор

Ivan Vorontsov
