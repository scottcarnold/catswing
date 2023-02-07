package org.xandercat.swing.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;

public class LoggingUtil {

	public static void configureLoggingToConsole() {
		configureLoggingToConsole(Level.INFO);
	}
	
	public static ConfigurationBuilder<?> configureBuilderWithAppender(
			String name, String plugin, Map<String, String> attributes) {
		ConfigurationBuilder<?> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		LayoutComponentBuilder layout = builder
				.newLayout("PatternLayout")
				.addAttribute("pattern", "%d %-5p %c{1} - %m%n");
		AppenderComponentBuilder appender = builder
				.newAppender(name, plugin)
				.add(layout);
		if (attributes != null) {
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				appender.addAttribute(entry.getKey(), entry.getValue());
			}
		}
		return builder.add(appender);		
	}
	
	public static ConfigurationBuilder<?> configureBuilderWithAppender(
			String name, String plugin) {
		return configureBuilderWithAppender(name, plugin, null);
	}
	
	public static void configureLoggingToConsole(Level level) {
		ConfigurationBuilder<?> builder = configureBuilderWithAppender("console", "Console");
		RootLoggerComponentBuilder rootLogger = builder
				.newRootLogger(level)
				.add(builder.newAppenderRef("console"));
		builder.add(rootLogger);
		Configurator.initialize(builder.build());
	}
	
	public static void configureLoggingToFile(Level level, String path) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("fileName", path);
		ConfigurationBuilder<?> builder = 
				configureBuilderWithAppender("file", "File", attributes);
		RootLoggerComponentBuilder rootLogger = builder
				.newRootLogger(level)
				.add(builder.newAppenderRef("file"));
		builder.add(rootLogger);
		Configurator.initialize(builder.build());		
	}
}
