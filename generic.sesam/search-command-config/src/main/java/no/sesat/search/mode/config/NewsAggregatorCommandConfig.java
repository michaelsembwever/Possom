/* Copyright (2007-2008) Schibsted Søk AS
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
package no.sesat.search.mode.config;

import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 *
 *
 * @version $Id$
 */
@Controller("NewsAggregatorSearchCommand")
public class NewsAggregatorCommandConfig extends ClusteringEspFastCommandConfig {

    private final static Logger LOG = Logger.getLogger(NewsAggregatorCommandConfig.class);

    private String xmlSource = "";
    private String xmlMainFile = "fp_main_main.xml";
    private int relatedMaxCount = 30;
    private String geographicFields = "";
    private String categoryFields = "";

    /**
     *
     */
    public NewsAggregatorCommandConfig(){}

    /**
     *
     * @param sc
     */
    public NewsAggregatorCommandConfig(SearchConfiguration sc) {

        if (sc instanceof NewsAggregatorCommandConfig) {
            final NewsAggregatorCommandConfig nasc = (NewsAggregatorCommandConfig) sc;
            xmlSource = nasc.getXmlSource();
            xmlMainFile = nasc.getXmlMainFile();
            relatedMaxCount = nasc.getRelatedMaxCount();
            geographicFields = nasc.getGeographicFields();
        }
    }

    /**
     *
     * @return
     */
    public int getRelatedMaxCount() {
        return relatedMaxCount;
    }

    /**
     *
     * @param relatedMaxCount
     */
    public void setRelatedMaxCount(int relatedMaxCount) {
        this.relatedMaxCount = relatedMaxCount;
    }

    /**
     *
     * @return
     */
    public String getXmlSource() {
        return xmlSource;
    }

    /**
     *
     * @param xmlSource
     */
    public void setXmlSource(String xmlSource) {
        this.xmlSource = xmlSource;
    }

    /**
     *
     * @param xmlMainFile
     */
    public void setXmlMainFile(String xmlMainFile) {
        this.xmlMainFile = xmlMainFile;
    }

    /**
     *
     * @return
     */
    public String getGeographicFields() {
        return geographicFields;
    }

    /**
     *
     * @param geographicFields
     */
    public void setGeographicFields(String geographicFields) {
        this.geographicFields = geographicFields;
    }

    /**
     *
     * @return
     */
    public String getCategoryFields() {
        return categoryFields;
    }

    /**
     *
     * @param categoryFields
     */
    public void setCategoryFields(String categoryFields) {
        this.categoryFields = categoryFields;
    }

    /**
     *
     * @return
     */
    public String getXmlMainFile() {
        return xmlMainFile;
    }

    /**
     *
     * @return
     */
    public String[] getCategoryFieldArray() {
        return categoryFields.split(",");
    }

    /**
     *
     * @return
     */
    public String[] getGeographicFieldArray() {
        return geographicFields.split(",");
    }
}
