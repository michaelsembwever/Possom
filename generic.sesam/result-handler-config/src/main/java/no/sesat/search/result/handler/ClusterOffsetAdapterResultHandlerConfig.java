/* Copyright (2007-2008) Schibsted ASA
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
import no.sesat.search.view.navigation.ResultPagingNavigationConfig;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 *
 * @version $Id$
 */
@Controller("ClusterOffsetAdapter")
public class ClusterOffsetAdapterResultHandlerConfig extends AbstractResultHandlerConfig {
    private static final Logger LOG = Logger.getLogger(ClusterOffsetAdapterResultHandlerConfig.class);
    private String offsetField = ResultPagingNavigationConfig.OFFSET_KEY;
    private String offsetResultField = "nextOffset";
    private int offsetInterval = 20;

    public String getOffsetField() {
        return offsetField;
    }

    public String getOffsetResultField() {
        return offsetResultField;
    }

    public int getOffsetInterval() {
        return offsetInterval;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        String optionalParameter = element.getAttribute("offset-field");
        if (optionalParameter != null && optionalParameter.length() > 0) {
            offsetField = optionalParameter;
        }
        optionalParameter = element.getAttribute("offset-result-field");
        if (optionalParameter != null && optionalParameter.length() > 0) {
            offsetResultField = optionalParameter;
        }
        optionalParameter = element.getAttribute("offset-interval");
        if (optionalParameter != null && optionalParameter.length() > 0) {
            try {
                offsetInterval = Integer.parseInt(optionalParameter);
            } catch (NumberFormatException e) {
                LOG.error("Could not parse offsetInterval", e);
            }
        }
        return this;
    }
}
