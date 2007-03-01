package no.schibstedsok.searchportal.mode.config;

import org.apache.log4j.Logger;
import no.schibstedsok.searchportal.result.SearchResult;

public class NewsAggregatorSearchConfiguration extends AbstractSearchConfiguration {
    private long MINUTE_IN_MILLIS = 1000 * 60;
    private final static Logger log = Logger.getLogger(NewsAggregatorSearchConfiguration.class);

    private String xmlSource;
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

}
