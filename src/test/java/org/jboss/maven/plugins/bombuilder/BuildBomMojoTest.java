package org.jboss.maven.plugins.bombuilder;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class BuildBomMojoTest {


    @Mock
    private PomDependencyVersionsTransformer versionTransformer;
    @Mock
    private BuildBomMojo.ModelWriter modelWriter;
    private BuildBomMojo mojo;

    @Before
    public void before() {
        initMocks(this);
        mojo = createBuildBomMojo();
    }
    
    @Test
    public void testDependencyVersionIsNotStoredInPropertiesByDefault() throws Exception {
        mojo.execute();

        verify(versionTransformer, never()).transformPomModel(any(Model.class));
    }

    @Test
    public void testDependencyVersionIsStoredInProperties() throws Exception {
        mojo.usePropertiesForVersion = true;

        mojo.execute();

        verify(versionTransformer, times(1)).transformPomModel(any(Model.class));
    }

    private BuildBomMojo createBuildBomMojo() {
        BuildBomMojo mojo = new BuildBomMojo(modelWriter, versionTransformer);
        mojo.mavenProject = new MavenProject();
        mojo.mavenProject.getBuild().setOutputDirectory("target");
        mojo.outputFilename = "pom.xml";
        return mojo;
    }


    @Test
    public void testMatchesExcludedDependency() throws Exception {
        assertArtifactMatchesExcludedDependency(true, "groupId", "artifactId", "groupId", "artifactId");
        assertArtifactMatchesExcludedDependency(true, "groupId", "artifactId", "*", "artifactId");
        assertArtifactMatchesExcludedDependency(true, "groupId", "artifactId", "groupId", "*");
        assertArtifactMatchesExcludedDependency(true, "groupId", "artifactId", "*", "*");
        assertArtifactMatchesExcludedDependency(true, "groupId", "artifactId", " * ", " * ");
        assertArtifactMatchesExcludedDependency(false, "groupId", "otherArtifactId", "groupId", null);
        assertArtifactMatchesExcludedDependency(false, "groupId", "otherArtifactId", null, "artifactId");
        assertArtifactMatchesExcludedDependency(false, "groupId", "otherArtifactId", "groupId", "artifactId");
        assertArtifactMatchesExcludedDependency(false, "otherGroupId", "artifactId", "groupId", "artifactId");
        assertArtifactMatchesExcludedDependency(false, "otherGroupId", "otherArtifactId", "groupId", "artifactId");
    }

    private void assertArtifactMatchesExcludedDependency(boolean expected, String artifactGroupId, String artifactArtifactId, String dependencyGroupId, String dependencyArtifactId) {
        Artifact artifact = createArtifact(artifactGroupId, artifactArtifactId);
        DependencyExclusion exclusion = createDependencyExclusion(dependencyGroupId, dependencyArtifactId);
        BuildBomMojo mojo = new BuildBomMojo();
        assertEquals(expected, mojo.matchesExcludedDependency(artifact, exclusion));
    }

    private DependencyExclusion createDependencyExclusion(String groupId, String artifactId) {
        return new DependencyExclusion(groupId, artifactId);
    }

    private Artifact createArtifact(String groupId, String artifactId) {
        return new DefaultArtifact(groupId, artifactId, "version", "scope", "type", "classifier", (ArtifactHandler)null);
    }
}