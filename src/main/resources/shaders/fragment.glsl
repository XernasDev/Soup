#version 450

out vec4 fragColor;

uniform bool isAlive;

void main() {
    if (isAlive) {
        fragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
    else {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}