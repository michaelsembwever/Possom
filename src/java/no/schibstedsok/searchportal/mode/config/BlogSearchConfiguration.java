
/*
 * BlogSearchConfiguration.java
 *
 * Created on July 12, 2006, 10:35 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.config;

/**
 *
 * @author maek
 */
public class BlogSearchConfiguration extends NavigatableAdvancedFastConfiguration {
    
    public BlogSearchConfiguration(){
        super(null);
    }

    public BlogSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
