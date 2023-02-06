package org.xandercat.swing.tree;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

//TODO: May want to also clear refreshed directories from the directory size cache
public class FileTreeRefreshNodeAction extends AbstractAction {

	private static final long serialVersionUID = 20091101L;
	
	protected FileTree tree;
	protected FileTreeNode node;
	
	public FileTreeRefreshNodeAction(FileTree tree, FileTreeNode node) {
		super("Refresh");
		this.tree = tree;
		this.node = node;
	}
	
	public void actionPerformed(ActionEvent event) {
		tree.refreshNode(node);
	}
}
