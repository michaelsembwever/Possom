/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.transform;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * A transformer to apply a regular expression to each term.
 * TODO Does not handle multi-term applications yet.
 *
 * If the regular expression has a capturing group,
 * it is only that group that is replacement,
 * not the match to the whole regular expression.
 * <b>It is therefore critical to use non-capturing groups for |?+* operations in the expressions.</b>
 *
 * @version $Id: RegExpQueryTransformer.java 4223 2006-12-22 12:11:49Z ssmiweve $
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 *
 */
public final class RegExpQueryTransformer extends AbstractQueryTransformer {

    private static final Logger LOG = Logger.getLogger(RegExpQueryTransformer.class);

    private static final String DEBUG_APPLIED_REGEXP = "Applied regexp to term ";

    private volatile Pattern regExPattern;
    /**
     * Holds value of property regexp.
     */
    private String regexp;
    /**
     * Holds value of property replacement.
     */
    private String replacement = "";

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final LeafClause clause) {

        final String term = (String) getTransformedTerms().get(clause);
        if(null != term && term.length()>0){
            if(regExPattern == null){
                regExPattern = Pattern.compile(regexp);
            }
            final Matcher m = regExPattern.matcher(term);
            if(m.find()){
                LOG.debug(DEBUG_APPLIED_REGEXP + term);
                getTransformedTerms().put(clause, m.replaceAll(replacement));
            }
        }
    }

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final DoubleOperatorClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }



    private Map<Clause,String> getTransformedTerms() {
        return getContext().getTransformedTerms();
    }

    /** TODO comment me. **/
    @Override
    public Object clone() throws CloneNotSupportedException {
        final RegExpQueryTransformer retValue = (RegExpQueryTransformer)super.clone();
        retValue.regexp = regexp;
        retValue.replacement = replacement;
        return retValue;
    }

    @Override
    public QueryTransformer readQueryTransformer(final Element qt){
        
        super.readQueryTransformer(qt);
        AbstractDocumentFactory.fillBeanProperty(this, null, "regexp", ParseType.String, qt, "");
        AbstractDocumentFactory.fillBeanProperty(this, null, "replacement", ParseType.String, qt, "");
        return this;
    }
    
    /**
     * Setter for property regexp.
     * @param regexp New value of property regexp.
     */
    public void setRegexp(final String regexp) {
        this.regexp = regexp;
    }

    /**
     * Setter for property replacement.
     * @param replacement New value of property replacement.
     */
    public void setReplacement(final String replacement) {
        this.replacement = replacement;
    }
}
