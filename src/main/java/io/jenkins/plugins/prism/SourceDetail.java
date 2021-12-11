package io.jenkins.plugins.prism;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang3.exception.ExceptionUtils;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Renders a source file containing an issue for the whole file or a specific line number.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class SourceDetail implements ModelObject {
    private final Run<?, ?> owner;
    private final String fileName;
    private final String sourceCode;

    /**
     * Creates a new source code view model instance.
     *
     * @param owner
     *         the current build as owner of this view
     * @param fileName
     *         the file name of the shown content
     * @param sourceCodeReader
     *         the source code file to show, provided by a {@link Reader} instance
     * @param annotation
     *         an annotation to show in the source code view
     */
    public SourceDetail(final Run<?, ?> owner, final String fileName, final Reader sourceCodeReader, final Annotation annotation) {
        this.owner = owner;

        this.fileName = fileName;
        sourceCode = render(sourceCodeReader, annotation);
    }

    private String render(final Reader affectedFile, final Annotation annotation) {
        try (BufferedReader reader = new BufferedReader(affectedFile)) {
            SourcePrinter sourcePrinter = new SourcePrinter();
            return sourcePrinter.render(fileName, reader.lines(), annotation);
        }
        catch (IOException e) {
            return String.format("%s%n%s", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public String getDisplayName() {
        return fileName;
    }

    /**
     * Returns the build as owner of this view.
     *
     * @return the build
     */
    public Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the colorized source code.
     *
     * @return the source code
     */
    public String getSourceCode() {
        return sourceCode;
    }
}

