/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractRunningQuery.java
 *
 * Created on 16 February 2006, 19:49
 *
 */

package no.schibstedsok.front.searchportal.query.run;

import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.SearchTabsCreator;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractRunningQuery implements RunningQuery {

    

    protected final Context context;

    /** Creates a new instance of AbstractRunningQuery */
    protected AbstractRunningQuery(final Context cxt) {
        context = cxt;
    }




}
