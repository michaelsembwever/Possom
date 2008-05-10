/* Copyright (2006-2007) Schibsted Søk AS
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
package no.sesat.search.result.handler;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;


/**
 * @version itthkjer
 * @version $Id$
 */
@Controller("FindFileFormat")
public final class FindFileFormatResultHandlerConfig extends AbstractResultHandlerConfig {

    private String field;
    private String urlField;

    /**
     *
     * @param field
     */
    public void setField(final String field){
        this.field = field;
    }

    /**
     *
     * @return
     */
    public String getField(){
        return field;
    }

    /**
     *
     * @param urlField
     */
    public void setUrlField(final String urlField){
        this.urlField = urlField;
    }

    /**
     *
     * @return
     */
    public String getUrlField(){
        return urlField;
    }

    @Override
    public FindFileFormatResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);
        AbstractDocumentFactory.fillBeanProperty(this, null, "field", ParseType.String, element, null);
        AbstractDocumentFactory.fillBeanProperty(this, null, "urlField", ParseType.String, element, "url");
        return this;
    }

}
