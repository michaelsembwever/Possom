/* Copyright (2006-2009) Schibsted ASA
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
import org.w3c.dom.Element;

/**
 * Search against Yahoo! BOSS.
 * http://developer.yahoo.com/search/boss
 *
 * @version <tt>$Id$</tt>
 */
@Controller("YahooBossSearchCommand")
public class YahooBossCommandConfig extends AbstractYahooSearchConfiguration {

    private String appid = "CXi9AK3V34Fgem_y.Rw988JmMF58pKEeeYc3lpzUm8lZY6IxaKMwNL1c4MaSq.bTU1eb";

    /**
     * Holds value of property region.
     */
    private String region;

    /**
     * Getter for property region.
     * @return Value of property region.
     */
    public String getRegion() {
        return this.region;
    }

    /**
     * Setter for property region.
     * Available values written in http://developer.yahoo.com/search/countries.html
     * @param country New value of property country.
     */
    public void setRegion(final String region) {
        this.region = region;
    }

    /**
     * Getter for property appid.
     * @return Value of property appid.
     */
    public String getAppid() {
        return appid;
    }

    /**
     * Setter for property appid.
     *
     * This value will be propagated to setPartnerId(..) in readSearchConfigurationAfter.
     *
     * Apply for a application ID here http://developer.yahoo.com/faq/index.html#appid
     * @param appid New value of property appid.
     */
    public void setAppid(final String appid) {
        this.appid = appid;
    }

    /**
     * Holds value of property language.
     */
    private String language = "en";

    /**
     * Getter for property language.
     * @return Value of property language.
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Setter for property language.
     * see supported languages http://developer.yahoo.com/search/languages.html
     * @param language New value of property language.
     */
    public void setLanguage(final String language) {
        this.language = language;
    }

    /**
     * Holds value of property site.
     */
    private String site;

    /**
     * Getter for property site.
     * @return Value of property site.
     */
    public String getSite() {
        return this.site;
    }

    /**
     * Setter for property site.
     * @param site New value of property site.
     */
    public void setSite(final String site) {
        this.site = site;
    }

    /**
     * Holds value of property format.
     */
    private String format = "xml";

    /**
     * Getter for property format.
     * @return Value of property format.
     */
    public String getFormat() {
        return this.format;
    }

    /**
     * Setter for property format.
     * @param format New value of property format.
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    /**
     * Holds value of property adult.
     */
    private boolean adult = false;

    /**
     * Getter for property adult.
     * @return Value of property adult.
     */
    public boolean getAdult() {
        return this.adult;
    }

    /**
     * Setter for property adult.
     * @param adult New value of property adult.
     */
    public void setAdult(final boolean adult) {
        this.adult = adult;
    }

    @Override
    public SearchConfiguration readSearchConfiguration(final Element element, final SearchConfiguration inherit, Context context) {
        super.readSearchConfiguration(element, inherit, context);
        setPartnerId(getAppid());
        return this;
    }
}
