package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.apache.log4j.Logger;

/**
 * It the search is hit in body(contains <b>) we leave the field untouched.
 * If the search does not match in body, we replace the body-field with
 * aggreggdisplayvalue alias intro alias staticleadin. This can be configured
 * in modes.xml through the source variable.
 *
 * However, this is a special case in newssearch so this class is probably not
 * very reusable.
 *
 * See jira: AGGREG-514
 *
 * <p/>
 * Created: Jun 13, 2007 1:09:12 PM
 * Author: Ola MH Sagli <a href="ola@sesam.no">ola at sesam.no</a>
 */
public class NewsSearchSetBodyResultHandler implements ResultHandler {

    private final static Logger LOG = Logger.getLogger(NewsSearchSetBodyResultHandler.class);

    /* Where to fetch alternative text from */
    private String source = null;
    
    /**
     * @param config the config
     */
    public NewsSearchSetBodyResultHandler(final ResultHandlerConfig config) {
        source = ((NewsSearchSetBodyResultHandlerConfig)config).getSource();
    }

    public void handleResult(Context cxt, DataModel datamodel) {
        ResultList<ResultItem> searchResult = cxt.getSearchResult();
        setDocumentSummary(searchResult);
        //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * @param searchResult
     */
    private void setDocumentSummary(final ResultList<ResultItem> searchResult) {

        if (searchResult == null || searchResult.getResults().size() == 0) {
//            LOG.debug("Search is empty");
            return;
        }

        for (ResultItem searchResultItem : searchResult.getResults()) {
            if (searchResultItem instanceof ResultList<?>) {
                 // Hope this works !Cutnpaste from DocumentSummaryResultHandler...
                setDocumentSummary((ResultList<ResultItem>) searchResultItem);
            }

            String body = searchResultItem.getField("body");
            String intro = searchResultItem.getField(source);

            if(!isMatchInString(body)) {
                searchResult.replaceResult(
                    searchResultItem,
                    searchResultItem.addField("body", intro));
            }
        }

    }

    /* We have a match in the string if it contains <b> */
    private boolean isMatchInString(String s) {
        if(s == null) {
            return false;
        }
        return s.indexOf("<b>") != -1;
    }
}
