/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of SpruceUI.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.spruceui.widget.text;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.lambdaurora.spruceui.Position;
import me.lambdaurora.spruceui.Tooltip;
import me.lambdaurora.spruceui.Tooltipable;
import me.lambdaurora.spruceui.navigation.NavigationDirection;
import me.lambdaurora.spruceui.util.ColorUtil;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a text field widget.
 *
 * @author LambdAurora
 * @version 2.1.0
 * @since 2.1.0
 */
public class SpruceTextFieldWidget extends AbstractSpruceTextInputWidget implements Tooltipable {
    public static final Predicate<String> INTEGER_INPUT_PREDICATE = input -> {
        if (input.isEmpty() || input.equals("-")) return true;
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };
    public static final Predicate<String> FLOAT_INPUT_PREDICATE = input -> {
        if (input.isEmpty() || input.equals("-") || input.equals(".")) return true;
        try {
            Float.parseFloat(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };
    public static final Predicate<String> DOUBLE_INPUT_PREDICATE = input -> {
        if (input.isEmpty() || input.equals("-") || input.equals(".")) return true;
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };

    private final Cursor cursor = new Cursor(true);
    private final Selection selection = new Selection();
    private String text = "";
    private Text tooltip;

    private Consumer<String> changedListener;
    private Predicate<String> textPredicate;
    private BiFunction<String, Integer, OrderedText> renderTextProvider;

    private int firstCharacterIndex = 0;
    private long editingTime;
    private int tooltipTicks;
    private long lastTick;

    public SpruceTextFieldWidget(@NotNull Position position, int width, int height, Text title) {
        super(position, width, height, title);
        this.cursor.toStart();
        this.sanitize();

        this.changedListener = (input) -> {
        };
        this.textPredicate = Objects::nonNull;
        this.renderTextProvider = (input, firstCharacterIndex) -> OrderedText.styledForwardsVisitedString(input, Style.EMPTY);
    }

    @Override
    public @NotNull String getText() {
        return this.text;
    }

    @Override
    public void setText(String text) {
        if (this.textPredicate.test(text)) {
            this.text = text;

            this.setCursorToEnd();
            this.selection.cancel();
            this.sanitize();
            this.onChanged();
        }
    }

    @Override
    public @NotNull Optional<Text> getTooltip() {
        return Optional.ofNullable(this.tooltip);
    }

    @Override
    public void setTooltip(@Nullable Text tooltip) {
        this.tooltip = tooltip;
    }

    public Consumer<String> getChangedListener() {
        return this.changedListener;
    }

    public void setChangedListener(Consumer<String> changedListener) {
        this.changedListener = changedListener;
    }

    public Predicate<String> getTextPredicate() {
        return this.textPredicate;
    }

    public void setTextPredicate(Predicate<String> textPredicate) {
        this.textPredicate = textPredicate;
    }

    public BiFunction<String, Integer, OrderedText> getRenderTextProvider() {
        return this.renderTextProvider;
    }

    public void setRenderTextProvider(BiFunction<String, Integer, OrderedText> renderTextProvider) {
        this.renderTextProvider = renderTextProvider;
    }

    @Override
    public void setCursorToStart() {
        this.cursor.toStart();
    }

    @Override
    public void setCursorToEnd() {
        this.cursor.toEnd();
    }

    @Override
    protected void sanitize() {
        this.cursor.sanitize();

        int textLength = this.text.length();
        if (this.firstCharacterIndex > textLength) {
            this.firstCharacterIndex = textLength;
        }

        int width = this.getInnerWidth();
        String string = this.client.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), width);
        int l = string.length() + this.firstCharacterIndex;
        if (this.cursor.column == this.firstCharacterIndex) {
            this.firstCharacterIndex -= this.client.textRenderer.trimToWidth(this.text, width, true).length();
        }

        if (this.cursor.column > l) {
            this.firstCharacterIndex += this.cursor.column - l;
        } else if (this.cursor.column <= this.firstCharacterIndex) {
            this.firstCharacterIndex -= this.firstCharacterIndex - this.cursor.column;
        }

        this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, textLength);
    }

    private void onChanged() {
        if (this.changedListener != null) {
            this.changedListener.accept(this.text);
        }

        this.editingTime = Util.getMeasuringTimeMs() + 5000L;
        this.queueNarration(500);
    }

    private boolean onSelectionUpdate(@NotNull Runnable action) {
        this.selection.tryStartSelection();
        action.run();
        this.selection.moveToCursor();
        this.sanitize();
        return true;
    }

    private void insertCharacter(char character) {
        if (this.getText().isEmpty()) {
            this.setText(String.valueOf(character));
            return;
        } else {
            this.selection.erase();
        }

        if (character == '\n') {
            return;
        }

        String text = this.getText();
        int cursorPosition = this.cursor.getPosition();

        String newText;
        if (cursorPosition >= text.length()) {
            newText = text + character;
        } else {
            newText = text.substring(0, cursorPosition) + character + text.substring(cursorPosition);
        }

        if (this.textPredicate.test(newText)) {
            this.text = newText;
            this.onChanged();
            this.cursor.moveRight();
        }
        this.sanitize();
    }

    private void eraseCharacter() {
        if (this.selection.erase()) {
            this.sanitize();
            return;
        }

        if (this.cursor.column == 0)
            return;

        String text = this.getText();
        int cursorPosition = this.cursor.getPosition();
        String newText = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
        if (this.textPredicate.test(newText)) {
            this.text = newText;
            this.onChanged();
            this.cursor.moveLeft();
        }
        this.sanitize();
    }

    private void removeCharacterForward() {
        if (this.selection.erase()) {
            this.sanitize();
            return;
        }

        if (this.getText().isEmpty()) {
            this.sanitize();
            return;
        }

        if (this.cursor.column >= this.getText().length())
            return;

        String text = this.getText();
        int cursorPosition = this.cursor.getPosition();

        String newText = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
        if (this.textPredicate.test(newText)) {
            this.text = newText;
            this.onChanged();
        }
        this.sanitize();
    }

    /**
     * Writes text where the cursor is.
     *
     * @param text the text to write
     */
    public void write(@NotNull String text) {
        if (text.isEmpty())
            return;

        if (this.getText().isEmpty()) {
            this.setText(text);
            this.setCursorToEnd();
            return;
        }
        this.selection.erase();

        String oldText = this.getText();
        int position = this.cursor.getPosition();

        String newText;
        if (position >= oldText.length()) {
            newText = oldText + text;
        } else {
            newText = oldText.substring(0, position) + text + oldText.substring(position);
        }

        if (this.textPredicate.test(newText)) {
            this.text = newText;
            this.onChanged();
            this.cursor.move(text.length());
        }
        this.sanitize();
    }

    /* Navigation */

    @Override
    public boolean onNavigation(@NotNull NavigationDirection direction, boolean tab) {
        if (this.requiresCursor()) return false;
        if (!tab && direction.isHorizontal()) {
            this.setFocused(true);
            boolean result = false;
            switch (direction) {
                case RIGHT:
                    result = this.onSelectionUpdate(this.cursor::moveRight);
                    break;
                case LEFT:
                    result = this.onSelectionUpdate(this.cursor::moveLeft);
                    break;
                default:
                    break;
            }
            if (result)
                return true;
        }
        return super.onNavigation(direction, tab);
    }

    /* Input */

    @Override
    protected boolean onCharTyped(char chr, int keyCode) {
        if (!this.isEditorActive() || !SharedConstants.isValidChar(chr))
            return false;

        if (this.isActive()) {
            this.insertCharacter(chr);
            this.selection.cancel();
        }
        return true;
    }

    @Override
    protected boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (!this.isEditorActive())
            return false;

        if (Screen.isSelectAll(keyCode)) {
            this.selection.selectAll();
            this.sanitize();
            return true;
        } else if (Screen.isPaste(keyCode)) {
            this.write(MinecraftClient.getInstance().keyboard.getClipboard());
            return true;
        } else if (Screen.isCopy(keyCode) || Screen.isCut(keyCode)) {
            String selected = this.selection.getSelectedText();
            if (!selected.isEmpty())
                MinecraftClient.getInstance().keyboard.setClipboard(selected);
            if (Screen.isCut(keyCode)) {
                this.selection.erase();
                this.sanitize();
            }
            return true;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_RIGHT:
                return this.onSelectionUpdate(this.cursor::moveRight);
            case GLFW.GLFW_KEY_LEFT:
                return this.onSelectionUpdate(this.cursor::moveLeft);
            case GLFW.GLFW_KEY_END:
                return this.onSelectionUpdate(this.cursor::toEnd);
            case GLFW.GLFW_KEY_HOME:
                return this.onSelectionUpdate(this.cursor::toStart);
            case GLFW.GLFW_KEY_BACKSPACE:
                this.eraseCharacter();
                return true;
            case GLFW.GLFW_KEY_DELETE:
                this.removeCharacterForward();
                return true;
            case GLFW.GLFW_KEY_D:
                if (Screen.hasControlDown() && !this.text.isEmpty()) {
                    this.setText("");
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    protected boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = MathHelper.floor(mouseX) - this.getX() - 4;

            this.setFocused(true);

            this.onSelectionUpdate(() -> {
                String displayedText = this.client.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
                this.cursor.lastColumn = this.cursor.column = this.firstCharacterIndex + this.client.textRenderer.trimToWidth(displayedText, x).length();
            });

            return true;
        }

        return false;
    }

    /* Rendering */

    @Override
    protected void renderWidget(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderWidget(matrices, mouseX, mouseY, delta);

        this.drawText(matrices);
        this.drawCursor(matrices);

        if (!this.dragging && this.editingTime == 0) {
            Tooltip.queueFor(this, mouseX, mouseY, this.tooltipTicks, i -> this.tooltipTicks = i, this.lastTick, i -> this.lastTick = i);
        } else if (this.editingTime < Util.getMeasuringTimeMs()) {
            this.editingTime = 0;
        }
    }

    /**
     * Draws the text of the text area.
     *
     * @param matrices the matrices
     */
    protected void drawText(@NotNull MatrixStack matrices) {
        int textColor = this.getTextColor();
        int x = this.getX() + 4;
        int y = this.getY() + this.getHeight() / 2 - 4;

        String displayedText = this.client.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());

        this.client.textRenderer.drawWithShadow(matrices, this.renderTextProvider.apply(displayedText, this.firstCharacterIndex), x, y, textColor);
        this.drawSelection(displayedText, y);
    }

    /**
     * Draws the selection over the text.
     *
     * @param line the current line
     * @param lineY the line Y-coordinates
     */
    protected void drawSelection(@NotNull String line, int lineY) {
        if (!this.isFocused() || !this.selection.active)
            return;

        int startIndex = Math.max(0, selection.getStart().column - this.firstCharacterIndex);
        int endIndex = Math.min(line.length(), selection.getEnd().column - this.firstCharacterIndex);

        if (startIndex >= line.length())
            return;

        int x = this.getX() + 4 + this.client.textRenderer.getWidth(line.substring(0, startIndex));
        String selected = line.substring(startIndex, endIndex);

        int x2 = x + this.client.textRenderer.getWidth(selected);
        int y2 = lineY + this.client.textRenderer.fontHeight;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.color4f(0.f, 0.f, 255.f, 255.f);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        buffer.begin(7, VertexFormats.POSITION);
        buffer.vertex(x, y2, 0.d).next();
        buffer.vertex(x2, y2, 0.d).next();
        buffer.vertex(x2, lineY, 0.d).next();
        buffer.vertex(x, lineY, 0.d).next();
        tessellator.draw();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    /**
     * Draws the cursor.
     *
     * @param matrices the matrices
     */
    protected void drawCursor(@NotNull MatrixStack matrices) {
        if (!this.isFocused())
            return;

        int cursorY = this.getY() + this.getHeight() / 2 - 4;

        if (this.text.isEmpty()) {
            drawTextWithShadow(matrices, this.client.textRenderer, new LiteralText("_"), this.getX() + 4, cursorY, ColorUtil.TEXT_COLOR);
            return;
        }

        this.cursor.sanitize();

        String cursorLine = this.text.substring(this.firstCharacterIndex);
        int cursorX = this.getX() + 4 + this.client.textRenderer.getWidth(cursorLine.substring(0, this.cursor.column - this.firstCharacterIndex));

        if (this.cursor.column - this.firstCharacterIndex < cursorLine.length())
            fill(matrices, cursorX - 1, cursorY - 1, cursorX, cursorY + 9, ColorUtil.TEXT_COLOR);
        else
            this.client.textRenderer.drawWithShadow(matrices, "_", cursorX, cursorY, ColorUtil.TEXT_COLOR);
    }

    /**
     * Represents a cursor.
     *
     * @version 2.1.0
     * @since 2.1.0
     */
    public class Cursor {
        boolean main;
        int column = 0;
        private int lastColumn = 0;

        public Cursor(boolean main) {
            this.main = main;
        }

        public void toStart() {
            this.lastColumn = this.column = 0;
        }

        public void moveRight() {
            this.move(1);
        }

        public void moveLeft() {
            this.move(-1);
        }

        public void move(int amount) {
            this.column += amount;

            if (this.column < 0) {
                this.toStart();
            } else if (this.column > text.length()) {
                this.column = text.length();
            }

            this.lastColumn = this.column;

            if (amount < 0 && this.column <= SpruceTextFieldWidget.this.firstCharacterIndex) {
                SpruceTextFieldWidget.this.firstCharacterIndex = MathHelper.clamp(SpruceTextFieldWidget.this.firstCharacterIndex = this.column - 1, 0, text.length());
            }
        }

        public void toEnd() {
            this.lastColumn = this.column = text.length();
        }

        /**
         * Copies the column from another cursor.
         *
         * @param cursor the other cursor
         */
        public void copy(@NotNull SpruceTextFieldWidget.Cursor cursor) {
            this.lastColumn = this.column = cursor.column;
        }

        /**
         * Sanitizes the cursor.
         */
        public void sanitize() {
            if (this.column < 0)
                this.toStart();
            else if (this.column > text.length())
                this.column = text.length();
        }

        /**
         * Returns whether this cursor is at the same place as the other cursor.
         *
         * @param other the other cursor
         * @return {@code true} if this cursor is at the same place as the other cursor, else {@code false}
         */
        public boolean isSame(@NotNull SpruceTextFieldWidget.Cursor other) {
            return this.column == other.column;
        }

        /**
         * Returns the position of the cursor in the text.
         *
         * @return the position
         */
        public int getPosition() {
            return this.column;
        }

        @Override
        public String toString() {
            return "SpruceTextAreaWidget$Cursor{main=" + this.main
                    + ", column=" + this.column
                    + ", lastColumn=" + this.lastColumn
                    + "}";
        }
    }

    /**
     * Represents a selection.
     *
     * @version 2.1.0
     * @since 2.1.0
     */
    public class Selection {
        private Cursor anchor = new Cursor(false);
        private Cursor follower = new Cursor(false);
        private boolean active = false;

        /**
         * Selects all.
         */
        public void selectAll() {
            this.anchor.toStart();
            cursor.toEnd();
            this.follower.copy(cursor);
            this.active = true;
        }

        /**
         * Cancels the selection.
         */
        public void cancel() {
            this.anchor.toStart();
            this.follower.toStart();
            this.active = false;
        }

        public void tryStartSelection() {
            if (!this.active && Screen.hasShiftDown()) {
                this.startSelection();
            }
        }

        public void startSelection() {
            this.anchor.copy(cursor);
            this.follower.copy(cursor);
            this.active = true;
        }

        public void moveToCursor() {
            if (!this.active)
                return;

            if (Screen.hasShiftDown()) {
                this.follower.copy(cursor);
            } else {
                this.cancel();
            }
        }

        /**
         * Erases the selected text.
         *
         * @return {@code true} if the text has been erased, else {@code false}
         */
        public boolean erase() {
            if (!this.active)
                return false;

            Cursor start = this.getStart();
            Cursor end = this.getEnd();

            if (start.isSame(end)) {
                this.cancel();
                return false;
            }

            if (start.column == 0 && end.column >= text.length()) {
                text = "";
                this.cancel();
                return true;
            }

            String text = getText();
            String newText = text.substring(0, start.getPosition()) + text.substring(end.getPosition());
            if (SpruceTextFieldWidget.this.textPredicate.test(newText)) {
                SpruceTextFieldWidget.this.text = newText;
                SpruceTextFieldWidget.this.onChanged();
            }

            cursor.copy(start);

            this.cancel();
            return true;
        }

        /**
         * Returns the selected text.
         *
         * @return the selected text, if no text is selected the return value is an empty string
         */
        public @NotNull String getSelectedText() {
            if (!this.active)
                return "";

            Cursor start = this.getStart();
            Cursor end = this.getEnd();

            if (start.isSame(end))
                return "";

            return getText().substring(start.getPosition(), end.getPosition());
        }

        public @NotNull Cursor getStart() {
            return this.isInverted() ? this.follower : this.anchor;
        }

        public @NotNull Cursor getEnd() {
            return this.isInverted() ? this.anchor : this.follower;
        }

        private boolean isInverted() {
            return this.anchor.column > this.follower.column;
        }
    }
}
