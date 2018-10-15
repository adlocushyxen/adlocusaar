/*
 Copyright 2009-2010 AdMob, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Based on contributions by Joe Hansche <jhansche@myyearbook.com>
 */

package com.hyxen.adlocusaar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AdLocusTargeting
{
	private boolean mTestMode = false;
	private Gender mGender = Gender.UNKNOWN;
	private GregorianCalendar mBirthDate = null;

	private String mTag = "";
	
	public AdLocusTargeting setTag(String tag)
	{
		if(tag == null)
		{
			mTag = "";
		}
		else
		{
			mTag = tag;
		}
		return this;
	}
	
	public String getTag()
	{
		return mTag.equals("") ? null : mTag;
	}
	
	public boolean getTestMode()
	{
		return mTestMode;
	}

	public AdLocusTargeting setTestMode(boolean testMode)
	{
		mTestMode = testMode;
		return this;
	}

	public enum Gender
	{
		UNKNOWN, MALE, FEMALE
	}

	public Gender getGender()
	{
		return mGender;
	}

	public AdLocusTargeting setGender(Gender gender)
	{
		if (gender == null)
		{
			gender = Gender.UNKNOWN;
		}

		mGender = gender;
		return this;
	}

	public int getAge()
	{
		if (mBirthDate != null)
		{
			return Calendar.getInstance().get(Calendar.YEAR) - mBirthDate.get(Calendar.YEAR);
		}

		return -1;
	}

	public GregorianCalendar getBirthDate()
	{
		return mBirthDate;
	}

	public AdLocusTargeting setBirthDate(GregorianCalendar birthDate)
	{
		mBirthDate = birthDate;
		return this;
	}
	
	public AdLocusTargeting setBirthday(Date date)
	{
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		mBirthDate = gc;
		return this;
	}

	public AdLocusTargeting setAge(int age)
	{
		if(age < 0)
		{
			mBirthDate = null;
		}
		else
		{
			mBirthDate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - age, 0, 1);
		}
		return this;
	}


	@Override
	public boolean equals(Object o)
	{
		if(o instanceof AdLocusTargeting)
		{
			AdLocusTargeting alt = (AdLocusTargeting) o;
			return alt.getAge() == getAge() && alt.getTestMode() == getTestMode() && alt.getGender() == getGender() && mTag.equals(alt.mTag);
		}
		return super.equals(o);
	}
}
