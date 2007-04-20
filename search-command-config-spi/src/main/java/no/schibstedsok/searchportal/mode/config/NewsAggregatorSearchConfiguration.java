// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author mick
 * @version $Id$
 */
@Controller("NewsAggregatorSearchCommand")
public class NewsAggregatorSearchConfiguration extends ClusteringESPFastConfiguration {
    
    private final static Logger LOG = Logger.getLogger(NewsAggregatorSearchConfiguration.class);

    private String xmlSource;
    private String xmlMainFile;
    private int relatedMaxCount = 30;
    private String geographicFields;
    private String categoryFields;

    /**
     * 
     * @param sc 
     */
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
        return StringUtils.split(categoryFields, ',');
    }

    /**
     * 
     * @return 
     */
    public String[] getGeographicFieldArray() {
        return StringUtils.split(geographicFields, ',');
    }

}
