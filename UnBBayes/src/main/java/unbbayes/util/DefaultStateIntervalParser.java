/**
 * 
 */
package unbbayes.util;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * Default implementation of {@link IStateIntervalParser}
 * @author Shou Matsumoto
 */
public class DefaultStateIntervalParser implements IStateIntervalParser {

	private Logger logger = Logger.getLogger(getClass());
	

	private String prefix= "_";
	private String splitter = "_to_";
	private String suffix = "";
	
	public DefaultStateIntervalParser() {}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.IStateIntervalParser#getPrefix()
	 */
	public String getPrefix() {
		return prefix;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.IStateIntervalParser#setPrefix(java.lang.String)
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.IStateIntervalParser#getSplitter()
	 */
	public String getSplitter() {
		return splitter;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.IStateIntervalParser#setSplitter(java.lang.String)
	 */
	public void setSplitter(String splitter) {
		this.splitter = splitter;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.IStateIntervalParser#getSuffix()
	 */
	public String getSuffix() {
		return suffix;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.IStateIntervalParser#setSuffix(java.lang.String)
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * It will use {@link #getPrefix()}, {@link #getSplitter()},
	 * and {@link #getSuffix()} to parse a state label and
	 * obtain lower and upper numeric values to sample from.
	 * @param state : state label
	 * @return : {@link Entry} in which {@link Entry#getKey()} is the lower
	 * bound and {@link Entry#getValue()} is the upper bound.
	 */
	public Map.Entry<Float, Float> parseLowerUpperBin(String state) {
		getLogger().trace("Parsing state " + state);
		
		// remove prefix
		if (state.startsWith(getPrefix())) {
			state = state.substring(getPrefix().length());
			getLogger().debug("Removed prefix " + getPrefix() + ". Result: " + state);
		}
		
		// remove suffix
		if (state.endsWith(getSuffix())) {
			state = state.substring(0, state.length() - getSuffix().length());
			getLogger().debug("Removed suffix " + getSuffix() + ". Result: " + state);
		}
		
		// split to 2 substrings with splitter
		String[] split = state.split(getSplitter());
		
		// parse the substrings
		float lower = Float.MAX_VALUE;
		float upper = -100000000f;
		try {
			lower = Float.parseFloat(split[0]);
			upper = lower;
			if (split.length >= 2 ) {
				upper = Float.parseFloat(split[1]);
			}
		} catch (Exception e) {
			getLogger().warn("Could not parse " + state + " with splitter " + getSplitter());
			getLogger().warn("Obtained split was: ");
			for (String string : split) {
				getLogger().warn(string);
			}
		}
		
		getLogger().trace("Parsed " + state + ". Lower = " + lower + ", upper = " + upper);
		
		return Collections.singletonMap(lower, upper).entrySet().iterator().next();
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

}
