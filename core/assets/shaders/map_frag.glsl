#version 120
#extension GL_EXT_texture_array : enable
#ifdef GL_ES
precision mediump float;
#endif
varying vec3 v_texCoords;
varying vec4 v_color;
uniform sampler2DArray u_texture;
void main()                                  
{                                            
  gl_FragColor = texture2DArray(u_texture, v_texCoords);
}
