package org.xandercat.swing.file;

import java.io.File;

/**
 * FileCopyProgressListener can be implemented by classes wishing to receive incremental 
 * updates on each file being copied by a FileCopier.  This is useful for tracking copy 
 * progress when file sizes are large.
 * 
 * @author Scott C Arnold
 */
public interface FileCopyProgressListener {
	
	public void fileCopying(File from, File to, long bytesCopied, boolean copyComplete);
}
