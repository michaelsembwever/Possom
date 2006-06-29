// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import no.schibstedsok.front.searchportal.result.SearchResult;
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

    private Collection<String> fields = new ArrayList<String>();

    public void handleResult(final Context cxt, final Map parameters) {

        final SearchResult result = cxt.getSearchResult();
        
        final JEP parser = new JEP();

        parser.addStandardConstants();
        parser.addStandardFunctions();
        parser.addComplex();
        for(String field : fields){
            final String value = result.getField(field);
            parser.addVariable(field, value != null && value.length()>0 ? Double.parseDouble(value) : 0D);
        }

        parser.parseExpression(operation);
        final NumberFormat formatter = NumberFormat.getInstance(cxt.getSite().getLocale());
        formatter.setMinimumIntegerDigits(minDigits);
        formatter.setMaximumIntegerDigits(maxDigits);
        formatter.setMinimumFractionDigits(minFractionDigits);
        formatter.setMaximumFractionDigits(maxFractionDigits);
        
        final String r = formatter.format(parser.getValue());
        LOG.debug(operation + '=' + r);
        result.addField(target, r);
    }

    public void addField(final String field) {
        fields.add(field);
    }

    /**
     * Holds value of property operation.
     */
    private String operation;

    /**
     * Setter for property operation.
     * @param operation New value of property operation.
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Holds value of property target.
     */
    private String target;

    /**
     * Setter for property target.
     * @param target New value of property target.
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Holds value of property minFractionDigits.
     */
    private int minFractionDigits;

    /**
     * Setter for property minFractionDigits.
     * @param minFractionDigits New value of property minFractionDigits.
     */
    public void setMinFractionDigits(int minFractionDigits) {
        this.minFractionDigits = minFractionDigits;
    }

    /**
     * Holds value of property maxFractionDigits.
     */
    private int maxFractionDigits;

    /**
     * Setter for property maxFractionDigitis.
     * @param maxFractionDigitis New value of property maxFractionDigitis.
     */
    public void setMaxFractionDigits(int maxFractionDigits) {
        this.maxFractionDigits = maxFractionDigits;
    }

    /**
     * Holds value of property minDigits.
     */
    private int minDigits;

    /**
     * Setter for property minDigits.
     * @param minDigits New value of property minDigits.
     */
    public void setMinDigits(int minDigits) {
        this.minDigits = minDigits;
    }

    /**
     * Holds value of property maxDigits.
     */
    private int maxDigits;

    /**
     * Setter for property maxDigits.
     * @param maxDigits New value of property maxDigits.
     */
    public void setMaxDigits(int maxDigits) {
        this.maxDigits = maxDigits;
    }
}
