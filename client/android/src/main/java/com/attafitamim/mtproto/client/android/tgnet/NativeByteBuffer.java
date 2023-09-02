package com.attafitamim.mtproto.client.android.tgnet;

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NativeByteBuffer extends AbstractSerializedData {

    protected long address;
    public ByteBuffer buffer;
    private boolean justCalc;
    private int len;
    public boolean reused = true;

    private static final ThreadLocal<NativeByteBuffer> addressWrapper = new ThreadLocal<NativeByteBuffer>() {
        @Override
        protected NativeByteBuffer initialValue() {
            return new NativeByteBuffer(0, true);
        }
    };

    public static NativeByteBuffer wrap(long address) {
        NativeByteBuffer result = addressWrapper.get();
        if (address != 0) {
            result.address = address;
            result.reused = false;
            result.buffer = native_getJavaByteBuffer(address);
            result.buffer.limit(native_limit(address));
            int position = native_position(address);
            if (position <= result.buffer.limit()) {
                result.buffer.position(position);
            }
            result.buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
//          byte [] byteTemp = result.buffer.array();
//			String strByte = "";
//			for(byte b:byteTemp)
//				strByte+=String.valueOf(b) + " ";
//			Log.e("!!!!!", "Address" + String.valueOf(address) + "bytes: " + strByte);
        return result;
    }

    private NativeByteBuffer(int address, boolean wrap) {

    }

    public NativeByteBuffer(int size) throws Exception {
        if (size >= 0) {
            address = native_getFreeBuffer(size);
            if (address != 0) {
                buffer = native_getJavaByteBuffer(address);
                buffer.position(0);
                buffer.limit(size);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            }
        } else {
            throw new Exception("invalid NativeByteBuffer size");
        }
    }

    public NativeByteBuffer(boolean calculate) {
        justCalc = calculate;
    }

    public int position() {
        return buffer.position();
    }

    public void position(int position) {
        buffer.position(position);
    }

    public int capacity() {
        return buffer.capacity();
    }

    public int limit() {
        return buffer.limit();
    }

    public void limit(int limit) {
        buffer.limit(limit);
    }

    public void put(ByteBuffer buff) {
        buffer.put(buff);
    }

    public void rewind() {
        if (justCalc) {
            len = 0;
        } else {
            buffer.rewind();
        }
    }

    public void compact() {
        buffer.compact();
    }

    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    @Override
    public void writeInt(int x) {
        try {
            if (!justCalc) {
                buffer.putInt(x);
            } else {
                len += 4;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void writeLong(long x) {
        try {
            if (!justCalc) {
                buffer.putLong(x);
            } else {
                len += 8;
            }
        } catch (Exception e) {
        }
    }

    @Override
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
                buffer.put(b);
            } else {
                len += b.length;
            }
        } catch (Exception e) {
        }
    }

    public void writeBytes(byte[] b, int offset, int count) {
        try {
            if (!justCalc) {
                buffer.put(b, offset, count);
            } else {
                len += count;
            }
        } catch (Exception e) {
        }
    }

    public void writeByte(int i) {
        writeByte((byte) i);
    }

    @Override
    public void writeByte(byte b) {
        try {
            if (!justCalc) {
                buffer.put(b);
            } else {
                len += 1;
            }
        } catch (Exception e) {
        }
    }

    @Override
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
                    buffer.put((byte) count);
                } else {
                    len += 1;
                }
            } else {
                if (!justCalc) {
                    buffer.put((byte) 254);
                    buffer.put((byte) count);
                    buffer.put((byte) (count >> 8));
                    buffer.put((byte) (count >> 16));
                } else {
                    len += 4;
                }
            }
            if (!justCalc) {
                buffer.put(b, offset, count);
            } else {
                len += count;
            }
            int i = count <= 253 ? 1 : 4;
            while ((count + i) % 4 != 0) {
                if (!justCalc) {
                    buffer.put((byte) 0);
                } else {
                    len += 1;
                }
                i++;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void writeByteArray(byte[] b) {
        try {
            if (b.length <= 253) {
                if (!justCalc) {
                    buffer.put((byte) b.length);
                } else {
                    len += 1;
                }
            } else {
                if (!justCalc) {
                    buffer.put((byte) 254);
                    buffer.put((byte) b.length);
                    buffer.put((byte) (b.length >> 8));
                    buffer.put((byte) (b.length >> 16));
                } else {
                    len += 4;
                }
            }
            if (!justCalc) {
                buffer.put(b);
            } else {
                len += b.length;
            }
            int i = b.length <= 253 ? 1 : 4;
            while ((b.length + i) % 4 != 0) {
                if (!justCalc) {
                    buffer.put((byte) 0);
                } else {
                    len += 1;
                }
                i++;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void writeDouble(double d) {
        try {
            writeLong(Double.doubleToRawLongBits(d));
        } catch (Exception e) {
        }
    }

    public void writeBytes(NativeByteBuffer b) {
        if (justCalc) {
            len += b.limit();
        } else {
            b.rewind();
            buffer.put(b.buffer);
        }
    }

    public int getIntFromByte(byte b) {
        return b >= 0 ? b : ((int) b) + 256;
    }

    public int length() {
        if (!justCalc) {
            return buffer.position();
        }
        return len;
    }

    public void skip(int count) {
        if (count == 0) {
            return;
        }
        if (!justCalc) {
            buffer.position(buffer.position() + count);
        } else {
            len += count;
        }
    }

    public int getPosition() {
        return buffer.position();
    }

    @NotNull
    public String readString(boolean exception) {
        int startReadPosition = getPosition();
        try {
            int sl = 1;
            int l = getIntFromByte(buffer.get());
            if (l >= 254) {
                l = getIntFromByte(buffer.get()) | (getIntFromByte(buffer.get()) << 8) | (getIntFromByte(buffer.get()) << 16);
                sl = 4;
            }
            byte[] b = new byte[l];
            buffer.get(b);
            int i = sl;
            while ((l + i) % 4 != 0) {
                buffer.get();
                i++;
            }
            return new String(b, "UTF-8");
        } catch (Exception e) {
            if (exception) {
                throw new RuntimeException("read string error", e);
            } else {
            }
            position(startReadPosition);
        }
        return "";
    }

    public void reuse() {
        if (address != 0) {
            reused = true;
            native_reuse(address);
        }
    }

    @Override
    public int remaining() {
        return buffer.remaining();
    }

    @Override
    public boolean readBoolean() {
        int consructor = readInt();
        if (consructor == 0x997275b5) {
            return true;
        } else if (consructor == 0xbc799737) {
            return false;
        }

        return false;
    }

    @Override
    public byte readByte() {
        return buffer.get();
    }

    @NotNull
    @Override
    public byte[] readByteArray() {
        try {
            int sl = 1;
            int l = getIntFromByte(buffer.get());
            if (l >= 254) {
                l = getIntFromByte(buffer.get()) | (getIntFromByte(buffer.get()) << 8) | (getIntFromByte(buffer.get()) << 16);
                sl = 4;
            }
            byte[] b = new byte[l];
            buffer.get(b);
            int i = sl;
            while ((l + i) % 4 != 0) {
                buffer.get();
                i++;
            }
            return b;
        } catch (Exception e) {
        }
        return new byte[0];
    }

    @NotNull
    @Override
    public byte[] readBytes(int i) {
        byte[] b = new byte[i];
        try {
            buffer.get(b);
        } catch (Exception e) {
        }

        return b;
    }

    @Override
    public double readDouble() {
        try {
            return Double.longBitsToDouble(readLong());
        } catch (Exception e) {
        }
        return 0;
    }

    @NotNull
    @Override
    public TLInputStream readInputStream() {
        try {
            int sl = 1;
            int l = getIntFromByte(buffer.get());
            if (l >= 254) {
                l = getIntFromByte(buffer.get()) | (getIntFromByte(buffer.get()) << 8) | (getIntFromByte(buffer.get()) << 16);
                sl = 4;
            }
            NativeByteBuffer b = new NativeByteBuffer(l);
            int old = buffer.limit();
            buffer.limit(buffer.position() + l);
            b.buffer.put(buffer);
            buffer.limit(old);
            b.buffer.position(0);
            int i = sl;
            while ((l + i) % 4 != 0) {
                buffer.get();
                i++;
            }
            return b;
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public int readInt() {
        try {
            return buffer.getInt();
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public long readLong() {
        try {
            return buffer.getLong();
        } catch (Exception e) {
        }
        return 0;
    }

    @NotNull
    @Override
    public String readString() {
        int startReadPosition = getPosition();
        try {
            int sl = 1;
            int l = getIntFromByte(buffer.get());
            if (l >= 254) {
                l = getIntFromByte(buffer.get()) | (getIntFromByte(buffer.get()) << 8) | (getIntFromByte(buffer.get()) << 16);
                sl = 4;
            }
            byte[] b = new byte[l];
            buffer.get(b);
            int i = sl;
            while ((l + i) % 4 != 0) {
                buffer.get();
                i++;
            }
            return new String(b, "UTF-8");
        } catch (Exception e) {
            position(startReadPosition);
        }
        return "";
    }

    @Override
    public void writeInputStream(@NotNull TLInputStream tlInputStream) {
        NativeByteBuffer buffer = (NativeByteBuffer) tlInputStream;
        try {
            int l = buffer.limit();
            if (l <= 253) {
                if (!justCalc) {
                    this.buffer.put((byte) l);
                } else {
                    len += 1;
                }
            } else {
                if (!justCalc) {
                    this.buffer.put((byte) 254);
                    this.buffer.put((byte) l);
                    this.buffer.put((byte) (l >> 8));
                    this.buffer.put((byte) (l >> 16));
                } else {
                    len += 4;
                }
            }
            if (!justCalc) {
                buffer.rewind();
                this.buffer.put(buffer.buffer);
            } else {
                len += l;
            }
            int i = l <= 253 ? 1 : 4;
            while ((l + i) % 4 != 0) {
                if (!justCalc) {
                    this.buffer.put((byte) 0);
                } else {
                    len += 1;
                }
                i++;
            }
        } catch (Exception e) {
        }
    }

    public static native long native_getFreeBuffer(int length);
    public static native ByteBuffer native_getJavaByteBuffer(long address);
    public static native int native_limit(long address);
    public static native int native_position(long address);
    public static native void native_reuse(long address);
}
