package org.xandercat.swing.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.dnd.FileTransferHandler;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.worker.SwingWorkerUtil;

/**
 * FileTree is a JTree designed for displaying a file system tree.  Hidden nodes are lazy loaded as they
 * become visible, and loading is performed in a separate thread for better user interface performance.
 * FileTree uses a custom renderer and icon set, and includes utility methods for actions such as refreshing
 * nodes in the tree.
 * 
 * TODO:  Add the ability to toggle showing hidden files on and off
 * 
 * @author Scott C Arnold
 */
public class FileTree extends JTree implements DropTargetListener, TreeWillExpandListener, TreeSelectionListener, TreeStateSaveableTree {

	private static final long serialVersionUID = 2009022001L;
	private static final Logger log = LogManager.getLogger(FileTree.class);
	
	private FileTreeFactory factory;
	private boolean expandSelectedPaths;
	private ReentrantLock lock = new ReentrantLock();		// can be used when manipulating nodes
	
	/**
	 * Construct a new file tree with the given root node.  
	 * 
	 * @param factory		the file tree factory to assign to this tree
	 * @param root			the file tree root node
	 * @param fileIconCache	file icon cache
	 */
	public FileTree(FileTreeFactory factory, FileTreeNode root, FileIconCache fileIconCache) {
		super(root);
		setRowHeight(0);	// let the renderer decide the row height
		this.factory = factory;
		addTreeWillExpandListener(this);
		addTreeSelectionListener(this);
		setCellRenderer(new FileTreeCellRenderer(fileIconCache));
	}

	public void lock() {
		this.lock.lock();
	}
	
	public void unlock() {
		this.lock.unlock();
	}
	
	@Override
	public void setDragEnabled(boolean enabled) {
		if (enabled) {
			setTransferHandler(new FileTransferHandler(this));
			setDropTarget(new DropTarget(this, this));			
		} else {
			setTransferHandler(null);
			setDropTarget(null);
		}
		super.setDragEnabled(enabled);
	}
	
	public FileTreeFactory getFactory() {
		return factory;
	}
	
	public boolean isShowDirectoriesOnly() {
		return factory.isDirectoriesOnly();
	}
	
	public boolean isExpandSelectedPaths() {
		return expandSelectedPaths;
	}
	
	public void setExpandSelectedPaths(boolean expandSelectedPaths) {
		this.expandSelectedPaths = expandSelectedPaths;
	}
	
	/**
	 * Refresh the currently selected node, reloading children beneath it.
	 */
	public void refreshSelectedPath() {
		TreePath path = getSelectionPath();
		if (path != null) {
			FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
			refreshNode(node);
		}
	}
	
	/**
	 * Refresh the given node, reloading children beneath it.  Should be called from
	 * event dispatch thread.
	 * 
	 * @param node			the node to refresh
	 */
	public void refreshNode(FileTreeNode node) {
		try {
			lock();
			DefaultTreeModel model = (DefaultTreeModel) getModel();
			if (!node.getFile().exists()) {
				// note: we rely here on the model using one of the FileTreeNode class' remove methods
				// so that the invalid descendants count will be properly maintained.
				model.removeNodeFromParent(node);
			} else {
				TreePath path = new TreePath(node.getPath());
				List<File> expandedDirectories = new ArrayList<File>();
				Enumeration<TreePath> xPathsEnum = getExpandedDescendants(path);
				if (xPathsEnum != null) {
					while (xPathsEnum.hasMoreElements()) {
						TreePath childPath = xPathsEnum.nextElement();
						FileTreeNode childNode = (FileTreeNode) childPath.getLastPathComponent();
						expandedDirectories.add(childNode.getFile());
					}
				}
				node.removeAllChildren();
				node.setChildrenLoaded(false);
				model.nodeStructureChanged(node);
				for (File directory : expandedDirectories) {
					FileTreeNode newNode = findNodeForFile(directory);
					if (newNode != null) {
						FileTreeNodeLoader nodeLoader = new FileTreeNodeLoader(this, newNode, true);
						SwingWorkerUtil.execute(nodeLoader);
					}
				}
			}	
		} finally {
			unlock();
		}
	}
	
	public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
		// nothing to do here
	}

	/**
	 * Start a thread to load the children of the node being expanded.
	 */
	public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
		FileTreeNode node = (FileTreeNode) event.getPath().getLastPathComponent();
		// using a node loader insures 2 levels deep are loaded
		FileTreeNodeLoader nodeLoader = new FileTreeNodeLoader(this, node, false);
		SwingWorkerUtil.execute(nodeLoader);
	}

	/**
	 * Load children of the selected node (if not already loaded), and expand the selected path 
	 * if expandSelectedPaths flag is set.
	 */
	public void valueChanged(TreeSelectionEvent event) {
		TreePath path = event.getNewLeadSelectionPath();
		if (path != null) {
			FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
			try {
				lock();
				if (!node.isChildrenLoaded()) {
					// tree is still loading and hasn't gotten to this node yet; skip ahead and load this node immediately
					List<FileTreeNode> children = factory.createChildren(node);
					factory.loadChildren(this, node, children);
					node.setChildrenLoaded(true);
				}
			} finally {
				unlock();
			}
			if (expandSelectedPaths && !path.equals(event.getOldLeadSelectionPath())) {
				expandPath(path);				
			}
		}
	}
	
	/**
	 * Get the selected node, if there is one.  This only returned a single node even if the
	 * selection mode is set to multiple.
	 * 
	 * @return		selected node
	 */
	public FileTreeNode getSelectedNode() {
		FileTreeNode selectedNode = null;
		TreePath selectionPath = getSelectionPath();
		if (selectionPath != null) {
			selectedNode = (FileTreeNode) selectionPath.getLastPathComponent();
		}
		return selectedNode;
	}
	
	/**
	 * Get the files represented by the selected nodes in the tree.
	 * 
	 * @return		files represented by the selected nodes in the tree
	 */
	public List<File> getSelectedFiles() {
		TreePath[] selectionPaths = getSelectionPaths();
		List<File> fileList = null;
		if (selectionPaths != null && selectionPaths.length > 0) {
			fileList = new ArrayList<File>();
			for (TreePath selectionPath : selectionPaths) {
				FileTreeNode selectionNode = (FileTreeNode) selectionPath.getLastPathComponent();
				fileList.add(selectionNode.getFile());
			}
		}
		return fileList;
	}

	/**
	 * Find and return the node for the given file.  Calling this method will force a 
	 * load of any file nodes down the path of the given file and should therefore be
	 * called sparingly.  If the node is not found, null is returned.
	 * 
	 * @param file		file to find node for
	 * 
	 * @return			node for given file (or null if node not found)
	 */
	public FileTreeNode findNodeForFile(File file) {
		String[] pathComponents = FileUtil.splitOnFileSeparator(file);
		FileTreeNode node = (FileTreeNode) getModel().getRoot();
		
		// find node for the given file
		if (pathComponents.length > 0) {
			int pci = 0;
			FileTreeNode nextNode = findNextNode(node, pathComponents[pci]);
			while (nextNode != null && pci < pathComponents.length) {
				pci++;
				node = nextNode;
				if (pci < pathComponents.length) {
					nextNode = findNextNode(node, pathComponents[pci]);
				}
			}	
		}
		return file.equals(node.getFile())? node : null;
	}
	
	protected FileTreeNode findNextNode(FileTreeNode node, String name) {
//		log.debug("Finding next node with name " + name);
		// load children if necessary
		try {
			lock();
			if (!node.isChildrenLoaded()) {
				List<FileTreeNode> children = factory.createChildren(node);
				factory.loadChildren(this, node, children);
			}
		} finally {
			unlock();
		}
		// search children for matching name
		for (int i=0,j=node.getChildCount(); i<j; i++) {
			FileTreeNode child = (FileTreeNode) node.getChildAt(i);
			String childName = child.getFile().getName();
			if (childName.length() == 0) {
				String absPath = child.getFile().getAbsolutePath();
				childName = absPath.substring(0, absPath.length() - File.separator.length());
			}
			if (name.equals(childName)) {
				return child;
			}
		}
		return null;
	}
	
	protected FileTreeNode findNextNode(FileTreeNode node, File file) {
//		System.out.println("Finding Next Node after " + (node.isRoot()? "root" : node.getFile().getAbsolutePath()) + " for " + file.getAbsolutePath());
		String[] filePathComponents = FileUtil.splitOnFileSeparator(file);
		if (node.isRoot()) {
			String rootPathComponent = (filePathComponents.length == 0)? "" : filePathComponents[0];
//			System.out.println("Node is root, next name is " + rootPathComponent);
			return findNextNode(node, rootPathComponent);
		}
		String[] nodePathComponents = FileUtil.splitOnFileSeparator(node.getFile());
		if (nodePathComponents.length == 0) {
			// must be the mac root, need to add one element for remaining code to work
			nodePathComponents = new String[] { "" };
		}
//		System.out.println("Node path components: " + nodePathComponents.length + "; file path components: " + filePathComponents.length);
		if (nodePathComponents.length >= filePathComponents.length) {
//			System.out.println("Cannot find name for next node.");
			return null;
		}
//		System.out.println("Name for next node is " + filePathComponents[nodePathComponents.length]);
		return findNextNode(node, filePathComponents[nodePathComponents.length]);
	}
	
	public SwingWorker<?,?> expandPathForId(Object id) {
		String filePath = (String) id;
		log.debug("Request to expand path for " + filePath);
		FileTreeNode node = null;
		if (filePath.length() == 0) {
			node = (FileTreeNode) getModel().getRoot();
		} else {
			File file = new File(filePath);
			node = findNodeForFile(file);
		}
		if (node == null) {
			log.warn("Unable to find and expand path for " + filePath);
		} else {
			// let node loader handle expanding the path and figuring out if children need to be loaded
			FileTreeNodeLoader loader = new FileTreeNodeLoader(this, node, true);
			SwingWorkerUtil.execute(loader);
			return loader;
		}
		return null;
	}

	public void dragEnter(DropTargetDragEvent event) {
		// do nothing (only interested in drop)
	}

	public void dragExit(DropTargetEvent event) {
		// do nothing (only interested in drop)
	}

	public void dragOver(DropTargetDragEvent event) {
		// do nothing (only interested in drop)
	}

	public void drop(DropTargetDropEvent event) {
		log.debug("Drop at loc " + event.getLocation().toString());
		boolean accept = event.getDropAction() == DnDConstants.ACTION_MOVE;
		lock();
		try {
			@SuppressWarnings("unchecked")
			List<File> files = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			if (accept && files != null) {
				TreePath closestPath = getClosestPathForLocation(event.getLocation().x, event.getLocation().y);
				FileTreeNode closestNode = (FileTreeNode) closestPath.getLastPathComponent();
				if (!closestNode.getFile().isDirectory()) {
					// if files are dropped on a file that is not a directory, use it's parent directory
					closestNode = (FileTreeNode) closestNode.getParent();
				}
				File dropDirectory = closestNode.getFile();
				List<FileTreeNode> refreshNodes = new ArrayList<FileTreeNode>();
				refreshNodes.add(closestNode);
				//TODO: Instead of refreshing the closest path, perform an insert for each file moved (because this will prevent node from collapsing)
				for (File file : files) {
					File destFile = new File(dropDirectory.getAbsolutePath() + File.separator + file.getName());
					if (file.renameTo(destFile)) {
						log.debug("moving/renaming " + file.getAbsolutePath());
						FileTreeNode nodeForSourceFile = findNodeForFile(file);
						if (nodeForSourceFile != null) {
							refreshNodes.add(nodeForSourceFile);
						}
					} else {
						log.warn("Unable to move/rename file " + file.getAbsolutePath() + " as " + destFile.getAbsolutePath());
					}
				}
				for (FileTreeNode node : refreshNodes) {
					log.debug("Refreshing node for file " + node.getFile().getAbsolutePath());
					if (getSelectedNode() == node) {
						setSelectionPath(null);
					}
					refreshNode(node);
				}
			}
		} catch (UnsupportedFlavorException e) {
			log.info("Unable to drop due to unsupported data flavor.", e);
			accept = false;
		} catch (Exception e) {
			log.error("Unsuccessful drop.", e);
			accept = false;
		} finally {
			unlock();
		}
		// is this necessary?  not really sure what effect it has
		if (accept) {
			log.debug("Drop was accepted");
			event.acceptDrop(event.getDropAction());
		} else {
			log.debug("Drop was rejected");
			event.rejectDrop();
		}
	}

	public void dropActionChanged(DropTargetDragEvent event) {
		// do nothing (only interested in drop)
	}
}
