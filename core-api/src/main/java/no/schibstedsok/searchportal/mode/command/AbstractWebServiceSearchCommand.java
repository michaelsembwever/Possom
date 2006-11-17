/*
 * AbstractWebServiceSearchCommand.java
 *
 * Created on May 30, 2006, 12:48 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;


import java.util.Map;

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
    public AbstractWebServiceSearchCommand(final Context cxt, final Map parameters) {
        super (cxt, parameters);
        
    }
    
    // Public --------------------------------------------------------
    
    // Z implementation ----------------------------------------------
    
    // AbstractSearchCommand overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}
