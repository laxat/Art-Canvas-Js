package ca.utoronto.utm.paint;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class PolylineManipulatorStrategy extends ShapeManipulatorStrategy{
	boolean left; 
	PolylineManipulatorStrategy(PaintModel paintModel) {
		super(paintModel);
	}

	private PolylineCommand polyCommand;
	@Override
	public void mouseReleased(MouseEvent e) {
		if(this.left && this.polyCommand != null && e.getButton() == MouseButton.PRIMARY)
			this.polyCommand.add(new Point((int)e.getX(), (int)e.getY()));
	}
	
	public void mouseMoved(MouseEvent  e) { 
		if(this.left) {
			if (this.polyCommand != null) {
				Point p2=new Point((int)e.getX(), (int)e.getY());
				this.polyCommand.setP2(p2);
		}
	}
		//this.polyCommand.add(new Point((int)e.getX(), (int)e.getY()));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isPrimaryButtonDown()) {
			left = true; 
			if(polyCommand == null) {
				this.polyCommand = new PolylineCommand();
				this.addCommand(polyCommand);
			}
		}else if(e.isSecondaryButtonDown()) {
			if(this.polyCommand != null) {
				polyCommand.setP2(null);
			}
			this.polyCommand = new PolylineCommand();
			left = false; 
		}
		//this.polyCommand = new PolylineCommand();
		//this.addCommand(polyCommand);
	}
	
}
