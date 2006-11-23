package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Directive to display share holder information in the yellowInfopage.vm. The
 * layout is controlled by the css.
 * 
 * The layout can be controlled by the following css elements:
 * 
 * tabsheader - The table header rolestable - defining the overall gui for the
 * table sh_left - Gui left Column sh_middle - Gui Middle Column sh_right - Gui
 * Rigth
 * 
 * hover_on - The gray color witch is every second row
 * 
 * The way to use this would be: #shareHolder($company.getInfo(yproller)
 * 
 * The format of shareholders is defines as : #aksjonaer0#
 * #bold#Navn#sep#Eierandel i %#sep#Antall aksjer#sepnl#
 * 
 * This class is not thread safe,,, must it be ?
 * 
 * @See JiraIssue https://jira.sesam.no/jira/browse/CAT-497
 * @author olas <ola@sesam.no>
 * 
 */
public class ShareHoldersDirective extends Directive {
    /** Logger */
    private static Logger log = Logger.getLogger(ShareHoldersDirective.class);

    // Constants -----------------------------------------------------
 
    /** Title Name */
    static final String MSG_NAME_SHAREHOLDER_TITLE = "Aksjonærer";

    /** Th Name */
    static final String MSG_NAME = "Navn";

    /** Th number of shares */
    static final String MSG_NUMB_OF_SHARES = "Antall aksjer";

    /** Th shares in percent */
    static final String MSG_SHARES_IN_PERCENT = "Andel i %";

    // Attributes -------------------------------------------------------
  
    /** Table Background colors, switch between white and gray */
    public enum TableBgColor {
        GRAY, WHITE
    }

    /** The first row in the shareholders table is color white */
    private TableBgColor tableBgColor = TableBgColor.WHITE;

    /** Html elements used for building up the table */
    static final String HTML_DIV = "div";

    /** Table Tag */
    static final String HTML_TABLE = "table";

    /** Tr Tag */
    static final String HTML_TR = "tr";

    /** Th Tag */
    static final String HTML_TH = "th";

    /** Td Tag */
    static final String HTML_TD = "td";

    /** Html Class */
    static final String HTML_CLASS = "class";

    /** Not supported by IE ? Equivalent to the css attribute border-spacing */
    static final String HTML_CELLSPACING = "cellspacing";

    /**
     * This is the information we get from the field 'yproller' aboute
     * shareholders.
     */
    private class ShareHolder {
        /** Name of the shareHolder field #1 */
        String name;

        /** Id so we can link to WhitePages */
        String id;

        /** Stocks in percent, field #2 */
        String sharesInPercent;

        /** Number of stocks, field #2 */
        String numberOfShares;
    }

    // Public --------------------------------------------------------

    /**
     * Name of Directive
     * 
     * @return directive name
     */
    public String getName() {
        return "shareHolders";
    }

    /**
     * Type of Directive
     * 
     * @return type as int value
     */
    public int getType() {
        return LINE;
    }

    /**
     * Render content 
     * 
     * @see org.apache.velocity.runtime.directive.Directive#render(org.apache.velocity.context.InternalContextAdapter,
     *      java.io.Writer, org.apache.velocity.runtime.parser.node.Node)
     */
    public boolean render(InternalContextAdapter ica, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException,
            MethodInvocationException {

        if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }
        // The text string from datafield which all the roledata is stored
        String ypRoles = node.jjtGetChild(0).value(ica).toString();

        return internalRender(ypRoles, writer, node);
    }

    // Private --------------------------------------------------------

    // -- Inerntal Render
    private boolean internalRender(String shareHoldersRaw, Writer writer,
            Node node) throws IOException {

        List<ShareHolder> shareHolders = parse(shareHoldersRaw);
        
        if (!hasShareHolders(shareHolders)) {
            return true;
        }
        
        Document root = createDocument();
        Element header = root.createElement(HTML_DIV);
        header.setAttribute(HTML_CLASS, "tabs_header");
        header.appendChild(root.createTextNode(MSG_NAME_SHAREHOLDER_TITLE));
        root.appendChild(header);
        internalWriteDocument(root, writer);
        writer.write("\n");
        // Write the parsed shareholders information
        Document content = createXmlDocumentFromShareHolders(shareHolders);
        internalWriteDocument(content, writer);
        return true;
    }

    // -- Check if we should print header and stuff
    private boolean hasShareHolders(List<ShareHolder> shareHolders) {
        return shareHolders.size() > 0;
    }

    // -- Thransform objects into xml document
    private Document createXmlDocumentFromShareHolders(
            List<ShareHolder> shareHolders) {
        Document doc = createDocument();

        Element root = doc.createElement(HTML_DIV);
        Element table = doc.createElement(HTML_TABLE);
        table.setAttribute(HTML_CLASS, "roletable");
        table.setAttribute("cellspacing", "1");
        root.appendChild(table);

        Element trH = doc.createElement(HTML_TR);

        Element th = doc.createElement(HTML_TH);
        th.setAttribute(HTML_CLASS, "sh_left");
        th.appendChild(doc.createTextNode(MSG_NAME));
        trH.appendChild(th);

        th = doc.createElement(HTML_TH);
        th.setAttribute(HTML_CLASS, "sh_middle");
        th.appendChild(doc.createTextNode(MSG_NUMB_OF_SHARES));
        trH.appendChild(th);

        th = doc.createElement(HTML_TH);
        th.setAttribute(HTML_CLASS, "sh_right");
        th.appendChild(doc.createTextNode(MSG_SHARES_IN_PERCENT));
        trH.appendChild(th);

        table.appendChild(trH);

        // Loop through all the shareholders and add them to the table
        for (ShareHolder sh : shareHolders) {
            Element tr = doc.createElement(HTML_TR);
            addTdToTr(doc, tr, "sh_left").appendChild(doc.createTextNode(sh.name));
            addTdToTr(doc, tr, "sh_middle").appendChild(
                    doc.createTextNode(sh.numberOfShares));
            addTdToTr(doc, tr, "sh_right").appendChild(
                    doc.createTextNode(sh.sharesInPercent));
            table.appendChild(tr);
            // Hover the td background color(white/gray)
            switchTdBgColor();
        }
        doc.appendChild(root);
        return doc;
    }
    
    // -- Switch bg color
    private void switchTdBgColor() {
        if (tableBgColor == TableBgColor.WHITE) {
            tableBgColor = TableBgColor.GRAY;
        } else {
            tableBgColor = TableBgColor.WHITE;
        }
    }
    
    // -- Add td to table row
    private Element addTdToTr(Document doc, Element tr, String cssClass) {
        if (tableBgColor == TableBgColor.WHITE) {
            Element td = doc.createElement(HTML_TD);
            td.setAttribute(HTML_CLASS, cssClass);
            tr.appendChild(td);
            return td;
        } else {
            Element td = doc.createElement(HTML_TD);
            td.setAttribute(HTML_CLASS, cssClass + " hover_on");
            tr.appendChild(td);
            return td;
        }
    }

    // -- Parse the the inpurt , using old school seperators like #sep#
    List<ShareHolder> parse(String content) {
        List<ShareHolder> shareHolders = new ArrayList();
        boolean isShareHoldersStarted = false;
        boolean isShareHolderRow = false;
        
        // Return empty list
        if (content == null) {
            return shareHolders;
        }
        if ("".equals(content.trim())) {
            return shareHolders;
        }

        for (String s : content.split("#sepnl#")) {
            s = s.trim();
            log.debug("Parsing " + s);

            if (isShareHolderRow) {
                String tmp[] = s.split("#sep#");

                if (tmp.length == 3) {
                    ShareHolder sh = new ShareHolder();
                    String tmp2[] = tmp[0].split("#id#");
                    sh.name = tmp2[0];
                    if (tmp2.length == 2) {
                        sh.id = tmp2[1];
                    }
                    sh.sharesInPercent = tmp[1];
                    sh.numberOfShares = tmp[2];
                    shareHolders.add(sh);
                }
            }
            if(s.startsWith("#")){
                isShareHoldersStarted = false;
                isShareHolderRow = false;
            }
            if (s.startsWith("#aksjonaer0#")) {
                isShareHoldersStarted = true;
                isShareHolderRow = true;
            }
            if (s.startsWith("#bold#Navn#sep") && isShareHoldersStarted) {
                isShareHolderRow = true;
            }
            
        }
        return shareHolders;
    }

    // -- Write the document to the writer
    private void internalWriteDocument(Document d, Writer w) {
        DOMSource source = new DOMSource(d);
        StreamResult result = new StreamResult(w);

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Xml Parser: " + e);
        } catch (TransformerException ignore) {
        }
    }

    // -- Create a DOM document
    private Document createDocument() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder builder = null;
        try {
            builder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document doc = builder.newDocument();
        return doc;
    }
}
