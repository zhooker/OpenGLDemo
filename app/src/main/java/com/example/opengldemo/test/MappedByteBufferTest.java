package com.example.opengldemo.test;

import com.example.opengldemo.util.L;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;

/**
 * Created by zhuangsj on 18-2-1.
 */

public class MappedByteBufferTest {
    public static void map() {
        File file = new File("/storage/emulated/0/238703-3.zip");
        long len = file.length() / 2000;
        L.d("MappedByteBufferTest len = " + len + " / " + (len/1024/1024));

        try {
            byte[] ds = new byte[1024];
            MappedByteBuffer mappedByteBuffer = new RandomAccessFile(file, "r")
                    .getChannel()
                    .map(FileChannel.MapMode.READ_ONLY, 0, 1*1024*1024*1024);

            mappedByteBuffer.position(1*1024*1024);
            for (int offset = 0; offset < ds.length;  offset++) {
                byte b = mappedByteBuffer.get();
                ds[offset] = b;
            }

            Scanner scan = new Scanner(new ByteArrayInputStream(ds)).useDelimiter(" ");
            while (scan.hasNext()) {
                L.d(scan.next() + " ");
            }

        } catch (IOException e) {
            L.d("IOException  " + e.getMessage());
        } catch (OutOfMemoryError error) {
            L.d("OutOfMemoryError  " + error.getMessage());
        }
    }

    public static void read() {
        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile("/storage/emulated/0/238703-3.zip", "rw");
            rf.seek(5 * 8);
            for (int i = 0; i < 10; i++) {
                L.d("Value " + i + ": " + rf.readChar());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rf != null) {
                try {
                    rf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void test() {
        try {
            MappedByteBufferTest();
            BufferTest();
            BufferedInputStreamTest();
        } catch (Exception e) {
            e.printStackTrace();
            L.d("test error " + e.getMessage());
        }
    }

    // 1、使用MappedByteBuffer: 0.7s
    public static void MappedByteBufferTest() throws Exception {
        String srcFile = "/storage/emulated/0/zzz.zip";
        String destFile = "/storage/emulated/0/MappedByteBufferTest.file";

        File f = new File(destFile);
        if (!f.exists())
            f.createNewFile();

        RandomAccessFile rafi = new RandomAccessFile(srcFile, "r");
        RandomAccessFile rafo = new RandomAccessFile(destFile, "rw");
        FileChannel fci = rafi.getChannel();
        FileChannel fco = rafo.getChannel();
        long size = fci.size();
        byte b;
        long start = System.currentTimeMillis();
        MappedByteBuffer mbbi = fci.map(FileChannel.MapMode.READ_ONLY, 0, size);
        L.d("MappedByteBufferTest output: " + (double) (System.currentTimeMillis() - start) / 1000 + "s");
        MappedByteBuffer mbbo = fco.map(FileChannel.MapMode.READ_WRITE, 0, size);
        start = System.currentTimeMillis();

        byte[] buf = new byte[1*1024];
        for (int i = 0; i < size-1024; i+=1024) {
            mbbi.get(buf);
            mbbo.position(i);
            mbbo.put(buf);
        }

        fci.close();
        fco.close();
        rafi.close();
        rafo.close();
        L.d("MappedByteBufferTest input: " + (double) (System.currentTimeMillis() - start) / 1000 + "s");
    }


    // 2、自己处理Buffer(RandomAccessFile): 0.13s
    public static void BufferTest() throws Exception{
        String srcFile = "/storage/emulated/0/zzz.zip";
        String destFile = "/storage/emulated/0/BufferTest.file";

        File f = new File(destFile);
        if (!f.exists())
            f.createNewFile();

        RandomAccessFile rafi = new RandomAccessFile(srcFile, "r");
        RandomAccessFile rafo = new RandomAccessFile(destFile, "rw");

        byte[] buf = new byte[4*1024];
        long start = System.currentTimeMillis();
        int c = rafi.read(buf);
        L.d("RandomAccessFile output: " + (double) (System.currentTimeMillis() - start) / 1000 + "s");
        start = System.currentTimeMillis();
        while (c > 0) {
            if (c == buf.length) {
                rafo.write(buf);
            } else {
                rafo.write(buf, 0, c);
            }

            c = rafi.read(buf);
        }
        L.d("RandomAccessFile input: " + (double) (System.currentTimeMillis() - start) / 1000 + "s");
        rafi.close();
        rafo.close();

    }

    // 3、BufferedInputStream&BufferedOutputStream: 3.02s
    public static void BufferedInputStreamTest() throws Exception{
        String srcFile = "/storage/emulated/0/zzz.zip";
        String destFile = "/storage/emulated/0/StreamTest2.file";

        File f = new File(destFile);
        if (!f.exists())
            f.createNewFile();


        FileInputStream rafi = new FileInputStream(srcFile);
        FileOutputStream rafo = new FileOutputStream(destFile);

        BufferedInputStream bis = new BufferedInputStream(rafi, 4096);
        BufferedOutputStream bos = new BufferedOutputStream(rafo, 4096);
        long size = rafi.available();

        long start = System.currentTimeMillis();

        for (int i = 0; i < size; i++) {
            byte b = (byte) bis.read();
            bos.write(b);
        }
        rafi.close();
        rafo.close();
        L.d("InputStream time: " + (double) (System.currentTimeMillis() - start) / 1000 + "s");

    }
}
