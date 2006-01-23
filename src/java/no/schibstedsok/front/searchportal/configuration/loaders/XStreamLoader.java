/* Copyright (2005-2006) Schibsted Søk AS
 * XStreamLoader.java
 *
 * Created on 23 January 2006, 09:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.configuration.loaders;

import com.thoughtworks.xstream.XStream;

/** ResourceLoader to deal with XStream deserialised xml resources.
 * TODO Hide XStream implementation specifics. This should be a generic XML loader.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface XStreamLoader extends ResourceLoader {
    /** initialise this resource loader with the resource name/path and the resource it will go into.
     *@param resource the name/path of the resource.
     *@param xstream the xstream that will be used to deserialise the resource.
     **/
    void init(String resource, XStream xstream);
    /** get the result of the xstream deserialisation.
     *@return the xstream result.
     **/
    Object getXStreamResult();

}
