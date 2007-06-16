// Copyright (2006) Schibsted Søk AS
/*
 * ImportPublishingPageTag.java
 *
 * Created on May 27, 2006, 5:38 PM
 */

package no.schibstedsok.searchportal.view.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.view.ImportPublish;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class ImportPublishingPageTag extends AbstractImportVelocityTemplateTag {

    private static final Logger LOG = Logger.getLogger(ImportPublishingPageTag.class);

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
