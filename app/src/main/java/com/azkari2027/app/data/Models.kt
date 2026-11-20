package com.azkari2027.app.data

data class Category(
    val id: Int,
    val name: String,
    val type: String,
    val isUserDefined: Boolean,
    val count: Int,
    val timeSeconds: Long,
    val languageId: Int,
    val icon: String,
    val prayerCount: Int = 0,
    val totalRepetitions: Int = 0
)

data class Prayer(
    val id: Int,
    val categoryId: Int,
    val text: String,
    val virtue: String,
    val repetition: Int,
    val order: Int,
    val languageId: Int,
    val audioName: String
)

data class RamadanDay(
    val id: Int,
    val nameAr: String,
    val nameEn: String
)

data class RamadanTaskType(
    val id: Int,
    val icon: String,
    val textAr: String,
    val textEn: String
)

data class RamadanTaskItem(
    val taskId: Int,
    val typeId: Int,
    val weight: Int,
    val textAr: String,
    val textEn: String,
    val optionsAr: List<String>,
    val optionsEn: List<String>,
    val weightAnswer: Int,
    val calculatedWeight: Double
) {
    val isMultiOption: Boolean get() = optionsAr.isNotEmpty()
    val isChecked: Boolean get() = weightAnswer > 0
}

enum class AppThemeMode { AMOLED_DARK, PARCHMENT_LIGHT, SYSTEM }

enum class AzkarFontFamily(val displayNameAr: String, val displayNameEn: String, val assetPath: String?) {
    DROID_NASKH("نسخ أصلي (Naskh)", "Original Naskh", "fonts/DroidNaskh-Regular.ttf"),
    DROID_KUFI("كوفي أصلي (Kufi)", "Original Kufi", "fonts/DroidKufi-Regular.ttf"),
    DROID_SANS_ARABIC("عربي مبسط (Sans)", "Droid Sans Arabic", "fonts/DroidSansArabic.ttf"),
    SYSTEM_DEFAULT("خط النظام", "System Default", null)
}

enum class ReminderKey(
    val prefKey: String,
    val requestCode: Int,
    val defaultHour: Int,
    val defaultMinute: Int,
    val arabicCategoryId: Int,
    val englishCategoryId: Int,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String
) {
    MORNING("rem_morning", 1001, 6, 30, 1, 47, "أذكار الصباح", "Morning Prayers", "حان وقت قراءة أذكار الصباح", "Time to read Morning Prayers"),
    EVENING("rem_evening", 1002, 17, 0, 2, 48, "أذكار المساء", "Evening Prayers", "حان وقت قراءة أذكار المساء", "Time to read Evening Prayers"),
    SLEEP("rem_sleep", 1003, 22, 30, 5, 51, "أذكار ما قبل النوم", "Remembrance Before Sleeping", "حان وقت قراءة أذكار النوم", "Time to read Remembrance before sleeping"),
    WAKE_UP("rem_wakeup", 1004, 5, 30, 6, 52, "أذكار الاستيقاظ", "Remembrance When Waking Up", "حان وقت قراءة أذكار الاستيقاظ من النوم", "Time to read Remembrance when waking up")
}

data class ReminderConfig(
    val key: ReminderKey,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

data class AppSettings(
    val languageId: Int = 1,
    val themeMode: AppThemeMode = AppThemeMode.AMOLED_DARK,
    val fontFamily: AzkarFontFamily = AzkarFontFamily.DROID_NASKH,
    val fontSizeSp: Float = 23f,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val autoAdvanceOnComplete: Boolean = true,
    val reminders: List<ReminderConfig> = ReminderKey.entries.map {
        ReminderConfig(it, enabled = false, hour = it.defaultHour, minute = it.defaultMinute)
    }
) {
    val isArabic: Boolean get() = languageId == 1
}
