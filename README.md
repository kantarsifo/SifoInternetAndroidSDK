# For the developers of the Analytics SDK

## Create and publish the framework

### Create the release folder and zip file

When you open the project, you will see 2 modules (app and libmobiletagging) and 2 default build variants(debug and release) for these modules under the build variants part of Android Studio. 

After release build variant is selected for libmoduletagging module, release folder and release zip file can be created in root folder of the project by running the app. The created folder and file can be seen in Project folder view.

Release folder is named as "Kantar-Sifo-Mobile-Analytics-Android-SDK-${version}".

libmoduletagging library is a dependency for the sample app so everytime we run the app target,the source code for the library is compiled before the sample code and we can install a sample app which uses the latest version of our mobile analytics SDK.


## What we have in the release folder?

Release folder contains Documentation, Framework and Sample folders. You can also find licence.txt and release-notes.txt files inside release folder which are copied from libmobiletagging folder.

### Documentation

Documentation folder contains javadoc folder and Developers guide pdf document. Javadoc folder is created after the SDK aar file compiled and copied into Documentation folder via build.gradle script of sample app project. Develoepers guide pdf document is copied from libmobiletagging folder.

### Framework

Framework folder contains "kantar.sifo.mobile.analytics-${version}.aar" file. "kantar.sifo.mobile.analytics-${version}.aar" is copied into release folder just after "kantar.sifo.mobile.analytics-${version}.aar" is created inside libmobiletagging library.

### Sample

Framework folder contains "kantar.sifo.mobile.analytics.sample-${version}.apk" file and source code of sample project. "kantar.sifo.mobile.analytics.sample-${version}.apk"is copied into release folder just after it is created. Sample source code is copied from the sample folder in the root folder of the project.
