package org.xandercat.swing.table;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.xandercat.swing.mouse.PopupListener;

/**
 * PopupListener that has additional functionality for popups on JTables.
 * 
 * @author Scott Arnold
 */
public class JTablePopupListener extends PopupListener {

	protected JTable table;
	private int lastPopupRow;
	private boolean autoSelectOnPopup;
	
	public JTablePopupListener(JPopupMenu popup, JTable table, boolean autoSelectOnPopup) {
		super(popup);
		this.table = table;
		this.autoSelectOnPopup = autoSelectOnPopup;
	}

	public int getLastPopupRow() {
		return lastPopupRow;
	}
	
	@Override
	protected void checkForPopup(MouseEvent event) {
		if (event.isPopupTrigger()) {
			this.lastPopupRow = this.table.rowAtPoint(event.getPoint());
			if (autoSelectOnPopup && lastPopupRow >= 0) {
				table.getSelectionModel().setSelectionInterval(lastPopupRow, lastPopupRow);
			}
		}
		super.checkForPopup(event);
	}

	
}
