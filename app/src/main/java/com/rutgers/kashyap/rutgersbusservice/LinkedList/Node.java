package com.rutgers.kashyap.rutgersbusservice.LinkedList;

import java.util.ArrayList;

/**
 * Created by Kashyap on 9/19/2014.
 */
public class Node
{
	public String route;
	public String source;
	public String destination;
	public ArrayList<Double> sMinutes = new ArrayList<>();
    public ArrayList<Double> dMinutes = new ArrayList<>();

	public Node(Node node)
	{
		this.route = node.route;
		this.source = node.source;
		this.destination = node.destination;
		this.sMinutes = node.sMinutes;
        this.dMinutes = node.dMinutes;
	}
	public Node() {}
}
