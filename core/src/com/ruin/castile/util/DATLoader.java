package com.ruin.castile.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.common.io.LittleEndianDataInputStream;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Prin on 2016/02/21.
 */
public class DATLoader {
    public static void load(String filename) {
        FileHandle file = Gdx.files.internal(filename);

        try {
            LittleEndianDataInputStream in = new LittleEndianDataInputStream(new BufferedInputStream(
                    file.read()));

            int numTextures = in.readUnsignedByte();
            in.skipBytes(5);
            int numObjects = in.readUnsignedByte();
            in.skipBytes(9);
            String[] texNames = new String[numTextures];

            for(int i = 0; i < numTextures; i++) {
                byte[] buf = new byte[16];
                in.read(buf);
                texNames[i] = new String(buf, 0, buf.length, "ASCII").trim();
            }
            for(int i = 0; i < numTextures; i++) {
                int len = in.readInt();
                byte[] buf = new byte[len];
                in.skipBytes(12);
                in.read(buf);
                FileHandle out = Gdx.files.local(texNames[i]);
                System.out.println(out.path());
                out.writeBytes(buf, false);
                System.out.println("Wrote " + texNames[i]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
