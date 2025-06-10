package com.projeku.finalproject.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.projeku.finalproject.R;
import com.projeku.finalproject.databinding.FragmentAddTaskBinding;
import com.projeku.finalproject.model.Task;
import com.projeku.finalproject.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddTaskFragment extends Fragment {

    private FragmentAddTaskBinding binding;
    private TaskViewModel taskViewModel;
    private NavController navController;
    private Task taskToEdit = null;
    private long dueDate = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        setupChipGroupListener();

        try {
            if (getArguments() != null && getArguments().containsKey("task_to_edit")) {
                taskToEdit = (Task) getArguments().getSerializable("task_to_edit");
            }
        } catch (Exception e) {
            taskToEdit = null;
            Log.e("AddTaskFragment", "Failed to get task argument, likely in Add mode.", e);
        }

        setupToolbar();
        if (taskToEdit != null) {
            configureUiForEditMode();
        } else {
            configureUiForAddMode();
        }

        binding.buttonDueDate.setOnClickListener(v -> showDatePicker());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        checkForUnsavedChangesAndExit();
                    }
                });
    }

    private void setupToolbar() {
        binding.toolbarAddTask.setNavigationOnClickListener(v -> checkForUnsavedChangesAndExit());
        binding.toolbarAddTask.inflateMenu(R.menu.add_task_menu);
        binding.toolbarAddTask.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_save_task) {
                saveTask();
                return true;
            } else if (itemId == R.id.action_delete_task) {
                showDeleteConfirmationDialog();
                return true;
            }
            return false;
        });
    }

    private void checkForUnsavedChangesAndExit() {
        if (hasUnsavedChanges()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.unsaved_changes)
                    .setMessage(R.string.unsaved_changes_message)
                    .setPositiveButton(R.string.discard, (dialog, which) -> navController.navigateUp())
                    .setNegativeButton(R.string.keep_editing, null)
                    .show();
        } else {
            navController.navigateUp();
        }
    }

    private boolean hasUnsavedChanges() {
        String title = binding.editTextTitle.getText().toString().trim();
        String description = binding.editTextDescription.getText().toString().trim();

        String category = getSelectedCategory();

        int priority = getSelectedPriority();

        if (taskToEdit == null) {
            return !title.isEmpty() || !description.isEmpty() || dueDate > 0 ||
                    !category.equals(getString(R.string.other));
        } else {
            return !title.equals(taskToEdit.getTitle()) ||
                    !description.equals(taskToEdit.getDescription()) ||
                    dueDate != taskToEdit.getDueDate() ||
                    priority != taskToEdit.getPriority() ||
                    !category.equals(taskToEdit.getCategory());
        }
    }

    private String getSelectedCategory() {
        int selectedChipId = binding.chipGroupCategory.getCheckedChipId();
        if (selectedChipId != View.NO_ID) {
            Chip selectedChip = binding.chipGroupCategory.findViewById(selectedChipId);
            String chipText = selectedChip.getText().toString();

            if (chipText.equalsIgnoreCase(getString(R.string.other))) {
                String customCategory = binding.editTextCustomCategory.getText().toString().trim();
                if (!TextUtils.isEmpty(customCategory)) {
                    return customCategory;
                }
            }
            return chipText;
        }
        return getString(R.string.other);
    }

    private int getSelectedPriority() {
        int priority = 1;
        int selectedPriorityId = binding.radioGroupPriority.getCheckedRadioButtonId();
        if (selectedPriorityId == R.id.radio_high) priority = 3;
        else if (selectedPriorityId == R.id.radio_medium) priority = 2;
        return priority;
    }

    private void setupChipGroupListener() {
        binding.chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                Chip selectedChip = binding.chipGroupCategory.findViewById(checkedId);
                if (selectedChip != null && selectedChip.getText().toString().equalsIgnoreCase(getString(R.string.other))) {
                    binding.textInputLayoutCustomCategory.setVisibility(View.VISIBLE);
                } else {
                    binding.textInputLayoutCustomCategory.setVisibility(View.GONE);
                }
            }
        });
    }


    private void configureUiForAddMode() {
        binding.toolbarAddTask.setTitle(getString(R.string.add_new_task));
    }

    private void configureUiForEditMode() {
        binding.toolbarAddTask.setTitle(getString(R.string.edit_task));
        binding.toolbarAddTask.getMenu().findItem(R.id.action_delete_task).setVisible(true);

        binding.editTextTitle.setText(taskToEdit.getTitle());
        binding.editTextDescription.setText(taskToEdit.getDescription());

        boolean categoryFound = false;
        for (int i = 0; i < binding.chipGroupCategory.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupCategory.getChildAt(i);
            String chipText = chip.getText().toString();
            if (chipText.equalsIgnoreCase(taskToEdit.getCategory())) {
                chip.setChecked(true);
                categoryFound = true;
                break;
            }
        }

        if (!categoryFound) {
            Chip otherChip = binding.chipOther;
            otherChip.setChecked(true);
            binding.editTextCustomCategory.setText(taskToEdit.getCategory());
        }

        switch (taskToEdit.getPriority()) {
            case 3: binding.radioHigh.setChecked(true); break;
            case 2: binding.radioMedium.setChecked(true); break;
            default: binding.radioLow.setChecked(true); break;
        }

        if (taskToEdit.getDueDate() > 0) {
            dueDate = taskToEdit.getDueDate();
            updateDueDateText();
        }
    }

    private void saveTask() {
        String title = binding.editTextTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            binding.textInputLayoutTitle.setError(getString(R.string.title_cannot_be_empty));
            return;
        }
        binding.textInputLayoutTitle.setError(null);

        String description = binding.editTextDescription.getText().toString().trim();

        String category = "";
        int selectedChipId = binding.chipGroupCategory.getCheckedChipId();
        if (selectedChipId != View.NO_ID) {
            Chip selectedChip = binding.chipGroupCategory.findViewById(selectedChipId);
            String chipText = selectedChip.getText().toString();

            if (chipText.equalsIgnoreCase(getString(R.string.other))) {
                String customCategory = binding.editTextCustomCategory.getText().toString().trim();
                if (!TextUtils.isEmpty(customCategory)) {
                    category = customCategory;
                } else {
                    binding.textInputLayoutCustomCategory.setError(getString(R.string.please_enter_category));
                    return;
                }
            } else {
                category = chipText;
            }
        } else {
            category = getString(R.string.other);
        }

        int priority = 1;
        int selectedPriorityId = binding.radioGroupPriority.getCheckedRadioButtonId();
        if (selectedPriorityId == R.id.radio_high) priority = 3;
        else if (selectedPriorityId == R.id.radio_medium) priority = 2;

        if (taskToEdit == null) {
            Task newTask = new Task(title, description, dueDate, priority, category, false);
            taskViewModel.insert(newTask);
            Toast.makeText(getContext(), R.string.task_saved, Toast.LENGTH_SHORT).show();
        } else {
            taskToEdit.setTitle(title);
            taskToEdit.setDescription(description);
            taskToEdit.setDueDate(dueDate);
            taskToEdit.setPriority(priority);
            taskToEdit.setCategory(category);
            taskViewModel.update(taskToEdit);
            Toast.makeText(getContext(), R.string.task_updated, Toast.LENGTH_SHORT).show();
        }

        navController.navigateUp();
    }

    private void showDeleteConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to permanently delete this task?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteTask())
                .show();
    }

    private void deleteTask() {
        if (taskToEdit != null) {
            taskViewModel.delete(taskToEdit);
            Toast.makeText(getContext(), R.string.task_deleted, Toast.LENGTH_SHORT).show();
            navController.navigateUp();
        }
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(dueDate > 0 ? dueDate : MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            dueDate = selection;
            updateDueDateText();
        });
        datePicker.show(getParentFragmentManager(), datePicker.toString());
    }

    private void updateDueDateText() {
        if (dueDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            binding.textViewSelectedDate.setText(String.format("Selected: %s", sdf.format(new Date(dueDate))));
            binding.textViewSelectedDate.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}