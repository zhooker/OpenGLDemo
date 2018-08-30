package com.example.opengldemo.solar.system;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author zhuangsj
 * @created 2018/8/30
 */
public class Planet {
    FloatBuffer m_VertexData;
    FloatBuffer m_NormalData;
    FloatBuffer m_TextureData;

    float m_Scale;
    float m_Squash;
    float m_Radius;
    int m_Stacks, m_Slices;


    public Planet(int stacks, int slices, float radius, float squash) {
        this.m_Stacks = stacks;
        this.m_Slices = slices;
        this.m_Radius = radius;
        this.m_Squash = squash;
        init(m_Stacks, m_Slices, radius, squash);
    }

    private void init(int stacks, int slices, float radius, float squash)        // 1
    {
        float[] vertexData;
        float[] normalData;
        float[] textData = null;

        int vIndex = 0;                //vertex index
        int nIndex = 0;                //normal index
        int tIndex = 0;                //texture index

        m_Scale = radius;
        m_Squash = squash;

        m_Stacks = stacks;
        m_Slices = slices;

        //Vertices

        vertexData = new float[3 * ((m_Slices * 2 + 2) * m_Stacks)];

        //Normal pointers for lighting

        normalData = new float[3 * ((m_Slices * 2 + 2) * m_Stacks)];

        textData = new float[2 * ((m_Slices * 2 + 2) * (m_Stacks))];

        int phiIdx, thetaIdx;

        //Latitude

        for (phiIdx = 0; phiIdx < m_Stacks; phiIdx++) {
            //Starts at -1.57 and goes up to +1.57 radians.
            ///The first circle.
            float phi0 = (float) Math.PI * ((float) (phiIdx + 0) * (1.0f / (float) (m_Stacks)) - 0.5f);

            //The next, or second one.
            float phi1 = (float) Math.PI * ((float) (phiIdx + 1) * (1.0f / (float) (m_Stacks)) - 0.5f);

            float cosPhi0 = (float) Math.cos(phi0);
            float sinPhi0 = (float) Math.sin(phi0);
            float cosPhi1 = (float) Math.cos(phi1);
            float sinPhi1 = (float) Math.sin(phi1);

            float cosTheta, sinTheta;

            //Longitude
            for (thetaIdx = 0; thetaIdx < m_Slices; thetaIdx++) {
                //Increment along the longitude circle each "slice."
                float theta = (float) (2.0f * (float) Math.PI * ((float) thetaIdx) * (1.0 / (float) (m_Slices - 1)));
                cosTheta = (float) Math.cos(theta);
                sinTheta = (float) Math.sin(theta);

                //We're generating a vertical pair of points, such
                //as the first point of stack 0 and the first point of
                //stack 1 above it. This is how TRIANGLE_STRIPS work,
                //taking a set of 4 vertices and essentially drawing two
                //triangles at a time. The first is v0-v1-v2, and the next
                //is v2-v1-v3, etc.

                //Get x-y-z for the first vertex of stack.

                vertexData[vIndex] = m_Scale * cosPhi0 * cosTheta;
                vertexData[vIndex + 1] = m_Scale * (sinPhi0 * m_Squash);
                vertexData[vIndex + 2] = m_Scale * (cosPhi0 * sinTheta);

                vertexData[vIndex + 3] = m_Scale * cosPhi1 * cosTheta;
                vertexData[vIndex + 4] = m_Scale * (sinPhi1 * m_Squash);
                vertexData[vIndex + 5] = m_Scale * (cosPhi1 * sinTheta);

                //Normal pointers for lighting

                normalData[nIndex + 0] = (float) (cosPhi0 * cosTheta);
                normalData[nIndex + 2] = cosPhi0 * sinTheta;
                normalData[nIndex + 1] = sinPhi0;

                //Get x-y-z for the first vertex of stack N.

                normalData[nIndex + 3] = cosPhi1 * cosTheta;
                normalData[nIndex + 5] = cosPhi1 * sinTheta;
                normalData[nIndex + 4] = sinPhi1;

                if (textData != null) {        //4
                    float texX = (float) thetaIdx * (1.0f / (float) (m_Slices - 1));
                    textData[tIndex + 0] = texX;
                    textData[tIndex + 1] = (float) (phiIdx + 0) * (1.0f / (float) (m_Stacks));
                    textData[tIndex + 2] = texX;
                    textData[tIndex + 3] = (float) (phiIdx + 1) * (1.0f / (float) (m_Stacks));
                }

                vIndex += 2 * 3;
                nIndex += 2 * 3;

                if (textData != null)            //5
                    tIndex += 2 * 2;

                //Degenerate triangle to connect stacks and maintain
                //winding order.

                vertexData[vIndex + 0] = vertexData[vIndex + 3] = vertexData[vIndex - 3];
                vertexData[vIndex + 1] = vertexData[vIndex + 4] = vertexData[vIndex - 2];
                vertexData[vIndex + 2] = vertexData[vIndex + 5] = vertexData[vIndex - 1];

                normalData[nIndex + 0] = normalData[nIndex + 3] = normalData[nIndex - 3];
                normalData[nIndex + 1] = normalData[nIndex + 4] = normalData[nIndex - 2];
                normalData[nIndex + 2] = normalData[nIndex + 5] = normalData[nIndex - 1];


                if (textData != null) {            //6
                    textData[tIndex + 0] = textData[tIndex + 2] = textData[tIndex - 2];
                    textData[tIndex + 1] = textData[tIndex + 3] = textData[tIndex - 1];
                }
            }
        }

        m_VertexData = makeFloatBuffer(vertexData);
        m_NormalData = makeFloatBuffer(normalData);

        if (textData != null)
            m_TextureData = makeFloatBuffer(textData);
    }

    protected static FloatBuffer makeFloatBuffer(float[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    public FloatBuffer getM_VertexData() {
        return m_VertexData;
    }

    public FloatBuffer getM_NormalData() {
        return m_NormalData;
    }

    public FloatBuffer getM_TextureData() {
        return m_TextureData;
    }
}
