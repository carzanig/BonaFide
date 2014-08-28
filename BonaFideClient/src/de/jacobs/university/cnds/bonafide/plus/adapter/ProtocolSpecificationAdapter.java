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


package de.jacobs.university.cnds.bonafide.plus.adapter;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;
import de.jacobs.university.cnds.bonafide.plus.utils.ServerConnector;

/**
 * ProtocolSpecificationAdapter is used to display the list of available protocol specifications
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class ProtocolSpecificationAdapter extends BaseAdapter {
	
	private List<String> protocolSpecifications;
	private Context context;
	
	private String ip = null; 	
	private Integer port = null;
	
	public ProtocolSpecificationAdapter(List<String> protocolSpecifications, Context context) {
		this.protocolSpecifications = protocolSpecifications;
		this.context = context;
		
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		ip = mPrefs.getString(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_IP, null);
		//port = Integer.valueOf(mPrefs.getString(ApplicationGlobalContext.PREFERENCES_KEY_SENTRAL_SERVER_PORT, null));
	}

	@Override
	public int getCount() {		
		return protocolSpecifications.size();
	}

	@Override
	public Object getItem(int position) {
		return protocolSpecifications.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View protocol;	
		if (convertView == null) {
			protocol = View.inflate(context, R.layout.protocol_item_layout, null);
		} else {
			protocol = convertView; 
		}
		
		final String protocolSpecificationName = protocolSpecifications.get(position);
		TextView name = (TextView) protocol.findViewById(R.id.text_protocol_name);
		name.setText(protocolSpecificationName);
		
		protocol.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {						
				ApplicationGlobalContext globalContext = ApplicationGlobalContext.getInstance();
								
				ProtocolDescription protocolHeader = globalContext.getProtocolHeader(protocolSpecificationName);
			//	if (protocolHeader == null) {
					
					protocolHeader = ServerConnector.getProtocolDescription(ip,
							port,
							protocolSpecificationName);
					
					if (protocolHeader == null) {
						showToast(context.getResources().getString(R.string.download_protocol_description_file_failed));
						return;
					}
					
					globalContext.putProtocolHeader(protocolHeader);					
			//	}
				/*Intent intent = new Intent(context, RunMeasurementTestActivity.class);
				intent.putExtra(ApplicationGlobalContext.BUNDLE_PROTOCOL_HEADER, protocolHeader);
				context.startActivity(intent);*/				
			}
		});
		
		return protocol;		
	}
	
	private void showToast(String text) {
		Toast.makeText(context, text, 5).show();	
	}

}
