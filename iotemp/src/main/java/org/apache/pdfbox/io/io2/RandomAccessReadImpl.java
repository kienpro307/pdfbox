package org.apache.pdfbox.io2;

import java.io.IOException;

public class RandomAccessReadImpl implements RandomAccessRead {

    @Override
    public int read() throws IOException {
        return 0;
    }

    @Override
    public int read(byte[] b, int offset, int len) throws IOException {
        // Logic đọc nhiều byte với offset và length
        return len; // Ví dụ số byte đã đọc
    }

    @Override
    public long getPosition() throws IOException {
        return 0;
    }

    @Override
    public void seek(long position) throws IOException {

    }

    @Override
    public long length() throws IOException {
        return 0;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public int peek() throws IOException  {
        int result = read();
        if (result != -1)
        {
            rewind(1);
        }
        return result;
    }

    @Override
    public void rewind(int bytes) throws IOException    {
        seek(getPosition() - bytes);
    }

    @Override
    public boolean isEOF() throws IOException {
        return false;
    }

    @Override
    public int available() throws IOException   {
        return (int) Math.min(length() - getPosition(), Integer.MAX_VALUE);
    }

    @Override
    public void skip(int length) throws IOException {
        seek(getPosition() + length);
    }


    @Override
    public RandomAccessReadView createView(long startPosition, long streamLength) throws IOException {
        return null;
    }

    @Override
    public  int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public void close() throws IOException {
        // Logic đóng
    }
}