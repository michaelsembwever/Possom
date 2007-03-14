// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import org.apache.log4j.Logger;

public class NewsAggregatorSearchConfiguration extends ESPFastSearchConfiguration {
    private final static Logger log = Logger.getLogger(NewsAggregatorSearchConfiguration.class);

    private String xmlSource;
    private String xmlMainFile;
    private int relatedMaxCount = 30;
    private int updateIntervalMinutes;

    public NewsAggregatorSearchConfiguration() {
        super(null);
    }

    public NewsAggregatorSearchConfiguration(SearchConfiguration sc) {
        super(sc);
        if (sc instanceof NewsAggregatorSearchConfiguration) {
            final NewsAggregatorSearchConfiguration nasc = (NewsAggregatorSearchConfiguration) sc;
            nasc.setXmlSource(nasc.getXmlSource());
            nasc.setUpdateIntervalMinutes(nasc.getUpdateIntervalMinutes());
        }
    }

    public int getRelatedMaxCount() {
        return relatedMaxCount;
    }

    public void setRelatedMaxCount(int relatedMaxCount) {
        this.relatedMaxCount = relatedMaxCount;
    }

    public String getXmlSource() {
        return xmlSource;
    }

    public void setXmlSource(String xmlSource) {
        this.xmlSource = xmlSource;
    }

    public int getUpdateIntervalMinutes() {
        return updateIntervalMinutes;
    }

    public void setUpdateIntervalMinutes(int updateIntervalMinutes) {
        this.updateIntervalMinutes = updateIntervalMinutes;
    }

    public void setXmlMainFile(String xmlMainFile) {
        this.xmlMainFile = xmlMainFile;
    }

    public String getXmlMainFile() {
        return xmlMainFile;
    }
}
