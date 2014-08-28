package de.jacobs.university.cnds.bonafide.plus.utils.unitconverter;


public class ConvertedUnit {
	private double value;
	private Unit unit;
	
	public ConvertedUnit(double value, Unit unit) {
		this.value=value;
		this.unit=unit;
	}
	
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public Unit getUnit() {
		return unit;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public ConvertedUnit getRepresentationInBits() {
		return new ConvertedUnit(value*8, Unit.getEquivalentInBits(this.unit)); // convert bytes to bits
	}
	
	public String toString() {
		double roundedValue=Math.round(this.value * 100.0) / 100.0; // 2 decimal places
		return roundedValue+" "+this.unit.getStringRepresentation();
	}
}