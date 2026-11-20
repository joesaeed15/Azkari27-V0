package com.azkari2027.app

import android.app.Application
import com.azkari2027.app.audio.OfflineAudioController
import com.azkari2027.app.data.AppSettings
import com.azkari2027.app.data.AzkariDatabaseHelper
import com.azkari2027.app.data.RamadanDatabaseHelper
import com.azkari2027.app.data.SettingsStore
import com.azkari2027.app.notifications.ReminderScheduler

class AzkariApp : Application() {
    lateinit var azkariDb: AzkariDatabaseHelper
        private set
    lateinit var ramadanDb: RamadanDatabaseHelper
        private set
    lateinit var settingsStore: SettingsStore
        private set
    lateinit var audioController: OfflineAudioController
        private set

    val defaultSettings = AppSettings()

    override fun onCreate() {
        super.onCreate()
        azkariDb = AzkariDatabaseHelper(this)
        ramadanDb = RamadanDatabaseHelper(this)
        settingsStore = SettingsStore(this)
        audioController = OfflineAudioController(this)
        ReminderScheduler.ensureChannel(this)
    }
}
