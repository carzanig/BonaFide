package de.jacobs.university.cnds.bonafide.plus.rest.model;

import com.google.gson.annotations.SerializedName;

/**
 * Moder representing drawable result-objects on map
 * @author Tomas
 *
 */

public class DrawableResult {
	@SerializedName("south_west_latitude")
	private double southWestLatitude=0;
	@SerializedName("south_west_longitude")
	private double southWestLongitude=0;
	
	@SerializedName("north_east_latitude")
	private double northEastLatitude=0;
	@SerializedName("north_east_longitude")
	private double northEastLongitude=0;
	
	/**
	 * Range 0-100
	 */
	@SerializedName("quality")
	private int quality=0; // 0-100
	
	public DrawableResult() {
		
	}

	public double getSouthWestLatitude() {
		return southWestLatitude;
	}

	public void setSouthWestLatitude(double southWestLatitude) {
		this.southWestLatitude = southWestLatitude;
	}

	public double getSouthWestLongitude() {
		return southWestLongitude;
	}

	public void setSouthWestLongitude(double southWestLongitude) {
		this.southWestLongitude = southWestLongitude;
	}

	public double getNorthEastLatitude() {
		return northEastLatitude;
	}

	public void setNorthEastLatitude(double northEastLatitude) {
		this.northEastLatitude = northEastLatitude;
	}

	public double getNorthEastLongitude() {
		return northEastLongitude;
	}

	public void setNorthEastLongitude(double northEastLongitude) {
		this.northEastLongitude = northEastLongitude;
	}

	public int getQuality() {
		if (quality>100) {
			return 100;
		}
		if (quality<0) {
			return 0;
		}
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}
	
	
}
