/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
*
* Jul 20, 2007 11:18:34 AM
*/
package no.schibstedsok.searchportal.view.navigation;




/**
 *
 */
public interface NavigationControllerFactory<T extends NavigationConfig.Nav> {
    NavigationController get(T nav);
}
