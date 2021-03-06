package application.model;

import java.util.HashMap;
import java.util.LinkedList;

import java.util.ArrayList;
import java.util.Collections;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class BusSystem implements MetroSystem {
    private HashMap<Integer, BusStop> stops;
    private HashMap<Integer, BusRoute> routes;
    private HashMap<Integer, Bus> buses;
    private HashMap<Integer, Road> roads;
   
    public BusSystem() {
        stops = new HashMap<Integer, BusStop>();
        routes = new HashMap<Integer, BusRoute>();
        buses = new HashMap<Integer, Bus>();
        roads = new HashMap<>();
    }

    public BusStop getStop(int stopID) {
        if (stops.containsKey(stopID)) { return stops.get(stopID); }
        return null;
    }

    public BusRoute getRoute(int routeID) {
        if (routes.containsKey(routeID)) { return routes.get(routeID); }
        return null;
    }

    public Bus getBus(int busID) {
        if (buses.containsKey(busID)) { return buses.get(busID); }
        return null;
    }

    public int makeStop(int uniqueID, String inputName, int inputRiders, double inputXCoord, double inputYCoord) {
        stops.put(uniqueID, new BusStop(uniqueID, inputName, inputRiders, inputXCoord, inputYCoord));
        return uniqueID;
    }

    public int makeRoute(int uniqueID, int inputNumber, String inputName) {
        routes.put(uniqueID, new BusRoute(uniqueID, inputNumber, inputName));
        return uniqueID;
    }

    public int makeBus(int uniqueID, int inputRoute, int inputLocation, int inputLocation2, int inputPassengers, int inputCapacity, int inputSpeed, String direction) {
        buses.put(uniqueID, new Bus(uniqueID, inputRoute, inputLocation, inputLocation2, inputPassengers, inputCapacity, inputSpeed, direction));
        return uniqueID;
    }
    
    public int makeRoad(Integer uniqueID, String roadName, 
			double roadLength, double averageSpeed, int trafficIndicator) {
    	roads.put(uniqueID, new Road(uniqueID, roadName, roadLength, averageSpeed, trafficIndicator));
    	return uniqueID;
    }
  
    public void appendStopToRoute(int routeID, int nextStopID) { routes.get(routeID).addNewStop(nextStopID); }
    public void appendRoadToRoute(int routeID, double length, double speed, double trafficstatus) { routes.get(routeID).addNewRoad(length, speed, trafficstatus); }

    
    public HashMap<Integer, BusStop> getStops() { return stops; }

    public HashMap<Integer, BusRoute> getRoutes() { return routes; }

    public HashMap<Integer, Bus> getBuses() { return buses; }
    
    public HashMap<Integer, Road> getRoads() {	
    	return roads;
    }
    
    public MovingHistory moveBus(Bus bus) {
    	BusStop currentStop = stops.get(bus.getCurrentLocation());
    	int currentRider = bus.getRiderList().size();
    	if (currentStop.getWaitingQueue().isEmpty()) {
    		currentStop.setWaitingQueue(new HashMap<>());
    	}
    	if (!currentStop.getWaitingQueue().containsKey(bus.getRouteID())) {
    		currentStop.getWaitingQueue().put(bus.getRouteID(), new LinkedList<>());
    	}
    	int riderWaiting = currentStop.getWaitingQueue().get(bus.getRouteID()).size();
    	currentStop.exchangeRiders(bus);
    	int afterExchange = bus.getRiderList().size();
    	int nextStop = bus.getNextLocation();
    	bus.setCurrentLocation(bus.getNextLocation());
    	return saveMovingHistory(currentStop, currentRider, riderWaiting, afterExchange, nextStop);
    }
    
    public void setStops(HashMap<Integer, BusStop> stops) {
		this.stops = stops;
	}

	public void setRoutes(HashMap<Integer, BusRoute> routes) {
		this.routes = routes;
	}

	public void setBuses(HashMap<Integer, Bus> buses) {
		this.buses = buses;
	}

	public void setRoads(HashMap<Integer, Road> roads) {
		this.roads = roads;
	}

	private MovingHistory saveMovingHistory(BusStop currentStop, int currentRider, int riderWaiting, int afterExchange,
			int nextStop) {
		MovingHistory moveHistory = new MovingHistory();
		moveHistory.setCurrentStop(currentStop.getStopName());
		moveHistory.setCurrentRider(currentRider);
		moveHistory.setPeopleWaiting(riderWaiting);
		moveHistory.setNewRider(afterExchange - currentRider);
		moveHistory.setNextStop(stops.get(nextStop).getName());
		return moveHistory;
	}

    public void displayModel() {
    	ArrayList<MiniPair> busNodes, stopNodes;
    	MiniPairComparator compareEngine = new MiniPairComparator();
    	
    	int[] colorScale = new int[] {9, 29, 69, 89, 101};
    	String[] colorName = new String[] {"#000077", "#0000FF", "#000000", "#770000", "#FF0000"};
    	Integer colorSelector, colorCount, colorTotal;
    	
    	try{
            // create new file access path
            String path="./bus.dot";
            File file = new File(path);

            // create the file if it doesn't exist
            if (!file.exists()) { file.createNewFile();}

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            
            bw.write("digraph G\n");
            bw.write("{\n");
    	
            busNodes = new ArrayList<MiniPair>();
            for (Bus b: buses.values()) { busNodes.add(new MiniPair(b.getID(), b.getPassengers())); }
            Collections.sort(busNodes, compareEngine);

            colorSelector = 0;
            colorCount = 0;
            colorTotal = busNodes.size();
            for (MiniPair c: busNodes) {
            	if (((int) (colorCount++ * 100.0 / colorTotal)) > colorScale[colorSelector]) { colorSelector++; }
            	bw.write("  bus" + c.getID() + " [ label=\"bus#" + c.getID() + " | " + c.getValue() + " riding\", color=\"" + colorName[colorSelector] + "\"];\n");
            }
            bw.newLine();
            
            stopNodes = new ArrayList<MiniPair>();
            for (BusStop s: stops.values()) { stopNodes.add(new MiniPair(s.getID(), s.getWaiting())); }
            Collections.sort(stopNodes, compareEngine);

            colorSelector = 0;
            colorCount = 0;
            colorTotal = stopNodes.size();    	
            for (MiniPair t: stopNodes) {
            	if (((int) (colorCount++ * 100.0 / colorTotal)) > colorScale[colorSelector]) { colorSelector++; }
            	bw.write("  stop" + t.getID() + " [ label=\"stop#" + t.getID() + " | " + t.getValue() + " waiting\", color=\"" + colorName[colorSelector] + "\"];\n");
            }
            bw.newLine();
            
            for (Bus m: buses.values()) {
            	Integer nextStop = m.getNextLocation();
            	//bw.write("  stop" + Integer.toString(prevStop) + " -> bus" + Integer.toString(m.getID()) + " [ label=\" dep\" ];\n");
            	bw.write("  bus" + Integer.toString(m.getID()) + " -> stop" + Integer.toString(nextStop) + " [ label=\" arr\" ];\n");
            }
    	
            bw.write("}\n");
            bw.close();
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    }
}
