package io.jenkins.plugins.prism;

import java.util.List;

import org.junit.Test;

import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.ConfiguratorException;
import io.jenkins.plugins.util.IntegrationTestWithJenkinsPerTest;

import static org.assertj.core.api.Assertions.*;

/**
 * Checks whether all parser can be imported using the configuration-as-code plug-in.
 *
 * @author Ullrich Hafner
 */
public class ConfigurationAsCodeITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * Reads the YAML file with permitted source code directories and verifies that the directories have been loaded.
     */
    @Test
    public void shouldImportSourceDirectoriesFromYaml() {
        configureJenkins("sourceDirectories.yaml");

        List<SourceDirectory> parsers = PrismConfiguration.getInstance().getSourceDirectories();
        assertThat(parsers.stream().map(SourceDirectory::getPath))
                .hasSize(2)
                .containsExactlyInAnyOrder("C:\\Windows", "/absolute");
    }

    private void configureJenkins(final String fileName) {
        try {
            ConfigurationAsCode.get().configure(getResourceAsFile(fileName).toUri().toString());
        }
        catch (ConfiguratorException e) {
            throw new AssertionError(e);
        }
    }
}
