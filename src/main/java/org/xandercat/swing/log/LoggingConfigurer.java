package org.xandercat.swing.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.util.ArgumentProcessor;
import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.util.ResourceManager;

/**
 * Originally, my swing apps had logging that was reconfigurable on the fly, so configuration could
 * be changed with an application flag.  This isn't possible with Log4J2 using static Loggers, as
 * the Loggers are already created prior to the programmatic configuration and there doesn't seem
 * to be any way to update them after the fact.  
 * 
 * To continue support for logging flags, this class will take the desired configuration, and instead of changing
 * the configuration on the fly, will instead replace the log4j2 configuration file at the root
 * of the application.  This allows configuration to still be updated via application flag, but 
 * now requires application restart to pick up the change.
 * 
 * If logging to window, the window class is LogFrame and it can be retrieved via the ResourceManager.
 * 
 * @author Scott C Arnold
 */
public class LoggingConfigurer {
	
	private static final Logger log = LogManager.getLogger(LoggingConfigurer.class);
	private static String CONSOLE_TEMPLATE_XML = "config/console.xml";
	private static String FILE_TEMPLATE_XML = "config/file.xml";
	private static String WINDOW_TEMPLATE_XML = "config/window.xml";
	private static File LOG4J2_CONFIG_FILE = new File("Log4j2.xml");
	
	public static enum Target { 
		OFF("off"), CONSOLE("console"), FILE("file"), WINDOW("window");
		private String name;
		private Target(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public static Target byName(String name) {
			for (Target target : Target.values()) {
				if (target.name.equalsIgnoreCase(name)) {
					return target;
				}
			}
			return null;
		}
	}
	
	private static String replaceTokens(String configXml, Map<String, String> replacementValues) {
		Pattern pattern = Pattern.compile("\\[(.+?)\\]");
		Matcher matcher = pattern.matcher(configXml);
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (matcher.find()) {
			String replacement = replacementValues.get(matcher.group(1));
			sb.append(configXml.substring(i, matcher.start()));
			if (replacement == null) {
				sb.append(matcher.group(0));
			} else {
				sb.append(replacement);
			}
			i = matcher.end();
		}
		sb.append(configXml.substring(i, configXml.length()));
		return sb.toString();
	}
	
	private static Map<String, String> replacementValues(Level level, String fileName) {
		Map<String, String> replacementValues = new HashMap<>();
		if (level != null) {
			replacementValues.put("level", level.name());
		}
		if (fileName != null) {
			replacementValues.put("fileName", fileName);
		}
		return replacementValues;
	}
	
	private static String readConfigXml(String configXmlPath) {
		InputStream is = LoggingConfigurer.class.getResourceAsStream(configXmlPath);
		StringBuilder configXml = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line = reader.readLine();
			while (line != null) {
				configXml.append(line).append("\n");
				line = reader.readLine();
			}
		} catch (IOException ioe) {
			return null;
		}
		return configXml.toString();
	}
	
	private static boolean writeLog4j2Configuration(String configXml) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG4J2_CONFIG_FILE))) {
			writer.write(configXml);
		} catch (IOException ioe) {
			log.error("Unable to write to Log4j2 configuration file.  Configuration will not be updated.", ioe);
			return false;
		}
		return true;
	}
	
	private static boolean configLogging(String templateXml, Level level, String fileName) {
		String configXmlTemplate = readConfigXml(templateXml);
		if (configXmlTemplate == null) {
			log.error("Unable to read template xml.  Logging configuration will not be updated.");
			return false;
		}
		String configXml = replaceTokens(configXmlTemplate, replacementValues(level, fileName));
		return writeLog4j2Configuration(configXml);		
	}
	
	public static boolean configureLoggingOff() {
		return configureLoggingToConsole(Level.OFF);
	}
	
	public static boolean configureLoggingToConsole() {
		return configureLoggingToConsole(Level.INFO);
	}
	
	public static boolean configureLoggingToConsole(Level level) {
		return configLogging(CONSOLE_TEMPLATE_XML, level, null);
	}
	
	public static boolean configureLoggingToFile(Level level, String fileName) {
		return configLogging(FILE_TEMPLATE_XML, level, fileName);
	}
	
	public static boolean configureLoggingToWindow(Level level) {
		return configLogging(WINDOW_TEMPLATE_XML, level, null);
	}
	
	public static boolean configureLogging(Target target, Level level, String fileName) {
		switch (target) {
		case OFF:
			return configureLoggingOff();
		case CONSOLE:
			return configureLoggingToConsole();
		case FILE:
			return configureLoggingToFile(level, fileName);
		case WINDOW:
			return configureLoggingToWindow(level);
		}
		return false;
	}
	
	/**
	 * Add a switch/value pair for logging to the given argument processor using the given logging switch key.
	 * 
	 * @param argumentProcessor			the argument processor to add switch/value pair to
	 * @param switchKey					the switch used to specify logging
	 */
	public static void addArgumentProcessorSwitchValuePair(ArgumentProcessor argumentProcessor, String switchKey) {
		String hint = Arrays.stream(Target.values())
				.map(target -> target.name)
				.collect(Collectors.joining("|"))
				.replace(Target.FILE.name, Target.FILE.name + ":[fileName]");
		argumentProcessor.addValidSwitchValuePair(switchKey, hint, "sets logging target", null);
	}
	
	/**
	 * Configure logging based on the command-line switch specified.  Use this method in combination with 
	 * addArgumentProcessorSwitchValuePair.  If the switch is not present, logging is configured based on 
	 * the given default target, but only if a log4j2.xml file does not already exist.  
	 * 
	 * For file logging, the argument will accept values in the form "file:[filename]".  If filename is not
	 * specified, the defaultFile will be used.
	 * 
	 * @param argumentProcessor         argument processor to read switch value from
	 * @param switchKey                 switch
	 * @param level                     logging level for root logger
	 * @param defaultTarget             logging target when switch is not present
	 * @param defaultFile               default file to log to if switch is not present and defaultTarget is FILE; can be left null if defaultTarget is not FILE
	 */	
	public static boolean configureLogging(ArgumentProcessor argumentProcessor, String switchKey, Level level, Target defaultTarget, String defaultFileName) {
		Target target = defaultTarget;
		String fileName = defaultFileName;
		String switchValue = argumentProcessor.getValueForSwitch(switchKey);
		if (switchValue != null) {
			if (switchValue.toLowerCase().startsWith(Target.FILE.name + ":")) {
				String[] valueParts = switchValue.split(":");
				switchValue = valueParts[0];
				if (valueParts[1].trim().length() > 0) {
					fileName = valueParts[1];
				}				
			}
			target = Target.byName(switchValue);
		} else if (FileUtil.exists(LOG4J2_CONFIG_FILE)) {
			// no switch and config already exists, nothing needs to be done
			return false;
		}
		log.info("Reconfiguring logging to " + target + " at level " + level.toString());
		return configureLogging(target, level, fileName);
	}
	
	public static JFrame getLogFrame() {
		return ResourceManager.getInstance().getResource(LogFrame.class);
	}
}
