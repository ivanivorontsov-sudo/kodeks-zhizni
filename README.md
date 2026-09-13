# Кодекс жизни / Kodeks Zhizni

Личный кодекс жизненных норм: законы-статьи, поиск, меры пресечения и ежедневные напоминания.

**Package:** `org.ivanivorontsov.kodekszhizni`  
**Version:** 1.1.0 (versionCode 2)  
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

## Возможности / Features (1.1.0)

| RU | EN |
|----|----|
| Законы кодекса с **блокировкой 30 дней** после создания (правка/удаление запрещены; дата разблокировки видна) | Laws lock for 30 days after create (edit/delete blocked) |
| Предупреждение перед добавлением закона + кнопка **«Страшный кодекс»** на главном экране | Pre-create confirmation + scary non-compliance warning on home |
| Меры **привязаны к закону** (обязательный выбор статьи) | Measures require a linked law |
| Расширенные русские пресеты мер (детокс, без сладкого, холодный душ, спорт, бюджет и др.); тип хранится как String | Expanded Russian measure presets; type as String |
| Без примеров-сидов: пустой старт; миграция удаляет `isSeed` | No seed examples; migration deletes seed rows |
| Поиск, Room, уведомления 3×/день + ежемесячный обзор | Search, Room, daily tips + monthly review |
| Material 3 Compose, minSdk 26, targetSdk 35 | Material 3 Compose UI |

### Миграция Room 1 → 2

- Добавляется `articles.lockedUntil` (= `createdAt` + 30 дней).
- Удаляются строки `isSeed = 1`.
- В `measures` добавляется `articleId` (у старых мер может быть `0` — закон «удалён» в UI).
- Тип меры уже был TEXT; enum заменён на String-пресеты с сохранением старых id (`DIGITAL_DETOX` и т.д.).

---

## CI

При push в `main` и по `workflow_dispatch` собирается debug APK и публикуется как артефакт `kodeks-zhizni-apk` (без секретов).

---

## Автор

Ivan Vorontsov
