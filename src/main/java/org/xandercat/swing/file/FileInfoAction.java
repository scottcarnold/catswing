package org.xandercat.swing.file;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import org.xandercat.swing.dialog.FileInfoDialog;
import org.xandercat.swing.file.icon.FileIconCache;

/**
 * Action for launching a file information dialog. 
 * 
 * @author Scott Arnold
 */
public class FileInfoAction extends AbstractAction {
	
	private static final long serialVersionUID = 2009102301L;
	
	private Window parent;
	private File file;
	private FileIconCache iconCache;
	
	public FileInfoAction(Window parent, File file, FileIconCache iconCache) {
		super("File Details");
		this.parent = parent;
		this.file = file;
		this.iconCache = iconCache;
	}
	
	public void actionPerformed(ActionEvent event) {
		FileInfoDialog dialog = new FileInfoDialog(parent, file, iconCache);
		dialog.showDialog();
	}

}
