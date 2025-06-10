package com.projeku.finalproject.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.projeku.finalproject.R;
import com.projeku.finalproject.adapter.TaskAdapter;
import com.projeku.finalproject.databinding.FragmentHomeBinding;
import com.projeku.finalproject.model.Task;
import com.projeku.finalproject.viewmodel.QuoteViewModel;
import com.projeku.finalproject.viewmodel.TaskViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TaskViewModel taskViewModel;
    private QuoteViewModel quoteViewModel;
    private TaskAdapter taskAdapter;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Initialize ViewModels
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        quoteViewModel = new ViewModelProvider(this).get(QuoteViewModel.class);

        setupRecyclerView();
        observeTasks();
        observeQuote();

        binding.fabAddTask.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_addTaskFragment));
        binding.buttonRefreshQuote.setOnClickListener(v -> fetchNewQuote());
    }

    private void setupRecyclerView() {
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter();
        binding.recyclerViewTasks.setAdapter(taskAdapter);

        // Handle item clicks (edit and checkbox)
        taskAdapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                // Navigate to AddTaskFragment with the task to edit
                Bundle bundle = new Bundle();
                bundle.putSerializable("task_to_edit", task);
                navController.navigate(R.id.action_homeFragment_to_addTaskFragment, bundle);
            }

            @Override
            public void onCheckboxClick(Task task, boolean isChecked) {
                task.setCompleted(isChecked);
                taskViewModel.update(task);
                if (isChecked) {
                    showCompletionDialog();
                } else {
                    Toast.makeText(getContext(), R.string.task_marked_as_incomplete, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle swipe gestures
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task task = taskAdapter.getTaskAt(position);
                taskViewModel.delete(task);

                // Show Snackbar with Undo option
                Snackbar.make(binding.getRoot(), R.string.task_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> taskViewModel.insert(task))
                        .show();
            }
        }).attachToRecyclerView(binding.recyclerViewTasks);
    }

    private void observeTasks() {
        taskViewModel.getUncompletedTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.setTasks(tasks);

            // Update the task counter
            TextView taskCounter = binding.taskCounter;
            taskCounter.setText(String.valueOf(tasks.size()));

            // Show/hide empty state if needed
            if (tasks.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewTasks.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.recyclerViewTasks.setVisibility(View.VISIBLE);
            }
        });
    }

    private void observeQuote() {
        showQuoteLoading(true);
        quoteViewModel.getQuoteOfTheDay().observe(getViewLifecycleOwner(), quote -> {
            showQuoteLoading(false);
            if (quote != null) {
                binding.textViewQuote.setText(String.format("\"%s\"", quote.getQuoteText()));
                binding.textViewAuthor.setText(String.format("- %s", quote.getAuthor()));
                binding.buttonRefreshQuote.setVisibility(View.GONE);
            } else {
                binding.buttonRefreshQuote.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchNewQuote() {
        showQuoteLoading(true);
        quoteViewModel.getRandomQuote().observe(getViewLifecycleOwner(), quote -> {
            showQuoteLoading(false);
            if (quote != null) {
                binding.textViewQuote.setText(String.format("\"%s\"", quote.getQuoteText()));
                binding.textViewAuthor.setText(String.format("- %s", quote.getAuthor()));
                binding.buttonRefreshQuote.setVisibility(View.GONE);
            } else {
                binding.buttonRefreshQuote.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Failed to load quote", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuoteLoading(boolean isLoading) {
        binding.progressBarQuote.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.textViewQuote.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        binding.textViewAuthor.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    private void showCompletionDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_quote);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView quoteText = dialog.findViewById(R.id.text_view_random_quote);
        TextView authorText = dialog.findViewById(R.id.text_view_random_author);
        ProgressBar progressBar = dialog.findViewById(R.id.progress_bar_random_quote);

        progressBar.setVisibility(View.VISIBLE);
        quoteViewModel.getRandomQuote().observe(getViewLifecycleOwner(), quote -> {
            progressBar.setVisibility(View.GONE);
            if (quote != null) {
                quoteText.setText(String.format("\"%s\"", quote.getQuoteText()));
                authorText.setText(String.format("- %s", quote.getAuthor()));
            } else {
                quoteText.setText(R.string.motivation_for_you);
                authorText.setText("");
            }
        });
        dialog.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
