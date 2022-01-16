package io.jenkins.plugins.prism;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.PathUtil;

/**
 * Filters source code directories that are not approved in Jenkins' global configuration. A directory is considered
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
     * @param workspacePath
     *         the path to the workspace containing the affected files
     * @param allowedSourceDirectories
     *         the approved source directories from the system configuration section
     * @param requestedSourceDirectories
     *         source directories either as a relative path in the agent workspace or as an absolute path on the agent
     * @param log
     *         logger
     *
     * @return the permitted source directories
     */
    public Set<String> getPermittedSourceDirectories(final String workspacePath,
            final Set<String> allowedSourceDirectories,
            final Set<String> requestedSourceDirectories,
            final FilteredLog log) {
        PathUtil pathUtil = new PathUtil();
        Set<String> filteredDirectories = new HashSet<>();
        for (String sourceDirectory : requestedSourceDirectories) {
            if (StringUtils.isNotBlank(sourceDirectory) && !"-".equals(sourceDirectory)) {
                String normalized = pathUtil.getAbsolutePath(sourceDirectory);
                if (pathUtil.isAbsolute(normalized)) {
                    if (allowedSourceDirectories.contains(normalized)) { // add only registered absolute paths
                        filteredDirectories.add(normalized);
                    }
                    else {
                        log.logError("Removing source directory '%s' - "
                                + "it has not been approved in Jenkins' global configuration.", normalized);
                    }
                }
                else {
                    filteredDirectories.add(pathUtil.createAbsolutePath(workspacePath, normalized)); // relative workspace paths are always ok
                }
            }
        }
        return filteredDirectories;
    }
}
