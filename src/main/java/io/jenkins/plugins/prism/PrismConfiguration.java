package io.jenkins.plugins.prism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;

import io.jenkins.plugins.util.GlobalConfigurationFacade;
import io.jenkins.plugins.util.GlobalConfigurationItem;

/**
 * Global system configuration for Prism. These configuration options are used globally for all jobs and require
 * administrator permissions.
 * <p>
 * The following settings can be configured:
 * </p>
 *
 * <ul>
 *     <li>
 *      <b>Allowed source code directories</b>: some plugins copy source code files to Jenkins' build folder so that these
 *      files can be rendered in the user interface together with build results (coverage, warnings, etc.).
 *      If these files are not part of the workspace of a build then Jenkins will not show them by default:
 *      otherwise sensitive files could be shown by accident. You can provide a list of additional source code directories
 *      that are allowed to be shown in Jenkins user interface here. Note, that such a directory must be an absolute path
 *      on the agent that executes the build.
 *     </li>
 *     <li>
 *      <b>Theme</b>: Prism supports several themes that can be used to adapt the look and feel. You can configure the
 *      default theme that is used for all Jenkins jobs.
 *     </li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
@Extension
@Symbol("prismConfiguration")
public class PrismConfiguration extends GlobalConfigurationItem {
    private static final PathUtil PATH_UTIL = new PathUtil();

    private List<SourceCodeDirectory> sourceDirectories = Collections.emptyList();
    private Set<String> normalizedSourceDirectories = Collections.emptySet();
    private PrismTheme theme = PrismTheme.PRISM;

    /**
     * Creates the global configuration of source code directories and loads the initial values from the corresponding
     * XML file.
     */
    public PrismConfiguration() {
        super();

        load();
    }

    @VisibleForTesting
    PrismConfiguration(final GlobalConfigurationFacade facade) {
        super(facade);

        load();
    }

    @Override
    protected void clearRepeatableProperties() {
        setSourceDirectories(new ArrayList<>());
    }

    /**
     * Returns the singleton instance of this {@link PrismConfiguration}.
     *
     * @return the singleton instance
     */
    public static PrismConfiguration getInstance() {
        return GlobalConfiguration.all().get(PrismConfiguration.class);
    }

    /**
     * Returns the list of allowed source code directories.
     *
     * @return the source root folders
     */
    public List<SourceCodeDirectory> getSourceDirectories() {
        return sourceDirectories;
    }

    /**
     * Sets the list of source directories to the specified elements. Previously set directories will be removed.
     *
     * @param sourceDirectories
     *         the source directories that contain the affected files
     */
    @DataBoundSetter
    public void setSourceDirectories(final List<SourceCodeDirectory> sourceDirectories) {
        this.sourceDirectories = new ArrayList<>(sourceDirectories);

        normalizedSourceDirectories = sourceDirectories.stream()
                .map(SourceCodeDirectory::getPath)
                .map(PATH_UTIL::getAbsolutePath)
                .collect(Collectors.toSet());

        save();
    }

    /**
     * Filters the specified collection of additional source code directories so that only permitted source directories
     * will be returned. Permitted source directories are absolute paths that have been registered using {@link
     * #setSourceDirectories(List)} or relative paths in the workspace.
     *
     * @param workspace
     *         the workspace containing the affected files
     * @param requestedSourceDirectories
     *         additional source directories (might be empty): either using a relative path in the workspace or an absolute path
     *
     * @return the permitted source directories
     */
    public Set<FilePath> getPermittedSourceDirectories(final FilePath workspace, final List<String> requestedSourceDirectories) {
        PathUtil pathUtil = new PathUtil();
        Set<FilePath> permittedDirectories = new HashSet<>();
        for (String sourceDirectory : requestedSourceDirectories) {
            if (StringUtils.isNotBlank(sourceDirectory) && !"-".equals(sourceDirectory)) {
                String normalized = pathUtil.getAbsolutePath(sourceDirectory);
                if (pathUtil.isAbsolute(normalized)) {
                    if (normalizedSourceDirectories.contains(normalized)) { // add only registered absolute paths
                        permittedDirectories.add(workspace.child(normalized));
                    }
                }
                else {
                    permittedDirectories.add(workspace.child(normalized)); // relative workspace paths are always ok
                }
            }
        }
        return permittedDirectories;
    }

    /**
     * Sets the active theme to be used when rendering the source code with prism.
     *
     * @param theme
     *         the theme to use
     */
    @DataBoundSetter
    public void setTheme(final PrismTheme theme) {
        this.theme = theme;
    }

    public PrismTheme getTheme() {
        return theme;
    }

    /**
     * Returns all available themes.
     *
     * @return a model with all available themes
     */
    public ListBoxModel doFillThemeItems() {
        ListBoxModel options = new ListBoxModel();
        options.addAll(PrismTheme.getAllDisplayNames());
        return options;
    }
}
