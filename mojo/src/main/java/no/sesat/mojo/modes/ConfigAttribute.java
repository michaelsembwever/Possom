package no.sesat.mojo.modes;

import com.sun.javadoc.MethodDoc;

/**
 * Data representing an attribute.
 *
 */
public class ConfigAttribute extends ConfigAbstract {

    protected String type = "CDATA";
    protected boolean required = false;

    /**
     * @param method Construct this attribute from a Javadoc element.
     */
    public ConfigAttribute(final MethodDoc method) {
        doc = parseDoc(method);

        name = Builder.toXmlName(method.name()).substring(4);
        type = "CDATA"; // method.parameters()[0].toString();
    }

    /**
     * @param name
     *            Name of this attribute.
     */
    protected ConfigAttribute(final String name) {
        this.name = name;
    }

    /**
     * @param name
     *            Name of this attribute.
     * @param doc
     *            Doc for this attribute.
     */
    protected ConfigAttribute(final String name, final String doc) {
        this.name = name;
        this.doc = doc;
    }

    /**
     * @param name
     *            Name of this attribute.
     * @param doc
     *            Doc for this attribute.
     * @param required
     *            if this is required attribute or not
     */
    protected ConfigAttribute(final String name, final String doc, final boolean required) {
        this.name = name;
        this.doc = doc;
        this.required = required;
    }

    private String parseDoc(final MethodDoc method) {
        if (method == null) {
            return null;
        }
        if (method.commentText().contains("{@inheritDoc}")) {
            return parseDoc(method.overriddenMethod());
        } else {
            return method.commentText();
        }
    }
}
