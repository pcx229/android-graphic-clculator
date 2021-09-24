package com.graphingcalculator;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.graphingcalculator.data.MainViewModel;
import com.graphingcalculator.data.Settings;
import com.graphingcalculator.graph.Range;
import com.graphingcalculator.graph.Ratio;

public class SettingsFragment extends Fragment {

    private MainViewModel viewModel;
    private EditText xMinRangeNumberInput,
            yMinRangeNumberInput,
            xMaxRangeNumberInput,
            yMaxRangeNumberInput,
            xRatioRangeNumberInput,
            yRatioRangeNumberInput;
    private Range range;
    private Ratio ratio;
    private TextView centerRange;
    private ImageButton ratioLockButton;
    private boolean isRatioLocked;

    public static final String NUMBER_FORMAT = "%.5f",
            POINT_FORMAT = "(%.5f, %.5f)";
    public static final int ERROR_RANGE = R.string.invalid_numbers_range;

    private boolean isSettingsLoaded;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings, container, false);
    }

    private void setErrorRangeX() {
        xMinRangeNumberInput.setError(getString(ERROR_RANGE));
        xMaxRangeNumberInput.setError(getString(ERROR_RANGE));
        centerRange.setText("(?, ?)");
    }
    private void setErrorRangeY() {
        yMinRangeNumberInput.setError(getString(ERROR_RANGE));
        yMaxRangeNumberInput.setError(getString(ERROR_RANGE));
        centerRange.setText("(?, ?)");
    }
    private void updateRange(Range range) {
        this.range = range;
        xMinRangeNumberInput.setError(null);
        xMaxRangeNumberInput.setError(null);
        yMinRangeNumberInput.setError(null);
        yMaxRangeNumberInput.setError(null);
        xMinRangeNumberInput.setText(String.format(NUMBER_FORMAT, range.startX));
        xMaxRangeNumberInput.setText(String.format(NUMBER_FORMAT, range.endX));
        yMinRangeNumberInput.setText(String.format(NUMBER_FORMAT, range.startY));
        yMaxRangeNumberInput.setText(String.format(NUMBER_FORMAT, range.endY));

        centerRange.setText(String.format(POINT_FORMAT, range.getCenterX(), range.getCenterY()));
    }
    private void updateOnRangeX() {
        xMinRangeNumberInput.setError(null);
        xMaxRangeNumberInput.setError(null);
        yMinRangeNumberInput.setText(String.format(NUMBER_FORMAT, range.startY));
        yMaxRangeNumberInput.setText(String.format(NUMBER_FORMAT, range.endY));

        centerRange.setText(String.format(POINT_FORMAT, range.getCenterX(), range.getCenterY()));
    }
    private void updateOnRangeY() {
        yMinRangeNumberInput.setError(null);
        yMaxRangeNumberInput.setError(null);
        xMinRangeNumberInput.setText(String.format(NUMBER_FORMAT, range.startX));
        xMaxRangeNumberInput.setText(String.format(NUMBER_FORMAT, range.endX));

        centerRange.setText(String.format(POINT_FORMAT, range.getCenterX(), range.getCenterY()));
    }
    private void updateRange() {
        updateRange(range);
    }

    private void setErrorRatio() {
        xRatioRangeNumberInput.setActivated(false);
        yRatioRangeNumberInput.setActivated(false);
        xRatioRangeNumberInput.setText("?");
        yRatioRangeNumberInput.setText("?");
    }
    private void updateRatio(Ratio ratio) {
        this.ratio = ratio;
        xRatioRangeNumberInput.setActivated(true);
        yRatioRangeNumberInput.setActivated(true);
        xRatioRangeNumberInput.setText(String.format(NUMBER_FORMAT, ratio.getXRatio()));
        yRatioRangeNumberInput.setText(String.format(NUMBER_FORMAT, ratio.getYRatio()));
    }
    private void updateRatio() {
        updateRatio(ratio);
    }

    private void updateLockedRatio(boolean isRatioLocked) {
        this.isRatioLocked = isRatioLocked;
        if(isRatioLocked) {
            ratioLockButton.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_baseline_lock_24));
        } else {
            ratioLockButton.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_baseline_lock_open_24));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // settings

        xMinRangeNumberInput = (EditText) view.findViewById(R.id.XMinRangeNumberInput);
        xMinRangeNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isSettingsLoaded || !xMinRangeNumberInput.hasFocus()) {
                    return;
                }
                float startX;
                try {
                    startX = Float.parseFloat(xMinRangeNumberInput.getText().toString());
                } catch (Exception e) {
                    return;
                }

                if(range.endX <= startX) {
                    setErrorRangeX();
                    setErrorRatio();
                } else {
                    range.startX = startX;
                    viewModel.setRange(range);
                    ratio.setXRangeWidth(range.getWidth(), isRatioLocked);
                    range.setHeight(ratio.getYRangeHeight());
                    updateRatio();
                    updateOnRangeX();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        xMaxRangeNumberInput = (EditText) view.findViewById(R.id.XMaxRangeNumberInput);
        xMaxRangeNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isSettingsLoaded || !xMaxRangeNumberInput.hasFocus()) {
                    return;
                }
                float endX;
                try {
                    endX = Float.parseFloat(xMaxRangeNumberInput.getText().toString());
                } catch (Exception e) {
                    return;
                }

                if(endX <= range.startX) {
                    setErrorRangeX();
                    setErrorRatio();
                } else {
                    range.endX = endX;
                    viewModel.setRange(range);
                    ratio.setXRangeWidth(range.getWidth(), isRatioLocked);
                    range.setHeight(ratio.getYRangeHeight());
                    updateRatio();
                    updateOnRangeX();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        yMinRangeNumberInput = (EditText) view.findViewById(R.id.YMinRangeNumberInput);
        yMinRangeNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isSettingsLoaded || !yMinRangeNumberInput.hasFocus()) {
                    return;
                }
                float startY;
                try {
                    startY = Float.parseFloat(yMinRangeNumberInput.getText().toString());
                } catch (Exception e) {
                    return;
                }

                if(range.endY <= startY) {
                    setErrorRangeY();
                    setErrorRatio();
                } else {
                    range.startY = startY;
                    viewModel.setRange(range);
                    ratio.setYRangeHeight(range.getHeight(), isRatioLocked);
                    range.setWidth(ratio.getXRangeWidth());
                    updateRatio();
                    updateOnRangeY();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        yMaxRangeNumberInput = (EditText) view.findViewById(R.id.YMaxRangeNumberInput);
        yMaxRangeNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isSettingsLoaded || !yMaxRangeNumberInput.hasFocus()) {
                    return;
                }
                float endY;
                try {
                    endY = Float.parseFloat(yMaxRangeNumberInput.getText().toString());
                } catch (Exception e) {
                    return;
                }

                if(endY <= range.startY) {
                    setErrorRangeY();
                    setErrorRatio();
                } else {
                    range.endY = endY;
                    viewModel.setRange(range);
                    ratio.setYRangeHeight(range.getHeight(), isRatioLocked);
                    range.setWidth(ratio.getXRangeWidth());
                    updateRatio();
                    updateOnRangeY();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        View.OnFocusChangeListener rangeFocusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!isSettingsLoaded) {
                    return;
                }
                if(!hasFocus) {
                    updateRange();
                }
            }
        };
        xMinRangeNumberInput.setOnFocusChangeListener(rangeFocusListener);
        xMaxRangeNumberInput.setOnFocusChangeListener(rangeFocusListener);
        yMinRangeNumberInput.setOnFocusChangeListener(rangeFocusListener);
        yMaxRangeNumberInput.setOnFocusChangeListener(rangeFocusListener);

        TextView.OnEditorActionListener rangeNumberInputActionListener = (textView, actionId, event) -> {
            if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN)
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                xMinRangeNumberInput.clearFocus();
                xMaxRangeNumberInput.clearFocus();
                yMinRangeNumberInput.clearFocus();
                yMaxRangeNumberInput.clearFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(xMinRangeNumberInput.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(xMaxRangeNumberInput.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(yMinRangeNumberInput.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(yMaxRangeNumberInput.getWindowToken(), 0);
            }
            return true;
        };
        xMinRangeNumberInput.setOnEditorActionListener(rangeNumberInputActionListener);
        xMaxRangeNumberInput.setOnEditorActionListener(rangeNumberInputActionListener);
        yMinRangeNumberInput.setOnEditorActionListener(rangeNumberInputActionListener);
        yMaxRangeNumberInput.setOnEditorActionListener(rangeNumberInputActionListener);

        xRatioRangeNumberInput = (EditText) view.findViewById(R.id.XRatioRangeNumberInput);
        xRatioRangeNumberInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!isSettingsLoaded || xRatioRangeNumberInput.hasFocus()) {
                    return;
                }
                float xRatio;
                try {
                    xRatio = Float.parseFloat(xRatioRangeNumberInput.getText().toString());
                } catch(Exception e) {
                    return;
                }
                if(xRatio > 0) {
                    ratio.setXRatio(xRatio);
                    range.setDimensions(ratio.getXRangeWidth(), ratio.getYRangeHeight());
                    viewModel.setRange(range);
                    updateRange();
                    updateRatio();
                }
            }
        });
        yRatioRangeNumberInput = (EditText) view.findViewById(R.id.YRatioRangeNumberInput);
        yRatioRangeNumberInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!isSettingsLoaded || yRatioRangeNumberInput.hasFocus()) {
                    return;
                }
                float yRatio;
                try {
                    yRatio = Float.parseFloat(yRatioRangeNumberInput.getText().toString());
                } catch(Exception e) {
                    return;
                }
                if(yRatio > 0) {
                    ratio.setYRatio(yRatio);
                    range.setDimensions(ratio.getXRangeWidth(), ratio.getYRangeHeight());
                    viewModel.setRange(range);
                    updateRange();
                    updateRatio();
                }
            }
        });
        TextView.OnEditorActionListener ratioRangeNumberActionListener = (textView, actionId, event) -> {
            if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN)
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                xRatioRangeNumberInput.clearFocus();
                yRatioRangeNumberInput.clearFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(xRatioRangeNumberInput.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(yRatioRangeNumberInput.getWindowToken(), 0);
            }
            return true;
        };
        xRatioRangeNumberInput.setOnEditorActionListener(ratioRangeNumberActionListener);
        yRatioRangeNumberInput.setOnEditorActionListener(ratioRangeNumberActionListener);

        centerRange = (TextView) view.findViewById(R.id.CenterRangeTextView);

        ratioLockButton = (ImageButton) view.findViewById(R.id.LockRatioRangeNumberInput);
        isRatioLocked = false;
        ratioLockButton.setOnClickListener(_view -> {
            updateLockedRatio(!isRatioLocked);
            viewModel.setRatioLock(isRatioLocked);
        });

        Switch showAxisSwitch = (Switch) view.findViewById(R.id.ShowAxisSwitch);
        showAxisSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            viewModel.setShowAxis(showAxisSwitch.isChecked());
        });

        Switch showGridSwitch = (Switch) view.findViewById(R.id.ShowGridSwitch);
        showGridSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            viewModel.setShowGrid(showGridSwitch.isChecked());
        });

        isSettingsLoaded = false;
        viewModel.getSettingsUpdates().observe(getActivity(), new Observer<Settings>() {
            @Override
            public void onChanged(Settings settings) {
                updateRange(settings.getRange());
                updateRatio(new Ratio(settings.getGraphWidth(), settings.getGraphHeight(), range.getWidth(), range.getHeight()));
                updateLockedRatio(settings.isRatioLock());

                showAxisSwitch.setChecked(settings.isShowAxis());

                showGridSwitch.setChecked(settings.isShowGrid());

                isSettingsLoaded = true;
                viewModel.getSettingsUpdates().removeObserver(this);
            }
        });
    }
}