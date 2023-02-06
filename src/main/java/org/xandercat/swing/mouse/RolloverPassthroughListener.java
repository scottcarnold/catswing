package org.xandercat.swing.mouse;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Mouse listener that passes mouse entered and mouse moved events along to some 
 * other "receiving component".  Typically the receiving component is the component 
 * underneath the source component, creating a "rollover passthrough" effect as if
 * the source component were not there.
 * 
 * @author Scott Arnold
 */
public class RolloverPassthroughListener extends MouseAdapter {

	private JComponent receivingComponent;
	private MouseListener[] mouseListeners;
	private MouseMotionListener[] mouseMotionListeners;
	
	public RolloverPassthroughListener(JComponent receivingComponent) {
		this.receivingComponent = receivingComponent;
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		this.mouseListeners = receivingComponent.getMouseListeners();
		final MouseEvent me = getModifiedEvent(e);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (MouseListener mouseListener : mouseListeners) {
					mouseListener.mouseEntered(me);
				}						
			}
		});
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.mouseMotionListeners = receivingComponent.getMouseMotionListeners();
		final MouseEvent me = getModifiedEvent(e);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
					mouseMotionListener.mouseMoved(me);
				}						
			}
		});
	}
	
	private MouseEvent getModifiedEvent(MouseEvent e) {
		Point p = e.getPoint();
		Point tl = receivingComponent.getLocationOnScreen();
		Point cl = ((Component) e.getSource()).getLocationOnScreen();
		p.x += (cl.x - tl.x);
		p.y += (cl.y - tl.y);
		MouseEvent me = new MouseEvent(receivingComponent, e.getID(), e.getWhen(), 
				e.getModifiers(), p.x, p.y, 0, false);
		return me;
	}
}
