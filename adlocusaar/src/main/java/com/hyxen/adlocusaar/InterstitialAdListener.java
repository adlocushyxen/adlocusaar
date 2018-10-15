package com.hyxen.adlocusaar;

import com.hyxen.adlocusaar.AdLocusLayout.ErrorCode;

public interface InterstitialAdListener
{
	void onReceiveAd(InterstitialVideoAd ad);
	void onFailedToReceiveAd(InterstitialVideoAd ad, ErrorCode errorCode);
	void onEnd();
}
