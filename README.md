Hand Gesture Recognition App
Yalun Qin (allenchin1990@gmail.com)
03/24/2014

Acknowledgments:

1. I got the idea of presampling from Simen Andresen's blog: 
http://simena86.github.io/blog/2013/08/12/hand-tracking-and-recognition-with-opencv/

2. In my code I used LibSVM on Android implemented by Kun Li:
https://github.com/cnbuff410/Libsvm-androidjni

3. In my code I used the file chooser library from here:
https://github.com/iPaulPro/aFileChooser

4. Part of the code implementing the package manager is from here:
http://blog.csdn.net/qinjuning/article/details/6867806

Installation:

1. Before importing the project into Eclipse, make sure you have already
installed and configured related tools, including Android SDK and OpenCV
library. I used Android 4.4.2 and OpenCV 2.4.8. 

2. Download and import aFileChooser into Eclipse. Import HandGestureApp project into
Eclipse and add aFileChooser to it(the setting is in Properties->Android).

3. Make sure OpenCV Manager is installed on your Android phone and then run
HandGestureApp project.

Usage:

1. The first mode you are in upon starting the app is background sampling
mode. You can lower the resolution using the option in the menu to make the app run faster.
After finished, just touch the screen and you will be in the second mode: hand
sampling mode. You need to cover the seven squares with your hand. This step
is the key to the performance and I suggest you bend your hand a little so that
the shadow could also be sampled. 

2. After presampling you can touch the screen again and go into next mode to
see the segmented hand. Touch again and you can see the extracted features,
along with three buttons. In this mode you can add new gestures to the gesture
database, train the SVM model or begin testing. I haven't implement deleting
gestures yet so be careful when you want to add a new one. All the data of the
gestures are stored in the ExternalStorageDirectory/MyDataSet folder. If you
don't know the location on your phone, just click "Add Gesture" button and try
to add a new gesture, the folder will be generated automatically in the
external storage directory. You can use the existing data set I collected which
is located in my project directory. You can click "Test" button to see the
realtime recognition performance of SVM classifier, but make sure the model has
already been generated, i.e., trained. 

3. This app can also let you quickly launch other apps using gestures. But first you need to
map your gestures to the apps by selecting "Map Apps" in the menu. The
selection is stored in the app so you don't need to map it every time you run
the app. Then you can test it by choosing the "App Test" in the menu.

A demo of the app can be found on YouTube: http://youtu.be/PF6hY-0VuN4
