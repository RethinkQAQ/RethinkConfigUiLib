#version 150

in vec3 Position;
in vec4 Color;

// Mirrors vanilla gui.vsh. RenderPipeline supplies these blocks for regular
// GUI quads, so this renderer never mutates raw OpenGL state.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
    float LineWidth;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

out vec4 vertexColor;
out vec2 rcuiLocalUv;

void main() {
    int corner = gl_VertexID % 4;
    rcuiLocalUv = vec2(corner >= 2 ? 1.0 : 0.0, corner == 1 || corner == 2 ? 1.0 : 0.0);
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
}
