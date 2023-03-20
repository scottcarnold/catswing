package org.xandercat.swing.file;

import java.io.File;

public class SwingFileCopierEvent {

	public static enum Type {
		COPYING, COPIED, COPY_COMPLETE, COPY_PROGRESS;
	}
	
	private Type type;
	private File from;
	private File to;
	private boolean isDirectory;
	private FileCopier.CopyResult copyResult;
	private boolean resolutionRequired;
	private long bytesCopied;
	private boolean copyComplete;
	private boolean copyCancelled;
	
	public SwingFileCopierEvent() {
	}
	
	public void setCopyingType(File from, File to, boolean isDirectory) {
		this.type = Type.COPYING;
		this.from = from;
		this.to = to;
		this.isDirectory = isDirectory;
	}
	
	public void setCopiedType(File from, File to, boolean isDirectory, FileCopier.CopyResult copyResult) {
		this.type = Type.COPIED;
		this.from = from;
		this.to = to;
		this.isDirectory = isDirectory;
		this.copyResult = copyResult;
	}
	
	public void setCopyCompleteType(boolean resolutionRequired, boolean copyCancelled) {
		this.type = Type.COPY_COMPLETE;
		this.resolutionRequired = resolutionRequired;
		this.copyCancelled = copyCancelled;
	}

	public void setCopyProgressType(File from, File to, long bytesCopied, boolean copyComplete) {
		this.type = Type.COPY_PROGRESS;
		this.from = from;
		this.to = to;
		this.bytesCopied = bytesCopied;
		this.copyComplete = copyComplete;
	}
	
	public Type getType() {
		return type;
	}

	public File getFrom() {
		return from;
	}

	public File getTo() {
		return to;
	}

	public boolean isDirectory() {
		return isDirectory;
	}
	
	public FileCopier.CopyResult getCopyResult() {
		return copyResult;
	}

	public boolean isResolutionRequired() {
		return resolutionRequired;
	}

	public boolean isCopyCancelled() {
		return copyCancelled;
	}
	
	public long getBytesCopied() {
		return bytesCopied;
	}

	public boolean isCopyComplete() {
		return copyComplete;
	}
}
