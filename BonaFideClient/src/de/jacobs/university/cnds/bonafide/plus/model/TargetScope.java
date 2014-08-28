package de.jacobs.university.cnds.bonafide.plus.model;

import android.content.Context;
import de.jacobs.university.cnds.bonafide.plus.utils.RessourcesUtils;

public class TargetScope {
	private Context context;
	private String name;
	
	public TargetScope(Context context, String name) {
		this.context=context;
		this.name=name;
	}
	
	public String getHumanReadableName() {
		return RessourcesUtils.getStringResourceByName(context, "scopes_names_"+this.name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return getHumanReadableName();
	}
	
	public boolean equals (Object other) {
		if (other instanceof TargetScope) {
			TargetScope otherScope = (TargetScope) other;
			return otherScope.getName().equals(this.name);
		}
		return false;
	}
}
