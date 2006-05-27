/*
 * AbstractDocumentFactory.java
 *
 * Created on 21. april 2006, 13:17
 *
 */

package no.schibstedsok.front.searchportal.util.config;

import no.schibstedsok.front.searchportal.configuration.loader.PropertiesContext;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractDocumentFactory {
    
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of AbstractDocumentFactory */
    public AbstractDocumentFactory() {
    }
    
    // Public --------------------------------------------------------
    
    // Z implementation ----------------------------------------------
    
    // Y overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    
    protected static final boolean parseBoolean(final String s, final boolean def){
        return s.trim().length() == 0 ? def : Boolean.parseBoolean(s);
    }
    
    protected static final float parseFloat(final String s, final float def){
        return s.trim().length() == 0 ? def : Float.parseFloat(s);
    }
    
    protected static final int parseInt(final String s, final int def){
        return s.trim().length() == 0 ? def : Integer.parseInt(s);
    }
    
    protected static final String parseString(final String s, final String def){
        return s.trim().length() == 0 ? def : s;
    }
    

    
    // Inner classes -------------------------------------------------
    
}
