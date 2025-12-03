package ir.at.UnfollowInsight;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnSelectJson, btnAnalyze;
    private Uri selectedZipUri = null;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedZipUri = result.getData().getData();
                    Toast.makeText(this, "فایل انتخاب شد", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "فایلی انتخاب نشد!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // اینجا درست شد

        btnSelectJson = findViewById(R.id.btnSelectJson);
        btnAnalyze = findViewById(R.id.btnAnalyze);

        btnSelectJson.setOnClickListener(v -> openFilePicker());

        btnAnalyze.setOnClickListener(v -> {
            if (selectedZipUri != null) {
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("zip_uri", selectedZipUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, "اول فایل ZIP را انتخاب کنید!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/zip", "application/octet-stream"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(intent);
    }
}
