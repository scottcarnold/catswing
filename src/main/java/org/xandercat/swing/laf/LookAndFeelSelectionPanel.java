package org.xandercat.swing.laf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.LookAndFeel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.list.CustomFontListCellRenderer;
import org.xandercat.swing.util.SpringUtilities;

public class LookAndFeelSelectionPanel extends JPanel implements ListSelectionListener {

	private static final long serialVersionUID = 2010072301L;
	private static final Logger log = LogManager.getLogger(LookAndFeelSelectionPanel.class);
	private static final String DEFAULT_NAME = "Unknown";
	private static final String DEFAULT_DESCRIPTION = "No additional detail is available for this Look and Feel.";
	
	private static class LAFDetail {
		private String name = DEFAULT_NAME;
		private String className;
		private Image image;
		private String description = DEFAULT_DESCRIPTION;
	}
	
	private String[] lafNames;
	private String currentLAFName;
	private Map<String, LAFDetail> lafDetailMap = new HashMap<String, LAFDetail>();
	private JList<String> lafList;
	private JLabel detailImageLabel;
	private ImageIcon detailImage;
	private JLabel detailLabel;
	private JTextPane detailDescriptionPane;
	private StyledDocument detailDescription;
	private Style detailDescriptionStyle;
	
	public LookAndFeelSelectionPanel() {
		super();
		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf != null) {
			this.currentLAFName = laf.getName();
		}
		LookAndFeelInfo[] infoArray = UIManager.getInstalledLookAndFeels();
		this.lafNames = new String[infoArray.length];
		for (int i=0; i<infoArray.length; i++) {
			this.lafNames[i] = infoArray[i].getName();
			LAFDetail lafDetail = new LAFDetail();
			lafDetail.name = infoArray[i].getName();
			lafDetail.className = infoArray[i].getClassName();
			this.lafDetailMap.put(infoArray[i].getName(), lafDetail);
		}
		addDetail("Mac OS X", "macosx.png", "A Look and Feel that emulates Mac OS X.");
		addDetail("Windows", "windows.png", "A Look and Feel that emulates versions of Microsoft Windows.");
		addDetail("Windows Classic", "windows_classic.png", "A Look and Feel that emulates old versions of Microsoft Windows.");
		addDetail("Nimbus", "nimbus.png", "A Java Look and Feel rendered using vector graphics.\n\nThis Look and Feel was introducted on October 15, 2008 as part of Java 6 update 10.");
		addDetail("Metal", "metal.png", "A Java Look and Feel that served for many years as the default for Java applications on systems running Microsoft Windows.");
		addDetail("CDE/Motif", "motif.png", "A Look and Feel that emulates the X Window system used for old UNIX desktop environments.");
		this.lafList = new JList<String>(this.lafNames);
		this.lafList.setSelectedValue(this.currentLAFName, true);
		this.lafList.addListSelectionListener(this);
		this.lafList.setCellRenderer(new CustomFontListCellRenderer(16f));
		LAFDetail currentDetail = this.lafDetailMap.get(this.currentLAFName);
		this.detailImage = new ImageIcon();
		this.detailImageLabel = new JLabel(this.detailImage);
		this.detailLabel = new JLabel("Unknown");
		this.detailLabel.setFont(this.detailLabel.getFont().deriveFont(20f));
		this.detailDescriptionPane = new JTextPane();
		this.detailDescriptionPane.setOpaque(false);
		this.detailDescriptionPane.setPreferredSize(new Dimension(350,50));
		this.detailDescriptionPane.setBackground(new Color(0, 0, 0, 0));
		this.detailDescriptionPane.setEditable(false);
		this.detailDescription = this.detailDescriptionPane.getStyledDocument();
		this.detailDescriptionStyle = this.detailDescription.addStyle("myStyle", null);
		//StyleConstants.setBold(style, false);
		StyleConstants.setFontSize(this.detailDescriptionStyle, 16);
		StyleConstants.setBackground(this.detailDescriptionStyle, this.getBackground());	
		setDetail(currentDetail);
		setLayout(new BorderLayout());
		this.lafList.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		add(this.lafList, BorderLayout.WEST);
		this.detailImageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		add(this.detailImageLabel, BorderLayout.CENTER);
		JPanel detailPanel = new JPanel();
		detailPanel.setLayout(new SpringLayout());
		detailPanel.add(this.detailLabel);
		detailPanel.add(this.detailDescriptionPane);
		detailPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		SpringUtilities.makeCompactGrid(detailPanel, 2, 1, 0, 0, 0, 0);
		add(detailPanel, BorderLayout.EAST);
	}
	
	private void setDetail(LAFDetail detail) {
		if (detail == null) {
			detail = new LAFDetail();
		}
		this.detailLabel.setText(detail.name);
		if (detail.image == null) {
			this.detailImage = null;
			this.detailImageLabel.setIcon(null);
		} else {
			if (this.detailImage == null) {
				this.detailImage = new ImageIcon();
				this.detailImageLabel.setIcon(this.detailImage);
			}
			this.detailImage.setImage(detail.image);  //TODO: Make this nicer and see why L&F images don't show on Mac
		}
		this.detailImageLabel.repaint();
		try {
			this.detailDescription.remove(0, this.detailDescription.getLength());
			this.detailDescription.insertString(0, detail.description, this.detailDescriptionStyle);
		} catch (Exception e) {
			log.error("Unable to update detail description", e);
		}
	}
	
	private void addDetail(String lafName, String imageName, String description) {
		try {
			LAFDetail lafDetail = this.lafDetailMap.get(lafName);
			if (lafDetail == null) {
				log.debug("Unable to lookup Look & Feel detail for Look & Feel with name \"" + lafName + "\"");
				StringBuilder sb = new StringBuilder("Available Look & Feel Names: ");
				for (String availLAF : this.lafDetailMap.keySet()) {
					sb.append(" [" + availLAF + "] ");
				}
				log.debug(sb.toString());
			} else {
				lafDetail.description = description;
				lafDetail.image = ImageIO.read(getClass().getResource(imageName));
			}
		} catch (IOException ioe) {
			log.error("Unable to load " + lafName + " Look & Feel example image " + imageName, ioe);
		}
	}
	
	public String getSelectedLookAndFeelName() {
		return (String) this.lafList.getSelectedValue();
	}

	public String getSelectedLookAndFeelClassName() {
		return (String) this.lafDetailMap.get(lafList.getSelectedValue()).className;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e != null && e.getSource() == this.lafList) {
			String lafName = (String) this.lafList.getSelectedValue();
			setDetail(this.lafDetailMap.get(lafName));
		}
	}
}
