/*
 * Copyright (2007) Schibsted Søk AS
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
 */

package no.sesat.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.dependency.fromConfiguration.CopyMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

/**
 *
 * @goal local-deploy
 * @author mick
 * @version $Id$
 */
public class DeploySesatWarfilesMojo extends CopyMojo{
    
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    
    // All of these attributes are just explicit overrides to get them into the mojo.
    //  read http://www.mail-archive.com/dev@maven.apache.org/msg60770.html
    
    /**
     * Strip artifact version during copy
     * 
     * @parameter expression="${mdep.stripVersion}" default-value="false"
     * @parameter
     */
    private boolean stripVersion = false;
    
    /**
     * Default location used for mojo unless overridden in ArtifactItem
     * 
     * @parameter expression="${outputDirectory}"
     *            default-value="${project.build.directory}/dependency"
     * @optional
     * @since 1.0
     */
    private File outputDirectory;

    /**
     * Overwrite release artifacts
     * 
     * @optional
     * @since 1.0
     * @parameter expression="${mdep.overWriteReleases}" default-value="false"
     */
    private boolean overWriteReleases;

    /**
     * Overwrite snapshot artifacts
     * 
     * @optional
     * @since 1.0
     * @parameter expression="${mdep.overWriteSnapshots}" default-value="false"
     */
    private boolean overWriteSnapshots;

    /**
     * Overwrite if newer
     * 
     * @optional
     * @since 2.0
     * @parameter expression="${mdep.overIfNewer}" default-value="true"
     */
    private boolean overWriteIfNewer;

    /**
     * Collection of ArtifactItems to work on. (ArtifactItem contains groupId,
     * artifactId, version, type, classifier, location, destFile, markerFile and overwrite.)
     * See "Usage" and "Javadoc" for details.
     * 
     * @parameter
     * @required
     * @since 1.0
     */
    private ArrayList artifactItems;
    
    /**
     * Used to look up Artifacts in the remote repository.
     * 
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    private org.apache.maven.artifact.factory.ArtifactFactory factory;
    
    /**
     * Used to look up Artifacts in the remote repository.
     * 
     * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /**
     * Artifact collector, needed to resolve dependencies.
     * 
     * @component role="org.apache.maven.artifact.resolver.ArtifactCollector"
     * @required
     * @readonly
     */
    private ArtifactCollector artifactCollector;

    /**
     * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
     *            hint="maven"
     * @required
     * @readonly
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * Location of the local repository.
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
     * To look up Archiver/UnArchiver implementations
     * 
     * @parameter expression="${component.org.codehaus.plexus.archiver.manager.ArchiverManager}"
     * @required
     * @readonly
     */
    private ArchiverManager archiverManager;

    /**
     * POM
     * 
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * Contains the full list of projects in the reactor.
     * 
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    private List reactorProjects;

    /**
     * If the plugin should be silent.
     * 
     * @optional
     * @since 2.0
     * @parameter expression="${silent}" default-value="false"
     */
    private boolean silent;

    /**
     * Output absolute filename for resolved artifacts
     * 
     * @optional
     * @since 2.0
     * @parameter expression="${outputAbsoluteArtifactFilename}"
     *            default-value="false"
     */
    private boolean outputAbsoluteArtifactFilename;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public DeploySesatWarfilesMojo() {
    }

    // Public --------------------------------------------------------
    
    // Z implementation ----------------------------------------------
    
    // CopyMojo overrides ---------------------------------------------------
    
    public void execute() throws MojoExecutionException{
        
        // pre-condition checks
        
        // 1. the output directory must exist
        if(getOutputDirectory().exists()){
            
            // 2. output directory is writable
            if(getOutputDirectory().canWrite()){
                
                // execute default behaviour
                pushFields();
                super.execute();
                
            }else{
                // 2.failure output directory isn't writable
                getLog().error( getOutputDirectory().getAbsolutePath() + " can not be written to.");
            }
        }else{
            // 1.failure: the output directory doesn't exist
            getLog().error( getOutputDirectory().getAbsolutePath() + " does not exist.");
            final String catalinaBase = System.getProperty("env.CATALINA_BASE");
            if(null == catalinaBase || 0 == catalinaBase.length()){
                getLog().info("Define system variable CATALINA_BASE to enable automatic deployment.");
            }
        }
        
        
    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    private void pushFields(){
        setArchiverManager(archiverManager);
        setArtifactCollector(artifactCollector);
        setArtifactMetadataSource(artifactMetadataSource);
        setFactory(factory);
        setResolver(resolver);
    }
    
    // Inner classes -------------------------------------------------
   
}
