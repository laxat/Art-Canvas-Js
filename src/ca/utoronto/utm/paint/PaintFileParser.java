package ca.utoronto.utm.paint;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.paint.Color;
/**
 * Parse a file in Version 1.0 PaintSaveFile format. An instance of this class
 * understands the paint save file format, storing information about
 * its effort to parse a file. After a successful parse, an instance
 * will have an ArrayList of PaintCommand suitable for rendering.
 * If there is an error in the parse, the instance stores information
 * about the error. For more on the format of Version 1.0 of the paint 
 * save file format, see the associated documentation.
 * 
 * @author 
 *
 */
public class PaintFileParser {
	private int lineNumber = 0; // the current line being parsed
	private String errorMessage =""; // error encountered during parse
	private PaintModel paintModel;
	private Color colour; 
	private String name; 
	private boolean is_filled; 
	private Point center; 
	private int radius; 
	private Point rec1; Point rec2;
	private Point poi;
	
	public PaintModel model; 
	private ArrayList<PaintCommand> com = new ArrayList<PaintCommand>(); 
	
	/**
	 * Below are Patterns used in parsing 
	 */
	private Pattern pFileStart=Pattern.compile("^PaintSaveFileVersion1.0$");
	private Pattern pFileEnd=Pattern.compile("^EndPaintSaveFile$");

	private Pattern pCircleStart=Pattern.compile("^Circle$");
	private Pattern pCircleEnd=Pattern.compile("^EndCircle$");
	// ADD MORE!!
	private Pattern pRectangleStart=Pattern.compile("^Rectangle$"); 
	private Pattern pRectangleEnd=Pattern.compile("^EndRectangle$"); 
	
	private Pattern pSquiggleStart = Pattern.compile("^Squiggle$"); 
	private Pattern pSquiggleEnd = Pattern.compile("^EndSquiggle$"); 
	
	private Pattern pPolylineStart = Pattern.compile("^Polyline$"); 
	private Pattern pPolylineEnd = Pattern.compile("^EndPolyline$"); 
	
	private Pattern pColor = Pattern.compile("^color:([0-9]{1,3}),([0-9]{1,3}),([0-9]{1,3})$"); 
	private Pattern pFill = Pattern.compile("^filled:(true|false)$"); 
	private Pattern pCenter = Pattern.compile("^center:\\((-?[0-9]+),(-?[0-9]+)\\)$"); 
	private Pattern pRadius = Pattern.compile("^radius:([0-9]+)$"); 
	private Pattern pP1 = Pattern.compile("^p1:\\((-?[0-9]+),(-?[0-9]+)\\)$"); 
	private Pattern pP2  = Pattern.compile("^p2:\\((-?[0-9]+),(-?[0-9]+)\\)$"); 
	private Pattern pPoint = Pattern.compile("^point:\\((-?[0-9]+),(-?[0-9]+)\\)"); 
	
	private Pattern sPoint = Pattern.compile("^points$"); 
	private Pattern ePoint = Pattern.compile("endpoints$"); 
	
	/**
	 * Store an appropriate error message in this, including 
	 * lineNumber where the error occurred.
	 * @param mesg
	 */
	private void error(String mesg){
		this.errorMessage = "Error in line "+lineNumber+" "+mesg;
	}
	
	/**
	 * 
	 * @return the error message resulting from an unsuccessful parse
	 */
	public String getErrorMessage(){
		return this.errorMessage;
	}
	
	/**
	 * Parse the inputStream as a Paint Save File Format file.
	 * The result of the parse is stored as an ArrayList of Paint command.
	 * If the parse was not successful, this.errorMessage is appropriately
	 * set, with a useful error message.
	 * 
	 * @param inputStream the open file to parse
	 * @param paintModel the paint model to add the commands to
	 * @return whether the complete file was successfully parsed
	 */
	public boolean parse(BufferedReader inputStream, PaintModel paintModel) {
		this.paintModel = paintModel;
		this.errorMessage="";
		
		// During the parse, we will be building one of the 
		// following commands. As we parse the file, we modify 
		// the appropriate command.
		
		CircleCommand circleCommand = null; 
		RectangleCommand rectangleCommand = null;
		SquiggleCommand squiggleCommand = null;
		PolylineCommand polyCommand = null; 
	
		try {	
			int state=0; Matcher m, r, s, p, f; 
			String l;
			
			this.lineNumber=0;
			while ((l = inputStream.readLine()) != null) {
				l = l.replaceAll("\\s+", "");
				if(l.isEmpty()) {
					continue; 
				}
				this.lineNumber++;
				System.out.println(lineNumber+" "+l+" "+state);
				switch(state){
					case 0:
						m=pFileStart.matcher(l);
						if(m.matches()){
							state=1;
							break;
						}
						error("Expected Start of Paint Save File");
						return false;
					case 1:
						m=pCircleStart.matcher(l);
						r=pRectangleStart.matcher(l); 
						s=pSquiggleStart.matcher(l); 
						p=pPolylineStart.matcher(l); 
						f = pFileEnd.matcher(l); 
						
						if(m.matches()){
							name  = "Circle"; 
							circleCommand = new CircleCommand(null, 0); 
							state=2; 
						}
						else if(p.matches()) {
							name  = "Polyline"; 
							polyCommand = new PolylineCommand();
							state = 2;  
							
						}
						
						else if(s.matches()) {
							name  = "Squiggle"; 
							squiggleCommand = new SquiggleCommand();
							state = 2; 
							
						}
						else if(r.matches()) {
							name  = "Rectangle"; 
							rectangleCommand = new RectangleCommand(null, null); 
							state = 2; 
						}
						else if(f.matches()) {
							if(com.size() >= 1) {
								for(PaintCommand paint: com) {
									paintModel.addCommand(paint);
								}
							}
							state = 0; 
							
						}
						else {
						}
						break; 
					case 2:
						m=pColor.matcher(l);
						if(m.matches()){
							colour = Color.rgb(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), 
									Integer.parseInt(m.group(3))); 
							state=3;
						}
						else { 
							error("Error: No color found"); 
							return false; 
						}
						break; 
					case 3:
						m = pFill.matcher(l); 
						if (m.matches()) {
							is_filled = Boolean.parseBoolean(m.group(1)); 
							if (name == "Circle") {
								state = 4; 
							}
							else if (name == "Polyline") {
								state  = 7; 
							}
							else if (name == "Squiggle") {
								state = 10; 
							}
							else if (name == "Rectangle") { 
								state = 13; 
							}
							
							}
							else { 
								error(": No fill is found in" + name); 
								return false; 
						}
						break; 
					case 4: 
						m = pCenter.matcher(l); 
						if(m.matches()) {
							center = new Point(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))); 
							state = 5; 
						}
						else {
							error(": No center found"); 
							return false; 
						}
						break; 
					case 5: 
						m = pRadius.matcher(l); 
						if(m.matches()) {
							radius = Integer.parseInt(m.group(1)); 
							state = 6; 
						}
						else {
							error(": No radius found"); 
							return false; 
							
						}
						break;
					case 6: 
						m = pCircleEnd.matcher(l);
						if (m.matches()) {
							circleCommand.setCentre(center);
							circleCommand.setColor(colour); 
							circleCommand.setFill(is_filled);
							circleCommand.setRadius(radius);
							com.add(circleCommand); 
							name = ""; 
							state = 1;
						}
						else {
							error(": No end statment found for Polyline"); 
							return false;
							
						}
						break;
					case 7: 
						m = sPoint.matcher(l); 
						if (m.matches()) {
							state = 8; 
						}
						else {
							error(" : No expected points found for Polyline"); 
							return false;
							 
						}
						break;
					case 8: 
						m = pPoint.matcher(l); 
						f = ePoint.matcher(l); 
						
						if(m.matches()) {
							poi = new Point(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))); 
							polyCommand.add(poi);
						}
						
						else if (f.matches()) {
							state = 9; 
						}
						else {
							error(": The expected point is not found for Polyline"); 
							return false;
							 
						}
						break;
					case 9: 
						m = pPolylineEnd.matcher(l);
						if (m.matches()) {
							polyCommand.setColor(colour);
							polyCommand.setFill(is_filled);
							this.com.add(polyCommand); 
							name = ""; 
							state = 1; 
						}
						else { 
							error(": No end statement found for Polyline"); 
							return false;
							
						}
						break; 
						
					case 10: 
						m = sPoint.matcher(l); 
						if (m.matches()) {
							state = 11; 
						}
						
						else { 
							error(": No Squiggle point found"); 
							return false;
							
						}
						break;
					case 11: 
						m = pPoint.matcher(l); 
						f = ePoint.matcher(l); 
						if (m.matches()) {
							poi = new Point(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
							squiggleCommand.add(poi);
						}
						else if(f.matches()) {
							state = 12; 
						}
						else {
							error(": No Squiggle endpoint found");
							return false;
							
						}
						break;
					case 12: 
						m = pSquiggleEnd.matcher(l); 
						if(m.matches()) {
							squiggleCommand.setColor(colour);
							squiggleCommand.setFill(is_filled);
							com.add(squiggleCommand); 
							name = ""; 
							state = 1; 
						}
						else { 
							error(": No end statement found for Squiggleline"); 
							return false;
							 
						}
						break;
					case 13: 
						m = pP1.matcher(l); 
						if(m.matches()) {
							rec1 = new Point(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))); 
							state = 14; 
						}
						else {
							error(": No first point found for Rectangle"); 
							return false;
							
						}
						break;
					case 14: 
						m = pP2.matcher(l); 
						if(m.matches()) {
							rec2 = new Point(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))); 
							state = 15; 
						}
						else {
							error(": No second point found for Rectangle"); 
							return false;
							
						}
						break;
					case 15: 
						m = pRectangleEnd.matcher(l);
						if(m.matches()) {
							rectangleCommand.setColor(colour);
							rectangleCommand.setFill(is_filled);
							rectangleCommand.setP1(rec1);
							rectangleCommand.setP2(rec2);
							com.add(rectangleCommand); 
							name = ""; 
							state = 1; 
						}
						else {
							error(": End Statement for Rectangle not found");
							return false;
							
						}
						break;
					}
				}
			
		if(state != 0) {
			error(": end of file not found"); 
			return false; 
		}
		}catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}
}
