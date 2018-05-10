package io.github.zwlxt.adbwireless;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class InfoDialog extends AppCompatActivity {

    private final String TAG = "InfoDialog";
    public static String INTENT_EXTRA = "InfoDialog";
    private TextView textListeningStatus;
    private TextView textActiveStatus;
    private TextView textAddress;
    private EditText editPort;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_info);

        textListeningStatus = findViewById(R.id.textview_listneing_status);
        textActiveStatus = findViewById(R.id.textview_active_status);
        textAddress = findViewById(R.id.textview_address);
        editPort = findViewById(R.id.edit_port);
        progressBar = findViewById(R.id.progressBar);
        Button buttonSave = findViewById(R.id.button_save_settings);

        buttonSave.setOnClickListener(v -> {
            String portString = editPort.getEditableText().toString();
            int port = -1;
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                Toast.makeText(InfoDialog.this, e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                e.printStackTrace();
                return;
            }
            SharedPreferences sharedPreferences
                    = getSharedPreferences(getPackageName(), MODE_PRIVATE);
            sharedPreferences.edit()
                    .putInt(QuickSettingsService.PREF_PORT, port)
                    .apply();
            Toast.makeText(InfoDialog.this, R.string.next_time, Toast.LENGTH_SHORT)
                    .show();
            finish();
        });

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            ADBState adbState = ADBUtils.getState(this);
            runOnUiThread(() -> {
                setADBState(adbState);
                progressBar.setVisibility(View.INVISIBLE);
            });
        }).start();
    }

    private void setListeningState(boolean state) {
        if (state) {
            textListeningStatus.setText(R.string.yes);
            textListeningStatus.setTextColor(Color.GREEN);
        } else {
            textListeningStatus.setText(R.string.no);
            textListeningStatus.setTextColor(Color.RED);
        }
    }

    private void setActiveState(boolean state) {
        if (state) {
            textActiveStatus.setText(R.string.yes);
            textActiveStatus.setTextColor(Color.GREEN);
        } else {
            textActiveStatus.setText(R.string.no);
            textActiveStatus.setTextColor(Color.RED);
        }
    }

    private void setADBState(ADBState adbState) {
        textAddress.setText(adbState.getAddress());
        editPort.setText(String.valueOf(adbState.getPort()));
        switch (adbState.getState()) {
            case 0:
                setListeningState(false);
                setActiveState(false);
                break;
            case 1:
                setListeningState(true);
                setActiveState(false);
                break;
            case 3:
                setListeningState(true);
                setActiveState(true);
                break;
        }
    }
}
