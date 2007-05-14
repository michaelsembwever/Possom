// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;

/**
 * DateFormatHandler is part of no.schibstedsok.searchportal.result
 * Transform fast inputdate to how it will be displayed in tv enrichment . Tv Enrichment
 * will most likely only display Hour of day.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version $Id$
 */
public final class DateFormatHandler implements ResultHandler {

    /**
     * 
     */
    public enum Fields {
        /**
         * 
         */
        YEAR,
        /**
         * 
         */
        MONTH,
        /**
         * 
         */
        DAY,
        /**
         * 
         */
        HOUR,
        /**
         * 
         */
        MINUTE,
        /**
         * 
         */
        SECOND;
    }
    
    private final DateFormatResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public DateFormatHandler(final ResultHandlerConfig config){
        this.config = (DateFormatResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final ResultItem item : cxt.getSearchResult().getResults()) {

            final String docDateTime = item.getField(config.getSourceField());

            if (docDateTime != null) {
                
                final String year = docDateTime.substring(0,4);
                final String month = docDateTime.substring(5,7);
                final String day = docDateTime.substring(8,10);
                final String hour = docDateTime.substring(11, 13);
                final String minute = docDateTime.substring(14, 16);
                final String second = docDateTime.substring(17, 19);
                
                cxt.getSearchResult().replaceResult(item, 
                    item.addField(config.getFieldPrefix() + Fields.YEAR.name(), year)
                            .addField(config.getFieldPrefix() + Fields.MONTH.name(), month)
                            .addField(config.getFieldPrefix() + Fields.DAY.name(), day)
                            .addField(config.getFieldPrefix() + Fields.HOUR.name(), hour)
                            .addField(config.getFieldPrefix() + Fields.MINUTE.name(), minute)
                            .addField(config.getFieldPrefix() + Fields.SECOND.name(), second)
                    );
            }

        }
    }
}
