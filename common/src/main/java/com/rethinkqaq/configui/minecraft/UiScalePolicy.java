/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 *
 * Rethink Config UI Lib is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 3 of the License.
 *
 * Rethink Config UI Lib is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Rethink Config UI Lib. If not, see <https://www.gnu.org/licenses/>.
 */
package com.rethinkqaq.configui.minecraft;

/**
 * Describes how a UI surface relates to Minecraft's GUI Scale setting.
 *
 * <p>{@link #minecraft()} preserves the normal Minecraft behaviour. {@link #adaptive()} keeps
 * a surface close to the physical size it has at GUI Scale 4, which is useful for standalone
 * configuration pages on high resolution displays. It does not alter the player's Minecraft
 * option; it only transforms this UI surface and its input coordinates.</p>
 */
public final class UiScalePolicy {
    private static final UiScalePolicy MINECRAFT = new UiScalePolicy(0f, 1f, 1f);
    private static final UiScalePolicy ADAPTIVE = new UiScalePolicy(4f, .5f, 4f);

    private final float referenceGuiScale;
    private final float minimumContentScale;
    private final float maximumContentScale;

    private UiScalePolicy(float referenceGuiScale, float minimumContentScale, float maximumContentScale) {
        this.referenceGuiScale = referenceGuiScale;
        this.minimumContentScale = minimumContentScale;
        this.maximumContentScale = maximumContentScale;
    }

    /** Uses Minecraft's selected GUI scale without additional UI scaling. */
    public static UiScalePolicy minecraft() { return MINECRAFT; }

    /**
     * Keeps controls and Minecraft text near their GUI Scale 4 physical size. The factor is
     * clamped from 0.5x to 4x, so accidental or unusual GUI scales remain safe.
     */
    public static UiScalePolicy adaptive() { return ADAPTIVE; }

    /** Starts a policy with a custom reference GUI scale. */
    public static Builder builder(float referenceGuiScale) { return new Builder(referenceGuiScale); }

    /** The factor used to transform the UI's logical canvas. */
    public float contentScale(double minecraftGuiScale) {
        if (referenceGuiScale <= 0f || !Double.isFinite(minecraftGuiScale) || minecraftGuiScale <= 0d) return 1f;
        float factor = (float) (referenceGuiScale / minecraftGuiScale);
        return Math.max(minimumContentScale, Math.min(maximumContentScale, factor));
    }

    public static final class Builder {
        private final float referenceGuiScale;
        private float minimumContentScale = .5f;
        private float maximumContentScale = 4f;

        private Builder(float referenceGuiScale) {
            if (!Float.isFinite(referenceGuiScale) || referenceGuiScale <= 0f) {
                throw new IllegalArgumentException("reference GUI scale must be positive");
            }
            this.referenceGuiScale = referenceGuiScale;
        }

        /** Limits the smallest allowed UI transform. */
        public Builder minimumContentScale(float value) {
            if (!Float.isFinite(value) || value <= 0f) throw new IllegalArgumentException("minimum content scale must be positive");
            minimumContentScale = value;
            return this;
        }

        /** Limits the largest allowed UI transform. */
        public Builder maximumContentScale(float value) {
            if (!Float.isFinite(value) || value <= 0f) throw new IllegalArgumentException("maximum content scale must be positive");
            maximumContentScale = value;
            return this;
        }

        public UiScalePolicy build() {
            if (minimumContentScale > maximumContentScale) {
                throw new IllegalStateException("minimum content scale cannot exceed maximum content scale");
            }
            return new UiScalePolicy(referenceGuiScale, minimumContentScale, maximumContentScale);
        }
    }
}
