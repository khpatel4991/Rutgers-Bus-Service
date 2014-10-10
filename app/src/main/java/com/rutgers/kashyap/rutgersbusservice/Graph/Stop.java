package com.rutgers.kashyap.rutgersbusservice.Graph;

import java.util.ArrayList;

/**
 * Created by Kashyap on 9/8/2014.
 */

public class Stop
{
	public int stopId;
	public double latitude;
	public double longitude;
	public String name;
	public String displayName;
	public ArrayList<Edge> incoming = new ArrayList<Edge>();
	public ArrayList<Edge> outgoing = new ArrayList<Edge>();

	public Stop(int stopId, double latitude, double longitude, String name, String displayName)
	{
		this.stopId = stopId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.displayName = displayName;
	}
}
