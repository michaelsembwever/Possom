/*
 * SyndicationGenerator.java
 *
 * Created on June 7, 2006, 2:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.view.output;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.util.Channels;
import no.schibstedsok.front.searchportal.view.i18n.TextMessages;
import org.apache.commons.lang.StringEscapeUtils;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * 
 * @author maek
 * @todo Consider rewriting this as a bean to be used by a jsp (rssDecorator.jsp). <jsp:useBean...> 
 * This would make it possible to render the templates using the ImportVelocityTemplateTag and simplify other things as well (such as date handling)
 * The only problem is to get rid of leading whitespace generated before the xml declaration by the jsp. JSP 2.1 has page property to do this.
 * 
 */
public class SyndicationGenerator {
    
    private static final String RSS_TPL_DIR="rss";
    private final SearchResult result;
    private final Site site;
    private final TextMessages text;
    private String feedType = "rss_2.0";
    private final String templateDir;
    private final VelocityEngine engine;
    private DateFormat dateFormat;
    private final String query;
    private final String uri;
    private final Channels channels;
    private final HttpServletRequest request;
    private String encoding = "UTF-8";

    // Any other way to get rid of the dc:date tags that ROME generates.
    private static final String DCDATE_PATTERN = "<dc:date>[^<]+</dc:date>";
    
    private static final Logger LOG = Logger.getLogger(VelocityResultHandler.class);
    
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ERR_TEMPLATE_NOT_FOUND =
            " Unable to find template for rss field: ";
    private static final String ERR_TEMPLATE_ERR =
            " Parse error in template: ";
    
    public SyndicationGenerator(final SearchResult result, 
                                final Site site,
                                final HttpServletRequest request,
                                final String modeId
                                ) {
        
        this.result = result;
        this.site = site;
        this.text = (TextMessages) request.getAttribute("text");
        this.channels = (Channels) request.getAttribute("channels");
        this.query = request.getParameter("q");
        this.uri = request.getRequestURL().append("?").append(request.getQueryString()).toString();
        this.request = request;
        
        String feedType = request.getParameter("feedtype");
        if (feedType != null) {
            this.feedType = feedType;
        }
    
        String encoding = request.getParameter("encoding");
        if (encoding != null) {
            if (encoding.equalsIgnoreCase("iso-8859-1")) {
                this.encoding = "iso-8859-1";
            }
        }
        
        templateDir = site.getTemplateDir() + "/rss/" + "/" + modeId + "/";
        engine = VelocityResultHandler.getEngine(site);
    }
    
    public String generate() {

        String dfString = DEFAULT_DATE_FORMAT;

        try {
            dfString = render("dateFormat_publishedDate", null);
        } catch (ResourceNotFoundException ex) {}

        final DateFormat df = new SimpleDateFormat(dfString);

        try {
            final SyndFeed feed = new SyndFeedImpl();

            feed.setEncoding(this.encoding);
            feed.setFeedType(feedType);
            feed.setDescription(render("description", null));
            feed.setTitle(render("title", null));
            feed.setPublishedDate(new Date());
            feed.setLink(render("link", null));

            final List<SyndEntry> entries = new ArrayList<SyndEntry>();
            
            for (SearchResultItem item : result.getResults()) {
                final SyndEntry entry = new SyndEntryImpl();

                final SyndContent content = new SyndContentImpl();

                content.setType("text/html");
                final String entryDescription = render("entryDescription", item);
                
                content.setValue(StringEscapeUtils.unescapeHtml(entryDescription));
                
                final String publishedDate = render("entryPublishedDate", item);

                try {
                    Date date = df.parse(publishedDate);
                    
                    if (date.getTime() > 0) {
                        entry.setPublishedDate(df.parse(publishedDate));
                    } else {
                        LOG.debug("Publish date set to epoch. Ignoring");
                    }
                } catch (ParseException ex) {
                    LOG.error("Cannot parse " + publishedDate + " using df " + dfString);
                    entry.setPublishedDate(new Date());
                }
                
                entry.setTitle(render("entryTitle", item));
                entry.setLink(render("entryUri", item));
                

                try {
                    final SyndEnclosure enclosure = new SyndEnclosureImpl();

                    enclosure.setUrl(render("entryEnclosure", item));

                    final List<SyndEnclosure> enclosures = new ArrayList();
                    enclosures.add(enclosure);
                    entry.setEnclosures(enclosures);

                    if (request.getParameter("c") == "swip") {
                        enclosure.setType("image/gif");
                    } else {
                        enclosure.setType("image/png");
                    }
                } catch (ResourceNotFoundException ex) {
                    LOG.debug("Template for enclosure not found. Skipping.");
                }
                
                
                final List contents = new ArrayList();
                
                contents.add(content);
                
                entry.setContents(contents);
                entry.setDescription(content);
                
                entries.add(entry);
            }
            
            feed.setEntries(entries);
            
            final Writer writer = new StringWriter();
            
            
            final SyndFeedOutput output = new SyndFeedOutput();
            
            return output.outputString(feed).replaceAll(DCDATE_PATTERN, "");
        } catch (ResourceNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (FeedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    private String render(final String name, final SearchResultItem item) throws ResourceNotFoundException {
        final String templateUri = templateDir + "/" + name + ".vm";
        
        try {
            final VelocityContext cxt = new VelocityContext();

            //TODO: figure out textbundling
//            final TextMessages txtMsgs = TextMessages.valueOf(site);
            cxt.put("text", text);
            
            if (item != null) {
                cxt.put("item", item);
            }
            
            cxt.put("query", query);
            
            final String origUri = uri.replaceAll("&?output=[^&]+", "").replaceAll("&?feedtype=[^&]+", "");
            cxt.put("uri", origUri);
            
            final Site cxtSite = this.site;
            cxt.put("channels", channels);
            
            final Template tpl = engine.getTemplate(templateUri);

            StringWriter writer = new StringWriter();
            tpl.merge(cxt, writer);
            
            return writer.toString();
        } catch (ParseErrorException ex) {
            LOG.error(ERR_TEMPLATE_ERR + templateUri);
            throw new RuntimeException(ex);
        } catch (MethodInvocationException ex) {
            throw new InfrastructureException(ex);
        } catch (ResourceNotFoundException ex) {
            LOG.error(ERR_TEMPLATE_NOT_FOUND + templateUri);
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }        
}