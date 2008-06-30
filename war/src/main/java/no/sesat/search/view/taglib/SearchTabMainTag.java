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
 * SearchTabMainTag.java
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
import no.sesat.search.view.config.SearchTab.Layout;

import org.apache.log4j.Logger;


/** Import's the SearchTab's main template from the appropriate layout.
 * Will use the "front" template if the layout defines it and the query is empty.
 *
 * The template may be either a velocity template or a JavaServer page.
 * If the extension is not specified it defaults to ".vm".
 *
 * A relative path is relative to templates/pages/
 *
 *
 * @version $Id$
 */

public final class SearchTabMainTag extends AbstractVelocityTemplateTag {


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(SearchTabMainTag.class);
    private static final String MISSING = "Missing_SearchTabMain_Template";

    private static final String PAGES_DIRECTORY = "/pages/";



    // Attributes ----------------------------------------------------


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public -------------------------------------------

    /** Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     *
     * Calling this tag also has the side effect of setting the layout in use into the context's attributes.
     *
     * @throws javax.servlet.jsp.JspException
     */
    @Override
    public void doTag() throws JspException {

        final PageContext cxt = (PageContext) getJspContext();
        final DataModel datamodel = (DataModel) cxt.findAttribute(DataModel.KEY);
        final Layout layout = findLayout(datamodel);

        final String front = null != layout.getFront() && 0 < layout.getFront().length()
                ? layout.getFront()
                : null;

        String include = datamodel.getQuery() != null && datamodel.getQuery().getQuery().isBlank() && null != front
                ? front
                : layout.getMain();

        include = include.startsWith("/")
                ? include
                : PAGES_DIRECTORY + include;

        try{
            if(null != include){

                final Map<String,Object> map = new HashMap<String,Object>();
                map.put("layout", layout);

                if(layout.getContentType() != null) {
                    cxt.getResponse().setContentType(layout.getContentType());
                }

                if(include.endsWith(".jsp")){

                    forwardJsp(include);

                }else if(include.endsWith(".vm")){

                    importVelocity(include, map);

                }else{
                    // legacy
                    importVelocity(include, map);
                }

            }
            if(null == include
                    || Boolean.TRUE == cxt.getAttribute("Missing_" + include.replaceAll("/","") + "_Template")){

                LOG.error(MISSING);
                cxt.getOut().println(MISSING);
                cxt.setAttribute(MISSING, Boolean.TRUE);
            }
        }catch(IOException ioe){
            throw new JspException(ioe);
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
