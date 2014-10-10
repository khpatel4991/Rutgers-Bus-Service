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
	public ArrayList<Double> minutes = new ArrayList<Double>();
	public Node next;

	public Node(String route, String source, String destination, ArrayList<Double> minutes)
	{
		this.next = null;
		this.route = route;
		this.source = source;
		this.destination = destination;
		this.minutes = minutes;
	}

	public Node(Node node)
	{
		this.next = node.next;
		this.route = node.route;
		this.source = node.source;
		this.destination = node.destination;
		this.minutes = node.minutes;
	}

}
