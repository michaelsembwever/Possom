/* Copyright (2009) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package no.sesat.search.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;
import java.net.URL;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/**
 *
 * A velocity directive to make domain name into IDN (Internationalized Domain Names).
 * It requires a complete URL to work, since we are passing it to java.net.URL.
 *
 */
public final class IDNDirective extends Directive {

    private static final Logger LOG = Logger.getLogger(IDNDirective.class);
    private static final String NAME = "idn";

    public String getName() {
        return NAME;
    }

    @Override
    public int getType() {
        return LINE;
    }

    public boolean render(
            final InternalContextAdapter context,
            final Writer writer,
            final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if (node.jjtGetNumChildren() < 1) {
            rsvc.error("#" + getName() + " - missing argument");
        } else {
            final String url = null != node.jjtGetChild(0).value(context)
                    ? node.jjtGetChild(0).value(context).toString()
                    : "";

            String host = null;
            try {
                host = new URL(url).getHost();
            } catch (MalformedURLException ex) {
                LOG.warn("Invalid url: " + url, ex);
            }

            if (host != null) {
                writer.write(url.replace(host, java.net.IDN.toASCII(host)));
            }
            else {
                writer.write(url);
            }
        }
        return true;
    }
}
