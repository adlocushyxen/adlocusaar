package com.hyxen.adlocusaar.view.main;

import com.hyxen.adlocusaar.repository.data.response.GetNewAndResponse;
import com.hyxen.adlocusaar.view.BaseContract;

public interface AdLocusContract {
    String ACTION_CLICK = "com.adlocus.push.action.CLICK";
    String ACTION_BIG_VIEW_INTENT = "com.adlocus.push.action.BIGVIEW_INTENTV";

    interface View extends BaseContract.View {
        void showUrlBrowser(String url);

        void showSettingDialog(android.view.View view);

        void closeDialog();

        void finish();
    }

    interface Presenter extends BaseContract.Presenter {
        void setUrlBrowser(String url);

        void setSettingEvent();

        void setShareEvent(GetNewAndResponse adInfo);

        void doFeedback(String type, String adId);
    }
}
