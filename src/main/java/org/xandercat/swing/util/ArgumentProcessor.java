package org.xandercat.swing.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ArgumentProcessor is for processing a program's command-line arguments (also called switches).  
 * If terminate-on-invalid-argument flag is set (which it is by default), any invalid command-line argument 
 * will cause the program to terminate and the listing of valid arguments will be output to System.out. Test
 * 
 * @author Scott C Arnold
 */
public class ArgumentProcessor {

	private static class SwitchDefinition {
		private boolean pair;
		private String valueHint;
		private String description;
		private String regex;
		private SwitchDefinition(String description) {
			this.pair = false;
			this.description = description;
		}
		private SwitchDefinition(String valueHint, String description, String regex) {
			this(description);
			this.pair = true;
			this.valueHint = valueHint;
			this.regex = regex;
		}
		public String toString() {
			return toString(20);
		}
		public String toString(int valueHintLength) {
			return ArgumentProcessor.padRight(valueHint, valueHintLength)
					+ " : " + description;
		}
	}
	
	private Map<String,SwitchDefinition> switchDefinitions = new HashMap<String,SwitchDefinition>();
	private Map<String,String> switches = new HashMap<String,String>();
	private String terminateHeading;
	private boolean terminateOnInvalidArgument = true;
	
	/**
	 * Construct a new argument processor.  By default, any invalid arguments will cause program to terminate.
	 */
	public ArgumentProcessor() {
	}
	
	/**
	 * Construct a new argument processor, explicitly setting whether or not to terminate program if an 
	 * invalid argument is encountered.
	 * 
	 * @param terminateOnInvalidArgument whether or not to terminate the program when there is an invalid argument
	 */
	public ArgumentProcessor(boolean terminateOnInvalidArgument) {
		this.terminateOnInvalidArgument = terminateOnInvalidArgument;
	}
	
	/**
	 * Add a valid stand-alone switch.
	 * 
	 * @param switchKey		the switch
	 * @param description	description of switch purpose
	 */
	public void addValidSwitch(String switchKey, String description) {
		switchDefinitions.put(switchKey, new SwitchDefinition(description));
	}
	
	/**
	 * Add a valid switch value pair.  This is for switches that have an associated value that 
	 * immediately follows the switch.
	 * 
	 * @param switchKey		the switch
	 * @param valueHint		indication of what switch value can be
	 * @param description	description of switch purpose
	 * @param regex			any regular expression the value should conform to (can be null if not needed)
	 */
	public void addValidSwitchValuePair(String switchKey, String valueHint, String description, String regex) {
		switchDefinitions.put(switchKey, new SwitchDefinition(valueHint, description, regex));
	}
	
	/**
	 * Set a heading to print on the first line of System.out when the program terminates due to an 
	 * invalid argument.  This can be something such as the program name and version.
	 * 
	 * @param heading		heading text
	 */
	public void setTerminationHeading(String heading) {
		this.terminateHeading = heading;
	}
	
	/**
	 * Get the map of loaded switch/value pairs.  It is recommended to use the getValueForSwitch
	 * methods rather than this method.
	 * 
	 * @return				map of switch/value pairs
	 */
	public Map<String,String> getSwitches() {
		return switches;
	}
	
	/**
	 * Get the value for the given switch.
	 * 
	 * @param switchKey		switch
	 * 
	 * @return				value for switch
	 */
	public String getValueForSwitch(String switchKey) {
		return switches.get(switchKey);
	}
	
	/**
	 * Returns whether or not the given switch was specified as an argument.
	 * 
	 * @param switchKey		the switch
	 * 
	 * @return				whether or not given switch was specified
	 */
	public boolean isSwitchPresent(String switchKey) {
		return switches.containsKey(switchKey);
	}
	
	/**
	 * Get the value for the given switch.  If switch was not specified, the given default
	 * value is returned.
	 * 
	 * @param switchKey		switch
	 * @param defaultValue	value to return if switch was not specified
	 * 
	 * @return				value for switch, or default value if switch was not specified
	 */
	public String getValueForSwitch(String switchKey, String defaultValue) {
		if (switches.containsKey(switchKey)) {
			return switches.get(switchKey);
		} else {
			return defaultValue;
		}
	}
	
	private static String padRight(String s, int len) {
		StringBuilder sb = new StringBuilder();
		if (s != null) {
			sb.append(s);
		}
		while (sb.length() < len) {
			sb.append(" ");
		}
		return sb.toString();
	}
	
	/**
	 * Process the command-line arguments.  Call this method after adding all valid switches.
	 * If the terminate-on-invalid-switch flag is set and an invalid argument is present in the
	 * list of arguments, program termination will occur within this method.
	 * 
	 * @param args			program command-line arguments
	 * 
	 * @return				whether or not all switches were valid
	 */
	public boolean process(String[] args) {
		boolean valid = true;
		for (int i=0; i<args.length; i++) {
			if (switchDefinitions.containsKey(args[i])) {
				SwitchDefinition def = switchDefinitions.get(args[i]);
				if (def.pair) {
					i++;
					if (i<args.length) {
						switches.put(args[i-1],args[i]);
						if (def.regex != null) {
							//TODO: test regex processing
							Pattern pattern = Pattern.compile(def.regex);
							Matcher matcher = pattern.matcher(args[i]);
							if (!matcher.matches()) {
								valid = false;
							}
						}
					} else {
						switches.put(args[i-1],null);
						valid = false;
					}
				} else {
					switches.put(args[i], null);
				}
			} else {
				valid = false;
			}
		}
		if (!valid && terminateOnInvalidArgument) {
			if (terminateHeading != null) {
				System.out.println(terminateHeading);
				System.out.println();
			}
			System.out.println("Valid command line switches:");
			int maxArgLength = 0;
			for (String key : switchDefinitions.keySet()) {
				maxArgLength = Math.max(maxArgLength, key.length());
			}
			int maxValueHintLength = 0;
			for (SwitchDefinition switchDefinition : switchDefinitions.values()) {
				int valueHintLength = (switchDefinition.valueHint == null)? 0 : switchDefinition.valueHint.length();
				maxValueHintLength = Math.max(maxValueHintLength, valueHintLength);
			}
			maxValueHintLength = Math.min(24, maxValueHintLength);
			for (Map.Entry<String,SwitchDefinition> entry : switchDefinitions.entrySet()) {
				System.out.println("  " + padRight(entry.getKey(), maxArgLength) + "  " + entry.getValue().toString(maxValueHintLength));
			}
			System.out.println();
			System.exit(0);
		}
		return valid;
	}
}
