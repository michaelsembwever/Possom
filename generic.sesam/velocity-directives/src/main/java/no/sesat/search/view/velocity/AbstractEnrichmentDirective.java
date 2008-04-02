/* Copyright (2007) Schibsted Søk AS
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
 */
package no.sesat.search.view.velocity;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.log4j.Logger;

import java.io.Writer;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.search.SearchDataObject;
import no.sesat.search.result.ResultList;
import no.sesat.search.site.Site;
import no.sesat.search.view.config.SearchTab;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.velocity.Template;
import static no.sesat.search.view.config.SearchTab.EnrichmentHint.*;
import org.apache.velocity.app.VelocityEngine;

/**
 * Handles presenting the enrichments
 *
 * The first argument allow inclusion/exclusion of each enrichment according to the subclasses implementation of
 *  placementCorrect(tab, placement, i, e)      <br/><br/>
 *
 * The second argument specifies the a string to use in beginning wrapping around each enrichment.
 * a third argument is expected for end wrapping.
 *  If no argument is specified no div is written around each enrichment.)      <br/><br/>
 * 
 * The enrichments that are rendered are those named as the results field for EnrichmentHint.NAME_KEY within the
 * "templates/enrichments/${placement}/" directory. If this is not found then the same named template 
 * within the "templates/enrichments/" directory is used.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mick Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractEnrichmentDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(AbstractEnrichmentDirective.class);
    private static final Logger PRODUCT_LOG = Logger.getLogger("no.sesat.Product");

    // subclasses have to copy this
    private static final String NAME = "enrichments";

    /**
      * {@inheritDoc}
     */
    public String getName() {
        return NAME;
    }

    /**
      * {@inheritDoc}
      */
    public int getType() {
        return LINE;
    }

    /**
     * @param cxt The cxt.
     * @param writer The writer.
     * @param node The node.
     *
     * @return return true on success.
     *
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     */
    public boolean render(final InternalContextAdapter cxt, final Writer writer, final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if (1 > node.jjtGetNumChildren() && 3 < node.jjtGetNumChildren()) {
            LOG.error("#" + getName() + " - wrong count of arguments");
            return false;
        }

        final String placement = getArgument(cxt, node, 0);

        if(null != getDataModel(cxt).getPage() && null != getDataModel(cxt).getPage().getCurrentTab()){
            
            final SearchTab tab = getDataModel(cxt).getPage().getCurrentTab();

            final Site site = getDataModel(cxt).getSite().getSite();
            
            final VelocityEngine engine = VelocityEngineFactory
                    .valueOf(getDataModel(cxt).getSite().getSite())
                    .getEngine();

            final boolean enrichmentsExisting = null != cxt.get("enrichments");

            @SuppressWarnings("unchecked")
            final Set<ResultList> enrichments = enrichmentsExisting
                    ? (Set<ResultList>)cxt.get("enrichments")
                    : new TreeSet<ResultList>(new Comparator<ResultList>(){
                
                public int compare(ResultList o1, ResultList o2) {
                    // highest scores first, ie descending order.
                    final int result = (int)((Float)o2.getObjectField(SCORE_KEY) - (Float)o1.getObjectField(SCORE_KEY));
                    // never return zero. in a treeset it means overriding the other enrichment.
                    return 0 != result 
                            ? result 
                            : String.CASE_INSENSITIVE_ORDER.compare(o1.getField(NAME_KEY), o2.getField(NAME_KEY)); 
                }
            });

            if(!enrichmentsExisting){
                for(SearchDataObject search : getDataModel(cxt).getSearches().values()){
                    if(null != search.getResults().getObjectField(HINT_KEY)){
                        enrichments.add(search.getResults());
                    }
                }
                cxt.put("enrichments", enrichments);
            }

            // we check later if this has already been written for this request
            final StringBuilder log = enrichmentsExisting ? null : new StringBuilder(
                    "<enrichments mode=\"" + tab.getKey()
                    + "\" size=\"" + enrichments.size() + "\">"
                    + "<query>" + getDataModel(cxt).getQuery().getXmlEscaped() + "</query>");

            int i = 0;
            for(ResultList e : enrichments){

                // product log
                if(!enrichmentsExisting){
                    log.append("<enrichment name=\"" + e.getField(NAME_KEY)
                            + "\" score=\"" + e.getObjectField(SCORE_KEY) + "\"/>");
                }


                // enrichments
                if (placementCorrect(getDataModel(cxt), placement, i, e)){

                    if(3 == node.jjtGetNumChildren()){
                        writer.append(getArgument(cxt, node, 1));
                    }

                    cxt.put("commandName", e.getField(NAME_KEY));
                    
                    Template template = null;
                    try{
                        template = VelocityEngineFactory
                                .getTemplate(engine, site, "/enrichments/" + placement + '/' + e.getField(NAME_KEY));
                        
                    }catch(ResourceNotFoundException rnfe){
                        LOG.debug(rnfe.getMessage(), rnfe); // not important
                        
                        template = VelocityEngineFactory
                                .getTemplate(engine, site, "/enrichments/" + e.getField(NAME_KEY));
                    }
                    
                    template.merge(cxt, writer);

                    if(3 == node.jjtGetNumChildren()){
                        writer.append(getArgument(cxt, node, 2));
                    }
                }
                ++i;
            }

            if(!enrichmentsExisting){
                PRODUCT_LOG.info(log.toString() + "</enrichments>");
            }

            return true;
        }
        return false;
    }

    protected abstract boolean placementCorrect(DataModel datamodel, String placement, int i, ResultList e);

}
