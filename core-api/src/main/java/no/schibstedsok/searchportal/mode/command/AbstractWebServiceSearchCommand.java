/*
 * AbstractWebServiceSearchCommand.java
 *
 * Created on May 30, 2006, 12:48 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;


import no.schibstedsok.searchportal.datamodel.DataModel;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractWebServiceSearchCommand extends AbstractSearchCommand{


    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of AbstractWebServiceSearchCommand
     */
    public AbstractWebServiceSearchCommand(
            final Context cxt,
            final DataModel datamodel) {

        super(cxt, datamodel);

    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // AbstractSearchCommand overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
