Overview
==================

Explore mobile API is Mentalab's open-source biosignal acquisition API for working with Mentalab Explore device. Amongst many things, it provides the following features:

* Real-time streaming of ExG, orientation and environmental data
* Explore device configuration

Requirements
==================

* Java Development Kit from here: <http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html>
* Android Studio with SDK bundle from this link: <https://developer.android.com/studio>
* Android device with at least Android Lollipop(OS version 5.1)


Quick installation
==================

To add the library to your project:

* In your project’s build.gradle add the following line
```
maven { url ‘https://jitpack.io’ }
```

* In your app’s build.gradle add the dependency
```
implementation 'com.github.salman2135:mentlabMobileApi:V__0.1'
```
* Add the following line in your anderoid manifest:
```
<uses-permission android:name="android.permission.BLUETOOTH" />
```
* Sync gradle and Mentlab API is ready to use!

A video demonstating above steps is also available: **TO DO**


[![SC2 Video](https://img.youtube.com/vi/--b-9HrKK6w/0.jpg)](http://www.youtube.com/watch?v=--b-9HrKK6w)

Please check troubleshooting section of this document in case any problem occurs.


Documentation
=============

For the full documentation of the API, please visit **TO DO**

Troubleshooting
===============

* If your phone is not recognized by Andoid Studio, make sure that USB debugging is turned on from your Android device.

You can also create a new issue in the GitHub repository.

Authors
=======

* [Salman Rahman](https://github.com/salman2135)
* [Florian Sesser](https://github.com/hacklschorsch)


License
=======
This project is licensed under the `MIT <https://github.com/salman2135/MLX_mobile_API/blob/main/LICENSE>` license. You can reach us at contact@mentalab.com.
