/* Copyright (2007) Schibsted Søk AS
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
/*
 * PlatefoodPPCCommandConfig.java
 *
 * Created on 24. august 2006, 10:06
 */

package no.sesat.search.mode.config;

import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 *
 * @author SSTHKJER
 * @version $Id$
 */
@Controller("PlatefoodPPCSearchCommand")
public class PlatefoodPpcCommandConfig extends AbstractYahooSearchConfiguration {

    private int resultsOnTop;


    /** @deprecated use views.xml instead **/
    public int getResultsOnTop() {
        return resultsOnTop;
    }

    /** @deprecated use views.xml instead **/
    public void setResultsOnTop(final int resultsOnTop) {
        this.resultsOnTop = resultsOnTop;
    }

    /**
     * Holds value of property url.
     */
    private String url;

    /**
     * Getter for property url.
     *
     * @return Value of property url.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Setter for property url.
     *
     * @param url New value of property url.
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    private String top3BackfillBlock;

    public String getTop3BackfillBlock() {
        return top3BackfillBlock;
    }

    public void setTop3BackfillBlock(String top3BackfillBlock) {
        this.top3BackfillBlock = top3BackfillBlock;
    }

    @Override
    public AbstractYahooSearchConfiguration readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit,
            final Context context) {

        super.readSearchConfiguration(element, inherit, context);

        AbstractDocumentFactory.fillBeanProperty(this, inherit, "url", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "resultsOnTop", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "top3BackfillBlock", ParseType.String, element, "");

        return this;
    }


}
