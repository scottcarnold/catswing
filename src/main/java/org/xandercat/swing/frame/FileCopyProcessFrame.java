package org.xandercat.swing.frame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.component.MessageScrollPane;
import org.xandercat.swing.file.FileCopier;
import org.xandercat.swing.file.FileCopierPathGenerator;
import org.xandercat.swing.file.FileCopyListener;
import org.xandercat.swing.file.FileCopyProgressListener;
import org.xandercat.swing.file.SwingFileCopier;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.table.ComponentRenderer;
import org.xandercat.swing.table.FileErrorTableModel;
import org.xandercat.swing.table.FileOverwriteTableModel;
import org.xandercat.swing.table.FileTable;
import org.xandercat.swing.table.JTableButtonMouseListener;
import org.xandercat.swing.worker.SwingWorkerUtil;

/**
 * FileCopyProcessFrame handles copying a set of files, displaying progress as it proceeds, and 
 * providing the means for the user to resolve copy problems.
 * 
 * To use FileCopyProcessFrame, construct it, and then call copy to launch the copy process.
 * 
 *     FileCopyProcessFrame frame = new FileCopyProcessFrame(...);
 *     frame.copy();
 * 
 * @author Scott C Arnold
 */
public class FileCopyProcessFrame extends JFrame implements FileCopyListener, WindowListener {

	//TODO:  Add way to save problem files for later retry and resolution
	
	private static final Logger log = LogManager.getLogger(FileCopyProcessFrame.class);
	private static final long serialVersionUID = 2009022101L;
	private static final String FILES_COPIED_TITLE = "Files Copied";
	private static final String DIRECTORIES_CREATED_TITLE = "Directories Created";
	private static final String ALREADY_EXIST_TITLE = "Already Existing";
	private static final String ERROR_TITLE = "Errors";
	private static final String SKIP_TITLE = "Skipped ";
	
	private JLabel headingLabel;
	private JLabel filesCopiedCounterLabel = new JLabel("0");
	private JLabel directoriesCreatedCounterLabel = new JLabel("0");
	private JLabel filesAlreadyExistCounterLabel = new JLabel("0");
	private JLabel copyErrorsCounterLabel = new JLabel("0");
	private JLabel skippedCounterLabel = new JLabel("0");
	private MessageScrollPane messageScrollPane;
	private List<File> files;
	private FileCopierPathGenerator pathGenerator;
	private File destination;
	private File source;
	private boolean copyComplete;
	private SwingFileCopier fileCopier;
	private JScrollPane overwritePane;
	private JScrollPane errorPane;
	private JSplitPane resolutionSplitPane;
	private JButton overwriteAllButton;
	private JButton overwriteCancelAllButton;
	private JButton errorRetryAllButton;
	private JButton errorCancelAllButton;
	private FileOverwriteTableModel overwriteModel;
	private FileErrorTableModel errorModel;
	private boolean startCopyMinimized;		// start the copy process frame minimized
	private boolean autoclose;				// autoclose if no problems to resolve
	private int processed;                  // includes both files and directory entries processed
	private int filesCopied;                // filesCopied includes only files (no directories) copied successfully
	private int directoriesCreated;         // directoriesCreated includes only directories (no files) created successfully
	private int toProcess;                  // total files and directory entries to process
	private List<FileCopyListener> fileCopyListeners;
	private List<FileCopyProgressListener> fileCopyProgressListeners;
	private FileIconCache fileIconCache;
	private int errorsUntilHalt;
	private int errorCount;
	private boolean haltedDueToErrors;
	private boolean testMode = false;
	private long testModeSpeedFactor;
	
	public FileCopyProcessFrame(List<File> files, FileIconCache fileIconCache,
			File destination, File source, boolean startCopyMinimized, boolean autoclose, int errorsUntilHalt) {
		super("Process " + files.size() + " Files");
		this.toProcess = files.size();
		this.files = files;
		this.fileIconCache = fileIconCache;
		this.destination = destination;
		this.source = source;
		this.startCopyMinimized = startCopyMinimized;
		this.autoclose = autoclose;
		this.errorsUntilHalt = errorsUntilHalt;
		initialize();
	}
	
	public FileCopyProcessFrame(List<File> files, FileIconCache fileIconCache,
			FileCopierPathGenerator pathGenerator, boolean startCopyMinimized, boolean autoclose, int errorsUntilHalt) {
		super("Process " + files.size() + " Files");
		this.toProcess = files.size();
		this.files = files;
		this.fileIconCache = fileIconCache;
		this.pathGenerator = pathGenerator;
		this.startCopyMinimized = startCopyMinimized;
		this.autoclose = autoclose;
		this.errorsUntilHalt = errorsUntilHalt;
		initialize();		
	}
	
	private void initialize() {
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		buildResolutionSplitPane();
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Copy Process", buildCopyProcessPanel());
		tabbedPane.addTab("Problem Resolution", resolutionSplitPane);
		setContentPane(tabbedPane);
		pack();
		setLocationRelativeTo(null);	
		this.fileCopyListeners = new ArrayList<FileCopyListener>();
	}
	private JPanel createCountPanel(String countTitle, JLabel countLabel) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel titleLabel = new JLabel(countTitle);
		panel.add(titleLabel);
		JPanel countLabelPanel = new JPanel(new BorderLayout());
		countLabelPanel.add(countLabel, BorderLayout.EAST);
		panel.add(countLabelPanel);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED),
				BorderFactory.createEmptyBorder(0, 5, 0, 5)));
		return panel;
	}
	
	private JPanel buildCopyProcessPanel() {
		JPanel copyProcessPanel = new JPanel(new BorderLayout());
		JPanel copyProcessHeaderNorthPanel = new JPanel();
		copyProcessHeaderNorthPanel.setLayout(new BoxLayout(copyProcessHeaderNorthPanel, BoxLayout.Y_AXIS));
		JPanel headingPanel = new JPanel(new FlowLayout());
		headingLabel = new JLabel("Preparing to copy files...");
		headingLabel.setFont(headingLabel.getFont().deriveFont(Font.BOLD, 14f));
		headingPanel.add(headingLabel);
		copyProcessHeaderNorthPanel.add(headingPanel);
		JPanel countersPanel = new JPanel(new FlowLayout());
		countersPanel.add(createCountPanel(FILES_COPIED_TITLE, filesCopiedCounterLabel));
		countersPanel.add(createCountPanel(DIRECTORIES_CREATED_TITLE, directoriesCreatedCounterLabel));
		countersPanel.add(createCountPanel(ALREADY_EXIST_TITLE, filesAlreadyExistCounterLabel));
		countersPanel.add(createCountPanel(SKIP_TITLE, skippedCounterLabel));
		countersPanel.add(createCountPanel(ERROR_TITLE, copyErrorsCounterLabel));
		copyProcessHeaderNorthPanel.add(countersPanel);
		copyProcessPanel.add(copyProcessHeaderNorthPanel, BorderLayout.NORTH);
		messageScrollPane = new MessageScrollPane();
		messageScrollPane.setPreferredSize(700, 240);
		copyProcessPanel.add(messageScrollPane, BorderLayout.CENTER);
		return copyProcessPanel;
	}
	
	private void buildResolutionSplitPane() {
		// overwrite panel
		JPanel overwritePanel = new JPanel();
		overwritePanel.setLayout(new BoxLayout(overwritePanel, BoxLayout.Y_AXIS));
		overwritePanel.setBorder(BorderFactory.createLoweredBevelBorder());
		JPanel overwriteHeadingPanel = new JPanel(new FlowLayout());
		overwriteHeadingPanel.add(new JLabel("Files That Already Exist:"));
		overwriteAllButton = new JButton("Overwrite All", FileOverwriteTableModel.OVERWRITE_ICON);
		overwriteCancelAllButton = new JButton("Cancel All", FileOverwriteTableModel.CANCEL_ICON);
		overwriteHeadingPanel.add(overwriteCancelAllButton);
		overwriteHeadingPanel.add(overwriteAllButton);
		overwritePanel.add(overwriteHeadingPanel);
		overwritePane = new JScrollPane();
		overwritePanel.add(overwritePane);
		// error panel
		JPanel errorPanel = new JPanel();
		errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
		errorPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		JPanel errorHeadingPanel = new JPanel(new FlowLayout());
		errorHeadingPanel.add(new JLabel("File Copy Errors:"));
		errorRetryAllButton = new JButton("Retry All", FileErrorTableModel.RETRY_ICON);
		errorCancelAllButton = new JButton("Cancel All", FileErrorTableModel.CANCEL_ICON);
		errorHeadingPanel.add(errorCancelAllButton);
		errorHeadingPanel.add(errorRetryAllButton);
		errorPanel.add(errorHeadingPanel);
		errorPane = new JScrollPane();
		errorPanel.add(errorPane);
		// split pane
		resolutionSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, overwritePanel, errorPanel);
		resolutionSplitPane.setOneTouchExpandable(true);
	}
	
	public void enableTestMode() {
		this.testMode = true;
	}
	
	public void enableTestMode(long speedFactor) {
		this.testMode = true;
		this.testModeSpeedFactor = speedFactor;
	}
	
	public void addFileCopyListener(FileCopyListener listener) {
		fileCopyListeners.add(listener);
	}
	
	public void removeFileCopyListener(FileCopyListener listener) {
		fileCopyListeners.remove(listener);
	}
	
	public void addFileCopyProgressListener(FileCopyProgressListener listener) {
		if (fileCopyProgressListeners == null) {
			fileCopyProgressListeners = new ArrayList<FileCopyProgressListener>();
		}
		fileCopyProgressListeners.add(listener);
	}
	
	public void removeFileCopyProgressListener(FileCopyProgressListener listener) {
		fileCopyProgressListeners.remove(listener);
	}
	
	public void copy() {
		if (startCopyMinimized) {
			setState(Frame.ICONIFIED);
		}
		setVisible(true);
		try {
			// create file copier
			if (pathGenerator == null) {
				fileCopier = new SwingFileCopier(files, destination, source);
			} else {
				fileCopier = new SwingFileCopier(files, pathGenerator);
			}
			if (testMode) {
				if (testModeSpeedFactor > 0) {
					fileCopier.enableTestMode(testModeSpeedFactor);
				} else {
					fileCopier.enableTestMode();
				}
			}
			fileCopier.addFileCopyListener(this);
			for (FileCopyListener listener : this.fileCopyListeners) {
				fileCopier.addFileCopyListener(listener);
			}
			if (this.fileCopyProgressListeners != null) {
				for (FileCopyProgressListener listener : this.fileCopyProgressListeners) {
					fileCopier.addFileCopyProgressListener(listener);
				}
			}
			if (testMode) {
				headingLabel.setText("Processing " + toProcess + " files/directories (SIMULATED)...");
			} else {
				headingLabel.setText("Processing " + toProcess + " files/directories...");
			}
			
			// set up overwrite files table
			overwriteModel = new FileOverwriteTableModel(fileCopier, overwriteAllButton, overwriteCancelAllButton);
			FileTable overwriteTable = new FileTable(overwriteModel, fileIconCache, null);
			overwriteTable.setDefaultRenderer(JButton.class, new ComponentRenderer());
			overwriteTable.addMouseListener(new JTableButtonMouseListener(overwriteTable));
			overwritePane.setViewportView(overwriteTable);
			
			// set up error files table
			errorModel = new FileErrorTableModel(fileCopier, errorRetryAllButton, errorCancelAllButton);
			FileTable errorTable = new FileTable(errorModel, fileIconCache, null);
			errorTable.setDefaultRenderer(JButton.class, new ComponentRenderer());
			errorTable.addMouseListener(new JTableButtonMouseListener(errorTable));
			errorPane.setViewportView(errorTable);
			
			// cleanup and start copying files
			resolutionSplitPane.setDividerLocation(0.5d);
			SwingWorkerUtil.execute(fileCopier);
		} catch (IllegalArgumentException iae) {
			headingLabel.setText("Unable to start copy process.");
			messageScrollPane.addMessage("Unable to start copy process: " + iae.getMessage());
			log.error("Unable to start copy process", iae);
		}
	}

	/**
	 * Cancel any copy currently in progress (if any).
	 */
	public boolean cancelCopyInProgress() {
		if (fileCopier != null) {
			fileCopier.cancel();
			return true;
		}
		return false;
	}
	
	public boolean isHaltedDueToErrors() {
		return haltedDueToErrors;
	}
	
	public void fileCopying(File from, File to) {
		if (from.isDirectory()) {
			messageScrollPane.addMessage("Creating directory " + to.getAbsolutePath());
		} else {
			messageScrollPane.addMessage("Copying file " + from.getAbsolutePath());
		}
	}

	public void fileCopied(File from, File to, FileCopier.CopyResult result) {
		processed++;
		String resultTag = result.toString();
		switch (result) {
		case ALREADY_EXISTS:
			filesAlreadyExistCounterLabel.setText(String.valueOf(fileCopier.getOverwriteFiles().size()));
			break;
		case COPIED:
			if (from.isDirectory()) {
				directoriesCreated++;
				directoriesCreatedCounterLabel.setText(String.valueOf(directoriesCreated));
				resultTag = "CREATED";
			} else {
				filesCopied++;
				filesCopiedCounterLabel.setText(String.valueOf(filesCopied));
			}
			break;
		case ERROR:
			errorCount++;
			copyErrorsCounterLabel.setText(String.valueOf(errorCount));
			break;
		case SKIPPED:
			skippedCounterLabel.setText(String.valueOf(fileCopier.getSkippedFiles().size()));
			break;
		}
		messageScrollPane.appendMessage(" [" + resultTag + "]");
		setTitle(processed + "/" + toProcess + " processed");
		if (errorCount > 0 && errorCount % errorsUntilHalt == 0) {
			int choice = JOptionPane.showConfirmDialog(this, 
					errorCount + " copy errors have occurred.  Do you wish to continue the copy process?", 
					"Continue?", 
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.ERROR_MESSAGE);
			if (choice == JOptionPane.NO_OPTION) {
				haltedDueToErrors = true;
				fileCopier.cancel();
			}
		}
	}
	
	public void copyComplete(boolean resolutionRequired, boolean copyCancelled) {
		this.copyComplete = true;
		if (resolutionRequired) {
			messageScrollPane.addMessage("Process complete.  Some files need resolution.");
			headingLabel.setText("Process complete.  Some files need resolution.");
			if (getState() == Frame.ICONIFIED) {
				setState(Frame.NORMAL);
			}
		} else {
			String message = copyCancelled? "Process cancelled." : "Process complete.";
			messageScrollPane.addMessage(message);
			headingLabel.setText(message);
			if (autoclose) {
				setVisible(false);
				dispose();
				return;
			}
		}
		if (!isFocusOwner()) {
			requestFocus();
		}
	}
	
	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {		
	}

	public void windowClosing(WindowEvent e) {
		if (!copyComplete) {
			int result = JOptionPane.showConfirmDialog(this,
					"Files are still being copied.\nCancel copy?",
					"Confirm Cancel Copy",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.NO_OPTION) {
				return;
			}
			fileCopier.cancel();
		}
		if (overwriteModel.getRowCount() + errorModel.getRowCount() > 0) {
			Object[] options = {"Close Copy Window", "Cancel"};
			int result = JOptionPane.showOptionDialog(this, 
					"You have not indicated how to handle some files that could not be copied.\n"
					+ "Indicate how to handle these files on the Problem Resolution tab.\n\n"
					+ "Choose Close to close without resolving uncopied files.", 
					"Confirm Exit Without Resolution", 
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null, options, options[0]);
			if (result == 1) {
				return;
			}
		}	
		setVisible(false);
		dispose();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}

