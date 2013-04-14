package com.luthfihariz.android2chrome;

import java.io.IOException;

import android.util.Log;

public class AsyncCreateTask extends CommonAsyncTask {

	AsyncCreateTask(MainActivity activity) {
		super(activity);
	}

	@Override
	protected void doInBackground() throws IOException {
		Log.d(Utils.TAG, "Dummy log : Adding url to the list...");
	}

	public static void run(MainActivity activity) {
		new AsyncCreateTask(activity).execute();
	}

}
