package de.jacobs.university.cnds.bonafide.plus.utils;

import android.content.Context;

public class RessourcesUtils {
	public static String getStringResourceByName(Context context, String input) {
	    String packageName = context.getPackageName();
	    int resId = context.getResources()
	            .getIdentifier(input, "string", packageName);
	    if (resId == 0) {
	        return input;
	    } else {
	        return context.getString(resId);
	    }
	}
}
