#version 150

#ifndef RCUI_RADIUS
#define RCUI_RADIUS 0
#endif
#ifndef RCUI_STROKE
#define RCUI_STROKE 0
#endif
#ifndef RCUI_STROKE_MODE
#define RCUI_STROKE_MODE 0
#endif

in vec4 vertexColor;
in vec2 rcuiLocalUv;
out vec4 fragColor;

float roundedBoxDistance(vec2 point, vec2 halfSize, float radius) {
    vec2 corner = abs(point) - halfSize + radius;
    return length(max(corner, 0.0)) + min(max(corner.x, corner.y), 0.0) - radius;
}

void main() {
    vec2 size = vec2(1.0 / max(fwidth(rcuiLocalUv.x), 0.00001), 1.0 / max(fwidth(rcuiLocalUv.y), 0.00001));
    vec2 halfSize = max(size * 0.5, vec2(0.5));
    float radius = min(float(RCUI_RADIUS), min(halfSize.x, halfSize.y));
    float outerDistance = roundedBoxDistance(rcuiLocalUv * size - halfSize, halfSize, radius);
    float feather = max(fwidth(outerDistance), 0.75);
    float alpha = 1.0 - smoothstep(-feather, feather, outerDistance);
#if RCUI_STROKE_MODE == 1
    float stroke = min(float(RCUI_STROKE), min(halfSize.x, halfSize.y));
    vec2 innerSize = max(halfSize - vec2(stroke), vec2(0.0));
    alpha *= smoothstep(-feather, feather, roundedBoxDistance(rcuiLocalUv * size - halfSize, innerSize, max(radius - stroke, 0.0)));
#endif
    if (alpha * vertexColor.a <= 0.0) discard;
    fragColor = vec4(vertexColor.rgb, vertexColor.a * alpha);
}
