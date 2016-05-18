# Worldview Mobile
Worldview Mobile is a REST client for NASA's [Global Imagery Browse Services](https://earthdata.nasa.gov/gibs) (GIBS).
GIBS is a public API providing access to satellite data and imagery from the EOS (Earth Observing System) and A-Train satellites.
While this application is not directly related to NASA's [Worldview](https://worldview.earthdata.nasa.gov),
I worked on improvements on the project in an internship in NASA's Goddard Space Flight Center and created this based on the idea a better interface could be made for mobile devices.

# Decisions I made (that would go to a decisions.md file)
- Google Maps API for Android is used to load and display tiles
    Worldview uses OpenLayers 3, which is a Javascript library. I could have done the same if I used a WebView, but I want this to be a native Android application.

- RxJava is used to create an event bus
	Currently the ViewPager which accepts fragments to "page" through them in tabs does not allow communication through each of these fragments. I am using a tabbed interface to allow the user to select Worldview layers and their categories displayed in a RecyclerView to display a stack of layers on the map. 
	The app needs a way to determine what category is selected to display the corresponding measurements in the next tab, and finally the revelant layers in the final tab. This is handled by creating an event bus in RxJava and allowing the activity and its fragments to subscribe 
	to events that appear in the bus as they are posted when a RecyclerView item is tapped. 
	
- BottomBar is used to help implement the UI
    To make the user interface more intuitive, I decided to implement common actions with bottom navigation instead of displaying them in a navigation drawer.
    Bottom navigation is actually used for quick navigation between top-level views, thus I am using this library to suit my needs as opposed to following Google's Material Design guidelines.

- Metadata is obtained and parsed from Worldview
    Worldview's metadata may be found in the wv.json file, and contains more metadata on the layers and categories compared to
    the [WMTSCapabilities.xml](http://map1.vis.earthdata.nasa.gov/wmts-webmerc/1.0.0/WMTSCapabilities.xml) file made available to developers who want to use GIBS's REST endpoints.
    Worldview is licensed under [NASA's open source license] (https://worldview.earthdata.nasa.gov/pages/license.html), I believe this is an appropriate use of the metadata.

# Acknowledgements
We acknowledge the use of imagery provided by services from the Global Imagery Browse Services (GIBS), operated by the NASA/GSFC/Earth Science Data and Information System (ESDIS, [https://earthdata.nasa.gov](https://earthdata.nasa.gov)) with funding provided by NASA/HQ.