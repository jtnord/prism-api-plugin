package io.jenkins.plugins.prism;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;

/**
 * Validates all properties (encoding, directories) of the source code configuration.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.GodClass")
public class SourceCodeConfigurationValidation {
    static final String DIRECTORY_NOT_REGISTERED = "Source directory is not permitted yet. Please registered this directory in Jenkins global configuration.";

    private static final Set<String> ALL_CHARSETS = Charset.availableCharsets().keySet();
    private final SourceDirectoryValidator sourceDirectoryValidator;

    /**
     * Creates a new instance of {@link SourceCodeConfigurationValidation}.
     */
    public SourceCodeConfigurationValidation() {
        this(new SourceDirectoryValidator());
    }

    @VisibleForTesting
    SourceCodeConfigurationValidation(final SourceDirectoryValidator sourceDirectoryValidator) {
        this.sourceDirectoryValidator = sourceDirectoryValidator;
    }

    /**
     * Returns all available character set names.
     *
     * @return all available character set names
     */
    public ComboBoxModel getAllCharsets() {
        return new ComboBoxModel(ALL_CHARSETS);
    }

    /**
     * Returns the default charset for the specified encoding string. If the default encoding is empty or {@code null},
     * or if the charset is not valid then the default encoding of the platform is returned.
     *
     * @param charset
     *         identifier of the character set
     *
     * @return the default charset for the specified encoding string
     */
    public Charset getCharset(@CheckForNull final String charset) {
        try {
            if (StringUtils.isNotBlank(charset)) {
                return Charset.forName(charset);
            }
        }
        catch (UnsupportedCharsetException | IllegalCharsetNameException exception) {
            // ignore and return default
        }
        return Charset.defaultCharset();
    }

    /**
     * Performs on-the-fly validation of the character encoding.
     *
     * @param reportEncoding
     *         the character encoding
     *
     * @return the validation result
     */
    public FormValidation validateCharset(final String reportEncoding) {
        try {
            if (StringUtils.isBlank(reportEncoding) || Charset.isSupported(reportEncoding)) {
                return FormValidation.ok();
            }
        }
        catch (IllegalCharsetNameException | UnsupportedCharsetException ignore) {
            // throw a FormValidation error
        }
        return FormValidation.errorWithMarkup(createWrongEncodingErrorMessage());
    }

    @VisibleForTesting
    static String createWrongEncodingErrorMessage() {
        return Messages.FieldValidator_Error_DefaultEncoding(
                "https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html");
    }

    /**
     * Performs on-the-fly validation on the source code directory. Checks if relative paths are part of the workspace and
     * that absolute paths are registered.
     *
     * @param project
     *         the project that is configured
     * @param sourceDirectory
     *         the source directory to use
     *
     * @return the validation result
     */
    public FormValidation validateSourceDirectory(@CheckForNull final AbstractProject<?, ?> project, final String sourceDirectory) {
        PathUtil pathUtil = new PathUtil();
        if (pathUtil.isAbsolute(sourceDirectory)) {
            if (sourceDirectoryValidator.isAllowedSourceDirectory(sourceDirectory)) {
                return FormValidation.ok();
            }
            return FormValidation.error(DIRECTORY_NOT_REGISTERED);
        }
        if (project != null) { // there is no workspace in pipelines
            try {
                FilePath workspace = project.getSomeWorkspace();
                if (workspace != null && workspace.exists()) {
                    return workspace.validateRelativeDirectory(sourceDirectory);
                }
            }
            catch (InterruptedException | IOException ignore) {
                // ignore and return ok
            }
        }

        return FormValidation.ok();
    }

    static class SourceDirectoryValidator {
        boolean isAllowedSourceDirectory(final String sourceDirectory) {
            return PrismConfiguration.getInstance().isAllowedSourceDirectory(sourceDirectory);
        }
    }
}
