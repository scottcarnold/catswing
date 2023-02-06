package org.xandercat.swing.menu;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * ExitAction is an action listener intended for a frame with default close operation as EXIT_ON_CLOSE.
 * When activated, the exit action will fire a window closing event to close the application.
 * 
 * @author Scott C Arnold
 */
public class ExitAction implements ActionListener {

	private Window source;
	
	public ExitAction(Window source) {
		this.source = source;
	}
	
	public void actionPerformed(ActionEvent e) {
		WindowEvent windowClosing = new WindowEvent(source, WindowEvent.WINDOW_CLOSING);
		source.getToolkit().getSystemEventQueue().postEvent(windowClosing);
	}
}
