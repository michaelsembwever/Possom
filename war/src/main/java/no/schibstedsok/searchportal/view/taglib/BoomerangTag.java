/*
 * LinkPulseTag.java
 *
 * Created on May 27, 2006, 5:55 PM
 */

package no.schibstedsok.searchportal.view.taglib;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.Boomerang;
import no.schibstedsok.searchportal.site.Site;

/**
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version
 */

public final class BoomerangTag extends SimpleTagSupport {

    /**
     * Initialization of url property.
     */
    private String url;

    /**
     * Initialization of param property.
     */
    private String param;

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
            final Site site = datamodel.getSite().getSite();

            out.print(Boomerang.getUrl(site, url, param));

        }catch(Exception e){
            throw new JspException(e);
        }

    }

    /**
     * Setter for the url attribute.
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Setter for the param attribute.
     */
    public void setParam(String value) {
        this.param = value;
    }

}
