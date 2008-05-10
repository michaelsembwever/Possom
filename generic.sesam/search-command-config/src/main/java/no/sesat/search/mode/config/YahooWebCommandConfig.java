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
package no.sesat.search.mode.config;

import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/** Configuration for searches against Yahoo's Contextual Web Service.
 * see http://developer.yahoo.com/search/web/V1/contextSearch.html
 *
 * @version <tt>$Id$</tt>
 */
@Controller("YahooWebSearchCommand")
public class YahooWebCommandConfig extends AbstractYahooSearchConfiguration {

    /**
     * Holds value of property similar.
     */
    private boolean similar = false;

    /**
     * Getter for property similar.
     * @return Value of property similar.
     */
    public boolean getSimilar() {
        return this.similar;
    }

    /**
     * Setter for property uniqusimilare.
     * @param similar New value of property similar.
     */
    public void setSimilar(final boolean similar) {
        this.similar = similar;
    }

    /**
     * Holds value of property region.
     */
    private String country;

    /**
     * Getter for property country.
     * @return Value of property country.
     */
    public String getCountry() {
        return this.country;
    }

    /**
     * Setter for property country.
     * Available values written in http://developer.yahoo.com/search/countries.html
     * @param country New value of property country.
     */
    public void setCountry(final String country) {
        this.country = country;
    }

    /**
     * Getter for property appid. Delegates to getPartnerId().
     * @return Value of property appid.
     */
    public String getAppid() {
        return getPartnerId();
    }

    /**
     * Setter for property appid. Delegates to setPartnerId(..).
     * Apply for a application ID here http://developer.yahoo.com/faq/index.html#appid
     * @param appid New value of property appid.
     */
    public void setAppid(final String appid) {
        setPartnerId(appid);
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
    private String format = "any";

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
    public YahooWebCommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit,
            final Context context) {

        super.readSearchConfiguration(element, inherit, context);

        AbstractDocumentFactory.fillBeanProperty(this, inherit, "appid", ParseType.String, element, "YahooDemo");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "format", ParseType.String, element, "any");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "adult_ok", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "similar_ok", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "language", ParseType.String, element, "en");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "country", ParseType.String, element, null);
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "site", ParseType.String, element, null);

        return this;
    }



}
