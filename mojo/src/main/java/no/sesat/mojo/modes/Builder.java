package no.sesat.mojo.modes;

import static com.sun.tools.javac.code.Flags.PROTECTED;
import static com.sun.tools.javac.code.Flags.PUBLIC;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
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

public class Builder {

	private static String outputDir = "";

	// testing
	public static void main(String args[]) {
		List<String> classpaths = new Vector<String>();
		classpaths.add("/home/haavard/dev/trunk/genericno.sesam.no/search-command-config/src/main/java/");
		classpaths.add("/home/haavard/dev/trunk/genericno.sesam.no/query-transform-config/src/main/java/");
		classpaths.add("/home/haavard/dev/trunk/genericno.sesam.no/result-handler-config/src/main/java/");
		classpaths.add("/home/haavard/dev/trunk/sesat-kernel/generic.sesam/search-command-config/src/main/java/");
		classpaths.add("/home/haavard/dev/trunk/sesat.kernel/generic.sesam/query-transform-config/src/main/java/");
		classpaths.add("/home/haavard/dev/trunk/sesat-kernel/generic.sesam/result-handler-config/src/main/java/");
		classpaths.add("/home/haavard/dev/trunk/sesat-kernel/query-transform-config-spi/src/main/java/");
		classpaths.add("/home/haavard/dev/trunk/sesat-kernel/result-handler-config-spi/src/main/java/");
		classpaths.add("/home/haavard/dev/trunk/sesat-kernel/search-command-config-spi/src/main/java/");

		String classpath = "";
		for (Iterator<String> iterator = classpaths.iterator(); iterator.hasNext();) {
			classpath += (String) iterator.next();
			if (iterator.hasNext())
				classpath += File.pathSeparator;
		}

		Build(classpath, "/home/haavard/wonk");
	}

	public static void Build(String classpath, String outputDir) {
		Builder.outputDir = new File(outputDir).getAbsolutePath() + File.separator;

		// hack to supress javadoc's warnings.
		PrintStream out = System.out;
		PrintStream err = System.err;
		System.setOut(new PrintStream(new ByteArrayOutputStream()));
		System.setErr(new PrintStream(new ByteArrayOutputStream()));

		Context context = new Context();
		Messager.preRegister(context, "Builder");
		JavadocTool comp = JavadocTool.make0(context);

		ListBuffer<String> subPackages = new ListBuffer<String>();
		subPackages.append("no");
		ListBuffer<String> xcludePackages = new ListBuffer<String>();
		ListBuffer<String> javaNames = new ListBuffer<String>();
		ListBuffer<String[]> options = new ListBuffer<String[]>();

		Options compOpts = Options.instance(context);
		compOpts.put("-classpath", classpath);

		RootDocImpl root = null;
		try {
			root = comp.getRootDocImpl("", "", new ModifierFilter(PUBLIC | PROTECTED), javaNames.toList(), options.toList(), false, subPackages.toList(),
					xcludePackages.toList(), false, false, false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.setOut(out);
		System.setErr(err);

		if (root != null)
			start(root);
	}

	/**
	 * This is the entry point for a doclet.
	 * 
	 * @param root
	 * @return
	 */
	public static boolean start(RootDoc root) {

		ConfigElement defaultConvert = new ConfigElement("default-convert");
		defaultConvert.attributes.add(new ConfigAttribute("name"));
		defaultConvert.attributes.add(new ConfigAttribute("prefix"));
		defaultConvert.attributes.add(new ConfigAttribute("postfix"));

		Vector<ConfigElement> commands = new Vector<ConfigElement>();
		Vector<ConfigElement> resultHandlers = new Vector<ConfigElement>();
		Vector<ConfigElement> queryTransformers = new Vector<ConfigElement>();
		{
			ClassDoc[] classes = root.classes();
			for (int i = 0; i < classes.length; i++) {
				String name = classes[i].name();
				if (name.endsWith("Config") && !classes[i].isAbstract()) {
					ConfigElement element = new ConfigElement(classes[i]);

					if (name.endsWith("CommandConfig")) {
						element.applyNameFilter(new NameFilter() {
							public String filter(String name) {
								name = name.substring(0, name.lastIndexOf("Config"));
								String res = toXmlName(name);
								return res;
							}
						});
						commands.add(element);
					} else if (name.endsWith("ResultHandlerConfig")) {
						element.applyNameFilter(new NameFilter() {
							public String filter(String name) {
								name = name.substring(0, name.lastIndexOf("ResultHandlerConfig"));
								String res = toXmlName(name);
								return res;
							}
						});
						if (!element.name.isEmpty())
							resultHandlers.add(element);
					} else if (name.endsWith("QueryTransformer") || (name.endsWith("QueryTransformerConfig"))) {
						element.applyNameFilter(new NameFilter() {
							public String filter(String name) {
								name = name.substring(0, name.lastIndexOf("QueryTransformer"));
								String res = toXmlName(name);
								return res;
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

		ConfigElement modes = new ConfigElement("modes");
		modes.attributes.add(new ConfigAttribute("template-prefix"));

		ConfigElement mode = new ConfigElement("mode");
		mode.attributes.add(new ConfigAttribute("id"));
		mode.attributes.add(new ConfigAttribute("inherit"));
		mode.attributes.add(new ConfigAttribute("analysis"));
		mode.attributes.add(new ConfigAttribute("executor"));

		mode.addChildren(commands);
		modes.addChild(mode);

		ConfigElement resultHandler = new ConfigElement("result-handlers");
		resultHandler.addChildren(resultHandlers);

		ConfigElement queryTransform = new ConfigElement("query-transformers");
		queryTransform.addChildren(queryTransformers);

		ConfigElement navigators = new ConfigElement("navigators");
		ConfigElement navigator = new ConfigElement("navigator");
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

		Runnable jobs[] = { new GenerateRelaxNG(modes, outputDir + "modes.rnc"), new GenerateXSD(modes, outputDir + "modes.xsd"),
				new GenerateDTD(modes, outputDir + "modes.dtd") };
		int jobCount = 5;
		for (int i = 0; i < (jobs.length + jobCount - 1); i++) {

			if (i < jobs.length) {
				// System.out.println("start job: " + i);
				Thread thread = new Thread(jobs[i]);
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

	public static String toXmlName(final String beanName) {
		final StringBuilder xmlName = new StringBuilder(beanName);
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