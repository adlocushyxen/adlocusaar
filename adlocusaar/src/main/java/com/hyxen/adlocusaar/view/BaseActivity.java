package com.hyxen.adlocusaar.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class BaseActivity extends Activity implements BaseContract.View {
    private static final String TAG = BaseActivity.class.getSimpleName();

    private BaseContract.Presenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setPresenter(BaseContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public Context getContext() {
        return this.getBaseContext();
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null)
            mPresenter.release();
        else {
            Log.e(TAG, "[onDestroy] mPresenter is null");
        }
        super.onDestroy();
    }
}
