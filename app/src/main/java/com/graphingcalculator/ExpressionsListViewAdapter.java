package com.graphingcalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.graphingcalculator.data.Entitys.expression;

import java.util.ArrayList;
import java.util.List;

class ExpressionDiffUtilCallBack extends DiffUtil.Callback {
    List<expression> newList;
    List<expression> oldList;

    public ExpressionDiffUtilCallBack(List<expression> newList, List<expression> oldList) {
        this.newList = newList;
        this.oldList = oldList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return newList.get(newItemPosition).getId() == oldList.get(oldItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return newList.get(newItemPosition).equals(oldList.get(oldItemPosition));
    }

    @Override
    public Bundle getChangePayload(int oldItemPosition, int newItemPosition) {
        // should return a bundle with all the changes between the old item and the new item
        // the bundle will be sent to the view holder to update the changes in a more precise way
        return null;
    }
}

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

    public List<expression> getItems() {
        return mValues;
    }

    public void setItemsUpdates(List<expression> items) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ExpressionDiffUtilCallBack(items, mValues));
        diffResult.dispatchUpdatesTo(this);
        if(mValues == null) {
            mValues = new ArrayList<>();
        } else {
            mValues.clear();
        }
        this.mValues.addAll(items);
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
    public void onBindViewHolder(ExpressionViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            Bundle changes = (Bundle) payloads.get(0);
            holder.update(mValues.get(position), changes);
        }
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