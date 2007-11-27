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
package no.sesat.search.mode.command;

import no.sesat.search.mode.config.NewsMyNewsCommandConfig;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author geir
 * @version $Id$
 */
public final class NewsMyNewsSearchCommand extends AbstractSearchCommand {
    
    private static final Logger LOG = Logger.getLogger(NewsMyNewsSearchCommand.class);
    private static final Pattern cookiePattern = Pattern.compile("(?:\\A|\\|)([^\\|]+)\\:{2}([^\\|]+)\\|?");

    /**
     * @param cxt        The context to execute in.
     */
    public NewsMyNewsSearchCommand(Context cxt) {
        super(cxt);
    }

    public ResultList<? extends ResultItem> execute() {

        LOG.debug("entering execute()");
        final NewsMyNewsCommandConfig config = getSearchConfiguration();
        String myNews = (String) context.getDataModel().getJunkYard().getValue("myNews");
        LOG.debug("Cookie is: " + myNews);
        if (myNews != null && myNews.length() > 0) {
            final ResultList<ResultItem> mergedResult = new BasicResultList<ResultItem>();
            Matcher matcher = cookiePattern.matcher(myNews);
            int hitCount = 0;
            while (matcher.find()) {
                // count all cookies
                hitCount++;
            }
            matcher.reset();

            int position = 0;
            int offset = getOffset();
            for (int i = 0; i < offset; i++) {
                // Forward matcher to correct place in cookie.
                if (!matcher.find()) {
                    break;
                }
            }

            while (matcher.find() && position < config.getResultsToReturn()) {
                ResultList<? extends ResultItem> collectedResult;
                String commandName = null;
                final String type = matcher.group(2);
                if (type.equals("knippe")) {
                    commandName = "clusterMyNews" + position;
                } else if (type.equals("sak")) {
                    commandName = "newsCase" + position;
                } else if (type.equals("person")) {
                    commandName = "newsPerson" + position;
                } else if (type.equals("art")) {
                    commandName = "article" + position;
                }

                if (commandName != null) {
                    try {
                        LOG.debug("Waiting for " + commandName);
                        collectedResult = getSearchResult(commandName, datamodel);

                        if (collectedResult != null
                                && collectedResult.getResults().size() > 0) {
                               // Article 
                            if(!(collectedResult.getResults().get(0) instanceof ResultList<?>)) {
                                ResultItem searchResultItem = collectedResult.getResults().get(0);
                                searchResultItem = searchResultItem.addField("type", type);
                                mergedResult.addResult(searchResultItem);
                            } else {
                                ResultList<ResultItem> searchResultItem
                                        = (ResultList<ResultItem>) collectedResult.getResults().get(0);

                                final int lastSubPos = Math.min(collectedResult.getResults().size(), 4);
                                if (lastSubPos > 1) {
                                    final ResultList<ResultItem> subSearchResults = new BasicResultList<ResultItem>();
                                    subSearchResults.setHitCount(collectedResult.getHitCount());
                                    searchResultItem.addResult(subSearchResults);
                                    for (int i = 1; i < lastSubPos; i++) {
                                        subSearchResults.addResult(collectedResult.getResults().get(i));
                                    }
                                }
                                searchResultItem = searchResultItem.addField("type", type);
                                if (type.equals("sak") || type.equals("person")) {
                                    searchResultItem = searchResultItem.addField("newsCase", matcher.group(1));
                                }
                                mergedResult.addResult(searchResultItem);
                                LOG.debug("Collected " + searchResultItem.getField("type") + ":" + searchResultItem.getField("title"));
                            }

                        } else {
                            LOG.debug("Command " + commandName + " is empty or wrong type: " + collectedResult);
                        }
                    } catch (InterruptedException e) {
                        LOG.error("Command was interrupted", e);
                    }
                }
                position++;
            }
            
            mergedResult.setHitCount(hitCount);
            setNextOffset(mergedResult, config.getResultsToReturn());

            return mergedResult;
        } else {
            
            LOG.info("Could not find cookie");
            ResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();
            searchResult.setHitCount(0);
            return searchResult;
        }
    }

    private void setNextOffset(ResultList<? extends ResultItem> searchResult, int returnedResults) {
        int offset = getOffset();
        if (offset + returnedResults < searchResult.getHitCount()) {
            LOG.debug("Setting next offset to: " + (offset + returnedResults));
            NewsEspSearchCommand.addNextOffsetField(offset + returnedResults, searchResult);
        }
    }

    private int getOffset() {
        int offset = 0;
        if (datamodel.getJunkYard().getValue("offset") != null) {
            offset = Integer.parseInt((String) datamodel.getJunkYard().getValue("offset"));
        }
        return offset;
    }

    @Override
    public NewsMyNewsCommandConfig getSearchConfiguration() {
        return (NewsMyNewsCommandConfig) super.getSearchConfiguration();
    }
}
