// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.navigation;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import no.fast.ds.search.IQueryResult;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;


// TODO Move to test directory
public class NavigationTest extends TestCase {

	String query = "pizza";
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testNoDupes(){

		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query);
		NavigationGraph geographicGraph = (NavigationGraph)handler.getGraphs().get(0);
		
		Search search = new Search();
		IQueryResult result = search.search(geographicGraph, "pizza");
		if(result == null) {
			System.out.println("Skipping testRootSearch(), do you have a Fast tunnel available?");
			return;
		}

		int sizeOne = geographicGraph.getPosition().options().size();
		
		search.search(geographicGraph, "pizza");
		
		int sizeTwo = geographicGraph.getPosition().options().size();
		
		assertTrue(sizeOne == sizeTwo);
		

	}
	
	/**
	 * Test getting a std. Yellow NavigationHandler.
	 */
	public void testAccquireYellowHandler() {

		String collection = "y";
		assertNotNull(NavigationHandler.getNewHandler(collection, query));
		assertTrue(NavigationHandler.getNewHandler(collection, query).getCollection().equals("y"));
//		assertNotNull(NavigationHandler.getNewHandler(collection, query).getHandlerId());
//		assertNotSame(NavigationHandler.getNewHandler(collection, query).getHandlerId(), 
//						NavigationHandler.getNewHandler(collection, query).getHandlerId());
//		
		assertTrue(NavigationHandler.getNewHandler(collection, query).getGraphs().size() == 2);
		
		Iterator graphs = NavigationHandler.getNewHandler(collection, query).getGraphs().iterator();

		printGraphInfo(collection);
		
		graphs = NavigationHandler.getNewHandler(collection, query).getGraphs().iterator();
		NavigationGraph graph = (NavigationGraph) graphs.next();
		
//		printNavigatorInfo(graph.getHierarchy());
		assertTrue(graph.getHierarchy().size() == 5);
		
		graph = (NavigationGraph) graphs.next();
//		printNavigatorInfo(graph.getHierarchy());
		assertTrue(graph.getHierarchy().size() == 1);
		
	}
	
	/**
	 * Test getting a std. Default NavigationHandler.
	 */
	public void testAccquireDefaultHandler() {

		String collection = "d";
		assertNotNull(NavigationHandler.getNewHandler(collection, query));
		assertTrue(NavigationHandler.getNewHandler(collection, query).getCollection().equals("d"));
//		printGraphInfo(collection);
		
		assertTrue(NavigationHandler.getNewHandler(collection, query).getGraphs().size() == 1);	// "sources"
		
		Iterator graphs = NavigationHandler.getNewHandler(collection, query).getGraphs().iterator();
		graphs = NavigationHandler.getNewHandler(collection, query).getGraphs().iterator();
		NavigationGraph graph = (NavigationGraph) graphs.next();
		
//		printNavigatorInfo(graph.getHierarchy());
		assertTrue(graph.getHierarchy().size() == 1);	//only contentSourceNavigator
				
	}


	/**
	 * Test std. POJO serialization.
	 */
	public void testSerialize() {
		
		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query); 
		assertNotNull(handler);
		assertTrue(handler.serialize().length > 0);
	}		

	/**
	 * Test std. POJO de-serialization.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void testDeSerialize() throws IOException, ClassNotFoundException {
		
		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query); 
		assertNotNull(handler);
		byte[] serialized = handler.serialize();
		
		ByteArrayInputStream byteStream = new ByteArrayInputStream(serialized);
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
		Object o = is.readObject();

		assertTrue(o instanceof NavigationHandler);
		
		NavigationHandler handler2 = (NavigationHandler) o;
//		assertTrue(handler2.getHandlerId().equals(handler.getHandlerId()));
		
		
	}		

	/**
	 * Test persistence of NavigationHandler. 
	 */
//	public void testPersist() {
//
//		String collection = "y";
//		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query); 
//		assertNotNull(handler);
//		
//		handler.persist();
//		
////		assertNotNull(id);
//		
////		assertNotNull(NavigationHandlerStore.getInstance().lookUp(id));
////		assertSame(NavigationHandlerStore.getInstance().lookUp(id), handler);
//	}

	/**
	 * Test create Fast Filters.
	 */
	public void testCreateFilter() {
		
		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query);
		assertNotNull(handler);
		
		assertNotNull(handler.getGraphs().get(0));
	
		NavigationGraph geographicGraph = (NavigationGraph)handler.getGraphs().get(0);
		geographicGraph.select("Test");
		printFilterInfo(geographicGraph);
		assertTrue(geographicGraph.getPosition().createFilter().equals(" +ywfylke:\"Test\""));		//root filter
		
		geographicGraph.select("Test2");
		printFilterInfo(geographicGraph);
		assertTrue(geographicGraph.getPosition().createFilter().equals(" +ywkommune:\"Test2\" +ywfylke:\"Test\""));	//filter
				
		geographicGraph.select("Test3");
		printFilterInfo(geographicGraph);
		geographicGraph.select("Test4");
		printFilterInfo(geographicGraph);
		geographicGraph.select("Test5");
		printFilterInfo(geographicGraph);
		geographicGraph.select("Test6");
		printFilterInfo(geographicGraph);
		geographicGraph.select("Test7");
		printFilterInfo(geographicGraph);
		geographicGraph.select("Test8");
		printFilterInfo(geographicGraph);
	}
	
	
	public void testSelections() {

		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query);
		assertNotNull(handler);
		NavigationGraph geographicGraph = (NavigationGraph)handler.getGraphs().iterator().next();
		assertNotNull(geographicGraph);
		
		Search search = new Search();
		IQueryResult result = search.search(geographicGraph, "pizza");
		assertEquals(0, geographicGraph.getPosition().options().size() );
		geographicGraph.select("Oslo");
//		printFilterInfo(geographicGraph);
		result = search.search(geographicGraph, "pizza");
		printFilterInfo(geographicGraph);
//		System.out.println(geographicGraph.getPosition());
//		Iterator options = geographicGraph.getPosition().options().iterator();
//		while (options.hasNext()) {
//			NavigationElement option = (NavigationElement) options.next();
//			System.out.println(option);
//			
//		}
		
	}
	

	/**
	 * Integration-Test a Fast search-request.
	 */
	public void testRootSearch() {

		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query);
		NavigationGraph geographicGraph = (NavigationGraph)handler.getGraphs().get(0);
		
		Search search = new Search();
		IQueryResult result = search.search(geographicGraph, "pizza");
		if(result == null) {
			System.out.println("Skipping testRootSearch(), do you have a Fast tunnel available?");
			return;
		}
		
		assertTrue(geographicGraph.getPosition().options().size() > 0);
		
//		printElementsInfo(geographicGraph.getPosition().options());
		int documents = result.getDocCount();
		assertTrue(documents > 0);
		
	}

	/**
	 * Integration-Test a Fast search-request.
	 */
	public void testBergenSearch() {

		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query);
		NavigationGraph geographicGraph = (NavigationGraph)handler.getGraphs().get(0);
		
		Search search = new Search();
		IQueryResult result = search.search(geographicGraph, "pizza");
		if(result == null) {
			System.out.println("Skipping testRootSearch(), do you have a Fast tunnel available?");
			return;
		}
		
		assertTrue(geographicGraph.getPosition().options().size() > 0);
		
		geographicGraph.select("Hordaland");
		System.out.println(geographicGraph.getPosition().createFilter());
		search.search(geographicGraph, "pizza");
		
		printElementsInfo(geographicGraph.getPosition().options());
		System.out.println();
		assertTrue(result.getDocCount() > 0);

		geographicGraph.select("Bergen");
		System.out.println(geographicGraph.getPosition().createFilter());
		search.search(geographicGraph, "pizza");
		
		printElementsInfo(geographicGraph.getPosition().options());
		System.out.println();
		assertTrue(result.getDocCount() > 0);

		geographicGraph.select("Bergen");
		System.out.println(geographicGraph.getPosition().createFilter());
		search.search(geographicGraph, "pizza");
		
		printElementsInfo(geographicGraph.getPosition().options());
		System.out.println();
		assertTrue(result.getDocCount() > 0);

	}

	/**
	 * Test handling of empty nodes/modifiers in navigators returned from Fast.
	 */
	public void testSearchSkipEmptyModifiers() {

		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query);
		NavigationGraph geographicGraph = (NavigationGraph)handler.getGraphs().get(0);
		System.out.println(geographicGraph.getHierarchy());
		
		Search search = new Search();
		IQueryResult result = search.search(geographicGraph, "pizza");
		if(result == null) {
			System.out.println("Skipping testSearchSkipEmptyModifiers(), do you have a Fast tunnel available?");
			return;
		}

		assertEquals(geographicGraph.getPosition().options().size(), 23);
		assertEquals(geographicGraph.getCurrentLevel(), 0);
		
		//pick Oslo and drill down - Should traverse down to "bydel navigator" at level 3 
		geographicGraph.select("Oslo");
		result = search.search(geographicGraph, "pizza");
		assertEquals(result.getDocCount(), 270); //from webpage is 268?
		assertEquals(geographicGraph.getCurrentLevel(), 3);
		System.out.println(geographicGraph.getPosition().createFilter());
		printElementsInfo(geographicGraph.getPosition().options());
		
		geographicGraph.select("Sentrum");
		result = search.search(geographicGraph, "pizza");
		assertEquals(result.getDocCount(), 28);	//from webpage
		assertEquals(5, geographicGraph.getCurrentLevel());
		System.out.println(geographicGraph.getPosition().createFilter());
		printElementsInfo(geographicGraph.getPosition().options());
		
		
	}
	
	public void testNavigators() {
		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query);
		NavigationGraph geographicGraph = (NavigationGraph)handler.getGraphs().get(0);
		assertEquals(geographicGraph.getNavigatorName(0), "ywfylkesnavigator");
		assertEquals(geographicGraph.getNavigatorName(1), "ywkommunenavigator");
		assertEquals(geographicGraph.getNavigatorName(2), "ywpoststednavigator");
		assertEquals(geographicGraph.getNavigatorName(3), "ywbydelnavigator");
		assertEquals(geographicGraph.getNavigatorName(4), "ywstedsnavnnavigator");
		assertEquals(geographicGraph.getNavigatorName(5), null);	//get beyond returns null

		NavigationGraph tradeGraph = (NavigationGraph)handler.getGraphs().get(1);
		assertEquals(tradeGraph.getNavigatorName(0), "ypbransjenavigator");
		assertEquals(tradeGraph.getNavigatorName(1), null); //get beyond returns null

	}
	
	public void testHierachy() {

		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query);
		NavigationGraph geographicGraph = (NavigationGraph)handler.getGraphs().get(0);
		
		assertTrue(geographicGraph.getPosition().getParents().size() == 0);
		assertEquals(0, geographicGraph.getCurrentLevel());
		System.out.println(geographicGraph.getPosition().getParents());
		
		// drill down
		geographicGraph.select("1");
		assertTrue(geographicGraph.getPosition().getParents().size() == 1);
		assertEquals(1, geographicGraph.getCurrentLevel());
		System.out.println(geographicGraph.getPosition().getParents());
		
		// drill down
		geographicGraph.select("2");
		assertTrue(geographicGraph.getPosition().getParents().size() == 2);
		assertEquals(2, geographicGraph.getCurrentLevel());
		System.out.println(geographicGraph.getPosition().getParents());
				
		// drill down
		geographicGraph.select("3");
		assertEquals(3, geographicGraph.getPosition().getParents().size());
		assertEquals(3, geographicGraph.getCurrentLevel());				
		System.out.println(geographicGraph.getPosition().getParents());

		// drill down invisible node
		geographicGraph.select("4");
		geographicGraph.getPosition().setVisible(false);
		assertEquals(4, geographicGraph.getCurrentLevel());

		geographicGraph.select("5");
		assertEquals(geographicGraph.getCurrentLevel(), 5);
		assertEquals(4, geographicGraph.getPosition().getParents().size());
		System.out.println(geographicGraph.getPosition().getParents());

	}
	
	public void testNavigateUpSequential() {

		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query);
		NavigationGraph geographicGraph = (NavigationGraph)handler.getGraphs().get(0);
		assertEquals(geographicGraph.getCurrentLevel(), 0);
		
		
		Search search = new Search();
		IQueryResult result = search.search(geographicGraph, "pizza");
		if(result == null) {
			System.out.println("Skipping testSearchSkipEmptyModifiers(), do you have a Fast tunnel available?");
			return;
		}

		//drill down
		geographicGraph.select("1");
		assertEquals(geographicGraph.getCurrentLevel(), 1);
		assertEquals(" +ywfylke:\"1\"", geographicGraph.getPosition().createFilter());
		geographicGraph.select("2");
		assertEquals(geographicGraph.getCurrentLevel(), 2);
		assertEquals(" +ywkommune:\"2\" +ywfylke:\"1\"", geographicGraph.getPosition().createFilter());
		geographicGraph.select("3");
		assertEquals(geographicGraph.getCurrentLevel(), 3);
		assertEquals(" +ywpoststed:\"3\" +ywkommune:\"2\" +ywfylke:\"1\"", geographicGraph.getPosition().createFilter());
				
		//go up
		geographicGraph.up("2");
		assertEquals(geographicGraph.getCurrentLevel(), 2);
		assertEquals(" +ywkommune:\"2\" +ywfylke:\"1\"", geographicGraph.getPosition().createFilter());
		geographicGraph.up("1");
		assertEquals(geographicGraph.getCurrentLevel(), 1);
		assertEquals(" +ywfylke:\"1\"", geographicGraph.getPosition().createFilter());
		geographicGraph.up("0");
		assertEquals(geographicGraph.getCurrentLevel(), 0);
		assertEquals("", geographicGraph.getPosition().createFilter());
	}

	public void testNavigateUpJumpInGraph() {

		String collection = "y";
		NavigationHandler handler = NavigationHandler.getNewHandler(collection, query);
		NavigationGraph geographicGraph = (NavigationGraph)handler.getGraphs().get(0);
		assertEquals(geographicGraph.getCurrentLevel(), 0);
		
		
		Search search = new Search();
		IQueryResult result = search.search(geographicGraph, "pizza");
		if(result == null) {
			System.out.println("Skipping testSearchSkipEmptyModifiers(), do you have a Fast tunnel available?");
			return;
		}

		//drill down
		geographicGraph.select("1");
		assertEquals(geographicGraph.getCurrentLevel(), 1);
		assertEquals(" +ywfylke:\"1\"", geographicGraph.getPosition().createFilter());
		geographicGraph.select("2");
		assertEquals(geographicGraph.getCurrentLevel(), 2);
		assertEquals(" +ywkommune:\"2\" +ywfylke:\"1\"", geographicGraph.getPosition().createFilter());
		geographicGraph.select("3");
		assertEquals(geographicGraph.getCurrentLevel(), 3);
		assertEquals(" +ywpoststed:\"3\" +ywkommune:\"2\" +ywfylke:\"1\"", geographicGraph.getPosition().createFilter());
		geographicGraph.select("4");
		assertEquals(geographicGraph.getCurrentLevel(), 4);
		geographicGraph.select("5");
		assertEquals(geographicGraph.getCurrentLevel(), 5);
		geographicGraph.select("6");
		assertEquals(geographicGraph.getCurrentLevel(), 5);
		geographicGraph.select("7");
		assertEquals(geographicGraph.getCurrentLevel(), 5);
				
		//go up
		geographicGraph.up("1");
		assertEquals(1, geographicGraph.getCurrentLevel());
		assertEquals(" +ywfylke:\"1\"", geographicGraph.getPosition().createFilter());
	}

	/**
	 * Pick any element from returned options.
	 * 
	 * @param geographicGraph
	 * @return
	 */
	private NavigationElement pickRandomElement(NavigationGraph geographicGraph) {
		Random  random = new Random(System.currentTimeMillis());
		NavigationElement selected = (NavigationElement)geographicGraph.getPosition().options().get(random.nextInt(geographicGraph.getPosition().options().size()));
		return selected;
	}

	/**
	 * Print info.
	 * @param geographicGraph
	 */
	private void printFilterInfo(NavigationGraph geographicGraph) {
		System.out.println(geographicGraph.getPosition().createFilter());
	}
	

	/**
	 * Print info.
	 * @param elements
	 */
	private void printElementsInfo(List elements) {
		Iterator iter = elements.iterator();
		while (iter.hasNext()) {
			NavigationElement element = (NavigationElement) iter.next();
			System.out.println(element);
		}
	}
	
	/**
	 * Print info.
	 * @param navigators
	 */
	private void printNavigatorInfo(List navigators) {
		
		Iterator iter = navigators.iterator();
		while (iter.hasNext()) {
			FastNavigator navigator = (FastNavigator)iter.next();
			
			System.out.println(navigator);
			System.out.println(navigator.getField());
			System.out.println(navigator.getDisplayName());
			
		}
	}
	
	/**
	 * Print info.
	 * @param collection
	 */
	private void printGraphInfo(String collection) {
		
		Iterator iter = NavigationHandler.getNewHandler(collection, query).getGraphs().iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
			
		}
	}
	
}
