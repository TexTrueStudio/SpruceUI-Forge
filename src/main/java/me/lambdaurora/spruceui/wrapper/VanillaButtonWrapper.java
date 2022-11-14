/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of SpruceUI.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.spruceui.wrapper;

import me.lambdaurora.spruceui.navigation.NavigationDirection;
import me.lambdaurora.spruceui.widget.AbstractSpruceButtonWidget;
import me.lambdaurora.spruceui.widget.SpruceElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a vanilla button wrapper for SpruceUI's own button widgets.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
@Environment(EnvType.CLIENT)
public class VanillaButtonWrapper extends ClickableWidget implements SpruceElement {
    private final AbstractSpruceButtonWidget widget;

    public VanillaButtonWrapper(@NotNull AbstractSpruceButtonWidget widget) {
        super(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), widget.getMessage());
        this.widget = widget;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.widget.getPosition().setRelativeY(this.y);
        this.widget.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.widget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.widget.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean onNavigation(@NotNull NavigationDirection direction, boolean tab) {
        return this.widget.onNavigation(direction, tab);
    }

    @Override
    public boolean changeFocus(boolean down) {
        boolean value = this.onNavigation(down ? NavigationDirection.DOWN : NavigationDirection.UP, true);
        return value;
    }
}
