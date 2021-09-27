package com.graphingcalculator;

import android.animation.Animator;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.graphingcalculator.data.Entitys.expression;
import com.graphingcalculator.data.Entitys.variable;
import com.graphingcalculator.data.MainViewModel;

import java.util.List;
import java.util.regex.PatternSyntaxException;

public class EquationsFragment extends Fragment {

    private MainViewModel viewModel;
    private RecyclerView equationsRecyclerView;
    private ExpressionsListViewAdapter equationsRecyclerViewAdapter;
    private ImageButton showMathInputHelpToggleButton, addMathInputExpressionButton;
    private TextView mathInputSyntaxInfoText;
    private EditText mathInputText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.equations, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        equationsRecyclerView = (RecyclerView) view.findViewById(R.id.EquationsRecyclerView);
        equationsRecyclerViewAdapter = new ExpressionsListViewAdapter();
        equationsRecyclerViewAdapter.setExpressionItemEditChangesListener(new ExpressionOptionsChangesListener() {
            @Override
            public void changeEquationColor(expression exp, Color color) {
                viewModel.changeEquationColor(exp, color);
            }

            @Override
            public void deleteExpression(expression exp) {
                viewModel.deleteExpression(exp);
                equationsRecyclerViewAdapter.removeItem(exp);
            }

            @Override
            public void changeEquationVisibility(expression exp, boolean visible) {
                viewModel.changeEquationVisibility(exp, visible);
            }

            @Override
            public void changeExpression(expression expOld, String text) {
                expression newExp = viewModel.changeExpression(expOld, text);
                equationsRecyclerViewAdapter.changeItem(expOld, newExp);
            }

            @Override
            public void changeVariableRange(expression exp, double start, double end) {
                viewModel.changeVariableRange(exp, start, end);
            }

            @Override
            public void changeVariableValueProgress(expression exp, double progress) {
                viewModel.changeVariableValueProgress(exp, progress);
            }

            @Override
            public void changeVariableValueSave(expression exp) {
                viewModel.changeVariableValueSave();
            }

            @Override
            public void changeAnimateVariableStatus(expression exp, boolean isAnimated, double step, variable.ANIMATION_MODE mode) {
                viewModel.changeAnimateVariableStatus(exp, isAnimated, step, mode);
            }

            @Override
            public void addVariablesAnimationListener(Animator.AnimatorListener listener) {
                viewModel.addVariablesAnimationListener(listener);
            }

            @Override
            public void removeVariablesAnimationListener(Animator.AnimatorListener listener) {
                viewModel.removeVariablesAnimationListener(listener);
            }

            @Override
            public void moveExpressionUp(expression exp) {
                viewModel.moveExpressionUp(exp);
                equationsRecyclerViewAdapter.moveItemUp(exp);
            }

            @Override
            public void moveExpressionDown(expression exp) {
                viewModel.moveExpressionDown(exp);
                equationsRecyclerViewAdapter.moveItemDown(exp);
            }
        });
        equationsRecyclerView.setAdapter(equationsRecyclerViewAdapter);
        equationsRecyclerView.addItemDecoration(new DividerItemDecoration(equationsRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        mathInputSyntaxInfoText = view.findViewById(R.id.MathInputSyntaxInfoText);

        mathInputText = view.findViewById(R.id.InputMathExpressionText);
        mathInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mathInputText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mathInputText.setOnEditorActionListener((textView, actionId, event) -> {
            if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN)
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                addMathInputExpressionButton.performClick();
            }
            return true;
        });

        showMathInputHelpToggleButton = (ImageButton) view.findViewById(R.id.ShowMathInputHelpToggleButton);
        showMathInputHelpToggleButton.setOnClickListener(_view -> {
            if(mathInputSyntaxInfoText.getVisibility() == View.VISIBLE) {
                mathInputSyntaxInfoText.setVisibility(View.GONE);
            } else {
                mathInputSyntaxInfoText.setVisibility(View.VISIBLE);
            }
        });

        addMathInputExpressionButton = (ImageButton) view.findViewById(R.id.AddMathInputExpressionButton);
        addMathInputExpressionButton.setOnClickListener(_view -> {
            try {
                expression exp = viewModel.addExpression(mathInputText.getText().toString());
                equationsRecyclerViewAdapter.addItem(exp);
                scrollExpressionListToTop();
                mathInputText.setText("");
            } catch(PatternSyntaxException e) {
                mathInputText.setError(e.getDescription());
            } catch(IllegalArgumentException e) {
                mathInputText.setError(e.getMessage());
            }
        });

        viewModel.getExpressionsUpdates().observe(getActivity(), new Observer<List<expression>>() {
            @Override
            public void onChanged(List<expression> expressions) {
                equationsRecyclerViewAdapter.setItemsUpdates(expressions);
                viewModel.getExpressionsUpdates().removeObserver(this);
            }
        });
    }

    private void scrollExpressionListToTop() {
        if (equationsRecyclerView != null) {
            equationsRecyclerView.postDelayed(() -> {
                ((LinearLayoutManager)equationsRecyclerView.getLayoutManager()).scrollToPositionWithOffset(0, 0);
                equationsRecyclerView.scrollToPosition(0);
            }, 500);
        }
    }
}