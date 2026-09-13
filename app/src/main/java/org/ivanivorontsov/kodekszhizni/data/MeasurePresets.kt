package org.ivanivorontsov.kodekszhizni.data

/**
 * Catalog of measure presets. Type is stored as [Preset.id] String in Room.
 * Legacy ids DIGITAL_DETOX / EARLY_WAKE / NO_SOCIAL / CUSTOM are preserved.
 */
object MeasurePresets {
    data class Preset(
        val id: String,
        val title: String,
        val description: String = ""
    )

    val ALL: List<Preset> = listOf(
        Preset("DIGITAL_DETOX", "Цифровой детокс", "Ограничить экран и уведомления на срок"),
        Preset("EARLY_WAKE", "Ранний подъём", "Вставать в согласованное время"),
        Preset("NO_SOCIAL", "Без соцсетей", "Не заходить в соцсети и ленты"),
        Preset("NO_SWEETS", "Без сладостей", "Не есть сладкое и десерты"),
        Preset("COLD_SHOWER", "Холодный душ", "Ежедневный холодный или контрастный душ"),
        Preset("EARLY_SLEEP", "Ранний сон", "Ложиться не позже выбранного времени"),
        Preset("NO_JUNK_FOOD", "Без фастфуда", "Не есть фастфуд и полуфабрикаты"),
        Preset("DAILY_SPORT", "Ежедневный спорт", "Минимальная тренировка каждый день"),
        Preset("NO_ALCOHOL", "Без алкоголя", "Полный отказ от алкоголя на срок"),
        Preset("BUDGET_FREEZE", "Заморозка бюджета", "Только обязательные траты"),
        Preset("PHONE_AWAY", "Телефон в стороне", "Телефон вне зоны досягаемости в ключевые часы"),
        Preset("READING_VS_YOUTUBE", "Чтение вместо YouTube", "Читать вместо коротких видео"),
        Preset("WALK_30", "Прогулка 30 минут", "Не менее 30 минут ходьбы в день"),
        Preset("MEDITATION", "Медитация", "Ежедневная практика тишины/дыхания"),
        Preset("NO_COMPLAINTS", "Без жалоб", "Не жаловаться вслух; искать действие"),
        Preset("DIGITAL_SABBATH", "Цифровой шаббат", "Сутки без необязательных экранов"),
        Preset("NO_SMOKING", "Без курения", "Отказ от никотина на срок"),
        Preset("WATER_FIRST", "Вода до еды", "Стакан воды перед основными приёмами пищи"),
        Preset("NO_LATE_CAFFEINE", "Без позднего кофеина", "Не пить кофеин после обеда"),
        Preset("GRATITUDE_JOURNAL", "Дневник благодарности", "Записывать 3 благодарности в день"),
        Preset("DEEP_WORK", "Глубокая работа", "Защищённый блок фокуса без отвлечений"),
        Preset("NO_ONLINE_SHOPPING", "Без онлайн-покупок", "Не покупать онлайн импульсивно"),
        Preset("STRETCHING", "Растяжка", "Ежедневная короткая растяжка"),
        Preset("CUSTOM", "Своя мера", "Свободная формулировка")
    )

    fun find(id: String): Preset? = ALL.find { it.id == id }

    fun label(id: String): String = find(id)?.title ?: id
}
