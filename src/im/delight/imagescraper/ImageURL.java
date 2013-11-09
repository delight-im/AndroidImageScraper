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

import java.util.Comparator;

public class ImageURL implements Comparable<ImageURL> {

	private String mURL;
	private int mFileSize;
	
	public ImageURL(String url, int fileSize) {
		mURL = url;
		mFileSize = fileSize;
	}
	
	public String getURL() {
		return mURL;
	}
	
	public long getFileSize() {
		return mFileSize;
	}
	
    public static Comparator<ImageURL> COMPARATOR = new Comparator<ImageURL>() {
		public int compare(ImageURL arg0, ImageURL arg1) {
			if (arg0.equals(arg1)) {
				return 0;
			}
	    	long other = arg0.getFileSize();
	    	long current = arg1.getFileSize();
	        if (current > other) {
	        	return 1;
	        }
	        else {
	        	if (current < other) {
	        		return -1;
	        	}
	        	else {
	        		return 0;
	        	}
	        }
		}
    };

	@Override
	public int compareTo(ImageURL arg0) {
		return COMPARATOR.compare(this, arg0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		ImageURL other = (ImageURL) obj;
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
		return "ImageURL [mURL=" + mURL + ", mFileSize=" + mFileSize + "]";
	}

}
