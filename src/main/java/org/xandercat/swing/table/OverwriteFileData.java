package org.xandercat.swing.table;

import java.io.File;

import javax.swing.JButton;

/**
 * OverwriteFileData is a FileData for files that require the user to decide whether to overwrite 
 * or cancel a copy operation on that file.
 * 
 * @author Scott C Arnold
 */
public class OverwriteFileData extends FileData {

	private JButton overwriteButton;
	private JButton cancelButton;
	
	public OverwriteFileData(File file, JButton overwriteButton, JButton cancelButton) {
		super(file);
		this.overwriteButton = overwriteButton;
		this.cancelButton = cancelButton;
	}

	public JButton getOverwriteButton() {
		return overwriteButton;
	}

	public void setOverwriteButton(JButton overwriteButton) {
		this.overwriteButton = overwriteButton;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	public void setCancelButton(JButton cancelButton) {
		this.cancelButton = cancelButton;
	}
}
