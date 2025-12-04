package ir.at.UnfollowInsight;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    private Button btn_select_json;
    private Button btn_analyze;
    private Uri selectedZipUri;

    private final HashSet<String> followers = new HashSet<>();
    private final HashSet<String> following = new HashSet<>();
    private final ArrayList<String> unfollowers = new ArrayList<>();

    private final ActivityResultLauncher<String> pickZip = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedZipUri = uri;
                    btn_analyze.setEnabled(true);
                    btn_analyze.setAlpha(1.0f);
                    btn_analyze.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
                    Toast.makeText(this, "فایل ZIP انتخاب شد", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_select_json = findViewById(R.id.btn_select_json);
        btn_analyze = findViewById(R.id.btn_analyze);
        btn_analyze.setEnabled(false);
        btn_analyze.setAlpha(0.6f);

        btn_select_json.setOnClickListener(v -> pickZip.launch("application/zip"));
        btn_analyze.setOnClickListener(v -> analyzeZip());
    }

    private void analyzeZip() {
        if (selectedZipUri == null) {
            Toast.makeText(this, "اول فایل ZIP رو انتخاب کن", Toast.LENGTH_SHORT).show();
            return;
        }

        followers.clear();
        following.clear();
        unfollowers.clear();

        try (InputStream is = getContentResolver().openInputStream(selectedZipUri);
             ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName().toLowerCase();

                if (!fileName.endsWith(".json")) {
                    zis.closeEntry();
                    continue;
                }

                Log.d("ZIP", "در حال پردازش: " + entry.getName());

                String json = streamToString(zis);

                // فقط فایل followers_1.json, followers_2.json و ... (آرایه مستقیم)
                if (fileName.matches(".*followers_\\d+\\.json$")) {
                    parseFollowers(json);
                }

                // فقط فایل following.json (JSONObject با relationships_following)
                if (fileName.endsWith("following.json")) {
                    parseFollowing(json);
                }

                zis.closeEntry();
            }

            // محاسبه آنفالوها
            for (String user : following) {
                if (!followers.contains(user.toLowerCase())) {
                    unfollowers.add(user);
                }
            }

            Collections.sort(unfollowers, String.CASE_INSENSITIVE_ORDER);

            Log.d("نتیجه نهایی", "فالورها: " + followers.size());
            Log.d("نتیجه نهایی", "فالوئینگ‌ها: " + following.size());
            Log.d("نتیجه نهایی", "آنفالوها: " + unfollowers.size());

            if (followers.isEmpty() && following.isEmpty()) {
                Toast.makeText(this, "هیچ داده‌ای پیدا نشد!", Toast.LENGTH_LONG).show();
                return;
            }

            if (unfollowers.isEmpty()) {
                Toast.makeText(this, "عالیه! همه فالوت کردن", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(this, ResultActivity.class);
            intent.putStringArrayListExtra("items", unfollowers);
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "خطا در خواندن فایل", Toast.LENGTH_LONG).show();
            Log.e("ZIP_ERROR", "خطا", e);
        }
    }

    private String streamToString(ZipInputStream zis) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        return sb.toString();
    }

    // پارس فالورها (آرایه مستقیم)
    private void parseFollowers(String json) {
        try {
            JSONArray array = new JSONArray(json.trim());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                JSONArray list = obj.optJSONArray("string_list_data");
                if (list != null && list.length() > 0) {
                    String username = list.getJSONObject(0).optString("value", "").trim();
                    if (!username.isEmpty()) {
                        followers.add(username.toLowerCase());
                    }
                }
            }
            Log.d("PARSE", "فالورها اضافه شد: " + followers.size());
        } catch (Exception e) {
            Log.e("PARSE", "خطا در فالورها", e);
        }
    }

    // پارس فالوئینگ‌ها (JSONObject با relationships_following)
    private void parseFollowing(String json) {
        try {
            JSONObject root = new JSONObject(json.trim());
            JSONArray array = root.optJSONArray("relationships_following");
            if (array == null) return;

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String username = item.optString("title", "").trim();
                if (username.isEmpty()) {
                    JSONArray list = item.optJSONArray("string_list_data");
                    if (list != null && list.length() > 0) {
                        username = list.getJSONObject(0).optString("value", "").trim();
                    }
                }
                if (!username.isEmpty()) {
                    following.add(username.toLowerCase());
                }
            }
            Log.d("PARSE", "فالوئینگ‌ها اضافه شد: " + following.size());
        } catch (Exception e) {
            Log.e("PARSE", "خطا در فالوئینگ", e);
        }
    }
}