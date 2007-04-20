// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;


/**
 * TvSearchSortingHandler sorts the result by channel, day or category
 * @author ajamtli
 * @version $Id$
 */
@Controller("TvSearchSorting")
public final class TvsearchSortingResultHandlerConfig extends AbstractResultHandlerConfig {

    /** Number of results per block of channels, days or categories. **/
    private int resultsPerBlock;

    /** Number of blocks to display per page. **/
    private int blocksPerPage;

    /**
     * 
     * @return 
     */
    public int getResultsPerBlock() {
        return resultsPerBlock;
    }

    /**
     * 
     * @param resultsPerBlock 
     */
    public void setResultsPerBlock(int resultsPerBlock) {
        this.resultsPerBlock = resultsPerBlock;
    }

    /**
     * 
     * @return 
     */
    public int getBlocksPerPage() {
        return blocksPerPage;
    }

    /**
     * 
     * @param blocksPerPage 
     */
    public void setBlocksPerPage(int blocksPerPage) {
        this.blocksPerPage = blocksPerPage;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        
        super.readResultHandler(element);
        
        setResultsPerBlock(Integer.parseInt(element.getAttribute("results-per-block")));
        setBlocksPerPage(Integer.parseInt(element.getAttribute("blocks-per-page")));
        
        return this;
    }
    
    

}
