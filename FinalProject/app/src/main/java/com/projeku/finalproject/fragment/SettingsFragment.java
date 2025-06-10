package com.projeku.finalproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.projeku.finalproject.R;
import com.projeku.finalproject.databinding.FragmentSettingsBinding;
import com.projeku.finalproject.util.ThemeHelper;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private int currentTheme;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // PERBAIKAN: Mengatur toolbar agar konsisten dengan layout
        binding.toolbar.setTitle(R.string.settings);

        currentTheme = ThemeHelper.loadThemePreference(requireContext());
        updateThemeSummary(currentTheme);

        binding.layoutTheme.setOnClickListener(v -> showThemeDialog());
    }

    private void showThemeDialog() {
        String[] themes = { getString(R.string.light), getString(R.string.dark), getString(R.string.follow_system) };
        int checkedItem;

        switch(currentTheme) {
            case ThemeHelper.DARK_MODE:
                checkedItem = 1;
                break;
            case ThemeHelper.FOLLOW_SYSTEM:
                checkedItem = 2;
                break;
            default: // ThemeHelper.LIGHT_MODE
                checkedItem = 0;
                break;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.choose_theme)
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    int selectedTheme;
                    switch (which) {
                        case 0:
                            selectedTheme = ThemeHelper.LIGHT_MODE;
                            break;
                        case 1:
                            selectedTheme = ThemeHelper.DARK_MODE;
                            break;
                        default:
                            selectedTheme = ThemeHelper.FOLLOW_SYSTEM;
                            break;
                    }
                    if (currentTheme != selectedTheme) {
                        ThemeHelper.applyTheme(selectedTheme);
                        ThemeHelper.saveThemePreference(requireContext(), selectedTheme);
                        currentTheme = selectedTheme;
                        updateThemeSummary(selectedTheme);
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void updateThemeSummary(int theme) {
        switch(theme) {
            case ThemeHelper.DARK_MODE:
                binding.textViewThemeSummary.setText(R.string.dark);
                break;
            case ThemeHelper.FOLLOW_SYSTEM:
                binding.textViewThemeSummary.setText(R.string.follow_system);
                break;
            default: // ThemeHelper.LIGHT_MODE
                binding.textViewThemeSummary.setText(R.string.light);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
