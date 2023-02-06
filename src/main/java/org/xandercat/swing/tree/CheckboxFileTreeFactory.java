package org.xandercat.swing.tree;

import java.io.File;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.file.icon.FileIconCache;

/**
 * CheckboxFileTreeFactory is a factory for creating a CheckboxFileTree starting at the system
 * roots and for creating new CheckboxFileTreeNodes.
 * 
 * @author Scott C Arnold
 */
public class CheckboxFileTreeFactory extends FileTreeFactory {

	private static final Logger log = LogManager.getLogger(CheckboxFileTreeFactory.class);
			
	private CheckboxFileTree tree;
	
	public CheckboxFileTreeFactory(boolean directoriesOnly, boolean showHiddenFiles, FileIconCache fileIconCache) {
		super(directoriesOnly, showHiddenFiles, fileIconCache);
	}

	/**
	 * Create a CheckboxFileTree starting at the system roots.
	 * 
	 * @param directoriesOnly		whether or not to only include directories
	 * @param showHiddenFiles		whether or not to show hidden files
	 * 
	 * @return						a CheckboxFileTree
	 */
	public static CheckboxFileTree createCheckboxFileTree(boolean directoriesOnly, boolean showHiddenFiles, FileIconCache fileIconCache) {
		return createCheckboxFileTree(directoriesOnly, showHiddenFiles, fileIconCache, "File System");
	}
	
	/**
	 * Create a CheckboxFileTree starting at the system roots.
	 * 
	 * @param directoriesOnly		whether or not to only include directories
	 * @param showHiddenFiles		whether or not to show hidden files
	 * 
	 * @return						a CheckboxFileTree
	 */
	public static CheckboxFileTree createCheckboxFileTree(boolean directoriesOnly, boolean showHiddenFiles, FileIconCache fileIconCache, String rootText) {
		CheckboxFileTreeFactory factory = new CheckboxFileTreeFactory(directoriesOnly, showHiddenFiles, fileIconCache);
		CheckboxFileTreeNode root = new CheckboxFileTreeNode(rootText);
		log.debug("Creating checkbox file tree...");
		factory.tree = new CheckboxFileTree(factory, root, fileIconCache);
		for (File file : File.listRoots()) {
			log.debug("Creating node for " + file.getAbsolutePath() + "...");
			FileTreeNode node = factory.createNode(root, file);
			if (!node.isInvalid()) {
				List<FileTreeNode> children = factory.createChildren(node);
				factory.loadChildren(factory.tree, node, children);
			}
			root.add(node);
		}
		log.debug("Finishing tree...");
		((DefaultTreeModel) factory.tree.getModel()).nodeStructureChanged(root);
		return factory.tree;
	}
	
	@Override
	public FileTreeNode createNode(FileTreeNode parent, File file) {
		return new CheckboxFileTreeNode(tree, file, fileIconCache, ((CheckboxFileTreeNode) parent).isSelected());
	}
}
