package io.jenkins.plugins.prism;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link SourceCodeDirectory}.
 *
 * @author Ullrich Hafner
 */
class SourceCodeDirectoryTest {
    private static final String DIRECTORY = "one";

    @Test
    void shouldObeyEqualsContract() {
        EqualsVerifier.forClass(SourceCodeDirectory.class).verify();
    }

    @Test
    void shouldCreateInstance() {
        SourceCodeDirectory directory = new SourceCodeDirectory(DIRECTORY);

        assertThat(directory.getPath()).isEqualTo(DIRECTORY);
    }
}
