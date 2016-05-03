package com.tju.secondsight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;
/*
 * 处理张片照片的Activity。有三个菜单，长按或者按住右上角的菜单可以出来三个选项
 * 分享。删除。或者编辑。
 * 
 */
public class LabActivity extends Activity {
	
	public static final String PHOTO_MIME_TYPE = "image/png";
	public static final String EXTRA_PHOTO_URI = "com.tju.secondsight.LabActivity.extra.PHOTO_URI";
	public static final String EXTRA_PHOTO_DATA_PATH = "com.tju.secondsight.LabActivity.extra.PHOTO_DATA_PATH";
	private Uri mUri;
	private String mDataPath;
	
	private ImageView imageView1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		
		mUri = intent.getParcelableExtra(EXTRA_PHOTO_URI);
		mDataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH);
		setContentView(R.layout.activity_lab);

		imageView1 = (ImageView) findViewById(R.id.imageView1);
		imageView1.setImageURI(mUri);
		this.registerForContextMenu(imageView1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.lab, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    getMenuInflater().inflate(R.menu.lab, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		 return this.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id){
			case R.id.action_settings: {
				return true;
			}case R.id.menu_delete:{
				deletePhoto();
				return true;
			}case R.id.menu_share:{
				sharePhoto();
				return true;
			}case R.id.menu_edit:{
				editPhoto();
				return true;
			}default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void sharePhoto(){
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType(PHOTO_MIME_TYPE);
		intent.putExtra(Intent.EXTRA_STREAM, mUri);
		intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.photo_send_extra_subject));
		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.photo_send_extra_text));
		startActivity(Intent.createChooser(intent, getString(R.string.photo_send_chooser_title)));
	}
	
	private void deletePhoto(){
		final AlertDialog.Builder alert = new AlertDialog.Builder( LabActivity.this);
		alert.setTitle(R.string.photo_delete_prompt_title);
		alert.setMessage(R.string.photo_delete_prompt_message);
		alert.setCancelable(false);
		alert.setPositiveButton(R.string.menu_delete,new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				getContentResolver().delete(
						Images.Media.EXTERNAL_CONTENT_URI,
						MediaStore.MediaColumns.DATA + "=?",
						new String[] { mDataPath });
				finish();
			}
			});
		alert.setNegativeButton(android.R.string.cancel, null);
		alert.show();
	}
	
	private void editPhoto(){
		final Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setDataAndType(mUri, PHOTO_MIME_TYPE);
		startActivity(Intent.createChooser(intent, getString(R.string.photo_edit_chooser_title)) );
	}
	
}
