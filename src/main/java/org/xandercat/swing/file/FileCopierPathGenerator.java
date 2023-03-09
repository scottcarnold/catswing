package org.xandercat.swing.file;

import java.io.File;

/**
 * FileCopierPathGenerator can be implemented by any class that wishes to provide a FileCopier
 * with destination paths for files to be copied rather than allowing the FileCopier to determine
 * these paths on it's own.
 *  
 * @author Scott C Arnold
 */
public interface FileCopierPathGenerator {

	public String generateDestinationPath(File file);
}
