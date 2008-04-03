package no.sesat.mojo.modes;

import static com.sun.tools.javac.code.Flags.PROTECTED;
import static com.sun.tools.javac.code.Flags.PUBLIC;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javadoc.JavadocTool;
import com.sun.tools.javadoc.Messager;
import com.sun.tools.javadoc.ModifierFilter;
import com.sun.tools.javadoc.RootDocImpl;

/**
 * Builder to build a structure that mirrors the structure that should be used
 * in the modes.xml files. *
 */
public final class Builder {

    private static String outputDir = "";
    private static String id = "";

    private Builder() {
    }

    /**
     * Build and generate schema for modes.xml files.
     *
     * @param classpath
     *            Where to find the classes
     * @param dir
     *            Where the results should be put
     * @param idString
     *            Id
     */
    public static void build(final String classpath, final String dir, final String idString) {
        outputDir = new File(dir).getAbsolutePath() + File.separator;
        id = idString;
        RootDocImpl root = null;
        
        // hack to suppress javadoc's warnings.
        final PrintStream out = System.out;
        final PrintStream err = System.err;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setErr(new PrintStream(new ByteArrayOutputStream()));

            final Context context = new Context();
            Messager.preRegister(context, "Builder");
            final JavadocTool comp = JavadocTool.make0(context);

            final ListBuffer<String> subPackages = new ListBuffer<String>();
            subPackages.append("no");
            final ListBuffer<String> xcludePackages = new ListBuffer<String>();
            final ListBuffer<String> javaNames = new ListBuffer<String>();
            final ListBuffer<String[]> options = new ListBuffer<String[]>();

            final Options compOpts = Options.instance(context);
            compOpts.put("-classpath", classpath);
            
            try {
                root = comp.getRootDocImpl("", "", new ModifierFilter(PUBLIC | PROTECTED), javaNames.toList(), options
                        .toList(), false, subPackages.toList(), xcludePackages.toList(), false, false, false);
            } catch (IOException e) {
                e.printStackTrace(err);
                return;
            }
        } catch (Throwable e) {
            // e.printStackTrace(out);
            out.print("Generating schema files failed due to error: " + e.getMessage());
        } finally {
            System.setOut(out);
            System.setErr(err);
        }
        if (root != null) {
            start(root);
        }
    }

    /**
     * This is the entry point for a doclet.
     *
     * @param root
     *            Root document
     * @return true when everything is ok
     */
    public static boolean start(final RootDoc root) {
        final ConfigElement defaultConvert = new ConfigElement("default-convert");
        defaultConvert.attributes.add(new ConfigAttribute("name"));
        defaultConvert.attributes.add(new ConfigAttribute("prefix"));
        defaultConvert.attributes.add(new ConfigAttribute("postfix"));

        final Vector<ConfigElement> commands = new Vector<ConfigElement>();
        final Vector<ConfigElement> resultHandlers = new Vector<ConfigElement>();
        final Vector<ConfigElement> queryTransformers = new Vector<ConfigElement>();
        {
            final ClassDoc[] classes = root.classes();
            for (int i = 0; i < classes.length; i++) {
                final String name = classes[i].name();
                if (name.endsWith("Config") && !classes[i].isAbstract()) {
                    final ConfigElement element = new ConfigElement(classes[i]);

                    if (name.endsWith("CommandConfig")) {
                        element.applyNameFilter(new NameFilter() {
                            public String filter(final String name) {
                                return toXmlName(name.substring(0, name.lastIndexOf("Config")));
                            }
                        });
                        commands.add(element);
                    } else if (name.endsWith("ResultHandlerConfig")) {
                        element.applyNameFilter(new NameFilter() {
                            public String filter(final String name) {
                                return toXmlName(name.substring(0, name.lastIndexOf("ResultHandlerConfig")));
                            }
                        });
                        if (!element.name.isEmpty()) {
                            resultHandlers.add(element);
                        }
                    } else if (name.endsWith("QueryTransformer") || (name.endsWith("QueryTransformerConfig"))) {
                        element.applyNameFilter(new NameFilter() {
                            public String filter(final String name) {
                                return toXmlName(name.substring(0, name.lastIndexOf("QueryTransformer")));
                            }
                        });

                        if (name.equals("NewsCaseQueryTransformerConfig")) {
                            element.addChild(defaultConvert);
                        }

                        queryTransformers.add(element);
                    } else {
                        System.out.println("Lost: " + element.name);
                    }
                }
            }
        }

        final ConfigElement modes = new ConfigElement("modes");
        modes.attributes.add(new ConfigAttribute("template-prefix"));

        final ConfigElement mode = new ConfigElement("mode");
        mode.attributes.add(new ConfigAttribute("id"));
        mode.attributes.add(new ConfigAttribute("inherit"));
        mode.attributes.add(new ConfigAttribute("analysis"));
        mode.attributes.add(new ConfigAttribute("executor"));

        mode.addChildren(commands);
        modes.addChild(mode);

        final ConfigElement resultHandler = new ConfigElement("result-handlers");
        resultHandler.addChildren(resultHandlers);

        final ConfigElement queryTransform = new ConfigElement("query-transformers");
        queryTransform.addChildren(queryTransformers);

        final ConfigElement navigators = new ConfigElement("navigators");
        final ConfigElement navigator = new ConfigElement("navigator");
        navigator.attributes.add(new ConfigAttribute("id", null, true));
        navigator.attributes.add(new ConfigAttribute("name", null, true));
        navigator.attributes.add(new ConfigAttribute("field", null, true));
        navigator.attributes.add(new ConfigAttribute("display-name", null, true));
        navigator.attributes.add(new ConfigAttribute("sort", null, false));
        navigator.attributes.add(new ConfigAttribute("boundary-match"));

        // only for fast commands
        navigators.addChild(navigator);

        for (ConfigElement command : commands) {
            command.addChild(resultHandler);
            command.addChild(queryTransform);
            command.addChild(navigators);
        }

        final Runnable[] jobs = {new GenerateRelaxNG(modes, outputDir + "modes.rnc", id),
                new GenerateXSD(modes, outputDir + "modes.xsd", id),
                new GenerateDTD(modes, outputDir + "modes.dtd", id)};
        final int jobCount = 5;
        for (int i = 0; i < (jobs.length + jobCount - 1); i++) {

            if (i < jobs.length) {
                // System.out.println("start job: " + i);
                final Thread thread = new Thread(jobs[i]);
                thread.start();
                jobs[i] = thread;
            }
            if (i >= (jobCount - 1)) {
                try {
                    ((Thread) jobs[i - jobCount + 1]).join();
                    // System.out.println("job done: " + (i - jobCount + 1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("commands : " + commands.size());
        System.out.println("result handlers : " + resultHandlers.size());
        System.out.println("query transformers : " + queryTransformers.size());

        return true;
    }

    /**
     * Helper function to convert a java name to the equivalent XML name.
     *
     * @param name
     *            Name that should be converted
     * @return The name converted like this MySuperClass --> my-super-class
     */
    public static String toXmlName(final String name) {
        final StringBuilder xmlName = new StringBuilder(name);
        for (int i = 0; i < xmlName.length(); ++i) {
            final char c = xmlName.charAt(i);
            if (Character.isUpperCase(c)) {
                xmlName.replace(i, i + 1, (i == 0 ? "" : "-") + Character.toLowerCase(c));
                ++i;
            }
        }
        return xmlName.toString();
    }
}
