package org.xandercat.swing.table;

/**
 * Interface to be implemented by classes that need to be notified whenever a FileTable
 * load is cancelled or completed.
 * 
 * @author Scott Arnold
 */
public interface FileTableModelLoaderListener {

	/**
	 * Fired when a FileTableModel load is interrupted before completion.
	 */
	public void fileTableLoadingCancelled();
	
	/**
	 * Fired when a FileTableModel load is completed successfully.
	 */
	public void fileTableLoadingComplete();
}
