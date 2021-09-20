package com.graphingcalculator;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.init();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager supportFragmentManager = getSupportFragmentManager();

        NavHostFragment navHostFragment =
                (NavHostFragment) supportFragmentManager.findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        FloatingActionButton functionsNavButton = findViewById(R.id.FunctionsFloatingActionButton);
        FloatingActionButton graphNavButton = findViewById(R.id.GraphFloatingActionButton);
        FloatingActionButton settingsNavButton = findViewById(R.id.SettingsFloatingActionButton);
        FloatingActionButton resetNavButton = findViewById(R.id.ResetFloatingActionButton);
        functionsNavButton.setOnClickListener(view -> {
            navController.navigate(R.id.action_GraphFragment_to_EquationsFragment);
            functionsNavButton.setVisibility(View.GONE);
            graphNavButton.setVisibility(View.VISIBLE);
            resetNavButton.animate()
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(300)
                    .setListener(null);
            settingsNavButton.animate()
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(300)
                    .setListener(null);
        });
        graphNavButton.setOnClickListener(view -> {
            switch (navController.getCurrentDestination().getId()) {
                case R.id.EquationsFragment:
                    navController.navigate(R.id.action_EquationsFragment_to_GraphFragment);
                    break;
                case R.id.SettingsFragment:
                    navController.navigate(R.id.action_SettingsFragment_to_GraphFragment);
                    break;
            }
            functionsNavButton.setVisibility(View.VISIBLE);
            graphNavButton.setVisibility(View.GONE);
            resetNavButton.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(300)
                    .setListener(null);
            settingsNavButton.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(300)
                    .setListener(null);
        });
        resetNavButton.setOnClickListener(view -> {
            viewModel.resetRange();
        });
        settingsNavButton.setOnClickListener(view -> {
            navController.navigate(R.id.action_GraphFragment_to_SettingsFragment);
            functionsNavButton.setVisibility(View.GONE);
            graphNavButton.setVisibility(View.VISIBLE);
            resetNavButton.animate()
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(300)
                    .setListener(null);
            settingsNavButton.animate()
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(300)
                    .setListener(null);
        });
    }

    @Override
    protected void onStop() {
        viewModel.saveSettings();

        super.onStop();
    }
}