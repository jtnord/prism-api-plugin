package io.jenkins.plugins.prism;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import hudson.FilePath;
import hudson.model.FreeStyleProject;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;

import io.jenkins.plugins.util.FormValidationAssert;

import static io.jenkins.plugins.prism.SourceCodeConfigurationValidation.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link SourceCodeConfigurationValidation}.
 *
 * @author Arne Schöntag
 * @author Stephan Plöderl
 * @author Ullrich Hafner
 */
class SourceCodeConfigurationValidationTest {
    @Test
    void shouldValidateCharsets() {
        SourceCodeConfigurationValidation model = createValidation();

        FormValidationAssert.assertThat(model.validateCharset(""))
                .isOk();
        FormValidationAssert.assertThat(model.validateCharset("UTF-8"))
                .isOk();
        FormValidationAssert.assertThat(model.validateCharset("Some wrong text"))
                .isError()
                .hasMessage(createWrongEncodingErrorMessage());
    }

    @Test
    void shouldContainDefaultCharsets() {
        SourceCodeConfigurationValidation model = createValidation();

        ComboBoxModel allCharsets = model.getAllCharsets();
        assertThat(allCharsets).isNotEmpty().contains("UTF-8", "ISO-8859-1");
    }

    @Test
    void shouldFallbackToPlatformCharset() {
        SourceCodeConfigurationValidation model = createValidation();

        assertThat(model.getCharset("UTF-8")).isEqualTo(StandardCharsets.UTF_8);
        assertThat(model.getCharset("nothing")).isEqualTo(Charset.defaultCharset());
        assertThat(model.getCharset("")).isEqualTo(Charset.defaultCharset());
        assertThat(model.getCharset(null)).isEqualTo(Charset.defaultCharset());
    }

    @Test
    void shouldValidateRelativeWithoutWorkspace() {
        SourceCodeConfigurationValidation model = createValidation();

        FormValidationAssert.assertThat(model.validateSourceDirectory(null, "skip")).isOk();

        FreeStyleProject job = mock(FreeStyleProject.class);
        FormValidationAssert.assertThat(model.validateSourceDirectory(job, "no-workspace")).isOk();

        FilePath workspace = mock(FilePath.class);
        when(job.getSomeWorkspace()).thenReturn(workspace);
        FormValidationAssert.assertThat(model.validateSourceDirectory(job, "workspace-does-not-exist")).isOk();
    }

    @Test
    void shouldValidateRelativeWithWorkspace() throws IOException, InterruptedException {
        SourceCodeConfigurationValidation model = createValidation();

        FreeStyleProject job = mock(FreeStyleProject.class);
        FilePath workspace = mock(FilePath.class);
        when(job.getSomeWorkspace()).thenReturn(workspace);
        when(workspace.exists()).thenReturn(true);
        String sourceDirectory = "relative";
        when(workspace.validateRelativeDirectory("relative")).thenReturn(FormValidation.ok(sourceDirectory));

        FormValidationAssert.assertThat(model.validateSourceDirectory(job, sourceDirectory))
                .isOk()
                .hasMessage(sourceDirectory);
    }

    @Test
    void shouldIgnoreExceptions() throws IOException, InterruptedException {
        SourceCodeConfigurationValidation model = createValidation();

        FreeStyleProject job = mock(FreeStyleProject.class);
        FilePath workspace = mock(FilePath.class);
        when(workspace.exists()).thenThrow(IOException.class);
        when(job.getSomeWorkspace()).thenReturn(workspace);

        FormValidationAssert.assertThat(model.validateSourceDirectory(job, "sourceDirectory"))
                .isOk();
    }

    @Test
    void shouldValidateWithWorkspace() {
        SourceDirectoryValidator sourceDirectoryValidator = mock(SourceDirectoryValidator.class);
        SourceCodeConfigurationValidation model = new SourceCodeConfigurationValidation(sourceDirectoryValidator);

        String absoluteDirectory = "/absolute";
        FormValidationAssert.assertThat(model.validateSourceDirectory(mock(FreeStyleProject.class), absoluteDirectory))
                .isError()
                .hasMessage(DIRECTORY_NOT_REGISTERED);
        when(sourceDirectoryValidator.isAllowedSourceDirectory(absoluteDirectory)).thenReturn(true);
        FormValidationAssert.assertThat(model.validateSourceDirectory(mock(FreeStyleProject.class), absoluteDirectory))
                .isOk();
    }

    private SourceCodeConfigurationValidation createValidation() {
        return new SourceCodeConfigurationValidation(mock(SourceDirectoryValidator.class));
    }
}
