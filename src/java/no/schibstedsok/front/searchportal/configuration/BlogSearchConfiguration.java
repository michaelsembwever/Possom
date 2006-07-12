
/*
 * BlogSearchConfiguration.java
 *
 * Created on July 12, 2006, 10:35 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.configuration;

/**
 *
 * @author maek
 */
public class BlogSearchConfiguration extends AdvancedFastSearchConfiguration {
    
    public BlogSearchConfiguration(){
        super(null);
    }

    public BlogSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
