package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	
	public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTStamp = ticket.getInTime().getTime();
        long outTStamp = ticket.getOutTime().getTime();

        long duration = outTStamp - inTStamp;
        double durationHour = ((duration / 1000.0) / 60.0) / 60.0;
        
        if (durationHour < 0.5){
        	ticket.setPrice(0.0);
        }
        
        else {
	        	switch (ticket.getParkingSpot().getParkingType()){
	            case CAR: {
	                ticket.setPrice(durationHour * Fare.CAR_RATE_PER_HOUR);
	                break;
	            }
	            case BIKE: {
	                ticket.setPrice(durationHour * Fare.BIKE_RATE_PER_HOUR);
	                break;
	            }
	            default: throw new IllegalArgumentException("Unkown Parking Type");
	        }
        }

        
    }
	
	public void calculateFare(Ticket ticket){
		calculateFare(ticket, false);
	}
}