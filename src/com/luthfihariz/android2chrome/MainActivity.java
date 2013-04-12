package com.luthfihariz.android2chrome;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;

public class MainActivity extends Activity {

	TextView urlTextView;
	GoogleAccountCredential credentials;
	private static final String PREF_ACCOUNT_NAME = "accountName";
	static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
	static final int REQUEST_AUTHORIZATION = 1;
	static final int REQUEST_ACCOUNT_PICKER = 2;
	Tasks service;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = new GsonFactory();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		Log.d("Android2Chrome", "Action : " + action + " type : " + type);

		urlTextView = (TextView) findViewById(R.id.url_tv);

		if (Intent.ACTION_VIEW.equals(action)) {
			sendUrlToGmailTask(intent);
		}

		// Google Accounts
		credential = GoogleAccountCredential.usingOAuth2(this,
				TasksScopes.TASKS);
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME,
				null));

		// Task client
		service = new Tasks.Builder(transport, jsonFactory, credential)
				.setApplicationName("Android2Chrome-Beta/0.1").build();

	}

	private void showGooglePlayAvailabilityErrorDialog(
			final int connectionStatusCode) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
						connectionStatusCode, MainActivity.this,
						REQUEST_GOOGLE_PLAY_SERVICES);
				dialog.show();

			}
		});
	}

	private void sendUrlToGmailTask(Intent intent) {
		Log.d("Android2Chrome", "send!");
		Uri sharedUrl = intent.getData();
		URL url = null;
		try {
			url = new URL(sharedUrl.getScheme(), sharedUrl.getHost(),
					sharedUrl.getPath());
			urlTextView.setText(url.toString());
		} catch (MalformedURLException e) {
			Log.d("Android2Chrome", "unable to get the url");
		}

	}
}