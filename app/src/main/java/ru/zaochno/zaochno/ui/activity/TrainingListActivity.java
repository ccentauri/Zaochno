package ru.zaochno.zaochno.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.zaochno.zaochno.R;
import ru.zaochno.zaochno.data.annotation.Restrict;
import ru.zaochno.zaochno.data.annotation.processor.RestrictProcessor;
import ru.zaochno.zaochno.data.api.Retrofit2Client;
import ru.zaochno.zaochno.data.enums.UserAuthLevel;
import ru.zaochno.zaochno.data.model.Training;
import ru.zaochno.zaochno.data.model.filter.TrainingFilter;
import ru.zaochno.zaochno.data.model.response.DataResponseWrapper;
import ru.zaochno.zaochno.data.provider.AuthProvider;
import ru.zaochno.zaochno.ui.adapter.BaseTrainingListAdapter;
import ru.zaochno.zaochno.ui.adapter.TrainingListAllAdapter;
import ru.zaochno.zaochno.ui.adapter.TrainingListFavouriteAdapter;
import ru.zaochno.zaochno.ui.adapter.TrainingListPayedAdapter;
import ru.zaochno.zaochno.ui.adapter.pager.TrainingListPagerAdapter;
import ru.zaochno.zaochno.ui.dialog.TrainingListFilterDialog;
import ru.zaochno.zaochno.ui.fragment.TrainingListAllFragment;
import ru.zaochno.zaochno.ui.fragment.TrainingListFavouriteFragment;
import ru.zaochno.zaochno.ui.fragment.TrainingListPayedFragment;

public class TrainingListActivity extends BaseNavDrawerActivity {
    private static final String TAG = "TrainingListActivity";

    public static final String INTENT_KEY_TAB = "INTENT_KEY_TAB";
    public static final String INTENT_KEY_TAB_ALL = "INTENT_KEY_TAB_ALL";
    public static final String INTENT_KEY_TAB_FAVOURITE = "INTENT_KEY_TAB_FAVOURITE";
    public static final String INTENT_KEY_TAB_PAYED = "INTENT_KEY_TAB_PAYED";

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @BindView(R.id.iv_toolbar_logo)
    public ImageView ivToolbarLogo;

    @BindView(R.id.et_search_query)
    public EditText etSearchQuery;

    @BindView(R.id.tabs)
    public TabLayout tabLayout;
    @BindView(R.id.view_pager)
    public ViewPager viewPager;

    @BindView(R.id.search_bar)
    public View searchBar;

    @BindView(R.id.iv_clear_search_query)
    public ImageView ivClearSearchQuery;

    private List<Training> listAll = new LinkedList<>();

    private TrainingListAllAdapter adapterAll;
    private TrainingListFavouriteAdapter adapterFavourite;
    private TrainingListPayedAdapter adapterPayed;

    private TrainingListAllFragment fragmentAll = new TrainingListAllFragment();
    private TrainingListFavouriteFragment fragmentFavourite = new TrainingListFavouriteFragment();
    private TrainingListPayedFragment fragmentPayed = new TrainingListPayedFragment();

    private TrainingListPagerAdapter pagerAdapter;

    private TrainingListFilterDialog dialogFilterSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_list);
        ButterKnife.bind(this);

        setToolbar(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setTitle("");
        setupDrawer();

        setupUi();
        setupAdapters();
        fetchData();
        setupTabs();
        checkIntent();

        //RestrictProcessor.bind(this);
    }

    @Restrict(userAuthLevel = UserAuthLevel.LOGGED)
    public void userIsSignedIn() {
        Log.d(TAG, "userIsSignedIn: called");
    }

    @Restrict(userAuthLevel = UserAuthLevel.ANONYMOUS)
    public void userIsNotLogged() {
        Log.d(TAG, "userIsNotLogged: called");
    }

    private void checkIntent() {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(INTENT_KEY_TAB)) {
            Integer switchToId = -1;

            switch (getIntent().getExtras().getString(INTENT_KEY_TAB)) {
                case INTENT_KEY_TAB_ALL:
                    switchToId = 0;
                    break;
                case INTENT_KEY_TAB_FAVOURITE:
                    switchToId = 1;
                    break;
                case INTENT_KEY_TAB_PAYED:
                    switchToId = 2;
                    break;
            }

            if (switchToId != -1)
                viewPager.setCurrentItem(switchToId);
        }
    }

    private void setupUi() {
        Picasso.with(this)
                .load(R.drawable.ic_launcher_logo)
                .into(ivToolbarLogo);

        if (!AuthProvider.getInstance(this).isAuthenticated()) {
            tabLayout.setVisibility(View.GONE);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    searchBar.setVisibility(View.VISIBLE);
                else
                    searchBar.setVisibility(View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    if (ivClearSearchQuery.getVisibility() != View.GONE)
                        ivClearSearchQuery.setVisibility(View.GONE);

                    adapterAll.setTrainings(listAll);
                } else {
                    if (ivClearSearchQuery.getVisibility() != View.VISIBLE)
                        ivClearSearchQuery.setVisibility(View.VISIBLE);

                    adapterAll.filterByName(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setupFilterSettingsDialog(Integer maxPrice) {
        dialogFilterSettings = new TrainingListFilterDialog();
        dialogFilterSettings.setFilter(new TrainingFilter(
                1000, // number
                AuthProvider.getInstance(this).isAuthenticated() ? AuthProvider.getInstance(this).getCurrentUser().getToken() : "", // token
                null,  // thematics
                0, // start price
                maxPrice // end price
        ));
        dialogFilterSettings.setOnFilterApplyListener(new TrainingListFilterDialog.OnFilterApplyListener() {
            @Override
            public void onApply(TrainingFilter filter) {
                fetchData(filter);
            }
        });
    }

    private void setupAdapters() {
        adapterAll = new TrainingListAllAdapter(this);
        adapterFavourite = new TrainingListFavouriteAdapter(this);
        adapterPayed = new TrainingListPayedAdapter(this);

        adapterAll.setOnItemClickListener(new BaseTrainingListAdapter.OnItemClickListener() {
            @Override
            public void onClick(Training training) {
                startActivity(new Intent(TrainingListActivity.this, TrainingInfoActivity.class)
                        .putExtra(TrainingInfoActivity.INTENT_KEY_TRAINING_MODEL, training));
            }
        });

        fragmentAll.setAdapter(adapterAll);
        fragmentFavourite.setAdapter(adapterFavourite);
        fragmentPayed.setAdapter(adapterPayed);
    }

    private void fetchData() {
        Retrofit2Client.getInstance().getApi().getTrainings(new TrainingFilter(
                1000, // number
                AuthProvider.getInstance(this).isAuthenticated() ? AuthProvider.getInstance(this).getCurrentUser().getToken() : "", // token
                null,  // thematics
                0, // start price
                1000 // end price
        )).enqueue(new Callback<DataResponseWrapper<List<Training>>>() {
            @Override
            public void onResponse(Call<DataResponseWrapper<List<Training>>> call, Response<DataResponseWrapper<List<Training>>> response) {
                if (response == null || response.body() == null || response.body().getResponseObj() == null) {
                    Toast.makeText(TrainingListActivity.this, "Ошибка", Toast.LENGTH_LONG).show();
                    return;
                }

                Integer maxPrice = 0;
                listAll.clear();
                adapterFavourite.getTrainings().clear();
                adapterPayed.getTrainings().clear();

                listAll.addAll(response.body().getResponseObj());

                for (Training training : response.body().getResponseObj()) {
                    if (training.getHighestPrice().getPrice() > maxPrice)
                        maxPrice = training.getHighestPrice().getPrice();

                    if (training.getFavourite())
                        adapterFavourite.getTrainings().add(training);

                    if (training.getPayed())
                        adapterPayed.getTrainings().add(training);
                }

                adapterAll.setTrainings(listAll);
                adapterAll.notifyDataSetChanged();
                adapterFavourite.notifyDataSetChanged();
                adapterPayed.notifyDataSetChanged();

                setupFilterSettingsDialog(maxPrice);
            }

            @Override
            public void onFailure(Call<DataResponseWrapper<List<Training>>> call, Throwable t) {
                Toast.makeText(TrainingListActivity.this, "Ошибка", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchData(@NonNull TrainingFilter trainingFilter) {
        if (trainingFilter.getThematics().get(0).getId() == null)
            trainingFilter.getThematics().remove(0);

        Retrofit2Client.getInstance().getApi().getTrainings(trainingFilter).enqueue(new Callback<DataResponseWrapper<List<Training>>>() {
            @Override
            public void onResponse(Call<DataResponseWrapper<List<Training>>> call, Response<DataResponseWrapper<List<Training>>> response) {
                if (response == null || response.body() == null || response.body().getResponseObj() == null) {
                    Toast.makeText(TrainingListActivity.this, "Ошибка", Toast.LENGTH_LONG).show();
                    return;
                }

                listAll.clear();
                listAll.addAll(response.body().getResponseObj());
                adapterAll.setTrainings(listAll);
                adapterAll.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<DataResponseWrapper<List<Training>>> call, Throwable t) {
                Toast.makeText(TrainingListActivity.this, "Ошибка", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupTabs() {
        pagerAdapter = new TrainingListPagerAdapter(getSupportFragmentManager());

        pagerAdapter.addFragment(fragmentAll, "Тренинги");

        if (AuthProvider.getInstance(this).isAuthenticated()) {
            pagerAdapter.addFragment(fragmentFavourite, "Избранное");
            pagerAdapter.addFragment(fragmentPayed, "Купленное");
        }

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @OnClick(R.id.ib_search_settings)
    public void openSearchSettingsDialog() {
        dialogFilterSettings.show(getSupportFragmentManager(), "dialogFilterSettings");
    }

    @OnClick(R.id.iv_clear_search_query)
    public void clearSearchQuery() {
        etSearchQuery.setText("");
    }
}