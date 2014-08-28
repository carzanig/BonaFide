package de.jacobs.university.cnds.bonafide.plus.utils.unitconverter;

public enum Unit {
	BYTES, KILOBYTES, MEGABYTES, GIGABYTES, TERABYTES,
	BITS, KILOBITS, MEGABITS, GIGABITS, TERABITS;
	
	public String getStringRepresentation() {
		switch(this) {
		case BYTES:
			return "B";
		case KILOBYTES:
			return "kB";
		case MEGABYTES:
			return "MB";
		case GIGABYTES:
			return "GB";
		case TERABYTES:
			return "TB";
		case BITS:
			return "bit";
		case KILOBITS:
			return "kbit";
		case MEGABITS:
			return "Mbit";
		case GIGABITS:
			return "Gbit";
		case TERABITS:
			return "Tbit";
		default:
			return "N/A";
		}
		
	}
	
	public static Unit getEquivalentInBits(Unit unitBytes) {
		switch (unitBytes) {
		case BYTES:
			return BITS;
		case KILOBYTES:
			return KILOBITS;
		case MEGABYTES:
			return MEGABITS;
		case GIGABYTES:
			return GIGABITS;
		case TERABYTES:
			return TERABITS;
		default:
			return null;
		}
	}
}