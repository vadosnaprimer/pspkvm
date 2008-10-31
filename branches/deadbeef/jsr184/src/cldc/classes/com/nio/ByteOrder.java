package com.nio;

public final class ByteOrder {
	/**
	 * Constant indicating big endian byte order.
	 */
	public static final ByteOrder BIG_ENDIAN = new ByteOrder();

	/**
	 * Constant indicating little endian byte order.
	 */
	public static final ByteOrder LITTLE_ENDIAN = new ByteOrder();

	/**
	 * Returns the native byte order of the platform currently running.
	 *
	 * @return the native byte order
	 */
	public static ByteOrder nativeOrder() {
		// Let this fail with an NPE when the property is not set correctly.
		// Otherwise we risk that NIO is silently working wrong.
		return (System.getProperty("gnu.cpu.endian").equals("big") ? BIG_ENDIAN
				: LITTLE_ENDIAN);
	}

	/**
	 * Returns a string representation of the byte order.
	 *
	 * @return the string
	 */
	public String toString() {
		return this == BIG_ENDIAN ? "BIG_ENDIAN" : "LITTLE_ENDIAN";
	}

	// This class can only be instantiated here.
	private ByteOrder() {
	}
}
