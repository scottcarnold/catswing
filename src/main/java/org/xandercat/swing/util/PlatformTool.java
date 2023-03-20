package org.xandercat.swing.util;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

/**
 * PlatformTool provides convenience methods for performing functions that are implemented in different
 * ways on different platforms.
 * 
 * @author Scott C Arnold
 */
public class PlatformTool {

	private static final Set<String> SYSTEM_FILENAMES = new HashSet<String>();
	private static boolean ignoreSystemFiles = false;
	
	static {
		SYSTEM_FILENAMES.add(".DS_Store");
	}
	
	public static enum MenuItemType {
		ABOUT, EXIT, PREFERENCES;
	};
	
	public static final FileFilter FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			if (ignoreSystemFiles) {
				return !PlatformTool.isSystemFile(pathname);
			}
			return true;
		}	
	};
	
	public static FilenameFilter FILENAME_FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			if (ignoreSystemFiles) {
				return !PlatformTool.isSystemFilename(name);
			}
			return true;
		}
	};
	
	private static final String osName = System.getProperty("os.name");
	
	/**
	 * When set to true, file handling classes throughout the CatSwing library will
	 * ignore "system files" such as the Mac ".DS_Store" file.  This can be 
	 * extended beyond CatSwing by using PlatformTool.FILE_FILTER and PlatformTool.FILENAME_FILTER.
	 * 
	 * @param ignoreSystemFiles whether or not to ignore system files
	 */
	public static void setIgnoreSystemFiles(boolean ignoreSystemFiles) {
		PlatformTool.ignoreSystemFiles = ignoreSystemFiles;
	}
	
	/**
	 * Return whether or not a file of given filename is considered to be a generic system file.
	 * 
	 * @param filename    filename to check
	 * 
	 * @return whether or not filename is considered to be a generic system file filename.
	 */
	public static boolean isSystemFilename(String filename) {
		if (filename == null) {
			return false;
		}
		int i = filename.lastIndexOf(File.separator);
		if (i >= 0) {
			filename = filename.substring(i+File.separator.length());
		}
		return SYSTEM_FILENAMES.contains(filename);
	}
	
	/**
	 * Return whether or not the given file is considered to be a generic system file.
	 * 
	 * @param file    file to check
	 * 
	 * @return whether or not file is considered to be a generic system file.
	 */
	public static boolean isSystemFile(File file) {
		if (file == null) {
			return false;
		}
		return SYSTEM_FILENAMES.contains(file.getName());
	}
	
	/**
	 * Return whether the platform is use is Apple Mac.
	 * 
	 * @return				whether the platform is Mac
	 */
	public static boolean isMac() {
		return (osName == null)? false : osName.contains("Mac");
	}
	
	/**
	 * Return whether the platform in use is Windows.
	 * 
	 * @return				whether the platform is Windows.
	 */
	public static boolean isWindows() {
		return (osName == null)? false : osName.startsWith("Windows");
	}
	
	/**
	 * Add OK and Cancel buttons to a panel.  OK and Cancel button will be added to the panel in
	 * the order that is standard for the platform in use.
	 * 
	 * @param panel				the panel to add the buttons to
	 * @param okButton			the OK button
	 * @param cancelButton		the Cancel button
	 */
	public static void addOkCancelButtons(JPanel panel, JButton okButton, JButton cancelButton) {
		if (isMac()) {
			panel.add(cancelButton);
			panel.add(okButton);
		} else {
			panel.add(okButton);
			panel.add(cancelButton);
		}
	}
	
	/**
	 * Attempt to launch a browser to the given URL.
	 * 
	 * Code courtesy of Bare Bones Browser Launch code from "http://www.centerkey.com/java/browser/".
	 * 
	 * @param url
	 * @throws Exception
	 * @deprecated for Java 6+, use Desktop.getDesktop().browse(URI)
	 */	
	public static void launchBrowser(URL url) throws Exception {
		launchBrowser(url.toString());
	}
	
	/**
	 * Attempt to launch a browser to the given URL.
	 * 
	 * Code courtesy of Bare Bones Browser Launch code from "http://www.centerkey.com/java/browser/".
	 * 
	 * @param url
	 * @throws Exception
	 * @deprecated for Java 6+, use Desktop.getDesktop().browse(URI)
	 */
	public static void launchBrowser(String url) throws Exception {
		if (isMac()) {
			Class<?> fileMgr = Class.forName("com.apple.eio.FileManager"); 
			Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class}); 
			openURL.invoke(null, new Object[] {url});			
		} else if (isWindows()) {
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
		} else {	// assume Unix or Linux
			String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" }; 
			String browser = null; 
			for (int count = 0; count < browsers.length && browser == null; count++) { 
				if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0) { 
					browser = browsers[count]; 
				}
			}
			if (browser == null) {
				throw new Exception("Could not find web browser"); 
			} else {
				Runtime.getRuntime().exec(new String[] {browser, url}); 
			}
		}
	}
	
	/**
	 * Connect to the given network share using the given local drive name with the given username and 
	 * password.
	 * 
	 * Example usage in Windows:
	 *   int exitValue = PlatformTool.connectToNetworkShare("h:", "\\\\Mgb-raid-pro\\public", "admin", "admin");
	 *   
	 * @param localDrive
	 * @param shareName
	 * @param username
	 * @param password
	 * @return
	 * @throws IOException
	 */
	public static int connectToNetworkShare(String localDrive, String shareName, String username, String password) throws IOException {
		if (isWindows()) {
			Process p = Runtime.getRuntime().exec(new String[] {"net", "use", localDrive, shareName, password, "/user:" + username});
			try {
				p.waitFor();
				return p.exitValue();
			} catch (InterruptedException ie) {
				return -1;
			}
		}
		return -2;
	}
	
	/**
	 * Set the icon to use for the application window and task bar button from the given resource.
	 * This only applies for Microsoft Windows.  Icon should ideally be 16x16.
	 *  
	 * @param jframe		the main application frame
	 * @param resource		
	 */
	public static void setWindowsIcon(JFrame jframe, Resource resource) {
		if (isWindows()) {
			ImageIcon icon = new ImageIcon(resource.getResource());
			jframe.setIconImage(icon.getImage());
		}
	}
	
	/**
	 * Set the apple look and feel to use the screen menu bar for any JMenuBar.  Calling this method will have
	 * no effect on platforms other than Mac (it is safe to call for all platforms).
	 */
	public static void useScreenMenuBarOnMac() {
		System.setProperty("apple.laf.useScreenMenuBar", "true");		
	}
	
	/**
	 * Set the name that is used in the application menu on a Mac; this needs to be done before any UI components
	 * are initialized (call it in the main method before starting the UI).  Calling this method will have no 
	 * effect on platforms other than Mac (it is safe to call for all platforms).
	 * 
	 * @param applicationName	the name of the application
	 */
	public static void setApplicationNameOnMac(String applicationName) {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationName);
	}
	
	/**
	 * Ensure that a WINDOW_CLOSING WindowEvent is fired on a Mac when quitting the application from
	 * the Mac application menu.  This will be overridden if you add a menu item of type EXIT.
	 * 
	 * @param window		the window to fire the event for
	 */
	public static void fireWindowClosingOnMacQuit(final Window window) {
		if (isMac()) {
			Application application = Application.getApplication();
			application.setQuitHandler(new QuitHandler() {
				@Override
				public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
					response.cancelQuit(); // let Java app take care of the quit
					window.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
				}
			});			
		}
	}
	
	/**
	 * Add menu item of given type to the application's menu.  If there is not a system specific
	 * menu the item should be added to, it is added to the systemInspecificMenu.
	 * 
	 * @param menuItem
	 * @param type
	 * @param systemInspecificMenu
	 */
	public static void addMenuItem(JMenuItem menuItem, MenuItemType type, JMenu systemInspecificMenu) {
		if (isMac()) {
			addMenuItemOnMac(menuItem, type);
		} else {
			systemInspecificMenu.add(menuItem);
		}
	}
	
	/**
	 * Add menu item of given type to the mac application menu.
	 * 
	 * @param menuItem
	 * @param type
	 */
	public static void addMenuItemOnMac(final JMenuItem menuItem, MenuItemType type) {
		Application application = Application.getApplication();
		switch (type) {
		case ABOUT:
			application.setAboutHandler(new AboutHandler() {
				@Override
				public void handleAbout(AboutEvent e) {
					menuItem.doClick();
				}
				
			});
			break;
		case EXIT:
			application.setQuitHandler(new QuitHandler() {
				@Override
				public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
					response.cancelQuit();  // let the menu item handle whether or not to quit
					menuItem.doClick();		
				}
				
			});
			break;
		case PREFERENCES:
			application.setPreferencesHandler(new PreferencesHandler() {
				@Override
				public void handlePreferences(PreferencesEvent e) {
					menuItem.doClick();
				}			
			});
			break;
		}
	}
}

