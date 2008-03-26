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
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import no.sesat.search.site.config.TextMessages;

/** Wraps functionality found in TextMessages into a custom tag.
 *
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class TextMessageTag extends SimpleTagSupport implements DynamicAttributes{

    /**
     * Initialization of key property.
     */
    private String key;

    /**
     * Initialization of args property.
     */
    private final List<Object> args = new ArrayList<Object>();

    /**Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     * @throws javax.servlet.jsp.JspException
     */
    @Override
    public void doTag() throws JspException {

        final PageContext cxt = (PageContext) getJspContext();
        final JspWriter out = cxt.getOut();

        try {

            final JspFragment f = getJspBody();
            if (f != null){
                f.invoke(out);
            }
            final TextMessages text = (TextMessages)cxt.findAttribute("text");

            out.print(text.getMessage(key, args.toArray()));

        } catch (IOException ex) {
            throw new JspException(ex.getMessage());
        }

    }

    /**
     * Setter for the key attribute.
     * @param value
     */
    public void setKey(final String value) {
        this.key = value;
    }

    public void setDynamicAttribute(
            final String uri,
            final String localName,
            final Object value) throws JspException {

        assert localName.startsWith("arg") : "Only dynamic attributes of format argX are supported: " + localName;

        final int i = Integer.valueOf(localName.replaceAll("arg", ""));
        while(args.size() <= i){
            args.add("");
        }
        args.set(i, value);
    }


}
