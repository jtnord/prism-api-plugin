package io.jenkins.plugins.prism;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.PathUtil;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import io.jenkins.plugins.util.GlobalConfigurationFacade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link PrismConfiguration}.
 *
 * @author Ullrich Hafner
 */
class PrismConfigurationTest {
    private static final PathUtil PATH_UTIL = new PathUtil();

    private static final String FIRST = "/One";
    private static final String SECOND = "/Two";
    private static final String ABSOLUTE_NOT_EXISTING = "/Three";
    private static final String RELATIVE = "Relative";

    private static final String NORMALIZED = PATH_UTIL.getAbsolutePath("/workspace");

    private static final List<PermittedSourceCodeDirectory> SOURCE_ROOTS
            = Arrays.asList(new PermittedSourceCodeDirectory(FIRST), new PermittedSourceCodeDirectory(SECOND));
    private static final SourceDirectoryFilter FILTER = new SourceDirectoryFilter();

    @Test
    void shouldHaveNoRootFoldersWhenCreated() {
        PrismConfiguration configuration = createConfiguration();

        assertThat(configuration.getSourceDirectories()).isEmpty();

        assertThat(get(configuration, "")).isEmpty();
        assertThat(get(configuration, "-")).isEmpty();
        assertThat(get(configuration, ABSOLUTE_NOT_EXISTING)).isEmpty();
        assertThat(get(configuration, RELATIVE)).containsExactly(getWorkspaceChild(RELATIVE));
    }

    @Test
    void shouldSaveConfigurationIfFoldersAreAdded() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);
        PrismConfiguration configuration = new PrismConfiguration(facade);

        configuration.setSourceDirectories(SOURCE_ROOTS);

        verify(facade).save();
        assertThat(configuration.getSourceDirectories()).isEqualTo(SOURCE_ROOTS);

        assertThat(get(configuration, "")).isEmpty();
        assertThat(get(configuration, "-")).isEmpty();
        assertThat(get(configuration, FIRST)).containsExactly(FIRST);
        assertThat(get(configuration, RELATIVE)).containsExactly(getWorkspaceChild(RELATIVE));
        assertThat(get(configuration, RELATIVE, FIRST)).containsExactlyInAnyOrder(FIRST, getWorkspaceChild(RELATIVE));
        assertThat(get(configuration, ABSOLUTE_NOT_EXISTING)).isEmpty();
        assertThat(get(configuration, ABSOLUTE_NOT_EXISTING, FIRST)).containsExactly(FIRST);

        configuration.clearRepeatableProperties();
        assertThat(configuration.getSourceDirectories()).isEmpty();
    }

    @Test
    void shouldNormalizePath() {
        PrismConfiguration configuration = createConfiguration();

        configuration.setSourceDirectories(
                Arrays.asList(new PermittedSourceCodeDirectory("/absolute/unix"),
                        new PermittedSourceCodeDirectory("C:\\absolute\\windows")));

        String relativeUnix = "relative/unix";
        String relativeWindows = "relative\\windows";
        String absoluteUnix = "/absolute/unix";
        String absoluteWindows = "C:\\absolute\\windows";
        String absoluteWindowsNormalized = "C:/absolute/windows";

        assertThat(get(configuration, relativeUnix)).containsExactly(getWorkspaceChild(relativeUnix));
        assertThat(get(configuration, relativeWindows)).containsExactly(getWorkspaceChild(relativeWindows));
        assertThat(get(configuration, absoluteUnix)).containsExactly(absoluteUnix);
        assertThat(get(configuration, absoluteWindows)).containsExactly(normalize(absoluteWindows));
        assertThat(get(configuration, absoluteWindowsNormalized)).containsExactly(absoluteWindowsNormalized);
    }

    @Test
    void shouldInitializeThemes() {
        PrismConfiguration configuration = createConfiguration();

        assertThat(configuration.getTheme())
                .isEqualTo(PrismTheme.PRISM)
                .extracting(PrismTheme::getFileName)
                .isEqualTo("prism.css");
        configuration.setTheme(PrismTheme.COY);
        assertThat(configuration.getTheme()).isEqualTo(PrismTheme.COY);

        assertThat(configuration.doFillThemeItems()).extracting(o -> o.value).contains(PrismTheme.PRISM.name());
    }

    private String getWorkspaceChild(final String expected) {
        return PATH_UTIL.createAbsolutePath(NORMALIZED, expected);
    }

    private String normalize(final String remote) {
        return PATH_UTIL.getAbsolutePath(remote);
    }

    private List<String> get(final PrismConfiguration configuration, final String... absolutePaths) {
        FilePath path = new FilePath((VirtualChannel) null, NORMALIZED);
        Set<String> sourceDirectories = configuration.getSourceDirectories()
                .stream()
                .map(PermittedSourceCodeDirectory::getPath)
                .map(PATH_UTIL::getAbsolutePath)
                .collect(Collectors.toSet());
        return FILTER.getPermittedSourceDirectories(path, sourceDirectories, new HashSet<>(Arrays.asList(absolutePaths)))
                .stream()
                .map(FilePath::getRemote)
                .map(this::normalize)
                .collect(Collectors.toList());
    }

    private PrismConfiguration createConfiguration() {
        return new PrismConfiguration(mock(GlobalConfigurationFacade.class));
    }
}
