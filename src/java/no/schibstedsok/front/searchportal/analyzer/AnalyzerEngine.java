/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.analyzer;


/**
 * A AnalyzerEngine.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class AnalyzerEngine {

	/** 
	 * 
	 * Return an Analyzer implementation.
	 * 
	 * @return
	 */
	public static Analyzer getAnalyzer() {
		return new SimpleTextualAnalyzer();
	}

	
}
