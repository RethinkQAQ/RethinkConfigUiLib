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
    float outerDistance = roundedBoxDistance(point, halfSize, radius);
    float feather = max(fwidth(outerDistance), 0.75);
    float alpha = 1.0 - smoothstep(-feather, feather, outerDistance);
    if (SdfStyle.z > 0.5) {
        float stroke = min(max(SdfStyle.y, 0.0), min(halfSize.x, halfSize.y));
        vec2 innerSize = max(halfSize - vec2(stroke), vec2(0.0));
        float innerRadius = max(radius - stroke, 0.0);
        alpha *= smoothstep(-feather, feather, roundedBoxDistance(point, innerSize, innerRadius));
    }
    vec4 color = vertexColor * ColorModulator;
    if (alpha * color.a <= 0.0) discard;
    fragColor = vec4(color.rgb, color.a * alpha);
}
