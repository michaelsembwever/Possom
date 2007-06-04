package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;

@Controller("AddGeographicNavigationResultHandler")
public class AddGeographicNavigationResultHandlerConfig extends AbstractResultHandlerConfig {
    private String geoXml = "geographic.xml";

    public String getGeoXml() {
        return geoXml;
    }

    public void setGeoXml(String geoXml) {
        this.geoXml = geoXml;
    }


    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        String optAttr = element.getAttribute("geo-xml");
        if (optAttr != null && optAttr.length() > 0) {
            geoXml = optAttr;
        }
        return this;
    }
}
