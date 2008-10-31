package com.nio;

final class ByteBufferHelper {
	public static char getChar(ByteBuffer buffer, ByteOrder order) {
		return (char) getShort(buffer, order);
	}

	public static void putChar(ByteBuffer buffer, char value, ByteOrder order) {
		putShort(buffer, (short) value, order);
	}

	public static char getChar(ByteBuffer buffer, int index, ByteOrder order) {
		return (char) getShort(buffer, index, order);
	}

	public static void putChar(ByteBuffer buffer, int index, char value,
			ByteOrder order) {
		putShort(buffer, index, (short) value, order);
	}

	public static short getShort(ByteBuffer buffer, ByteOrder order) {
		buffer.checkForUnderflow(2);

		if (order == ByteOrder.LITTLE_ENDIAN) {
			return (short) ((buffer.get() & 0xff) + (buffer.get() << 8));
		}

		return (short) ((buffer.get() << 8) + (buffer.get() & 0xff));
	}

	public static void putShort(ByteBuffer buffer, short value, ByteOrder order) {
		buffer.checkForOverflow(2);

		if (order == ByteOrder.LITTLE_ENDIAN) {
			buffer.put((byte) value);
			buffer.put((byte) (value >> 8));
		} else {
			buffer.put((byte) (value >> 8));
			buffer.put((byte) value);
		}
	}

	public static short getShort(ByteBuffer buffer, int index, ByteOrder order) {
		if (order == ByteOrder.LITTLE_ENDIAN) {
			return (short) ((buffer.get(index) & 0xff) + (buffer.get(++index) << 8));
		}

		return (short) ((buffer.get(index) << 8) + (buffer.get(++index) & 0xff));
	}

	public static void putShort(ByteBuffer buffer, int index, short value,
			ByteOrder order) {
		if (order == ByteOrder.LITTLE_ENDIAN) {
			buffer.put(index, (byte) value);
			buffer.put(++index, (byte) (value >> 8));
		} else {
			buffer.put(index, (byte) (value >> 8));
			buffer.put(++index, (byte) value);
		}
	}

	public static int getInt(ByteBuffer buffer, ByteOrder order) {
		buffer.checkForUnderflow(4);

		if (order == ByteOrder.LITTLE_ENDIAN) {
			return ((buffer.get() & 0xff) + ((buffer.get() & 0xff) << 8)
					+ ((buffer.get() & 0xff) << 16) + (buffer.get() << 24));
		}

		return (int) ((buffer.get() << 24) + ((buffer.get() & 0xff) << 16)
				+ ((buffer.get() & 0xff) << 8) + (buffer.get() & 0xff));
	}

	public static void putInt(ByteBuffer buffer, int value, ByteOrder order) {
		buffer.checkForOverflow(4);

		if (order == ByteOrder.LITTLE_ENDIAN) {
			buffer.put((byte) value);
			buffer.put((byte) (value >> 8));
			buffer.put((byte) (value >> 16));
			buffer.put((byte) (value >> 24));
		} else {
			buffer.put((byte) (value >> 24));
			buffer.put((byte) (value >> 16));
			buffer.put((byte) (value >> 8));
			buffer.put((byte) value);
		}
	}

	public static int getInt(ByteBuffer buffer, int index, ByteOrder order) {
		if (order == ByteOrder.LITTLE_ENDIAN) {
			return ((buffer.get(index) & 0xff)
					+ ((buffer.get(++index) & 0xff) << 8)
					+ ((buffer.get(++index) & 0xff) << 16) + (buffer
					.get(++index) << 24));
		}

		return ((buffer.get(index) << 24)
				+ ((buffer.get(++index) & 0xff) << 16)
				+ ((buffer.get(++index) & 0xff) << 8) + (buffer.get(++index) & 0xff));
	}

	public static void putInt(ByteBuffer buffer, int index, int value,
			ByteOrder order) {
		if (order == ByteOrder.LITTLE_ENDIAN) {
			buffer.put(index, (byte) value);
			buffer.put(++index, (byte) (value >> 8));
			buffer.put(++index, (byte) (value >> 16));
			buffer.put(++index, (byte) (value >> 24));
		} else {
			buffer.put(index, (byte) (value >> 24));
			buffer.put(++index, (byte) (value >> 16));
			buffer.put(++index, (byte) (value >> 8));
			buffer.put(++index, (byte) value);
		}
	}

	public static long getLong(ByteBuffer buffer, ByteOrder order) {
		buffer.checkForUnderflow(8);

		if (order == ByteOrder.LITTLE_ENDIAN) {
			return ((buffer.get() & 0xff) + (((buffer.get() & 0xff)) << 8)
					+ (((buffer.get() & 0xff)) << 16)
					+ (((buffer.get() & 0xffL)) << 24)
					+ (((buffer.get() & 0xffL)) << 32)
					+ (((buffer.get() & 0xffL)) << 40)
					+ (((buffer.get() & 0xffL)) << 48) + (((long) buffer.get()) << 56));
		}

		return ((((long) buffer.get()) << 56) + ((buffer.get() & 0xffL) << 48)
				+ ((buffer.get() & 0xffL) << 40)
				+ ((buffer.get() & 0xffL) << 32)
				+ ((buffer.get() & 0xffL) << 24)
				+ ((buffer.get() & 0xff) << 16) + ((buffer.get() & 0xff) << 8) + (buffer
				.get() & 0xff));
	}

	public static void putLong(ByteBuffer buffer, long value, ByteOrder order) {
		buffer.checkForOverflow(8);

		if (order == ByteOrder.LITTLE_ENDIAN) {
			buffer.put((byte) value);
			buffer.put((byte) (value >> 8));
			buffer.put((byte) (value >> 16));
			buffer.put((byte) (value >> 24));
			buffer.put((byte) (value >> 32));
			buffer.put((byte) (value >> 40));
			buffer.put((byte) (value >> 48));
			buffer.put((byte) (value >> 56));
		} else {
			buffer.put((byte) (value >> 56));
			buffer.put((byte) (value >> 48));
			buffer.put((byte) (value >> 40));
			buffer.put((byte) (value >> 32));
			buffer.put((byte) (value >> 24));
			buffer.put((byte) (value >> 16));
			buffer.put((byte) (value >> 8));
			buffer.put((byte) value);
		}
	}

	public static long getLong(ByteBuffer buffer, int index, ByteOrder order) {
		if (order == ByteOrder.LITTLE_ENDIAN) {
			return ((buffer.get(index) & 0xff)
					+ ((buffer.get(++index) & 0xff) << 8)
					+ ((buffer.get(++index) & 0xff) << 16)
					+ ((buffer.get(++index) & 0xffL) << 24)
					+ ((buffer.get(++index) & 0xffL) << 32)
					+ ((buffer.get(++index) & 0xffL) << 40)
					+ ((buffer.get(++index) & 0xffL) << 48) + (((long) buffer
					.get(++index)) << 56));
		}

		return ((((long) buffer.get(index)) << 56)
				+ ((buffer.get(++index) & 0xffL) << 48)
				+ ((buffer.get(++index) & 0xffL) << 40)
				+ ((buffer.get(++index) & 0xffL) << 32)
				+ ((buffer.get(++index) & 0xffL) << 24)
				+ ((buffer.get(++index) & 0xff) << 16)
				+ ((buffer.get(++index) & 0xff) << 8) + (buffer.get(++index) & 0xff));
	}

	public static void putLong(ByteBuffer buffer, int index, long value,
			ByteOrder order) {
		if (order == ByteOrder.LITTLE_ENDIAN) {
			buffer.put(index, (byte) value);
			buffer.put(++index, (byte) (value >> 8));
			buffer.put(++index, (byte) (value >> 16));
			buffer.put(++index, (byte) (value >> 24));
			buffer.put(++index, (byte) (value >> 32));
			buffer.put(++index, (byte) (value >> 40));
			buffer.put(++index, (byte) (value >> 48));
			buffer.put(++index, (byte) (value >> 56));
		} else {
			buffer.put(index, (byte) (value >> 56));
			buffer.put(++index, (byte) (value >> 48));
			buffer.put(++index, (byte) (value >> 40));
			buffer.put(++index, (byte) (value >> 32));
			buffer.put(++index, (byte) (value >> 24));
			buffer.put(++index, (byte) (value >> 16));
			buffer.put(++index, (byte) (value >> 8));
			buffer.put(++index, (byte) value);
		}
	}

	public static float getFloat(ByteBuffer buffer, ByteOrder order) {
		return Float.intBitsToFloat(getInt(buffer, order));
	}

	public static void putFloat(ByteBuffer buffer, float value, ByteOrder order) {
		// FIXME
		// We use floatToIntBits() that might collapse the NaN
		// MIDP currently not support floatToRawIntBits()
		// putInt(buffer, Float.floatToRawIntBits(value), order);
		putInt(buffer, Float.floatToIntBits(value), order);
	}

	public static float getFloat(ByteBuffer buffer, int index, ByteOrder order) {
		return Float.intBitsToFloat(getInt(buffer, index, order));
	}

	public static void putFloat(ByteBuffer buffer, int index, float value,
			ByteOrder order) {
		// FIXME
		// We use floatToIntBits() that might collapse the NaN
		// MIDP currently not support floatToRawIntBits()
		// putInt(buffer, Float.floatToRawIntBits(value), order);
		putInt(buffer, index, Float.floatToIntBits(value), order);
	}

	public static double getDouble(ByteBuffer buffer, ByteOrder order) {
		return Double.longBitsToDouble(getLong(buffer, order));
	}

	public static void putDouble(ByteBuffer buffer, double value,
			ByteOrder order) {
		// FIXME
		// We use doubleToLongBits() that might collapse the NaN
		// MIDP currently not support doubleToRawLongBits()
		// putLong(buffer, Double.doubleToRawLongBits(value), order);
		putLong(buffer, Double.doubleToLongBits(value), order);
	}

	public static double getDouble(ByteBuffer buffer, int index, ByteOrder order) {
		return Double.longBitsToDouble(getLong(buffer, index, order));
	}

	public static void putDouble(ByteBuffer buffer, int index, double value,
			ByteOrder order) {
		// FIXME
		// We use doubleToLongBits() that might collapse the NaN
		// MIDP currently not support doubleToRawLongBits()
		// putLong(buffer, Double.doubleToRawLongBits(value), order);
		putLong(buffer, index, Double.doubleToLongBits(value), order);
	}
}
