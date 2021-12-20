package io.jenkins.plugins.prism;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;

import static org.assertj.core.api.Assertions.*;

/**
 * Checks whether all parser can be imported using the configuration-as-code plug-in.
 *
 * @author Ullrich Hafner
 */
public class ConfigurationAsCodeITest {
    /** Jenkins SUT. */
    @Rule @SuppressFBWarnings("URF")
    public JenkinsConfiguredWithCodeRule jenkins = new JenkinsConfiguredWithCodeRule();

    /**
     * Reads a YAML file with permitted source code directories and verifies that the directories have been loaded.
     */
    @Test @ConfiguredWithCode("sourceDirectories.yaml")
    public void shouldImportSourceDirectoriesFromYaml() {
        List<SourceCodeDirectory> folders = PrismConfiguration.getInstance().getSourceDirectories();
        assertThat(folders.stream().map(SourceCodeDirectory::getPath))
                .hasSize(2)
                .containsExactlyInAnyOrder("C:\\Windows", "/absolute");
    }

    /**
     * Reads a YAML file with the active theme.
     */
    @Test @ConfiguredWithCode("theme.yaml")
    public void shouldImportTheme() {
        assertThat(PrismConfiguration.getInstance().getTheme()).isEqualTo(PrismTheme.DARK);
    }
}
