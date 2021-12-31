package io.jenkins.plugins.prism;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import hudson.util.ComboBoxModel;

import io.jenkins.plugins.util.FormValidationAssert;

import static io.jenkins.plugins.prism.SourceEncodingValidation.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link SourceDirectoryValidation}.
 *
 * @author Arne Schöntag
 * @author Stephan Plöderl
 * @author Ullrich Hafner
 */
class SourceEncodingValidationTest {
    @Test
    void shouldValidateCharsets() {
        SourceEncodingValidation model = new SourceEncodingValidation();

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
        SourceEncodingValidation model = new SourceEncodingValidation();

        ComboBoxModel allCharsets = model.getAllCharsets();
        assertThat(allCharsets).isNotEmpty().contains("UTF-8", "ISO-8859-1");
    }

    @Test
    void shouldFallbackToPlatformCharset() {
        SourceEncodingValidation model = new SourceEncodingValidation();

        assertThat(model.getCharset("UTF-8")).isEqualTo(StandardCharsets.UTF_8);
        assertThat(model.getCharset("nothing")).isEqualTo(Charset.defaultCharset());
        assertThat(model.getCharset("")).isEqualTo(Charset.defaultCharset());
        assertThat(model.getCharset(null)).isEqualTo(Charset.defaultCharset());
    }

}
