package com.deshang365.meeting.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;

public class WebActivity extends BaseActivity {
	private WebView mWebView;
	private LinearLayout mLlBack;
	private TextView mTvTopical;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		initView();
	}

	private void initView() {
		String urlString = getIntent().getStringExtra("url");
		String topical = getIntent().getStringExtra("topical");
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText(topical);
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mWebView = (WebView) findViewById(R.id.wv_web);
		// 启用支持javascript
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				// 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
				return true;
			}
		});
		mWebView.loadUrl(urlString);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
