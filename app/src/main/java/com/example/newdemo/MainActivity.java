package com.example.newdemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.lang.Math;

import org.opencv.android.JavaCameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Rect;

import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import com.example.newdemo.AppInfo;
import com.example.newdemo.BrowseApplicationInfoAdapter;
import com.example.newdemo.R;
import com.ipaulpro.afilechooser.utils.FileUtils;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MainActivity extends Activity implements CvCameraViewListener2 {

    //Just for debugging
    private static final String TAG = "HandGestureApp";

    //Color Space used for hand segmentation
    private static final int COLOR_SPACE = Imgproc.COLOR_RGB2Lab;

    //Number of frames collected for each gesture in the training set
	private static final int GES_FRAME_MAX= 10;
	
	public final Object sync = new Object();
	
	//Mode that presamples hand colors
	public static final int SAMPLE_MODE = 0;
	
	//Mode that generates binary image
	public static final int DETECTION_MODE = 1; 
	
	//Mode that displays color image together with contours, fingertips,
	//defect points and so on.
	public static final int TRAIN_REC_MODE = 2;
	
	//Mode that presamples background colors
	public static final int BACKGROUND_MODE = 3;
	
	//Mode that is started when user clicks the 'Add Gesture' button.
	public static final int ADD_MODE = 4;    
	
	//Mode that is started when user clicks the 'Test' button.
	public static final int TEST_MODE = 5;    
	
	//Mode that is started when user clicks 'App Test' in the menu.
	public static final int APP_TEST_MODE = 6;  
	
	//Mode that is started when user clicks 'Data Collection' in the menu.
	public static final int DATA_COLLECTION_MODE = 0; 
	
	//Mode that is started when user clicks 'Map Apps' in the menu.
	public static final int MAP_APPS_MODE = 1;   
	
	//Number of frames used for prediction
	private static final int FRAME_BUFFER_NUM = 1; 
	
	//Frame interval between two launching events
	private static final int APP_TEST_DELAY_NUM = 10;
	
	private boolean isPictureSaved = false;
	private int appTestFrameCount = 0;
	
	private int testFrameCount = 0;
	private float[][] values = new float[FRAME_BUFFER_NUM][];
	private int[][] indices = new int[FRAME_BUFFER_NUM][];
	
	// onActivityResult request
	private static final int REQUEST_CODE = 6384; 
	
	private static final int REQUEST_SELECTED_APP = 1111;
	
	private String diagResult = null;
	private Handler mHandler = new Handler();
	private static final String DATASET_NAME = "/train_data.txt";
	
	private String storeFolderName = null;
	private File storeFolder = null;
	private FileWriter fw = null;

    //Stores the mapping results from gesture labels to app intents
	private HashMap<Integer, Intent> table = new HashMap<Integer, Intent>();
	
	   
    private List<AppInfo> mlistAppInfo = null;
	private MyCameraView mOpenCvCameraView;
	private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
  
    
	private List<android.hardware.Camera.Size> mResolutionList;
	
	//Initial mode is BACKGROUND_MODE to presample the colors of the hand
	private int mode = BACKGROUND_MODE;
	
	private int chooserMode = DATA_COLLECTION_MODE;
	
	private static final int SAMPLE_NUM = 7;
	
	
	private Point[][] samplePoints = null;
	private double[][] avgColor = null;
	private double[][] avgBackColor = null;
	
	private double[] channelsPixel = new double[4];
	private ArrayList<ArrayList<Double>> averChans = new ArrayList<ArrayList<Double>>();
	
	private double[][] cLower = new double[SAMPLE_NUM][3];
	private double[][] cUpper = new double[SAMPLE_NUM][3];
	private double[][] cBackLower = new double[SAMPLE_NUM][3];
	private double[][] cBackUpper = new double[SAMPLE_NUM][3];
	
	private Scalar lowerBound = new Scalar(0, 0, 0);
	private Scalar upperBound = new Scalar(0, 0, 0);
	private int squareLen;
	
	private Mat sampleColorMat = null;
	private List<Mat> sampleColorMats = null;
	
	private Mat[] sampleMats = null ;
	
	private Mat rgbaMat = null;
	
	private Mat rgbMat = null;
	private Mat bgrMat = null;
	
	
	private Mat interMat = null;

	private Mat binMat = null;
	private Mat binTmpMat = null;
	private Mat binTmpMat2 = null;
	private Mat binTmpMat0 = null;
	private Mat binTmpMat3 = null;
	
	private Mat tmpMat = null;
	private Mat backMat = null;
	private Mat difMat = null;
	private Mat binDifMat = null;
	
    
    private Scalar               mColorsRGB[] = null;
    
    //Stores all the information about the hand
	private HandGesture hg = null;
	
	private int imgNum;
	private int gesFrameCount;
	private int curLabel = 0;
	private int selectedLabel = -2;
	private int curMaxLabel = 0;
	private int selectedMappedLabel = -2;
	
	//Stores string representation of features to be written to train_data.txt
	private ArrayList<String> feaStrs = new ArrayList<String>();
	
	File sdCardDir = Environment.getExternalStorageDirectory();
	File sdFile = new File(sdCardDir, "AppMap.txt"); 
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch(status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i("Android Tutorial", "OpenCV loaded successfully");
				
				System.loadLibrary("HandGestureApp");
				
				  try {
			            System.loadLibrary("signal");
			        } catch (UnsatisfiedLinkError ule) {
			            Log.e(TAG, "Hey, could not load native library signal");
			        }
				  
				mOpenCvCameraView.enableView();
				
				 mOpenCvCameraView.setOnTouchListener(new OnTouchListener() {
					 
					 //Called when user touch the view screen
					 //Mode flow: BACKGROUND_MODE --> SAMPLE_MODE --> DETECTION_MODE <--> TRAIN_REC_MODE
	    	    	    public boolean onTouch(View v, MotionEvent event) {
	    	    	        // ... Respond to touch events 
	    	    	    	 int action = MotionEventCompat.getActionMasked(event);
	    	    	         
	    	    	    	    switch(action) {
	    	    	    	        case (MotionEvent.ACTION_DOWN) :
	    	    	    	            Log.d(TAG,"Action was DOWN");
	    	    	    	            String toastStr = null;
	    	    	    	            if (mode == SAMPLE_MODE) {
	    	    	    	            	mode = DETECTION_MODE;
	    	    	    	            	toastStr = "Sampling Finished!";
	    	    	    	            } else if (mode == DETECTION_MODE) {
	    	    	    	            	mode = TRAIN_REC_MODE;
	    	    	    	         
	    	    	    	            	((Button)findViewById(R.id.AddBtn)).setVisibility(View.VISIBLE);
	    	    	    	            	((Button)findViewById(R.id.TrainBtn)).setVisibility(View.VISIBLE);
	    	    	    	            	((Button)findViewById(R.id.TestBtn)).setVisibility(View.VISIBLE);
	    	    	    	            	toastStr = "Binary Display Finished!";
	    	    	    	            	
	    	    	    	            	preTrain();
	    	    	    	            	
	    	    	    	            } else if (mode == TRAIN_REC_MODE){
	    	    	    	            	mode = DETECTION_MODE;
	    	    	    	            	((Button)findViewById(R.id.AddBtn)).setVisibility(View.INVISIBLE);
	    	    	    	            	((Button)findViewById(R.id.TrainBtn)).setVisibility(View.INVISIBLE);
	    	    	    	            	((Button)findViewById(R.id.TestBtn)).setVisibility(View.INVISIBLE);
	    	    	    	            	
	    	    	    	            	toastStr = "train finished!";
	    	    	    	            } else if (mode == BACKGROUND_MODE) {
	    	    	    	            	toastStr = "First background sampled!";
	    	    	    	            	rgbaMat.copyTo(backMat);
	    	    	    	            	mode = SAMPLE_MODE;
	    	    	    	            }
	    	    	    	            
	    	    	    	        	Toast.makeText(getApplicationContext(), toastStr, Toast.LENGTH_LONG).show();
	    	    	    	            return false;
	    	    	    	        case (MotionEvent.ACTION_MOVE) :
	    	    	    	            Log.d(TAG,"Action was MOVE");
	    	    	    	            return true;
	    	    	    	        case (MotionEvent.ACTION_UP) :
	    	    	    	            Log.d(TAG,"Action was UP");
	    	    	    	            return true;
	    	    	    	        case (MotionEvent.ACTION_CANCEL) :
	    	    	    	            Log.d(TAG,"Action was CANCEL");
	    	    	    	            return true;
	    	    	    	        case (MotionEvent.ACTION_OUTSIDE) :
	    	    	    	            Log.d(TAG,"Movement occurred outside bounds " +
	    	    	    	                    "of current screen element");
	    	    	    	            return true;      
	    	    	    	        default : 
	    	    	    	            return true;
	    	    	    	    }      
	    	    	    	    
	    	    	        
	    	    	    }
	    	     });
				 
			} break;
			default: {
				super.onManagerConnected(status);
				
			}break;
			}
		}
	};
	
	 // svm native
    private native int trainClassifierNative(String trainingFile, int kernelType,
    		int cost, float gamma, int isProb, String modelFile);
    private native int doClassificationNative(float values[][], int indices[][],
    		int isProb, String modelFile, int labels[], double probs[]);
   
    //SVM training which outputs a file named as "model" in MyDataSet
    private void train() {
    	// Svm training
    	int kernelType = 2; // Radial basis function
    	int cost = 4; // Cost
    	int isProb = 0;
    	float gamma = 0.001f; // Gamma
    	String trainingFileLoc = storeFolderName+DATASET_NAME;
    	String modelFileLoc = storeFolderName+"/model";
    	Log.i("Store Path", modelFileLoc);
    	
    	if (trainClassifierNative(trainingFileLoc, kernelType, cost, gamma, isProb,
    			modelFileLoc) == -1) {
    		Log.d(TAG, "training err");
    		finish();
    	}
    	Toast.makeText(this, "Training is done", 2000).show();
    }
    
	public void initLabel() {
		       
		File file[] = storeFolder.listFiles();
	
		int maxLabel = 0;
		for (int i=0; i < file.length; i++)
		{
		 
			String fullName = file[i].getName();
			
			final int dotId = fullName.lastIndexOf('.');
			if (dotId > 0) {
				String name = fullName.substring(0, dotId);
				String extName = fullName.substring(dotId+1);
				if (extName.equals("jpg")) {
					int curName = Integer.valueOf(name);
					if (curName > maxLabel)
						maxLabel = curName;
				}
				
			}
		}
		
		curLabel = maxLabel;
		curMaxLabel = curLabel;
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);
		setContentView(R.layout.activity_main);
		
		try{
	        FileInputStream fis = new FileInputStream(sdFile);
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        while(true)
	        {
	             try{
	            
	                   int key = ois.readInt();
	                   String value =(String) ois.readObject();

	                   Intent intent = Intent.parseUri(value, 0);
	                   table.put(key, intent);
	             }
	             catch(IOException e)
	             {
	                  break;
	             }
	        }  
	        ois.close();
	        Log.e("ReadFile","read succeeded......");
        }catch(Exception ex)
        {
        	Log.e("ReadFile","read ended......");
        	
        }
		
		mOpenCvCameraView = (MyCameraView) findViewById(R.id.HandGestureApp);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		samplePoints = new Point[SAMPLE_NUM][2];
		for (int i = 0; i < SAMPLE_NUM; i++)
		{
			for (int j = 0; j < 2; j++)
			{
				samplePoints[i][j] = new Point();
			}
		}
		
		avgColor = new double[SAMPLE_NUM][3];
		avgBackColor = new double[SAMPLE_NUM][3];
		
		for (int i = 0; i < 3; i++)
			averChans.add(new ArrayList<Double>());
		
		//HLS
		//initCLowerUpper(7, 7, 80, 80, 80, 80);
		
		//RGB
		//initCLowerUpper(30, 30, 30, 30, 30, 30);
		
		//HSV
		//initCLowerUpper(15, 15, 50, 50, 50, 50);
		//initCBackLowerUpper(5, 5, 80, 80, 100, 100);
		
		//Ycrcb
	//	initCLowerUpper(40, 40, 10, 10, 10, 10);
		
		//Lab
		initCLowerUpper(50, 50, 10, 10, 10, 10);
	    initCBackLowerUpper(50, 50, 3, 3, 3, 3);
		
		SharedPreferences numbers = getSharedPreferences("Numbers", 0);
        imgNum = numbers.getInt("imgNum", 0);
		
        
        
        initOpenCV();
        
        Log.i(TAG, "Created!");
	}

	public void initOpenCV() {
		
	}
	
	//Things triggered by clicking any items in the menu start here
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		
	    switch (item.getItemId()) {
	        case R.id.action_save:
	        	isPictureSaved = true;
	          //  savePicture();
	            return true;
	        case R.id.data_collection:
	        	callDataCollection();
	        	return true;
	        case R.id.map_apps:
	        	callMapApps();
	        	return true;
	        case R.id.app_test:
	        	if (mode == APP_TEST_MODE) {
	        		mode = TRAIN_REC_MODE;
	        		Toast.makeText(getApplicationContext(), "App testing ends!", Toast.LENGTH_LONG).show();
	        	} else {
	        		mode = APP_TEST_MODE;
	        		Toast.makeText(getApplicationContext(), "App testing begins!", Toast.LENGTH_LONG).show();
	        		appTestFrameCount = 0;
	        	}
	        	return true;
	      //  default:
	        //    return super.onOptionsItemSelected(item);
	    }
	    
	    int groupId = item.getGroupId();
	    
	    if (item.getGroupId() == 2) {
	    	 int id = item.getItemId();
	            Camera.Size resolution = mResolutionList.get(id);
	            mOpenCvCameraView.setResolution(resolution);
	            resolution = mOpenCvCameraView.getResolution();
	            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
	            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
	            
	            return true;
	    }
	    
	    return super.onOptionsItemSelected(item);
	}
	
	public void restoreUI() {
		
		setContentView(R.layout.activity_main);
		
		mOpenCvCameraView = (MyCameraView) findViewById(R.id.HandGestureApp);
		mOpenCvCameraView.enableView();
		
		 mOpenCvCameraView.setOnTouchListener(new OnTouchListener() {
	    	    public boolean onTouch(View v, MotionEvent event) {
	    	        // ... Respond to touch events 
	    	    	 int action = MotionEventCompat.getActionMasked(event);
	    	         
	    	    	    switch(action) {
	    	    	        case (MotionEvent.ACTION_DOWN) :
	    	    	            Log.d(TAG,"Action was DOWN");
	    	    	            String toastStr = null;
	    	    	            if (mode == SAMPLE_MODE) {
	    	    	            	mode = DETECTION_MODE;
	    	    	            	toastStr = "Sampling Finished!";
	    	    	            } else if (mode == DETECTION_MODE) {
	    	    	            	mode = TRAIN_REC_MODE;
	    	    	            	((Button)findViewById(R.id.AddBtn)).setVisibility(View.VISIBLE);
	    	    	            	((Button)findViewById(R.id.TrainBtn)).setVisibility(View.VISIBLE);
	    	    	            	((Button)findViewById(R.id.TestBtn)).setVisibility(View.VISIBLE);
	    	    	            	toastStr = "Binary Display Finished!";
	    	    	            	
	    	    	            	preTrain();
	    	    	            	
	    	    	            } else if (mode == TRAIN_REC_MODE){
	    	    	            	mode = DETECTION_MODE;
	    	    	            	((Button)findViewById(R.id.AddBtn)).setVisibility(View.INVISIBLE);
	    	    	            	((Button)findViewById(R.id.TrainBtn)).setVisibility(View.INVISIBLE);
	    	    	            	((Button)findViewById(R.id.TestBtn)).setVisibility(View.INVISIBLE);
	    	    	            	
	    	    	            	toastStr = "train finished!";
	    	    	            } else if (mode == BACKGROUND_MODE) {
	    	    	            	toastStr = "First background sampled!";
	    	    	            	rgbaMat.copyTo(backMat);
	    	    	            	mode = SAMPLE_MODE;
	    	    	            }
	    	    	            
	    	    	        	Toast.makeText(getApplicationContext(), toastStr, Toast.LENGTH_LONG).show();
	    	    	            return false;
	    	    	        case (MotionEvent.ACTION_MOVE) :
	    	    	            Log.d(TAG,"Action was MOVE");
	    	    	            return true;
	    	    	        case (MotionEvent.ACTION_UP) :
	    	    	            Log.d(TAG,"Action was UP");
	    	    	            return true;
	    	    	        case (MotionEvent.ACTION_CANCEL) :
	    	    	            Log.d(TAG,"Action was CANCEL");
	    	    	            return true;
	    	    	        case (MotionEvent.ACTION_OUTSIDE) :
	    	    	            Log.d(TAG,"Movement occurred outside bounds " +
	    	    	                    "of current screen element");
	    	    	            return true;      
	    	    	        default : 
	    	    	            return true;
	    	    	    }      
	    	    	    
	    	        
	    	    }
	     });
		 
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		((Button)findViewById(R.id.AddBtn)).setVisibility(View.VISIBLE);
    	((Button)findViewById(R.id.TrainBtn)).setVisibility(View.VISIBLE);
    	((Button)findViewById(R.id.TestBtn)).setVisibility(View.VISIBLE);
    	
    	
	}
	
	public void showDialogBeforeAdd(String title,String message){
		Log.i("Show Dialog", "Entered");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(  
                  this);  
             // set title  
             alertDialogBuilder.setTitle(title);  
             // set dialog message  
             alertDialogBuilder  
                  .setMessage(message)  
                  .setCancelable(false)  
                  .setPositiveButton("Yes",new DialogInterface.OnClickListener() {  
                       public void onClick(DialogInterface dialog,int id) {  
                           
                            doAddNewGesture();
                            
                            synchronized(sync) {
                            	sync.notify();
                            }
                            
                            dialog.cancel();  
                            
                           
                       }  
                   })  
                  .setNegativeButton("No",new DialogInterface.OnClickListener() {  
                       public void onClick(DialogInterface dialog,int id) {  
                            // if this button is clicked, just close  
                            // the dialog box and do nothing 
                    	   
                    	   synchronized(sync) {
                               sync.notify();
                               }
                    	   
                            dialog.cancel(); 
                            
                           
                       }  
                  });  
                  // create alert dialog  
                  AlertDialog alertDialog = alertDialogBuilder.create();  
                  // show it  
                  alertDialog.show();  
   }  
	
	public void showDialog(final Context v, String title,String message, 
			String posStr, String negStr, String neuStr){
	
		
		diagResult = null;
		
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(  
                  v);  
             // set title  
             alertDialogBuilder.setTitle(title);  
             // set dialog message  
             alertDialogBuilder  
                  .setMessage(message)  
                  .setCancelable(false)  
                  .setPositiveButton(posStr,new DialogInterface.OnClickListener() {  
                       public void onClick(DialogInterface dialog,int id) {  
                           
                           
                            diagResult = "Positive";
                            
                            
                            Toast.makeText(getApplicationContext(), "Add more to Gesture "
         							+ selectedLabel, Toast.LENGTH_SHORT).show();
                            
                            curLabel = selectedLabel - 1;
                            
                            
                            dialog.cancel();  
                            
                           
                       }  
                   })  
                  .setNegativeButton(negStr,new DialogInterface.OnClickListener() {  
                       public void onClick(DialogInterface dialog,int id) {  
                            // if this button is clicked, just close  
                            // the dialog box and do nothing 
                    	   
                    	  
                    	    diagResult = "Negative"; 
                    	    
                    	    doDeleteGesture(selectedLabel);
                    	    
                    	    Toast.makeText(getApplicationContext(), "Gesture "
         							+ selectedLabel + " is deleted", Toast.LENGTH_SHORT).show();
                    	            	             	    
                    	    curLabel = selectedLabel - 1;
                            dialog.cancel(); 
                            
                           
                       }  
                  });  
             
             if (neuStr != null) {
             alertDialogBuilder.setNeutralButton(neuStr, new DialogInterface.OnClickListener() {  
                 public void onClick(DialogInterface dialog,int id) {  
                     
                     
                     diagResult = "Neutral";
                     
                     Toast.makeText(getApplicationContext(), "Canceled"
  							, Toast.LENGTH_SHORT).show();
                     
                     selectedLabel = -2;
                     dialog.cancel();  
                     
                    
                }  
             })  ;
             }
                  // create alert dialog  
                  AlertDialog alertDialog = alertDialogBuilder.create();  
                  // show it  
                  alertDialog.show();  
   }  
	
	/* Checks if external storage is available for read and write */
 	public boolean isExternalStorageWritable() {
 	    String state = Environment.getExternalStorageState();
 	    if (Environment.MEDIA_MOUNTED.equals(state)) {
 	        return true;
 	    }
 	    return false;
 	}
 	
 	//Called when user clicks "Data Collection" in the menu
 	//Starts a file chooser which lets the user chooses whatever gesture data he want to add
 	//Or add a new one
 	public void callDataCollection() {
 		if (mode != TRAIN_REC_MODE) {
 			Toast.makeText(getApplicationContext(), "Please do it in training mode!", Toast.LENGTH_SHORT).show();
 		} else {
 			String dataPath;
 			if (storeFolder != null) {
 				dataPath = storeFolderName;
 			} else {
 				dataPath = Environment.getExternalStorageDirectory().toString();
 			}
 			
  			
 			selectedLabel = -2;
 			chooserMode = DATA_COLLECTION_MODE;
 			showChooser();
 			
 			
 		}
 	}
 	
 	//Called when user clicks "Map Apps" in the menu
 	//Starts a file chooser that let user choose the gesture and app he want to match
 	public void callMapApps() {
 		if (mode != TRAIN_REC_MODE) {
 			Toast.makeText(getApplicationContext(), "Please do it in training mode!", Toast.LENGTH_SHORT).show();
 		} else {
 			String dataPath;
 			if (storeFolder != null) {
 				dataPath = storeFolderName;
 			} else {
 				dataPath = Environment.getExternalStorageDirectory().toString();
 			}
 			
 	 			
 			selectedMappedLabel = -2;
 			chooserMode = MAP_APPS_MODE;
 			showChooser();
 			
 			
 		}
 	}
 	
 	 private void showChooser() {
         // Use the GET_CONTENT intent from the utility class
         Intent target = FileUtils.createGetContentIntent();
         // Create the chooser Intent
         Intent intent = Intent.createChooser(
                 target, getString(R.string.chooser_title));
         try {
             startActivityForResult(intent, REQUEST_CODE);
         } catch (ActivityNotFoundException e) {
             // The reason for the existence of aFileChooser
         }
     }

 	 
 	 //This function deals with the results returned by file chooser triggered
 	 //by clicking "Data Collection" and "Map Apps" in the menu
 	 //RequestCode indicates which activity the request correspond to
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case REQUEST_CODE: // Responding to the activity started by "Data Collection"
            	                // and "Map Apps"
           
                 if (resultCode == RESULT_OK) {
                     if (data != null) {
                         // Get the URI of the selected file
                         final Uri uri = data.getData();
                         Log.i(TAG, "Uri = " + uri.toString());
                         try {
                             // Get the file path from the URI
                             final String path = FileUtils.getPath(this, uri);
                          
                             int slashId = path.lastIndexOf('/');
                             int slashDot = path.lastIndexOf('.');
                             String selectedLabelStr = path.substring(slashId+1, slashDot);
                             
                             if (chooserMode == DATA_COLLECTION_MODE) {
	                             selectedLabel = Integer.valueOf(selectedLabelStr);
	                             
	                             if (selectedLabel != -2) {
	                  				showDialog(this, "Add or Delete", "Selected Label is " + 
	                  			selectedLabel + ",\nAdd to this gesture or delete it?", 
	                  			"Add", "Delete", "Cancel");
	                  			
	                  			} 
                             } else if (chooserMode == MAP_APPS_MODE) {
                            	 
                            	 //The label of choosen gesture to be matched
                            	 selectedMappedLabel = Integer.valueOf(selectedLabelStr);
                            	 
                            	 if (selectedMappedLabel != -2) {
                            		 callAppList();
                            	 }
                             }
                             
                         } catch (Exception e) {
                             Log.e("FileSelectorTestActivity", "File select error", e);
                         }
                     }
                 }
                 break;
                 
             case REQUEST_SELECTED_APP: //Responding to the activity started in callAppList()
            	 if (resultCode == RESULT_OK) {
            		 if (selectedMappedLabel != -2) {
            			 
            			 //Postion of selected app in the app list
            			 int position = data.getIntExtra("Position", -1);
            			 
            			 if (position != -1) {
            				 
            				 table.put(selectedMappedLabel, mlistAppInfo.get(position).getIntent());
            				 Log.i(TAG, "selected Label = " + selectedMappedLabel +
            						 "Position = " + position);
            			 }
            			 else {
            				 
            			 }
            			 
            		 }
            	 }
            	 break;
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 	
     //Start a new activity containing a list of callable apps
     public void callAppList() {
    	 	Log.i(TAG, "Call App List!");
 
    	 	
    	 	mlistAppInfo = new ArrayList<AppInfo>();  
	        queryAppInfo(); 
	        BrowseApplicationInfoAdapter browseAppAdapter = new BrowseApplicationInfoAdapter(  
	                this, mlistAppInfo);  
	        
    	 	Intent intent = new Intent(this, DisplayAppList.class);
    	 	
    	 	startActivityForResult(intent, REQUEST_SELECTED_APP);
     }
     
    
     
     public void queryAppInfo() {  
	        PackageManager pm = this.getPackageManager(); // ���PackageManager����  
	        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);  
	        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);  
	      
	        List<ResolveInfo> resolveInfos = pm  
	                .queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);  

	        Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));  
	        if (mlistAppInfo != null) {  
	            mlistAppInfo.clear();  
	            for (ResolveInfo reInfo : resolveInfos) {  
	                String activityName = reInfo.activityInfo.name;  
	                String pkgName = reInfo.activityInfo.packageName;   
	                String appLabel = (String) reInfo.loadLabel(pm);   
	                Drawable icon = reInfo.loadIcon(pm);   
	                
	                Intent launchIntent = new Intent();  
	                launchIntent.setComponent(new ComponentName(pkgName,  
	                        activityName));  
	               
	                AppInfo appInfo = new AppInfo();  
	                appInfo.setAppLabel(appLabel);  
	                appInfo.setPkgName(pkgName);  
	                appInfo.setAppIcon(icon);  
	                appInfo.setIntent(launchIntent);  
	                mlistAppInfo.add(appInfo); 
  

	            }  
	        }  
	    }  
     
     //Called when user clicks "Add Gesture" button
     //Prepare train_data.txt file and set the mode to be ADD_MODE
 	public void addNewGesture(View view) {
 	   		
 		if (mode == TRAIN_REC_MODE) {
 		if (storeFolder != null) {
 			File myFile = new File(storeFolderName + DATASET_NAME);
			
 			if (myFile.exists()) {
 				
 			} else {
 				try {
 				myFile.createNewFile();
 				} catch (Exception e) {
 					Toast.makeText(getApplicationContext(), "Failed to create dataset at "
 							+ myFile, Toast.LENGTH_SHORT).show();
 				}
 			}
 			
 			
			 try {
				 fw = new FileWriter(myFile, true);
				
				 
				 feaStrs.clear();
				
				 
				 if (selectedLabel == -2)
					 curLabel = curMaxLabel + 1;
				 else {
					 curLabel++;
					 selectedLabel = -2;
				 }
				 
				 
				 gesFrameCount = 0;
				 mode = ADD_MODE;
			     
				 
		    } catch (FileNotFoundException e) {
		        e.printStackTrace();
		        Log.i(TAG, "******* File not found. Did you" +
		                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
		    } catch (IOException e) {
		        e.printStackTrace();
		    }   
 		}
 		} else {
 			Toast.makeText(getApplicationContext(), "Please do it in TRAIN_REC mode"
					, Toast.LENGTH_SHORT).show();
 		}
 		
 	}
 	
 	//Write the strings of features to the file train_data.txt
 	//Save the screenshot of the gesture
 	public void doAddNewGesture() {
 		try {
 		for (int i = 0; i < feaStrs.size(); i++) {
			fw.write(feaStrs.get(i));
		}
 		
 		fw.close();
 		
 		} catch (Exception e) {
 			
 		}
 		
 		savePicture();
 		
 		if (curLabel > curMaxLabel) {
 			curMaxLabel = curLabel;
 		}
 		
		
 	}
 	
 	public void doDeleteGesture(int label) {
 		
 	}
 	
 	//All the trained gestures jpg files and SVM training model, train_data.txt
 	//are stored in ExternalStorageDirectory/MyDataSet
 	//If MyDataSet doesn't exist, then it will be created in this function
 	public void preTrain() { 
 		 if (!isExternalStorageWritable()) {
 			 Toast.makeText(getApplicationContext(), "External storage is not writable!", Toast.LENGTH_SHORT).show();
		 } else if (storeFolder == null) {
 			storeFolderName = Environment.getExternalStorageDirectory() + "/MyDataSet";
 			storeFolder = new File(storeFolderName);
 			boolean success = true;
 			if (!storeFolder.exists()) {
 			    success = storeFolder.mkdir();
 			}
 			if (success) {
 			    // Do something on success
 				
 				
 			} else {
 				Toast.makeText(getApplicationContext(), "Failed to create directory "+ storeFolderName, Toast.LENGTH_SHORT).show();
 				storeFolder = null;
 				storeFolderName = null;
 			}
 		}
 		 
 		 if (storeFolder != null) {
 			 initLabel();
 		 }
 		 
 		
 	}
 	
 	//Called when user clicks "Train" button
 	public void train(View view) {
 		train();
 	}
 	
 	//Called when user clicks "Test" button
 	public void test(View view) {
 		
 		if (mode == TRAIN_REC_MODE)
 			mode = TEST_MODE;
 		else if (mode == TEST_MODE) {
 			mode = TRAIN_REC_MODE;
 		}
 	}
 	
	boolean savePicture()
	{
		Mat img;
		
		
		
		if (((mode == BACKGROUND_MODE) || (mode == SAMPLE_MODE)
				|| (mode == TRAIN_REC_MODE)) || (mode == ADD_MODE) || 
				(mode == TEST_MODE)) {
			Imgproc.cvtColor(rgbaMat, bgrMat, Imgproc.COLOR_RGBA2BGR, 3);
			img = bgrMat;
		} else if (mode == DETECTION_MODE) {
			img = binMat;
		} else 
			img = null;
		
		if (img != null) {
			 if (!isExternalStorageWritable()) {
	 	//		 Toast.makeText(getApplicationContext(), "External storage is not writable!", Toast.LENGTH_SHORT).show();
				  return false;
			 }
	 	
			 File path;
			 String filename;
			 if (mode != ADD_MODE) {
				 path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				 filename = "image_" + imgNum + ".jpg";
			 } else {
				 path = storeFolder;
				 filename = curLabel + ".jpg";
			 }
			 
			 
			 imgNum++;
			 File file = new File(path, filename);
		
			 
			  
			  Boolean bool = false;
			  filename = file.toString();
			  
			
			  bool = Highgui.imwrite(filename, img);
		
			  if (bool == true) {
				//  Toast.makeText(getApplicationContext(), "Saved as " + filename, Toast.LENGTH_SHORT).show();
				  Log.d(TAG, "Succeed writing image to" + filename);
			  } else
			    Log.d(TAG, "Fail writing image to external storage");
			  
			  return bool;
		}
		
		return false;
	}
	
	//Just initialize boundaries of the first sample
	void initCLowerUpper(double cl1, double cu1, double cl2, double cu2, double cl3,
			double cu3)
	{
		cLower[0][0] = cl1;
		cUpper[0][0] = cu1;
		cLower[0][1] = cl2;
		cUpper[0][1] = cu2;
		cLower[0][2] = cl3;
		cUpper[0][2] = cu3;
	}
	
	void initCBackLowerUpper(double cl1, double cu1, double cl2, double cu2, double cl3,
			double cu3)
	{
		cBackLower[0][0] = cl1;
		cBackUpper[0][0] = cu1;
		cBackLower[0][1] = cl2;
		cBackUpper[0][1] = cu2;
		cBackLower[0][2] = cl3;
		cBackUpper[0][2] = cu3;
	}
	
	//Initialize menu and resolution list.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Camera.Size> resolutionItr = mResolutionList.listIterator();
        int idx = 0;
        while(resolutionItr.hasNext()) {
            Camera.Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
         }
        
        checkCameraParameters();
        
		return true;
	}

	 AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

		  @Override
		  public void onAutoFocus(boolean arg0, Camera arg1) {
		   // TODO Auto-generated method stub
			  int maxFocusAreas = mOpenCvCameraView.getMaxNumFocusAreas();
			  
			  Log.i("MaxFocusAreas", "" + maxFocusAreas);
		  }};
		  
	public void checkCameraParameters()
	{
		if (mOpenCvCameraView.isAutoWhiteBalanceLockSupported()) {
			if (mOpenCvCameraView.getAutoWhiteBalanceLock()) {
				Log.d("AutoWhiteBalanceLock", "Locked");
			} else {
				Log.d("AutoWhiteBalanceLock", "Not Locked");
				mOpenCvCameraView.setAutoWhiteBalanceLock(true);
				
				if (mOpenCvCameraView.getAutoWhiteBalanceLock()) {
					Log.d("AutoWhiteBalanceLockAfter", "Locked");
				}
			}
		} else {
			Log.d("AutoWhiteBalanceLock", "Not Supported");
		}

		
	}
	
	public void releaseCVMats() {
		releaseCVMat(sampleColorMat);
		sampleColorMat = null;
		
		if (sampleColorMats!=null) {
			for (int i = 0; i < sampleColorMats.size(); i++)
			{
				releaseCVMat(sampleColorMats.get(i));
			
			}
		}
		sampleColorMats = null;
		
		if (sampleMats != null) {
			for (int i = 0; i < sampleMats.length; i++)
			{
				releaseCVMat(sampleMats[i]);
			}
		}
		sampleMats = null;
		
		releaseCVMat(rgbMat);
		rgbMat = null;
		
		releaseCVMat(bgrMat);
		bgrMat = null;
		
		releaseCVMat(interMat);
		interMat = null;
		
		releaseCVMat(binMat);
		binMat = null;
		
		releaseCVMat(binTmpMat0);
		binTmpMat0 = null;
		
		releaseCVMat(binTmpMat3);
		binTmpMat3 = null;
		
		releaseCVMat(binTmpMat2);
		binTmpMat2 = null;
		
		releaseCVMat(tmpMat);
		tmpMat = null;
		
		releaseCVMat(backMat);
		backMat = null;
		
		releaseCVMat(difMat);
		difMat = null;
		
		releaseCVMat(binDifMat);
		binDifMat = null;
		
	}
	
	public void releaseCVMat(Mat img) {
		if (img != null)
			img.release();

	}
	
	
	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "On cameraview started!");
		
			
		if (sampleColorMat == null)
		sampleColorMat = new Mat();
		
	
		if (sampleColorMats == null)
		sampleColorMats = new ArrayList<Mat>();
		
		if (sampleMats == null) {
		sampleMats = new Mat[SAMPLE_NUM];
		for (int i = 0; i < SAMPLE_NUM; i++)
			sampleMats[i] = new Mat();
		}
		
		if (rgbMat == null)
		rgbMat = new Mat();
		
		if (bgrMat == null)
		bgrMat = new Mat();
		
		if (interMat == null)
		interMat = new Mat();
		
		if (binMat == null)
		binMat = new Mat();
		
		if (binTmpMat == null)
		binTmpMat = new Mat();
		
		if (binTmpMat2 == null)
		binTmpMat2 = new Mat();
		
		if (binTmpMat0 == null)
		binTmpMat0 = new Mat();
		
		if (binTmpMat3 == null)
		binTmpMat3 = new Mat();
		
		if (tmpMat == null)
		tmpMat = new Mat();
		
		if (backMat==null)
		backMat = new Mat();
		
		if (difMat == null)
		difMat = new Mat();
		
		if (binDifMat == null)
		binDifMat = new Mat();
		
		
		if (hg == null)
		hg = new HandGesture();
		
	    
        mColorsRGB = new Scalar[] { new Scalar(255, 0, 0, 255), new Scalar(0, 255, 0, 255), new Scalar(0, 0, 255, 255) };
        
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		Log.i(TAG, "On cameraview stopped!");
	//	releaseCVMats();
	}

	
	//Called when each frame data gets received
	//inputFrame contains the data for each frame
	//Mode flow: BACKGROUND_MODE --> SAMPLE_MODE --> DETECTION_MODE <--> TRAIN_REC_MODE
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		rgbaMat = inputFrame.rgba();
		
		
		Core.flip(rgbaMat, rgbaMat, 1);
		
		
		Imgproc.GaussianBlur(rgbaMat, rgbaMat, new Size(5,5), 5, 5);
		
		Imgproc.cvtColor(rgbaMat, rgbMat, Imgproc.COLOR_RGBA2RGB);
		
		//Convert original RGB colorspace to the colorspace indicated by COLR_SPACE
		Imgproc.cvtColor(rgbaMat, interMat, COLOR_SPACE);
		
		
		
		if (mode == SAMPLE_MODE) { //Second mode which presamples the colors of the hand  
		
			preSampleHand(rgbaMat);
			
		} else if (mode == DETECTION_MODE) { //Third mode which generates the binary image containing the
			                                 //segmented hand represented by white color
			produceBinImg(interMat, binMat);
			
			
			return binMat;
		
		
		} else if ((mode == TRAIN_REC_MODE)||(mode == ADD_MODE) 
		|| (mode == TEST_MODE) || (mode == APP_TEST_MODE)){
		
			produceBinImg(interMat, binMat);
			
			makeContours();
		
			
			
			String entry = hg.featureExtraction(rgbaMat, curLabel);
			
			//Collecting the frame data of a certain gesture and storing it in the file train_data.txt.
			//This mode stops when the number of frames processed equals GES_FRAME_MAX
			if (mode == ADD_MODE) {
				gesFrameCount++;
				Core.putText(rgbaMat, Integer.toString(gesFrameCount), new Point(10, 
				10), Core.FONT_HERSHEY_SIMPLEX, 0.6, Scalar.all(0));
				
				
				
				feaStrs.add(entry);
				
				if (gesFrameCount == GES_FRAME_MAX) {
					
					 Runnable runnableShowBeforeAdd = new Runnable() {
				            @Override
				            public void run() {                
				                {                    
				                	showDialogBeforeAdd("Add or not", "Add this new gesture labeled as "
											+ curLabel + "?");
				                }
				            }
				        };     
				        
					mHandler.post(runnableShowBeforeAdd);
					
					
					
					try {
						
						synchronized(sync) {
							sync.wait();
						}
					
					
					
					
					
					} catch (Exception e) {
						
					}
					
					
					
					mode = TRAIN_REC_MODE;
				}
			} else if ((mode == TEST_MODE)||(mode == APP_TEST_MODE)) {
				Double[] doubleValue = hg.features.toArray(new Double[hg.features.size()]);
				values[0] = new float[doubleValue.length];
				indices[0] = new int[doubleValue.length];
				
				for (int i = 0; i < doubleValue.length; i++)
				{
					values[0][i] = (float)(doubleValue[i]*1.0f);
					indices[0][i] = i+1;
				}
				
				int isProb = 0;
				
				
				String modelFile = storeFolderName + "/model";
				int[] returnedLabel = {0};
				double[] returnedProb = {0.0};
				
				//Predicted labels are stored in returnedLabel
				//Since currently prediction is made for each frame, only returnedLabel[0] is useful.
				int r = doClassificationNative(values, indices, isProb, modelFile, returnedLabel, returnedProb);
				
				if (r == 0) {
					
					if (mode == TEST_MODE)
					Core.putText(rgbaMat, Integer.toString(returnedLabel[0]), new Point(15, 
							15), Core.FONT_HERSHEY_SIMPLEX, 0.6, mColorsRGB[0]);
					else if (mode == APP_TEST_MODE) { //Launching other apps
						Core.putText(rgbaMat, Integer.toString(returnedLabel[0]), new Point(15, 
								15), Core.FONT_HERSHEY_SIMPLEX, 0.6, mColorsRGB[2]);
						
						if (returnedLabel[0] != 0) {
							
							if (appTestFrameCount == APP_TEST_DELAY_NUM) {
								
								//Call other apps according to the predicted label
								//This is done every APP_TEST_DELAY_NUM frames
								callAppByLabel(returnedLabel[0]);
							} else {
								appTestFrameCount++;
							}
							
							
						}
					}
				}
			}
			
	
		} else if (mode == BACKGROUND_MODE) { //First mode which presamples background colors
			preSampleBack(rgbaMat);
		} 
		
		if (isPictureSaved) {
			savePicture();
			isPictureSaved = false;
		}
		return rgbaMat;
	
	
	}
	
	void callAppByLabel(int label) {
		Intent intent = table.get(label);
		
		if (intent != null) {
			appTestFrameCount = 0;
			startActivity(intent);
		}
	}
	
	//Presampling hand colors.
	//Output is avgColor, which is essentially a 7 by 3 matrix storing the colors sampled by seven squares
	void preSampleHand(Mat img)
	{
		int cols = img.cols();
		int rows = img.rows();
		squareLen = rows/20;
		Scalar color = mColorsRGB[2];  //Blue Outline
		
		
		samplePoints[0][0].x = cols/2;
		samplePoints[0][0].y = rows/4;
		samplePoints[1][0].x = cols*5/12;
		samplePoints[1][0].y = rows*5/12;
		samplePoints[2][0].x = cols*7/12;
		samplePoints[2][0].y = rows*5/12;
		samplePoints[3][0].x = cols/2;
		samplePoints[3][0].y = rows*7/12;
		samplePoints[4][0].x = cols/1.5;
		samplePoints[4][0].y = rows*7/12;
		samplePoints[5][0].x = cols*4/9;
		samplePoints[5][0].y = rows*3/4;
		samplePoints[6][0].x = cols*5/9;
		samplePoints[6][0].y = rows*3/4;
		
		for (int i = 0; i < SAMPLE_NUM; i++)
		{
			samplePoints[i][1].x = samplePoints[i][0].x+squareLen;
			samplePoints[i][1].y = samplePoints[i][0].y+squareLen;
		}
		
		for (int i = 0; i < SAMPLE_NUM; i++)
		{
			Core.rectangle(img,  samplePoints[i][0], samplePoints[i][1], color, 1);
		}
		
		
		
		for (int i = 0; i < SAMPLE_NUM; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				avgColor[i][j] = (interMat.get((int)(samplePoints[i][0].y+squareLen/2), (int)(samplePoints[i][0].x+squareLen/2)))[j];
			}
		}
		
		
	}
	
	//Presampling background colors.
    //Output is avgBackColor, which is essentially a 7 by 3 matrix storing the colors sampled by seven squares
	void preSampleBack(Mat img)
	{
		int cols = img.cols();
		int rows = img.rows();
		squareLen = rows/20;
		Scalar color = mColorsRGB[2];  //Blue Outline
		
		
		
		samplePoints[0][0].x = cols/6;
		samplePoints[0][0].y = rows/3;
		samplePoints[1][0].x = cols/6;
		samplePoints[1][0].y = rows*2/3;
		samplePoints[2][0].x = cols/2;
		samplePoints[2][0].y = rows/6;
		samplePoints[3][0].x = cols/2;
		samplePoints[3][0].y = rows/2;
		samplePoints[4][0].x = cols/2;
		samplePoints[4][0].y = rows*5/6;
		samplePoints[5][0].x = cols*5/6;
		samplePoints[5][0].y = rows/3;
		samplePoints[6][0].x = cols*5/6;
		samplePoints[6][0].y = rows*2/3;
		
		for (int i = 0; i < SAMPLE_NUM; i++)
		{
			samplePoints[i][1].x = samplePoints[i][0].x+squareLen;
			samplePoints[i][1].y = samplePoints[i][0].y+squareLen;
		}
		
		for (int i = 0; i < SAMPLE_NUM; i++)
		{
			Core.rectangle(img,  samplePoints[i][0], samplePoints[i][1], color, 1);
		}
		
		
		
		for (int i = 0; i < SAMPLE_NUM; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				avgBackColor[i][j] = (interMat.get((int)(samplePoints[i][0].y+squareLen/2), (int)(samplePoints[i][0].x+squareLen/2)))[j];
			}
		}
		
	}
	
	
	
		
	void boundariesCorrection()
	{
		for (int i = 1; i < SAMPLE_NUM; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				cLower[i][j] = cLower[0][j];
				cUpper[i][j] = cUpper[0][j];
				
				cBackLower[i][j] = cBackLower[0][j];
				cBackUpper[i][j] = cBackUpper[0][j];
			}
		}
		
		for (int i = 0; i < SAMPLE_NUM; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				if (avgColor[i][j] - cLower[i][j] < 0)
					cLower[i][j] = avgColor[i][j];
				
				if (avgColor[i][j] + cUpper[i][j] > 255)
					cUpper[i][j] = 255 - avgColor[i][j];
				
				if (avgBackColor[i][j] - cBackLower[i][j] < 0)
					cBackLower[i][j] = avgBackColor[i][j];
				
				if (avgBackColor[i][j] + cBackUpper[i][j] > 255)
					cBackUpper[i][j] = 255 - avgBackColor[i][j];
			}
		}
	}
	
	
	
	void cropBinImg(Mat imgIn, Mat imgOut)
	{
		imgIn.copyTo(binTmpMat3);
		
		Rect boxRect = makeBoundingBox(binTmpMat3);
		Rect finalRect = null;
		
		if (boxRect!=null) {
		Mat roi = new Mat(imgIn, boxRect);
		int armMargin = 2;
		
		Point tl = boxRect.tl();
		Point br = boxRect.br();
		
		int colNum = imgIn.cols();
		int rowNum = imgIn.rows();
		
		int wristThresh = 10;
		
		List<Integer> countOnes = new ArrayList<Integer>();
		
		if (tl.x < armMargin) {
			double rowLimit = br.y;
			int localMinId = 0;
			for (int x = (int)tl.x; x < br.x; x++)
			{
				int curOnes = Core.countNonZero(roi.col(x));
				int lstTail = countOnes.size()-1;
				if (lstTail >= 0) {
					if (curOnes < countOnes.get(lstTail)) {
						localMinId = x;
					}
				}
				
				if (curOnes > (countOnes.get(localMinId) + wristThresh))
					break;
				
				countOnes.add(curOnes);
			}
			
			Rect newBoxRect = new Rect(new Point(localMinId, tl.y), br);
			roi = new Mat(imgIn, newBoxRect);
			
			Point newtl = newBoxRect.tl();
			Point newbr = newBoxRect.br();
			
			int y1 = (int)newBoxRect.tl().y;
			while (Core.countNonZero(roi.row(y1)) < 2) {
				y1++;
			}
			
			int y2 = (int)newBoxRect.br().y;
			while (Core.countNonZero(roi.row(y2)) < 2) {
				y2--;
			}
			finalRect = new Rect(new Point(newtl.x, y1), new Point(newbr.x, y2));
		} else if (br.y > rowNum - armMargin) {
			double rowLimit = br.y;
			
			
			
			int scanCount = 0;
			int scanLength = 8;
			int scanDelta = 8;
			int y;
			for (y = (int)br.y - 1; y > tl.y; y--)
			{
				int curOnes = Core.countNonZero((roi.row(y - (int)tl.y)));
				int lstTail = countOnes.size()-1;
				if (lstTail >= 0) {
					countOnes.add(curOnes);
					
					if (scanCount % scanLength == 0) {
						int curDelta = curOnes - countOnes.get(scanCount-5);
						if (curDelta > scanDelta)
							break;
					}
					
					
				} else
					countOnes.add(curOnes);
				
				scanCount++;
			}
			
			
			
			
			finalRect = new Rect(tl, new Point(br.x, y+scanLength));
			
		
		}
		

		if (finalRect!=null) {
			roi = new Mat(imgIn, finalRect);
			roi.copyTo(tmpMat);
			imgIn.copyTo(imgOut);
			imgOut.setTo(Scalar.all(0));
			roi = new Mat(imgOut, finalRect);
			tmpMat.copyTo(roi);
		}
		
		
		}
		
	}
	
	void adjustBoundingBox(Rect initRect, Mat img)
	{
		
	}
	
	//Generates binary image containing user's hand
	void produceBinImg(Mat imgIn, Mat imgOut)
	{
		int colNum = imgIn.cols();
		int rowNum = imgIn.rows();
		int boxExtension = 0;
		
		boundariesCorrection();
		
		produceBinHandImg(imgIn, binTmpMat);
	
		
		produceBinBackImg(imgIn, binTmpMat2);
	
		
		Core.bitwise_and(binTmpMat, binTmpMat2, binTmpMat);
		binTmpMat.copyTo(tmpMat);
		binTmpMat.copyTo(imgOut);
		
		Rect roiRect = makeBoundingBox(tmpMat);
		adjustBoundingBox(roiRect, binTmpMat);
		
		if (roiRect!=null) {
			roiRect.x = Math.max(0, roiRect.x - boxExtension);
			roiRect.y = Math.max(0, roiRect.y - boxExtension);
			roiRect.width = Math.min(roiRect.width+boxExtension, colNum);
			roiRect.height = Math.min(roiRect.height+boxExtension, rowNum);
			
			
			Mat roi1 = new Mat(binTmpMat, roiRect);
			Mat roi3 = new Mat(imgOut, roiRect);
			imgOut.setTo(Scalar.all(0));
			
			roi1.copyTo(roi3);
		
			Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));	
			Imgproc.dilate(roi3, roi3, element, new Point(-1, -1), 2);
			
			Imgproc.erode(roi3, roi3, element, new Point(-1, -1), 2);
			
			
		}
		
	//	cropBinImg(imgOut, imgOut);
		
	}
	
	//Generates binary image thresholded only by sampled hand colors
	void produceBinHandImg(Mat imgIn, Mat imgOut)
	{
		for (int i = 0; i < SAMPLE_NUM; i++)
		{
			lowerBound.set(new double[]{avgColor[i][0]-cLower[i][0], avgColor[i][1]-cLower[i][1],
					avgColor[i][2]-cLower[i][2]});
			upperBound.set(new double[]{avgColor[i][0]+cUpper[i][0], avgColor[i][1]+cUpper[i][1],
					avgColor[i][2]+cUpper[i][2]});
			
		
			
			
			Core.inRange(imgIn, lowerBound, upperBound, sampleMats[i]);
			
			
		}
		
		imgOut.release();
		sampleMats[0].copyTo(imgOut);
	
		
		for (int i = 1; i < SAMPLE_NUM; i++)
		{
			Core.add(imgOut, sampleMats[i], imgOut);
		}
		
	
		
		Imgproc.medianBlur(imgOut, imgOut, 3);
	}
	
	//Generates binary image thresholded only by sampled background colors
	void produceBinBackImg(Mat imgIn, Mat imgOut)
	{
		for (int i = 0; i < SAMPLE_NUM; i++)
		{
		
			
			lowerBound.set(new double[]{avgBackColor[i][0]-cBackLower[i][0], avgBackColor[i][1]-cBackLower[i][1],
					avgBackColor[i][2]-cBackLower[i][2]});
			upperBound.set(new double[]{avgBackColor[i][0]+cBackUpper[i][0], avgBackColor[i][1]+cBackUpper[i][1],
					avgBackColor[i][2]+cBackUpper[i][2]});
			
			
			Core.inRange(imgIn, lowerBound, upperBound, sampleMats[i]);
			
			
		}
		
		imgOut.release();
		sampleMats[0].copyTo(imgOut);
	
		
		for (int i = 1; i < SAMPLE_NUM; i++)
		{
			Core.add(imgOut, sampleMats[i], imgOut);
		}
		
		Core.bitwise_not(imgOut, imgOut);
		
		
		Imgproc.medianBlur(imgOut, imgOut, 7);
		
		
	}
	
	
	void dilate(Mat img)
	{
		int cols = img.cols();
		int rows = img.rows();
		
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
		
		Mat roi = new Mat(img, new Rect(cols/4, rows/6, cols/2, rows*2/3));
		
		
		Imgproc.dilate(roi, roi, element);
		
		
	}
	

	
	void makeContours()
	{
		hg.contours.clear();
		Imgproc.findContours(binMat, hg.contours, hg.hie, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
		//Find biggest contour and return the index of the contour, which is hg.cMaxId
		hg.findBiggestContour();
		
		if (hg.cMaxId > -1) {
			
			hg.approxContour.fromList(hg.contours.get(hg.cMaxId).toList());
			Imgproc.approxPolyDP(hg.approxContour, hg.approxContour, 2, true);
			hg.contours.get(hg.cMaxId).fromList(hg.approxContour.toList());
			
			//hg.contours.get(hg.cMaxId) represents the contour of the hand
			Imgproc.drawContours(rgbaMat, hg.contours, hg.cMaxId, mColorsRGB[0], 1);
			
			//Palm center is stored in hg.inCircle, radius of the inscribed circle is stored in hg.inCircleRadius
			hg.findInscribedCircle(rgbaMat);
			
			
			hg.boundingRect = Imgproc.boundingRect(hg.contours.get(hg.cMaxId));
			
			Imgproc.convexHull(hg.contours.get(hg.cMaxId), hg.hullI, false);
			
			hg.hullP.clear();
			for (int i = 0; i < hg.contours.size(); i++)
				hg.hullP.add(new MatOfPoint());
			
			int[] cId = hg.hullI.toArray();
			List<Point> lp = new ArrayList<Point>();
			Point[] contourPts = hg.contours.get(hg.cMaxId).toArray();
			
			for (int i = 0; i < cId.length; i++)
			{
				lp.add(contourPts[cId[i]]);
				//Core.circle(rgbaMat, contourPts[cId[i]], 2, new Scalar(241, 247, 45), -3);
			}
			
			//hg.hullP.get(hg.cMaxId) returns the locations of the points in the convex hull of the hand
			hg.hullP.get(hg.cMaxId).fromList(lp);
			lp.clear();
			
			
			
			hg.fingerTips.clear();
			hg.defectPoints.clear();
			hg.defectPointsOrdered.clear();
			
			hg.fingerTipsOrdered.clear();
			hg.defectIdAfter.clear();
			
			
			if ((contourPts.length >= 5) 
					&& hg.detectIsHand(rgbaMat) && (cId.length >=5)){
				Imgproc.convexityDefects(hg.contours.get(hg.cMaxId), hg.hullI, hg.defects);
				List<Integer> dList = hg.defects.toList();
			
							
				Point prevPoint = null;
				
				for (int i = 0; i < dList.size(); i++)
				{
					int id = i % 4;
					Point curPoint;
					
					if (id == 2) { //Defect point
						double depth = (double)dList.get(i+1)/256.0;
						curPoint = contourPts[dList.get(i)];
						
						Point curPoint0 = contourPts[dList.get(i-2)];
						Point curPoint1 = contourPts[dList.get(i-1)];
						Point vec0 = new Point(curPoint0.x - curPoint.x, curPoint0.y - curPoint.y);
						Point vec1 = new Point(curPoint1.x - curPoint.x, curPoint1.y - curPoint.y);
						double dot = vec0.x*vec1.x + vec0.y*vec1.y;
						double lenth0 = Math.sqrt(vec0.x*vec0.x + vec0.y*vec0.y);
						double lenth1 = Math.sqrt(vec1.x*vec1.x + vec1.y*vec1.y);
						double cosTheta = dot/(lenth0*lenth1);
						
						if ((depth > hg.inCircleRadius*0.7)&&(cosTheta>=-0.7)
								&& (!isClosedToBoundary(curPoint0, rgbaMat))
								&&(!isClosedToBoundary(curPoint1, rgbaMat))
								){
							
												
							hg.defectIdAfter.add((i));
							
							
							Point finVec0 = new Point(curPoint0.x-hg.inCircle.x,
									curPoint0.y-hg.inCircle.y);
							double finAngle0 = Math.atan2(finVec0.y, finVec0.x);
							Point finVec1 = new Point(curPoint1.x-hg.inCircle.x,
									curPoint1.y - hg.inCircle.y);
							double finAngle1 = Math.atan2(finVec1.y, finVec1.x);
							
							
							
							if (hg.fingerTipsOrdered.size() == 0) {
								hg.fingerTipsOrdered.put(finAngle0, curPoint0);
								hg.fingerTipsOrdered.put(finAngle1, curPoint1);
								
							} else {
							   
							   
							  		hg.fingerTipsOrdered.put(finAngle0, curPoint0);
							  
							    	
							    	hg.fingerTipsOrdered.put(finAngle1, curPoint1);
							    
							  
							}
							
						}
						
										
					
					}
				}
				

			}
			
		}
		
		if (hg.detectIsHand(rgbaMat)) {

			//hg.boundingRect represents four coordinates of the bounding box. 
			Core.rectangle(rgbaMat, hg.boundingRect.tl(), hg.boundingRect.br(), mColorsRGB[1], 2);
			Imgproc.drawContours(rgbaMat, hg.hullP, hg.cMaxId, mColorsRGB[2]);
		}
		
	}
	
	boolean isClosedToBoundary(Point pt, Mat img)
	{
		int margin = 5;
		if ((pt.x > margin) && (pt.y > margin) && 
				(pt.x < img.cols()-margin) &&
				(pt.y < img.rows()-margin)) {
			return false;
		}
		
		return true;
	}
	
	Rect makeBoundingBox(Mat img)
	{
		hg.contours.clear();
		Imgproc.findContours(img, hg.contours, hg.hie, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		hg.findBiggestContour();
		
		if (hg.cMaxId > -1) {
			
			hg.boundingRect = Imgproc.boundingRect(hg.contours.get(hg.cMaxId));
			
			
			
		}
		
		if (hg.detectIsHand(rgbaMat)) {

			return hg.boundingRect;
		} else
			return null;
	}
	
	
	
	@Override
	public void onPause(){
		Log.i(TAG, "Paused!");
		super.onPause();
		if (mOpenCvCameraView != null){
			mOpenCvCameraView.disableView();
		}
	}
	
	@Override
	public void onResume() {
		
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	
		Log.i(TAG, "Resumed!");
	}
	
	@Override
	public void onDestroy(){
		Log.i(TAG, "Destroyed!");
		releaseCVMats();
		
		super.onDestroy();
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
		 SharedPreferences numbers = getSharedPreferences("Numbers", 0);
	     SharedPreferences.Editor editor = numbers.edit();
	     editor.putInt("imgNum", imgNum);
	     editor.commit();
	     
	     try{
		        FileOutputStream fos = new FileOutputStream(sdFile);
		        Log.e("Fos","write succeeded......");
		        ObjectOutputStream oos = new ObjectOutputStream(fos);
		        Log.e("oos","write succeeded......");
		        for (Map.Entry<Integer, Intent> entry : table.entrySet()) {  
		        	  
		          oos.writeInt(entry.getKey());
		          String tmp =entry.getValue().toURI();
		          Log.e("111111",tmp);
		          oos.writeObject(tmp);
		        }
		        

		        Log.e("Ob","write succeeded......");
		        oos.close();
		        Log.e("Write","write succeeded......");
	        }catch(Exception ex)
	        {
	        	Log.e("Write","write failed......");
	        }
	     
	     
	}
	

	
	public static boolean deleteDir(File dir) {
	      if (dir != null && dir.isDirectory()) {
	         String[] children = dir.list();
	         for (int i = 0; i < children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	               return false;
	            }
	         }
	      }

	      // The directory is now empty so delete it
	      return dir.delete();
	   }

	

}
