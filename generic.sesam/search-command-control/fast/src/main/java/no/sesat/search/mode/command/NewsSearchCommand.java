/* Copyright (2006-2008) Schibsted Søk AS
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
 * NewsSearchCommand.java
 *
 * Created on March 7, 2006, 5:31 PM
 *
 */

package no.sesat.search.mode.command;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.Visitor;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.token.TokenPredicate;

/**
 *
 *
 * @version $Id$
 */
public class NewsSearchCommand extends FastSearchCommand {

    // Filter used to get all articles.
    private static final String FAST_SIZE_HACK = " +size:>0";

    /** Creates a new instance of NewsSearchCommand
     *
     * @param cxt Search command context.
     */
    public NewsSearchCommand(final Context cxt) {

        super(cxt);
    }

    private StringBuilder filterBuilder = null;

    /**
     *
     * @param clause The clause to examine.
     */
    @Override
    protected void visitXorClause(final Visitor visitor, final XorClause clause) {
        switch(clause.getHint()){
            case FULLNAME_ON_LEFT:
            case PHRASE_ON_LEFT:
                // Web searches should use phrases over separate words.
                clause.getFirstClause().accept(visitor);
                break;
            default:
                // All other high level clauses are ignored.
                clause.getSecondClause().accept(visitor);
                break;
        }
    }

    /**
     * LeafClause
     *
     * A leaf clause with a site field does not add anything to the query. Also
     * if the query just contains the prefix do not output anything.
     *
     */
    @Override
    protected void visitImpl(final LeafClause clause) {
        if (!  containsJustThePrefix() ) {
            super.visitImpl(clause);
        }
    }

    @Override
    protected String getAdditionalFilter() {
        synchronized (this) {
            if (filterBuilder == null) {
                filterBuilder = new StringBuilder(super.getAdditionalFilter());

                // <-start- TODO this needs to be put into its own NewsSearchCOmmand.getAdditionalFilter() in genericse.seam.se
                if ("se".equals(getSearchConfiguration().getProject())) {
                    // Add filter to retrieve all documents.
                    if (containsJustThePrefix() || getTransformedQuery().equals("")) {
                            filterBuilder.append(FAST_SIZE_HACK);
                    }

                    if (!getSearchConfiguration().isIgnoreNavigation()) {

                        final String contentSource = getParameter("contentsource");
                        final String newsCountry = getParameter("newscountry");

                        // AAhhrghh. Need to provide backwards compatibility.
                        // People are linking us using contentsource="Norske nyheter"
                        if (contentSource != null && !contentSource.equals("")) {
                            if (contentSource.equals("Norske nyheter")) {
                                filterBuilder.append(" +newscountry:Norge");
                            } else {
                                filterBuilder.append(" +contentsource:"+ contentSource);
                            }
                        }
                        if (newsCountry != null && !newsCountry.equals("")) {
                            filterBuilder.append(" +newscountry:"+ newsCountry);
                        }
                    } // -end->

                // need a date clause in the query to get the last 50 dates for the lastnews navigator
                } else if ("lastnews".equals(getSearchConfiguration().getProject())) {
                    GregorianCalendar calendar = new java.util.GregorianCalendar();
                    calendar.add( java.util.Calendar.DATE, -49 );
                    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    String newsdate = formatter.format(calendar.getTime());

                    final String contentSource = getParameter("contentsource");
                    final String newsCountry = getParameter("newscountry");
                    final String newsSource = getParameter("newssource");
                    final String language = getParameter("language");

                    if (!contentSource.equals("Mediearkivet")) {
                        if (contentSource != null && !contentSource.equals("")) {
                            if (contentSource.equals("Norske nyheter")) {
                                filterBuilder.append(" AND newscountry:Norge");
                            } else {
                                filterBuilder.append(" AND contentsource:\""+ contentSource + "\"");
                            }
                        } else {
                            if (newsCountry != null && !newsCountry.equals(""))
                                filterBuilder.append(" AND newscountry:\""+ newsCountry + "\"");
                            else // for newscount navigator
                                filterBuilder.append(" AND newscountry:Norge");
                        }
                        if (language != null && !language.equals("")) {
                            filterBuilder.append(" AND language:\"" + language + "\"");
                        }
                        if (newsSource != null && !newsSource.equals("")) {
                            filterBuilder.append(" AND newssource:\"" + newsSource + "\"");
                        }
                        filterBuilder.append(" ANDNOT meta.collection:mano");
                        filterBuilder.append(" AND docdatetime:>" + newsdate);

                    // PAPERNEWS:
                    } else {
                        filterBuilder.append(" AND contentsource:" + contentSource);
                        filterBuilder.append(" AND docdatetime:>" + newsdate);
                    }

                } else {
                    GregorianCalendar calendar = new java.util.GregorianCalendar();
                    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    calendar.add( java.util.Calendar.MONTH, -24 );

                    String newsdate = formatter.format(calendar.getTime());

                    if (!getSearchConfiguration().isIgnoreNavigation()) {

                        final String contentSource = getParameter("contentsource");
                        final String newsCountry = getParameter("newscountry");
                        final String newsSource = getParameter("newssource");

                        // general rule is to display news fresher than 2 years, but with exceptions for:
                        // "norske papiraviser" -> display for all years
                        // certain newssources (as listed below) -> display for all years
                        if (!contentSource.equals("Mediearkivet")) {

                            // AAhhrghh. Need to provide backwards compatibility.
                            // People are linking us using contentsource="Norske nyheter"
                            if (contentSource != null && !contentSource.equals("")) {
                                if (contentSource.equals("Norske nyheter")) {
                                    filterBuilder.append(" AND newscountry:Norge");
                                } else {
                                    filterBuilder.append(" AND contentsource:\""+ contentSource + "\"");
                                }
                            } else {
                                if (newsCountry != null && !newsCountry.equals(""))
                                    filterBuilder.append(" AND newscountry:\""+ newsCountry + "\"");
                                else // for newscount navigator
                                    filterBuilder.append(" AND newscountry:Norge");
                            }
                            filterBuilder.append(" ANDNOT meta.collection:mano");
                            filterBuilder.append(" AND ( docdatetime:>" + newsdate);
                            filterBuilder.append(" OR newssource:Digi.no");
                            filterBuilder.append(" OR newssource:DinSide");
                            filterBuilder.append(" OR newssource:ITavisen");
                            filterBuilder.append(" OR newssource:iMarkedet");
                            filterBuilder.append(" OR newssource:Propaganda )");
                        // PAPERNEWS:
                        } else {
                            filterBuilder.append(" AND contentsource:" + contentSource);
                        }
                    } else {
                        filterBuilder.append(" AND (docdatetime:>" + newsdate);
                        filterBuilder.append(" OR newssource:Digi.no");
                        filterBuilder.append(" OR newssource:DinSide");
                        filterBuilder.append(" OR newssource:ITavisen");
                        filterBuilder.append(" OR newssource:iMarkedet");
                        filterBuilder.append(" OR newssource:Propaganda ");
                        filterBuilder.append(" OR meta.collection:mano )");
                    }
                }
            }
        }
        return filterBuilder.toString();
    }

    private boolean containsJustThePrefix() {

        final LeafClause firstLeaf = getQuery().getFirstLeafClause();

        return getQuery().getRootClause() == firstLeaf
          && (firstLeaf.getKnownPredicates().contains(TokenPredicate.Categories.NEWS_MAGIC)
              || firstLeaf.getPossiblePredicates().contains(TokenPredicate.Categories.NEWS_MAGIC));
    }

}
