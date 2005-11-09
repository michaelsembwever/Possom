package no.schibstedsok.front.searchportal.configuration;

import java.util.Properties;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface SearchTabsCreator {
    SearchTabs createSearchTabs();
    Properties getProperties();
}
