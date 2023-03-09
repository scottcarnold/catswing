package org.xandercat.swing.file;

import java.io.File;


/**
 * FileCopyListener can be implemented by any class wishing to be kept updated on the progress
 * of a FileCopier.
 * 
 * @author Scott C Arnold
 */
public interface FileCopyListener {

	public void fileCopying(File from, File to);

	public void fileCopied(File from, File to, FileCopier.CopyResult result);
	
	public void copyComplete(boolean resolutionRequired, boolean copyCancelled);
}
