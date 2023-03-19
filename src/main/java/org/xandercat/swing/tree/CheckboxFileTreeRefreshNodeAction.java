package org.xandercat.swing.tree;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckboxFileTreeRefreshNodeAction extends FileTreeRefreshNodeAction {

	private static final long serialVersionUID = 20091101L;
	
	private List<File> requiredFiles = new ArrayList<File>();
	private Set<File> requiredDirectories = new HashSet<File>();
	
	public CheckboxFileTreeRefreshNodeAction(CheckboxFileTree tree, CheckboxFileTreeNode node) {
		super(tree, node);
	}

	public CheckboxFileTreeRefreshNodeAction(CheckboxFileTree tree, CheckboxFileTreeNode node, Collection<File> requiredFiles, Collection<File> requiredDirectories) {
		this(tree, node);
		// trim down the required files and directories to only those under the given node
		String nodeFilePath = node.getFile().getAbsolutePath();
		for (File file : requiredFiles) {
			if (file.getAbsolutePath().startsWith(nodeFilePath)) {
				this.requiredFiles.add(file);
				if (requiredDirectories.contains(file)) {
					this.requiredDirectories.add(file);
				}
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		CheckboxFileTree cTree = (CheckboxFileTree) tree;
		CheckboxFileTreeNode cNode = (CheckboxFileTreeNode) node;
		List<File> checkedFiles = cTree.getCheckedDescendantFiles(cNode);
		super.actionPerformed(event);
		// look through previously checked files and attempt to recheck them
		for (File checkedFile : checkedFiles) {
			// see if there is still a node for a previously checked file
			FileTreeNode tNode = tree.findNodeForFile(checkedFile);
			if (tNode != null) {
				// previously checked file still exists; recheck it
				cTree.selectAddFile(checkedFile, checkedFile.isDirectory(), true);
			} else if (this.requiredFiles.contains(checkedFile)) {
				// previously checked file no longer exists but is in the required set so add it in
				cTree.selectAddFile(checkedFile, this.requiredDirectories.contains(checkedFile), true);
			}
		}
	}	
}
