package org.xandercat.swing.label;

import java.awt.Font;

import org.xandercat.swing.app.ApplicationFrame;

public class VersionLabel extends AALabel {

	private static final long serialVersionUID = 2010072401L;

	public VersionLabel(ApplicationFrame applicationFrame) {
		this("Version " + applicationFrame.getApplicationVersion());
	}
	
	public VersionLabel(String labelText) {
		super(labelText);
		setFont(new Font(getFont().getFamily(), Font.PLAIN, 12));
	}
}
