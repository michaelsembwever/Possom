// Copyright (2007) Schibsted Søk AS
/*
 * DataModel.java
 *
 * Created on 22 January 2007, 21:27
 *
 */

package no.schibstedsok.searchportal.datamodel;


import java.io.Serializable;
import no.schibstedsok.searchportal.datamodel.generic.DataNode;
import static no.schibstedsok.searchportal.datamodel.access.ControlLevel.*;
import no.schibstedsok.searchportal.datamodel.access.AccessAllow;
import no.schibstedsok.searchportal.datamodel.junkyard.JunkYardDataObject;
import no.schibstedsok.searchportal.datamodel.query.QueryDataObject;
import no.schibstedsok.searchportal.datamodel.request.BrowserDataObject;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
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
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataNode
public interface DataModel extends Serializable{

    public static final String KEY = "DataModel";

    // ParametersDataObject ------------------------------------------------------------

    ParametersDataObject getParameters();

    @AccessAllow(REQUEST_CONSTRUCTION)
    void setParameters(ParametersDataObject parameters);

    // BrowserDataObject ------------------------------------------------------------

    BrowserDataObject getBrowser();

    @AccessAllow(DATA_MODEL_CONSTRUCTION)
    void setBrowser(BrowserDataObject browser);

    // UserDataObject ------------------------------------------------------------

    UserDataObject getUser();

    @AccessAllow(REQUEST_CONSTRUCTION)
    void setUser(UserDataObject user);

    // SiteDataObject ------------------------------------------------------------

    SiteDataObject getSite();

    @AccessAllow(DATA_MODEL_CONSTRUCTION)
    void setSite(SiteDataObject site);

    // QueryDataObject ------------------------------------------------------------

    QueryDataObject getQuery();

    @AccessAllow({REQUEST_CONSTRUCTION, RUNNING_QUERY_CONSTRUCTION})
    void setQuery(QueryDataObject query);

    // JunkYardDataObject ------------------------------------------------------------

    /** @deprecated Provides access to datamodel elements not yet migrated into the DataModel proper.**/
    JunkYardDataObject getJunkYard();

    @AccessAllow(REQUEST_CONSTRUCTION)
    void setJunkYard(JunkYardDataObject junkYard);

}
