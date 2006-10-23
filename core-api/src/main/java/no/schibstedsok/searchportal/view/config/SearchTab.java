// Copyright (2006) Schibsted Søk AS
/*
 * SearchTab.java
 *
 * Created on 20 April 2006, 07:55
 *
 */

package no.schibstedsok.searchportal.view.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Immutable POJO holding the view configuration for a given tab.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class SearchTab {


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(SearchTab.class);
    private static final String ERR_ENRICHMENT_BY_COMMAND_NON_EXISTENT
            = "No enrichment, in this SearchTab, is linked to the command ";
    private static final String ERR_NAVIGATION_HINT_NOT_FOUND
            = "Navigation hint not found for ";


    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of SearchTab */
    SearchTab(
                final SearchTab inherit,
                final String id,
                final String mode,
                final String key,
                final String parentKey,
                final String rssResultName,
                final int pageSize,
                final Collection<NavigatorHint> navigations,
                final int enrichmentLimit,
                final int enrichmentOnTop,
                final int enrichmentOnTopScore,
                final Collection<EnrichmentHint> enrichments,
                final String adCommand,
                final int adLimit,
                final int adOnTop,
                final List<String> css,
                final boolean absoluteOrdering){

        this.inherit = inherit;
        this.id = id;

        // rather compact code. simply assigns the property to that pass in, or that from the inherit object, or null/-1
        this.mode = mode != null && mode.trim().length() >0 ? mode : inherit != null ? inherit.mode : null;
        this.key = key != null && key.trim().length() >0 ? key : inherit != null ? inherit.key : null;
        this.parentKey = parentKey != null && parentKey.trim().length() >0
                ? parentKey
                : inherit != null ? inherit.parentKey : null;
        this.pageSize = pageSize >=0 || inherit == null ? pageSize : inherit.pageSize;
        this.navigators.addAll(navigations);
        this.enrichmentLimit = enrichmentLimit >=0 || inherit == null ? enrichmentLimit : inherit.enrichmentLimit;
        this.enrichmentOnTop = enrichmentOnTop >=0 || inherit == null ? enrichmentOnTop : inherit.enrichmentOnTop;
        this.enrichmentOnTopScore = enrichmentOnTopScore >=0 || inherit == null
                ? enrichmentOnTopScore
                : inherit.enrichmentOnTopScore;
        this.enrichments.addAll(enrichments);
        this.adCommand = adCommand != null && adCommand.trim().length() >0
                ? adCommand
                : inherit != null ? inherit.adCommand : null;
        this.adLimit = adLimit >=0 || inherit == null ? adLimit : inherit.adLimit;
        this.adOnTop = adOnTop >=0 || inherit == null ? adOnTop : inherit.adOnTop;
        if(inherit != null){
            this.navigators.addAll(inherit.navigators);
            this.enrichments.addAll(inherit.enrichments);
            this.css.addAll(inherit.css);
        }
        this.rssResultName = rssResultName;
        this.css.addAll(css);
        this.absoluteOrdering = absoluteOrdering;
    }

    // Getters --------------------------------------------------------

    /**
     * Holds value of property id.
     */
    private final String id;

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Holds value of property pageSize.
     */
    private final int pageSize;

    /**
     * Getter for property pageSize.
     * @return Value of property pageSize.
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * Holds value of property enrichmentLimit.
     */
    private final int enrichmentLimit;

    /**
     * Getter for property enrichmentLimit.
     * @return Value of property enrichmentLimit.
     */
    public int getEnrichmentLimit() {
        return this.enrichmentLimit;
    }

    /**
     * Holds value of property enrichmentOnTop.
     */
    private final int enrichmentOnTop;

    /**
     * Getter for property enrichmentOnTop.
     * @return Value of property enrichmentOnTop.
     */
    public int getEnrichmentOnTop() {
        return this.enrichmentOnTop;
    }

    /**
     * Holds value of property adCommand.
     */
    private final String adCommand;

    /**
     * Getter for property adCommand.
     * @return Value of property adCommand.
     */
    public String getAdCommand() {
        return this.adCommand;
    }

    /**
     * Holds value of property adLimit.
     */
    private final int adLimit;

    /**
     * Getter for property adsLimit.
     * @return Value of property adsLimit.
     */
    public int getAdLimit() {
        return this.adLimit;
    }

    /**
     * Holds value of property adOnTop.
     */
    private final int adOnTop;

    /**
     * Getter for property adOnTop.
     * @return Value of property adOnTop.
     */
    public int getAdOnTop() {
        return this.adOnTop;
    }

    /**
     * Holds value of property inherit.
     */
    private final SearchTab inherit;

    /**
     * Getter for property inherit.
     * @return Value of property inherit.
     */
    public SearchTab getInherit() {
        return this.inherit;
    }

    /**
     * Holds value of property key.
     */
    private final String key;

    /**
     * Getter for property key.
     * @return Value of property key.
     */
    public String getKey() {

       if (parentKey != null) {
            return parentKey;
        } else {
            return key;
        }
    }

    /**
     * Holds value of property parentKey.
     */
    private final String parentKey;

    /**
     * Getter for property parentKey.
     * @return Value of property parentKey.
     */
    public String getParentKey() {
        return this.parentKey;
    }

    /**
     * Holds value of property rssResultName
     */
    private final String rssResultName;

    /**
     * Getter for property rssResultName.
     * @return Value of property rssResultName.
     */
    public String getRssResultName() {
        return rssResultName;
    }


    /**
     * Holds the value of property absoluteOrdering
     */
    private final boolean absoluteOrdering;
    
    /**
     * Getter for property absoluteOrdering
     */
    public boolean getAbsoluteOrdering() {
        return absoluteOrdering;
    }
    
    
    /**
     * Holds value of property enrichments.
     */
    private final Collection<EnrichmentHint> enrichments = new ArrayList<EnrichmentHint> ();

    /**
     * Getter for property enrichments.
     * @return Value of property enrichments.
     */
    public Collection<EnrichmentHint> getEnrichments() {
        return Collections.unmodifiableCollection(enrichments);
    }

    public EnrichmentHint getEnrichmentByCommand(final String command){

        for(EnrichmentHint e : enrichments){
            if(e.getCommand().equals(command)){
                return e;
            }
        }
        return null;
    }

    /**
     * Holds value of property mode.
     */
    private final String mode;

    /**
     * Getter for property mode.
     * @return Value of property mode.
     */
    public String getMode() {
        return this.mode;
    }

    /**
     * Holds value of property navigations.
     */
    private final Collection<NavigatorHint> navigators = new ArrayList<NavigatorHint>();

    /**
     * Getter for property navigations.
     * @return Value of property navigations.
     */
    public Collection<NavigatorHint> getNavigators() {
        return Collections.unmodifiableCollection(navigators);
    }

    /**
     * Returns the navigator hint matching name. Returns null if no navigator
     * hint matches.
     * The name parameter passed in is to be the shorter string if a prefix or suffix match is to be found.
     */
    public NavigatorHint getNavigationHint(final String name) {
        for (NavigatorHint hint : navigators) {
            switch(hint.match) {
                case EQUAL:
                    if (hint.name.equals(name))
                        return hint;
                    break;
                case PREFIX:
                    if (hint.name.startsWith(name))
                        return hint;
                case SUFFIX:
                    if (hint.name.endsWith(name))
                        return hint;
            }
        }

        LOG.error(ERR_NAVIGATION_HINT_NOT_FOUND + name);

        return null;
    }

    public String toString(){
        return id + (inherit != null ? " --> " + inherit.toString() : "");
    }

    /**
     * Holds value of property enrichmentOnTopScore.
     */
    private int enrichmentOnTopScore;

    /**
     * Getter for property enrichmentScoreOnTop.
     * @return Value of property enrichmentScoreOnTop.
     */
    public int getEnrichmentOnTopScore() {
        return this.enrichmentOnTopScore;
    }

    public List<SearchTab> getAncestry(){
        // XXX cache result
        final List<SearchTab> ancestry = new ArrayList<SearchTab>();
        for(SearchTab t = this; t != null; t = t.getInherit()){
            ancestry.add(t);
        }
        Collections.reverse(ancestry);
        return Collections.unmodifiableList(ancestry);
    }

    // Inner classes -------------------------------------------------

    /** Immutable POJO holding Enrichment properties from a given tab.
     **/
    public static final class EnrichmentHint  {

        public EnrichmentHint(
                final String rule,
                final int threshold,
                final float weight,
                final String command){

            this.rule = rule;
            this.threshold = threshold;
            this .weight = weight;
            this.command = command;
        }

        /**
         * Holds value of property rule.
         */
        private final String rule;

        /**
         * Getter for property rule.
         * @return Value of property rule.
         */
        public String getRule() {
            return this.rule;
        }

        /**
         * Holds value of property threshold.
         */
        private final int threshold;

        /**
         * Getter for property threshold.
         * @return Value of property threshold.
         */
        public int getThreshold() {
            return this.threshold;
        }

        /**
         * Holds value of property command.
         */
        private final String command;

        /**
         * Getter for property command.
         * @return Value of property command.
         */
        public String getCommand() {
            return this.command;
        }

        /**
         * Holds value of property weight.
         */
        private final float weight;

        /**
         * Getter for property weight.
         * @return Value of property weight.
         */
        public float getWeight() {
            return this.weight;
        }
    }

    /** Immutable POJO holding navigation information for a given tab **/
    public static final class NavigatorHint {
        public NavigatorHint(
                final String id,
                final String name,
                final String displayName,
                final MatchType match,
                final String tabName,
                final String urlSuffix,
                final String image,
                final int priority,
                final SearchTabFactory tabFactory){

            this.id = id;
            this.name = name;
            this.displayName = displayName;
            this.match = match;
            this.tabName = tabName;
            this.urlSuffix = urlSuffix;
            this.image = image;
            this.priority = priority;
            this.tabFactory = tabFactory;
        }

        public enum MatchType {
            PREFIX,
            EQUAL,
            SUFFIX;
        }

        private final SearchTabFactory tabFactory;

        private final String id;
        /**
         * Getter for property id
         * @return Value of property id.
         */
        public String getId() {
            return id;
        }
   
        /**
         * Holds value of property tabName.
         */
        private final String tabName;


        /**
         * Getter for property tab.
         * @return Value of property tab.
         */
        public String getTabName() {

            return this.tabName;
        }

        /**
         * Returns the tab associated with this hint.
         */
        public SearchTab getTab() {
            return tabFactory.getTabByName(tabName);
        }

        /**
         * Holds value of property name.
         */
        private final String name;

        /**
         * Getter for property name.
         * @return Value of property name.
         */
        public String getName() {
            return this.name;
        }
        /**
         * Holds value of property name.
         */
        private final String displayName;

        /**
         * Getter for property name.
         * @return Value of property name.
         */
        public String getDisplayName() {
            return this.displayName;
        }

        /**
         * Holds value of property match.
         */
        private final MatchType match;

        /**
         * Getter for property match.
         * @return Value of property match.
         */
        public MatchType getMatch() {
            return this.match;
        }

        /**
         * Holds value of property urlSuffix.
         */
        private final String urlSuffix;

        /**
         * Getter for property urlSuffix.
         * @return Value of property urlSuffix.
         */
        public String getUrlSuffix() {
            return this.urlSuffix;
        }

        /**
         * Holds value of property image.
         */
        private String image;

        /**
         * Getter for property image.
         * @return Value of property image.
         */
        public String getImage() {
            return this.image;
        }

        private final int priority;

        /**
         * Getter for  property priority.
         * @return Value of property priority.
         */
        public int getPriority() {
            return this.priority;
        }
    }

    /**
     * Holds value of property css.
     */
    private final List<String> css = new ArrayList<String>();

    /**
     * Getter for property css.
     * @return Value of property css.
     */
    public List<String> getCss() {
        return Collections.unmodifiableList(css);
    }



}
