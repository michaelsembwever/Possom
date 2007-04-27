// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.StormweatherCommandConfig;
import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import no.schibstedsok.searchportal.datamodel.DataModel;

/**
 * @author <a href="mailto:lars.johansson@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 0 $</tt>
 */
public final class StormWeatherSearchCommand extends FastSearchCommand {

    //OSCache stuff
    private static final int EVICTIONPERIOD_WEATHER_CACHE = 60 * 5; //five minutes
    private static final GeneralCacheAdministrator ADMIN = new GeneralCacheAdministrator();
    private static final String STORM_WEATHER_SEARCH_HOST = "www.storm.no";

    private static final Logger LOG = Logger.getLogger(StormWeatherSearchCommand.class);
    /** TODO comment me. **/
    private final HTTPClient client = HTTPClient.instance(STORM_WEATHER_SEARCH_HOST, 80);

    /**
     * @param query         The query to act on.
     * @param configuration The search configuration associated with this
     *                      command.
     * @param parameters    Command parameters.
     */
    public StormWeatherSearchCommand(final Context cxt) {

        super(cxt);
        LOG.debug("Creating WeatherSearchCommand");

    }

    /** TODO comment me. **/
    protected FastSearchResult executeFastCommand(){
        return (FastSearchResult) super.execute();
    }

    /**
     * First do a Fast search to get the lat/long of the place in question,
     * then use fast navigator values to query the Storm weather service.
     *
     *
     * The model is that a location may carry one or more forecasts.
     * Each resultItem carries forecasts in a nested searchResult named
     * with the key "forecasts".
     *
     * Location is in FAST index, forecasts is fetched from Storm.
     *
     *
     */
    public SearchResult execute() {

        //Fast search
        final FastSearchResult fastResult = executeFastCommand();

        //on empty queries return only the navigators
//        if(getRunningQuery().getQuery().isBlank()){
//            fastResult.setHitCount(0);
//            return fastResult;
//        }

        // "enrich" the Fast result with Storm weather forecasts based on lat/long.
        if(fastResult.getResults().size() > 0){

            for (final Iterator results = fastResult.getResults().iterator(); results.hasNext();) {

                final BasicSearchResultItem result = (BasicSearchResultItem) results.next();

                final SearchResult forecasts = new BasicSearchResult(this);

                //based on latitude, longitude, get the current forecast
                if(result.getField("lat") != null && result.getField("long") != null){

                    final String lat = result.getField("lat");
                    final String lon = result.getField("long");
                    final String alt = result.getField("altitude");

                    //infopage or resultlisting?
                    if(getParameter("igeneric1") != null &! "".equals(getParameter("igeneric1"))){

                        getForecasts(forecasts, lat, lon, alt);

                    } else {

                        forecasts.addResult(getCurrentForecast(lat, lon, alt));

                    }
                }

                //add forecasts to the fast result
                result.addNestedSearchResult("forecasts", forecasts);
            }

        }

        return fastResult;

    }

    private SearchResultItem getCurrentForecast(final String la, final String lo){
        return getCurrentForecast(la, lo, "0");
    }

    private SearchResultItem getCurrentForecast(final String la, final String lo, final String altitude) {

        BasicSearchResultItem e = null;

        final String cacheKey = la + "#" +lo + "#" + altitude;
        boolean updated = false; //cache flag used for eviction/update deadlock.

        try {

            // Get from the cache
            e = (BasicSearchResultItem) ADMIN.getFromCache(cacheKey, EVICTIONPERIOD_WEATHER_CACHE);

        } catch (NeedsRefreshException nre) {

            LOG.debug("Refreshing cache for " + cacheKey);

            try {

                // Get from Storm service
                final Document doc = getForecastDocument(la, lo, altitude);

                final Element resultElement = doc.getDocumentElement();

                if (doc != null) {
                    final NodeList nl = doc.getElementsByTagName("pointforecast");
                    if(nl != null && nl.getLength() > 0) {
                        final Calendar cal = Calendar.getInstance();
                        final int hour = cal.get(Calendar.HOUR_OF_DAY);
                        int idx = 0;

                        if (hour >= 21) {
                            idx = 3;
                        } else if (hour >= 15) {
                            idx = 2;
                        } else if (hour >= 9) {
                            idx = 1;
                        } else {
                            idx = 0;
                        }

                        final Element el = (Element)nl.item(idx);	// current forecast
                        e = getItem(el);
                    }
                }

                // Store in the cache
                ADMIN.putInCache(cacheKey, e);

                updated = true;

            } catch (Exception ex) {

                // We have the outdated content for fail-over. May become stale!
                e = (BasicSearchResultItem) nre.getCacheContent();
                LOG.error("Cache update exception, forecasts may become stale! " + ex.getMessage());
            } finally{
                if (!updated) {
                    // It is essential that cancelUpdate is called if the
                    // cached content could not be rebuilt
                    ADMIN.cancelUpdate(cacheKey);
                }
            }
        }

        return e;
    }

    /** TODO comment me. **/
    protected void getForecasts(final SearchResult result, final String la, final String lo){
        getForecasts(result, la, lo, "0");
    }


    /**
     * Query the service for a weatherforecast and transform response into BasicSearchResult.
     *
     * @param la
     * @param lo
     * @param height
     * @return
     */
    private void getForecasts(final SearchResult result, final String la, final String lo, String altitude) {

        final Document doc = getForecastDocument(la, lo, altitude);

        final Element resultElement = doc.getDocumentElement();

        if (doc != null) {

            final NodeList nl = doc.getElementsByTagName("pointforecast");
            if(nl != null && nl.getLength() > 0) {
                for(int i = 0 ; i < nl.getLength();i++) {
                    final Element el = (Element)nl.item(i);
                    final BasicSearchResultItem e1 = getItem(el);
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
     */
    private Document getForecastDocument(String la, String lo, String altitude) {
         //use dot notation
         if(la!=null){
             la = la.replace(',', '.');
         }
         if(lo != null){
             lo = lo.replace(',', '.');
         }

         final StringBuilder url = new StringBuilder();
         try {
             url.append("/kunder/schibsted/wod.aspx?la=").append(URLEncoder.encode(la, "utf-8")).append("&lo=").append(URLEncoder.encode(lo, "UTF8"));
         } catch(UnsupportedEncodingException e1){
             LOG.error("Unable to encode URL when speaking with Storm weather service: " + e1.getMessage());
             throw new InfrastructureException(e1);
         }

         if(altitude != null){
             altitude = altitude.replace(',', '.');
             url.append("&m=").append(altitude);
         }

         LOG.debug("Using url:" + url.toString());

         final Document doc = doSearch(url.toString());
         return doc;
     }


     /**
      * Create a ResultItem using the resultFields listed in configuration.
      *
      * @param element
      * @return
      */
     private BasicSearchResultItem getItem(final Element element) {

         final BasicSearchResultItem e = new BasicSearchResultItem();
         for (final Iterator iter = getSearchConfiguration().getElementValues().iterator(); iter.hasNext();) {
             final String field = (String) iter.next();
             e.addField(field, getTextValue(element, field));
         }
         return e;
     }

     public StormweatherCommandConfig getSearchConfiguration() {
         return (StormweatherCommandConfig)super.getSearchConfiguration();
     }


     private String getTextValue(final Element ele, final String tagName) {
         String textVal = null;
         final NodeList nl = ele.getElementsByTagName(tagName);
         if(nl != null && nl.getLength() > 0) {
             final Element el = (Element)nl.item(0);
             textVal = el.getFirstChild().getNodeValue();
         }
         return textVal;
     }

     private Document doSearch(final String url) {

         Document doc = null;
         final String cacheKey = url;


         boolean updated = false; //cache flag used for eviction/update deadlock.

         try {

             // Get from the cache
             doc = (Document) ADMIN.getFromCache(cacheKey, EVICTIONPERIOD_WEATHER_CACHE);

         } catch (NeedsRefreshException nre) {

             try {

                 // Get from Storm service
                 doc = client.getXmlDocument(url);

                 // Store in the cache
                 ADMIN.putInCache(cacheKey, doc);

                 updated = true;

             } catch (Exception ex) {

                 // We have the outdated content for fail-over. May become stale!
                 doc = (Document) nre.getCacheContent();
                 LOG.error("Cache update exception, document may become stale! " + ex.getMessage());

             } finally{
                 if (!updated) {
                     // It is essential that cancelUpdate is called if the
                     // cached content could not be rebuilt
                     ADMIN.cancelUpdate(cacheKey);
                 }
             }

         }

         return doc;
     }

}
