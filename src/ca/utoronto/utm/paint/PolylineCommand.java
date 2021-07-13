package ca.utoronto.utm.paint;

import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;

public class PolylineCommand extends PaintCommand{
	
	
	private ArrayList<Point> points=new ArrayList<Point>();
	private Point p1, p2; 
	
	public void add(Point p){ 
		this.points.add(p); 
		this.setChanged();
		this.notifyObservers();
	}
	public ArrayList<Point> getPoints(){ return this.points; }
	
	public void setP2(Point p2) {
		this.p2 = p2;
		this.setChanged();
		this.notifyObservers();
	}
	
	public Point getP2() {
		return p2; 
	}
	
	@Override
	public void execute(GraphicsContext g) {
		ArrayList<Point> points = this.getPoints();
		g.setStroke(this.getColor());
		for(int i=0;i<points.size()-1;i++){
			Point p1 = points.get(i);
			Point p2 = points.get(i+1);
			g.strokeLine(p1.x, p1.y, p2.x, p2.y);
		}
		if(this.p2 != null) {
			Point p = points.get(points.size()- 1); 
			g.strokeLine(p.x, p.y, p2.x, p2.y); 
		}
	}
}
