/* Copyright (2006-2008) Schibsted Søk AS
 * This file is part of SESAT.
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

 * SyndicationGenerator.java
 *
 * Created on June 7, 2006, 2:39 PM
 */

package no.sesat.search.view.output;

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
import no.sesat.search.InfrastructureException;
import no.sesat.search.datamodel.DataModelContext;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.site.config.ResourceContext;
import no.sesat.search.view.config.SearchTab;
import no.sesat.search.site.config.TextMessages;
import no.sesat.search.view.output.syndication.modules.SearchResultModule;
import no.sesat.search.view.output.syndication.modules.SearchResultModuleImpl;
import no.sesat.search.view.velocity.VelocityEngineFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import javax.resource.NotSupportedException;
import no.sesat.search.result.BasicResultList;

/**
 * Used by the rssDecorator.jsp to print out the results in rss format.
 *
 *
 */
public final class SyndicationGenerator {

    /**
     * The context this class needs to do its job.
     */
    public interface Context extends SiteContext, DataModelContext, ResourceContext {
        /**
         * The tab to generate rss for.
         *
         * @return The search tab to generate rss for.
         */
        SearchTab getTab();

        /**
         * The complete URL of the original page the rss represents.
         *
         * @return the url of the original page.
         */
        String getURL();
    }

    // Constants -----------------------------------------------------

    // Any other way to get rid of the dc:date tags that ROME generates.
    private static final Logger LOG = Logger.getLogger(SyndicationGenerator.class);

    private static final String DCDATE_PATTERN = "<dc:date>[^<]+</dc:date>";

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ERR_TEMPLATE_NOT_FOUND = " Unable to find template for rss field: ";
    private static final String ERR_TEMPLATE_ERR = " Parse error in template: ";
    private static final String DEBUG_USING_DEFAULT_DATE_FORMAT = "Using default date format";

    // Attributes ----------------------------------------------------

    private final Context context;

    private final ResultList<ResultItem> result;
    private final Site site;
    private final TextMessages text;
    private String feedType = "rss_2.0";
    private final String templateDir;
    private final VelocityEngine engine;
    private final String uri;
    //private final Channels channels;
    private String encoding = "UTF-8";
    private String nowStringUTC;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance.
     *
     * @param context The context this class needs to do its work.
     * @throws SyndicationNotSupportedException
     */
    public SyndicationGenerator(final Context context) throws SyndicationNotSupportedException{

        if(null == context.getTab().getRssResultName()){ throw new SyndicationNotSupportedException(); }

        this.context = context;

        this.result = null != context.getDataModel().getSearch(context.getTab().getRssResultName())
                ? context.getDataModel().getSearch(context.getTab().getRssResultName()).getResults()
                : new BasicResultList<ResultItem>();

        this.site = context.getSite();

        this.text = TextMessages.valueOf(getTextMessagesContext());
        this.uri = context.getURL();

        final String type = getParameter("feedType");

        if (! "".equals(type)) {
            this.feedType = type;
        }

        final String enc = getParameter("encoding");
        if (! "".equals(enc)) {
            if (encoding.equalsIgnoreCase("iso-8859-1")) {
                this.encoding = "iso-8859-1";
            }
        }

        templateDir = "rss/" + context.getTab().getId() + "/";

        engine = VelocityEngineFactory.valueOf(site).getEngine();
    }

    // Public --------------------------------------------------------

    /**
     * Returns the generated rss content.
     *
     * @return the rss document.
     */

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
            feed.setDescription(StringEscapeUtils.unescapeXml(render("description", null, 0)));
            feed.setTitle(StringEscapeUtils.unescapeXml(render("title", null, 0)));
            feed.setPublishedDate(new Date());
            feed.setLink(render("link", null, 0));

            final List<SyndEntry> entries = new ArrayList<SyndEntry>();

            int idx = 0;
            for (ResultItem item : result.getResults()) {
                ++idx;

                final SyndEntry entry = new SyndEntryImpl();

                final SearchResultModule entryModule = new SearchResultModuleImpl();

                if (item.getField("age") != null && !"".equals(item.getField("age"))) {
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
                    if (!(publishedDate == null || publishedDate.trim().equals(""))) {
                        LOG.error("Cannot parse " + publishedDate + " using df " + dfString);
                    } else {
                        LOG.debug("Publish date is empty. Using current time");
                    }

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

                    // @todo. specific to sesam.no. put somewhere else...
                    if ("swip".equals(context.getTab().getKey())) {
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
            final ResultItem item,
            final int itemIdx) throws ResourceNotFoundException {

        final String templateUri = templateDir + name;

        try {
            final VelocityContext cxt = VelocityEngineFactory.newContextInstance();

            cxt.put("text", text);
            cxt.put("now", nowStringUTC);

            if (item != null) {
                cxt.put("item", item);
                cxt.put("itemIdx", itemIdx);
            }

            cxt.put("datamodel", context.getDataModel());

            final String origUri = uri.replaceAll("&?layout=[^&]+", "").replaceAll("&?feedtype=[^&]+", "");
            cxt.put("uri", origUri);

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

    private String getParameter(final String parameterName) {
        final StringDataObject value = context.getDataModel().getParameters().getValue(parameterName);

        if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }

    private TextMessages.Context getTextMessagesContext() {
        return new TextMessages.Context() {
            public Site getSite() {
                return context.getSite();
            }

            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteCxt,
                    final String resource,
                    final Properties properties) {
            return context.newPropertiesLoader(siteCxt, resource, properties);
            }
        };
    }

//    private Channels.Context getChannelContext() {
//        return new Channels.Context() {
//            public Site getSite() {
//                return context.getSite();
//            }
//            public DocumentLoader newDocumentLoader(
//                    final SiteContext cxt,
//                    final String resource,
//                    final DocumentBuilder builder) {
//                return context.newDocumentLoader(cxt, resource, builder);
//            }
//        };
//    }

    // Inner classes -------------------------------------------------

    public static final class SyndicationNotSupportedException extends Exception{

    }
}
