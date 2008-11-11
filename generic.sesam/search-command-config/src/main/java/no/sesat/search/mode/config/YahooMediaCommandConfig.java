/* Copyright (2007-2008) Schibsted Søk AS
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
package no.sesat.search.mode.config;

import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.mode.config.querybuilder.PrefixQueryBuilderConfig;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 * Search configuration for the Yahoo! media search.
 *
 * @version $Id$
 */
@Controller("YahooMediaSearchCommand")
public final class YahooMediaCommandConfig extends AbstractYahooSearchConfiguration {

    /**
     *
     */
    public final static String DEFAULT_OCR = "yes";
    /**
     *
     */
    public final static String DEFAULT_CATALOG = "image";

    private String catalog = DEFAULT_CATALOG;
    private String ocr = DEFAULT_OCR;
    private String site = "";

    /**
     * Getter for property 'site'.
     *
     * @return Value for property 'site'.
     */
    public String getSite() {
        return site;
    }

    /**
     * Setter for property 'site'.
     *
     * @param site Value to set for property 'site'.
     */
    public void setSite(final String site) {
        this.site = site;
    }

    /**
     * Getter for property 'catalog'.
     *
     * @return Value for property 'catalog'.
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Setter for property 'catalog'.
     *
     * @param catalog Value to set for property 'catalog'.
     */
    public void setCatalog(final String catalog) {
        this.catalog = catalog;
    }

    /**
     * Getter for property 'ocr'.
     *
     * @return Value for property 'ocr'.
     */
    public String getOcr() {
        return ocr;
    }

    /**
     * Setter for property 'ocr'.
     *
     * @param ocr Value to set for property 'ocr'.
     */
    public void setOcr(final String ocr) {
        this.ocr = ocr;
    }
}
