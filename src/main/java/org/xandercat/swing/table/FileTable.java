package org.xandercat.swing.table;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.dnd.FileTransferHandler;
import org.xandercat.swing.file.icon.FileIconCache;

/**
 * FileTable is a table for a list of files.  The list of files in the table can be set or
 * modified by interacting with the FileTableModel.
 * 
 * FileTables are not automatically made sortable.  In most cases, it is advisable to set a 
 * RowSorter on the FileTable, or simply call setAutoCreateRowSorter(true).
 * 
 * File tables attempt to support drag/drop of files (move operation only).  To enable this ability,
 * call setDragEnabled(true).   
 * 
 * @author Scott C Arnold
 */
public class FileTable extends NonBlockingTable implements DropTargetListener {

	private static final long serialVersionUID = 2009040401L;
	private static final Logger log = LogManager.getLogger(FileTable.class);
	
	private boolean directoryColumnVisible = true;
	private TableColumn directoryColumn;
	
	/**
	 * Construct a new file table using the given FileTableModel.
	 * 
	 * @param model				FileTableModel for the table
	 * @param fileIconCache		file icon cache
	 * @param maxFractionDigits max fraction digits for file sizes displayed in table; can be null
	 */
	public FileTable(FileTableModel model, FileIconCache fileIconCache, Integer maxFractionDigits) {
		super(model);
		setTablePreferences(fileIconCache, maxFractionDigits);
		setTransferHandler(new FileTransferHandler(this));
		setDropTarget(new DropTarget(this, this));
	}
	
	/**
	 * Construct a new file table using a default FileTableModel.
	 * 
	 * @param fileIconCache		file icon cache
	 * @param maxFractionDigits max fraction digits for file sizes displayed in table; can be null
	 */
	public FileTable(FileIconCache fileIconCache, Integer maxFractionDigits) {
		this(new FileTableModel(), fileIconCache, maxFractionDigits);
	}
	
	private void setTablePreferences(FileIconCache fileIconCache, Integer maxFractionDigits) {
		getColumnModel().getColumn(0).setPreferredWidth(200);
		getColumnModel().getColumn(1).setPreferredWidth(175);
		FileSizeRenderer renderer = new FileSizeRenderer(FileSizeRenderer.Render.VALUE_ONLY, SwingConstants.RIGHT);
		renderer.setMaxFractionDigits(maxFractionDigits);
		getColumnModel().getColumn(2).setCellRenderer(renderer);
		renderer = new FileSizeRenderer(FileSizeRenderer.Render.BINARY_PREFIX_ONLY, SwingConstants.LEFT);
		getColumnModel().getColumn(3).setCellRenderer(renderer);
		getColumnModel().getColumn(0).setCellRenderer(new FileNameRenderer(fileIconCache, true, false));
		getColumnModel().getColumn(1).setCellRenderer(new FileNameRenderer(fileIconCache));
		setRowHeight(22);
	}
	
	/**
	 * Get the selected files in the table.
	 * 
	 * @return			selected files
	 */
	public List<File> getSelectedFiles() {
		List<File> fileList = null;
		int[] selectedRows = getSelectedRows();
		if (selectedRows != null && selectedRows.length > 0) {
			fileList = new ArrayList<File>();
			FileTableModel model = (FileTableModel) getModel();
			for (int row : selectedRows) {
				fileList.add(model.getFile(row));
			}
		}
		return fileList;
	}

	public boolean isDirectoryColumnVisible() {
		return directoryColumnVisible;
	}

	public void setDirectoryColumnVisible(boolean directoryColumnVisible) {
		if (directoryColumnVisible != this.directoryColumnVisible) {
			TableColumnModel columnModel = getColumnModel();
			if (directoryColumnVisible) {
				columnModel.addColumn(this.directoryColumn);
				int idx = columnModel.getColumnCount() - 1;
				columnModel.moveColumn(idx, 0);	
			} else {
				if (this.directoryColumn == null) {
					this.directoryColumn = columnModel.getColumn(0);
				}
				columnModel.removeColumn(this.directoryColumn);
			}
			this.directoryColumnVisible = directoryColumnVisible;
		}
	}

	public void dragEnter(DropTargetDragEvent event) {
		// do nothing
	}

	public void dragExit(DropTargetEvent event) {
		// do nothing
	}

	public void dragOver(DropTargetDragEvent event) {
		// do nothing
	}

	public void drop(DropTargetDropEvent event) {
		log.debug("Drop at loc " + event.getLocation().toString());
		boolean accept = event.getDropAction() == DnDConstants.ACTION_MOVE;
		try {
			@SuppressWarnings("unchecked")
			List<File> files = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			if (accept && files != null) {
				FileTableModel model = (FileTableModel) getModel();
				File destDir = model.getDirectory();
				// determine what file drop occurred over; if it is a directory drop into that directory;
				// otherwise, drop into the directory represented by this table
				int dropRow = event.getLocation().y / getRowHeight();
				if (dropRow >= getRowCount() || dropRow < 0) {
					log.warn("Drop row of " + dropRow + " is invalid and will be ignored.");
				} else {
					File dropFile = model.getFile(dropRow);
					log.info("Drop occurred on " + dropFile.getAbsolutePath());
					if (dropFile.isDirectory()) {
						destDir = dropFile;
					}
				}
				for (File file : files) {
					File destFile = new File(destDir.getAbsolutePath() + File.separator + file.getName());
					// note: if a file is dragged from and to the same directory, we should not attempt
					// to rename it; however, we will still add it to the model as a duplicate, as the 
					// transfer handler will not be aware of this and will remove it (in such case it 
					// will simply remove the duplicate).
					if (file.equals(destFile) || file.renameTo(destFile)) {
						if (destDir == model.getDirectory()) {
							model.addFile(destFile);
						}
					}
				}
			}
		} catch (UnsupportedFlavorException e) {
			log.info("Unable to drop due to unsupported data flavor.", e);
			accept = false;
		} catch (Exception e) {
			log.error("Unsuccessful drop.", e);
			accept = false;
		}
		// is this necessary?  not really sure what effect it has
		if (accept) {
			log.debug("Drop was accepted");
			event.acceptDrop(event.getDropAction());
		} else {
			log.debug("Drop was rejected");
			event.rejectDrop();
		}
	}

	public void dropActionChanged(DropTargetDragEvent event) {
		// do nothing
	}
}