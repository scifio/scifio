/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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

package io.scif;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract superclass of all SCIFIO {@link io.scif.Format} implementations.
 * 
 * @see io.scif.Format
 * @see io.scif.Metadata
 * @see io.scif.Parser
 * @see io.scif.Reader
 * @see io.scif.Writer
 * @see io.scif.Checker
 * @see io.scif.services.FormatService
 * @author Mark Hiner
 */
public abstract class AbstractFormat extends AbstractSCIFIOComponent implements
	Format
{

	// -- Fields --

	/** Valid suffixes for this file format. */
	protected String[] suffixes;

	private boolean enabled = true;

	// Class references to the components of this Format
	private Class<? extends Metadata> metadataClass;
	private Class<? extends Checker> checkerClass;
	private Class<? extends Parser> parserClass;
	private Class<? extends Reader> readerClass;
	private Class<? extends Writer> writerClass;

	// -- Constructor --

	public AbstractFormat() {
		metadataClass = DefaultMetadata.class;
		checkerClass = DefaultChecker.class;
		parserClass = DefaultParser.class;
		readerClass = DefaultReader.class;
		writerClass = DefaultWriter.class;

		updateCustomClasses();
	}

	// -- Format API Methods --

	/*
	 * @see io.scif.Format#setEnabled(boolean)
	 */
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	/*
	 * @see io.scif.Format#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/*
	 * @see io.scif.Format#createMetadata()
	 */
	public Metadata createMetadata() throws FormatException {
		return createContextualObject(getMetadataClass());
	}

	/*
	 * @see io.scif.Format#createChecker()
	 */
	public Checker createChecker() throws FormatException {
		return createContextualObject(getCheckerClass());
	}

	/*
	 * @see io.scif.Format#createParser()
	 */
	public Parser createParser() throws FormatException {
		return createContextualObject(getParserClass());
	}

	/*
	 * @see io.scif.Format#createReader()
	 */
	public Reader createReader() throws FormatException {
		return createContextualObject(getReaderClass());
	}

	/*
	 * @see io.scif.Format#createWriter()
	 */
	public Writer createWriter() throws FormatException {
		return createContextualObject(getWriterClass());
	}

	/*
	 * @see io.scif.Format#getMetadataClass()
	 */
	public Class<? extends Metadata> getMetadataClass() {
		return metadataClass;
	}

	/*
	 * @see io.scif.Format#getCheckerClass()
	 */
	public Class<? extends Checker> getCheckerClass() {
		return checkerClass;
	}

	/*
	 * @see io.scif.Format#getParserClass()
	 */
	public Class<? extends Parser> getParserClass() {
		return parserClass;
	}

	/*
	 * @see io.scif.Format#getReaderClass()
	 */
	public Class<? extends Reader> getReaderClass() {
		return readerClass;
	}

	/*
	 * @see io.scif.Format#getWriterClass()
	 */
	public Class<? extends Writer> getWriterClass() {
		return writerClass;
	}

	// -- Helper Methods --

	/*
	 * Creates a SCIFIO component from its class. Also sets its context based
	 * on this format's context.
	 */
	private <T extends HasFormat> T createContextualObject(final Class<T> c)
		throws FormatException
	{
		final T t = createObject(c);
		t.setContext(getContext());

		// if we are creating a Default component, we need to
		// manually set its Format.
		if (DefaultComponent.class.isAssignableFrom(t.getClass())) {
			try {
				final java.lang.reflect.Field fmt =
					t.getClass().getDeclaredField("format");
				fmt.setAccessible(true);
				fmt.set(t, this);
			}
			catch (final NoSuchFieldException e) {
				throw new FormatException("Failed to populate DefaultComponent field",
					e);
			}
			catch (final SecurityException e) {
				throw new FormatException("Failed to populate DefaultComponent field",
					e);
			}
			catch (final IllegalArgumentException e) {
				throw new FormatException("Failed to populate DefaultComponent field",
					e);
			}
			catch (final IllegalAccessException e) {
				throw new FormatException("Failed to populate DefaultComponent field",
					e);
			}
		}
		return t;
	}

	/*
	 * Returns an instance of an object from its Class
	 */
	private <T extends HasFormat> T createObject(final Class<T> c)
		throws FormatException
	{
		try {
			return c.newInstance();
		}
		catch (final InstantiationException e) {
			throw new FormatException(e);
		}
		catch (final IllegalAccessException e) {
			throw new FormatException(e);
		}
	}

	/*
	 * Overrides the default classes with declared custom components.
	 */
	@SuppressWarnings("unchecked")
	private void updateCustomClasses() {

		for (final Class<?> c : buildClassList()) {
			if (Metadata.class.isAssignableFrom(c)) metadataClass =
				(Class<? extends Metadata>) c;
			else if (Checker.class.isAssignableFrom(c)) checkerClass =
				(Class<? extends Checker>) c;
			else if (Parser.class.isAssignableFrom(c)) parserClass =
				(Class<? extends Parser>) c;
			else if (Reader.class.isAssignableFrom(c)) readerClass =
				(Class<? extends Reader>) c;
			else if (Writer.class.isAssignableFrom(c)) writerClass =
				(Class<? extends Writer>) c;
		}
	}

	/*
	 * Searches for all nested classes within this class and recursively
	 * adds them to a complete class list.
	 */
	private List<Class<?>> buildClassList() {
		final Class<?>[] classes = this.getClass().getDeclaredClasses();
		final List<Class<?>> classList = new ArrayList<Class<?>>();

		for (final Class<?> c : classes) {
			check(c, classList);
		}

		return classList;
	}

	/*
	 * Recursive method to add a class, and all nested classes declared in that
	 * class, to the provided list of classes.
	 */
	private void check(final Class<?> newClass, final List<Class<?>> classList) {
		classList.add(newClass);

		for (final Class<?> c : newClass.getDeclaredClasses())
			check(c, classList);

	}
}
