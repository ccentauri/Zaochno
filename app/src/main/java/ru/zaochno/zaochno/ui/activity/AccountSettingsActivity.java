package ru.zaochno.zaochno.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.zaochno.zaochno.R;
import ru.zaochno.zaochno.data.api.Retrofit2Client;
import ru.zaochno.zaochno.data.enums.UserType;
import ru.zaochno.zaochno.data.model.Token;
import ru.zaochno.zaochno.data.model.User;
import ru.zaochno.zaochno.data.model.response.BaseErrorResponse;
import ru.zaochno.zaochno.data.model.response.DataResponseWrapper;
import ru.zaochno.zaochno.data.provider.AuthProvider;
import ru.zaochno.zaochno.data.shared.SharedPrefUtils;
import ru.zaochno.zaochno.ui.callback.OnUserUpdateListener;
import ru.zaochno.zaochno.ui.fragment.AccountSettingsLawFragment;
import ru.zaochno.zaochno.ui.fragment.AccountSettingsPhysFragment;
import ru.zaochno.zaochno.utils.FileUtils;

public class AccountSettingsActivity extends BaseNavDrawerActivity implements OnUserUpdateListener {
    private static final String TAG = "AccountSettingsActivity";

    public User user;
    private AccountSettingsPhysFragment physFragment = new AccountSettingsPhysFragment();
    private AccountSettingsLawFragment lawFragment = new AccountSettingsLawFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        ButterKnife.bind(this);

        setupDrawer();

        setupUi();
        fetchData();
    }

    private void setupFragments() {
        String userType = AuthProvider.getInstance(this).getCurrentUser().getType();

        physFragment.setUser(user);
        lawFragment.setUser(user);

        physFragment.setOnUserUpdateListener(this);
        lawFragment.setOnUserUpdateListener(this);

        if (userType.equals(UserType.PHYS.rawType())) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, physFragment)
                    .commit();
        } else if (userType.equals(UserType.LAW.rawType())) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, lawFragment)
                    .commit();
        }
    }

    private void fetchData() {
        Retrofit2Client.getInstance().getApi().getUserInfo(new Token(AuthProvider.getInstance(this).getCurrentUser().getToken())).enqueue(new Callback<DataResponseWrapper<User>>() {
            @Override
            public void onResponse(Call<DataResponseWrapper<User>> call, Response<DataResponseWrapper<User>> response) {
                if (response == null || response.body() == null || response.body().getResponseObj() == null) {
                    Toast.makeText(AccountSettingsActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                    return;
                }

                AccountSettingsActivity.this.user = response.body().getResponseObj();
                setupFragments();
            }

            @Override
            public void onFailure(Call<DataResponseWrapper<User>> call, Throwable t) {
                Toast.makeText(AccountSettingsActivity.this, R.string.error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupUi() {
    }

    @Override
    public void onAvatarUpdate(Uri avatarUri) {
        // use the FileUtils to get the actual file by uri
        File file = new File(avatarUri.toString());

        Log.d(TAG, "onAvatarUpdate: file: " + file.getAbsolutePath());
        Log.d(TAG, "onAvatarUpdate: uri: " + avatarUri.getPath());
        Log.d(TAG, "onAvatarUpdate: uri: " + avatarUri.toString());

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);

        // add another part within the multipart request
        RequestBody token = RequestBody.create(okhttp3.MultipartBody.FORM, AuthProvider.getInstance(this).getCurrentUser().getToken());

        Retrofit2Client.getInstance().getApi().updateUserAvatar(token, body).enqueue(new Callback<BaseErrorResponse>() {
            @Override
            public void onResponse(Call<BaseErrorResponse> call, Response<BaseErrorResponse> response) {
                Log.d(TAG, "onResponse: " + response.toString());
            }

            @Override
            public void onFailure(Call<BaseErrorResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public void onUserInfoUpdate(User user) {
        Retrofit2Client.getInstance().getApi().updateUserInfo(user).enqueue(new Callback<BaseErrorResponse>() {
            @Override
            public void onResponse(Call<BaseErrorResponse> call, Response<BaseErrorResponse> response) {
                if (response == null || response.body() == null || response.body().getError()) {
                    Toast.makeText(AccountSettingsActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                    return;
                }

                Retrofit2Client.getInstance().getApi().getUserInfo(new Token(AuthProvider.getInstance(AccountSettingsActivity.this).getCurrentUser().getToken()))
                        .enqueue(new Callback<DataResponseWrapper<User>>() {
                            @Override
                            public void onResponse(Call<DataResponseWrapper<User>> call, Response<DataResponseWrapper<User>> response) {
                                if (response == null || response.body() == null || response.body().getResponseObj() == null) {
                                    Toast.makeText(AccountSettingsActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                User user = response.body().getResponseObj();
                                user.setToken(AuthProvider.getInstance(AccountSettingsActivity.this).getCurrentUser().getToken());
                                new SharedPrefUtils(AccountSettingsActivity.this).setCurrentUser(user);

                                startActivity(new Intent(AccountSettingsActivity.this, TrainingListActivity.class));
                                finish();
                            }

                            @Override
                            public void onFailure(Call<DataResponseWrapper<User>> call, Throwable t) {
                                Toast.makeText(AccountSettingsActivity.this, R.string.error, Toast.LENGTH_LONG).show();

                            }
                        });
            }

            @Override
            public void onFailure(Call<BaseErrorResponse> call, Throwable t) {
                Toast.makeText(AccountSettingsActivity.this, R.string.error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
