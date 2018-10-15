package com.hyxen.adlocusaar.repository.data.response;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hyxen.adlocusaar.repository.data.RemoteResponse;
import com.hyxen.adlocusaar.R;
import com.hyxen.adlocusaar.utils.Logger;

@JsonObject(fieldNamingPolicy = JsonObject.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class GetNewAndResponse extends RemoteResponse implements Parcelable {
    private static final String TAG = GetNewAndResponse.class.getSimpleName();

    @JsonField
    private String adType;
    @JsonField
    private String adId;
    @JsonField
    private String sid;
    @JsonField
    private String gourse;
    @JsonField
    private String devId;
    @JsonField
    private String trackImp;
    @JsonField
    private String adLink;
    @JsonField
    private String adIcon;
    @JsonField
    private String adLeftIcon;
    @JsonField
    private String adTitle;
    @JsonField
    private String adBody;
    @JsonField
    private String adNativeText;
    @JsonField
    private String adlocusAppPosition;
    @JsonField
    private String bvShareLink;

    public GetNewAndResponse() {
    }

    protected GetNewAndResponse(Parcel in) {
        adType = in.readString();
        adId = in.readString();
        sid = in.readString();
        gourse = in.readString();
        devId = in.readString();
        trackImp = in.readString();
        adLink = in.readString();
        adIcon = in.readString();
        adLeftIcon = in.readString();
        adTitle = in.readString();
        adBody = in.readString();
        adNativeText = in.readString();
        adlocusAppPosition = in.readString();
        bvShareLink = in.readString();
    }

    public static final Creator<GetNewAndResponse> CREATOR = new Creator<GetNewAndResponse>() {
        @Override
        public GetNewAndResponse createFromParcel(Parcel in) {
            return new GetNewAndResponse(in);
        }

        @Override
        public GetNewAndResponse[] newArray(int size) {
            return new GetNewAndResponse[size];
        }
    };

    public String getGourse() {
        return gourse;
    }

    public void setGourse(String gourse) {
        this.gourse = gourse;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getAdType() {
        return adType;
    }

    public void setAdType(String adType) {
        this.adType = adType;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getTrackImp() {
        return trackImp;
    }

    public void setTrackImp(String trackImp) {
        this.trackImp = trackImp;
    }

    public String getAdLink() {
        return adLink;
    }

    public void setAdLink(String adLink) {
        this.adLink = adLink;
    }

    public String getAdIcon() {
        return adIcon;
    }

    public void setAdIcon(String adIcon) {
        this.adIcon = adIcon;
    }

    public String getAdLeftIcon() {
        return adLeftIcon;
    }

    public void setAdLeftIcon(String adLeftIcon) {
        this.adLeftIcon = adLeftIcon;
    }

    public String getAdTitle() {
        return adTitle;
    }

    public void setAdTitle(String adTitle) {
        this.adTitle = adTitle;
    }

    public String getAdBody() {
        return adBody;
    }

    public void setAdBody(String adBody) {
        this.adBody = adBody;
    }

    public String getAdNativeText() {
        return adNativeText;
    }

    public void setAdNativeText(String adNativeText) {
        this.adNativeText = adNativeText;
    }

    public String getAdlocusAppPosition() {
        return adlocusAppPosition;
    }

    public void setAdlocusAppPosition(String adlocusAppPosition) {
        this.adlocusAppPosition = adlocusAppPosition;
    }

    public String getBvShareLink() {
        return bvShareLink;
    }

    public void setBvShareLink(String bvShareLink) {
        this.bvShareLink = bvShareLink;
    }

    /*
     ********** Help **********
     */

    /**
     * Get left icon resource
     *
     * @return
     */
    public int getLeftIconResource() {
        return switchLeftIconResource();
    }

    /**
     * Switch left icon resource
     *
     * @return
     */
    private int switchLeftIconResource() {
        int icon = !TextUtils.isEmpty(getAdLeftIcon()) ? Integer.parseInt(getAdLeftIcon()) : 0;
        int resource = 0;
        switch (icon) {
            case 1:
                resource = R.drawable.pm_ic_01;
                break;
            case 2:
                resource = R.drawable.pm_ic_02;
                break;
            case 3:
                resource = R.drawable.pm_ic_03;
                break;
            case 4:
                resource = R.drawable.pm_ic_04;
                break;
            case 5:
                resource = R.drawable.pm_ic_05;
                break;
            case 6:
                resource = R.drawable.pm_ic_06;
                break;
            case 7:
                resource = R.drawable.pm_ic_07;
                break;
            case 8:
                resource = R.drawable.pm_ic_08;
                break;
        }

        return resource;
    }

    /**
     * get banner info
     *
     * @param position
     * @return
     */
    public BannerInfo getBannerInfo(int position) {
        BannerInfo bannerInfo = new BannerInfo();
        int backgroundViewId = 0;
        int iconViewId = 0;
        switch (position) {
            case 1:
                backgroundViewId = R.id.bv_p_iv_bg1;
                iconViewId = R.id.bv_p_iv_icon1;
                break;
            case 2:
                backgroundViewId = R.id.bv_p_iv_bg2;
                iconViewId = R.id.bv_p_iv_icon2;
                break;
            case 3:
                backgroundViewId = R.id.bv_p_iv_bg3;
                iconViewId = R.id.bv_p_iv_icon3;
                break;
            case 4:
                backgroundViewId = R.id.bv_p_iv_bg4;
                iconViewId = R.id.bv_p_iv_icon4;
                break;
        }
        bannerInfo.setBackgroundViewId(backgroundViewId);
        bannerInfo.setIconViewId(iconViewId);
        return bannerInfo;
    }

    /**
     * Check Data is collect
     */
    public boolean dataIsCorrect() {
        boolean correct = true;
        if (TextUtils.isEmpty(getAdId())) {
            Logger.e(TAG, "[dataIsCorrect] getAdId is null");
            correct = false;
        }
        if (TextUtils.isEmpty(getSid())) {
            Logger.e(TAG, "[dataIsCorrect] getSid is null");
            correct = false;
        }
        return correct;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(adType);
        dest.writeString(adId);
        dest.writeString(sid);
        dest.writeString(gourse);
        dest.writeString(devId);
        dest.writeString(trackImp);
        dest.writeString(adLink);
        dest.writeString(adIcon);
        dest.writeString(adLeftIcon);
        dest.writeString(adTitle);
        dest.writeString(adBody);
        dest.writeString(adNativeText);
        dest.writeString(adlocusAppPosition);
        dest.writeString(bvShareLink);
    }

    public class BannerInfo {
        private int backgroundIcon;
        private int backgroundViewId;
        private int iconViewId;

        public int getBackgroundIcon() {
            return backgroundIcon;
        }

        public void setBackgroundIcon(int backgroundIcon) {
            this.backgroundIcon = backgroundIcon;
        }

        public int getBackgroundViewId() {
            return backgroundViewId;
        }

        public void setBackgroundViewId(int backgroundViewId) {
            this.backgroundViewId = backgroundViewId;
        }

        public int getIconViewId() {
            return iconViewId;
        }

        public void setIconViewId(int iconViewId) {
            this.iconViewId = iconViewId;
        }
    }
}
