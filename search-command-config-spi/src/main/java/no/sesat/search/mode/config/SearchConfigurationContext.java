/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 * SearchConfigurationContext.java
 *
 * Created on 23 February 2006, 14:42
 */

package no.sesat.search.mode.config;

import no.schibstedsok.commons.ioc.BaseContext;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface SearchConfigurationContext extends BaseContext {
    /**
     * 
     * @return 
     */
    SearchConfiguration getSearchConfiguration();
}
