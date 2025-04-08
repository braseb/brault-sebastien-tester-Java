package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static FareCalculatorService fareCalculatorService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    
    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){
    	dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Ticket ticketCheck = ticketDAO.getTicket("ABCDEF");
        
        assertNotNull(ticketCheck);
        assertEquals(1, ticketCheck.getId());
        assertEquals("ABCDEF", ticketCheck.getVehicleRegNumber());
        assertNotNull(ticketCheck.getInTime());
        assertNull(ticketCheck.getOutTime());
        assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
                
        
        
       
    }

    @Test
    public void testParkingLotExit() {
        
    	testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
                
        Ticket ticketCheck = ticketDAO.getTicket("ABCDEF");
         
        assertNotNull(ticketCheck);
        assertNotNull(ticketCheck.getOutTime());
        assertEquals(0.0, ticketDAO.getTicket("ABCDEF").getPrice());
        assertEquals(1, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
       
        
       
    }
    
    @Test
    public void testParkingLotExitRecurringUser() throws Exception{
    	
    	
    	testParkingLotExit();
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	parkingService.processIncomingVehicle();
    	parkingService.processExitingVehicle();
    	
    	assertTrue(ticketDAO.getNbTicket("ABCDEF")>1);
    	
    	Ticket ticket = ticketDAO.getTicket("ABCDEF");
    	//Change the outTime in the database for test the calculateFare function
        Date outTime = new Date(ticket.getInTime().getTime() + (60*60*1000));
        ticket.setOutTime(outTime);
        fareCalculatorService.calculateFare(ticket, ticketDAO.getNbTicket("ABCDEF")>1);
        ticketDAO.updateTicket(ticket);
        
        
    	
        assertEquals(Fare.CAR_RATE_PER_HOUR * 0.95, ticketDAO.getTicket("ABCDEF").getPrice());
    	
    	
    }
    

}
