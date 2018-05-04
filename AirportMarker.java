package AirportSelectionTool;

import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;

/*
 * A class to represent AirportMarkers on a world map.
 * @author DMou
 */
public class AirportMarker extends CommonMarker {
	/*
	 * @param source
	 * to store whether this airport is departure airport
	 * @param des
	 * to store whether this airport is arrival airport
	 */
	private boolean source=false;
	private boolean des=false;
	
	public static List<SimpleLinesMarker> routes;
	
	public AirportMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
		id=city.getId();
	}
	
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		if(source){
			pg.fill(200,50,50);
			pg.ellipse(x, y, 8, 8);
		}else if(des){
			pg.fill(50,50,200);
			pg.ellipse(x, y, 8, 8);
		}else if(selected){
			pg.fill(50,200,50);
			pg.ellipse(x, y, 10, 10);
		}else{
			pg.fill(11);
			pg.ellipse(x, y, 5, 5);
		}
		
		
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) {
		String title =(String) getProperty("name");
		title=title.substring(1,title.length()-1);
		pg.pushStyle();
		
		pg.rectMode(PConstants.CENTER);
		
//		pg.stroke(110);
		pg.fill(255);
		pg.rect(x, y - 16, pg.textWidth(title) + 6, 18, 5);
		
		
		pg.textAlign(PConstants.CENTER, PConstants.CENTER);
		pg.fill(0,50,0);
		pg.textSize(12);
		pg.text(title, x + 1 , y - 18);
		
		
		pg.popStyle();
	}
	
	public void setSource(boolean set){
		source=set;
	}
	public void setDes(boolean set){
		des=set;
	}
	public boolean isSource(){
		return source;
	}
	public boolean isDes(){
		return des;
	}
}
