package com.rutgers.kashyap.rutgersbusservice.LinkedList;

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
			Node temp = new Node(head);
			while (temp.next != null)
			{
				temp = temp.next;
			}
			temp.next = newNode;
		}
	}

}
