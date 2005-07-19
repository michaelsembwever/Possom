/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.analyzer;

import java.util.List;

/**
 * A Analyzer.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public interface Analyzer {

	public List analyze(String theString);

	/** 
	 * Replaces the corresponding patterns from String.
	 * @param test
	 */
	public String replace(String test);
}
