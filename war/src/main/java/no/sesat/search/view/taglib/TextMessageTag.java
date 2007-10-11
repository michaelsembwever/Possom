/* Copyright (2007) Schibsted Søk AS
 *   This file is part of SESAT.
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
 * TextMessageTag.java
 *
 * Created on May 29, 2006, 3:55 PM
 */

package no.sesat.search.view.taglib;

import java.io.IOException;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import no.sesat.search.site.Site;
import no.sesat.search.site.config.TextMessages;

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
