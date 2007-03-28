// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class NewsAggregatorSearchConfiguration extends ClusteringESPFastConfiguration {
    private final static Logger LOG = Logger.getLogger(NewsAggregatorSearchConfiguration.class);

    private String xmlSource;
    private String xmlMainFile;
    private int relatedMaxCount = 30;
    private String geographicFields;
    private String categoryFields;

    public NewsAggregatorSearchConfiguration(SearchConfiguration sc) {
        super(sc);
        if (sc instanceof NewsAggregatorSearchConfiguration) {
            final NewsAggregatorSearchConfiguration nasc = (NewsAggregatorSearchConfiguration) sc;
            xmlSource = nasc.getXmlSource();
            xmlMainFile = nasc.getXmlMainFile();
            relatedMaxCount = nasc.getRelatedMaxCount();
            geographicFields = nasc.getGeographicFields();
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

    public void setXmlMainFile(String xmlMainFile) {
        this.xmlMainFile = xmlMainFile;
    }

    public String getGeographicFields() {
        return geographicFields;
    }

    public void setGeographicFields(String geographicFields) {
        this.geographicFields = geographicFields;
    }

    public String getCategoryFields() {
        return categoryFields;
    }

    public void setCategoryFields(String categoryFields) {
        this.categoryFields = categoryFields;
    }

    public String getXmlMainFile() {
        return xmlMainFile;
    }

    public String[] getCategoryFieldArray() {
        return StringUtils.split(categoryFields, ',');
    }

    public String[] getGeographicFieldArray() {
        return StringUtils.split(geographicFields, ',');
    }

}
