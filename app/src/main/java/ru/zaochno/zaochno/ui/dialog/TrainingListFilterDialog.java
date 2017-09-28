package ru.zaochno.zaochno.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.zaochno.zaochno.R;
import ru.zaochno.zaochno.data.api.Retrofit2Client;
import ru.zaochno.zaochno.data.model.Thematic;
import ru.zaochno.zaochno.data.model.filter.TrainingFilter;
import ru.zaochno.zaochno.data.model.response.DataResponseWrapper;

public class TrainingListFilterDialog extends DialogFragment {
    @BindView(R.id.range_bar)
    public RangeSeekBar<Integer> rangeSeekBar;

    @BindView(R.id.spinner_thematic)
    public Spinner spinnerThematic;

    @BindView(R.id.tv_max_price_label)
    public TextView tvMaxPriceLabel;

    private TrainingFilter filter;
    private TrainingFilter internalFilter = new TrainingFilter();
    private OnFilterApplyListener onFilterApplyListener;

    private List<Thematic> thematics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_training_filter, container, false);
        ButterKnife.bind(this, rootView);

        if (getFilter() != null) {
            rangeSeekBar.setRangeValues(0, getFilter().getPriceEnd());
            tvMaxPriceLabel.setText(String.valueOf(getFilter().getPriceEnd()).concat(" руб"));
        }

        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<Integer> bar, Integer minValue, Integer maxValue) {
                internalFilter.setPriceStart(minValue);
                internalFilter.setPriceEnd(maxValue);
            }
        });

        Retrofit2Client.getInstance().getApi().getThematics().enqueue(new Callback<DataResponseWrapper<List<Thematic>>>() {
            @Override
            public void onResponse(Call<DataResponseWrapper<List<Thematic>>> call, Response<DataResponseWrapper<List<Thematic>>> response) {
                if (response == null || response.body() == null || response.body().getResponseObj() == null) {
                    Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_LONG).show();
                    return;
                }

                thematics = new ArrayList<>();
                thematics.add(new Thematic("Все"));
                thematics.addAll(response.body().getResponseObj());
                setupSpinner();
            }

            @Override
            public void onFailure(Call<DataResponseWrapper<List<Thematic>>> call, Throwable t) {

            }
        });

        return rootView;
    }

    private void setupSpinner() {
        ArrayAdapter<Thematic> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, thematics);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerThematic.setAdapter(dataAdapter);
        spinnerThematic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                internalFilter.getThematics().clear();
                internalFilter.getThematics().add((Thematic) adapterView.getItemAtPosition(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @OnClick(R.id.iv_close)
    public void closeDialog() {
        getDialog().cancel();
    }

    @OnClick(R.id.btn_apply)
    public void apply() {
        if (onFilterApplyListener != null)
            onFilterApplyListener.onApply(internalFilter);

        getDialog().cancel();
    }

    public TrainingFilter getFilter() {
        return filter;
    }

    public void setFilter(TrainingFilter filter) {
        this.filter = filter;

        internalFilter.setNumber(filter.getNumber());
        internalFilter.setPriceStart(filter.getPriceStart());
        internalFilter.setPriceEnd(filter.getPriceEnd());
        internalFilter.setToken(filter.getToken());
    }

    public void setOnFilterApplyListener(OnFilterApplyListener onFilterApplyListener) {
        this.onFilterApplyListener = onFilterApplyListener;
    }

    public interface OnFilterApplyListener {
        void onApply(TrainingFilter filter);
    }
}
