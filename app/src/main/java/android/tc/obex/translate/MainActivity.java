package android.tc.obex.translate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import javax.obex.utils.BluetoothOPPHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btObex = findViewById(R.id.bt_obex);

        String filePath = "";
        btObex.setOnClickListener(v -> {
            new BluetoothOPPHelper().pushOppFile(filePath);
        });
    }
}