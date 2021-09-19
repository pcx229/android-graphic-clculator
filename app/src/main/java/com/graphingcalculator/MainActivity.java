package com.graphingcalculator;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager supportFragmentManager = getSupportFragmentManager();

        NavHostFragment navHostFragment =
                (NavHostFragment) supportFragmentManager.findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        ExtendedFloatingActionButton navFab = findViewById(R.id.navFab);
        navFab.setOnClickListener(view -> {
            switch (navController.getCurrentDestination().getId()) {
                case R.id.GraphFragment:
                    navController.navigate(R.id.action_GraphFragment_to_EquationsFragment);
                    navFab.setIcon(getDrawable(R.drawable.ic_baseline_arrow_back_24));
                    navFab.setText("Back To Graph");
                    break;
                case R.id.EquationsFragment:
                    navController.navigate(R.id.action_EquationsFragment_to_GraphFragment);
                    navFab.setIcon(getDrawable(R.drawable.ic_baseline_functions_24));
                    navFab.setText("Functions");
                    break;
            }
        });
    }
}