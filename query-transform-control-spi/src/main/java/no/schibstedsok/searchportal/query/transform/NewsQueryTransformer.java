/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
package no.schibstedsok.searchportal.query.transform;


/**
 * NewsTransformer is part of no.schibstedsok.searchportal.query
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision: 3359 $, $Author: mickw $, $Date: 2006-08-03 10:13:22 +0200 (Thu, 03 Aug 2006) $
 */
public final class NewsQueryTransformer extends AbstractQueryTransformer implements QueryTransformer {


    /**
     *
     * @param config
     */
    public NewsQueryTransformer(final QueryTransformerConfig config){
    }


    /**
     * Add keywords to query to get better searchresults
     *
     * @param originalQuery
     * @return
     */
    public String getTransformedQuery() {

        final String originalQuery = getContext().getTransformedQuery();

       return originalQuery;
    }

    /**
     * Set filter for thiw query.
     * Example to add docdatetime argument
     * <p/>
     * +docdatetime:>2005-10-28
     *
     * @return filterstring
     */
    public String getFilter() {

        final String origQuery = getContext().getTransformedQuery();

        if (origQuery == null) {
            throw new IllegalArgumentException("setQuery not called with minimum empty query");
        }

        if ("".equals(origQuery.trim())) {
            return " +size:>0 ";
        }

        return null;
    }


}
