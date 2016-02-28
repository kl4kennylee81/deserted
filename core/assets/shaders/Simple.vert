uniform mat4 unWorld;
uniform mat4 unVP;

attribute vec4 vPosition;
attribute vec2 vUV;

varying vec2 fUV;

void main() {
	fUV = vUV;
	
	vec4 worldPos = unWorld * vPosition;
	gl_Position = unVP * worldPos;
}
