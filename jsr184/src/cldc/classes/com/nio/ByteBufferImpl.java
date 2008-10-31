package com.nio;

final class ByteBufferImpl extends ByteBuffer {
	private boolean readOnly;

	ByteBufferImpl(byte[] buffer, int offset, int capacity, int limit,
			int position, int mark, boolean readOnly) {
		super(capacity, limit, position, mark);
		this.backing_buffer = buffer;
		this.array_offset = offset;
		this.readOnly = readOnly;
	}

	//  public CharBuffer asCharBuffer ()
	//  {
	//    return new CharViewBufferImpl (this, remaining() >> 1);
	//  }

	public ShortBuffer asShortBuffer() {
		return new ShortViewBufferImpl(this, remaining() >> 1);
	}

	//  public IntBuffer asIntBuffer ()
	//  {
	//    return new IntViewBufferImpl (this, remaining() >> 2);
	//  }

	public LongBuffer asLongBuffer() {
		return new LongViewBufferImpl(this, remaining() >> 3);
	}

	public FloatBuffer asFloatBuffer() {
		return new FloatViewBufferImpl(this, remaining() >> 2);
	}

	//  public DoubleBuffer asDoubleBuffer ()
	//  {
	//    return new DoubleViewBufferImpl (this, remaining() >> 3);
	//  }

	public boolean isReadOnly() {
		return readOnly;
	}

	public ByteBuffer slice() {
		return new ByteBufferImpl(backing_buffer, array_offset + position(),
				remaining(), remaining(), 0, -1, isReadOnly());
	}

	public ByteBuffer duplicate() {
		return new ByteBufferImpl(backing_buffer, array_offset, capacity(),
				limit(), position(), mark, isReadOnly());
	}

	public ByteBuffer asReadOnlyBuffer() {
		return new ByteBufferImpl(backing_buffer, array_offset, capacity(),
				limit(), position(), mark, true);
	}

	void shiftDown(int dst_offset, int src_offset, int count) {
		System.arraycopy(backing_buffer, array_offset + src_offset,
				backing_buffer, array_offset + dst_offset, count);
	}

	public ByteBuffer compact() {
		checkIfReadOnly();
		mark = -1;
		int pos = position();
		int n = limit() - pos;
		if (n > 0)
			shiftDown(0, pos, n);
		position(n);
		limit(capacity());
		return this;
	}

	public boolean isDirect() {
		return false;
	}

	/**
	 * Reads the <code>byte</code> at this buffer's current position,
	 * and then increments the position.
	 *
	 * @exception BufferUnderflowException If there are no remaining
	 * <code>bytes</code> in this buffer.
	 */
	public byte get() {
		if (pos >= limit)
			throw new BufferUnderflowException();

		return backing_buffer[(pos++) + array_offset];
	}

	/**
	 * Bulk get
	 */
	public ByteBuffer get(byte[] dst, int offset, int length) {
		checkArraySize(dst.length, offset, length);
		if ((limit - pos) < length) // check for overflow
			throw new BufferUnderflowException();

		System.arraycopy(backing_buffer, pos + array_offset, dst, offset,
				length);
		pos += length;

		return this;
	}

	/**
	 * Relative bulk put(), overloads the ByteBuffer impl.
	 */
	public ByteBuffer put(byte[] src, int offset, int length) {
		if ((limit - pos) < length) // check for overflow
			throw new BufferOverflowException();
		checkArraySize(src.length, offset, length);

		System.arraycopy(src, offset, backing_buffer, pos + array_offset,
				length);
		pos += length;

		return this;
	}

	/**
	 * Relative put method. Writes <code>value</code> to the next position
	 * in the buffer.
	 *
	 * @exception BufferOverflowException If there is no remaining
	 * space in this buffer.
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public ByteBuffer put(byte value) {
		if (readOnly)
			throw new ReadOnlyBufferException();
		if (pos >= limit)
			throw new BufferOverflowException();

		backing_buffer[(pos++) + array_offset] = value;
		return this;
	}

	/**
	 * Absolute get method. Reads the <code>byte</code> at position
	 * <code>index</code>.
	 *
	 * @exception IndexOutOfBoundsException If index is negative or not smaller
	 * than the buffer's limit.
	 */
	public byte get(int index) {
		checkIndex(index);

		return backing_buffer[index + array_offset];
	}

	/**
	 * Absolute put method. Writes <code>value</code> to position
	 * <code>index</code> in the buffer.
	 *
	 * @exception IndexOutOfBoundsException If index is negative or not smaller
	 * than the buffer's limit.
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public ByteBuffer put(int index, byte value) {
		checkIfReadOnly();
		checkIndex(index);

		backing_buffer[index + array_offset] = value;
		return this;
	}

	public char getChar() {
		return ByteBufferHelper.getChar(this, order());
	}

	public ByteBuffer putChar(char value) {
		if (readOnly)
			throw new ReadOnlyBufferException();
		if ((limit - pos) < 2)
			throw new BufferOverflowException();

		if (endian == ByteOrder.LITTLE_ENDIAN) {
			backing_buffer[(pos++) + array_offset] = (byte) (value & 0xFF);
			backing_buffer[(pos++) + array_offset] = (byte) (value >> 8);
		} else {
			backing_buffer[(pos++) + array_offset] = (byte) (value >> 8);
			backing_buffer[(pos++) + array_offset] = (byte) (value & 0xFF);
		}
		return this;
	}

	public char getChar(int index) {
		return ByteBufferHelper.getChar(this, index, order());
	}

	public ByteBuffer putChar(int index, char value) {
		ByteBufferHelper.putChar(this, index, value, order());
		return this;
	}

	public short getShort() {
		return ByteBufferHelper.getShort(this, order());
	}

	public ByteBuffer putShort(short value) {
		ByteBufferHelper.putShort(this, value, order());
		return this;
	}

	public short getShort(int index) {
		return ByteBufferHelper.getShort(this, index, order());
	}

	public ByteBuffer putShort(int index, short value) {
		ByteBufferHelper.putShort(this, index, value, order());
		return this;
	}

	public int getInt() {
		return ByteBufferHelper.getInt(this, order());
	}

	public ByteBuffer putInt(int value) {
		ByteBufferHelper.putInt(this, value, order());
		return this;
	}

	public int getInt(int index) {
		return ByteBufferHelper.getInt(this, index, order());
	}

	public ByteBuffer putInt(int index, int value) {
		ByteBufferHelper.putInt(this, index, value, order());
		return this;
	}

	public long getLong() {
		return ByteBufferHelper.getLong(this, order());
	}

	public ByteBuffer putLong(long value) {
		ByteBufferHelper.putLong(this, value, order());
		return this;
	}

	public long getLong(int index) {
		return ByteBufferHelper.getLong(this, index, order());
	}

	public ByteBuffer putLong(int index, long value) {
		ByteBufferHelper.putLong(this, index, value, order());
		return this;
	}

	public float getFloat() {
		return ByteBufferHelper.getFloat(this, order());
	}

	public ByteBuffer putFloat(float value) {
		ByteBufferHelper.putFloat(this, value, order());
		return this;
	}

	public float getFloat(int index) {
		return ByteBufferHelper.getFloat(this, index, order());
	}

	public ByteBuffer putFloat(int index, float value) {
		ByteBufferHelper.putFloat(this, index, value, order());
		return this;
	}

	public double getDouble() {
		return ByteBufferHelper.getDouble(this, order());
	}

	public ByteBuffer putDouble(double value) {
		ByteBufferHelper.putDouble(this, value, order());
		return this;
	}

	public double getDouble(int index) {
		return ByteBufferHelper.getDouble(this, index, order());
	}

	public ByteBuffer putDouble(int index, double value) {
		ByteBufferHelper.putDouble(this, index, value, order());
		return this;
	}
}
