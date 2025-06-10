package com.projeku.finalproject.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

        // Langkah 1: Coba ambil data tugas yang akan diedit dari argumen navigasi
        try {
            if (getArguments() != null && getArguments().containsKey("task_to_edit")) {
                taskToEdit = (Task) getArguments().getSerializable("task_to_edit");
            }
        } catch (Exception e) {
            taskToEdit = null;
            Log.e("AddTaskFragment", "Gagal mendapatkan argumen tugas, mungkin mode Tambah.", e);
        }

        // Langkah 2: Siapkan UI berdasarkan mode (Tambah atau Edit)
        setupToolbar();
        if (taskToEdit != null) {
            // Jika taskToEdit tidak null, kita dalam mode EDIT
            configureUiForEditMode();
        } else {
            // Jika taskToEdit null, kita dalam mode TAMBAH
            configureUiForAddMode();
        }

        // Langkah 3: Siapkan listener untuk tombol tanggal
        binding.buttonDueDate.setOnClickListener(v -> showDatePicker());
    }

    private void setupToolbar() {
        binding.toolbarAddTask.setNavigationOnClickListener(v -> navController.navigateUp());
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

    private void configureUiForAddMode() {
        binding.toolbarAddTask.setTitle(getString(R.string.add_new_task));
        // Tombol hapus sudah disembunyikan dari file menu XML, jadi tidak perlu melakukan apa-apa.
    }

    private void configureUiForEditMode() {
        binding.toolbarAddTask.setTitle(getString(R.string.edit_task));
        // Tampilkan tombol hapus karena ini adalah mode edit
        binding.toolbarAddTask.getMenu().findItem(R.id.action_delete_task).setVisible(true);

        // Isi semua field dengan data dari tugas yang ada
        binding.editTextTitle.setText(taskToEdit.getTitle());
        binding.editTextDescription.setText(taskToEdit.getDescription());

        for (int i = 0; i < binding.chipGroupCategory.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupCategory.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(taskToEdit.getCategory())) {
                chip.setChecked(true);
                break;
            }
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
            category = selectedChip.getText().toString();
        } else {
            category = getString(R.string.other);
        }

        int priority = 1;
        int selectedPriorityId = binding.radioGroupPriority.getCheckedRadioButtonId();
        if (selectedPriorityId == R.id.radio_high) priority = 3;
        else if (selectedPriorityId == R.id.radio_medium) priority = 2;

        if (taskToEdit == null) {
            // Mode Tambah: Buat tugas baru
            Task newTask = new Task(title, description, dueDate, priority, category, false);
            taskViewModel.insert(newTask);
            Toast.makeText(getContext(), R.string.task_saved, Toast.LENGTH_SHORT).show();
        } else {
            // Mode Edit: Perbarui tugas yang ada
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
                .setTitle("Hapus Tugas")
                .setMessage("Apakah Anda yakin ingin menghapus tugas ini secara permanen?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (dialog, which) -> deleteTask())
                .show();
    }

    private void deleteTask() {
        if (taskToEdit != null) {
            taskViewModel.delete(taskToEdit);
            Toast.makeText(getContext(), "Tugas telah dihapus", Toast.LENGTH_SHORT).show();
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