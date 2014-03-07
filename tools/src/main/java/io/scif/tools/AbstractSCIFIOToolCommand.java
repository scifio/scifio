/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Board of Regents of the University of
 * Wisconsin-Madison
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
 * #L%
 */

package io.scif.tools;

import io.scif.AbstractSCIFIOPlugin;

import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * Abstract {@link SCIFIOToolCommand} superclass. Has a
 * {@link #runCommand(String...)} entry point which takes care of parsing
 * arguments, printing command usage, etc...
 * 
 * @author Mark Hiner
 */
public abstract class AbstractSCIFIOToolCommand extends AbstractSCIFIOPlugin
	implements SCIFIOToolCommand
{

	// -- Fields --

	@Parameter
	private LogService logService;

	// -- Parameters --

	@Option(name = "-h", aliases = "--help", usage = "print help information")
	private boolean help;

	@Option(name = "-v", aliases = "--version", usage = "print version and exit")
	private boolean printVersion;

	@Option(name = "-d", aliases = "--debug", usage = "turn on debugging output")
	private boolean debug;

	// -- SCIFIOToolCommand API --

	@Override
	public void runCommand(final String... args) {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);
			if (getExtraArguments() != null && !getExtraArguments().isEmpty()) {
				warn("the following arguments were not used:");
				for (final String arg : getExtraArguments()) {
					warn("\t" + arg);
				}
			}
			if (help) {
				info(getName() + " - " + description() + "\nUsage:\n" + "scifio " +
					getName() + " [options...] arguments...\nParameters:");
				parser.printUsage(System.out);
				return;
			}
			validateParams();
			if (printVersion) {
				// TODO
			}
		}
		catch (final CmdLineException e) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			err(e.getMessage());
			err("scifio " + getName() + " [options...] arguments...\nParameters:");
			// print the list of available options
			parser.printUsage(System.err);
			return;
		}
		try {
			run();
		}
		catch (final CmdLineException e) {
			logService.error(e.getMessage(), e);
		}
	}

	// -- AbstractSCIFIOToolCommand API --

	/**
	 * Performs the actual tool operations. Invoked by
	 * {@link #runCommand(String...)}.
	 */
	protected abstract void run() throws CmdLineException;

	/**
	 * @return A description of this tool command.
	 */
	protected abstract String description();

	/**
	 * @return A name for this tool.
	 */
	protected abstract String getName();

	/**
	 * Each SCIFIOToolCommand implementation should have a {@code List<String>}
	 * {@link Argument} field which is {@code multiValued = true} and with an
	 * {@code index} putting it at the end of the argument list. The user will be
	 * warned that these arguments were not used.
	 * 
	 * @return A list of any unused arguments
	 */
	protected abstract List<String> getExtraArguments();

	/**
	 * Very that all parameters were set correctly, in case any need checking that
	 * the {@link Parameter} or {@link Argument} can't provide.
	 */
	protected abstract void validateParams() throws CmdLineException;

	// -- Logging methods --

	/**
	 * @param msg Error message to print
	 */
	protected void err(final String msg) {
		logService.error(msg);
	}

	/**
	 * @param msg Warning message to print
	 */
	protected void warn(final String msg) {
		logService.warn(msg);
	}

	/**
	 * @param msg Info message to print
	 */
	protected void info(final String msg) {
		logService.info(msg);
	}

	/**
	 * @param msg Debug statement to print
	 */
	protected void debug(final String msg) {
		if (debug) logService.debug(msg);
	}


	// -- SCIFIOToolCommand methods --

	/**
	 * Default command name is a lower-case version of the class name. Override
	 * if desired, as this is what is displayed to the user.
	 */
	@Override
	public String commandName() {
		return getClass().toString().toLowerCase();
	}
}
