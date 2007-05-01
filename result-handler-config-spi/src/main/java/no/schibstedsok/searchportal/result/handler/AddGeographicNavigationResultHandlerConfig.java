package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;

@Controller("AddGeographicNavigationResultHandler")
public class AddGeographicNavigationResultHandlerConfig extends AbstractResultHandlerConfig {
    private String geoXml = "geographic.xml";
    private String countryRegionField = "countryregion";
    private String countyField = "county";
    private String municipalityField = "municipality";


    public String getGeoXml() {
        return geoXml;
    }

    public void setGeoXml(String geoXml) {
        this.geoXml = geoXml;
    }

    public String getCountryRegionField() {
        return countryRegionField;
    }

    public void setCountryRegionField(String countryRegionField) {
        this.countryRegionField = countryRegionField;
    }

    public String getCountyField() {
        return countyField;
    }

    public void setCountyField(String countyField) {
        this.countyField = countyField;
    }

    public String getMunicipalityField() {
        return municipalityField;
    }

    public void setMunicipalityField(String municipalityField) {
        this.municipalityField = municipalityField;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        String optAttr = element.getAttribute("geo-xml");
        if (optAttr != null && optAttr.length() > 0) {
            geoXml = optAttr;
        }
        optAttr = element.getAttribute("country-region-field");
        if (optAttr != null && optAttr.length() > 0) {
            countryRegionField = optAttr;
        }
        optAttr = element.getAttribute("county-field");
        if (optAttr != null && optAttr.length() > 0) {
            countyField = optAttr;
        }

        optAttr = element.getAttribute("municipality-field");
        if (optAttr != null && optAttr.length() > 0) {
            municipalityField = optAttr;
        }
        return this;
    }
}
