package no.schibstedsok.front.searchportal.navigation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.ConfigurationException;
import no.fast.ds.search.FastSearchEngineFactory;
import no.fast.ds.search.IFastSearchEngine;
import no.fast.ds.search.IFastSearchEngineFactory;
import no.fast.ds.search.IQuery;
import no.fast.ds.search.IQueryResult;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.Query;
import no.fast.ds.search.SearchEngineException;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchParameters;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.navigation.*;
import no.schibstedsok.front.searchportal.query.transform.QueryTransformer;
import no.schibstedsok.front.searchportal.site.Site;

public class Search {

	private static IFastSearchEngineFactory engineFactory;
	private static IFastSearchEngine engine;
	private boolean spellcheck;

	private FastConfiguration fastConfiguration = new FastConfiguration();

	private Map parameters;
	private String transformedQuery;

	static {
		try {
			engineFactory = FastSearchEngineFactory.newInstance();
			engine = engineFactory.createSearchEngine("http://localhost:15200/");
		} catch (ConfigurationException e) {
			throw new InfrastructureException(e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	protected IQueryResult search(NavigationGraph graph, String query) {

		IQuery fastQuery = createQuery(query, graph);
		IQueryResult result = null;
		try {
			result = engine.search(fastQuery);
//			showOriginalQuery(result);
			
			graph.updateNavigationGraph(result);
			
		} catch (SearchEngineException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

//	private void updateGraph(NavigationGraph graph, IQueryResult result) {
//		
//		int level = graph.getPosition().level;
//		updateGraph(graph, result, level);
//		
//	}
//
//	private void updateGraph(NavigationGraph graph, IQueryResult result, int level) {
//		
//		INavigator nav = result.getNavigator(graph.getNavigatorName(level));
//		if(nav != null) {
//			if(nav.modifierCount() == 1) {
//				IModifier modifier = (IModifier)nav.modifiers().next();
//				updateGraph(graph, result, level + 1);
//				graph.select(modifier.getValue());	//auto-select levels with one entry
//			} else {
//				Iterator modifiers = nav.modifiers();
//				while (modifiers.hasNext()) {
//					IModifier modifier = (IModifier) modifiers.next();
//					graph.getPosition().addOption(modifier.getName(), modifier.getValue(), modifier.getCount());
//				}
//			}
//		}
//	}


	private IQuery createQuery(String queryString, NavigationGraph graph) {
		
		ISearchParameters params = new SearchParameters();
		params.setParameter(new SearchParameter(BaseParameter.LEMMATIZE,
				fastConfiguration.isLemmatizeEnabled()));

		if (spellcheck)
			spellCheckEnable(params);

		params.setParameter(new SearchParameter("filtertype", "any"));

		params.setParameter(new SearchParameter(BaseParameter.TYPE, "all"));

		params.setParameter(new SearchParameter(BaseParameter.QUERY, queryString));
		params.setParameter(new SearchParameter(BaseParameter.COLLAPSING, fastConfiguration.isCollapsingEnabled()));
		params.setParameter(new SearchParameter(BaseParameter.LANGUAGE, "no"));
		params.setParameter(new SearchParameter(BaseParameter.NAVIGATION,true));
		params.setParameter(new SearchParameter(BaseParameter.NAVIGATION_HITS, fastConfiguration.getResultsToReturn()));
		params.setParameter(new SearchParameter(BaseParameter.CLUSTERING, fastConfiguration.isClusteringEnabled()));

		if (fastConfiguration.getResultView() != null) {
			params.setParameter(new SearchParameter(BaseParameter.RESULT_VIEW, fastConfiguration.getResultView()));
		}

		if (fastConfiguration.getSortBy() != null) {
			params.setParameter(new SearchParameter(BaseParameter.SORT_BY, fastConfiguration.getSortBy()));
		}

		params.setParameter(new SearchParameter(BaseParameter.NAVIGATORS, getNavigatorsString()));
		String filter = "";
		filter = graph.getPosition().createFilter();
		
//		System.out.println("Using filter " + filter);
        
		params.setParameter(new SearchParameter(BaseParameter.FILTER, filter));
		
		IQuery query = new Query(params);

		return query;
	}
    
	private void showOriginalQuery(IQueryResult res) {
		System.out.println("Constructed query: " + res.getOriginalQuery());
	}

	private String getNavigatorsString() {

		String a = "";
		if (fastConfiguration.getNavigators() != null) {

			Collection allFlattened = new ArrayList();

			for (Iterator iterator = fastConfiguration.getNavigators().values()
					.iterator(); iterator.hasNext();) {
				FastNavigator navigator = (FastNavigator) iterator.next();
				allFlattened.addAll(flattenNavigators(new ArrayList(),
						navigator));
			}

			for (Iterator iterator = allFlattened.iterator(); iterator
					.hasNext();) {
				// if(iterator.next() == null) continue;
				FastNavigator nav = (FastNavigator) iterator.next();
				if (nav.getName() != null)
					a += nav + ", ";
			}

		} else {
			return "";
		}

		return a;
	}

	private Collection flattenNavigators(Collection soFar, FastNavigator nav) {
		soFar.add(nav);

		if (nav.getChildNavigator() != null) {
			flattenNavigators(soFar, nav.getChildNavigator());
		}

		return soFar;
	}

	protected Map getParameters() {
		return parameters;
	}

	/**
	 * Returns the query as it is after the query transformers have been applied
	 * to it.
	 * 
	 * @return The transformed query.
	 */
	public String getTransformedQuery() {
		return transformedQuery;
	}

	private String applyQueryTransformers(List transformers) {
		String transformedQuery = this.transformedQuery;

		if (transformers != null) {
			for (Iterator iterator = transformers.iterator(); iterator.hasNext();) {
				QueryTransformer transformer = (QueryTransformer) iterator.next();
                
                final String origQuery = transformedQuery;
                final QueryTransformer.Context qtCxt = new QueryTransformer.Context(){
                    public String getQueryString() {
                        return origQuery;
                    }

                    public Site getSite() {
                        return Site.DEFAULT;
                    }
                    
                };
				transformedQuery = transformer.getTransformedQuery(qtCxt);
			}
		}
		return transformedQuery;
	}

	private void spellCheckEnable(ISearchParameters params) {
		params.setParameter(new SearchParameter(BaseParameter.SPELL,
				"suggest"));
		params.setParameter(new SearchParameter("qtf_spellcheck:addconsidered",
				"1"));
		params.setParameter(new SearchParameter(
				"qtf_spellcheck:consideredverbose", "1"));
	}

}
