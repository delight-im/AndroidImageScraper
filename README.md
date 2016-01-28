# AndroidImageScraper

Extracts all image URLs from a given web page ordered by file size

## Installation

 * Include one of the [JARs](JARs) in your `libs` folder
 * or
 * Copy the Java package to your project's source folder
 * or
 * Create a new library project from this repository and reference it in your project

## Usage

```
public class MyActivity extends Activity implements ImageScraperCallback {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// ...
		// get three images from <http://www.example.org/some/folder/gallery.html>
		new ImageScraper(this, "http://www.example.org/some/folder/gallery.html", 3).start();
	}

	@Override
	public void onStarted() {
		// do something (or not)
	}
	
	@Override
	public void onFinished(ImageScraperResult output) {
		// String[] imageFileURLs = output.getImageURLs();
		// String pageTitle = output.getTitle();
		// int errorCode = output.getErrorCode(); // should be ImageScraperResult.ERROR_NONE
	}

}
```

# License

```
Copyright (c) delight.im <info@delight.im>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
