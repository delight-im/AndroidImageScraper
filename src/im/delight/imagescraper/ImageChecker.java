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

import android.annotation.SuppressLint;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Checks a given list of image URLs for their filesize and MIME type and returns the largest image files to its callback */
public class ImageChecker {

    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final int THREAD_COUNT = 6;
    private final PriorityBlockingQueue<ImageURL> mImageURLs;
    private final LinkedBlockingQueue<Runnable> mWorkQueue;
    private final ThreadPoolExecutor mThreadPool;
    private ImageCheckerCallback mCallback;
    private String mUserAgent;

    /**
     * Constructs a new ThreadedImageChecker with the given callback
     *
     * @param callback callback where the results will be sent to
     * @param userAgent the value for the `User-Agent` HTTP header
     * @param allowCoreThreadTimeOut value for `ThreadPoolExecutor.allowCoreThreadTimeOut()`
     */
	@SuppressLint("NewApi")
	public ImageChecker(ImageCheckerCallback callback, String userAgent, boolean allowCoreThreadTimeOut) {
    	mCallback = callback;
    	mImageURLs = new PriorityBlockingQueue<ImageURL>();
    	mWorkQueue = new LinkedBlockingQueue<Runnable>();
    	mThreadPool = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mWorkQueue) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                ImageCheckerTask g = (ImageCheckerTask) r;
                mImageURLs.add(new ImageURL(g.getURL(), g.getFileSize()));
            }
        };
        mUserAgent = userAgent;
        if (allowCoreThreadTimeOut) {
        	if (android.os.Build.VERSION.SDK_INT >= 9) {
        		mThreadPool.allowCoreThreadTimeOut(true);
        	}
        }
    }

    /**
     * Downloads the given list of URLs and returns the largest ones that have the correct MIME type
     *
     * @param urls list of URLs to download
     */
    public void start(Iterable<String> urls) {
        for (String url : urls) {
        	mThreadPool.execute(new ImageCheckerTask(url, mUserAgent));
        }
        mThreadPool.shutdown();
        try {
			while (!mThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				Thread.sleep(100);
			}
			onFinished();
		}
        catch (InterruptedException e) {
        	onFinished();
		}
    }

    /** Executes as soon as all downloads have finished */
    private void onFinished() {
    	if (mCallback != null) {
    		mCallback.onImageCheckerFinished(mImageURLs);
    	}
    }

}
