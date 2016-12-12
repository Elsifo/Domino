package it.beyondthecube.domino.exceptions;

import it.beyondthecube.domino.data.database.SQLQuery;

public class DatabaseException extends Exception 
{
	private static final long serialVersionUID = 2L;
	private SQLQuery q;
	private Exception prevEx;
	public DatabaseException(SQLQuery q, Exception prevEx, String mex)
	{
		super(mex);
		this.q=q;
		this.prevEx=prevEx;
		prevEx.printStackTrace();
	}
	public SQLQuery getQuery()
	{
		return q;
	}
	public void printPreviousStackTrace()
	{
		prevEx.printStackTrace();
	}
}
