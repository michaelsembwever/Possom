/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.result;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.result.handler.PhoneNumberChooser;
import no.schibstedsok.searchportal.result.handler.PhoneNumberFormatter;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.view.config.SearchTab;


import no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.result.handler.PhoneNumberChooserResultHandlerConfig;
import no.schibstedsok.searchportal.result.handler.PhoneNumberFormatterResultHandlerConfig;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.BytecodeLoader;
import no.schibstedsok.searchportal.site.config.UrlResourceLoader;
import org.apache.log4j.Logger;

public final class YellowSearchResult<T extends ResultItem> extends FastSearchResult<T> {

    private int pseudoLocalHitCount;
    private final Collection<T> pseudoLocalResults = new ArrayList<T>();
    private final FastSearchResult localResult;
    private final FastSearchResult pseudoLocalResult;
    private final boolean local;
    private final FastSearchResult top3;
    private int addedTop3 = 0;
    private final String query;

    private static final Logger LOG = Logger.getLogger(YellowSearchResult.class);

    public YellowSearchResult(
            final AbstractSimpleFastSearchCommand command, 
            final FastSearchResult localResult, 
            final FastSearchResult pseudoLocalResult, 
            final FastSearchResult top3, 
            final boolean local,
            final String query) {
        
        super(command);
        this.localResult = localResult;
        this.pseudoLocalResult = pseudoLocalResult;
        this.top3 = top3;
        this.local = local;
        this.query = query;

        final List resultToAlter = getResults();

        final List resultsToAdd = new ArrayList();

        for (final Iterator iter = top3.getResults().iterator(); iter.hasNext();) {
            final ResultItem item = (ResultItem) iter.next();
            
            if (Integer.parseInt(item.getField("rank")) > 100000) {

                if (localResult != null && localResult.getResults().contains(item)) {
                    localResult.getResults().remove(item);
                }
                
                if (pseudoLocalResults != null && pseudoLocalResult.getResults().contains(item)) {
                    pseudoLocalResult.getResults().remove(item);
                }
                
                resultsToAdd.add(item);
                ++addedTop3;
            }
        }

        resultToAlter.addAll(0, resultsToAdd);

        while (resultToAlter.size() > 10) {
            resultToAlter.remove(resultToAlter.size() - 1);
        }
    }

    private FastSearchResult getDelegator() {
        if (local) {
            return localResult;
        } else {
            return pseudoLocalResult;
        }
    }

    private FastSearchResult getOtherDelegator() {
        if (!local) {
            return localResult;
        } else {
            return pseudoLocalResult;
        }
    }

    public void setPseudoLocalHitCount(final int hitCount) {
        this.pseudoLocalHitCount = hitCount;
    }

    /**
     * Get the pseudoLocalHitCount.
     *
     * @return the pseudoLocalHitCount.
     */
    public int getPseudoLocalHitCount() {
        return pseudoLocalHitCount;
    }

    public Collection<T> getPseudoLocalResults() {
        return pseudoLocalResults;
    }

    public void addPseudoLocalResult(final T item) {
        pseudoLocalResults.add(item);
    }

    public List getResults() {
        return getDelegator().getResults();
    }

    public Modifier getModifier(final String navigatorName, final String modifierName) {
        return getDelegator().getModifier(navigatorName, modifierName);
    }

    public int getModifierCount(final String navigatorName, final String modifierName) {
        return getDelegator().getModifierCount(navigatorName, modifierName);
    }

    public List getModifiers(final String navigatorName) {
        return getDelegator().getModifiers(navigatorName);
    }

    public int getHitCount() {
        return getDelegator().getHitCount();
    }

    public String getField(final String name) {
        return getDelegator().getField(name);
    }

    public YellowSearchResult addField(final String name, final String value) {
        getDelegator().addField(name, value);
        return this;
    }
    
    public FastSearchResult getLocalResult() {
        return localResult;
    }

    public int getSecondaryHitCount() {
        return getOtherDelegator().getHitCount();
    }

    public int getAllHitCount() {
        int hits = 0;

        if (localResult != null) {
            hits += localResult.getHitCount();
        }

        if (pseudoLocalResult != null) {
            hits += pseudoLocalResult.getHitCount();
        }

	hits += addedTop3;

        return hits;
    }

    /** Not a unit testable method because of the UrlResourceLoader calls. 
     * @deprecated creates a malformed ResultHandler.Context. Needs to be rewritten.
     **/
    public List<ResultItem> getSecondaryHits() {

        final ResultHandler.Context resultHandlerContext = new ResultHandler.Context() {
                public ResultList<ResultItem> getSearchResult() {
                    return pseudoLocalResult;
                }
                public Site getSite() {
                    return Site.DEFAULT; // FIXME !!! Needs to work on a per Skin basis.
                }
                public PropertiesLoader newPropertiesLoader(
                        final SiteContext siteCxt, 
                        final String resource, 
                        final Properties properties) {
                    
                    return UrlResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
                }
                public DocumentLoader newDocumentLoader(
                        final SiteContext siteCxt, 
                        final String resource,  
                        final DocumentBuilder builder) {
                    
                    return UrlResourceLoader.newDocumentLoader(siteCxt, resource, builder);
                }
                public BytecodeLoader newBytecodeLoader(final SiteContext context, final String className) {
                    return UrlResourceLoader.newBytecodeLoader(context, className);
                }       
                public void addSource(final Modifier modifier) {
                    command.getRunningQuery().addSource(modifier);
                }
                public SearchTab getSearchTab(){
                    return command.getRunningQuery().getSearchTab();
                }
                public SearchConfiguration getSearchConfiguration(){
                    return null; // better not be used !!
                }
            };

        final PhoneNumberChooser chooser = new PhoneNumberChooser(new PhoneNumberChooserResultHandlerConfig());
        chooser.handleResult(resultHandlerContext, null);

        final PhoneNumberFormatter formatter = new PhoneNumberFormatter(new PhoneNumberFormatterResultHandlerConfig());
        formatter.handleResult(resultHandlerContext, null);

        final Iterator pResults = pseudoLocalResult.getResults().iterator();

        final List<ResultItem> secondaryHits = new ArrayList<ResultItem>();

        while (pResults.hasNext() && localResult.getHitCount() + secondaryHits.size() < 10) {
            final ResultItem item = (ResultItem) pResults.next();

            if (!localResult.getResults().contains(item)) {
                secondaryHits.add(item);
            }
        }


        return secondaryHits;
    }

    /**
     * Get the local.
     *
     * @return the local.
     */
    public boolean isLocal() {
        return local;
    }
}
