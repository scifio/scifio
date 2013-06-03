/*
 * #%L
 * Legacy layer preserving compatibility between legacy Bio-Formats and SCIFIO.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package loci.common.adapter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.scijava.plugin.Plugin;

import loci.common.IRandomAccess;
import loci.legacy.adapter.AbstractLegacyAdapter;
import loci.legacy.adapter.Wrapper;

/**
 * As interfaces can not contain implementation, this class manages
 * interface-level delegation between {@link loci.common.IRandomAccess} and
 * {@link io.scif.io.IRandomAccess}
 * <p>
 * Delegation is maintained by two WeakHashTables. See {@link AbstractLegacyAdapter}
 * </p>
 * <p>
 * Functionally, the delegation is handled in the nested classes - one for
 * wrapping from io.scif.io.IRandomAccess to loci.common.IRandomAccess,
 * and one for the reverse direction.
 * </p>
 * @author Mark Hiner
 *
 */
@Plugin(type=IRandomAccessAdapter.class)
public class IRandomAccessAdapter extends 
  AbstractLegacyAdapter<IRandomAccess, io.scif.io.IRandomAccess> {
  
  // -- Constructor --
  
  public IRandomAccessAdapter() {
    super(IRandomAccess.class, io.scif.io.IRandomAccess.class);
  }
  
  // -- LegacyAdapter API Methods --

  @Override
  protected IRandomAccess wrapToLegacy(io.scif.io.IRandomAccess modern) {
    return new ModernWrapper(modern);
  }

  @Override
  protected io.scif.io.IRandomAccess wrapToModern(IRandomAccess legacy) {
    return new LegacyWrapper(legacy);
  }
  
  // -- Delegation Classes --

  /**
   * This class can be used to wrap loci.common.IRandomAccess
   * objects and be passed to API expecting an io.scif.io.IRandomAccess
   * object.
   * <p>
   * All functionality is delegated to the loci-common implementation.
   * </p>
   * 
   * @author Mark Hiner
   */
  public static class LegacyWrapper
    implements io.scif.io.IRandomAccess, Wrapper<IRandomAccess> {
    
    // -- Fields --
    
    private WeakReference<IRandomAccess> ira;
    
    // -- Constructor --
    
    public LegacyWrapper(IRandomAccess ira) {
      this.ira = new WeakReference<IRandomAccess>(ira);
    }
    
    // -- Wrapper API Methods --
    
    /* @see Wrapper#unwrap() */
    public IRandomAccess unwrap() {
      return ira.get();
    }
    
    // -- IRandomAccess API --

    public void close() throws IOException {
      unwrap().close();
    }

    public long getFilePointer() throws IOException {
      return unwrap().getFilePointer();
    }

    public long length() throws IOException {
      return unwrap().length();
    }

    public ByteOrder getOrder() {
      return unwrap().getOrder();
    }

    public int read(byte[] b) throws IOException {
      return unwrap().read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
      return unwrap().read(b, off, len);
    }

    public int read(ByteBuffer buffer) throws IOException {
      return unwrap().read(buffer);
    }

    public int read(ByteBuffer buffer, int offset, int len) throws IOException {
      return unwrap().read(buffer, offset, len);
    }

    public boolean readBoolean() throws IOException {
      return unwrap().readBoolean();
    }

    public byte readByte() throws IOException {
      return unwrap().readByte();
    }

    public char readChar() throws IOException {
      return unwrap().readChar();
    }

    public double readDouble() throws IOException {
      return unwrap().readDouble();
    }

    public float readFloat() throws IOException {
      return unwrap().readFloat();
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
      unwrap().readFully(b, off, len);
    }

    public void readFully(byte[] b) throws IOException {
      unwrap().readFully(b);
    }

    public int readInt() throws IOException {
      return unwrap().readInt();
    }

    public String readLine() throws IOException {
      return unwrap().readLine();
    }

    public long readLong() throws IOException {
      return unwrap().readLong();
    }

    public short readShort() throws IOException {
      return unwrap().readShort();
    }

    public String readUTF() throws IOException {
      return unwrap().readUTF();
    }

    public int readUnsignedByte() throws IOException {
      return unwrap().readUnsignedByte();
    }

    public int readUnsignedShort() throws IOException {
      return unwrap().readUnsignedShort();
    }

    public void setOrder(ByteOrder order) {
      unwrap().setOrder(order);
    }

    public void seek(long pos) throws IOException {
      unwrap().seek(pos);
    }

    public int skipBytes(int n) throws IOException {
      return unwrap().skipBytes(n);
    }

    public void write(byte[] b, int off, int len) throws IOException {
      unwrap().write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
      unwrap().write(b);
    }

    public void write(ByteBuffer buf) throws IOException {
      unwrap().write(buf);
    }

    public void write(ByteBuffer buf, int off, int len) throws IOException {
      unwrap().write(buf, off, len);
    }

    public void write(int b) throws IOException {
      unwrap().write(b);
    }

    public void writeBoolean(boolean v) throws IOException {
      unwrap().writeBoolean(v);
    }

    public void writeByte(int v) throws IOException {
      unwrap().writeByte(v);
    }

    public void writeBytes(String s) throws IOException {
      unwrap().writeBytes(s);
    }

    public void writeChar(int v) throws IOException {
      unwrap().writeChar(v);
    }

    public void writeChars(String s) throws IOException {
      unwrap().writeChars(s);
    }

    public void writeDouble(double v) throws IOException {
      unwrap().writeDouble(v);
    }

    public void writeFloat(float v) throws IOException {
      unwrap().writeFloat(v);
    }

    public void writeInt(int v) throws IOException {
      unwrap().writeInt(v);
    }

    public void writeLong(long v) throws IOException {
      unwrap().writeLong(v);
    }

    public void writeShort(int v) throws IOException {
      unwrap().writeShort(v);
    }

    public void writeUTF(String s) throws IOException {
      unwrap().writeUTF(s);
    }
  }
  
  /**
   * This class can be used to wrap io.scif.io.IRandomAccess
   * objects and be passed to API expecting a loci.common.IRandomAccess
   * object.
   * <p>
   * All functionality is delegated to the scifio implementation.
   * </p>
   * 
   * @author Mark Hiner
   */
  public static class ModernWrapper implements IRandomAccess, Wrapper<io.scif.io.IRandomAccess> {
    
    // -- Fields --

    private WeakReference<io.scif.io.IRandomAccess> ira;

    // -- Constructor --

    public ModernWrapper(io.scif.io.IRandomAccess ira) {
      this.ira = new WeakReference<io.scif.io.IRandomAccess>(ira);
    }
    
    // -- Wrapper API Methods --

    /* @see Wrapper#unwrap() */
    public io.scif.io.IRandomAccess unwrap() {
      return ira.get();
    }

    // -- IRandomAccess API Methods --

    public boolean readBoolean() throws IOException {
      return unwrap().readBoolean();
    }

    public byte readByte() throws IOException {
      return unwrap().readByte();
    }

    public char readChar() throws IOException {
      return unwrap().readChar();
    }

    public double readDouble() throws IOException {
      return unwrap().readDouble();
    }

    public float readFloat() throws IOException {
      return unwrap().readFloat();
    }

    public void readFully(byte[] b) throws IOException {
      unwrap().readFully(b);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
      unwrap().readFully(b, off, len);
    }

    public int readInt() throws IOException {
      return unwrap().readInt();
    }

    public String readLine() throws IOException {
      return unwrap().readLine();
    }

    public long readLong() throws IOException {
      return unwrap().readLong();
    }

    public short readShort() throws IOException {
      return unwrap().readShort();
    }

    public String readUTF() throws IOException {
      return unwrap().readUTF();
    }

    public int readUnsignedByte() throws IOException {
      return unwrap().readUnsignedByte();
    }

    public int readUnsignedShort() throws IOException {
      return unwrap().readUnsignedShort();
    }

    public int skipBytes(int n) throws IOException {
      return unwrap().skipBytes(n);
    }

    public void write(int b) throws IOException {
      unwrap().write(b);
    }

    public void write(byte[] b) throws IOException {
      unwrap().write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
      unwrap().write(b, off, len);
    }

    public void writeBoolean(boolean v) throws IOException {
      unwrap().writeBoolean(v);
    }

    public void writeByte(int v) throws IOException {
      unwrap().writeByte(v);
    }

    public void writeBytes(String s) throws IOException {
      unwrap().writeBytes(s);
    }

    public void writeChar(int v) throws IOException {
      unwrap().writeChar(v);
    }

    public void writeChars(String s) throws IOException {
      unwrap().writeChars(s);
    }

    public void writeDouble(double v) throws IOException {
      unwrap().writeDouble(v);
    }

    public void writeFloat(float v) throws IOException {
      unwrap().writeFloat(v);
    }

    public void writeInt(int v) throws IOException {
      unwrap().writeInt(v);
    }

    public void writeLong(long v) throws IOException {
      unwrap().writeLong(v);
    }

    public void writeShort(int v) throws IOException {
      unwrap().writeShort(v);
    }

    public void writeUTF(String s) throws IOException {
      unwrap().writeUTF(s);
    }

    public void close() throws IOException {
      unwrap().close();
    }

    public long getFilePointer() throws IOException {
      return unwrap().getFilePointer();
    }

    public long length() throws IOException {
      return unwrap().length();
    }

    public ByteOrder getOrder() {
      return unwrap().getOrder();
    }

    public void setOrder(ByteOrder order) {
      unwrap().setOrder(order);
    }

    public int read(byte[] b) throws IOException {
      return unwrap().read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
      return unwrap().read(b, off, len);
    }

    public int read(ByteBuffer buffer) throws IOException {
      return unwrap().read(buffer);
    }

    public int read(ByteBuffer buffer, int offset, int len) throws IOException {
      return unwrap().read(buffer, offset, len);
    }

    public void seek(long pos) throws IOException {
      unwrap().seek(pos);
    }

    public void write(ByteBuffer buf) throws IOException {
      unwrap().write(buf);
    }

    public void write(ByteBuffer buf, int off, int len) throws IOException {
      unwrap().write(buf, off, len);
    }
  }
}
