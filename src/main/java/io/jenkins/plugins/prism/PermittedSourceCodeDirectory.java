package io.jenkins.plugins.prism;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Approved directory that contains source code files that can be shown in Jenkins´ user interface.
 *
 * @author Ullrich Hafner
 */
public class PermittedSourceCodeDirectory extends AbstractDescribableImpl<PermittedSourceCodeDirectory> implements Serializable {
    private static final long serialVersionUID = -3864564528382064924L;

    private final String path;

    /**
     * Creates a new instance of {@link PermittedSourceCodeDirectory}.
     *
     * @param path
     *         the name of the directory
     */
    @DataBoundConstructor
    public PermittedSourceCodeDirectory(final String path) {
        super();

        this.path = path;
    }

    public String getPath() {
        return path;
    }

    /**
     * Descriptor to validate {@link PermittedSourceCodeDirectory}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<PermittedSourceCodeDirectory> {
        @NonNull
        @Override
        public String getDisplayName() {
            return StringUtils.EMPTY;
        }
    }
}

