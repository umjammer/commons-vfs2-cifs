package com.github.vbauer.vfs2.provider.smb;

import jcifs.smb.SmbRandomAccessFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Vladislav Bauer
 */
public class SmbFileInputStream extends InputStream {

    private final SmbRandomAccessFile raf;

    public SmbFileInputStream(SmbRandomAccessFile raf) {
        this.raf = raf;
    }

    @Override
    public int read() throws IOException {
        return raf.readByte() & 0xFF;
    }

    @Override
    public long skip(long n) throws IOException {
        raf.seek(raf.getFilePointer() + n);
        return n;
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return raf.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return raf.read(b, off, len);
    }

    @Override
    public int available() throws IOException {
        long available = raf.length() - raf.getFilePointer();
        return (int) Math.min(available, Integer.MAX_VALUE);
    }
}
