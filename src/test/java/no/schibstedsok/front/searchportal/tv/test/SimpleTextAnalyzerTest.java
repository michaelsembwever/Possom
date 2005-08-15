/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.tv.test;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.analyzer.Analyzer;
import no.schibstedsok.front.searchportal.analyzer.AnalyzerEngine;
import no.schibstedsok.tv.service.TvFeedImpl;

/**
 * A SimpleTextAnalyzerTest.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class SimpleTextAnalyzerTest extends TestCase {

	public static void main(String[] args) {
	}

	public void testPatternReplace() {
		
		String test = "TV Sommer";
		Analyzer analyzer = AnalyzerEngine.getAnalyzer();
		assertEquals("Sommer", analyzer.replace(test));
		
	}

	public void testSeriesTrim() {
		String test = "Hotel Cæsar (19)";
		assertEquals(TvFeedImpl.trim(test), "Hotel Cæsar");
	}
	
	public void testPatternMatching() {

		Analyzer analyzer = AnalyzerEngine.getAnalyzer();
		assertTrue(analyzer.analyze("TV ").size() == 1);
		assertTrue(analyzer.analyze("TV test").size() == 1);
		assertTrue(analyzer.analyze("tv ").size() == 1);
		assertTrue(analyzer.analyze("tv test").size() == 1);
		assertTrue(analyzer.analyze("tv-series").size() == 1);
		
		assertTrue(analyzer.analyze("tvette").size() == 0);
		assertTrue(analyzer.analyze("a tv-series").size() == 0);
		assertTrue(analyzer.analyze("t").size() == 0);
		
	}
	
}
