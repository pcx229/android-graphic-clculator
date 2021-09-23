package com.graphingcalculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.graphingcalculator.data.Entitys.expression;

import java.util.List;

public class ExpressionsListViewAdapter extends RecyclerView.Adapter<ExpressionViewHolder> {

    private List<expression> mValues;
    private ExpressionOptionsChangesListener expressionsChangesListener;

    public ExpressionsListViewAdapter() {
        mValues = null;
    }

    public ExpressionsListViewAdapter(List<expression> items) {
        mValues = items;
    }

    public void setItems(List<expression> items) {
        mValues = items;
        notifyDataSetChanged();
    }

    public void setExpressionItemEditChangesListener(ExpressionOptionsChangesListener listener) {
        expressionsChangesListener = listener;
    }

    @Override
    public ExpressionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.equations_list_item, parent, false);
        return new ExpressionViewHolder(view, expressionsChangesListener);
    }

    @Override
    public void onBindViewHolder(ExpressionViewHolder holder, int position) {
        holder.init(mValues.get(position));
    }

    @Override
    public int getItemCount() {
        return (mValues == null) ? 0 : mValues.size();
    }

}