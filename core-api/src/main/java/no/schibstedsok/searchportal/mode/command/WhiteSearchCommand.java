// Copyright (2006-2007) Schibsted Søk AS
/*
 * WhiteSearchCommand.java
 *
 * Created on March 4, 2006, 1:59 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
import no.schibstedsok.searchportal.query.IntegerClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.PhoneNumberClause;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.util.GeoSearchUtil;

/**
 *
 * @author magnuse
 */
public class WhiteSearchCommand extends CorrectingFastSearchCommand {

    private static final String PREFIX_INTEGER = "whitepages:";
    private static final String PREFIX_PHONETIC = "whitephon:";

    /**
     *
     * @param cxt The context to execute in.
     */
    public WhiteSearchCommand(final Context cxt) {

        super(cxt);
    }
    
    /**
     * @see no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand#getSortBy()
     */
    @Override
    protected String getSortBy(){
        final ParametersDataObject pdo = datamodel.getParameters();
        if(GeoSearchUtil.isGeoSearch(pdo)){
            return GeoSearchUtil.GEO_SORT_BY;
        }
        return super.getSortBy();
    }

    /**
     * Adds non phonetic prefix to integer terms.
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final IntegerClause clause) {
        if (! getTransformedTerm(clause).equals("")) {
            appendToQueryRepresentation(PREFIX_INTEGER);
        }

        super.visitImpl(clause);
    }

    /**
     * Adds non phonetic prefix to phone number terms.
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final PhoneNumberClause clause) {
        if (! getTransformedTerm(clause).equals("")) {
            appendToQueryRepresentation(PREFIX_INTEGER);
        }
        super.visitImpl(clause);
    }
    /**
     * Adds phonetic prefix to a leaf clause.
     * Remove dots from words. (people, street, suburb, or city names do not have dots.)
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final LeafClause clause) {

        if (null == clause.getField()) {
            if (!getTransformedTerm(clause).equals("")) {
                appendToQueryRepresentation(PREFIX_PHONETIC
                        + getTransformedTerm(clause).replaceAll("\\.|-", QL_AND + PREFIX_PHONETIC));
            }

        }else if(null == getFieldFilter(clause)){

            if (!getTransformedTerm(clause).equals("")) {
                // we also accept terms with fields that haven't been permitted for the searchConfiguration
                appendToQueryRepresentation(PREFIX_PHONETIC
                        + clause.getField() + "\\:"
                        + getTransformedTerm(clause).replaceAll("\\.|-", QL_AND + PREFIX_PHONETIC));

            }

        }
    }
    
    /**
     * If the search is a GEO search, add required GEO search parameters.
     * @see no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand#setAdditionalParameters(ISearchParameters)
     */
    protected void setAdditionalParameters(ISearchParameters params) {
        super.setAdditionalParameters(params);
        final ParametersDataObject pdo = datamodel.getParameters();
       
        if(!GeoSearchUtil.isGeoSearch(pdo)){
            return;
        }
     
        final String center = GeoSearchUtil.getCenter(pdo);
 
        params.setParameter(new SearchParameter("qtf_geosearch:unit", GeoSearchUtil.RADIUS_MEASURE_UNIT_TYPE));
        params.setParameter(new SearchParameter("qtf_geosearch:radius", GeoSearchUtil.getRadiusRestriction(pdo)));
        params.setParameter(new SearchParameter("qtf_geosearch:center", center));
    }

    /**
     * An implementation that ignores phrase searches.
     *
     * Visits only the left clause, unless that clause is a phrase or organisation clause, in
     * which case only the right clause is visited. Phrase and organisation searches are not
     * possible against the white index.
     */
    protected void visitXorClause(final Visitor visitor, final XorClause clause) {

        // If we have a match on an international phone number, but it is not recognized as
        // a local phone number, force it to use the original number string.
        switch(clause.getHint()){
        case FULLNAME_ON_LEFT:
        case NUMBER_GROUP_ON_LEFT:
        case PHRASE_ON_LEFT:
            clause.getSecondClause().accept(visitor);
            break;
            
        case PHONE_NUMBER_ON_LEFT:
            if (!clause.getFirstClause().getKnownPredicates().contains(TokenPredicate.PHONENUMBER)) {
                clause.getSecondClause().accept(visitor);
                break;
            }
        default:
            super.visitXorClause(visitor, clause);
            break;
        }
    }
}
