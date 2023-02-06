package org.xandercat.swing.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * FileTransferData is a Transferable for transferring a list of files in a drag/drop operation.
 *  
 * @author Scott C Arnold
 */
public class FileTransferData implements Transferable {

	private static final DataFlavor[] transferDataFlavors 
		= new DataFlavor[] { DataFlavor.javaFileListFlavor };
	
	private List<File> files;
	
	public FileTransferData(List<File> files) {
		this.files = files;
	}
	
	public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
		if (dataFlavor != null && dataFlavor.isFlavorJavaFileListType()) {
			return files;
		} else {
			throw new UnsupportedFlavorException(dataFlavor);
		}
	}

	public DataFlavor[] getTransferDataFlavors() {
		return transferDataFlavors;
	}

	public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
		return dataFlavor != null && dataFlavor.isFlavorJavaFileListType();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Transfer Files: ");
		for (File file : files) {
			sb.append(file.getName() + "; ");
		}
		return sb.toString();
	}
}
