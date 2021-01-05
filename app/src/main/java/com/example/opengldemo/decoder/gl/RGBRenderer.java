package com.example.opengldemo.decoder.gl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.opengldemo.util.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * RGBRenderer
 * 生成RGB纹理方式渲染预览画面
 */
public class RGBRenderer implements GLSurfaceView.Renderer {

    // Videp bitmap mode
    private static final int VIDEO_BITMAP_DstInSrc = 0;
    private static final int VIDEO_BITMAP_SrcInDst = 1;
    private static final int VIDEO_BITMAP_SrcFitDst = 2;

    // Board member.
    private int m_iWndWidth = 0;
    private int m_iWndHeight = 0;

    private int m_iTexture = -1;

    private Object m_sDraw = new Object();
    private ByteBuffer m_byBuf = null;
    private int m_iDrawWidth = 0;
    private int m_iDrawHeight = 0;
    private int m_iDrawFillMode = VIDEO_BITMAP_DstInSrc;

    private String m_sVertexShader = "";
    private String m_sFragmentShader = "";

    private int mProgram;
    private int glHPosition;
    private int glHTexture;
    private int glHCoordinate;
    private int glHMatrix;
    private FloatBuffer bPos;
    private FloatBuffer bCoord;
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private final float[] sPos = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
    };

    private final float[] sCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    public RGBRenderer() {
        super();

        m_sVertexShader = "attribute vec4 vPosition;\n"
                + "attribute vec2 vCoordinate;\n"
                + "uniform mat4 vMatrix;\n"
                + "varying vec2 aCoordinate;\n"
                + "void main(){\n"
                + "    gl_Position=vMatrix*vPosition;\n"
                + "    aCoordinate=vCoordinate;\n"
                + "}";

        m_sFragmentShader = "precision mediump float;\n"
                + "uniform sampler2D vTexture;\n"
                + "varying vec2 aCoordinate;\n"
                + "void main(){\n"
                + "    vec4 rgba=texture2D(vTexture,aCoordinate);\n"
                + "    gl_FragColor=vec4(rgba.b,rgba.g,rgba.r,rgba.a);\n"
                + "}";

        ByteBuffer bb = ByteBuffer.allocateDirect(sPos.length * 4);
        bb.order(ByteOrder.nativeOrder());
        bPos = bb.asFloatBuffer();
        bPos.put(sPos);
        bPos.position(0);

        ByteBuffer cc = ByteBuffer.allocateDirect(sCoord.length * 4);
        cc.order(ByteOrder.nativeOrder());
        bCoord = cc.asFloatBuffer();
        bCoord.put(sCoord);
        bCoord.position(0);

        Log.d("DevExtend", "MyRenderer.MyRenderer");
    }

    volatile boolean isTextureChanged = false;

    public void DrawBitmap(byte[] byData, int iWidth, int iHeight, int iFillMode) {
        try {
            synchronized (m_sDraw) {
                m_byBuf = ByteBuffer.wrap(byData);
                m_iDrawWidth = iWidth;
                m_iDrawHeight = iHeight;
                m_iDrawFillMode = iFillMode;

                isTextureChanged = true;
            }
        } catch (Exception ex) {
            Log.d("DevExtend", "MyRenderer.DrawBitmap, ex=" + ex.toString());
        }
    }

    public void DrawClean() {
        try {
            synchronized (m_sDraw) {
                m_byBuf = null;
            }
        } catch (Exception ex) {
            Log.d("DevExtend", "MyRenderer.DrawClean, ex=" + ex.toString());
        }
    }

    @Override
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {

        long start = System.currentTimeMillis();
        try {
            //synchronized(m_sDraw) {

            int iErr;
            if (m_byBuf != null) {

                float sWH = (float) m_iDrawWidth / (float) m_iDrawHeight;
                float sWidthHeight = m_iWndWidth / (float) m_iWndHeight;

                if (m_iDrawFillMode == VIDEO_BITMAP_DstInSrc) {
                    if (sWH > sWidthHeight) {
                        Matrix.orthoM(mProjectMatrix, 0, -(sWidthHeight / sWH), (sWidthHeight / sWH), -1, 1, 3, 5);
                    } else {
                        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -(sWH / sWidthHeight), (sWH / sWidthHeight), 3, 5);
                    }
                } else if (m_iDrawFillMode == VIDEO_BITMAP_SrcInDst) {
                    if (sWH > sWidthHeight) {
                        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -(sWH / sWidthHeight), (sWH / sWidthHeight), 3, 5);
                    } else {
                        Matrix.orthoM(mProjectMatrix, 0, -(sWidthHeight / sWH), (sWidthHeight / sWH), -1, 1, 3, 5);
                    }
                } else {
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1, 1, 3, 5);
                }

                Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
                Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                GLES20.glUseProgram(mProgram);
                GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0);
                GLES20.glEnableVertexAttribArray(glHPosition);
                GLES20.glEnableVertexAttribArray(glHCoordinate);
                GLES20.glUniform1i(glHTexture, 0);

                if (isTextureChanged) {
                    synchronized (m_sDraw) {
                        createTexture();
                    }
                    isTextureChanged = false;
                }

                GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos);
                GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            } else {
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                if ((iErr = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                    Log.d("DevExtend", "MyRenderer.onDrawFrame: glClearColor, iErr=" + iErr);
                }

                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

                Log.d("DevExtend", "MyRenderer.onDrawFrame clear");
            }
            //}
        } catch (Exception ex) {
            Log.d("DevExtend", "MyRenderer.onDrawFrame, ex=" + ex.toString());
        }

        Log.d("zsj", "onDrawFrame: time=" + (System.currentTimeMillis() - start));
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
        Log.d("DevExtend", "MyRenderer.onSurfaceChanged");

        m_iWndWidth = width;
        m_iWndHeight = height;

        int iErr;

        GLES20.glViewport(0, 0, width, height);
        if ((iErr = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.d("DevExtend", "MyRenderer.onSurfaceChanged: glViewport, iErr=" + iErr);
        }
    }

    @Override
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 arg0,
                                 javax.microedition.khronos.egl.EGLConfig arg1) {
        // TODO Auto-generated method stub
        Log.d("DevExtend", "MyRenderer.onSurfaceCreated");


        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        mProgram = ShaderUtils.createProgram(m_sVertexShader, m_sFragmentShader);
        glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
        glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
        glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
    }

    private void createTexture() {
        int iErr;

        if (m_iTexture < 0) {
            int iTexture[] = new int[1];
            GLES20.glGenTextures(1, iTexture, 0);
            if ((iErr = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.d("DevExtend", "MyRenderer.onDrawFrame: glGenTextures, iErr=" + iErr);
            }
            m_iTexture = iTexture[0];
            Log.d("DevExtend", "MyRenderer.onDrawFrame, m_iTexture=" + m_iTexture);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_iTexture);
        if ((iErr = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.d("DevExtend", "MyRenderer.onDrawFrame: glBindTexture, iErr=" + iErr);
        }

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        if ((iErr = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.d("DevExtend", "MyRenderer.onDrawFrame: glTexParameteri, iErr=" + iErr);
        }

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, m_iDrawWidth,
                m_iDrawHeight, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, m_byBuf);
        if ((iErr = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.d("DevExtend", "MyRenderer.onDrawFrame: glTexImage2D, iErr=" + iErr);
        }
    }
}