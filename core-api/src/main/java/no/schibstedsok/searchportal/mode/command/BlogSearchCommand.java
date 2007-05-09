// Copyright (2007) Schibsted Søk AS
/*
 * BlogSearchCommand.java
 *
 * Created on July 11, 2006, 12:34 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.ArrayList;
import java.util.List;

import no.schibstedsok.searchportal.query.UrlClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.result.BlogSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 *
 * Implementation of blog search command.
 *
 * Due to how the blog index is configured queries sent to the index must
 * be applied to two composite fields:
 *
 * A search for "sesam bloggsøk" results in the following query sent to the index:
 *
 * (content:sesam or extended:sesam) and (content:bloggsøk or extended:bloggsøk)
 *
 * @author maek
 */
public final class BlogSearchCommand extends AbstractESPFastSearchCommand {

    /** Creates a new instance of FastSearchCommand
     *
     * @param cxt Search command context.
     */
    public BlogSearchCommand(final Context cxt) {

        super(cxt);
    }


    protected String getSortBy() {

        String sortBy = super.getSortBy();

        if (getParameters().containsKey("userSortBy")) {
                final String userSortBy = getParameter("userSortBy");

                if ("blogdate".equals(userSortBy)) {
                    sortBy = "-publishedtime";
                } else if ("blogrel".equals(userSortBy)) {
                    sortBy = "+blogsearchprofile";
                }
           }
        System.out.println("**** SORT BY = " + sortBy);
        return sortBy;
    }


    // Public --------------------------------------------------------
    public SearchResult execute() {
    	
    	List<BlogSearchResultItem> blogSearchResultItems = new ArrayList<BlogSearchResultItem>();
        final SearchResult result = super.execute();
        
        for(SearchResultItem item : result.getResults()) {
     	    BlogSearchResultItem blogResult = new BlogSearchResultItem();
            String publishedTime = item.getField("publishedtime");

    	    for (Object o : item.getFieldNames()) {
                final String s = (String) o;
                final String v = item.getField(s);
                blogResult.addField(s, v);
            }
    	    
            if (isEpoch(publishedTime)) {
            	publishedTime = item.getField("httpheaderdate");
            }

            // linkTitle, url, creator, comments, body, moreHits, collapseId, fcocount
            item.addField("publishedtime", publishedTime);
            blogResult.setParentUrl(item.getField("parenturl"));
            blogResult.setDisplayDate(publishedTime);
            blogResult.setBlogTitle(item.getField("title"));
            blogResult.setContentTitle(item.getField("contenttitle"));
            blogResult.setBlogTitle(item.getField("title"));
            blogResult.setUrl(item.getField("url"));
            blogResult.setAuthor(item.getField("creator"));
            blogResult.setBody(item.getField("body"));
            blogResult.setMoreHits(item.getField("moreHits"));
            blogResult.setComments(item.getField("comments"));

            blogSearchResultItems.add(blogResult);
            
        }

        // Dont understand this?
        if (getParameter("collapse") != null && !getParameter("collapse").equals("")) {
            final SearchResultItem item = result.getResults().get(0);
            String publishedTime = item.getField("publishedtime");
            if (isEpoch(publishedTime)) {
            	publishedTime = item.getField("httpheaderdate");
            }
            if(!isEpoch(publishedTime)) {
            	result.addField("expandedBlog", item.getField("title"));
            }
        }
        
        result.getResults().clear();
        result.getResults().addAll(blogSearchResultItems);
        return result;
    }


    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null && !getTransformedTerm(clause).trim().equals("")) {
            appendTermRepresentation(getTransformedTerm(clause));
        } else if(null != clause.getField() && null == getFieldFilter(clause)) {
            appendTermRepresentation(escapeFieldedLeaf(clause));
        }
    }

    /**
     * Adds quotes around the URL. Failing so will produce syntax error in filter.
     *
     * @param clause The url clause.
     */
    protected void visitImpl(final UrlClause clause) {
        appendTermRepresentation('"' + getTransformedTerm(clause) + '"');
    }

    private void appendTermRepresentation(final String term) {
        appendToQueryRepresentation("(");
        appendToQueryRepresentation("content:");
        appendToQueryRepresentation(term);
        appendToQueryRepresentation(" or ");
        appendToQueryRepresentation("extended:");
        appendToQueryRepresentation(term);
        appendToQueryRepresentation(")");
    }
    
    private boolean isEpoch(String dateString) {
    	return dateString.startsWith("1970");
    }



}
