package de.jacobs.university.cnds.bonafide.plus.gui.components;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.plus.rest.model.DrawableResultFilter;

public class FilterMultiSpinner extends Spinner implements
		OnMultiChoiceClickListener, OnCancelListener {

	private DrawableResultFilter filter;
	private List<String> items;
	private boolean[] selected;
	private String defaultText;
	private MultiSpinnerListener listener;

	public FilterMultiSpinner(Context context) {
		super(context);
	}

	public FilterMultiSpinner(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
	}

	public FilterMultiSpinner(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		if (isChecked)
			selected[which] = true;
		else
			selected[which] = false;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// refresh text on spinner
		StringBuffer spinnerBuffer = new StringBuffer();
		boolean someUnselected = false;
		List<String> selectedStrings=new ArrayList<String>();
		for (int i = 0; i < items.size(); i++) {
			if (selected[i] == true) {
				selectedStrings.add(items.get(i));
				spinnerBuffer.append(items.get(i));
				spinnerBuffer.append(", ");
			} else {
				someUnselected = true;
			}
		}
		String spinnerText;
		if (someUnselected) {
			spinnerText = spinnerBuffer.toString();
			if (spinnerText.length() > 2)
				spinnerText = spinnerText
						.substring(0, spinnerText.length() - 2);
		} else {
			spinnerText = defaultText;
		}
		// ignore previous
		spinnerText = defaultText;
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item,
				new String[] { spinnerText });
		setAdapter(adapter);
		listener.onItemsSelected(filter, selectedStrings);
	}

	@Override
	public boolean performClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMultiChoiceItems(
				items.toArray(new CharSequence[items.size()]), selected, this);
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		builder.setOnCancelListener(this);
		builder.show();
		return true;
	}
	
	public void setItems(DrawableResultFilter filter, List<DrawableResultFilter> activeFilters, String defaultText,
			MultiSpinnerListener listener) {
		this.filter=filter;
		this.items = filter.getFilterOptions();
		this.defaultText = defaultText;
		this.listener = listener;

		// lookup which should be checked by default
		selected = new boolean[items.size()];
		
		if (activeFilters!=null && activeFilters.size()>0 && activeFilters.contains(filter)) {
			// we have active filters. Check only what should be checked
			DrawableResultFilter activeFilter=activeFilters.get(activeFilters.indexOf(filter));
			List<String> activeOptions = activeFilter.getFilterOptions();
			
			for (int i = 0; i < selected.length; i++) {
				if (activeOptions.contains(items.get(i))) {
					// is active
					selected[i] = true;
				}
				else {
					selected[i] = false;
				}
			}
		}
		else {
			for (int i = 0; i < selected.length; i++)
				selected[i] = false;
		}

		// all text on the spinner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item, new String[] { defaultText });
		setAdapter(adapter);
	}

	/*public void setItems1(List<String> items, String allText,
			MultiSpinnerListener listener) {
		this.items = items;
		this.defaultText = allText;
		this.listener = listener;

		// all selected by default
		selected = new boolean[items.size()];
		for (int i = 0; i < selected.length; i++)
			selected[i] = true;

		// all text on the spinner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item, new String[] { allText });
		setAdapter(adapter);
	}*/

	public interface MultiSpinnerListener {
		public void onItemsSelected(DrawableResultFilter filter, List<String> selectedStrings);
	}
}