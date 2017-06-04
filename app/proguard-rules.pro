# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\android_sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.dynamic.** { *; }
-keep class com.google.android.gms.dynamite.** { *; }
-keep class android.support.design.** { *; }
-keep class android.support.v7.widget.** { *; }
-dontwarn com.google.android.gms.maps.**

#Rules for RxJava found at https://github.com/artem-zinnatullin/RxJavaProGuardRules, currently empty

#Tell Proguard to keep OkHttp3 and retrofit
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

-keep class okio.** { *; }
-dontwarn okio.**

-keep class com.iamtechknow.terraview.** { *; }

#Some classes used don't exist in the Android runtime, get rid of warning messages
-keep class org.simpleframework.xml.Serializer { *; }
-keep class org.simpleframework.xml.stream.InputNode { *; }
-dontwarn org.simpleframework.xml.stream.**
