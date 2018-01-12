package io.github.zwlxt.adbwireless;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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
    private final String KEY_STATE = "ADB_STATE";
    private final String NOTIF_CHAN = "Adb status";
    private final int NOTIF_ID = 1;
    private final int NOTIF_REQUEST_CODE = 1;
    private ADBState savedState;

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

        if (savedInstanceState != null) {
            ADBState adbState = (ADBState) savedInstanceState.getSerializable(KEY_STATE);
            savedState = adbState;
            updateView(adbState);
        } else
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (savedState != null)
            outState.putSerializable(KEY_STATE, savedState);
        super.onSaveInstanceState(outState);
    }

    @OnClick(R.id.button_refresh)
    public void updateStatus() {
        new Thread(() -> {
            ADBState state = ADBUtils.getState(MainActivity.this);
            savedState = state;
            setNotification(state);
            runOnUiThread(() -> updateView(state));
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

    private void updateView(ADBState model) {
        if (model.getPort() != 0)
            editPort.setText(String.valueOf(model.getPort()));
        textViewAddress.setText(model.getAddress());
        if (!model.isConnectedToWifi())
            setInstructionText();
        switch (model.getStatus()) {
            case 0:
                setListeningStatus(false);
                setActiveStatus(false);
                setInstructionText();
                break;
            case 1:
                setListeningStatus(true);
                setActiveStatus(false);
                setInstructionText(model.getAddress(), getDefinedPort());
                break;
            case 3:
                setListeningStatus(true);
                setActiveStatus(true);
                setInstructionText(model.getAddress(), getDefinedPort());
                break;
            default:
        }
    }

    private void setNotification(ADBState state) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null)
            return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIF_CHAN);
        switch (state.getStatus()) {
            case 0:

                break;
            case 1:
            case 3:
                Intent intent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        NOTIF_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_developer_mode_black_24dp)
                        .setContentTitle(getString(R.string.adb_is_running))
                        .setContentText(String.format(Locale.getDefault(),
                                "at %s", state.getAddress()))
                        .setSmallIcon(R.drawable.ic_developer_mode_black_24dp)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(NOTIF_CHAN, NOTIF_CHAN, NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }
                Notification notification = builder.build();
                notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                notificationManager.notify(NOTIF_ID, notification);
                break;
        }

    }
}
