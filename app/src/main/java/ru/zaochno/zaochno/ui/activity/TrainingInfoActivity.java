package ru.zaochno.zaochno.ui.activity;

import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.ProgressView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.zaochno.zaochno.R;
import ru.zaochno.zaochno.data.api.Retrofit2Client;
import ru.zaochno.zaochno.data.event.TrainingFavouriteEvent;
import ru.zaochno.zaochno.data.model.BaseChapter;
import ru.zaochno.zaochno.data.model.Chapter;
import ru.zaochno.zaochno.data.model.SubChapter;
import ru.zaochno.zaochno.data.model.Training;
import ru.zaochno.zaochno.data.model.TrainingFull;
import ru.zaochno.zaochno.data.model.response.BaseErrorResponse;
import ru.zaochno.zaochno.data.model.response.DataResponseWrapper;
import ru.zaochno.zaochno.data.provider.AuthProvider;
import ru.zaochno.zaochno.data.utils.DateUtils;
import ru.zaochno.zaochno.databinding.ActivityTrainingInfoBinding;
import ru.zaochno.zaochno.ui.adapter.ChapterListAdapter;

public class TrainingInfoActivity extends BaseNavDrawerActivity implements ChapterListAdapter.OnChapterClickListener {
    private static final String TAG = "TrainingInfoActivity";

    public static final String INTENT_KEY_TRAINING_MODEL = "INTENT_KEY_TRAINING_MODEL";

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @BindView(R.id.btn_buy)
    public Button btnBuy;

    @BindView(R.id.progress_bar)
    public ProgressView progressBar;

    @BindView(R.id.nested_scroll_view)
    public NestedScrollView nestedScrollView;

    @BindView(R.id.btn_read_more)
    public Button btnReadMore;

    @BindView(R.id.tv_description)
    public TextView tvDescription;

    @BindView(R.id.container_progress)
    public View containerProgress;

    @BindView(R.id.tv_valid_tow)
    public TextView tvValidTo;

    @BindView(R.id.btn_to_fav)
    public TextView tvButtonFavourite;

    @BindView(R.id.btn_schedule)
    public Button btnSchedule;

    @BindView(R.id.recycler_view_title)
    public TextView tvRecyclerViewTitle;

    @BindView(R.id.recycler_view_chapters)
    public RecyclerView recyclerViewChapters;

    @BindView(R.id.btn_container)
    public LinearLayout llBtnContainer;

    @BindView(R.id.btn_demo)
    public View cBtnDemo;

    private Training training;
    private TrainingFull trainingFull;
    private ActivityTrainingInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_training_info);
        ButterKnife.bind(this);

        hideContent();
        showProgressBar();

        setToolbar(toolbar);
        setupDrawer();

        if (!getDataFromIntent())
            return;
        fetchTraining();
    }

    private void fetchTraining() {
        if (AuthProvider.getInstance(this).isAuthenticated())
            training.setUserToken(AuthProvider.getInstance(this).getCurrentUser().getToken());

        Retrofit2Client.getInstance().getApi().getFullTraining(training).enqueue(new Callback<DataResponseWrapper<TrainingFull>>() {
            @Override
            public void onResponse(Call<DataResponseWrapper<TrainingFull>> call, Response<DataResponseWrapper<TrainingFull>> response) {
                if (response == null || response.body() == null) {
                    Toast.makeText(TrainingInfoActivity.this, "Ошибка", Toast.LENGTH_LONG).show();
                    return;
                }

                trainingFull = response.body().getResponseObj();
                setupUi();
                showContent();
                hideProgressBar();
            }

            @Override
            public void onFailure(Call<DataResponseWrapper<TrainingFull>> call, Throwable t) {
                Toast.makeText(TrainingInfoActivity.this, "Ошибка", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showContent() {
        nestedScrollView.setVisibility(View.VISIBLE);
    }

    private void hideContent() {
        nestedScrollView.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void setupUi() {
        binding.setTraining(training);
        binding.setTrainingFull(trainingFull);

        Drawable drawable = ResourcesCompat.getDrawable(
                getResources(),
                training.getFavourite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp,
                null);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tvButtonFavourite.setCompoundDrawables(null, drawable, null, null);

        if (training.getPayed())
            setupUiPayed();
        else
            setupUiNotPayed();
    }

    private void setupUiPayed() {
        btnBuy.setVisibility(View.GONE);
//        tvRecyclerViewTitle.setVisibility(View.VISIBLE);
        containerProgress.setVisibility(View.VISIBLE);
        btnSchedule.setVisibility(View.VISIBLE);
        cBtnDemo.setVisibility(View.GONE);
        llBtnContainer.setWeightSum(3);

        tvDescription.setMaxLines(3);

        btnReadMore.setVisibility(View.VISIBLE);
        btnReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvDescription.setMaxLines(999);
                btnReadMore.setVisibility(View.GONE);
            }
        });

        if (trainingFull.getDurationMillis() != null && trainingFull.getPurchaseDate() != null) {
            tvValidTo.setVisibility(View.VISIBLE);
            tvValidTo.setText(tvValidTo.getText().toString().concat(
                    DateUtils.millisToPattern(trainingFull.getDurationMillis() + trainingFull.getPurchaseDate(), DateUtils.PATTERN_DEFAULT)
            ));
        }

        setupRecyclerViews();
    }

    private void setupUiNotPayed() {
        btnBuy.setVisibility(View.VISIBLE);
        containerProgress.setVisibility(View.GONE);
        btnSchedule.setVisibility(View.GONE);

        btnBuy.setText("Купить (от ".concat(String.valueOf(training.getLowestPrice().getPrice())).concat(" руб.)"));

        tvDescription.setMaxLines(999);

        btnReadMore.setVisibility(View.GONE);
        tvValidTo.setVisibility(View.GONE);

//        tvRecyclerViewTitle.setVisibility(View.GONE);
        setupRecyclerViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onTrainingFavouriteEvent(TrainingFavouriteEvent event) {
        training = event.getTraining();
//        fetchTraining();
        setupUi();
    }

    private void setupRecyclerViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LinkedList<BaseChapter> chapters = new LinkedList<>();

        // Prepare data set
        for (int k = 0; k < trainingFull.getChapters().size(); k++) {
            Chapter chapter = trainingFull.getChapters().get(k);
            chapter.setPosition(k + 1);
            chapters.add(chapter);

            if (chapter.getSubChapters() != null)
                for (int i = 0; i < chapter.getSubChapters().size(); i++) {
                    if (chapter.getSubChapters().get(i) != null) {
                        chapter.getSubChapters().get(i).setPosition(i + 1);
                        chapter.getSubChapters().get(i).setParentId(chapter.getId());
                        chapters.add(chapter.getSubChapters().get(i));
                    }
                }
        }

        ChapterListAdapter adapter = new ChapterListAdapter(this, chapters);
        adapter.setOnChapterClickListener(this);

        recyclerViewChapters.setAdapter(adapter);
        recyclerViewChapters.setHasFixedSize(true);
        recyclerViewChapters.setLayoutManager(layoutManager);
    }

    @BindingAdapter("android:src")
    public static void setImageUrl(ImageView view, String url) {
        Picasso.with(view.getContext())
                .load(url)
                .into(view);
    }

    public boolean getDataFromIntent() {
        if (getIntent() == null || getIntent().getExtras() == null || !getIntent().getExtras().containsKey(INTENT_KEY_TRAINING_MODEL))
            return false;

        training = ((Training) getIntent().getExtras().get(INTENT_KEY_TRAINING_MODEL));
        return training != null;
    }

    @OnClick(R.id.btn_schedule)
    public void schedule() {
        startActivity(new Intent(TrainingInfoActivity.this, ExamNewActivity.class)
                .putExtra(ExamNewActivity.INTENT_KEY_TRAINING_MODEL, training));
    }

    @OnClick(R.id.btn_buy)
    public void onBuy() {
    }

    @OnClick(R.id.btn_demo)
    public void onDemo() {
        startActivity(new Intent(TrainingInfoActivity.this, TrainingInfoActivity.class)
                .putExtra(TrainingInfoActivity.INTENT_KEY_TRAINING_MODEL, training));
    }

    @OnClick(R.id.btn_to_fav)
    public void onFavourite() {
        if (!AuthProvider.getInstance(this).isAuthenticated()) {
            startActivity(new Intent(TrainingInfoActivity.this, LoginActivity.class));
            return;
        }

        // Invert favourite status
        training.setFavourite(!training.getFavourite());
        training.setUserToken(AuthProvider.getInstance(this).getCurrentUser().getToken());

        Retrofit2Client.getInstance().getApi().favouriteTraining(training).enqueue(new Callback<BaseErrorResponse>() {
            @Override
            public void onResponse(Call<BaseErrorResponse> call, Response<BaseErrorResponse> response) {
                if (response == null || response.body() == null || response.body().getError()) {
                    Toast.makeText(TrainingInfoActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.body().getError())
                    return;

                Toast.makeText(TrainingInfoActivity.this, R.string.added_to_fav, Toast.LENGTH_LONG).show();
                EventBus.getDefault().post(new TrainingFavouriteEvent(training));
            }

            @Override
            public void onFailure(Call<BaseErrorResponse> call, Throwable t) {
                Toast.makeText(TrainingInfoActivity.this, R.string.error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @OnClick(R.id.btn_test)
    public void onTest() {
        startActivity(new Intent(TrainingInfoActivity.this, TestListActivity.class)
                .putExtra(TestListActivity.INTENT_KEY_TRAINING_ID, training.getId()));
        finish();
    }

    @OnClick(R.id.btn_share)
    public void onShare() {
    }

    @Override
    public void onChapterClick(Chapter chapter) {
        startActivity(new Intent(TrainingInfoActivity.this, TrainingMaterialActivity.class)
                .putExtra(TrainingMaterialActivity.INTENT_KEY_TRAINING_ID, training.getId())
                .putExtra(TrainingMaterialActivity.INTENT_KEY_SHOW_TYPE, TrainingMaterialActivity.SHOW_TYPE_CHAPTER)
                .putExtra(TrainingMaterialActivity.INTENT_KEY_CHAPTER_ID, chapter.getId())
        );
    }

    @Override
    public void onSubChapterClick(SubChapter subChapter) {
        startActivity(new Intent(TrainingInfoActivity.this, TrainingMaterialActivity.class)
                .putExtra(TrainingMaterialActivity.INTENT_KEY_TRAINING_ID, training.getId())
                .putExtra(TrainingMaterialActivity.INTENT_KEY_SHOW_TYPE, TrainingMaterialActivity.SHOW_TYPE_SUB_CHAPTER)
                .putExtra(TrainingMaterialActivity.INTENT_KEY_CHAPTER_ID, subChapter.getParentId())
                .putExtra(TrainingMaterialActivity.INTENT_KEY_SUB_CHAPTER_ID, subChapter.getId())
        );
    }
}