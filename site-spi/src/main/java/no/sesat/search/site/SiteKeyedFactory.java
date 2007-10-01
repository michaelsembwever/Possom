/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * SiteKeyedFactory.java
 *
 * Created on 5 May 2006, 07:27
 *
 */

package no.sesat.search.site;

/** Factories that have Site->Factory mappings should implement this interface
 * to ensure general behaviours.
 *  SiteKeyedFactories by default have a synchronised site to factory mapping.
 *  If this is not the case it must be clearly noted in the class's javadoc.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public interface SiteKeyedFactory {
    
    /** Remove the factory the maps to the given site. **/
    boolean remove(Site site);
    
}
