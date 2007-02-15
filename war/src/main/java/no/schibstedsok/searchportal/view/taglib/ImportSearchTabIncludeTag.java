// Copyright (2006-2007) Schibsted Søk AS
/*
 * ImportSearchTabIncludeTag.java
 *
 * Created on May 26, 2006, 3:17 PM
 */
package no.schibstedsok.searchportal.view.taglib;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.util.PagingDisplayHelper;
import no.schibstedsok.searchportal.view.config.SearchTab;
import org.apache.log4j.Logger;


/** Imports (and merges) a velocity template from a site-config into the jsp according to the SearchTab's includes. 
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class ImportSearchTabIncludeTag extends AbstractImportVelocityTemplateTag {

    
    // Constants -----------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(ImportSearchTabIncludeTag.class);
    
    private static final String LAYOUT_DIRECTORY = "/fragments/layout/";
    
    
    // Attributes ----------------------------------------------------
    
    /**
     * Initialization of template property.
     */
    private String include;
    

    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------

    /**
     * Setter for the template attribute.
     */
    public void setInclude(final String include) {
        this.include = include;
    }
    
    /**Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     */
    @Override
    public void doTag() throws JspException {
        
        final PageContext cxt = (PageContext) getJspContext();
        final SearchTab tab = (SearchTab) cxt.findAttribute("tab");
        final String template = LAYOUT_DIRECTORY + tab.getLayout().getInclude(include);
        try{
            cxt.getOut().println("<!-- " + include + " -->");
        }catch(IOException ioe){
            LOG.warn("Failed to write include comment", ioe);
        }
        
        final Map<String,Object> map = new HashMap<String,Object>();
        
        // HACK the pager until the datamodel provides methods to access "paging" commands in the current mode.
        map.put("commandName", tab.getLayout().getOrigin());
//        map.put("pager", 
//                ((Map<String,PagingDisplayHelper>)cxt.findAttribute("pagers")).get(tab.getLayout().getOrigin()));
//        map.put("result", 
//                ((Map<String,SearchResult>)cxt.findAttribute("results")).get(tab.getLayout().getOrigin()));
        // end-HACK
        
        importTemplate(template, map);

    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
