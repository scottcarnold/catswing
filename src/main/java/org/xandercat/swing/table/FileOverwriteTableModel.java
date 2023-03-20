package org.xandercat.swing.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.component.ObjectRemovable;
import org.xandercat.swing.component.RemoveObjectAction;
import org.xandercat.swing.file.FileCopier;
import org.xandercat.swing.file.FileCopyListener;
import org.xandercat.swing.file.SwingFileCopier;

public class FileOverwriteTableModel extends FileTableModel implements FileCopyListener, ObjectRemovable, ActionListener {

	private static final long serialVersionUID = 2009022201L;
	private static final Logger log = LogManager.getLogger(FileOverwriteTableModel.class);
	private static final String OVERWRITE = "Overwrite";
	private static final String CANCEL = "Cancel";
	private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] { JButton.class, JButton.class };
	private static final String[] COLUMN_NAMES = new String[] { CANCEL, OVERWRITE };

	public static final ImageIcon CANCEL_ICON = new ImageIcon(FileOverwriteTableModel.class.getResource("/icon/file/Cancel-16x16.png"));
	public static final ImageIcon OVERWRITE_ICON = new ImageIcon(FileOverwriteTableModel.class.getResource("/icon/file/next-16x16.png"));

	private int fileCols;
	private SwingFileCopier fileCopier;
	private JButton overwriteAllButton;
	private JButton cancelAllButton;
	
	public FileOverwriteTableModel(SwingFileCopier fileCopier, JButton overwriteAllButton, JButton cancelAllButton) {
		super();
		this.fileCols = super.getColumnCount();
		this.fileCopier = fileCopier;
		this.overwriteAllButton = overwriteAllButton;
		this.cancelAllButton = cancelAllButton;
		overwriteAllButton.setActionCommand(OVERWRITE);
		overwriteAllButton.addActionListener(this);
		cancelAllButton.setActionCommand(CANCEL);
		cancelAllButton.addActionListener(this);
		overwriteAllButton.setEnabled(false);
		cancelAllButton.setEnabled(false);
		fileCopier.addFileCopyListener(this);
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		if (col < fileCols) {
			return super.getColumnClass(col);
		} else {
			col -= fileCols;
			return COLUMN_CLASSES[col];
		}
	}

	@Override
	public int getColumnCount() {
		return fileCols + COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName(int col) {
		if (col < fileCols) {
			return super.getColumnName(col);
		} else {
			col -= fileCols;
			return COLUMN_NAMES[col];
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col < fileCols) {
			return super.getValueAt(row, col);
		} else {
			col -= fileCols;
			OverwriteFileData fileDataItem = (OverwriteFileData) elements.get(row);
			if (col == 0) {
				return fileDataItem.getCancelButton();
			} else {
				return fileDataItem.getOverwriteButton();
			}
		}
	}

	/**
	 * Handles the RetryAll and CancelAll button actions.
	 */
	public void actionPerformed(ActionEvent event) {
		// using a copy will help avoid screwy thread synchronization problems
		List<FileData> copy = new ArrayList<FileData>();
		copy.addAll(this.elements);
		String actionCommand = event.getActionCommand();
		for (FileData data : copy) {
			remove(data, actionCommand);
		}
	}
	
	public void remove(Object obj, String actionCommand) {
		OverwriteFileData fileDataItem = (OverwriteFileData) obj;
		int row = this.elements.indexOf(fileDataItem);
		if (row >= 0) {
			removeElement(fileDataItem);
			if (this.elements.size() == 0) {
				overwriteAllButton.setEnabled(false);
				cancelAllButton.setEnabled(false);
			}
			fileCopier.resolveOverwrite(fileDataItem.getFile(), OVERWRITE.equals(actionCommand));
		} else {
			log.warn("Request to remove object from overwrite list cannot be completed; object not found in list");
		}
	}
	
	public void fileCopied(File from, File to, boolean isDirectory, FileCopier.CopyResult result) {
		if (result == FileCopier.CopyResult.ALREADY_EXISTS) {
			JButton overwriteButton = new JButton();
			JButton cancelButton = new JButton();
			OverwriteFileData fileDataItem = new OverwriteFileData(from, overwriteButton, cancelButton);
			cancelButton.setAction(new RemoveObjectAction(this, fileDataItem, CANCEL_ICON, CANCEL));	
			overwriteButton.setAction(new RemoveObjectAction(this, fileDataItem, OVERWRITE_ICON, OVERWRITE));
			addElement(fileDataItem);
			overwriteAllButton.setEnabled(true);
			cancelAllButton.setEnabled(true);
		}
	}
	
	@Override
	public void setDirectory(File directory) {
		throw new UnsupportedOperationException("Directory cannot be set on a FileOverwriteTableModel");
	}

	public void copyComplete(boolean resolutionRequired, boolean copyCancelled) {
		// nothing to do here
	}
	
	public void fileCopying(File from, File to, boolean isDirectory) {
		// nothing to do here
	}	
}
