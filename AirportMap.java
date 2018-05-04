package AirportSelectionTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;
import processing.core.PFont;

/* 
 * An applet that shows airports and routes information
 * on a world map.  
 * @author DMou
 */


public class AirportMap extends PApplet {
	/*
	 * @param selRoute
	 * selected route, determined by departure and arrival airport;
	 * @param lastSelected
	 * airports that mouse is hovering on;
	 * @param sourceAirport
	 * selected departure airport or airport stored in lastSelected if no selected departure airport;
	 * @param desAirport
	 * selected arrival airport or airport stored in lastSelected if no selected arrival airport and departure airport is selected;
	 * */
	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	private Marker selRoute;
	private CommonMarker lastSelected;
	private AirportMarker sourceAirport;
	private AirportMarker desAirport;
	
	public void setup() {
		// setting up PAppler
		size(990,620, OPENGL);
		
		// setting up map and default events
		map = new UnfoldingMap(this, 220, 20, 750, 580);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		
		// create markers from features
		for(PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature);
			//airport is not added if it has no airport code, disable if statement if want to add all airport
			String code=(String)feature.getProperty("code");			
			if(code.length()>2){
				m.setRadius(5);
				airportList.add(m);
				// put airport in hashmap with OpenFlights unique id for key
				airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
			}
		
		}
		
		
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
			sl.setHidden(true);

			routeList.add(sl);
		}
		
		map.addMarkers(routeList);		
		map.addMarkers(airportList);		
	}
	
	public void draw() {
		background(230);
		map.draw();
		routeInfo();
	}
	
	
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		
		// store the hovered on airport into lastSelected
		selectMarkerIfHover(airportList);
		
		//find the temporary route
		if(sourceAirport==null || desAirport==null){
			if (lastSelected != null){
				if(sourceAirport!=null){
					selRoute=findRoute(sourceAirport,(AirportMarker)lastSelected);
				}else if(desAirport!=null){
					selRoute=findRoute((AirportMarker)lastSelected, desAirport);
				}
			}else {
				if(selRoute!=null){
					selRoute=null;
				}
			}
		}
		hiddenRouteUpdate();
	}
	
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker m : markers) 
		{
			CommonMarker marker = (CommonMarker)m;
			if (!marker.isHidden() && marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}
	
	/*
	 *  The event handler for mouse clicks
	 */
	@Override
	public void mouseClicked()
	{
		checkAirportForClick();
		hiddenAirportUpdate();
		hiddenRouteUpdate();
	}
	
	/*
	 * change the departure and arrival airport and update the route accordingly
	 */
	private void checkAirportForClick()
	{
		for (Marker marker : airportList) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {				
				AirportMarker am = (AirportMarker)marker;
				if(am==sourceAirport){
					am.setSource(false);
					sourceAirport=null;
				}else if(am==desAirport){
						am.setDes(false);
						desAirport=null;
				}else{						
					if(sourceAirport==null){	
						am.setSource(true);
						sourceAirport=am;
						
					}else if(desAirport==null){
						am.setDes(true);
						desAirport=am;
					}
				}
				
				if(sourceAirport!=null && desAirport!=null){
					selRoute=findRoute(sourceAirport,desAirport);
				}else if(selRoute!=null){
					selRoute.setHidden(true);
					selRoute=null;
				}
				return;
			}
			
		}
	}
	
	
	/*
	 * display the selected or temporary route on the map when mouse is moved or clicked
	 */
	private void hiddenRouteUpdate(){
		if(selRoute!=null){
			for(Marker route:routeList){
				route.setHidden(true);
			}
			selRoute.setHidden(false);
		}
	}
	
	/*
	 * updated displayed airports on the map
	 */
	private void hiddenAirportUpdate(){
		if(sourceAirport==null && desAirport==null){
			for (Marker marker : airportList) marker.setHidden(false);
		}else{
			for (Marker marker : airportList) marker.setHidden(true);
			if(sourceAirport!=null && desAirport!=null){
				sourceAirport.setHidden(false);
				desAirport.setHidden(false);
				return;
			}
			if(sourceAirport!=null){
				sourceAirport.setHidden(false);
				showRouteAirports(sourceAirport, "source", "destination");
			}
			if(desAirport!=null){
				desAirport.setHidden(false);
				showRouteAirports(desAirport, "destination", "source");
			}	
		}
	}
	
	/*
	 * hide the airports with no flight to the selected airport
	 * @param airport
	 * the selected airport
	 * @param selected
	 * string to determine whether the selected airport is departure or arrival airport
	 * @param toFind
	 * string to determine whether the airport connected to selected airport is departure or arrival airport
	 */
	private void showRouteAirports(Marker airport, String selected, String toFind){
		String selectedID=airport.getId();
		for(Marker route:routeList){
			String selID=(String)route.getProperty(selected);
			if(selectedID.equals(selID)){
				String desID=(String)route.getProperty(toFind);
				for(Marker marker:airportList){
					if(desID.equals(marker.getId())){
						marker.setHidden(false);
						break;
					}
				}
			}
		}
	}
	
	/*
	 * text format structure to show the airport and route information
	 */
	private void routeInfo(){
		int x=30;
		int y1=50;
		int dy=25;
		int y2=250;
		int y3=175;
				
		PFont font1,font2;
		font1 = createFont("Arial Bold", 12);
		font2 = createFont("Arial", 12);
		textFont(font1);
		textSize(14);
		fill(150,70,70);
		text("Departure:", x, y1);
		fill(30);
		text("Distance:", x, y3);
		fill(70,70,150);
		text("Arival:", x, y2);
		
		textFont(font2);
		textSize(12);
		fill(100,50,50);
		text("Name:", x, y1+dy);
		text("City:", x, y1+dy*2);
		text("Country:", x, y1+dy*3);
		fill(50,50,100);
		text("Name:", x, y2+dy);
		text("City:", x, y2+dy*2);
		text("Country:", x, y2+dy*3);
		

		line(x,y1+5,x+170,y1+5);
		line(x,y2+5,x+170,y2+7);
		line(x,y3+5,x+170,y3+5);
		
		fill(100);
		textFont(font1);
		if(sourceAirport!=null){
			showAirportInfo(sourceAirport,x+55,y1,dy);
		}else if(lastSelected!=null && !lastSelected.isHidden() &&lastSelected!=desAirport){
			showAirportInfo((AirportMarker)lastSelected,x+55,y1,dy);
		}
		
		if(desAirport!=null){
			showAirportInfo(desAirport,x+55,y2,dy);
		}else if(lastSelected!=null && !lastSelected.isHidden() && sourceAirport!=null && lastSelected!=sourceAirport){
			showAirportInfo((AirportMarker)lastSelected,x+55,y2,dy);
		}
		
		if(selRoute!=null){
			showRouteInfo(x, y3);
		}
	}
	
	private void showAirportInfo(AirportMarker airport, int x, int y, int dy){
		String departure=(String)airport.getProperty("code");
		departure=departure.substring(1,departure.length()-1);
		String name=(String)airport.getProperty("name");
		name=name.substring(1,name.length()-1);
		String city=(String)airport.getProperty("city");
		city=city.substring(1,city.length()-1);
		String country=(String)airport.getProperty("country");
		country=country.substring(1,country.length()-1);
					
		textSize(14);
		text(departure, x+25, y);
			
		
		textSize(12);
		text(name, x, y+dy*1);
		text(city, x, y+dy*2);
		text(country, x, y+dy*3);
	}
	
	private void showRouteInfo(int x, int y){
		SimpleLinesMarker temp=(SimpleLinesMarker)selRoute;
		int distance=(int)temp.getLocation(0).getDistance(temp.getLocation(1));
		String dis=distance+" miles";
		textSize(12);
		text(dis, x, y+25);
	}
	
	/*
	 * find the route between departure and arrival airports
	 */
	private Marker findRoute(AirportMarker source,AirportMarker des){
		for(Marker route:routeList){
			String sourceID=(String)route.getProperty("source");
			String desID=(String)route.getProperty("destination");
			if(source.getId().equals(sourceID) && des.getId().equals(desID)){
				return route;
			}
		}
		return null;
	}

}


