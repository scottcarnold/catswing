package org.xandercat.swing.mouse;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class ClickPassthroughListener extends MouseAdapter {

	private JComponent receivingComponent;
	
	public ClickPassthroughListener(JComponent receivingComponent) {
		this.receivingComponent = receivingComponent;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		MouseListener[] mouseListeners = receivingComponent.getMouseListeners();
		final MouseEvent me = getModifiedEvent(e);
		SwingUtilities.invokeLater(() -> {
			for (MouseListener mouseListener : mouseListeners) {
				mouseListener.mouseClicked(me);
			}
		});
	}

	@Override
	public void mousePressed(MouseEvent e) {
		MouseListener[] mouseListeners = receivingComponent.getMouseListeners();
		final MouseEvent me = getModifiedEvent(e);
		SwingUtilities.invokeLater(() -> {
			for (MouseListener mouseListener : mouseListeners) {
				mouseListener.mousePressed(me);
			}
		});
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		MouseListener[] mouseListeners = receivingComponent.getMouseListeners();
		final MouseEvent me = getModifiedEvent(e);
		SwingUtilities.invokeLater(() -> {
			for (MouseListener mouseListener : mouseListeners) {
				mouseListener.mouseReleased(me);
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
