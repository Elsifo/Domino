package it.beyondthecube.domino.exceptions;

public class ParseException extends Exception
{
	private static final long serialVersionUID = 10L;
	public ParseException(String error)
	{
		super(error);
	}
}
