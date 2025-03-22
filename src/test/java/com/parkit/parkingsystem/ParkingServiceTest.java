package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    private static Ticket ticket;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            
        	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        	//Ticket ticket = new Ticket();
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTestWithoutDiscount(){
    	when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);
    	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	parkingService.processExitingVehicle();
        verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        
    }
    
       
    @Test
    public void processExitingVehicleTestWithDiscount(){
    	when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
    	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	parkingService.processExitingVehicle();
        verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }
    
    @Test
    public void processExitingVehicleTestUnableUpdate() {
    	when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);
    	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
    	parkingService.processExitingVehicle();
    	verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
    	verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
    	
    }
    
    @Test
    public void testProcessIncomingVehicleWithDiscount() {
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
    	when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
    	lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	parkingService.processIncomingVehicle();
    	
    	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
    	verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
    	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }
    
    @Test
    public void testProcessIncomingVehicle() {
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
    	when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);
    	lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	parkingService.processIncomingVehicle();
    	
    	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
    	verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
    	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }
    
    @Test
    public void testProcessIncomingVehicleTypeBike() {
    	when(inputReaderUtil.readSelection()).thenReturn(2);
    	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(4);
    	when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);
    	lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	parkingService.processIncomingVehicle();
    	
    	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.BIKE);
    	verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
    	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailable() {
    	try {
			lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
    	lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
    	assertEquals(1, parkingSpot.getId());
    	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
    	
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
    	try {
			lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);
    	lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
    	assertEquals(null, parkingSpot);
    	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
    	try {
			lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	when(inputReaderUtil.readSelection()).thenReturn(3);
    	lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
    	assertEquals(null, parkingSpot);
    	
    }
    

}
