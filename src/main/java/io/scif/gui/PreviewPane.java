/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2017 SCIFIO developers.
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

package io.scif.gui;

import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.services.InitializeService;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.imagej.axis.CalibratedAxis;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * PreviewPane is a panel for use as a JFileChooser accessory, displaying a
 * thumbnail for the selected image, loaded in a separate thread.
 */
public class PreviewPane extends JPanel implements PropertyChangeListener,
	Runnable
{

	// -- Parameters --

	@Parameter
	LogService logService;

	@Parameter
	InitializeService initializeService;

	// -- Fields --

	/** Reader for use when loading thumbnails. */
	private Reader reader;

	/** Current ID to load. */
	private String loadId;

	/** Last ID loaded. */
	private String lastId;

	/** Thumbnail loading thread. */
	private Thread loader;

	/** Flag indicating whether loader thread should keep running. */
	private boolean loaderAlive;

	/** Method for syncing the view to the model. */
	private Runnable refresher;

	// -- Fields - view --

	/** Labels containing thumbnail and dimensional information. */
	private final JLabel iconLabel, formatLabel, resLabel, zctLabel, typeLabel;

	// -- Fields - model --

	private ImageIcon icon;

	private String iconText, formatText, resText, npText, typeText;

	private String iconTip, formatTip, resTip, zctTip, typeTip;

	// -- Constructor --

	/** Constructs a preview pane for the given file chooser. */
	public PreviewPane(final Context context, final JFileChooser jc) {
		super();

		context.inject(this);

		// create view
		setBorder(new EmptyBorder(0, 10, 0, 10));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		iconLabel = new JLabel();
		iconLabel.setMinimumSize(new java.awt.Dimension(128, -1));
		iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(iconLabel);
		add(Box.createVerticalStrut(7));
		formatLabel = new JLabel();
		formatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(formatLabel);
		add(Box.createVerticalStrut(5));
		resLabel = new JLabel();
		resLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(resLabel);
		zctLabel = new JLabel();
		zctLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(zctLabel);
		typeLabel = new JLabel();
		typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(typeLabel);

		// smaller font for most labels
		Font font = formatLabel.getFont();
		font = font.deriveFont(font.getSize2D() - 3);
		formatLabel.setFont(font);
		resLabel.setFont(font);
		zctLabel.setFont(font);
		typeLabel.setFont(font);

		// populate model
		icon = null;
		iconText = formatText = resText = npText = typeText = "";
		iconTip = formatTip = resTip = zctTip = typeTip = null;

		if (jc != null) {
			jc.setAccessory(this);
			jc.addPropertyChangeListener(this);

			refresher = new Runnable() {

				@Override
				public void run() {
					iconLabel.setIcon(icon);
					iconLabel.setText(iconText);
					iconLabel.setToolTipText(iconTip);
					formatLabel.setText(formatText);
					formatLabel.setToolTipText(formatTip);
					resLabel.setText(resText);
					resLabel.setToolTipText(resTip);
					zctLabel.setText(npText);
					zctLabel.setToolTipText(zctTip);
					typeLabel.setText(typeText);
					typeLabel.setToolTipText(typeTip);
				}
			};

			// start separate loader thread
			loaderAlive = true;
			loader = new Thread(this, "Preview");
			loader.start();
		}
	}

	// -- Component API methods --

	/* @see java.awt.Component#getPreferredSize() */
	@Override
	public Dimension getPreferredSize() {
		final Dimension prefSize = super.getPreferredSize();
		return new Dimension(148, prefSize.height);
	}

	// -- PropertyChangeListener API methods --

	/**
	 * Property change event, to listen for when a new file is selected, or the
	 * file chooser closes.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent e) {
		final String prop = e.getPropertyName();
		if (prop.equals("JFileChooserDialogIsClosingProperty")) {
			// notify loader thread that it should stop
			loaderAlive = false;
		}

		if (!prop.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) return;

		File f = (File) e.getNewValue();
		if (f != null && (f.isDirectory() || !f.exists())) f = null;

		loadId = f == null ? null : f.getAbsolutePath();
	}

	// -- Runnable API methods --

	/** Thumbnail loading routine. */
	@Override
	public void run() {
		while (loaderAlive) {
			try {
				Thread.sleep(100);
			}
			catch (final InterruptedException exc) {
				logService.info("", exc);
			}

			try { // catch-all for unanticipated exceptions
				final String id = loadId;
				if (id == lastId) continue;
				lastId = id;

				icon = null;
				iconText = id == null ? "" : "Reading...";
				formatText = resText = npText = typeText = "";
				iconTip = id;
				formatTip = resTip = zctTip = typeTip = "";

				if (id == null) {
					SwingUtilities.invokeLater(refresher);
					continue;
				}

				try {
					reader = initializeService.initializeReader(id);
					reader.setNormalized(true);
				}
				catch (final FormatException exc) {
					logService.debug("Failed to initialize " + id, exc);
					final boolean badFormat = exc.getMessage().startsWith(
						"Unknown file format");
					iconText = "Unsupported " + (badFormat ? "format" : "file");
					formatText = resText = "";
					SwingUtilities.invokeLater(refresher);
					lastId = null;
					continue;
				}
				catch (final IOException exc) {
					logService.debug("Failed to initialize " + id, exc);
					iconText = "Unsupported file";
					formatText = resText = "";
					SwingUtilities.invokeLater(refresher);
					lastId = null;
					continue;
				}
				if (id != loadId) {
					SwingUtilities.invokeLater(refresher);
					continue;
				}

				icon = new ImageIcon(makeImage("Loading..."));
				iconText = "";
				final String format = reader.getFormat().getFormatName();
				formatText = format;
				formatTip = format;
				final ImageMetadata iMeta = reader.getMetadata().get(0);
				resText = getText(iMeta, iMeta.getAxesPlanar());
				npText = getText(iMeta, iMeta.getAxesNonPlanar());
				SwingUtilities.invokeLater(refresher);

				// open middle image thumbnail
				final long planeIndex = iMeta.getPlaneCount() / 2;
				Plane thumbPlane = null;
				try {
					thumbPlane = reader.openPlane(0, planeIndex);
				}
				catch (final FormatException exc) {
					logService.debug("Failed to read thumbnail #" + planeIndex +
						" from " + id, exc);
				}
				catch (final IOException exc) {
					logService.debug("Failed to read thumbnail #" + planeIndex +
						" from " + id, exc);
				}
				final BufferedImage thumb = AWTImageTools.openThumbImage(thumbPlane,
					reader, 0, iMeta.getAxesLengthsPlanar(), (int) iMeta.getThumbSizeX(),
					(int) iMeta.getThumbSizeY(), false);
				icon = new ImageIcon(thumb == null ? makeImage("Failed") : thumb);
				iconText = "";

				SwingUtilities.invokeLater(refresher);
			}
			catch (final Exception exc) {
				logService.info("", exc);
				icon = null;
				iconText = "Thumbnail failure";
				formatText = resText = npText = typeText = "";
				iconTip = loadId;
				formatTip = resTip = zctTip = typeTip = "";
				SwingUtilities.invokeLater(refresher);
			}
		}
	}

	private String getText(final ImageMetadata meta,
		final List<CalibratedAxis> axes)
	{
		String text = "";
		for (final CalibratedAxis axis : axes) {
			if (text.length() > 0) text += " x ";
			text += meta.getAxisLength(axis) + " " + axis.type().getLabel();
		}
		return text;
	}

	// -- PreviewPane API methods --

	/** Closes the underlying image reader. */
	public void close() throws IOException {
		if (reader != null) {
			reader.close();
		}
	}

	// -- Helper methods --

	/**
	 * Creates a blank image with the given message painted on top (e.g., a
	 * loading or error message), matching the size of the active reader's
	 * thumbnails.
	 */
	private BufferedImage makeImage(final String message) {
		final ImageMetadata iMeta = reader.getMetadata().get(0);
		int w = (int) iMeta.getThumbSizeX(), h = (int) iMeta.getThumbSizeY();
		if (w < 128) w = 128;
		if (h < 32) h = 32;
		final BufferedImage image = new BufferedImage(w, h,
			BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = image.createGraphics();
		final Rectangle2D.Float r = (Rectangle2D.Float) g.getFont().getStringBounds(
			message, g.getFontRenderContext());
		g.drawString(message, (w - r.width) / 2, (h - r.height) / 2 + r.height);
		g.dispose();
		return image;
	}

}
