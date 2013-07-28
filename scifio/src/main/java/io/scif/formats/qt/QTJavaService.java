
package io.scif.formats.qt;

import io.scif.FormatException;
import io.scif.MissingLibraryException;
import io.scif.common.ReflectException;
import io.scif.common.ReflectedUniverse;

import java.awt.Dimension;
import java.awt.Image;

import org.scijava.service.Service;

/**
 * Interface for services that work with QuickTime for Java.
 * 
 * @author Curtis Rueden
 * @author Melissa Linkert
 */
public interface QTJavaService extends Service {

	/** Whether QuickTime is available to this JVM. */
	boolean canDoQT();

	/** Whether this JVM is 64-bit. */
	boolean isJVM64Bit();

	/** Whether QuickTime for Java has expired. */
	boolean isQTExpired();

	/** Gets the QuickTime for Java version number. */
	String getQTVersion();

	/** Gets QuickTime for Java reflected universe. */
	ReflectedUniverse getUniverse();

	/** Gets width and height for the given PICT bytes. */
	Dimension getPictDimensions(byte[] bytes) throws FormatException,
		ReflectException;

	/** Converts the given byte array in PICT format to a Java image. */
	Image pictToImage(byte[] bytes) throws FormatException;

	/** Checks whether QTJava is available, throwing an exception if not. */
	void checkQTLibrary() throws MissingLibraryException;

}
