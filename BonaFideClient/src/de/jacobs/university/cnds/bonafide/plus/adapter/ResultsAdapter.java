package de.jacobs.university.cnds.bonafide.plus.adapter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.plus.activities.FrontendActivity;
import de.jacobs.university.cnds.bonafide.plus.activities.ResultsActivity;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementResult;
import de.jacobs.university.cnds.bonafide.plus.utils.unitconverter.UnitConverter;

public class ResultsAdapter extends BaseExpandableListAdapter {
	public static final int VIEW_ID=R.layout.result_listitem;
	public static final int CHILD_VIEW_ID=R.layout.result_listitem_child;
	
	private Context _context;
    private List<MeasurementResult> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<MeasurementResult, List<MeasurementResult>> _listDataChild;
    
    public ResultsAdapter(Context context, List<MeasurementResult> listDataHeader,
            HashMap<MeasurementResult, List<MeasurementResult>> listChildData) {
    	this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

	@Override
    public Object getChild(int groupPosition, int childPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosition);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View view, ViewGroup parent) {
 
        //final String childText = (String) getChild(groupPosition, childPosition);
 
        //if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(CHILD_VIEW_ID, null);
        //}
 
        // get results
        MeasurementResult result = (MeasurementResult) getChild(groupPosition, childPosition);
        
        // print general informations
        appendHeaderLineToView(view, view.getContext().getString(R.string.result_list_child_measurement));
        
        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_protocol), result.getProtocolName());
        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_latency), String.valueOf(result.getLatency())+" ms");
        if (result.isMobileNetwork()) {
	        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_operator), result.getOperatorName()+" ("+result.getOperator()+")");
	        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_network_type), result.getNetworkType());
	        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_country), result.getCountry());
	        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_signal), String.valueOf(result.getSignalStrength()));
        }
        else {
        	// WiFi
        	appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_network_type), view.getContext().getString(R.string.result_list_child_network_type_wifi));
        }
        
        appendHeaderLineToView(view, view.getContext().getString(R.string.result_list_child_location));
        
        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_latitude), String.valueOf(result.getLatitude()==MeasurementResult.UNKNOWN_LATITUDE ? view.getContext().getString(R.string.result_list_child_unknown_location) : result.getLatitude()));
        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_longitude), String.valueOf(result.getLongitude()==MeasurementResult.UNKNOWN_LONGITUDE ? view.getContext().getString(R.string.result_list_child_unknown_location) : result.getLongitude()));
        
        if (result.getLatitude()!=MeasurementResult.UNKNOWN_LATITUDE && result.getLongitude()!=MeasurementResult.UNKNOWN_LONGITUDE) {
        	appendMapLocatorButtonToView(view, result.getLatitude(), result.getLongitude(), result.getProtocolName(), view.getContext().getString(R.string.result_list_child_show_on_map_button));
        }
        
        // download
        appendHeaderLineToView(view, view.getContext().getString(R.string.result_list_child_download));
        
        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_protocol_flow), getHumanReadableBandwidthInBits(result.getDownloadProtocolBandwidth()));
        appendResultLineToView(view, "", getHumanReadableBandwidthInBytes(result.getDownloadProtocolBandwidth()));
        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_random), getHumanReadableBandwidthInBits(result.getDownloadRandomBandwidth()));
        appendResultLineToView(view, "", getHumanReadableBandwidthInBytes(result.getDownloadRandomBandwidth()));
        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_total_traffic), getHumanReadableBytes(result.getDownloadTotalBytes()));
        
        // upload
        appendHeaderLineToView(view, view.getContext().getString(R.string.result_list_child_upload));
        
        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_protocol_flow), getHumanReadableBandwidthInBits(result.getUploadProtocolBandwidth()));
        appendResultLineToView(view, "", getHumanReadableBandwidthInBytes(result.getUploadProtocolBandwidth()));
        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_random), getHumanReadableBandwidthInBits(result.getUploadRandomBandwidth()));
        appendResultLineToView(view, "", getHumanReadableBandwidthInBytes(result.getUploadRandomBandwidth()));
        appendResultLineToView(view, view.getContext().getString(R.string.result_list_child_total_traffic), getHumanReadableBytes(result.getUploadTotalBytes()));
        
        return view;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View view, ViewGroup parent) {
        MeasurementResult result = (MeasurementResult) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(VIEW_ID, null);
        }
 
        TextView server = (TextView) view.findViewById(R.id.result_listitem_textView1);
		TextView date = (TextView) view.findViewById(R.id.result_listitem_textView2);
		TextView protocol = (TextView) view.findViewById(R.id.result_listitem_textView3);
		
		if (result.getMeasurementServerName()!=null && result.getMeasurementServerName().trim().length()>0) {
			server.setText(result.getMeasurementServerName());
		}
		else {
			server.setText("Unknown server");
		}
		
		
		if (result.getDate()!=null) {
			SimpleDateFormat formatter = new SimpleDateFormat();
			date.setText(formatter.format(result.getDate()));
		}
		else {
			date.setText("N/A");
		}
		
		protocol.setText(result.getProtocolName());
 
        return view;
    }
    
    private String getHumanReadableBytes(long bytes) {
    	// default = bytes
    	return UnitConverter.convertBytesToHumanReadableUnit(bytes).toString();
    }
    
    private String getHumanReadableBandwidthInBytes(long bandwidth) {
    	return UnitConverter.convertBytesToHumanReadableUnit(bandwidth).toString()+"/s";
    }
    
    private String getHumanReadableBandwidthInBits(long bandwidth) {
    	// default = bytes
    	return UnitConverter.convertBytesToHumanReadableUnitInBits(bandwidth).toString()+"/s";
    }
    
    private View appendMapLocatorButtonToView(View view, final double latitude, final double longitude, final String pointName, String buttonText) {
    	Button button = new Button(view.getContext());
    	button.setText(buttonText);
    	button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				FrontendActivity.getFrontendActivity().moveCameraToMeasurementServer(latitude, longitude, pointName,"");
				((ResultsActivity) view.getContext()).finish();
			}
		});
    	
    	LinearLayout resultsView = (LinearLayout) view.findViewById(R.id.result_list_child_results);
    	resultsView.addView(button);
    	
    	return view;
    }
    
    private View appendResultLineToView(View view, String label, String value) {
    	LinearLayout resultsView = (LinearLayout) view.findViewById(R.id.result_list_child_results);
    	
    	// create new entry
    	LinearLayout newEntry = new LinearLayout(view.getContext());
    	newEntry.setOrientation(LinearLayout.HORIZONTAL);
    	
    	TextView labelView = new TextView(view.getContext());
    	labelView.setText(label);
    	labelView.setWidth(0);
    	labelView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1L));
    	newEntry.addView(labelView);
    	
    	TextView valueView = new TextView(view.getContext());
    	valueView.setText(value);
    	newEntry.addView(valueView);
    	
    	resultsView.addView(newEntry);
    	
    	return view;
    }
    private View appendHeaderLineToView(View view, String header) {
    	LinearLayout resultsView = (LinearLayout) view.findViewById(R.id.result_list_child_results);
    	
    	TextView headerView = new TextView(view.getContext());
    	headerView.setText(header);
    	headerView.setTextAppearance(view.getContext(), android.R.style.TextAppearance_Medium);
    	
    	resultsView.addView(headerView);
    	
    	return view;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
	
	/*public ResultsAdapter(Context context, int resource, List<MeasurementResult> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		MeasurementResult result = getItem(position);
		if (view==null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(VIEW_ID, null);
		}
		
		TextView server = (TextView) view.findViewById(R.id.result_listitem_textView1);
		TextView date = (TextView) view.findViewById(R.id.result_listitem_textView2);
		TextView protocol = (TextView) view.findViewById(R.id.result_listitem_textView3);
		
		server.setText("Server");
		date.setText("date");
		protocol.setText("SIP");
		
		return view;
		//return super.getView(position, view, parent);
	}*/
}
