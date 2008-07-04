/* Copyright (2007-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * DataModel.java
 *
 * Created on 22 January 2007, 21:27
 *
 */

package no.sesat.search.datamodel;


import java.io.Serializable;
import java.util.Map;
import no.sesat.search.datamodel.generic.DataNode;
import static no.sesat.search.datamodel.access.ControlLevel.*;
import no.sesat.search.datamodel.access.AccessAllow;
import no.sesat.search.datamodel.access.AccessDisallow;
import no.sesat.search.datamodel.junkyard.JunkYardDataObject;
import no.sesat.search.datamodel.navigation.NavigationDataObject;
import no.sesat.search.datamodel.page.PageDataObject;
import no.sesat.search.datamodel.query.QueryDataObject;
import no.sesat.search.datamodel.request.BrowserDataObject;
import no.sesat.search.datamodel.request.ParametersDataObject;
import no.sesat.search.datamodel.search.SearchDataObject;
import no.sesat.search.datamodel.site.SiteDataObject;
import no.sesat.search.datamodel.user.UserDataObject;

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
 *   https://dev.sesat.no/confluence/display/TECHDEV/Search+Portal+DataModel+%28Sesam-3.0%29
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

    /** Get the ParametersDataObject associated to this request.
     *
     * @return the ParametersDataObject updated for this request.
     */
    ParametersDataObject getParameters();

    /** Update the ParametersDataObject.
     *
     * @param parameters ParametsDataObject associated to this request.
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION})
    void setParameters(ParametersDataObject parameters);

    // BrowserDataObject ------------------------------------------------------------

    /** Get the BrowserDataObject associated to this session.
     *
     * @return the BrowserDataObject created at the beginning of the session.
     */
    BrowserDataObject getBrowser();

    /** Assign the BrowserDataObject. Can only be called once.
     *
     * @param browser BrowserDataObject holding session-static information.
     */
    @AccessAllow(DATA_MODEL_CONSTRUCTION)
    void setBrowser(BrowserDataObject browser);

    // UserDataObject ------------------------------------------------------------

    /** Get the UserDataObject holding the user information.
     *
     * @return the UserDataObject.
     */
    UserDataObject getUser();

    /** Assign the UserDataObject when user logs in.
     *
     * @param user UserDataObject holding user information.
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION})
    void setUser(UserDataObject user);

    // SiteDataObject ------------------------------------------------------------

    /** Get the SiteDataObject holding site and skin information.
     *
     * @return the SiteDataObject
     */
    SiteDataObject getSite();

    /** Assign the SiteDataObject.
     * Usually only done once but some browsers don't use separate sessions for sub-domains.
     *
     * @param site the SiteDataObject holding site and skin information.
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION})
    void setSite(SiteDataObject site);

    // QueryDataObject ------------------------------------------------------------

    /** The QueryDataObject holding all query related information associated to this request.
     *
     * @return the QueryDataObject
     */
    QueryDataObject getQuery();

    /** Set the QueryDataObject associated to this request.
     *
     * @param query the QueryDataObject holding all query related information
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
    @AccessDisallow({RUNNING_QUERY_CONSTRUCTION, SEARCH_COMMAND_CONSTRUCTION})
    Map<String,SearchDataObject> getSearches();

    /** Get the SearchDataObject holding the given search's results
     *
     * @param key the search (commandName) to get SearchDataObject for
     * @return the SearchDataObject
     */
    @AccessAllow({SEARCH_COMMAND_EXECUTION, RUNNING_QUERY_HANDLING, VIEW_CONSTRUCTION})
    SearchDataObject getSearch(final String key);

    /** Set a search command's finished results.
     * Any call to this method must be followed by the code:
     *  synchronized(getSearches()){ getSearches().notifyAll(); }
     *  to inform any other parties waiting for the results that they are ready.
     *
     * @param key
     * @param value
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION, SEARCH_COMMAND_EXECUTION, RUNNING_QUERY_HANDLING})
    void setSearch(final String key, final SearchDataObject value);

    // NavigationDataObject ------------------------------------------------------------

    /** Get the NavigationDataObject associated to the SearchMode.
     *
     * @return the NavigationDataObject
     */
    NavigationDataObject getNavigation();

    /** Set the NavigationDataObject.
     * @param value the NavigationDataObject associated to the SearchMode
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, RUNNING_QUERY_CONSTRUCTION})
    void setNavigation(final NavigationDataObject value);

    // PageDataObject ------------------------------------------------------------

    /** Get the PageDataObject associated to the SearchTab.
     *
     * @return the PageDataObject
     */
    PageDataObject getPage();

    /** Set the PageDataObject.
     * @param value the PageDataObject associated to the SearchTab
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION})
    void setPage(final PageDataObject value);

    // JunkYardDataObject ------------------------------------------------------------

    /** @return all the old crap
     * @deprecated Provides access to datamodel elements not yet migrated into the DataModel proper.
     **/
    JunkYardDataObject getJunkYard();

    /**
     *
     * @param junkYard
     * @deprecated  Provides access to datamodel elements not yet migrated into the DataModel proper.
     */
    @AccessAllow({DATA_MODEL_CONSTRUCTION, REQUEST_CONSTRUCTION})
    void setJunkYard(JunkYardDataObject junkYard);

}
