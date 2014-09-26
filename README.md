=====================================================================
======== Facebook-App: Android Facebook Platform Assignment =========
=====================================================================

Notice:
1. This project is a assignment of Facebook Partner Engineer interview.
   Every one are forbidden to use it for other purpose.

2. There may be a bug with Facebook-sdk-3.18:
   Bug-Descripton: 
   Android Facebook SDK UserSettingsFragment crashes when exit before the profile image updates finished. 

   Same issue can also found on:
   http://stackoverflow.com/questions/18295737/android-facebook-sdk-usersettingsfragment-crashes-when-paused-before-it-finishes 

   Solution:
   “modified the processImageResponse(String id, ImageResponse response) to check if the fragment is added”

   I don’t know whether this is really a bug or not, but it actually sometimes result my app crash. The solution can really get 
   me out of this crash.

 3. The project uses Facebook-sdk-3.18 sdk Graph API to query user's profile data and albums photos. 
    To use Facebook-sdk-3.18 sdk, developer must register this app on Facebook App Dashboard (https://developers.facebook.com/apps/)

 4. The project uses googl-play-services_lib to embed Google Maps and Google Cloud Messaging.
    To use googl-play-services_lib, developer also need to register this app on Google API Console to obtain an API accees key.

 5. This project is only a interview assignment. Because of limited time, the app’s UI isn’t well designed, and the function is not well tested 
    on different devices either. I make it works well on my Xiao-Mi 2S devise(OS v4.1.1), no crash. 

 6. All the function are works well except for GCM (Google Cloud Messaging). I have reviewed my code and carefully take reference to google documation.
    I don't found any issue in my code, but I have never got received a echo message I sent to GCM. Maybe it is related with China Great Firwall, which
    blocks the GCM service. I have no way to verify the GCM function. 

 7. There are three projects:
    (1) Facebook-sdk-3.18: This is the facebook sdk project. See how to get it: https://developers.facebook.com/docs/android/
    (2) googl-play-services_lib: This is a part of Android SDK extra package. See how to get it: https://developer.android.com/google/play-services/setup.html
    (3) AndroidAssignment: This is my own project, which reference Facebook-sdk-3.18 and googl-play-services_lib.

8. Import these three project into your Eclipse workspace. These projects require at least Android API-Level 11.

9. In order to build a real work APK, you should replace your Android debug.keystore with my own debug.keystore. Copy it to replace the one on /Users/username/.android/debug.keystore. Because I have registered this app on Facebook Dashboard and Google API Console both with my own debug.keystore. 

10. Activity Hierarchy of AndroidAssignment project:
   (1) MainActivity contains three fragement:
       <1> LoginFragment: show the login button page.
       <2> ProfileFragment: show current user's profile info. And it also a main pagen, that can navigate to Album page, Google map page and GCM page.
       <3> UserSettingsFragment: show the logout page. This is a standard component provide by Facebook SDK. You can click menu to open it.
   (2) AlbumActivity contains a grid view to show user's all albums.
   (3) PhotoActivity contains a grid view to show all the photos of a album.
   (4) GMSMapActivity contains a google standard MapFragment that will show the map.
   (5) GcmActivity take response to register and send/receive message to/from GCM.


 Author: Baochu,Wang
 Email: wbc.wbc@hotmail.com
 Phone: +86-13581664849
 Any question, please send eamil to me. 

 Thanks. 

