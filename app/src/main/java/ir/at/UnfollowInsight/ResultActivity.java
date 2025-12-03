package ir.at.UnfollowInsight;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

public class ResultActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private final List<String> unfollowersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        processJsonFiles();

        adapter = new UserAdapter(unfollowersList);
        recyclerView.setAdapter(adapter);
    }

    private void processJsonFiles() {
        List<String> followers = new ArrayList<>();
        List<String> following = new ArrayList<>();

        try {
            File folder = new File(getFilesDir(), "unzipped/connections");
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
            if (files == null) return;

            for (File file : files) {
                String json = readFile(file);
                JSONObject obj = new JSONObject(json);

                if (file.getName().contains("followers")) {
                    JSONArray arr = obj.getJSONArray("relationships_followers");
                    extractUsernames(arr, followers);
                } else if (file.getName().contains("following")) {
                    JSONArray arr = obj.getJSONArray("relationships_following");
                    extractUsernames(arr, following);
                }
            }

            for (String user : following) {
                if (!followers.contains(user)) {
                    unfollowersList.add(user);
                }
            }

        } catch (Exception e) {
            Log.e("JSON_ERROR", "Processing JSON failed: ", e);
        }
    }

    private void extractUsernames(JSONArray array, List<String> list) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONArray stringList = array.getJSONObject(i)
                        .getJSONArray("string_list_data");
                String username = stringList.getJSONObject(0).getString("value");
                list.add(username);
            } catch (Exception ignored) {
            }
        }
    }

    private String readFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}
