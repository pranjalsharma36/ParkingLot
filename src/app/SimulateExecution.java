package app;

import java.io.*;
import java.util.*;

public class SimulateExecution {
	public static void main (String[] args) {
		System.out.println("GfG!");
		
		// Entry Gate -------------------------------------------------------------------------
		// Parking lot
		List<List<Spot>> floorList= new ArrayList<>();
		List<Spot> sub = new ArrayList<>();
		Spot spot = new Spot(1, SpotStatus.FREE, VehicleType.CAR);
		sub.add(spot);
		floorList.add(sub);
		
        /*
        int id;
        SpotStatus status;
        VehicleType type;
        */
    
        ParkingLot parkingLot = new ParkingLot(floorList);
        int number = 123;
        Vehicle vehicle = new Car(number);
        
		
		SpotSelectionStrategy  spotSelectionStrategy = new DefaultSpotSelectionStrategy(parkingLot); // it will have parking lot info
		
		BookingManager bookingManager = new BookingManager();
		
		Gate entryGate = new EntryGate(spotSelectionStrategy, bookingManager);
		
		// Exit Gate --------------------------------------------------------------------------
		PriceCalculationStrategy priceCalculationStrategy = new DefaultPriceCalculationStrategy();
		
		PaymentStrategy paymentStrategy = new CashPaymentStrategy();
		
		Gate exitGate = new ExitGate(priceCalculationStrategy, paymentStrategy, bookingManager);
		
		// executing simulation
		
		System.out.println("Executing Simulation");
		
		
		entryGate.pass(vehicle);
		
		exitGate.pass(vehicle);
		
	}
}

interface Gate{
    
    public void pass(Vehicle vehicle);
}

// TODO : we can have managers for entry gate and exit gate

class EntryGate implements Gate{
    
    // spot selection
    SpotSelectionStrategy spotSelectionStrategy;
    //createBooking
    BookingManager bookingManager;
    
    // Construcotr
    EntryGate(SpotSelectionStrategy spotSelectionStrategy, BookingManager bookingManager){
        this.spotSelectionStrategy = spotSelectionStrategy;
        this.bookingManager = bookingManager;
    }
    
    public void pass(Vehicle vehicle){
        
        // this strategy will have parking lot info1
        Spot spot = spotSelectionStrategy.getSpot(vehicle.type);
        
        bookingManager.createBooking(vehicle, spot, System.currentTimeMillis());
        spot.status = SpotStatus.OCCUPIED;
        System.out.println("Please go to spot "+ spot.id);
    }
    
}

enum SpotStatus{
    FREE,
    OCCUPIED
}

class ExitGate implements Gate{
    
    // calculate Price
    
    PriceCalculationStrategy priceCalculationStrategy;
    // take Payment
    PaymentStrategy paymentStrategy;
    
    // free the spot & remove booking
    BookingManager bookingManager;
    
    // construcor
    ExitGate(PriceCalculationStrategy priceCalculationStrategy, PaymentStrategy paymentStrategy, BookingManager bookingManager){
        
        this.priceCalculationStrategy = priceCalculationStrategy;
        this.paymentStrategy = paymentStrategy;
        this.bookingManager = bookingManager;
    }
    
    public void pass(Vehicle vehicle){
        
        Booking booking = bookingManager.getBooking(vehicle.number);
        // this steategy will have info of booking
        int amount = priceCalculationStrategy.calculateAmount(System.currentTimeMillis(), booking);
        // we can have a log statement here
        
        paymentStrategy.pay(amount);
        
        booking.spot.status = SpotStatus.FREE;
        bookingManager.removeBooking(vehicle.number);
        
    }
}

class Booking{
    
    Vehicle vehicle;
    Spot spot;
    long start_time;
    
    // we can identify a unique booking using a unique vehicle number
    
    // constructor
    Booking(Vehicle vehicle, Spot spot, long start_time){
        this.vehicle = vehicle;
        this.spot = spot;
        this.start_time = start_time;
    }
}

class BookingManager{
    
    HashMap<Integer, Booking> bookingCatalog;
    
    BookingManager(){
        bookingCatalog = new HashMap();
    }
    
    public void createBooking(Vehicle vehicle, Spot spot, long start_time){
        Booking booking = new Booking(vehicle, spot, start_time);
        bookingCatalog.put(vehicle.number, booking);
        System.out.println("Booking created successfully for " + vehicle.number);
    }
    
    public void removeBooking(int number){
        bookingCatalog.remove(number);
        System.out.println("Booking removed successfully for " + number);
    }
    
    public Booking getBooking(int number){
        return bookingCatalog.get(number);
    }
}

class Vehicle{
    
    int number;
    VehicleType type;
    Vehicle(int number, VehicleType type){
        this.number = number;
        this.type = type;
        
    }
}

class Car extends Vehicle{
    
    
    Car(int number){
        super(number, VehicleType.CAR);
    }
}

enum VehicleType{
    BIKE,
    CAR,
    BUS
}

class Spot{
    
    int id;
    SpotStatus status;
    VehicleType type;
    
    // constructor
    Spot(int id, SpotStatus status, VehicleType type){
        this.id = id;
        this.status = status;
        this.type =type;
    }
}

class ParkingLot{
    
    // initialize parking lot
    // manager for spots
    
    List<List<Spot>> floorList; // each floor will have a list of spots todo : better naming
    
    // constructor
    ParkingLot(List<List<Spot>> floorList){
        this.floorList = floorList;
    }
}

interface PaymentStrategy{
    
    public void pay(int amount);
}

class CashPaymentStrategy implements PaymentStrategy{
    
    public void pay(int amount){
        System.out.println("Paid using Cash " + amount);
    }
}

interface SpotSelectionStrategy{
    
    public Spot getSpot(VehicleType type); // will have Parkinng lot info
}

class DefaultSpotSelectionStrategy implements SpotSelectionStrategy{
    
    ParkingLot parkingLot;
    
    // constructor
    DefaultSpotSelectionStrategy(ParkingLot parkingLot){
        this.parkingLot = parkingLot;   
    }
    
    public Spot getSpot(VehicleType type){
        // tofo : for every floor we can have 2 separate list : using simplified version now
        
        List<List<Spot>> floorList = parkingLot.floorList;
        
        for(List<Spot> spots : floorList){
            
            for(Spot spot : spots){
                
                if(spot.status == SpotStatus.FREE && spot.type == type){
                    return spot;
                }
            }
        }
        return null;
    }
    
    // we can throw exception and a message related to no spot as free
}

interface PriceCalculationStrategy{
    
    public int calculateAmount(long end_time, Booking booking);
}


class DefaultPriceCalculationStrategy implements PriceCalculationStrategy{
    
    public int calculateAmount(long end_time, Booking booking){
        
        int amount = (int)(end_time - booking.start_time)*10;
        
        // TODO : we can have more sophesticated logic based on vehicle type etc
        return amount;
    }
}

/*
Gate
Entry Gste
, ExitGate
vehicle
TwoWheelerVrhicle etc
VehicleType

Parkinglot
floors
Slot

ticket
Booking
BookingsManager

Price Calculation Strategy
PaymentStrategy
SpotSelectionStrategy

*/