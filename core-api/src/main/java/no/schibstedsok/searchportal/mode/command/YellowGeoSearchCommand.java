/*
 * YellowGeoSearchCommand.java
 *
 * Created on 17. august 2006, 10:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
