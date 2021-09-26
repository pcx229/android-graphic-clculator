package com.graphingcalculator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.graphingcalculator.data.Entitys.equation;
import com.graphingcalculator.data.Entitys.expression;
import com.graphingcalculator.data.Entitys.variable;

import java.util.regex.PatternSyntaxException;

public class ExpressionViewHolder extends RecyclerView.ViewHolder {
    private View mView;

    private expression exp;

    private ExpressionOptionsChangesListener mChangesListener;

    private EditText editMathExpressionText;
    private boolean isEquationVisible;
    private Button equationVisibleColorButton;
    private ImageButton saveChangesMathExpressionButton;
    private ImageButton removeMathExpressionButton;
    private View variableRangeOptionsView;
    private EditText startVariableRangeEditText, endVariableRangeEditText;
    private SeekBar changeVariableValueInRangeSeekBar;
    private boolean isVariableAnimated;
    private ImageButton animateVariableValuesButton;
    private Animator.AnimatorListener variableAnimationListener;

    private void setEquationVisible(boolean visible) {
        isEquationVisible = visible;
        int resId;
        if(visible) {
            resId = R.drawable.equation_color_pick_active;
        } else {
            resId = R.drawable.equation_color_pick_not_active;
        }
        equationVisibleColorButton.setForeground(mView.getContext().getDrawable(resId));
        equationVisibleColorButton.invalidate();
    }

    private void setVariableAnimated(boolean animated) {
        isVariableAnimated = animated;
        int resId;
        if(animated) {
            resId = R.drawable.ic_baseline_pause_circle_outline_24;
        } else {
            resId = R.drawable.ic_baseline_play_circle_outline_24;
        }
        animateVariableValuesButton.setImageIcon(Icon.createWithResource(mView.getContext(), resId));

        if(animated) {
            editMathExpressionText.setEnabled(false);
            startVariableRangeEditText.setEnabled(false);
            endVariableRangeEditText.setEnabled(false);
            changeVariableValueInRangeSeekBar.setEnabled(false);
        } else {
            editMathExpressionText.setEnabled(true);
            startVariableRangeEditText.setEnabled(true);
            endVariableRangeEditText.setEnabled(true);
            changeVariableValueInRangeSeekBar.setEnabled(true);
        }
    }

    private void setVariableProgress(double start, double end, double progress) {
        startVariableRangeEditText.setText(String.format("%.3f", start));
        endVariableRangeEditText.setText(String.format("%.3f", end));
        changeVariableValueInRangeSeekBar.setProgress((int) (progress*changeVariableValueInRangeSeekBar.getMax()));
    }

    private void setVariableProgress(variable var) {
        setVariableProgress(var.getRangeStart(), var.getRangeEnd(), var.getValueProgress());
    }

    public ExpressionViewHolder(View view, ExpressionOptionsChangesListener changesListener) {
        super(view);
        mChangesListener = changesListener;
        mView = view;

        editMathExpressionText = (EditText) view.findViewById(R.id.EditMathExpressionText);
        editMathExpressionText.setOnFocusChangeListener((_view, hasFocus) -> {
            if(hasFocus) {
                saveChangesMathExpressionButton.setVisibility(View.VISIBLE);
            }
        });
        editMathExpressionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editMathExpressionText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        editMathExpressionText.setOnEditorActionListener((textView, actionId, event) -> {
            if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN)
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                saveChangesMathExpressionButton.performClick();
                return true;
            }
            return false;
        });

        equationVisibleColorButton = (Button) view.findViewById(R.id.EquationVisibleColorButton);
        equationVisibleColorButton.setOnLongClickListener(_view -> {
            ColorPickerDialogBuilder
                    .with(view.getContext())
                    .setTitle("Choose color")
                    .initialColor(equationVisibleColorButton.getForegroundTintList().getDefaultColor())
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setOnColorSelectedListener(new OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int selectedColor) {
                            equationVisibleColorButton.setForegroundTintList(ColorStateList.valueOf(selectedColor));
                            mChangesListener.changeEquationColor(exp, Color.valueOf(selectedColor));
                        }
                    })
                    .build()
                    .show();
            return false;
        });
        equationVisibleColorButton.setOnClickListener(_view -> {
            setEquationVisible(!isEquationVisible);
            mChangesListener.changeEquationVisibility(exp, isEquationVisible);
        });

        removeMathExpressionButton = (ImageButton) view.findViewById(R.id.RemoveMathExpressionButton);
        removeMathExpressionButton.setOnClickListener(_view -> {
            mChangesListener.deleteExpression(exp);
        });

        saveChangesMathExpressionButton = (ImageButton) view.findViewById(R.id.SaveChangesMathExpressionButton);
        saveChangesMathExpressionButton.setOnClickListener(_view -> {
            try {
                editMathExpressionText.clearFocus();
                mChangesListener.changeExpression(exp, editMathExpressionText.getText().toString());
                InputMethodManager imm = (InputMethodManager) mView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editMathExpressionText.getWindowToken(), 0);
            } catch(PatternSyntaxException e) {
                editMathExpressionText.setError(e.getDescription());
            } catch(IllegalArgumentException e) {
                editMathExpressionText.setError(e.getMessage());
            }
        });

        variableRangeOptionsView = (View) view.findViewById(R.id.VariableRangeOptionsView);
        startVariableRangeEditText = (EditText) view.findViewById(R.id.StartVariableRangeEditText);
        endVariableRangeEditText = (EditText) view.findViewById(R.id.EndVariableRangeEditText);
        TextView.OnEditorActionListener variableEditRangeActionListener = new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                double start = Double.parseDouble(startVariableRangeEditText.getText().toString()),
                        end = Double.parseDouble(endVariableRangeEditText.getText().toString());
                if(start >= end) {
                    String error = "invalid range";
                    startVariableRangeEditText.setError(error);
                    endVariableRangeEditText.setError(error);
                    changeVariableValueInRangeSeekBar.setActivated(false);
                } else {
                    mChangesListener.changeVariableRange(exp, start, end);
                    setVariableProgress((variable) exp);
                    editMathExpressionText.setText(exp.getExpression());
                    startVariableRangeEditText.clearFocus();
                    endVariableRangeEditText.clearFocus();
                }
                if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN)
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) mView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(startVariableRangeEditText.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(endVariableRangeEditText.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        };
        startVariableRangeEditText.setOnEditorActionListener(variableEditRangeActionListener);
        endVariableRangeEditText.setOnEditorActionListener(variableEditRangeActionListener);
        TextWatcher variableEditRangeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                startVariableRangeEditText.setError(null);
                endVariableRangeEditText.setError(null);
                changeVariableValueInRangeSeekBar.setActivated(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        startVariableRangeEditText.addTextChangedListener(variableEditRangeTextWatcher);
        endVariableRangeEditText.addTextChangedListener(variableEditRangeTextWatcher);
        changeVariableValueInRangeSeekBar = (SeekBar) view.findViewById(R.id.ChangeVariableValueInRangeSeekBar);
        changeVariableValueInRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    double x = 1.0*changeVariableValueInRangeSeekBar.getProgress() / changeVariableValueInRangeSeekBar.getMax();
                    mChangesListener.changeVariableValueProgress(exp, x);
                    editMathExpressionText.setText(exp.getExpression());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mChangesListener.changeVariableValueSave(exp);
            }
        });
        animateVariableValuesButton = (ImageButton) view.findViewById(R.id.AnimateVariableValuesButton);
        animateVariableValuesButton.setOnClickListener(_view -> {
            setVariableAnimated(!isVariableAnimated);
            variable i = (variable) exp;
            double step = (i.getRangeEnd() - i.getRangeStart()) / changeVariableValueInRangeSeekBar.getMax();
            mChangesListener.changeAnimateVariableStatus(exp, isVariableAnimated, step, variable.ANIMATION_MODE.BACK_AND_FORTH);
        });
        variableAnimationListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                if(exp instanceof variable && ((variable) exp).isAnimated()) {
                    editMathExpressionText.setText(exp.getExpression());
                    setVariableProgress((variable)exp);
                }
            }
        };
        mChangesListener.addVariablesAnimationListener(variableAnimationListener);
    }

    public void onInitialized(expression exp) {
        this.exp = exp;
        // reset
        variableRangeOptionsView.setVisibility(View.GONE);
        equationVisibleColorButton.setVisibility(View.GONE);
        saveChangesMathExpressionButton.setVisibility(View.GONE);
        // build
        editMathExpressionText.setText(exp.getExpression());
        if(exp instanceof equation) {
            equation o = (equation) exp;
            equationVisibleColorButton.setVisibility(View.VISIBLE);
            setEquationVisible(o.isVisible());
            equationVisibleColorButton.setForegroundTintList(ColorStateList.valueOf(o.getColor().toArgb()));
        } else if(exp instanceof variable) {
            variable o = (variable) exp;
            variableRangeOptionsView.setVisibility(View.VISIBLE);
            setVariableProgress(o);
            setVariableAnimated(o.isAnimated());
        }
    }

    public void onUpdated(expression exp, Bundle changes) {
        onInitialized(exp);
    }

    public void onAttached() {

    }

    public void onRecycled() {

    }

    public void onDetached() {
        mChangesListener.removeVariablesAnimationListener(variableAnimationListener);
    }
}