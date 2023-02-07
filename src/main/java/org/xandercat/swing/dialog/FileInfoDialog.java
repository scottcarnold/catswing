package org.xandercat.swing.dialog;

import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.xandercat.swing.file.BinaryPrefix;
import org.xandercat.swing.file.FilesSize;
import org.xandercat.swing.file.FilesSizeCalculator;
import org.xandercat.swing.file.FilesSizeHandler;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.label.SpinnerIconLabel;
import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.util.SpringUtilities;
import org.xandercat.swing.worker.SwingWorkerUtil;

/**
 * Dialog for showing information on a file or directory.
 * 
 * @author Scott Arnold
 */
public class FileInfoDialog extends JDialog implements FilesSizeHandler {

	private static final long serialVersionUID = 2009102301L;
	
	private File file;
	private ImageIcon icon;
	private boolean isDirectory;
	private SpinnerIconLabel loadingLabel;
	private FilesSizeCalculator calc;
	
	public FileInfoDialog(Window parent, File file, FileIconCache iconCache) {
		super(parent);
		setModalityType(ModalityType.MODELESS);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.file = file;
		this.icon = iconCache.get(file);
		setSize(new Dimension(400, 250));
		Boolean isDirectory = FileUtil.isDirectory(file);
		this.isDirectory = (isDirectory == Boolean.TRUE);
		String titlePrefix = (this.isDirectory)? "Directory: " : "File: ";
		setTitle(titlePrefix + file.getAbsolutePath());
		this.loadingLabel = new SpinnerIconLabel("Loading...", 150, 30, 8, 3);
		this.loadingLabel.setFont(this.loadingLabel.getFont().deriveFont(16f));
		this.loadingLabel.startAnimate();
		JPanel panel = new JPanel();
		if (isDirectory == null) {
			panel.add(new JLabel("Unable to read file."));
			setContentPane(panel);
			this.loadingLabel.stopAnimate();
		} else {
			panel.add(this.loadingLabel);
			this.calc = new FilesSizeCalculator(Collections.singletonList(file), this);
			setContentPane(panel);
			SwingWorkerUtil.execute(calc);
		}
		setLocationRelativeTo(parent);
	}
	
	public void showDialog() {
		setVisible(true);
	}

	public void handleDirectoryProcessing(List<File> directories) {
		// no operation	
	}

	public void handleFilesSize(FilesSizeCalculator calculator,	FilesSize filesSize) {
		this.loadingLabel.stopAnimate();
		JPanel panel = new JPanel(new SpringLayout());
		panel.add(new JLabel((isDirectory? "Directory: " : "File: ")));
		if (isDirectory && FileUtil.isDirectoryRootPath(file)) {
			panel.add(new JLabel(file.getAbsolutePath(), icon, SwingConstants.LEFT));
		} else {
			panel.add(new JLabel(file.getName(), icon, SwingConstants.LEFT));
		}
		panel.add(new JLabel("Parent:"));
		panel.add(new JLabel(file.getParent()));
		panel.add(new JLabel("Size:"));
		panel.add(new JLabel(FileUtil.formatFileSize(filesSize.getBytes(), BinaryPrefix.GiB)));
		panel.add(new JLabel("Last Modified:"));
		panel.add(new JLabel(SimpleDateFormat.getDateTimeInstance().format(new Date(file.lastModified()))));
		String attribs = "";
		if (file.canRead()) {
			attribs += "R ";
		}
		if (file.canWrite()) {
			attribs += "W ";
		}
		if (file.isHidden()) {
			attribs += "H ";
		}
		panel.add(new JLabel("Attributes:"));
		panel.add(new JLabel(attribs));
		int rows = 5;
		if (isDirectory) {
			panel.add(new JLabel("Files:"));
			panel.add(new JLabel(NumberFormat.getInstance().format(filesSize.getFiles())));
			panel.add(new JLabel("Subdirectories:"));
			panel.add(new JLabel(NumberFormat.getInstance().format(filesSize.getDirectories()-1)));
			rows += 2;
		}
		SpringUtilities.makeCompactGrid(panel, rows, 2, 10, 10, 5, 5);
		//panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		//panel.setAlignmentY(Component.CENTER_ALIGNMENT);
		JScrollPane scrollPane = new JScrollPane(panel);
		setContentPane(scrollPane);
		validate();
	}

	public void handleFilesSizeInterrupted() {
		this.loadingLabel.stopAnimate();
		JPanel panel = new JPanel();
		panel.add(new JLabel("Interrupted."));
		setContentPane(panel);
		validate();
	}
	
	@Override
	public void dispose() {
		this.loadingLabel.stopAnimate();
		if (this.calc != null && !this.calc.isDone()) {
			this.calc.cancel(true);
		}
		super.dispose();
	}
}
