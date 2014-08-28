package de.jacobs.university.cnds.bonafide.plus.utils.unitconverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class UnitConverter {
	private static List<Unit> unitSequence = new ArrayList<Unit>();
	static {
		unitSequence.add(Unit.KILOBYTES);
		unitSequence.add(Unit.MEGABYTES);
		unitSequence.add(Unit.GIGABYTES);
		unitSequence.add(Unit.TERABYTES);
	}
	
	public static ConvertedUnit convertBytesToHumanReadableUnit(long bytes) {
		double result = bytes;
		Unit unit=Unit.BYTES;
		
		Iterator<Unit> iter = unitSequence.iterator();
		while (iter.hasNext() && result>=1024) {
			result=result/1024;
			unit=iter.next();
		}
		
		return new ConvertedUnit(result, unit);
	}
	
	public static ConvertedUnit convertBytesToHumanReadableUnitInBits(long bytes) {
		
		ConvertedUnit bytesRepresentation = new ConvertedUnit(bytes, Unit.BYTES);
		ConvertedUnit bitsRepresentation = bytesRepresentation.getRepresentationInBits();
		
		double result = bitsRepresentation.getValue();
		Unit unit = bitsRepresentation.getUnit(); // bits
		
		Iterator<Unit> iter = unitSequence.iterator();
		while (iter.hasNext() && result>=1024) {
			result=result/1024;
			unit=Unit.getEquivalentInBits(iter.next());
		}
		
		return new ConvertedUnit(result, unit);
	}
}
