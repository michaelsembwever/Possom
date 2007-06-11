// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import java.text.NumberFormat;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.BasicResultItem;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;


/** Perform a JEP operation.
 *
 * @author mick
 * @version <tt>$Id$</tt>
 */
public final class NumberOperationHandler implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(NumberOperationHandler.class);

    private final NumberOperationResultHandlerConfig config;
    
    
    /**
     * 
     * @param config 
     */
    public NumberOperationHandler(final ResultHandlerConfig config){
        this.config = (NumberOperationResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        final ResultList<ResultItem> result = cxt.getSearchResult();

        final NumberFormat formatter = NumberFormat.getInstance(datamodel.getSite().getSite().getLocale());
        formatter.setMinimumIntegerDigits(config.getMinDigits());
        formatter.setMaximumIntegerDigits(config.getMaxDigits());
        formatter.setMinimumFractionDigits(config.getMinFractionDigits());
        formatter.setMaximumFractionDigits(config.getMaxFractionDigits());

        final JEP parser = new JEP();

        parser.addStandardConstants();
        parser.addStandardFunctions();
        parser.addComplex();

        for(ResultItem i : result.getResults()){

            final BasicResultItem item = (BasicResultItem) i;
            for(String field : config.getFields()){
                final String value = (String) item.getField(field);
                parser.addVariable(field, value != null && value.length()>0 ? Double.parseDouble(value) : 0D);
            }

            parser.parseExpression(config.getOperation());

            final String r = formatter.format(parser.getValue());
            LOG.debug(config.getOperation() + '=' + r);
            item.addField(config.getTarget(), r);
        }
    }

}
