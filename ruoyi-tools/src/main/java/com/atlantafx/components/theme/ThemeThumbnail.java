/* SPDX-License-Identifier: MIT */

package com.atlantafx.components.theme;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Theme;
import com.atlantafx.util.FileResource;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * 主题缩略图
 */
public final class ThemeThumbnail extends VBox implements Toggle {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private final RadioButton toggle; // internal helper node to manage selected state

    private final Theme theme;

    private Map<String, String> colors;

    public ThemeThumbnail(Theme theme) {
        super();

        toggle = new RadioButton();
        this.theme = theme;

        try {
            Map<String, String> colors = this.parseColors();

            var circles = new HBox(
                    createCircle(colors.get("-color-fg-default"), colors.get("-color-fg-default"), false),
                    createCircle(colors.get("-color-fg-default"), colors.get("-color-accent-emphasis"), true),
                    createCircle(colors.get("-color-fg-default"), colors.get("-color-success-emphasis"), true),
                    createCircle(colors.get("-color-fg-default"), colors.get("-color-danger-emphasis"), true),
                    createCircle(colors.get("-color-fg-default"), colors.get("-color-warning-emphasis"), true)
            );
            circles.setAlignment(Pos.CENTER);

            var nameLbl = new Label(theme.getName());
            nameLbl.getStyleClass().add(Styles.TEXT_CAPTION);
            Styles.appendStyle(nameLbl, "-fx-text-fill", colors.get("-color-fg-muted"));

            setStyle("""
                    -fx-background-radius: 10px, 8px;
                    -fx-background-insets: 0, 3px
                    """
            );
            Styles.appendStyle(
                    this,
                    "-fx-background-color",
                    "-color-thumbnail-border," + colors.get("-color-bg-default")
            );
            setOnMouseClicked(e -> setSelected(true));
            getStyleClass().add("theme-thumbnail");
            getChildren().setAll(nameLbl, circles);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        selectedProperty().addListener(
                (obs, old, val) -> pseudoClassStateChanged(SELECTED, val)
        );
    }

    private Circle createCircle(String borderColor, String bgColor, boolean overlap) {
        var circle = new Circle(10);
        Styles.appendStyle(circle, "-fx-stroke", borderColor);
        Styles.appendStyle(circle, "-fx-fill", bgColor);
        if (overlap) {
            HBox.setMargin(circle, new Insets(0, 0, 0, -5));
        }
        return circle;
    }

    @Override
    public ToggleGroup getToggleGroup() {
        return toggle.getToggleGroup();
    }

    @Override
    public void setToggleGroup(ToggleGroup toggleGroup) {
        toggle.setToggleGroup(toggleGroup);
    }

    @Override
    public ObjectProperty<ToggleGroup> toggleGroupProperty() {
        return toggle.toggleGroupProperty();
    }

    @Override
    public boolean isSelected() {
        return toggle.isSelected();
    }

    @Override
    public void setSelected(boolean selected) {
        toggle.setSelected(selected);
    }

    @Override
    public BooleanProperty selectedProperty() {
        return toggle.selectedProperty();
    }

    @Override
    public void setUserData(Object value) {
        toggle.setUserData(value);
    }

    public Map<String, String> parseColors() throws IOException {
        FileResource file = FileResource.createInternal(theme.getUserAgentStylesheet(), Theme.class);
        return file.internal() ? parseColorsForClasspath(file) : parseColorsForFilesystem(file);
    }

    private static final Pattern COLOR_PATTERN =
            Pattern.compile("\s*?(-color-(fg|bg|accent|success|danger|warning)-.+?):\s*?(.+?);");

    private static final int PARSE_LIMIT = 250;

    private Map<String, String> parseColors(BufferedReader br) throws IOException {
        Map<String, String> colors = new HashMap<>();

        String line;
        int lineCount = 0;

        while ((line = br.readLine()) != null) {
            Matcher matcher = COLOR_PATTERN.matcher(line);
            if (matcher.matches()) {
                colors.put(matcher.group(1), matcher.group(3));
            }

            lineCount++;
            if (lineCount > PARSE_LIMIT) {
                break;
            }
        }

        return colors;
    }

    private Map<String, String> parseColorsForClasspath(FileResource file) throws IOException {
        try (var br = new BufferedReader(new InputStreamReader(file.getInputStream(), UTF_8))) {
            colors = parseColors(br);
        }
        return colors;
    }

    private FileTime lastModified;

    private Map<String, String> parseColorsForFilesystem(FileResource file) throws IOException {
        // return cached colors if file wasn't changed since the last read
        FileTime fileTime = Files.getLastModifiedTime(file.toPath(), NOFOLLOW_LINKS);
        if (Objects.equals(fileTime, lastModified)) {
            return colors;
        }

        try (var br = new BufferedReader(new InputStreamReader(file.getInputStream(), UTF_8))) {
            colors = parseColors(br);
        }

        // don't save time before parsing is finished to avoid
        // remembering operation that might end up with an error
        lastModified = fileTime;

        return colors;
    }
}