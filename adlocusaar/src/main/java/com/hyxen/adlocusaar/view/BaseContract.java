package com.hyxen.adlocusaar.view;

import android.content.Context;

public interface BaseContract {
    String TAG_PAGE = "com.hyxen.adlocusaar.page";

    interface View {
        void setPresenter(Presenter presenter);

        Context getContext();
    }

    interface Presenter {
        void setView(View view);

        void release();
    }
}
