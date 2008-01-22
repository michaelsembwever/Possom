/* Copyright (2006-2007) Schibsted Søk AS
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


/** Import's the SearchTab's main template from the appropriate layout.
 * Will use the "front" template if the layout defines it and the query is empty.
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
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
        final SearchTab tab = datamodel.getPage().getCurrentTab();
        final StringDataObject layoutDO = datamodel.getParameters().getValue(RunningQueryImpl.PARAM_LAYOUT);
        final Layout layout = null != cxt.getAttribute("layout") ? (Layout)cxt.getAttribute("layout") : null != layoutDO
                ? tab.getLayouts().get(layoutDO.getXmlEscaped())
                : tab.getDefaultLayout();
        cxt.setAttribute("layout", layout);

        final String front = null != layout.getFront() && 0 < layout.getFront().length()
                ? layout.getFront()
                : null;

        final String include = datamodel.getQuery().getQuery().isBlank() && null != front
                ? front
                : layout.getMain();

        if(null != include){

            final Map<String,Object> map = new HashMap<String,Object>();

            map.put("layout", layout);

            importTemplate(include.startsWith("/") ? include : PAGES_DIRECTORY + include, map);

        }else{
            // use the default httpDecorator.jsp
            cxt.setAttribute(MISSING, Boolean.TRUE);
        }

    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
