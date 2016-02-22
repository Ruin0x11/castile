uniform mat4 u_mvpMatrix;                   
attribute vec4 a_position;                  
attribute vec3 a_texcoords;
attribute vec4 a_color;
varying vec3 v_texCoords;
varying vec4 v_color;
void main()                                 
{                                           
   v_texCoords = a_texcoords;
   v_color = a_color;
   gl_Position = u_mvpMatrix * a_position;  
}
