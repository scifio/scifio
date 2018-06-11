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

import io.scif.BufferedImagePlane;
import io.scif.FormatException;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.SCIFIOService;
import io.scif.Writer;
import io.scif.services.FormatService;
import io.scif.services.InitializeService;
import io.scif.util.FormatTools;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.Context;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.service.SciJavaService;

/**
 * A basic renderer for image data.
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 */
public class ImageViewer extends JFrame implements ActionListener,
	ChangeListener, KeyListener, MouseMotionListener, Runnable, WindowListener
{

	// -- Constants --

	@Parameter
	private Context context;

	@Parameter
	private LogService logService;

	@Parameter
	private FormatService formatService;

	@Parameter
	private InitializeService initializeService;

	@Parameter
	private GUIService guiService;

	private static final String TITLE = "SCIFIO Viewer";

	private static final char ANIMATION_KEY = ' ';

	// -- Fields --

	/** Current format reader. */
	private Reader myReader;

	/** Current format writer. */
	private Writer myWriter;

	private final JPanel pane;

	private final ImageIcon icon;

	private final JLabel iconLabel;

	private final JPanel sliderPanel;

	private final JSlider nSlider;

	private final JLabel probeLabel;

	private final JMenuItem fileView, fileSave;

	private String filename;

	private BufferedImage[] images;

	private boolean anim = false;

	private int fps = 10;

	private boolean canCloseReader = true;

	// -- Constructor --

	/** Constructs an image viewer. */
	public ImageViewer(final Context context) {
		super(TITLE);
		context.inject(this);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(this);

		// content pane
		pane = new JPanel();
		pane.setLayout(new BorderLayout());
		setContentPane(pane);
		setSize(350, 350); // default size

		// navigation sliders
		sliderPanel = new JPanel();
		sliderPanel.setVisible(false);
		sliderPanel.setBorder(new EmptyBorder(5, 3, 5, 3));
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
		pane.add(BorderLayout.SOUTH, sliderPanel);

		final JPanel nPanel = new JPanel();
		nPanel.setLayout(new BoxLayout(nPanel, BoxLayout.X_AXIS));
		sliderPanel.add(nPanel);
		sliderPanel.add(Box.createVerticalStrut(2));

		nSlider = new JSlider(1, 1);
		nSlider.setEnabled(false);
		nSlider.addChangeListener(this);
		nPanel.add(new JLabel("N"));
		nPanel.add(Box.createHorizontalStrut(3));
		nPanel.add(nSlider);

		final JPanel ztcPanel = new JPanel();
		ztcPanel.setLayout(new BoxLayout(ztcPanel, BoxLayout.X_AXIS));
		sliderPanel.add(ztcPanel);

		// image icon
		final BufferedImage dummy = AWTImageTools.makeImage(new byte[1][1], 1, 1,
			false);
		icon = new ImageIcon(dummy);
		iconLabel = new JLabel(icon, SwingConstants.LEFT);
		iconLabel.setVerticalAlignment(SwingConstants.TOP);
		pane.add(new JScrollPane(iconLabel));

		// cursor probe
		probeLabel = new JLabel(" ");
		probeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		probeLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
		pane.add(BorderLayout.NORTH, probeLabel);
		iconLabel.addMouseMotionListener(this);

		// menu bar
		final JMenuBar menubar = new JMenuBar();
		// FIXME: currently the menu bar is disabled to restrict the use of
		// ImageViewer to the Show command. We could attempt to get this
		// implementation working nicely, or just convert to an IJ2
		// implementation.
//		setJMenuBar(menubar);

		final JMenu file = new JMenu("File");
		file.setMnemonic('f');
		menubar.add(file);
		final JMenuItem fileOpen = new JMenuItem("Open...");
		fileOpen.setMnemonic('o');
		fileOpen.setActionCommand("open");
		fileOpen.addActionListener(this);
		file.add(fileOpen);
		fileSave = new JMenuItem("Save...");
		fileSave.setMnemonic('s');
		fileSave.setEnabled(false);
		fileSave.setActionCommand("save");
		fileSave.addActionListener(this);
		file.add(fileSave);
		fileView = new JMenuItem("View Metadata...");
		final JMenuItem fileExit = new JMenuItem("Exit");
		fileExit.setMnemonic('x');
		fileExit.setActionCommand("exit");
		fileExit.addActionListener(this);
		file.add(fileExit);

		final JMenu options = new JMenu("Options");
		options.setMnemonic('p');
		menubar.add(options);
		final JMenuItem optionsFPS = new JMenuItem("Frames per Second...");
		optionsFPS.setMnemonic('f');
		optionsFPS.setActionCommand("fps");
		optionsFPS.addActionListener(this);
		options.add(optionsFPS);

		final JMenu help = new JMenu("Help");
		help.setMnemonic('h');
		menubar.add(help);
		final JMenuItem helpAbout = new JMenuItem("About...");
		helpAbout.setMnemonic('a');
		helpAbout.setActionCommand("about");
		helpAbout.addActionListener(this);
		help.add(helpAbout);

		// add key listener to focusable components
		nSlider.addKeyListener(this);
	}

	/**
	 * Constructs an image viewer.
	 *
	 * @param canCloseReader whether or not the underlying reader can be closed
	 */
	public ImageViewer(final Context context, final boolean canCloseReader) {
		this(context);
		this.canCloseReader = canCloseReader;
	}

	/**
	 * Convenience overload of {@link #open(Location)} for backwards
	 * compatibility.
	 *
	 * @param id
	 */
	public void open(final String id) {
		open(new FileLocation(id));
	}

	/** Opens the given data source using the current format reader. */
	public void open(final Location id) {
		wait(true);
		try {
			canCloseReader = true;
			myReader = initializeService.initializeReader(id);
			final long planeCount = myReader.getMetadata().get(0).getPlaneCount();
			final ProgressMonitor progress = new ProgressMonitor(this, "Reading " +
				id, null, 0, 1);
			progress.setProgress(1);
			final BufferedImage[] img = new BufferedImage[(int) planeCount];
			for (long planeIndex = 0; planeIndex < planeCount; planeIndex++) {
				if (progress.isCanceled()) break;
				final Plane plane = myReader.openPlane(0, planeIndex);
				img[(int) planeIndex] = AWTImageTools.openImage(plane, myReader, 0);
			}
			progress.setProgress(2);
			setImages(myReader, img);
			myReader.close(true);
		}
		catch (final FormatException exc) {
			logService.info("", exc);
			wait(false);
			return;
		}
		catch (final IOException exc) {
			logService.info("", exc);
			wait(false);
			return;
		}
		wait(false);
	}

	/**
	 * Convenience overload of {@link #save}, saves the current image to the given
	 * local file destination.
	 *
	 * @param id
	 */
	public void save(final String id) {
		save(new FileLocation(id));
	}

	/**
	 * Saves the current images to the given destination using the current format
	 * writer.
	 */
	public void save(final Location id) {
		if (images == null) return;
		wait(true);
		try {
			myWriter.setDest(id);
			final boolean stack = myWriter.canDoStacks();
			final ProgressMonitor progress = new ProgressMonitor(this, "Saving " + id,
				null, 0, stack ? images.length : 1);
			if (stack) {
				// save entire stack
				for (int i = 0; i < images.length; i++) {
					progress.setProgress(i);
					final boolean canceled = progress.isCanceled();
					myWriter.savePlane(0, i, getPlane(images[i]));
					if (canceled) break;
				}
				progress.setProgress(images.length);
			}
			else {
				// save current image only
				myWriter.savePlane(0, 0, getPlane(getImage()));
				progress.setProgress(1);
			}
			myWriter.close();
		}
		catch (FormatException | IOException exc) {
			logService.info("", exc);
		}
		wait(false);
	}

	/** Sets the viewer to display the given images. */
	public void setImages(final BufferedImage[] img) {
		setImages(null, img);
	}

	/**
	 * Sets the viewer to display the given images, obtaining corresponding core
	 * metadata from the specified format reader.
	 */
	public void setImages(final Reader reader, final BufferedImage[] img) {
		filename = reader == null ? null : reader.getCurrentFile().getName();
		myReader = reader;
		images = img;

		fileView.setEnabled(true);

		fileSave.setEnabled(true);
		nSlider.removeChangeListener(this);
		nSlider.setValue(1);
		nSlider.setMaximum(images.length);
		nSlider.setEnabled(images.length > 1);
		nSlider.addChangeListener(this);
		sliderPanel.setVisible(images.length > 1);

		updateLabel(-1, -1);
		sb.setLength(0);
		if (filename != null) {
			sb.append(reader.getCurrentFile().getName());
			sb.append(" ");
		}
		final String format = reader == null ? null : reader.getFormat()
			.getFormatName();
		if (format != null) {
			sb.append("(");
			sb.append(format);
			sb.append(")");
			sb.append(" ");
		}
		if (filename != null || format != null) sb.append("- ");
		sb.append(TITLE);
		setTitle(sb.toString());
		if (images != null) icon.setImage(images[0]);
		pack();
	}

	/** Gets the currently displayed image. */
	public BufferedImage getImage() {
		final int ndx = getPlaneIndex();
		return images == null || ndx >= images.length ? null : images[ndx];
	}

	public Plane getPlane(final BufferedImage image) {
		final BufferedImagePlane plane = new BufferedImagePlane(context);
		plane.setData(image);
		return plane;
	}

	/** Gets the index of the currently displayed image. */
	public int getPlaneIndex() {
		return nSlider.getValue() - 1;
	}

	// -- Window API methods --
	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		// kick off animation thread
		new Thread(this).start();
	}

	// -- ActionListener API methods --

	/** Handles menu commands. */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final String cmd = e.getActionCommand();
		if ("open".equals(cmd)) {
			wait(true);
			final JFileChooser chooser = guiService.buildFileChooser(formatService
				.getAllFormats());
			wait(false);
			final int rval = chooser.showOpenDialog(this);
			if (rval == JFileChooser.APPROVE_OPTION) {
				final File file = chooser.getSelectedFile();
				if (file != null) open(file.getAbsolutePath(), myReader);
			}
		}
		else if ("save".equals(cmd)) {
			wait(true);
			final JFileChooser chooser = guiService.buildFileChooser(formatService
				.getOutputFormats());
			wait(false);
			final int rval = chooser.showSaveDialog(this);
			if (rval == JFileChooser.APPROVE_OPTION) {
				if (myWriter != null) {
					try {
						myWriter.close();
					}
					catch (final IOException e1) {
						logService.error(e1);
					}
				}
				final File file = chooser.getSelectedFile();
				try {
					myWriter = initializeService.initializeWriter(myReader.getMetadata(),
						new FileLocation(file));
				}
				catch (FormatException | IOException e1) {
					logService.error(e);
				}
				if (file != null) save(file.getAbsolutePath(), myWriter);
			}
		}
		else if ("exit".equals(cmd)) dispose();
		else if ("fps".equals(cmd)) {
			// HACK - JOptionPane prevents shutdown on dispose
			setDefaultCloseOperation(EXIT_ON_CLOSE);

			final String result = JOptionPane.showInputDialog(this,
				"Animate using space bar. How many frames per second?", "" + fps);
			try {
				fps = Integer.parseInt(result);
			}
			catch (final NumberFormatException exc) {
				logService.debug("Could not parse fps " + fps, exc);
			}
		}
		else if ("about".equals(cmd)) {
			// HACK - JOptionPane prevents shutdown on dispose
			setDefaultCloseOperation(EXIT_ON_CLOSE);

			final String msg = "<html>" + "SCIFIO core for reading and " +
				"converting file formats." + "<br>Copyright (C) 2005 - 2013" +
				" Open Microscopy Environment:" + "<ul>" +
				"<li>Board of Regents of the University of Wisconsin-Madison</li>" +
				"<li>Glencoe Software, Inc.</li>" + "<li>University of Dundee</li>" +
				"</ul>" + "<br><br>See <a href=\"" +
				"http://loci.wisc.edu/software/scifio\">" +
				"http://loci.wisc.edu/software/scifio</a>" +
				"<br>for help with using SCIFIO.";
			JOptionPane.showMessageDialog(null, msg, "SCIFIO",
				JOptionPane.INFORMATION_MESSAGE);
		}
	}

	// -- ChangeListener API methods --

	/** Handles slider events. */
	@Override
	public void stateChanged(final ChangeEvent e) {
		final boolean outOfBounds = false;
		updateLabel(-1, -1);
		final BufferedImage image = outOfBounds ? null : getImage();
		if (image == null) {
			iconLabel.setIcon(null);
			iconLabel.setText("No image plane");
		}
		else {
			icon.setImage(image);
			iconLabel.setIcon(icon);
			iconLabel.setText(null);
		}
	}

	// -- KeyListener API methods --

	/** Handles key presses. */
	@Override
	public void keyPressed(final KeyEvent e) {
		if (e.getKeyChar() == ANIMATION_KEY) anim = !anim; // toggle animation
	}

	@Override
	public void keyReleased(final KeyEvent e) {}

	@Override
	public void keyTyped(final KeyEvent e) {}

	// -- MouseMotionListener API methods --

	/** Handles cursor probes. */
	@Override
	public void mouseDragged(final MouseEvent e) {
		updateLabel(e.getX(), e.getY());
	}

	/** Handles cursor probes. */
	@Override
	public void mouseMoved(final MouseEvent e) {
		updateLabel(e.getX(), e.getY());
	}

	// -- Runnable API methods --

	/** Handles animation. */
	@Override
	public void run() {
		while (isVisible()) {
			try {
				Thread.sleep(1000 / fps);
			}
			catch (final InterruptedException exc) {
				logService.debug("", exc);
			}
		}
	}

	// -- WindowListener API methods --

	@Override
	public void windowClosing(final WindowEvent e) {}

	@Override
	public void windowActivated(final WindowEvent e) {}

	@Override
	public void windowDeactivated(final WindowEvent e) {}

	@Override
	public void windowOpened(final WindowEvent e) {}

	@Override
	public void windowIconified(final WindowEvent e) {}

	@Override
	public void windowDeiconified(final WindowEvent e) {}

	@Override
	public void windowClosed(final WindowEvent e) {
		try {
			if (myWriter != null) {
				myWriter.close();
			}
			if (canCloseReader && myReader != null) {
				myReader.close();
			}
		}
		catch (final IOException io) {}
	}

	// -- Helper methods --

	private final StringBuffer sb = new StringBuffer();

	/** Updates cursor probe label. */
	protected void updateLabel(int x, int y) {
		if (images == null) return;
		final int ndx = getPlaneIndex();
		sb.setLength(0);
		if (images.length > 1) {
			sb.append("N=");
			sb.append(ndx + 1);
			sb.append("/");
			sb.append(images.length);
		}
		final BufferedImage image = images[ndx];
		final int w = image == null ? -1 : image.getWidth();
		final int h = image == null ? -1 : image.getHeight();
		if (x >= w) x = w - 1;
		if (y >= h) y = h - 1;
		if (x >= 0 && y >= 0) {
			if (images.length > 1) sb.append("; ");
			sb.append("X=");
			sb.append(x);
			if (w > 0) {
				sb.append("/");
				sb.append(w);
			}
			sb.append("; Y=");
			sb.append(y);
			if (h > 0) {
				sb.append("/");
				sb.append(h);
			}
			if (image != null) {
				final Raster r = image.getRaster();
				final double[] pix = r.getPixel(x, y, (double[]) null);
				sb.append("; value");
				sb.append(pix.length > 1 ? "s=(" : "=");
				for (int i = 0; i < pix.length; i++) {
					if (i > 0) sb.append(", ");
					sb.append(pix[i]);
				}
				if (pix.length > 1) sb.append(")");
				sb.append("; type=");
				final int pixelType = AWTImageTools.getPixelType(image);
				sb.append(FormatTools.getPixelTypeString(pixelType));
			}
		}
		sb.append(" ");
		probeLabel.setText(sb.toString());
	}

	/** Toggles wait cursor. */
	protected void wait(final boolean wait) {
		setCursor(wait ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : null);
	}

	/**
	 * Opens from the given data source using the specified reader in a separate
	 * thread.
	 */
	protected void open(final String id, final Reader r) {
		new Thread("ImageViewer-Opener") {

			@Override
			public void run() {
				try {
					myReader.close();
				}
				catch (final IOException exc) {
					logService.info("", exc);
				}
				myReader = r;
				open(id);
			}
		}.start();
	}

	/**
	 * Saves to the given data destination using the specified writer in a
	 * separate thread.
	 */
	protected void save(final String id, final Writer w) {
		new Thread("ImageViewer-Saver") {

			@Override
			public void run() {
				try {
					myWriter.close();
				}
				catch (final IOException exc) {
					logService.info("", exc);
				}
				myWriter = w;
				save(id);
			}
		}.start();
	}

	// -- Main method --

	public static void main(final String[] args) {
		final ImageViewer viewer = new ImageViewer(new Context(SCIFIOService.class,
			SciJavaService.class));
		viewer.setVisible(true);
		if (args.length > 0) viewer.open(args[0]);
	}

}
