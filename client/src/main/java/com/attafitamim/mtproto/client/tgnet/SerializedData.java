/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package com.attafitamim.mtproto.client.tgnet;

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class SerializedData extends AbstractSerializedData {
    protected boolean isOut = true;
    private ByteArrayOutputStream outbuf;
    private DataOutputStream out;
    private ByteArrayInputStream inbuf;
    private DataInputStream in;
    private boolean justCalc = false;
    private int len;

    public SerializedData() {
        outbuf = new ByteArrayOutputStream();
        out = new DataOutputStream(outbuf);
    }

    public SerializedData(boolean calculate) {
        if (!calculate) {
            outbuf = new ByteArrayOutputStream();
            out = new DataOutputStream(outbuf);
        }
        justCalc = calculate;
        len = 0;
    }

    public SerializedData(int size) {
        outbuf = new ByteArrayOutputStream(size);
        out = new DataOutputStream(outbuf);
    }

    public SerializedData(byte[] data) {
        isOut = false;
        inbuf = new ByteArrayInputStream(data);
        in = new DataInputStream(inbuf);
        len = 0;
    }

    public void cleanup() {
        try {
            if (inbuf != null) {
                inbuf.close();
                inbuf = null;
            }
        } catch (Exception e) {
        }
        try {
            if (in != null) {
                in.close();
                in = null;
            }
        } catch (Exception e) {
        }
        try {
            if (outbuf != null) {
                outbuf.close();
                outbuf = null;
            }
        } catch (Exception e) {
        }
        try {
            if (out != null) {
                out.close();
                out = null;
            }
        } catch (Exception e) {
        }
    }

    public SerializedData(File file) throws Exception {
        FileInputStream is = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        new DataInputStream(is).readFully(data);
        is.close();

        isOut = false;
        inbuf = new ByteArrayInputStream(data);
        in = new DataInputStream(inbuf);
    }

    public void writeInt(int x) {
        if (!justCalc) {
            writeInt32(x, out);
        } else {
            len += 4;
        }
    }

    private void writeInt32(int x, DataOutputStream out) {
        try {
            for (int i = 0; i < 4; i++) {
                out.write(x >> (i * 8));
            }
        } catch (Exception ignored) {
        }
    }

    public void writeLong(long i) {
        if (!justCalc) {
            writeInt64(i, out);
        } else {
            len += 8;
        }
    }

    private void writeInt64(long x, DataOutputStream out) {
        try {
            for (int i = 0; i < 8; i++) {
                out.write((int) (x >> (i * 8)));
            }
        } catch (Exception e) {
        }
    }

    public void writeBoolean(boolean value) {
        if (!justCalc) {
            if (value) {
                writeInt(0x997275b5);
            } else {
                writeInt(0xbc799737);
            }
        } else {
            len += 4;
        }
    }

    public void writeBytes(byte[] b) {
        try {
            if (!justCalc) {
                out.write(b);
            } else {
                len += b.length;
            }
        } catch (Exception e) {
        }
    }

    public void writeBytes(byte[] b, int offset, int count) {
        try {
            if (!justCalc) {
                out.write(b, offset, count);
            } else {
                len += count;
            }
        } catch (Exception e) {
        }
    }

    public void writeByte(int i) {
        try {
            if (!justCalc) {
                out.writeByte((byte) i);
            } else {
                len += 1;
            }
        } catch (Exception e) {
        }
    }

    public void writeByte(byte b) {
        try {
            if (!justCalc) {
                out.writeByte(b);
            } else {
                len += 1;
            }
        } catch (Exception e) {
        }
    }

    public void writeByteArray(byte[] b) {
        try {
            if (b.length <= 253) {
                if (!justCalc) {
                    out.write(b.length);
                } else {
                    len += 1;
                }
            } else {
                if (!justCalc) {
                    out.write(254);
                    out.write(b.length);
                    out.write(b.length >> 8);
                    out.write(b.length >> 16);
                } else {
                    len += 4;
                }
            }
            if (!justCalc) {
                out.write(b);
            } else {
                len += b.length;
            }
            int i = b.length <= 253 ? 1 : 4;
            while ((b.length + i) % 4 != 0) {
                if (!justCalc) {
                    out.write(0);
                } else {
                    len += 1;
                }
                i++;
            }
        } catch (Exception e) {
        }
    }

    public void writeString(String s) {
        try {
            writeByteArray(s.getBytes("UTF-8"));
        } catch (Exception e) {
        }
    }

    public void writeByteArray(byte[] b, int offset, int count) {
        try {
            if (count <= 253) {
                if (!justCalc) {
                    out.write(count);
                } else {
                    len += 1;
                }
            } else {
                if (!justCalc) {
                    out.write(254);
                    out.write(count);
                    out.write(count >> 8);
                    out.write(count >> 16);
                } else {
                    len += 4;
                }
            }
            if (!justCalc) {
                out.write(b, offset, count);
            } else {
                len += count;
            }
            int i = count <= 253 ? 1 : 4;
            while ((count + i) % 4 != 0) {
                if (!justCalc) {
                    out.write(0);
                } else {
                    len += 1;
                }
                i++;
            }
        } catch (Exception e) {
        }
    }

    public void writeDouble(double d) {
        try {
            writeLong(Double.doubleToRawLongBits(d));
        } catch (Exception e) {
        }
    }

    public int length() {
        if (!justCalc) {
            return isOut ? outbuf.size() : inbuf.available();
        }
        return len;
    }

    protected void set(byte[] newData) {
        isOut = false;
        inbuf = new ByteArrayInputStream(newData);
        in = new DataInputStream(inbuf);
    }

    public byte[] toByteArray() {
        return outbuf.toByteArray();
    }

    public void skip(int count) {
        if (count == 0) {
            return;
        }
        if (!justCalc) {
            if (in != null) {
                try {
                    in.skipBytes(count);
                } catch (Exception e) {
                }
            }
        } else {
            len += count;
        }
    }

    public int getPosition() {
        return len;
    }

    public boolean readBoolean(boolean exception) {
        int consructor = readInt(exception);
        if (consructor == 0x997275b5) {
            return true;
        } else if (consructor == 0xbc799737) {
            return false;
        }
        if (exception) {
            throw new RuntimeException("Not bool value!");
        }
        return false;
    }

    public void readBytes(byte[] b, boolean exception) {
        try {
            in.read(b);
            len += b.length;
        } catch (Exception e) {
            if (exception) {
                throw new RuntimeException("read bytes error", e);
            }
        }
    }

    public byte[] readData(int count, boolean exception) {
        byte[] arr = new byte[count];
        readBytes(arr, exception);
        return arr;
    }

    public String readString(boolean exception) {
        try {
            int sl = 1;
            int l = in.read();
            len++;
            if (l >= 254) {
                l = in.read() | (in.read() << 8) | (in.read() << 16);
                len += 3;
                sl = 4;
            }
            byte[] b = new byte[l];
            in.read(b);
            len++;
            int i = sl;
            while ((l + i) % 4 != 0) {
                in.read();
                len++;
                i++;
            }
            return new String(b, "UTF-8");
        } catch (Exception e) {
            if (exception) {
                throw new RuntimeException("read string error", e);
            }
        }
        return null;
    }

    public byte[] readByteArray(boolean exception) {
        try {
            int sl = 1;
            int l = in.read();
            len++;
            if (l >= 254) {
                l = in.read() | (in.read() << 8) | (in.read() << 16);
                len += 3;
                sl = 4;
            }
            byte[] b = new byte[l];
            in.read(b);
            len++;
            int i = sl;
            while ((l + i) % 4 != 0) {
                in.read();
                len++;
                i++;
            }
            return b;
        } catch (Exception e) {
            if (exception) {
                throw new RuntimeException("read byte array error", e);
            }
        }
        return null;
    }

    public double readDouble(boolean exception) {
        try {
            return Double.longBitsToDouble(readLong(exception));
        } catch (Exception e) {
            if (exception) {
                throw new RuntimeException("read double error", e);
            }
        }
        return 0;
    }

    public int readInt(boolean exception) {
        try {
            int i = 0;
            for (int j = 0; j < 4; j++) {
                i |= (in.read() << (j * 8));
                len++;
            }
            return i;
        } catch (Exception e) {
            if (exception) {
                throw new RuntimeException("read int32 error", e);
            }
        }
        return 0;
    }

    public long readLong(boolean exception) {
        try {
            long i = 0;
            for (int j = 0; j < 8; j++) {
                i |= ((long) in.read() << (j * 8));
                len++;
            }
            return i;
        } catch (Exception e) {
            if (exception) {
                throw new RuntimeException("read int64 error", e);
            }
        }
        return 0;
    }

    @Override
    public int remaining() {
        try {
            return in.available();
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean readBoolean() {
        return false;
    }

    @Override
    public byte readByte() {
        return 0;
    }

    @NotNull
    @Override
    public byte[] readByteArray() {
        return new byte[0];
    }

    @NotNull
    @Override
    public byte[] readBytes(int i) {
        return new byte[0];
    }

    @Override
    public double readDouble() {
        return 0;
    }

    @NotNull
    @Override
    public TLInputStream readInputStream() {
        return null;
    }

    @Override
    public int readInt() {
        return 0;
    }

    @Override
    public long readLong() {
        return 0;
    }

    @NotNull
    @Override
    public String readString() {
        return null;
    }

    @Override
    public void writeInputStream(@NotNull TLInputStream tlInputStream) {

    }
}
