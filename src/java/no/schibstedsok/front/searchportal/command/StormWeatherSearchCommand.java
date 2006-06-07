// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.command;


import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.configuration.StormWeatherSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.FastSearchResult;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import com.opensymphony.oscache.base.NeedsRefreshException;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.net.URLEncoder;

/**
 * @author <a href="mailto:lars.johansson@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 0 $</tt>
 */
public class StormWeatherSearchCommand extends FastSearchCommand {

	//OSCache stuff
	private static final int EVICTIONPERIOD_WEATHER_CACHE = 60 * 5; //five minutes
	private static final GeneralCacheAdministrator admin = new GeneralCacheAdministrator();
	
	private static Log log = LogFactory.getLog(StormWeatherSearchCommand.class);
    HTTPClient client = HTTPClient.instance("weather", SearchConstants.STORM_WEATHER_SEARCH_HOST, 80);

    /**
     * @param query         The query to act on.
     * @param configuration The search configuration associated with this
     *                      command.
     * @param parameters    Command parameters.
     */
    public StormWeatherSearchCommand(final SearchCommand.Context cxt, final Map parameters) {
        super(cxt, parameters);
    }
    
    protected FastSearchResult executeFastCommand(){
    	return (FastSearchResult) super.execute();
    }
    
    /**
     * First do a Fast search to get the lat/long of the place in question,
     * then use fast navigator values to query the Storm weather service. 
     * 
     */
    public SearchResult execute() {

    	//Fast search
        FastSearchResult fastResult = executeFastCommand();
        fastResult.setHitCount(0);

    	// "enrich" the Fast result with Storm weather forecasts based on lat/long.
        if(fastResult.getResults().size() > 0){
        	
        	fastResult.setHitCount(fastResult.getResults().size());
   
        	for (Iterator results = fastResult.getResults().iterator(); results.hasNext();) {
        		
        		BasicSearchResultItem result = (BasicSearchResultItem) results.next();
			
        		SearchResult forecasts = new BasicSearchResult(this);
        		
        		//based on latitude, longitude, get the current forecast
        		if(result.getField("lat") != null && result.getField("long") != null){
 
        		String lat = result.getField("lat");
        		String lon = result.getField("long");
        		
        			if(getParameter("igeneric1") != null &! "".equals(getParameter("igeneric1"))){

        				getForecasts(forecasts, lat, lon);
        		        
        			} else {
        				
        				forecasts.addResult(getCurrentForecast(lat, lon));
        			
        			}
        		} 

        		//add forecasts to the fast result
        		result.addNestedSearchResult("forecasts", forecasts);
        	}
        	
        } else {
        	log.debug("No navigators found");
        }

        return fastResult;
        
    }

    private SearchResultItem getCurrentForecast(String la, String lo){
    	return getCurrentForecast(la, lo, null);
    }
    
    private SearchResultItem getCurrentForecast(String la, String lo, String height) {

    	BasicSearchResultItem e = null;

    	String cacheKey = la + "#" +lo;
    	boolean updated = false; //cache flag used for eviction/update deadlock.

    	try {
    	
    		// Get from the cache
    		e = (BasicSearchResultItem) admin.getFromCache(cacheKey, EVICTIONPERIOD_WEATHER_CACHE);
    	
    	} catch (NeedsRefreshException nre) {
    		
    		log.info("Refreshing cache for " + cacheKey);
    		
    		try {

    			// Get from Storm service  
    			final Document doc = getForecastDocument(la, lo, height);

    			final Element resultElement = doc.getDocumentElement();

    			if (doc != null) {
    				NodeList nl = doc.getElementsByTagName("pointforecast");
    				if(nl != null && nl.getLength() > 0) {
    					Element el = (Element)nl.item(1);	// current forecast
    					e = getItem(el);
    				}
    			}

    			// Store in the cache
    			admin.putInCache(cacheKey, e);

    			updated = true;

	    		} catch (Exception ex) {
	
	    			// We have the outdated content for fail-over. May become stale!
	    			e = (BasicSearchResultItem) nre.getCacheContent();
	    			log.error("Cache update exception, forecasts may become stale! " + ex.getMessage());
	    		} finally{
    			if (!updated) {
    				// It is essential that cancelUpdate is called if the
    				// cached content could not be rebuilt
    				admin.cancelUpdate(cacheKey);
    			}
    		}
    	}
    	
        return e;
    }
    
    protected void getForecasts(SearchResult result, String la, String lo){
    	getForecasts(result, la, lo, null);
    }

    
    /**
     * Query the service for a weatherforecast and transform response into BasicSearchResult.
     * 
     * @param la
     * @param lo
     * @param height
     * @return
     */
    private void getForecasts(SearchResult result, String la, String lo, String height) {

    	final Document doc = getForecastDocument(la, lo, height);
    
        final Element resultElement = doc.getDocumentElement();

        if (doc != null) {

    		NodeList nl = doc.getElementsByTagName("pointforecast");
    		if(nl != null && nl.getLength() > 0) {
    			for(int i = 0 ; i < nl.getLength();i++) {
    				Element el = (Element)nl.item(i);
    				BasicSearchResultItem e1 = getItem(el);
    				result.addResult(e1);
    			}
    		}
        }
    }

	/**
	 * 
	 * Get the forecast xml-document. 
	 * 
	 * 
	 * @param la
	 * @param lo
	 * @param height
	 * @return
	 */private Document getForecastDocument(String la, String lo, String height) {
		//use dot notation
    	if(la!=null)
    		la = la.replace(',', '.');
    	if(lo != null)
    		lo = lo.replace(',', '.');
    	
    	StringBuffer url = new StringBuffer();
    	try {
    		url.append("/kunder/schibsted/wod.aspx?la=").append(URLEncoder.encode(la, "utf-8")).append("&lo=").append(URLEncoder.encode(lo, "UTF8"));
    	} catch(UnsupportedEncodingException e1){
    		log.error("Unable to encode URL when speaking with Storm weather service: " + e1.getMessage());
    		throw new InfrastructureException(e1);
    	}
    	
    	if(height!=null){
    		height = height.replace(',', '.');
    		url.append("&m=").append(height);    		
    	}
    	
    	if(log.isDebugEnabled())
    		log.debug("Using url:" + url.toString());
    	
        final Document doc = doSearch(url.toString());
		return doc;
	}


	/**
	 * Create a ResultItem using the resultFields listed in configuration.
	 * 
	 * @param element
	 * @return
	 */
    private BasicSearchResultItem getItem(Element element) {

		BasicSearchResultItem e = new BasicSearchResultItem();
		for (Iterator iter = ((StormWeatherSearchConfiguration)context.getSearchConfiguration()).getElementValues().iterator(); iter.hasNext();) {
			String field = (String) iter.next();
			e.addField(field, getTextValue(element, field));
		}
		return e;
	}


	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal;
	}

	private Document doSearch(final String url) {
        try {
            return client.getXmlDocument("weather", url);
        } catch (HttpException e) {
            log.error("Unable to connect to " + url, e);
        } catch (IOException e) {
            log.error("Problems with connection to " + url, e);
        } catch (SAXException e) {
            log.error("XML Parse error " + url , e);
        }
        return null;
    }
	
}
