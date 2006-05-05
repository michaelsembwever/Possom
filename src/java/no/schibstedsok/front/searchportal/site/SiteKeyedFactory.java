/*
 * SiteKeyedFactory.java
 *
 * Created on 5 May 2006, 07:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.site;

/** Factories that have Site->Factory mappings should implement this interface
 * to ensure general behaviours.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface SiteKeyedFactory {
    
    /** Remove the factory the maps to the given site. **/
    boolean remove(Site site);
    
}
