package org.xandercat.swing.file;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.app.ApplicationFrame;
import org.xandercat.swing.app.CloseListener;
import org.xandercat.swing.menu.RecentlyLoadedFilesManager;
import org.xandercat.swing.util.FileUtil;

/**
 * FileManager simplifies the process of managing a users open files.  FileManager takes care of 
 * prompting the user when necessary and provides support for autosaving.  FileManager is not
 * thread safe.
 * 
 * To use FileManager, the saved files must consist of a single serializable object (if there are
 * multiple objects that make up a single file, you can either put them into a Collection or create
 * a wrapper class for the objects).
 * 
 * It is recommended that setClassChecked be called after creating an instance of FileManager; If an
 * object of wrong class type is loaded from file, having setClassChecked set will cause an exception
 * to occur at the time the object is loaded; if not set, the error will not be caught until something
 * attempts to use the loaded object.
 * 
 * By default, if the FileManager is managing a List of comparable objects, the List will be copied
 * into a new List and sorted before it is saved.  This prevents object order from affecting whether
 * or not a file is considered "dirty" and in need of a save.  This behavior can be disabled by 
 * calling setSaveListsInSortedOrder(false).
 * 
 * @author Scott Arnold
 */
public class FileManager<T> extends WindowAdapter implements WindowListener, CloseListener, AutoSavable {

	//TODO: Add some kind of listener where the user of the FileManager can be notified of which 
	// file is being acted upon so it can switch to it (primarly useful for handling application quit
	// without first closing opened files).
	
	//TODO: Provide a way for the using app to pick the file name?
	
	private static final Logger log = LogManager.getLogger(FileManager.class);
	private static final String DATA_FILE_NAME = "filemanager.dat";
	private static final int DEFAULT_BUFFER_SIZE = 2048;
	
	public static class FileState<T> implements Serializable { 
		private static final long serialVersionUID = 2008122201L;
		private transient T object;
		private String tempFileName;
		private String fileName;
		public T getObject() {
			return object;
		}
		public String getFileName() {
			return fileName;
		}
		public String getShortFileName() {
			return getShortFilename(fileName);
		}
	}
	private JFrame parent;
	private JFileChooser fileChooser = new JFileChooser();
	private String defaultExtension;
	private boolean multiple = false;
	private Map<String,FileState<T>> fileStates = new HashMap<String,FileState<T>>();
	private String activeFileStateKey = null;
	private File fileStateFile = new File(DATA_FILE_NAME);
	private byte buff1[] = new byte[DEFAULT_BUFFER_SIZE];
	private byte buff2[] = new byte[DEFAULT_BUFFER_SIZE];
	private AutoSaver autoSaver;
	private long autosaveInterval = 300000; // default 300000 == 5 minutes
	private int keyCount = 0;
	private List<JComponent> managedJComponents;
	private boolean managedJComponentsEnabled = false;
	private List<ActiveFileChangeListener> afcListeners;
	private List<FileManagerListener<T>> fmListeners;
	private boolean notifyFileManangerListeners = true;
	private RecentlyLoadedFilesManager recentlyLoadedFilesManager;
	private Class<?> clazz;				// for checking class when object loaded from file
	private Class<?> collectionClazz;	// for when managed object is a collection
	private boolean saveListsInSortedOrder = true;
	private String newFileDescription = "file";
	private Importer<T> importer;
	
	/**
	 * Construct a new FileManager with the given parent frame.  The FileManager will default
	 * to only allowing 1 open file at a time.  If parent is an ApplicationFrame, the FileManager
	 * will register itself as an CloseListener; otherwise, the FileManager will add itself
	 * as a window listener on the parent frame.
	 * 
	 * @param parent		Parent frame
	 */
	public FileManager(JFrame parent) {
		this.parent = parent;
		if (parent instanceof ApplicationFrame) {
			((ApplicationFrame) parent).addCloseListener(this);			
		} else {
			parent.addWindowListener(this);
			log.warn("FileManager being used without AppCloseManager; user will not be able to cancel close operations.");
		} 
	}
	
	/**
	 * Construct a new FileManager with the given parent frame, and set up a file filter with
	 * the given extension and description.  The FileManager will default to only allowing
	 * 1 open file at a time.
	 * 
	 * @param parent			parent frame
	 * @param defaultExtension	file extension for file filter
	 * @param description		description of file filter
	 */
	public FileManager(JFrame parent, final String defaultExtension, final String description) {
		this(parent);
		if (defaultExtension.startsWith(".")) {
			this.defaultExtension = defaultExtension.substring(1);
		} else {
			this.defaultExtension = defaultExtension;
		}
		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory() || file.getAbsolutePath().endsWith("." + FileManager.this.defaultExtension);
			}
			@Override
			public String getDescription() {
				return description + " (*." + FileManager.this.defaultExtension + ")";
			}
		};
		fileChooser.addChoosableFileFilter(filter);
	}
	
	/**
	 * Construct a new FileManager with the given parent frame.  If multiple is set to true,
	 * the FileManager will allow multiple files to be opened simultaneously; if false, FileManager
	 * will only allow one file to be open at a time.
	 * 
	 * @param parent			Parent frame
	 * @param multiple			Whether or not to allow multiple files to be open simultaneously
	 */
	public FileManager(JFrame parent, boolean multiple) {
		this(parent);
		this.multiple = multiple;
	}
	
	/**
	 * Construct a new FileManager with the given parent frame, setting up a file filter with
	 * the given extension and description.  If multiple is set to true, the FileManager
	 * will allow multiple files to be open simultaneously; if false, FileManager will only
	 * allow one open file at a time.
	 * 
	 * @param parent				parent frame
	 * @param multiple				whether or not to allow multiple files to be open simultaneously
	 * @param defaultExtension		file extension for file filter
	 * @param description			description for file filter
	 */
	public FileManager(JFrame parent, boolean multiple, String defaultExtension, String description) {
		this(parent, defaultExtension, description);
		this.multiple = multiple;
	}

	/**
	 * When set, any object loaded from file will be checked to ensure it is of the type specified.
	 * 
	 * @param clazz				class loaded objects should be of type
	 */
	public void setClassChecked(Class<?> clazz) {
		this.collectionClazz = null;
		this.clazz = clazz;
	}
	
	/**
	 * When set, any object loaded from fiel will be checked to ensure it is of the collection type
	 * specified and it's members are of the member class specified.  If member class is not specified,
	 * member object types will not be checked.
	 * 
	 * @param collectionClass	collection class
	 * @param memberClass		member class (class of objects in the collection)
	 */
	public void setClassChecked(Class<?> collectionClazz, Class<?> memberClazz) {
		this.collectionClazz = collectionClazz;
		this.clazz = memberClazz;
	}
	
	/**
	 * Adds a file manager listener.
	 * 
	 * @param listener
	 */
	public void addFileManagerListener(FileManagerListener<T> listener) {
		if (this.fmListeners == null) {
			this.fmListeners = new ArrayList<FileManagerListener<T>>();
		}
		this.fmListeners.add(listener);
	}
	
	/**
	 * Removes a file manager listener.
	 * 
	 * @param listener
	 */
	public void removeFileManagerListener(FileManagerListener<T> listener) {
		if (this.fmListeners != null) {
			this.fmListeners.remove(listener);
		}
	}
	
	/**
	 * Add an ActiveFileChangeListener to this FileManager.  Whenever the active file changes,
	 * all ActiveFileChangeListeners will be notified.
	 * 
	 * @param listener
	 */
	public void addActiveFileChangeListener(ActiveFileChangeListener listener) {
		if (afcListeners == null) {
			afcListeners = new ArrayList<ActiveFileChangeListener>();
		}
		afcListeners.add(listener);
	}
	
	/**
	 * Sets an importer to be used when loaded class is not of appropriate type.
	 * 
	 * @param importer
	 */
	public void setImporter(Importer<T> importer) {
		this.importer = importer;
	}
	
	/**
	 * Return whether or not lists will be saved in their natural sorted order.  This only applies
	 * to FileManager instances managing Lists of comparable objects.
	 * 
	 * @return
	 */
	public boolean isSaveListsInSortedOrder() {
		return saveListsInSortedOrder;
	}

	/**
	 * Set whether or not lists should be saved in their natural sorted order.  This only applies
	 * to FileManager instances managing Lists of comparable objects.
	 * 
	 * @param saveListsInSortedOrder
	 */
	public void setSaveListsInSortedOrder(boolean saveListsInSortedOrder) {
		this.saveListsInSortedOrder = saveListsInSortedOrder;
	}

	/**
	 * Set the name used for a new file before it has a file name.  This name will be used
	 * in dialogs until the file has a file name.
	 * 
	 * @param newFileDescription		file description text
	 */
	public void setNewFileDescription(String newFileDescription) {
		this.newFileDescription = newFileDescription;
	}
	
	@SuppressWarnings("unchecked")
	private T getSavableObject(FileState<T> fileState) {
		if (saveListsInSortedOrder 
				&& fileState.object instanceof List
				&& ((List<?>) fileState.object).size() > 0
				&& ((List<?>) fileState.object).get(0) instanceof Comparable) {
			try {
				// having unchecked operations here seems to be unavoidable
				log.debug("Preparing list in sorted order...");
				List objectCopy = (List) fileState.object.getClass().newInstance();
				objectCopy.addAll((List) fileState.object);
				Collections.sort(objectCopy);
				return (T) objectCopy;
			} catch (Exception e) {
				log.error("Unable to copy list; list will remain in it's existing order", e);
			}
		} 
		return fileState.object;
	}
	
	/**
	 * Return file name less path for the given file.
	 * 
	 * @param filepath		long file path (absolute path) to get short filename for
	 * 
	 * @return				file name without path
	 */
	private static String getShortFilename(String filepath) {
		int i = filepath.lastIndexOf(File.separator);
		return (i >= 0)? filepath.substring(i + File.separator.length()) : filepath;
		
	}
	
	/**
	 * Return file name less path for the given file.
	 * 
	 * @param file			file to get short filename for
	 * 
	 * @return				file name without path
	 */
	private static String getShortFilename(File file) {
		return getShortFilename(file.getAbsolutePath());
	}
	
	/**
	 * Set the frequency in milliseconds of the autosave feature.  
	 * 
	 * @param autosaveInterval	frequency (in milliseconds) of auto save
	 */
	public void setAutosaveInterval(long autosaveInterval) {
		this.autosaveInterval = autosaveInterval;
		if (autoSaver != null) {
			autoSaver.setInterval(autosaveInterval);
		}
	}
	
	/**
	 * Turn the autosave feature on or off.
	 * 
	 * @param autosave			true == on, false == off
	 */
	public void setAutosave(boolean autosave) {
		if (autosave && autoSaver == null) {
			autoSaver = new AutoSaver(this, this.autosaveInterval);
			autoSaver.start();
		} else if (!autosave && autoSaver != null) {
			autoSaver.stopRun();
			autoSaver = null;
		}
	}
	
	/**
	 * Get the recently loaded files manager, if there is one.  
	 * 
	 * @return					recently loaded files manager
	 */
	public RecentlyLoadedFilesManager getRecentlyLoadedFilesManager() {
		return recentlyLoadedFilesManager;
	}

	/**
	 * Set the recently loaded files manager.  Once set, the file manager will automatically
	 * notify the recently loaded files manager whenever a file is loaded or a new file is saved.
	 * It will NOT attempt to handle RecentlyLoadedActionEvents.
	 * 
	 * @param recentlyLoadedFilesManager	the recently loaded files manager to use
	 */
	public void setRecentlyLoadedFilesManager(RecentlyLoadedFilesManager recentlyLoadedFilesManager) {
		this.recentlyLoadedFilesManager = recentlyLoadedFilesManager;
	}

	/**
	 * Return a count of the number of currently open files.
	 * 
	 * @return number of open files
	 */
	public int getOpenFileCount() {
		return fileStates.size();
	}
	
	/**
	 * Set which open file is considered to be the active open file using the file's key value.
	 * 
	 * @param key
	 */
	public void setActiveFile(String key) {
		if (key == null || fileStates.containsKey(key)) {
			activeFileStateKey = key;
			if (afcListeners != null) {
				for (ActiveFileChangeListener listener : afcListeners) {
					listener.activeFileChanged(activeFileStateKey);
				}
			}
		}
	}
	
	/**
	 * Get the object associated with the active open file.
	 * 
	 * @return		object associated with the currently active file.
	 */
	public T getObject() {
		if (activeFileStateKey == null) {
			return null;
		} else {
			return getObject(activeFileStateKey);
		}
	}
	
	/**
	 * Get the object associated with the file of given key.
	 * 
	 * @param key	key associated with file
	 * 
	 * @return		object from file with given key
	 */
	public T getObject(String key) {
		if (!fileStates.containsKey(key)) {
			throw new IllegalArgumentException("File key \"" + key + "\" is invalid.");
		}
		return fileStates.get(key).object;
	}
	
	/**
	 * Get the filename associated with the active open file.
	 * 
	 * @return		file name of currently active file.
	 */
	public String getFileName() {
		if (activeFileStateKey == null) {
			return null;
		} else {
			return getFileName(activeFileStateKey);
		}
	}
	
	/**
	 * Get the filename associated with the file of given key.
	 * 
	 * @param key	key of file
	 * 
	 * @return		file name of file with given key
	 */
	public String getFileName(String key) {
		if (!fileStates.containsKey(key)) {
			throw new IllegalArgumentException("File key \"" + key + "\" is invalid.");
		}
		return fileStates.get(key).fileName;
	}

	/**
	 * Attempt to restore any autosaved files that were previously lost due to an abnormal
	 * program termination.  This call should be performed on program startup for any program
	 * utilizing the autosave feature; if this method returns true, then program
	 * should then iterate through the keys and reconstruct it's state through calls
	 * to getObject and getFileName.  If files are restored, the active file is set to the first
	 * restored file; the program should manually set the active file if necessary.
	 * 
	 * @return		whether or not any files were restored.
	 */
	@SuppressWarnings("unchecked")
	public boolean executeRestore() throws IOException {
		if (fileStates.size() > 0) {
			log.warn("executeRestore() cannot restore files when other files are already open.");
			return false;
		}
		if (!fileStateFile.exists()) {
			return false;
		}
		List<String> entriesToRemove = new ArrayList<String>();
		fileStates = (Map<String,FileState<T>>) loadObject(fileStateFile, null);
		for (Map.Entry<String,FileState<T>> entry : fileStates.entrySet()) {
			FileState fileState = entry.getValue();
			File fileToLoad = new File(fileState.fileName);
			if (fileState.tempFileName != null) {
				File tempFile = new File(fileState.tempFileName);
				if (tempFile.exists() && (!fileToLoad.exists() || tempFile.lastModified() > fileToLoad.lastModified())) {
					fileToLoad = tempFile;
				}
			}
			fileState.object = loadObject(fileToLoad, getCheckedBaseClass());
			if (fileState.object == null) {
				log.error("Unable to restore copy of file " + fileToLoad.getAbsolutePath() + " (object could not be loaded)");
				entriesToRemove.add(entry.getKey());
			} else { 
				try {
					checkClass(fileState.object, fileToLoad);
					if (activeFileStateKey == null) {
						setActiveFile(entry.getKey());
					}
				} catch (IOException ioe) {
					log.error("Unable to restore copy of file " + fileToLoad.getAbsolutePath() + " (object is of wrong class)");
					entriesToRemove.add(entry.getKey());
				}
			}
		}
		for (String key : entriesToRemove) {
			fileStates.remove(key);
		}
		if (fileStates.size() > 0) {
			setJComponentsEnabled(true);
			return true;
		}
		return false;
	}
	
	private Class<?> getCheckedBaseClass() {
		return (this.collectionClazz == null)? this.clazz : this.collectionClazz;
	}
	
	/**
	 * Check that the object is of the expected class type.  Throws an IOException if class is not
	 * of the expected type.  Class checking is only performed if checked classes were previously 
	 * set by a call to one of the setClassChecked methods.
	 * 
	 * @param object			object to test
	 * 
	 * @return					object to use; may or may not be the same object
	 * 
	 * @throws IOException
	 */
	private Object checkClass(Object object, File file) throws IOException {
		Class<?> baseClazz = getCheckedBaseClass();
		if (baseClazz == null) {
			log.warn("Object loaded from file is not being class checked (use setClassCheck method to avoid this warning).");
			return object;
		} else if (baseClazz != object.getClass()) {
			throw new IOException("Loaded object class " + object.getClass().getName() + " is not of expected type " + baseClazz.getName());
		}
		if (this.collectionClazz != null && this.clazz != null) {
			if (!(object instanceof Collection)) {
				throw new IOException("Loaded object class is not a Collection type");
			}
			Collection<?> collection = (Collection<?>) object;
			Iterator<?> iter = collection.iterator();
			while (iter.hasNext()) {
				Object memberObject = iter.next();
				if (!this.clazz.getName().equals(memberObject.getClass().getName())) {
					throw new IOException("Loaded collection member class (" + memberObject.getClass().getName() + ") is not of expected type (" + this.clazz.getName() + ")");
				}						
			}
		} 
		return object;
	}
	
	/**
	 * Return next auto-generated key.
	 * 
	 * @return		next auto-generated key
	 */
	private synchronized String getNextKey() {
		return "file" + (++keyCount);
	}
	
	public JMenuItem buildMenuItemNew() {
		JMenuItem item = new JMenuItem("New...");
		return item;
	}
	
	public JMenuItem buildMenuItemOpen() {
		JMenuItem menuItem = new JMenuItem("Open...");
		menuItem.setMnemonic(KeyEvent.VK_O);
		return menuItem;
	}
	
	public JMenuItem buildMenuItemSave() {
		JMenuItem menuItem = new JMenuItem("Save");
		menuItem.setMnemonic(KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		activateOnFileOpen(menuItem);
		return menuItem;
	}
	
	public JMenuItem buildMenuItemSaveAs() {
		JMenuItem menuItem = new JMenuItem("Save As...");
		activateOnFileOpen(menuItem);
		return menuItem;
	}
	
	public JMenuItem buildMenuItemClose() {
		JMenuItem menuItem = new JMenuItem("Close");
		activateOnFileOpen(menuItem);
		return menuItem;
	}
	
	/**
	 * Create a new file using the given object.
	 * 
	 * @param object		object associated with new file
	 * 
	 * @return				auto-generated key for new file
	 * 
	 * @throws IOException
	 */
	public String executeNew(T object) throws IOException {
		String key = getNextKey();
		if (executeNew(key, object)) {
			return key;
		} else {
			return null;
		}
	}
	
	/**
	 * Create a new file using the given object and key.  Key value must be unique.  This method
	 * provides the main program the ability to use it's own key values rather than auto-generated keys.
	 * 
	 * @param key			key to associate with new file
	 * @param object		object associated with new file
	 * 
	 * @return				whether or not new file was set up successfully
	 * 
	 * @throws IOException
	 */
	public boolean executeNew(String key, T object) throws IOException {
		if (fileStates.containsKey(key)) {
			throw new IllegalArgumentException("An object with key \"" + key + "\" already exists.");
		}
		if (!multiple && fileStates.size() == 1) {
			for (String priorKey : fileStates.keySet()) {
				if (!executeClose(priorKey)) {
					return false;
				}
			}
		}
		FileState<T> fileState = new FileState<T>();
		fileState.object = object;
		synchronized(fileStates) {
			fileStates.put(key, fileState);
		}
		setActiveFile(key);
		if (!managedJComponentsEnabled) {
			setJComponentsEnabled(true);
		}
		fireAfterOpen(key);
		return true;
	}
	
	/**
	 * Open a file chosen by user prompt.
	 * 
	 * @return				key of opened file (null if no file was opened)
	 * @throws IOException
	 */
	public String executeOpen() throws IOException {
		int result = fileChooser.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			return executeOpen(fileChooser.getSelectedFile());
		}
		return null;
	}
	
	public String executeOpen(String fileName) throws IOException {
		return executeOpen(new File(fileName));
	}
	
	public boolean executeOpen(String key, String fileName) throws IOException {
		return executeOpen(key, new File(fileName));
	}
	
	/**
	 * Open the given file.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public String executeOpen(File file) throws IOException {
		String key = getNextKey();
		if (executeOpen(key, file)) {
			return key;
		}
		return null;
	}
	
	/**
	 * Open the given file using the given key.  Key value must be unique.  This method provides
	 * the program the option of using it's own key values rather than using auto-generated keys.
	 * 
	 * @param key
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")  // checking will be optionally performed by a call to checkClass
	public boolean executeOpen(String key, File file) throws IOException {
		if (fileStates.containsKey(key)) {
			throw new IllegalArgumentException("An object with key \"" + key + "\" already exists.");
		}
		if (file.exists()) {
			if (!multiple && fileStates.size() == 1) {
				for (String priorKey : fileStates.keySet()) {
					if (!executeClose(priorKey)) {
						return false;
					}
				}				
			}
			FileState<T> fileState = new FileState<T>();
			fileState.fileName = file.getAbsolutePath();
			Object object = loadObject(file, null);
			try {
				object = checkClass(object, file);	// check class 
			} catch (IOException ioe) {
				if (this.importer != null && this.importer.getFromObjectClasses().contains(object.getClass())) {
					int result = JOptionPane.showConfirmDialog(parent, 
							this.importer.getImportConfirmationMessage(object),
							"Continue Load?",
							JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.NO_OPTION) {
						return false;
					}
					object = this.importer.importObject(object);
					object = checkClass(object, file);
				} else {
					throw ioe;
				}
			}
			fileState.object = (T) object;
			synchronized(fileStates) {
				fileStates.put(key, fileState);
			}
			setActiveFile(key);
			if (!managedJComponentsEnabled) {
				setJComponentsEnabled(true);
			}
			if (recentlyLoadedFilesManager != null) {
				recentlyLoadedFilesManager.fileLoadedOrSaved(file);
			}
			fireAfterOpen(key);
			return true;
		}
		return false;
	}
	
	/**
	 * Save the currently active file.
	 * 
	 * @return				whether or not file was saved successfully
	 * 
	 * @throws IOException
	 */
	public boolean executeSave() throws IOException {
		return (activeFileStateKey == null)? false : executeSave(activeFileStateKey);
	}
	
	/**
	 * Save the file of given key.
	 * 
	 * @param key			key of file to save
	 * 
	 * @return				whether or not file was saved successfully
	 * 
	 * @throws IOException
	 */
	public boolean executeSave(String key) throws IOException {
		if (!fileStates.containsKey(key)) {
			throw new IllegalArgumentException("File key \"" + key + "\" is invalid.");
		}
		FileState<T> fileState = fileStates.get(key);
		if (fileState.fileName == null) {
			return executeSaveAs(key);
		} else {
			fireBeforeSaveOrClose(fileState.object);
			synchronized(fileState) {
				saveObject(new File(fileState.fileName), getSavableObject(fileState));
			}
			return true;
		}
	}
	
	/**
	 * Save the given Object, prompting for file name/location.  This method provides the
	 * convenience of the FileManager save as feature for files that are not under the
	 * control of the FileManager.
	 * 
	 * @param parent			parent frame
	 * @param object			object to save to file
	 * @param dialogTitle		title for file chooser dialog window
	 * 
	 * @return					whether or not file was saved
	 * 
	 * @throws IOException
	 */
	public static boolean executeSaveAs(JFrame parent, Object object, String dialogTitle) throws IOException {
		JFileChooser jFileChooser = new JFileChooser();
		if (dialogTitle != null) {
			jFileChooser.setDialogTitle(dialogTitle);
		}
		int result = jFileChooser.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = jFileChooser.getSelectedFile();
			if (file.exists()) {
				result = JOptionPane.showConfirmDialog(parent, "A file with name " + getShortFilename(file) + " already exists.\nOverwrite?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.NO_OPTION) {
					return FileManager.executeSaveAs(parent, object, dialogTitle);
				}
			}
			if (object instanceof String) {
				saveString(file, object);
			} else {
				saveObject(file, object);
			}
			return true;
		}
		return false;
	}

	/**
	 * Save the given Object, prompting for file name/location.  This method provides the
	 * convenience of the FileManager save as feature for files that are not under the
	 * control of the FileManager.
	 * 
	 * @param parent			parent frame
	 * @param object			object to save to file
	 * 
	 * @return					whether or not file was saved
	 * 
	 * @throws IOException
	 */
	public static boolean executeSaveAs(JFrame parent, Object object) throws IOException {
		return executeSaveAs(parent, object, null);
	}
	
	/**
	 * Save the currently active file, prompting for file name/location.
	 * 
	 * @return					whether or not file was saved
	 * 
	 * @throws IOException
	 */
	public boolean executeSaveAs() throws IOException {
		return (activeFileStateKey == null)? false : executeSaveAs(activeFileStateKey);
	}
	
	/**
	 * Save the file with given key, prompting for file name/location.
	 * 
	 * @param key				key of file to save
	 * 
	 * @return					whether or not file was saved
	 * 
	 * @throws IOException
	 */
	public boolean executeSaveAs(String key) throws IOException {
		if (!fileStates.containsKey(key)) {
			throw new IllegalArgumentException("File key \"" + key + "\" is invalid.");
		}
		FileState<T> fileState = fileStates.get(key);
		int result = fileChooser.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (defaultExtension != null && file.getName().indexOf(".") < 0) {
				file = new File(file.getAbsolutePath() + "." + defaultExtension);
			}
			if (file.exists()) {
				result = JOptionPane.showConfirmDialog(parent, "A file with name " + getShortFilename(file) + " already exists.\nOverwrite?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.NO_OPTION) {
					return executeSaveAs(key);
				}
			}
			synchronized(fileState) {
				String oldFileName = fileState.fileName;
				String newFileName = file.getAbsolutePath();
				fireBeforeSaveOrClose(fileState.object);
				saveObject(file, getSavableObject(fileState));
				fileState.fileName = newFileName;
				if (!newFileName.equals(oldFileName)) {
					fireFilePathChange(newFileName);
				}
				if (recentlyLoadedFilesManager != null) {
					recentlyLoadedFilesManager.fileLoadedOrSaved(file);
				}
			}
			return true;
		}
		return false;
	}
	
	private void fireAfterOpen(String key) {
		if (this.fmListeners != null) {
			for (FileManagerListener<T> listener : this.fmListeners) {
				listener.afterOpen(key);
			}
		}
	}
	
	private void fireFilePathChange(String newAbsolutePath) {
		if (this.fmListeners != null) {
			for (FileManagerListener<T> listener : this.fmListeners) {
				listener.filePathChange(newAbsolutePath);
			}
		}
	}
	
	private void fireBeforeSaveOrClose(T object) {
		if (this.fmListeners != null && this.notifyFileManangerListeners) {
			for (FileManagerListener<T> listener : this.fmListeners) {
				listener.beforeSaveOrClose(object);
			}
		}
	}
	
	private void fireAfterClose() {
		if (this.fmListeners != null) {
			for (FileManagerListener<T> listener : this.fmListeners) {
				listener.afterClose();
			}
		}
	}
	
	/**
	 * Close the file with given key.
	 * 
	 * @param key			key of file to close
	 * 
	 * @return				whether or not file was closed (close can be canceled by user if AppCloseManager is being used)
	 * 
	 * @throws IOException
	 */
	public boolean executeClose(String key) throws IOException {
		if (!fileStates.containsKey(key)) {
			throw new IllegalArgumentException("No file for the given key \"" + key + "\" exists.");
		}
		FileState<T> fileState = fileStates.get(key);
		if (fileState.fileName == null) {
			//prompt for save-as of new file
			int choices = (this.parent instanceof ApplicationFrame)? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION;
			int result = JOptionPane.showConfirmDialog(parent, "Save " + this.newFileDescription + " before closing?", "Confirm Close", choices);
			if (result == JOptionPane.YES_OPTION) {
				executeSaveAs(key);
			} else if (result == JOptionPane.CANCEL_OPTION) {
				log.debug("File close cancelled by user for new file.");
				return false;
			}
		} else {
			File file = new File(fileState.fileName);
			try {
				fireBeforeSaveOrClose(fileState.object);
				File tempFile = File.createTempFile("temp", ".tmp");
				tempFile.deleteOnExit();
				saveObject(tempFile, getSavableObject(fileState));
				if (!file.exists() || !FileUtil.bitwiseEquals(file, tempFile, buff1, buff2)) {
					// prompt for save of file
					int choices = (this.parent instanceof ApplicationFrame)? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION;
					int result = JOptionPane.showConfirmDialog(parent, "Save " + getShortFilename(file) + " before closing?", "Confirm Close", choices);
					if (result == JOptionPane.YES_OPTION) {
						this.notifyFileManangerListeners = false;
						executeSave(key);
						this.notifyFileManangerListeners = true;
					} else if (result == JOptionPane.CANCEL_OPTION) {
						log.debug("File close cancelled by user for " + file.getAbsolutePath());
						return false;
					}
				}
			} catch (IOException ioe) {
				log.error("Unable to close file " + file.getAbsolutePath());
				throw ioe;
			}
		}
		if (key.equals(activeFileStateKey)) {
			setActiveFile(null);		//TODO: would it be better to pick next available key if multiple files open?
		}
		synchronized(fileStates) {
			fileStates.remove(key);
		}
		if (fileStates.size() == 0) {
			setJComponentsEnabled(false);
		}
		fireAfterClose();
		return true;
	}
	
	/**
	 * Close the currently active file.  The file key is returned in case the calling code
	 * is uncertain of which file is the currently active file; null is returned if no file 
	 * is closed.
	 * 
	 * @return				key of file closed
	 * 
	 * @throws IOException
	 */
	public String executeClose() throws IOException {
		if (activeFileStateKey == null) {
			return null;
		}
		String key = activeFileStateKey;
		if (executeClose(key)) {
			return key;
		}
		return null;
	}

	/**
	 * Load an Object from the given file.  If no object class is provided, the object 
	 * class is not checked.
	 *  
	 * @param file			the file
	 * @param objectClass	class of object to be loaded (if null, object is not class checked)
	 * 
	 * @return				object stored in file
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T loadObject(File file, Class<T> objectClass) throws IOException {
		Object object = null;
		ObjectInput oi = null;
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			oi = new ObjectInputStream(is);
			object = oi.readObject();
			if (objectClass != null && object.getClass() != objectClass) {
				throw new IOException("Object of type " + object.getClass().getName() + " does not match expected type " + objectClass.getName());
			}
		} catch (ClassNotFoundException cnfe) {
			throw new IOException("ClassNotFoundException when attempting to read object from file.", cnfe);
		} finally {
			if (oi != null) {
				try {
					oi.close();
				} catch (IOException ioe) { }
			}
		}
		return (T) object;
	}

	/**
	 * Load an Object from the given file.  If class of object in file is not of expected
	 * object class, an attempt will be made to import the object using the provided 
	 * object importer.
	 *  
	 * @param file			the file
	 * @param objectClass	class of object to be loaded
	 * @param objectImporter object importer
	 * 
	 * @return				object stored in file
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T loadObject(File file, Class<T> objectClass, Importer<T> objectImporter) throws IOException {
		T typedObject = null;
		Object object = loadObject(file, null);
		if (object.getClass() == objectClass) {
			typedObject = (T) object;
		} else if (objectImporter.getFromObjectClasses().contains(object.getClass())) {
			typedObject = objectImporter.importObject(object);
		} else {
			throw new IOException("Object of type " + object.getClass().getName() + " does not match any of the allowed types.");
		}
		return typedObject;
	}
	
	/**
	 * Load an Object from the file of given name.
	 * 
	 * @param fileName			name of file
	 * @param objectClass		class of object to be loaded
	 * 
	 * @return					object stored in file
	 * 
	 * @throws IOException
	 */
	public static <T> T loadObject(String fileName, Class<T> objectClass) throws IOException {
		return loadObject(new File(fileName), objectClass);
	}
	
	/**
	 * Load the contents of given file as a String.  This will read the entire file as a single
	 * String and should be used only for text files of reasonably small size.
	 * 
	 * @param file		file to read
	 * @return			contents of file as a String
	 * 
	 * @throws IOException
	 */
	public static String loadString(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				sb.append(line);
				line = reader.readLine();
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) { }
			}
		}
		return sb.toString();
	}
	
	/**
	 * Save an Object to the given file.  Object may be XCSerializable.  
	 * 
	 * @param file
	 * @param object
	 * @throws IOException
	 */
	public static void saveObject(File file, Object object) throws IOException {
		ObjectOutput oo = null;
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
			oo = new ObjectOutputStream(os);
			oo.writeObject(object);
		} finally {
			if (oo != null) {
				try {
					oo.close();
				} catch (IOException ioe) { }
			}
		}
	}
	
	/**
	 * Save an Object's String value to the given file.
	 * 
	 * @param file
	 * @param object
	 * @throws IOException
	 */
	public static void saveString(File file, Object object) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(object.toString());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ioe) { }
			}
		}
	}
	
	/**
	 * auto save any open files to their temporary file names.
	 */
	public void autoSave() {
		for (Map.Entry<String,FileState<T>> entry : fileStates.entrySet()) {
			FileState<T> fileState = entry.getValue();
			synchronized(fileState) {
				if (fileState.tempFileName == null) {
					try {
						File tempFile = File.createTempFile("temp", ".tmp");
						saveObject(tempFile, getSavableObject(fileState));
						fileState.tempFileName = tempFile.getAbsolutePath();
					} catch (IOException ioe) {
						log.warn("Unable to autosave file " + fileState.fileName);
					}
				} else {
					File tempFile = new File(fileState.tempFileName);
					try {
						saveObject(tempFile, getSavableObject(fileState));
					} catch (IOException ioe) {
						log.error("Unable to save file state with key " + entry.getKey() + ", file " + fileState.fileName, ioe);
					}
				}
			}
		}
		synchronized(fileStates) {
			try {
				saveObject(fileStateFile, fileStates);
			} catch (IOException ioe) {
				log.error("Unable to save file states", ioe);
			}
		}
	}
	


	/**
	 * Stop the autosaver if it's active and close all open files.  Programs using the File Manager
	 * do not need to explicitly call this method for any reason, but may choose to on program exit
	 * if the program exit strategy does not always trigger a windowClosing event and either does
	 * not alternately trigger a windowClosed event or the windowClosed event is considered 
	 * undesirable (because the parent window will already be gone from the screen if caught by 
	 * the window closed event).  In such cases, the program can manually call the FileManager
	 * closeDown() method to ensure that save file prompts occur at the desired time on program
	 * shutdown.
	 * 
	 * @return			whether or not close down was completed (value of false may indicate user canceled close down on a file save op)
	 */
	public boolean closeDown() {
		log.debug("FileManager closing down...");
		if (autoSaver != null) {
			autoSaver.stopRun();
		}
		if (fileStates.size() > 0) {
			autoSave();
			List<String> keys = new ArrayList<String>();
			keys.addAll(fileStates.keySet());
			for (String key : keys) {
				try {
					// Check if close was canceled; if so, immediately return false
					if (!executeClose(key)) {
						//TODO: Should also restart auto saver here?
						return false;
					}
				} catch (IOException ioe) {
					log.error("Unable to close file with key " + key);
					//TODO: Should we prompt user here for what to do?  We could optionally return false if AppCloseManager is being used
				}
			}
		}	
		if (fileStateFile.exists()) {
			fileStateFile.delete();
		}
		return true;
	}
	
	/**
	 * Set all managed JComponents to the given enabled status.
	 * 
	 * @param enabled	whether to enable or disable managed JComponents
	 */
	private void setJComponentsEnabled(boolean enabled) {
		if (managedJComponents != null) {
			for (JComponent jcomponent : managedJComponents) {
				jcomponent.setEnabled(enabled);
			}
		}
		managedJComponentsEnabled = enabled;
	}
	
	/**
	 * Add a JComponent to be managed by the FileManager; component will be enabled when files
	 * are open and disabled when no files are open.  This can be useful for menu items, buttons,
	 * or any other component that is only valid when files are open.
	 * 
	 * @param jcomponent		menu item to be managed
	 */
	public void activateOnFileOpen(JComponent jcomponent) {
		if (managedJComponents == null) {
			managedJComponents = new ArrayList<JComponent>();
		}
		managedJComponents.add(jcomponent);
		jcomponent.setEnabled(managedJComponentsEnabled);
	}

	public boolean closeAction(WindowEvent event) {
		return closeDown();
	}

	@Override
	public void windowClosed(WindowEvent event) {
		closeDown();
	}

	@Override
	public void windowClosing(WindowEvent event) {
		closeDown();
	}
}
