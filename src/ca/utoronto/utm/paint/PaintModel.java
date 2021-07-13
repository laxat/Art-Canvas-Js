package ca.utoronto.utm.paint;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javafx.scene.canvas.GraphicsContext;

public class PaintModel extends Observable implements Observer {

	public void save(PrintWriter writer) {
		
		writer.println("Paint Save File Version 1.0");
		for(PaintCommand c: this.commands) {
			
			if (c instanceof CircleCommand ) {
				writer.println("Circle");
				writer.println("	color: " + c.getColorString());
				writer.println("  	filled: " + c.isFill());
				writer.println("	center:" + ((CircleCommand) c).getCentre());
				writer.println("	radius: "+ ((CircleCommand) c).getRadius()); 
				writer.println("EndCircle"); 
			}
			else if(c instanceof RectangleCommand) {
				writer.println("Rectangle");
				writer.println("	color: " + c.getColorString());
				writer.println("  	filled: " + c.isFill());
				writer.println("	p1: "+ ((RectangleCommand) c).getP1()); 
				writer.println("	p2:" + ((RectangleCommand) c).getP2());
				writer.println("EndRectangle"); 
			}
			else if(c instanceof SquiggleCommand) {
				writer.println("Squiggle");
				writer.println("	color: " + c.getColorString());
				writer.println("  	filled: " + c.isFill());
				writer.println("	points");
				if (((SquiggleCommand) c).getPoints().size() >= 1){ 
					for(Point i: ((SquiggleCommand) c).getPoints()) {
						writer.println("		point:" + i.toString());
					}
				writer.println("	end points");
				}
				writer.println("EndSquiggle"); 
			}
			else {
				writer.println("Polyline");
				writer.println("	color: " + c.getColorString());
				writer.println("  	filled: " + c.isFill());
				writer.println("	points");
				if (((PolylineCommand) c).getPoints().size() >= 1){ 
					for(Point i: ((PolylineCommand) c).getPoints()) {
						writer.println("		point:" + i.toString());
					}
				writer.println("	end points");
				}
				writer.println("EndPolyline");
			}
		}
		writer.println("End Paint Save File");
		
	}
	public void reset(){
		for(PaintCommand c: this.commands){
			c.deleteObserver(this);
		}
		this.commands.clear();
		this.setChanged();
		this.notifyObservers();
	}
	
	public void addCommand(PaintCommand command){
		this.commands.add(command);
		command.addObserver(this);
		this.setChanged();
		this.notifyObservers();
	}
	
	private ArrayList<PaintCommand> commands = new ArrayList<PaintCommand>();

	public void executeAll(GraphicsContext g) {
		for(PaintCommand c: this.commands){
			
			c.execute(g);
		}
	}
	
	/**
	 * We Observe our model components, the PaintCommands
	 */
	@Override
	public void update(Observable o, Object arg) {
		this.setChanged();
		this.notifyObservers();
	}
}
