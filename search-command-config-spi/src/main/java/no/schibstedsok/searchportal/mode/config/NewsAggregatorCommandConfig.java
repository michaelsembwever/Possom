// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * 
 * @author geir
 * @version $Id$
 */
@Controller("NewsAggregatorSearchCommand")
public class NewsAggregatorCommandConfig extends ClusteringEspFastCommandConfig {
    
    private final static Logger LOG = Logger.getLogger(NewsAggregatorCommandConfig.class);

    private String xmlSource;
    private String xmlMainFile;
    private int relatedMaxCount = 30;
    private String geographicFields;
    private String categoryFields;

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
        return StringUtils.split(categoryFields, ',');
    }

    /**
     * 
     * @return 
     */
    public String[] getGeographicFieldArray() {
        return StringUtils.split(geographicFields, ',');
    }

    @Override
    public CommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "xmlSource", ParseType.String, element, "");
        AbstractDocumentFactory
                .fillBeanProperty(this, inherit, "xmlMainFile", ParseType.String, element, "fp_main_main.xml");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "geographicFields", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "categoryFields", ParseType.String, element, "");

        return this;
    }

    
}
