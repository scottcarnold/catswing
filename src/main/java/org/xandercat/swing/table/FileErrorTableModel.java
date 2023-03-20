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
import org.xandercat.swing.component.ExceptionDetailAction;
import org.xandercat.swing.component.ObjectRemovable;
import org.xandercat.swing.component.RemoveObjectAction;
import org.xandercat.swing.file.FileCopier;
import org.xandercat.swing.file.FileCopyListener;
import org.xandercat.swing.file.SwingFileCopier;

public class FileErrorTableModel extends FileTableModel implements FileCopyListener, ObjectRemovable, ActionListener {

	private static final long serialVersionUID = 2009022201L;
	
	private static final Logger log = LogManager.getLogger(FileErrorTableModel.class);
	private static final String RETRY = "Retry";
	private static final String CANCEL = "Cancel";
	private static final String DETAIL = "Detail";
	private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] { JButton.class, JButton.class, JButton.class };
	private static final String[] COLUMN_NAMES = new String[] { CANCEL, RETRY, DETAIL };

	public static final ImageIcon CANCEL_ICON = new ImageIcon(FileErrorTableModel.class.getResource("/icon/file/Cancel-16x16.png"));
	public static final ImageIcon RETRY_ICON = new ImageIcon(FileErrorTableModel.class.getResource("/icon/file/next-16x16.png"));
	public static final ImageIcon INFO_ICON = new ImageIcon(FileErrorTableModel.class.getResource("/icon/file/Info-16x16.png"));

	private int fileCols;
	private SwingFileCopier fileCopier;
	private JButton retryAllButton;
	private JButton cancelAllButton;
	
	public FileErrorTableModel(SwingFileCopier fileCopier, JButton retryAllButton, JButton cancelAllButton) {
		super();
		this.fileCols = super.getColumnCount();
		this.fileCopier = fileCopier;
		this.retryAllButton = retryAllButton;
		retryAllButton.setActionCommand(RETRY);
		retryAllButton.addActionListener(this);
		this.cancelAllButton = cancelAllButton;
		cancelAllButton.setActionCommand(CANCEL);
		cancelAllButton.addActionListener(this);
		fileCopier.addFileCopyListener(this);
		retryAllButton.setEnabled(false);
		cancelAllButton.setEnabled(false);
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
			ErrorFileData fileDataItem = (ErrorFileData) elements.get(row);
			if (col == 0) {
				return fileDataItem.getCancelButton();
			} else if (col == 1) {
				return fileDataItem.getRetryButton();
			} else {
				return fileDataItem.getDetailButton();
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

	/**
	 * Remove the given ErrorFileData and notify the file copier to resolve the error as specified
	 * by the action command.
	 */
	public void remove(Object obj, String actionCommand) {
		ErrorFileData fileDataItem = (ErrorFileData) obj;
		int row = this.elements.indexOf(fileDataItem);
		if (row >= 0) {
			removeElement(fileDataItem);
			if (elements.size() == 0) {
				retryAllButton.setEnabled(false);
				cancelAllButton.setEnabled(false);
			}
			// we need not worry about the result of the resolveError call; it will be handled by
			// the listener method fileCopied
			fileCopier.resolveError(fileDataItem.getFile(), RETRY.equals(actionCommand));
		} else {
			log.warn("Request to remove object from error list cannot be completed; object not found in list");
		}
	}
	
	public void fileCopied(File from, File to, boolean isDirectory, FileCopier.CopyResult result) {
		if (result == FileCopier.CopyResult.ERROR) {
			JButton retryButton = new JButton();
			JButton cancelButton = new JButton();
			JButton detailButton = new JButton();
			Throwable throwable = fileCopier.getFileCopyException(from);
			ErrorFileData fileDataItem = new ErrorFileData(from, throwable, retryButton, cancelButton, detailButton);
			cancelButton.setAction(new RemoveObjectAction(this, fileDataItem, CANCEL_ICON, CANCEL));	
			retryButton.setAction(new RemoveObjectAction(this, fileDataItem, RETRY_ICON, RETRY));
			detailButton.setAction(new ExceptionDetailAction(
					INFO_ICON, fileDataItem.getThrowable(), "Copy Error Detail: " + fileDataItem.getFile().getAbsolutePath()));
			addElement(fileDataItem);
			retryAllButton.setEnabled(true);
			cancelAllButton.setEnabled(true);
		}
	}
	
	@Override
	public void setDirectory(File directory) {
		throw new UnsupportedOperationException("Directory cannot be set on a FileErrorTableModel.");
	}

	public void copyComplete(boolean resolutionRequired, boolean copyCancelled) {
		// nothing to do here
	}
	
	public void fileCopying(File from, File to, boolean isDirectory) {
		// nothing to do here
	}		
}
