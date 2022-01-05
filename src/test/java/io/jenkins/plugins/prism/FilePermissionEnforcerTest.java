package io.jenkins.plugins.prism;

import java.io.File;

import org.junit.jupiter.api.Test;

import hudson.FilePath;

import static hudson.Functions.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

/**
 * Tests the class {@link FilePermissionEnforcer}.
 *
 * @author Ullrich Hafner
 */
class FilePermissionEnforcerTest {
    private static final FilePath WORKSPACE = new FilePath(new File("/workspace"));

    @Test
    void shouldComparePathsOnUnix() {
        FilePermissionEnforcer validator = new FilePermissionEnforcer();
        assertThat(validator.isInWorkspace("/a/b.c", WORKSPACE, "/a")).isTrue();
        assertThat(validator.isInWorkspace("/a/b.c", WORKSPACE, "/")).isTrue();

        assertThat(validator.isInWorkspace("/a/b.c", WORKSPACE, "/a/b")).isFalse();
        assertThat(validator.isInWorkspace("/b/a/b.c", WORKSPACE, "/a")).isFalse();
    }

    @Test
    void shouldAllowWorkspaceByDefault() {
        FilePermissionEnforcer validator = new FilePermissionEnforcer();
        assertThat(validator.isInWorkspace("/workspace/b.c", WORKSPACE, "/a")).isTrue();
        assertThat(validator.isInWorkspace("/b/workspace/b.c", WORKSPACE, "/a")).isFalse();
        assertThat(validator.isInWorkspace("b.c", WORKSPACE, "/a")).isFalse();
    }

    @Test @org.jvnet.hudson.test.Issue("JENKINS-63782")
    void shouldComparePathsCaseInsensitiveOnWindows() {
        assumeThat(isWindows()).isTrue();

        FilePermissionEnforcer validator = new FilePermissionEnforcer();

        assertThat(validator.isInWorkspace("C:\\a\\b.c", WORKSPACE, "C:\\a")).isTrue();
        assertThat(validator.isInWorkspace("C:\\a\\b.c", WORKSPACE, "C:\\")).isTrue();
        assertThat(validator.isInWorkspace("C:\\a\\b.c", WORKSPACE, "C:\\A")).isTrue();
        assertThat(validator.isInWorkspace("C:\\A\\b.c", WORKSPACE, "C:\\a")).isTrue();
        assertThat(validator.isInWorkspace("c:\\a\\b.c", WORKSPACE, "C:\\a")).isTrue();
    }
}
