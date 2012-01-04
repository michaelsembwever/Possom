/*
 * Copyright (2012) Schibsted ASA
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
 */

package no.sesat.search.run.handler;

import no.sesat.search.run.handler.AbstractRunHandlerConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/** Copy a SearchDataObject giving the new copy a new name.
 * $Id$
 */
@Controller("CopySearchRunHandler")
public final class CopySearchRunHandlerConfig implements RunHandlerConfig {

    private String from, to;

    public CopySearchRunHandlerConfig() {}

    public String getFrom(){
        return from;
    }

    public void setFrom(final String from){
        this.from = from;
    }

    public String getTo(){
        return to;
    }

    public void setTo(final String to){
        this.to = to;
    }

    public RunHandlerConfig readRunHandler(final Element element) {


        AbstractDocumentFactory.fillBeanProperty(this, null, "from", ParseType.String, element, "defaultSearch");
        AbstractDocumentFactory.fillBeanProperty(this, null, "to", ParseType.String, element, "defaultSearchCopy");

        return this;
    }
}
