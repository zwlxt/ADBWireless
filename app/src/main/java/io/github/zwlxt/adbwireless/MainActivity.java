package io.github.zwlxt.adbwireless;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.textview_listneing_status)
    TextView textViewListeningStatus;

    @BindView(R.id.textview_active_status)
    TextView textViewActiveStatus;

    @BindView(R.id.textview_address)
    TextView textViewAddress;

    @BindView(R.id.edit_port)
    EditText editPort;

    @BindView(R.id.textview_instruction)
    TextView textViewInstruction;

    @BindView(R.id.button_start_stop)
    Button buttonAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        updateStatus();
    }

    private void setListeningStatus(boolean status) {
        if (status) {
            textViewListeningStatus.setText(R.string.yes);
            textViewListeningStatus.setTextColor(Color.GREEN);

            buttonAction.setText(R.string.stop_adb);
            buttonAction.setOnClickListener(v -> controlADB(false));
        } else {
            textViewListeningStatus.setText(R.string.no);
            textViewListeningStatus.setTextColor(Color.RED);

            buttonAction.setText(R.string.start_adb);
            buttonAction.setOnClickListener(v -> controlADB(true));
        }
    }

    private void setActiveStatus(boolean status) {
        if (status) {
            textViewActiveStatus.setText(R.string.yes);
            textViewActiveStatus.setTextColor(Color.GREEN);
        } else {
            textViewActiveStatus.setText(R.string.no);
            textViewActiveStatus.setTextColor(Color.RED);
        }
    }

    private void setInstructionText(String ip, int port) {
        final String instructionTemplate = getString(R.string.instruction_on);
        textViewInstruction.setText(String.format(Locale.getDefault(),
                instructionTemplate, ip, port));
    }

    private void setInstructionText() {
        textViewInstruction.setText(R.string.instruction_off);
    }

    @OnClick(R.id.button_refresh)
    public void updateStatus() {
        new Thread(() -> {
            int status = ADBUtils.getStatus();

            runOnUiThread(() -> {
                String ipAddress;
                if (ADBUtils.isWifiConnected(MainActivity.this)) {
                    ipAddress = ADBUtils.getAddress(MainActivity.this);
                } else {
                    ipAddress = getString(R.string.wifi_not_connected);
                    setInstructionText();
                }
                textViewAddress.setText(ipAddress);
                switch (status) {
                    case 0:
                        setListeningStatus(false);
                        setActiveStatus(false);
                        setInstructionText();
                        break;
                    case 1:
                        setListeningStatus(true);
                        setActiveStatus(false);
                        setInstructionText(ipAddress, getDefinedPort());
                        break;
                    case 3:
                        setListeningStatus(true);
                        setActiveStatus(true);
                        setInstructionText(ipAddress, getDefinedPort());
                        break;
                    default:
                }
            });
        }).start();
    }

    private int getDefinedPort() {
        String portString = editPort.getEditableText().toString();
        try {
            return Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void controlADB(boolean newState) {
        new Thread(() -> {
            if (newState) {
                ADBUtils.start(getDefinedPort());
            } else {
                ADBUtils.stop();
            }
            updateStatus();
        }).run();
    }
}
