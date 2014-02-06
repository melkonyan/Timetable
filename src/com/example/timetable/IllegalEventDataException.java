package com.example.timetable;

public class IllegalEventDataException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6515663823607585327L;

	public IllegalEventDataException(String message) {
		super(message);
	}
	
	public IllegalEventDataException() {
		super();
	}
	
}
