package com.graphingcalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.graphingcalculator.graph.MathGraph;
import com.graphingcalculator.graph.Range;

public class GraphFragment extends Fragment {

    private MainViewModel viewModel;
    private MathGraph graph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.graph, container, false);
    }

    private void updateGraphRange(Range range) {
        if (Double.isNaN(range.startY) && Double.isNaN(range.endY)) {
            graph.setRangeByStartEnd((float) range.startX, (float) range.endX, MathGraph.FIT_RANGE.BOTH);
        } else {
            graph.setRangeByStartEnd((float) range.startX, (float) range.endX, (float) range.startY, (float) range.endY);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        graph = (MathGraph) view.findViewById(R.id.graph);
        graph.setOnRangeChangesListener(range -> {
            viewModel.setRange(range);
        });
        graph.setOnGraphSizeChangesListener((width, height) -> {
            viewModel.setGraphSize(width, height);
        });
        viewModel.getSettingsUpdates().observe(getActivity(), new Observer<Settings>() {
            @Override
            public void onChanged(Settings settings) {
                updateGraphRange(settings.getRange());
                graph.setShowingAxis(settings.isShowAxis());
                graph.setShowingGrid(settings.isShowGrid());

                viewModel.getSettingsUpdates().removeObserver(this);
            }
        });
        viewModel.setOnResetRangeListener(range -> {
            updateGraphRange(range);
        });
        viewModel.getEquationsUpdates().observe(getActivity(), equations -> {
            graph.setEquations(equations);
        });
    }
}