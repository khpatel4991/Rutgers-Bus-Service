package com.rutgers.kashyap.rutgersbusservice.LinkedList;

import android.util.Log;

/**
 * Created by Kashyap on 9/19/2014.
 */
public class LinkedList
{

	public Node head;

	public LinkedList()
	{
		head = null;
	}

	public void append(Node newNode)
	{
		if (head == null) head = newNode;
		else
		{
			Log.d("LL", "in else in append");
			Node temp = new Node(head);
			while (temp.next != null) temp = temp.next;
			temp.next = newNode;
			Log.d("LL", "In append: size = " + size());
		}
	}

	public String printList()
	{
		if(head == null) return "No Routes";
		StringBuilder output = new StringBuilder();
		Node temp = new Node(head);
		while(temp != null)
		{
			output.append(temp.route + "\n");
			output.append("From: " + temp.source + " ");
			output.append("To: " + temp.destination + "\n");
			output.append("Time:\n");

			for (int i = 0; i < temp.minutes.size(); i++)
				output.append(temp.minutes.get(i) + ",");
			temp = temp.next;
		}
		Log.d("LL", "Final = " + output.toString());
		return output.toString();
	}

	public int size()
	{
		int s = 0;
		Node temp = new Node(head);
		while(temp != null)
		{
			s++;
			temp = temp.next;
		}
		return s;
	}

}
