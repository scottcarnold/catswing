package org.xandercat.swing.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.xandercat.swing.file.icon.FileIconCache;

/**
 * FileTreeFactory is a factory for creating file tree nodes and file trees starting at 
 * system roots.
 * 
 * @author Scott C Arnold
 */
public class FileTreeFactory {

	protected FileIconCache fileIconCache;
	private boolean directoriesOnly;
	private boolean showHiddenFiles;
	
	/**
	 * Create a file tree starting at the system roots.
	 * 
	 * @param directoriesOnly		whether or not to only show directories in the tree
	 * @param showHiddenFiles		whether or not to show hidden files in the tree
	 * @param fileIconCache			file icon cache
	 * 
	 * @return			a file tree
	 */
	public static FileTree createFileTree(boolean directoriesOnly, boolean showHiddenFiles, FileIconCache fileIconCache) {
		FileTreeFactory factory = new FileTreeFactory(directoriesOnly, showHiddenFiles, fileIconCache);
		FileTreeNode root = new FileTreeNode("File System");
		for (File file : File.listRoots()) {
			FileTreeNode node = factory.createNode(root, file);
			List<FileTreeNode> children = factory.createChildren(node);
			for (FileTreeNode child : children) {
				node.add(child);
			}
			node.setChildrenLoaded(true);
			root.add(node);
		}
		root.setChildrenLoaded(true);
		return new FileTree(factory, root, fileIconCache);
	}
	
	/**
	 * Construct a new file tree factory.
	 * 
	 * @param directoriesOnly		whether or not to only show directories in the tree
	 * @param showHiddenFiles		whether or not to show hidden files in the tree
	 * @param fileIconCache			file icon cache
	 */
	public FileTreeFactory(boolean directoriesOnly, boolean showHiddenFiles, FileIconCache fileIconCache) {
		this.directoriesOnly = directoriesOnly;
		this.showHiddenFiles = showHiddenFiles;
		this.fileIconCache = fileIconCache;
	}
	
	/**
	 * Return whether this factory creates trees and nodes for directories only.
	 * 
	 * @return			whether trees and nodes are for directories only
	 */
	public boolean isDirectoriesOnly() {
		return directoriesOnly;
	}
	
	/**
	 * Return whether this factory creates trees and nodes for hidden files.
	 * 
	 * @return			whether trees and nodes are created for hidden files
	 */
	public boolean isShowHiddenFiles() {
		return showHiddenFiles;
	}
	
	/**
	 * Create and return a new file tree node for the given file and with the given parent node.
	 * 
	 * @param parent		parent node
	 * @param file			file this node represents
	 * 
	 * @return				new file tree node
	 */
	public FileTreeNode createNode(FileTreeNode parent, File file) {
		return new FileTreeNode(file, fileIconCache);
	}
	
	/**
	 * Create children for the given node.  
	 * 
	 * @param node			node to load children of
	 * 
	 * @return				whether or not children had to be loaded
	 */
	public List<FileTreeNode> createChildren(FileTreeNode node) {
		List<FileTreeNode> children = new ArrayList<FileTreeNode>();
		File file = node.getFile();
		if (file != null) {
			File[] childFiles = file.listFiles();
			if (childFiles != null) {
				for (File childFile : childFiles) {
					if ((!directoriesOnly || childFile.isDirectory())
							&& (showHiddenFiles || !childFile.isHidden())) {
						children.add(createNode(node, childFile));
					}
				}
			}
		}
		return children;
	}
	
	/**
	 * Load the given children into the given node in order from the last existing
	 * child.  This should be called from the event dispatch thread.
	 * 
	 * @param model
	 * @param node
	 * @param children
	 * @return
	 */
	public void loadChildren(FileTree tree, FileTreeNode node, List<FileTreeNode> children) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		node.setChildrenLoaded(true);
		if (children != null && children.size() > 0) {
			for (FileTreeNode child : children) {
				model.insertNodeInto(child, node, node.getChildCount());
			}
		} else {
			model.nodeChanged(node);
		}
		
	}
}
