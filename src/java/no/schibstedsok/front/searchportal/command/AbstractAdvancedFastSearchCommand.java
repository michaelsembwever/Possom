/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractAdvancedFastSearchCommand.java
 *
 * Created on 14 March 2006, 19:51
 *
 */

package no.schibstedsok.front.searchportal.command;

import java.util.Map;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractAdvancedFastSearchCommand extends AbstractSearchCommand {

    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of AbstractAdvancedFastSearchCommand */
    public AbstractAdvancedFastSearchCommand(
                    final Context cxt,
                    final Map parameters) {

        super(cxt, parameters);
        throw new UnsupportedOperationException("AbstractAdvancedFastSearchCommand has not been implemented yet.");
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
