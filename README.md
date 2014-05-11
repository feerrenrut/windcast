windcast
========

An Android app to help visualise real-time wind data collected from weather stations around
Australia. Information is collected by BOM (Bureau of Meteorology, http://www.bom.gov.au).


Requirements
============

Android Studio 0.5.8
See docs/SDK-installedPackages.PNG for sdk versions required.

Setup Release Signing:
 on windows define an environment variable: GRADLE_USER_HOME pointing to a directory
 containing a file: gradle.properties
 this should have the following defined:
 RELEASE_STORE_FILE={path to your keystore (using forward slashes and relative to app/build.gradle)}
 RELEASE_STORE_PASSWORD=*****
 RELEASE_KEY_ALIAS=*****
 RELEASE_KEY_PASSWORD=*****
 see: http://stackoverflow.com/a/21020469/346188

Other dependencies are retrieved via gradle
