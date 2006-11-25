/*
 * YellowGeoSearchCommand.java
 *
 * Created on 17. august 2006, 10:41
 *
 */

package no.schibstedsok.searchportal.mode.command;
import java.util.Map;

/**
 *
 * @author ssthkjer
 */
public class YellowGeoSearchCommand extends YellowSearchCommand {
    
    private String additionalFilter;
    
    /** Creates a new instance of YellowGeoSearchCommand */
    public YellowGeoSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }
    
    protected String getSortBy() {
        return getSearchConfiguration().getSortBy();
    }    
       
}
