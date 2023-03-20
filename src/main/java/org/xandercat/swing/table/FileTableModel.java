package org.xandercat.swing.table;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.xandercat.swing.file.DirectorySizeCache;
import org.xandercat.swing.util.PlatformTool;
import org.xandercat.swing.worker.SwingWorkerUtil;

/**
 * FileTableModel is a sortable table model to represent a directory with rows representing files
 * within that directory.  FileTableModel can also represent arbitrary files in any directory if
 * no directory is set.
 * 
 * @author Scott C Arnold
 */
public class FileTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 2009070601L;
	private static final String[] COLUMN_NAMES = new String[] {"Parent Directory", "Filename", "Size", "Unit", "Type"};
	private static final Class<?>[] COLUMN_CLASSES = new Class[] {File.class, File.class, Long.class, Long.class, String.class};
	
	protected File directory;
	private FileTableModelLoader loader = null;
	private boolean useDirectorySizeCache;
	protected List<FileData> elements = new ArrayList<FileData>();

	public boolean isUseDirectorySizeCache() {
		return useDirectorySizeCache;
	}
	
	/**
	 * Set whether or not to use Directory Size Cache for obtaining directory sizes.  When set to true,
	 * directory sizes are obtained from the Directory Size Cache at the time the files are added to the 
	 * model.  However, it is up to the client class to ensure the Directory Size Cache is populated.
	 *   
	 * @param useDirectorySizeCache
	 */
	public void setUseDirectorySizeCache(boolean useDirectorySizeCache) {
		this.useDirectorySizeCache = useDirectorySizeCache;
	}

	/**
	 * Refreshes contents of the table to pick up any changes in the directory.
	 * 
	 * @param listener			file table model loader listener
	 */
	public void refresh(FileTableModelLoaderListener listener) {
		setDirectory(directory, listener);
	}
	
	/**
	 * Set the directory to be represented by this file table model.  Model will be loaded with all
	 * files in the given directory.
	 * 
	 * @param directory			directory this file table model should represent
	 */
	public void setDirectory(File directory) {
		setDirectory(directory, null);
	}

	/**
	 * Set the directory to be represented by this file table model.  Model will be loaded with all
	 * files in the given directory.  Listener will be notified when model is either fully loaded
	 * or model load is interrupted.
	 * 
	 * @param directory			directory this file table model should represent
	 * @param listener			file table model loader listener
	 */
	public void setDirectory(File directory, FileTableModelLoaderListener listener) {
		if (directory != null && !directory.isDirectory()) {
			throw new IllegalArgumentException("The file passed to setDirectory must be a directory.");
		}
		this.directory = directory;
		File[] files = (directory == null)? null : directory.listFiles(PlatformTool.FILE_FILTER);
		elements.clear();
		if (files != null) {
			if (loader != null && !loader.isDone()) {
				//files for another directory are still loading; stop this process in order to start a new one
				loader.cancel(true);
			}
			//FileTableModelLoader allows UI to remain responsive when loading large directories 
			loader = new FileTableModelLoader(this, files, useDirectorySizeCache);
			if (listener != null) {
				loader.addFileTableModelLoaderListener(listener);
			}
			SwingWorkerUtil.execute(loader);
		}
	}
	
	/**
	 * Add a new file to this table model.  If the directory this table model currently represents 
	 * is non-null, it must be equal to the parent of the given file.
	 * 
	 * @param file			file to add to the model
	 */
	public void addFile(File file) {
		if (this.directory != null && !this.directory.equals(file.getParentFile())) {
			throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not belong to directory " + this.directory.getAbsolutePath());
		}
		FileData data = new FileData(file);
		if (file.isDirectory() && useDirectorySizeCache) {
			//TODO: Fix this, if size is not in cache, it needs to handle the situation properly
			data.setLength(DirectorySizeCache.getInstance().getBytes(file));
		}
		addElement(data);
	}
	
	public void setElements(List<FileData> elements) {
		this.elements.clear();
		this.elements.addAll(elements);
		fireTableDataChanged();
	}
	
	protected void addElement(FileData element) {		
		addElements(Collections.singletonList(element));
	}

	protected void addElements(List<FileData> elements) {
		if (this.directory != null) {
			for (FileData element : elements) {
				if (!this.directory.equals(element.getFile().getParentFile())) {
					throw new IllegalArgumentException("File " + element.getFile().getAbsolutePath() + " does not belong to directory " + this.directory.getAbsolutePath());					
				}
			}
		}
		this.elements.addAll(elements);
		fireTableRowsInserted(this.elements.size()-elements.size(), this.elements.size()-1);
	}

	/**
	 * Finds and replaces the equivalent FileData with the provided FileData.  FileData keys
	 * off of the file only.  If an equivalent FileData is not found, the provided FileData
	 * is added.
	 * 
	 * @param datum			FileData to find/replace
	 */
	protected void replaceElement(FileData datum) {
		int idx = indexOf(datum);
		if (idx >= 0) {
			this.elements.set(idx, datum);
			fireTableRowsUpdated(idx, idx);
		} else {
			addElement(datum);
		}
	}
	
	protected void removeElement(FileData datum) {
		int idx = indexOf(datum);
		if (idx >= 0) {
			this.elements.remove(idx);
			fireTableRowsDeleted(idx, idx);
		}
	}
	
	protected int indexOf(FileData datum) {
		return this.elements.indexOf(datum);
	}
	
	/**
	 * Remove the given file from this table model.
	 * 
	 * @param file			file to remove from the model
	 * 
	 * @return				whether or not file was found and successfully removed
	 */
	public boolean removeFile(File file) {
		if (file == null) {
			return false;
		}
		for (Iterator<FileData> iter = elements.iterator(); iter.hasNext();) {
			FileData datum = iter.next();
			if (file.equals(datum.getFile())) {
				int idx = elements.indexOf(datum);
				iter.remove();
				fireTableRowsDeleted(idx, idx);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the file represented by the given row of the table model.
	 * 
	 * @param row			row of the table model
	 * 
	 * @return				file given row represents
	 */
	public File getFile(int row) {
		return elements.get(row).getFile();
	}

	/**
	 * Removes all rows from the model.
	 */
	public void clear() {
		this.elements.clear();
		fireTableDataChanged();
	}
	
	/**
	 * Get the directory represented by the table model.
	 * 
	 * @return				directory represented by the table model
	 */
	public File getDirectory() {
		return directory;
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		return COLUMN_CLASSES[col];
	}

	@Override
	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return elements.size();
	}

	public Object getValueAt(int row, int col) {
		FileData fileDataItem = elements.get(row);
		switch (col) {
		case 0:
			return fileDataItem.getFile().getParentFile();
		case 1:
			return fileDataItem.getFile();
		case 2:
		case 3:
			return fileDataItem.getLength();
		case 4:
			return fileDataItem.getFileType();
		}
		return null;
	}
}
