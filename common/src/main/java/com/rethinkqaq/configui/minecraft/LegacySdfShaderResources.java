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

//? if >=1.21.3 {
/*final class LegacySdfShaderResources { }
*///?} else {
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;

// Embedded shader source provider used by the legacy ShaderInstance backend.
final class LegacySdfShaderResources {
    private static final String JSON = """
        {"blend":{"func":"add","srcrgb":"srcalpha","dstrgb":"1-srcalpha"},"vertex":"rethink_config_ui_lib/rcui_sdf","fragment":"rethink_config_ui_lib/rcui_sdf","samplers":[],"uniforms":[
        {"name":"ModelViewMat","type":"matrix4x4","count":16,"values":[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1]},
        {"name":"ProjMat","type":"matrix4x4","count":16,"values":[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1]},
        {"name":"ColorModulator","type":"float","count":4,"values":[1,1,1,1]},
        {"name":"SdfBounds","type":"float","count":4,"values":[0,0,1,1]},
        {"name":"SdfStyle","type":"float","count":4,"values":[0,0,0,0]}]}
        """;
    private static final String VERTEX = """
        #version 150
        in vec3 Position;
        in vec4 Color;
        uniform mat4 ModelViewMat;
        uniform mat4 ProjMat;
        out vec4 vertexColor;
        void main() { gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0); vertexColor = Color; }
        """;
    private static final String FRAGMENT = """
        #version 150
        in vec4 vertexColor;
        uniform vec4 ColorModulator;
        uniform vec4 SdfBounds;
        uniform vec4 SdfStyle;
        out vec4 fragColor;
        float roundedBoxDistance(vec2 point, vec2 halfSize, float radius) {
            vec2 corner = abs(point) - halfSize + radius;
            return length(max(corner, 0.0)) + min(max(corner.x, corner.y), 0.0) - radius;
        }
        void main() {
            vec2 halfSize = max(SdfBounds.zw * 0.5, vec2(0.5));
            float radius = min(max(SdfStyle.x, 0.0), min(halfSize.x, halfSize.y));
            vec2 point = gl_FragCoord.xy - (SdfBounds.xy + halfSize);
            float feather = max(fwidth(roundedBoxDistance(point, halfSize, radius)), 0.75);
            float alpha = 1.0 - smoothstep(-feather, feather, roundedBoxDistance(point, halfSize, radius));
            if (SdfStyle.z > 0.5) {
                float stroke = min(max(SdfStyle.y, 0.0), min(halfSize.x, halfSize.y));
                vec2 innerSize = max(halfSize - vec2(stroke), vec2(0.0));
                alpha *= smoothstep(-feather, feather, roundedBoxDistance(point, innerSize, max(radius - stroke, 0.0)));
            }
            vec4 color = vertexColor * ColorModulator;
            if (alpha * color.a <= 0.0) discard;
            fragColor = vec4(color.rgb, color.a * alpha);
        }
        """;

    private LegacySdfShaderResources() { }

    static ResourceProvider provider() {
        return LegacySdfShaderResources::resourceFromEmbeddedSource;
    }

    private static Optional<Resource> resourceFromEmbeddedSource(ResourceLocation location) {
        if (!location.getNamespace().equals("minecraft") || !location.getPath().startsWith("shaders/core/rethink_config_ui_lib/")) {
            return Optional.empty();
        }
        String shaderSource = switch (location.getPath()) {
            case "shaders/core/rethink_config_ui_lib/rcui_sdf.json" -> JSON;
            case "shaders/core/rethink_config_ui_lib/rcui_sdf.vsh" -> VERTEX;
            case "shaders/core/rethink_config_ui_lib/rcui_sdf.fsh" -> FRAGMENT;
            default -> null;
        };
        if (shaderSource == null) return Optional.empty();
        // Resource needs a non-null source pack for diagnostics. Shader bytes deliberately come
        // from the embedded strings, independent of Fabric's optional resource-pack integration.
        return Minecraft.getInstance().getResourceManager().getResource(location.withPath("shaders/core/position_color.json")).map(vanilla ->
            new Resource(vanilla.source(), () -> new ByteArrayInputStream(shaderSource.getBytes(StandardCharsets.UTF_8)))
        );
    }
}
//?}
