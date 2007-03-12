// Copyright (2006) Schibsted Søk AS
/*
 * ImportPublishingPageTag.java
 *
 * Created on May 27, 2006, 5:38 PM
 */

package no.schibstedsok.searchportal.view.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.view.ImportPublish;
import org.apache.log4j.Logger;

/**
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class ImportPublishingPageTag extends SimpleTagSupport {

    private static final Logger LOG = Logger.getLogger(ImportPublishingPageTag.class);

    /**
     * Initialization of page property.
     */
    private String page;

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
            ImportPublish.importPage(page, datamodel, out);
            
        }catch(IOException e){
            LOG.error("Failed to import pub" + page + ".html");
        }

    }

    /**
     * Setter for the command attribute.
     */
    public void setPage(final String value) {
        this.page = value;
    }
}
