package no.schibstedsok.front.searchportal.connectors.test;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.command.ConnectorCommand;
import no.schibstedsok.front.searchportal.command.FastConnectorCommand;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfigurationImpl;
import no.schibstedsok.front.searchportal.connectors.FastConnector;
import no.schibstedsok.front.searchportal.response.FastSearchResponseImpl;
import no.schibstedsok.front.searchportal.util.SearchConstants;

public class FastConnectorTest extends TestCase {

	
	FastSearchConfigurationImpl configuration = null;
	
    public static void main(String[] args) {
        junit.textui.TestRunner.run(FastConnectorTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
		configuration = new FastSearchConfigurationImpl();
		configuration.setCollection(SearchConstants.DEFAULTCOLLECTION);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
		configuration.setNavigatorString("");
    }

	public void testCollectionSearchInRetriever() {
		String query ="Schibsted ASA";
		doCollectionSearch(SearchConstants.MEDIA_COLLECTION, query);
	}
	
	public void testCollectionSearchInWiki() {
		String query ="Lars Johansson";
		doCollectionSearch(SearchConstants.WIKI_COLLECTION, query);
	}
	
	public void testNavigators() {

		String query = "Schibsted ASA";
		
		configuration.setCollection(SearchConstants.DEFAULTCOLLECTION);
		configuration.setNavigatorString(SearchConstants.COUNTERNAVIGATORSTRING);
		configuration.setQuery(query);
		configuration.setDocsToReturn(1);
		configuration.setOffSet(0);
		
		
		FastConnectorCommand command = new FastConnectorCommand();	
		
		FastConnector.getInstance().execute(command, configuration);
//		System.out.println(command.getResponse());
		FastSearchResponseImpl response = (FastSearchResponseImpl)command.getResponse();
		System.out.println(response);
		assertTrue(response.getWebCrawlDocumentsInIndex() > 0);
//		
	}
	
	public void doCollectionSearch(String collection, String query) {
	
		configuration.setCollection(SearchConstants.DEFAULTCOLLECTION);
		ConnectorCommand command = new FastConnectorCommand();

		configuration.setQuery(query);
		configuration.setDocsToReturn(10);
		configuration.setOffSet(0);
		
		configuration.setCollection(collection);
		
		FastConnector.getInstance().execute(command, configuration);
//		System.out.println(command.getResponse());
	}

	public void testOffestStart() {

		ConnectorCommand command = new FastConnectorCommand();

		//	make sure this query returns more than 200 docs or so for the tests!
		configuration.setQuery("Elvis Presley");	
        configuration.setDocsToReturn(10);
		configuration.setOffSet(0);
//		
//        FastConnector.getInstance().execute(command, configuration);
//		assertEquals(11, command.getResponse().getConsequtiveSearchStartsAt());
//		assertEquals(10, command.getResponse().getDocumentsReturned());
//		
//		configuration.setDocsToReturn(100);
//		configuration.setOffSet(0);
//
//        FastConnector.getInstance().execute(command, configuration);
//		assertEquals(101, command.getResponse().getConsequtiveSearchStartsAt());
//		assertEquals(100, command.getResponse().getDocumentsReturned());
//		
		
		configuration.setDocsToReturn(10);
		configuration.setOffSet(10);

        FastConnector.getInstance().execute(command, configuration);
		assertEquals(21, command.getResponse().getConsequtiveSearchStartsAt());
		assertEquals(10, command.getResponse().getDocumentsReturned());
		
		configuration.setDocsToReturn(10);
		configuration.setOffSet(34);

        FastConnector.getInstance().execute(command, configuration);
		assertEquals(45, command.getResponse().getConsequtiveSearchStartsAt());
		
		configuration.setDocsToReturn(10);
		configuration.setOffSet(151);

        FastConnector.getInstance().execute(command, configuration);
		assertEquals(162, command.getResponse().getConsequtiveSearchStartsAt());
		
//		configuration.setDocsToReturn(10);
//		configuration.setOffSet((1890);
//
//        FastConnector.getInstance().execute(command, configuration);
//		assertEquals(1901, command.getResponse().getConsequtiveSearchStartsAt());
		
	}
	
	public void testDoQuery() {

        
        ConnectorCommand command = new FastConnectorCommand();

        configuration.setQuery("Elvis Presley");
        configuration.setDocsToReturn(10);
		configuration.setOffSet(0);

        FastConnector.getInstance().execute(command, configuration);
        assertNotNull(command.getResponse());
        
//        System.out.println(command.getResponse());

    }

    public void _testSpellingSuggestion() {

        ConnectorCommand command = new FastConnectorCommand();

        configuration.setQuery("arnsen");

        FastConnector.getInstance().execute(command, configuration);

        FastSearchResponseImpl response = (FastSearchResponseImpl) command
                .getResponse();
        
        System.out.println(response);

        assertNotNull(response);
//        assertNotSame("", response.getSpellingSuggestion());

    }

}
