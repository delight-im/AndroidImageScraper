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

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts all JPEG URLs from the given HTML source text
 * <p>
 * The specific part that we know most about is the file extension (which is actually at the end)
 * <p>
 * Therefore we reverse the HTML string and the RegEx to match the file extension at the beginning of every reversed URL
 **/
public class ImageURLFinder {
	
	private static final String REGEX_JPEG_FIRST = "[a-zA-Z0-9\\_\\/\\?]";
	private static final String REGEX_JPEG_OTHERS = "[a-zA-Z0-9\\-\\.\\_\\~\\:\\/\\?\\[\\]\\@\\!\\$\\&\\(\\)\\*\\+\\,\\;\\=\\%]+";
	private static final String REGEX_JPEG_DIVIDER = "\\.";
	private static final String REGEX_JPEG_EXTENSION = "(gpj|gepj)"; // reversed string as we want the specific part 
	private static final String REGEX_JPEG = REGEX_JPEG_EXTENSION+REGEX_JPEG_DIVIDER+REGEX_JPEG_OTHERS+REGEX_JPEG_FIRST;
	private String mRootURL;
	private String mBaseURL;

	public ImageURLFinder(String rootURL, String baseURL) {
		mRootURL = rootURL;
		mBaseURL = baseURL;
	}
	
	public Iterable<String> find(final String htmlSource) {
		HashSet<String> out = new HashSet<String>();
		final String reversedHTML = new StringBuilder(htmlSource).reverse().toString();
		final Pattern jpegURLRegex = Pattern.compile(REGEX_JPEG);

		final Matcher jpegURL = jpegURLRegex.matcher(reversedHTML);
		String imageURL;
		while (jpegURL.find()) {
			imageURL = new StringBuilder(jpegURL.group(0)).reverse().toString();
			out.add(makeAbsoluteURL(imageURL)); // add the re-reversed result to the output list
		}
		return out;
	}
	
	public String makeAbsoluteURL(String url) {
		if (url.startsWith("http://") || url.startsWith("https://")) { // URL is already complete with protocol
			return url;
		}
		else if (url.startsWith("//")) { // URL is complete but protocol-relative
			return "http://"+url.substring(2);
		}
		else if (url.startsWith("/")) { // URL is absolute
			return mRootURL+url.substring(1);
		}
		else { // URL is relative
			return mBaseURL+url;
		}
	}

}
