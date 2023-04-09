package org.xandercat.swing.menu;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.app.ApplicationFrame;
import org.xandercat.swing.file.FileManager;
import org.xandercat.swing.file.SaveOnCloseAction;

/**
 * RecentlyLoadedFilesManager manages a list of files and a set of menu items for those files.
 * When addFilesToMenu is called, the RecentlyLoadedFilesManager will add menu items to the 
 * provided menu for the list of files.  The menu items created will fire RecentlyLoadedActionEvents.
 * To catch these events, the calling class should add a RecentlyLoadedActionListener to the 
 * RecentlyLoadedFilesManager.  The RecentlyLoadedActionListener will then be notified whenever
 * a recently loaded file menu item is activated and can retrieve the associated file from the
 * event.
 * 
 * @author Scott C Arnold
 */
public class RecentlyLoadedFilesManager {

	private static final Logger log = LogManager.getLogger(RecentlyLoadedFilesManager.class);
			
	public static final String DEFAULT_FILE_EXTENSION = ".rlf";
	
	private JMenu menu;
	private List<File> recentlyLoadedFiles;
	private int maxRecentlyLoadedFiles;
	private List<RecentlyLoadedActionListener> listeners;
	private int menuStartPosition;
	private int maxFilenameDisplayLength;
	
	private static List<File> loadRecentlyLoadedFiles(File file) {
		List<File> recentlyLoadedFiles = new ArrayList<File>();
		if (file.exists()) {
			try {
				List<?> list = FileManager.loadObject(file, List.class);
				for (Object item : list) {
					if (item instanceof File) {
						recentlyLoadedFiles.add((File) item);
					}
				} 
			} catch (IOException ioe) {
				log.error("Unable to read recently loaded files file.", ioe);
			}
		}
		return recentlyLoadedFiles;
	}

	private static List<File> loadRecentlyLoadedFiles(Window window) {
		return loadRecentlyLoadedFiles(getDefaultFile(window));
	}
	
	private static File getDefaultFile(Window window) {
		String name = window.getClass().getName();
		int idx = name.lastIndexOf('.');
		if (idx >= 0) {
			name = name.substring(idx+1);
		}
		return new File(name + DEFAULT_FILE_EXTENSION);
	}
	
	/**
	 * Construct a new RecentlyLoadedFilesManager for the given parent window, menu, and maximum 
	 * list size.  The list of recent files will be loaded from a file using the default file name 
	 * and extension.  The file should contain a List&lt;File&gt;, and should be in order from newest file 
	 * to oldest file.  Menu items for any files in the list are automatically added to the given
	 * menu in this constructor, so call this constructor after adding any other menu items that should
	 * appear prior to the recently loaded files.  The list of files will be saved back to the file 
	 * when the parent window closes.
	 * 
	 * @param applicationFrame			the application frame
	 * @param maxRecentlyLoadedFiles	the maximum number of files that should be contained in the list
	 */
	public RecentlyLoadedFilesManager(ApplicationFrame applicationFrame, int maxRecentlyLoadedFiles) {
		this(loadRecentlyLoadedFiles(applicationFrame), maxRecentlyLoadedFiles);
		applicationFrame.addCloseListener(
				new SaveOnCloseAction<List<File>>(getDefaultFile(applicationFrame), this.recentlyLoadedFiles)
		);		
	}
	
	/**
	 * Construct a new RecentlyLoadedFilesManager for the given parent window, menu, file, and maximum 
	 * list size.  The file should contain a List&lt;File&gt;, and should be in order from newest file 
	 * to oldest file.  Menu items for any files in the list are automatically added to the given
	 * menu in this constructor, so call this constructor after adding any other menu items that should
	 * appear prior to the recently loaded files.  The list of files will be saved back to the file 
	 * when the parent window closes.
	 * 
	 * @param applicationFrame			the application frame
	 * @param recentlyLoadedFilesFile	the file to load and save the list of files from and to
	 * @param maxRecentlyLoadedFiles	the maximum number of files that should be contained in the list
	 */
	public RecentlyLoadedFilesManager(ApplicationFrame applicationFrame, File recentlyLoadedFilesFile, int maxRecentlyLoadedFiles) {
		this(loadRecentlyLoadedFiles(recentlyLoadedFilesFile), maxRecentlyLoadedFiles);
		applicationFrame.addCloseListener(
				new SaveOnCloseAction<List<File>>(recentlyLoadedFilesFile, this.recentlyLoadedFiles)
		);
	}
	
	/**
	 * Construct a new RecentlyLoadedFilesManager for the given list of files, and maximum list size.
	 * The list of files passed in will be the list that is modified, and should be in order from newest
	 * file to oldest file.  When using this constructor, the list of recently loaded files is not 
	 * automatically saved to a file.
	 * 
	 * @param recentlyLoadedFiles		the recently loaded files list to be managed
	 * @param maxRecentlyLoadedFiles	the maximum size the recently loaded files list is allowed to grow to
	 */
	public RecentlyLoadedFilesManager(List<File> recentlyLoadedFiles, int maxRecentlyLoadedFiles) {
		if (recentlyLoadedFiles == null) {
			throw new IllegalArgumentException("The recently loaded files list cannot be null.");
		} 
		this.recentlyLoadedFiles = recentlyLoadedFiles;
		this.maxRecentlyLoadedFiles = maxRecentlyLoadedFiles;
		this.maxFilenameDisplayLength = 30;
		this.listeners = new ArrayList<RecentlyLoadedActionListener>();
	}
	
	/**
	 * Add menu items for recently loaded files to the given menu.  Items will be added at the end of 
	 * the menu, so call this method when constructing the menu at the point where you want the menu
	 * items added.
	 * 
	 * @param menu						the menu to add recently loaded files to; normally a File menu
	 */
	public void addFilesToMenu(JMenu menu) {
		this.menu = menu;	
		this.menuStartPosition = menu.getItemCount();
		for (File file : this.recentlyLoadedFiles) {
			JMenuItem menuItem = getRecentlyLoadedMenuItem(file);
			menu.add(menuItem);
		}
	}
	
	private JMenuItem getRecentlyLoadedMenuItem(final File file) {
		String shortFilename = file.getAbsolutePath();
		if (shortFilename.length() > maxFilenameDisplayLength) {
			shortFilename = "..." + shortFilename.substring(shortFilename.length() - maxFilenameDisplayLength);
		}
		JMenuItem menuItem = new JMenuItem(shortFilename);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				fireRecentlyLoadedActionEvent(event, file);
			}
		});
		return menuItem;
	}
	
	/**
	 * Get the maximum length to display for a file name in a menu item.
	 * 
	 * @return		maximum length to display
	 */
	public int getMaxFilenameDisplayLength() {
		return maxFilenameDisplayLength;
	}

	/**
	 * Set the maximum length to display for a file name in a menu item.
	 * 
	 * @param maxFilenameDisplayLength		maximum length to display
	 */
	public void setMaxFilenameDisplayLength(int maxFilenameDisplayLength) {
		this.maxFilenameDisplayLength = maxFilenameDisplayLength;
	}

	/**
	 * Get the List of recently loaded files.
	 * 
	 * @return				list of recently loaded files
	 */
	public List<File> getRecentlyLoadedFiles() {
		return recentlyLoadedFiles;
	}
	
	/**
	 * Get the most recently loaded file.
	 * 
	 * @return				the most recently loaded file
	 */
	public File getMostRecentLoadedFile() {
		if (recentlyLoadedFiles != null && recentlyLoadedFiles.size() > 0) {
			return recentlyLoadedFiles.get(0);
		}
		return null;
	}
	
	/**
	 * Add action listener to be notified whenever a recently loaded file menu item is activated.
	 * 
	 * @param listener		the recently loaded action listener
	 */
	public void addRecentlyLoadedActionListener(RecentlyLoadedActionListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Call this method whenever a new file is opened/saved.  This method will either add the new file
	 * to the list or bump it up to the top of the list if already in the list.  
	 * 
	 * @param file			the file to add
	 */
	public void fileLoadedOrSaved(File file) {
		int index = recentlyLoadedFiles.indexOf(file);
		if (index < 0) {
			// file not in list; add it to list and drop oldest file if already at max allowed
			if (recentlyLoadedFiles.size() >= maxRecentlyLoadedFiles) {
				removeAtIndex(recentlyLoadedFiles.size() - 1);
			}
			addAtIndex(0, file);
		} else if (index > 0) {
			// file in list but not at top; bump it to top
			removeAtIndex(index);
			addAtIndex(0, file);
		}
	}

	/**
	 * Call this method to remove an obsolete file from the list.
	 * 
	 * @param file file to remove from recently loaded files list
	 */
	public void remove(File file) {
		int index = recentlyLoadedFiles.indexOf(file);
		if (index >= 0) {
			removeAtIndex(index);
		}
	}
	
	private void addAtIndex(int index, File file) {
		JMenuItem menuItem = getRecentlyLoadedMenuItem(file);
		File bumpFile = file;
		for (int i=index; i<this.recentlyLoadedFiles.size(); i++) {
			File tempFile = this.recentlyLoadedFiles.get(i);
			this.recentlyLoadedFiles.set(i, bumpFile);
			bumpFile = tempFile;
		}
		this.recentlyLoadedFiles.add(bumpFile);
		if (this.menu != null) {
			this.menu.insert(menuItem, menuStartPosition + index);
		}
	}
	
	private void removeAtIndex(int index) {
		this.recentlyLoadedFiles.remove(index);
		if (this.menu != null) {
			this.menu.remove(menuStartPosition + index);
		}
	}
	
	private void fireRecentlyLoadedActionEvent(ActionEvent event, File file) {
		RecentlyLoadedActionEvent recentlyLoadedEvent = new RecentlyLoadedActionEvent(event, file);
		for (RecentlyLoadedActionListener listener : this.listeners) {
			listener.actionPerformed(recentlyLoadedEvent);
		}
	}
	
	
}
