/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
/*
 * ImportPublishingPageTag.java
 *
 * Created on May 27, 2006, 5:38 PM
 */

package no.sesat.search.view.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.xml.parsers.ParserConfigurationException;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.view.ImportPublish;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class PublishingPageTag extends AbstractVelocityTemplateTag {

    private static final Logger LOG = Logger.getLogger(PublishingPageTag.class);

    private String page;
    
    private String template;

    /**Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     */
    public void doTag() throws JspException {

        final PageContext cxt = (PageContext) getJspContext();
        final JspWriter out = cxt.getOut();

        try {

            final JspFragment f = getJspBody();
            if (f != null){
                f.invoke(out);
            }

            final DataModel datamodel = (DataModel) cxt.findAttribute(DataModel.KEY);
            
            if(null == template){
                out.write(ImportPublish.importPage(page, datamodel));
                
            }else{
                
                try {
                    
                    final Map<String,Object> map = new HashMap<String,Object>();
                    map.put("document", ImportPublish.importXml(page, datamodel));
                    importTemplate(template, map);

                } catch (ParserConfigurationException e) {
                    LOG.error("Failed to import " + page + ".html");
                } catch (SAXException e) {
                    LOG.error("Failed to import " + page + ".html");
                }
            }
            
        }catch(IOException e){
            LOG.error("Failed to import " + page + ".html");
        }

    }

    /**
     * Setter for the command attribute.
     */
    public void setPage(final String value) {
        this.page = value;
    }
    
    /** Setter for template to parse **/
    public void setTemplate(final String template){
        this.template = template;
    }
}
