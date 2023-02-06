package org.xandercat.swing.tree;

import java.io.File;
import java.util.List;

/**
 * CheckboxFileTreeListener can be implemented by any class that wishes to be notified of 
 * selection changes in the tree.
 * 
 * @author Scott C Arnold
 */
public interface CheckboxFileTreeListener {

	public void filesChanged(CheckboxFileTree source, List<File> addedFiles, List<File> removedFiles);
}
