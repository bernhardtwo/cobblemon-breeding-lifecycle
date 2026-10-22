package io.github.bernhardtwo.breedinglifecycle.screenshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ScreenshotsSwitchTest {
    @Test
    void onlyTrueEnables() {
        for (String value : new String[] {null, "false", "yes", ""}) {
            Properties props = new Properties();
            if (value != null) {
                props.setProperty("breedinglifecycle.screenshots", value);
            }
            props.setProperty("breedinglifecycle.screenshots.serverConfig", "/x/c.json");
            assertThat(ScreenshotsSwitch.read(props)).isEmpty();
        }
        Properties on = new Properties();
        on.setProperty("breedinglifecycle.screenshots", "true");
        on.setProperty("breedinglifecycle.screenshots.serverConfig", "/x/c.json");
        assertThat(ScreenshotsSwitch.read(on)).contains(Path.of("/x/c.json"));
    }

    @Test
    void missingServerConfigFails() {
        Properties props = new Properties();
        props.setProperty("breedinglifecycle.screenshots", "true");
        assertThatThrownBy(() -> ScreenshotsSwitch.read(props))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("breedinglifecycle.screenshots.serverConfig is not set");
    }
}
