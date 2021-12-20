package io.jenkins.plugins.prism;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.PathUtil;

import hudson.FilePath;

/**
 * Removes source code directories that are not approved from the set of source code paths. A directory is considered
 * safe if it is a sub-folder in the agent workspace. Directories outside the workspace need to be approved by an
 * administrator in Jenkins global configuration page.
 *
 * @author Ullrich Hafner
 * @see PrismConfiguration
 */
public class SourceDirectoryFilter {
    /**
     * Filters the specified collection of additional source code directories so that only permitted source directories
     * will be returned. Permitted source directories are absolute paths that have been registered using {@link
     * PrismConfiguration#setSourceDirectories(java.util.List)} or relative paths in the workspace.
     *
     * @param workspace
     *         the workspace containing the affected files
     * @param allowedSourceDirectories
     *         the approved source directories from the system configuration section
     * @param requestedSourceDirectories
     *         source directories either as a relative path in the agent workspace or as an absolute path on the agent
     *
     * @return the permitted source directories
     */
    public Set<FilePath> getPermittedSourceDirectories(final FilePath workspace,
            final Set<String> allowedSourceDirectories,
            final Set<String> requestedSourceDirectories) {
        PathUtil pathUtil = new PathUtil();
        Set<FilePath> filteredDirectories = new HashSet<>();
        for (String sourceDirectory : requestedSourceDirectories) {
            if (StringUtils.isNotBlank(sourceDirectory) && !"-".equals(sourceDirectory)) {
                String normalized = pathUtil.getAbsolutePath(sourceDirectory);
                if (pathUtil.isAbsolute(normalized)) {
                    if (allowedSourceDirectories.contains(normalized)) { // add only registered absolute paths
                        filteredDirectories.add(workspace.child(normalized));
                    }
                }
                else {
                    filteredDirectories.add(workspace.child(normalized)); // relative workspace paths are always ok
                }
            }
        }
        return filteredDirectories;
    }
}
