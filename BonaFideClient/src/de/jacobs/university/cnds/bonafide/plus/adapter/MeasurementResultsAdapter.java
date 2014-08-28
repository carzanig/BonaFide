/* THIS CLASS IS NOT USED ANYMORE. REPLACED BY ResultsAdapter
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
// 
//THIS
//

package de.jacobs.university.cnds.bonafide.plus.adapter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.plus.model.ReportFile;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;
import de.jacobs.university.cnds.bonafide.plus.utils.ServerConnector;

/**
 * MeasurementResultsAdapter is used to display the list of measurement reports files
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 */
public class MeasurementResultsAdapter extends BaseAdapter {
	
	private Context context;
	private List<ReportFile> measurementResults;
	
	private String ip = null; 	
	private Integer port = null;
	
	public MeasurementResultsAdapter(Context context, List<ReportFile> list) {
		this.context = context;
		this.measurementResults = list;
		
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		ip = mPrefs.getString(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_IP, null);
		//port = Integer.valueOf(mPrefs.getString(ApplicationGlobalContext.PREFERENCES_KEY_SENTRAL_SERVER_PORT, null));
	}

	@Override
	public int getCount() {
		return measurementResults.size();
	}

	@Override
	public Object getItem(int position) {
		return measurementResults.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int pos = position;
		View measurementResult;	
		if (convertView == null) {
			measurementResult = View.inflate(context, R.layout.measurement_result_layout, null);
		} else {
			measurementResult = convertView; 
		}
		
		TextView text = (TextView) measurementResult.findViewById(R.id.text_view_measurement_result_file_name);
		final ImageButton upload = (ImageButton) measurementResult.findViewById(R.id.image_button_upload_file);
		ImageButton delete = (ImageButton) measurementResult.findViewById(R.id.image_button_delete_file);
		
		final ReportFile file = measurementResults.get(position);
		text.setText(file.getFileName());
		
		if (file.isUploaded()) {
			upload.setVisibility(View.INVISIBLE);
		} else {
			upload.setVisibility(View.VISIBLE);
		}
		
		measurementResult.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(new File(file.getAbsolutePath())));
				intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");			
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				
				context.startActivity(intent);
			}
		});
		
		upload.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new Builder(context);
				String message = context.getResources().getString(R.string.upload_file_dialog, file.getFileName());
				builder.setMessage(message);
				builder.setTitle(R.string.upload_confirmation);
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						boolean success = ServerConnector.uploadTestResultsToServer(ip,
								port, 
								file.getAbsolutePath());
						
						dialog.cancel();
						
						if (success) {		
							
							try {
								ApplicationGlobalContext.getInstance().addFileToStorageInfo(file.getAbsolutePath());
								file.setUploaded(true);
								upload.setVisibility(View.INVISIBLE);														
								showToast(context.getResources().getString(R.string.result_uploaded));	
							} catch (IOException e) {
								showToast(context.getResources().getString(R.string.result_uploaded_fail));
							}														
						} else {
							showToast(context.getResources().getString(R.string.result_uploaded_fail));		
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
		});
		
		delete.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {		
				AlertDialog.Builder builder = new Builder(context);
				String message = context.getResources().getString(R.string.delete_file_dialog, file.getFileName());
				builder.setMessage(message);
				builder.setTitle(R.string.delete_confirmation);
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						File f = new File(file.getAbsolutePath());
						boolean del = f.delete();
						
						dialog.cancel();
						
						if (del) {						
							showToast(context.getResources().getString(R.string.file_deleted, file.getFileName()));	
						} else {
							showToast(context.getResources().getString(R.string.file_deleted_failed, file.getFileName()));
						}
						
						measurementResults.remove(pos);
						MeasurementResultsAdapter.this.notifyDataSetChanged();
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
		});
		
		return measurementResult;
	}
	
	private void showToast(String text) {
		Toast.makeText(context, text, 5).show();	
	}
	
}
