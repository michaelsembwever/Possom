// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.mode.config.NewsMyNewsCommandConfig;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsMyNewsSearchCommand extends AbstractSearchCommand {
    private static final Logger LOG = Logger.getLogger(NewsMyNewsSearchCommand.class);
    private static final Pattern cookiePattern = Pattern.compile("(?:\\A|\\|)([^\\|]+)\\:{2}([^\\|]+)\\|?");

    /**
     * @param cxt        The context to execute in.
     * @param parameters The search parameters to use.
     */
    public NewsMyNewsSearchCommand(Context cxt) {
        super(cxt);
    }

    public SearchResult execute() {
        String myNews = (String) context.getDataModel().getJunkYard().getValue("myNews");
        LOG.debug("Cookie is: " + myNews);
        if (myNews != null && myNews.length() > 0) {
            final SearchResult mergedResult = new BasicSearchResult(this);
            Matcher matcher = cookiePattern.matcher(myNews);
            int position = 0;
            int clusterPos = 0;
            while (matcher.find()) {
                SearchResult collectedResult;
                String commandName = null;
                final String type = matcher.group(2);
                if (type.equals("knippe")) {
                    commandName = "clusterMyNews";
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
                        collectedResult = context.getRunningQuery().getSearchResult(commandName);
                        if (collectedResult != null && collectedResult.getResults().size() > 0) {
                            SearchResultItem searchResultItem;
                            if (type.equals("knippe") && collectedResult.getResults().size() > clusterPos) {
                                searchResultItem = collectedResult.getResults().get(clusterPos);
                                clusterPos++;
                            } else {
                                searchResultItem = collectedResult.getResults().get(0);
                                final int lastSubPos = Math.min(collectedResult.getResults().size(), 4);
                                if (lastSubPos > 1) {
                                    final SearchResult subSearchResults = new BasicSearchResult(this);
                                    subSearchResults.setHitCount(collectedResult.getHitCount());
                                    searchResultItem.addNestedSearchResult("entries", subSearchResults);
                                    for (int i = 1; i < lastSubPos; i++) {
                                        subSearchResults.addResult(collectedResult.getResults().get(i));
                                    }
                                }
                            }
                            searchResultItem.addField("type", type);
                            if (type.equals("sak") || type.equals("person")) {
                                searchResultItem.addField("newsCase", matcher.group(1));
                            }
                            mergedResult.addResult(searchResultItem);
                            LOG.debug("Collected " + searchResultItem.getField("type") + ":" + searchResultItem.getField("title"));
                        }
                    } catch (InterruptedException e) {
                        LOG.error("Command was interrupted", e);
                    } catch (ExecutionException e) {
                        LOG.error("Command could not be executed", e);
                    }
                }
                position++;
            }
            mergedResult.setHitCount(position);
            return mergedResult;
        } else {
            return new BasicSearchResult(this);
        }
    }

    @Override
    public NewsMyNewsCommandConfig getSearchConfiguration() {
        return (NewsMyNewsCommandConfig) super.getSearchConfiguration();
    }
}
