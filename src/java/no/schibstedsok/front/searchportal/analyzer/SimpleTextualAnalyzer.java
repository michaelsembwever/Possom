/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.analyzer;

import java.util.ArrayList;
import java.util.List;


/**
 * A TextualAnalyzer.
 * 
 * See: http://www.regular-expressions.info/java.html
 * 
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class SimpleTextualAnalyzer extends TextualAnalyzer {


	SimpleTextualAnalyzerTV tvAnalyzer = null;
	/**
	 * Create a new SimpleTextualAnalyzer.
	 * 
	 */
	public SimpleTextualAnalyzer() {
		super();
	    tvAnalyzer = SimpleTextualAnalyzerTV.getInstance();
		
	}
	
	protected List doAnalyze(String input) {
		
		ArrayList result = new ArrayList();

		//tv-analyzer
		result.addAll(tvAnalyzer.analyze(input));
		
		//weather
//		result.addAll(weatherAnalyzer.analyze(input));
		
		return result;
	}

	protected String doReplace(String input) {
		StringBuffer buffer = new StringBuffer();
		
		//tv-analyzer
		buffer.append(tvAnalyzer.replace(input));
		
		return buffer.toString();
	}


}
