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
 */

package com.hyxen.adlocusaar.obj;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class AdLocusAd implements Parcelable
{
	public int type;
	public String id;
	public int linkType;
	public String link;
	public Bitmap image;
	public String description;

	public int leftImageType;
	public int clickType;	
	public String sid;
	public int second;
	public int distance;
	public int errorCode;
	
	public String errorMassage;
	
	public boolean isHouseAd = false;
	
	public String scad_txt;
	public String scad_url;
	
	public String bv_banner;
	public String bv_text;
	public String bv_share_text;

	public String track_imp;

	public int icon_id = -1;
	
	public AdLocusAd()
	{
	}
	
	public boolean isNewStyle()
	{
		return scad_txt != null && !scad_txt.equals("") && scad_url != null && !scad_url.equals("");
	}
	
	public boolean isBigView()
	{
		return  bv_text != null && !bv_text.equals("") &&
				bv_banner != null && !bv_banner.equals("") &&
				bv_share_text != null && !bv_share_text.equals("") ;
	}

    public static final Creator<AdLocusAd> CREATOR = new Creator<AdLocusAd>() {
        public AdLocusAd createFromParcel(Parcel in) {
            return new AdLocusAd(in);
        }

        public AdLocusAd[] newArray(int size) {
            return new AdLocusAd[size];
        }
    };
    
    private AdLocusAd(Parcel in) {
		type = in.readInt();
		id = in.readString();
		linkType = in.readInt();
		link = in.readString();
		image = in.readParcelable(null);
		description = in.readString();
		leftImageType = in.readInt();
		clickType = in.readInt();
		sid = in.readString();
		second = in.readInt();
		distance = in.readInt();
		errorCode = in.readInt();
		errorMassage = in.readString();
		isHouseAd = in.readByte() != 0;
		scad_txt = in.readString();
		scad_url = in.readString();
		bv_banner = in.readString();
		bv_text = in.readString();
		bv_share_text = in.readString();
		track_imp = in.readString();
    }
	
	@Override
	public int describeContents() 
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(type);
		dest.writeString(id);
		dest.writeInt(linkType);
		dest.writeString(link);
		dest.writeParcelable(image, flags);
		dest.writeString(description);
		dest.writeInt(leftImageType);
		dest.writeInt(clickType);
		dest.writeString(sid);
		dest.writeInt(second);
		dest.writeInt(distance);
		dest.writeInt(errorCode);
		dest.writeString(errorMassage);
		dest.writeByte((byte) (isHouseAd ? 1 : 0));
		dest.writeString(scad_txt);
		dest.writeString(scad_url);
		dest.writeString(bv_banner);
		dest.writeString(bv_text);
		dest.writeString(bv_share_text);
		dest.writeString(track_imp);
	}


	public String getBigViewTitle(Context context) {
		return context.getString(context.getApplicationInfo().labelRes);
	}
}
