package com.projeku.finalproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.projeku.finalproject.databinding.FragmentCompletedTasksBinding;
import com.projeku.finalproject.model.Task;
import com.projeku.finalproject.viewmodel.TaskViewModel;

public class CompletedTasksFragment extends Fragment {

    private FragmentCompletedTasksBinding binding;
    private TaskViewModel taskViewModel;
    private TaskAdapter taskAdapter;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCompletedTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // PERBAIKAN: Mengatur toolbar agar konsisten dengan layout
        binding.toolbar.setTitle(R.string.completed_tasks);

        setupRecyclerView();
        observeCompletedTasks();
    }

    private void setupRecyclerView() {
        binding.recyclerViewCompletedTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter();
        binding.recyclerViewCompletedTasks.setAdapter(taskAdapter);

        taskAdapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("task_to_edit", task);
                navController.navigate(R.id.action_completedTasksFragment_to_addTaskFragment, bundle);
            }

            @Override
            public void onCheckboxClick(Task task, boolean isChecked) {
                task.setCompleted(false);
                taskViewModel.update(task);
                Toast.makeText(getContext(), "Task moved back to Ongoing", Toast.LENGTH_SHORT).show();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Task task = taskAdapter.getTaskAt(viewHolder.getAdapterPosition());
                taskViewModel.delete(task);
                Snackbar.make(binding.getRoot(), "Task permanently deleted", Snackbar.LENGTH_LONG).show();
            }
        }).attachToRecyclerView(binding.recyclerViewCompletedTasks);
    }

    private void observeCompletedTasks() {
        taskViewModel.getCompletedTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.setTasks(tasks);

            // Update the task counter
            TextView taskCount = binding.textViewTaskCount;
            taskCount.setText(String.valueOf(tasks.size()));

            if (tasks == null || tasks.isEmpty()) {
                binding.textViewEmpty.setVisibility(View.VISIBLE);
                binding.recyclerViewCompletedTasks.setVisibility(View.GONE);
            } else {
                binding.textViewEmpty.setVisibility(View.GONE);
                binding.recyclerViewCompletedTasks.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
