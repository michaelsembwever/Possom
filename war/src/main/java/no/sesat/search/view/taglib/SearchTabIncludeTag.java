/* Copyright (2006-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * ImportSearchTabIncludeTag.java
 *
 * Created on May 26, 2006, 3:17 PM
 */
package no.sesat.search.view.taglib;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.run.RunningQueryImpl;
import no.sesat.search.view.config.SearchTab;
import no.sesat.search.view.config.SearchTab.Layout;
import org.apache.log4j.Logger;


/** Imports (and merges) a velocity template from a site-config into the jsp according to the SearchTab's includes. 
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class SearchTabIncludeTag extends AbstractVelocityTemplateTag {

    
    // Constants -----------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(SearchTabIncludeTag.class);
    
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
     * @param include 
     */
    public void setInclude(final String include) {
        this.include = include;
    }
    
    /**Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     * @throws javax.servlet.jsp.JspException
     * @throws java.io.IOException 
     */
    @Override
    public void doTag() throws JspException, IOException {
        
        final PageContext cxt = (PageContext) getJspContext();
        final Layout layout = findLayout((DataModel) cxt.findAttribute(DataModel.KEY));
        
        if(null != layout.getInclude(include) && layout.getInclude(include).length() > 0 ){
            
            final String template = LAYOUT_DIRECTORY + layout.getInclude(include);
            try{
                cxt.getOut().println("<!-- " + include + " -->");
            }catch(IOException ioe){
                LOG.warn("Failed to write include comment", ioe);
            }

            final Map<String,Object> map = new HashMap<String,Object>();

            map.put("layout", layout);
            // HACK the pager until the datamodel provides methods to access "paging" commands in the current mode.
            map.put("commandName", layout.getOrigin());
            // end-HACK

            importTemplate(template, map);

        }else{
            // could not find include
            cxt.getOut().write("<!-- " + include + " not found -->");
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
