/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 * ImportVelocityTemplateTag.java
 *
 * Created on May 26, 2006, 3:17 PM
 */
package no.sesat.search.view.taglib;


import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.view.config.SearchTab.Layout;
import org.apache.log4j.Logger;


/** Imports (and merges) a velocity template from a site-config into the jsp.
 *
 *
 * @version $Id$
 */

public final class VelocityTemplateTag extends AbstractVelocityTemplateTag {


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(VelocityTemplateTag.class);


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
     * @throws javax.servlet.jsp.JspException
     */
    @Override
    public void doTag() throws JspException {
        final Layout layout = findLayout((PageContext)getJspContext());
        final Map<String,Object> map = new HashMap<String,Object>();
        map.put("layout", layout);
        map.put("commandName", command != null ? command : this.template);
        importVelocity(this.template, map);
    }

    /**
     * Setter for the template attribute.
     * @param value
     */
    public void setTemplate(final String value) {
        this.template = value;
    }

    /**
     * Setter for the command attribute.
     * @param value
     */
    public void setCommand(final String value) {
        this.command = value;
    }


    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
