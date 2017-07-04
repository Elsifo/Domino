package it.beyondthecube.domino.exceptions;

import it.beyondthecube.domino.residents.Resident;

public class InsufficientRankException extends Exception 
{
	private static final long serialVersionUID = 3L;
	private Resident adder;
	public InsufficientRankException(Resident adder, String msg)
	{
		super(msg);
		this.adder=adder;
	}
	public Resident getAdder()
	{
		return adder;
	}
}
