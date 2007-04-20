// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;
import org.nfunk.jep.type.Complex;


/** Perform a JEP operation.
 *
 * @author mick
 * @version <tt>$Id$</tt>
 */
public final class NumberOperationHandler implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(NumberOperationHandler.class);

    private final NumberOperationResultHandlerConfig config;
    
    
    public NumberOperationHandler(final ResultHandlerConfig config){
        this.config = (NumberOperationResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        final SearchResult result = cxt.getSearchResult();

        final NumberFormat formatter = NumberFormat.getInstance(datamodel.getSite().getSite().getLocale());
        formatter.setMinimumIntegerDigits(config.getMinDigits());
        formatter.setMaximumIntegerDigits(config.getMaxDigits());
        formatter.setMinimumFractionDigits(config.getMinFractionDigits());
        formatter.setMaximumFractionDigits(config.getMaxFractionDigits());

        final JEP parser = new JEP();

        parser.addStandardConstants();
        parser.addStandardFunctions();
        parser.addComplex();

        for(SearchResultItem item : result.getResults()){

            for(String field : config.getFields()){
                final String value = item.getField(field);
                parser.addVariable(field, value != null && value.length()>0 ? Double.parseDouble(value) : 0D);
            }

            parser.parseExpression(config.getOperation());

            final String r = formatter.format(parser.getValue());
            LOG.debug(config.getOperation() + '=' + r);
            item.addField(config.getTarget(), r);
        }
    }

}
