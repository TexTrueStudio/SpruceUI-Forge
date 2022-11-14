/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of SpruceUI.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.spruceui.option;

import me.lambdaurora.spruceui.Position;
import me.lambdaurora.spruceui.widget.SpruceWidget;
import me.lambdaurora.spruceui.widget.text.SpruceNamedTextFieldWidget;
import me.lambdaurora.spruceui.widget.text.SpruceTextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a float input option.
 *
 * @author LambdAurora
 * @version 2.1.0
 * @since 2.1.0
 */
public class SpruceFloatInputOption extends SpruceOption {
    private final Supplier<Float> getter;
    private final Consumer<Float> setter;

    public SpruceFloatInputOption(String key, Supplier<Float> getter, Consumer<Float> setter, @Nullable Text tooltip) {
        super(key);
        this.getter = getter;
        this.setter = setter;
        this.setTooltip(tooltip);
    }

    @Override
    public SpruceWidget createWidget(Position position, int width) {
        SpruceTextFieldWidget textField = new SpruceTextFieldWidget(position, width, 20, this.getPrefix());
        textField.setText(String.valueOf(this.get()));
        textField.setTextPredicate(SpruceTextFieldWidget.FLOAT_INPUT_PREDICATE);
        textField.setRenderTextProvider((displayedText, offset) -> {
            try {
                Float.parseFloat(textField.getText());
                return OrderedText.styledForwardsVisitedString(displayedText, Style.EMPTY);
            } catch (NumberFormatException e) {
                return OrderedText.styledForwardsVisitedString(displayedText, Style.EMPTY.withColor(Formatting.RED));
            }
        });
        textField.setChangedListener(input -> {
            float value;
            try {
                value = Float.parseFloat(input);
            } catch (NumberFormatException e) {
                value = 0;
            }
            this.set(value);
        });
        this.getOptionTooltip().ifPresent(textField::setTooltip);
        return new SpruceNamedTextFieldWidget(textField);
    }

    public void set(float value) {
        this.setter.accept(value);
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public float get() {
        return this.getter.get();
    }
}
