package com.luthfihariz.android2chrome;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

public class AsyncCheckTaskList extends CommonAsyncTask {

	private boolean listAvailable;
	private String listId;

	AsyncCheckTaskList(MainActivity activity) {
		super(activity);
	}

	@Override
	protected void doInBackground() throws IOException {
		listAvailable = false;
		listId = null;
		
		TaskLists taskLists = new TaskLists();
		taskLists = client.tasklists().list().execute();

		for (TaskList taskList : taskLists.getItems()) {
			
			if (taskList.getTitle().equals(Utils.TAG)) {
				Log.d(Utils.TAG, "Yap, found!");
				listAvailable = true;
				listId = taskList.getId();
			}
		}

		if (!listAvailable) {
			Log.d(Utils.TAG, "Not found..creating new list...");
			TaskList newTaskList = new TaskList();
			newTaskList.setTitle(Utils.TAG);
			newTaskList = client.tasklists().insert(newTaskList).execute();
			listId = newTaskList.getId();
		}

		Log.d(Utils.TAG, "list id : " + listId);

		SharedPreferences sharedPref = activity
				.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(MainActivity.PREF_TASKLIST_ID, listId);
		editor.commit();
	}
	
	

	public static void run(MainActivity activity) {
		new AsyncCheckTaskList(activity).execute();
	}

}
