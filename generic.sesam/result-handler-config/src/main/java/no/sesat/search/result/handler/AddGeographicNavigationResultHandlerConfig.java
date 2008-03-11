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
 */
package no.sesat.search.result.handler;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;

/** 
 * @deprecated Create a NavigationController instead.  SEARCH-3427
 */
@Controller("AddGeographicNavigationResultHandler")
public class AddGeographicNavigationResultHandlerConfig extends AbstractResultHandlerConfig {
    private String geoXml = "geographic.xml";

    public String getGeoXml() {
        return geoXml;
    }

    public void setGeoXml(String geoXml) {
        this.geoXml = geoXml;
    }


    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        String optAttr = element.getAttribute("geo-xml");
        if (optAttr != null && optAttr.length() > 0) {
            geoXml = optAttr;
        }
        return this;
    }
}
