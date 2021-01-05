package com.example.opengldemo.util;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderUtils {
    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.d("DevExtend", "ShaderUtil.loadShader: Could not compile shader" + shaderType + ":");
                Log.d("DevExtend", "ShaderUtil.loadShader: " + GLES20.glGetShaderInfoLog(shader));
                shader = 0;
            }
        } else {
            Log.d("DevExtend", "ShaderUtil.loadShader: glCreateShader failed");
        }
        return shader;
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            Log.d("DevExtend", "ShaderUtil.createProgram: loadShader ��GL_VERTEX_SHADER�� failed");
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            Log.d("DevExtend", "ShaderUtil.createProgram: loadShader ��GL_FRAGMENT_SHADER�� failed");
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");

            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");

            GLES20.glLinkProgram(program);

            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.d("DevExtend", "ShaderUtil.createProgram: could not link program::");
                Log.d("DevExtend", "ShaderUtil.createProgram: " + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        } else {
            Log.d("DevExtend", "ShaderUtil.createProgram: glCreateProgram failed");
        }

        return program;
    }

    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.d("DevExtend", op + ":  glError" + error);
        }
    }
}
