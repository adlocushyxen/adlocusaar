package com.hyxen.adlocusaar.repository.remote.net;

public interface RequestListener
{
	void processContent(int errorCode, String content);
}
