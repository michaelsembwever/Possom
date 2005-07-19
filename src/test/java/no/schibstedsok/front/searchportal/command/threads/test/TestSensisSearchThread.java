package no.schibstedsok.front.searchportal.command.threads.test;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SensisFastSearchConfiguration;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.filters.sensis.SensisSearchConsumer;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.util.VelocityTemplates;

public class TestSensisSearchThread extends TestCase {

	String query = "Elvis Presley";

	long maxTime = 20000;

	int maxResults = 10;

	int offset = 0;

	FastSearchConfiguration config;

	StringWriter w = new StringWriter();

	Writer writer = new BufferedWriter(w);

	protected void setUp() throws Exception {
		super.setUp();
		config = new SensisFastSearchConfiguration();
		config.setMaxTime(maxTime);
		config.setDocsToReturn(maxResults);
		config.setOffSet(offset);
		config.setLanguage("en");
		config.setQuery(query);
		config.setTemplate(VelocityTemplates.GLOBAL_SEARCH);

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSensisSearchThread() throws Exception {

		SearchConsumer consumer = new SensisSearchConsumer(writer, config);
		consumer.run();
		writer.flush();
		System.out.println(w);
		assertNotSame("", w.toString());
	}

	public void testSensisDocCounter() throws Exception {

		config.setTemplate(VelocityTemplates.GLOBAL_COUNT);
		config.setNavigatorString(SearchConstants.COUNTERNAVIGATORSTRING);
		SearchConsumer consumer = new SensisSearchConsumer(writer, config);
		consumer.run();
		writer.flush();
		System.out.println(w);
		assertNotSame("", w.toString());
	}

}
