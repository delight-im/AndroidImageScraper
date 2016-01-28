package im.delight.imagescraper;

/*
 * Copyright (c) delight.im <info@delight.im>
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

/** Callback that is executed when the ImageChecker has finished work and wants to send the results back to ImageScraper */
public interface ImageCheckerCallback {

	/**
	 * Called as soon as the ImageChecker finished its work
	 * 
	 * @param imageURLs list of ImageURL instances sorted by their file size in descending order (largest files first)
	 */
	public void onImageCheckerFinished(PriorityBlockingQueue<ImageURL> imageURLs);

}
