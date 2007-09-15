/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * NavigationHelper.java
 *
 * Created on 12/06/2007, 17:13:43
 *
 */

package no.sesat.search.view.navigation;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.result.NavigationItem;
import no.sesat.search.result.BasicNavigationItem;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * All public methods require take a datamodel argument. This datamodel must in VIEW_CONSTRUCTION state.
 * It essentially means that this helper class can only be used from jsp/velocity code.
 * 
 * @author Geir H. Pettersen(T-Rank)
 * @author mick
 * @version $Id$
 */
public final class NavigationHelper {

    // Constants -----------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(NavigationHelper.class);

    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    /**
     * Gets the url fragment you should use when navigating on this value.
     *
     * @param navigator the navigator you are using
     * @param value     the specific field you are using
     * @return a url fragemt to use
     */
    public static String getUrlParameters(
            final DataModel datamodel,
            final String navigator,
            final String value) {

        final NavigationConfig.Nav navEntry = getConfig(datamodel).getNavMap().get(navigator);
        return getUrlFragment(getUrlParameters(datamodel, navEntry, value, null));
    }

    public static String getUrlFragment(
            final DataModel datamodel,
            final String navigator,
            final String value) {

        final NavigationConfig.Nav navEntry = getConfig(datamodel).getNavMap().get(navigator);
        return getUrlFragment(getUrlParameters(datamodel, navEntry, value, null));
    }

    public static String getUrlFragment(
            final DataModel datamodel,
            final NavigationConfig.Nav navEntry,
            final String value,
            final String navigatorName) {

        return getUrlFragment(getUrlParameters(datamodel, navEntry, value, navigatorName));
    }

    public static String removeQuery(final String url) {
        return url.replaceAll("(&amp;)?q=[^&]*", ""); 
    }

    /**
     * @param navEntry
     * @param value
     * @param navigatorName
     * @return
     */
    public static Map<String, String> getUrlParameters(
            final DataModel datamodel,
            final NavigationConfig.Nav navEntry,
            final String value,
            final String navigatorName) {

        final Map<String, String> parameters = new HashMap<String, String>();

        if (navEntry.getTab() != null) {
            addParameter(parameters, "c", navEntry.getTab());
        }

        if (!navEntry.isExcludeQuery()) {
            addParameter(parameters, "q", datamodel.getQuery().getUtf8UrlEncoded());
        }
        if (value != null && value.length() > 0) {
            addParameter(parameters, enc(navEntry.getField()), enc(value));
        }
        if (!navEntry.isOut()) {
            addParentFragment(datamodel, parameters, navEntry);
            for (NavigationConfig.Navigation navigation : getConfig(datamodel).getNavigationList()) {
                if (navigation != navEntry.getNavigation()) {
                    addNavigationFragments(datamodel, navigation, parameters, navEntry);
                }
            }
        }
        for (String key : navEntry.getStaticParameters().keySet()) {
            addFragment(parameters, navEntry, key, navEntry.getStaticParameters().get(key));
        }

        if (!parameters.containsKey("c")) {
            addParameter(parameters, "c", datamodel.getParameters().getValue("c").getUtf8UrlEncoded());
        }

        return parameters;
    }

    public static String getUrlFragment(final Map<String, String> parameters) {
        final StringBuilder builder = new StringBuilder();

        for (final String parameterName : parameters.keySet()) {
            builder.append(parameterName).append("=").append(parameters.get(parameterName));
            builder.append("&amp;");
        }

        return builder.toString().substring(0, builder.lastIndexOf("&"));
    }

    private static void addParameter(final Map parameters, final String parameter, final String value) {
        parameters.put(parameter, value);
    }

    public static NavigationConfig getConfig(final DataModel datamodel){

        return datamodel.getNavigation().getConfiguration();
    }

    public static NavigationConfig.Nav getFirstNotSelected(DataModel dm, NavigationConfig.Nav nav) {
        
        if (dm.getParameters().getValue(nav.getId()) != null
                && !nav.getChildNavs().isEmpty()
                && !nav.getChildNavs().get(0).isVirtual()) {
            
            return getFirstNotSelected(dm, nav.getChildNavs().get(0));
            
        } else {
            
            final int navResultSize = null != nav.getId() 
                    && null != dm.getNavigation().getNavigation(nav.getId())
                    && null != dm.getNavigation().getNavigation(nav.getId()).getResults()
                    ? dm.getNavigation().getNavigation(nav.getId()).getResults().size()
                    : 0;
                    
            return 1 == navResultSize && !nav.getChildNavs().isEmpty()
                    ? getFirstNotSelected(dm, nav.getChildNavs().get(0))
                    : nav;
        }
    }

    public static NavigationItem getSingleNavigationItem(DataModel dm, final String navId, final String value) {
        final NavigationItem item = dm.getNavigation().getNavigation(navId);
        return item != null ? item.getChildByTitle(value) : new BasicNavigationItem();
    }

    // Constructors --------------------------------------------------
    // Public --------------------------------------------------------
    // Package protected ---------------------------------------------
    // Protected -----------------------------------------------------
    // Private -------------------------------------------------------

    private static void addNavigationFragments(
            final DataModel datamodel,
            final NavigationConfig.Navigation navigation,
            final Map parameters,
            final NavigationConfig.Nav navEntry) {

        final Set<String> fieldFilterSet = new HashSet<String>();
        for (NavigationConfig.Nav nav : navigation.getNavList()) {
            addNavigationFragment(datamodel, fieldFilterSet, nav, parameters, navEntry);
        }
    }

    private static void addNavigationFragment(
            final DataModel datamodel,
            final Set<String> fieldFilterSet,
            final NavigationConfig.Nav nav,
            final Map parameters,
            final NavigationConfig.Nav navEntry) {

        StringDataObject fieldValue = datamodel.getParameters().getValue(nav.getField());
        if (!fieldFilterSet.contains(nav.getField())) {
            if (! nav.isOut()) {
                addPreviousField(datamodel, fieldValue, parameters, navEntry, nav.getField());
            }
            fieldFilterSet.add(nav.getField());
            for (String staticKey : nav.getStaticParameters().keySet()) {
                fieldValue = datamodel.getParameters().getValue(staticKey);
                if (!fieldFilterSet.contains(staticKey)) {
                    addPreviousField(datamodel, fieldValue, parameters, navEntry, staticKey);
                }
            }
            for (NavigationConfig.Nav childNav : nav.getChildNavs()) {
                addNavigationFragment(datamodel, fieldFilterSet, childNav, parameters, navEntry);
            }
        }
    }

    private static void addPreviousField(
            final DataModel datamodel,
            StringDataObject fieldValue,
            final Map parameters,
            final NavigationConfig.Nav navEntry,
            final String fieldName) {

        if (fieldValue != null) {
            addFragment(parameters, navEntry, fieldName, fieldValue.getString());
        }
    }

    private static boolean addFragment(

            final Map parameters, final NavigationConfig.Nav nav,
            final String id,
            final String value) {

        if (!nav.getNavigation().getResetNavSet().contains(id)) {
            addParameter(parameters, enc(id), enc(value));
            return true;
        }
        return false;
    }

    private static void addParentFragment(
            final DataModel datamodel,
            final Map parameters,
            final NavigationConfig.Nav navEntry) {

        NavigationConfig.Nav parentNav = navEntry.getParent();
        if (parentNav != null) {
            StringDataObject fieldValue = datamodel.getParameters().getValue(parentNav.getField());
            addPreviousField(datamodel, fieldValue, parameters, navEntry, parentNav.getField());
            addParentFragment(datamodel, parameters, parentNav);
        }
    }

    private static String enc(final String str) {

        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.fatal("UTF-8 encoding not available");
        }
        return str;
    }
    
    // Inner classes -------------------------------------------------
}
