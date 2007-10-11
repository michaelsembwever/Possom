/* Copyright (2006-2007) Schibsted Søk AS
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
 *
 * SearchTab.java
 *
 * Created on 20 April 2006, 07:55
 *
 */

package no.sesat.search.view.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.sesat.search.view.navigation.NavigationConfig;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Immutable POJO holding the view configuration for a given tab.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class SearchTab implements Serializable{


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(SearchTab.class);
    
    // Attributes ----------------------------------------------------
    
    private final String id;
    private final int pageSize;
    private final int pagingSize = 10;    
    private final int enrichmentLimit;
    private final int enrichmentOnTop;
    private final String adCommand;
    private final int adLimit;
    private final int adOnTop;
    private final SearchTab inherit;
    private final String key;
    private final String parentKey;
    private final String rssResultName;
    private final boolean rssHidden;
    private final boolean absoluteOrdering;
    private final boolean displayCss;   
    private final boolean executeOnBlank;
    private final Collection<EnrichmentHint> enrichments = new ArrayList<EnrichmentHint> ();
    private final String mode;
    private final int enrichmentOnTopScore;
    private final List<String> css = new ArrayList<String>();
    private final List<String> javascript = new ArrayList<String>();
    private final Layout layout;
    private final NavigationConfig navigationConfig;
    
    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of SearchTab */
    public SearchTab(
                final SearchTab inherit,
                final String id,
                final String mode,
                final String key,
                final String parentKey,
                final String rssResultName,
                final boolean rssHidden,
                final int pageSize,
                final NavigationConfig navConf,
                final int enrichmentLimit,
                final int enrichmentOnTop,
                final int enrichmentOnTopScore,
                final Collection<EnrichmentHint> enrichments,
                final String adCommand,
                final int adLimit,
                final int adOnTop,
                final List<String> css,
                final List<String> javascript,
                final boolean absoluteOrdering,
                final boolean displayCss,
                final boolean executeOnBlank,
                final Layout layout){

        this.inherit = inherit;
        this.id = id;

        // rather compact code. simply assigns the property to that pass in, or that from the inherit object, or null/-1
        this.mode = mode != null && mode.trim().length() >0 ? mode : inherit != null ? inherit.mode : null;
        this.key = key != null && key.trim().length() >0 ? key : inherit != null ? inherit.key : null;
        this.parentKey = parentKey != null && parentKey.trim().length() >0
                ? parentKey
                : inherit != null ? inherit.parentKey : null;
        this.pageSize = pageSize >=0 || inherit == null ? pageSize : inherit.pageSize;
        this.navigationConfig = navConf;
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
        this.displayCss = displayCss;
        if(inherit != null){
            // we cannot inherit navigators because there require a live reference to the applicable SearchTabFactory
            // but we do inherit enrichments and css
            this.enrichments.addAll(inherit.enrichments);
            this.css.addAll(inherit.css);            
        }
        this.rssResultName = rssResultName;
        this.css.addAll(css);
        this.javascript.addAll(javascript);
        this.absoluteOrdering = absoluteOrdering;
        this.executeOnBlank = executeOnBlank;
        this.rssHidden = rssHidden;
        this.layout = layout;
    }

    // Getters --------------------------------------------------------

    

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Getter for property pageSize.
     * @return Value of property pageSize.
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * Getter for property pagingSize.
     * @return Value of property pagingSize.
     */
    public int getPagingSize() {
        return this.pagingSize;
    }
    
    /**
     * Getter for property enrichmentLimit.
     * @return Value of property enrichmentLimit.
     */
    public int getEnrichmentLimit() {
        return this.enrichmentLimit;
    }

    /**
     * Getter for property enrichmentOnTop.
     * @return Value of property enrichmentOnTop.
     */
    public int getEnrichmentOnTop() {
        return this.enrichmentOnTop;
    }

    /**
     * Getter for property adCommand.
     * @return Value of property adCommand.
     */
    public String getAdCommand() {
        return this.adCommand;
    }

    /**
     * Getter for property adsLimit.
     * @return Value of property adsLimit.
     */
    public int getAdLimit() {
        return this.adLimit;
    }

    /**
     * Getter for property adOnTop.
     * @return Value of property adOnTop.
     */
    public int getAdOnTop() {
        return this.adOnTop;
    }

    /**
     * Getter for property inherit.
     * @return Value of property inherit.
     */
    public SearchTab getInherit() {
        return this.inherit;
    }

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
     * Getter for property parentKey.
     * @return Value of property parentKey.
     */
    public String getParentKey() {
        return this.parentKey;
    }

    /**
     * Getter for property rssResultName.
     * @return Value of property rssResultName.
     */
    public String getRssResultName() {
        return rssResultName;
    }

    /**
     * Returns true if there should be no visible links to the rss version of this tab.
     * @deprecated Not JavaBean compatable. Use isRssHidden() instead.
     *
     * @return true if hidden.
     */
    public boolean getRssHidden() {
        return rssHidden;
    }
    
    /**
     * Returns true if there should be no visible links to the rss version of this tab.
     *
     * @return true if hidden.
     */
    public boolean isRssHidden() {
        return rssHidden;
    }
    
    /**
     * Getter for property showRss
     * @return 
     * @deprecated Not JavaBean compatable. Use isShowRss() instead.
     */
    public boolean getShowRss() {
        return rssResultName != "" && !getRssHidden();
    }
    
    /**
     * Getter for property showRss
     * @return 
     */
    public boolean isShowRss() {
        return rssResultName != "" && !isRssHidden();
    }

    /**
     * Getter for property absoluteOrdering
     * @return 
     * @deprecated Not JavaBean compatable. Use isAbsoluteOrdering() instead.
     */
    public boolean getAbsoluteOrdering() {
        return absoluteOrdering;
    }

    /**
     * Getter for property executeOnFront
     * @return 
     */
    public boolean isExecuteOnBlank() {
        return executeOnBlank;
    }
    
    /**
     * Getter for property absoluteOrdering
     * @return 
     */
    public boolean isAbsoluteOrdering() {
        return absoluteOrdering;
    }
    
    /**
     * Getter for property displayCss
     * @return 
     */
    public boolean isDisplayCss() {
        return displayCss;
    }    
    
    /**
     * Getter for property enrichments.
     * @return Value of property enrichments.
     */
    public Collection<EnrichmentHint> getEnrichments() {
        return Collections.unmodifiableCollection(enrichments);
    }

    /**
     * 
     * @param command 
     * @return 
     */
    public EnrichmentHint getEnrichmentByCommand(final String command){

        for(EnrichmentHint e : enrichments){
            if(e.getCommand().equals(command)){
                return e;
            }
        }
        return null;
    }

    /**
     * Getter for property mode.
     * @return Value of property mode.
     */
    public String getMode() {
        return this.mode;
    }

    @Override
    public String toString(){
        return id + (inherit != null ? " --> " + inherit.toString() : "");
    }

    /**
     * Getter for property enrichmentScoreOnTop.
     * @return Value of property enrichmentScoreOnTop.
     */
    public int getEnrichmentOnTopScore() {
        return this.enrichmentOnTopScore;
    }

    /**
     * 
     * @return 
     */
    public List<SearchTab> getAncestry(){
        // XXX cache result
        final List<SearchTab> ancestry = new ArrayList<SearchTab>();
        for(SearchTab t = this; t != null; t = t.getInherit()){
            if (t.displayCss) {
                ancestry.add(t);
            }
        }
        Collections.reverse(ancestry);
        return Collections.unmodifiableList(ancestry);
    }

    /**
     * Getter for property css.
     * @return Value of property css.
     */
    public List<String> getCss() {
        return Collections.unmodifiableList(css);
    }

    /**
     * Getter for property javascript.
     * @return Value of property javascript.
     */
    public List<String> getJavascript() {
        return Collections.unmodifiableList(javascript);
    }
    
    /**
     * Getter for property layout.
     * @return Value of property layout.
     */
    public Layout getLayout() {
        return layout;
    }
    
    public NavigationConfig getNavigationConfiguration(){
        return navigationConfig;
    }

    // Inner classes -------------------------------------------------

    /** Immutable POJO holding Enrichment properties from a given tab. **/
    public static final class EnrichmentHint implements Serializable {

        /**
         * 
         * @param rule 
         * @param threshold 
         * @param weight 
         * @param command 
         */
        public EnrichmentHint(
                final String rule,
                final int baseScore,
                final int threshold,
                final float weight,
                final String command,
                final boolean alwaysvisible){

            this.rule = rule;
            this.baseScore = baseScore;
            this.threshold = threshold;
            this .weight = weight;
            this.command = command;
            this.alwaysvisible=alwaysvisible;
        }


        private final String rule;
        private final int baseScore;
        private final int threshold;
        private final String command;
        private final float weight;
        private final boolean alwaysvisible; 

        /**
         * Getter for property rule.
         * @return Value of property rule. Returns null if value equals empty 
         * String("").
         */
        public String getRule() {
            
        	if(this.rule.equalsIgnoreCase(""))return null;
        	else return this.rule;
        }
        
        /**
         * Getter for property baseScore.
         * @return Value of property baseScore.
         */
        public int getBaseScore() {
            return this.baseScore;
        }

        /**
         * Getter for property threshold.
         * @return Value of property threshold.
         */
        public int getThreshold() {
            return this.threshold;
        }

        /**
         * Getter for property command.
         * @return Value of property command.
         */
        public String getCommand() {
            return this.command;
        }

        /**
         * Getter for property weight.
         * @return Value of property weight.
         */
        public float getWeight() {
            return this.weight;
        }

        @Override
        public String toString() {
            return rule + '[' + command + ']';
        }
        
        /**
         * Getter for property alwaysvisible.
         *  alwaysvisible = means that there is no "firstPage" limitation if the
         *  enrichments should be visible or not. 
         * @return Value of property alwaysvisible.
         */
        public boolean isAlwaysvisible(){
        	return this.alwaysvisible;
        }

    }
    
    /** POJO holding layout information for the given tab. 
     * readLayout(Element) is the only way to mutate the bean and can only be called once.
     **/
    public static final class Layout implements Serializable {
        
        private String origin;
        private String main;
        private String front;
        private Map<String,String> includes;
        private Map<String,String> properties;
        
        private Layout(){}
        
        /**
         * 
         * @param inherit 
         */
        public Layout(final Layout inherit){
            if( null != inherit ){
                // origin cannot be inherited!
                main = inherit.main;
                front = inherit.front;
                includes = inherit.includes;
                properties = inherit.properties;
            }
        }
        
        /**
         * 
         * @return 
         */
        public Map<String,String> getIncludes(){
            
            return includes;
        }
        
        /**
         * 
         * @param key 
         * @return 
         */
        public String getInclude(final String key){
            
            return includes.get(key);
        }
        
        /**
         * 
         * @return 
         */
        public Map<String,String> getProperties(){
            return properties;
        }
        
        /**
         * 
         * @param key 
         * @return 
         */
        public String getProperty(final String key){
            return properties.get(key);
        }
        
        /** 
         * @return 
         @deprecated **/
        public String getOrigin(){
            return origin;
        }
        
        /**
         * 
         * @return 
         */
        public String getMain(){
            return main;
        }
        
        /**
         * 
         * @return 
         */
        public String getFront(){
            return front;
        }
        
        /** Will return null when the element argument is null. 
         * Otherwise returns the Layout object deserialised from the contents of the Element.
         ** @param element 
         * @return 
         */
        public Layout readLayout(final Element element){
            
            if( null != origin ){
                throw new IllegalStateException("Not allowed to call readLayout(element) twice");
            }
            if( null != element ){

                origin = element.getAttribute("origin");
                if(0 < element.getAttribute("main").length()){
                    main = element.getAttribute("main");
                }
                if(0 < element.getAttribute("front").length()){
                    front = element.getAttribute("front");
                }
                includes = readMap(includes, element.getElementsByTagName("include"), "key", "template");
                properties = readMap(properties, element.getElementsByTagName("property"), "key", "value");
            }
            
            return null == element ? null : this;
        }
        
        private Map<String,String> readMap(
                final Map<String,String> inherited,
                final NodeList list, 
                final String keyElementName, 
                final String valueElementName){
            
            final Map<String,String> map 
                    = new HashMap<String,String>(null != inherited ? inherited : Collections.<String, String>emptyMap());
            
            for(int i = 0; i< list.getLength(); ++i){
                final Element include = (Element) list.item(i);
                final String key = include.getAttribute(keyElementName);
                map.put(key, include.hasAttribute(valueElementName) ? include.getAttribute(valueElementName) : "");
            }
            return Collections.unmodifiableMap(map);
        }
    }


}
