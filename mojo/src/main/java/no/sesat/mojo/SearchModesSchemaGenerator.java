package no.sesat.mojo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import no.sesat.mojo.modes.Builder;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * @goal searchModesSchemaGenerator
 */
public class SearchModesSchemaGenerator extends AbstractMojo {

    /**
     * The Maven project.
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
     * sourceArtifacts
     * 
     * @parameter
     */
    private List<String> sourceArtifacts;

    /**
     * Output directory
     * 
     * @parameter
     */
    private String outputDir;

    /**
     * Used to look up Artifacts in the remote serverDeployLocation.
     * 
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    private ArtifactFactory factory;

    /**
     * Used to look up Artifacts in the remote serverDeployLocation.
     * 
     * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     */
    private ArtifactResolver resolver;

    /**
     * Location of the local serverDeployLocation.
     * 
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private org.apache.maven.artifact.repository.ArtifactRepository local;

    /**
     * List of Remote Repositories used by the resolver
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private java.util.List remoteRepos;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
        getLog().info(this.getClass().getName());

        if (outputDir == null) {
            getLog().error("outputDir variable must be specified");
        }

        String classpath = "";
        if (classpaths != null) {
            for (final Iterator<String> iterator = classpaths.iterator(); iterator.hasNext();) {
                final String name = iterator.next();
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
                    if (iterator.hasNext()) {
                        classpath += File.pathSeparator;
                    }
                } else {
                    getLog().warn("Classpath not found : " + file.getAbsolutePath());
                }
            }
        }

        if (sourceArtifacts != null) {
            Map<String, Artifact> artifactMap = project.getArtifactMap();
            for (String artifactName : sourceArtifacts) {
                String[] ap = artifactName.split(":");

                Artifact a = factory.createArtifactWithClassifier(ap[0], ap[1], ap[2], "jar", "sources");

                try {
                    resolver.resolve(a, remoteRepos, local);
                } catch (ArtifactResolutionException e) {
                    e.printStackTrace();
                } catch (ArtifactNotFoundException e) {
                    e.printStackTrace();
                }

                File outFolder = new File("target/source/");
                outFolder.mkdirs();
                if (!classpath.equals("")) {
                    classpath += File.pathSeparator;
                }
                classpath += outFolder.getAbsolutePath();

                try {
                    JarFile jarFile = new JarFile(a.getFile());

                    for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
                        JarEntry entry = (JarEntry) e.nextElement();
                        File file = new File(outFolder, entry.getName());
                        if (entry.isDirectory()) {
                            file.mkdir();
                        } else {
                            InputStream in = jarFile.getInputStream(entry);
                            PrintStream out = new PrintStream(file);
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        File outputDirFile = new File(outputDir);
        if (!outputDirFile.isAbsolute()) {
            outputDirFile = new File(project.getBasedir(), outputDir);
        }

        outputDir = outputDirFile.getAbsolutePath();

        getLog().info("Using: classpath = " + classpath);
        getLog().info("Using: outputDir = " + outputDir);

        Builder.build(classpath, outputDir, project.getName());
    }
}
