package org.xandercat.swing.dnd;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.table.FileTable;
import org.xandercat.swing.table.FileTableModel;
import org.xandercat.swing.tree.FileTree;
import org.xandercat.swing.tree.FileTreeNode;

/**
 * FileTransferHandler is a transfer handler for handling the transfer of files via drag and drop (move
 * operation only).  Supported Java classes include FileTree and FileTable.  Limited support for drag and 
 * drop from outside Java (such as system clipboard, drag to/from desktop, system file browser, etc).
 * 
 * FileTransferHandler only handles creating the transferable and removing file nodes/rows from the
 * FileTree or FileTable if either are the source.  Actual moving of the files and adding of the files
 * to the destination FileTree or FileTable should be handled by a DropTargetListener.
 * 
 * @author Scott C Arnold
 */
public class FileTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 2009040401L;
	private static final Logger log = LogManager.getLogger(FileTransferHandler.class);
	
	private String parent;
	
	public FileTransferHandler(JComponent parent) {
		this.parent = parent.getName();
	}
	
	private void log(String s) {
		log.debug(" (parent->" + parent + ") " + s);
	}
	
	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		boolean canImport = true;
		if (transferFlavors != null && comp instanceof Transferable) {
			Transferable trans = (Transferable) comp;
			for (DataFlavor flavor : transferFlavors) {
				if (!trans.isDataFlavorSupported(flavor)) {
					canImport = false;
				}
			}
		} else {
			canImport = false;
		}
		log("canImport = " + canImport);
		return canImport;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		log("createTransferable");
		List<File> files = null;
		if (c instanceof FileTable) {
			files = ((FileTable) c).getSelectedFiles();
		} else if (c instanceof FileTree) {
			files = ((FileTree) c).getSelectedFiles();
		} 
		return (files == null)? null : new FileTransferData(files);
	}

	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		log("exportAsDrag");
		super.exportAsDrag(comp, e, action);
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		log("exportDone");
		if (action != TransferHandler.MOVE) {
			log.warn("Unexpected action " + action + " on file transfer completion.");
		}
		try {
			@SuppressWarnings("unchecked")
			List<File> files = (List<File>) data.getTransferData(DataFlavor.javaFileListFlavor);
			if (source instanceof FileTree) {
				FileTree tree = (FileTree) source;
				DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
				for (File file : files) {
					FileTreeNode node = tree.findNodeForFile(file);
					if (node != null) {
						model.removeNodeFromParent(node);
					}
				}
			} else if (source instanceof FileTable) {
				FileTable table = (FileTable) source;
				FileTableModel model = (FileTableModel) table.getModel();
				for (File file : files) {
					model.removeFile(file);
				}
			}
		} catch (Exception e) {
			log.error("Error completing file transfer.", e);
		}
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {
		log("exportToClipboard");
		super.exportToClipboard(comp, clip, action);
	}

	@Override
	public int getSourceActions(JComponent c) {
		log("getSourceActions");
		return TransferHandler.MOVE;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		log("importData");
		return super.importData(comp, t);
	}
}
