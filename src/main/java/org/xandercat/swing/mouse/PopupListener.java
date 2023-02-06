package org.xandercat.swing.mouse;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

/**
 * PopupListener is a MouseListener for triggering popup menus.
 * 
 * @author Scott Arnold
 */
public class PopupListener extends MouseAdapter {

	private JPopupMenu popup;
	private MouseEvent lastEvent;
	
	public PopupListener(JPopupMenu popup) {
		super();
		this.popup = popup;
	}
	
	public MouseEvent getLastPopupEvent() {
		return lastEvent;
	}
	
	@Override
	public void mousePressed(MouseEvent event) {
		checkForPopup(event);
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		checkForPopup(event);
	}

	protected void checkForPopup(MouseEvent event) {
		if (event.isPopupTrigger()) {
			lastEvent = event;
			popup.show(event.getComponent(), event.getX(), event.getY());
		}
	}
}
