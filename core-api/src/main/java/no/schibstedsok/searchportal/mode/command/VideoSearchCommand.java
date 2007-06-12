/*
 * VideoSearchCommand.java
 *
 * Created on May 28, 2007, 10:30 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.text.Format;
import java.util.Date;

import no.schibstedsok.searchportal.result.BasicResultList;
import no.schibstedsok.searchportal.result.BasicResultItem;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author mla
 */
public class VideoSearchCommand extends AbstractXmlSearchCommand {

    private static final Logger LOG = Logger.getLogger(VideoSearchCommand.class);
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    final SimpleDateFormat timeFormatter = new SimpleDateFormat("m:ss");

    // http://usp1.blinkx.com/partnerapi/user/?uid=7d51d9&text=pixies

    public VideoSearchCommand(final Context cxt) {
        super(cxt);

    }

    protected String createRequestURL() {
        /*
         * The following parameters are available.
            Parameter   Description     Required
            AdultFilter     Allows adult content to be filtered out.
            AnyLanguage     Allows documents of any language to be returned.
            BiasDate    Controls the ordering of the returned results.
            ChannelBias     Applies channel biasing to results.
            DatabaseMatch   Restricts the results to a particular source.
            LanguageType    The language type of the query text.
            MaxDate     The latest date permitted for a result document.
            MaxResults  The maximum number of results to be returned.
            MinDate     The earliest date permitted for a result document.
            PrintFields     Return additional information.
            Start   Prints results only from this position onwards.
            Text    The query text.
         */
        LOG.info("zz124");
//        return "http://usp1.blinkx.com/partnerapi/user/?uid=7d51d9&text=pixies";
        String query = getTransformedQuery();
        try {
            query = URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }
        final String sortByString = this.getParameters().get("userSortBy") != null ? (String) this.getParameters().get("userSortBy") : "datetime";
        String biasDate = "0";
        if (sortByString.equals("standard")) {
            biasDate = "100";
        }
        return "/partnerapi/user/?uid=7d51d9&Anylanguage=true&Adultfilter=true&printfields=media_duration&BiasDate="+biasDate+"&Start="+getCurrentOffset(1)+"&text="+query;
    }

    public ResultList<? extends ResultItem> execute() {

        final BasicResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();
        searchResult.setHitCount(0);
        try {
            final Document doc = this.getXmlResult();
            final Node rootElement = doc.getDocumentElement();
            final Node  responseData = rootElement.getFirstChild().getNextSibling().getNextSibling();
            if (responseData.getNodeName().equals("responsedata")) {
//                LOG.info("zz128");
                final String hits = responseData.getFirstChild().getNextSibling().getTextContent();
                searchResult.setHitCount(Integer.parseInt(hits));

                Node nextSibling = responseData.getFirstChild();
                long now = new Date().getTime();
                while(nextSibling != null ) {
                    if (nextSibling.getNodeName().equals("autn:hit")) {
                        final BasicResultItem item = new BasicResultItem();
                        Node nextSibling2 = nextSibling.getFirstChild();
                        while(nextSibling2 != null ) {
                            if (nextSibling2.getNodeName().equals("autn:reference")) {
                                item.addField("url", nextSibling2.getTextContent());
                            } else if (nextSibling2.getNodeName().equals("autn:title")) {
                                item.addField("title", nextSibling2.getTextContent());
                            } else if (nextSibling2.getNodeName().equals("autn:summary")) {
                                item.addField("summary", nextSibling2.getTextContent().replaceAll(" Date.*html", "..."));
                            } else if (nextSibling2.getNodeName().equals("autn:date")) {
                                // age function  ?
                                long videoDate = new Date(Long.parseLong(nextSibling2.getTextContent())*1000).getTime();
                                long age = now - videoDate;
                                if (age > 3600000 * 24 * 4) {
                                    item.addField("date", formatter.format(new Date(videoDate)) );
                                } else if (age > 3600000 * 24) {
                                    int days = (int)(age / (3600000 * 24));
                                    item.addField("date", days+(days==1?" dag":" dagar") +" gammal");
                                } else if (age > 3600000) {
                                    int hours = (int)(age / (3600000 ));
                                    item.addField("date", ""+hours +(hours==1?" timme":" timmar") +" gammal");
                                } else  {
                                    int minutes = (int)(age / (60000 ));
                                    item.addField("date", ""+minutes +(minutes==1?" minut":" minuter") +" gammal");
                                }
                            } else if (nextSibling2.getNodeName().equals("autn:content")) {
                                Node nextSibling3 = nextSibling2.getFirstChild().getFirstChild();
                                while(nextSibling3 != null ) {
                                    if (nextSibling3.getNodeName().equals("CHANNEL")) {
                                        item.addField("source", nextSibling3.getTextContent());
                                    } else if (nextSibling3.getNodeName().equals("IMAGE")) {
                                        item.addField("preview", nextSibling3.getTextContent());
                                    } else if (nextSibling3.getNodeName().equals("MEDIA_DURATION")) {
                                        item.addField("videoDuration", timeFormatter.format(new Date(Long.parseLong(nextSibling3.getTextContent()) * 1 )));
                                    } else if (nextSibling3.getNodeName().equals("MEDIA_TYPE_STRING")) {
                                        item.addField("videoType", nextSibling3.getTextContent());
                                    } else if (nextSibling3.getNodeName().equals("DOMAIN")) {
                                        item.addField("videoDomain", nextSibling3.getTextContent());
                                    }
                                    nextSibling3 = nextSibling3.getNextSibling();
                                }
                            }
                            nextSibling2 = nextSibling2.getNextSibling();
                        }
                        searchResult.addResult(item);
                    }
                    nextSibling = nextSibling.getNextSibling();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return searchResult;
    }


}
