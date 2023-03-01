package org.xandercat.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.app.ApplicationFrame;
import org.xandercat.swing.component.HtmlPane;
import org.xandercat.swing.label.AALabel;

import org.markdownj.MarkdownProcessor;

/**
 * AboutDialog is a general purpose dialog for showing information about an application. (as might be launched
 * from a menu option such as "Help->About...").  The primary HTML content is handled using JEditorPanes
 * that support hyperlinks and handle wrapping the HTML content.
 * 
 * Call the build() method after adding all desired content to the dialog.
 * 
 * @author Scott C Arnold
 */
public class AboutDialog extends JDialog implements HyperlinkListener {

	private static final long serialVersionUID = 2009020201L;
	private static final Logger log = LogManager.getLogger(AboutDialog.class);
	
	private Dimension maximumViewportSize = new Dimension(550, 300);
	private JPanel htmlPanel;
	private List<HtmlPane> htmlPanes = new ArrayList<HtmlPane>();
	private JScrollPane htmlScrollPane;
	private Frame owner;
	private String applicationName;
	private String applicationVersion;
	private ImageIcon imageIcon;

	private static ImageIcon getImageIcon(URL imageURL) {
		return (imageURL == null)? null : new ImageIcon(imageURL);
	}
	
	public AboutDialog(Frame owner, String applicationName, String applicationVersion) {
		super(owner, true);
		this.owner = owner;
		this.applicationName = applicationName;
		this.applicationVersion = applicationVersion;
	}
	
	public AboutDialog(ApplicationFrame applicationFrame) {
		this(applicationFrame, applicationFrame.getApplicationName(), applicationFrame.getApplicationVersion());
	}
	
	public void setImageIcon(ImageIcon imageIcon) {
		this.imageIcon = imageIcon;
	}
	
	public void setImageIcon(URL imageURL) {
		this.imageIcon = getImageIcon(imageURL);
	}
	
	public void addHtmlContent(URL htmlURL) {
		try {
			HtmlPane htmlPane = new HtmlPane(htmlURL);
			this.htmlPanes.add(htmlPane);
		} catch (IOException ioe) {
			log.error("Unable to load HTML text", ioe);
		}
	}
	
	public void addMarkdownContent(URL markdownURL) {
		addMarkdownContent(markdownURL, null);
	}
	
	public void addMarkdownContent(URL markdownURL, String cssStyle) {
		addMarkdownContent(new File(markdownURL.getFile()), cssStyle);
	}
	
	public void addMarkdownContent(File markdownFile) {
		addMarkdownContent(markdownFile, null);
	}
	
	public void addMarkdownContent(File markdownFile, String cssStyle) {
		StringBuilder markdownText = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(markdownFile))) {
			String line = reader.readLine();
			while (line != null) {
				markdownText.append(line).append("\n");
				line = reader.readLine();
			}
		} catch (IOException ioe) {
			log.error("Unable to fully read markdown text from file", ioe);
		}
		MarkdownProcessor markdownProcessor = new MarkdownProcessor();
		String html = markdownProcessor.markdown(markdownText.toString());
		if (cssStyle != null) {
			html = "<div style=\"" + cssStyle + "\">\n" + html + "</div>";
		}
		HtmlPane htmlPane = new HtmlPane(html);
		this.htmlPanes.add(htmlPane);
	}
	
	public void build() {
		setTitle("About " + applicationName);
		this.htmlPanes = (htmlPanes == null)? new ArrayList<HtmlPane>() : htmlPanes;
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel detailPanel = new JPanel(new BorderLayout());
		if (imageIcon != null) {
			JPanel imagePanel = new JPanel();
			imagePanel.add(new JLabel(imageIcon));
			detailPanel.add(imagePanel,BorderLayout.WEST);
		}
		JPanel detailTextPanel = new JPanel();
		detailTextPanel.setLayout(new BorderLayout());
		detailTextPanel.setBackground(Color.WHITE);
		detailTextPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		Font headingFont = new Font("Sans-serif", Font.BOLD, 14);
		Font font = new Font("Sans-serif", Font.PLAIN, 12);
		JPanel pnvPanel = new JPanel();
		pnvPanel.setLayout(new BoxLayout(pnvPanel, BoxLayout.Y_AXIS));
		pnvPanel.setBackground(Color.WHITE);
		pnvPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		JLabel label = new AALabel(applicationName);
		label.setFont(headingFont);
		label.setForeground(Color.BLACK);
		pnvPanel.add(label);
		label = new AALabel("Version: " + applicationVersion);
		label.setFont(headingFont);
		label.setForeground(Color.BLACK);
		pnvPanel.add(label);
		detailTextPanel.add(pnvPanel,BorderLayout.NORTH);
		this.htmlPanel = new JPanel();
		htmlPanel.setLayout(new BoxLayout(htmlPanel, BoxLayout.Y_AXIS));
		for (HtmlPane htmlPane : this.htmlPanes) {
			htmlPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			htmlPane.setFont(font);
			htmlPane.setForeground(Color.BLACK);
			htmlPanel.add(htmlPane);
		}
		this.htmlScrollPane = new JScrollPane(htmlPanel);
		this.htmlScrollPane.setBorder(null);
		this.htmlScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		setMaximumViewportSize(this.maximumViewportSize);
		detailTextPanel.add(htmlScrollPane, BorderLayout.CENTER);
		detailPanel.add(detailTextPanel, BorderLayout.CENTER);
		mainPanel.add(detailPanel,BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});
		buttonPanel.add(okButton);
		mainPanel.add(buttonPanel,BorderLayout.SOUTH);
		setContentPane(mainPanel);
		pack();
		setLocationRelativeTo(owner);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// this call won't work unless it is invoked later
				htmlScrollPane.getViewport().setViewPosition(new Point(0, 0));		
			}
		});
	}
	
	public Dimension getMaximumViewportSize() {
		return maximumViewportSize;
	}

	/**
	 * Set a maximum size for the viewport area.  Internally, this is setting the scroll pane's 
	 * preferred size in such a way that it behaves as if a maximum size was specified.
	 *  
	 * @param maximumViewportSize		maximum size for the viewable area
	 */
	public void setMaximumViewportSize(Dimension maximumViewportSize) {
		this.maximumViewportSize = maximumViewportSize;
		for (HtmlPane htmlPane : htmlPanes) {
			htmlPane.setMaxContentWidth(0);
		}
		int width = Math.min(htmlPanel.getPreferredSize().width, maximumViewportSize.width);
		int height = Math.min(htmlPanel.getPreferredSize().height, maximumViewportSize.height);
		int maxHtmlContentWidth = width - 30;
		boolean setMaxContentWidth = false;
		for (HtmlPane htmlPane : htmlPanes) {
			if (htmlPane.getPreferredSize().width > maxHtmlContentWidth) {
				setMaxContentWidth = true;
				break;
			}
		}
		if (setMaxContentWidth) {
			for (HtmlPane htmlPane : htmlPanes) {
				int oldWidth = htmlPane.getPreferredSize().width;
				htmlPane.setMaxContentWidth(maxHtmlContentWidth);
				log.debug("html pane preferred width changed from " + oldWidth + " to " + htmlPane.getPreferredSize().width);
			}
		}
		htmlScrollPane.setPreferredSize(new Dimension(width, height));
	}

	/**
	 * Create and return a JMenuItem with an action that shows this dialog.
	 * 
	 * @return			JMenuItem with an action that shows this dialog
	 */
	public JMenuItem buildMenuItem() {
		JMenuItem item = new JMenuItem(getTitle());
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				AboutDialog.this.setVisible(true);
			}
		});
		return item;
	}
	
	public void hyperlinkUpdate(HyperlinkEvent event) {	
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				log.info("Launching in browser: " + event.getURL().toString());
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
}
