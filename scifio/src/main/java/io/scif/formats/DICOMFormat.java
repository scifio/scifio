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

package io.scif.formats;

import io.scif.AbstractChecker;
import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.FilePattern;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.MetadataLevel;
import io.scif.UnsupportedCompressionException;
import io.scif.codec.Codec;
import io.scif.codec.CodecOptions;
import io.scif.codec.JPEG2000Codec;
import io.scif.codec.JPEGCodec;
import io.scif.codec.PackbitsCodec;
import io.scif.common.DataTools;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.services.InitializeService;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * DICOMReader is the file format reader for DICOM files. Much of this code is
 * adapted from ImageJ's DICOM reader; see
 * http://rsb.info.nih.gov/ij/developer/source/ij/plugin/DICOM.java.html
 * 
 * @author Mark Hiner
 */
@Plugin(type = Format.class)
public class DICOMFormat extends AbstractFormat {

	// -- Constants --

	public static final String DICOM_MAGIC_STRING = "DICM";

	private static final Hashtable<Integer, String> TYPES = buildTypes();

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "DICOM";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "dic", "dcm", "dicom", "jp2", "j2ki", "j2kr", "raw",
			"ima" };
	}

	// -- Static Helper methods --

	/**
	 * Assemble the data dictionary. This is incomplete at best, since there are
	 * literally thousands of fields defined by the DICOM specifications.
	 */
	private static Hashtable<Integer, String> buildTypes() {
		final Hashtable<Integer, String> dict = new Hashtable<Integer, String>();

		dict.put(0x00020002, "Media Storage SOP Class UID");
		dict.put(0x00020003, "Media Storage SOP Instance UID");
		dict.put(0x00020010, "Transfer Syntax UID");
		dict.put(0x00020012, "Implementation Class UID");
		dict.put(0x00020013, "Implementation Version Name");
		dict.put(0x00020016, "Source Application Entity Title");
		dict.put(0x00080005, "Specific Character Set");
		dict.put(0x00080008, "Image Type");
		dict.put(0x00080010, "Recognition Code");
		dict.put(0x00080012, "Instance Creation Date");
		dict.put(0x00080013, "Instance Creation Time");
		dict.put(0x00080014, "Instance Creator UID");
		dict.put(0x00080016, "SOP Class UID");
		dict.put(0x00080018, "SOP Instance UID");
		dict.put(0x0008001a, "Related General SOP Class UID");
		dict.put(0x0008001b, "Original Specialized SOP Class UID");
		dict.put(0x00080020, "Study Date");
		dict.put(0x00080021, "Series Date");
		dict.put(0x00080022, "Acquisition Date");
		dict.put(0x00080023, "Content Date");
		dict.put(0x00080024, "Overlay Date");
		dict.put(0x00080025, "Curve Date");
		dict.put(0x0008002a, "Acquisition Date/Time");
		dict.put(0x00080030, "Study Time");
		dict.put(0x00080031, "Series Time");
		dict.put(0x00080032, "Acquisition Time");
		dict.put(0x00080033, "Content Time");
		dict.put(0x00080034, "Overlay Time");
		dict.put(0x00080035, "Curve Time");
		dict.put(0x00080041, "Data Set Subtype");
		dict.put(0x00080050, "Accession Number");
		dict.put(0x00080052, "Query/Retrieve Level");
		dict.put(0x00080054, "Retrieve AE Title");
		dict.put(0x00080056, "Instance Availability");
		dict.put(0x00080058, "Failed SOP Instance UID List");
		dict.put(0x00080060, "Modality");
		dict.put(0x00080061, "Modalities in Study");
		dict.put(0x00080062, "SOP Classes in Study");
		dict.put(0x00080064, "Conversion Type");
		dict.put(0x00080068, "Presentation Intent Type");
		dict.put(0x00080070, "Manufacturer");
		dict.put(0x00080080, "Institution Name");
		dict.put(0x00080081, "Institution Address");
		dict.put(0x00080082, "Institution Code Sequence");
		dict.put(0x00080090, "Referring Physician's Name");
		dict.put(0x00080092, "Referring Physician's Address");
		dict.put(0x00080094, "Referring Physician's Telephone");
		dict.put(0x00080096, "Referring Physician ID");
		dict.put(0x00080100, "Code Value");
		dict.put(0x00080102, "Coding Scheme Designator");
		dict.put(0x00080103, "Coding Scheme Version");
		dict.put(0x00080104, "Code Meaning");
		dict.put(0x00080105, "Mapping Resource");
		dict.put(0x00080106, "Context Group Version");
		dict.put(0x00080107, "Context Group Local Version");
		dict.put(0x0008010b, "Context Group Extension Flag");
		dict.put(0x0008010c, "Coding Scheme UID");
		dict.put(0x0008010d, "Context Group Extension Creator UID");
		dict.put(0x0008010f, "Context ID");
		dict.put(0x00080110, "Coding Scheme ID");
		dict.put(0x00080112, "Coding Scheme Registry");
		dict.put(0x00080114, "Coding Scheme External ID");
		dict.put(0x00080115, "Coding Scheme Name");
		dict.put(0x00080116, "Responsible Organization");
		dict.put(0x00080201, "Timezone Offset from UTC");
		dict.put(0x00081010, "Station Name");
		dict.put(0x00081030, "Study Description");
		dict.put(0x00081032, "Procedure Code Sequence");
		dict.put(0x0008103e, "Series Description");
		dict.put(0x00081040, "Institutional Department Name");
		dict.put(0x00081048, "Physician(s) of Record");
		dict.put(0x00081049, "Physician(s) of Record ID");
		dict.put(0x00081050, "Performing Physician's Name");
		dict.put(0x00081052, "Performing Physican ID");
		dict.put(0x00081060, "Name of Physician(s) Reading Study");
		dict.put(0x00081062, "Physician(s) Reading Study ID");
		dict.put(0x00081070, "Operator's Name");
		dict.put(0x00081072, "Operator ID");
		dict.put(0x00081080, "Admitting Diagnoses Description");
		dict.put(0x00081084, "Admitting Diagnoses Code Sequence");
		dict.put(0x00081090, "Manufacturer's Model Name");
		dict.put(0x00081100, "Referenced Results Sequence");
		dict.put(0x00081110, "Referenced Study Sequence");
		dict.put(0x00081111, "Referenced Performed Procedure Step");
		dict.put(0x00081115, "Referenced Series Sequence");
		dict.put(0x00081120, "Referenced Patient Sequence");
		dict.put(0x00081125, "Referenced Visit Sequence");
		dict.put(0x00081130, "Referenced Overlay Sequence");
		dict.put(0x0008113a, "Referenced Waveform Sequence");
		dict.put(0x00081140, "Referenced Image Sequence");
		dict.put(0x00081145, "Referenced Curve Sequence");
		dict.put(0x0008114a, "Referenced Instance Sequence");
		dict.put(0x00081150, "Referenced SOP Class UID");
		dict.put(0x00081155, "Referenced SOP Instance UID");
		dict.put(0x0008115a, "SOP Classes Supported");
		dict.put(0x00081160, "Referenced Frame Number");
		dict.put(0x00081195, "Transaction UID");
		dict.put(0x00081197, "Failure Reason");
		dict.put(0x00081198, "Failed SOP Sequence");
		dict.put(0x00081199, "Referenced SOP Sequence");
		dict.put(0x00081200,
			"Studies Containing Other Referenced Instances Sequence");
		dict.put(0x00081250, "Related Series Sequence");
		dict.put(0x00082111, "Derivation Description");
		dict.put(0x00082112, "Source Image Sequence");
		dict.put(0x00082120, "Stage Name");
		dict.put(0x00082122, "Stage Number");
		dict.put(0x00082124, "Number of Stages");
		dict.put(0x00082127, "View Name");
		dict.put(0x00082128, "View Number");
		dict.put(0x00082129, "Number of Event Timers");
		dict.put(0x0008212a, "Number of Views in Stage");
		dict.put(0x00082130, "Event Elapsed Time(s)");
		dict.put(0x00082132, "Event Timer Name(s)");
		dict.put(0x00082142, "Start Trim");
		dict.put(0x00082143, "Stop Trim");
		dict.put(0x00082144, "Recommended Display Frame Rate");
		dict.put(0x00082218, "Anatomic Region Sequence");
		dict.put(0x00082220, "Anatomic Region Modifier Sequence");
		dict.put(0x00082228, "Primary Anatomic Structure Sequence");
		dict.put(0x00082229, "Anatomic Structure Sequence");
		dict.put(0x00082230, "Primary Anatomic Structure Modifier");
		dict.put(0x00082240, "Transducer Position Sequence");
		dict.put(0x00082242, "Transducer Position Modifier Sequence");
		dict.put(0x00082244, "Transducer Orientation Sequence");
		dict.put(0x00082246, "Transducer Orientation Modifier");
		dict.put(0x00083001, "Alternate Representation Sequence");
		dict.put(0x00089007, "Frame Type");
		dict.put(0x00089092, "Referenced Image Evidence Sequence");
		dict.put(0x00089121, "Referenced Raw Data Sequence");
		dict.put(0x00089123, "Creator-Version UID");
		dict.put(0x00089124, "Derivation Image Sequence");
		dict.put(0x00089154, "Source Image Evidence Sequence");
		dict.put(0x00089205, "Pixel Representation");
		dict.put(0x00089206, "Volumetric Properties");
		dict.put(0x00089207, "Volume Based Calculation Technique");
		dict.put(0x00089208, "Complex Image Component");
		dict.put(0x00089209, "Acquisition Contrast");
		dict.put(0x00089215, "Derivation Code Sequence");
		dict.put(0x00089237, "Reference Grayscale Presentation State");
		dict.put(0x00100010, "Patient's Name");
		dict.put(0x00100020, "Patient ID");
		dict.put(0x00100021, "Issuer of Patient ID");
		dict.put(0x00100030, "Patient's Birth Date");
		dict.put(0x00100032, "Patient's Birth Time");
		dict.put(0x00100040, "Patient's Sex");
		dict.put(0x00100050, "Patient's Insurance Plane Code");
		dict.put(0x00100101, "Patient's Primary Language Code");
		dict.put(0x00100102, "Patient's Primary Language Modifier");
		dict.put(0x00101000, "Other Patient IDs");
		dict.put(0x00101001, "Other Patient Names");
		dict.put(0x00101005, "Patient's Birth Name");
		dict.put(0x00101010, "Patient's Age");
		dict.put(0x00101020, "Patient's Size");
		dict.put(0x00101030, "Patient's Weight");
		dict.put(0x00101040, "Patient's Address");
		dict.put(0x00101060, "Patient's Mother's Birth Name");
		dict.put(0x00101080, "Military Rank");
		dict.put(0x00101081, "Branch of Service");
		dict.put(0x00101090, "Medical Record Locator");
		dict.put(0x00102000, "Medical Alerts");
		dict.put(0x00102110, "Contrast Allergies");
		dict.put(0x00102150, "Country of Residence");
		dict.put(0x00102152, "Region of Residence");
		dict.put(0x00102154, "Patient's Telephone Numbers");
		dict.put(0x00102160, "Ethnic Group");
		dict.put(0x00102180, "Occupation");
		dict.put(0x001021a0, "Smoking Status");
		dict.put(0x001021b0, "Additional Patient History");
		dict.put(0x001021c0, "Pregnancy Status");
		dict.put(0x001021d0, "Last Menstrual Date");
		dict.put(0x001021f0, "Patient's Religious Preference");
		dict.put(0x00104000, "Patient Comments");
		dict.put(0x00120010, "Clinical Trial Sponsor Name");
		dict.put(0x00120020, "Clinical Trial Protocol ID");
		dict.put(0x00120021, "Clinical Trial Protocol Name");
		dict.put(0x00120030, "Clinical Trial Site ID");
		dict.put(0x00120031, "Clinical Trial Site Name");
		dict.put(0x00120040, "Clinical Trial Subject ID");
		dict.put(0x00120042, "Clinical Trial Subject Reading ID");
		dict.put(0x00120050, "Clinical Trial Time Point ID");
		dict.put(0x00120051, "Clinical Trial Time Point Description");
		dict.put(0x00120060, "Clinical Trial Coordinating Center");
		dict.put(0x00180010, "Contrast/Bolus Agent");
		dict.put(0x00180012, "Contrast/Bolus Agent Sequence");
		dict.put(0x00180014, "Contrast/Bolus Admin. Route Sequence");
		dict.put(0x00180015, "Body Part Examined");
		dict.put(0x00180020, "Scanning Sequence");
		dict.put(0x00180021, "Sequence Variant");
		dict.put(0x00180022, "Scan Options");
		dict.put(0x00180023, "MR Acquisition Type");
		dict.put(0x00180024, "Sequence Name");
		dict.put(0x00180025, "Angio Flag");
		dict.put(0x00180026, "Intervention Drug Information Sequence");
		dict.put(0x00180027, "Intervention Drug Stop Time");
		dict.put(0x00180028, "Intervention Drug Dose");
		dict.put(0x00180029, "Intervention Drug Sequence");
		dict.put(0x0018002a, "Additional Drug Sequence");
		dict.put(0x00180031, "Radiopharmaceutical");
		dict.put(0x00180034, "Intervention Drug Name");
		dict.put(0x00180035, "Intervention Drug Start Time");
		dict.put(0x00180036, "Intervention Sequence");
		dict.put(0x00180038, "Intervention Status");
		dict.put(0x0018003a, "Intervention Description");
		dict.put(0x00180040, "Cine Rate");
		dict.put(0x00180050, "Slice Thickness");
		dict.put(0x00180060, "KVP");
		dict.put(0x00180070, "Counts Accumulated");
		dict.put(0x00180071, "Acquisition Termination Condition");
		dict.put(0x00180072, "Effective Duration");
		dict.put(0x00180073, "Acquisition Start Condition");
		dict.put(0x00180074, "Acquisition Start Condition Data");
		dict.put(0x00180075, "Acquisition Termination Condition Data");
		dict.put(0x00180080, "Repetition Time");
		dict.put(0x00180081, "Echo Time");
		dict.put(0x00180082, "Inversion Time");
		dict.put(0x00180083, "Number of Averages");
		dict.put(0x00180084, "Imaging Frequency");
		dict.put(0x00180085, "Imaged Nucleus");
		dict.put(0x00180086, "Echo Number(s)");
		dict.put(0x00180087, "Magnetic Field Strength");
		dict.put(0x00180088, "Spacing Between Slices");
		dict.put(0x00180089, "Number of Phase Encoding Steps");
		dict.put(0x00180090, "Data Collection Diameter");
		dict.put(0x00180091, "Echo Train Length");
		dict.put(0x00180093, "Percent Sampling");
		dict.put(0x00180094, "Percent Phase Field of View");
		dict.put(0x00180095, "Pixel Bandwidth");
		dict.put(0x00181000, "Device Serial Number");
		dict.put(0x00181004, "Plate ID");
		dict.put(0x00181010, "Secondary Capture Device ID");
		dict.put(0x00181011, "Hardcopy Creation Device ID");
		dict.put(0x00181012, "Date of Secondary Capture");
		dict.put(0x00181014, "Time of Secondary Capture");
		dict.put(0x00181016, "Secondary Capture Device Manufacturer");
		dict.put(0x00181017, "Hardcopy Device Manufacturer");
		dict.put(0x00181018, "Secondary Capture Device Model Name");
		dict.put(0x00181019, "Secondary Capture Device Software Version");
		dict.put(0x0018101a, "Hardcopy Device Software Version");
		dict.put(0x0018101b, "Hardcopy Device Model Name");
		dict.put(0x00181020, "Software Version(s)");
		dict.put(0x00181022, "Video Image Format Acquired");
		dict.put(0x00181023, "Digital Image Format Acquired");
		dict.put(0x00181030, "Protocol Name");
		dict.put(0x00181040, "Contrast/Bolus Route");
		dict.put(0x00181041, "Contrast/Bolus Volume");
		dict.put(0x00181042, "Contrast/Bolus Start Time");
		dict.put(0x00181043, "Contrast/Bolus Stop Time");
		dict.put(0x00181044, "Contrast/Bolus Total Dose");
		dict.put(0x00181045, "Syringe Counts");
		dict.put(0x00181046, "Contrast Flow Rate");
		dict.put(0x00181047, "Contrast Flow Duration");
		dict.put(0x00181048, "Contrast/Bolus Ingredient");
		dict.put(0x00181049, "Contrast Ingredient Concentration");
		dict.put(0x00181050, "Spatial Resolution");
		dict.put(0x00181060, "Trigger Time");
		dict.put(0x00181061, "Trigger Source or Type");
		dict.put(0x00181062, "Nominal Interval");
		dict.put(0x00181063, "Frame Time");
		dict.put(0x00181064, "Framing Type");
		dict.put(0x00181065, "Frame Time Vector");
		dict.put(0x00181066, "Frame Delay");
		dict.put(0x00181067, "Image Trigger Delay");
		dict.put(0x00181068, "Multiplex Group Time Offset");
		dict.put(0x00181069, "Trigger Time Offset");
		dict.put(0x0018106a, "Synchronization Trigger");
		dict.put(0x0018106c, "Synchronization Channel");
		dict.put(0x0018106e, "Trigger Sample Position");
		dict.put(0x00181070, "Radiopharmaceutical Route");
		dict.put(0x00181071, "Radiopharmaceutical Volume");
		dict.put(0x00181072, "Radiopharmaceutical Start Time");
		dict.put(0x00181073, "Radiopharmaceutical Stop Time");
		dict.put(0x00181074, "Radionuclide Total Dose");
		dict.put(0x00181075, "Radionuclide Half Life");
		dict.put(0x00181076, "Radionuclide Positron Fraction");
		dict.put(0x00181077, "Radiopharmaceutical Specific Activity");
		dict.put(0x00181080, "Beat Rejection Flag");
		dict.put(0x00181081, "Low R-R Value");
		dict.put(0x00181082, "High R-R Value");
		dict.put(0x00181083, "Intervals Acquired");
		dict.put(0x00181084, "Intervals Rejected");
		dict.put(0x00181085, "PVC Rejection");
		dict.put(0x00181086, "Skip Beats");
		dict.put(0x00181088, "Heart Rate");
		dict.put(0x00181090, "Cardiac Number of Images");
		dict.put(0x00181094, "Trigger Window");
		dict.put(0x00181100, "Reconstruction Diameter");
		dict.put(0x00181110, "Distance Source to Detector");
		dict.put(0x00181111, "Distance Source to Patient");
		dict.put(0x00181114, "Estimated Radiographic Mag. Factor");
		dict.put(0x00181120, "Gantry/Detector Tilt");
		dict.put(0x00181121, "Gantry/Detector Skew");
		dict.put(0x00181130, "Table Height");
		dict.put(0x00181131, "Table Traverse");
		dict.put(0x00181134, "Table Motion");
		dict.put(0x00181135, "Table Vertical Increment");
		dict.put(0x00181136, "Table Lateral Increment");
		dict.put(0x00181137, "Table Longitudinal Increment");
		dict.put(0x00181138, "Table Angle");
		dict.put(0x0018113a, "Table Type");
		dict.put(0x00181140, "Rotation Direction");
		dict.put(0x00181141, "Angular Position");
		dict.put(0x00181142, "Radial Position");
		dict.put(0x00181143, "Scan Arc");
		dict.put(0x00181144, "Angular Step");
		dict.put(0x00181145, "Center of Rotation Offset");
		dict.put(0x00181147, "Field of View Shape");
		dict.put(0x00181149, "Field of View Dimension(s)");
		dict.put(0x00181150, "Exposure Time");
		dict.put(0x00181151, "X-ray Tube Current");
		dict.put(0x00181152, "Exposure");
		dict.put(0x00181153, "Exposure in uAs");
		dict.put(0x00181154, "Average Pulse Width");
		dict.put(0x00181155, "Radiation Setting");
		dict.put(0x00181156, "Rectification Type");
		dict.put(0x0018115a, "Radiation Mode");
		dict.put(0x0018115e, "Image Area Dose Product");
		dict.put(0x00181160, "Filter Type");
		dict.put(0x00181161, "Type of Filters");
		dict.put(0x00181162, "Intensifier Size");
		dict.put(0x00181164, "Imager Pixel Spacing");
		dict.put(0x00181166, "Grid");
		dict.put(0x00181170, "Generator Power");
		dict.put(0x00181180, "Collimator/Grid Name");
		dict.put(0x00181181, "Collimator Type");
		dict.put(0x00181182, "Focal Distance");
		dict.put(0x00181183, "X Focus Center");
		dict.put(0x00181184, "Y Focus Center");
		dict.put(0x00181190, "Focal Spot(s)");
		dict.put(0x00181191, "Anode Target Material");
		dict.put(0x001811a0, "Body Part Thickness");
		dict.put(0x001811a2, "Compression Force");
		dict.put(0x00181200, "Date of Last Calibration");
		dict.put(0x00181201, "Time of Last Calibration");
		dict.put(0x00181210, "Convolution Kernel");
		dict.put(0x00181242, "Actual Frame Duration");
		dict.put(0x00181243, "Count Rate");
		dict.put(0x00181244, "Preferred Playback Sequencing");
		dict.put(0x00181250, "Receive Coil Name");
		dict.put(0x00181251, "Transmit Coil Name");
		dict.put(0x00181260, "Plate Type");
		dict.put(0x00181261, "Phosphor Type");
		dict.put(0x00181300, "Scan Velocity");
		dict.put(0x00181301, "Whole Body Technique");
		dict.put(0x00181302, "Scan Length");
		dict.put(0x00181310, "Acquisition Matrix");
		dict.put(0x00181312, "In-plane Phase Encoding Direction");
		dict.put(0x00181314, "Flip Angle");
		dict.put(0x00181315, "Variable Flip Angle Flag");
		dict.put(0x00181316, "SAR");
		dict.put(0x00181318, "dB/dt");
		dict.put(0x00181400, "Acquisition Device Processing Descr.");
		dict.put(0x00181401, "Acquisition Device Processing Code");
		dict.put(0x00181402, "Cassette Orientation");
		dict.put(0x00181403, "Cassette Size");
		dict.put(0x00181404, "Exposures on Plate");
		dict.put(0x00181405, "Relative X-ray Exposure");
		dict.put(0x00181450, "Column Angulation");
		dict.put(0x00181460, "Tomo Layer Height");
		dict.put(0x00181470, "Tomo Angle");
		dict.put(0x00181480, "Tomo Time");
		dict.put(0x00181490, "Tomo Type");
		dict.put(0x00181491, "Tomo Class");
		dict.put(0x00181495, "Number of Tomosynthesis Source Images");
		dict.put(0x00181500, "Positioner Motion");
		dict.put(0x00181508, "Positioner Type");
		dict.put(0x00181510, "Positioner Primary Angle");
		dict.put(0x00181511, "Positioner Secondary Angle");
		dict.put(0x00181520, "Positioner Primary Angle Increment");
		dict.put(0x00181521, "Positioner Secondary Angle Increment");
		dict.put(0x00181530, "Detector Primary Angle");
		dict.put(0x00181531, "Detector Secondary Angle");
		dict.put(0x00181600, "Shutter Shape");
		dict.put(0x00181602, "Shutter Left Vertical Edge");
		dict.put(0x00181604, "Shutter Right Vertical Edge");
		dict.put(0x00181606, "Shutter Upper Horizontal Edge");
		dict.put(0x00181608, "Shutter Lower Horizontal Edge");
		dict.put(0x00181610, "Center of Circular Shutter");
		dict.put(0x00181612, "Radius of Circular Shutter");
		dict.put(0x00181620, "Vertices of the Polygonal Shutter");
		dict.put(0x00181622, "Shutter Presentation Value");
		dict.put(0x00181623, "Shutter Overlay Group");
		dict.put(0x00181700, "Collimator Shape");
		dict.put(0x00181702, "Collimator Left Vertical Edge");
		dict.put(0x00181704, "Collimator Right Vertical Edge");
		dict.put(0x00181706, "Collimator Upper Horizontal Edge");
		dict.put(0x00181708, "Collimator Lower Horizontal Edge");
		dict.put(0x00181710, "Center of Circular Collimator");
		dict.put(0x00181712, "Radius of Circular Collimator");
		dict.put(0x00181720, "Vertices of the polygonal Collimator");
		dict.put(0x00181800, "Acquisition Time Synchronized");
		dict.put(0x00181801, "Time Source");
		dict.put(0x00181802, "Time Distribution Protocol");
		dict.put(0x00181803, "NTP Source Address");
		dict.put(0x00182001, "Page Number Vector");
		dict.put(0x00182002, "Frame Label Vector");
		dict.put(0x00182003, "Frame Primary Angle Vector");
		dict.put(0x00182004, "Frame Secondary Angle Vector");
		dict.put(0x00182005, "Slice Location Vector");
		dict.put(0x00182006, "Display Window Label Vector");
		dict.put(0x00182010, "Nominal Scanned Pixel Spacing");
		dict.put(0x00182020, "Digitizing Device Transport Direction");
		dict.put(0x00182030, "Rotation of Scanned Film");
		dict.put(0x00183100, "IVUS Acquisition");
		dict.put(0x00183101, "IVUS Pullback Rate");
		dict.put(0x00183102, "IVUS Gated Rate");
		dict.put(0x00183103, "IVUS Pullback Start Frame Number");
		dict.put(0x00183104, "IVUS Pullback Stop Frame Number");
		dict.put(0x00183105, "Lesion Number");
		dict.put(0x00185000, "Output Power");
		dict.put(0x00185010, "Transducer Data");
		dict.put(0x00185012, "Focus Depth");
		dict.put(0x00185020, "Processing Function");
		dict.put(0x00185021, "Postprocessing Fuction");
		dict.put(0x00185022, "Mechanical Index");
		dict.put(0x00185024, "Bone Thermal Index");
		dict.put(0x00185026, "Cranial Thermal Index");
		dict.put(0x00185027, "Soft Tissue Thermal Index");
		dict.put(0x00185028, "Soft Tissue-focus Thermal Index");
		dict.put(0x00185029, "Soft Tissue-surface Thermal Index");
		dict.put(0x00185050, "Depth of scan field");
		dict.put(0x00185100, "Patient Position");
		dict.put(0x00185101, "View Position");
		dict.put(0x00185104, "Projection Eponymous Name Code");
		dict.put(0x00186000, "Sensitivity");
		dict.put(0x00186011, "Sequence of Ultrasound Regions");
		dict.put(0x00186012, "Region Spatial Format");
		dict.put(0x00186014, "Region Data Type");
		dict.put(0x00186016, "Region Flags");
		dict.put(0x00186018, "Region Location Min X0");
		dict.put(0x0018601a, "Region Location Min Y0");
		dict.put(0x0018601c, "Region Location Max X1");
		dict.put(0x0018601e, "Region Location Max Y1");
		dict.put(0x00186020, "Reference Pixel X0");
		dict.put(0x00186022, "Reference Pixel Y0");
		dict.put(0x00186024, "Physical Units X Direction");
		dict.put(0x00186026, "Physical Units Y Direction");
		dict.put(0x00186028, "Reference Pixel Physical Value X");
		dict.put(0x0018602a, "Reference Pixel Physical Value Y");
		dict.put(0x0018602c, "Physical Delta X");
		dict.put(0x0018602e, "Physical Delta Y");
		dict.put(0x00186030, "Transducer Frequency");
		dict.put(0x00186031, "Transducer Type");
		dict.put(0x00186032, "Pulse Repetition Frequency");
		dict.put(0x00186034, "Doppler Correction Angle");
		dict.put(0x00186036, "Steering Angle");
		dict.put(0x00186039, "Doppler Sample Volume X Position");
		dict.put(0x0018603b, "Doppler Sample Volume Y Position");
		dict.put(0x0018603d, "TM-Line Position X0");
		dict.put(0x0018603f, "TM-Line Position Y0");
		dict.put(0x00186041, "TM-Line Position X1");
		dict.put(0x00186043, "TM-Line Position Y1");
		dict.put(0x00186044, "Pixel Component Organization");
		dict.put(0x00186046, "Pixel Component Mask");
		dict.put(0x00186048, "Pixel Component Range Start");
		dict.put(0x0018604a, "Pixel Component Range Stop");
		dict.put(0x0018604c, "Pixel Component Physical Units");
		dict.put(0x0018604e, "Pixel Component Data Type");
		dict.put(0x00186050, "Number of Table Break Points");
		dict.put(0x00186052, "Table of X Break Points");
		dict.put(0x00186054, "Table of Y Break Points");
		dict.put(0x00186056, "Number of Table Entries");
		dict.put(0x00186058, "Table of Pixel Values");
		dict.put(0x0018605a, "Table of Parameter Values");
		dict.put(0x00186060, "R Wave Time Vector");
		dict.put(0x00187000, "Detector Conditions Nominal Flag");
		dict.put(0x00187001, "Detector Temperature");
		dict.put(0x00187004, "Detector Type");
		dict.put(0x00187005, "Detector Configuration");
		dict.put(0x00187006, "Detector Description");
		dict.put(0x00187008, "Detector Mode");
		dict.put(0x0018700a, "Detector ID");
		dict.put(0x0018700c, "Date of Last Detector Calibration");
		dict.put(0x0018700e, "Time of Last Detector Calibration");
		dict.put(0x00187012, "Detector Time Since Last Exposure");
		dict.put(0x00187014, "Detector Active Time");
		dict.put(0x00187016, "Detector Activation Offset");
		dict.put(0x0018701a, "Detector Binning");
		dict.put(0x00187020, "Detector Element Physical Size");
		dict.put(0x00187022, "Detector Element Spacing");
		dict.put(0x00187024, "Detector Active Shape");
		dict.put(0x00187026, "Detector Active Dimension(s)");
		dict.put(0x00187028, "Detector Active Origin");
		dict.put(0x0018702a, "Detector Manufacturer Name");
		dict.put(0x0018702b, "Detector Model Name");
		dict.put(0x00187030, "Field of View Origin");
		dict.put(0x00187032, "Field of View Rotation");
		dict.put(0x00187034, "Field of View Horizontal Flip");
		dict.put(0x00187040, "Grid Absorbing Material");
		dict.put(0x00187041, "Grid Spacing Material");
		dict.put(0x00187042, "Grid Thickness");
		dict.put(0x00187044, "Grid Pitch");
		dict.put(0x00187046, "Grid Aspect Ratio");
		dict.put(0x00187048, "Grid Period");
		dict.put(0x0018704c, "Grid Focal Distance");
		dict.put(0x00187050, "Filter Material");
		dict.put(0x00187052, "Filter Thickness Min");
		dict.put(0x00187054, "Filter Thickness Max");
		dict.put(0x00187060, "Exposure Control Mode");
		dict.put(0x0020000d, "Study Instance UID");
		dict.put(0x0020000e, "Series Instance UID");
		dict.put(0x00200011, "Series Number");
		dict.put(0x00200012, "Acquisition Number");
		dict.put(0x00200013, "Instance Number");
		dict.put(0x00200020, "Patient Orientation");
		dict.put(0x00200030, "Image Position");
		dict.put(0x00200032, "Image Position (Patient)");
		dict.put(0x00200037, "Image Orientation (Patient)");
		dict.put(0x00200050, "Location");
		dict.put(0x00200052, "Frame of Reference UID");
		dict.put(0x00200070, "Image Geometry Type");
		dict.put(0x00201001, "Acquisitions in Series");
		dict.put(0x00201020, "Reference");
		dict.put(0x00201041, "Slice Location");
		// skipped a bunch of stuff here - not used
		dict.put(0x00280002, "Samples per pixel");
		dict.put(0x00280003, "Samples per pixel used");
		dict.put(0x00280004, "Photometric Interpretation");
		dict.put(0x00280006, "Planar Configuration");
		dict.put(0x00280008, "Number of frames");
		dict.put(0x00280009, "Frame Increment Pointer");
		dict.put(0x0028000a, "Frame Dimension Pointer");
		dict.put(0x00280010, "Rows");
		dict.put(0x00280011, "Columns");
		dict.put(0x00280012, "Planes");
		dict.put(0x00280014, "Ultrasound Color Data Present");
		dict.put(0x00280030, "Pixel Spacing");
		dict.put(0x00280031, "Zoom Factor");
		dict.put(0x00280032, "Zoom Center");
		dict.put(0x00280034, "Pixel Aspect Ratio");
		dict.put(0x00280051, "Corrected Image");
		dict.put(0x00280100, "Bits Allocated");
		dict.put(0x00280101, "Bits Stored");
		dict.put(0x00280102, "High Bit");
		dict.put(0x00280103, "Pixel Representation");
		dict.put(0x00280106, "Smallest Image Pixel Value");
		dict.put(0x00280107, "Largest Image Pixel Value");
		dict.put(0x00280108, "Smallest Pixel Value in Series");
		dict.put(0x00280109, "Largest Pixel Value in Series");
		dict.put(0x00280110, "Smallest Image Pixel Value in Plane");
		dict.put(0x00280111, "Largest Image Pixel Value in Plane");
		dict.put(0x00280120, "Pixel Padding Value");
		dict.put(0x00280300, "Quality Control Image");
		dict.put(0x00280301, "Burned in Annotation");
		dict.put(0x00281040, "Pixel Intensity Relationship");
		dict.put(0x00281041, "Pixel Intensity Relationship Sign");
		dict.put(0x00281050, "Window Center");
		dict.put(0x00281051, "Window Width");
		dict.put(0x00281052, "Rescale Intercept");
		dict.put(0x00281053, "Rescale Slope");
		dict.put(0x00281054, "Rescale Type");
		dict.put(0x00281055, "Window Center and Width Explanation");
		dict.put(0x00281090, "Recommended Viewing Mode");
		dict.put(0x00281101, "Red Palette Color LUT Descriptor");
		dict.put(0x00281102, "Green Palette Color LUT Descriptor");
		dict.put(0x00281103, "Blue Palette Color LUT Descriptor");
		dict.put(0x00281199, "Palette Color LUT UID");
		dict.put(0x00281201, "Red Palette Color LUT Data");
		dict.put(0x00281202, "Green Palette Color LUT Data");
		dict.put(0x00281203, "Blue Palette Color LUT Data");
		dict.put(0x00281221, "Segmented Red Palette Color LUT Data");
		dict.put(0x00281222, "Segmented Green Palette Color LUT Data");
		dict.put(0x00281223, "Segmented Blue Palette Color LUT Data");
		dict.put(0x00281300, "Implant Present");
		dict.put(0x00281350, "Partial View");
		dict.put(0x00281351, "Partial View Description");
		dict.put(0x00282110, "Lossy Image Compression");
		dict.put(0x00282112, "Lossy Image Compression Ratio");
		dict.put(0x00282114, "Lossy Image Compression Method");
		dict.put(0x00283000, "Modality LUT Sequence");
		dict.put(0x00283002, "LUT Descriptor");
		dict.put(0x00283003, "LUT Explanation");
		dict.put(0x00283004, "Modality LUT Type");
		dict.put(0x00283006, "LUT Data");
		dict.put(0x00283010, "VOI LUT Sequence");
		dict.put(0x00283110, "Softcopy VOI LUT Sequence");
		dict.put(0x00285000, "Bi-Plane Acquisition Sequence");
		dict.put(0x00286010, "Representative Frame Number");
		dict.put(0x00286020, "Frame Numbers of Interest (FOI)");
		dict.put(0x00286022, "Frame(s) of Interest Description");
		dict.put(0x00286023, "Frame of Interest Type");
		dict.put(0x00286040, "R Wave Pointer");
		dict.put(0x00286100, "Mask Subtraction Sequence");
		dict.put(0x00286101, "Mask Operation");
		dict.put(0x00286102, "Applicable Frame Range");
		dict.put(0x00286110, "Mask Frame Numbers");
		dict.put(0x00286112, "Contrast Frame Averaging");
		dict.put(0x00286114, "Mask Sub-pixel Shift");
		dict.put(0x00286120, "TID Offset");
		dict.put(0x00286190, "Mask Operation Explanation");
		dict.put(0x00289001, "Data Point Rows");
		dict.put(0x00289002, "Data Point Columns");
		dict.put(0x00289003, "Signal Domain Columns");
		dict.put(0x00289108, "Data Representation");
		dict.put(0x00289110, "Pixel Measures Sequence");
		dict.put(0x00289132, "Frame VOI LUT Sequence");
		dict.put(0x00289145, "Pixel Value Transformation Sequence");
		dict.put(0x00289235, "Signal Domain Rows");
		// skipping some more stuff
		dict.put(0x00540011, "Number of Energy Windows");
		dict.put(0x00540021, "Number of Detectors");
		dict.put(0x00540051, "Number of Rotations");
		dict.put(0x00540080, "Slice Vector");
		dict.put(0x00540081, "Number of Slices");
		dict.put(0x00540202, "Type of Detector Motion");
		dict.put(0x00540400, "Image ID");
		dict.put(0x20100100, "Border Density");

		return dict;
	}

	// -- Nested Classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Constants --

		public static final String CNAME = "io.scif.formats.DICOMFormat$Metadata";

		// -- Fields --

		byte[][] lut = null;
		short[][] shortLut = null;
		private ColorTable8 lut8;
		private ColorTable16 lut16;
		private long[] offsets = null;
		private boolean isJP2K = false;
		private boolean isJPEG = false;
		private boolean isRLE = false;
		private boolean isDeflate = false;
		private boolean oddLocations = false;
		private int maxPixelValue;
		private int imagesPerFile = 0;
		private double rescaleSlope = 1.0, rescaleIntercept = 0.0;
		private Hashtable<Integer, Vector<String>> fileList;
		private boolean inverted = false;

		private String pixelSizeX, pixelSizeY;
		private Double pixelSizeZ;

		private String date, time, imageType;
		private String originalDate, originalTime, originalInstance;
		private int originalSeries;

		private Vector<String> companionFiles = new Vector<String>();

		// Getters and Setters

		public long[] getOffsets() {
			return offsets;
		}

		public void setOffsets(final long[] offsets) {
			this.offsets = offsets;
		}

		public double getRescaleSlope() {
			return rescaleSlope;
		}

		public void setRescaleSlope(final double rescaleSlope) {
			this.rescaleSlope = rescaleSlope;
		}

		public double getRescaleIntercept() {
			return rescaleIntercept;
		}

		public void setRescaleIntercept(final double rescaleIntercept) {
			this.rescaleIntercept = rescaleIntercept;
		}

		public String getPixelSizeX() {
			return pixelSizeX;
		}

		public void setPixelSizeX(final String pixelSizeX) {
			this.pixelSizeX = pixelSizeX;
		}

		public String getPixelSizeY() {
			return pixelSizeY;
		}

		public void setPixelSizeY(final String pixelSizeY) {
			this.pixelSizeY = pixelSizeY;
		}

		public Double getPixelSizeZ() {
			return pixelSizeZ;
		}

		public void setPixelSizeZ(final Double pixelSizeZ) {
			this.pixelSizeZ = pixelSizeZ;
		}

		public boolean isInverted() {
			return inverted;
		}

		public void setInverted(final boolean inverted) {
			this.inverted = inverted;
		}

		public boolean isJP2K() {
			return isJP2K;
		}

		public void setJP2K(final boolean isJP2K) {
			this.isJP2K = isJP2K;
		}

		public boolean isJPEG() {
			return isJPEG;
		}

		public void setJPEG(final boolean isJPEG) {
			this.isJPEG = isJPEG;
		}

		public boolean isRLE() {
			return isRLE;
		}

		public void setRLE(final boolean isRLE) {
			this.isRLE = isRLE;
		}

		public boolean isDeflate() {
			return isDeflate;
		}

		public void setDeflate(final boolean isDeflate) {
			this.isDeflate = isDeflate;
		}

		public boolean isOddLocations() {
			return oddLocations;
		}

		public void setOddLocations(final boolean oddLocations) {
			this.oddLocations = oddLocations;
		}

		public int getMaxPixelValue() {
			return maxPixelValue;
		}

		public void setMaxPixelValue(final int maxPixelValue) {
			this.maxPixelValue = maxPixelValue;
		}

		public int getImagesPerFile() {
			return imagesPerFile;
		}

		public void setImagesPerFile(final int imagesPerFile) {
			this.imagesPerFile = imagesPerFile;
		}

		public Hashtable<Integer, Vector<String>> getFileList() {
			return fileList;
		}

		public void setFileList(final Hashtable<Integer, Vector<String>> fileList) {
			this.fileList = fileList;
		}

		public String getDate() {
			return date;
		}

		public void setDate(final String date) {
			this.date = date;
		}

		public String getTime() {
			return time;
		}

		public void setTime(final String time) {
			this.time = time;
		}

		public String getImageType() {
			return imageType;
		}

		public void setImageType(final String imageType) {
			this.imageType = imageType;
		}

		public String getOriginalDate() {
			return originalDate;
		}

		public void setOriginalDate(final String originalDate) {
			this.originalDate = originalDate;
		}

		public String getOriginalTime() {
			return originalTime;
		}

		public void setOriginalTime(final String originalTime) {
			this.originalTime = originalTime;
		}

		public String getOriginalInstance() {
			return originalInstance;
		}

		public void setOriginalInstance(final String originalInstance) {
			this.originalInstance = originalInstance;
		}

		public int getOriginalSeries() {
			return originalSeries;
		}

		public void setOriginalSeries(final int originalSeries) {
			this.originalSeries = originalSeries;
		}

		public Vector<String> getCompanionFiles() {
			return companionFiles;
		}

		public void setCompanionFiles(final Vector<String> companionFiles) {
			this.companionFiles = companionFiles;
		}

		// -- ColorTable API Methods --

		@Override
		public ColorTable getColorTable(final int imageIndex, final long planeIndex)
		{
			final int pixelType = get(0).getPixelType();

			switch (pixelType) {
				case FormatTools.INT8:
				case FormatTools.UINT8:
					if (lut != null && lut8 == null) lut8 = new ColorTable8(lut);
					return lut8;
				case FormatTools.INT16:
				case FormatTools.UINT16:
					if (shortLut != null && lut16 == null) lut16 =
						new ColorTable16(shortLut);
					return lut16;
			}

			return null;
		}

		// -- Metadata API Methods --

		@Override
		public void populateImageMetadata() {
			log().info("Populating metadata");

			// TODO this isn't going to work because each parsing will
			// get the same filelist size and repeat infinitely
			final int seriesCount = fileList.size();

			final Integer[] keys = fileList.keySet().toArray(new Integer[0]);
			Arrays.sort(keys);

			for (int i = 0; i < seriesCount; i++) {
				int sizeZ = 0;
				if (seriesCount == 1) {
					sizeZ = getOffsets().length * fileList.get(keys[i]).size();

					get(i).setMetadataComplete(true);
					get(i).setFalseColor(false);
					if (isRLE) {
						get(i).setAxisTypes(Axes.X, Axes.Y, Axes.CHANNEL);
					}
					if (get(i).getAxisLength(Axes.CHANNEL) > 1) {
						get(i).setPlanarAxisCount(3);
					}
					else {
						get(i).setPlanarAxisCount(2);
					}
				}
				else {

					try {
						final Parser p = (Parser) getFormat().createParser();
						p.setGroupFiles(false);
						final Metadata m = p.parse(fileList.get(keys[i]).get(0));
						add(m.get(0));
						sizeZ *= fileList.get(keys[i]).size();
					}
					catch (final IOException e) {
						log().error("Error creating Metadata from DICOM companion files.",
							e);
					}
					catch (final FormatException e) {
						log().error("Error creating Metadata from DICOM companion files.",
							e);
					}

				}

				get(i).setAxisLength(Axes.Z, sizeZ);
			}
		}

		// -- HasSource API Methods --

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);

			if (!fileOnly) {
				oddLocations = false;
				isJPEG = isJP2K = isRLE = isDeflate = false;
				lut = null;
				offsets = null;
				shortLut = null;
				maxPixelValue = 0;
				rescaleSlope = 1.0;
				rescaleIntercept = 0.0;
				pixelSizeX = pixelSizeY = null;
				pixelSizeZ = null;
				imagesPerFile = 0;
				fileList = null;
				inverted = false;
				date = time = imageType = null;
				originalDate = originalTime = originalInstance = null;
				originalSeries = 0;
				// TODO the resetting is a bit too aggressive, perhaps it should just
				// clear out fields..
				// companionFiles.clear();
			}
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Checker extends AbstractChecker {

		// -- Constants --

		private static final String[] DICOM_SUFFIXES = { "dic", "dcm", "dicom",
			"j2ki", "j2kr" };

		// -- Constructor --

		public Checker() {
			suffixSufficient = false;
			suffixNecessary = false;
		}

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final String name, final boolean open) {
			// extension is sufficient as long as it is DIC, DCM, DICOM, J2KI, or J2KR
			if (FormatTools.checkSuffix(name, DICOM_SUFFIXES)) return true;
			return super.isFormat(name, open);
		}

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int blockLen = 2048;
			if (!FormatTools.validStream(stream, blockLen, true)) return false;

			stream.seek(128);
			if (stream.readString(4).equals(DICOM_MAGIC_STRING)) return true;
			stream.seek(0);

			try {
				final int tag = DICOMUtils.getNextTag(stream).get();
				return TYPES.get(tag) != null;
			}
			catch (final NullPointerException e) {}
			catch (final FormatException e) {}
			return false;
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Constants --

		private static final int PIXEL_REPRESENTATION = 0x00280103;
		private static final int TRANSFER_SYNTAX_UID = 0x00020010;
		private static final int SLICE_SPACING = 0x00180088;
		private static final int SAMPLES_PER_PIXEL = 0x00280002;
		private static final int PHOTOMETRIC_INTERPRETATION = 0x00280004;
		private static final int PLANAR_CONFIGURATION = 0x00280006;
		private static final int NUMBER_OF_FRAMES = 0x00280008;
		private static final int ROWS = 0x00280010;
		private static final int COLUMNS = 0x00280011;
		private static final int PIXEL_SPACING = 0x00280030;
		private static final int BITS_ALLOCATED = 0x00280100;
		private static final int WINDOW_CENTER = 0x00281050;
		private static final int WINDOW_WIDTH = 0x00281051;
		private static final int RESCALE_INTERCEPT = 0x00281052;
		private static final int RESCALE_SLOPE = 0x00281053;
		private static final int ICON_IMAGE_SEQUENCE = 0x00880200;
		private static final int ITEM = 0xFFFEE000;
		private static final int ITEM_DELIMINATION = 0xFFFEE00D;
		private static final int SEQUENCE_DELIMINATION = 0xFFFEE0DD;
		private static final int PIXEL_DATA = 0x7FE00010;

		// -- Parser API Methods --

		@Override
		public int fileGroupOption(final String id) throws FormatException,
			IOException
		{
			return FormatTools.CAN_GROUP;
		}

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			meta.createImageMetadata(1);

			stream.order(true);

			final ImageMetadata iMeta = meta.get(0);

			// look for companion files
			final Vector<String> companionFiles = new Vector<String>();
			attachCompanionFiles(companionFiles);
			meta.setCompanionFiles(companionFiles);

			int location = 0;
			boolean isJP2K = false;
			boolean isJPEG = false;
			boolean isRLE = false;
			boolean isDeflate = false;
			boolean oddLocations = false;
			int maxPixelValue = -1;
			int imagesPerFile = 0;
			boolean bigEndianTransferSyntax = false;
			long[] offsets = null;

			int sizeX = 0;
			int sizeY = 0;
			int bitsPerPixel = 0;
			boolean interleaved;

			// some DICOM files have a 128 byte header followed by a 4 byte identifier

			log().info("Verifying DICOM format");
			final MetadataLevel level = getMetadataOptions().getMetadataLevel();

			in.seek(128);
			if (in.readString(4).equals("DICM")) {
				if (level != MetadataLevel.MINIMUM) {
					// header exists, so we'll read it
					in.seek(0);
					meta.getTable().put("Header information", in.readString(128));
					in.skipBytes(4);
				}
				location = 128;
			}
			else in.seek(0);

			log().info("Reading tags");

			long baseOffset = 0;

			boolean decodingTags = true;
			boolean signed = false;

			while (decodingTags) {
				if (in.getFilePointer() + 4 >= in.length()) {
					break;
				}
				log().debug("Reading tag from " + in.getFilePointer());
				final DICOMTag tag =
					DICOMUtils.getNextTag(in, bigEndianTransferSyntax, oddLocations);
				iMeta.setLittleEndian(tag.isLittleEndian());

				if (tag.getElementLength() <= 0) continue;

				oddLocations = (location & 1) != 0;

				log().debug(
					"  tag=" + tag.get() + " len=" + tag.getElementLength() + " fp=" +
						in.getFilePointer());

				String s = null;
				switch (tag.get()) {
					case TRANSFER_SYNTAX_UID:
						// this tag can indicate which compression scheme is used
						s = in.readString(tag.getElementLength());
						addInfo(meta, tag, s);
						if (s.startsWith("1.2.840.10008.1.2.4.9")) isJP2K = true;
						else if (s.startsWith("1.2.840.10008.1.2.4")) isJPEG = true;
						else if (s.startsWith("1.2.840.10008.1.2.5")) isRLE = true;
						else if (s.equals("1.2.8.10008.1.2.1.99")) isDeflate = true;
						else if (s.indexOf("1.2.4") > -1 || s.indexOf("1.2.5") > -1) {
							throw new UnsupportedCompressionException(
								"Sorry, compression type " + s + " not supported");
						}
						if (s.indexOf("1.2.840.10008.1.2.2") >= 0) {
							bigEndianTransferSyntax = true;
						}
						break;
					case NUMBER_OF_FRAMES:
						s = in.readString(tag.getElementLength());
						addInfo(meta, tag, s);
						final double frames = Double.parseDouble(s);
						if (frames > 1.0) imagesPerFile = (int) frames;
						break;
					case SAMPLES_PER_PIXEL:
						addInfo(meta, tag, in.readShort());
						break;
					case PLANAR_CONFIGURATION:
						final int config = in.readShort();
						interleaved = config == 0;
						if (interleaved) {
							iMeta.setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y);
							iMeta.setPlanarAxisCount(3);
						}
						addInfo(meta, tag, config);
						break;
					case ROWS:
						if (sizeY == 0) {
							sizeY = in.readShort();
							iMeta.addAxis(Axes.Y, sizeY);
						}
						else in.skipBytes(2);
						addInfo(meta, tag, sizeY);
						break;
					case COLUMNS:
						if (sizeX == 0) {
							sizeX = in.readShort();
							iMeta.addAxis(Axes.X, sizeX);
						}
						else in.skipBytes(2);
						addInfo(meta, tag, sizeX);
						break;
					case PHOTOMETRIC_INTERPRETATION:
					case PIXEL_SPACING:
					case SLICE_SPACING:
					case RESCALE_INTERCEPT:
					case WINDOW_CENTER:
					case RESCALE_SLOPE:
						addInfo(meta, tag, in.readString(tag.getElementLength()));
						break;
					case BITS_ALLOCATED:
						if (bitsPerPixel == 0) bitsPerPixel = in.readShort();
						else in.skipBytes(2);
						addInfo(meta, tag, bitsPerPixel);
						break;
					case PIXEL_REPRESENTATION:
						final short ss = in.readShort();
						signed = ss == 1;
						addInfo(meta, tag, ss);
						break;
					case 537262910:
					case WINDOW_WIDTH:
						final String t = in.readString(tag.getElementLength());
						if (t.trim().length() == 0) maxPixelValue = -1;
						else {
							try {
								maxPixelValue = new Double(t.trim()).intValue();
							}
							catch (final NumberFormatException e) {
								maxPixelValue = -1;
							}
						}
						addInfo(meta, tag, t);
						break;
					case PIXEL_DATA:
					case ITEM:
					case 0xffee000:
						if (tag.getElementLength() != 0) {
							baseOffset = in.getFilePointer();
							addInfo(meta, tag, location);
							decodingTags = false;
						}
						else addInfo(meta, tag, null);
						break;
					case 0x7f880010:
						if (tag.getElementLength() != 0) {
							baseOffset = location + 4;
							decodingTags = false;
						}
						break;
					case 0x7fe00000:
						in.skipBytes(tag.getElementLength());
						break;
					case 0:
						in.seek(in.getFilePointer() - 4);
						break;
					default:
						final long oldfp = in.getFilePointer();
						addInfo(meta, tag, s);
						in.seek(oldfp + tag.getElementLength());
				}
				if (in.getFilePointer() >= (in.length() - 4)) {
					decodingTags = false;
				}
			}
			if (imagesPerFile == 0) imagesPerFile = 1;

			int bpp = bitsPerPixel;

			while (bitsPerPixel % 8 != 0)
				bitsPerPixel++;
			if (bitsPerPixel == 24 || bitsPerPixel == 48) {
				bitsPerPixel /= 3;
				bpp /= 3;
			}

			final int pixelType =
				FormatTools.pixelTypeFromBytes(bitsPerPixel / 8, signed, false);

			iMeta.setBitsPerPixel(bpp);
			iMeta.setPixelType(pixelType);

			final int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);

			final int planeSize =
				sizeX *
					sizeY *
					(int) (meta.getColorTable(0, 0) == null ? meta.get(0).getAxisLength(
						Axes.CHANNEL) : 1) * bytesPerPixel;

			meta.setJP2K(isJP2K);
			meta.setJPEG(isJPEG);
			meta.setImagesPerFile(imagesPerFile);
			meta.setRLE(isRLE);
			meta.setDeflate(isDeflate);
			meta.setMaxPixelValue(maxPixelValue);
			meta.setOddLocations(oddLocations);

			log().info("Calculating image offsets");

			// calculate the offset to each plane

			in.seek(baseOffset - 12);
			final int len = in.readInt();
			if (len >= 0 && len + in.getFilePointer() < in.length()) {
				in.skipBytes(len);
				final int check = in.readShort() & 0xffff;
				if (check == 0xfffe) {
					baseOffset = in.getFilePointer() + 2;
				}
			}

			offsets = new long[imagesPerFile];
			meta.setOffsets(offsets);

			for (int i = 0; i < imagesPerFile; i++) {
				if (isRLE) {
					if (i == 0) in.seek(baseOffset);
					else {
						in.seek(offsets[i - 1]);
						final CodecOptions options = new CodecOptions();
						options.maxBytes = planeSize / bytesPerPixel;
						for (int q = 0; q < bytesPerPixel; q++) {
							new PackbitsCodec().decompress(in, options);
							while (in.read() == 0) { /* Read to non-0 data */ }
							in.seek(in.getFilePointer() - 1);
						}
					}
					in.skipBytes(i == 0 ? 64 : 53);
					while (in.read() == 0) { /* Read to non-0 data */ }
					offsets[i] = in.getFilePointer() - 1;
				}
				else if (isJPEG || isJP2K) {
					// scan for next JPEG magic byte sequence
					if (i == 0) offsets[i] = baseOffset;
					else offsets[i] = offsets[i - 1] + 3;

					final byte secondCheck = isJPEG ? (byte) 0xd8 : (byte) 0x4f;

					in.seek(offsets[i]);
					final byte[] buf = new byte[8192];
					int n = in.read(buf);
					boolean found = false;
					while (!found) {
						for (int q = 0; q < n - 2; q++) {
							if (buf[q] == (byte) 0xff && buf[q + 1] == secondCheck &&
								buf[q + 2] == (byte) 0xff)
							{
								if (isJPEG || (isJP2K && buf[q + 3] == 0x51)) {
									found = true;
									offsets[i] = in.getFilePointer() + q - n;
									break;
								}
							}
						}
						if (!found) {
							for (int q = 0; q < 4; q++) {
								buf[q] = buf[buf.length + q - 4];
							}
							n = in.read(buf, 4, buf.length - 4) + 4;
						}
					}
				}
				else offsets[i] = baseOffset + planeSize * i;
			}

			makeFileList();
		}

		@Override
		public String[] getImageUsedFiles(final int imageIndex,
			final boolean noPixels)
		{
			FormatTools.assertId(currentId, true, 1);
			if (noPixels || metadata.getFileList() == null) return null;
			final Integer[] keys =
				metadata.getFileList().keySet().toArray(new Integer[0]);
			Arrays.sort(keys);
			final Vector<String> files = metadata.getFileList().get(keys[imageIndex]);
			for (final String f : metadata.getCompanionFiles()) {
				files.add(f);
			}
			return files == null ? null : files.toArray(new String[files.size()]);
		}

		// -- Helper methods --

		private void makeFileList() throws FormatException, IOException {
			log().info("Building file list");

			if (metadata.getFileList() == null &&
				metadata.getOriginalInstance() != null &&
				metadata.getOriginalDate() != null &&
				metadata.getOriginalTime() != null && isGroupFiles())
			{
				currentId = new Location(getContext(), currentId).getAbsolutePath();
				final Hashtable<Integer, Vector<String>> fileList =
					new Hashtable<Integer, Vector<String>>();
				final Integer s = new Integer(metadata.getOriginalSeries());
				fileList.put(s, new Vector<String>());

				final int instanceNumber =
					Integer.parseInt(metadata.getOriginalInstance()) - 1;
				if (instanceNumber == 0) fileList.get(s).add(currentId);
				else {
					while (instanceNumber > fileList.get(s).size()) {
						fileList.get(s).add(null);
					}
					fileList.get(s).add(currentId);
				}

				// look for matching files in the current directory
				final Location currentFile =
					new Location(getContext(), currentId).getAbsoluteFile();
				Location directory = currentFile.getParentFile();
				scanDirectory(directory, false);

				// move up a directory and look for other directories that
				// could contain matching files

				directory = directory.getParentFile();
				final String[] subdirs = directory.list(true);
				if (subdirs != null) {
					for (final String subdir : subdirs) {
						final Location f =
							new Location(getContext(), directory, subdir).getAbsoluteFile();
						if (!f.isDirectory()) continue;
						scanDirectory(f, true);
					}
				}

				final Integer[] keys = fileList.keySet().toArray(new Integer[0]);
				Arrays.sort(keys);
				for (final Integer key : keys) {
					for (int j = 0; j < fileList.get(key).size(); j++) {
						if (fileList.get(key).get(j) == null) {
							fileList.get(key).remove(j);
							j--;
						}
					}
				}

				metadata.setFileList(fileList);
			}
			else if (metadata.getFileList() == null) {
				final Hashtable<Integer, Vector<String>> fileList =
					new Hashtable<Integer, Vector<String>>();
				fileList.put(0, new Vector<String>());
				fileList.get(0).add(currentId);
				metadata.setFileList(fileList);
			}
		}

		/**
		 * DICOM datasets produced by:
		 * http://www.ct-imaging.de/index.php/en/ct-systeme-e/mikro-ct-e.html
		 * contain a bunch of extra metadata and log files. We do not parse these
		 * extra files, but do locate and attach them to the DICOM file(s).
		 */
		private void attachCompanionFiles(final Vector<String> companionFiles) {
			final Location parent =
				new Location(getContext(), currentId).getAbsoluteFile().getParentFile();
			final Location grandparent = parent.getParentFile();

			if (new Location(getContext(), grandparent, parent.getName() + ".mif")
				.exists())
			{
				final String[] list = grandparent.list(true);
				for (final String f : list) {
					final Location file = new Location(getContext(), grandparent, f);
					if (!file.isDirectory()) {
						companionFiles.add(file.getAbsolutePath());
					}
				}
			}
		}

		/**
		 * Scan the given directory for files that belong to this dataset.
		 */
		private void scanDirectory(final Location dir, final boolean checkSeries)
			throws FormatException, IOException
		{
			final Location currentFile =
				new Location(getContext(), currentId).getAbsoluteFile();
			final FilePattern pattern =
				new FilePattern(getContext(), currentFile.getName(), dir
					.getAbsolutePath());
			String[] patternFiles = pattern.getFiles();
			if (patternFiles == null) patternFiles = new String[0];
			Arrays.sort(patternFiles);
			final String[] files = dir.list(true);
			if (files == null) return;
			Arrays.sort(files);
			for (final String f : files) {
				final String file =
					new Location(getContext(), dir, f).getAbsolutePath();
				log().debug("Checking file " + file);
				if (!f.equals(currentId) && !file.equals(currentId) &&
					getFormat().createChecker().isFormat(file) &&
					Arrays.binarySearch(patternFiles, file) >= 0)
				{
					addFileToList(file, checkSeries);
				}
			}
		}

		/**
		 * Determine if the given file belongs in the same dataset as this file.
		 */
		private void addFileToList(final String file, final boolean checkSeries)
			throws FormatException, IOException
		{
			final RandomAccessInputStream stream =
				new RandomAccessInputStream(getContext(), file);
			if (!getFormat().createChecker().isFormat(stream)) {
				stream.close();
				return;
			}
			stream.order(true);

			stream.seek(128);
			if (!stream.readString(4).equals("DICM")) stream.seek(0);

			int fileSeries = -1;

			String date = null, time = null, instance = null;
			while (date == null || time == null || instance == null ||
				(checkSeries && fileSeries < 0))
			{
				final long fp = stream.getFilePointer();
				if (fp + 4 >= stream.length() || fp < 0) break;
				final DICOMTag tag = DICOMUtils.getNextTag(stream);
				final String key = TYPES.get(new Integer(tag.get()));
				if ("Instance Number".equals(key)) {
					instance = stream.readString(tag.getElementLength()).trim();
					if (instance.length() == 0) instance = null;
				}
				else if ("Acquisition Time".equals(key)) {
					time = stream.readString(tag.getElementLength());
				}
				else if ("Acquisition Date".equals(key)) {
					date = stream.readString(tag.getElementLength());
				}
				else if ("Series Number".equals(key)) {
					fileSeries =
						Integer.parseInt(stream.readString(tag.getElementLength()).trim());
				}
				else stream.skipBytes(tag.getElementLength());
			}
			stream.close();

			if (date == null || time == null || instance == null ||
				(checkSeries && fileSeries == metadata.getOriginalSeries()))
			{
				return;
			}

			int stamp = 0;
			try {
				stamp = Integer.parseInt(time);
			}
			catch (final NumberFormatException e) {}

			int timestamp = 0;
			try {
				timestamp = Integer.parseInt(metadata.getOriginalTime());
			}
			catch (final NumberFormatException e) {}

			if (date.equals(metadata.getOriginalDate()) &&
				(Math.abs(stamp - timestamp) < 150))
			{
				int position = Integer.parseInt(instance) - 1;
				if (position < 0) position = 0;
				final Hashtable<Integer, Vector<String>> fileList =
					metadata.getFileList();
				if (fileList.get(fileSeries) == null) {
					fileList.put(fileSeries, new Vector<String>());
				}
				if (position < fileList.get(fileSeries).size()) {
					while (position < fileList.get(fileSeries).size() &&
						fileList.get(fileSeries).get(position) != null)
					{
						position++;
					}
					if (position < fileList.get(fileSeries).size()) {
						fileList.get(fileSeries).setElementAt(file, position);
					}
					else fileList.get(fileSeries).add(file);
				}
				else {
					while (position > fileList.get(fileSeries).size()) {
						fileList.get(fileSeries).add(null);
					}
					fileList.get(fileSeries).add(file);
				}
			}
		}

		private void addInfo(final Metadata meta, final DICOMTag tag,
			final String value) throws IOException
		{
			final String oldValue = value;
			String info = getHeaderInfo(tag, value);

			if (info != null && tag.get() != ITEM) {
				info = info.trim();
				if (info.equals("")) info = oldValue == null ? "" : oldValue.trim();

				String key = TYPES.get(tag.get());
				if (key == null) {
					key = formatTag(tag.get());
				}
				if (key.equals("Samples per pixel")) {
					final int sizeC = Integer.parseInt(info);
					if (sizeC > 1) {
						meta.get(0).setAxisLength(Axes.CHANNEL, sizeC);
						meta.get(0).setPlanarAxisCount(2);
					}
				}
				else if (key.equals("Photometric Interpretation")) {
					if (info.equals("PALETTE COLOR")) {
						meta.get(0).setIndexed(true);
						meta.get(0).setAxisLength(Axes.CHANNEL, 1);
						meta.lut = new byte[3][];
						meta.shortLut = new short[3][];

					}
					else if (info.startsWith("MONOCHROME")) {
						meta.setInverted(info.endsWith("1"));
					}
				}
				else if (key.equals("Acquisition Date")) meta.setOriginalDate(info);
				else if (key.equals("Acquisition Time")) meta.setOriginalTime(info);
				else if (key.equals("Instance Number")) {
					if (info.trim().length() > 0) {
						meta.setOriginalInstance(info);
					}
				}
				else if (key.equals("Series Number")) {
					try {
						meta.setOriginalSeries(Integer.parseInt(info));
					}
					catch (final NumberFormatException e) {}
				}
				else if (key.indexOf("Palette Color LUT Data") != -1) {
					final String color = key.substring(0, key.indexOf(" ")).trim();
					final int ndx =
						color.equals("Red") ? 0 : color.equals("Green") ? 1 : 2;
					final long fp = in.getFilePointer();
					in.seek(in.getFilePointer() - tag.getElementLength() + 1);
					meta.shortLut[ndx] = new short[tag.getElementLength() / 2];
					meta.lut[ndx] = new byte[tag.getElementLength() / 2];
					for (int i = 0; i < meta.lut[ndx].length; i++) {
						meta.shortLut[ndx][i] = in.readShort();
						meta.lut[ndx][i] = (byte) (meta.shortLut[ndx][i] & 0xff);
					}
					in.seek(fp);
				}
				else if (key.equals("Content Time")) meta.setTime(info);
				else if (key.equals("Content Date")) meta.setDate(info);
				else if (key.equals("Image Type")) meta.setImageType(info);
				else if (key.equals("Rescale Intercept")) {
					meta.setRescaleIntercept(Double.parseDouble(info));
				}
				else if (key.equals("Rescale Slope")) {
					meta.setRescaleSlope(Double.parseDouble(info));
				}
				else if (key.equals("Pixel Spacing")) {
					meta.setPixelSizeX(info.substring(0, info.indexOf("\\")));
					meta.setPixelSizeY(info.substring(info.lastIndexOf("\\") + 1));
				}
				else if (key.equals("Spacing Between Slices")) {
					meta.setPixelSizeZ(new Double(info));
				}

				if (((tag.get() & 0xffff0000) >> 16) != 0x7fe0) {
					key = formatTag(tag.get()) + " " + key;
					final int imageIndex = meta.getImageCount() - 1;

					Object v;
					if ((v = meta.get(imageIndex).getTable().get(key)) != null) {
						// make sure that values are not overwritten
						meta.get(imageIndex).getTable().remove(key);
						meta.get(imageIndex).getTable().putList(key, v);
						meta.get(imageIndex).getTable().putList(key, info);
					}
					else {
						meta.get(imageIndex).getTable().put(key, info);
					}
				}
			}

		}

		private String formatTag(final int tag) {
			String s = Integer.toHexString(tag);
			while (s.length() < 8) {
				s = "0" + s;
			}
			return s.substring(0, 4) + "," + s.substring(4);
		}

		private void addInfo(final Metadata meta, final DICOMTag tag,
			final int value) throws IOException
		{
			addInfo(meta, tag, Integer.toString(value));
		}

		private String getHeaderInfo(final DICOMTag tag, String value)
			throws IOException
		{
			if (tag.get() == ITEM_DELIMINATION || tag.get() == SEQUENCE_DELIMINATION)
			{
				tag.setInSequence(false);
			}

			String id = TYPES.get(new Integer(tag.get()));
			int vr = tag.getVR();

			if (id != null) {
				if (vr == DICOMUtils.IMPLICIT_VR) {
					vr = (id.charAt(0) << 8) + id.charAt(1);
					tag.setVR(vr);
				}
				if (id.length() > 2) id = id.substring(2);
			}

			if (tag.get() == ITEM) return id != null ? id : null;
			if (value != null) return value;

			boolean skip = false;
			switch (vr) {
				case DICOMUtils.AE:
				case DICOMUtils.AS:
				case DICOMUtils.AT:
				case DICOMUtils.CS:
				case DICOMUtils.DA:
				case DICOMUtils.DS:
				case DICOMUtils.DT:
				case DICOMUtils.IS:
				case DICOMUtils.LO:
				case DICOMUtils.LT:
				case DICOMUtils.PN:
				case DICOMUtils.SH:
				case DICOMUtils.ST:
				case DICOMUtils.TM:
				case DICOMUtils.UI:
					value = in.readString(tag.getElementLength());
					break;
				case DICOMUtils.US:
					if (tag.getElementLength() == 2) value =
						Integer.toString(in.readShort());
					else {
						value = "";
						final int n = tag.getElementLength() / 2;
						for (int i = 0; i < n; i++) {
							value += Integer.toString(in.readShort()) + " ";
						}
					}
					break;
				case DICOMUtils.IMPLICIT_VR:
					value = in.readString(tag.getElementLength());
					if (tag.getElementLength() <= 4 || tag.getElementLength() > 44) value =
						null;
					break;
				case DICOMUtils.SQ:
					value = "";
					final boolean privateTag = ((tag.getElementLength() >> 16) & 1) != 0;
					if (tag.get() == ICON_IMAGE_SEQUENCE || privateTag) skip = true;
					break;
				default:
					skip = true;
			}
			if (skip) {
				final long skipCount = tag.getElementLength();
				if (in.getFilePointer() + skipCount <= in.length()) {
					in.skipBytes((int) skipCount);
				}
				tag.addLocation(tag.getElementLength());
				value = "";
			}

			if (value != null && id == null && !value.equals("")) return value;
			else if (id == null) return null;
			else return value;
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		@Parameter
		private InitializeService initializeService;

		public Reader() {
			domains = new String[] { FormatTools.MEDICAL_DOMAIN };
			hasCompanionFiles = true;
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, long planeIndex,
			final ByteArrayPlane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			FormatTools.checkPlaneParameters(meta, imageIndex, planeIndex, plane
				.getData().length, planeMin, planeMax);

			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) planeMin[xAxis],
								y = (int) planeMin[yAxis],
								w = (int) planeMax[xAxis],
								h = (int) planeMax[yAxis];

			final Hashtable<Integer, Vector<String>> fileList = meta.getFileList();

			final Integer[] keys = fileList.keySet().toArray(new Integer[0]);
			Arrays.sort(keys);
			if (fileList.get(keys[imageIndex]).size() > 1) {
				final int fileNumber = (int)(planeIndex / meta.getImagesPerFile());
				planeIndex = planeIndex % meta.getImagesPerFile();
				final String file = fileList.get(keys[imageIndex]).get(fileNumber);
				final io.scif.Reader r = initializeService.initializeReader(file);
				return (ByteArrayPlane) r.openPlane(imageIndex, planeIndex, plane,
					planeMin, planeMax);
			}

			final int ec =
				meta.get(0).isIndexed() ? 1 : (int) meta.get(imageIndex).getAxisLength(
					Axes.CHANNEL);
			final int bpp =
				FormatTools.getBytesPerPixel(meta.get(imageIndex).getPixelType());
			final int bytes =
				(int) (meta.get(imageIndex).getAxisLength(Axes.X) *
					meta.get(imageIndex).getAxisLength(Axes.Y) * bpp * ec);
			getStream().seek(meta.getOffsets()[(int) planeIndex]);

			if (meta.isRLE()) {
				// plane is compressed using run-length encoding
				final CodecOptions options = new CodecOptions();
				options.maxBytes =
					(int) (meta.get(imageIndex).getAxisLength(Axes.X) * meta.get(
						imageIndex).getAxisLength(Axes.Y));
				for (int c = 0; c < ec; c++) {
					final PackbitsCodec codec = new PackbitsCodec();
					byte[] t = null;

					if (bpp > 1) {
						// TODO unused int planeSize = bytes / (bpp * ec);
						final byte[][] tmp = new byte[bpp][];
						for (int i = 0; i < bpp; i++) {
							tmp[i] = codec.decompress(getStream(), options);
							if (planeIndex < meta.getImagesPerFile() - 1 || i < bpp - 1) {
								while (getStream().read() == 0) { /* Read to non-0 data */ }
								getStream().seek(getStream().getFilePointer() - 1);
							}
						}
						t = new byte[bytes / ec];
						for (int i = 0; i < planeIndex; i++) {
							for (int j = 0; j < bpp; j++) {
								final int byteIndex =
									meta.get(imageIndex).isLittleEndian() ? bpp - j - 1 : j;
								if (i < tmp[byteIndex].length) {
									t[i * bpp + j] = tmp[byteIndex][i];
								}
							}
						}
					}
					else {
						t = codec.decompress(getStream(), options);
						if (t.length < (bytes / ec)) {
							final byte[] tmp = t;
							t = new byte[bytes / ec];
							System.arraycopy(tmp, 0, t, 0, tmp.length);
						}
						if (planeIndex < meta.getImagesPerFile() - 1 || c < ec - 1) {
							while (getStream().read() == 0) { /* Read to non-0 data */ }
							getStream().seek(getStream().getFilePointer() - 1);
						}
					}

					final int rowLen = w * bpp;
					final int srcRowLen =
						(int) meta.get(imageIndex).getAxisLength(Axes.X) * bpp;

					// TODO unused int srcPlane = meta.getAxisLength(imageIndex, Axes.Y) *
					// srcRowLen;

					for (int row = 0; row < h; row++) {
						final int src = (row + y) * srcRowLen + x * bpp;
						final int dest = (h * c + row) * rowLen;
						final int len = Math.min(rowLen, t.length - src - 1);
						if (len < 0) break;
						System.arraycopy(t, src, plane.getBytes(), dest, len);
					}
				}
			}
			else if (meta.isJPEG() || meta.isJP2K()) {
				// plane is compressed using JPEG or JPEG-2000
				final long end =
					planeIndex < meta.getOffsets().length - 1
						? meta.getOffsets()[(int)planeIndex + 1] : getStream().length();
				byte[] b = new byte[(int) (end - getStream().getFilePointer())];
				getStream().read(b);

				if (b[2] != (byte) 0xff) {
					final byte[] tmp = new byte[b.length + 1];
					tmp[0] = b[0];
					tmp[1] = b[1];
					tmp[2] = (byte) 0xff;
					System.arraycopy(b, 2, tmp, 3, b.length - 2);
					b = tmp;
				}
				if ((b[3] & 0xff) >= 0xf0) {
					b[3] -= (byte) 0x30;
				}

				int pt = b.length - 2;
				while (pt >= 0 && b[pt] != (byte) 0xff || b[pt + 1] != (byte) 0xd9) {
					pt--;
				}
				if (pt < b.length - 2) {
					final byte[] tmp = b;
					b = new byte[pt + 2];
					System.arraycopy(tmp, 0, b, 0, b.length);
				}

				Codec codec = null;
				final CodecOptions options = new CodecOptions();
				options.littleEndian = meta.get(imageIndex).isLittleEndian();
				options.interleaved = meta.get(imageIndex).getInterleavedAxisCount() > 0;
				if (meta.isJPEG()) codec = new JPEGCodec();
				else codec = new JPEG2000Codec();
				b = codec.decompress(b, options);

				final int rowLen = w * bpp;
				final int srcRowLen =
					(int) meta.get(imageIndex).getAxisLength(Axes.X) * bpp;

				final int srcPlane =
					(int) meta.get(imageIndex).getAxisLength(Axes.Y) * srcRowLen;

				for (int c = 0; c < ec; c++) {
					for (int row = 0; row < h; row++) {
						System.arraycopy(b, c * srcPlane + (row + y) * srcRowLen + x * bpp,
							plane.getBytes(), h * rowLen * c + row * rowLen, rowLen);
					}
				}
			}
			else if (meta.isDeflate()) {
				// TODO
				throw new UnsupportedCompressionException(
					"Deflate data is not supported.");
			}
			else {
				// plane is not compressed
				readPlane(getStream(), imageIndex, planeMin, planeMax, plane);
			}

			if (meta.isInverted()) {
				// pixels are stored such that white -> 0; invert the values so that
				// white -> 255 (or 65535)
				if (bpp == 1) {
					for (int i = 0; i < plane.getBytes().length; i++) {
						plane.getBytes()[i] = (byte) (255 - plane.getBytes()[i]);
					}
				}
				else if (bpp == 2) {
					if (meta.getMaxPixelValue() == -1) meta.setMaxPixelValue(65535);
					final boolean little = meta.get(imageIndex).isLittleEndian();
					for (int i = 0; i < plane.getBytes().length; i += 2) {
						final short s =
							DataTools.bytesToShort(plane.getBytes(), i, 2, little);
						DataTools.unpackBytes(meta.getMaxPixelValue() - s,
							plane.getBytes(), i, 2, little);
					}
				}
			}

			// NB: do *not* apply the rescale function

			return plane;
		}
	}

	// -- DICOM Helper Classes --

	private static class DICOMUtils {

		private static final int AE = 0x4145, AS = 0x4153, AT = 0x4154,
				CS = 0x4353;
		private static final int DA = 0x4441, DS = 0x4453, DT = 0x4454,
				FD = 0x4644;
		private static final int FL = 0x464C, IS = 0x4953, LO = 0x4C4F,
				LT = 0x4C54;
		private static final int PN = 0x504E, SH = 0x5348, SL = 0x534C,
				SS = 0x5353;
		private static final int ST = 0x5354, TM = 0x544D, UI = 0x5549,
				UL = 0x554C;
		private static final int US = 0x5553, UT = 0x5554, OB = 0x4F42,
				OW = 0x4F57;
		private static final int SQ = 0x5351, UN = 0x554E, QQ = 0x3F3F;

		private static final int IMPLICIT_VR = 0x2d2d;

		private static DICOMTag getNextTag(final RandomAccessInputStream stream)
			throws FormatException, IOException
		{
			return getNextTag(stream, false);
		}

		private static DICOMTag getNextTag(final RandomAccessInputStream stream,
			final boolean bigEndianTransferSyntax) throws FormatException,
			IOException
		{
			return getNextTag(stream, bigEndianTransferSyntax, false);
		}

		private static DICOMTag getNextTag(final RandomAccessInputStream stream,
			final boolean bigEndianTransferSyntax, final boolean isOddLocations)
			throws FormatException, IOException
		{
			final long fp = stream.getFilePointer();
			int groupWord = stream.readShort() & 0xffff;
			final DICOMTag diTag = new DICOMTag();
			boolean littleEndian = true;

			if (groupWord == 0x0800 && bigEndianTransferSyntax) {
				littleEndian = false;
				groupWord = 0x0008;
				stream.order(false);
			}
			else if (groupWord == 0xfeff || groupWord == 0xfffe) {
				stream.skipBytes(6);
				return DICOMUtils.getNextTag(stream, bigEndianTransferSyntax);
			}

			int elementWord = stream.readShort();
			int tag = ((groupWord << 16) & 0xffff0000) | (elementWord & 0xffff);

			diTag.setElementLength(getLength(stream, diTag));
			if (diTag.getElementLength() > stream.length()) {
				stream.seek(fp);
				littleEndian = !littleEndian;
				stream.order(littleEndian);

				groupWord = stream.readShort() & 0xffff;
				elementWord = stream.readShort();
				tag = ((groupWord << 16) & 0xffff0000) | (elementWord & 0xffff);
				diTag.setElementLength(getLength(stream, diTag));

				if (diTag.getElementLength() > stream.length()) {
					throw new FormatException("Invalid tag length " +
						diTag.getElementLength());
				}
				diTag.setTagValue(tag);
				return diTag;
			}

			if (diTag.getElementLength() < 0 && groupWord == 0x7fe0) {
				stream.skipBytes(12);
				diTag.setElementLength(stream.readInt());
				if (diTag.getElementLength() < 0) diTag.setElementLength(stream
					.readInt());
			}

			if (diTag.getElementLength() == 0 &&
				(groupWord == 0x7fe0 || tag == 0x291014))
			{
				diTag.setElementLength(getLength(stream, diTag));
			}
			else if (diTag.getElementLength() == 0) {
				stream.seek(stream.getFilePointer() - 4);
				final String v = stream.readString(2);
				if (v.equals("UT")) {
					stream.skipBytes(2);
					diTag.setElementLength(stream.readInt());
				}
				else stream.skipBytes(2);
			}

			// HACK - needed to read some GE files
			// The element length must be even!
			if (!isOddLocations && (diTag.getElementLength() % 2) == 1) diTag
				.incrementElementLength();

			// "Undefined" element length.
			// This is a sort of bracket that encloses a sequence of elements.
			if (diTag.getElementLength() == -1) {
				diTag.setElementLength(0);
				diTag.setInSequence(true);
			}
			diTag.setTagValue(tag);
			diTag.setLittleEndian(littleEndian);

			return diTag;
		}

		private static int getLength(final RandomAccessInputStream stream,
			final DICOMTag tag) throws IOException
		{
			final byte[] b = new byte[4];
			stream.read(b);

			// We cannot know whether the VR is implicit or explicit
			// without the full DICOM Data Dictionary for public and
			// private groups.

			// We will assume the VR is explicit if the two bytes
			// match the known codes. It is possible that these two
			// bytes are part of a 32-bit length for an implicit VR.

			final int vr = ((b[0] & 0xff) << 8) | (b[1] & 0xff);
			tag.setVR(vr);
			switch (vr) {
				case OB:
				case OW:
				case SQ:
				case UN:
					// Explicit VR with 32-bit length if other two bytes are zero
					if ((b[2] == 0) || (b[3] == 0)) {
						return stream.readInt();
					}
					tag.setVR(IMPLICIT_VR);
					return DataTools.bytesToInt(b, stream.isLittleEndian());
				case AE:
				case AS:
				case AT:
				case CS:
				case DA:
				case DS:
				case DT:
				case FD:
				case FL:
				case IS:
				case LO:
				case LT:
				case PN:
				case SH:
				case SL:
				case SS:
				case ST:
				case TM:
				case UI:
				case UL:
				case US:
				case UT:
				case QQ:
					// Explicit VR with 16-bit length
					if (tag.get() == 0x00283006) {
						return DataTools.bytesToInt(b, 2, 2, stream.isLittleEndian());
					}
					int n1 = DataTools.bytesToShort(b, 2, 2, stream.isLittleEndian());
					int n2 = DataTools.bytesToShort(b, 2, 2, !stream.isLittleEndian());
					n1 &= 0xffff;
					n2 &= 0xffff;
					if (n1 < 0 || n1 + stream.getFilePointer() > stream.length()) return n2;
					if (n2 < 0 || n2 + stream.getFilePointer() > stream.length()) return n1;
					return n1;
				case 0xffff:
					tag.setVR(IMPLICIT_VR);
					return 8;
				default:
					tag.setVR(IMPLICIT_VR);
					int len = DataTools.bytesToInt(b, stream.isLittleEndian());
					if (len + stream.getFilePointer() > stream.length() || len < 0) {
						len = DataTools.bytesToInt(b, 2, 2, stream.isLittleEndian());
						len &= 0xffff;
					}
					return len;
			}
		}
	}

	public static class DICOMTag {

		private int elementLength = 0;
		private int tagValue;
		private int vr = 0;
		private boolean inSequence = false;
		private int location = 0;
		private boolean littleEndian;

		public int getLocation() {
			return location;
		}

		public void setLocation(final int location) {
			this.location = location;
		}

		public void addLocation(final int offset) {
			location += offset;
		}

		public int getVR() {
			return vr;
		}

		public void setVR(final int vr) {
			this.vr = vr;
		}

		public int getElementLength() {
			return elementLength;
		}

		public void setElementLength(final int elementLength) {
			this.elementLength = elementLength;
		}

		public void incrementElementLength() {
			elementLength++;
		}

		public int get() {
			return tagValue;
		}

		public void setTagValue(final int tagValue) {
			this.tagValue = tagValue;
		}

		public boolean isInSequence() {
			return inSequence;
		}

		public void setInSequence(final boolean inSequence) {
			this.inSequence = inSequence;
		}

		public boolean isLittleEndian() {
			return littleEndian;
		}

		public void setLittleEndian(final boolean littleEndian) {
			this.littleEndian = littleEndian;
		}
	}
}
