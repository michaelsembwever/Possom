/*
 * LinkPulseTag.java
 *
 * Created on May 27, 2006, 5:55 PM
 */

package no.schibstedsok.searchportal.view.taglib;

import java.util.Properties;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import no.schibstedsok.searchportal.mode.config.SiteConfiguration;
import no.schibstedsok.searchportal.result.Linkpulse;
import no.schibstedsok.searchportal.site.Site;

/**
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version
 */

public final class LinkPulseTag extends SimpleTagSupport {

    /**
     * Initialization of url property.
     */
    private String url;

    /**
     * Initialization of param property.
     */
    private String param;

    /**
     * Initialization of script property.
     */
    private String script = "sgo";

    /**
     * Initialization of index property.
     */
    private String index;
    
    /**Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     */
    public void doTag() throws JspException {
        
        final PageContext cxt = (PageContext) getJspContext();
        final JspWriter out = cxt.getOut();
        
        try {
            
            final JspFragment f=getJspBody();
            if (f != null){ 
                f.invoke(out);
            }
            
            final Site site = (Site) cxt.findAttribute(Site.NAME_KEY);
            final Properties props = SiteConfiguration.valueOf(site).getProperties();
            final Linkpulse linkpulse = new Linkpulse(site, props);
            
            out.print(linkpulse.getUrl(url, param, script, index));
            
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

    /**
     * Setter for the script attribute.
     */
    public void setScript(String value) {
        this.script = value;
    }

    /**
     * Setter for the index attribute.
     */
    public void setIndex(String value) {
        this.index = value;
    }
}
