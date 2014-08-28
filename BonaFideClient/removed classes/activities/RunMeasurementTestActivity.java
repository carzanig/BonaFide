/*
	Copyright (c) 2012, Vitali Bashko
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met: 

	1. Redistributions of source code must retain the above copyright notice, this
   	list of conditions and the following disclaimer. 
	2. Redistributions in binary form must reproduce the above copyright notice,
   	this list of conditions and the following disclaimer in the documentation
   	and/or other materials provided with the distribution. 

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
	ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

	The views and conclusions contained in the software and documentation are those
	of the authors and should not be interpreted as representing official policies, 
	either expressed or implied, of the FreeBSD Project.
*/


package de.jacobs.university.cnds.bonafide.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.jacobs.university.cnds.bonafide.R;
import de.jacobs.university.cnds.bonafide.model.ClientExecutionResults;
import de.jacobs.university.cnds.bonafide.model.MeasurementTestEventListener;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.utils.ApplicationGlobalContext;
import de.jacobs.university.cnds.bonafide.utils.GlobalConstants;
import de.jacobs.university.cnds.bonafide.utils.ResultAnalyzer;
import de.jacobs.university.cnds.bonafide.utils.ResultAnalyzer.Statistics;
import de.jacobs.university.cnds.bonafide.utils.ServerConnector;
import de.jacobs.university.cnds.bonafide.utils.TestResultsPrinter;

/**
 * This activity displays the selected application protocol details 
 * and allows to trigger the measurement test execution.
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class RunMeasurementTestActivity extends Activity {
	
	private Button startTestButton;	
	private Button uploadResultsButton;
	private Button removeResultsButton;
	private Button viewResultsButton;
	private EditText cyclesNumber;
	private LinearLayout start_group;
	private LinearLayout result_group;
	private TextView testCompletedTextView;
	
	private ProtocolDescription protocolHeader;
	private ClientExecutionResults executionResults;
	
	private TelephonyManager telephonyManager;
	private NetworkPhoneStateListener networkPhoneStateListener;
	
	ConcurrentHashMap<String, String> networkState = new ConcurrentHashMap<String, String>();
	ConcurrentHashMap<String, String> signalStrength = new ConcurrentHashMap<String, String>();
	
	private String ip = null; 	
	private Integer port = null;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		protocolHeader = (ProtocolDescription) getIntent().getSerializableExtra(ApplicationGlobalContext.BUNDLE_PROTOCOL_HEADER);
		
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		ip = mPrefs.getString(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_IP, null);
		port = Integer.valueOf(mPrefs.getString(ApplicationGlobalContext.PREFERENCES_KEY_SENTRAL_SERVER_PORT, null));
		
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		networkPhoneStateListener = new NetworkPhoneStateListener(networkState, signalStrength);
								
		setContentView(R.layout.start_protocol_specification_test);
		
		TextView PFport = (TextView) findViewById(R.id.text_view_port1);
		TextView RFport = (TextView) findViewById(R.id.text_view_port2);
		TextView protocolName = (TextView) findViewById(R.id.text_view_protocol_name);						
		
		PFport.setText(String.valueOf(protocolHeader.getPFPort()));
		RFport.setText(String.valueOf(protocolHeader.getRFPort()));
		protocolName.setText(protocolHeader.getProtocolName());		
		
		cyclesNumber = (EditText) findViewById(R.id.edit_text_cycles_number);		
		
		start_group = (LinearLayout) findViewById(R.id.linear_layout_start_measurements_block);
		result_group = (LinearLayout) findViewById(R.id.linear_layout_results_block);
		testCompletedTextView = (TextView) findViewById(R.id.text_view_test_completed);
		
		uploadResultsButton = (Button) findViewById(R.id.button_submit_results);
		uploadResultsButton.setOnClickListener(new OnClickListener() {			
			@Override			
			public void onClick(View v) {
				if (executionResults != null && executionResults.getResultFilePath() != null) {
					
					AlertDialog.Builder builder = new Builder(RunMeasurementTestActivity.this);
					String message = getResources().getString(R.string.upload_file_dialog, executionResults.getResultFilePath());
					builder.setMessage(message);
					builder.setTitle(R.string.upload_confirmation);
					builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							boolean success = ServerConnector.uploadTestResultsToServer(ip,
									port, 
									executionResults.getResultFilePath());
							
							dialog.cancel();
							
							if (success) {		
								try {
									ApplicationGlobalContext.getInstance().addFileToStorageInfo(executionResults.getResultFilePath());
									showToast(getResources().getString(R.string.result_uploaded));
									RunMeasurementTestActivity.this.uploadResultsButton.setEnabled(false);
								} catch (IOException e) {
									showToast(getResources().getString(R.string.result_uploaded_fail));	
								}
																								
							} else {
								showToast(getResources().getString(R.string.result_uploaded_fail));		
							}					
						}
					});
					
					builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					
					builder.create().show();
				}
			}
		});
		
		removeResultsButton = (Button) findViewById(R.id.button_remove_results);
		removeResultsButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (executionResults != null && executionResults.getResultFilePath() != null) {
					
					AlertDialog.Builder builder = new Builder(RunMeasurementTestActivity.this);
					String message = getResources().getString(R.string.delete_file_dialog, executionResults.getResultFilePath());
					builder.setMessage(message);
					builder.setTitle(R.string.delete_confirmation);
					builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							File f = new File(executionResults.getResultFilePath());
							boolean del = f.delete();
							
							dialog.cancel();
							
							if (del) {						
								showToast(getResources().getString(R.string.file_deleted, executionResults.getResultFilePath()));
								
								removeResultsButton.setEnabled(false);
								uploadResultsButton.setEnabled(false);
							} else {
								showToast(getResources().getString(R.string.file_deleted_failed, executionResults.getResultFilePath()));
							}						
						}
					});
					
					builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					
					builder.create().show();
				}
			}
		});
		
		startTestButton = (Button) findViewById(R.id.button_start_test);
		startTestButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				boolean correct = true;				
				try {
					Integer intValue = Integer.valueOf(cyclesNumber.getEditableText().toString());
					if (intValue < 3 || intValue > 15) {
						correct = false;
					}
				} catch (Exception e) {
					correct = false;
				}
				
				if (correct) {
					startTest();	
				} else {
					final Dialog dialog = new Dialog(RunMeasurementTestActivity.this);
					
					View view = View.inflate(RunMeasurementTestActivity.this, R.layout.failure_dialog_layout, null);
					TextView textView = (TextView) view.findViewById(R.id.failure_dialog_text);
					textView.setText(R.string.number_of_tests_failure_text);		
					Button cancelBtn = (Button) view.findViewById(R.id.button_close_application);
					cancelBtn.setText(R.string.ok);
					cancelBtn.setOnClickListener(new OnClickListener() {				
						@Override
						public void onClick(View v) {
							dialog.cancel();							
						}
					});
								
					dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);			
					dialog.setTitle(R.string.number_of_tests_failure_title);
					dialog.setContentView(view);
					dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);
					dialog.show();	
				}				
				
			}
		});
		
		viewResultsButton = (Button) findViewById(R.id.button_view_results);
		viewResultsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(new File(executionResults.getResultFilePath())));
				intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				startActivity(intent);
			}
		});
	}
	
	
	/**
	 * create the options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu2_layout, menu);
		return true;
	}
	
	/**
	 * Set handlers to the options menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		switch (item.getItemId()) {
			case R.id.menu_about:
				Intent aboutIntent = new Intent(RunMeasurementTestActivity.this, AboutActivity.class);
				startActivity(aboutIntent);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private void startTest() {
		new ExecuteTest().execute();
	}
			
	private void showToast(int stringId) {
		Toast.makeText(RunMeasurementTestActivity.this, stringId, 5).show();	
	}
	
	private void showToast(String text) {
		Toast.makeText(RunMeasurementTestActivity.this, text, 5).show();	
	}
	
	/*
	 * NetworkPhoneStateListener is responsible for tracking all mobile network state changes 
	 * (including signal strength changes).
	 */
	public class NetworkPhoneStateListener extends PhoneStateListener {
		
		private final ConcurrentHashMap<String, String> networkState;
		private final ConcurrentHashMap<String, String> signalStrength;
				
		public NetworkPhoneStateListener(ConcurrentHashMap<String, String> networkState, ConcurrentHashMap<String, String> signalStrength) {
			this.networkState = networkState;
			this.signalStrength = signalStrength;
		}

		
		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {											
	        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss.SSS aa");
	        String time = simpleDateFormat.format(System.currentTimeMillis());
			StringBuffer sb = new StringBuffer();
			sb.append("State: ");
			switch (state) {
				case TelephonyManager.DATA_CONNECTED:
					sb.append("Connected");
					break;
				case TelephonyManager.DATA_CONNECTING:
					sb.append("Connecting");
					break;
				case TelephonyManager.DATA_DISCONNECTED:
					sb.append("Disconnected");
					break;
				case TelephonyManager.DATA_SUSPENDED:
					sb.append("Suspended");
					break;								
			}
			sb.append(". Network Type: ");
			switch (networkType) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					sb.append("1xRTT");
					break;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					sb.append("CDMA");
					break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					sb.append("EDGE");
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					sb.append("EVDO_0");
					break;				
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					sb.append("EVDO_A");
					break;
				case TelephonyManager.NETWORK_TYPE_GPRS:
					sb.append("GPRS");
					break;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					sb.append("HSDPA");
					break;			
				case TelephonyManager.NETWORK_TYPE_HSPA:
					sb.append("HSPA");
					break;			
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					sb.append("HSUPA");
					break;
				case TelephonyManager.NETWORK_TYPE_IDEN:
					sb.append("IDEN");
					break;			
				case TelephonyManager.NETWORK_TYPE_UMTS:
					sb.append("UMTS");
					break;			
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					sb.append("UNKNOWN");
					break;										
			}
			
			networkState.put(time, sb.toString());
		}

		@Override
		public void onSignalStrengthsChanged(SignalStrength signal) {
	        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss.SSS aa");
	        String time = simpleDateFormat.format(System.currentTimeMillis());
			StringBuffer sb = new StringBuffer();
			sb.append("Signal strength changed: ");
			sb.append(signal.getGsmSignalStrength());
			sb.append(" asu");
			signalStrength.put(time, sb.toString());						
		}
		
	}
	
	/*
	 * ExecuteTest is an asynchronous task that executes selected measurement test.
	 */
	private class ExecuteTest extends AsyncTask<Void, Void, ClientExecutionResults> implements MeasurementTestEventListener {

		private ProgressDialog progressDialog;
		int total_progress = 0;
					
		@Override
		public void handleStepCompletedEvent() {
			total_progress++;
			synchronized (progressDialog) {
				if (progressDialog.isShowing()) {
					progressDialog.setProgress(total_progress);		
				} 
			}
			
		}

		@Override
		protected void onPreExecute() {
			
			progressDialog = new ProgressDialog(RunMeasurementTestActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage(getResources().getString(R.string.wait_until_test_complete));
			progressDialog.setMax(2*Integer.valueOf(cyclesNumber.getEditableText().toString()) + 1);
			progressDialog.setProgress(0);
			
			progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
				
				@Override
				public boolean onKey(final DialogInterface dialog, int keyCode, KeyEvent event) {
					
					if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
						
						AlertDialog.Builder builder = new Builder(RunMeasurementTestActivity.this);						
						builder.setMessage(R.string.do_you_want_to_terminate);
						builder.setTitle(R.string.terminate_confirmation);
						builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {					
							@Override
							public void onClick(DialogInterface d, int which) {															
								d.cancel();
								ApplicationGlobalContext.getInstance().setTerminate(true);																				
							}
						});
						
						builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {					
							@Override
							public void onClick(DialogInterface d, int which) {
								d.cancel();
							}
						});
						
						builder.create().show();
						return true;
					}
					
					return true;
				}
			});
			
			progressDialog.show();
		}
		
		@Override
		protected void onPostExecute(ClientExecutionResults results) {			
			if (!results.isTerminated()) {//If the measurement test has not been terminated then analyze the measurement results and print them into the measurment test report file
				progressDialog.dismiss();
				
				if (results != null && results.getErrorMessage() == null) {
					try {
						Statistics[] statictics = ResultAnalyzer.analyzeResults(results);//do statistical analysis
						TestResultsPrinter.printTestExecutionResultsAsHTML(results, statictics);//print result to HTML-formatted file
						RunMeasurementTestActivity.this.executionResults = results;
						RunMeasurementTestActivity.this.start_group.setVisibility(View.GONE);
						RunMeasurementTestActivity.this.result_group.setVisibility(View.VISIBLE);	
						RunMeasurementTestActivity.this.testCompletedTextView.setText(getResources().getString(R.string.test_completed, results.getResultFilePath()));				
					} catch (IOException e) {
						RunMeasurementTestActivity.this.showToast(R.string.error_while_printing);
						return;
					}				
				} else {
					if (results != null) {
						if (results.getErrorMessage().equals("no connectivity")) {
							RunMeasurementTestActivity.this.showToast(R.string.network_failure_text);
							return;
						}
					}
					RunMeasurementTestActivity.this.showToast(R.string.error_occur_during_the_test);
				}
												
			} else {
				RunMeasurementTestActivity.this.showToast(R.string.terminated_toast);
				progressDialog.cancel();
			}
		}

		@Override
		protected ClientExecutionResults doInBackground(Void... params) {
			
			int cycles = Integer.valueOf(cyclesNumber.getEditableText().toString());
			ClientExecutionResults results = new ClientExecutionResults(cycles);
			results.setCycles(cycles);
			
			//Check the Internet connectivity
			final ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);		
			boolean isWiFi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
			boolean isMobileNetwork = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
			
			if (!isMobileNetwork && !isWiFi) {
				results.setErrorMessage("no connectivity");
				telephonyManager.listen(networkPhoneStateListener, PhoneStateListener.LISTEN_NONE);
				return results;
			}
			
			if (isMobileNetwork) {
				results.setMobileNetwork(true);
			}
			
			//Read information about the mobile operator
			telephonyManager.listen(networkPhoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE); 
			telephonyManager.listen(networkPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);												
			results.setOperatorName(telephonyManager.getNetworkOperatorName());
			results.setOperator(telephonyManager.getNetworkOperator());
			results.setCountry(telephonyManager.getSimCountryIso());
			results.setProtocolSpecificationName(protocolHeader.getProtocolName());
						
			
			ApplicationGlobalContext globalContext = ApplicationGlobalContext.getInstance();
			if (globalContext.isTerminateAndReturnFlag()) {
				results.setTerminated(true);
				progressDialog.cancel();
				return results;
			}
			
			//Check if specified port numbers are blocked
/*			List<Integer> blockedPorts = protocolHeader.getBlockedPorts();			 		
			if (blockedPorts != null) {					
				for (Integer portNumber: blockedPorts) {
					boolean portAvailabile = ServerConnector.isPortAvailabile(ip, port);
					results.addPortScanResult(portNumber, Boolean.valueOf(portAvailabile));					
				}
			}
						
			handleStepCompletedEvent();
*/						
						
			//Execute measurement bandwidth tests
			ServerConnector.runTest(ip,
					port,						
					protocolHeader,
					cycles,
					results,
					this);				
										
			if (results == null || results.getErrorMessage() != null) {
				return results;
			}
			
			if (results.isTerminated()) {
				progressDialog.cancel();				
				return results;
			}
															
			telephonyManager.listen(networkPhoneStateListener, PhoneStateListener.LISTEN_NONE);
			/*
			 * results.setNetworkState(networkState);
			 * results.setSignalStrength(signalStrength);
			 */
			
			
			if (globalContext.isTerminateAndReturnFlag()) {
				results.setTerminated(true);
				progressDialog.cancel();
				return results;				
			}
			
			progressDialog.getButton(ProgressDialog.BUTTON_NEUTRAL).setClickable(false);
			
			//Retrieve download bandwidth measurement results
			String serverTestExecutionResults = ServerConnector.retrieveServerTestExecutionResults(ip, 
					port, 
					results.getUUID());						
			
			if (serverTestExecutionResults != null && !serverTestExecutionResults.equals(GlobalConstants.TEST_RESULTS_NOT_FOUND) ) {
				serverTestExecutionResults = serverTestExecutionResults.substring(0, serverTestExecutionResults.length() - GlobalConstants.END_OF_MESSAGE.length());
				ClientExecutionResults.parseServerTestExecutionResults(results, serverTestExecutionResults);	
			} else {
				results.setErrorMessage("Test Failed. Error occur while retrieving measurements result form the server side");
			}
			
			handleStepCompletedEvent();
										
			return results;				
		}
	}
	
}
