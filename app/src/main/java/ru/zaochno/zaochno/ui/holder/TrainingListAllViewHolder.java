package ru.zaochno.zaochno.ui.holder;

import android.view.View;

import butterknife.ButterKnife;

public class TrainingListAllViewHolder extends BaseTrainingListViewHolder {
    public TrainingListAllViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
