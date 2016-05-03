package com.tju.secondsight;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.tju.secondsight.filters.Filter;
import com.tju.secondsight.filters.NoneFilter;
import com.tju.secondsight.filters.convolution.StrokeEdgesFilter;
import com.tju.secondsight.filters.curve.CrossProcessCurveFilter;
import com.tju.secondsight.filters.curve.PortraCurveFilter;
import com.tju.secondsight.filters.curve.ProviaCurveFilter;
import com.tju.secondsight.filters.curve.VelviaCurveFilter;
import com.tju.secondsight.filters.mixer.RecolorCMVFilter;
import com.tju.secondsight.filters.mixer.RecolorRCFilter;
import com.tju.secondsight.filters.mixer.RecolorRGVFilter;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

public class CameraActivity extends Activity implements CvCameraViewListener2{
	
	private String TAG ="CameraActivity";
	private static final String STATE_CAMERA_INDEX = "cameraIndex";
	// Keys for storing the indices of the active filters.
	private static final String STATE_CURVE_FILTER_INDEX = "curveFilterIndex";
	private static final String STATE_MIXER_FILTER_INDEX = "mixerFilterIndex";
	private static final String STATE_CONVOLUTION_FILTER_INDEX = "convolutionFilterIndex";
	private Filter[] mCurveFilters;
	private Filter[] mMixerFilters;
	private Filter[] mConvolutionFilters;
	
	private int mCurveFilterIndex;
	private int mMixerFilterIndex;
	private int mConvolutionFilterIndex;
	
	private int mNumberCameras; //摄像头的数量
	private int mCameraIndex; //摄像头的index
	
	private boolean mIsCameraFacing; //是否是前置摄像头
	
	private boolean mIsMenuLocked; //菜单锁
	private boolean mIsPhotoPending; //照相
	
	private Mat mBgr; //保存返回的Frame
	private CameraBridgeViewBase mCameraView; //主要的view
	private Canvas canvas; 	//在cameara上画小圆圈的
	
	private int mScreenOritention;
	private OnTouchListener mOnTouchListener = new OnTouchListener(){
		private long lastDownTime = 0;
		@Override
		public boolean onTouch( View v, MotionEvent event){
			int action = event.getAction();
			switch(action){
			case MotionEvent.ACTION_DOWN:
				//两次Down事件的间隔小于500毫秒则进行拍照
				long interVal = System.currentTimeMillis() - lastDownTime;
				Log.i(TAG, "time: "+interVal );
				if( interVal < 500){
					mIsPhotoPending = true;
				}
				lastDownTime = System.currentTimeMillis();
				paintCircle( event.getX(),event.getY());
				break;
			case MotionEvent.ACTION_UP:
				//长按则返回false
				if( System.currentTimeMillis()- lastDownTime > 500){
					return false;
				}else{
					break;
				}
			default:
				return false;
			}
			return true;
		}
	};
	
	//在cameraView 上面画小圆圈
	private void paintCircle(float x, float y){
		if(mCameraView.getHolder().getSurface().isValid()){
			canvas = mCameraView.getHolder().lockCanvas();
			Paint paint = new Paint();
			paint.setColor(Color.rgb(255, 255, 255));
			paint.setStrokeWidth( (float)1.0 );
			canvas.drawCircle(x, y, (float) 100.0, paint);
			mCameraView.getHolder().unlockCanvasAndPost(canvas);
		}
	}
	
	//设置加载OpenCV库之后的CallBack， 并重载了该对象的函数，加载成功后，使能 mCameraView.
	private BaseLoaderCallback mBaseLoaderCallback = new BaseLoaderCallback(this){
		@Override
		public void onManagerConnected(int status){
			switch(status){
			case LoaderCallbackInterface.SUCCESS:
				mCameraView.enableView();
				mBgr = new Mat();
				mCurveFilters = new Filter[]{
						new NoneFilter(),
						new CrossProcessCurveFilter(),
						new PortraCurveFilter(),
						new ProviaCurveFilter(),
						new VelviaCurveFilter(),
				};
				mMixerFilters = new Filter[]{
					new NoneFilter(),
					new RecolorCMVFilter(),
					new RecolorRCFilter(),
					new RecolorRGVFilter()
				};
				mConvolutionFilters = new Filter[]{
					new NoneFilter(),
					new StrokeEdgesFilter()
				};
				break;
			default:
				super.onManagerConnected(status);
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置窗口常亮
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//读取之前存储的摄像头标号
		if (savedInstanceState != null) {
				mCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0);
				mConvolutionFilterIndex = savedInstanceState.getInt(STATE_CONVOLUTION_FILTER_INDEX);
				mCurveFilterIndex = savedInstanceState.getInt(STATE_CURVE_FILTER_INDEX);
				mMixerFilterIndex = savedInstanceState.getInt(STATE_MIXER_FILTER_INDEX);
		} else {
				mCameraIndex = 0;
				mConvolutionFilterIndex = 0;
				mCurveFilterIndex = 0;
				mMixerFilterIndex = 0;
		}
		//只有特定版本之后的安卓是有两个摄像头的
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD){
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(mCameraIndex, cameraInfo);
			this.mIsCameraFacing = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
			mNumberCameras = Camera.getNumberOfCameras();
		} else{
			this.mIsCameraFacing = false;
			mNumberCameras = 1;
		}
		//获取屏幕方向
		mScreenOritention = this.getWindowManager().getDefaultDisplay().getRotation();
		//获取cameraview 并绑定Listener
		mCameraView = new JavaCameraView(this, this.mCameraIndex);
		mCameraView.setVisibility(SurfaceView.VISIBLE);
		mCameraView.setCvCameraViewListener(this);
		mCameraView.setOnTouchListener(mOnTouchListener);
		
		setContentView(mCameraView);
		registerForContextMenu( mCameraView);
		displayNotification(this.getText(R.string.app_name), 
				this.getText(R.string.nofication_start));
	}

	protected void displayNotification(CharSequence title, CharSequence message){
		int notificationID = 1;
		//点击notification之后要弹出一个自定义的Notification class，实际上一个Activity
		Intent i = new Intent(this, NotificationView.class);
		i.putExtra(NotificationView.NOTIFICATION_ID, notificationID);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);
		NotificationManager nm = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
		Notification notif = new Notification.Builder(this)
				.setContentTitle(title)
				.setContentText(message)
				.setSmallIcon(R.drawable.ic_launcher)
				//.setAutoCancel(true)
				.setContentIntent(pendingIntent)
				.build();
		nm.notify(notificationID, notif);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.camera, menu);
		if( this.mNumberCameras == 1){
			menu.removeItem(R.id.menu_next_camera);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if( this.mIsMenuLocked){
			return true;
		}
		switch (item.getItemId()){
		case R.id.menu_next_camera: {
			mIsMenuLocked = true;
			mCameraIndex++;
			if (mCameraIndex == mNumberCameras) {
				mCameraIndex = 0;
			}
			recreate();
			return true;
		}case R.id.menu_take_photo:{
			mIsMenuLocked = true;
			mIsPhotoPending = true;
			return true;
		}case R.id.menu_next_convolution_filter :{
			mConvolutionFilterIndex++;
			if( mConvolutionFilterIndex == mConvolutionFilters.length){
				mConvolutionFilterIndex = 0;
			}
			Toast.makeText(this, mConvolutionFilters[mConvolutionFilterIndex].getClass().getSimpleName(),
							Toast.LENGTH_SHORT).show();
			return true;
		} case R.id.menu_next_curve_filter: {
			mCurveFilterIndex ++;
			if( mCurveFilterIndex == mCurveFilters.length){
				mCurveFilterIndex =0;
			}
			Toast.makeText(this, mCurveFilters[mCurveFilterIndex].getClass().getSimpleName(), 
					Toast.LENGTH_SHORT).show();
			return true;
		} case R.id.menu_next_mixer_filter: {
			mMixerFilterIndex ++;
			if( this.mMixerFilterIndex == this.mMixerFilters.length){
				mMixerFilterIndex = 0;
			}
			Toast.makeText(this, mMixerFilters[mMixerFilterIndex].getClass().getSimpleName(), 
					Toast.LENGTH_SHORT).show();
			return true;
		}case R.id.menu_live: {
			this.mIsMenuLocked =true;
			final Intent i = new Intent(this, LiveCamActivity.class);
			startActivity(i);
			return true;
		}case R.id.menu_notification:{
			this.mIsMenuLocked =true;
			final Intent i = new Intent(this, NotificationView.class);
			startActivity(i);
			return true;
		}default:{
			return super.onOptionsItemSelected(item);
		}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    getMenuInflater().inflate(R.menu.camera, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		 return this.onOptionsItemSelected(item);
	}
	
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat rgba = inputFrame.rgba();
		this.mCurveFilters[this.mCurveFilterIndex].apply(rgba, rgba);
		this.mMixerFilters[this.mMixerFilterIndex].apply(rgba, rgba);
		this.mConvolutionFilters[this.mConvolutionFilterIndex].apply(rgba, rgba);
		adjustOritation(rgba);
		if( this.mIsPhotoPending){
			mIsPhotoPending = false;
			takePhoto(rgba);
		}
		return rgba;
	}
	
	private void adjustOritation(Mat rgba){
		switch( this.mScreenOritention){
		case Surface.ROTATION_0:
			if(this.mIsCameraFacing){
				Core.flip(rgba.t(), rgba, 0);
			}else{
				Core.flip(rgba.t(), rgba, 1);
			}
			break;
		case Surface.ROTATION_270:
			Core.flip(rgba, rgba, 0);
			break;
		default:
			break;
		}
	}
	
	private void takePhoto( final Mat rgba){
		// Determine the path and metadata for the photo.
		final long currentTimeMillis = System.currentTimeMillis();
		final String appName = getString(R.string.app_name);
		final String galleryPath = Environment.getExternalStoragePublicDirectory(
										Environment.DIRECTORY_PICTURES).toString();
		final String albumPath = galleryPath + "/" + appName;
		final String photoPath = albumPath + "/" + currentTimeMillis + ".png";
		
		final ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, photoPath);
		values.put(Images.Media.MIME_TYPE, LabActivity.PHOTO_MIME_TYPE);
		values.put(Images.Media.TITLE, appName);
		values.put(Images.Media.DESCRIPTION, appName);
		values.put(Images.Media.DATE_TAKEN, currentTimeMillis);
		
		// Ensure that the album directory exists.
		File album = new File(albumPath);
		if (!album.isDirectory() && !album.mkdirs()) {
			Log.e(TAG, "Failed to create album directory at " + albumPath);
			onTakePhotoFailed();
			return;
		}
		
		// Try to create the photo.
		Imgproc.cvtColor(rgba, mBgr, Imgproc.COLOR_RGBA2BGR, 3);
		if (!Highgui.imwrite(photoPath, mBgr)) {
			Log.e(TAG, "Failed to save photo to " + photoPath);
			onTakePhotoFailed();
		}
		Log.d(TAG, "Photo saved successfully to " + photoPath);
		// Try to insert the photo into the MediaStore.
		Uri uri;
		
		try {
			uri = getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		} catch (final Exception e) {
			Log.e(TAG, "Failed to insert photo into MediaStore");
			e.printStackTrace();
			// Since the insertion failed, delete the photo.
			File photo = new File(photoPath);
			if (!photo.delete()) {
				Log.e(TAG, "Failed to delete non-inserted photo");
			}
				onTakePhotoFailed();
			return;
		}
		
		// Open the photo in LabActivity.
		final Intent intent = new Intent(this, LabActivity.class);
		intent.putExtra(LabActivity.EXTRA_PHOTO_URI, uri);
		intent.putExtra(LabActivity.EXTRA_PHOTO_DATA_PATH, photoPath);
		startActivity(intent);
		
	}
	private void onTakePhotoFailed() {
		mIsMenuLocked = false;
		// Show an error message.
		final String errorMessage = getString(R.string.photo_error_message);
		runOnUiThread(new Runnable() { 
			@Override
			public void run() {
				Toast.makeText(CameraActivity.this, errorMessage,Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		mScreenOritention = getWindowManager().getDefaultDisplay().getRotation();
		this.mCameraView.enableView();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, this.mBaseLoaderCallback)){
			Log.i(TAG, "OpenCvLoader failed");
		}
		mIsMenuLocked = false;
	}
	
	@Override
	public void onPause(){
		if( this.mCameraView != null)
			this.mCameraView.disableView();
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(this.mCameraView != null){
			this.mCameraView.disableView();
		}
	}
	
	@Override
	public void onCameraViewStarted(int width, int height) {		
		
	}

	@Override
	public void onCameraViewStopped() {
		
	}
	//保存摄像头的标号到 一个变量里面，方便下次接着使用
	public void onSaveInstanceState(Bundle savedInstance){
		savedInstance.putInt(STATE_CAMERA_INDEX, mCameraIndex);
		savedInstance.putInt(STATE_CONVOLUTION_FILTER_INDEX, mConvolutionFilterIndex);
		savedInstance.putInt(STATE_CURVE_FILTER_INDEX, mCurveFilterIndex);
		savedInstance.putInt(STATE_MIXER_FILTER_INDEX, mMixerFilterIndex);
		super.onSaveInstanceState(savedInstance);
	}
}
