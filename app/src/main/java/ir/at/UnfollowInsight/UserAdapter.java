package ir.at.UnfollowInsight;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final List<String> users;

    public UserAdapter(List<String> users) {
        this.users = users;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItem;
        public ViewHolder(@NonNull View view) {
            super(view);
            textViewItem = view.findViewById(R.id.textViewItem);
        }
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textViewItem.setText(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
