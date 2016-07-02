/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
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

package io.scif.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;

/**
 * A general-purpose reflection wrapper class.
 * 
 * @deprecated Use {@link org.scijava.util.ReflectedUniverse} instead.
 */
@Deprecated
public class ReflectedUniverse {

	// -- Fields --

	/** Hashtable containing all variables present in the universe. */
	private final HashMap<String, Object> variables;

	/** Class loader for imported classes. */
	private final ClassLoader loader;

	/** Whether to force our way past restrictive access modifiers. */
	private boolean force;

	private final LogService log;

	// -- Constructors --

	/** Constructs a new reflected universe. */
	public ReflectedUniverse() {
		this(null, null);
	}

	/** Constructs a new reflected universe. */
	public ReflectedUniverse(final LogService log) {
		this(null, log);
	}

	/**
	 * Constructs a new reflected universe, with the given URLs representing
	 * additional search paths for imported classes (in addition to the
	 * CLASSPATH).
	 */
	public ReflectedUniverse(final URL[] urls) {
		this(urls == null ? null : new URLClassLoader(urls));
	}

	/** Constructs a new reflected universe that uses the given class loader. */
	public ReflectedUniverse(final ClassLoader loader) {
		this(loader, null);
	}

	public ReflectedUniverse(final ClassLoader loader, final LogService log) {
		variables = new HashMap<>();
		this.loader = loader == null ? getClass().getClassLoader() : loader;
		this.log = log == null ? new StderrLogService() : log;
	}

	// -- Utility methods --

	/**
	 * Returns whether the given object is compatible with the specified class for
	 * the purposes of reflection.
	 */
	public static boolean isInstance(final Class<?> c, final Object o) {
		return (o == null || c.isInstance(o) ||
			(c == byte.class && o instanceof Byte) ||
			(c == short.class && o instanceof Short) ||
			(c == int.class && o instanceof Integer) ||
			(c == long.class && o instanceof Long) ||
			(c == float.class && o instanceof Float) ||
			(c == double.class && o instanceof Double) ||
			(c == boolean.class && o instanceof Boolean) || (c == char.class && o instanceof Character));
	}

	// -- ReflectedUniverse API methods --

	/**
	 * Executes a command in the universe. The following syntaxes are valid:
	 * <ul>
	 * <li>import fully.qualified.package.ClassName</li>
	 * <li>var = new ClassName(param1, ..., paramN)</li>
	 * <li>var.method(param1, ..., paramN)</li>
	 * <li>var2 = var.method(param1, ..., paramN)</li>
	 * <li>ClassName.method(param1, ..., paramN)</li>
	 * <li>var2 = ClassName.method(param1, ..., paramN)</li>
	 * <li>var2 = var</li>
	 * </ul>
	 * Important guidelines:
	 * <ul>
	 * <li>Any referenced class must be imported first using "import".</li>
	 * <li>Variables can be exported from the universe with getVar().</li>
	 * <li>Variables can be imported to the universe with setVar().</li>
	 * <li>Each parameter must be either:
	 * <ol>
	 * <li>a variable in the universe</li>
	 * <li>a static or instance field (i.e., no nested methods)</li>
	 * <li>a string literal (remember to escape the double quotes)</li>
	 * <li>an integer literal</li>
	 * <li>a long literal (ending in L)</li>
	 * <li>a double literal (containing a decimal point)</li>
	 * <li>a boolean literal (true or false)</li>
	 * <li>the null keyword</li>
	 * </ol>
	 * </li>
	 * </ul>
	 */
	public Object exec(String command) throws ReflectException {
		command = command.trim();
		if (command.startsWith("import ")) {
			// command is an import statement
			command = command.substring(7).trim();
			final int dot = command.lastIndexOf(".");
			final String varName = dot < 0 ? command : command.substring(dot + 1);
			Class<?> c;
			try {
				c = Class.forName(command, true, loader);
			}
			catch (final NoClassDefFoundError err) {
				log.debug("No such class: " + command, err);
				throw new ReflectException("No such class: " + command, err);
			}
			catch (final ClassNotFoundException exc) {
				log.debug("No such class: " + command, exc);
				throw new ReflectException("No such class: " + command, exc);
			}
			catch (final RuntimeException exc) {
				// HACK: workaround for bug in Apache Axis2
				final String msg = exc.getMessage();
				if (msg != null && !msg.contains("ClassNotFound")) throw exc;
				log.debug("No such class: " + command, exc);
				throw new ReflectException("No such class: " + command, exc);
			}
			setVar(varName, c);
			return null;
		}

		// get variable where results of command should be stored
		final int eqIndex = command.indexOf("=");
		String target = null;
		if (eqIndex >= 0) {
			target = command.substring(0, eqIndex).trim();
			command = command.substring(eqIndex + 1).trim();
		}

		Object result = null;

		// parse parentheses
		final int leftParen = command.indexOf("(");
		if (leftParen < 0) {
			// command is a simple assignment
			result = getVar(command);
			if (target != null) setVar(target, result);
			return result;
		}
		else if (leftParen != command.lastIndexOf("(") ||
			command.indexOf(")") != command.length() - 1)
		{
			throw new ReflectException("Invalid parentheses");
		}

		// parse arguments
		final String arglist = command.substring(leftParen + 1);
		final StringTokenizer st = new StringTokenizer(arglist, "(,)");
		final int len = st.countTokens();
		final Object[] args = new Object[len];
		for (int i = 0; i < len; i++) {
			final String arg = st.nextToken().trim();
			args[i] = getVar(arg);
		}
		command = command.substring(0, leftParen);

		if (command.startsWith("new ")) {
			// command is a constructor call
			final String className = command.substring(4).trim();
			final Object var = getVar(className);
			if (var == null) {
				throw new ReflectException("Class not found: " + className);
			}
			else if (!(var instanceof Class<?>)) {
				throw new ReflectException("Not a class: " + className);
			}
			final Class<?> cl = (Class<?>) var;

			// Search for a constructor that matches the arguments.
			// Unfortunately,
			// calling cl.getConstructor(argClasses) does not work, because
			// getConstructor() is not flexible enough to detect when the
			// arguments
			// are subclasses of the constructor argument classes, making a
			// brute
			// force search through all public constructors necessary.
			Constructor<?> constructor = null;
			final Constructor<?>[] c = cl.getConstructors();
			for (int i = 0; i < c.length; i++) {
				if (force) c[i].setAccessible(true);
				final Class<?>[] params = c[i].getParameterTypes();
				if (params.length == args.length) {
					boolean match = true;
					for (int j = 0; j < params.length; j++) {
						if (!isInstance(params[j], args[j])) {
							match = false;
							break;
						}
					}
					if (match) {
						constructor = c[i];
						break;
					}
				}
			}
			if (constructor == null) {
				final StringBuilder sb = new StringBuilder(command);
				for (int i = 0; i < args.length; i++) {
					sb.append(i == 0 ? "(" : ", ");
					sb.append(args[i].getClass().getName());
				}
				sb.append(")");
				throw new ReflectException("No such constructor: " + sb.toString());
			}

			// invoke constructor
			Exception exc = null;
			try {
				result = constructor.newInstance(args);
			}
			catch (final InstantiationException e) {
				exc = e;
			}
			catch (final IllegalAccessException e) {
				exc = e;
			}
			catch (final InvocationTargetException e) {
				exc = e;
			}
			if (exc != null) {
				log.debug("Cannot instantiate object", exc);
				throw new ReflectException("Cannot instantiate object", exc);
			}
		}
		else {
			// command is a method call
			final int dot = command.indexOf(".");
			if (dot < 0) throw new ReflectException("Syntax error");
			final String varName = command.substring(0, dot).trim();
			final String methodName = command.substring(dot + 1).trim();
			final Object var = getVar(varName);
			if (var == null) {
				throw new ReflectException("No such variable: " + varName);
			}
			final Class<?> varClass =
				var instanceof Class<?> ? (Class<?>) var : var.getClass();

			// Search for a method that matches the arguments. Unfortunately,
			// calling varClass.getMethod(methodName, argClasses) does not work,
			// because getMethod() is not flexible enough to detect when the
			// arguments are subclasses of the method argument classes, making a
			// brute force search through all public methods necessary.
			Method method = null;
			for (final Method candidateMethod : varClass.getMethods()) {
				if (force) candidateMethod.setAccessible(true);
				if (methodName.equals(candidateMethod.getName())) {
					final Class<?>[] params = candidateMethod.getParameterTypes();
					if (params.length == args.length) {
						boolean match = true;
						for (int j = 0; j < params.length; j++) {
							if (!isInstance(params[j], args[j])) {
								match = false;
								break;
							}
						}
						if (match) {
							method = candidateMethod;
							break;
						}
					}
				}
			}
			if (method == null) {
				throw new ReflectException("No such method: " + methodName);
			}

			// invoke method
			Exception exc = null;
			try {
				result = method.invoke(var, args);
			}
			catch (final IllegalAccessException e) {
				exc = e;
			}
			catch (final InvocationTargetException e) {
				exc = e;
			}
			if (exc != null) {
				log.debug("Cannot execute method: " + methodName, exc);
				throw new ReflectException("Cannot execute method: " + methodName, exc);
			}
		}

		// assign result to proper variable
		if (target != null) setVar(target, result);
		return result;
	}

	/** Registers a variable in the universe. */
	public void setVar(final String varName, final Object obj) {
		if (obj == null) variables.remove(varName);
		else variables.put(varName, obj);
	}

	/** Registers a variable of primitive type boolean in the universe. */
	public void setVar(final String varName, final boolean b) {
		setVar(varName, Boolean.valueOf(b));
	}

	/** Registers a variable of primitive type byte in the universe. */
	public void setVar(final String varName, final byte b) {
		setVar(varName, new Byte(b));
	}

	/** Registers a variable of primitive type char in the universe. */
	public void setVar(final String varName, final char c) {
		setVar(varName, Character.valueOf(c));
	}

	/** Registers a variable of primitive type double in the universe. */
	public void setVar(final String varName, final double d) {
		setVar(varName, new Double(d));
	}

	/** Registers a variable of primitive type float in the universe. */
	public void setVar(final String varName, final float f) {
		setVar(varName, new Float(f));
	}

	/** Registers a variable of primitive type int in the universe. */
	public void setVar(final String varName, final int i) {
		setVar(varName, Integer.valueOf(i));
	}

	/** Registers a variable of primitive type long in the universe. */
	public void setVar(final String varName, final long l) {
		setVar(varName, Long.valueOf(l));
	}

	/** Registers a variable of primitive type short in the universe. */
	public void setVar(final String varName, final short s) {
		setVar(varName, Short.valueOf(s));
	}

	/**
	 * Returns the value of a variable or field in the universe. Primitive types
	 * will be wrapped in their Java Object wrapper classes.
	 */
	public Object getVar(final String varName) throws ReflectException {
		if (varName.equals("null")) {
			// variable is a null value
			return null;
		}
		else if (varName.equals("true")) {
			// variable is a boolean literal
			return Boolean.TRUE;
		}
		else if (varName.equals("false")) {
			// variable is a boolean literal
			return Boolean.FALSE;
		}
		else if (varName.startsWith("\"") && varName.endsWith("\"")) {
			// variable is a string literal
			return varName.substring(1, varName.length() - 1);
		}
		try {
			if (varName.matches("-?\\d+")) {
				// variable is an int literal
				return new Integer(varName);
			}
			else if (varName.matches("-?\\d+L")) {
				// variable is a long literal
				return new Long(varName);
			}
			else if (varName.matches("-?\\d*\\.\\d*")) {
				// variable is a double literal
				return new Double(varName);
			}
		}
		catch (final NumberFormatException exc) {
			throw new ReflectException("Invalid literal: " + varName, exc);
		}
		final int dot = varName.indexOf(".");
		if (dot >= 0) {
			// get field value of variable
			final String className = varName.substring(0, dot).trim();
			final Object var = variables.get(className);
			if (var == null) {
				throw new ReflectException("No such class: " + className);
			}
			final Class<?> varClass =
				var instanceof Class<?> ? (Class<?>) var : var.getClass();
			final String fieldName = varName.substring(dot + 1).trim();
			Field field;
			try {
				field = varClass.getField(fieldName);
				if (force) field.setAccessible(true);
			}
			catch (final NoSuchFieldException exc) {
				log.debug("No such field: " + varName, exc);
				throw new ReflectException("No such field: " + varName, exc);
			}
			Object fieldVal;
			try {
				fieldVal = field.get(var);
			}
			catch (final IllegalAccessException exc) {
				log.debug("Cannot get field value: " + varName, exc);
				throw new ReflectException("Cannot get field value: " + varName, exc);
			}
			return fieldVal;
		}
		// get variable
		final Object var = variables.get(varName);
		return var;
	}

	/** Sets whether access modifiers (protected, private, etc.) are ignored. */
	public void setAccessibilityIgnored(final boolean ignore) {
		force = ignore;
	}

	/** Gets whether access modifiers (protected, private, etc.) are ignored. */
	public boolean isAccessibilityIgnored() {
		return force;
	}

	// -- Main method --

	/**
	 * Allows exploration of a reflected universe in an interactive environment.
	 */
	public static void main(final String[] args) throws IOException {
		final ReflectedUniverse r = new ReflectedUniverse();
		System.out.println("Reflected universe test environment. "
			+ "Type commands, or press ^D to quit.");
		if (args.length > 0) {
			r.setAccessibilityIgnored(true);
			System.out.println("Ignoring accessibility modifiers.");
		}
		final BufferedReader in =
			new BufferedReader(new InputStreamReader(System.in, Constants.ENCODING));
		while (true) {
			System.out.print("> ");
			final String line = in.readLine();
			if (line == null) break;
			try {
				r.exec(line);
			}
			catch (final ReflectException exc) {
				r.log.debug("Could not execute '" + line + "'", exc);
			}
		}
		System.out.println();
	}

}
