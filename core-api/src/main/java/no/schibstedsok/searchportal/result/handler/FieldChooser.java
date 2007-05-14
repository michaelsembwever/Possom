// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import org.apache.log4j.Logger;

import java.util.Collection;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class FieldChooser implements ResultHandler {
    
    private static final Logger LOG = Logger.getLogger(FieldChooser.class);

    private final FieldChooserResultHandlerConfig config;

    /**
     * 
     * @param config 
     */
    public FieldChooser(final ResultHandlerConfig config) {
        this.config = (FieldChooserResultHandlerConfig) config;
    }

    public void handleResult(final Context cxt, final DataModel datamodel) {

        final Collection<String> fields = config.getFields();
        final ResultList<ResultItem> searchResult = cxt.getSearchResult();
        chooseField(searchResult, fields);
    }

    private void chooseField(final ResultList<ResultItem> searchResult, final Collection<String> fields) {
        
        if (searchResult != null) {
            
            for (ResultItem i : searchResult.getResults()) {
                
                ResultItem item = i;
                
                for (String field : fields) {
                    if (item.getField(field) != null) {
                        item = item.addField(config.getTargetField(), item.getField(field));
                        break;
                    }
                }
                
                if (config.getDefaultValue() != null && item.getField(config.getTargetField()) == null) {
                    item = item.addField(config.getTargetField(), config.getDefaultValue());
                }
                
                if (item instanceof ResultList<?>) {                    
                    chooseField((ResultList<ResultItem>)item, fields);
                }
                
                searchResult.replaceResult(i, item);
            }
            
        }
    }


}
