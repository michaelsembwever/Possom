package no.sesat.mojo.modes;

import java.util.Iterator;

/**
 * Generator for Relax NG.
 */
public class GenerateRelaxNG extends GenerateSchemaFile {

    /**
     * @param element
     *            Root element.
     * @param name
     *            File name.
     * @param idString
     *            id.
     */
    protected GenerateRelaxNG(final ConfigElement element, final String name, final String idString) {
        super(element, name, idString);
    }

    /**
     * Generate Relax NG.
     */
    @Override
    protected void runImpl() {
        generate(root);
    }

    private void generate(final ConfigElement element) {

        if (element.hasDoc()) {
            final String[] docArray = element.doc.split("\n");
            for (int i = 0; i < docArray.length; i++) {
                println("## " + docArray[i]);
            }
        }

        // prevent blowing the stack. This is because we currently don't support
        // recursive elements in this RelaxNG generator.
        boolean empty = true;
        for (ConfigElement child : element.children) {
            if (!element.name.equals(child.name)) {
                empty = false;
            }
        }

        println("element " + element.name + " {");
        indent();
        if (element.attributes.isEmpty() && empty) {
            print(" empty ");
        } else {
            for (final Iterator<ConfigAttribute> iterator = element.attributes.iterator(); iterator.hasNext();) {
                final ConfigAttribute attrib = iterator.next();

                generate(attrib);
                if (iterator.hasNext() || !empty) {
                    println(",");
                } else {
                    println("");
                }
            }
        }

        if (!empty) {
            println("(");
            boolean one = false;
            for (ConfigElement child : element.children) {
                if (!element.name.equals(child.name)) {
                    if(one) {
                        println("|");
                    } else {
                        one = true;
                    }
                    generate(child);
                }
            }
            println(")*");
        }
        unindent();
        println("}*");

    }

    private void generate(final ConfigAttribute attrib) {
        if (attrib.hasDoc()) {
            final String[] docArray = attrib.doc.split("\n");
            for (int i = 0; i < docArray.length; i++) {
                println("## " + docArray[i]);
            }
        }
        print("attribute " + attrib.name + " { text }" + (attrib.required ? "" : "?"));
    }

}
