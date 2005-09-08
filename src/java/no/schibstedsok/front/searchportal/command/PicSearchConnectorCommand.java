/*
 * Copyright (2005) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.command;

import no.schibstedsok.front.searchportal.response.CommandResponse;
import no.schibstedsok.front.searchportal.response.SearchResponseImpl;
import no.schibstedsok.front.searchportal.response.PicSearchResult;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.util.PagingDisplayHelper;
import no.schibstedsok.tv.service.SearchResult;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PicSearchConnectorCommand implements ConnectorCommand {
    private SearchConfiguration configuration;
    private SearchResponseImpl response;

    private Logger log = Logger.getLogger(this.getClass());

    public CommandResponse getResponse() {
        return response;
    }

    public void setConfiguration(SearchConfiguration config) {
        this.configuration = config;
    }

    public void execute() {
        response = new SearchResponseImpl();

        String url = SearchConstants.PIC_SEARCH_BASE_URL + "&q=" + configuration.getQuery() + "&start=" + configuration.getOffSet();


        System.out.println(url);

        Document doc = doSearch(url);

        Element resultElement = doc.getDocumentElement();

        response.setTotalDocumentsAvailable(Integer.parseInt(resultElement.getAttribute("hits") + 1));

        List searchResult = new ArrayList();

        NodeList list = resultElement.getElementsByTagName("image");
        for (int i = 0; i < list.getLength(); i++) {

            Element picture = (Element) list.item(i);
            String thumbUrl = picture.getAttribute("thumb_url");
            String pageUrl = picture.getAttribute("page_url");
            String thumbWidth = picture.getAttribute("thumb_width");
            String thumbHeight = picture.getAttribute("thumb_height");
            String height = picture.getAttribute("height");
            String width = picture.getAttribute("width");
            String size = picture.getAttribute("filesize");
            String imageUrl = picture.getAttribute("image_url");
            String imageTitle = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            PicSearchResult result = new PicSearchResult(thumbUrl, thumbWidth, thumbHeight, pageUrl, width, height, size, imageTitle);

            searchResult.add(result);

        }


        PagingDisplayHelper pager = new PagingDisplayHelper(response.getTotalDocumentsAvailable(), SearchConstants.PICTURES_PER_PAGE, 10);

        pager.setCurrentOffset(configuration.getOffSet());
        response.setPager(pager);
        response.setResults(searchResult);
        response.setQuery(configuration.getQuery());
    }

    private Document doSearch(String url) {
        try {
            HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());

            GetMethod get = new GetMethod(url);

            client.executeMethod(get);

            String response = get.getResponseBodyAsString();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(new InputSource(new StringReader(response)));
            return doc;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }


    public static void main(String[] args) {
        PicSearchConnectorCommand cmd = new PicSearchConnectorCommand();
        cmd.execute();
    }
}
