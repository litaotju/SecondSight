package com.tju.secondsight;

import com.tju.secondsight.net.Babe;
import com.tju.secondsight.net.Mvpic;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;

public class NotificationView extends Activity{
	static private final String TAG = "NotificationView"; 
	static public final String NOTIFICATION_ID = "notificationID";
	//static private final String uriBase = "http://tlwechat.herokuapp.com/babe/";
	static private final String myBlog = "https://litaotju.github.io";
	static private final String uriBase = "https://nlproxy-taolee.rhcloud.com/babe/";
	private Random mRandom = new Random();
	
	private Button mButtonNav;
	private Button mButtonRandom;
	private Button mButtonMeinv;
	private Button mButtonBlog;
	private EditText mEditText;
	
	private String mUriString;
	private String mTitle = null;
	private Babe mBabe = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		if (i.hasExtra(NOTIFICATION_ID)){
			NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(i.getExtras().getInt(NOTIFICATION_ID));
		}
		setContentView(R.layout.notification);
		mButtonNav = (Button)findViewById(R.id.buttonNav);
		mButtonNav.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				if(mUriString !=null){
					startWeb();
				}else{
					Toast.makeText(getBaseContext(), "NoneTargetBabe", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		mButtonBlog = (Button)findViewById(R.id.blogButton);
		mButtonBlog.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View view){
				mUriString = myBlog;
				mTitle = "";
				startWeb();
			}
		});
		
		mButtonRandom =(Button)findViewById(R.id.buttonRondom);
		mButtonRandom.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				int i = mRandom.nextInt();
				mEditText.setText("" + i);
				mUriString = uriBase + i;
				mTitle = "看看你的运气";
			}
		});
		
		mButtonMeinv = (Button)findViewById(R.id.meinvButton);
		mButtonMeinv.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				 Thread getMeinvThread = new Thread()
				 {
				     public void run()
				     {
						mBabe = Mvpic.getMeinv();
				     }
				 };
				getMeinvThread.start();
				try{
					getMeinvThread.join(600);
					if(mBabe == null){
						Toast.makeText(getBaseContext(), "Error Get Meinv", Toast.LENGTH_SHORT).show();
					}else{
						mEditText.setHint(mBabe.getTitle());
						mUriString = mBabe.getUrl();
					}
				}catch(InterruptedException e){
					Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
		    	}
			}
		});
		
		mEditText = (EditText) findViewById(R.id.editText1);
		mEditText.addTextChangedListener(new TextWatcher(){
			@Override
			public void onTextChanged(CharSequence s, int start, int before,     
	                int count){
				String str = mEditText.getText().toString();
	            try {  
	                Integer.parseInt(str);  
	            } catch (Exception e) {  
	                showDialog();  
	            }
	            mUriString = uriBase + str;
			}
			@Override
			public void afterTextChanged(Editable s){
				mUriString = uriBase + mEditText.getText().toString();
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,  
	                int after){
			}
		});

	}
	
    private void showDialog(){  
        AlertDialog dialog;  
        AlertDialog.Builder builder = new AlertDialog.Builder(NotificationView.this);  
        builder.setTitle("消息").setIcon(android.R.drawable.stat_notify_error);  
        builder.setMessage("你输出的整型数字有误，请改正");  
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){  
            @Override 
            public void onClick(DialogInterface dialog, int which) {                    
            }                     
        });  
        dialog = builder.create();  
        dialog.show();  
    }
    
    private void startWeb(){
		Intent i = new Intent(this, BrowserActivity.class);
		i.setData(Uri.parse(mUriString));
		i.putExtra("title", mTitle);
		startActivity(i);
    }
}
;