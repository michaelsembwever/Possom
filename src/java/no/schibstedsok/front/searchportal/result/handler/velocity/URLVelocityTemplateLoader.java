package no.schibstedsok.front.searchportal.result.handler.velocity;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import java.io.InputStream;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.commons.collections.ExtendedProperties;

/** XXX This source file needs to be published to the internet as it is open-source code.
 *
 * XXX Implement caching!
 *
 * This is a simple URL-based loader.
 * ORIGINAL FROM http://svn.apache.org/repos/asf/jakarta/velocity/engine/trunk/whiteboard/geir/URLResourceLoader.java
 *
 * original version Id: URLResourceLoader.java,v 1.3 2004/03/19 17:13:40 dlr Exp
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 *
 *
 * MODIFIED TO SUIT SCHIBSTEDSØK's NEEDS.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id: URLResourceLoader.java,v 1.3 2004/03/19 17:13:40 dlr Exp $
 */
public final class URLVelocityTemplateLoader extends ResourceLoader {

    private static final Log LOG = LogFactory.getLog(URLVelocityTemplateLoader.class);

    /** {@inheritDoc}
     */
    public void init(final ExtendedProperties configuration) { }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param url  url of template to fetch bytestream of
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in the file template path.
     */
    public synchronized InputStream getResourceStream( final String url )
        throws ResourceNotFoundException{

        LOG.trace("start getResourceStream( "+url+" )");
        InputStream inputStream;
        try{

            final URL u = new URL( url );
            final URLConnection conn = u.openConnection();
            inputStream = conn.getInputStream();

        }catch( IOException e ){
            final String msg = "Error: cannot find resource " + url;

            LOG.error( msg );

            throw new ResourceNotFoundException( msg );
        }

        return inputStream;

    }

    /** {@inheritDoc}
     */
    public boolean isSourceModified(Resource resource){
        return true;
    }

    /** {@inheritDoc}
     */
    public long getLastModified(Resource resource){
        return 0;
    }
}

