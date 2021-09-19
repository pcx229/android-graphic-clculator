package com.graphingcalculator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    public enum Pages{ GRAPH, EQUATIONS };
    private final MutableLiveData<Pages> atPage = new MutableLiveData<Pages>();

    public void setAtPage(Pages page) {
        atPage.setValue(page);
    }

    public LiveData<Pages> getAtPage() {
        return atPage;
    }
}