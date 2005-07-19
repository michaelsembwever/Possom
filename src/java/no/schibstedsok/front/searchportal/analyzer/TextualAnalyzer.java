/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.analyzer;

import java.util.List;

/**
 * A TextualAnalyzer.
 * 
 * Override with implementations.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public abstract class TextualAnalyzer implements Analyzer {


	/**
	 * Create a new TextualAnalyzer.
	 * 
	 */
	protected TextualAnalyzer() {
		super();
	}
	
	/**
	 * 
	 * Basic version, uses simple String pattern recognition.
	 * 
	 */
	public List analyze(String input) {
		
		if(input == null)
			return null;
		
		return doAnalyze(input);
	}

	/** Override this in implementations.*/
	protected abstract List doAnalyze(String input);


	public String replace(String input) {
		
		if(input == null)
			return null;

		return doReplace(input);
	}

	/** 
	 * Override this in implementations.
	 * 
	 * @param input
	 * @return
	 */
	protected abstract String doReplace(String input);
	
}
