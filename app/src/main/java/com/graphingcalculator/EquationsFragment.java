package com.graphingcalculator;

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

import com.graphingcalculator.data.Entitys.equation;
import com.graphingcalculator.data.Entitys.expression;
import com.graphingcalculator.data.Entitys.function;
import com.graphingcalculator.data.Entitys.variable;
import com.graphingcalculator.data.MainViewModel;
import com.graphingcalculator.graph.SystemOfEquations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class EquationsFragment extends Fragment {

    private MainViewModel viewModel;
    private RecyclerView equationsRecyclerView;
    private ExpressionsListViewAdapter equationsRecyclerViewAdapter;
    private ImageButton showMathInputHelpToggleButton, addMathInputExpressionButton;
    private TextView mathInputSyntaxInfoText;
    private EditText mathInputText;

    private SystemOfEquations equations;
    private List<expression> expressions;

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
                ((equation)exp).setColor(color);
                viewModel.updateExpression(exp);
            }

            @Override
            public void removeExpression(expression exp) {
                viewModel.removeExpression(exp);
            }

            @Override
            public void changeEquationVisibility(expression exp, boolean visible) {
                ((equation)exp).setVisible(visible);
                viewModel.updateExpression(exp);
            }

            @Override
            public void changeExpression(expression expOld, String text) {
                expression expNew = parseExpression(expOld, text);
                if(expNew == expOld) {
                    viewModel.updateExpression(expNew);
                } else {
                    viewModel.changeExpression(expOld, expNew);
                }
            }

            @Override
            public void changeVariableRange(expression exp, double start, double end) {
                ((variable)exp).setRange(start, end);
                viewModel.updateExpression(exp);
            }

            @Override
            public void changeVariableValueProgress(expression exp, double progress) {
                ((variable)exp).setValueProgress(progress);
            }

            @Override
            public void changeVariableValueSave(expression exp) {
                viewModel.updateExpression(exp);
            }

            @Override
            public void changeAnimateVariableStatus(expression exp, boolean isAnimated, double step, variable.ANIMATION_MODE mode) {
                ((variable)exp).setAnimation(isAnimated, step, mode);
                viewModel.updateExpression(exp);
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
                expression exp = parseExpression(null, mathInputText.getText().toString());
                viewModel.addExpression(exp);
                scrollExpressionListToTop();
                mathInputText.setText("");
            } catch(PatternSyntaxException e) {
                mathInputText.setError(e.getDescription());
            } catch(IllegalArgumentException e) {
                mathInputText.setError(e.getMessage());
            }
        });

        viewModel.getEquationsUpdates().observe(getActivity(), new Observer<SystemOfEquations>() {
            @Override
            public void onChanged(SystemOfEquations sys) {
                equations = sys;
            }
        });

        viewModel.getExpressionsUpdates().observe(getActivity(), new Observer<List<expression>>() {
            @Override
            public void onChanged(List<expression> sys) {
                expressions = sys;
                equationsRecyclerViewAdapter.setItemsUpdates(sys);
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

    private int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    public boolean expressionsHaveTheSameType(expression e1, expression e2) {
        if(e1 instanceof equation && e2 instanceof equation) {
            return true;
        }
        if(e1 instanceof variable && e2 instanceof variable) {
            return true;
        }
        if(e1 instanceof function && e2 instanceof function) {
            return true;
        }
        return false;
    }

    public expression parseExpression(expression last, String pattern) {

        if(pattern.trim().equals("")) {
            throw new PatternSyntaxException("expression is empty", pattern , 0);
        }

        // equation
        Pattern equationPattern = Pattern.compile("^[ ]*y[ ]*=[ ]*(.+)[ ]*$");
        Matcher equationMatcher = equationPattern.matcher(pattern);
        if(equationMatcher.matches()) {
            String text = equationMatcher.group(1);
            if(last != null && last instanceof equation) {
                ((equation) last).setBody(text);
                return last;
            } else {
                Color color = Color.valueOf(getRandomColor());
                return new equation(text, color);
            }
        }

        // variable
        Pattern variablePattern = Pattern.compile("^[ ]*([a-zA-Z][a-zA-Z0-9]*)[ ]*=[ ]*([-]?\\d+(\\.\\d+)?)[ ]*$");
        Matcher variableMatcher = variablePattern.matcher(pattern);
        if(variableMatcher.matches()) {
            String name = variableMatcher.group(1);
            double value = Double.parseDouble(variableMatcher.group(2));
            if(last != null && last instanceof variable) {
                ((variable) last).setName(name);
                ((variable) last).setValue(value);
                return last;
            } else {
                double startValue = value - 10.0,
                        endValue = value + 10.0;
                if (equations.hasVariable(name)) {
                    throw new IllegalArgumentException("variable name already exist");
                }
                return new variable(name, value, startValue, endValue);
            }
        }

        // function
        Pattern functionPattern = Pattern.compile("^[ ]*([a-zA-Z][a-zA-Z0-9]*)\\(((?:[ ]*[a-zA-Z][a-zA-Z0-9]*[ ]*,)*(?:[ ]*[a-zA-Z][a-zA-Z0-9]*[ ]*)?)\\)[ ]*=[ ]*(.+)[ ]*$");
        Matcher functionMatcher = functionPattern.matcher(pattern);
        if(functionMatcher.matches()) {
            String name = functionMatcher.group(1);
            String argumentsString = functionMatcher.group(2);
            List<String> arguments = new ArrayList<String>();
            for(String arg : argumentsString.split(",")) {
                arg = arg.trim();
                if(!arg.isEmpty()) {
                    arguments.add(arg);
                }
            }
            String body = functionMatcher.group(3);
            if(last != null && last instanceof function) {
                ((function) last).setBody(body);
                ((function) last).setName(name);
                ((function) last).setArguments(arguments);
                return last;
            } else {
                if(equations.hasFunction(name)) {
                    throw new IllegalArgumentException("function name already exist");
                }
                return new function(name, arguments, body);
            }
        }

        throw new PatternSyntaxException("syntax error", pattern , 0);
    }
}