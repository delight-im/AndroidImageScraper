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

/** Callback that is executed when the ImageScraper has finished work and wants to send the results to the calling Activity */
public interface ImageScraperCallback {
	
	public static final int ERROR_CONNECTION_PROBLEM = 1;
	public static final int ERROR_EMPTY_RESPONSE = 2;

	/** Called as soon as the ImageScraper has started its work */
	public void onStarted();
	/**
	 * Called as soon as the ImageScraper has finished its work
	 * @param output result of this ImageScraper with the requested number of URLs (the largest image files that were found)
	 */
	public void onFinished(ImageScraperResult output);

}
