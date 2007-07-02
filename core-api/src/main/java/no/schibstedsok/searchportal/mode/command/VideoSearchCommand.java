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

import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.NotClause;
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

        String query = getTransformedQuery();
        try {
            query = URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }
        final String sortByString = this.getParameters().get("userSortBy") != null ? (String) this.getParameters().get("userSortBy") : "mix";
        final String videoSource = this.getParameters().get("videoSource") != null ? (String) this.getParameters().get("videoSource") : "";
        final String videoLanguage = this.getParameters().get("videoLanguage") != null ? (String) this.getParameters().get("videoLanguage") : "";

        String biasDate = "50"; // mix
        if (sortByString.equals("datetime")) {
            biasDate="0";
        } else if (sortByString.equals("standard")) {
            biasDate="100";
        }

        // Sample url: http://usp1.blinkx.com/partnerapi/sesam/?Adultfilter=true&channelhits=true&printfields=media_duration,media_format_string,language&fieldhits=language,media_format_string&highlight=terms,summaryterms&searchtype=full&Anylanguage=true&BiasDate=50&Start=0&text=pixies
        // Please note that Schibsted is charged for every search on Blinkx!
        // API is available at http://usp1.blinkx.com/partnerapi/help/

        StringBuilder url = new StringBuilder(255);
        url.append("/partnerapi/sesam/?Adultfilter=true&channelhits=true&printfields=media_duration,media_format_string,language_type&fieldhits=language,media_format_string&highlight=terms,summaryterms");
        url.append("&searchtype="); url.append(searchType);
        url.append(videoSource.length()>0?"&databasematch="+videoSource.toLowerCase():"");
        url.append(videoLanguage.length()>0?"&languageType="+videoLanguage.toUpperCase():"&Anylanguage=true");
        url.append("&BiasDate="); url.append(biasDate);
        url.append("&Start="); url.append(getCurrentOffset(0));
        url.append("&text="); url.append(query);
        LOG.debug("VSC_URL: http://usp1.blinkx.com"+url);
        return url.toString();
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
                searchResult.addObjectField("videoSources", new TreeSet());
                while(nextSibling != null ) {
                    if (nextSibling.getNodeName().equals("autn:channelhits")) {
                        addVideoSourcesNavigatorField(searchResult, nextSibling);
                    } else if (nextSibling.getNodeName().equals("autn:languagehits")) {
                            addVideoLanguageNavigatorField(searchResult, nextSibling);
                    } else if (nextSibling.getNodeName().equals("autn:totalhits")) {
                        searchResult.setHitCount(Integer.parseInt(nextSibling.getTextContent()));
                    } else if (nextSibling.getNodeName().equals("autn:hit")) {
                        final BasicResultItem item = new BasicResultItem();
                        item.addField("videoDuration", "-");
                        item.addField("summary", "");
                        Node itemSibling = nextSibling.getFirstChild();
                        while(itemSibling != null ) {
                            if (itemSibling.getNodeName().equals("autn:reference")) {
                                item.addField("url", itemSibling.getTextContent());
                            } else if (itemSibling.getNodeName().equals("autn:title")) {
                                String title = itemSibling.getTextContent();
                                item.addField("title", title.length()>2?title:"[Titel saknas]");
                            } else if (itemSibling.getNodeName().equals("autn:summary")) {
                                String summary = itemSibling.getTextContent();
                                if (summary.length()>2) {
                                    item.addField("summary", summary.replaceAll("\\s*Date.*html", "..."));
                                }
                            } else if (itemSibling.getNodeName().equals("autn:date")) {
                                addDateField(now, item, itemSibling);
                            } else if (itemSibling.getNodeName().equals("autn:content")) {
                                addContentFields(item, itemSibling);
                            }
                            itemSibling = itemSibling.getNextSibling();
                        }
                        searchResult.addResult(item);
                    }
                    nextSibling = nextSibling.getNextSibling();
                }
            }
        } catch (IOException e) {
            LOG.error("IOException:",e);
            throw new InfrastructureException(e);
        } catch (SAXException e) {
            LOG.error("SAXException:",e);
            throw new InfrastructureException(e);
        }
        return searchResult;
    }

    protected void visitImpl(final DefaultOperatorClause clause) {
        // Leading NOT operator not supported by blinkx. Rearrage query to try to avoid it.
        if (clause.getFirstClause() instanceof NotClause) {
            clause.getSecondClause().accept(this);
            appendToQueryRepresentation(" AND ");
            clause.getFirstClause().accept(this);
        } else {
            clause.getFirstClause().accept(this);
            appendToQueryRepresentation(" AND ");
            clause.getSecondClause().accept(this);
        }
    }

    protected void visitImpl(final OrClause clause) {
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" OR ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(")");
    }

    protected void visitXorClause(final Visitor visitor, final XorClause clause) {
            switch (clause.getHint()) {
                // Honor phrases.
                case PHRASE_ON_LEFT:
                    clause.getFirstClause().accept(visitor);
                    break;
                    // Treat all other high level clauses (like non-normalized phone numbers) as individual terms.
                default:
                    clause.getSecondClause().accept(visitor);
                    break;
            }
    }

    private void addContentFields(final BasicResultItem item, Node itemSibling) {
        Node itemContentSibling = itemSibling.getFirstChild().getFirstChild();
        while(itemContentSibling != null ) {
            if (itemContentSibling.getNodeName().equals("CHANNEL")) {
                item.addField("source", itemContentSibling.getTextContent());
            } else if (itemContentSibling.getNodeName().equals("IMAGE")) {
                item.addField("preview", itemContentSibling.getTextContent());
            } else if (itemContentSibling.getNodeName().equals("MEDIA_DURATION")) {
                item.addField("videoDuration", timeFormatter.format(new Date(Long.parseLong(itemContentSibling.getTextContent()))));
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

    private void addVideoLanguageNavigatorField(final BasicResultList<ResultItem> searchResult, Node nextSibling) {
        TreeMap<String, String> videoLanguage = new TreeMap<String, String>();
        Node channelSibling = nextSibling.getFirstChild();
        while(channelSibling != null ) {
            videoLanguage.put(channelSibling.getAttributes().getNamedItem("name").getTextContent().toLowerCase(),channelSibling.getTextContent());
            channelSibling = channelSibling.getNextSibling();
        }
        TreeSet<Map.Entry> set = new TreeSet<Map.Entry>(new Comparator<Map.Entry>() {
            public int compare(Map.Entry a, Map.Entry b) { // Swedish first then sort descending by hits, ascending by case insensitive language name if number of hits is equal
                // Always swedish first.... change this to a property?
                if (a.getKey().equals("swedish")) {
                    return -1;
                } else if (b.getKey().equals("swedish")) {
                    return 1;
                }
                int ret = ((Comparable) Integer.parseInt((String)((Map.Entry)a).getValue())).compareTo(Integer.parseInt((String)((Map.Entry)b).getValue()))*-1;
                if (ret == 0) {
                    ret = ((Comparable) ((Map.Entry)a).getKey()).toString().toLowerCase().compareTo(((Map.Entry)b).getKey().toString().toLowerCase());
                }
                return ret;
            }
        });
        set.addAll(videoLanguage.entrySet());
        searchResult.addObjectField("videoLanguages", set);
    }

    private void addVideoSourcesNavigatorField(final BasicResultList<ResultItem> searchResult, Node nextSibling) {
        TreeMap<String, String> videoSources = new TreeMap<String, String>();
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
    }

    private void addDateField(long now, final BasicResultItem item, Node itemSibling) {
        long videoDate = new Date(Long.parseLong(itemSibling.getTextContent())*1000).getTime();
        long age = now - videoDate;
        if (age > 3600000 * 24 * 3) { // older than 3 days
            item.addField("date", formatter.format(new Date(videoDate)) );
        } else if (age > 3600000 * 24) { // older than 1 day
            int days = (int)(age / (3600000 * 24));
            item.addField("date", days+(days==1?" dag":" dagar") +" gammal");
        } else if (age > 3600000) { // older than 1 hour
            int hours = (int)(age / (3600000 ));
            item.addField("date", hours +(hours==1?" timme":" timmar") +" gammal");
        } else  { // less than 1 hour
            int minutes = (int)(age / (60000 ));
            item.addField("date", minutes +(minutes==1?" minut":" minuter") +" gammal");
        }
    }

}
