package org.xandercat.swing.log;

import java.io.Serializable;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.xandercat.swing.component.MessageScrollPane;
import org.xandercat.swing.util.ResourceManager;

/**
 * A log4j appender for appending log messages to a MessageScrollPane.  As a shortcut to utilizing
 * a complex construct, the ResourceManager is used to store the LogFrame.
 * 
 * @author Scott Arnold
 */
@Plugin(name="WindowAppender", category=Core.CATEGORY_NAME, elementType=Appender.ELEMENT_TYPE)
public class MessageScrollPaneAppender extends AbstractAppender {

	protected MessageScrollPaneAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
		super(name, filter, layout, true, null);
		this.messageScrollPane = new MessageScrollPane();
		LogFrame logFrame = new LogFrame(this.messageScrollPane);
		ResourceManager.getInstance().register(logFrame);
	}

	@PluginFactory
	public static MessageScrollPaneAppender createAppender(
			@PluginAttribute("name") String name,
			@PluginElement("Filter") Filter filter,
			@PluginElement("Layout") Layout<? extends Serializable> layout) {
		return new MessageScrollPaneAppender(name, filter, layout);
	}
	
	private MessageScrollPane messageScrollPane;

	@Override
	public void append(LogEvent event) {
		final String message = getLayout().toSerializable(event).toString();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MessageScrollPaneAppender.this.messageScrollPane.addMessage(message);
			}
		});		
	}
}
