# Hand Gesture Recognition App

Yalun Qin (allenchin1990@gmail.com)

03/24/2014

## Acknowledgments:

1. The idea of presampling is from Simen Andresen's [blog](http://simena86.github.io/blog/2013/08/12/hand-tracking-and-recognition-with-opencv/) 
2. Used LibSVM on Android implemented by Kun Li. [link](https://github.com/cnbuff410/Libsvm-androidjni)
3. Used [aFileChooser library](https://github.com/iPaulPro/aFileChooser) to implement the file chooser.
4. To implement mapping human gestures to Android apps, [this article](http://blog.csdn.net/qinjuning/article/details/6867806) is quite helpful.

**This is the new Android Studio project! If you miss the old Eclipse ADT project, check out the other branch "eclipse_adt_old"**

**@raquezha also made great efforts in making the original project work with Android Studio. Check out his project https://github.com/raquezha/HandGestureApp too. Thanks for his work!** 

## Installation:

1. Before importing the project into Android Studio (mine is the 1.5 version), make sure you have already installed and configured related tools, including Android SDK, Android NDK. In this project Android 4.4.2 and OpenCV 2.4.9 are used. **I have already imported the OpenCV for Android Library and its native code so you don't have to download or import it yourself ^_^**. 
You can download and install NDK from "Project Structure" -> "SDK Location" -> "Download Android NDK", or download from the website(http://developer.android.com/ndk/downloads/index.html) and follow the instructions to install(make sure you fill in the NDK path in "Project Structure"/"SDK Location"/"Android NDK location"). Then **be sure to follow this post (http://kn-gloryo.github.io/Build_NDK_AndroidStudio_detail/, [video](https://www.youtube.com/watch?v=RmPuwdxR1qs)) to configure NDK tools**. This will make your life with NDK much easier! 

2. Import the project into Android Studio. Build the jni libraries libHandGestureApp.so and libsignal.so by right click on app/sr c/main/jni and choose NDK -> ndk build. This will call the ndk build tool and generate those two jni libraries in app/src/main/libs. This must be done if you change the c++ source files in the jni folder. 

3. Make sure OpenCV Manager has been installed on your Android phone and then build and run the project!

## Usage:

1. The first mode you are in upon starting the app is background sampling mode. You can lower the resolution using the option in the menu to make the app run faster. After finished, just touch the screen and you will be in the second mode: hand sampling mode. You need to cover the seven squares with your hand. This step is crucial to the performance and I suggest you bend your hand a little so that the shadow could also be sampled. 

2. After presampling you can touch the screen again and go into next mode to see the segmented hand. Touch again and you can see the extracted features, along with three buttons. In this mode you can add new gestures to the gesture database, train the SVM model or begin testing. The deletion of gestures has not been implemented yet so be careful when you want to add a new one. All the data of the gestures are stored in the ExternalStorageDirectory/MyDataSet folder. If you don't know the location on your phone, just click "Add Gesture" button and try to add a new gesture, the folder will be generated automatically in the external storage directory. You can use the existing data set which is located in the project directory. You can click "Test" button to see the realtime recognition performance of SVM classifier, but make sure the model has already been generated, i.e., trained. 

3. To add a new gesture to the training set, simply make a static gesture before the camera and click "Add Gesture" button. If you want to increase the number of a gesture that is already in the training set, you need to click "Data Collection" in the menu and choose the image representing the gesture, and then click "Add Gesture" button. After you click that button, the app will start gathering data immediately for a certain number of frames(currently the number is set to be 10). Finally a dialog will pop up asking you wheather to save those data or not.

4. This app can also let you quickly launch other apps using gestures. But first you need to map your gestures to the apps by selecting "Map Apps" in the menu. The selection is stored in the app so you don't need to map it every time you run the app. Then you can test it by choosing the "App Test" in the menu.

A demo of the app can be found on YouTube: http://youtu.be/PF6hY-0VuN4

Project page: http://eaglesky.github.io/2015/12/26/HandGestureRecognition/ 

If you find any problem about the code, **feel free to create an issue to report it**. Also feel free to refactor the code and send pull requests to me. It would be great if we could improve this project together!

