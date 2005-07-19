package no.schibstedsok.front.searchportal.command.threads.test;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfigurationImpl;
import no.schibstedsok.front.searchportal.filters.fast.FastSearchConsumer;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.util.VelocityTemplates;

public class TestFastAllCollectionsThread extends TestCase {

	String query = "Lloyd Cole";
	long maxTime = 300000;
	int maxResults = 10;
	int offset = 0;

	StringWriter w = new StringWriter();
	Writer writer = new BufferedWriter(w);
	
	FastSearchConfigurationImpl config ;
	
	protected void setUp() throws Exception {
		super.setUp();
		config = new FastSearchConfigurationImpl();
		config.setQuery(query);
		config.setMaxTime(maxTime);
		config.setDocsToReturn(maxResults);
		config.setOffSet(offset);
		config.setCollection(SearchConstants.DEFAULTCOLLECTION);
		config.setTemplate(VelocityTemplates.ALL_COLLECTIONS_SEARCH);

	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/** 
	 * Test the search with templating using a Writer
	 * @throws Exception
	 */
	public void testFastSearchThread() throws Exception {
		
		FastSearchConsumer consumer = new FastSearchConsumer(writer, config);
		consumer.run(); 
		writer.flush();
		System.out.println(w);
		assertNotSame("", w.toString());
	}
	
	public void testFastDocCounter() throws Exception {
	
		config.setTemplate(VelocityTemplates.LOCAL_COUNT);
		config.setNavigatorString(SearchConstants.COUNTERNAVIGATORSTRING);
		config.setDocsToReturn(1);
		FastSearchConsumer consumer = new FastSearchConsumer(writer, config);
		consumer.run();
		writer.flush();
		System.out.println(w);
		assertNotSame("", w.toString());
		
	}
	
	public void _testFastSearchThreadTimeout() throws Exception {
		fail();
	}
}
