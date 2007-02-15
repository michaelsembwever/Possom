// Copyright (2006-2007) Schibsted Søk AS
/*
 * ImportVelocityTemplateTag.java
 *
 * Created on May 26, 2006, 3:17 PM
 */
package no.schibstedsok.searchportal.view.taglib;


import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.JspException;
import org.apache.log4j.Logger;


/** Imports (and merges) a velocity template from a site-config into the jsp. 
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class ImportVelocityTemplateTag extends AbstractImportVelocityTemplateTag {

    
    // Constants -----------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(ImportVelocityTemplateTag.class);
    
    
    // Attributes ----------------------------------------------------
    
    /**
     * Initialization of template property.
     */
    private String template;

    /**
     * Initialization of command property.
     */
    private String command;
    

    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------

    /**Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     */
    @Override
    public void doTag() throws JspException {
        
        final Map<String,Object> map = new HashMap<String,Object>();
        map.put("commandName", command != null ? command : this.template);
        importTemplate(this.template, map);

    }

    /**
     * Setter for the template attribute.
     */
    public void setTemplate(final String value) {
        this.template = value;
    }

    /**
     * Setter for the command attribute.
     */
    public void setCommand(final String value) {
        this.command = value;
    }
    
    
    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
