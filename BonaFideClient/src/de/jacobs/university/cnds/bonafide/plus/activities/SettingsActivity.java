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


package de.jacobs.university.cnds.bonafide.plus.activities;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;
import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.plus.rest.RESTServiceProvider;
import de.jacobs.university.cnds.bonafide.plus.services.BonafideService;
import de.jacobs.university.cnds.bonafide.plus.tasks.RedrawMeasurementServersTask;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;
import de.jacobs.university.cnds.bonafide.plus.utils.LocationProviderAvailabilityChecker;


/**
 * Settings Activity allows to store the measurement server connection parameter in shared preferences storage mechanism 
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private EditTextPreference centralServerUrlPreference;
	private EditTextPreference centralServerUserToken;
	private SwitchPreference automeasurementActivePreference;
	private EditTextPreference automeasurementTresholdTimePreference;
	private EditTextPreference automeasurementTresholdMovementPreference;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		centralServerUrlPreference = (EditTextPreference) getPreferenceScreen().findPreference(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_IP);
		centralServerUserToken = (EditTextPreference) getPreferenceScreen().findPreference(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_USER_TOKEN);
		automeasurementActivePreference = (SwitchPreference) getPreferenceScreen().findPreference(ApplicationGlobalContext.PREFERENCES_KEY_AUTOMEASUREMENT_ACTIVE);
		automeasurementTresholdTimePreference = (EditTextPreference) getPreferenceScreen().findPreference(ApplicationGlobalContext.PREFERENCES_KEY_AUTOMEASUREMENT_TRESHOLD_TIME);
		automeasurementTresholdMovementPreference = (EditTextPreference) getPreferenceScreen().findPreference(ApplicationGlobalContext.PREFERENCES_KEY_AUTOMEASUREMENT_TRESHOLD_MOVEMENT);
		
		automeasurementTresholdTimePreference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		automeasurementTresholdMovementPreference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		
		
		centralServerUrlPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					URL checkUrl = new URL((String)newValue);
				} catch (MalformedURLException e) {
					// URL is invalid. Dont accept
					showToast(getString(R.string.settings_central_server_host_invalid_url));
					return false;
				}
				
				return true;
			}
		});
		
		
		// boundaries check
		automeasurementTresholdTimePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int value = Integer.valueOf((String)newValue);
				if (value<5) {
					showToast(getString(R.string.settings_automeasurement_treshold_time_invalid_boundaries));
					return false;
				}
				return true;
			}
		});
		automeasurementTresholdMovementPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int value = Integer.valueOf((String)newValue);
				if (value<50) {
					showToast(getString(R.string.settings_automeasurement_treshold_movement_invalid_boundaries));
					return false;
				}
				return true;
			}
		});
		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(ApplicationGlobalContext.PREFERENCES_KEY_AUTOMEASUREMENT_ACTIVE) || key.equals(ApplicationGlobalContext.PREFERENCES_KEY_AUTOMEASUREMENT_TRESHOLD_TIME) || key.equals(ApplicationGlobalContext.PREFERENCES_KEY_AUTOMEASUREMENT_TRESHOLD_MOVEMENT)) {
			automeasurementPropertiesChanged();
		}
		else if (key.equals(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_IP)) {
			// update central service url
			RESTServiceProvider.getInstance().setServiceUrl(centralServerUrlPreference.getText());
			
			// update view from the new url
			new RedrawMeasurementServersTask().execute();
			FrontendActivity.getFrontendActivity().startResultsTask();
		}
		else if (key.equals(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_USER_TOKEN)) {
			// update central service url
			RESTServiceProvider.getInstance().setUserToken(centralServerUserToken.getText());
		}
	}
	
	/**
	 * updates the automeasurement service
	 */
	private void automeasurementPropertiesChanged() {
		// send changes to the BonaFideService, which handles automatic measurements
		Intent intent = new Intent(this, BonafideService.class);
		intent.putExtra(BonafideService.BONAFIDE_SERVICE_ACTION_KEY, BonafideService.BONAFIDE_SERVICE_UPDATE_PREFERENCES);
		intent.putExtra(BonafideService.BONAFIDE_SERVICE_PREFERENCE_ACTIVE, automeasurementActivePreference.isChecked());
		intent.putExtra(BonafideService.BONAFIDE_SERVICE_PREFERENCE_TRESHOLD_TIME, (int)Integer.valueOf(automeasurementTresholdTimePreference.getText()));
		intent.putExtra(BonafideService.BONAFIDE_SERVICE_PREFERENCE_TRESHOLD_MOVEMENT, (int)Integer.valueOf(automeasurementTresholdMovementPreference.getText()));
		
		startService(intent);
		
		LocationProviderAvailabilityChecker.requestLocationProviderPermissionWhenDisabled(this);
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}
	
	

}
