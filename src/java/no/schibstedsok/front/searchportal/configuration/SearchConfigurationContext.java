/* Copyright (2005-2006) Schibsted Søk AS
 * SearchConfigurationContext.java
 *
 * Created on 23 February 2006, 14:42
 */

package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.common.ioc.BaseContext;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface SearchConfigurationContext extends BaseContext {
    SearchConfiguration getSearchConfiguration();
}
