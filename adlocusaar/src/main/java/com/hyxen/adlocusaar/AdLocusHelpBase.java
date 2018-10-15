package com.hyxen.adlocusaar;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.disposables.Disposable;

public class AdLocusHelpBase {
    private Set<Disposable> mTasks;

    public AdLocusHelpBase() {
        mTasks = new HashSet<>();
    }

    protected void addTask(Disposable task) {
        if (task != null)
            mTasks.add(task);
    }

    protected void release() {
        // Un-subscribe all tasks
        for (Disposable task : mTasks) {
            if (task != null && !task.isDisposed()) {
                task.dispose();
            }
        }
    }
}
