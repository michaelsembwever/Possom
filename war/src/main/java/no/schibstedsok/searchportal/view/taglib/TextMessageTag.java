/*
 * TextMessageTag.java
 *
 * Created on May 29, 2006, 3:55 PM
 */

package no.schibstedsok.searchportal.view.taglib;

import java.io.IOException;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.view.i18n.TextMessages;

/**
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class TextMessageTag extends SimpleTagSupport {

    /**
     * Initialization of key property.
     */
    private String key;

    /**
     * Initialization of args property.
     */
    private Object args;
    
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
            final TextMessages text = (TextMessages)cxt.findAttribute("text");
            
            out.print(text.getMessage(key, args));
            
        } catch (IOException ex) {
            throw new JspException(ex.getMessage());
        }
        
    }

    /**
     * Setter for the key attribute.
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Setter for the args attribute.
     */
    public void setArgs(Object value) {
        this.args = value;
    }
}
