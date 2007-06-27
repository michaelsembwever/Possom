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
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import no.schibstedsok.searchportal.mode.config.VideoCommandConfig;
import no.schibstedsok.searchportal.result.BasicResultItem;
import no.schibstedsok.searchportal.result.BasicResultList;
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
    private String searchType;

    public VideoSearchCommand(final Context cxt) {
        super(cxt);
        final VideoCommandConfig vcConfig = (VideoCommandConfig) context.getSearchConfiguration();

        searchType = vcConfig.getSearchType();

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
        LOG.info("zz124: "+searchType);
        String query = getTransformedQuery();
        try {
            query = URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }
        final String sortByString = this.getParameters().get("userSortBy") != null ? (String) this.getParameters().get("userSortBy") : "datetime";
        final String videoSource = this.getParameters().get("videosource") != null ? (String) this.getParameters().get("videosource") : "";
        String biasDate = "0";
        if (sortByString.equals("standard")) {
            biasDate = "100";
        }
        // http://usp1.blinkx.com/partnerapi/sesam/?searchtype=full&Anylanguage=true&Adultfilter=true&printfields=media_duration&BiasDate=100&text=pixies
        return "/partnerapi/sesam/?searchtype="+searchType+"&Anylanguage=true&Adultfilter=true&channelhits=true&printfields=media_duration"+(videoSource.length()>0?"&databasematch="+videoSource.toLowerCase():"")+"&BiasDate="+biasDate+"&Start="+getCurrentOffset(1)+"&text="+query;
    }

    public ResultList<? extends ResultItem> execute() {

        final BasicResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();

        searchResult.addField("searchquery", getTransformedQuery());
        searchResult.setHitCount(0);
        try {
            final Document doc = this.getXmlResult();
            final Node rootElement = doc.getDocumentElement();
            final Node  responseData = rootElement.getFirstChild().getNextSibling().getNextSibling();
            if (responseData.getNodeName().equals("responsedata")) {
                Node nextSibling = responseData.getFirstChild();
                long now = new Date().getTime();
                TreeMap<String, String> videoSources = new TreeMap<String, String>();
                searchResult.addObjectField("videoSources", new TreeSet());
                while(nextSibling != null ) {
                    if (nextSibling.getNodeName().equals("autn:channelhits")) {
                        Node channelSibling = nextSibling.getFirstChild();
                        while(channelSibling != null ) {
                            videoSources.put(channelSibling.getAttributes().getNamedItem("name").getTextContent(),channelSibling.getTextContent());
                            channelSibling = channelSibling.getNextSibling();
                        }
                        TreeSet<Map.Entry> set = new TreeSet<Map.Entry>(new Comparator<Map.Entry>() {
                            public int compare(Map.Entry a, Map.Entry b) { // Sort descending by hits, ascending by case insensitive channel name if number of hits is equal
                                int ret = ((Comparable) Integer.parseInt((String)((Map.Entry)a).getValue())).compareTo(Integer.parseInt((String)((Map.Entry)b).getValue()))*-1;
                                if (ret == 0) {
                                    ret = ((Comparable) ((Map.Entry)a).getKey()).toString().toLowerCase().compareTo(((Map.Entry)b).getKey().toString().toLowerCase());
                                }
                                return ret;
                            }
                        });
                        set.addAll(videoSources.entrySet());
                        searchResult.addObjectField("videoSources", set);
                    } else if (nextSibling.getNodeName().equals("autn:totalhits")) {
                        searchResult.setHitCount(Integer.parseInt(nextSibling.getTextContent()));
                    } else if (nextSibling.getNodeName().equals("autn:hit")) {
                        final BasicResultItem item = new BasicResultItem();
                        item.addField("videoDuration", "-");
                        item.addField("summary", "-");
                        Node itemSibling = nextSibling.getFirstChild();
                        while(itemSibling != null ) {
                            if (itemSibling.getNodeName().equals("autn:reference")) {
                                item.addField("url", itemSibling.getTextContent());
                            } else if (itemSibling.getNodeName().equals("autn:title")) {
                                item.addField("title", itemSibling.getTextContent());
                            } else if (itemSibling.getNodeName().equals("autn:summary")) {
                                String summary = itemSibling.getTextContent();
                                if (summary.length()>2) {
                                    item.addField("summary", summary.replaceAll(" Date.*html", "..."));
                                }
                            } else if (itemSibling.getNodeName().equals("autn:date")) {
                                // age function  ?
                                long videoDate = new Date(Long.parseLong(itemSibling.getTextContent())*1000).getTime();
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
                            } else if (itemSibling.getNodeName().equals("autn:content")) {
                                Node itemContentSibling = itemSibling.getFirstChild().getFirstChild();
                                while(itemContentSibling != null ) {
                                    if (itemContentSibling.getNodeName().equals("CHANNEL")) {
                                        item.addField("source", itemContentSibling.getTextContent());
                                    } else if (itemContentSibling.getNodeName().equals("IMAGE")) {
                                        item.addField("preview", itemContentSibling.getTextContent());
                                    } else if (itemContentSibling.getNodeName().equals("MEDIA_DURATION")) {
                                        item.addField("videoDuration", timeFormatter.format(new Date(Long.parseLong(itemContentSibling.getTextContent()) * 1 )));
                                    } else if (itemContentSibling.getNodeName().equals("MEDIA_TYPE_STRING")) {
                                        item.addField("videoType", itemContentSibling.getTextContent());
                                    } else if (itemContentSibling.getNodeName().equals("DOMAIN")) {
                                        item.addField("videoDomain", itemContentSibling.getTextContent());
                                    } else if (itemContentSibling.getNodeName().equals("STATICPREVIEW")) {
                                        item.addField("staticPreview", itemContentSibling.getTextContent());
                                    }
                                    itemContentSibling = itemContentSibling.getNextSibling();
                                }
                            }
                            itemSibling = itemSibling.getNextSibling();
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
