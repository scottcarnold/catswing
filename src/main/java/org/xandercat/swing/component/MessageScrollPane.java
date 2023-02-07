package org.xandercat.swing.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MessageScrollPane is a scroll pane for displaying text messages that will, when text is appended,
 * remain scrolled to the bottom when already at the bottom and remain at current scroll position when 
 * not at the bottom.  This is useful for tasks such as displaying log files that are actively being
 * appended to.
 * 
 * @author Scott C Arnold
 */
public class MessageScrollPane extends JScrollPane implements AdjustmentListener {

	private static final long serialVersionUID = 2009021901L;
	private static final Logger log = LogManager.getLogger(MessageScrollPane.class);
			
	private JTextPane textPane;
	private StyledDocument doc;
	private DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
	private boolean autoscrollEnabled;
	private int maxLines = 300;
	private int lineCount = 0;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame testFrame = new JFrame("Test MessageScrollPane");
				testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				JPanel testPanel = new JPanel(new FlowLayout());
				final MessageScrollPane messageScrollPane = new MessageScrollPane();
				messageScrollPane.setPreferredSize(400, 100);
				messageScrollPane.setMaxLines(10);
				testPanel.add(messageScrollPane);
				for (int i=0; i<25; i++) {
					messageScrollPane.addMessage("Test Message " + i);
				}
				JButton button = new JButton("Add line");
				button.addActionListener(new ActionListener() {
					private int i = 25;
					public void actionPerformed(ActionEvent event) {
						i++;
						messageScrollPane.addMessage("Test Message " + i);
						for (int j=0; j<(i-24); j++) {
							messageScrollPane.appendMessage(" blah ");
						}
					}
				});
				testPanel.add(button);
				testFrame.setContentPane(testPanel);
				testFrame.pack();
				testFrame.setLocationRelativeTo(null);
				testFrame.setVisible(true);
			}
		});
	}
	
	public MessageScrollPane() {
		super();
		getViewport().setBackground(Color.WHITE);	// prevents gray space in viewport; necessary due to how word wrap prevention is handled 
		textPane = new JTextPane() {
			private static final long serialVersionUID = 2009031801L;
			public void scrollRectToVisible(Rectangle aRect) {
				if (autoscrollEnabled) {
					super.scrollRectToVisible(aRect);
				}
			}
			public boolean getScrollableTracksViewportWidth() {
				return false;  // prevent word wrapping
			}
		};
		doc = textPane.getStyledDocument();
		textPane.setEditable(false);
		setViewportView(textPane);
		getVerticalScrollBar().addAdjustmentListener(this);
	}

	public int getMaxLines() {
		return maxLines;
	}

	public void setMaxLines(int maxLines) {
		this.maxLines = maxLines;
	}

	public void setPreferredSize(int width, int height) {
		setPreferredSize(new Dimension(width, height));
	}
	
	/**
	 * Set the date/time format string to used for the date/time that is shown at the beginning of each 
	 * message.  Setting this to null will disable date/time display.
	 * 
	 * @param dateFormatString		the date format string
	 */
	public void setShowMessageTime(String dateFormatString) {
		if (dateFormatString == null) {
			dateFormat = null;
		} else {
			dateFormat = new SimpleDateFormat(dateFormatString);
		}
	}
	
	/**
	 * Add the given message on a new line.  This should only be called from the EDT.
	 * 
	 * Note:  The message scroll pane may appear to function okay when this method is not
	 * called from the EDT, but the auto-scroll to bottom function will not work.
	 * 
	 * @param message				message to add to text pane
	 */
	public void addMessage(String message) {
		try {
			if (this.lineCount >= this.maxLines) {
				int pos = -1;
				int offset = -100;
				int length = doc.getLength();
				while (pos < 0 && offset < length) {
					offset += 100;
					String text = doc.getText(offset, 100);
					pos = text.indexOf("\n");
				}
				doc.remove(0, offset + pos + 1);
			} else {
				this.lineCount++;
			}
			doc.insertString(doc.getLength(), "\n", null);
			if (dateFormat != null) {
				doc.insertString(doc.getLength(), "[" + dateFormat.format(new Date()) + "] ", null);
			}
			doc.insertString(doc.getLength(), message, null);
		} catch (BadLocationException ble) {
			log.error("Unable to add message", ble);
		}
	}
	
	/**
	 * Append the given text to the last line.  This should only be called from the EDT. 
	 * 
	 * Embedded new-lines will be inserted as is, causing a new line.  It is recommended
	 * this not be done as it will throw off the internal line counter.
	 * 
	 * @param appendText			text to append
	 */
	public void appendMessage(String appendText) {
		try {
			doc.insertString(doc.getLength(), appendText, null);
		} catch (BadLocationException ble) {
			log.error("Unable to append message", ble);
		}
	}

	public void adjustmentValueChanged(AdjustmentEvent event) {
			if (!event.getValueIsAdjusting()) {
				return;
			}
			JScrollBar sb = getVerticalScrollBar();
			this.autoscrollEnabled = (sb.getVisibleAmount() + sb.getValue() == sb.getMaximum());
	}
}
