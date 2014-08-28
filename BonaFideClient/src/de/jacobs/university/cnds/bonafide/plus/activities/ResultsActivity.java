package de.jacobs.university.cnds.bonafide.plus.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.plus.adapter.ResultsAdapter;
import de.jacobs.university.cnds.bonafide.plus.tasks.FetchMeasurementResults;
import de.jacobs.university.cnds.bonafide.plus.tasks.FetchMeasurementResultsCallbackInterface;

public class ResultsActivity extends Activity implements FetchMeasurementResultsCallbackInterface {
	private ProgressDialog progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadResults();
	}
	
	public void loadResults() {
		showProgressBar();
		new FetchMeasurementResults(this,this).execute();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.results_refresh:
	        	loadResults();
	        	break;
	        case android.R.id.home:
	        	Intent intent = new Intent(this,FrontendActivity.class);
	        	startActivity(intent);
	        	finish();
	        	break;
	    }
	    
	    return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.results, menu);
		return true;
	}

	@Override
	public void measurementResultsRecieved(ResultsAdapter resultsAdapter) {
		if (resultsAdapter==null) {
			hideProgressBar();
			showErrorDialog();
			return;
		}
		ExpandableListView expandableListView = (ExpandableListView)findViewById(R.id.resultsExpandableListView1);
		expandableListView.setAdapter(resultsAdapter);
		hideProgressBar();
	}
	
	private void showProgressBar() {
		progress = ProgressDialog.show(this, getString(R.string.result_list_progress_title),
				getString(R.string.result_list_progress), true);
	}
	
	private void hideProgressBar() {
		if (progress!=null) {
			progress.dismiss();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void showErrorDialog() {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(getString(R.string.result_list_retrieval_error_title));
		alert.setMessage(getString(R.string.result_list_retrieval_error));
		alert.setButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		alert.show();
	}

}
