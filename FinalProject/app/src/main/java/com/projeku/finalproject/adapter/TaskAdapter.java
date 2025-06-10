package com.projeku.finalproject.adapter;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.projeku.finalproject.R;
import com.projeku.finalproject.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = tasks.get(position);
        holder.textViewTitle.setText(currentTask.getTitle());
        holder.textViewCategory.setText(currentTask.getCategory());

        // Set the description text
        if (currentTask.getDescription() != null && !currentTask.getDescription().isEmpty()) {
            holder.textViewDescription.setVisibility(View.VISIBLE);
            holder.textViewDescription.setText(currentTask.getDescription());
        } else {
            holder.textViewDescription.setVisibility(View.GONE);
        }

        // Format the due date
        if (currentTask.getDueDate() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.textViewDueDate.setText(sdf.format(new Date(currentTask.getDueDate())));
            holder.textViewDueDate.setVisibility(View.VISIBLE);
        } else {
            holder.textViewDueDate.setVisibility(View.GONE);
        }

        // Set priority indicator
        switch (currentTask.getPriority()) {
            case 3: // High
                holder.priorityIndicator.setBackgroundColor(Color.parseColor("#FF4757"));
                break;
            case 2: // Medium
                holder.priorityIndicator.setBackgroundColor(Color.parseColor("#FFA502"));
                break;
            default: // Low
                holder.priorityIndicator.setBackgroundColor(Color.parseColor("#2ED573"));
                break;
        }

        // Set completed status
        holder.checkBoxCompleted.setChecked(currentTask.isCompleted());
        if (currentTask.isCompleted()) {
            holder.textViewTitle.setPaintFlags(holder.textViewTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            if (holder.textViewDescription.getVisibility() == View.VISIBLE) {
                holder.textViewDescription.setPaintFlags(holder.textViewDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        } else {
            holder.textViewTitle.setPaintFlags(holder.textViewTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.textViewDescription.setPaintFlags(holder.textViewDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDescription;
        private TextView textViewCategory;
        private TextView textViewDueDate;
        private CheckBox checkBoxCompleted;
        private View priorityIndicator;
        public CardView cardView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewCategory = itemView.findViewById(R.id.text_view_category);
            textViewDueDate = itemView.findViewById(R.id.text_view_due_date);
            checkBoxCompleted = itemView.findViewById(R.id.checkbox_completed);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            cardView = itemView.findViewById(R.id.card_view_task);

            // Listener for item click (for editing)
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(tasks.get(position));
                }
            });

            // Listener for checkbox (to mark as completed)
            checkBoxCompleted.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onCheckboxClick(tasks.get(position), checkBoxCompleted.isChecked());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Task task);
        void onCheckboxClick(Task task, boolean isChecked);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}