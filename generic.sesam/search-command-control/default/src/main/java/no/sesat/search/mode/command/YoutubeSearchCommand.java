/*
 * Copyright (2008) Schibsted ASA
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

 */
package no.sesat.search.mode.command;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import no.sesat.search.mode.config.YoutubeCommandConfig;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @see YoutubeCommandConfig
 * @version $Id$
 */
public class YoutubeSearchCommand extends AbstractXmlSearchCommand {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(YoutubeSearchCommand.class);

    private static final String COMMAND_URL_PATTERN =
             "/feeds/api/videos?vq={0}&orderby={1}&start-index={2}&max-results={3}"
            + "&format={4}&racy={5}";

    // Static --------------------------------------------------------
    // Attributes ----------------------------------------------------

    private final Context cxt;
    private final YoutubeCommandConfig conf;

    // Constructors -------------------------------------------------

    public YoutubeSearchCommand(final Context cxt) {
        super(cxt);
        this.cxt = cxt;
        conf = (YoutubeCommandConfig) cxt.getSearchConfiguration();
        setXmlRestful(
                new AbstractXmlRestful(cxt) {
                    public String createRequestURL() {

                        return MessageFormat.format(COMMAND_URL_PATTERN,
                                cxt.getDataModel().getQuery().getUtf8UrlEncoded(),
                                (null != getParameter("userSortBy")
                                    ? getParameter("userSortBy")
                                    : conf.getSortBy()),
                                // first result is 1, not 0
                                (null != YoutubeSearchCommand.this.getParameter("offset")
                                    ? Integer.parseInt(YoutubeSearchCommand.this.getParameter("offset"))+1
                                    : "1"),
                                conf.getResultsToReturn(),
                                conf.getFormat(),
                                conf.getRacy());
                    }
        });
    }

    // Public --------------------------------------------------------

    @Override
    @SuppressWarnings("static-access")
    public ResultList<ResultItem> execute() {

        final ResultList<ResultItem> result = new BasicResultList<ResultItem>();
        Document doc;
        try {
            doc = getXmlRestful().getXmlResult();

        } catch (SocketTimeoutException ste) {
            LOG.error(getSearchConfiguration().getName() +  " --> " + ste.getMessage());
            return new BasicResultList<ResultItem>();
        } catch (IOException ex) {
            throw new SearchCommandException(ex);
        } catch (SAXException ex) {
            throw new SearchCommandException(ex);
        }

        if(null != doc) {
            final Element rootEl = doc.getDocumentElement();
            final Element totalResultsEl = (Element) rootEl.getElementsByTagName("openSearch:totalResults").item(0);
            final String totalResults = totalResultsEl.getFirstChild().getNodeValue();
            result.setHitCount(Integer.parseInt(totalResults));

            final NodeList entryList = rootEl.getElementsByTagName("entry");

            for(int i = 0; i < entryList.getLength(); i++) {

                result.addResult(createItem((Element) entryList.item(i)));
            }

        }
        return result;
    }

    // Protected --------------------------------------------------------

    @Override
    protected ResultItem createItem(final Element entryEl) {

        ResultItem resItem = new BasicResultItem();
        final Element publishedEl = (Element) entryEl.getElementsByTagName("published").item(0);
        final String published = publishedEl.getFirstChild().getNodeValue();

        final NodeList categoryList = entryEl.getElementsByTagName("category");
        List<String> tags = new ArrayList<String>();
        List<String> categories = new ArrayList<String>();
        for(int j = 0; j < categoryList.getLength(); j++) {
            final Element categoryEl = (Element) categoryList.item(j);
            final String term = categoryEl.getAttribute("term");
            final String label = categoryEl.getAttribute("label");
            if(null != label && label.length() > 0) {
                categories.add(term);
            } else if(!term.startsWith("http")) { // Filter out junk
                tags.add(term);
            }
        }

        final Element titleEl = (Element) entryEl.getElementsByTagName("title").item(0);
        final String title = titleEl.getFirstChild().getNodeValue();

        final Element contentEl = (Element) entryEl.getElementsByTagName("content").item(0);
        final String content = contentEl.getFirstChild().getNodeValue();

        final Element authorEl = (Element) entryEl.getElementsByTagName("author").item(0);
        final Element authorNameEl = (Element) authorEl.getElementsByTagName("name").item(0);
        final Element authorUrlEl = (Element) authorEl.getElementsByTagName("uri").item(0);
        final String author = authorNameEl.getFirstChild().getNodeValue();
        final String authorUrl = authorUrlEl.getFirstChild().getNodeValue();

        final Element mediaGroupEl = (Element) entryEl.getElementsByTagName("media:group").item(0);
        final Element durationEl = (Element) mediaGroupEl.getElementsByTagName("yt:duration").item(0);
        final String duration = durationEl.getAttribute("seconds");
        final String durationMin = String.valueOf(Integer.parseInt(duration) / 60);
        final String durationSec = String.valueOf(Integer.parseInt(duration) % 60);

        final NodeList mediaContentList = mediaGroupEl.getElementsByTagName("media:content");
        for(int j = 0; j < mediaContentList.getLength(); j++) {
            final Element mediaContentEl = (Element) mediaContentList.item(j);
            final String url = mediaContentEl.getAttribute("url");
            final String format = mediaContentEl.getAttribute("yt:format");
            final String type = mediaContentEl.getAttribute("type");
            if(format.equals(conf.getFormat())) {
                resItem = resItem
                        .addField("videoUrl", url)
                        .addField("videoType", type);
            }
        }

        final Element mediaPlayerEl = (Element) mediaGroupEl.getElementsByTagName("media:player").item(0);
        final String playerUrl = mediaPlayerEl.getAttribute("url");

        final List<Map<String,String>> thumbnails = new ArrayList<Map<String,String>>();
        final NodeList mediaThumbnailList = mediaGroupEl.getElementsByTagName("media:thumbnail");
        for(int j = 0; j < mediaThumbnailList.getLength(); j++) {
            final Element mediaThumbnailEl = (Element) mediaThumbnailList.item(j);
            final String url = mediaThumbnailEl.getAttribute("url");
            final String height = mediaThumbnailEl.getAttribute("height");
            final String width = mediaThumbnailEl.getAttribute("width");
            final String time = mediaThumbnailEl.getAttribute("time");
            final Map<String,String> thumbnail = new HashMap<String,String>();
            thumbnail.put("url", url);
            thumbnail.put("height", height);
            thumbnail.put("width", width);
            thumbnail.put("time", time);
            int lastDot = url.lastIndexOf(".");
            thumbnail.put("id", url.substring(lastDot-1, lastDot));
            thumbnails.add(thumbnail);
        }

        final Element gdRating = (Element)entryEl.getElementsByTagName("gd:rating").item(0);
        String ratingNum = null;
        String rating = null;
        if(null != gdRating) {
            ratingNum = gdRating.getAttribute("numRaters");
            rating = gdRating.getAttribute("average");
        }

        return resItem
                .addField("title", title)
                .addField("content", content)
                .addField("published", published)
                .addField("author", author)
                .addField("authorUrl", authorUrl)
                .addField("duration", duration)
                .addField("durationMin", durationMin)
                .addField("durationSec", durationSec)
                .addObjectField("thumbnails",(Serializable) thumbnails)
                .addObjectField("categories",(Serializable) categories)
                .addObjectField("tags",(Serializable) tags)
                .addField("ratingNum", ratingNum)
                .addField("rating", rating)
                .addField("youtubeUrl", playerUrl);

    }

    @Override
    protected String getParameter(String paramName) {
        return super.getParameter(paramName);
    }

    // Private --------------------------------------------------------

}