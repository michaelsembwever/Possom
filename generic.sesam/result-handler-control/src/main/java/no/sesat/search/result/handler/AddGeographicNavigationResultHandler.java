/* Copyright (2007) Schibsted Søk AS
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
package no.sesat.search.result.handler;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.result.FastSearchResult;
import no.sesat.search.result.Modifier;
import no.sesat.search.site.config.DocumentLoader;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;

/**
 * Adds a navigatable geograpic modifier hirarcy. (Can not use FAST navigators since the have no knowlege
 * of the hirarcy)
 * @deprecated Create a NavigationController instead. SEARCH-3427
 *
 * @author Geir H. Pettersen (T-Rank)
 * @version $Id$
 */
public class AddGeographicNavigationResultHandler implements ResultHandler {
    private static final Logger LOG = Logger.getLogger(AddGeographicNavigationResultHandler.class);
    private LinkedHashMap<String, Geo> geoMap;

    private final AddGeographicNavigationResultHandlerConfig config;
    private static final String NAME_ATTRIBUTE = "name";
    private static final String GEO_ELEMENT = "geo";
    private static final String KEY_ATTRIBUTE = "key";


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
                addGeoNavigators(datamodel, (FastSearchResult) cxt.getSearchResult(), geoMap);
            } else {
                LOG.error("Can not use " + AddGeographicNavigationResultHandler.class.getName() + " on a generic searchResult. Must be a " + FastSearchResult.class.getName());
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Could not parse categories.", e);
        }
    }

    private void addGeoNavigators(DataModel datamodel, FastSearchResult searchResult, LinkedHashMap<String, Geo> geoMap) {
        if (geoMap != null && geoMap.size() > 0) {
            for (Geo geo : geoMap.values()) {
                LOG.debug("Adding geoNav: " + geo.getKey() + "=" + geo.getName());
                searchResult.addModifier(geo.getKey(), new Modifier(geo.getName(), -1, null));
                StringDataObject selectedSubItem = datamodel.getParameters().getValue(geo.getKey());
                if (selectedSubItem != null && geo.getName().equals(selectedSubItem.getString())) {
                    addGeoNavigators(datamodel, searchResult, geo.getSubelements());
                }
            }
        }
    }

    private LinkedHashMap<String, Geo> parseGeo(Context cxt, DataModel datamodel) throws ParserConfigurationException {
        Document doc = getDocument(cxt, datamodel);
        final Element root = doc.getDocumentElement();
        return parseGeo(root);
    }

    private LinkedHashMap<String, Geo> parseGeo(Element root) {
        final List<Element> geoElements = getDirectChildren(root, GEO_ELEMENT);
        if (geoElements.size() > 0) {
            final LinkedHashMap<String, Geo> geoList = new LinkedHashMap<String, Geo>();
            for (Element geoElement : geoElements) {
                final Geo geo = new Geo();
                geo.setKey(geoElement.getAttribute(KEY_ATTRIBUTE));
                geo.setName(geoElement.getAttribute(NAME_ATTRIBUTE));
                geo.setSubelements(parseGeo(geoElement));
                geoList.put(geo.getName(), geo);
            }
            return geoList;
        }
        return null;
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

    private static class Geo {
        private String key;
        private String name;
        private LinkedHashMap<String, Geo> subelements;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LinkedHashMap<String, Geo> getSubelements() {
            return subelements;
        }

        public void setSubelements(LinkedHashMap<String, Geo> subelements) {
            this.subelements = subelements;
        }


        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Geo geo = (Geo) o;

            if (key != null ? !key.equals(geo.key) : geo.key != null) return false;
            if (name != null ? !name.equals(geo.name) : geo.name != null) return false;
            if (subelements != null ? !subelements.equals(geo.subelements) : geo.subelements != null) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (key != null ? key.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (subelements != null ? subelements.hashCode() : 0);
            return result;
        }
    }
}
