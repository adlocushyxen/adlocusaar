package com.hyxen.adlocusaar.view;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.disposables.Disposable;

public class BasePresenter implements BaseContract.Presenter {
    private Set<Disposable> mTasks;
    private BaseContract.View mView;

    public BasePresenter() {
        mTasks = new HashSet<>();
    }

    @Override
    public void setView(BaseContract.View view) {
        mView = view;
    }

    @Override
    public void release() {

    }

    protected void addTask(Disposable task) {
        if (task != null) {
            mTasks.add(task);
        }
    }
}
