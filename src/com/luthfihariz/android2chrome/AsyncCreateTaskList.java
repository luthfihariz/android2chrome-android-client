package com.luthfihariz.android2chrome;

import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.api.services.tasks.model.TaskList;

public class AsyncCreateTaskList extends CommonAsyncTask {

	AsyncCreateTaskList(MainActivity activity) {
		super(activity);
	}

	@Override
	protected void doInBackground() throws IOException {

		TaskList taskList = new TaskList();
		taskList.setTitle("Android2Chrome");
		
		Log.d(Utils.TAG, "Creating app task list...");		
		taskList = client.tasklists().insert(taskList).execute();

		Log.d(Utils.TAG, "Save list id to shared preferences...");
		SharedPreferences sharedPref = activity
				.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(MainActivity.PREF_TASKLIST_ID, taskList.getId());
		editor.commit();
	}

	public static void run(MainActivity activity) {
		new AsyncCreateTaskList(activity).execute();
	}
}
