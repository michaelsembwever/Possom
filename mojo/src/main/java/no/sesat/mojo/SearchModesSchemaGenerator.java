package no.sesat.mojo;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import no.sesat.mojo.modes.Builder;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * @goal searchModesSchemaGenerator
 */
public class SearchModesSchemaGenerator extends AbstractMojo {

	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 * @description "the maven project to use"
	 */
	private MavenProject project;

	/**
	 * Classpath
	 * 
	 * @parameter
	 */
	private List<String> classpaths;

	/**
	 * Output directory
	 * 
	 * @parameter
	 */
	private String outputDir;

	public void execute() throws MojoExecutionException {
		getLog().info(this.getClass().getName());

		if (classpaths == null)
			getLog().error("classpaths variable must be spesified");

		if (outputDir == null)
			getLog().error("outputDir variable must be spesified");

		String classpath = "";
		for (Iterator<String> iterator = classpaths.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			File file = new File(name);
			if (!file.isAbsolute()) {
				file = new File(project.getBasedir(), name);
			}
			if (file.exists()) {
				try {
					classpath += file.getCanonicalPath() + File.separator;
				} catch (IOException e) {
					getLog().warn(e);
				}
				if (iterator.hasNext())
					classpath += File.pathSeparator;
			} else {
				getLog().warn("Classpath not found : " + file.getAbsolutePath());
			}
		}

		File outputDirFile = new File(outputDir);
		if (!outputDirFile.isAbsolute())
			outputDirFile = new File(project.getBasedir(), outputDir);

		outputDir = outputDirFile.getAbsolutePath();

		getLog().info("Using: classpath = " + classpath);
		getLog().info("Using: outputDir = " + outputDir);

		Builder.Build(classpath, outputDir);
	}
}