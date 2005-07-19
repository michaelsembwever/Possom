/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import no.schibstedsok.front.searchportal.filters.tv.TvFilter;
import no.schibstedsok.front.searchportal.util.SearchConstants;

import org.apache.log4j.Logger;


/**
 * A SimpleTextualAnalyzerTV.
 * 
 * See: http://www.regular-expressions.info/java.html
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class SimpleTextualAnalyzerTV implements Analyzer {

	/** The pattern is overriden by propertyfile in Constructor */
	public static String pattern = "^TV |^TV-";
	
	private static String myFilterImpl = TvFilter.class.getName();

	private static Pattern p;
	private static SimpleTextualAnalyzerTV instance = null; 
	
	private Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Create a new SimpleTextualAnalyzer.
	 * 
	 */
	private SimpleTextualAnalyzerTV() {
		super();

		Properties properties = new Properties();
		try {
			properties.load(getClass().getResourceAsStream("/" + SearchConstants.PATTERNS_PROPERTYFILE));
			pattern = properties.getProperty(SearchConstants.PROPERTY_KEY___TV_PATTERN);
		} catch (Exception e1) {
			log.error("Unable to read pattern property file. Using default pattern:" + pattern);
		}

		try {

			int options = Pattern.CANON_EQ | Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE;
	        p = Pattern.compile(pattern, options);
			if(log.isDebugEnabled())
				log.debug("Compiled pattern: " + pattern);
			
		 } catch (PatternSyntaxException e) {
			log.error("Regex syntax error: " + e.getMessage ());
			log.error("Error description: " + e.getDescription ());
			log.error("Error index: " + e.getIndex ());
			log.error("Erroneous pattern: " + e.getPattern ());
	        
			 throw new RuntimeException(e);
	     }
		
	}
	
	/** synchronization not striclty necessary */
	public static SimpleTextualAnalyzerTV getInstance() {
		
		if(instance == null) {
			instance = new SimpleTextualAnalyzerTV();
		}
		return instance;
	}

	public List analyze(String input) {
		List result = new ArrayList(1);
		Matcher matcher = p.matcher((CharSequence)input);
		
		if(matcher.find()) {
			result.add(myFilterImpl);
			if(log.isDebugEnabled())
				log.debug("Added " + myFilterImpl);
			return result; 
		}
		return result;
			
	}
	
	public String replace(String input) {
		
		Matcher matcher = p.matcher((CharSequence)input);
		return matcher.replaceAll("");
			
			
	}
	
}
