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

import java.util.concurrent.PriorityBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

/** Extracts all image files from a given web page and returns as many of the largest files' URLs as requested */
public class ImageScraper extends Thread implements ImageCheckerCallback {

	/** The maximum file size in bytes that an image file may have (otherwise file size is returned as 0) */
	public static final int MAX_FILESIZE_BYTES = 786432;
	/** The minimum file size in bytes that an image file must have (otherwise file size is returned as 0) */
	public static final int MIN_FILESIZE_BYTES = 9050;
	/** Timeout for network read operations (in milliseconds) */
	public static final int NETWORK_READ_TIMEOUT_MILLIS = 5000;
	/** Timeout for network connection attempts (in milliseconds) */
	public static final int NETWORK_CONNECT_TIMEOUT_MILLIS = 3000;
	/** User-Agent string to send when accessing web pages */
	public static final String NETWORK_DEFAULT_USER_AGENT = "Android";
	/** MIME type that files must have to be eligible */
	public static final String MIME_TYPE_JPEG = "image/jpeg";
	/** RegEx that matches OpenGraph meta tags' property attributes */
	private static final String REGEX_OPEN_GRAPH_TAG_PROPERTY = "property(\\s*)=(\\s*)([\"']{1})(og:[a-zA-Z0-9]+)([\"']{1})";
	/** RegEx that matches OpenGraph meta tags' content attributes */
	private static final String REGEX_OPEN_GRAPH_TAG_CONTENT = "content(\\s*)=(\\s*)([\"']{1})([^\"']+)([\"']{1})";
	/** RegEx that matches the connection between OpenGraph property and content attributes */
	private static final String REGEX_OPEN_GRAPH_TAG_SEPARATOR = "(\\s+)";
	/** RegEx that finds complete OpenGraph meta tags (concatenation of the expressions above) */
	private static final String REGEX_OPEN_GRAPH_TAG = "("+REGEX_OPEN_GRAPH_TAG_PROPERTY+REGEX_OPEN_GRAPH_TAG_SEPARATOR+REGEX_OPEN_GRAPH_TAG_CONTENT+"|"+REGEX_OPEN_GRAPH_TAG_CONTENT+REGEX_OPEN_GRAPH_TAG_SEPARATOR+REGEX_OPEN_GRAPH_TAG_PROPERTY+")";
	/** RegEx that finds an optional base URL that may be set in an HTML document */
	private static final String REGEX_BASE_HREF = "base(\\s*)href=([\\\"']{1})([^\\\"']+)([\\\"']{1})";
	/** The callback that results will be delivered to from this ImageScraper instance */
	private ImageScraperCallback mCallback;
	/** The number of image files that have been requested by the calling Activity */
	private final int mImagesRequestedCount;
	/** The URL of the web page that this ImageScraper instance should work on */
	private final String mURL;
	/** The (optional) pre-defined title of the given web page */
	private final String mTitle;
	/** The URL to the root directory of the given web page */
	private final String mRootURL;
	/** The base URL of the given web page that is usually the directory containing the web page */
	private String mBaseURL;
	/** A pending result that may exist which has not been delivered to the calling Activity yet as there was no callback */
	private ImageScraperResult mPendingResult;
	/** The result that this ImageScraper builds and will ultimately deliver back to the calling Activity */
	private ImageScraperResult mOutput;
	/** Whether the core thread for the ImageChecker may time out or not */
	private boolean mAllowCoreThreadTimeOut;
	/** Custom User-Agent string that will be sent with all requests */
	private String mUserAgent;

	public ImageScraper(ImageScraperCallback callback, String url, int imagesRequestedCount) {
		this(callback, url, imagesRequestedCount, true);
	}

	public ImageScraper(ImageScraperCallback callback, String url, int imagesRequestedCount, boolean allowCoreThreadTimeOut) {
		this(callback, url, imagesRequestedCount, allowCoreThreadTimeOut, "");
	}

	public ImageScraper(ImageScraperCallback callback, String url, int imagesRequestedCount, boolean allowCoreThreadTimeOut, String title) {
		mCallback = callback;
		mURL = url;
		mTitle = title;
		mImagesRequestedCount = imagesRequestedCount;
		mAllowCoreThreadTimeOut = allowCoreThreadTimeOut;
		mRootURL = makeRootPath(url);
		mBaseURL = makeBasePath(url);
	}
	
	public void setUserAgent(String userAgent) {
		mUserAgent = userAgent;
	}
	
	public void setCallback(ImageScraperCallback callback) {
		mCallback = callback;
		if (mPendingResult != null) {
			mCallback.onFinished(mPendingResult); // notify the callback that the ImageScraper has finished and return its results
			mPendingResult = null;
			mCallback = null; // unset the callback as we do not need to receive any further information
		}
	}
	
	private static String makeBasePath(String url) {
		final int lastSlashPosition = url.lastIndexOf("/");
		if (lastSlashPosition >= 0) {
			return url.substring(0, lastSlashPosition)+"/";
		}
		else {
			return url;
		}
	}
	
	private static String makeRootPath(String url) {
		final String urlWithoutProtocol = url.replace("://", "");
		int firstSlashPosition = urlWithoutProtocol.indexOf("/");
		if (!url.equals(urlWithoutProtocol)) {
			firstSlashPosition += 3; // as we have removed 3 chars before and must consider this for the slash position
		}
		if (firstSlashPosition >= 0) {
			return url.substring(0, firstSlashPosition)+"/";
		}
		else {
			return url;
		}
	}

	@Override
	public void run() {
		if (mCallback != null) {
			mCallback.onStarted();
		}
		// FETCH HTML BEGIN
		BasicHttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 2500);
		HttpConnectionParams.setSoTimeout(httpParameters, 4000);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		String responseStr;
		try {
			HttpGet httpGet = new HttpGet(mURL);
			httpGet.setHeader("User-Agent", getUserAgent());
			final HttpResponse responseData = client.execute(httpGet);
			responseStr = EntityUtils.toString(responseData.getEntity());
		}
		catch (Exception e) {
			if (mCallback != null) {
				mCallback.onFinished(new ImageScraperResult(ImageScraperResult.ERROR_IO)); // notify the callback that the ImageScraper has finished and return its results
				mCallback = null; // unset the callback as we do not need to receive any further information
			}
			return;
		}
		if (responseStr == null || responseStr.equals("")) {
			if (mCallback != null) {
				mCallback.onFinished(new ImageScraperResult(ImageScraperResult.ERROR_EMPTY)); // notify the callback that the ImageScraper has finished and return its results
				mCallback = null; // unset the callback as we do not need to receive any further information
			}
			return;
		}
		mOutput = new ImageScraperResult(mURL, mTitle, mImagesRequestedCount);
		// FETCH HTML END

		// SEE IF HTML DOCUMENT HAS ANY BASE PATH SET BEGIN
		final Pattern basePathRegex = Pattern.compile(REGEX_BASE_HREF);
		final Matcher basePath = basePathRegex.matcher(responseStr);
		if (basePath.find()) {
			mBaseURL = basePath.group(3);
		}
		// SEE IF HTML DOCUMENT HAS ANY BASE PATH SET END

		// TRY TO FIND OPEN GRAPH META TAGS BEGIN
		final Pattern openGraphTagRegex = Pattern.compile(REGEX_OPEN_GRAPH_TAG);
		final Matcher openGraphTag = openGraphTagRegex.matcher(responseStr);
		String[] propertyName = new String[2];
		String[] propertyContent = new String[2];
		while (openGraphTag.find()) {
			propertyName[0] = openGraphTag.group(5);
			propertyContent[0] = openGraphTag.group(11);
			propertyName[1] = openGraphTag.group(22);
			propertyContent[1] = openGraphTag.group(16);
			for (int i = 0; i < 2; i++) {
				if (propertyName[i] != null && !propertyName[i].equals("")) {
					if (propertyName[i].equals("og:url")) {
						mOutput.setURL(propertyContent[i]);
					}
					else if (propertyName[i].equals("og:title")) {
						mOutput.setTitle(propertyContent[i]);
					}
					else if (propertyName[i].equals("og:image")) {
						mOutput.addImageURL(propertyContent[i], true);
					}
				}
			}
		}
		// TRY TO FIND OPEN GRAPH META TAGS END

		// TRY TO PARSE FULL HTML DOCUMENT BEGIN
		final ImageURLFinder imageURLFinder = new ImageURLFinder(mRootURL, mBaseURL);
		final Iterable<String> imageURLs = imageURLFinder.find(responseStr);
		// TRY TO PARSE FULL HTML DOCUMENT END

		// GET THE LARGEST IMAGE FILES AND WAIT FOR CALLBACK BEGIN
		new ImageChecker(this, getUserAgent(), mAllowCoreThreadTimeOut).start(imageURLs);
		// GET THE LARGEST IMAGE FILES FROM THE LIST END
	}
	
	protected String getUserAgent() {
		return mUserAgent == null ? NETWORK_DEFAULT_USER_AGENT : mUserAgent;
	}

	@Override
	public void onImageCheckerFinished(PriorityBlockingQueue<ImageURL> imageURLs) {
		// COLLECT THE LARGEST IMAGE FILES BEGIN
		boolean imageSlotsAvailable = true;
		while (imageSlotsAvailable) { // while images available in queue
			ImageURL imageURL = imageURLs.poll(); // get the next image
			if (imageURL != null) { // if still images in queue
				if (imageURL.getFileSize() > 0) { // if image could be accessed
					imageSlotsAvailable = mOutput.addImageURL(imageURL.getURL()); // add it to result list
				}
			}
			else { // if no more images in queue
				imageSlotsAvailable = false; // stop iterating
			}
		}
		// COLLECT THE LARGEST IMAGE FILES END
		
		if (mCallback != null) {
			mCallback.onFinished(mOutput); // notify the callback that the ImageScraper has finished and return its results
			mCallback = null; // unset the callback as we do not need to receive any further information
		}
		else {
			mPendingResult = mOutput;
		}
	}
	
}