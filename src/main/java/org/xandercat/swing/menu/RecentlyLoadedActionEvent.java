package org.xandercat.swing.menu;

import java.awt.event.ActionEvent;
import java.io.File;

/**
 * RecentlyLoadedActionEvent is an ActionEvent that contains a reference to a file.
 * 
 * @author Scott C Arnold
 */
public class RecentlyLoadedActionEvent extends ActionEvent {

	private static final long serialVersionUID = 2009032801L;
	
	private File file;
	
	public RecentlyLoadedActionEvent(ActionEvent event, File file) {
		super(event.getSource(), event.getID(), event.getActionCommand(), event.getWhen(), event.getModifiers());
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
}
