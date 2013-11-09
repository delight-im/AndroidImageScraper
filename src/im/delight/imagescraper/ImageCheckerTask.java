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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;

/** Single task that is run by the ImageChecker and checks a single image URL for its MIME type and file size */
public class ImageCheckerTask implements Runnable {

	private final String mURL;
    private final String mUserAgent;
    private int mFileSize;

    /**
     * Constructs a new single ImageChecker task with the given URL
     * 
     * @param url image URL to check for MIME type and file size
     */
    public ImageCheckerTask(String url, String userAgent) {
        mURL = url;
        mUserAgent = userAgent;
    }
    
    /**
     * Returns the URL that this ImageChecker task has been working on
     * 
     * @return image file URL
     */
    public String getURL() {
    	return mURL;
    }

    /**
     * Returns the file size that this ImageChecker task has found for the URL that was passed to it
     * 
     * @return the image file's size
     */
    public int getFileSize() {
        return mFileSize;
    }

    /** Runs the ImageChecker tasks and detects the MIME type and file size for the URL that was passed */
	@Override
	public void run() {
		HttpURLConnection urlConnection = null;
		URL url;
		try {
			url = new URL(mURL);
		}
		catch (MalformedURLException e) {
			mFileSize = 0;
			return;
		}
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setReadTimeout(ImageScraper.NETWORK_READ_TIMEOUT_MILLIS);
			urlConnection.setConnectTimeout(ImageScraper.NETWORK_CONNECT_TIMEOUT_MILLIS);
			urlConnection.setUseCaches(false);
			urlConnection.setRequestProperty("User-Agent", mUserAgent);
			urlConnection.connect();
			final String mimeType = urlConnection.getContentType();
			if (mimeType == null || mimeType.contains(ImageScraper.MIME_TYPE_JPEG)) {
				mFileSize = urlConnection.getContentLength();
			}
			else {
				mFileSize = 0;
			}
			if (mFileSize < ImageScraper.MIN_FILESIZE_BYTES || mFileSize > ImageScraper.MAX_FILESIZE_BYTES) {
				mFileSize = 0;
			}
		}
		catch (IOException e) {
			mFileSize = 0;
		}
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}

}
