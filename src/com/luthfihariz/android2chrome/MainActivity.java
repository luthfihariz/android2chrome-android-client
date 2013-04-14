package com.luthfihariz.android2chrome;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
	GoogleAccountCredential credential;
	private static final String PREF_ACCOUNT_NAME = "accountName";
	public static final String PREF_TASKLIST_ID = "taskListId";
	static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
	static final int REQUEST_AUTHORIZATION = 1;
	static final int REQUEST_ACCOUNT_PICKER = 2;
	Tasks service;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = new GsonFactory();

	List<String> urlList;
	ArrayAdapter<String> adapter;
	int numAsyncTasks;

	private ListView listView;

	Intent intentReceived;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// view and menu
		setContentView(R.layout.calendarlist);
		urlTextView = (TextView) findViewById(R.id.url_tv);
		listView = (ListView) findViewById(R.id.list);

		intentReceived = getIntent();
		// String action = intentReceived.getAction();

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

	@Override
	protected void onResume() {
		super.onResume();
		if (checkGooglePlayServicesAvailable())
			haveGooglePlayServices();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_GOOGLE_PLAY_SERVICES:
			if (resultCode == Activity.RESULT_OK) {
				haveGooglePlayServices();
			} else {
				checkGooglePlayServicesAvailable();
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				Log.d(Utils.TAG, "request authorization!");

				// do something
				sendUrlToGmailTask(intentReceived);

			} else {
				chooseAccount();
			}
			break;
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == Activity.RESULT_OK && data != null
					&& data.getExtras() != null) {
				String accountName = data.getExtras().getString(
						AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					credential.setSelectedAccountName(accountName);
					SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(PREF_ACCOUNT_NAME, accountName);
					editor.commit();

					// do something with google task api
					sendUrlToGmailTask(intentReceived);

				}
			}
			break;
		}
	}

	private void haveGooglePlayServices() {
		if (credential.getSelectedAccountName() == null) {
			chooseAccount();
		} else {
			// do something with google api

			sendUrlToGmailTask(intentReceived);
		}
	}

	/** Check that Google Play services APK is installed and up to date */
	private boolean checkGooglePlayServicesAvailable() {
		final int connectionStatusCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
			return false;
		}
		return true;
	}

	public void refreshView() {
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, urlList);
		listView.setAdapter(adapter);
	}

	private void sendUrlToGmailTask(Intent intent) {
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Log.d(Utils.TAG, "Ready to sent...");
			Uri sharedUrl = intent.getData();
			URL url = null;
			try {
				url = new URL(sharedUrl.getScheme(), sharedUrl.getHost(),
						sharedUrl.getPath());
				urlTextView.setText(url.toString());
			} catch (MalformedURLException e) {
				Log.d("Android2Chrome", "unable to get the url");
			}

			if (url != null) {

				SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
				String taskListId = settings.getString(PREF_TASKLIST_ID, null);
				Log.d(Utils.TAG, "URL is not null... & tasklist id : "
						+ taskListId);
				if (taskListId == null) {
					Log.d(Utils.TAG, "It is checking...");
					AsyncCheckTaskList.run(this);
				}
				
				AsyncCreateTask.run(this);

			}
		}

	}

	public void showGooglePlayServicesAvailabilityErrorDialog(
			final int connectionStatusCode) {
		runOnUiThread(new Runnable() {
			public void run() {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
						connectionStatusCode, MainActivity.this,
						REQUEST_GOOGLE_PLAY_SERVICES);
				dialog.show();
			}
		});
	}

	private void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
	}

}