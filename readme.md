# Terraview
<a href="https://play.google.com/store/apps/details?id=com.iamtechknow.terraview"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge.png" width="185" height="60"/></a><br>
Terraview is a REST client for NASA's [Global Imagery Browse Services](https://earthdata.nasa.gov/gibs) (GIBS).
GIBS is a public API providing access to satellite data and imagery from the EOS (Earth Observing System) and A-Train satellites.
While this application is not directly related to NASA's [Worldview](https://worldview.earthdata.nasa.gov),
I worked on improvements on the project in an internship in NASA's Goddard Space Flight Center and created this based on the idea a better interface could be made for mobile devices.

## Decisions
* Code is split by feature under Model-View-Presenter Architecture
	I started this application by implementing core features within the Activities and Fragments. There are still features to be implemented which will lead to code bloat. Recently I underwent the process of converting to MVP to seperate business logic from the view to simplify the logic needed in the Activities and Fragments. In the layer picker feature, the event bus has been greatly simplified as a result to minimize coupling. There are instructmentation and unit tests to demonstrate MVP testing and code coverage. Over time I like to improve the MVP structure to create more meaningful tests, but due to dependency on Google Maps this is not very feasable.

* RxJava is used to create an event bus and to perform networking tasks
	Currently the ViewPager which accepts fragments to "page" through them in tabs does not allow communication through each of these fragments. 
	The app needs a way to determine what category is selected to display the corresponding measurements in the next tab, and finally the revelant layers in the final tab. This is handled by creating an event bus in RxJava allowing the activities and fragments to have their own reference to the same event bus instance to post and consume events.
	Observables and subscribers are also used to pass Retrofit calls to background threads and the response bodies may be subscribed upon back in the main thread. They allow tasks to run in timed intervals to make animations possible.

* Retrofit is used to obtain HTML of layer information and parse XML
    When selecting layers in the Worldview application, information and sources are displayed.
    This is found in a web page. I simplified the process of generating the URLs by creating my own API interface to use with Retrofit. No converter is used, the raw ResponseBody HTML object is returned and shown to the user.
	Retrofit is also used in conjunction with the Simple XML framework to parse XML of colormaps data due to its consistent structure for each supported layer. 

* RecyclerView features are used to help manage layers
	The RecyclerView is used extensively to represent lists in the layer picker and the current layers list on the right hand drawer in the map Activity. In the layer picker, the RecyclerView adapters bind the list item ViewHolders to a OnClickListener that emits an event to the event bus. On the layer list, the adapter can keep track of what items are selected to populate the layer "stack" that represents the order of the layers on the map with the help of a Hash map and a [SparseBooleanArray](SparseBooleanArray) for each list item position. For the current layer list, an [ItemTouchHelper](https://developer.android.com/reference/android/support/v7/widget/helper/ItemTouchHelper.html) and callbacks are used to allow the list to be modified by gestures. List items may be swapped with adjacent entries by drag handles or long tapping, they may be swiped to the left to be removed, or hidden by the visibility icon.
	
* Metadata is obtained and parsed from Worldview
    Worldview's metadata may be found in the wv.json file, and contains more metadata on the layers and categories compared to
    the [WMTSCapabilities.xml](http://map1.vis.earthdata.nasa.gov/wmts-webmerc/1.0.0/WMTSCapabilities.xml) file made available to developers who want to use GIBS's REST endpoints.
    Worldview is licensed under [NASA's open source license] (https://worldview.earthdata.nasa.gov/pages/license.html), I believe this is an appropriate use of the metadata.

## Screenshots
<img src="art/map.png" width="33%" />
<img src="art/picker.png" width="33%" />
<img src="art/colormaps.png" width="33%" />
	
## Libraries
* [Google Maps API for Android](https://developers.google.com/maps/android/)
* [RxJava](https://github.com/ReactiveX/RxJava) and [RxAndroid](https://github.com/ReactiveX/RxAndroid)
* [Retrofit 2](http://square.github.io/retrofit/)
* [Simple XML](http://simple.sourceforge.net/)
* [Gson](https://github.com/google/gson)
* [OkHttp3](http://square.github.io/okhttp/)
* [Espresso](https://google.github.io/android-testing-support-library/docs/espresso/index.html)
* [Mockito](http://mockito.org/)

## Future development
* Improve the MVP structure to simplify the WorldPresenter (in progress)
* Allow searching of layers within the layer picker feature
* Allow Colormaps to be touchable to reveal information based on where a tap occur
* Seperate the utility layers, such as Coastlines, Reference Features and Labels
* Integrate [NASA EONET](http://eonet.sci.gsfc.nasa.gov/) to allow easier discovery of natural events
* Allow generation of GIF animations and pictures of the shown satellite data

## Requirements
This project uses API Level 24, Google Play Services for Google Maps, and an API key for Google Maps. Replace the API key value with your own API key. Android N features are not used yet, but some Java 8 features are in use (effectively final variables, lambdas) and more may be used in the future.

## Helpful Tutorials and samples I used:
* [RecyclerView drag and swipe tutorial](https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.hhn9yujya)
* [TODO-MVP samples](https://github.com/googlesamples/android-architecture/tree/todo-mvp)
* [Event Bus with RxJava tutorial](http://blog.kaush.co/2014/12/24/implementing-an-event-bus-with-rxjava-rxbus/)
* [Android MVP Unit Testing](http://verybadalloc.com/android/adding-unit-tests-to-MVP-project.html)
* [Testing RecyclerView with Espresso](https://spin.atomicobject.com/2016/04/15/espresso-testing-recyclerviews/)
* [RxJava Android Samples](https://github.com/kaushikgopal/RxJava-Android-Samples)
* [Google I/O app repository](https://github.com/google/iosched)
* [GIBS API for Developers](https://wiki.earthdata.nasa.gov/display/GIBS/GIBS+API+for+Developers)

## Acknowledgements
We acknowledge the use of imagery provided by services from the Global Imagery Browse Services (GIBS), operated by the NASA/GSFC/Earth Science Data and Information System (ESDIS, [https://earthdata.nasa.gov](https://earthdata.nasa.gov)) with funding provided by NASA/HQ.