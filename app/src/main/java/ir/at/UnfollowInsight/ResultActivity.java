package ir.at.UnfollowInsight;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textViewTitle;
    private TextView textViewCount;
    private ResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewCount = findViewById(R.id.textViewCount);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<String> unfollowers = getIntent().getStringArrayListExtra("items");

        if (unfollowers == null || unfollowers.isEmpty()) {
            textViewTitle.setText("هیچ آنفالویی پیدا نشد");
            textViewCount.setText("عالی! همه فالوت کردن");
        } else {
            textViewTitle.setText("لیست آنفالوها");
            textViewCount.setText(unfollowers.size() + " نفر شما را فالو نکرده‌اند");

            adapter = new ResultAdapter(this, unfollowers);
            recyclerView.setAdapter(adapter);
        }
    }
}