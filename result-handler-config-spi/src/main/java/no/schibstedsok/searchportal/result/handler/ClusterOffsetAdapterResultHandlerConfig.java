package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * @author Geir H. Pettersn (T-Rank)
 */
@Controller("ClusterOffsetAdapter")
public class ClusterOffsetAdapterResultHandlerConfig extends AbstractResultHandlerConfig {
    private static final Logger LOG = Logger.getLogger(ClusterOffsetAdapterResultHandlerConfig.class);
    private String offsetField = "offset";
    private String offsetResultField = "nextOffset";
    private int offsetInterval = 20;

    public String getOffsetField() {
        return offsetField;
    }

    public String getOffsetResultField() {
        return offsetResultField;
    }

    public int getOffsetInterval() {
        return offsetInterval;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        String optionalParameter = element.getAttribute("offset-field");
        if (optionalParameter != null && optionalParameter.length() > 0) {
            offsetField = optionalParameter;
        }
        optionalParameter = element.getAttribute("offset-result-field");
        if (optionalParameter != null && optionalParameter.length() > 0) {
            offsetResultField = optionalParameter;
        }
        optionalParameter = element.getAttribute("offset-interval");
        if (optionalParameter != null && optionalParameter.length() > 0) {
            try {
                offsetInterval = Integer.parseInt(optionalParameter);
            } catch (NumberFormatException e) {
                LOG.error("Could not parse offsetInterval", e);
            }
        }
        return this;
    }
}
