package org.xandercat.swing.file;

import java.io.File;
import java.util.List;


/**
 * FilesSizeHandler can be implemented by any class that wishes to process the results of
 * a FilesSizeCalculator.
 * 
 * @see org.xandercat.common.util.file.FilesSizeCalculator
 * 
 * @author Scott C Arnold
 */
public interface FilesSizeHandler {

	public void handleDirectoryProcessing(List<File> directories);
	
	public void handleFilesSize(FilesSizeCalculator calculator, FilesSize filesSize);
	
	public void handleFilesSizeInterrupted();
}
