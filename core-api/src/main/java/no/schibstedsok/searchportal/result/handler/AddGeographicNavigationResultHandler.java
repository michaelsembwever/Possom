// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Adds a navigatable geograpic modifier hirarcy. (Can not use FAST navigators since the have no knowlege
 * of the hirarcy)
 *
 * @author Geir H. Pettersen (T-Rank)
 * @version $Id$
 */
public class AddGeographicNavigationResultHandler implements ResultHandler {
    private static final Logger LOG = Logger.getLogger(AddGeographicNavigationResultHandler.class);
    private LinkedHashMap<String, LinkedHashMap<String, List<String>>> geoMap;

    private final AddGeographicNavigationResultHandlerConfig config;
    private static final String COUNTRYREGION_ELEMENT = "countryregion";
    private static final String COUNTY_ELEMENT = "county";
    private static final String MUNICIPALITY_ELEMENT = "municipality";
    private static final String NAME_ATTRIBUTE = "name";


    public AddGeographicNavigationResultHandler(ResultHandlerConfig config) {
        this.config = (AddGeographicNavigationResultHandlerConfig) config;
    }

    public void handleResult(Context cxt, DataModel datamodel) {
        try {
            if (cxt.getSearchResult() instanceof FastSearchResult) {
                if (geoMap == null) {
                    // This could happen more than once, but synchronize overhead would be on every call, so it ok.
                    geoMap = parseGeo(cxt, datamodel);
                }
                addGeoNavigators(datamodel, (FastSearchResult) cxt.getSearchResult());
            } else {
                LOG.error("Can not use " + AddGeographicNavigationResultHandler.class.getName() + " on a generic searchResult. Must be a " + FastSearchResult.class.getName());
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Could not parse categories.", e);
        }
    }

    private void addGeoNavigators(DataModel datamodel, FastSearchResult searchResult) {
        if (geoMap != null && geoMap.size() > 0) {
            for (String countryRegion : geoMap.keySet()) {
                searchResult.addModifier(config.getCountryRegionField(), new Modifier(countryRegion, -1, null));
            }
            StringDataObject selectedCountryRegion = datamodel.getParameters().getValue(config.getCountryRegionField());
            if (selectedCountryRegion != null) {
                LinkedHashMap<String, List<String>> countyMap = geoMap.get(selectedCountryRegion.getString());
                if (countyMap != null && countyMap.size() > 0) {
                    for (String county : countyMap.keySet()) {
                        searchResult.addModifier(config.getCountyField(), new Modifier(county, -1, null));
                    }
                    StringDataObject selectedCounty = datamodel.getParameters().getValue(config.getCountyField());
                    if (selectedCounty != null) {
                        List<String> municList = countyMap.get(selectedCounty.getString());
                        if (municList != null && municList.size() > 0) {
                            for (String munic : municList) {
                                searchResult.addModifier(config.getMunicipalityField(), new Modifier(munic, -1, null));
                            }
                        }
                    }
                }
            }
        }
    }

    private LinkedHashMap<String, LinkedHashMap<String, List<String>>> parseGeo(Context cxt, DataModel datamodel) throws ParserConfigurationException {
        Document doc = getDocument(cxt, datamodel);
        final Element root = doc.getDocumentElement();
        return parseGeo(root);
    }

    private LinkedHashMap<String, LinkedHashMap<String, List<String>>> parseGeo(Element root) {
        final LinkedHashMap<String, LinkedHashMap<String, List<String>>> geoMap = new LinkedHashMap<String, LinkedHashMap<String, List<String>>>();
        final List<Element> countryRegionElements = getDirectChildren(root, COUNTRYREGION_ELEMENT);
        for (Element countryRegionElement : countryRegionElements) {
            LinkedHashMap<String, List<String>> countyMap = null;
            final List<Element> countyElements = getDirectChildren(countryRegionElement, COUNTY_ELEMENT);
            if (countyElements.size() > 0) {
                countyMap = new LinkedHashMap<String, List<String>>();
                for (Element countyElement : countyElements) {
                    ArrayList<String> municapalityList = null;
                    final List<Element> municipalityElements = getDirectChildren(countryRegionElement, MUNICIPALITY_ELEMENT);
                    if (municipalityElements.size() > 0) {
                        municapalityList = new ArrayList<String>();
                        for (Element municipalityElement : municipalityElements) {
                            municapalityList.add(municipalityElement.getAttribute(NAME_ATTRIBUTE));
                        }
                    }
                    countyMap.put(countyElement.getAttribute(NAME_ATTRIBUTE), municapalityList);
                }
            }
            geoMap.put(countryRegionElement.getAttribute(NAME_ATTRIBUTE), countyMap);
        }
        return geoMap;
    }

    private List<Element> getDirectChildren(Element element, String elementName) {
        ArrayList<Element> children = new ArrayList<Element>();
        if (element != null) {
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode instanceof Element && childNode.getNodeName().equals(elementName)) {
                    children.add((Element) childNode);
                }
            }
        }
        return children;
    }


    private Document getDocument(Context cxt, DataModel dataModel) throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        DocumentLoader documentLoader = cxt.newDocumentLoader(dataModel.getSite(), config.getGeoXml(), builder);
        documentLoader.abut();
        return documentLoader.getDocument();
    }
}
