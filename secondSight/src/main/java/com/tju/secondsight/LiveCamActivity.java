package com.tju.secondsight;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class LiveCamActivity extends Activity {
	private String TAG = "LiveCamActivity";
	private int mWifiAddress;
	private WifiInfo mWifiInfo;
	private WifiManager mWifiManager;
	private TextView mTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
		setContentView(R.layout.activity_live_cam);
		mTextView = (TextView)findViewById(R.id.textViewLiveFragMent);
		if( mWifiManager != null){
			mWifiInfo = mWifiManager.getConnectionInfo();
			if(mWifiInfo != null){
				mWifiAddress = mWifiInfo.getIpAddress();
				Log.i(TAG, ""+mWifiAddress );
				mTextView.setText("IP:" + mWifiAddress);
			}
		}else{
			Log.e(TAG, "WifiManager is NULL");
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.live_cam, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
