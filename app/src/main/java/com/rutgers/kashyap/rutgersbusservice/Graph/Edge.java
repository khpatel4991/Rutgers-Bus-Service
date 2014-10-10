package com.rutgers.kashyap.rutgersbusservice.Graph;

/**
 * Created by Kashyap on 9/8/2014.
 */
public class Edge
{
	public String startStop;
	public String endStop;
	public String route;
	public String routeDisplayName;

	public Edge(String startStop, String endStop, String route, String routeDisplayName)
	{
		this.startStop = startStop;
		this.endStop = endStop;
		this.route = route;
		this.routeDisplayName = routeDisplayName;
	}
}
