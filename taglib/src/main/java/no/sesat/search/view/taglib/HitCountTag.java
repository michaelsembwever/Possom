/* Copyright (2012) Schibsted ASA
 *   This file is part of Possom.
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
 *
 * HitCountTag.java
 */

package no.sesat.search.view.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.HitCount;
import no.sesat.search.site.Site;

/** SimpleTagSupport around the HitCount utility class.
 *
 *
 * @version $Id$
 */
public final class HitCountTag extends SimpleTagSupport {

    private int hitcount;

    /**
    * Called by the container to invoke this tag.
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

            final DataModel datamodel = (DataModel) cxt.findAttribute(DataModel.KEY);
            final Site site = datamodel.getSite().getSite();

            out.print(HitCount.present(hitcount, site.getLocale()));

        }catch(IOException e){
            throw new JspException(e);
        }

    }

    public void setHitcount(int hitcount) {
        this.hitcount = hitcount;
    }
}