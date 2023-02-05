package org.xandercat.swing.util;

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
	
	public static void configureLoggingToConsole(Level level) {
		ConfigurationBuilder<?> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		LayoutComponentBuilder layout = builder
				.newLayout("PatternLayout")
				.addAttribute("pattern", "%d %-5p %c{1} - %m%n");
		AppenderComponentBuilder consoleAppender = builder
				.newAppender("console", "Console")
				.add(layout);
		builder.add(consoleAppender);
		RootLoggerComponentBuilder rootLogger = builder
				.newRootLogger(level)
				.add(builder.newAppenderRef("console"));
		builder.add(rootLogger);
		Configurator.initialize(builder.build());
	}
}
