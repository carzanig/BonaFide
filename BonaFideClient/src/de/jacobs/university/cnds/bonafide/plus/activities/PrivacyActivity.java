package de.jacobs.university.cnds.bonafide.plus.activities;

import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.plus.R.layout;
import de.jacobs.university.cnds.bonafide.plus.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class PrivacyActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_privacy);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.privacy, menu);
		return true;
	}

}
