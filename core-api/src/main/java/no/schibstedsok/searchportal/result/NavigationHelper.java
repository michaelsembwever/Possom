/*
 * NavigationHelper.java
 *
 * Created on 12/06/2007, 17:13:43
 *
 */

package no.schibstedsok.searchportal.result;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.mode.NavigationConfig;
import org.apache.log4j.Logger;

/**
 * All public methods require take a datamodel argument. This datamodel must in VIEW_CONSTRUCTION state.
 * It essentially means that this helper class can only be used from jsp/velocity code.
 * XXX so maybe it should be moved into the view package?
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
    public static String getUrlFragment(
            final DataModel datamodel,
            final String navigator,
            final String value) {

        final NavigationConfig.Nav navEntry = getConfig(datamodel).getNavMap().get(navigator);
        return getUrlFragment(datamodel, navEntry, value, null);
    }

    /**
     * @param navEntry
     * @param value
     * @param navigatorName
     * @return
     */
    public static String getUrlFragment(
            final DataModel datamodel,
            final NavigationConfig.Nav navEntry,
            final String value,
            final String navigatorName) {

        final StringBuilder sb = new StringBuilder();
        String tab = navEntry.getTabByValue(value);
        if (tab == null) {
            tab = navEntry.getTab();
            if (tab == null) {
                tab = datamodel.getParameters().getValue("c").getUtf8UrlEncoded();
            }
        }
        sb.append("c=").append(tab);
        if (!navEntry.isExcludeQuery()) {
            sb.append("&q=").append(datamodel.getQuery().getUtf8UrlEncoded());
        }
        if (value != null && value.length() > 0) {
            sb.append('&').append(enc(navEntry.getField())).append('=').append(enc(value));
            if (navEntry.isRealNavigator() && navigatorName != null) {
                sb.append('&').append("nav_").append(enc(navEntry.getField())).append('=').append(enc(navigatorName));
            }
        }
        if (!navEntry.isOut()) {
            addParentFragment(datamodel, sb, navEntry);
            for (NavigationConfig.Navigation navigation : getConfig(datamodel).getNavigationList()) {
                if (navigation != navEntry.getNavigation()) {
                    addNavigationFragments(datamodel, navigation, sb, navEntry);
                }
            }
        }
        for (String key : navEntry.getStaticParameters().keySet()) {
            addFragment(sb, navEntry, key, navEntry.getStaticParameters().get(key));
        }
        return sb.toString();
    }
    
    public static NavigationConfig getConfig(final DataModel datamodel){

        return datamodel.getNavigation().getConfiguration();
    }
    
    // Constructors --------------------------------------------------
    // Public --------------------------------------------------------
    // Package protected ---------------------------------------------
    // Protected -----------------------------------------------------
    // Private -------------------------------------------------------

    private static void addNavigationFragments(
            final DataModel datamodel,
            final NavigationConfig.Navigation navigation,
            final StringBuilder sb,
            final NavigationConfig.Nav navEntry) {

        final Set<String> fieldFilterSet = new HashSet<String>();
        for (NavigationConfig.Nav nav : navigation.getNavList()) {
            addNavigationFragment(datamodel, fieldFilterSet, nav, sb, navEntry);
        }
    }

    private static void addNavigationFragment(
            final DataModel datamodel,
            final Set<String> fieldFilterSet,
            final NavigationConfig.Nav nav,
            final StringBuilder sb,
            final NavigationConfig.Nav navEntry) {

        StringDataObject fieldValue = datamodel.getParameters().getValue(nav.getField());
        if (!fieldFilterSet.contains(nav.getField())) {
            addPreviousField(datamodel, fieldValue, sb, navEntry, nav.getField());
            fieldFilterSet.add(nav.getField());
            for (String staticKey : nav.getStaticParameters().keySet()) {
                fieldValue = datamodel.getParameters().getValue(staticKey);
                if (!fieldFilterSet.contains(staticKey)) {
                    addPreviousField(datamodel, fieldValue, sb, navEntry, staticKey);
                }
            }
            if (nav.getChildNavs() != null) {
                for (NavigationConfig.Nav childNav : nav.getChildNavs()) {
                    addNavigationFragment(datamodel, fieldFilterSet, childNav, sb, navEntry);
                }
            }
        }
    }

    private static void addPreviousField(
            final DataModel datamodel,
            StringDataObject fieldValue,
            final StringBuilder sb,
            final NavigationConfig.Nav navEntry,
            final String fieldName) {

        if (fieldValue != null && addFragment(sb, navEntry, fieldName, fieldValue.getString())) {
            fieldValue = datamodel.getParameters().getValue("nav_" + fieldName);
            if (fieldValue != null) {
                addFragment(sb, navEntry, "nav_" + fieldName, fieldValue.getString());
            }
        }
    }

    private static boolean addFragment(

            final StringBuilder sb,final NavigationConfig.Nav nav,
            final String id,
            final String value) {

        if (!nav.getNavigation().getResetNavSet().contains(id)) {
            sb.append('&').append(enc(id)).append('=').append(enc(value));
            return true;
        }
        return false;
    }

    private static void addParentFragment(
            final DataModel datamodel,
            final StringBuilder sb,
            final NavigationConfig.Nav navEntry) {

        NavigationConfig.Nav parentNav = navEntry.getParentNav();
        if (parentNav != null) {
            StringDataObject fieldValue = datamodel.getParameters().getValue(parentNav.getField());
            addPreviousField(datamodel, fieldValue, sb, navEntry, parentNav.getField());
            addParentFragment(datamodel, sb, parentNav);
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
