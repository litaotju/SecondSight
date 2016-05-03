package com.tju.secondsight;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class BrowserActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser);
		Uri url = getIntent().getData();
		String title = getIntent().getExtras().getString("title", null);
		if(title != null){
			this.setTitle(title);
		}
		WebView webView = (WebView)findViewById(R.id.webView1);
		webView.loadUrl(url.toString());
		webView.setWebChromeClient(new WebChromeClient());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.browser, menu);
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
