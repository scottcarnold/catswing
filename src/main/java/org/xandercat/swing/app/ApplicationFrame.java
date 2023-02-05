package org.xandercat.swing.app;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.SplashScreen;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.util.PlatformTool;

/**
 * ApplicationFrame can be extended to serve as the main JFrame of an application.  ApplicationFrame
 * provides a consistent means for handling application close down that meets the following criteria:
 * <ol>
 * <li>Provides consistency between using the window close button, using the Quit menu option on Mac, and using a custom action (such as an "Exit" menu item)</li>
 * <li>Provides a way for other "components" of the application to listen for application close (close listeners).</li>
 * <li>Provides a way for close listeners to optionally cancel application close.</li>
 * <li>Allows close listeners to fully complete even when launching dialogs.</li>
 * <li>Application ultimately exits using a call to System.exit(0).</li>  
 * </ol>
 * 
 * @author Scott C Arnold
 */
public class ApplicationFrame extends JFrame implements WindowListener {

	private static final long serialVersionUID = 2009090801L;
	private static final Logger log = LogManager.getLogger(ApplicationFrame.class);
			
	private List<CloseListener> closeListeners;
	private SplashScreen splashScreen;
	private Graphics2D splashGraphics2D;
	private Font splashFont = new Font("Arial", Font.BOLD, 14);
	
	public ApplicationFrame() throws HeadlessException {
		super();
		initialize();
	}

	public ApplicationFrame(GraphicsConfiguration graphicsConfiguration) {
		super(graphicsConfiguration);
		initialize();
	}

	public ApplicationFrame(String title, GraphicsConfiguration graphicsConfiguration) {
		super(title, graphicsConfiguration);
		initialize();
	}

	public ApplicationFrame(String title) throws HeadlessException {
		super(title);
		initialize();
	}
	
	private void initialize() {
		this.splashScreen = SplashScreen.getSplashScreen();
		log.info("Splash screen " + ((this.splashScreen == null)? "disabled." : "enabled."));
		if (this.splashScreen != null) {
			this.splashGraphics2D = this.splashScreen.createGraphics();
		}
		super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.closeListeners = new ArrayList<CloseListener>();
		addWindowListener(this);
		PlatformTool.fireWindowClosingOnMacQuit(this);
		//TODO: Consider whether or not to replace VersionHistory functionality; for now, it has been removed
		//Note that VersionHistory was also set to set the product name in the title (setTitle(versionHistory.getProductName())
	}
	
	@Override
	public void setVisible(boolean visible) {
		// close down the splash screen if it's up
		if (this.splashGraphics2D != null) {
			this.splashGraphics2D.dispose();
			this.splashGraphics2D = null;
		}
		if (this.splashScreen != null) {
			if (this.splashScreen.isVisible()) {
				this.splashScreen.close();
			}
			this.splashScreen = null;
		}
		super.setVisible(visible);
	}

	/**
	 * Sets the font to use for splash status text.
	 * 
	 * @param font			font to use on splash image for splash status text
	 */
	protected void setSplashFont(Font font) {
		this.splashFont = font;
	}
	
	/**
	 * Draws the given status string at the bottom of the splash image.  If there is 
	 * no active splash screen, calling this method has no effect.
	 * 
	 * @param status		status message
	 */
	protected void setSplashStatus(String status) {
		if (status != null && this.splashScreen != null && this.splashGraphics2D != null) {
			this.splashGraphics2D.setFont(this.splashFont);
			FontMetrics fm = this.splashGraphics2D.getFontMetrics();
			Dimension splashSize = this.splashScreen.getSize();
			// clear previous text
			this.splashGraphics2D.setComposite(AlphaComposite.Clear);
			this.splashGraphics2D.fillRect(5, splashSize.height - fm.getHeight() - 5, splashSize.width - 5, fm.getHeight() + 10);
			// apply new text
			this.splashGraphics2D.setPaintMode();		
			this.splashGraphics2D.setColor(Color.BLACK);
			this.splashGraphics2D.drawString(status, 5, splashSize.height - fm.getDescent() - 5);
			this.splashScreen.update();
		}
	}
	
	/**
	 * Setting the default close operation is not supported, as application close down logic is
	 * handled internally.  
	 */
	@Override
	public void setDefaultCloseOperation(int operation) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Add a CloseListener to the application that will listen for application close requests.
	 * 
	 * @param closeListener		the CloseListener
	 */
	public void addCloseListener(CloseListener closeListener) {
		this.closeListeners.add(closeListener);
	}
	
	/**
	 * Remove a CloseListener from the application.
	 * 
	 * @param closeListener		the CloseListener
	 */
	public void removeCloseListener(CloseListener closeListener) {
		if (this.closeListeners.indexOf(closeListener) < 0) {
			log.warn("CloseListener " + closeListener.getClass().getName() + " not found in list of listeners.");
		}
		this.closeListeners.remove(closeListener);
	}

	/**
	 * Close down the application.  This will fire a window closing WindowEvent much like clicking
	 * the window's close button.  This provides consistent behavior between using the window's 
	 * close button and explicitly closing the application through a call to this method.  The 
	 * ApplicationFrame will in turn catch the window closing event in order to execute the actions
	 * of any registered CloseListeners.  
	 */
	public void closeApplication() {
		WindowEvent windowClosing = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		getToolkit().getSystemEventQueue().postEvent(windowClosing);
	}
	
	public void windowActivated(WindowEvent event) {
	}

	public void windowClosed(WindowEvent event) {
	}

	public void windowClosing(WindowEvent event) {
		log.info("Preparing for application close...");
		for (CloseListener closeListener : this.closeListeners) {
			if (!closeListener.closeAction(event)) {
				log.info("Application close cancelled.");
				return;
			}
		}
		log.info("Application closed.");
		System.exit(0);		
	}

	public void windowDeactivated(WindowEvent event) {		
	}

	public void windowDeiconified(WindowEvent event) {		
	}

	public void windowIconified(WindowEvent event) {	
	}

	public void windowOpened(WindowEvent event) {
	}
}

