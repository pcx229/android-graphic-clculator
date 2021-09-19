package com.graphingcalculator;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.graphingcalculator.graph.Equation;
import com.graphingcalculator.graph.MathGraph;
import com.graphingcalculator.graph.SystemOfEquations;

public class GraphFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.graph, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MathGraph graph = (MathGraph) view.findViewById(R.id.graph);
        graph.setRangeByStartEnd(-10, 10, MathGraph.FIT_RANGE.BOTH);
        // test equations
        SystemOfEquations eqs = new SystemOfEquations();
        eqs.addEquation(new Equation("y==x^2", Color.valueOf(Color.RED)));
        eqs.addEquation(new Equation("y==sinr(x)*2.4", Color.valueOf(Color.RED)));
        graph.setEquations(eqs);
    }
}