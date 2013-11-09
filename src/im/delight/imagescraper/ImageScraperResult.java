package im.delight.imagescraper;

/**
 * Copyright 2013 www.delight.im <info@delight.im>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageScraperResult implements Parcelable {

	public static final int ERROR_NONE = 0;
	public static final int ERROR_IO = 1;
	public static final int ERROR_EMPTY = 2;
	private final int mMaxImageURLs;
	private final int mErrorCode;
	private String mURL;
	private String mTitle;
	private String[] mImageURLs;
	
	public ImageScraperResult(String url, String title, int maxImageURLs) {
		mURL = url;
		mTitle = title;
		mMaxImageURLs = maxImageURLs;
		mImageURLs = new String[mMaxImageURLs];
		mErrorCode = ERROR_NONE;
	}
	
	public ImageScraperResult(int errorCode) {
		mMaxImageURLs = 0;
		mErrorCode = errorCode;
	}
	
	public int getErrorCode() {
		return mErrorCode;
	}
	
	public String getURL() {
		return mURL;
	}
	
	public void setURL(String url) {
		mURL = url;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public String[] getImageURLs() {
		return mImageURLs;
	}
	
	public boolean addImageURL(String imageURL) {
		return addImageURL(imageURL, false);
	}
	
	public boolean addImageURL(String imageURL, boolean isLowPriority) {
		final int startIndex;
		final int endIndex;
		if (isLowPriority) {
			startIndex = mMaxImageURLs-1;
			endIndex = 0;
		}
		else {
			startIndex = 0;
			endIndex = mMaxImageURLs-1;
		}
		for (int i = startIndex; i <= endIndex; i++) {
			if (mImageURLs[i] == null) {
				mImageURLs[i] = imageURL;
				return true;
			}
		}
		return false;
	}
	
	public boolean isComplete() {
		if (mTitle == null || mURL == null || mImageURLs == null) {
			return false;
		}
		if (mTitle.equals("")) {
			return false;
		}
		if (mURL.equals("")) {
			return false;
		}
		boolean hasImages = false;
		for (int i = 0; i < mMaxImageURLs; i++) {
			if (mImageURLs[i] != null && !mImageURLs[i].equals("")) {
				hasImages = true;
			}
		}
		return hasImages;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mErrorCode;
		result = prime * result + Arrays.hashCode(mImageURLs);
		result = prime * result + ((mTitle == null) ? 0 : mTitle.hashCode());
		result = prime * result + ((mURL == null) ? 0 : mURL.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ImageScraperResult other = (ImageScraperResult) obj;
		if (mErrorCode != other.mErrorCode) {
			return false;
		}
		if (!Arrays.equals(mImageURLs, other.mImageURLs)) {
			return false;
		}
		if (mTitle == null) {
			if (other.mTitle != null) {
				return false;
			}
		}
		else if (!mTitle.equals(other.mTitle)) {
			return false;
		}
		if (mURL == null) {
			if (other.mURL != null) {
				return false;
			}
		}
		else if (!mURL.equals(other.mURL)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ImageScraperResult [mURL=" + mURL + ", mTitle=" + mTitle + ", mImageURLs=" + Arrays.toString(mImageURLs) + ", mMaxImageURLs=" + mMaxImageURLs + ", mErrorCode=" + mErrorCode + "]";
	}
    
	public static final Parcelable.Creator<ImageScraperResult> CREATOR = new Parcelable.Creator<ImageScraperResult>() {
		@Override
		public ImageScraperResult createFromParcel(Parcel in) {
			return new ImageScraperResult(in);
		}
		@Override
		public ImageScraperResult[] newArray(int size) {
			return new ImageScraperResult[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(mURL);
		out.writeString(mTitle);
		out.writeStringArray(mImageURLs);
		out.writeInt(mMaxImageURLs);
		out.writeInt(mErrorCode);
	}
	
	private ImageScraperResult(Parcel in) {
		mURL = in.readString();
		mTitle = in.readString();
		mImageURLs = in.createStringArray();
		mMaxImageURLs = in.readInt();
		mErrorCode = in.readInt();
	}

}
