package org.xandercat.swing.component;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HtmlPane extends JEditorPane implements HyperlinkListener {

	private static final long serialVersionUID = 2009073001L;
	private static final Logger log = LogManager.getLogger(HtmlPane.class);
	private static final String WIDTH_DIV_PREFIX = "<div class=\"HtmlPane\" ";
	
	private int maxContentWidth = 0;	// maximum width of HTML content; 0 = no maximum
	
	public HtmlPane() {
		super();
		setContentType("text/html");
		initialize();		
	}
	
	public HtmlPane(URL url) throws IOException {
		super(url);
		initialize();
		//putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
	}

	public HtmlPane(String html) {
		super("text/html", html);
		initialize();
	}
	
	private void initialize() {
		try {
			setEditable(false);
		} catch (Exception e) {
			// strange...this has resulted in NPE on Mac on rare occasions
			log.warn("Unable to set HtmlPane editable to false", e);
		}
		addHyperlinkListener(this);
	}
	
	public int getMaxContentWidth() {
		return maxContentWidth;
	}
	
	public void setMaxContentWidth(int maxContentWidth) {
		String text = getText();
		int bodyCloseStart = text.indexOf("</body");
		if (bodyCloseStart < 0) {
			bodyCloseStart = text.indexOf("</BODY");
		}
		if (bodyCloseStart < 0) {
			log.error("Unable to set max content width; cannot find body close tag");
			return;
		}
		int divStart = text.indexOf(WIDTH_DIV_PREFIX);
		int divEnd = 0;
		if (divStart < 0) {
			divStart = text.indexOf("<body");
			if (divStart < 0) {
				divStart = text.indexOf("<BODY");
			}
			if (divStart < 0) {
				log.error("Unable to set max content width; cannot find start of HTML body");
				return;
			}
			divStart = text.indexOf(">", divStart) + 1;
			if (divStart <= 0) {
				log.error("Unable to set max content width; cannot find end of HTML body tag");
				return;
			}
			divEnd = divStart;
		} else {
			divEnd = text.indexOf(">", divStart) + 1;
		}
		if (maxContentWidth <= 0) {
			if (divStart != divEnd) {
				StringBuilder sb = new StringBuilder();
				sb.append(text.substring(0, divStart));
				sb.append(text.substring(divEnd, bodyCloseStart - divEnd - 6));
				sb.append(text.substring(bodyCloseStart));
				setText(sb.toString());
			}
		} else {
			String divText = WIDTH_DIV_PREFIX + "style=\"width: " + maxContentWidth + "\">";
			StringBuilder sb = new StringBuilder();
			sb.append(text.substring(0, divStart));
			sb.append(divText);
			if (divStart != divEnd) {
				sb.append(text.substring(divEnd, bodyCloseStart - divEnd));
				sb.append("</div>");
				sb.append(text.substring(bodyCloseStart));
			} else {
				sb.append(text.substring(divEnd));
			}
			setText(sb.toString());
		}
		this.maxContentWidth = maxContentWidth;
		invalidate();
	}
	
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				log.info("Launching browser: " + event.getURL().toString());
				Desktop.getDesktop().browse(event.getURL().toURI());
			} catch (Exception e) {
				log.warn("Unable to launch web browser", e);
			}
		} else if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		} else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}		
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		Graphics2D graphics2D = (Graphics2D) graphics;
		// bullets on a JEditorPane are just far too ugly to leave antialiasing off
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		super.paintComponent(graphics);
	}
	
	
}
