package com.hyxen.adlocusaar;

import com.hyxen.adlocusaar.AdLocusLayout.ErrorCode;

public interface AdListener
{
	void onReceiveAd(Ad ad);
	void onFailedToReceiveAd(Ad ad, ErrorCode errorCode);
	void onEnd();
}
