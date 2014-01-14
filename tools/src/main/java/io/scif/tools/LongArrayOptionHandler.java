/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Open Microscopy Environment:
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

package io.scif.tools;

import java.lang.reflect.Field;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.scijava.util.ClassUtils;

/**
 * {@link OptionHandler} implementation for setting long array fields. Accepts a
 * ','-delimited String of values.
 * 
 * @author Mark Hiner
 */
public class LongArrayOptionHandler extends OptionHandler<Long> {

	// -- Fields --

	/**
	 * Need a field for ClassUtils conversion
	 */
	private long[] lArray;

	// -- Constructor --

	public LongArrayOptionHandler(final CmdLineParser parser,
		final OptionDef option, final Setter<Long> setter)
	{
		super(parser, option, setter);
	}

	// -- OptionHandler API --

	@Override
	public int parseArguments(final Parameters params) throws CmdLineException {
		final String val = params.getParameter(0);
		final String[] splitVal = val.split(",");

		// Set the instance field on this option handler
		try {
			final Field f = getClass().getDeclaredField("lArray");
			ClassUtils.setValue(f, this, splitVal);
		}
		catch (final SecurityException e) {
			e.printStackTrace();
		}
		catch (final NoSuchFieldException e) {
			e.printStackTrace();
		}
		// Pass the values to the actual option being set. Has to be one at a time
		// for some reason..
		for (final long l : lArray) {
			setter.addValue(l);
		}
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "VAL,VAL,...";
	}

}
