package io.jenkins.plugins.prism;

import org.apache.commons.lang3.StringUtils;

/**
 * An annotation for a specific line range in the source code. A range is given by the first and last line and may
 * contain column information as well.
 *
 * @author Ullrich Hafner
 */
public class Annotation {
    private final String title;
    private final String icon;
    private final String description;

    private final int lineStart;
    private final int lineEnd;
    private final int columnStart;
    private final int columnEnd;

    Annotation(final String title, final String icon, final String description, final int lineStart,
            final int lineEnd, final int columnStart,
            final int columnEnd) {
        this.title = title;
        this.icon = icon;
        this.description = description;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.columnStart = columnStart;
        this.columnEnd = columnEnd;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public int getLineStart() {
        return lineStart;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    public int getColumnStart() {
        return columnStart;
    }

    public int getColumnEnd() {
        return columnEnd;
    }

    /**
     * Creates instances of {@link Annotation annotations} using the builder pattern.
     *
     * @author Ullrich Hafner
     */
    @SuppressWarnings({"ParameterHidesMemberVariable", "checkstyle:HiddenField"})
    public static class AnnotationBuilder {
        private String title = StringUtils.EMPTY;
        private String icon = StringUtils.EMPTY;
        private String description = StringUtils.EMPTY;
        private int lineStart;
        private int lineEnd;
        private int columnStart;
        private int columnEnd;

        /**
         * Defines the title of the annotation card. This title must not contain HTML tags.
         *
         * @param title
         *         the title, should fit into a single line
         *
         * @return this builder
         */
        public AnnotationBuilder withTitle(final String title) {
            this.title = title;
            return this;
        }

        /**
         * Defines the icon of the annotation card.
         *
         * @param icon
         *         the icon (if available use an SVG icon)
         *
         * @return this builder
         */
        public AnnotationBuilder withIcon(final String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Defines the title of the annotation card.
         *
         * @param description
         *         the detailed description of the annotation. This description may contain valid HTML elements.
         *
         * @return this builder
         */
        public AnnotationBuilder withDescription(final String description) {
            this.description = description;
            return this;
        }

        /**
         * Returns the first line of this issue (lines start at 1; 0 indicates the whole file).
         *
         * @param lineStart
         *         the first line
         *
         * @return this builder
         */
        public AnnotationBuilder withLineStart(final int lineStart) {
            this.lineStart = lineStart;
            return this;
        }

        /**
         * Returns the last line of this issue (lines start at 1).
         *
         * @param lineEnd
         *         the last line
         *
         * @return this builder
         */
        public AnnotationBuilder withLineEnd(final int lineEnd) {
            this.lineEnd = lineEnd;
            return this;
        }

        /**
         * Returns the first column of this issue (columns start at 1, 0 indicates the whole line).
         *
         * @param columnStart
         *         the first column
         *
         * @return this builder
         */
        public AnnotationBuilder withColumnStart(final int columnStart) {
            this.columnStart = columnStart;
            return this;
        }

        /**
         * Returns the last column of this issue (columns start at 1).
         *
         * @param columnEnd
         *         the last column
         *
         * @return this builder
         */
        public AnnotationBuilder withColumnEnd(final int columnEnd) {
            this.columnEnd = columnEnd;
            return this;
        }

        /**
         * Creates an immutable {@link Annotation} instance using the configured properties.
         *
         * @return the new annotation
         */
        public Annotation build() {
            int providedLineStart = defaultInteger(lineStart);
            int providedLineEnd = defaultInteger(lineEnd) == 0 ? providedLineStart : defaultInteger(lineEnd);
            if (providedLineStart == 0) {
                this.lineStart = providedLineEnd;
                this.lineEnd = providedLineEnd;
            }
            else {
                this.lineStart = Math.min(providedLineStart, providedLineEnd);
                this.lineEnd = Math.max(providedLineStart, providedLineEnd);
            }

            int providedColumnStart = defaultInteger(columnStart);
            int providedColumnEnd = defaultInteger(columnEnd) == 0 ? providedColumnStart : defaultInteger(columnEnd);
            if (providedColumnStart == 0) {
                this.columnStart = providedColumnEnd;
                this.columnEnd = providedColumnEnd;
            }
            else {
                this.columnStart = Math.min(providedColumnStart, providedColumnEnd);
                this.columnEnd = Math.max(providedColumnStart, providedColumnEnd);
            }

            return new Annotation(title, icon, description, lineStart, lineEnd, columnStart, columnEnd);
        }

        /**
         * Creates a default Integer representation for undefined input parameters.
         *
         * @param integer
         *         the integer to check
         *
         * @return the valid string or a default string if the specified string is not valid
         */
        private int defaultInteger(final int integer) {
            return Math.max(integer, 0);
        }
    }
}
