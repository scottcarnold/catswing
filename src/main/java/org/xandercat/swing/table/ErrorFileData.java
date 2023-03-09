package org.xandercat.swing.table;

import java.io.File;

import javax.swing.JButton;

/**
 * ErrorFileData is a FileData for files that are associated with some exception.
 * 
 * @author Scott C Arnold
 */
public class ErrorFileData extends FileData {

	private Throwable throwable;
	private JButton retryButton;
	private JButton cancelButton;
	private JButton detailButton;
	
	public ErrorFileData(File file, Throwable throwable, JButton retryButton, JButton cancelButton, JButton detailButton) {
		super(file);
		this.throwable = throwable;
		this.retryButton = retryButton;
		this.cancelButton = cancelButton;
		this.detailButton = detailButton;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public JButton getRetryButton() {
		return retryButton;
	}

	public void setRetryButton(JButton retryButton) {
		this.retryButton = retryButton;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	public void setCancelButton(JButton cancelButton) {
		this.cancelButton = cancelButton;
	}

	public JButton getDetailButton() {
		return detailButton;
	}

	public void setDetailButton(JButton detailButton) {
		this.detailButton = detailButton;
	}
}
