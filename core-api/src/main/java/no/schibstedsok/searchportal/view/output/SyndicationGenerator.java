/* Copyright (2006) Schibsted Søk AS
 * SyndicationGenerator.java
 *
 * Created on June 7, 2006, 2:39 PM
 */

package no.schibstedsok.searchportal.view.output;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.util.Channels;
import no.schibstedsok.searchportal.view.i18n.TextMessages;
import no.schibstedsok.searchportal.view.output.syndication.modules.SearchResultModule;
import no.schibstedsok.searchportal.view.velocity.VelocityEngineFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

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
import no.schibstedsok.searchportal.view.output.syndication.modules.SearchResultModuleImpl;

/** Used by the rssDecorator.jsp to print out the results in rss format.
 *
 * @author maek
 * @todo Consider rewriting this as a bean to be used by a jsp (rssDecorator.jsp). <jsp:useBean.../>
 * This would make it possible to render the templates using the ImportVelocityTemplateTag
 * and simplify other things as well (such as date handling)
 * The only problem is to get rid of leading whitespace generated before the xml declaration
 * by the jsp. JSP 2.1 has page property to do this.
 *
 */
public final class SyndicationGenerator {

    
    // Constants -----------------------------------------------------
    
   
    // Any other way to get rid of the dc:date tags that ROME generates.
    private static final String DCDATE_PATTERN = "<dc:date>[^<]+</dc:date>";

    private static final Logger LOG = Logger.getLogger(SyndicationGenerator.class);

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ERR_TEMPLATE_NOT_FOUND =
            " Unable to find template for rss field: ";
    private static final String ERR_TEMPLATE_ERR =
            " Parse error in template: ";
    private static final String DEBUG_TEMPLATE_NOT_FOUND = "Could not find template ";
    private static final String DEBUG_USING_DEFAULT_DATE_FORMAT = "Using default date format";
    
    // Attributes ----------------------------------------------------
    
    private final SearchResult result;
    private final Site site;
    private final TextMessages text;
    private String feedType = "rss_2.0";
    private final String templateDir;
    private final VelocityEngine engine;
    private final String query;
    private final String uri;
    private final Channels channels;
    private final HttpServletRequest request;
    private String encoding = "UTF-8";
    private String nowStringUTC;

    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------

    public SyndicationGenerator(final SearchResult result,
                                final Site site,
                                final HttpServletRequest request,
                                final String modeId) {

        this.result = result;
        this.site = site;
        this.text = (TextMessages) request.getAttribute("text");
        this.channels = (Channels) request.getAttribute("channels");
        this.query = request.getParameter("q");
        this.uri = request.getRequestURL().append("?").append(request.getQueryString()).toString();
        this.request = request;

        final String feedType = request.getParameter("feedtype");
        if (feedType != null) {
            this.feedType = feedType;
        }

        final String encoding = request.getParameter("encoding");
        if (encoding != null) {
            if (encoding.equalsIgnoreCase("iso-8859-1")) {
                this.encoding = "iso-8859-1";
            }
        }

        templateDir = "rss/" + modeId + "/";
        // TODO should be using a context here. Below can be used in tests.
        engine = VelocityEngineFactory.valueOf(site).getEngine();
    }

    // Public --------------------------------------------------------
    
    /** TODO comment me. **/
    public String generate() {

        String dfString = DEFAULT_DATE_FORMAT;

        try {
            dfString = render("dateFormat_publishedDate", null, 0);
        } catch (ResourceNotFoundException ex) {
            LOG.trace(DEBUG_USING_DEFAULT_DATE_FORMAT);
        }

        final DateFormat df = new SimpleDateFormat(dfString);

        // Zulu time is UTC. But java doesn't know that.
        if (dfString.endsWith("'Z'")) {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        nowStringUTC = df.format(new Date());

        try {
            final SyndFeed feed = new SyndFeedImpl();
            final SearchResultModule m = new SearchResultModuleImpl();

            m.setNumberOfHits(Integer.toString(result.getHitCount()));

            final List<SearchResultModule> modules = new ArrayList<SearchResultModule>();
            
            modules.add(m);
            
            feed.setModules(modules);

            feed.setEncoding(this.encoding);
            feed.setFeedType(feedType);
            feed.setDescription(render("description", null, 0));
            feed.setTitle(render("title", null, 0));
            feed.setPublishedDate(new Date());
            feed.setLink(render("link", null, 0));

            final List<SyndEntry> entries = new ArrayList<SyndEntry>();

            int idx = 0;
            for (SearchResultItem item : result.getResults()) {
                ++idx;

                final SyndEntry entry = new SyndEntryImpl();

                final SearchResultModule entryModule = new SearchResultModuleImpl();

                if (item.getField("age") != null && ! "".equals(item.getField("age"))) {
                    entryModule.setArticleAge(item.getField("age"));
                }

                if (item.getField("newssource") != null && !"".equals(item.getField("newssource"))) {
                    entryModule.setNewsSource(item.getField("newssource"));
                }

                final List<SearchResultModule> sModules = new ArrayList<SearchResultModule>();
                sModules.add(entryModule);
                entry.setModules(sModules);
                final SyndContent content = new SyndContentImpl();

                content.setType("text/html");
                final String entryDescription = render("entryDescription", item, idx);

                content.setValue(StringEscapeUtils.unescapeHtml(entryDescription));

                final String publishedDate = render("entryPublishedDate", item, idx);

                try {
                    final Date date = df.parse(publishedDate);

                    if (date.getTime() > 0) {
                        entry.setPublishedDate(df.parse(publishedDate));
                    } else {
                        LOG.debug("Publish date set to epoch. Ignoring");
                    }
                } catch (ParseException ex) {
                    LOG.error("Cannot parse " + publishedDate + " using df " + dfString);
                    entry.setPublishedDate(new Date());
                }

                entry.setTitle(render("entryTitle", item, idx));
                entry.setLink(render("entryUri", item, idx));


                try {
                    final SyndEnclosure enclosure = new SyndEnclosureImpl();

                    enclosure.setUrl(render("entryEnclosure", item, idx));

                    final List<SyndEnclosure> enclosures = new ArrayList<SyndEnclosure>();
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


                final List<SyndContent> contents = new ArrayList<SyndContent>();

                contents.add(content);

                entry.setContents(contents);
                entry.setDescription(content);

                entries.add(entry);
            }

            feed.setEntries(entries);

            final SyndFeedOutput output = new SyndFeedOutput();
            return output.outputString(feed).replaceAll(DCDATE_PATTERN, "");
        } catch (ResourceNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (FeedException ex) {
            throw new RuntimeException(ex);
        }
    }


    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
    private String render(
            final String name,
            final SearchResultItem item,
            final int itemIdx) throws ResourceNotFoundException {

        final String templateUri = templateDir + name;

        try {
            final VelocityContext cxt = VelocityEngineFactory.newContextInstance(engine);

            cxt.put("text", text);
            cxt.put("now", nowStringUTC);

            if (item != null) {
                cxt.put("item", item);
                cxt.put("itemIdx", itemIdx);
            }

            cxt.put("query", query);

            final String origUri = uri.replaceAll("&?output=[^&]+", "").replaceAll("&?feedtype=[^&]+", "");
            cxt.put("uri", origUri);

            cxt.put("channels", channels);
            
            if (request.getParameter("c").equals("m")) {
                if (request.getParameter("contentsource") != null && request.getParameter("contentsource").startsWith("Interna"))
                    cxt.put("newstype", "- Internasjonale nyheter");
                else if (request.getParameter("contentsource") != null && request.getParameter("contentsource").equals("Mediearkivet"))
                    cxt.put("newstype", "- Papiraviser");
                else if (request.getParameter("newscountry") != null && request.getParameter("newscountry").equals("Sverige")) 
                    cxt.put("newstype", "- Svenske nyheter");
                else if (request.getParameter("newscountry") != null && request.getParameter("newscountry").equals("Island")) 
                    cxt.put("newstype", "- Islandske nyheter");
                else if (request.getParameter("newscountry") != null && request.getParameter("newscountry").equals("Finland")) 
                    cxt.put("newstype", "- Finske nyheter");
                else if (request.getParameter("newscountry") != null && request.getParameter("newscountry").equals("Danmark")) 
                    cxt.put("newstype", "- Danske nyheter");
                else
                    cxt.put("newstype", "- Norske nyheter");
            }

            final Template tpl = VelocityEngineFactory.getTemplate(engine, site, templateUri);

            final StringWriter writer = new StringWriter();
            tpl.merge(cxt, writer);

            return writer.toString();

        } catch (ParseErrorException ex) {
            LOG.error(ERR_TEMPLATE_ERR + templateUri);
            throw new InfrastructureException(ex);

        } catch (MethodInvocationException ex) {
            throw new InfrastructureException(ex);

        } catch (ResourceNotFoundException ex) {
            LOG.debug(ERR_TEMPLATE_NOT_FOUND + templateUri);
            throw ex;

        } catch (Exception ex) {
            throw new InfrastructureException(ex);
        }
    }
}


    // Inner classes -------------------------------------------------
