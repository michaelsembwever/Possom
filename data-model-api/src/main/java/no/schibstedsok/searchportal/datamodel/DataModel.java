// Copyright (2007) Schibsted Søk AS
/*
 * DataModel.java
 *
 * Created on 22 January 2007, 21:27
 *
 */

package no.schibstedsok.searchportal.datamodel;


import java.io.Serializable;
import java.util.Map;
import no.schibstedsok.searchportal.datamodel.generic.DataNode;
import static no.schibstedsok.searchportal.datamodel.access.ControlLevel.*;
import no.schibstedsok.searchportal.datamodel.access.AccessAllow;
import no.schibstedsok.searchportal.datamodel.junkyard.JunkYardDataObject;
import no.schibstedsok.searchportal.datamodel.query.QueryDataObject;
import no.schibstedsok.searchportal.datamodel.request.BrowserDataObject;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
import no.schibstedsok.searchportal.datamodel.search.SearchDataObject;
import no.schibstedsok.searchportal.datamodel.site.SiteDataObject;
import no.schibstedsok.searchportal.datamodel.user.UserDataObject;

/** The DataModel.
 * The root DataNode to the DataModel.
 *
 * There exists a general pattern through the DataModel that
 *  there exists both getter and setter methods to child dataObjects in dataNodes,
 *  but there exists *only* getter methods on non-dataNode dataObjects.
 * This allows dataObject, separated from their heirarchical context, to be immutable
 *  if the implementation so wishes. (The MapDataObject is an exception to this pattern).
 * 
 * The original design documentation is at
 *   https://dev.schibstedsok.no/confluence/display/TECHDEV/Search+Portal+DataModel+%28Sesam-3.0%29
 *
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataNode
public interface DataModel extends Serializable{

    /** The key to be used when the datamodel is to be stored, eg in the request or velocity context.
     */
    public static final String KEY = "DataModel";

    // ParametersDataObject ------------------------------------------------------------

    /**
     * 
     * @return 
     */
    ParametersDataObject getParameters();

    /**
     * 
     * @param parameters 
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION})
    void setParameters(ParametersDataObject parameters);

    // BrowserDataObject ------------------------------------------------------------

    /**
     * 
     * @return 
     */
    BrowserDataObject getBrowser();

    /**
     * 
     * @param browser 
     */
    @AccessAllow(DATA_MODEL_CONSTRUCTION)
    void setBrowser(BrowserDataObject browser);

    // UserDataObject ------------------------------------------------------------

    /**
     * 
     * @return 
     */
    UserDataObject getUser();

    /**
     * 
     * @param user 
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION})
    void setUser(UserDataObject user);

    // SiteDataObject ------------------------------------------------------------

    /**
     * 
     * @return 
     */
    SiteDataObject getSite();

    /**
     * 
     * @param site 
     */
    @AccessAllow(DATA_MODEL_CONSTRUCTION)
    void setSite(SiteDataObject site);

    // QueryDataObject ------------------------------------------------------------

    /**
     * 
     * @return 
     */
    QueryDataObject getQuery();

    /**
     * 
     * @param query 
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION, RUNNING_QUERY_CONSTRUCTION})
    void setQuery(QueryDataObject query);

    // SearchDataObject ------------------------------------------------------------
    
    /** Map containing all the search results. See SearchDataObject.
     * Keys match each search configuration's name.
     * When a search command adds it's finished SearchDataObject, see setSearch(..)
     *  it is expected to call notifyAll() on the map.
     * This enables others to use wait() on the map so to get access to the results once they are ready.
     * 
     * @return 
     */
    Map<String,SearchDataObject> getSearches();
    
//    /**
//     * 
//     * @param searches 
//     */
//    void setSearches(Map<String,SearchDataObject> searches);

    /**
     * 
     * @param key 
     * @return 
     */
    @AccessAllow(DATA_MODEL_CONSTRUCTION)
    SearchDataObject getSearch(final String key);

    /** Set a search command's finished results.
     * Any call to this method must be followed by the code:
     *  synchronized(getSearches()){ getSearches().notifyAll(); }
     *  to inform any other parties waiting for the results that they are ready.
     * 
     * @param key 
     * @param value 
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, SEARCH_COMMAND_EXECUTION})
    void setSearch(final String key, final SearchDataObject value);
    
    // JunkYardDataObject ------------------------------------------------------------

    /** @return 
     * @deprecated Provides access to datamodel elements not yet migrated into the DataModel proper.
     **/
    JunkYardDataObject getJunkYard();

    /**
     * 
     * @param junkYard 
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION})
    void setJunkYard(JunkYardDataObject junkYard);

}
