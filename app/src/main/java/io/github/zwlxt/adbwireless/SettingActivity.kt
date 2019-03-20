package io.github.zwlxt.adbwireless

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.service.quicksettings.TileService
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SettingActivity : AppCompatActivity() {

    private lateinit var textInputLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        textInputLayout = findViewById(R.id.textInputLayout)
        val editPort = findViewById<TextInputEditText>(R.id.edit_port)
        val buttonSave = findViewById<MaterialButton>(R.id.button_save_setting)

        val preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

        val portPreference = preferences.getInt(WirelessAdbTileService.PREF_KEY_PORT, 5555)
        editPort.setText(portPreference.toString())

        buttonSave.setOnClickListener{
            try {
                val port = editPort.text.toString().toInt()
                if (port <= 0 || port > 65535) {
                    handleIllegalPortNumber()
                    return@setOnClickListener
                }
                preferences.edit()
                        .putInt(WirelessAdbTileService.PREF_KEY_PORT, port)
                        .apply()
                textInputLayout.isHelperTextEnabled = true
                textInputLayout.helperText = getString(R.string.settings_helper_text_saved)
            } catch (e: NumberFormatException) {
                handleIllegalPortNumber()
            }
        }

        TileService.requestListeningState(this,
                ComponentName(this, WirelessAdbTileService::class.java))
    }

    private fun handleIllegalPortNumber() {
        textInputLayout.isErrorEnabled = true
        textInputLayout.error = getString(R.string.settings_error_text_illegal)
    }
}
