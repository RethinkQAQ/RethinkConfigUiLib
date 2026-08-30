#version 150

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 ColorModulator;

out vec4 vertexColor;
out vec2 rcuiLocalUv;

void main() {
    int corner = gl_VertexID % 4;
    rcuiLocalUv = vec2(corner >= 2 ? 1.0 : 0.0, corner == 1 || corner == 2 ? 1.0 : 0.0);
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color * ColorModulator;
}
