package com.pos.olympia.btsdk;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.support.v4.view.ViewCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@SuppressLint({"SdCardPath"})
public class PrintPic {
    public byte[] bitbuf = null;

    /* renamed from: bm */
    public Bitmap f7bm = null;
    public Canvas canvas = null;
    public float length = 0.0f;
    public Paint paint = null;
    public int width;

    public int getLength() {
        return ((int) this.length) + 20;
    }

    public void initCanvas(int w) {
        this.f7bm = Bitmap.createBitmap(w, w * 10, Config.ARGB_4444);
        this.canvas = new Canvas(this.f7bm);
        this.canvas.drawColor(-1);
        this.width = w;
        this.bitbuf = new byte[(this.width / 8)];
    }

    public void initPaint() {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.paint.setStyle(Style.STROKE);
    }

    public void drawImage(float x, float y, String path) {
        try {
            Bitmap btm = BitmapFactory.decodeFile(path);
            this.canvas.drawBitmap(btm, x, y, null);
            if (this.length < ((float) btm.getHeight()) + y) {
                this.length = ((float) btm.getHeight()) + y;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printPng() {
        File f = new File("/mnt/sdcard/0.png");
        Bitmap nbm = Bitmap.createBitmap(this.f7bm, 0, 0, this.width, getLength());
        try {
            FileOutputStream fos = new FileOutputStream(f);
            nbm.compress(CompressFormat.PNG, 50, fos);
            FileOutputStream fileOutputStream = fos;
        } catch (FileNotFoundException e2) {

            e2.printStackTrace();
        }
    }

    public byte[] printDraw() {
        int p0;
        int p1;
        int p2;
        int p3;
        int p4;
        int p5;
        int p6;
        int p7;
        Bitmap nbm = Bitmap.createBitmap(this.f7bm, 0, 0, this.width, getLength());
        byte[] imgbuf = new byte[(((this.width / 8) * getLength()) + 8)];
        imgbuf[0] = 29;
        imgbuf[1] = 118;
        imgbuf[2] = 48;
        imgbuf[3] = 0;
        imgbuf[4] = (byte) (this.width / 8);
        imgbuf[5] = 0;
        imgbuf[6] = (byte) (getLength() % 256);
        imgbuf[7] = (byte) (getLength() / 256);
        int s = 7;
        for (int i = 0; i < getLength(); i++) {
            for (int k = 0; k < this.width / 8; k++) {
                if (nbm.getPixel((k * 8) + 0, i) == -1) {
                    p0 = 0;
                } else {
                    p0 = 1;
                }
                if (nbm.getPixel((k * 8) + 1, i) == -1) {
                    p1 = 0;
                } else {
                    p1 = 1;
                }
                if (nbm.getPixel((k * 8) + 2, i) == -1) {
                    p2 = 0;
                } else {
                    p2 = 1;
                }
                if (nbm.getPixel((k * 8) + 3, i) == -1) {
                    p3 = 0;
                } else {
                    p3 = 1;
                }
                if (nbm.getPixel((k * 8) + 4, i) == -1) {
                    p4 = 0;
                } else {
                    p4 = 1;
                }
                if (nbm.getPixel((k * 8) + 5, i) == -1) {
                    p5 = 0;
                } else {
                    p5 = 1;
                }
                if (nbm.getPixel((k * 8) + 6, i) == -1) {
                    p6 = 0;
                } else {
                    p6 = 1;
                }
                if (nbm.getPixel((k * 8) + 7, i) == -1) {
                    p7 = 0;
                } else {
                    p7 = 1;
                }
                this.bitbuf[k] = (byte) ((p0 * 128) + (p1 * 64) + (p2 * 32) + (p3 * 16) + (p4 * 8) + (p5 * 4) + (p6 * 2) + p7);
            }
            for (int t = 0; t < this.width / 8; t++) {
                s++;
                imgbuf[s] = this.bitbuf[t];
            }
        }
        return imgbuf;
    }
}
