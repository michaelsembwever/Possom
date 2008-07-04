/*
 * Copyright (2008) Schibsted Søk AS
 * This file is part of SESAT.
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
 */

package no.sesat.search.run.handler;

import no.sesat.search.run.handler.AbstractRunHandlerConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/** Generic RunHandler that can be configured to merge result lists together
 * $Id$
 */
@Controller("FederatorRunHandler")
public final class FederatorRunHandlerConfig implements RunHandlerConfig {

    public enum Blend {
        /** Insert all results from first command, then all result from second command, and so on. **/
        SEQUEL,
        /** Insert first result from first command, then first from second, etc, then second from first, etc.
         * not implemented yet.
         **/
        ROBIN,
        /** Random mixture of command result's inserted. Results from the same command are still ordered.
         * not implemented yet.
         **/
        RANDOM
    };

    private String from[], to;

    private int insertPosition, insertCount;

    private Blend blend;

    public FederatorRunHandlerConfig() {}

    public String[] getFrom(){
        return from;
    }

    public String getTo(){
        return to;
    }

    public void setTo(final String to){
        this.to = to;
    }

    public int getInsertPosition(){
        return insertPosition;
    }

    public void setInsertPosition(final int pos){
        insertPosition = pos;
    }

    public int getInsertCount(){
        return insertCount;
    }

    public void setInsertCount(final int count){
        insertCount = count;
    }

    public Blend getBlend(){
        return blend;
    }

    public RunHandlerConfig readRunHandler(final Element element) {


        from = element.getAttribute("from").split(",");
        AbstractDocumentFactory.fillBeanProperty(this, null, "to", ParseType.String, element, "defaultSearch");
        AbstractDocumentFactory.fillBeanProperty(this, null, "insertPosition", ParseType.Int, element, "0");
        AbstractDocumentFactory.fillBeanProperty(this, null, "insertCount", ParseType.Int, element, "1");
        blend = Blend.valueOf(element.getAttribute("blend").toUpperCase());

        return this;
    }
}
