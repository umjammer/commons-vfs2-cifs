package com.github.vbauer.vfs2.provider.smb;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractRandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;


/**
 * RandomAccess for smb files.
 *
 * @author Vladislav Bauer
 */
public class SmbFileRandomAccessContent extends AbstractRandomAccessContent {

    public static final char MODE_READ = 'r';
    public static final char MODE_WRITE = 'w';

    private static final String ERROR_OPEN_FAILED = "vfs.provider/random-access-open-failed.error";

    private final SmbRandomAccessFile raf;
    private final InputStream rafis;

    public SmbFileRandomAccessContent(SmbFile smbFile, RandomAccessMode mode) throws FileSystemException {
        super(mode);

        StringBuilder modes = new StringBuilder(2);
        if (mode.requestRead()) {
            modes.append(MODE_READ);
        }
        if (mode.requestWrite()) {
            modes.append(MODE_WRITE);
        }

        try {
            raf = new SmbRandomAccessFile(smbFile, modes.toString());
            rafis = new SmbFileInputStream(raf);
        } catch (SmbException ex) {
            throw new FileSystemException(ERROR_OPEN_FAILED, smbFile, ex);
        }
    }

    @Override
    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }

    @Override
    public void seek(long pos) throws IOException {
        raf.seek(pos);
    }

    @Override
    public void setLength(long newLength) throws IOException {
        raf.setLength(newLength);
    }

    @Override
    public long length() throws IOException {
        return raf.length();
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    @Override
    public byte readByte() throws IOException {
        return raf.readByte();
    }

    @Override
    public char readChar() throws IOException {
        return raf.readChar();
    }

    @Override
    public double readDouble() throws IOException {
        return raf.readDouble();
    }

    @Override
    public float readFloat() throws IOException {
        return raf.readFloat();
    }

    @Override
    public int readInt() throws IOException {
        return raf.readInt();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return raf.readUnsignedByte();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return raf.readUnsignedShort();
    }

    @Override
    public long readLong() throws IOException {
        return raf.readLong();
    }

    @Override
    public short readShort() throws IOException {
        return raf.readShort();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return raf.readBoolean();
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return raf.skipBytes(n);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        raf.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        raf.readFully(b, off, len);
    }

    @Override
    public String readUTF() throws IOException {
        return raf.readUTF();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return rafis;
    }

    @Override
    public void writeDouble(double v) throws IOException {
        raf.writeDouble(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        raf.writeFloat(v);
    }

    @Override
    public void write(int b) throws IOException {
        raf.write(b);
    }

    @Override
    public void writeByte(int v) throws IOException {
        raf.writeByte(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        raf.writeChar(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        raf.writeInt(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        raf.writeShort(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        raf.writeLong(v);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        raf.writeBoolean(v);
    }

    @Override
    public void write(byte[] b) throws IOException {
        raf.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        raf.write(b, off, len);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        raf.writeBytes(s);
    }

    @Override
    public void writeChars(String s) throws IOException {
        raf.writeChars(s);
    }

    @Override
    public void writeUTF(String str) throws IOException {
        raf.writeUTF(str);
    }
}
