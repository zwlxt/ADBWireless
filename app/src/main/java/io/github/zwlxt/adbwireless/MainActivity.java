package io.github.zwlxt.adbwireless;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    @BindView(R.id.button_start_stop)
    Button buttonAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        refreshStatus();
    }

    private void setListeningStatus(boolean status) {
        textViewListeningStatus.setText(status ? "Yes" : "No");
        textViewListeningStatus.setTextColor(status ? Color.GREEN : Color.RED);
    }

    private void setActiveStatus(boolean status) {
        textViewActiveStatus.setText(status ? "Yes" : "No");
        textViewActiveStatus.setTextColor(status ? Color.GREEN : Color.RED);
    }

    private void refreshStatus() {
        new Thread(() -> {
            int status = ADBUtils.status();
            runOnUiThread(() -> {
                switch (status) {
                    case 0:
                        setListeningStatus(false);
                        setActiveStatus(false);
                        break;
                    case 1:
                        setListeningStatus(true);
                        setActiveStatus(false);
                        break;
                    case 3:
                        setListeningStatus(true);
                        setActiveStatus(true);
                        break;
                    default:
                }
            });
        }).run();
    }
}
