package it.beyondthecube.domino.exceptions;

import it.beyondthecube.domino.residents.Resident;

public class CityNotFoundException extends Exception
{
	private static final long serialVersionUID = 6L;
	private Resident r;
     public CityNotFoundException(Resident r)
     {
    	 super("");
    	 this.r=r;
     }
     public CityNotFoundException() 
     {
		super("");
     }
     public Resident getResident()
     {
    	 return r;
     }
}
