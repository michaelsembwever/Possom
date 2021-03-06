/* Copyright (20072012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.view.velocity;

import java.io.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import no.sesat.Interpreter;

import org.apache.log4j.Logger;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * Load templates and adds debuginformation if VELOCITY_DEBUG is set to true.
 *
 * @see URLResourceLoader
 *
 *
 *
 *
 */
public class URLVelocityTemplateLoader extends URLResourceLoader {

    private static final Logger LOG = Logger.getLogger(URLVelocityTemplateLoader.class);

    private static final String DIV_TAG = "div";
    private static final String STYLE_ATTRIB = "style";
    private static final String STYLE_BORDER = "margin:3px;border:1px solid #C0C0C0";
    private static final String STYLE_HEADING="text-decoration:underline;font-size:10px";
    private static final String ONMOUSEOVER = "this.style.border='1px solid #C0C0C0';this.style.margin='4px'";
    private static final String ONMOUSEOUT = "this.style.border='none'";

    private static final String VELOCITY_DEBUG = "VELOCITY_DEBUG";
    private static final String VELOCITY_DEBUG_ON = "VELOCITY_DEBUG_ON";
    private static final String VELOCITY_DEBUG_STYLE = "VELOCITY_DEBUG_STYLE";
    private static final String VELOCITY_DEVELOP_BASEDIR = "VELOCITY_DEVELOP_BASEDIR";

    /**
     * getResourceStream() loads resource from url. Then add border around the
     * template so its easy to see wich templates are loaded.
     * @throws org.apache.velocity.exception.ResourceNotFoundException
     */
    @Override
    public InputStream getResourceStream(final String url) throws ResourceNotFoundException {

        // Enable/disable velocity debug
        final boolean velocityDebug = Boolean.getBoolean(VELOCITY_DEBUG);
        // Activate debug (Show borders/debuginfo)
        final boolean velocityDebugOn = Boolean.getBoolean(VELOCITY_DEBUG_ON);
        // Onmouseover style
        final boolean styleOnmouseover ="onmouseover".equals(System.getProperty(VELOCITY_DEBUG_STYLE));
        // Silent style
        final boolean styleSilent ="silent".equals(System.getProperty(VELOCITY_DEBUG_STYLE));
        // Indicates if we found file local.(Can be edited)
        boolean foundLocal = false;

        if(velocityDebug) {

            final String templatesDir = System.getProperty(VELOCITY_DEVELOP_BASEDIR);

            // Get the file equivalece of the URL by removing the host as well as the web application context path.
            final String filePath = url.replaceAll("http://(.*?)/[^/]+/", "/").replace("localhost/", "");
            final File file = getFile(templatesDir, filePath);

            foundLocal = file.exists();

            final InputStream stream = file.exists() ? getStream(file) : super.getResourceStream(url);

            if(velocityDebugOn && -1 == url.indexOf("rss")){
                StringBuilder  content = this.readTemplateFrom(stream);

                // Create html
                final StringBuilder template= new  StringBuilder();
                template.append("\n");
                template.append(content);
                template.append("\n");

                final StringWriter writer = new StringWriter();
                final Document doc = createDocument();

                final Element div = doc.createElement(DIV_TAG);

                if(styleOnmouseover) {

                    div.setAttribute("onmouseover", ONMOUSEOVER);
                    div.setAttribute("onmouseout", ONMOUSEOUT);

                }else if(styleSilent){

                        // Just print title as popup.
                }else {

                    final Element divHeader = doc.createElement(DIV_TAG);
                    divHeader.setAttribute(STYLE_ATTRIB, STYLE_HEADING);
                    divHeader.appendChild(doc.createTextNode(filePath));
                    div.appendChild(divHeader);
                    div.setAttribute("style", STYLE_BORDER);
                }

                div.setAttribute("title", file.getName() + (foundLocal ? "(Editable)" : "(Not editable)"));
                div.appendChild(doc.createCDATASection(template.toString()));
                doc.appendChild(div);

                internalWriteDocument(doc, writer);

                final String result = writer.getBuffer().toString()
                        .replace("<![CDATA[", "")
                        .replace("]]>", "");

                return new ByteArrayInputStream(result.getBytes());

            }else{
                // If debug is not currently activated OR If rss, means the output is xml.
                return stream;
            }
        }
        return super.getResourceStream(url);
    }

    /**
     * Read template utf8.
     * @param is to read from
     * @return template as StringBuilder object
     */
    private StringBuilder readTemplateFrom(final InputStream is) {
        StringBuilder builder = new StringBuilder();

        try {
            InputStreamReader isr = new InputStreamReader(is, "UTF8");
            Reader in = new BufferedReader(isr);
            int ch;
            while ((ch = in.read()) > -1) {
                builder.append((char)ch);
            }
            in.close();
        } catch (IOException e) {
            LOG.error(e);
        }
        return builder;
    }

    /**
     * Create file object
     */
    private File getFile(final String templatesDir, final String filePath) {

        File result = null;

        if(null == templatesDir) {
            result = new File("null" + filePath);

        }else{
            for(String p : templatesDir.split(",")) {

                final File file = new File(p + filePath);
                if(file.exists()) {
                    result = file;
                    break;
                }
            }
        }

        return null == result ? new File(filePath) : result;
    }

    /*
     * Get stream from file of or url.
     */
    private InputStream getStream(final File file) {

        if (file.exists()) {

            try {
                FileInputStream fileStream = new FileInputStream(file);
                return fileStream;

            } catch (FileNotFoundException ignore) {
                throw new IllegalStateException("File exist but filenotfoundexception thrown: " + ignore);
            }

        } else {
            throw new IllegalArgumentException("File does not exist");
        }
    }

    // -- Write the document to the writer
    private void internalWriteDocument(final Document d, final Writer w) {

        final DOMSource source = new DOMSource(d);
        final StreamResult result = new StreamResult(w);

        final TransformerFactory factory = TransformerFactory.newInstance();

        try {
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(source, result);

        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Xml Parser: " + e);

        } catch (TransformerException ignore) {
            LOG.debug("Ingoring the following ", ignore);
        }
    }

    // -- Create a DOM document
    private Document createDocument() {

        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        try {
            return docFactory.newDocumentBuilder().newDocument();

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add some debug function to the interpreter.
     */
    static {
        Interpreter.addFunction("velocity-debug", new Interpreter.Function() {
            public String execute(Interpreter.Context ctx) {
                if (ctx.length() == 1) {
                    System.setProperty(VELOCITY_DEBUG_ON, ctx.getArgument(0));
                    String style = ctx.getKeywordArgument("style");
                    if (style != null) {
                        if (style.toUpperCase().startsWith("ON")) {
                            System.setProperty(VELOCITY_DEBUG_STYLE, "onmouseover");
                        }
                        else if (style.toUpperCase().startsWith("SI")) {
                            System.setProperty(VELOCITY_DEBUG_STYLE, "silent");
                        }
                        else {
                            System.setProperty(VELOCITY_DEBUG_STYLE, "default");
                        }
                    }
                }
                String res = VELOCITY_DEBUG + " = " + Boolean.parseBoolean(System.getProperty(VELOCITY_DEBUG)) + " (must be set before starting up)\n";
                res += VELOCITY_DEBUG_ON + " = " + Boolean.parseBoolean(System.getProperty(VELOCITY_DEBUG_ON)) + "\n";
                res += VELOCITY_DEBUG_STYLE + " = " + System.getProperty(VELOCITY_DEBUG_STYLE) + "\n";
                res += VELOCITY_DEVELOP_BASEDIR + " = " + System.getProperty(VELOCITY_DEVELOP_BASEDIR) + "\n";

                return res;
            }

            @Override
            protected String describe() {
                return "Change or view velocity debug options. 'velocity-debug [true|false] [style=silent|onmouseover|default]'";
            }
        });
    }
}
