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
import io.scif.codec.CodecService;
import io.scif.codec.JPEG2000Codec;
import io.scif.codec.JPEGCodec;
import io.scif.codec.PackbitsCodec;
import io.scif.common.DataTools;
import io.scif.config.SCIFIOConfig;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.services.InitializeService;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import net.imagej.axis.Axes;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * DICOMReader is the file format reader for DICOM files. Much of this code is
 * adapted from
 * <a href="http://imagej.net/developer/source/ij/plugin/DICOM.java.html">ImageJ
 * 's DICOM reader</a>.
 *
 * @author Mark Hiner
 */
@Plugin(type = Format.class, name = "DICOM")
public class DICOMFormat extends AbstractFormat {

	// -- Constants --

	public static final String DICOM_MAGIC_STRING = "DICM";

	private static final Hashtable<Integer, String[]> TYPES = buildTypes();

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "dic", "dcm", "dicom", "jp2", "j2ki", "j2kr", "raw",
			"ima" };
	}

	// -- Static Helper methods --

	/**
	 * Assemble the data dictionary. This is incomplete at best, since there are
	 * literally thousands of fields defined by the DICOM specifications.
	 */
	private static Hashtable<Integer, String[]> buildTypes() {
		final Hashtable<Integer, String[]> dict = new Hashtable<Integer, String[]>();

		addAttributeGroup0002(dict);
    addAttributeGroup0008(dict);
    addAttributeGroup0010(dict);
    addAttributeGroup0012(dict);
    addAttributeGroup0014(dict);
    addAttributeGroup0018(dict);
    addAttributeGroup0020(dict);
    addAttributeGroup0022(dict);
    addAttributeGroup0024(dict);
    addAttributeGroup0028(dict);
    addAttributeGroup0032(dict);
    addAttributeGroup0038(dict);
    addAttributeGroup003A(dict);
    addAttributeGroup0042(dict);
    addAttributeGroup0044(dict);
    addAttributeGroup0046(dict);
    addAttributeGroup0048(dict);
    addAttributeGroup0050(dict);
    addAttributeGroup0052(dict);
    addAttributeGroup0054(dict);
    addAttributeGroup0060(dict);
    addAttributeGroup0062(dict);
    addAttributeGroup0064(dict);
    addAttributeGroup0066(dict);
    addAttributeGroup0068(dict);
    addAttributeGroup0070(dict);
    addAttributeGroup0072(dict);
    addAttributeGroup0074(dict);
    addAttributeGroup0076(dict);
    addAttributeGroup0078(dict);
    addAttributeGroup0080(dict);
    addAttributeGroup0088(dict);
    addAttributeGroup0100(dict);
    addAttributeGroup0400(dict);
    addAttributeGroup2000(dict);
    addAttributeGroup2010(dict);
    addAttributeGroup2020(dict);
    addAttributeGroup2030(dict);
    addAttributeGroup2040(dict);
    addAttributeGroup2050(dict);
    addAttributeGroup2100(dict);
    addAttributeGroup2110(dict);
    addAttributeGroup2120(dict);
    addAttributeGroup2130(dict);
    addAttributeGroup2200(dict);
    addAttributeGroup3002(dict);
    addAttributeGroup3004(dict);
    addAttributeGroup3006(dict);
    addAttributeGroup3008(dict);
    addAttributeGroup300A(dict);
    addAttributeGroup300C(dict);
    addAttributeGroup300E(dict);
    addAttributeGroup4000(dict);
    addAttributeGroup4008(dict);
    addAttributeGroup4010(dict);
    addAttributeGroup4FFE(dict);
    addAttributeGroup5200(dict);
    addAttributeGroup5400(dict);
    addAttributeGroup5600(dict);
    addAttributeGroup7FE0(dict);
    addAttributeGroupFFFA(dict);

		return dict;
	}
  /**
   * Adds attributes of group 0x0002.
   */
  private static void addAttributeGroup0002(Hashtable<Integer, String[]> dict) {
    dict.put(0x00020000, new String[] {"File Meta Information Group Length", "UL"});
    dict.put(0x00020001, new String[] {"File Meta Information Version", "OB"});
    dict.put(0x00020002, new String[] {"Media Storage SOP Class UID", "UI"});
    dict.put(0x00020003, new String[] {"Media Storage SOP Instance UID", "UI"});
    dict.put(0x00020010, new String[] {"Transfer Syntax UID", "UI"});
    dict.put(0x00020012, new String[] {"Implementation Class UID", "UI"});
    dict.put(0x00020013, new String[] {"Implementation Version Name", "SH"});
    dict.put(0x00020016, new String[] {"Source Application Entity Title", "AE"});
    dict.put(0x00020017, new String[] {"Sending Application Entity Title", "AE"});
    dict.put(0x00020018, new String[] {"Receiving Application Entity Title", "AE"});
    dict.put(0x00020100, new String[] {"Private Information Creator UID", "UI"});
    dict.put(0x00020102, new String[] {"Private Information", "OB"});
  }

  /**
   * Adds attributes of group 0x0008.
   */
  private static void addAttributeGroup0008(Hashtable<Integer, String[]> dict) {
    dict.put(0x00080001, new String[] {"Length to End", "UL"}); // Retired
    dict.put(0x00080005, new String[] {"Specific Character Set", "CS"});
    dict.put(0x00080006, new String[] {"Language Code Sequence", "SQ"});
    dict.put(0x00080008, new String[] {"Image Type", "CS"});
    dict.put(0x00080010, new String[] {"Recognition Code", "SH"}); // Retired
    dict.put(0x00080012, new String[] {"Instance Creation Date", "DA"});
    dict.put(0x00080013, new String[] {"Instance Creation Time", "TM"});
    dict.put(0x00080014, new String[] {"Instance Creator UID", "UI"});
    dict.put(0x00080015, new String[] {"Instance Coercion DateTime", "DT"});
    dict.put(0x00080016, new String[] {"SOP Class UID", "UI"});
    dict.put(0x00080018, new String[] {"SOP Instance UID", "UI"});
    dict.put(0x0008001A, new String[] {"Related General SOP Class UID", "UI"});
    dict.put(0x0008001B, new String[] {"Original Specialized SOP ClassUID", "UI"});
    dict.put(0x00080020, new String[] {"Study Date", "DA"});
    dict.put(0x00080021, new String[] {"Series Date", "DA"});
    dict.put(0x00080022, new String[] {"Acquisition Date", "DA"});
    dict.put(0x00080023, new String[] {"Content Date", "DA"});
    dict.put(0x00080024, new String[] {"Overlay Date", "DA"}); // Retired
    dict.put(0x00080025, new String[] {"Curve Date", "DA"}); // Retired
    dict.put(0x0008002A, new String[] {"Acquisition DateTime", "DT"});
    dict.put(0x00080030, new String[] {"Study Time", "TM"});
    dict.put(0x00080031, new String[] {"Series Time", "TM"});
    dict.put(0x00080032, new String[] {"Acquisition Time", "TM"});
    dict.put(0x00080033, new String[] {"Content Time", "TM"});
    dict.put(0x00080034, new String[] {"Overlay Time", "TM"}); // Retired
    dict.put(0x00080035, new String[] {"Curve Time", "TM"}); // Retired
    dict.put(0x00080040, new String[] {"Data Set Type", "US"}); // Retired
    dict.put(0x00080041, new String[] {"Data Set Subtype", "LO"}); // Retired
    dict.put(0x00080042, new String[] {"Nuclear Medicine Series Type", "CS"}); // Retired
    dict.put(0x00080050, new String[] {"Accession Number", "SH"});
    dict.put(0x00080051, new String[] {"Issuer of Accession NumberSequence", "SQ"});
    dict.put(0x00080052, new String[] {"Query/Retrieve Level", "CS"});
    dict.put(0x00080053, new String[] {"Query/Retrieve View", "CS"});
    dict.put(0x00080054, new String[] {"Retrieve AE Title", "AE"});
    dict.put(0x00080056, new String[] {"Instance Availability", "CS"});
    dict.put(0x00080058, new String[] {"Failed SOP Instance UID List", "UI"});
    dict.put(0x00080060, new String[] {"Modality", "CS"});
    dict.put(0x00080061, new String[] {"Modalities in Study", "CS"});
    dict.put(0x00080062, new String[] {"SOP Classes in Study", "UI"});
    dict.put(0x00080064, new String[] {"Conversion Type", "CS"});
    dict.put(0x00080068, new String[] {"Presentation Intent Type", "CS"});
    dict.put(0x00080070, new String[] {"Manufacturer", "LO"});
    dict.put(0x00080080, new String[] {"Institution Name", "LO"});
    dict.put(0x00080081, new String[] {"Institution Address", "ST"});
    dict.put(0x00080082, new String[] {"Institution Code Sequence", "SQ"});
    dict.put(0x00080090, new String[] {"Referring Physician's Name", "PN"});
    dict.put(0x00080092, new String[] {"Referring Physician's Address", "ST"});
    dict.put(0x00080094, new String[] {"Referring Physician's TelephoneNumbers", "SH"});
    dict.put(0x00080096, new String[] {"Referring Physician IdentificationSequence", "SQ"});
    dict.put(0x0008009C, new String[] {"Consulting Physician's Name", "PN"});
    dict.put(0x0008009D, new String[] {"Consulting Physician IdentificationSequence", "SQ"});
    dict.put(0x00080100, new String[] {"Code Value", "SH"});
    dict.put(0x00080101, new String[] {"Extended Code Value", "LO"}); // DICOS
    dict.put(0x00080102, new String[] {"Coding Scheme Designator", "SH"});
    dict.put(0x00080103, new String[] {"Coding Scheme Version", "SH"});
    dict.put(0x00080104, new String[] {"Code Meaning", "LO"});
    dict.put(0x00080105, new String[] {"Mapping Resource", "CS"});
    dict.put(0x00080106, new String[] {"Context Group Version", "DT"});
    dict.put(0x00080107, new String[] {"Context Group Local Version", "DT"});
    dict.put(0x00080108, new String[] {"Extended Code Meaning", "LT"}); // DICOS
    dict.put(0x0008010B, new String[] {"Context Group Extension Flag", "CS"});
    dict.put(0x0008010C, new String[] {"Coding Scheme UID", "UI"});
    dict.put(0x0008010D, new String[] {"Context Group Extension CreatorUID", "UI"});
    dict.put(0x0008010F, new String[] {"Context Identifier", "CS"});
    dict.put(0x00080110, new String[] {"Coding Scheme IdentificationSequence", "SQ"});
    dict.put(0x00080112, new String[] {"Coding Scheme Registry", "LO"});
    dict.put(0x00080114, new String[] {"Coding Scheme External ID", "ST"});
    dict.put(0x00080115, new String[] {"Coding Scheme Name", "ST"});
    dict.put(0x00080116, new String[] {"Coding Scheme ResponsibleOrganization", "ST"});
    dict.put(0x00080117, new String[] {"Context UID", "UI"});
    dict.put(0x00080118, new String[] {"Mapping Resource UID", "UI"});
    dict.put(0x00080119, new String[] {"Long Code Value", "UC"});
    dict.put(0x00080120, new String[] {"URN Code Value", "UR"});
    dict.put(0x00080121, new String[] {"Equivalent Code Sequence", "SQ"});
    dict.put(0x00080201, new String[] {"Timezone Offset From UTC", "SH"});
    dict.put(0x00080300, new String[] {"Private Data ElementCharacteristics Sequence", "SQ"});
    dict.put(0x00080301, new String[] {"Private Group Reference", "US"});
    dict.put(0x00080302, new String[] {"Private Creator Reference", "LO"});
    dict.put(0x00080303, new String[] {"Block Identifying Information Status", "CS"});
    dict.put(0x00080304, new String[] {"Nonidentifying Private Elements", "US"});
    dict.put(0x00080306, new String[] {"Identifying Private Elements", "US"});
    dict.put(0x00080305, new String[] {"Deidentification Action Sequence", "SQ"});
    dict.put(0x00080307, new String[] {"Deidentification Action", "CS"});
    dict.put(0x00081000, new String[] {"Network ID", "AE"}); // Retired
    dict.put(0x00081010, new String[] {"Station Name", "SH"});
    dict.put(0x00081030, new String[] {"Study Description", "LO"});
    dict.put(0x00081032, new String[] {"Procedure Code Sequence", "SQ"});
    dict.put(0x0008103E, new String[] {"Series Description", "LO"});
    dict.put(0x0008103F, new String[] {"Series Description Code Sequence", "SQ"});
    dict.put(0x00081040, new String[] {"Institutional Department Name", "LO"});
    dict.put(0x00081048, new String[] {"Physician(s) of Record", "PN"});
    dict.put(0x00081049, new String[] {"Physician(s) of RecordIdentification Sequence", "SQ"});
    dict.put(0x00081050, new String[] {"Performing Physician's Name", "PN"});
    dict.put(0x00081052, new String[] {"Performing Physician IdentificationSequence", "SQ"});
    dict.put(0x00081060, new String[] {"Name of Physician(s) ReadingStudy", "PN"});
    dict.put(0x00081062, new String[] {"Physician(s) Reading StudyIdentification Sequence", "SQ"});
    dict.put(0x00081070, new String[] {"Operators' Name", "PN"});
    dict.put(0x00081072, new String[] {"Operator Identification Sequence", "SQ"});
    dict.put(0x00081080, new String[] {"Admitting Diagnoses Description", "LO"});
    dict.put(0x00081084, new String[] {"Admitting Diagnoses CodeSequence", "SQ"});
    dict.put(0x00081090, new String[] {"Manufacturer's Model Name", "LO"});
    dict.put(0x00081100, new String[] {"Referenced Results Sequence", "SQ"}); // Retired
    dict.put(0x00081110, new String[] {"Referenced Study Sequence", "SQ"});
    dict.put(0x00081111, new String[] {"Referenced Performed ProcedureStep Sequence", "SQ"});
    dict.put(0x00081115, new String[] {"Referenced Series Sequence", "SQ"});
    dict.put(0x00081120, new String[] {"Referenced Patient Sequence", "SQ"});
    dict.put(0x00081125, new String[] {"Referenced Visit Sequence", "SQ"});
    dict.put(0x00081130, new String[] {"Referenced Overlay Sequence", "SQ"}); // Retired
    dict.put(0x00081134, new String[] {"Referenced Stereometric InstanceSequence", "SQ"});
    dict.put(0x0008113A, new String[] {"Referenced Waveform Sequence", "SQ"});
    dict.put(0x00081140, new String[] {"Referenced Image Sequence", "SQ"});
    dict.put(0x00081145, new String[] {"Referenced Curve Sequence", "SQ"}); // Retired
    dict.put(0x0008114A, new String[] {"Referenced Instance Sequence", "SQ"});
    dict.put(0x0008114B,
             new String[] {"Referenced Real World ValueMapping Instance Sequence", "SQ"});
    dict.put(0x00081150, new String[] {"Referenced SOP Class UID", "UI"});
    dict.put(0x00081155, new String[] {"Referenced SOP Instance UID", "UI"});
    dict.put(0x0008115A, new String[] {"SOP Classes Supported", "UI"});
    dict.put(0x00081160, new String[] {"Referenced Frame Number", "IS"});
    dict.put(0x00081161, new String[] {"Simple Frame List", "UL"});
    dict.put(0x00081162, new String[] {"Calculated Frame List", "UL"});
    dict.put(0x00081163, new String[] {"Time Range", "FD"});
    dict.put(0x00081164, new String[] {"Frame Extraction Sequence", "SQ"});
    dict.put(0x00081167, new String[] {"Multi-frame Source SOP InstanceUID", "UI"});
    dict.put(0x00081190, new String[] {"Retrieve URL", "UR"});
    dict.put(0x00081195, new String[] {"Transaction UID", "UI"});
    dict.put(0x00081196, new String[] {"Warning Reason", "US"});
    dict.put(0x00081197, new String[] {"Failure Reason", "US"});
    dict.put(0x00081198, new String[] {"Failed SOP Sequence", "SQ"});
    dict.put(0x00081199, new String[] {"Referenced SOP Sequence", "SQ"});
    dict.put(0x00081200,
             new String[] {"Studies Containing OtherReferenced Instances Sequence", "SQ"});
    dict.put(0x00081250, new String[] {"Related Series Sequence", "SQ"});
    dict.put(0x00082110, new String[] {"Lossy Image Compression(Retired)", "CS"}); // Retired
    dict.put(0x00082111, new String[] {"Derivation Description", "ST"});
    dict.put(0x00082112, new String[] {"Source Image Sequence", "SQ"});
    dict.put(0x00082120, new String[] {"Stage Name", "SH"});
    dict.put(0x00082122, new String[] {"Stage Number", "IS"});
    dict.put(0x00082124, new String[] {"Number of Stages", "IS"});
    dict.put(0x00082127, new String[] {"View Name", "SH"});
    dict.put(0x00082128, new String[] {"View Number", "IS"});
    dict.put(0x00082129, new String[] {"Number of Event Timers", "IS"});
    dict.put(0x0008212A, new String[] {"Number of Views in Stage", "IS"});
    dict.put(0x00082130, new String[] {"Event Elapsed Time(s)", "DS"});
    dict.put(0x00082132, new String[] {"Event Timer Name(s)", "LO"});
    dict.put(0x00082133, new String[] {"Event Timer Sequence", "SQ"});
    dict.put(0x00082134, new String[] {"Event Time Offset", "FD"});
    dict.put(0x00082135, new String[] {"Event Code Sequence", "SQ"});
    dict.put(0x00082142, new String[] {"Start Trim", "IS"});
    dict.put(0x00082143, new String[] {"Stop Trim", "IS"});
    dict.put(0x00082144, new String[] {"Recommended Display FrameRate", "IS"});
    dict.put(0x00082200, new String[] {"Transducer Position", "CS"}); // Retired
    dict.put(0x00082204, new String[] {"Transducer Orientation", "CS"}); // Retired
    dict.put(0x00082208, new String[] {"Anatomic Structure", "CS"}); // Retired
    dict.put(0x00082218, new String[] {"Anatomic Region Sequence", "SQ"});
    dict.put(0x00082220, new String[] {"Anatomic Region ModifierSequence", "SQ"});
    dict.put(0x00082228, new String[] {"Primary Anatomic StructureSequence", "SQ"});
    dict.put(0x00082229, new String[] {"Anatomic Structure, Space orRegion Sequence", "SQ"});
    dict.put(0x00082230, new String[] {"Primary Anatomic StructureModifier Sequence", "SQ"});
    dict.put(0x00082240, new String[] {"Transducer Position Sequence", "SQ"}); // Retired
    dict.put(0x00082242, new String[] {"Transducer Position ModifierSequence", "SQ"}); // Retired
    dict.put(0x00082244, new String[] {"Transducer Orientation Sequence", "SQ"}); // Retired
    dict.put(0x00082246, new String[] {"Transducer Orientation ModifierSequence", "SQ"}); // Retired
    dict.put(0x00082251,
             new String[] {"Anatomic Structure Space OrRegion Code Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x00082253, new String[] {"Anatomic Portal Of Entrance CodeSequence (Trial)", "SQ"}); // Retired
    dict.put(0x00082255, new String[] {"Anatomic Approach Direction CodeSequence (Trial)", "SQ"}); // Retired
    dict.put(0x00082256, new String[] {"Anatomic Perspective Description(Trial)", "ST"}); // Retired
    dict.put(0x00082257, new String[] {"Anatomic Perspective CodeSequence (Trial)", "SQ"}); // Retired
    dict.put(0x00082258,
             new String[] {"Anatomic Location Of ExaminingInstrument Description (Trial)", "ST"}); // Retired
    dict.put(0x00082259,
             new String[] {"Anatomic Location Of ExaminingInstrument Code Sequence (Trial)",
                           "SQ"}); // Retired
    dict.put(0x0008225A,
             new String[] {"Anatomic Structure Space OrRegion Modifier Code Sequence(Trial)",
                           "SQ"}); // Retired
    dict.put(0x0008225C,
             new String[] {"On Axis Background AnatomicStructure Code Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x00083001, new String[] {"Alternate RepresentationSequence", "SQ"});
    dict.put(0x00083010, new String[] {"Irradiation Event UID", "UI"});
    dict.put(0x00083011, new String[] {"Source Irradiation Event Sequence", "SQ"});
    dict.put(0x00083012, new String[] {"RadiopharmaceuticalAdministration Event UID", "UI"});
    dict.put(0x00084000, new String[] {"Identifying Comments", "LT"}); // Retired
    dict.put(0x00089007, new String[] {"Frame Type", "CS"});
    dict.put(0x00089092, new String[] {"Referenced Image EvidenceSequence", "SQ"});
    dict.put(0x00089121, new String[] {"Referenced Raw Data Sequence", "SQ"});
    dict.put(0x00089123, new String[] {"Creator-Version UID", "UI"});
    dict.put(0x00089124, new String[] {"Derivation Image Sequence", "SQ"});
    dict.put(0x00089154, new String[] {"Source Image Evidence Sequence", "SQ"});
    dict.put(0x00089205, new String[] {"Pixel Presentation", "CS"});
    dict.put(0x00089206, new String[] {"Volumetric Properties", "CS"});
    dict.put(0x00089207, new String[] {"Volume Based CalculationTechnique", "CS"});
    dict.put(0x00089208, new String[] {"Complex Image Component", "CS"});
    dict.put(0x00089209, new String[] {"Acquisition Contrast", "CS"});
    dict.put(0x00089215, new String[] {"Derivation Code Sequence", "SQ"});
    dict.put(0x00089237, new String[] {"Referenced Presentation StateSequence", "SQ"});
    dict.put(0x00089410, new String[] {"Referenced Other Plane Sequence", "SQ"});
    dict.put(0x00089458, new String[] {"Frame Display Sequence", "SQ"});
    dict.put(0x00089459, new String[] {"Recommended Display FrameRate in Float", "FL"});
    dict.put(0x00089460, new String[] {"Skip Frame Range Flag", "CS"});
  }

  /**
   * Adds attributes of group 0x0010.
   */
  private static void addAttributeGroup0010(Hashtable<Integer, String[]> dict) {
    dict.put(0x00100010, new String[] {"Patient's Name", "PN"});
    dict.put(0x00100020, new String[] {"Patient ID", "LO"});
    dict.put(0x00100021, new String[] {"Issuer of Patient ID", "LO"});
    dict.put(0x00100022, new String[] {"Type of Patient ID", "CS"});
    dict.put(0x00100024, new String[] {"Issuer of Patient ID QualifiersSequence", "SQ"});
    dict.put(0x00100030, new String[] {"Patient's Birth Date", "DA"});
    dict.put(0x00100032, new String[] {"Patient's Birth Time", "TM"});
    dict.put(0x00100040, new String[] {"Patient's Sex", "CS"});
    dict.put(0x00100050, new String[] {"Patient's Insurance Plan CodeSequence", "SQ"});
    dict.put(0x00100101, new String[] {"Patient's Primary Language CodeSequence", "SQ"});
    dict.put(0x00100102, new String[] {"Patient's Primary LanguageModifier Code Sequence", "SQ"});
    dict.put(0x00100200, new String[] {"Quality Control Subject", "CS"});
    dict.put(0x00100201, new String[] {"Quality Control Subject Type CodeSequence", "SQ"});
    dict.put(0x00101000, new String[] {"Other Patient IDs", "LO"});
    dict.put(0x00101001, new String[] {"Other Patient Names", "PN"});
    dict.put(0x00101002, new String[] {"Other Patient IDs Sequence", "SQ"});
    dict.put(0x00101005, new String[] {"Patient's Birth Name", "PN"});
    dict.put(0x00101010, new String[] {"Patient's Age", "AS"});
    dict.put(0x00101020, new String[] {"Patient's Size", "DS"});
    dict.put(0x00101021, new String[] {"Patient's Size Code Sequence", "SQ"});
    dict.put(0x00101030, new String[] {"Patient's Weight", "DS"});
    dict.put(0x00101040, new String[] {"Patient's Address", "LO"});
    dict.put(0x00101050, new String[] {"Insurance Plan Identification", "LO"}); // Retired
    dict.put(0x00101060, new String[] {"Patient's Mother's Birth Name", "PN"});
    dict.put(0x00101080, new String[] {"Military Rank", "LO"});
    dict.put(0x00101081, new String[] {"Branch of Service", "LO"});
    dict.put(0x00101090, new String[] {"Medical Record Locator", "LO"});
    dict.put(0x00101100, new String[] {"Referenced Patient PhotoSequence", "SQ"});
    dict.put(0x00102000, new String[] {"Medical Alerts", "LO"});
    dict.put(0x00102110, new String[] {"Allergies", "LO"});
    dict.put(0x00102150, new String[] {"Country of Residence", "LO"});
    dict.put(0x00102152, new String[] {"Region of Residence", "LO"});
    dict.put(0x00102154, new String[] {"Patient's Telephone Numbers", "SH"});
    dict.put(0x00102155, new String[] {"Patient's Telecom Information", "LT"});
    dict.put(0x00102160, new String[] {"Ethnic Group", "SH"});
    dict.put(0x00102180, new String[] {"Occupation", "SH"});
    dict.put(0x001021A0, new String[] {"Smoking Status", "CS"});
    dict.put(0x001021B0, new String[] {"Additional Patient History", "LT"});
    dict.put(0x001021C0, new String[] {"Pregnancy Status", "US"});
    dict.put(0x001021D0, new String[] {"Last Menstrual Date", "DA"});
    dict.put(0x001021F0, new String[] {"Patient's Religious Preference", "LO"});
    dict.put(0x00102201, new String[] {"Patient Species Description", "LO"});
    dict.put(0x00102202, new String[] {"Patient Species Code Sequence", "SQ"});
    dict.put(0x00102203, new String[] {"Patient's Sex Neutered", "CS"});
    dict.put(0x00102210, new String[] {"Anatomical Orientation Type", "CS"});
    dict.put(0x00102292, new String[] {"Patient Breed Description", "LO"});
    dict.put(0x00102293, new String[] {"Patient Breed Code Sequence", "SQ"});
    dict.put(0x00102294, new String[] {"Breed Registration Sequence", "SQ"});
    dict.put(0x00102295, new String[] {"Breed Registration Number", "LO"});
    dict.put(0x00102296, new String[] {"Breed Registry Code Sequence", "SQ"});
    dict.put(0x00102297, new String[] {"Responsible Person", "PN"});
    dict.put(0x00102298, new String[] {"Responsible Person Role", "CS"});
    dict.put(0x00102299, new String[] {"Responsible Organization", "LO"});
    dict.put(0x00104000, new String[] {"Patient Comments", "LT"});
    dict.put(0x00109431, new String[] {"Examined Body Thickness", "FL"});
  }

  /**
   * Adds attributes of group 0x0012.
   */
  private static void addAttributeGroup0012(Hashtable<Integer, String[]> dict) {
    dict.put(0x00120010, new String[] {"Clinical Trial Sponsor Name", "LO"});
    dict.put(0x00120020, new String[] {"Clinical Trial Protocol ID", "LO"});
    dict.put(0x00120021, new String[] {"Clinical Trial Protocol Name", "LO"});
    dict.put(0x00120030, new String[] {"Clinical Trial Site ID", "LO"});
    dict.put(0x00120031, new String[] {"Clinical Trial Site Name", "LO"});
    dict.put(0x00120040, new String[] {"Clinical Trial Subject ID", "LO"});
    dict.put(0x00120042, new String[] {"Clinical Trial Subject Reading ID", "LO"});
    dict.put(0x00120050, new String[] {"Clinical Trial Time Point ID", "LO"});
    dict.put(0x00120051, new String[] {"Clinical Trial Time Point Description", "ST"});
    dict.put(0x00120060, new String[] {"Clinical Trial Coordinating CenterName", "LO"});
    dict.put(0x00120062, new String[] {"Patient Identity Removed", "CS"});
    dict.put(0x00120063, new String[] {"De-identification Method", "LO"});
    dict.put(0x00120064, new String[] {"De-identification Method CodeSequence", "SQ"});
    dict.put(0x00120071, new String[] {"Clinical Trial Series ID", "LO"});
    dict.put(0x00120072, new String[] {"Clinical Trial Series Description", "LO"});
    dict.put(0x00120081, new String[] {"Clinical Trial Protocol EthicsCommittee Name", "LO"});
    dict.put(0x00120082,
             new String[] {"Clinical Trial Protocol EthicsCommittee Approval Number", "LO"});
    dict.put(0x00120083, new String[] {"Consent for Clinical Trial UseSequence", "SQ"});
    dict.put(0x00120084, new String[] {"Distribution Type", "CS"});
    dict.put(0x00120085, new String[] {"Consent for Distribution Flag", "CS"});
  }

  /**
   * Adds attributes of group 0x0014.
   */
  private static void addAttributeGroup0014(Hashtable<Integer, String[]> dict) {
    dict.put(0x00140023, new String[] {"CAD File Format", "ST"}); // Retired
    dict.put(0x00140024, new String[] {"Component Reference System", "ST"}); // Retired
    dict.put(0x00140025, new String[] {"Component ManufacturingProcedure", "ST"}); // DICONDE
    dict.put(0x00140028, new String[] {"Component Manufacturer", "ST"}); // DICONDE
    dict.put(0x00140030, new String[] {"Material Thickness", "DS"}); // DICONDE
    dict.put(0x00140032, new String[] {"Material Pipe Diameter", "DS"}); // DICONDE
    dict.put(0x00140034, new String[] {"Material Isolation Diameter", "DS"}); // DICONDE
    dict.put(0x00140042, new String[] {"Material Grade", "ST"}); // DICONDE
    dict.put(0x00140044, new String[] {"Material Properties Description", "ST"}); // DICONDE
    dict.put(0x00140045, new String[] {"Material Properties File Format(Retired)", "ST"}); // Retired
    dict.put(0x00140046, new String[] {"Material Notes", "LT"}); // DICONDE
    dict.put(0x00140050, new String[] {"Component Shape", "CS"}); // DICONDE
    dict.put(0x00140052, new String[] {"Curvature Type", "CS"}); // DICONDE
    dict.put(0x00140054, new String[] {"Outer Diameter", "DS"}); // DICONDE
    dict.put(0x00140056, new String[] {"Inner Diameter", "DS"}); // DICONDE
    dict.put(0x00141010, new String[] {"Actual Environmental Conditions", "ST"}); // DICONDE
    dict.put(0x00141020, new String[] {"Expiry Date", "DA"}); // DICONDE
    dict.put(0x00141040, new String[] {"Environmental Conditions", "ST"}); // DICONDE
    dict.put(0x00142002, new String[] {"Evaluator Sequence", "SQ"}); // DICONDE
    dict.put(0x00142004, new String[] {"Evaluator Number", "IS"}); // DICONDE
    dict.put(0x00142006, new String[] {"Evaluator Name", "PN"}); // DICONDE
    dict.put(0x00142008, new String[] {"Evaluation Attempt", "IS"}); // DICONDE
    dict.put(0x00142012, new String[] {"Indication Sequence", "SQ"}); // DICONDE
    dict.put(0x00142014, new String[] {"Indication Number", "IS"}); // DICONDE
    dict.put(0x00142016, new String[] {"Indication Label", "SH"}); // DICONDE
    dict.put(0x00142018, new String[] {"Indication Description", "ST"}); // DICONDE
    dict.put(0x0014201A, new String[] {"Indication Type", "CS"}); // DICONDE
    dict.put(0x0014201C, new String[] {"Indication Disposition", "CS"}); // DICONDE
    dict.put(0x0014201E, new String[] {"Indication ROI Sequence", "SQ"}); // DICONDE
    dict.put(0x00142030, new String[] {"Indication Physical PropertySequence", "SQ"}); // DICONDE
    dict.put(0x00142032, new String[] {"Property Label", "SH"}); // DICONDE
    dict.put(0x00142202, new String[] {"Coordinate System Number ofAxes", "IS"}); // DICONDE
    dict.put(0x00142204, new String[] {"Coordinate System Axes Sequence", "SQ"}); // DICONDE
    dict.put(0x00142206, new String[] {"Coordinate System AxisDescription", "ST"}); // DICONDE
    dict.put(0x00142208, new String[] {"Coordinate System Data SetMapping", "CS"}); // DICONDE
    dict.put(0x0014220A, new String[] {"Coordinate System Axis Number", "IS"}); // DICONDE
    dict.put(0x0014220C, new String[] {"Coordinate System Axis Type", "CS"}); // DICONDE
    dict.put(0x0014220E, new String[] {"Coordinate System Axis Units", "CS"}); // DICONDE
    dict.put(0x00142210, new String[] {"Coordinate System Axis Values", "OB"}); // DICONDE
    dict.put(0x00142220, new String[] {"Coordinate System TransformSequence", "SQ"}); // DICONDE
    dict.put(0x00142222, new String[] {"Transform Description", "ST"}); // DICONDE
    dict.put(0x00142224, new String[] {"Transform Number of Axes", "IS"}); // DICONDE
    dict.put(0x00142226, new String[] {"Transform Order of Axes", "IS"}); // DICONDE
    dict.put(0x00142228, new String[] {"Transformed Axis Units", "CS"}); // DICONDE
    dict.put(0x0014222A,
             new String[] {"Coordinate System TransformRotation and Scale Matrix", "DS"}); // DICONDE
    dict.put(0x0014222C, new String[] {"Coordinate System TransformTranslation Matrix", "DS"}); // DICONDE
    dict.put(0x00143011, new String[] {"Internal Detector Frame Time", "DS"}); // DICONDE
    dict.put(0x00143012, new String[] {"Number of Frames Integrated", "DS"}); // DICONDE
    dict.put(0x00143020, new String[] {"Detector Temperature Sequence", "SQ"}); // DICONDE
    dict.put(0x00143022, new String[] {"Sensor Name", "ST"}); // DICONDE
    dict.put(0x00143024, new String[] {"Horizontal Offset of Sensor", "DS"}); // DICONDE
    dict.put(0x00143026, new String[] {"Vertical Offset of Sensor", "DS"}); // DICONDE
    dict.put(0x00143028, new String[] {"Sensor Temperature", "DS"}); // DICONDE
    dict.put(0x00143040, new String[] {"Dark Current Sequence", "SQ"}); // DICONDE
    // dict.put(0x00143050, new String[] {"Dark Current Counts", "OB or OW"}); //DICONDE
    dict.put(0x00143060, new String[] {"Gain Correction ReferenceSequence", "SQ"}); // DICONDE
    // dict.put(0x00143070, new String[] {"Air Counts", "OB or OW"}); //DICONDE
    dict.put(0x00143071, new String[] {"KV Used in Gain Calibration", "DS"}); // DICONDE
    dict.put(0x00143072, new String[] {"MA Used in Gain Calibration", "DS"}); // DICONDE
    dict.put(0x00143073, new String[] {"Number of Frames Used forIntegration", "DS"}); // DICONDE
    dict.put(0x00143074, new String[] {"Filter Material Used in GainCalibration", "LO"}); // DICONDE
    dict.put(0x00143075, new String[] {"Filter Thickness Used in GainCalibration", "DS"}); // DICONDE
    dict.put(0x00143076, new String[] {"Date of Gain Calibration", "DA"}); // DICONDE
    dict.put(0x00143077, new String[] {"Time of Gain Calibration", "TM"}); // DICONDE
    dict.put(0x00143080, new String[] {"Bad Pixel Image", "OB"}); // DICONDE
    dict.put(0x00143099, new String[] {"Calibration Notes", "LT"}); // DICONDE
    dict.put(0x00144002, new String[] {"Pulser Equipment Sequence", "SQ"}); // DICONDE
    dict.put(0x00144004, new String[] {"Pulser Type", "CS"}); // DICONDE
    dict.put(0x00144006, new String[] {"Pulser Notes", "LT"}); // DICONDE
    dict.put(0x00144008, new String[] {"Receiver Equipment Sequence", "SQ"}); // DICONDE
    dict.put(0x0014400A, new String[] {"Amplifier Type", "CS"}); // DICONDE
    dict.put(0x0014400C, new String[] {"Receiver Notes", "LT"}); // DICONDE
    dict.put(0x0014400E, new String[] {"Pre-Amplifier Equipment Sequence", "SQ"}); // DICONDE
    dict.put(0x0014400F, new String[] {"Pre-Amplifier Notes", "LT"}); // DICONDE
    dict.put(0x00144010, new String[] {"Transmit Transducer Sequence", "SQ"}); // DICONDE
    dict.put(0x00144011, new String[] {"Receive Transducer Sequence", "SQ"}); // DICONDE
    dict.put(0x00144012, new String[] {"Number of Elements", "US"}); // DICONDE
    dict.put(0x00144013, new String[] {"Element Shape", "CS"}); // DICONDE
    dict.put(0x00144014, new String[] {"Element Dimension A", "DS"}); // DICONDE
    dict.put(0x00144015, new String[] {"Element Dimension B", "DS"}); // DICONDE
    dict.put(0x00144016, new String[] {"Element Pitch A", "DS"}); // DICONDE
    dict.put(0x00144017, new String[] {"Measured Beam Dimension A", "DS"}); // DICONDE
    dict.put(0x00144018, new String[] {"Measured Beam Dimension B", "DS"}); // DICONDE
    dict.put(0x00144019, new String[] {"Location of Measured BeamDiameter", "DS"}); // DICONDE
    dict.put(0x0014401A, new String[] {"Nominal Frequency", "DS"}); // DICONDE
    dict.put(0x0014401B, new String[] {"Measured Center Frequency", "DS"}); // DICONDE
    dict.put(0x0014401C, new String[] {"Measured Bandwidth", "DS"}); // DICONDE
    dict.put(0x0014401D, new String[] {"Element Pitch B", "DS"}); // DICONDE
    dict.put(0x00144020, new String[] {"Pulser Settings Sequence", "SQ"}); // DICONDE
    dict.put(0x00144022, new String[] {"Pulse Width", "DS"}); // DICONDE
    dict.put(0x00144024, new String[] {"Excitation Frequency", "DS"}); // DICONDE
    dict.put(0x00144026, new String[] {"Modulation Type", "CS"}); // DICONDE
    dict.put(0x00144028, new String[] {"Damping", "DS"}); // DICONDE
    dict.put(0x00144030, new String[] {"Receiver Settings Sequence", "SQ"}); // DICONDE
    dict.put(0x00144031, new String[] {"Acquired Soundpath Length", "DS"}); // DICONDE
    dict.put(0x00144032, new String[] {"Acquisition Compression Type", "CS"}); // DICONDE
    dict.put(0x00144033, new String[] {"Acquisition Sample Size", "IS"}); // DICONDE
    dict.put(0x00144034, new String[] {"Rectifier Smoothing", "DS"}); // DICONDE
    dict.put(0x00144035, new String[] {"DAC Sequence", "SQ"}); // DICONDE
    dict.put(0x00144036, new String[] {"DAC Type", "CS"}); // DICONDE
    dict.put(0x00144038, new String[] {"DAC Gain Points", "DS"}); // DICONDE
    dict.put(0x0014403A, new String[] {"DAC Time Points", "DS"}); // DICONDE
    dict.put(0x0014403C, new String[] {"DAC Amplitude", "DS"}); // DICONDE
    dict.put(0x00144040, new String[] {"Pre-Amplifier Settings Sequence", "SQ"}); // DICONDE
    dict.put(0x00144050, new String[] {"Transmit Transducer SettingsSequence", "SQ"}); // DICONDE
    dict.put(0x00144051, new String[] {"Receive Transducer SettingsSequence", "SQ"}); // DICONDE
    dict.put(0x00144052, new String[] {"Incident Angle", "DS"}); // DICONDE
    dict.put(0x00144054, new String[] {"Coupling Technique", "ST"}); // DICONDE
    dict.put(0x00144056, new String[] {"Coupling Medium", "ST"}); // DICONDE
    dict.put(0x00144057, new String[] {"Coupling Velocity", "DS"}); // DICONDE
    dict.put(0x00144058, new String[] {"Probe Center Location X", "DS"}); // DICONDE
    dict.put(0x00144059, new String[] {"Probe Center Location Z", "DS"}); // DICONDE
    dict.put(0x0014405A, new String[] {"Sound Path Length", "DS"}); // DICONDE
    dict.put(0x0014405C, new String[] {"Delay Law Identifier", "ST"}); // DICONDE
    dict.put(0x00144060, new String[] {"Gate Settings Sequence", "SQ"}); // DICONDE
    dict.put(0x00144062, new String[] {"Gate Threshold", "DS"}); // DICONDE
    dict.put(0x00144064, new String[] {"Velocity of Sound", "DS"}); // DICONDE
    dict.put(0x00144070, new String[] {"Calibration Settings Sequence", "SQ"}); // DICONDE
    dict.put(0x00144072, new String[] {"Calibration Procedure", "ST"}); // DICONDE
    dict.put(0x00144074, new String[] {"Procedure Version", "SH"}); // DICONDE
    dict.put(0x00144076, new String[] {"Procedure Creation Date", "DA"}); // DICONDE
    dict.put(0x00144078, new String[] {"Procedure Expiration Date", "DA"}); // DICONDE
    dict.put(0x0014407A, new String[] {"Procedure Last Modified Date", "DA"}); // DICONDE
    dict.put(0x0014407C, new String[] {"Calibration Time", "TM"}); // DICONDE
    dict.put(0x0014407E, new String[] {"Calibration Date", "DA"}); // DICONDE
    dict.put(0x00144080, new String[] {"Probe Drive Equipment Sequence", "SQ"}); // DICONDE
    dict.put(0x00144081, new String[] {"Drive Type", "CS"}); // DICONDE
    dict.put(0x00144082, new String[] {"Probe Drive Notes", "LT"}); // DICONDE
    dict.put(0x00144083, new String[] {"Drive Probe Sequence", "SQ"}); // DICONDE
    dict.put(0x00144084, new String[] {"Probe Inductance", "DS"}); // DICONDE
    dict.put(0x00144085, new String[] {"Probe Resistance", "DS"}); // DICONDE
    dict.put(0x00144086, new String[] {"Receive Probe Sequence", "SQ"}); // DICONDE
    dict.put(0x00144087, new String[] {"Probe Drive Settings Sequence", "SQ"}); // DICONDE
    dict.put(0x00144088, new String[] {"Bridge Resistors", "DS"}); // DICONDE
    dict.put(0x00144089, new String[] {"Probe Orientation Angle", "DS"}); // DICONDE
    dict.put(0x0014408B, new String[] {"User Selected Gain Y", "DS"}); // DICONDE
    dict.put(0x0014408C, new String[] {"User Selected Phase", "DS"}); // DICONDE
    dict.put(0x0014408D, new String[] {"User Selected Offset X", "DS"}); // DICONDE
    dict.put(0x0014408E, new String[] {"User Selected Offset Y", "DS"}); // DICONDE
    dict.put(0x00144091, new String[] {"Channel Settings Sequence", "SQ"}); // DICONDE
    dict.put(0x00144092, new String[] {"Channel Threshold", "DS"}); // DICONDE
    dict.put(0x0014409A, new String[] {"Scanner Settings Sequence", "SQ"}); // DICONDE
    dict.put(0x0014409B, new String[] {"Scan Procedure", "ST"}); // DICONDE
    dict.put(0x0014409C, new String[] {"Translation Rate X", "DS"}); // DICONDE
    dict.put(0x0014409D, new String[] {"Translation Rate Y", "DS"}); // DICONDE
    dict.put(0x0014409F, new String[] {"Channel Overlap", "DS"}); // DICONDE
    dict.put(0x001440A0, new String[] {"Image Quality Indicator Type", "LO"}); // DICONDE
    dict.put(0x001440A1, new String[] {"Image Quality Indicator Material", "LO"}); // DICONDE
    dict.put(0x001440A2, new String[] {"Image Quality Indicator Size", "LO"}); // DICONDE
    dict.put(0x00145002, new String[] {"LINAC Energy", "IS"}); // DICONDE
    dict.put(0x00145004, new String[] {"LINAC Output", "IS"}); // DICONDE
    dict.put(0x00145100, new String[] {"Active Aperture", "US"}); // DICONDE
    dict.put(0x00145101, new String[] {"Total Aperture", "DS"}); // DICONDE
    dict.put(0x00145102, new String[] {"Aperture Elevation", "DS"}); // DICONDE
    dict.put(0x00145103, new String[] {"Main Lobe Angle", "DS"}); // DICONDE
    dict.put(0x00145104, new String[] {"Main Roof Angle", "DS"}); // DICONDE
    dict.put(0x00145105, new String[] {"Connector Type", "CS"}); // DICONDE
    dict.put(0x00145106, new String[] {"Wedge Model Number", "SH"}); // DICONDE
    dict.put(0x00145107, new String[] {"Wedge Angle Float", "DS"}); // DICONDE
    dict.put(0x00145108, new String[] {"Wedge Roof Angle", "DS"}); // DICONDE
    dict.put(0x00145109, new String[] {"Wedge Element 1 Position", "CS"}); // DICONDE
    dict.put(0x0014510A, new String[] {"Wedge Material Velocity", "DS"}); // DICONDE
    dict.put(0x0014510B, new String[] {"Wedge Material", "SH"}); // DICONDE
    dict.put(0x0014510C, new String[] {"Wedge Offset Z", "DS"}); // DICONDE
    dict.put(0x0014510D, new String[] {"Wedge Origin Offset X", "DS"}); // DICONDE
    dict.put(0x0014510E, new String[] {"Wedge Time Delay", "DS"}); // DICONDE
    dict.put(0x0014510F, new String[] {"Wedge Name", "SH"}); // DICONDE
    dict.put(0x00145110, new String[] {"Wedge Manufacturer Name", "SH"}); // DICONDE
    dict.put(0x00145111, new String[] {"Wedge Description", "LO"}); // DICONDE
    dict.put(0x00145112, new String[] {"Nominal Beam Angle", "DS"}); // DICONDE
    dict.put(0x00145113, new String[] {"Wedge Offset X", "DS"}); // DICONDE
    dict.put(0x00145114, new String[] {"Wedge Offset Y", "DS"}); // DICONDE
    dict.put(0x00145115, new String[] {"Wedge Total Length", "DS"}); // DICONDE
    dict.put(0x00145116, new String[] {"Wedge In Contact Length", "DS"}); // DICONDE
    dict.put(0x00145117, new String[] {"Wedge Front Gap", "DS"}); // DICONDE
    dict.put(0x00145118, new String[] {"Wedge Total Height", "DS"}); // DICONDE
    dict.put(0x00145119, new String[] {"Wedge Front Height", "DS"}); // DICONDE
    dict.put(0x0014511A, new String[] {"Wedge Rear Height", "DS"}); // DICONDE
    dict.put(0x0014511B, new String[] {"Wedge Total Width", "DS"}); // DICONDE
    dict.put(0x0014511C, new String[] {"Wedge In Contact Width", "DS"}); // DICONDE
    dict.put(0x0014511D, new String[] {"Wedge Chamfer Height", "DS"}); // DICONDE
    dict.put(0x0014511E, new String[] {"Wedge Curve", "CS"}); // DICONDE
    dict.put(0x0014511F, new String[] {"Radius Along the Wedge", "DS"}); // DICONDE
  }

  /**
   * Adds attributes of group 0x0018.
   */
  private static void addAttributeGroup0018(Hashtable<Integer, String[]> dict) {
    dict.put(0x00180010, new String[] {"Contrast/Bolus Agent", "LO"});
    dict.put(0x00180012, new String[] {"Contrast/Bolus Agent Sequence", "SQ"});
    dict.put(0x00180013, new String[] {"Contrast/Bolus T1 Relaxivity", "FL"});
    dict.put(0x00180014, new String[] {"Contrast/Bolus AdministrationRoute Sequence", "SQ"});
    dict.put(0x00180015, new String[] {"Body Part Examined", "CS"});
    dict.put(0x00180020, new String[] {"Scanning Sequence", "CS"});
    dict.put(0x00180021, new String[] {"Sequence Variant", "CS"});
    dict.put(0x00180022, new String[] {"Scan Options", "CS"});
    dict.put(0x00180023, new String[] {"MR Acquisition Type", "CS"});
    dict.put(0x00180024, new String[] {"Sequence Name", "SH"});
    dict.put(0x00180025, new String[] {"Angio Flag", "CS"});
    dict.put(0x00180026, new String[] {"Intervention Drug InformationSequence", "SQ"});
    dict.put(0x00180027, new String[] {"Intervention Drug Stop Time", "TM"});
    dict.put(0x00180028, new String[] {"Intervention Drug Dose", "DS"});
    dict.put(0x00180029, new String[] {"Intervention Drug Code Sequence", "SQ"});
    dict.put(0x0018002A, new String[] {"Additional Drug Sequence", "SQ"});
    dict.put(0x00180030, new String[] {"Radionuclide", "LO"}); // Retired
    dict.put(0x00180031, new String[] {"Radiopharmaceutical", "LO"});
    dict.put(0x00180032, new String[] {"Energy Window Centerline", "DS"}); // Retired
    dict.put(0x00180033, new String[] {"Energy Window Total Width", "DS"}); // Retired
    dict.put(0x00180034, new String[] {"Intervention Drug Name", "LO"});
    dict.put(0x00180035, new String[] {"Intervention Drug Start Time", "TM"});
    dict.put(0x00180036, new String[] {"Intervention Sequence", "SQ"});
    dict.put(0x00180037, new String[] {"Therapy Type", "CS"}); // Retired
    dict.put(0x00180038, new String[] {"Intervention Status", "CS"});
    dict.put(0x00180039, new String[] {"Therapy Description", "CS"}); // Retired
    dict.put(0x0018003A, new String[] {"Intervention Description", "ST"});
    dict.put(0x00180040, new String[] {"Cine Rate", "IS"});
    dict.put(0x00180042, new String[] {"Initial Cine Run State", "CS"});
    dict.put(0x00180050, new String[] {"Slice Thickness", "DS"});
    dict.put(0x00180060, new String[] {"KVP", "DS"});
    dict.put(0x00180070, new String[] {"Counts Accumulated", "IS"});
    dict.put(0x00180071, new String[] {"Acquisition Termination Condition", "CS"});
    dict.put(0x00180072, new String[] {"Effective Duration", "DS"});
    dict.put(0x00180073, new String[] {"Acquisition Start Condition", "CS"});
    dict.put(0x00180074, new String[] {"Acquisition Start Condition Data", "IS"});
    dict.put(0x00180075, new String[] {"Acquisition Termination ConditionData", "IS"});
    dict.put(0x00180080, new String[] {"Repetition Time", "DS"});
    dict.put(0x00180081, new String[] {"Echo Time", "DS"});
    dict.put(0x00180082, new String[] {"Inversion Time", "DS"});
    dict.put(0x00180083, new String[] {"Number of Averages", "DS"});
    dict.put(0x00180084, new String[] {"Imaging Frequency", "DS"});
    dict.put(0x00180085, new String[] {"Imaged Nucleus", "SH"});
    dict.put(0x00180086, new String[] {"Echo Number(s)", "IS"});
    dict.put(0x00180087, new String[] {"Magnetic Field Strength", "DS"});
    dict.put(0x00180088, new String[] {"Spacing Between Slices", "DS"});
    dict.put(0x00180089, new String[] {"Number of Phase Encoding Steps", "IS"});
    dict.put(0x00180090, new String[] {"Data Collection Diameter", "DS"});
    dict.put(0x00180091, new String[] {"Echo Train Length", "IS"});
    dict.put(0x00180093, new String[] {"Percent Sampling", "DS"});
    dict.put(0x00180094, new String[] {"Percent Phase Field of View", "DS"});
    dict.put(0x00180095, new String[] {"Pixel Bandwidth", "DS"});
    dict.put(0x00181000, new String[] {"Device Serial Number", "LO"});
    dict.put(0x00181002, new String[] {"Device UID", "UI"});
    dict.put(0x00181003, new String[] {"Device ID", "LO"});
    dict.put(0x00181004, new String[] {"Plate ID", "LO"});
    dict.put(0x00181005, new String[] {"Generator ID", "LO"});
    dict.put(0x00181006, new String[] {"Grid ID", "LO"});
    dict.put(0x00181007, new String[] {"Cassette ID", "LO"});
    dict.put(0x00181008, new String[] {"Gantry ID", "LO"});
    dict.put(0x00181010, new String[] {"Secondary Capture Device ID", "LO"});
    dict.put(0x00181011, new String[] {"Hardcopy Creation Device ID", "LO"}); // Retired
    dict.put(0x00181012, new String[] {"Date of Secondary Capture", "DA"});
    dict.put(0x00181014, new String[] {"Time of Secondary Capture", "TM"});
    dict.put(0x00181016, new String[] {"Secondary Capture DeviceManufacturer", "LO"});
    dict.put(0x00181017, new String[] {"Hardcopy Device Manufacturer", "LO"}); // Retired
    dict.put(0x00181018, new String[] {"Secondary Capture DeviceManufacturer's Model Name", "LO"});
    dict.put(0x00181019, new String[] {"Secondary Capture DeviceSoftware Versions", "LO"});
    dict.put(0x0018101A, new String[] {"Hardcopy Device Software Version", "LO"}); // Retired
    dict.put(0x0018101B, new String[] {"Hardcopy Device Manufacturer'sModel Name", "LO"}); // Retired
    dict.put(0x00181020, new String[] {"Software Version(s)", "LO"});
    dict.put(0x00181022, new String[] {"Video Image Format Acquired", "SH"});
    dict.put(0x00181023, new String[] {"Digital Image Format Acquired", "LO"});
    dict.put(0x00181030, new String[] {"Protocol Name", "LO"});
    dict.put(0x00181040, new String[] {"Contrast/Bolus Route", "LO"});
    dict.put(0x00181041, new String[] {"Contrast/Bolus Volume", "DS"});
    dict.put(0x00181042, new String[] {"Contrast/Bolus Start Time", "TM"});
    dict.put(0x00181043, new String[] {"Contrast/Bolus Stop Time", "TM"});
    dict.put(0x00181044, new String[] {"Contrast/Bolus Total Dose", "DS"});
    dict.put(0x00181045, new String[] {"Syringe Counts", "IS"});
    dict.put(0x00181046, new String[] {"Contrast Flow Rate", "DS"});
    dict.put(0x00181047, new String[] {"Contrast Flow Duration", "DS"});
    dict.put(0x00181048, new String[] {"Contrast/Bolus Ingredient", "CS"});
    dict.put(0x00181049, new String[] {"Contrast/Bolus IngredientConcentration", "DS"});
    dict.put(0x00181050, new String[] {"Spatial Resolution", "DS"});
    dict.put(0x00181060, new String[] {"Trigger Time", "DS"});
    // dict.put(0x00181061, new String[] {"Trigger Source or Type", "LO"});
    dict.put(0x00181062, new String[] {"Nominal Interval", "IS"});
    dict.put(0x00181063, new String[] {"Frame Time", "DS"});
    dict.put(0x00181064, new String[] {"Cardiac Framing Type", "LO"});
    dict.put(0x00181065, new String[] {"Frame Time Vector", "DS"});
    dict.put(0x00181066, new String[] {"Frame Delay", "DS"});
    dict.put(0x00181067, new String[] {"Image Trigger Delay", "DS"});
    dict.put(0x00181068, new String[] {"Multiplex Group Time Offset", "DS"});
    dict.put(0x00181069, new String[] {"Trigger Time Offset", "DS"});
    dict.put(0x0018106A, new String[] {"Synchronization Trigger", "CS"});
    dict.put(0x0018106C, new String[] {"Synchronization Channel", "US"});
    dict.put(0x0018106E, new String[] {"Trigger Sample Position", "UL"});
    dict.put(0x00181070, new String[] {"Radiopharmaceutical Route", "LO"});
    dict.put(0x00181071, new String[] {"Radiopharmaceutical Volume", "DS"});
    dict.put(0x00181072, new String[] {"Radiopharmaceutical Start Time", "TM"});
    dict.put(0x00181073, new String[] {"Radiopharmaceutical Stop Time", "TM"});
    dict.put(0x00181074, new String[] {"Radionuclide Total Dose", "DS"});
    dict.put(0x00181075, new String[] {"Radionuclide Half Life", "DS"});
    dict.put(0x00181076, new String[] {"Radionuclide Positron Fraction", "DS"});
    dict.put(0x00181077, new String[] {"Radiopharmaceutical SpecificActivity", "DS"});
    dict.put(0x00181078, new String[] {"Radiopharmaceutical StartDateTime", "DT"});
    dict.put(0x00181079, new String[] {"Radiopharmaceutical StopDateTime", "DT"});
    dict.put(0x00181080, new String[] {"Beat Rejection Flag", "CS"});
    dict.put(0x00181081, new String[] {"Low R-R Value", "IS"});
    dict.put(0x00181082, new String[] {"High R-R Value", "IS"});
    dict.put(0x00181083, new String[] {"Intervals Acquired", "IS"});
    dict.put(0x00181084, new String[] {"Intervals Rejected", "IS"});
    dict.put(0x00181085, new String[] {"PVC Rejection", "LO"});
    dict.put(0x00181086, new String[] {"Skip Beats", "IS"});
    dict.put(0x00181088, new String[] {"Heart Rate", "IS"});
    dict.put(0x00181090, new String[] {"Cardiac Number of Images", "IS"});
    dict.put(0x00181094, new String[] {"Trigger Window", "IS"});
    dict.put(0x00181100, new String[] {"Reconstruction Diameter", "DS"});
    dict.put(0x00181110, new String[] {"Distance Source to Detector", "DS"});
    dict.put(0x00181111, new String[] {"Distance Source to Patient", "DS"});
    dict.put(0x00181114, new String[] {"Estimated RadiographicMagnification Factor", "DS"});
    dict.put(0x00181120, new String[] {"Gantry/Detector Tilt", "DS"});
    dict.put(0x00181121, new String[] {"Gantry/Detector Slew", "DS"});
    dict.put(0x00181130, new String[] {"Table Height", "DS"});
    dict.put(0x00181131, new String[] {"Table Traverse", "DS"});
    dict.put(0x00181134, new String[] {"Table Motion", "CS"});
    dict.put(0x00181135, new String[] {"Table Vertical Increment", "DS"});
    dict.put(0x00181136, new String[] {"Table Lateral Increment", "DS"});
    dict.put(0x00181137, new String[] {"Table Longitudinal Increment", "DS"});
    dict.put(0x00181138, new String[] {"Table Angle", "DS"});
    dict.put(0x0018113A, new String[] {"Table Type", "CS"});
    dict.put(0x00181140, new String[] {"Rotation Direction", "CS"});
    dict.put(0x00181141, new String[] {"Angular Position", "DS"}); // Retired
    dict.put(0x00181142, new String[] {"Radial Position", "DS"});
    dict.put(0x00181143, new String[] {"Scan Arc", "DS"});
    dict.put(0x00181144, new String[] {"Angular Step", "DS"});
    dict.put(0x00181145, new String[] {"Center of Rotation Offset", "DS"});
    dict.put(0x00181146, new String[] {"Rotation Offset", "DS"}); // Retired
    dict.put(0x00181147, new String[] {"Field of View Shape", "CS"});
    dict.put(0x00181149, new String[] {"Field of View Dimension(s)", "IS"});
    dict.put(0x00181150, new String[] {"Exposure Time", "IS"});
    dict.put(0x00181151, new String[] {"X-Ray Tube Current", "IS"});
    dict.put(0x00181152, new String[] {"Exposure", "IS"});
    dict.put(0x00181153, new String[] {"Exposure in uAs", "IS"});
    dict.put(0x00181154, new String[] {"Average Pulse Width", "DS"});
    dict.put(0x00181155, new String[] {"Radiation Setting", "CS"});
    dict.put(0x00181156, new String[] {"Rectification Type", "CS"});
    dict.put(0x0018115A, new String[] {"Radiation Mode", "CS"});
    dict.put(0x0018115E, new String[] {"Image and Fluoroscopy Area DoseProduct", "DS"});
    dict.put(0x00181160, new String[] {"Filter Type", "SH"});
    dict.put(0x00181161, new String[] {"Type of Filters", "LO"});
    dict.put(0x00181162, new String[] {"Intensifier Size", "DS"});
    dict.put(0x00181164, new String[] {"Imager Pixel Spacing", "DS"});
    dict.put(0x00181166, new String[] {"Grid", "CS"});
    dict.put(0x00181170, new String[] {"Generator Power", "IS"});
    dict.put(0x00181180, new String[] {"Collimator/grid Name", "SH"});
    dict.put(0x00181181, new String[] {"Collimator Type", "CS"});
    dict.put(0x00181182, new String[] {"Focal Distance", "IS"});
    dict.put(0x00181183, new String[] {"X Focus Center", "DS"});
    dict.put(0x00181184, new String[] {"Y Focus Center", "DS"});
    dict.put(0x00181190, new String[] {"Focal Spot(s)", "DS"});
    dict.put(0x00181191, new String[] {"Anode Target Material", "CS"});
    dict.put(0x001811A0, new String[] {"Body Part Thickness", "DS"});
    dict.put(0x001811A2, new String[] {"Compression Force", "DS"});
    dict.put(0x001811A4, new String[] {"Paddle Description", "LO"});
    dict.put(0x00181200, new String[] {"Date of Last Calibration", "DA"});
    dict.put(0x00181201, new String[] {"Time of Last Calibration", "TM"});
    dict.put(0x00181202, new String[] {"DateTime of Last Calibration", "DT"});
    dict.put(0x00181210, new String[] {"Convolution Kernel", "SH"});
    dict.put(0x00181240, new String[] {"Upper/Lower Pixel Values", "IS"}); // Retired
    dict.put(0x00181242, new String[] {"Actual Frame Duration", "IS"});
    dict.put(0x00181243, new String[] {"Count Rate", "IS"});
    dict.put(0x00181244, new String[] {"Preferred Playback Sequencing", "US"});
    dict.put(0x00181250, new String[] {"Receive Coil Name", "SH"});
    dict.put(0x00181251, new String[] {"Transmit Coil Name", "SH"});
    dict.put(0x00181260, new String[] {"Plate Type", "SH"});
    dict.put(0x00181261, new String[] {"Phosphor Type", "LO"});
    dict.put(0x00181300, new String[] {"Scan Velocity", "DS"});
    dict.put(0x00181301, new String[] {"Whole Body Technique", "CS"});
    dict.put(0x00181302, new String[] {"Scan Length", "IS"});
    dict.put(0x00181310, new String[] {"Acquisition Matrix", "US"});
    dict.put(0x00181312, new String[] {"In-plane Phase Encoding Direction", "CS"});
    dict.put(0x00181314, new String[] {"Flip Angle", "DS"});
    dict.put(0x00181315, new String[] {"Variable Flip Angle Flag", "CS"});
    dict.put(0x00181316, new String[] {"SAR", "DS"});
    dict.put(0x00181318, new String[] {"dB/dt", "DS"});
    dict.put(0x00181400, new String[] {"Acquisition Device ProcessingDescription", "LO"});
    dict.put(0x00181401, new String[] {"Acquisition Device ProcessingCode", "LO"});
    dict.put(0x00181402, new String[] {"Cassette Orientation", "CS"});
    dict.put(0x00181403, new String[] {"Cassette Size", "CS"});
    dict.put(0x00181404, new String[] {"Exposures on Plate", "US"});
    dict.put(0x00181405, new String[] {"Relative X-Ray Exposure", "IS"});
    dict.put(0x00181411, new String[] {"Exposure Index", "DS"});
    dict.put(0x00181412, new String[] {"Target Exposure Index", "DS"});
    dict.put(0x00181413, new String[] {"Deviation Index", "DS"});
    dict.put(0x00181450, new String[] {"Column Angulation", "DS"});
    dict.put(0x00181460, new String[] {"Tomo Layer Height", "DS"});
    dict.put(0x00181470, new String[] {"Tomo Angle", "DS"});
    dict.put(0x00181480, new String[] {"Tomo Time", "DS"});
    dict.put(0x00181490, new String[] {"Tomo Type", "CS"});
    dict.put(0x00181491, new String[] {"Tomo Class", "CS"});
    dict.put(0x00181495, new String[] {"Number of Tomosynthesis SourceImages", "IS"});
    dict.put(0x00181500, new String[] {"Positioner Motion", "CS"});
    dict.put(0x00181508, new String[] {"Positioner Type", "CS"});
    dict.put(0x00181510, new String[] {"Positioner Primary Angle", "DS"});
    dict.put(0x00181511, new String[] {"Positioner Secondary Angle", "DS"});
    dict.put(0x00181520, new String[] {"Positioner Primary Angle Increment", "DS"});
    dict.put(0x00181521, new String[] {"Positioner Secondary AngleIncrement", "DS"});
    dict.put(0x00181530, new String[] {"Detector Primary Angle", "DS"});
    dict.put(0x00181531, new String[] {"Detector Secondary Angle", "DS"});
    dict.put(0x00181600, new String[] {"Shutter Shape", "CS"});
    dict.put(0x00181602, new String[] {"Shutter Left Vertical Edge", "IS"});
    dict.put(0x00181604, new String[] {"Shutter Right Vertical Edge", "IS"});
    dict.put(0x00181606, new String[] {"Shutter Upper Horizontal Edge", "IS"});
    dict.put(0x00181608, new String[] {"Shutter Lower Horizontal Edge", "IS"});
    dict.put(0x00181610, new String[] {"Center of Circular Shutter", "IS"});
    dict.put(0x00181612, new String[] {"Radius of Circular Shutter", "IS"});
    dict.put(0x00181620, new String[] {"Vertices of the Polygonal Shutter", "IS"});
    dict.put(0x00181622, new String[] {"Shutter Presentation Value", "US"});
    dict.put(0x00181623, new String[] {"Shutter Overlay Group", "US"});
    dict.put(0x00181624, new String[] {"Shutter Presentation Color CIELabValue", "US"});
    dict.put(0x00181700, new String[] {"Collimator Shape", "CS"});
    dict.put(0x00181702, new String[] {"Collimator Left Vertical Edge", "IS"});
    dict.put(0x00181704, new String[] {"Collimator Right Vertical Edge", "IS"});
    dict.put(0x00181706, new String[] {"Collimator Upper Horizontal Edge", "IS"});
    dict.put(0x00181708, new String[] {"Collimator Lower Horizontal Edge", "IS"});
    dict.put(0x00181710, new String[] {"Center of Circular Collimator", "IS"});
    dict.put(0x00181712, new String[] {"Radius of Circular Collimator", "IS"});
    dict.put(0x00181720, new String[] {"Vertices of the PolygonalCollimator", "IS"});
    dict.put(0x00181800, new String[] {"Acquisition Time Synchronized", "CS"});
    dict.put(0x00181801, new String[] {"Time Source", "SH"});
    dict.put(0x00181802, new String[] {"Time Distribution Protocol", "CS"});
    dict.put(0x00181803, new String[] {"NTP Source Address", "LO"});
    dict.put(0x00182001, new String[] {"Page Number Vector", "IS"});
    dict.put(0x00182002, new String[] {"Frame Label Vector", "SH"});
    dict.put(0x00182003, new String[] {"Frame Primary Angle Vector", "DS"});
    dict.put(0x00182004, new String[] {"Frame Secondary Angle Vector", "DS"});
    dict.put(0x00182005, new String[] {"Slice Location Vector", "DS"});
    dict.put(0x00182006, new String[] {"Display Window Label Vector", "SH"});
    dict.put(0x00182010, new String[] {"Nominal Scanned Pixel Spacing", "DS"});
    dict.put(0x00182020, new String[] {"Digitizing Device TransportDirection", "CS"});
    dict.put(0x00182030, new String[] {"Rotation of Scanned Film", "DS"});
    dict.put(0x00182041, new String[] {"Biopsy Target Sequence", "SQ"});
    dict.put(0x00182042, new String[] {"Target UID", "UI"});
    dict.put(0x00182043, new String[] {"Localizing Cursor Position", "FL"});
    dict.put(0x00182044, new String[] {"Calculated Target Position", "FL"});
    dict.put(0x00182045, new String[] {"Target Label", "SH"});
    dict.put(0x00182046, new String[] {"Displayed Z Value", "FL"});
    dict.put(0x00183100, new String[] {"IVUS Acquisition", "CS"});
    dict.put(0x00183101, new String[] {"IVUS Pullback Rate", "DS"});
    dict.put(0x00183102, new String[] {"IVUS Gated Rate", "DS"});
    dict.put(0x00183103, new String[] {"IVUS Pullback Start Frame Number", "IS"});
    dict.put(0x00183104, new String[] {"IVUS Pullback Stop Frame Number", "IS"});
    dict.put(0x00183105, new String[] {"Lesion Number", "IS"});
    dict.put(0x00184000, new String[] {"Acquisition Comments", "LT"}); // Retired
    dict.put(0x00185000, new String[] {"Output Power", "SH"});
    dict.put(0x00185010, new String[] {"Transducer Data", "LO"});
    dict.put(0x00185012, new String[] {"Focus Depth", "DS"});
    dict.put(0x00185020, new String[] {"Processing Function", "LO"});
    dict.put(0x00185021, new String[] {"Postprocessing Function", "LO"}); // Retired
    dict.put(0x00185022, new String[] {"Mechanical Index", "DS"});
    dict.put(0x00185024, new String[] {"Bone Thermal Index", "DS"});
    dict.put(0x00185026, new String[] {"Cranial Thermal Index", "DS"});
    dict.put(0x00185027, new String[] {"Soft Tissue Thermal Index", "DS"});
    dict.put(0x00185028, new String[] {"Soft Tissue-focus Thermal Index", "DS"});
    dict.put(0x00185029, new String[] {"Soft Tissue-surface Thermal Index", "DS"});
    dict.put(0x00185030, new String[] {"Dynamic Range", "DS"}); // Retired
    dict.put(0x00185040, new String[] {"Total Gain", "DS"}); // Retired
    dict.put(0x00185050, new String[] {"Depth of Scan Field", "IS"});
    dict.put(0x00185100, new String[] {"Patient Position", "CS"});
    dict.put(0x00185101, new String[] {"View Position", "CS"});
    dict.put(0x00185104, new String[] {"Projection Eponymous Name CodeSequence", "SQ"});
    dict.put(0x00185210, new String[] {"Image Transformation Matrix", "DS"}); // Retired
    dict.put(0x00185212, new String[] {"Image Translation Vector", "DS"}); // Retired
    dict.put(0x00186000, new String[] {"Sensitivity", "DS"});
    dict.put(0x00186011, new String[] {"Sequence of Ultrasound Regions", "SQ"});
    dict.put(0x00186012, new String[] {"Region Spatial Format", "US"});
    dict.put(0x00186014, new String[] {"Region Data Type", "US"});
    dict.put(0x00186016, new String[] {"Region Flags", "UL"});
    dict.put(0x00186018, new String[] {"Region Location Min X0", "UL"});
    dict.put(0x0018601A, new String[] {"Region Location Min Y0", "UL"});
    dict.put(0x0018601C, new String[] {"Region Location Max X1", "UL"});
    dict.put(0x0018601E, new String[] {"Region Location Max Y1", "UL"});
    dict.put(0x00186020, new String[] {"Reference Pixel X0", "SL"});
    dict.put(0x00186022, new String[] {"Reference Pixel Y0", "SL"});
    dict.put(0x00186024, new String[] {"Physical Units X Direction", "US"});
    dict.put(0x00186026, new String[] {"Physical Units Y Direction", "US"});
    dict.put(0x00186028, new String[] {"Reference Pixel Physical Value X", "FD"});
    dict.put(0x0018602A, new String[] {"Reference Pixel Physical Value Y", "FD"});
    dict.put(0x0018602C, new String[] {"Physical Delta X", "FD"});
    dict.put(0x0018602E, new String[] {"Physical Delta Y", "FD"});
    dict.put(0x00186030, new String[] {"Transducer Frequency", "UL"});
    dict.put(0x00186031, new String[] {"Transducer Type", "CS"});
    dict.put(0x00186032, new String[] {"Pulse Repetition Frequency", "UL"});
    dict.put(0x00186034, new String[] {"Doppler Correction Angle", "FD"});
    dict.put(0x00186036, new String[] {"Steering Angle", "FD"});
    dict.put(0x00186038, new String[] {"Doppler Sample Volume X Position(Retired)", "UL"}); // Retired
    dict.put(0x00186039, new String[] {"Doppler Sample Volume X Position", "SL"});
    dict.put(0x0018603A, new String[] {"Doppler Sample Volume Y Position(Retired)", "UL"}); // Retired
    dict.put(0x0018603B, new String[] {"Doppler Sample Volume Y Position", "SL"});
    dict.put(0x0018603C, new String[] {"TM-Line Position X0 (Retired)", "UL"}); // Retired
    dict.put(0x0018603D, new String[] {"TM-Line Position X0", "SL"});
    dict.put(0x0018603E, new String[] {"TM-Line Position Y0 (Retired)", "UL"}); // Retired
    dict.put(0x0018603F, new String[] {"TM-Line Position Y0", "SL"});
    dict.put(0x00186040, new String[] {"TM-Line Position X1 (Retired)", "UL"}); // Retired
    dict.put(0x00186041, new String[] {"TM-Line Position X1", "SL"});
    dict.put(0x00186042, new String[] {"TM-Line Position Y1 (Retired)", "UL"}); // Retired
    dict.put(0x00186043, new String[] {"TM-Line Position Y1", "SL"});
    dict.put(0x00186044, new String[] {"Pixel Component Organization", "US"});
    dict.put(0x00186046, new String[] {"Pixel Component Mask", "UL"});
    dict.put(0x00186048, new String[] {"Pixel Component Range Start", "UL"});
    dict.put(0x0018604A, new String[] {"Pixel Component Range Stop", "UL"});
    dict.put(0x0018604C, new String[] {"Pixel Component Physical Units", "US"});
    dict.put(0x0018604E, new String[] {"Pixel Component Data Type", "US"});
    dict.put(0x00186050, new String[] {"Number of Table Break Points", "UL"});
    dict.put(0x00186052, new String[] {"Table of X Break Points", "UL"});
    dict.put(0x00186054, new String[] {"Table of Y Break Points", "FD"});
    dict.put(0x00186056, new String[] {"Number of Table Entries", "UL"});
    dict.put(0x00186058, new String[] {"Table of Pixel Values", "UL"});
    dict.put(0x0018605A, new String[] {"Table of Parameter Values", "FL"});
    dict.put(0x00186060, new String[] {"R Wave Time Vector", "FL"});
    dict.put(0x00187000, new String[] {"Detector Conditions Nominal Flag", "CS"});
    dict.put(0x00187001, new String[] {"Detector Temperature", "DS"});
    dict.put(0x00187004, new String[] {"Detector Type", "CS"});
    dict.put(0x00187005, new String[] {"Detector Configuration", "CS"});
    dict.put(0x00187006, new String[] {"Detector Description", "LT"});
    dict.put(0x00187008, new String[] {"Detector Mode", "LT"});
    dict.put(0x0018700A, new String[] {"Detector ID", "SH"});
    dict.put(0x0018700C, new String[] {"Date of Last Detector Calibration", "DA"});
    dict.put(0x0018700E, new String[] {"Time of Last Detector Calibration", "TM"});
    dict.put(0x00187010, new String[] {"Exposures on Detector Since LastCalibration", "IS"});
    dict.put(0x00187011, new String[] {"Exposures on Detector SinceManufactured", "IS"});
    dict.put(0x00187012, new String[] {"Detector Time Since Last Exposure", "DS"});
    dict.put(0x00187014, new String[] {"Detector Active Time", "DS"});
    dict.put(0x00187016, new String[] {"Detector Activation Offset FromExposure", "DS"});
    dict.put(0x0018701A, new String[] {"Detector Binning", "DS"});
    dict.put(0x00187020, new String[] {"Detector Element Physical Size", "DS"});
    dict.put(0x00187022, new String[] {"Detector Element Spacing", "DS"});
    dict.put(0x00187024, new String[] {"Detector Active Shape", "CS"});
    dict.put(0x00187026, new String[] {"Detector Active Dimension(s)", "DS"});
    dict.put(0x00187028, new String[] {"Detector Active Origin", "DS"});
    dict.put(0x0018702A,
             new String[] {"Detector Manufacturer Name  DetectorManufacturerName  LO  1 (0018,702B) Detector Manufacturer's ModelName",
                           "LO"});
    dict.put(0x00187030, new String[] {"Field of View Origin", "DS"});
    dict.put(0x00187032, new String[] {"Field of View Rotation", "DS"});
    dict.put(0x00187034, new String[] {"Field of View Horizontal Flip", "CS"});
    dict.put(0x00187036, new String[] {"Pixel Data Area Origin Relative ToFOV", "FL"});
    dict.put(0x00187038, new String[] {"Pixel Data Area Rotation AngleRelative To FOV", "FL"});
    dict.put(0x00187040, new String[] {"Grid Absorbing Material", "LT"});
    dict.put(0x00187041, new String[] {"Grid Spacing Material", "LT"});
    dict.put(0x00187042, new String[] {"Grid Thickness", "DS"});
    dict.put(0x00187044, new String[] {"Grid Pitch", "DS"});
    dict.put(0x00187046, new String[] {"Grid Aspect Ratio", "IS"});
    dict.put(0x00187048, new String[] {"Grid Period", "DS"});
    dict.put(0x0018704C, new String[] {"Grid Focal Distance", "DS"});
    dict.put(0x00187050, new String[] {"Filter Material", "CS"});
    dict.put(0x00187052, new String[] {"Filter Thickness Minimum", "DS"});
    dict.put(0x00187054, new String[] {"Filter Thickness Maximum", "DS"});
    dict.put(0x00187056, new String[] {"Filter Beam Path Length Minimum", "FL"});
    dict.put(0x00187058, new String[] {"Filter Beam Path Length Maximum", "FL"});
    dict.put(0x00187060, new String[] {"Exposure Control Mode", "CS"});
    dict.put(0x00187062, new String[] {"Exposure Control Mode Description", "LT"});
    dict.put(0x00187064, new String[] {"Exposure Status", "CS"});
    dict.put(0x00187065, new String[] {"Phototimer Setting", "DS"});
    dict.put(0x00188150, new String[] {"Exposure Time in uS", "DS"});
    dict.put(0x00188151, new String[] {"X-Ray Tube Current in uA", "DS"});
    dict.put(0x00189004, new String[] {"Content Qualification", "CS"});
    dict.put(0x00189005, new String[] {"Pulse Sequence Name", "SH"});
    dict.put(0x00189006, new String[] {"MR Imaging Modifier Sequence", "SQ"});
    dict.put(0x00189008, new String[] {"Echo Pulse Sequence", "CS"});
    dict.put(0x00189009, new String[] {"Inversion Recovery", "CS"});
    dict.put(0x00189010, new String[] {"Flow Compensation", "CS"});
    dict.put(0x00189011, new String[] {"Multiple Spin Echo", "CS"});
    dict.put(0x00189012, new String[] {"Multi-planar Excitation", "CS"});
    dict.put(0x00189014, new String[] {"Phase Contrast", "CS"});
    dict.put(0x00189015, new String[] {"Time of Flight Contrast", "CS"});
    dict.put(0x00189016, new String[] {"Spoiling", "CS"});
    dict.put(0x00189017, new String[] {"Steady State Pulse Sequence", "CS"});
    dict.put(0x00189018, new String[] {"Echo Planar Pulse Sequence", "CS"});
    dict.put(0x00189019, new String[] {"Tag Angle First Axis", "FD"});
    dict.put(0x00189020, new String[] {"Magnetization Transfer", "CS"});
    dict.put(0x00189021, new String[] {"T2 Preparation", "CS"});
    dict.put(0x00189022, new String[] {"Blood Signal Nulling", "CS"});
    dict.put(0x00189024, new String[] {"Saturation Recovery", "CS"});
    dict.put(0x00189025, new String[] {"Spectrally Selected Suppression", "CS"});
    dict.put(0x00189026, new String[] {"Spectrally Selected Excitation", "CS"});
    dict.put(0x00189027, new String[] {"Spatial Pre-saturation", "CS"});
    dict.put(0x00189028, new String[] {"Tagging", "CS"});
    dict.put(0x00189029, new String[] {"Oversampling Phase", "CS"});
    dict.put(0x00189030, new String[] {"Tag Spacing First Dimension", "FD"});
    dict.put(0x00189032, new String[] {"Geometry of k-Space Traversal", "CS"});
    dict.put(0x00189033, new String[] {"Segmented k-Space Traversal", "CS"});
    dict.put(0x00189034, new String[] {"Rectilinear Phase EncodeReordering", "CS"});
    dict.put(0x00189035, new String[] {"Tag Thickness", "FD"});
    dict.put(0x00189036, new String[] {"Partial Fourier Direction", "CS"});
    dict.put(0x00189037, new String[] {"Cardiac Synchronization Technique", "CS"});
    dict.put(0x00189041, new String[] {"Receive Coil Manufacturer Name", "LO"});
    dict.put(0x00189042, new String[] {"MR Receive Coil Sequence", "SQ"});
    dict.put(0x00189043, new String[] {"Receive Coil Type", "CS"});
    dict.put(0x00189044, new String[] {"Quadrature Receive Coil", "CS"});
    dict.put(0x00189045, new String[] {"Multi-Coil Definition Sequence", "SQ"});
    dict.put(0x00189046, new String[] {"Multi-Coil Configuration", "LO"});
    dict.put(0x00189047, new String[] {"Multi-Coil Element Name", "SH"});
    dict.put(0x00189048, new String[] {"Multi-Coil Element Used", "CS"});
    dict.put(0x00189049, new String[] {"MR Transmit Coil Sequence", "SQ"});
    dict.put(0x00189050, new String[] {"Transmit Coil Manufacturer Name", "LO"});
    dict.put(0x00189051, new String[] {"Transmit Coil Type", "CS"});
    dict.put(0x00189052, new String[] {"Spectral Width", "FD"});
    dict.put(0x00189053, new String[] {"Chemical Shift Reference", "FD"});
    dict.put(0x00189054, new String[] {"Volume Localization Technique", "CS"});
    dict.put(0x00189058, new String[] {"MR Acquisition FrequencyEncoding Steps", "US"});
    dict.put(0x00189059, new String[] {"De-coupling", "CS"});
    dict.put(0x00189060, new String[] {"De-coupled Nucleus", "CS"});
    dict.put(0x00189061, new String[] {"De-coupling Frequency", "FD"});
    dict.put(0x00189062, new String[] {"De-coupling Method", "CS"});
    dict.put(0x00189063, new String[] {"De-coupling Chemical ShiftReference", "FD"});
    dict.put(0x00189064, new String[] {"k-space Filtering", "CS"});
    dict.put(0x00189065, new String[] {"Time Domain Filtering", "CS"});
    dict.put(0x00189066, new String[] {"Number of Zero Fills", "US"});
    dict.put(0x00189067, new String[] {"Baseline Correction", "CS"});
    dict.put(0x00189069, new String[] {"Parallel Reduction Factor In-plane", "FD"});
    dict.put(0x00189070, new String[] {"Cardiac R-R Interval Specified", "FD"});
    dict.put(0x00189073, new String[] {"Acquisition Duration", "FD"});
    dict.put(0x00189074, new String[] {"Frame Acquisition DateTime", "DT"});
    dict.put(0x00189075, new String[] {"Diffusion Directionality", "CS"});
    dict.put(0x00189076, new String[] {"Diffusion Gradient DirectionSequence", "SQ"});
    dict.put(0x00189077, new String[] {"Parallel Acquisition", "CS"});
    dict.put(0x00189078, new String[] {"Parallel Acquisition Technique", "CS"});
    dict.put(0x00189079, new String[] {"Inversion Times", "FD"});
    dict.put(0x00189080, new String[] {"Metabolite Map Description", "ST"});
    dict.put(0x00189081, new String[] {"Partial Fourier", "CS"});
    dict.put(0x00189082, new String[] {"Effective Echo Time", "FD"});
    dict.put(0x00189083, new String[] {"Metabolite Map Code Sequence", "SQ"});
    dict.put(0x00189084, new String[] {"Chemical Shift Sequence", "SQ"});
    dict.put(0x00189085, new String[] {"Cardiac Signal Source", "CS"});
    dict.put(0x00189087, new String[] {"Diffusion b-value", "FD"});
    dict.put(0x00189089, new String[] {"Diffusion Gradient Orientation", "FD"});
    dict.put(0x00189090, new String[] {"Velocity Encoding Direction", "FD"});
    dict.put(0x00189091, new String[] {"Velocity Encoding Minimum Value", "FD"});
    dict.put(0x00189092, new String[] {"Velocity Encoding AcquisitionSequence", "SQ"});
    dict.put(0x00189093, new String[] {"Number of k-Space Trajectories", "US"});
    dict.put(0x00189094, new String[] {"Coverage of k-Space", "CS"});
    dict.put(0x00189095, new String[] {"Spectroscopy Acquisition PhaseRows", "UL"});
    dict.put(0x00189096, new String[] {"Parallel Reduction Factor In-plane(Retired)", "FD"}); // Retired
    dict.put(0x00189098, new String[] {"Transmitter Frequency", "FD"});
    dict.put(0x00189100, new String[] {"Resonant Nucleus", "CS"});
    dict.put(0x00189101, new String[] {"Frequency Correction", "CS"});
    dict.put(0x00189103, new String[] {"MR Spectroscopy FOV/GeometrySequence", "SQ"});
    dict.put(0x00189104, new String[] {"Slab Thickness", "FD"});
    dict.put(0x00189105, new String[] {"Slab Orientation", "FD"});
    dict.put(0x00189106, new String[] {"Mid Slab Position", "FD"});
    dict.put(0x00189107, new String[] {"MR Spatial Saturation Sequence", "SQ"});
    dict.put(0x00189112, new String[] {"MR Timing and RelatedParameters Sequence", "SQ"});
    dict.put(0x00189114, new String[] {"MR Echo Sequence", "SQ"});
    dict.put(0x00189115, new String[] {"MR Modifier Sequence", "SQ"});
    dict.put(0x00189117, new String[] {"MR Diffusion Sequence", "SQ"});
    dict.put(0x00189118, new String[] {"Cardiac Synchronization Sequence", "SQ"});
    dict.put(0x00189119, new String[] {"MR Averages Sequence", "SQ"});
    dict.put(0x00189125, new String[] {"MR FOV/Geometry Sequence", "SQ"});
    dict.put(0x00189126, new String[] {"Volume Localization Sequence", "SQ"});
    dict.put(0x00189127, new String[] {"Spectroscopy Acquisition DataColumns", "UL"});
    dict.put(0x00189147, new String[] {"Diffusion Anisotropy Type", "CS"});
    dict.put(0x00189151, new String[] {"Frame Reference DateTime", "DT"});
    dict.put(0x00189152, new String[] {"MR Metabolite Map Sequence", "SQ"});
    dict.put(0x00189155, new String[] {"Parallel Reduction Factorout-of-plane", "FD"});
    dict.put(0x00189159, new String[] {"Spectroscopy AcquisitionOut-of-plane Phase Steps", "UL"});
    dict.put(0x00189166, new String[] {"Bulk Motion Status", "CS"}); // Retired
    dict.put(0x00189168, new String[] {"Parallel Reduction Factor SecondIn-plane", "FD"});
    dict.put(0x00189169, new String[] {"Cardiac Beat Rejection Technique", "CS"});
    dict.put(0x00189170, new String[] {"Respiratory Motion CompensationTechnique", "CS"});
    dict.put(0x00189171, new String[] {"Respiratory Signal Source", "CS"});
    dict.put(0x00189172, new String[] {"Bulk Motion CompensationTechnique", "CS"});
    dict.put(0x00189173, new String[] {"Bulk Motion Signal Source", "CS"});
    dict.put(0x00189174, new String[] {"Applicable Safety Standard Agency", "CS"});
    dict.put(0x00189175, new String[] {"Applicable Safety StandardDescription", "LO"});
    dict.put(0x00189176, new String[] {"Operating Mode Sequence", "SQ"});
    dict.put(0x00189177, new String[] {"Operating Mode Type", "CS"});
    dict.put(0x00189178, new String[] {"Operating Mode", "CS"});
    dict.put(0x00189179, new String[] {"Specific Absorption Rate Definition", "CS"});
    dict.put(0x00189180, new String[] {"Gradient Output Type", "CS"});
    dict.put(0x00189181, new String[] {"Specific Absorption Rate Value", "FD"});
    dict.put(0x00189182, new String[] {"Gradient Output", "FD"});
    dict.put(0x00189183, new String[] {"Flow Compensation Direction", "CS"});
    dict.put(0x00189184, new String[] {"Tagging Delay", "FD"});
    dict.put(0x00189185,
             new String[] {"Respiratory Motion CompensationTechnique Description", "ST"});
    dict.put(0x00189186, new String[] {"Respiratory Signal Source ID", "SH"});
    dict.put(0x00189195, new String[] {"Chemical Shift Minimum IntegrationLimit in Hz", "FD"}); // Retired
    dict.put(0x00189196, new String[] {"Chemical Shift MaximumIntegration Limit in Hz", "FD"}); // Retired
    dict.put(0x00189197, new String[] {"MR Velocity Encoding Sequence", "SQ"});
    dict.put(0x00189198, new String[] {"First Order Phase Correction", "CS"});
    dict.put(0x00189199, new String[] {"Water Referenced PhaseCorrection", "CS"});
    dict.put(0x00189200, new String[] {"MR Spectroscopy Acquisition Type", "CS"});
    dict.put(0x00189214, new String[] {"Respiratory Cycle Position", "CS"});
    dict.put(0x00189217, new String[] {"Velocity Encoding Maximum Value", "FD"});
    dict.put(0x00189218, new String[] {"Tag Spacing Second Dimension", "FD"});
    dict.put(0x00189219, new String[] {"Tag Angle Second Axis", "SS"});
    dict.put(0x00189220, new String[] {"Frame Acquisition Duration", "FD"});
    dict.put(0x00189226, new String[] {"MR Image Frame Type Sequence", "SQ"});
    dict.put(0x00189227, new String[] {"MR Spectroscopy Frame TypeSequence", "SQ"});
    dict.put(0x00189231, new String[] {"MR Acquisition Phase EncodingSteps in-plane", "US"});
    dict.put(0x00189232, new String[] {"MR Acquisition Phase EncodingSteps out-of-plane", "US"});
    dict.put(0x00189234, new String[] {"Spectroscopy Acquisition PhaseColumns", "UL"});
    dict.put(0x00189236, new String[] {"Cardiac Cycle Position", "CS"});
    dict.put(0x00189239, new String[] {"Specific Absorption Rate Sequence", "SQ"});
    dict.put(0x00189240, new String[] {"RF Echo Train Length", "US"});
    dict.put(0x00189241, new String[] {"Gradient Echo Train Length", "US"});
    dict.put(0x00189250, new String[] {"Arterial Spin Labeling Contrast", "CS"});
    dict.put(0x00189251, new String[] {"MR Arterial Spin LabelingSequence", "SQ"});
    dict.put(0x00189252, new String[] {"ASL Technique Description", "LO"});
    dict.put(0x00189253, new String[] {"ASL Slab Number", "US"});
    dict.put(0x00189254, new String[] {"ASL Slab Thickness", "FD"});
    dict.put(0x00189255, new String[] {"ASL Slab Orientation", "FD"});
    dict.put(0x00189256, new String[] {"ASL Mid Slab Position", "FD"});
    dict.put(0x00189257, new String[] {"ASL Context", "CS"});
    dict.put(0x00189258, new String[] {"ASL Pulse Train Duration", "UL"});
    dict.put(0x00189259, new String[] {"ASL Crusher Flag", "CS"});
    dict.put(0x0018925A, new String[] {"ASL Crusher Flow Limit", "FD"});
    dict.put(0x0018925B, new String[] {"ASL Crusher Description", "LO"});
    dict.put(0x0018925C, new String[] {"ASL Bolus Cut-off Flag", "CS"});
    dict.put(0x0018925D, new String[] {"ASL Bolus Cut-off TimingSequence", "SQ"});
    dict.put(0x0018925E, new String[] {"ASL Bolus Cut-off Technique", "LO"});
    dict.put(0x0018925F, new String[] {"ASL Bolus Cut-off Delay Time", "UL"});
    dict.put(0x00189260, new String[] {"ASL Slab Sequence", "SQ"});
    dict.put(0x00189295, new String[] {"Chemical Shift Minimum IntegrationLimit in ppm", "FD"});
    dict.put(0x00189296, new String[] {"Chemical Shift MaximumIntegration Limit in ppm", "FD"});
    dict.put(0x00189297, new String[] {"Water Reference Acquisition", "CS"});
    dict.put(0x00189298, new String[] {"Echo Peak Position", "IS"});
    dict.put(0x00189301, new String[] {"CT Acquisition Type Sequence", "SQ"});
    dict.put(0x00189302, new String[] {"Acquisition Type", "CS"});
    dict.put(0x00189303, new String[] {"Tube Angle", "FD"});
    dict.put(0x00189304, new String[] {"CT Acquisition Details Sequence", "SQ"});
    dict.put(0x00189305, new String[] {"Revolution Time", "FD"});
    dict.put(0x00189306, new String[] {"Single Collimation Width", "FD"});
    dict.put(0x00189307, new String[] {"Total Collimation Width", "FD"});
    dict.put(0x00189308, new String[] {"CT Table Dynamics Sequence", "SQ"});
    dict.put(0x00189309, new String[] {"Table Speed", "FD"});
    dict.put(0x00189310, new String[] {"Table Feed per Rotation", "FD"});
    dict.put(0x00189311, new String[] {"Spiral Pitch Factor", "FD"});
    dict.put(0x00189312, new String[] {"CT Geometry Sequence", "SQ"});
    dict.put(0x00189313, new String[] {"Data Collection Center (Patient)", "FD"});
    dict.put(0x00189314, new String[] {"CT Reconstruction Sequence", "SQ"});
    dict.put(0x00189315, new String[] {"Reconstruction Algorithm", "CS"});
    dict.put(0x00189316, new String[] {"Convolution Kernel Group", "CS"});
    dict.put(0x00189317, new String[] {"Reconstruction Field of View", "FD"});
    dict.put(0x00189318, new String[] {"Reconstruction Target Center(Patient)", "FD"});
    dict.put(0x00189319, new String[] {"Reconstruction Angle", "FD"});
    dict.put(0x00189320, new String[] {"Image Filter", "SH"});
    dict.put(0x00189321, new String[] {"CT Exposure Sequence", "SQ"});
    dict.put(0x00189322, new String[] {"Reconstruction Pixel Spacing", "FD"});
    dict.put(0x00189323, new String[] {"Exposure Modulation Type", "CS"});
    dict.put(0x00189324, new String[] {"Estimated Dose Saving", "FD"});
    dict.put(0x00189325, new String[] {"CT X-Ray Details Sequence", "SQ"});
    dict.put(0x00189326, new String[] {"CT Position Sequence", "SQ"});
    dict.put(0x00189327, new String[] {"Table Position", "FD"});
    dict.put(0x00189328, new String[] {"Exposure Time in ms", "FD"});
    dict.put(0x00189329, new String[] {"CT Image Frame Type Sequence", "SQ"});
    dict.put(0x00189330, new String[] {"X-Ray Tube Current in mA", "FD"});
    dict.put(0x00189332, new String[] {"Exposure in mAs", "FD"});
    dict.put(0x00189333, new String[] {"Constant Volume Flag", "CS"});
    dict.put(0x00189334, new String[] {"Fluoroscopy Flag", "CS"});
    dict.put(0x00189335, new String[] {"Distance Source to Data CollectionCenter", "FD"});
    dict.put(0x00189337, new String[] {"Contrast/Bolus Agent Number", "US"});
    dict.put(0x00189338, new String[] {"Contrast/Bolus Ingredient CodeSequence", "SQ"});
    dict.put(0x00189340, new String[] {"Contrast Administration ProfileSequence", "SQ"});
    dict.put(0x00189341, new String[] {"Contrast/Bolus Usage Sequence", "SQ"});
    dict.put(0x00189342, new String[] {"Contrast/Bolus Agent Administered", "CS"});
    dict.put(0x00189343, new String[] {"Contrast/Bolus Agent Detected", "CS"});
    dict.put(0x00189344, new String[] {"Contrast/Bolus Agent Phase", "CS"});
    dict.put(0x00189345, new String[] {"CTDIvol", "FD"});
    dict.put(0x00189346, new String[] {"CTDI Phantom Type CodeSequence", "SQ"});
    dict.put(0x00189351, new String[] {"Calcium Scoring Mass FactorPatient", "FL"});
    dict.put(0x00189352, new String[] {"Calcium Scoring Mass FactorDevice", "FL"});
    dict.put(0x00189353, new String[] {"Energy Weighting Factor", "FL"});
    dict.put(0x00189360, new String[] {"CT Additional X-Ray SourceSequence", "SQ"});
    dict.put(0x00189401, new String[] {"Projection Pixel CalibrationSequence", "SQ"});
    dict.put(0x00189402, new String[] {"Distance Source to Isocenter", "FL"});
    dict.put(0x00189403, new String[] {"Distance Object to Table Top", "FL"});
    dict.put(0x00189404, new String[] {"Object Pixel Spacing in Center ofBeam", "FL"});
    dict.put(0x00189405, new String[] {"Positioner Position Sequence", "SQ"});
    dict.put(0x00189406, new String[] {"Table Position Sequence", "SQ"});
    dict.put(0x00189407, new String[] {"Collimator Shape Sequence", "SQ"});
    dict.put(0x00189410, new String[] {"Planes in Acquisition", "CS"});
    dict.put(0x00189412, new String[] {"XA/XRF Frame CharacteristicsSequence", "SQ"});
    dict.put(0x00189417, new String[] {"Frame Acquisition Sequence", "SQ"});
    dict.put(0x00189420, new String[] {"X-Ray Receptor Type", "CS"});
    dict.put(0x00189423, new String[] {"Acquisition Protocol Name", "LO"});
    dict.put(0x00189424, new String[] {"Acquisition Protocol Description", "LT"});
    dict.put(0x00189425, new String[] {"Contrast/Bolus Ingredient Opaque", "CS"});
    dict.put(0x00189426, new String[] {"Distance Receptor Plane toDetector Housing", "FL"});
    dict.put(0x00189427, new String[] {"Intensifier Active Shape", "CS"});
    dict.put(0x00189428, new String[] {"Intensifier Active Dimension(s)", "FL"});
    dict.put(0x00189429, new String[] {"Physical Detector Size", "FL"});
    dict.put(0x00189430, new String[] {"Position of Isocenter Projection", "FL"});
    dict.put(0x00189432, new String[] {"Field of View Sequence", "SQ"});
    dict.put(0x00189433, new String[] {"Field of View Description", "LO"});
    dict.put(0x00189434, new String[] {"Exposure Control Sensing RegionsSequence", "SQ"});
    dict.put(0x00189435, new String[] {"Exposure Control Sensing RegionShape", "CS"});
    dict.put(0x00189436, new String[] {"Exposure Control Sensing RegionLeft Vertical Edge", "SS"});
    dict.put(0x00189437,
             new String[] {"Exposure Control Sensing RegionRight Vertical Edge", "SS"});
    dict.put(0x00189438,
             new String[] {"Exposure Control Sensing RegionUpper Horizontal Edge", "SS"});
    dict.put(0x00189439,
             new String[] {"Exposure Control Sensing RegionLower Horizontal Edge", "SS"});
    dict.put(0x00189440, new String[] {"Center of Circular ExposureControl Sensing Region", "SS"});
    dict.put(0x00189441, new String[] {"Radius of Circular ExposureControl Sensing Region", "US"});
    dict.put(0x00189442,
             new String[] {"Vertices of the Polygonal ExposureControl Sensing Region", "SS"});
    dict.put(0x00189445, new String[] {"Column Angulation (Patient)", "FL"});
    dict.put(0x00189449, new String[] {"Beam Angle", "FL"});
    dict.put(0x00189451, new String[] {"Frame Detector ParametersSequence", "SQ"});
    dict.put(0x00189452, new String[] {"Calculated Anatomy Thickness", "FL"});
    dict.put(0x00189455, new String[] {"Calibration Sequence", "SQ"});
    dict.put(0x00189456, new String[] {"Object Thickness Sequence", "SQ"});
    dict.put(0x00189457, new String[] {"Plane Identification", "CS"});
    dict.put(0x00189461, new String[] {"Field of View Dimension(s) in Float", "FL"});
    dict.put(0x00189462, new String[] {"Isocenter Reference SystemSequence", "SQ"});
    dict.put(0x00189463, new String[] {"Positioner Isocenter Primary Angle", "FL"});
    dict.put(0x00189464, new String[] {"Positioner Isocenter SecondaryAngle", "FL"});
    dict.put(0x00189465, new String[] {"Positioner Isocenter DetectorRotation Angle", "FL"});
    dict.put(0x00189466, new String[] {"Table X Position to Isocenter", "FL"});
    dict.put(0x00189467, new String[] {"Table Y Position to Isocenter", "FL"});
    dict.put(0x00189468, new String[] {"Table Z Position to Isocenter", "FL"});
    dict.put(0x00189469, new String[] {"Table Horizontal Rotation Angle", "FL"});
    dict.put(0x00189470, new String[] {"Table Head Tilt Angle", "FL"});
    dict.put(0x00189471, new String[] {"Table Cradle Tilt Angle", "FL"});
    dict.put(0x00189472, new String[] {"Frame Display Shutter Sequence", "SQ"});
    dict.put(0x00189473, new String[] {"Acquired Image Area Dose Product", "FL"});
    dict.put(0x00189474, new String[] {"C-arm Positioner TabletopRelationship", "CS"});
    dict.put(0x00189476, new String[] {"X-Ray Geometry Sequence", "SQ"});
    dict.put(0x00189477, new String[] {"Irradiation Event IdentificationSequence", "SQ"});
    dict.put(0x00189504, new String[] {"X-Ray 3D Frame Type Sequence", "SQ"});
    dict.put(0x00189506, new String[] {"Contributing Sources Sequence", "SQ"});
    dict.put(0x00189507, new String[] {"X-Ray 3D Acquisition Sequence", "SQ"});
    dict.put(0x00189508, new String[] {"Primary Positioner Scan Arc", "FL"});
    dict.put(0x00189509, new String[] {"Secondary Positioner Scan Arc", "FL"});
    dict.put(0x00189510, new String[] {"Primary Positioner Scan StartAngle", "FL"});
    dict.put(0x00189511, new String[] {"Secondary Positioner Scan StartAngle", "FL"});
    dict.put(0x00189514, new String[] {"Primary Positioner Increment", "FL"});
    dict.put(0x00189515, new String[] {"Secondary Positioner Increment", "FL"});
    dict.put(0x00189516, new String[] {"Start Acquisition DateTime", "DT"});
    dict.put(0x00189517, new String[] {"End Acquisition DateTime", "DT"});
    dict.put(0x00189518, new String[] {"Primary Positioner Increment Sign", "SS"});
    dict.put(0x00189519, new String[] {"Secondary Positioner IncrementSign", "SS"});
    dict.put(0x00189524, new String[] {"Application Name", "LO"});
    dict.put(0x00189525, new String[] {"Application Version", "LO"});
    dict.put(0x00189526, new String[] {"Application Manufacturer", "LO"});
    dict.put(0x00189527, new String[] {"Algorithm Type", "CS"});
    dict.put(0x00189528, new String[] {"Algorithm Description", "LO"});
    dict.put(0x00189530, new String[] {"X-Ray 3D ReconstructionSequence", "SQ"});
    dict.put(0x00189531, new String[] {"Reconstruction Description", "LO"});
    dict.put(0x00189538, new String[] {"Per Projection AcquisitionSequence", "SQ"});
    dict.put(0x00189541, new String[] {"Detector Position Sequence", "SQ"});
    dict.put(0x00189542, new String[] {"X-Ray Acquisition Dose Sequence", "SQ"});
    dict.put(0x00189543, new String[] {"X-Ray Source Isocenter PrimaryAngle", "FD"});
    dict.put(0x00189544, new String[] {"X-Ray Source Isocenter SecondaryAngle", "FD"});
    dict.put(0x00189545, new String[] {"Breast Support Isocenter PrimaryAngle", "FD"});
    dict.put(0x00189546, new String[] {"Breast Support IsocenterSecondary Angle", "FD"});
    dict.put(0x00189547, new String[] {"Breast Support X Position toIsocenter", "FD"});
    dict.put(0x00189548, new String[] {"Breast Support Y Position toIsocenter", "FD"});
    dict.put(0x00189549, new String[] {"Breast Support Z Position toIsocenter", "FD"});
    dict.put(0x00189550, new String[] {"Detector Isocenter Primary Angle", "FD"});
    dict.put(0x00189551, new String[] {"Detector Isocenter SecondaryAngle", "FD"});
    dict.put(0x00189552, new String[] {"Detector X Position to Isocenter", "FD"});
    dict.put(0x00189553, new String[] {"Detector Y Position to Isocenter", "FD"});
    dict.put(0x00189554, new String[] {"Detector Z Position to Isocenter", "FD"});
    dict.put(0x00189555, new String[] {"X-Ray Grid Sequence", "SQ"});
    dict.put(0x00189556, new String[] {"X-Ray Filter Sequence", "SQ"});
    dict.put(0x00189557, new String[] {"Detector Active Area TLHCPosition", "FD"});
    dict.put(0x00189558, new String[] {"Detector Active Area Orientation", "FD"});
    dict.put(0x00189559, new String[] {"Positioner Primary Angle Direction", "CS"});
    dict.put(0x00189601, new String[] {"Diffusion b-matrix Sequence", "SQ"});
    dict.put(0x00189602, new String[] {"Diffusion b-value XX", "FD"});
    dict.put(0x00189603, new String[] {"Diffusion b-value XY", "FD"});
    dict.put(0x00189604, new String[] {"Diffusion b-value XZ", "FD"});
    dict.put(0x00189605, new String[] {"Diffusion b-value YY", "FD"});
    dict.put(0x00189606, new String[] {"Diffusion b-value YZ", "FD"});
    dict.put(0x00189607, new String[] {"Diffusion b-value ZZ", "FD"});
    dict.put(0x00189701, new String[] {"Decay Correction DateTime", "DT"});
    dict.put(0x00189715, new String[] {"Start Density Threshold", "FD"});
    dict.put(0x00189716, new String[] {"Start Relative Density DifferenceThreshold", "FD"});
    dict.put(0x00189717, new String[] {"Start Cardiac Trigger CountThreshold", "FD"});
    dict.put(0x00189718, new String[] {"Start Respiratory Trigger CountThreshold", "FD"});
    dict.put(0x00189719, new String[] {"Termination Counts Threshold", "FD"});
    dict.put(0x00189720, new String[] {"Termination Density Threshold", "FD"});
    dict.put(0x00189721, new String[] {"Termination Relative DensityThreshold", "FD"});
    dict.put(0x00189722, new String[] {"Termination Time Threshold", "FD"});
    dict.put(0x00189723, new String[] {"Termination Cardiac Trigger CountThreshold", "FD"});
    dict.put(0x00189724, new String[] {"Termination Respiratory TriggerCount Threshold", "FD"});
    dict.put(0x00189725, new String[] {"Detector Geometry", "CS"});
    dict.put(0x00189726, new String[] {"Transverse Detector Separation", "FD"});
    dict.put(0x00189727, new String[] {"Axial Detector Dimension", "FD"});
    dict.put(0x00189729, new String[] {"Radiopharmaceutical AgentNumber", "US"});
    dict.put(0x00189732, new String[] {"PET Frame Acquisition Sequence", "SQ"});
    dict.put(0x00189733, new String[] {"PET Detector Motion DetailsSequence", "SQ"});
    dict.put(0x00189734, new String[] {"PET Table Dynamics Sequence", "SQ"});
    dict.put(0x00189735, new String[] {"PET Position Sequence", "SQ"});
    dict.put(0x00189736, new String[] {"PET Frame Correction FactorsSequence", "SQ"});
    dict.put(0x00189737, new String[] {"Radiopharmaceutical UsageSequence", "SQ"});
    dict.put(0x00189738, new String[] {"Attenuation Correction Source", "CS"});
    dict.put(0x00189739, new String[] {"Number of Iterations", "US"});
    dict.put(0x00189740, new String[] {"Number of Subsets", "US"});
    dict.put(0x00189749, new String[] {"PET Reconstruction Sequence", "SQ"});
    dict.put(0x00189751, new String[] {"PET Frame Type Sequence", "SQ"});
    dict.put(0x00189755, new String[] {"Time of Flight Information Used", "CS"});
    dict.put(0x00189756, new String[] {"Reconstruction Type", "CS"});
    dict.put(0x00189758, new String[] {"Decay Corrected", "CS"});
    dict.put(0x00189759, new String[] {"Attenuation Corrected", "CS"});
    dict.put(0x00189760, new String[] {"Scatter Corrected", "CS"});
    dict.put(0x00189761, new String[] {"Dead Time Corrected", "CS"});
    dict.put(0x00189762, new String[] {"Gantry Motion Corrected", "CS"});
    dict.put(0x00189763, new String[] {"Patient Motion Corrected", "CS"});
    dict.put(0x00189764, new String[] {"Count Loss NormalizationCorrected", "CS"});
    dict.put(0x00189765, new String[] {"Randoms Corrected", "CS"});
    dict.put(0x00189766, new String[] {"Non-uniform Radial SamplingCorrected", "CS"});
    dict.put(0x00189767, new String[] {"Sensitivity Calibrated", "CS"});
    dict.put(0x00189768, new String[] {"Detector Normalization Correction", "CS"});
    dict.put(0x00189769, new String[] {"Iterative Reconstruction Method", "CS"});
    dict.put(0x00189770, new String[] {"Attenuation Correction TemporalRelationship", "CS"});
    dict.put(0x00189771, new String[] {"Patient Physiological StateSequence", "SQ"});
    dict.put(0x00189772, new String[] {"Patient Physiological State CodeSequence", "SQ"});
    dict.put(0x00189801, new String[] {"Depth(s) of Focus", "FD"});
    dict.put(0x00189803, new String[] {"Excluded Intervals Sequence", "SQ"});
    dict.put(0x00189804, new String[] {"Exclusion Start DateTime", "DT"});
    dict.put(0x00189805, new String[] {"Exclusion Duration", "FD"});
    dict.put(0x00189806, new String[] {"US Image Description Sequence", "SQ"});
    dict.put(0x00189807, new String[] {"Image Data Type Sequence", "SQ"});
    dict.put(0x00189808, new String[] {"Data Type", "CS"});
    dict.put(0x00189809, new String[] {"Transducer Scan Pattern CodeSequence", "SQ"});
    dict.put(0x0018980B, new String[] {"Aliased Data Type", "CS"});
    dict.put(0x0018980C, new String[] {"Position Measuring Device Used", "CS"});
    dict.put(0x0018980D, new String[] {"Transducer Geometry CodeSequence", "SQ"});
    dict.put(0x0018980E, new String[] {"Transducer Beam Steering CodeSequence", "SQ"});
    dict.put(0x0018980F, new String[] {"Transducer Application CodeSequence", "SQ"});
    // dict.put(0x00189810, new String[] {"Zero Velocity Pixel Value", "US or SS"});
    dict.put(0x0018A001, new String[] {"Contributing Equipment Sequence", "SQ"});
    dict.put(0x0018A002, new String[] {"Contribution DateTime", "DT"});
    dict.put(0x0018A003, new String[] {"Contribution Description", "ST"});
  }

  /**
   * Adds attributes of group 0x0020.
   */
  private static void addAttributeGroup0020(Hashtable<Integer, String[]> dict) {
    dict.put(0x0020000D, new String[] {"Study Instance UID", "UI"});
    dict.put(0x0020000E, new String[] {"Series Instance UID", "UI"});
    dict.put(0x00200010, new String[] {"Study ID", "SH"});
    dict.put(0x00200011, new String[] {"Series Number", "IS"});
    dict.put(0x00200012, new String[] {"Acquisition Number", "IS"});
    dict.put(0x00200013, new String[] {"Instance Number", "IS"});
    dict.put(0x00200014, new String[] {"Isotope Number", "IS"}); // Retired
    dict.put(0x00200015, new String[] {"Phase Number", "IS"}); // Retired
    dict.put(0x00200016, new String[] {"Interval Number", "IS"}); // Retired
    dict.put(0x00200017, new String[] {"Time Slot Number", "IS"}); // Retired
    dict.put(0x00200018, new String[] {"Angle Number", "IS"}); // Retired
    dict.put(0x00200019, new String[] {"Item Number", "IS"});
    dict.put(0x00200020, new String[] {"Patient Orientation", "CS"});
    dict.put(0x00200022, new String[] {"Overlay Number", "IS"}); // Retired
    dict.put(0x00200024, new String[] {"Curve Number", "IS"}); // Retired
    dict.put(0x00200026, new String[] {"LUT Number", "IS"}); // Retired
    dict.put(0x00200030, new String[] {"Image Position", "DS"}); // Retired
    dict.put(0x00200032, new String[] {"Image Position (Patient)", "DS"});
    dict.put(0x00200035, new String[] {"Image Orientation", "DS"}); // Retired
    dict.put(0x00200037, new String[] {"Image Orientation (Patient)", "DS"});
    dict.put(0x00200050, new String[] {"Location", "DS"}); // Retired
    dict.put(0x00200052, new String[] {"Frame of Reference UID", "UI"});
    dict.put(0x00200060, new String[] {"Laterality", "CS"});
    dict.put(0x00200062, new String[] {"Image Laterality", "CS"});
    dict.put(0x00200070, new String[] {"Image Geometry Type", "LO"}); // Retired
    dict.put(0x00200080, new String[] {"Masking Image", "CS"}); // Retired
    dict.put(0x002000AA, new String[] {"Report Number", "IS"}); // Retired
    dict.put(0x00200100, new String[] {"Temporal Position Identifier", "IS"});
    dict.put(0x00200105, new String[] {"Number of Temporal Positions", "IS"});
    dict.put(0x00200110, new String[] {"Temporal Resolution", "DS"});
    dict.put(0x00200200, new String[] {"Synchronization Frame ofReference UID", "UI"});
    dict.put(0x00200242, new String[] {"SOP Instance UID ofConcatenation Source", "UI"});
    dict.put(0x00201000, new String[] {"Series in Study", "IS"}); // Retired
    dict.put(0x00201001, new String[] {"Acquisitions in Series", "IS"}); // Retired
    dict.put(0x00201002, new String[] {"Images in Acquisition", "IS"});
    dict.put(0x00201003, new String[] {"Images in Series", "IS"}); // Retired
    dict.put(0x00201004, new String[] {"Acquisitions in Study", "IS"}); // Retired
    dict.put(0x00201005, new String[] {"Images in Study", "IS"}); // Retired
    dict.put(0x00201020, new String[] {"Reference", "LO"}); // Retired
    dict.put(0x00201040, new String[] {"Position Reference Indicator", "LO"});
    dict.put(0x00201041, new String[] {"Slice Location", "DS"});
    dict.put(0x00201070, new String[] {"Other Study Numbers", "IS"}); // Retired
    dict.put(0x00201200, new String[] {"Number of Patient Related Studies", "IS"});
    dict.put(0x00201202, new String[] {"Number of Patient Related Series", "IS"});
    dict.put(0x00201204, new String[] {"Number of Patient RelatedInstances", "IS"});
    dict.put(0x00201206, new String[] {"Number of Study Related Series", "IS"});
    dict.put(0x00201208, new String[] {"Number of Study Related Instances", "IS"});
    dict.put(0x00201209, new String[] {"Number of Series RelatedInstances", "IS"});
    dict.put(0x00203401, new String[] {"Modifying Device ID", "CS"}); // Retired
    dict.put(0x00203402, new String[] {"Modified Image ID", "CS"}); // Retired
    dict.put(0x00203403, new String[] {"Modified Image Date", "DA"}); // Retired
    dict.put(0x00203404, new String[] {"Modifying Device Manufacturer", "LO"}); // Retired
    dict.put(0x00203405, new String[] {"Modified Image Time", "TM"}); // Retired
    dict.put(0x00203406, new String[] {"Modified Image Description", "LO"}); // Retired
    dict.put(0x00204000, new String[] {"Image Comments", "LT"});
    dict.put(0x00205000, new String[] {"Original Image Identification", "AT"}); // Retired
    dict.put(0x00205002, new String[] {"Original Image IdentificationNomenclature", "LO"}); // Retired
    dict.put(0x00209056, new String[] {"Stack ID", "SH"});
    dict.put(0x00209057, new String[] {"In-Stack Position Number", "UL"});
    dict.put(0x00209071, new String[] {"Frame Anatomy Sequence", "SQ"});
    dict.put(0x00209072, new String[] {"Frame Laterality", "CS"});
    dict.put(0x00209111, new String[] {"Frame Content Sequence", "SQ"});
    dict.put(0x00209113, new String[] {"Plane Position Sequence", "SQ"});
    dict.put(0x00209116, new String[] {"Plane Orientation Sequence", "SQ"});
    dict.put(0x00209128, new String[] {"Temporal Position Index", "UL"});
    dict.put(0x00209153, new String[] {"Nominal Cardiac Trigger DelayTime", "FD"});
    dict.put(0x00209154, new String[] {"Nominal Cardiac Trigger Time PriorTo R-Peak", "FL"});
    dict.put(0x00209155, new String[] {"Actual Cardiac Trigger Time PriorTo R-Peak", "FL"});
    dict.put(0x00209156, new String[] {"Frame Acquisition Number", "US"});
    dict.put(0x00209157, new String[] {"Dimension Index Values", "UL"});
    dict.put(0x00209158, new String[] {"Frame Comments", "LT"});
    dict.put(0x00209161, new String[] {"Concatenation UID", "UI"});
    dict.put(0x00209162, new String[] {"In-concatenation Number", "US"});
    dict.put(0x00209163, new String[] {"In-concatenation Total Number", "US"});
    dict.put(0x00209164, new String[] {"Dimension Organization UID", "UI"});
    dict.put(0x00209165, new String[] {"Dimension Index Pointer", "AT"});
    dict.put(0x00209167, new String[] {"Functional Group Pointer", "AT"});
    dict.put(0x00209170, new String[] {"Unassigned Shared ConvertedAttributes Sequence", "SQ"});
    dict.put(0x00209171, new String[] {"Unassigned Per-Frame ConvertedAttributes Sequence", "SQ"});
    dict.put(0x00209172, new String[] {"Conversion Source AttributesSequence", "SQ"});
    dict.put(0x00209213, new String[] {"Dimension Index Private Creator", "LO"});
    dict.put(0x00209221, new String[] {"Dimension Organization Sequence", "SQ"});
    dict.put(0x00209222, new String[] {"Dimension Index Sequence", "SQ"});
    dict.put(0x00209228, new String[] {"Concatenation Frame OffsetNumber", "UL"});
    dict.put(0x00209238, new String[] {"Functional Group Private Creator", "LO"});
    dict.put(0x00209241, new String[] {"Nominal Percentage of CardiacPhase", "FL"});
    dict.put(0x00209245, new String[] {"Nominal Percentage of RespiratoryPhase", "FL"});
    dict.put(0x00209246, new String[] {"Starting Respiratory Amplitude", "FL"});
    dict.put(0x00209247, new String[] {"Starting Respiratory Phase", "CS"});
    dict.put(0x00209248, new String[] {"Ending Respiratory Amplitude", "FL"});
    dict.put(0x00209249, new String[] {"Ending Respiratory Phase", "CS"});
    dict.put(0x00209250, new String[] {"Respiratory Trigger Type", "CS"});
    dict.put(0x00209251, new String[] {"R-R Interval Time Nominal", "FD"});
    dict.put(0x00209252, new String[] {"Actual Cardiac Trigger Delay Time", "FD"});
    dict.put(0x00209253, new String[] {"Respiratory SynchronizationSequence", "SQ"});
    dict.put(0x00209254, new String[] {"Respiratory Interval Time", "FD"});
    dict.put(0x00209255, new String[] {"Nominal Respiratory Trigger DelayTime", "FD"});
    dict.put(0x00209256, new String[] {"Respiratory Trigger DelayThreshold", "FD"});
    dict.put(0x00209257, new String[] {"Actual Respiratory Trigger DelayTime", "FD"});
    dict.put(0x00209301, new String[] {"Image Position (Volume)", "FD"});
    dict.put(0x00209302, new String[] {"Image Orientation (Volume)", "FD"});
    dict.put(0x00209307, new String[] {"Ultrasound Acquisition Geometry", "CS"});
    dict.put(0x00209308, new String[] {"Apex Position", "FD"});
    dict.put(0x00209309, new String[] {"Volume to Transducer MappingMatrix", "FD"});
    dict.put(0x0020930A, new String[] {"Volume to Table Mapping Matrix", "FD"});
    dict.put(0x0020930B, new String[] {"Volume to Transducer Relationship", "CS"});
    dict.put(0x0020930C, new String[] {"Patient Frame of Reference Source", "CS"});
    dict.put(0x0020930D, new String[] {"Temporal Position Time Offset", "FD"});
    dict.put(0x0020930E, new String[] {"Plane Position (Volume) Sequence", "SQ"});
    dict.put(0x0020930F, new String[] {"Plane Orientation (Volume)  Sequence", "SQ"});
    dict.put(0x00209310, new String[] {"Temporal Position Sequence", "SQ"});
    dict.put(0x00209311, new String[] {"Dimension Organization Type", "CS"});
    dict.put(0x00209312, new String[] {"Volume Frame of Reference UID", "UI"});
    dict.put(0x00209313, new String[] {"Table Frame of Reference UID", "UI"});
    dict.put(0x00209421, new String[] {"Dimension Description Label", "LO"});
    dict.put(0x00209450, new String[] {"Patient Orientation in FrameSequence", "SQ"});
    dict.put(0x00209453, new String[] {"Frame Label", "LO"});
    dict.put(0x00209518, new String[] {"Acquisition Index", "US"});
    dict.put(0x00209529, new String[] {"Contributing SOP InstancesReference Sequence", "SQ"});
    dict.put(0x00209536, new String[] {"Reconstruction Index", "US"});
  }

  /**
   * Adds attributes of group 0x0022.
   */
  private static void addAttributeGroup0022(Hashtable<Integer, String[]> dict) {
    dict.put(0x00220001, new String[] {"Light Path Filter Pass-ThroughWavelength", "US"});
    dict.put(0x00220002, new String[] {"Light Path Filter Pass Band", "US"});
    dict.put(0x00220003, new String[] {"Image Path Filter Pass-ThroughWavelength", "US"});
    dict.put(0x00220004, new String[] {"Image Path Filter Pass Band", "US"});
    dict.put(0x00220005, new String[] {"Patient Eye MovementCommanded", "CS"});
    dict.put(0x00220006, new String[] {"Patient Eye Movement CommandCode Sequence", "SQ"});
    dict.put(0x00220007, new String[] {"Spherical Lens Power", "FL"});
    dict.put(0x00220008, new String[] {"Cylinder Lens Power", "FL"});
    dict.put(0x00220009, new String[] {"Cylinder Axis", "FL"});
    dict.put(0x0022000A, new String[] {"Emmetropic Magnification", "FL"});
    dict.put(0x0022000B, new String[] {"Intra Ocular Pressure", "FL"});
    dict.put(0x0022000C, new String[] {"Horizontal Field of View", "FL"});
    dict.put(0x0022000D, new String[] {"Pupil Dilated", "CS"});
    dict.put(0x0022000E, new String[] {"Degree of Dilation", "FL"});
    dict.put(0x00220010, new String[] {"Stereo Baseline Angle", "FL"});
    dict.put(0x00220011, new String[] {"Stereo Baseline Displacement", "FL"});
    dict.put(0x00220012, new String[] {"Stereo Horizontal Pixel Offset", "FL"});
    dict.put(0x00220013, new String[] {"Stereo Vertical Pixel Offset", "FL"});
    dict.put(0x00220014, new String[] {"Stereo Rotation", "FL"});
    dict.put(0x00220015, new String[] {"Acquisition Device Type CodeSequence", "SQ"});
    dict.put(0x00220016, new String[] {"Illumination Type Code Sequence", "SQ"});
    dict.put(0x00220017, new String[] {"Light Path Filter Type Stack CodeSequence", "SQ"});
    dict.put(0x00220018, new String[] {"Image Path Filter Type Stack CodeSequence", "SQ"});
    dict.put(0x00220019, new String[] {"Lenses Code Sequence", "SQ"});
    dict.put(0x0022001A, new String[] {"Channel Description CodeSequence", "SQ"});
    dict.put(0x0022001B, new String[] {"Refractive State Sequence", "SQ"});
    dict.put(0x0022001C, new String[] {"Mydriatic Agent Code Sequence", "SQ"});
    dict.put(0x0022001D, new String[] {"Relative Image Position CodeSequence", "SQ"});
    dict.put(0x0022001E, new String[] {"Camera Angle of View", "FL"});
    dict.put(0x00220020, new String[] {"Stereo Pairs Sequence", "SQ"});
    dict.put(0x00220021, new String[] {"Left Image Sequence", "SQ"});
    dict.put(0x00220022, new String[] {"Right Image Sequence", "SQ"});
    dict.put(0x00220028, new String[] {"Stereo Pairs Present", "CS"});
    dict.put(0x00220030, new String[] {"Axial Length of the Eye", "FL"});
    dict.put(0x00220031, new String[] {"Ophthalmic Frame LocationSequence", "SQ"});
    dict.put(0x00220032, new String[] {"Reference Coordinates", "FL"});
    dict.put(0x00220035, new String[] {"Depth Spatial Resolution", "FL"});
    dict.put(0x00220036, new String[] {"Maximum Depth Distortion", "FL"});
    dict.put(0x00220037, new String[] {"Along-scan Spatial Resolution", "FL"});
    dict.put(0x00220038, new String[] {"Maximum Along-scan Distortion", "FL"});
    dict.put(0x00220039, new String[] {"Ophthalmic Image Orientation", "CS"});
    dict.put(0x00220041, new String[] {"Depth of Transverse Image", "FL"});
    dict.put(0x00220042, new String[] {"Mydriatic Agent ConcentrationUnits Sequence", "SQ"});
    dict.put(0x00220048, new String[] {"Across-scan Spatial Resolution", "FL"});
    dict.put(0x00220049, new String[] {"Maximum Across-scan Distortion", "FL"});
    dict.put(0x0022004E, new String[] {"Mydriatic Agent Concentration", "DS"});
    dict.put(0x00220055, new String[] {"Illumination Wave Length", "FL"});
    dict.put(0x00220056, new String[] {"Illumination Power", "FL"});
    dict.put(0x00220057, new String[] {"Illumination Bandwidth", "FL"});
    dict.put(0x00220058, new String[] {"Mydriatic Agent Sequence", "SQ"});
    dict.put(0x00221007, new String[] {"Ophthalmic Axial MeasurementsRight Eye Sequence", "SQ"});
    dict.put(0x00221008, new String[] {"Ophthalmic Axial MeasurementsLeft Eye Sequence", "SQ"});
    dict.put(0x00221009, new String[] {"Ophthalmic Axial MeasurementsDevice Type", "CS"});
    dict.put(0x00221010, new String[] {"Ophthalmic Axial LengthMeasurements Type", "CS"});
    dict.put(0x00221012, new String[] {"Ophthalmic Axial Length Sequence", "SQ"});
    dict.put(0x00221019, new String[] {"Ophthalmic Axial Length", "FL"});
    dict.put(0x00221024, new String[] {"Lens Status Code Sequence", "SQ"});
    dict.put(0x00221025, new String[] {"Vitreous Status Code Sequence", "SQ"});
    dict.put(0x00221028, new String[] {"IOL Formula Code Sequence", "SQ"});
    dict.put(0x00221029, new String[] {"IOL Formula Detail", "LO"});
    dict.put(0x00221033, new String[] {"Keratometer Index", "FL"});
    dict.put(0x00221035, new String[] {"Source of Ophthalmic Axial LengthCode Sequence", "SQ"});
    dict.put(0x00221037, new String[] {"Target Refraction", "FL"});
    dict.put(0x00221039, new String[] {"Refractive Procedure Occurred", "CS"});
    dict.put(0x00221040, new String[] {"Refractive Surgery Type CodeSequence", "SQ"});
    dict.put(0x00221044, new String[] {"Ophthalmic Ultrasound MethodCode Sequence", "SQ"});
    dict.put(0x00221050, new String[] {"Ophthalmic Axial LengthMeasurements Sequence", "SQ"});
    dict.put(0x00221053, new String[] {"IOL Power", "FL"});
    dict.put(0x00221054, new String[] {"Predicted Refractive Error", "FL"});
    dict.put(0x00221059, new String[] {"Ophthalmic Axial Length Velocity", "FL"});
    dict.put(0x00221065, new String[] {"Lens Status Description", "LO"});
    dict.put(0x00221066, new String[] {"Vitreous Status Description", "LO"});
    dict.put(0x00221090, new String[] {"IOL Power Sequence", "SQ"});
    dict.put(0x00221092, new String[] {"Lens Constant Sequence", "SQ"});
    dict.put(0x00221093, new String[] {"IOL Manufacturer", "LO"});
    dict.put(0x00221094, new String[] {"Lens Constant Description", "LO"}); // Retired
    dict.put(0x00221095, new String[] {"Implant Name", "LO"});
    dict.put(0x00221096, new String[] {"Keratometry Measurement TypeCode Sequence", "SQ"});
    dict.put(0x00221097, new String[] {"Implant Part Number", "LO"});
    dict.put(0x00221100, new String[] {"Referenced Ophthalmic AxialMeasurements Sequence", "SQ"});
    dict.put(0x00221101,
             new String[] {"Ophthalmic Axial LengthMeasurements Segment NameCode Sequence", "SQ"});
    dict.put(0x00221103,
             new String[] {"Refractive Error Before RefractiveSurgery Code Sequence", "SQ"});
    dict.put(0x00221121, new String[] {"IOL Power For Exact Emmetropia", "FL"});
    dict.put(0x00221122, new String[] {"IOL Power For Exact TargetRefraction", "FL"});
    dict.put(0x00221125, new String[] {"Anterior Chamber Depth DefinitionCode Sequence", "SQ"});
    dict.put(0x00221127, new String[] {"Lens Thickness Sequence", "SQ"});
    dict.put(0x00221128, new String[] {"Anterior Chamber Depth Sequence", "SQ"});
    dict.put(0x00221130, new String[] {"Lens Thickness", "FL"});
    dict.put(0x00221131, new String[] {"Anterior Chamber Depth", "FL"});
    dict.put(0x00221132, new String[] {"Source of Lens Thickness DataCode Sequence", "SQ"});
    dict.put(0x00221133,
             new String[] {"Source of Anterior Chamber DepthData Code Sequence", "SQ"});
    dict.put(0x00221134, new String[] {"Source of RefractiveMeasurements Sequence", "SQ"});
    dict.put(0x00221135, new String[] {"Source of RefractiveMeasurements Code Sequence", "SQ"});
    dict.put(0x00221140, new String[] {"Ophthalmic Axial LengthMeasurement Modified", "CS"});
    dict.put(0x00221150, new String[] {"Ophthalmic Axial Length DataSource Code Sequence", "SQ"});
    dict.put(0x00221153,
             new String[] {"Ophthalmic Axial LengthAcquisition Method CodeSequence", "SQ"}); // Retired
    dict.put(0x00221155, new String[] {"Signal to Noise Ratio", "FL"});
    dict.put(0x00221159, new String[] {"Ophthalmic Axial Length DataSource Description", "LO"});
    dict.put(0x00221210,
             new String[] {"Ophthalmic Axial LengthMeasurements Total LengthSequence", "SQ"});
    dict.put(0x00221211,
             new String[] {"Ophthalmic Axial LengthMeasurements Segmental LengthSequence", "SQ"});
    dict.put(0x00221212,
             new String[] {"Ophthalmic Axial LengthMeasurements Length SummationSequence", "SQ"});
    dict.put(0x00221220,
             new String[] {"Ultrasound Ophthalmic AxialLength Measurements Sequence", "SQ"});
    dict.put(0x00221225,
             new String[] {"Optical Ophthalmic Axial LengthMeasurements Sequence", "SQ"});
    dict.put(0x00221230,
             new String[] {"Ultrasound Selected OphthalmicAxial Length Sequence", "SQ"});
    dict.put(0x00221250,
             new String[] {"Ophthalmic Axial Length SelectionMethod Code Sequence", "SQ"});
    dict.put(0x00221255, new String[] {"Optical Selected Ophthalmic AxialLength Sequence", "SQ"});
    dict.put(0x00221257,
             new String[] {"Selected Segmental OphthalmicAxial Length Sequence", "SQ"});
    dict.put(0x00221260, new String[] {"Selected Total Ophthalmic AxialLength Sequence", "SQ"});
    dict.put(0x00221262, new String[] {"Ophthalmic Axial Length QualityMetric Sequence", "SQ"});
    dict.put(0x00221265,
             new String[] {"Ophthalmic Axial Length QualityMetric Type Code Sequence", "SQ"}); // Retired
    dict.put(0x00221273,
             new String[] {"Ophthalmic Axial Length QualityMetric Type Description", "LO"}); // Retired
    dict.put(0x00221300, new String[] {"Intraocular Lens Calculations RightEye Sequence", "SQ"});
    dict.put(0x00221310, new String[] {"Intraocular Lens Calculations LeftEye Sequence", "SQ"});
    dict.put(0x00221330,
             new String[] {"Referenced Ophthalmic AxialLength Measurement QC ImageSequence",
                           "SQ"});
    dict.put(0x00221415, new String[] {"Ophthalmic Mapping Device Type", "CS"});
    dict.put(0x00221420, new String[] {"Acquisition Method CodeSequence", "SQ"});
    dict.put(0x00221423, new String[] {"Acquisition Method AlgorithmSequence", "SQ"});
    dict.put(0x00221436, new String[] {"Ophthalmic Thickness Map TypeCode Sequence", "SQ"});
    dict.put(0x00221443, new String[] {"Ophthalmic Thickness MappingNormals Sequence", "SQ"});
    dict.put(0x00221445, new String[] {"Retinal Thickness Definition CodeSequence", "SQ"});
    dict.put(0x00221450, new String[] {"Pixel Value Mapping to CodedConcept Sequence", "SQ"});
    // dict.put(0x00221452, new String[] {"Mapped Pixel Value", "US or SS"});
    dict.put(0x00221454, new String[] {"Pixel Value Mapping Explanation", "LO"});
    dict.put(0x00221458,
             new String[] {"Ophthalmic Thickness Map QualityThreshold Sequence", "SQ"});
    dict.put(0x00221460, new String[] {"Ophthalmic Thickness MapThreshold Quality Rating", "FL"});
    dict.put(0x00221463, new String[] {"Anatomic Structure ReferencePoint", "FL"});
    dict.put(0x00221465, new String[] {"Registration to Localizer Sequence", "SQ"});
    dict.put(0x00221466, new String[] {"Registered Localizer Units", "CS"});
    dict.put(0x00221467, new String[] {"Registered Localizer Top Left HandCorner", "FL"});
    dict.put(0x00221468, new String[] {"Registered Localizer Bottom RightHand Corner", "FL"});
    dict.put(0x00221470, new String[] {"Ophthalmic Thickness Map QualityRating Sequence", "SQ"});
    dict.put(0x00221472, new String[] {"Relevant OPT Attributes Sequence", "SQ"});
    dict.put(0x00221512, new String[] {"Transformation Method CodeSequence", "SQ"});
    dict.put(0x00221513, new String[] {"Transformation AlgorithmSequence", "SQ"});
    dict.put(0x00221515, new String[] {"Ophthalmic Axial Length Method", "CS"});
    dict.put(0x00221517, new String[] {"Ophthalmic FOV", "FL"});
    dict.put(0x00221518, new String[] {"Two Dimensional to ThreeDimensional Map Sequence", "SQ"});
    dict.put(0x00221525,
             new String[] {"Wide Field OphthalmicPhotography Quality RatingSequence", "SQ"});
    dict.put(0x00221526,
             new String[] {"Wide Field OphthalmicPhotography Quality ThresholdSequence", "SQ"});
    dict.put(0x00221527,
             new String[] {"Wide Field OphthalmicPhotography Threshold QualityRating", "FL"});
    dict.put(0x00221528, new String[] {"X Coordinates Center Pixel ViewAngle", "FL"});
    dict.put(0x00221529, new String[] {"Y Coordinates Center Pixel ViewAngle", "FL"});
    dict.put(0x00221530, new String[] {"Number of Map Points", "UL"});
    dict.put(0x00221531, new String[] {"Two Dimensional to ThreeDimensional Map Data", "OF"});
  }

  /**
   * Adds attributes of group 0x0024.
   */
  private static void addAttributeGroup0024(Hashtable<Integer, String[]> dict) {
    dict.put(0x00240010, new String[] {"Visual Field Horizontal Extent", "FL"});
    dict.put(0x00240011, new String[] {"Visual Field Vertical Extent", "FL"});
    dict.put(0x00240012, new String[] {"Visual Field Shape", "CS"});
    dict.put(0x00240016, new String[] {"Screening Test Mode CodeSequence", "SQ"});
    dict.put(0x00240018, new String[] {"Maximum Stimulus Luminance", "FL"});
    dict.put(0x00240020, new String[] {"Background Luminance", "FL"});
    dict.put(0x00240021, new String[] {"Stimulus Color Code Sequence", "SQ"});
    dict.put(0x00240024, new String[] {"Background Illumination ColorCode Sequence", "SQ"});
    dict.put(0x00240025, new String[] {"Stimulus Area", "FL"});
    dict.put(0x00240028, new String[] {"Stimulus Presentation Time", "FL"});
    dict.put(0x00240032, new String[] {"Fixation Sequence", "SQ"});
    dict.put(0x00240033, new String[] {"Fixation Monitoring CodeSequence", "SQ"});
    dict.put(0x00240034, new String[] {"Visual Field Catch Trial Sequence", "SQ"});
    dict.put(0x00240035, new String[] {"Fixation Checked Quantity", "US"});
    dict.put(0x00240036, new String[] {"Patient Not Properly FixatedQuantity", "US"});
    dict.put(0x00240037, new String[] {"Presented Visual Stimuli Data Flag", "CS"});
    dict.put(0x00240038, new String[] {"Number of Visual Stimuli", "US"});
    dict.put(0x00240039, new String[] {"Excessive Fixation Losses DataFlag", "CS"});
    dict.put(0x00240040, new String[] {"Excessive Fixation Losses", "CS"});
    dict.put(0x00240042, new String[] {"Stimuli Retesting Quantity", "US"});
    dict.put(0x00240044, new String[] {"Comments on Patient'sPerformance of Visual Field", "LT"});
    dict.put(0x00240045, new String[] {"False Negatives Estimate Flag", "CS"});
    dict.put(0x00240046, new String[] {"False Negatives Estimate", "FL"});
    dict.put(0x00240048, new String[] {"Negative Catch Trials Quantity", "US"});
    dict.put(0x00240050, new String[] {"False Negatives Quantity", "US"});
    dict.put(0x00240051, new String[] {"Excessive False Negatives DataFlag", "CS"});
    dict.put(0x00240052, new String[] {"Excessive False Negatives", "CS"});
    dict.put(0x00240053, new String[] {"False Positives Estimate Flag", "CS"});
    dict.put(0x00240054, new String[] {"False Positives Estimate", "FL"});
    dict.put(0x00240055, new String[] {"Catch Trials Data Flag", "CS"});
    dict.put(0x00240056, new String[] {"Positive Catch Trials Quantity", "US"});
    dict.put(0x00240057, new String[] {"Test Point Normals Data Flag", "CS"});
    dict.put(0x00240058, new String[] {"Test Point Normals Sequence", "SQ"});
    dict.put(0x00240059, new String[] {"Global Deviation ProbabilityNormals Flag", "CS"});
    dict.put(0x00240060, new String[] {"False Positives Quantity", "US"});
    dict.put(0x00240061, new String[] {"Excessive False Positives DataFlag", "CS"});
    dict.put(0x00240062, new String[] {"Excessive False Positives", "CS"});
    dict.put(0x00240063, new String[] {"Visual Field Test Normals Flag", "CS"});
    dict.put(0x00240064, new String[] {"Results Normals Sequence", "SQ"});
    dict.put(0x00240065,
             new String[] {"Age Corrected Sensitivity DeviationAlgorithm Sequence", "SQ"});
    dict.put(0x00240066, new String[] {"Global Deviation From Normal", "FL"});
    dict.put(0x00240067,
             new String[] {"Generalized Defect SensitivityDeviation Algorithm Sequence", "SQ"});
    dict.put(0x00240068, new String[] {"Localized Deviation From Normal", "FL"});
    dict.put(0x00240069, new String[] {"Patient Reliability Indicator", "LO"});
    dict.put(0x00240070, new String[] {"Visual Field Mean Sensitivity", "FL"});
    dict.put(0x00240071, new String[] {"Global Deviation Probability", "FL"});
    dict.put(0x00240072, new String[] {"Local Deviation Probability NormalsFlag", "CS"});
    dict.put(0x00240073, new String[] {"Localized Deviation Probability", "FL"});
    dict.put(0x00240074, new String[] {"Short Term Fluctuation Calculated", "CS"});
    dict.put(0x00240075, new String[] {"Short Term Fluctuation", "FL"});
    dict.put(0x00240076, new String[] {"Short Term Fluctuation ProbabilityCalculated", "CS"});
    dict.put(0x00240077, new String[] {"Short Term Fluctuation Probability", "FL"});
    dict.put(0x00240078,
             new String[] {"Corrected Localized DeviationFrom Normal Calculated", "CS"});
    dict.put(0x00240079, new String[] {"Corrected Localized DeviationFrom Normal", "FL"});
    dict.put(0x00240080,
             new String[] {"Corrected Localized DeviationFrom Normal Probability Calculated",
                           "CS"});
    dict.put(0x00240081,
             new String[] {"Corrected Localized DeviationFrom Normal Probability", "FL"});
    dict.put(0x00240083, new String[] {"Global Deviation ProbabilitySequence", "SQ"});
    dict.put(0x00240085, new String[] {"Localized Deviation ProbabilitySequence", "SQ"});
    dict.put(0x00240086, new String[] {"Foveal Sensitivity Measured", "CS"});
    dict.put(0x00240087, new String[] {"Foveal Sensitivity", "FL"});
    dict.put(0x00240088, new String[] {"Visual Field Test Duration", "FL"});
    dict.put(0x00240089, new String[] {"Visual Field Test Point Sequence", "SQ"});
    dict.put(0x00240090, new String[] {"Visual Field Test PointX-Coordinate", "FL"});
    dict.put(0x00240091, new String[] {"Visual Field Test PointY-Coordinate", "FL"});
    dict.put(0x00240092, new String[] {"Age Corrected Sensitivity DeviationValue", "FL"});
    dict.put(0x00240093, new String[] {"Stimulus Results", "CS"});
    dict.put(0x00240094, new String[] {"Sensitivity Value", "FL"});
    dict.put(0x00240095, new String[] {"Retest Stimulus Seen", "CS"});
    dict.put(0x00240096, new String[] {"Retest Sensitivity Value", "FL"});
    dict.put(0x00240097, new String[] {"Visual Field Test Point NormalsSequence", "SQ"});
    dict.put(0x00240098, new String[] {"Quantified Defect", "FL"});
    dict.put(0x00240100,
             new String[] {"Age Corrected Sensitivity DeviationProbability Value", "FL"});
    dict.put(0x00240102,
             new String[] {"Generalized Defect CorrectedSensitivity Deviation Flag", "CS"});
    dict.put(0x00240103,
             new String[] {"Generalized Defect CorrectedSensitivity Deviation Value", "FL"});
    dict.put(0x00240104,
             new String[] {"Generalized Defect CorrectedSensitivity Deviation ProbabilityValue",
                           "FL"});
    dict.put(0x00240105, new String[] {"Minimum Sensitivity Value", "FL"});
    dict.put(0x00240106, new String[] {"Blind Spot Localized", "CS"});
    dict.put(0x00240107, new String[] {"Blind Spot X-Coordinate", "FL"});
    dict.put(0x00240108, new String[] {"Blind Spot Y-Coordinate", "FL"});
    dict.put(0x00240110, new String[] {"Visual Acuity MeasurementSequence", "SQ"});
    dict.put(0x00240112, new String[] {"Refractive Parameters Used onPatient Sequence", "SQ"});
    dict.put(0x00240113, new String[] {"Measurement Laterality", "CS"});
    dict.put(0x00240114,
             new String[] {"Ophthalmic Patient ClinicalInformation Left Eye Sequence", "SQ"});
    dict.put(0x00240115,
             new String[] {"Ophthalmic Patient ClinicalInformation Right Eye Sequence", "SQ"});
    dict.put(0x00240117, new String[] {"Foveal Point Normative Data Flag", "CS"});
    dict.put(0x00240118, new String[] {"Foveal Point Probability Value", "FL"});
    dict.put(0x00240120, new String[] {"Screening Baseline Measured", "CS"});
    dict.put(0x00240122, new String[] {"Screening Baseline MeasuredSequence", "SQ"});
    dict.put(0x00240124, new String[] {"Screening Baseline Type", "CS"});
    dict.put(0x00240126, new String[] {"Screening Baseline Value", "FL"});
    dict.put(0x00240202, new String[] {"Algorithm Source", "LO"});
    dict.put(0x00240306, new String[] {"Data Set Name", "LO"});
    dict.put(0x00240307, new String[] {"Data Set Version", "LO"});
    dict.put(0x00240308, new String[] {"Data Set Source", "LO"});
    dict.put(0x00240309, new String[] {"Data Set Description", "LO"});
    dict.put(0x00240317,
             new String[] {"Visual Field Test Reliability GlobalIndex Sequence", "SQ"});
    dict.put(0x00240320, new String[] {"Visual Field Global Results IndexSequence", "SQ"});
    dict.put(0x00240325, new String[] {"Data Observation Sequence", "SQ"});
    dict.put(0x00240338, new String[] {"Index Normals Flag", "CS"});
    dict.put(0x00240341, new String[] {"Index Probability", "FL"});
    dict.put(0x00240344, new String[] {"Index Probability Sequence", "SQ"});
  }

  /**
   * Adds attributes of group 0x0028.
   */
  private static void addAttributeGroup0028(Hashtable<Integer, String[]> dict) {
    dict.put(0x00280002, new String[] {"Samples per Pixel", "US"});
    dict.put(0x00280003, new String[] {"Samples per Pixel Used", "US"});
    dict.put(0x00280004, new String[] {"Photometric Interpretation", "CS"});
    dict.put(0x00280005, new String[] {"Image Dimensions", "US"}); // Retired
    dict.put(0x00280006, new String[] {"Planar Configuration", "US"});
    dict.put(0x00280008, new String[] {"Number of Frames", "IS"});
    dict.put(0x00280009, new String[] {"Frame Increment Pointer", "AT"});
    dict.put(0x0028000A, new String[] {"Frame Dimension Pointer", "AT"});
    dict.put(0x00280010, new String[] {"Rows", "US"});
    dict.put(0x00280011, new String[] {"Columns", "US"});
    dict.put(0x00280012, new String[] {"Planes", "US"}); // Retired
    dict.put(0x00280014, new String[] {"Ultrasound Color Data Present", "US"});
    dict.put(0x00280030, new String[] {"Pixel Spacing", "DS"});
    dict.put(0x00280031, new String[] {"Zoom Factor", "DS"});
    dict.put(0x00280032, new String[] {"Zoom Center", "DS"});
    dict.put(0x00280034, new String[] {"Pixel Aspect Ratio", "IS"});
    dict.put(0x00280040, new String[] {"Image Format", "CS"}); // Retired
    dict.put(0x00280050, new String[] {"Manipulated Image", "LO"}); // Retired
    dict.put(0x00280051, new String[] {"Corrected Image", "CS"});
    dict.put(0x0028005F, new String[] {"Compression Recognition Code", "LO"}); // Retired
    dict.put(0x00280060, new String[] {"Compression Code", "CS"}); // Retired
    dict.put(0x00280061, new String[] {"Compression Originator", "SH"}); // Retired
    dict.put(0x00280062, new String[] {"Compression Label", "LO"}); // Retired
    dict.put(0x00280063, new String[] {"Compression Description", "SH"}); // Retired
    dict.put(0x00280065, new String[] {"Compression Sequence", "CS"}); // Retired
    dict.put(0x00280066, new String[] {"Compression Step Pointers", "AT"}); // Retired
    dict.put(0x00280068, new String[] {"Repeat Interval", "US"}); // Retired
    dict.put(0x00280069, new String[] {"Bits Grouped", "US"}); // Retired
    dict.put(0x00280070, new String[] {"Perimeter Table", "US"}); // Retired
    // dict.put(0x00280071, new String[] {"Perimeter Value", "US or SS"}); //Retired
    dict.put(0x00280080, new String[] {"Predictor Rows", "US"}); // Retired
    dict.put(0x00280081, new String[] {"Predictor Columns", "US"}); // Retired
    dict.put(0x00280082, new String[] {"Predictor Constants", "US"}); // Retired
    dict.put(0x00280090, new String[] {"Blocked Pixels", "CS"}); // Retired
    dict.put(0x00280091, new String[] {"Block Rows", "US"}); // Retired
    dict.put(0x00280092, new String[] {"Block Columns", "US"}); // Retired
    dict.put(0x00280093, new String[] {"Row Overlap", "US"}); // Retired
    dict.put(0x00280094, new String[] {"Column Overlap", "US"}); // Retired
    dict.put(0x00280100, new String[] {"Bits Allocated", "US"});
    dict.put(0x00280101, new String[] {"Bits Stored", "US"});
    dict.put(0x00280102, new String[] {"High Bit", "US"});
    dict.put(0x00280103, new String[] {"Pixel Representation", "US"});
    // dict.put(0x00280104, new String[] {"Smallest Valid Pixel Value", "US or SS"}); //Retired
    // dict.put(0x00280105, new String[] {"Largest Valid Pixel Value", "US or SS"}); //Retired
    // dict.put(0x00280106, new String[] {"Smallest Image Pixel Value", "US or SS"});
    // dict.put(0x00280107, new String[] {"Largest Image Pixel Value", "US or SS"});
    // dict.put(0x00280108, new String[] {"Smallest Pixel Value in Series", "US or SS"});
    // dict.put(0x00280109, new String[] {"Largest Pixel Value in Series", "US or SS"});
    // dict.put(0x00280110, new String[] {"Smallest Image Pixel Value inPlane", "US or SS1"});
    // //Retired
    // dict.put(0x00280111, new String[] {"Largest Image Pixel Value in Plane", "US or SS"});
    // //Retired
    // dict.put(0x00280120, new String[] {"Pixel Padding Value", "US or SS"});
    // dict.put(0x00280121, new String[] {"Pixel Padding Range Limit", "US or SS"});
    dict.put(0x00280122, new String[] {"Float Pixel Padding Value", "FL"});
    dict.put(0x00280123, new String[] {"Double Float Pixel Padding Value", "FD"});
    dict.put(0x00280124, new String[] {"Float Pixel Padding Range Limit", "FL"});
    dict.put(0x00280125, new String[] {"Double Float Pixel Padding RangeLimit", "FD"});
    dict.put(0x00280200, new String[] {"Image Location", "US"}); // Retired
    dict.put(0x00280300, new String[] {"Quality Control Image", "CS"});
    dict.put(0x00280301, new String[] {"Burned In Annotation", "CS"});
    dict.put(0x00280302, new String[] {"Recognizable Visual Features", "CS"});
    dict.put(0x00280303, new String[] {"Longitudinal Temporal InformationModified", "CS"});
    dict.put(0x00280304, new String[] {"Referenced Color Palette InstanceUID", "UI"});
    dict.put(0x00280400, new String[] {"Transform Label", "LO"}); // Retired
    dict.put(0x00280401, new String[] {"Transform Version Number", "LO"}); // Retired
    dict.put(0x00280402, new String[] {"Number of Transform Steps", "US"}); // Retired
    dict.put(0x00280403, new String[] {"Sequence of Compressed Data", "LO"}); // Retired
    dict.put(0x00280404, new String[] {"Details of Coefficients", "AT"}); // Retired
    dict.put(0x00280700, new String[] {"DCT Label", "LO"}); // Retired
    dict.put(0x00280701, new String[] {"Data Block Description", "CS"}); // Retired
    dict.put(0x00280702, new String[] {"Data Block", "AT"}); // Retired
    dict.put(0x00280710, new String[] {"Normalization Factor Format", "US"}); // Retired
    dict.put(0x00280720, new String[] {"Zonal Map Number Format", "US"}); // Retired
    dict.put(0x00280721, new String[] {"Zonal Map Location", "AT"}); // Retired
    dict.put(0x00280722, new String[] {"Zonal Map Format", "US"}); // Retired
    dict.put(0x00280730, new String[] {"Adaptive Map Format", "US"}); // Retired
    dict.put(0x00280740, new String[] {"Code Number Format", "US"}); // Retired
    dict.put(0x00280A02, new String[] {"Pixel Spacing Calibration Type", "CS"});
    dict.put(0x00280A04, new String[] {"Pixel Spacing CalibrationDescription", "LO"});
    dict.put(0x00281040, new String[] {"Pixel Intensity Relationship", "CS"});
    dict.put(0x00281041, new String[] {"Pixel Intensity Relationship Sign", "SS"});
    dict.put(0x00281050, new String[] {"Window Center", "DS"});
    dict.put(0x00281051, new String[] {"Window Width", "DS"});
    dict.put(0x00281052, new String[] {"Rescale Intercept", "DS"});
    dict.put(0x00281053, new String[] {"Rescale Slope", "DS"});
    dict.put(0x00281054, new String[] {"Rescale Type", "LO"});
    dict.put(0x00281055, new String[] {"Window Center & WidthExplanation", "LO"});
    dict.put(0x00281056, new String[] {"VOI LUT Function", "CS"});
    dict.put(0x00281080, new String[] {"Gray Scale", "CS"}); // Retired
    dict.put(0x00281090, new String[] {"Recommended Viewing Mode", "CS"});
    // dict.put(0x00281100, new String[] {"Gray Lookup Table Descriptor", "US or SS"}); //Retired
    // dict.put(0x00281101, new String[] {"Red Palette Color Lookup TableDescriptor", "US or SS"});
    // dict.put(0x00281102, new String[] {"Green Palette Color Lookup TableDescriptor", "US or
    // SS"});
    // dict.put(0x00281103, new String[] {"Blue Palette Color Lookup TableDescriptor", "US or
    // SS"});
    dict.put(0x00281104, new String[] {"Alpha Palette Color Lookup TableDescriptor", "US"});
    // dict.put(0x00281111, new String[] {"Large Red Palette Color LookupTable Descriptor", "US or
    // SS"}); //Retired
    // dict.put(0x00281112, new String[] {"Large Green Palette Color LookupTable Descriptor", "US
    // or SS"}); //Retired
    // dict.put(0x00281113, new String[] {"Large Blue Palette Color LookupTable Descriptor", "US or
    // SS"}); //Retired
    dict.put(0x00281199, new String[] {"Palette Color Lookup Table UID", "UI"});
    // dict.put(0x00281200, new String[] {"Gray Lookup Table Data", "US or SS or OW"}); //Retired
    dict.put(0x00281201, new String[] {"Red Palette Color Lookup TableData", "OW"});
    dict.put(0x00281202, new String[] {"Green Palette Color Lookup TableData", "OW"});
    dict.put(0x00281203, new String[] {"Blue Palette Color Lookup TableData", "OW"});
    dict.put(0x00281204, new String[] {"Alpha Palette Color Lookup TableData", "OW"});
    dict.put(0x00281211, new String[] {"Large Red Palette Color LookupTable Data", "OW"}); // Retired
    dict.put(0x00281212, new String[] {"Large Green Palette Color LookupTable Data", "OW"}); // Retired
    dict.put(0x00281213, new String[] {"Large Blue Palette Color LookupTable Data", "OW"}); // Retired
    dict.put(0x00281214, new String[] {"Large Palette Color Lookup TableUID", "UI"}); // Retired
    dict.put(0x00281221, new String[] {"Segmented Red Palette ColorLookup Table Data", "OW"});
    dict.put(0x00281222, new String[] {"Segmented Green Palette ColorLookup Table Data", "OW"});
    dict.put(0x00281223, new String[] {"Segmented Blue Palette ColorLookup Table Data", "OW"});
    dict.put(0x00281300, new String[] {"Breast Implant Present", "CS"});
    dict.put(0x00281350, new String[] {"Partial View", "CS"});
    dict.put(0x00281351, new String[] {"Partial View Description", "ST"});
    dict.put(0x00281352, new String[] {"Partial View Code Sequence", "SQ"});
    dict.put(0x0028135A, new String[] {"Spatial Locations Preserved", "CS"});
    dict.put(0x00281401, new String[] {"Data Frame Assignment Sequence", "SQ"});
    dict.put(0x00281402, new String[] {"Data Path Assignment", "CS"});
    dict.put(0x00281403, new String[] {"Bits Mapped to Color Lookup Table", "US"});
    dict.put(0x00281404, new String[] {"Blending LUT 1 Sequence", "SQ"});
    dict.put(0x00281405, new String[] {"Blending LUT 1 Transfer Function", "CS"});
    dict.put(0x00281406, new String[] {"Blending Weight Constant", "FD"});
    dict.put(0x00281407, new String[] {"Blending Lookup Table Descriptor", "US"});
    dict.put(0x00281408, new String[] {"Blending Lookup Table Data", "OW"});
    dict.put(0x0028140B, new String[] {"Enhanced Palette Color LookupTable Sequence", "SQ"});
    dict.put(0x0028140C, new String[] {"Blending LUT 2 Sequence", "SQ"});
    dict.put(0x0028140D,
             new String[] {"Blending LUT 2 Transfer Function  BlendingLUT2TransferFunction  CS  1 (0028,140E) Data Path ID",
                           "CS"});
    dict.put(0x0028140F, new String[] {"RGB LUT Transfer Function", "CS"});
    dict.put(0x00281410, new String[] {"Alpha LUT Transfer Function", "CS"});
    dict.put(0x00282000, new String[] {"ICC Profile", "OB"});
    dict.put(0x00282110, new String[] {"Lossy Image Compression", "CS"});
    dict.put(0x00282112, new String[] {"Lossy Image Compression Ratio", "DS"});
    dict.put(0x00282114, new String[] {"Lossy Image Compression Method", "CS"});
    dict.put(0x00283000, new String[] {"Modality LUT Sequence", "SQ"});
    // dict.put(0x00283002, new String[] {"LUT Descriptor", "US or SS"});
    dict.put(0x00283003, new String[] {"LUT Explanation", "LO"});
    dict.put(0x00283004, new String[] {"Modality LUT Type", "LO"});
    // dict.put(0x00283006, new String[] {"LUT Data", "US or OW"});
    dict.put(0x00283010, new String[] {"VOI LUT Sequence", "SQ"});
    dict.put(0x00283110, new String[] {"Softcopy VOI LUT Sequence", "SQ"});
    dict.put(0x00284000, new String[] {"Image Presentation Comments", "LT"}); // Retired
    dict.put(0x00285000, new String[] {"Bi-Plane Acquisition Sequence", "SQ"}); // Retired
    dict.put(0x00286010, new String[] {"Representative Frame Number", "US"});
    dict.put(0x00286020, new String[] {"Frame Numbers of Interest (FOI)", "US"});
    dict.put(0x00286022, new String[] {"Frame of Interest Description", "LO"});
    dict.put(0x00286023, new String[] {"Frame of Interest Type", "CS"});
    dict.put(0x00286030, new String[] {"Mask Pointer(s)", "US"}); // Retired
    dict.put(0x00286040, new String[] {"R Wave Pointer", "US"});
    dict.put(0x00286100, new String[] {"Mask Subtraction Sequence", "SQ"});
    dict.put(0x00286101, new String[] {"Mask Operation", "CS"});
    dict.put(0x00286102, new String[] {"Applicable Frame Range", "US"});
    dict.put(0x00286110, new String[] {"Mask Frame Numbers", "US"});
    dict.put(0x00286112, new String[] {"Contrast Frame Averaging", "US"});
    dict.put(0x00286114, new String[] {"Mask Sub-pixel Shift", "FL"});
    dict.put(0x00286120, new String[] {"TID Offset", "SS"});
    dict.put(0x00286190, new String[] {"Mask Operation Explanation", "ST"});
    dict.put(0x00287000, new String[] {"Equipment Administrator Sequence", "SQ"});
    dict.put(0x00287001, new String[] {"Number of Display Subsystems", "US"});
    dict.put(0x00287002, new String[] {"Current Configuration ID", "US"});
    dict.put(0x00287003, new String[] {"Display Subsystem ID", "US"});
    dict.put(0x00287004, new String[] {"Display Subsystem Name", "SH"});
    dict.put(0x00287005, new String[] {"Display Subsystem Description", "LO"});
    dict.put(0x00287006, new String[] {"System Status", "CS"});
    dict.put(0x00287007, new String[] {"System Status Comment", "LO"});
    dict.put(0x00287008, new String[] {"Target Luminance CharacteristicsSequence", "SQ"});
    dict.put(0x00287009, new String[] {"Luminance Characteristics ID", "US"});
    dict.put(0x0028700A, new String[] {"Display Subsystem ConfigurationSequence", "SQ"});
    dict.put(0x0028700B, new String[] {"Configuration ID", "US"});
    dict.put(0x0028700C, new String[] {"Configuration Name", "SH"});
    dict.put(0x0028700D, new String[] {"Configuration Description", "LO"});
    dict.put(0x0028700E, new String[] {"Referenced Target LuminanceCharacteristics ID", "US"});
    dict.put(0x0028700F, new String[] {"QA Results Sequence", "SQ"});
    dict.put(0x00287010, new String[] {"Display Subsystem QA ResultsSequence", "SQ"});
    dict.put(0x00287011, new String[] {"Configuration QA ResultsSequence", "SQ"});
    dict.put(0x00287012, new String[] {"Measurement EquipmentSequence", "SQ"});
    dict.put(0x00287013, new String[] {"Measurement Functions", "CS"});
    dict.put(0x00287014, new String[] {"Measurement Equipment Type", "CS"});
    dict.put(0x00287015, new String[] {"Visual Evaluation Result Sequence", "SQ"});
    dict.put(0x00287016, new String[] {"Display Calibration ResultSequence", "SQ"});
    dict.put(0x00287017, new String[] {"DDL Value", "US"});
    dict.put(0x00287018, new String[] {"CIExy White Point", "FL"});
    dict.put(0x00287019, new String[] {"Display Function Type", "CS"});
    dict.put(0x0028701A, new String[] {"Gamma Value", "FL"});
    dict.put(0x0028701B, new String[] {"Number of Luminance Points", "US"});
    dict.put(0x0028701C, new String[] {"Luminance Response Sequence", "SQ"});
    dict.put(0x0028701D, new String[] {"Target Minimum Luminance", "FL"});
    dict.put(0x0028701E, new String[] {"Target Maximum Luminance", "FL"});
    dict.put(0x0028701F, new String[] {"Luminance Value", "FL"});
    dict.put(0x00287020, new String[] {"Luminance Response Description", "LO"});
    dict.put(0x00287021, new String[] {"White Point Flag", "CS"});
    dict.put(0x00287022, new String[] {"Display Device Type CodeSequence", "SQ"});
    dict.put(0x00287023, new String[] {"Display Subsystem Sequence", "SQ"});
    dict.put(0x00287024, new String[] {"Luminance Result Sequence", "SQ"});
    dict.put(0x00287025, new String[] {"Ambient Light Value Source", "CS"});
    dict.put(0x00287026, new String[] {"Measured Characteristics", "CS"});
    dict.put(0x00287027, new String[] {"Luminance Uniformity ResultSequence", "SQ"});
    dict.put(0x00287028, new String[] {"Visual Evaluation Test Sequence", "SQ"});
    dict.put(0x00287029, new String[] {"Test Result", "CS"});
    dict.put(0x0028702A, new String[] {"Test Result Comment", "LO"});
    dict.put(0x0028702B, new String[] {"Test Image Validation", "CS"});
    dict.put(0x0028702C, new String[] {"Test Pattern Code Sequence", "SQ"});
    dict.put(0x0028702D, new String[] {"Measurement Pattern CodeSequence", "SQ"});
    dict.put(0x0028702E, new String[] {"Visual Evaluation Method CodeSequence", "SQ"});
    dict.put(0x00287FE0, new String[] {"Pixel Data Provider URL", "UR"});
    dict.put(0x00289001, new String[] {"Data Point Rows", "UL"});
    dict.put(0x00289002, new String[] {"Data Point Columns", "UL"});
    dict.put(0x00289003, new String[] {"Signal Domain Columns", "CS"});
    dict.put(0x00289099, new String[] {"Largest Monochrome Pixel Value", "US"}); // Retired
    dict.put(0x00289108, new String[] {"Data Representation", "CS"});
    dict.put(0x00289110, new String[] {"Pixel Measures Sequence", "SQ"});
    dict.put(0x00289132, new String[] {"Frame VOI LUT Sequence", "SQ"});
    dict.put(0x00289145, new String[] {"Pixel Value TransformationSequence", "SQ"});
    dict.put(0x00289235, new String[] {"Signal Domain Rows", "CS"});
    dict.put(0x00289411, new String[] {"Display Filter Percentage", "FL"});
    dict.put(0x00289415, new String[] {"Frame Pixel Shift Sequence", "SQ"});
    dict.put(0x00289416, new String[] {"Subtraction Item ID", "US"});
    dict.put(0x00289422, new String[] {"Pixel Intensity Relationship LUTSequence", "SQ"});
    dict.put(0x00289443, new String[] {"Frame Pixel Data PropertiesSequence", "SQ"});
    dict.put(0x00289444, new String[] {"Geometrical Properties", "CS"});
    dict.put(0x00289445, new String[] {"Geometric Maximum Distortion", "FL"});
    dict.put(0x00289446, new String[] {"Image Processing Applied", "CS"});
    dict.put(0x00289454, new String[] {"Mask Selection Mode", "CS"});
    dict.put(0x00289474, new String[] {"LUT Function", "CS"});
    dict.put(0x00289478, new String[] {"Mask Visibility Percentage", "FL"});
    dict.put(0x00289501, new String[] {"Pixel Shift Sequence", "SQ"});
    dict.put(0x00289502, new String[] {"Region Pixel Shift Sequence", "SQ"});
    dict.put(0x00289503, new String[] {"Vertices of the Region", "SS"});
    dict.put(0x00289505, new String[] {"Multi-frame Presentation Sequence", "SQ"});
    dict.put(0x00289506, new String[] {"Pixel Shift Frame Range", "US"});
    dict.put(0x00289507, new String[] {"LUT Frame Range", "US"});
    dict.put(0x00289520, new String[] {"Image to Equipment MappingMatrix", "DS"});
    dict.put(0x00289537, new String[] {"Equipment Coordinate SystemIdentification", "CS"});
  }

  /**
   * Adds attributes of group 0x0032.
   */
  private static void addAttributeGroup0032(Hashtable<Integer, String[]> dict) {
    dict.put(0x0032000A, new String[] {"Study Status ID", "CS"}); // Retired
    dict.put(0x0032000C, new String[] {"Study Priority ID", "CS"}); // Retired
    dict.put(0x00320012, new String[] {"Study ID Issuer", "LO"}); // Retired
    dict.put(0x00320032, new String[] {"Study Verified Date", "DA"}); // Retired
    dict.put(0x00320033, new String[] {"Study Verified Time", "TM"}); // Retired
    dict.put(0x00320034, new String[] {"Study Read Date", "DA"}); // Retired
    dict.put(0x00320035, new String[] {"Study Read Time", "TM"}); // Retired
    dict.put(0x00321000, new String[] {"Scheduled Study Start Date", "DA"}); // Retired
    dict.put(0x00321001, new String[] {"Scheduled Study Start Time", "TM"}); // Retired
    dict.put(0x00321010, new String[] {"Scheduled Study Stop Date", "DA"}); // Retired
    dict.put(0x00321011, new String[] {"Scheduled Study Stop Time", "TM"}); // Retired
    dict.put(0x00321020, new String[] {"Scheduled Study Location", "LO"}); // Retired
    dict.put(0x00321021, new String[] {"Scheduled Study Location AE Title", "AE"}); // Retired
    dict.put(0x00321030, new String[] {"Reason for Study", "LO"}); // Retired
    dict.put(0x00321031, new String[] {"Requesting Physician IdentificationSequence", "SQ"});
    dict.put(0x00321032, new String[] {"Requesting Physician", "PN"});
    dict.put(0x00321033, new String[] {"Requesting Service", "LO"});
    dict.put(0x00321034, new String[] {"Requesting Service CodeSequence", "SQ"});
    dict.put(0x00321040, new String[] {"Study Arrival Date", "DA"}); // Retired
    dict.put(0x00321041, new String[] {"Study Arrival Time", "TM"}); // Retired
    dict.put(0x00321050, new String[] {"Study Completion Date", "DA"}); // Retired
    dict.put(0x00321051, new String[] {"Study Completion Time", "TM"}); // Retired
    dict.put(0x00321055, new String[] {"Study Component Status ID", "CS"}); // Retired
    dict.put(0x00321060, new String[] {"Requested Procedure Description", "LO"});
    dict.put(0x00321064, new String[] {"Requested Procedure CodeSequence", "SQ"});
    dict.put(0x00321070, new String[] {"Requested Contrast Agent", "LO"});
    dict.put(0x00324000, new String[] {"Study Comments", "LT"}); // Retired
  }

  /**
   * Adds attributes of group 0x0038.
   */
  private static void addAttributeGroup0038(Hashtable<Integer, String[]> dict) {
    dict.put(0x00380004, new String[] {"Referenced Patient Alias Sequence", "SQ"});
    dict.put(0x00380008, new String[] {"Visit Status ID", "CS"});
    dict.put(0x00380010, new String[] {"Admission ID", "LO"});
    dict.put(0x00380011, new String[] {"Issuer of Admission ID", "LO"}); // Retired
    dict.put(0x00380014, new String[] {"Issuer of Admission ID Sequence", "SQ"});
    dict.put(0x00380016, new String[] {"Route of Admissions", "LO"});
    dict.put(0x0038001A, new String[] {"Scheduled Admission Date", "DA"}); // Retired
    dict.put(0x0038001B, new String[] {"Scheduled Admission Time", "TM"}); // Retired
    dict.put(0x0038001C, new String[] {"Scheduled Discharge Date", "DA"}); // Retired
    dict.put(0x0038001D, new String[] {"Scheduled Discharge Time", "TM"}); // Retired
    dict.put(0x0038001E, new String[] {"Scheduled Patient InstitutionResidence", "LO"}); // Retired
    dict.put(0x00380020, new String[] {"Admitting Date", "DA"});
    dict.put(0x00380021, new String[] {"Admitting Time", "TM"});
    dict.put(0x00380030, new String[] {"Discharge Date", "DA"}); // Retired
    dict.put(0x00380032, new String[] {"Discharge Time", "TM"}); // Retired
    dict.put(0x00380040, new String[] {"Discharge Diagnosis Description", "LO"}); // Retired
    dict.put(0x00380044, new String[] {"Discharge Diagnosis CodeSequence", "SQ"}); // Retired
    dict.put(0x00380050, new String[] {"Special Needs", "LO"});
    dict.put(0x00380060, new String[] {"Service Episode ID", "LO"});
    dict.put(0x00380061, new String[] {"Issuer of Service Episode ID", "LO"}); // Retired
    dict.put(0x00380062, new String[] {"Service Episode Description", "LO"});
    dict.put(0x00380064, new String[] {"Issuer of Service Episode IDSequence", "SQ"});
    dict.put(0x00380100, new String[] {"Pertinent Documents Sequence", "SQ"});
    dict.put(0x00380101, new String[] {"Pertinent Resources Sequence", "SQ"});
    dict.put(0x00380102, new String[] {"Resource Description", "LO"});
    dict.put(0x00380300, new String[] {"Current Patient Location", "LO"});
    dict.put(0x00380400, new String[] {"Patient's Institution Residence", "LO"});
    dict.put(0x00380500, new String[] {"Patient State", "LO"});
    dict.put(0x00380502, new String[] {"Patient Clinical Trial ParticipationSequence", "SQ"});
    dict.put(0x00384000, new String[] {"Visit Comments", "LT"});
  }

  /**
   * Adds attributes of group 0x003A.
   */
  private static void addAttributeGroup003A(Hashtable<Integer, String[]> dict) {
    dict.put(0x00400001, new String[] {"Scheduled Station AE Title", "AE"});
    dict.put(0x00400002, new String[] {"Scheduled Procedure Step StartDate", "DA"});
    dict.put(0x00400003, new String[] {"Scheduled Procedure Step StartTime", "TM"});
    dict.put(0x00400004, new String[] {"Scheduled Procedure Step EndDate", "DA"});
    dict.put(0x00400005, new String[] {"Scheduled Procedure Step EndTime", "TM"});
    dict.put(0x00400006, new String[] {"Scheduled Performing Physician'sName", "PN"});
    dict.put(0x00400007, new String[] {"Scheduled Procedure StepDescription", "LO"});
    dict.put(0x00400008, new String[] {"Scheduled Protocol CodeSequence", "SQ"});
    dict.put(0x00400009, new String[] {"Scheduled Procedure Step ID", "SH"});
    dict.put(0x0040000A, new String[] {"Stage Code Sequence", "SQ"});
    dict.put(0x0040000B,
             new String[] {"Scheduled Performing PhysicianIdentification Sequence", "SQ"});
    dict.put(0x00400010, new String[] {"Scheduled Station Name", "SH"});
    dict.put(0x00400011, new String[] {"Scheduled Procedure StepLocation", "SH"});
    dict.put(0x00400012, new String[] {"Pre-Medication", "LO"});
    dict.put(0x00400020, new String[] {"Scheduled Procedure Step Status", "CS"});
    dict.put(0x00400026, new String[] {"Order Placer Identifier Sequence", "SQ"});
    dict.put(0x00400027, new String[] {"Order Filler Identifier Sequence", "SQ"});
    dict.put(0x00400031, new String[] {"Local Namespace Entity ID", "UT"});
    dict.put(0x00400032, new String[] {"Universal Entity ID", "UT"});
    dict.put(0x00400033, new String[] {"Universal Entity ID Type", "CS"});
    dict.put(0x00400035, new String[] {"Identifier Type Code", "CS"});
    dict.put(0x00400036, new String[] {"Assigning Facility Sequence", "SQ"});
    dict.put(0x00400039, new String[] {"Assigning Jurisdiction CodeSequence", "SQ"});
    // dict.put(0x0040003A, new String[] {"Assigning Agency or DepartmentCode Sequence", "SQ"});
    dict.put(0x00400100, new String[] {"Scheduled Procedure StepSequence", "SQ"});
    dict.put(0x00400220,
             new String[] {"Referenced Non-Image CompositeSOP Instance Sequence", "SQ"});
    dict.put(0x00400241, new String[] {"Performed Station AE Title", "AE"});
    dict.put(0x00400242, new String[] {"Performed Station Name", "SH"});
    dict.put(0x00400243, new String[] {"Performed Location", "SH"});
    dict.put(0x00400244, new String[] {"Performed Procedure Step StartDate", "DA"});
    dict.put(0x00400245, new String[] {"Performed Procedure Step StartTime", "TM"});
    dict.put(0x00400250, new String[] {"Performed Procedure Step EndDate", "DA"});
    dict.put(0x00400251, new String[] {"Performed Procedure Step EndTime", "TM"});
    dict.put(0x00400252, new String[] {"Performed Procedure Step Status", "CS"});
    dict.put(0x00400253, new String[] {"Performed Procedure Step ID", "SH"});
    dict.put(0x00400254, new String[] {"Performed Procedure StepDescription", "LO"});
    dict.put(0x00400255, new String[] {"Performed Procedure TypeDescription", "LO"});
    dict.put(0x00400260, new String[] {"Performed Protocol CodeSequence", "SQ"});
    dict.put(0x00400261, new String[] {"Performed Protocol Type", "CS"});
    dict.put(0x00400270, new String[] {"Scheduled Step AttributesSequence", "SQ"});
    dict.put(0x00400275, new String[] {"Request Attributes Sequence", "SQ"});
    dict.put(0x00400280, new String[] {"Comments on the PerformedProcedure Step", "ST"});
    dict.put(0x00400281,
             new String[] {"Performed Procedure StepDiscontinuation Reason CodeSequence", "SQ"});
    dict.put(0x00400293, new String[] {"Quantity Sequence", "SQ"});
    dict.put(0x00400294, new String[] {"Quantity", "DS"});
    dict.put(0x00400295, new String[] {"Measuring Units Sequence", "SQ"});
    dict.put(0x00400296, new String[] {"Billing Item Sequence", "SQ"});
    dict.put(0x00400300, new String[] {"Total Time of Fluoroscopy", "US"});
    dict.put(0x00400301, new String[] {"Total Number of Exposures", "US"});
    dict.put(0x00400302, new String[] {"Entrance Dose", "US"});
    dict.put(0x00400303, new String[] {"Exposed Area", "US"});
    dict.put(0x00400306, new String[] {"Distance Source to Entrance", "DS"});
    dict.put(0x00400307, new String[] {"Distance Source to Support", "DS"}); // Retired
    dict.put(0x0040030E, new String[] {"Exposure Dose Sequence", "SQ"});
    dict.put(0x00400310, new String[] {"Comments on Radiation Dose", "ST"});
    dict.put(0x00400312, new String[] {"X-Ray Output", "DS"});
    dict.put(0x00400314, new String[] {"Half Value Layer", "DS"});
    dict.put(0x00400316, new String[] {"Organ Dose", "DS"});
    dict.put(0x00400318, new String[] {"Organ Exposed", "CS"});
    dict.put(0x00400320, new String[] {"Billing Procedure Step Sequence", "SQ"});
    dict.put(0x00400321, new String[] {"Film Consumption Sequence", "SQ"});
    dict.put(0x00400324, new String[] {"Billing Supplies and DevicesSequence", "SQ"});
    dict.put(0x00400330, new String[] {"Referenced Procedure StepSequence", "SQ"}); // Retired
    dict.put(0x00400340, new String[] {"Performed Series Sequence", "SQ"});
    dict.put(0x00400400, new String[] {"Comments on the ScheduledProcedure Step", "LT"});
    dict.put(0x00400440, new String[] {"Protocol Context Sequence", "SQ"});
    dict.put(0x00400441, new String[] {"Content Item Modifier Sequence", "SQ"});
    dict.put(0x00400500, new String[] {"Scheduled Specimen Sequence", "SQ"});
    dict.put(0x0040050A, new String[] {"Specimen Accession Number", "LO"}); // Retired
    dict.put(0x00400512, new String[] {"Container Identifier", "LO"});
    dict.put(0x00400513, new String[] {"Issuer of the Container IdentifierSequence", "SQ"});
    dict.put(0x00400515, new String[] {"Alternate Container IdentifierSequence", "SQ"});
    dict.put(0x00400518, new String[] {"Container Type Code Sequence", "SQ"});
    dict.put(0x0040051A, new String[] {"Container Description", "LO"});
    dict.put(0x00400520, new String[] {"Container Component Sequence", "SQ"});
    dict.put(0x00400550, new String[] {"Specimen Sequence", "SQ"}); // Retired
    dict.put(0x00400551, new String[] {"Specimen Identifier", "LO"});
    dict.put(0x00400552, new String[] {"Specimen Description Sequence(Trial)", "SQ"}); // Retired
    dict.put(0x00400553, new String[] {"Specimen Description (Trial)", "ST"}); // Retired
    dict.put(0x00400554, new String[] {"Specimen UID", "UI"});
    dict.put(0x00400555, new String[] {"Acquisition Context Sequence", "SQ"});
    dict.put(0x00400556, new String[] {"Acquisition Context Description", "ST"});
    dict.put(0x0040059A, new String[] {"Specimen Type Code Sequence", "SQ"});
    dict.put(0x00400560, new String[] {"Specimen Description Sequence", "SQ"});
    dict.put(0x00400562, new String[] {"Issuer of the Specimen IdentifierSequence", "SQ"});
    dict.put(0x00400600, new String[] {"Specimen Short Description", "LO"});
    dict.put(0x00400602, new String[] {"Specimen Detailed Description", "UT"});
    dict.put(0x00400610, new String[] {"Specimen Preparation Sequence", "SQ"});
    dict.put(0x00400612, new String[] {"Specimen Preparation StepContent Item Sequence", "SQ"});
    dict.put(0x00400620, new String[] {"Specimen Localization ContentItem Sequence", "SQ"});
    dict.put(0x004006FA, new String[] {"Slide Identifier", "LO"}); // Retired
    dict.put(0x0040071A, new String[] {"Image Center Point CoordinatesSequence", "SQ"});
    dict.put(0x0040072A, new String[] {"X Offset in Slide CoordinateSystem", "DS"});
    dict.put(0x0040073A, new String[] {"Y Offset in Slide CoordinateSystem", "DS"});
    dict.put(0x0040074A, new String[] {"Z Offset in Slide CoordinateSystem", "DS"});
    dict.put(0x004008D8, new String[] {"Pixel Spacing Sequence", "SQ"}); // Retired
    dict.put(0x004008DA, new String[] {"Coordinate System Axis CodeSequence", "SQ"}); // Retired
    dict.put(0x004008EA, new String[] {"Measurement Units CodeSequence", "SQ"});
    dict.put(0x004009F8, new String[] {"Vital Stain Code Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x00401001, new String[] {"Requested Procedure ID", "SH"});
    dict.put(0x00401002, new String[] {"Reason for the RequestedProcedure", "LO"});
    dict.put(0x00401003, new String[] {"Requested Procedure Priority", "SH"});
    dict.put(0x00401004, new String[] {"Patient Transport Arrangements", "LO"});
    dict.put(0x00401005, new String[] {"Requested Procedure Location", "LO"});
    dict.put(0x00401006, new String[] {"Placer Order Number / Procedure", "SH"}); // Retired
    dict.put(0x00401007, new String[] {"Filler Order Number / Procedure", "SH"}); // Retired
    dict.put(0x00401008, new String[] {"Confidentiality Code", "LO"});
    dict.put(0x00401009, new String[] {"Reporting Priority", "SH"});
    dict.put(0x0040100A, new String[] {"Reason for Requested ProcedureCode Sequence", "SQ"});
    dict.put(0x00401010, new String[] {"Names of Intended Recipients ofResults", "PN"});
    dict.put(0x00401011,
             new String[] {"Intended Recipients of ResultsIdentification Sequence", "SQ"});
    dict.put(0x00401012, new String[] {"Reason For Performed ProcedureCode Sequence", "SQ"});
    dict.put(0x00401060, new String[] {"Requested Procedure Description(Trial)", "LO"}); // Retired
    dict.put(0x00401101, new String[] {"Person Identification CodeSequence", "SQ"});
    dict.put(0x00401102, new String[] {"Person's Address", "ST"});
    dict.put(0x00401103, new String[] {"Person's Telephone Numbers", "LO"});
    dict.put(0x00401104, new String[] {"Person's Telecom Information", "LT"});
    dict.put(0x00401400, new String[] {"Requested Procedure Comments", "LT"});
    dict.put(0x00402001, new String[] {"Reason for the Imaging ServiceRequest", "LO"}); // Retired
    dict.put(0x00402004, new String[] {"Issue Date of Imaging ServiceRequest", "DA"});
    dict.put(0x00402005, new String[] {"Issue Time of Imaging ServiceRequest", "TM"});
    dict.put(0x00402006,
             new String[] {"Placer Order Number / ImagingService Request (Retired)", "SH"}); // Retired
    dict.put(0x00402007,
             new String[] {"Filler Order Number / ImagingService Request (Retired)", "SH"}); // Retired
    dict.put(0x00402008, new String[] {"Order Entered By", "PN"});
    dict.put(0x00402009, new String[] {"Order Enterer's Location", "SH"});
    dict.put(0x00402010, new String[] {"Order Callback Phone Number", "SH"});
    dict.put(0x00402011, new String[] {"Order Callback TelecomInformation", "LT"});
    dict.put(0x00402016, new String[] {"Placer Order Number / ImagingService Request", "LO"});
    dict.put(0x00402017, new String[] {"Filler Order Number / ImagingService Request", "LO"});
    dict.put(0x00402400, new String[] {"Imaging Service RequestComments", "LT"});
    dict.put(0x00403001,
             new String[] {"Confidentiality Constraint onPatient Data Description", "LO"});
    dict.put(0x00404001, new String[] {"General Purpose ScheduledProcedure Step Status", "CS"}); // Retired
    dict.put(0x00404002, new String[] {"General Purpose PerformedProcedure Step Status", "CS"}); // Retired
    dict.put(0x00404003, new String[] {"General Purpose ScheduledProcedure Step Priority", "CS"}); // Retired
    dict.put(0x00404004, new String[] {"Scheduled Processing ApplicationsCode Sequence", "SQ"}); // Retired
    dict.put(0x00404005, new String[] {"Scheduled Procedure Step StartDateTime", "DT"});
    dict.put(0x00404006, new String[] {"Multiple Copies Flag", "CS"}); // Retired
    dict.put(0x00404007, new String[] {"Performed Processing ApplicationsCode Sequence", "SQ"});
    dict.put(0x00404009, new String[] {"Human Performer Code Sequence", "SQ"});
    dict.put(0x00404010, new String[] {"Scheduled Procedure StepModification DateTime", "DT"});
    dict.put(0x00404011, new String[] {"Expected Completion DateTime", "DT"});
    dict.put(0x00404015,
             new String[] {"Resulting General PurposePerformed Procedure StepsSequence", "SQ"}); // Retired
    dict.put(0x00404016,
             new String[] {"Referenced General PurposeScheduled Procedure StepSequence", "SQ"}); // Retired
    dict.put(0x00404018, new String[] {"Scheduled Workitem CodeSequence", "SQ"});
    dict.put(0x00404019, new String[] {"Performed Workitem CodeSequence", "SQ"});
    dict.put(0x00404020, new String[] {"Input Availability Flag", "CS"});
    dict.put(0x00404021, new String[] {"Input Information Sequence", "SQ"});
    dict.put(0x00404022, new String[] {"Relevant Information Sequence", "SQ"}); // Retired
    dict.put(0x00404023,
             new String[] {"Referenced General PurposeScheduled Procedure StepTransaction UID",
                           "UI"}); // Retired
    dict.put(0x00404025, new String[] {"Scheduled Station Name CodeSequence", "SQ"});
    dict.put(0x00404026, new String[] {"Scheduled Station Class CodeSequence", "SQ"});
    dict.put(0x00404027,
             new String[] {"Scheduled Station GeographicLocation Code Sequence", "SQ"});
    dict.put(0x00404028, new String[] {"Performed Station Name CodeSequence", "SQ"});
    dict.put(0x00404029, new String[] {"Performed Station Class CodeSequence", "SQ"});
    dict.put(0x00404030,
             new String[] {"Performed Station GeographicLocation Code Sequence", "SQ"});
    dict.put(0x00404031, new String[] {"Requested Subsequent WorkitemCode Sequence", "SQ"}); // Retired
    dict.put(0x00404032, new String[] {"Non-DICOM Output CodeSequence", "SQ"}); // Retired
    dict.put(0x00404033, new String[] {"Output Information Sequence", "SQ"});
    dict.put(0x00404034, new String[] {"Scheduled Human PerformersSequence", "SQ"});
    dict.put(0x00404035, new String[] {"Actual Human PerformersSequence", "SQ"});
    dict.put(0x00404036, new String[] {"Human Performer's Organization", "LO"});
    dict.put(0x00404037, new String[] {"Human Performer's Name", "PN"});
    dict.put(0x00404040, new String[] {"Raw Data Handling", "CS"});
    dict.put(0x00404041, new String[] {"Input Readiness State", "CS"});
    dict.put(0x00404050, new String[] {"Performed Procedure Step StartDateTime", "DT"});
    dict.put(0x00404051, new String[] {"Performed Procedure Step EndDateTime", "DT"});
    dict.put(0x00404052, new String[] {"Procedure Step CancellationDateTime", "DT"});
    dict.put(0x00408302, new String[] {"Entrance Dose in mGy", "DS"});
    dict.put(0x00409092, new String[] {"Parametric Map Frame TypeSequence", "SQ"});
    dict.put(0x00409094, new String[] {"Referenced Image Real WorldValue Mapping Sequence", "SQ"});
    dict.put(0x00409096, new String[] {"Real World Value MappingSequence", "SQ"});
    dict.put(0x00409098, new String[] {"Pixel Value Mapping CodeSequence", "SQ"});
    dict.put(0x00409210, new String[] {"LUT Label", "SH"});
    // dict.put(0x00409211, new String[] {"Real World Value Last ValueMapped", "US or SS"});
    dict.put(0x00409212, new String[] {"Real World Value LUT Data", "FD"});
    // dict.put(0x00409216, new String[] {"Real World Value First ValueMapped", "US or SS"});
    dict.put(0x00409220, new String[] {"Quantity Definition Sequence", "SQ"});
    dict.put(0x00409224, new String[] {"Real World Value Intercept", "FD"});
    dict.put(0x00409225, new String[] {"Real World Value Slope", "FD"});
    dict.put(0x0040A007, new String[] {"Findings Flag (Trial)", "CS"}); // Retired
    dict.put(0x0040A010, new String[] {"Relationship Type", "CS"});
    dict.put(0x0040A020, new String[] {"Findings Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A021, new String[] {"Findings Group UID (Trial)", "UI"}); // Retired
    dict.put(0x0040A022, new String[] {"Referenced Findings Group UID(Trial)", "UI"}); // Retired
    dict.put(0x0040A023, new String[] {"Findings Group Recording Date(Trial)", "DA"}); // Retired
    dict.put(0x0040A024, new String[] {"Findings Group Recording Time(Trial)", "TM"}); // Retired
    dict.put(0x0040A026, new String[] {"Findings Source Category CodeSequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A027, new String[] {"Verifying Organization", "LO"});
    dict.put(0x0040A028,
             new String[] {"Documenting OrganizationIdentifier Code Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A030, new String[] {"Verification DateTime", "DT"});
    dict.put(0x0040A032, new String[] {"Observation DateTime", "DT"});
    dict.put(0x0040A040, new String[] {"Value Type", "CS"});
    dict.put(0x0040A043, new String[] {"Concept Name Code Sequence", "SQ"});
    dict.put(0x0040A047, new String[] {"Measurement Precision Description(Trial)", "LO"}); // Retired
    dict.put(0x0040A050, new String[] {"Continuity Of Content", "CS"});
    // dict.put(0x0040A057, new String[] {"Urgency or Priority Alerts (Trial)", "CS"}); //Retired
    dict.put(0x0040A060, new String[] {"Sequencing Indicator (Trial)", "LO"}); // Retired
    dict.put(0x0040A066, new String[] {"Document Identifier CodeSequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A067, new String[] {"Document Author (Trial)", "PN"}); // Retired
    dict.put(0x0040A068, new String[] {"Document Author Identifier CodeSequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A070, new String[] {"Identifier Code Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A073, new String[] {"Verifying Observer Sequence", "SQ"});
    dict.put(0x0040A074, new String[] {"Object Binary Identifier (Trial)", "OB"}); // Retired
    dict.put(0x0040A075, new String[] {"Verifying Observer Name", "PN"});
    dict.put(0x0040A076,
             new String[] {"Documenting Observer IdentifierCode Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A078, new String[] {"Author Observer Sequence", "SQ"});
    dict.put(0x0040A07A, new String[] {"Participant Sequence", "SQ"});
    dict.put(0x0040A07C, new String[] {"Custodial Organization Sequence", "SQ"});
    dict.put(0x0040A080, new String[] {"Participation Type", "CS"});
    dict.put(0x0040A082, new String[] {"Participation DateTime", "DT"});
    dict.put(0x0040A084, new String[] {"Observer Type", "CS"});
    dict.put(0x0040A085, new String[] {"Procedure Identifier CodeSequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A088, new String[] {"Verifying Observer IdentificationCode Sequence", "SQ"});
    dict.put(0x0040A089, new String[] {"Object Directory Binary Identifier(Trial)", "OB"}); // Retired
    dict.put(0x0040A090, new String[] {"Equivalent CDA DocumentSequence", "SQ"}); // Retired
    dict.put(0x0040A0B0, new String[] {"Referenced Waveform Channels", "US"});
    // dict.put(0x0040A110, new String[] {"Date of Document or VerbalTransaction (Trial)", "DA"});
    // //Retired
    dict.put(0x0040A112,
             new String[] {"Time of Document Creation orVerbal Transaction (Trial)", "TM"}); // Retired
    dict.put(0x0040A120, new String[] {"DateTime", "DT"});
    dict.put(0x0040A121, new String[] {"Date", "DA"});
    dict.put(0x0040A122, new String[] {"Time", "TM"});
    dict.put(0x0040A123, new String[] {"Person Name", "PN"});
    dict.put(0x0040A124, new String[] {"UID", "UI"});
    dict.put(0x0040A125, new String[] {"Report Status ID (Trial)", "CS"}); // Retired
    dict.put(0x0040A130, new String[] {"Temporal Range Type", "CS"});
    dict.put(0x0040A132, new String[] {"Referenced Sample Positions", "UL"});
    dict.put(0x0040A136, new String[] {"Referenced Frame Numbers", "US"});
    dict.put(0x0040A138, new String[] {"Referenced Time Offsets", "DS"});
    dict.put(0x0040A13A, new String[] {"Referenced DateTime", "DT"});
    dict.put(0x0040A160, new String[] {"Text Value", "UT"});
    dict.put(0x0040A161, new String[] {"Floating Point Value", "FD"});
    dict.put(0x0040A162, new String[] {"Rational Numerator Value", "SL"});
    dict.put(0x0040A163, new String[] {"Rational Denominator Value", "UL"});
    dict.put(0x0040A167, new String[] {"Observation Category CodeSequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A168, new String[] {"Concept Code Sequence", "SQ"});
    dict.put(0x0040A16A, new String[] {"Bibliographic Citation (Trial)", "ST"}); // Retired
    dict.put(0x0040A170, new String[] {"Purpose of Reference CodeSequence", "SQ"});
    dict.put(0x0040A171, new String[] {"Observation UID", "UI"});
    dict.put(0x0040A172, new String[] {"Referenced Observation UID (Trial)", "UI"}); // Retired
    dict.put(0x0040A173, new String[] {"Referenced Observation Class(Trial)", "CS"}); // Retired
    dict.put(0x0040A174, new String[] {"Referenced Object ObservationClass (Trial)", "CS"}); // Retired
    dict.put(0x0040A180, new String[] {"Annotation Group Number", "US"});
    dict.put(0x0040A192, new String[] {"Observation Date (Trial)", "DA"}); // Retired
    dict.put(0x0040A193, new String[] {"Observation Time (Trial)", "TM"}); // Retired
    dict.put(0x0040A194, new String[] {"Measurement Automation (Trial)", "CS"}); // Retired
    dict.put(0x0040A195, new String[] {"Modifier Code Sequence", "SQ"});
    dict.put(0x0040A224, new String[] {"Identification Description (Trial)", "ST"}); // Retired
    dict.put(0x0040A290, new String[] {"Coordinates Set Geometric Type(Trial)", "CS"}); // Retired
    dict.put(0x0040A296, new String[] {"Algorithm Code Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A297, new String[] {"Algorithm Description (Trial)", "ST"}); // Retired
    dict.put(0x0040A29A, new String[] {"Pixel Coordinates Set (Trial)", "SL"}); // Retired
    dict.put(0x0040A300, new String[] {"Measured Value Sequence", "SQ"});
    dict.put(0x0040A301, new String[] {"Numeric Value Qualifier CodeSequence", "SQ"});
    dict.put(0x0040A307, new String[] {"Current Observer (Trial)", "PN"}); // Retired
    dict.put(0x0040A30A, new String[] {"Numeric Value", "DS"});
    dict.put(0x0040A313, new String[] {"Referenced Accession Sequence(Trial)", "SQ"}); // Retired
    dict.put(0x0040A33A, new String[] {"Report Status Comment (Trial)", "ST"}); // Retired
    dict.put(0x0040A340, new String[] {"Procedure Context Sequence(Trial)", "SQ"}); // Retired
    dict.put(0x0040A352, new String[] {"Verbal Source (Trial)", "PN"}); // Retired
    dict.put(0x0040A353, new String[] {"Address (Trial)", "ST"}); // Retired
    dict.put(0x0040A354, new String[] {"Telephone Number (Trial)", "LO"}); // Retired
    dict.put(0x0040A358, new String[] {"Verbal Source Identifier CodeSequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A360, new String[] {"Predecessor Documents Sequence", "SQ"});
    dict.put(0x0040A370, new String[] {"Referenced Request Sequence", "SQ"});
    dict.put(0x0040A372, new String[] {"Performed Procedure CodeSequence", "SQ"});
    dict.put(0x0040A375, new String[] {"Current Requested ProcedureEvidence Sequence", "SQ"});
    dict.put(0x0040A380, new String[] {"Report Detail Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A385, new String[] {"Pertinent Other EvidenceSequence", "SQ"});
    dict.put(0x0040A390, new String[] {"HL7 Structured DocumentReference Sequence", "SQ"});
    dict.put(0x0040A402, new String[] {"Observation Subject UID (Trial)", "UI"}); // Retired
    dict.put(0x0040A403, new String[] {"Observation Subject Class (Trial)", "CS"}); // Retired
    dict.put(0x0040A404, new String[] {"Observation Subject Type CodeSequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A491, new String[] {"Completion Flag", "CS"});
    dict.put(0x0040A492, new String[] {"Completion Flag Description", "LO"});
    dict.put(0x0040A493, new String[] {"Verification Flag", "CS"});
    dict.put(0x0040A494, new String[] {"Archive Requested", "CS"});
    dict.put(0x0040A496, new String[] {"Preliminary Flag", "CS"});
    dict.put(0x0040A504, new String[] {"Content Template Sequence", "SQ"});
    dict.put(0x0040A525, new String[] {"Identical Documents Sequence", "SQ"});
    dict.put(0x0040A600, new String[] {"Observation Subject Context Flag(Trial)", "CS"}); // Retired
    dict.put(0x0040A601, new String[] {"Observer Context Flag (Trial)", "CS"}); // Retired
    dict.put(0x0040A603, new String[] {"Procedure Context Flag (Trial)", "CS"}); // Retired
    dict.put(0x0040A730, new String[] {"Content Sequence", "SQ"});
    dict.put(0x0040A731, new String[] {"Relationship Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A732, new String[] {"Relationship Type Code Sequence(Trial)", "SQ"}); // Retired
    dict.put(0x0040A744, new String[] {"Language Code Sequence (Trial)", "SQ"}); // Retired
    dict.put(0x0040A992, new String[] {"Uniform Resource Locator (Trial)", "ST"}); // Retired
    dict.put(0x0040B020, new String[] {"Waveform Annotation Sequence", "SQ"});
    dict.put(0x0040DB00, new String[] {"Template Identifier", "CS"});
    dict.put(0x0040DB06, new String[] {"Template Version", "DT"}); // Retired
    dict.put(0x0040DB07, new String[] {"Template Local Version", "DT"}); // Retired
    dict.put(0x0040DB0B, new String[] {"Template Extension Flag", "CS"}); // Retired
    dict.put(0x0040DB0C, new String[] {"Template Extension OrganizationUID", "UI"}); // Retired
    dict.put(0x0040DB0D, new String[] {"Template Extension Creator UID", "UI"}); // Retired
    dict.put(0x0040DB73, new String[] {"Referenced Content Item Identifier", "UL"});
    dict.put(0x0040E001, new String[] {"HL7 Instance Identifier", "ST"});
    dict.put(0x0040E004, new String[] {"HL7 Document Effective Time", "DT"});
    dict.put(0x0040E006, new String[] {"HL7 Document Type CodeSequence", "SQ"});
    dict.put(0x0040E008, new String[] {"Document Class Code Sequence", "SQ"});
    dict.put(0x0040E010, new String[] {"Retrieve URI", "UR"});
    dict.put(0x0040E011, new String[] {"Retrieve Location UID", "UI"});
    dict.put(0x0040E020, new String[] {"Type of Instances", "CS"});
    dict.put(0x0040E021, new String[] {"DICOM Retrieval Sequence", "SQ"});
    dict.put(0x0040E022, new String[] {"DICOM Media Retrieval Sequence", "SQ"});
    dict.put(0x0040E023, new String[] {"WADO Retrieval Sequence", "SQ"});
    dict.put(0x0040E024, new String[] {"XDS Retrieval Sequence", "SQ"});
    dict.put(0x0040E025, new String[] {"WADO-RS Retrieval Sequence", "SQ"});
    dict.put(0x0040E030, new String[] {"Repository Unique ID", "UI"});
    dict.put(0x0040E031, new String[] {"Home Community ID", "UI"});
  }

  /**
   * Adds attributes of group 0x0042.
   */
  private static void addAttributeGroup0042(Hashtable<Integer, String[]> dict) {
    dict.put(0x00420010, new String[] {"Document Title", "ST"});
    dict.put(0x00420011, new String[] {"Encapsulated Document", "OB"});
    dict.put(0x00420012, new String[] {"MIME Type of EncapsulatedDocument", "LO"});
    dict.put(0x00420013, new String[] {"Source Instance Sequence", "SQ"});
    dict.put(0x00420014, new String[] {"List of MIME Types", "LO"});
  }

  /**
   * Adds attributes of group 0x0044.
   */
  private static void addAttributeGroup0044(Hashtable<Integer, String[]> dict) {
    dict.put(0x00440001, new String[] {"Product Package Identifier", "ST"});
    dict.put(0x00440002, new String[] {"Substance Administration Approval", "CS"});
    dict.put(0x00440003, new String[] {"Approval Status FurtherDescription", "LT"});
    dict.put(0x00440004, new String[] {"Approval Status DateTime", "DT"});
    dict.put(0x00440007, new String[] {"Product Type Code Sequence", "SQ"});
    dict.put(0x00440008, new String[] {"Product Name", "LO"});
    dict.put(0x00440009, new String[] {"Product Description", "LT"});
    dict.put(0x0044000A, new String[] {"Product Lot Identifier", "LO"});
    dict.put(0x0044000B, new String[] {"Product Expiration DateTime", "DT"});
    dict.put(0x00440010, new String[] {"Substance AdministrationDateTime", "DT"});
    dict.put(0x00440011, new String[] {"Substance Administration Notes", "LO"});
    dict.put(0x00440012, new String[] {"Substance Administration DeviceID", "LO"});
    dict.put(0x00440013, new String[] {"Product Parameter Sequence", "SQ"});
    dict.put(0x00440019, new String[] {"Substance AdministrationParameter Sequence", "SQ"});
  }

  /**
   * Adds attributes of group 0x0046.
   */
  private static void addAttributeGroup0046(Hashtable<Integer, String[]> dict) {
    dict.put(0x00460012, new String[] {"Lens Description", "LO"});
    dict.put(0x00460014, new String[] {"Right Lens Sequence", "SQ"});
    dict.put(0x00460015, new String[] {"Left Lens Sequence", "SQ"});
    dict.put(0x00460016, new String[] {"Unspecified Laterality LensSequence", "SQ"});
    dict.put(0x00460018, new String[] {"Cylinder Sequence", "SQ"});
    dict.put(0x00460028, new String[] {"Prism Sequence", "SQ"});
    dict.put(0x00460030, new String[] {"Horizontal Prism Power", "FD"});
    dict.put(0x00460032, new String[] {"Horizontal Prism Base", "CS"});
    dict.put(0x00460034, new String[] {"Vertical Prism Power", "FD"});
    dict.put(0x00460036, new String[] {"Vertical Prism Base", "CS"});
    dict.put(0x00460038, new String[] {"Lens Segment Type", "CS"});
    dict.put(0x00460040, new String[] {"Optical Transmittance", "FD"});
    dict.put(0x00460042, new String[] {"Channel Width", "FD"});
    dict.put(0x00460044, new String[] {"Pupil Size", "FD"});
    dict.put(0x00460046, new String[] {"Corneal Size", "FD"});
    dict.put(0x00460050, new String[] {"Autorefraction Right Eye Sequence", "SQ"});
    dict.put(0x00460052, new String[] {"Autorefraction Left Eye Sequence", "SQ"});
    dict.put(0x00460060, new String[] {"Distance Pupillary Distance", "FD"});
    dict.put(0x00460062, new String[] {"Near Pupillary Distance", "FD"});
    dict.put(0x00460063, new String[] {"Intermediate Pupillary Distance", "FD"});
    dict.put(0x00460064, new String[] {"Other Pupillary Distance", "FD"});
    dict.put(0x00460070, new String[] {"Keratometry Right Eye Sequence", "SQ"});
    dict.put(0x00460071, new String[] {"Keratometry Left Eye Sequence", "SQ"});
    dict.put(0x00460074, new String[] {"Steep Keratometric Axis Sequence", "SQ"});
    dict.put(0x00460075, new String[] {"Radius of Curvature", "FD"});
    dict.put(0x00460076, new String[] {"Keratometric Power", "FD"});
    dict.put(0x00460077, new String[] {"Keratometric Axis", "FD"});
    dict.put(0x00460080, new String[] {"Flat Keratometric Axis Sequence", "SQ"});
    dict.put(0x00460092, new String[] {"Background Color", "CS"});
    dict.put(0x00460094, new String[] {"Optotype", "CS"});
    dict.put(0x00460095, new String[] {"Optotype Presentation", "CS"});
    dict.put(0x00460097, new String[] {"Subjective Refraction Right EyeSequence", "SQ"});
    dict.put(0x00460098, new String[] {"Subjective Refraction Left EyeSequence", "SQ"});
    dict.put(0x00460100, new String[] {"Add Near Sequence", "SQ"});
    dict.put(0x00460101, new String[] {"Add Intermediate Sequence", "SQ"});
    dict.put(0x00460102, new String[] {"Add Other Sequence", "SQ"});
    dict.put(0x00460104, new String[] {"Add Power", "FD"});
    dict.put(0x00460106, new String[] {"Viewing Distance", "FD"});
    dict.put(0x00460121, new String[] {"Visual Acuity Type Code Sequence", "SQ"});
    dict.put(0x00460122, new String[] {"Visual Acuity Right Eye Sequence", "SQ"});
    dict.put(0x00460123, new String[] {"Visual Acuity Left Eye Sequence", "SQ"});
    dict.put(0x00460124, new String[] {"Visual Acuity Both Eyes OpenSequence", "SQ"});
    dict.put(0x00460125, new String[] {"Viewing Distance Type", "CS"});
    dict.put(0x00460135, new String[] {"Visual Acuity Modifiers", "SS"});
    dict.put(0x00460137, new String[] {"Decimal Visual Acuity", "FD"});
    dict.put(0x00460139, new String[] {"Optotype Detailed Definition", "LO"});
    dict.put(0x00460145, new String[] {"Referenced RefractiveMeasurements Sequence", "SQ"});
    dict.put(0x00460146, new String[] {"Sphere Power", "FD"});
    dict.put(0x00460147, new String[] {"Cylinder Power", "FD"});
    dict.put(0x00460201, new String[] {"Corneal Topography Surface", "CS"});
    dict.put(0x00460202, new String[] {"Corneal Vertex Location", "FL"});
    dict.put(0x00460203, new String[] {"Pupil Centroid X-Coordinate", "FL"});
    dict.put(0x00460204, new String[] {"Pupil Centroid Y-Coordinate", "FL"});
    dict.put(0x00460205, new String[] {"Equivalent Pupil Radius", "FL"});
    dict.put(0x00460207, new String[] {"Corneal Topography Map TypeCode Sequence", "SQ"});
    dict.put(0x00460208, new String[] {"Vertices of the Outline of Pupil", "IS"});
    dict.put(0x00460210, new String[] {"Corneal Topography MappingNormals Sequence", "SQ"});
    dict.put(0x00460211, new String[] {"Maximum Corneal CurvatureSequence", "SQ"});
    dict.put(0x00460212, new String[] {"Maximum Corneal Curvature", "FL"});
    dict.put(0x00460213, new String[] {"Maximum Corneal CurvatureLocation", "FL"});
    dict.put(0x00460215, new String[] {"Minimum Keratometric Sequence", "SQ"});
    dict.put(0x00460218, new String[] {"Simulated Keratometric CylinderSequence", "SQ"});
    dict.put(0x00460220, new String[] {"Average Corneal Power", "FL"});
    dict.put(0x00460224, new String[] {"Corneal I-S Value", "FL"});
    dict.put(0x00460227, new String[] {"Analyzed Area", "FL"});
    dict.put(0x00460230, new String[] {"Surface Regularity Index", "FL"});
    dict.put(0x00460232, new String[] {"Surface Asymmetry Index", "FL"});
    dict.put(0x00460234, new String[] {"Corneal Eccentricity Index", "FL"});
    dict.put(0x00460236, new String[] {"Keratoconus Prediction Index", "FL"});
    dict.put(0x00460238, new String[] {"Decimal Potential Visual Acuity", "FL"});
    dict.put(0x00460242, new String[] {"Corneal Topography Map QualityEvaluation", "CS"});
    dict.put(0x00460244, new String[] {"Source Image Corneal ProcessedData Sequence", "SQ"});
    dict.put(0x00460247, new String[] {"Corneal Point Location", "FL"});
    dict.put(0x00460248, new String[] {"Corneal Point Estimated", "CS"});
    dict.put(0x00460249, new String[] {"Axial Power", "FL"});
    dict.put(0x00460250, new String[] {"Tangential Power", "FL"});
    dict.put(0x00460251, new String[] {"Refractive Power", "FL"});
    dict.put(0x00460252, new String[] {"Relative Elevation", "FL"});
    dict.put(0x00460253, new String[] {"Corneal Wavefront", "FL"});
  }

  /**
   * Adds attributes of group 0x0048.
   */
  private static void addAttributeGroup0048(Hashtable<Integer, String[]> dict) {
    dict.put(0x00480001, new String[] {"Imaged Volume Width", "FL"});
    dict.put(0x00480002, new String[] {"Imaged Volume Height", "FL"});
    dict.put(0x00480003, new String[] {"Imaged Volume Depth", "FL"});
    dict.put(0x00480006, new String[] {"Total Pixel Matrix Columns", "UL"});
    dict.put(0x00480007, new String[] {"Total Pixel Matrix Rows", "UL"});
    dict.put(0x00480008, new String[] {"Total Pixel Matrix Origin Sequence", "SQ"});
    dict.put(0x00480010, new String[] {"Specimen Label in Image", "CS"});
    dict.put(0x00480011, new String[] {"Focus Method", "CS"});
    dict.put(0x00480012, new String[] {"Extended Depth of Field", "CS"});
    dict.put(0x00480013, new String[] {"Number of Focal Planes", "US"});
    dict.put(0x00480014, new String[] {"Distance Between Focal Planes", "FL"});
    dict.put(0x00480015, new String[] {"Recommended Absent PixelCIELab Value", "US"});
    dict.put(0x00480100, new String[] {"Illuminator Type Code Sequence", "SQ"});
    dict.put(0x00480102, new String[] {"Image Orientation (Slide)", "DS"});
    dict.put(0x00480105, new String[] {"Optical Path Sequence", "SQ"});
    dict.put(0x00480106, new String[] {"Optical Path Identifier", "SH"});
    dict.put(0x00480107, new String[] {"Optical Path Description", "ST"});
    dict.put(0x00480108, new String[] {"Illumination Color Code Sequence", "SQ"});
    dict.put(0x00480110, new String[] {"Specimen Reference Sequence", "SQ"});
    dict.put(0x00480111, new String[] {"Condenser Lens Power", "DS"});
    dict.put(0x00480112, new String[] {"Objective Lens Power", "DS"});
    dict.put(0x00480113, new String[] {"Objective Lens Numerical Aperture", "DS"});
    dict.put(0x00480120, new String[] {"Palette Color Lookup TableSequence", "SQ"});
    dict.put(0x00480200, new String[] {"Referenced Image NavigationSequence", "SQ"});
    dict.put(0x00480201, new String[] {"Top Left Hand Corner of LocalizerArea", "US"});
    dict.put(0x00480202, new String[] {"Bottom Right Hand Corner ofLocalizer Area", "US"});
    dict.put(0x00480207, new String[] {"Optical Path IdentificationSequence", "SQ"});
    dict.put(0x0048021A, new String[] {"Plane Position (Slide) Sequence", "SQ"});
    dict.put(0x0048021E, new String[] {"Column Position In Total ImagePixel Matrix", "SL"});
    dict.put(0x0048021F, new String[] {"Row Position In Total Image PixelMatrix", "SL"});
    dict.put(0x00480301, new String[] {"Pixel Origin Interpretation", "CS"});
    dict.put(0x00480001, new String[] {"Imaged Volume Width", "FL"});
    dict.put(0x00480002, new String[] {"Imaged Volume Height", "FL"});
    dict.put(0x00480003, new String[] {"Imaged Volume Depth", "FL"});
    dict.put(0x00480006, new String[] {"Total Pixel Matrix Columns", "UL"});
    dict.put(0x00480007, new String[] {"Total Pixel Matrix Rows", "UL"});
    dict.put(0x00480008, new String[] {"Total Pixel Matrix Origin Sequence", "SQ"});
    dict.put(0x00480010, new String[] {"Specimen Label in Image", "CS"});
    dict.put(0x00480011, new String[] {"Focus Method", "CS"});
    dict.put(0x00480012, new String[] {"Extended Depth of Field", "CS"});
    dict.put(0x00480013, new String[] {"Number of Focal Planes", "US"});
    dict.put(0x00480014, new String[] {"Distance Between Focal Planes", "FL"});
    dict.put(0x00480015, new String[] {"Recommended Absent PixelCIELab Value", "US"});
    dict.put(0x00480100, new String[] {"Illuminator Type Code Sequence", "SQ"});
    dict.put(0x00480102, new String[] {"Image Orientation (Slide)", "DS"});
    dict.put(0x00480105, new String[] {"Optical Path Sequence", "SQ"});
    dict.put(0x00480106, new String[] {"Optical Path Identifier", "SH"});
    dict.put(0x00480107, new String[] {"Optical Path Description", "ST"});
    dict.put(0x00480108, new String[] {"Illumination Color Code Sequence", "SQ"});
    dict.put(0x00480110, new String[] {"Specimen Reference Sequence", "SQ"});
    dict.put(0x00480111, new String[] {"Condenser Lens Power", "DS"});
    dict.put(0x00480112, new String[] {"Objective Lens Power", "DS"});
    dict.put(0x00480113, new String[] {"Objective Lens Numerical Aperture", "DS"});
    dict.put(0x00480120, new String[] {"Palette Color Lookup TableSequence", "SQ"});
    dict.put(0x00480200, new String[] {"Referenced Image NavigationSequence", "SQ"});
    dict.put(0x00480201, new String[] {"Top Left Hand Corner of LocalizerArea", "US"});
    dict.put(0x00480202, new String[] {"Bottom Right Hand Corner ofLocalizer Area", "US"});
    dict.put(0x00480207, new String[] {"Optical Path IdentificationSequence", "SQ"});
    dict.put(0x0048021A, new String[] {"Plane Position (Slide) Sequence", "SQ"});
    dict.put(0x0048021E, new String[] {"Column Position In Total ImagePixel Matrix", "SL"});
    dict.put(0x0048021F, new String[] {"Row Position In Total Image PixelMatrix", "SL"});
    dict.put(0x00480301, new String[] {"Pixel Origin Interpretation", "CS"});
  }

  /**
   * Adds attributes of group 0x0050.
   */
  private static void addAttributeGroup0050(Hashtable<Integer, String[]> dict) {
    dict.put(0x00500004, new String[] {"Calibration Image", "CS"});
    dict.put(0x00500010, new String[] {"Device Sequence", "SQ"});
    dict.put(0x00500012, new String[] {"Container Component Type CodeSequence", "SQ"});
    dict.put(0x00500013, new String[] {"Container Component Thickness", "FD"});
    dict.put(0x00500014, new String[] {"Device Length", "DS"});
    dict.put(0x00500015, new String[] {"Container Component Width", "FD"});
    dict.put(0x00500016, new String[] {"Device Diameter", "DS"});
    dict.put(0x00500017, new String[] {"Device Diameter Units", "CS"});
    dict.put(0x00500018, new String[] {"Device Volume", "DS"});
    dict.put(0x00500019, new String[] {"Inter-Marker Distance", "DS"});
    dict.put(0x0050001A, new String[] {"Container Component Material", "CS"});
    dict.put(0x0050001B, new String[] {"Container Component ID", "LO"});
    dict.put(0x0050001C, new String[] {"Container Component Length", "FD"});
    dict.put(0x0050001D, new String[] {"Container Component Diameter", "FD"});
    dict.put(0x0050001E, new String[] {"Container Component Description", "LO"});
    dict.put(0x00500020, new String[] {"Device Description", "LO"});
  }

  /**
   * Adds attributes of group 0x0052.
   */
  private static void addAttributeGroup0052(Hashtable<Integer, String[]> dict) {
    dict.put(0x00520001, new String[] {"Contrast/Bolus Ingredient Percentby Volume", "FL"});
    dict.put(0x00520002, new String[] {"OCT Focal Distance", "FD"});
    dict.put(0x00520003, new String[] {"Beam Spot Size", "FD"});
    dict.put(0x00520004, new String[] {"Effective Refractive Index", "FD"});
    dict.put(0x00520006, new String[] {"OCT Acquisition Domain", "CS"});
    dict.put(0x00520007, new String[] {"OCT Optical Center Wavelength", "FD"});
    dict.put(0x00520008, new String[] {"Axial Resolution", "FD"});
    dict.put(0x00520009, new String[] {"Ranging Depth", "FD"});
    dict.put(0x00520011, new String[] {"A-line Rate", "FD"});
    dict.put(0x00520012, new String[] {"A-lines Per Frame", "US"});
    dict.put(0x00520013, new String[] {"Catheter Rotational Rate", "FD"});
    dict.put(0x00520014, new String[] {"A-line Pixel Spacing", "FD"});
    dict.put(0x00520016, new String[] {"Mode of Percutaneous AccessSequence", "SQ"});
    dict.put(0x00520025, new String[] {"Intravascular OCT Frame TypeSequence", "SQ"});
    dict.put(0x00520026, new String[] {"OCT Z Offset Applied", "CS"});
    dict.put(0x00520027, new String[] {"Intravascular Frame ContentSequence", "SQ"});
    dict.put(0x00520028, new String[] {"Intravascular Longitudinal Distance", "FD"});
    dict.put(0x00520029, new String[] {"Intravascular OCT Frame ContentSequence", "SQ"});
    dict.put(0x00520030, new String[] {"OCT Z Offset Correction", "SS"});
    dict.put(0x00520031, new String[] {"Catheter Direction of Rotation", "CS"});
    dict.put(0x00520033, new String[] {"Seam Line Location", "FD"});
    dict.put(0x00520034, new String[] {"First A-line Location", "FD"});
    dict.put(0x00520036, new String[] {"Seam Line Index", "US"});
    dict.put(0x00520038, new String[] {"Number of Padded A-lines", "US"});
    dict.put(0x00520039, new String[] {"Interpolation Type", "CS"});
    dict.put(0x0052003A, new String[] {"Refractive Index Applied", "CS"});
  }

  /**
   * Adds attributes of group 0x0054.
   */
  private static void addAttributeGroup0054(Hashtable<Integer, String[]> dict) {
    dict.put(0x00540010, new String[] {"Energy Window Vector", "US"});
    dict.put(0x00540011, new String[] {"Number of Energy Windows", "US"});
    dict.put(0x00540012, new String[] {"Energy Window InformationSequence", "SQ"});
    dict.put(0x00540013, new String[] {"Energy Window Range Sequence", "SQ"});
    dict.put(0x00540014, new String[] {"Energy Window Lower Limit", "DS"});
    dict.put(0x00540015, new String[] {"Energy Window Upper Limit", "DS"});
    dict.put(0x00540016, new String[] {"Radiopharmaceutical InformationSequence", "SQ"});
    dict.put(0x00540017, new String[] {"Residual Syringe Counts", "IS"});
    dict.put(0x00540018, new String[] {"Energy Window Name", "SH"});
    dict.put(0x00540020, new String[] {"Detector Vector", "US"});
    dict.put(0x00540021, new String[] {"Number of Detectors", "US"});
    dict.put(0x00540022, new String[] {"Detector Information Sequence", "SQ"});
    dict.put(0x00540030, new String[] {"Phase Vector", "US"});
    dict.put(0x00540031, new String[] {"Number of Phases", "US"});
    dict.put(0x00540032, new String[] {"Phase Information Sequence", "SQ"});
    dict.put(0x00540033, new String[] {"Number of Frames in Phase", "US"});
    dict.put(0x00540036, new String[] {"Phase Delay", "IS"});
    dict.put(0x00540038, new String[] {"Pause Between Frames", "IS"});
    dict.put(0x00540039, new String[] {"Phase Description", "CS"});
    dict.put(0x00540050, new String[] {"Rotation Vector", "US"});
    dict.put(0x00540051, new String[] {"Number of Rotations", "US"});
    dict.put(0x00540052, new String[] {"Rotation Information Sequence", "SQ"});
    dict.put(0x00540053, new String[] {"Number of Frames in Rotation", "US"});
    dict.put(0x00540060, new String[] {"R-R Interval Vector", "US"});
    dict.put(0x00540061, new String[] {"Number of R-R Intervals", "US"});
    dict.put(0x00540062, new String[] {"Gated Information Sequence", "SQ"});
    dict.put(0x00540063, new String[] {"Data Information Sequence", "SQ"});
    dict.put(0x00540070, new String[] {"Time Slot Vector", "US"});
    dict.put(0x00540071, new String[] {"Number of Time Slots", "US"});
    dict.put(0x00540072, new String[] {"Time Slot Information Sequence", "SQ"});
    dict.put(0x00540073, new String[] {"Time Slot Time", "DS"});
    dict.put(0x00540080, new String[] {"Slice Vector", "US"});
    dict.put(0x00540081, new String[] {"Number of Slices", "US"});
    dict.put(0x00540090, new String[] {"Angular View Vector", "US"});
    dict.put(0x00540100, new String[] {"Time Slice Vector", "US"});
    dict.put(0x00540101, new String[] {"Number of Time Slices", "US"});
    dict.put(0x00540200, new String[] {"Start Angle", "DS"});
    dict.put(0x00540202, new String[] {"Type of Detector Motion", "CS"});
    dict.put(0x00540210, new String[] {"Trigger Vector", "IS"});
    dict.put(0x00540211, new String[] {"Number of Triggers in Phase", "US"});
    dict.put(0x00540220, new String[] {"View Code Sequence", "SQ"});
    dict.put(0x00540222, new String[] {"View Modifier Code Sequence", "SQ"});
    dict.put(0x00540300, new String[] {"Radionuclide Code Sequence", "SQ"});
    dict.put(0x00540302, new String[] {"Administration Route CodeSequence", "SQ"});
    dict.put(0x00540304, new String[] {"Radiopharmaceutical CodeSequence", "SQ"});
    dict.put(0x00540306, new String[] {"Calibration Data Sequence", "SQ"});
    dict.put(0x00540308, new String[] {"Energy Window Number", "US"});
    dict.put(0x00540400, new String[] {"Image ID", "SH"});
    dict.put(0x00540410, new String[] {"Patient Orientation Code Sequence", "SQ"});
    dict.put(0x00540412, new String[] {"Patient Orientation Modifier CodeSequence", "SQ"});
    dict.put(0x00540414, new String[] {"Patient Gantry Relationship CodeSequence", "SQ"});
    dict.put(0x00540500, new String[] {"Slice Progression Direction", "CS"});
    dict.put(0x00540501, new String[] {"Scan Progression Direction", "CS"});
    dict.put(0x00541000, new String[] {"Series Type", "CS"});
    dict.put(0x00541001, new String[] {"Units", "CS"});
    dict.put(0x00541002, new String[] {"Counts Source", "CS"});
    dict.put(0x00541004, new String[] {"Reprojection Method", "CS"});
    dict.put(0x00541006, new String[] {"SUV Type", "CS"});
    dict.put(0x00541100, new String[] {"Randoms Correction Method", "CS"});
    dict.put(0x00541101, new String[] {"Attenuation Correction Method", "LO"});
    dict.put(0x00541102, new String[] {"Decay Correction", "CS"});
    dict.put(0x00541103, new String[] {"Reconstruction Method", "LO"});
    dict.put(0x00541104, new String[] {"Detector Lines of Response Used", "LO"});
    dict.put(0x00541105, new String[] {"Scatter Correction Method", "LO"});
    dict.put(0x00541200, new String[] {"Axial Acceptance", "DS"});
    dict.put(0x00541201, new String[] {"Axial Mash", "IS"});
    dict.put(0x00541202, new String[] {"Transverse Mash", "IS"});
    dict.put(0x00541203, new String[] {"Detector Element Size", "DS"});
    dict.put(0x00541210, new String[] {"Coincidence Window Width", "DS"});
    dict.put(0x00541220, new String[] {"Secondary Counts Type", "CS"});
    dict.put(0x00541300, new String[] {"Frame Reference Time", "DS"});
    dict.put(0x00541310, new String[] {"Primary (Prompts) CountsAccumulated", "IS"});
    dict.put(0x00541311, new String[] {"Secondary Counts Accumulated", "IS"});
    dict.put(0x00541320, new String[] {"Slice Sensitivity Factor", "DS"});
    dict.put(0x00541321, new String[] {"Decay Factor", "DS"});
    dict.put(0x00541322, new String[] {"Dose Calibration Factor", "DS"});
    dict.put(0x00541323, new String[] {"Scatter Fraction Factor", "DS"});
    dict.put(0x00541324, new String[] {"Dead Time Factor", "DS"});
    dict.put(0x00541330, new String[] {"Image Index", "US"});
    dict.put(0x00541400, new String[] {"Counts Included", "CS"}); // Retired
    dict.put(0x00541401, new String[] {"Dead Time Correction Flag", "CS"}); // Retired
  }

  /**
   * Adds attributes of group 0x0060.
   */
  private static void addAttributeGroup0060(Hashtable<Integer, String[]> dict) {
    dict.put(0x00603000, new String[] {"Histogram Sequence", "SQ"});
    dict.put(0x00603002, new String[] {"Histogram Number of Bins", "US"});
    // dict.put(0x00603004, new String[] {"Histogram First Bin Value", "US or SS"});
    // dict.put(0x00603006, new String[] {"Histogram Last Bin Value", "US or SS"});
    dict.put(0x00603008, new String[] {"Histogram Bin Width", "US"});
    dict.put(0x00603010, new String[] {"Histogram Explanation", "LO"});
    dict.put(0x00603020, new String[] {"Histogram Data", "UL"});
  }

  /**
   * Adds attributes of group 0x0062.
   */
  private static void addAttributeGroup0062(Hashtable<Integer, String[]> dict) {
    dict.put(0x00620001, new String[] {"Segmentation Type", "CS"});
    dict.put(0x00620002, new String[] {"Segment Sequence", "SQ"});
    dict.put(0x00620003, new String[] {"Segmented Property CategoryCode Sequence", "SQ"});
    dict.put(0x00620004, new String[] {"Segment Number", "US"});
    dict.put(0x00620005, new String[] {"Segment Label", "LO"});
    dict.put(0x00620006, new String[] {"Segment Description", "ST"});
    dict.put(0x00620008, new String[] {"Segment Algorithm Type", "CS"});
    dict.put(0x00620009, new String[] {"Segment Algorithm Name", "LO"});
    dict.put(0x0062000A, new String[] {"Segment Identification Sequence", "SQ"});
    dict.put(0x0062000B, new String[] {"Referenced Segment Number", "US"});
    dict.put(0x0062000C, new String[] {"Recommended Display GrayscaleValue", "US"});
    dict.put(0x0062000D, new String[] {"Recommended Display CIELabValue", "US"});
    dict.put(0x0062000E, new String[] {"Maximum Fractional Value", "US"});
    dict.put(0x0062000F, new String[] {"Segmented Property Type CodeSequence", "SQ"});
    dict.put(0x00620010, new String[] {"Segmentation Fractional Type", "CS"});
    dict.put(0x00620011, new String[] {"Segmented Property Type ModifierCode Sequence", "SQ"});
    dict.put(0x00620012, new String[] {"Used Segments Sequence", "SQ"});
  }

  /**
   * Adds attributes of group 0x0064.
   */
  private static void addAttributeGroup0064(Hashtable<Integer, String[]> dict) {
    dict.put(0x00640002, new String[] {"Deformable Registration Sequence", "SQ"});
    dict.put(0x00640003, new String[] {"Source Frame of Reference UID", "UI"});
    dict.put(0x00640005, new String[] {"Deformable Registration GridSequence", "SQ"});
    dict.put(0x00640007, new String[] {"Grid Dimensions", "UL"});
    dict.put(0x00640008, new String[] {"Grid Resolution", "FD"});
    dict.put(0x00640009, new String[] {"Vector Grid Data", "OF"});
    dict.put(0x0064000F, new String[] {"Pre Deformation MatrixRegistration Sequence", "SQ"});
    dict.put(0x00640010, new String[] {"Post Deformation MatrixRegistration Sequence", "SQ"});
  }

  /**
   * Adds attributes of group 0x0066.
   */
  private static void addAttributeGroup0066(Hashtable<Integer, String[]> dict) {
    dict.put(0x00660001, new String[] {"Number of Surfaces", "UL"});
    dict.put(0x00660002, new String[] {"Surface Sequence", "SQ"});
    dict.put(0x00660003, new String[] {"Surface Number", "UL"});
    dict.put(0x00660004, new String[] {"Surface Comments", "LT"});
    dict.put(0x00660009, new String[] {"Surface Processing", "CS"});
    dict.put(0x0066000A, new String[] {"Surface Processing Ratio", "FL"});
    dict.put(0x0066000B, new String[] {"Surface Processing Description", "LO"});
    dict.put(0x0066000C, new String[] {"Recommended PresentationOpacity", "FL"});
    dict.put(0x0066000D, new String[] {"Recommended Presentation Type", "CS"});
    dict.put(0x0066000E, new String[] {"Finite Volume", "CS"});
    dict.put(0x00660010, new String[] {"Manifold", "CS"});
    dict.put(0x00660011, new String[] {"Surface Points Sequence", "SQ"});
    dict.put(0x00660012, new String[] {"Surface Points Normals Sequence", "SQ"});
    dict.put(0x00660013, new String[] {"Surface Mesh Primitives Sequence", "SQ"});
    dict.put(0x00660015, new String[] {"Number of Surface Points", "UL"});
    dict.put(0x00660016, new String[] {"Point Coordinates Data", "OF"});
    dict.put(0x00660017, new String[] {"Point Position Accuracy", "FL"});
    dict.put(0x00660018, new String[] {"Mean Point Distance", "FL"});
    dict.put(0x00660019, new String[] {"Maximum Point Distance", "FL"});
    dict.put(0x0066001A, new String[] {"Points Bounding Box Coordinates", "FL"});
    dict.put(0x0066001B, new String[] {"Axis of Rotation", "FL"});
    dict.put(0x0066001C, new String[] {"Center of Rotation", "FL"});
    dict.put(0x0066001E, new String[] {"Number of Vectors", "UL"});
    dict.put(0x0066001F, new String[] {"Vector Dimensionality", "US"});
    dict.put(0x00660020, new String[] {"Vector Accuracy", "FL"});
    dict.put(0x00660021, new String[] {"Vector Coordinate Data", "OF"});
    dict.put(0x00660023, new String[] {"Triangle Point Index List", "OW"});
    dict.put(0x00660024, new String[] {"Edge Point Index List", "OW"});
    dict.put(0x00660025, new String[] {"Vertex Point Index List", "OW"});
    dict.put(0x00660026, new String[] {"Triangle Strip Sequence", "SQ"});
    dict.put(0x00660027, new String[] {"Triangle Fan Sequence", "SQ"});
    dict.put(0x00660028, new String[] {"Line Sequence", "SQ"});
    dict.put(0x00660029, new String[] {"Primitive Point Index List", "OW"});
    dict.put(0x0066002A, new String[] {"Surface Count", "UL"});
    dict.put(0x0066002B, new String[] {"Referenced Surface Sequence", "SQ"});
    dict.put(0x0066002C, new String[] {"Referenced Surface Number", "UL"});
    dict.put(0x0066002D,
             new String[] {"Segment Surface GenerationAlgorithm Identification Sequence", "SQ"});
    dict.put(0x0066002E, new String[] {"Segment Surface Source InstanceSequence", "SQ"});
    dict.put(0x0066002F, new String[] {"Algorithm Family Code Sequence", "SQ"});
    dict.put(0x00660030, new String[] {"Algorithm Name Code Sequence", "SQ"});
    dict.put(0x00660031, new String[] {"Algorithm Version", "LO"});
    dict.put(0x00660032, new String[] {"Algorithm Parameters", "LT"});
    dict.put(0x00660034, new String[] {"Facet Sequence", "SQ"});
    dict.put(0x00660035,
             new String[] {"Surface Processing AlgorithmIdentification Sequence", "SQ"});
    dict.put(0x00660036, new String[] {"Algorithm Name", "LO"});
    dict.put(0x00660037, new String[] {"Recommended Point Radius", "FL"});
    dict.put(0x00660038, new String[] {"Recommended Line Thickness", "FL"});
    dict.put(0x00660040, new String[] {"Long Primitive Point Index List", "UL"});
    dict.put(0x00660041, new String[] {"Long Triangle Point Index List", "UL"});
    dict.put(0x00660042, new String[] {"Long Edge Point Index List", "UL"});
    dict.put(0x00660043, new String[] {"Long Vertex Point Index List", "UL"});
  }

  /**
   * Adds attributes of group 0x0068.
   */
  private static void addAttributeGroup0068(Hashtable<Integer, String[]> dict) {
    dict.put(0x00686210, new String[] {"Implant Size", "LO"});
    dict.put(0x00686221, new String[] {"Implant Template Version", "LO"});
    dict.put(0x00686222, new String[] {"Replaced Implant TemplateSequence", "SQ"});
    dict.put(0x00686223, new String[] {"Implant Type", "CS"});
    dict.put(0x00686224, new String[] {"Derivation Implant TemplateSequence", "SQ"});
    dict.put(0x00686225, new String[] {"Original Implant TemplateSequence", "SQ"});
    dict.put(0x00686226, new String[] {"Effective DateTime", "DT"});
    dict.put(0x00686230, new String[] {"Implant Target Anatomy Sequence", "SQ"});
    dict.put(0x00686260, new String[] {"Information From ManufacturerSequence", "SQ"});
    dict.put(0x00686265, new String[] {"Notification From ManufacturerSequence", "SQ"});
    dict.put(0x00686270, new String[] {"Information Issue DateTime", "DT"});
    dict.put(0x00686280, new String[] {"Information Summary", "ST"});
    dict.put(0x006862A0, new String[] {"Implant Regulatory DisapprovalCode Sequence", "SQ"});
    dict.put(0x006862A5, new String[] {"Overall Template Spatial Tolerance", "FD"});
    dict.put(0x006862C0, new String[] {"HPGL Document Sequence", "SQ"});
    dict.put(0x006862D0, new String[] {"HPGL Document ID", "US"});
    dict.put(0x006862D5, new String[] {"HPGL Document Label", "LO"});
    dict.put(0x006862E0, new String[] {"View Orientation Code Sequence", "SQ"});
    dict.put(0x006862F0, new String[] {"View Orientation Modifier", "FD"});
    dict.put(0x006862F2, new String[] {"HPGL Document Scaling", "FD"});
    dict.put(0x00686300, new String[] {"HPGL Document", "OB"});
    dict.put(0x00686310, new String[] {"HPGL Contour Pen Number", "US"});
    dict.put(0x00686320, new String[] {"HPGL Pen Sequence", "SQ"});
    dict.put(0x00686330, new String[] {"HPGL Pen Number", "US"});
    dict.put(0x00686340, new String[] {"HPGL Pen Label", "LO"});
    dict.put(0x00686345, new String[] {"HPGL Pen Description", "ST"});
    dict.put(0x00686346, new String[] {"Recommended Rotation Point", "FD"});
    dict.put(0x00686347, new String[] {"Bounding Rectangle", "FD"});
    dict.put(0x00686350, new String[] {"Implant Template 3D ModelSurface Number", "US"});
    dict.put(0x00686360, new String[] {"Surface Model DescriptionSequence", "SQ"});
    dict.put(0x00686380, new String[] {"Surface Model Label", "LO"});
    dict.put(0x00686390, new String[] {"Surface Model Scaling Factor", "FD"});
    dict.put(0x006863A0, new String[] {"Materials Code Sequence", "SQ"});
    dict.put(0x006863A4, new String[] {"Coating Materials Code Sequence", "SQ"});
    dict.put(0x006863A8, new String[] {"Implant Type Code Sequence", "SQ"});
    dict.put(0x006863AC, new String[] {"Fixation Method Code Sequence", "SQ"});
    dict.put(0x006863B0, new String[] {"Mating Feature Sets Sequence", "SQ"});
    dict.put(0x006863C0, new String[] {"Mating Feature Set ID", "US"});
    dict.put(0x006863D0, new String[] {"Mating Feature Set Label", "LO"});
    dict.put(0x006863E0, new String[] {"Mating Feature Sequence", "SQ"});
    dict.put(0x006863F0, new String[] {"Mating Feature ID", "US"});
    dict.put(0x00686400, new String[] {"Mating Feature Degree of FreedomSequence", "SQ"});
    dict.put(0x00686410, new String[] {"Degree of Freedom ID", "US"});
    dict.put(0x00686420, new String[] {"Degree of Freedom Type", "CS"});
    dict.put(0x00686430, new String[] {"2D Mating Feature CoordinatesSequence", "SQ"});
    dict.put(0x00686440, new String[] {"Referenced HPGL Document ID", "US"});
    dict.put(0x00686450, new String[] {"2D Mating Point", "FD"});
    dict.put(0x00686460, new String[] {"2D Mating Axes", "FD"});
    dict.put(0x00686470, new String[] {"2D Degree of Freedom Sequence", "SQ"});
    dict.put(0x00686490, new String[] {"3D Degree of Freedom Axis", "FD"});
    dict.put(0x006864A0, new String[] {"Range of Freedom", "FD"});
    dict.put(0x006864C0, new String[] {"3D Mating Point", "FD"});
    dict.put(0x006864D0, new String[] {"3D Mating Axes", "FD"});
    dict.put(0x006864F0, new String[] {"2D Degree of Freedom Axis", "FD"});
    dict.put(0x00686500, new String[] {"Planning Landmark PointSequence", "SQ"});
    dict.put(0x00686510, new String[] {"Planning Landmark Line Sequence", "SQ"});
    dict.put(0x00686520, new String[] {"Planning Landmark PlaneSequence", "SQ"});
    dict.put(0x00686530, new String[] {"Planning Landmark ID", "US"});
    dict.put(0x00686540, new String[] {"Planning Landmark Description", "LO"});
    dict.put(0x00686545, new String[] {"Planning Landmark IdentificationCode Sequence", "SQ"});
    dict.put(0x00686550, new String[] {"2D Point Coordinates Sequence", "SQ"});
    dict.put(0x00686560, new String[] {"2D Point Coordinates", "FD"});
    dict.put(0x00686590, new String[] {"3D Point Coordinates", "FD"});
    dict.put(0x006865A0, new String[] {"2D Line Coordinates Sequence", "SQ"});
    dict.put(0x006865B0, new String[] {"2D Line Coordinates", "FD"});
    dict.put(0x006865D0, new String[] {"3D Line Coordinates", "FD"});
    dict.put(0x006865E0, new String[] {"2D Plane Coordinates Sequence", "SQ"});
    dict.put(0x006865F0, new String[] {"2D Plane Intersection", "FD"});
    dict.put(0x00686610, new String[] {"3D Plane Origin", "FD"});
    dict.put(0x00686620, new String[] {"3D Plane Normal", "FD"});
  }

  /**
   * Adds attributes of group 0x0070.
   */
  private static void addAttributeGroup0070(Hashtable<Integer, String[]> dict) {
    dict.put(0x00700001, new String[] {"Graphic Annotation Sequence", "SQ"});
    dict.put(0x00700002, new String[] {"Graphic Layer", "CS"});
    dict.put(0x00700003, new String[] {"Bounding Box Annotation Units", "CS"});
    dict.put(0x00700004, new String[] {"Anchor Point Annotation Units", "CS"});
    dict.put(0x00700005, new String[] {"Graphic Annotation Units", "CS"});
    dict.put(0x00700006, new String[] {"Unformatted Text Value", "ST"});
    dict.put(0x00700008, new String[] {"Text Object Sequence", "SQ"});
    dict.put(0x00700009, new String[] {"Graphic Object Sequence", "SQ"});
    dict.put(0x00700010, new String[] {"Bounding Box Top Left HandCorner", "FL"});
    dict.put(0x00700011, new String[] {"Bounding Box Bottom Right HandCorner", "FL"});
    dict.put(0x00700012, new String[] {"Bounding Box Text HorizontalJustification", "CS"});
    dict.put(0x00700014, new String[] {"Anchor Point", "FL"});
    dict.put(0x00700015, new String[] {"Anchor Point Visibility", "CS"});
    dict.put(0x00700020, new String[] {"Graphic Dimensions", "US"});
    dict.put(0x00700021, new String[] {"Number of Graphic Points", "US"});
    dict.put(0x00700022, new String[] {"Graphic Data", "FL"});
    dict.put(0x00700023, new String[] {"Graphic Type", "CS"});
    dict.put(0x00700024, new String[] {"Graphic Filled", "CS"});
    dict.put(0x00700040, new String[] {"Image Rotation (Retired)", "IS"}); // Retired
    dict.put(0x00700041, new String[] {"Image Horizontal Flip", "CS"});
    dict.put(0x00700042, new String[] {"Image Rotation", "US"});
    dict.put(0x00700050, new String[] {"Displayed Area Top Left HandCorner (Trial)", "US"}); // Retired
    dict.put(0x00700051, new String[] {"Displayed Area Bottom Right HandCorner (Trial)", "US"}); // Retired
    dict.put(0x00700052, new String[] {"Displayed Area Top Left HandCorner", "SL"});
    dict.put(0x00700053, new String[] {"Displayed Area Bottom Right HandCorner", "SL"});
    dict.put(0x0070005A, new String[] {"Displayed Area SelectionSequence", "SQ"});
    dict.put(0x00700060, new String[] {"Graphic Layer Sequence", "SQ"});
    dict.put(0x00700062, new String[] {"Graphic Layer Order", "IS"});
    dict.put(0x00700066, new String[] {"Graphic Layer RecommendedDisplay Grayscale Value", "US"});
    dict.put(0x00700067, new String[] {"Graphic Layer RecommendedDisplay RGB Value", "US"}); // Retired
    dict.put(0x00700068, new String[] {"Graphic Layer Description", "LO"});
    dict.put(0x00700080, new String[] {"Content Label", "CS"});
    dict.put(0x00700081, new String[] {"Content Description", "LO"});
    dict.put(0x00700082, new String[] {"Presentation Creation Date", "DA"});
    dict.put(0x00700083, new String[] {"Presentation Creation Time", "TM"});
    dict.put(0x00700084, new String[] {"Content Creator's Name", "PN"});
    dict.put(0x00700086, new String[] {"Content Creator's IdentificationCode Sequence", "SQ"});
    dict.put(0x00700087, new String[] {"Alternate Content DescriptionSequence", "SQ"});
    dict.put(0x00700100, new String[] {"Presentation Size Mode", "CS"});
    dict.put(0x00700101, new String[] {"Presentation Pixel Spacing", "DS"});
    dict.put(0x00700102, new String[] {"Presentation Pixel Aspect Ratio", "IS"});
    dict.put(0x00700103, new String[] {"Presentation Pixel MagnificationRatio", "FL"});
    dict.put(0x00700207, new String[] {"Graphic Group Label", "LO"});
    dict.put(0x00700208, new String[] {"Graphic Group Description", "ST"});
    dict.put(0x00700209, new String[] {"Compound Graphic Sequence", "SQ"});
    dict.put(0x00700226, new String[] {"Compound Graphic Instance ID", "UL"});
    dict.put(0x00700227, new String[] {"Font Name", "LO"});
    dict.put(0x00700228, new String[] {"Font Name Type", "CS"});
    dict.put(0x00700229, new String[] {"CSS Font Name", "LO"});
    dict.put(0x00700230, new String[] {"Rotation Angle", "FD"});
    dict.put(0x00700231, new String[] {"Text Style Sequence", "SQ"});
    dict.put(0x00700232, new String[] {"Line Style Sequence", "SQ"});
    dict.put(0x00700233, new String[] {"Fill Style Sequence", "SQ"});
    dict.put(0x00700234, new String[] {"Graphic Group Sequence", "SQ"});
    dict.put(0x00700241, new String[] {"Text Color CIELab Value", "US"});
    dict.put(0x00700242, new String[] {"Horizontal Alignment", "CS"});
    dict.put(0x00700243, new String[] {"Vertical Alignment", "CS"});
    dict.put(0x00700244, new String[] {"Shadow Style", "CS"});
    dict.put(0x00700245, new String[] {"Shadow Offset X", "FL"});
    dict.put(0x00700246, new String[] {"Shadow Offset Y", "FL"});
    dict.put(0x00700247, new String[] {"Shadow Color CIELab Value", "US"});
    dict.put(0x00700248, new String[] {"Underlined", "CS"});
    dict.put(0x00700249, new String[] {"Bold", "CS"});
    dict.put(0x00700250, new String[] {"Italic", "CS"});
    dict.put(0x00700251, new String[] {"Pattern On Color CIELab Value", "US"});
    dict.put(0x00700252, new String[] {"Pattern Off Color CIELab Value", "US"});
    dict.put(0x00700253, new String[] {"Line Thickness", "FL"});
    dict.put(0x00700254, new String[] {"Line Dashing Style", "CS"});
    dict.put(0x00700255, new String[] {"Line Pattern", "UL"});
    dict.put(0x00700256, new String[] {"Fill Pattern", "OB"});
    dict.put(0x00700257, new String[] {"Fill Mode", "CS"});
    dict.put(0x00700258, new String[] {"Shadow Opacity", "FL"});
    dict.put(0x00700261, new String[] {"Gap Length", "FL"});
    dict.put(0x00700262, new String[] {"Diameter of Visibility", "FL"});
    dict.put(0x00700273, new String[] {"Rotation Point", "FL"});
    dict.put(0x00700274, new String[] {"Tick Alignment", "CS"});
    dict.put(0x00700278, new String[] {"Show Tick Label", "CS"});
    dict.put(0x00700279, new String[] {"Tick Label Alignment", "CS"});
    dict.put(0x00700282, new String[] {"Compound Graphic Units", "CS"});
    dict.put(0x00700284, new String[] {"Pattern On Opacity", "FL"});
    dict.put(0x00700285, new String[] {"Pattern Off Opacity", "FL"});
    dict.put(0x00700287, new String[] {"Major Ticks Sequence", "SQ"});
    dict.put(0x00700288, new String[] {"Tick Position", "FL"});
    dict.put(0x00700289, new String[] {"Tick Label", "SH"});
    dict.put(0x00700294, new String[] {"Compound Graphic Type", "CS"});
    dict.put(0x00700295, new String[] {"Graphic Group ID", "UL"});
    dict.put(0x00700306, new String[] {"Shape Type", "CS"});
    dict.put(0x00700308, new String[] {"Registration Sequence", "SQ"});
    dict.put(0x00700309, new String[] {"Matrix Registration Sequence", "SQ"});
    dict.put(0x0070030A, new String[] {"Matrix Sequence", "SQ"});
    dict.put(0x0070030C, new String[] {"Frame of ReferenceTransformation Matrix Type", "CS"});
    dict.put(0x0070030D, new String[] {"Registration Type Code Sequence", "SQ"});
    dict.put(0x0070030F, new String[] {"Fiducial Description", "ST"});
    dict.put(0x00700310, new String[] {"Fiducial Identifier", "SH"});
    dict.put(0x00700311, new String[] {"Fiducial Identifier Code Sequence", "SQ"});
    dict.put(0x00700312, new String[] {"Contour Uncertainty Radius", "FD"});
    dict.put(0x00700314, new String[] {"Used Fiducials Sequence", "SQ"});
    dict.put(0x00700318, new String[] {"Graphic Coordinates DataSequence", "SQ"});
    dict.put(0x0070031A, new String[] {"Fiducial UID", "UI"});
    dict.put(0x0070031C, new String[] {"Fiducial Set Sequence", "SQ"});
    dict.put(0x0070031E, new String[] {"Fiducial Sequence", "SQ"});
    dict.put(0x00700401, new String[] {"Graphic Layer RecommendedDisplay CIELab Value", "US"});
    dict.put(0x00700402, new String[] {"Blending Sequence", "SQ"});
    dict.put(0x00700403, new String[] {"Relative Opacity", "FL"});
    dict.put(0x00700404, new String[] {"Referenced Spatial RegistrationSequence", "SQ"});
    dict.put(0x00700405, new String[] {"Blending Position", "CS"});
  }

  /**
   * Adds attributes of group 0x0072.
   */
  private static void addAttributeGroup0072(Hashtable<Integer, String[]> dict) {
    dict.put(0x00720002, new String[] {"Hanging Protocol Name", "SH"});
    dict.put(0x00720004, new String[] {"Hanging Protocol Description", "LO"});
    dict.put(0x00720006, new String[] {"Hanging Protocol Level", "CS"});
    dict.put(0x00720008, new String[] {"Hanging Protocol Creator", "LO"});
    dict.put(0x0072000A, new String[] {"Hanging Protocol CreationDateTime", "DT"});
    dict.put(0x0072000C, new String[] {"Hanging Protocol DefinitionSequence", "SQ"});
    dict.put(0x0072000E, new String[] {"Hanging Protocol UserIdentification Code Sequence", "SQ"});
    dict.put(0x00720010, new String[] {"Hanging Protocol User GroupName", "LO"});
    dict.put(0x00720012, new String[] {"Source Hanging ProtocolSequence", "SQ"});
    dict.put(0x00720014, new String[] {"Number of Priors Referenced", "US"});
    dict.put(0x00720020, new String[] {"Image Sets Sequence", "SQ"});
    dict.put(0x00720022, new String[] {"Image Set Selector Sequence", "SQ"});
    dict.put(0x00720024, new String[] {"Image Set Selector Usage Flag", "CS"});
    dict.put(0x00720026, new String[] {"Selector Attribute", "AT"});
    dict.put(0x00720028, new String[] {"Selector Value Number", "US"});
    dict.put(0x00720030, new String[] {"Time Based Image Sets Sequence", "SQ"});
    dict.put(0x00720032, new String[] {"Image Set Number", "US"});
    dict.put(0x00720034, new String[] {"Image Set Selector Category", "CS"});
    dict.put(0x00720038, new String[] {"Relative Time", "US"});
    dict.put(0x0072003A, new String[] {"Relative Time Units", "CS"});
    dict.put(0x0072003C, new String[] {"Abstract Prior Value", "SS"});
    dict.put(0x0072003E, new String[] {"Abstract Prior Code Sequence", "SQ"});
    dict.put(0x00720040, new String[] {"Image Set Label", "LO"});
    dict.put(0x00720050, new String[] {"Selector Attribute VR", "CS"});
    dict.put(0x00720052, new String[] {"Selector Sequence Pointer", "AT"});
    dict.put(0x00720054, new String[] {"Selector Sequence Pointer PrivateCreator", "LO"});
    dict.put(0x00720056, new String[] {"Selector Attribute Private Creator", "LO"});
    dict.put(0x00720060, new String[] {"Selector AT Value", "AT"});
    dict.put(0x00720062, new String[] {"Selector CS Value", "CS"});
    dict.put(0x00720064, new String[] {"Selector IS Value", "IS"});
    dict.put(0x00720066, new String[] {"Selector LO Value", "LO"});
    dict.put(0x00720068, new String[] {"Selector LT Value", "LT"});
    dict.put(0x0072006A, new String[] {"Selector PN Value", "PN"});
    dict.put(0x0072006C, new String[] {"Selector SH Value", "SH"});
    dict.put(0x0072006E, new String[] {"Selector ST Value", "ST"});
    dict.put(0x00720070, new String[] {"Selector UT Value", "UT"});
    dict.put(0x00720072, new String[] {"Selector DS Value", "DS"});
    dict.put(0x00720074, new String[] {"Selector FD Value", "FD"});
    dict.put(0x00720076, new String[] {"Selector FL Value", "FL"});
    dict.put(0x00720078, new String[] {"Selector UL Value", "UL"});
    dict.put(0x0072007A, new String[] {"Selector US Value", "US"});
    dict.put(0x0072007C, new String[] {"Selector SL Value", "SL"});
    dict.put(0x0072007E, new String[] {"Selector SS Value", "SS"});
    dict.put(0x0072007F, new String[] {"Selector UI Value", "UI"});
    dict.put(0x00720080, new String[] {"Selector Code Sequence Value", "SQ"});
    dict.put(0x00720100, new String[] {"Number of Screens", "US"});
    dict.put(0x00720102, new String[] {"Nominal Screen DefinitionSequence", "SQ"});
    dict.put(0x00720104, new String[] {"Number of Vertical Pixels", "US"});
    dict.put(0x00720106, new String[] {"Number of Horizontal Pixels", "US"});
    dict.put(0x00720108, new String[] {"Display Environment SpatialPosition", "FD"});
    dict.put(0x0072010A, new String[] {"Screen Minimum Grayscale BitDepth", "US"});
    dict.put(0x0072010C, new String[] {"Screen Minimum Color Bit Depth", "US"});
    dict.put(0x0072010E, new String[] {"Application Maximum Repaint Time", "US"});
    dict.put(0x00720200, new String[] {"Display Sets Sequence", "SQ"});
    dict.put(0x00720202, new String[] {"Display Set Number", "US"});
    dict.put(0x00720203, new String[] {"Display Set Label", "LO"});
    dict.put(0x00720204, new String[] {"Display Set Presentation Group", "US"});
    dict.put(0x00720206, new String[] {"Display Set Presentation GroupDescription", "LO"});
    dict.put(0x00720208, new String[] {"Partial Data Display Handling", "CS"});
    dict.put(0x00720210, new String[] {"Synchronized Scrolling Sequence", "SQ"});
    dict.put(0x00720212, new String[] {"Display Set Scrolling Group", "US"});
    dict.put(0x00720214, new String[] {"Navigation Indicator Sequence", "SQ"});
    dict.put(0x00720216, new String[] {"Navigation Display Set", "US"});
    dict.put(0x00720218, new String[] {"Reference Display Sets", "US"});
    dict.put(0x00720300, new String[] {"Image Boxes Sequence", "SQ"});
    dict.put(0x00720302, new String[] {"Image Box Number", "US"});
    dict.put(0x00720304, new String[] {"Image Box Layout Type", "CS"});
    dict.put(0x00720306, new String[] {"Image Box Tile HorizontalDimension", "US"});
    dict.put(0x00720308, new String[] {"Image Box Tile Vertical Dimension", "US"});
    dict.put(0x00720310, new String[] {"Image Box Scroll Direction", "CS"});
    dict.put(0x00720312, new String[] {"Image Box Small Scroll Type", "CS"});
    dict.put(0x00720314, new String[] {"Image Box Small Scroll Amount", "US"});
    dict.put(0x00720316, new String[] {"Image Box Large Scroll Type", "CS"});
    dict.put(0x00720318, new String[] {"Image Box Large Scroll Amount", "US"});
    dict.put(0x00720320, new String[] {"Image Box Overlap Priority", "US"});
    dict.put(0x00720330, new String[] {"Cine Relative to Real-Time", "FD"});
    dict.put(0x00720400, new String[] {"Filter Operations Sequence", "SQ"});
    dict.put(0x00720402, new String[] {"Filter-by Category", "CS"});
    dict.put(0x00720404, new String[] {"Filter-by Attribute Presence", "CS"});
    dict.put(0x00720406, new String[] {"Filter-by Operator", "CS"});
    dict.put(0x00720420, new String[] {"Structured Display BackgroundCIELab Value", "US"});
    dict.put(0x00720421, new String[] {"Empty Image Box CIELab Value", "US"});
    dict.put(0x00720422, new String[] {"Structured Display Image BoxSequence", "SQ"});
    dict.put(0x00720424, new String[] {"Structured Display Text BoxSequence", "SQ"});
    dict.put(0x00720427, new String[] {"Referenced First Frame Sequence", "SQ"});
    dict.put(0x00720430, new String[] {"Image Box SynchronizationSequence", "SQ"});
    dict.put(0x00720432, new String[] {"Synchronized Image Box List", "US"});
    dict.put(0x00720434, new String[] {"Type of Synchronization", "CS"});
    dict.put(0x00720500, new String[] {"Blending Operation Type", "CS"});
    dict.put(0x00720510, new String[] {"Reformatting Operation Type", "CS"});
    dict.put(0x00720512, new String[] {"Reformatting Thickness", "FD"});
    dict.put(0x00720514, new String[] {"Reformatting Interval", "FD"});
    dict.put(0x00720516, new String[] {"Reformatting Operation Initial ViewDirection", "CS"});
    dict.put(0x00720520, new String[] {"3D Rendering Type", "CS"});
    dict.put(0x00720600, new String[] {"Sorting Operations Sequence", "SQ"});
    dict.put(0x00720602, new String[] {"Sort-by Category", "CS"});
    dict.put(0x00720604, new String[] {"Sorting Direction", "CS"});
    dict.put(0x00720700, new String[] {"Display Set Patient Orientation", "CS"});
    dict.put(0x00720702, new String[] {"VOI Type", "CS"});
    dict.put(0x00720704, new String[] {"Pseudo-Color Type", "CS"});
    dict.put(0x00720705, new String[] {"Pseudo-Color Palette InstanceReference Sequence", "SQ"});
    dict.put(0x00720706, new String[] {"Show Grayscale Inverted", "CS"});
    dict.put(0x00720710, new String[] {"Show Image True Size Flag", "CS"});
    dict.put(0x00720712, new String[] {"Show Graphic Annotation Flag", "CS"});
    dict.put(0x00720714, new String[] {"Show Patient Demographics Flag", "CS"});
    dict.put(0x00720716, new String[] {"Show Acquisition Techniques Flag", "CS"});
    dict.put(0x00720717, new String[] {"Display Set Horizontal Justification", "CS"});
    dict.put(0x00720718, new String[] {"Display Set Vertical Justification", "CS"});
  }

  /**
   * Adds attributes of group 0x0074.
   */
  private static void addAttributeGroup0074(Hashtable<Integer, String[]> dict) {
    dict.put(0x00740120, new String[] {"Continuation Start Meterset", "FD"});
    dict.put(0x00740121, new String[] {"Continuation End Meterset", "FD"});
    dict.put(0x00741000, new String[] {"Procedure Step State", "CS"});
    dict.put(0x00741002, new String[] {"Procedure Step ProgressInformation Sequence", "SQ"});
    dict.put(0x00741004, new String[] {"Procedure Step Progress", "DS"});
    dict.put(0x00741006, new String[] {"Procedure Step ProgressDescription", "ST"});
    dict.put(0x00741008, new String[] {"Procedure Step CommunicationsURI Sequence", "SQ"});
    dict.put(0x0074100A, new String[] {"Contact URI", "UR"});
    dict.put(0x0074100C, new String[] {"Contact Display Name", "LO"});
    dict.put(0x0074100E,
             new String[] {"Procedure Step DiscontinuationReason Code Sequence", "SQ"});
    dict.put(0x00741020, new String[] {"Beam Task Sequence", "SQ"});
    dict.put(0x00741022, new String[] {"Beam Task Type", "CS"});
    dict.put(0x00741024, new String[] {"Beam Order Index (Trial)", "IS"}); // Retired
    dict.put(0x00741025, new String[] {"Autosequence Flag", "CS"});
    dict.put(0x00741026, new String[] {"Table Top Vertical AdjustedPosition", "FD"});
    dict.put(0x00741027, new String[] {"Table Top Longitudinal AdjustedPosition", "FD"});
    dict.put(0x00741028, new String[] {"Table Top Lateral AdjustedPosition", "FD"});
    dict.put(0x0074102A, new String[] {"Patient Support Adjusted Angle", "FD"});
    dict.put(0x0074102B, new String[] {"Table Top Eccentric AdjustedAngle", "FD"});
    dict.put(0x0074102C, new String[] {"Table Top Pitch Adjusted Angle", "FD"});
    dict.put(0x0074102D, new String[] {"Table Top Roll Adjusted Angle", "FD"});
    dict.put(0x00741030, new String[] {"Delivery Verification ImageSequence", "SQ"});
    dict.put(0x00741032, new String[] {"Verification Image Timing", "CS"});
    dict.put(0x00741034, new String[] {"Double Exposure Flag", "CS"});
    dict.put(0x00741036, new String[] {"Double Exposure Ordering", "CS"});
    dict.put(0x00741038, new String[] {"Double Exposure Meterset (Trial)", "DS"}); // Retired
    dict.put(0x0074103A, new String[] {"Double Exposure Field Delta (Trial)", "DS"}); // Retired
    dict.put(0x00741040, new String[] {"Related Reference RT ImageSequence", "SQ"});
    dict.put(0x00741042, new String[] {"General Machine VerificationSequence", "SQ"});
    dict.put(0x00741044, new String[] {"Conventional Machine VerificationSequence", "SQ"});
    dict.put(0x00741046, new String[] {"Ion Machine Verification Sequence", "SQ"});
    dict.put(0x00741048, new String[] {"Failed Attributes Sequence", "SQ"});
    dict.put(0x0074104A, new String[] {"Overridden Attributes Sequence", "SQ"});
    dict.put(0x0074104C, new String[] {"Conventional Control PointVerification Sequence", "SQ"});
    dict.put(0x0074104E, new String[] {"Ion Control Point VerificationSequence", "SQ"});
    dict.put(0x00741050, new String[] {"Attribute Occurrence Sequence", "SQ"});
    dict.put(0x00741052, new String[] {"Attribute Occurrence Pointer", "AT"});
    dict.put(0x00741054, new String[] {"Attribute Item Selector", "UL"});
    dict.put(0x00741056, new String[] {"Attribute Occurrence PrivateCreator", "LO"});
    dict.put(0x00741057, new String[] {"Selector Sequence Pointer Items", "IS"});
    dict.put(0x00741200, new String[] {"Scheduled Procedure Step Priority", "CS"});
    dict.put(0x00741202, new String[] {"Worklist Label", "LO"});
    dict.put(0x00741204, new String[] {"Procedure Step Label", "LO"});
    dict.put(0x00741210, new String[] {"Scheduled Processing ParametersSequence", "SQ"});
    dict.put(0x00741212, new String[] {"Performed Processing ParametersSequence", "SQ"});
    dict.put(0x00741216,
             new String[] {"Unified Procedure Step PerformedProcedure Sequence", "SQ"});
    dict.put(0x00741220, new String[] {"Related Procedure Step Sequence", "SQ"}); // Retired
    dict.put(0x00741222, new String[] {"Procedure Step Relationship Type", "LO"}); // Retired
    dict.put(0x00741224, new String[] {"Replaced Procedure StepSequence", "SQ"});
    dict.put(0x00741230, new String[] {"Deletion Lock", "LO"});
    dict.put(0x00741234, new String[] {"Receiving AE", "AE"});
    dict.put(0x00741236, new String[] {"Requesting AE", "AE"});
    dict.put(0x00741238, new String[] {"Reason for Cancellation", "LT"});
    dict.put(0x00741242, new String[] {"SCP Status", "CS"});
    dict.put(0x00741244, new String[] {"Subscription List Status", "CS"});
    dict.put(0x00741246, new String[] {"Unified Procedure Step List Status", "CS"});
    dict.put(0x00741324, new String[] {"Beam Order Index", "UL"});
    dict.put(0x00741338, new String[] {"Double Exposure Meterset", "FD"});
    dict.put(0x0074133A, new String[] {"Double Exposure Field Delta", "FD"});
  }

  /**
   * Adds attributes of group 0x0076.
   */
  private static void addAttributeGroup0076(Hashtable<Integer, String[]> dict) {
    dict.put(0x00760001, new String[] {"Implant Assembly Template Name", "LO"});
    dict.put(0x00760003, new String[] {"Implant Assembly Template Issuer", "LO"});
    dict.put(0x00760006, new String[] {"Implant Assembly TemplateVersion", "LO"});
    dict.put(0x00760008, new String[] {"Replaced Implant AssemblyTemplate Sequence", "SQ"});
    dict.put(0x0076000A, new String[] {"Implant Assembly Template Type", "CS"});
    dict.put(0x0076000C, new String[] {"Original Implant AssemblyTemplate Sequence", "SQ"});
    dict.put(0x0076000E, new String[] {"Derivation Implant AssemblyTemplate Sequence", "SQ"});
    dict.put(0x00760010, new String[] {"Implant Assembly Template TargetAnatomy Sequence", "SQ"});
    dict.put(0x00760020, new String[] {"Procedure Type Code Sequence", "SQ"});
    dict.put(0x00760030, new String[] {"Surgical Technique", "LO"});
    dict.put(0x00760032, new String[] {"Component Types Sequence", "SQ"});
    dict.put(0x00760034, new String[] {"Component Type Code Sequence", "CS"});
    dict.put(0x00760036, new String[] {"Exclusive Component Type", "CS"});
    dict.put(0x00760038, new String[] {"Mandatory Component Type", "CS"});
    dict.put(0x00760040, new String[] {"Component Sequence", "SQ"});
    dict.put(0x00760055, new String[] {"Component ID", "US"});
    dict.put(0x00760060, new String[] {"Component Assembly Sequence", "SQ"});
    dict.put(0x00760070, new String[] {"Component 1 Referenced ID", "US"});
    dict.put(0x00760080, new String[] {"Component 1 Referenced MatingFeature Set ID", "US"});
    dict.put(0x00760090, new String[] {"Component 1 Referenced MatingFeature ID", "US"});
    dict.put(0x007600A0, new String[] {"Component 2 Referenced ID", "US"});
    dict.put(0x007600B0, new String[] {"Component 2 Referenced MatingFeature Set ID", "US"});
    dict.put(0x007600C0, new String[] {"Component 2 Referenced MatingFeature ID", "US"});
  }

  /**
   * Adds attributes of group 0x0078.
   */
  private static void addAttributeGroup0078(Hashtable<Integer, String[]> dict) {
    dict.put(0x00780001, new String[] {"Implant Template Group Name", "LO"});
    dict.put(0x00780010, new String[] {"Implant Template GroupDescription", "ST"});
    dict.put(0x00780020, new String[] {"Implant Template Group Issuer", "LO"});
    dict.put(0x00780024, new String[] {"Implant Template Group Version", "LO"});
    dict.put(0x00780026, new String[] {"Replaced Implant Template GroupSequence", "SQ"});
    dict.put(0x00780028, new String[] {"Implant Template Group TargetAnatomy Sequence", "SQ"});
    dict.put(0x0078002A, new String[] {"Implant Template Group MembersSequence", "SQ"});
    dict.put(0x0078002E, new String[] {"Implant Template Group MemberID", "US"});
    dict.put(0x00780050, new String[] {"3D Implant Template GroupMember Matching Point", "FD"});
    dict.put(0x00780060, new String[] {"3D Implant Template GroupMember Matching Axes", "FD"});
    dict.put(0x00780070,
             new String[] {"Implant Template Group MemberMatching 2D CoordinatesSequence", "SQ"});
    dict.put(0x00780090, new String[] {"2D Implant Template GroupMember Matching Point", "FD"});
    dict.put(0x007800A0, new String[] {"2D Implant Template GroupMember Matching Axes", "FD"});
    dict.put(0x007800B0,
             new String[] {"Implant Template Group VariationDimension Sequence", "SQ"});
    dict.put(0x007800B2, new String[] {"Implant Template Group VariationDimension Name", "LO"});
    dict.put(0x007800B4,
             new String[] {"Implant Template Group VariationDimension Rank Sequence", "SQ"});
    dict.put(0x007800B6, new String[] {"Referenced Implant TemplateGroup Member ID", "US"});
    dict.put(0x007800B8, new String[] {"Implant Template Group VariationDimension Rank", "US"});
  }

  /**
   * Adds attributes of group 0x0080.
   */
  private static void addAttributeGroup0080(Hashtable<Integer, String[]> dict) {
    dict.put(0x00800001, new String[] {"Surface Scan Acquisition TypeCode Sequence", "SQ"});
    dict.put(0x00800002, new String[] {"Surface Scan Mode CodeSequence", "SQ"});
    dict.put(0x00800003, new String[] {"Registration Method CodeSequence", "SQ"});
    dict.put(0x00800004, new String[] {"Shot Duration Time", "FD"});
    dict.put(0x00800005, new String[] {"Shot Offset Time", "FD"});
    dict.put(0x00800006, new String[] {"Surface Point Presentation ValueData", "US"});
    dict.put(0x00800007, new String[] {"Surface Point Color CIELab ValueData", "US"});
    dict.put(0x00800008, new String[] {"UV Mapping Sequence", "SQ"});
    dict.put(0x00800009, new String[] {"Texture Label", "SH"});
    dict.put(0x00800010, new String[] {"U Value Data", "OF"});
    dict.put(0x00800011, new String[] {"V Value Data", "OF"});
    dict.put(0x00800012, new String[] {"Referenced Texture Sequence", "SQ"});
    dict.put(0x00800013, new String[] {"Referenced Surface DataSequence", "SQ"});
  }

  /**
   * Adds attributes of group 0x0088.
   */
  private static void addAttributeGroup0088(Hashtable<Integer, String[]> dict) {
    dict.put(0x00880130, new String[] {"Storage Media File-set ID", "SH"});
    dict.put(0x00880140, new String[] {"Storage Media File-set UID", "UI"});
    dict.put(0x00880200, new String[] {"Icon Image Sequence", "SQ"});
    dict.put(0x00880904, new String[] {"Topic Title", "LO"}); // Retired
    dict.put(0x00880906, new String[] {"Topic Subject", "ST"}); // Retired
    dict.put(0x00880910, new String[] {"Topic Author", "LO"}); // Retired
    dict.put(0x00880912, new String[] {"Topic Keywords", "LO"}); // Retired
  }

  /**
   * Adds attributes of group 0x0100.
   */
  private static void addAttributeGroup0100(Hashtable<Integer, String[]> dict) {
    dict.put(0x01000410, new String[] {"SOP Instance Status", "CS"});
    dict.put(0x01000420, new String[] {"SOP Authorization DateTime", "DT"});
    dict.put(0x01000424, new String[] {"SOP Authorization Comment", "LT"});
    dict.put(0x01000426, new String[] {"Authorization EquipmentCertification Number", "LO"});
  }

  /**
   * Adds attributes of group 0x0400.
   */
  private static void addAttributeGroup0400(Hashtable<Integer, String[]> dict) {
    dict.put(0x04000005, new String[] {"MAC ID Number", "US"});
    dict.put(0x04000010, new String[] {"MAC Calculation Transfer SyntaxUID", "UI"});
    dict.put(0x04000015, new String[] {"MAC Algorithm", "CS"});
    dict.put(0x04000020, new String[] {"Data Elements Signed", "AT"});
    dict.put(0x04000100, new String[] {"Digital Signature UID", "UI"});
    dict.put(0x04000105, new String[] {"Digital Signature DateTime", "DT"});
    dict.put(0x04000110, new String[] {"Certificate Type", "CS"});
    dict.put(0x04000115, new String[] {"Certificate of Signer", "OB"});
    dict.put(0x04000120, new String[] {"Signature", "OB"});
    dict.put(0x04000305, new String[] {"Certified Timestamp Type", "CS"});
    dict.put(0x04000310, new String[] {"Certified Timestamp", "OB"});
    dict.put(0x04000401, new String[] {"Digital Signature Purpose CodeSequence", "SQ"});
    dict.put(0x04000402, new String[] {"Referenced Digital SignatureSequence", "SQ"});
    dict.put(0x04000403, new String[] {"Referenced SOP Instance MACSequence", "SQ"});
    dict.put(0x04000404, new String[] {"MAC", "OB"});
    dict.put(0x04000500, new String[] {"Encrypted Attributes Sequence", "SQ"});
    dict.put(0x04000510, new String[] {"Encrypted Content Transfer SyntaxUID", "UI"});
    dict.put(0x04000520, new String[] {"Encrypted Content", "OB"});
    dict.put(0x04000550, new String[] {"Modified Attributes Sequence", "SQ"});
    dict.put(0x04000561, new String[] {"Original Attributes Sequence", "SQ"});
    dict.put(0x04000562, new String[] {"Attribute Modification DateTime", "DT"});
    dict.put(0x04000563, new String[] {"Modifying System", "LO"});
    dict.put(0x04000564, new String[] {"Source of Previous Values", "LO"});
    dict.put(0x04000565, new String[] {"Reason for the AttributeModification", "CS"});
  }

  /**
   * Adds attributes of group 0x2000.
   */
  private static void addAttributeGroup2000(Hashtable<Integer, String[]> dict) {
    dict.put(0x20000010, new String[] {"Number of Copies", "IS"});
    dict.put(0x2000001E, new String[] {"Printer Configuration Sequence", "SQ"});
    dict.put(0x20000020, new String[] {"Print Priority", "CS"});
    dict.put(0x20000030, new String[] {"Medium Type", "CS"});
    dict.put(0x20000040, new String[] {"Film Destination", "CS"});
    dict.put(0x20000050, new String[] {"Film Session Label", "LO"});
    dict.put(0x20000060, new String[] {"Memory Allocation", "IS"});
    dict.put(0x20000061, new String[] {"Maximum Memory Allocation", "IS"});
    dict.put(0x20000062, new String[] {"Color Image Printing Flag", "CS"}); // Retired
    dict.put(0x20000063, new String[] {"Collation Flag", "CS"}); // Retired
    dict.put(0x20000065, new String[] {"Annotation Flag", "CS"}); // Retired
    dict.put(0x20000067, new String[] {"Image Overlay Flag", "CS"}); // Retired
    dict.put(0x20000069, new String[] {"Presentation LUT Flag", "CS"}); // Retired
    dict.put(0x2000006A, new String[] {"Image Box Presentation LUT Flag", "CS"}); // Retired
    dict.put(0x200000A0, new String[] {"Memory Bit Depth", "US"});
    dict.put(0x200000A1, new String[] {"Printing Bit Depth", "US"});
    dict.put(0x200000A2, new String[] {"Media Installed Sequence", "SQ"});
    dict.put(0x200000A4, new String[] {"Other Media Available Sequence", "SQ"});
    dict.put(0x200000A8, new String[] {"Supported Image Display FormatsSequence", "SQ"});
    dict.put(0x20000500, new String[] {"Referenced Film Box Sequence", "SQ"});
    dict.put(0x20000510, new String[] {"Referenced Stored Print Sequence", "SQ"}); // Retired
  }

  /**
   * Adds attributes of group 0x2010.
   */
  private static void addAttributeGroup2010(Hashtable<Integer, String[]> dict) {
    dict.put(0x20100010, new String[] {"Image Display Format", "ST"});
    dict.put(0x20100030, new String[] {"Annotation Display Format ID", "CS"});
    dict.put(0x20100040, new String[] {"Film Orientation", "CS"});
    dict.put(0x20100050, new String[] {"Film Size ID", "CS"});
    dict.put(0x20100052, new String[] {"Printer Resolution ID", "CS"});
    dict.put(0x20100054, new String[] {"Default Printer Resolution ID", "CS"});
    dict.put(0x20100060, new String[] {"Magnification Type", "CS"});
    dict.put(0x20100080, new String[] {"Smoothing Type", "CS"});
    dict.put(0x201000A6, new String[] {"Default Magnification Type", "CS"});
    dict.put(0x201000A7, new String[] {"Other Magnification TypesAvailable", "CS"});
    dict.put(0x201000A8, new String[] {"Default Smoothing Type", "CS"});
    dict.put(0x201000A9, new String[] {"Other Smoothing Types Available", "CS"});
    dict.put(0x20100100, new String[] {"Border Density", "CS"});
    dict.put(0x20100110, new String[] {"Empty Image Density", "CS"});
    dict.put(0x20100120, new String[] {"Min Density", "US"});
    dict.put(0x20100130, new String[] {"Max Density", "US"});
    dict.put(0x20100140, new String[] {"Trim", "CS"});
    dict.put(0x20100150, new String[] {"Configuration Information", "ST"});
    dict.put(0x20100152, new String[] {"Configuration InformationDescription", "LT"});
    dict.put(0x20100154, new String[] {"Maximum Collated Films", "IS"});
    dict.put(0x2010015E, new String[] {"Illumination", "US"});
    dict.put(0x20100160, new String[] {"Reflected Ambient Light", "US"});
    dict.put(0x20100376, new String[] {"Printer Pixel Spacing", "DS"});
    dict.put(0x20100500, new String[] {"Referenced Film SessionSequence", "SQ"});
    dict.put(0x20100510, new String[] {"Referenced Image Box Sequence", "SQ"});
    dict.put(0x20100520, new String[] {"Referenced Basic Annotation BoxSequence", "SQ"});
  }

  /**
   * Adds attributes of group 0x2020.
   */
  private static void addAttributeGroup2020(Hashtable<Integer, String[]> dict) {
    dict.put(0x20200010, new String[] {"Image Box Position", "US"});
    dict.put(0x20200020, new String[] {"Polarity", "CS"});
    dict.put(0x20200030, new String[] {"Requested Image Size", "DS"});
    dict.put(0x20200040, new String[] {"Requested Decimate/CropBehavior", "CS"});
    dict.put(0x20200050, new String[] {"Requested Resolution ID", "CS"});
    dict.put(0x202000A0, new String[] {"Requested Image Size Flag", "CS"});
    dict.put(0x202000A2, new String[] {"Decimate/Crop Result", "CS"});
    dict.put(0x20200110, new String[] {"Basic Grayscale Image Sequence", "SQ"});
    dict.put(0x20200111, new String[] {"Basic Color Image Sequence", "SQ"});
    dict.put(0x20200130, new String[] {"Referenced Image Overlay BoxSequence", "SQ"}); // Retired
    dict.put(0x20200140, new String[] {"Referenced VOI LUT BoxSequence", "SQ"}); // Retired1
  }

  /**
   * Adds attributes of group 0x2030.
   */
  private static void addAttributeGroup2030(Hashtable<Integer, String[]> dict) {
    dict.put(0x20300010, new String[] {"Annotation Position", "US"});
    dict.put(0x20300020, new String[] {"Text String", "LO"});
  }

  /**
   * Adds attributes of group 0x2040.
   */
  private static void addAttributeGroup2040(Hashtable<Integer, String[]> dict) {
    dict.put(0x20400010, new String[] {"Referenced Overlay PlaneSequence", "SQ"}); // Retired
    dict.put(0x20400011, new String[] {"Referenced Overlay Plane Groups", "US"}); // Retired
    dict.put(0x20400020, new String[] {"Overlay Pixel Data Sequence", "SQ"}); // Retired
    dict.put(0x20400060, new String[] {"Overlay Magnification Type", "CS"}); // Retired
    dict.put(0x20400070, new String[] {"Overlay Smoothing Type", "CS"}); // Retired
    // dict.put(0x20400072, new String[] {"Overlay or Image Magnification", "CS"}); //Retired
    dict.put(0x20400074, new String[] {"Magnify to Number of Columns", "US"}); // Retired
    dict.put(0x20400080, new String[] {"Overlay Foreground Density", "CS"}); // Retired
    dict.put(0x20400082, new String[] {"Overlay Background Density", "CS"}); // Retired
    dict.put(0x20400090, new String[] {"Overlay Mode", "CS"}); // Retired
    dict.put(0x20400100, new String[] {"Threshold Density", "CS"}); // Retired
    dict.put(0x20400500, new String[] {"Referenced Image Box Sequence(Retired)", "SQ"}); // Retired
  }

  /**
   * Adds attributes of group 0x2050.
   */
  private static void addAttributeGroup2050(Hashtable<Integer, String[]> dict) {
    dict.put(0x20500010, new String[] {"Presentation LUT Sequence", "SQ"});
    dict.put(0x20500020, new String[] {"Presentation LUT Shape", "CS"});
    dict.put(0x20500500, new String[] {"Referenced Presentation LUTSequence", "SQ"});
  }

  /**
   * Adds attributes of group 0x2100.
   */
  private static void addAttributeGroup2100(Hashtable<Integer, String[]> dict) {
    dict.put(0x21000010, new String[] {"Print Job ID", "SH"}); // Retired
    dict.put(0x21000020, new String[] {"Execution Status", "CS"});
    dict.put(0x21000030, new String[] {"Execution Status Info", "CS"});
    dict.put(0x21000040, new String[] {"Creation Date", "DA"});
    dict.put(0x21000050, new String[] {"Creation Time", "TM"});
    dict.put(0x21000070, new String[] {"Originator", "AE"});
    dict.put(0x21000140, new String[] {"Destination AE", "AE"}); // Retired
    dict.put(0x21000160, new String[] {"Owner ID", "SH"});
    dict.put(0x21000170, new String[] {"Number of Films", "IS"});
    dict.put(0x21000500, new String[] {"Referenced Print Job Sequence(Pull Stored Print)", "SQ"}); // Retired
  }

  /**
   * Adds attributes of group 0x2110.
   */
  private static void addAttributeGroup2110(Hashtable<Integer, String[]> dict) {
    dict.put(0x21100010, new String[] {"Printer Status", "CS"});
    dict.put(0x21100020, new String[] {"Printer Status Info", "CS"});
    dict.put(0x21100030, new String[] {"Printer Name", "LO"});
    dict.put(0x21100099, new String[] {"Print Queue ID", "SH"}); // Retired
  }

  /**
   * Adds attributes of group 0x2120.
   */
  private static void addAttributeGroup2120(Hashtable<Integer, String[]> dict) {
    dict.put(0x21200010, new String[] {"Queue Status", "CS"}); // Retired
    dict.put(0x21200050, new String[] {"Print Job Description Sequence", "SQ"}); // Retired
    dict.put(0x21200070, new String[] {"Referenced Print Job Sequence", "SQ"}); // Retired
  }

  /**
   * Adds attributes of group 0x2130.
   */
  private static void addAttributeGroup2130(Hashtable<Integer, String[]> dict) {
    dict.put(0x21300010, new String[] {"Print Management CapabilitiesSequence", "SQ"}); // Retired
    dict.put(0x21300015, new String[] {"Printer Characteristics Sequence", "SQ"}); // Retired
    dict.put(0x21300030, new String[] {"Film Box Content Sequence", "SQ"}); // Retired
    dict.put(0x21300040, new String[] {"Image Box Content Sequence", "SQ"}); // Retired
    dict.put(0x21300050, new String[] {"Annotation Content Sequence", "SQ"}); // Retired
    dict.put(0x21300060, new String[] {"Image Overlay Box ContentSequence", "SQ"}); // Retired
    dict.put(0x21300080, new String[] {"Presentation LUT ContentSequence", "SQ"}); // Retired
    dict.put(0x213000A0, new String[] {"Proposed Study Sequence", "SQ"}); // Retired
    dict.put(0x213000C0, new String[] {"Original Image Sequence", "SQ"}); // Retired
  }

  /**
   * Adds attributes of group 0x2200.
   */
  private static void addAttributeGroup2200(Hashtable<Integer, String[]> dict) {
    dict.put(0x22000001, new String[] {"Label Using Information ExtractedFrom Instances", "CS"});
    dict.put(0x22000002, new String[] {"Label Text", "UT"});
    dict.put(0x22000003, new String[] {"Label Style Selection", "CS"});
    dict.put(0x22000004, new String[] {"Media Disposition", "LT"});
    dict.put(0x22000005, new String[] {"Barcode Value", "LT"});
    dict.put(0x22000006, new String[] {"Barcode Symbology", "CS"});
    dict.put(0x22000007, new String[] {"Allow Media Splitting", "CS"});
    dict.put(0x22000008, new String[] {"Include Non-DICOM Objects", "CS"});
    dict.put(0x22000009, new String[] {"Include Display Application", "CS"});
    dict.put(0x2200000A, new String[] {"Preserve Composite InstancesAfter Media Creation", "CS"});
    dict.put(0x2200000B, new String[] {"Total Number of Pieces of MediaCreated", "US"});
    dict.put(0x2200000C, new String[] {"Requested Media ApplicationProfile", "LO"});
    dict.put(0x2200000D, new String[] {"Referenced Storage MediaSequence", "SQ"});
    dict.put(0x2200000E,
             new String[] {"Failure Attributes  FailureAttributes AT  1-n(2200,000F)  Allow Lossy Compression",
                           "CS"});
    dict.put(0x22000020, new String[] {"Request Priority", "CS"});
  }

  /**
   * Adds attributes of group 0x3002.
   */
  private static void addAttributeGroup3002(Hashtable<Integer, String[]> dict) {
    dict.put(0x30020002, new String[] {"RT Image Label", "SH"});
    dict.put(0x30020003, new String[] {"RT Image Name", "LO"});
    dict.put(0x30020004, new String[] {"RT Image Description", "ST"});
    dict.put(0x3002000A, new String[] {"Reported Values Origin", "CS"});
    dict.put(0x3002000C, new String[] {"RT Image Plane", "CS"});
    dict.put(0x3002000D, new String[] {"X-Ray Image Receptor Translation", "DS"});
    dict.put(0x3002000E, new String[] {"X-Ray Image Receptor Angle", "DS"});
    dict.put(0x30020010, new String[] {"RT Image Orientation", "DS"});
    dict.put(0x30020011, new String[] {"Image Plane Pixel Spacing", "DS"});
    dict.put(0x30020012, new String[] {"RT Image Position", "DS"});
    dict.put(0x30020020, new String[] {"Radiation Machine Name", "SH"});
    dict.put(0x30020022, new String[] {"Radiation Machine SAD", "DS"});
    dict.put(0x30020024, new String[] {"Radiation Machine SSD", "DS"});
    dict.put(0x30020026, new String[] {"RT Image SID", "DS"});
    dict.put(0x30020028, new String[] {"Source to Reference ObjectDistance", "DS"});
    dict.put(0x30020029, new String[] {"Fraction Number", "IS"});
    dict.put(0x30020030, new String[] {"Exposure Sequence", "SQ"});
    dict.put(0x30020032, new String[] {"Meterset Exposure", "DS"});
    dict.put(0x30020034, new String[] {"Diaphragm Position", "DS"});
    dict.put(0x30020040, new String[] {"Fluence Map Sequence", "SQ"});
    dict.put(0x30020041, new String[] {"Fluence Data Source", "CS"});
    dict.put(0x30020042, new String[] {"Fluence Data Scale", "DS"});
    dict.put(0x30020050, new String[] {"Primary Fluence Mode Sequence", "SQ"});
    dict.put(0x30020051, new String[] {"Fluence Mode", "CS"});
    dict.put(0x30020052, new String[] {"Fluence Mode ID", "SH"});
  }

  /**
   * Adds attributes of group 0x3004.
   */
  private static void addAttributeGroup3004(Hashtable<Integer, String[]> dict) {
    dict.put(0x30040001, new String[] {"DVH Type", "CS"});
    dict.put(0x30040002, new String[] {"Dose Units", "CS"});
    dict.put(0x30040004, new String[] {"Dose Type", "CS"});
    dict.put(0x30040005, new String[] {"Spatial Transform of Dose", "CS"});
    dict.put(0x30040006, new String[] {"Dose Comment", "LO"});
    dict.put(0x30040008, new String[] {"Normalization Point", "DS"});
    dict.put(0x3004000A, new String[] {"Dose Summation Type", "CS"});
    dict.put(0x3004000C, new String[] {"Grid Frame Offset Vector", "DS"});
    dict.put(0x3004000E, new String[] {"Dose Grid Scaling", "DS"});
    dict.put(0x30040010, new String[] {"RT Dose ROI Sequence", "SQ"});
    dict.put(0x30040012, new String[] {"Dose Value", "DS"});
    dict.put(0x30040014, new String[] {"Tissue Heterogeneity Correction", "CS"});
    dict.put(0x30040040, new String[] {"DVH Normalization Point", "DS"});
    dict.put(0x30040042, new String[] {"DVH Normalization Dose Value", "DS"});
    dict.put(0x30040050, new String[] {"DVH Sequence", "SQ"});
    dict.put(0x30040052, new String[] {"DVH Dose Scaling", "DS"});
    dict.put(0x30040054, new String[] {"DVH Volume Units", "CS"});
    dict.put(0x30040056, new String[] {"DVH Number of Bins", "IS"});
    dict.put(0x30040058, new String[] {"DVH Data", "DS"});
    dict.put(0x30040060, new String[] {"DVH Referenced ROI Sequence", "SQ"});
    dict.put(0x30040062, new String[] {"DVH ROI Contribution Type", "CS"});
    dict.put(0x30040070, new String[] {"DVH Minimum Dose", "DS"});
    dict.put(0x30040072, new String[] {"DVH Maximum Dose", "DS"});
    dict.put(0x30040074, new String[] {"DVH Mean Dose", "DS"});
  }

  /**
   * Adds attributes of group 0x3006.
   */
  private static void addAttributeGroup3006(Hashtable<Integer, String[]> dict) {
    dict.put(0x30060002, new String[] {"Structure Set Label", "SH"});
    dict.put(0x30060004, new String[] {"Structure Set Name", "LO"});
    dict.put(0x30060006, new String[] {"Structure Set Description", "ST"});
    dict.put(0x30060008, new String[] {"Structure Set Date", "DA"});
    dict.put(0x30060009, new String[] {"Structure Set Time", "TM"});
    dict.put(0x30060010, new String[] {"Referenced Frame of ReferenceSequence", "SQ"});
    dict.put(0x30060012, new String[] {"RT Referenced Study Sequence", "SQ"});
    dict.put(0x30060014, new String[] {"RT Referenced Series Sequence", "SQ"});
    dict.put(0x30060016, new String[] {"Contour Image Sequence", "SQ"});
    dict.put(0x30060018, new String[] {"Predecessor Structure SetSequence", "SQ"});
    dict.put(0x30060020, new String[] {"Structure Set ROI Sequence", "SQ"});
    dict.put(0x30060022, new String[] {"ROI Number", "IS"});
    dict.put(0x30060024, new String[] {"Referenced Frame of ReferenceUID", "UI"});
    dict.put(0x30060026, new String[] {"ROI Name", "LO"});
    dict.put(0x30060028, new String[] {"ROI Description", "ST"});
    dict.put(0x3006002A, new String[] {"ROI Display Color", "IS"});
    dict.put(0x3006002C, new String[] {"ROI Volume", "DS"});
    dict.put(0x30060030, new String[] {"RT Related ROI Sequence", "SQ"});
    dict.put(0x30060033, new String[] {"RT ROI Relationship", "CS"});
    dict.put(0x30060036, new String[] {"ROI Generation Algorithm", "CS"});
    dict.put(0x30060038, new String[] {"ROI Generation Description", "LO"});
    dict.put(0x30060039, new String[] {"ROI Contour Sequence", "SQ"});
    dict.put(0x30060040, new String[] {"Contour Sequence", "SQ"});
    dict.put(0x30060042, new String[] {"Contour Geometric Type", "CS"});
    dict.put(0x30060044, new String[] {"Contour Slab Thickness", "DS"});
    dict.put(0x30060045, new String[] {"Contour Offset Vector", "DS"});
    dict.put(0x30060046, new String[] {"Number of Contour Points", "IS"});
    dict.put(0x30060048, new String[] {"Contour Number", "IS"});
    dict.put(0x30060049, new String[] {"Attached Contours", "IS"});
    dict.put(0x30060050, new String[] {"Contour Data", "DS"});
    dict.put(0x30060080, new String[] {"RT ROI Observations Sequence", "SQ"});
    dict.put(0x30060082, new String[] {"Observation Number", "IS"});
    dict.put(0x30060084, new String[] {"Referenced ROI Number", "IS"});
    dict.put(0x30060085, new String[] {"ROI Observation Label", "SH"});
    dict.put(0x30060086, new String[] {"RT ROI Identification CodeSequence", "SQ"});
    dict.put(0x30060088, new String[] {"ROI Observation Description", "ST"});
    dict.put(0x300600A0, new String[] {"Related RT ROI ObservationsSequence", "SQ"});
    dict.put(0x300600A4, new String[] {"RT ROI Interpreted Type", "CS"});
    dict.put(0x300600A6, new String[] {"ROI Interpreter", "PN"});
    dict.put(0x300600B0, new String[] {"ROI Physical Properties Sequence", "SQ"});
    dict.put(0x300600B2, new String[] {"ROI Physical Property", "CS"});
    dict.put(0x300600B4, new String[] {"ROI Physical Property Value", "DS"});
    dict.put(0x300600B6, new String[] {"ROI Elemental CompositionSequence", "SQ"});
    dict.put(0x300600B7, new String[] {"ROI Elemental Composition AtomicNumber", "US"});
    dict.put(0x300600B8, new String[] {"ROI Elemental Composition AtomicMass Fraction", "FL"});
    dict.put(0x300600B9, new String[] {"Additional RT ROI IdentificationCode Sequence", "SQ"});
    dict.put(0x300600C0, new String[] {"Frame of Reference RelationshipSequence", "SQ"}); // Retired
    dict.put(0x300600C2, new String[] {"Related Frame of Reference UID", "UI"}); // Retired
    dict.put(0x300600C4, new String[] {"Frame of ReferenceTransformation Type", "CS"}); // Retired
    dict.put(0x300600C6, new String[] {"Frame of ReferenceTransformation Matrix", "DS"});
    dict.put(0x300600C8, new String[] {"Frame of ReferenceTransformation Comment", "LO"});
  }

  /**
   * Adds attributes of group 0x3008.
   */
  private static void addAttributeGroup3008(Hashtable<Integer, String[]> dict) {
    dict.put(0x30080010, new String[] {"Measured Dose ReferenceSequence", "SQ"});
    dict.put(0x30080012, new String[] {"Measured Dose Description", "ST"});
    dict.put(0x30080014, new String[] {"Measured Dose Type", "CS"});
    dict.put(0x30080016, new String[] {"Measured Dose Value", "DS"});
    dict.put(0x30080020, new String[] {"Treatment Session BeamSequence", "SQ"});
    dict.put(0x30080021, new String[] {"Treatment Session Ion BeamSequence", "SQ"});
    dict.put(0x30080022, new String[] {"Current Fraction Number", "IS"});
    dict.put(0x30080024, new String[] {"Treatment Control Point Date", "DA"});
    dict.put(0x30080025, new String[] {"Treatment Control Point Time", "TM"});
    dict.put(0x3008002A, new String[] {"Treatment Termination Status", "CS"});
    dict.put(0x3008002B, new String[] {"Treatment Termination Code", "SH"});
    dict.put(0x3008002C, new String[] {"Treatment Verification Status", "CS"});
    dict.put(0x30080030, new String[] {"Referenced Treatment RecordSequence", "SQ"});
    dict.put(0x30080032, new String[] {"Specified Primary Meterset", "DS"});
    dict.put(0x30080033, new String[] {"Specified Secondary Meterset", "DS"});
    dict.put(0x30080036, new String[] {"Delivered Primary Meterset", "DS"});
    dict.put(0x30080037, new String[] {"Delivered Secondary Meterset", "DS"});
    dict.put(0x3008003A, new String[] {"Specified Treatment Time", "DS"});
    dict.put(0x3008003B, new String[] {"Delivered Treatment Time", "DS"});
    dict.put(0x30080040, new String[] {"Control Point Delivery Sequence", "SQ"});
    dict.put(0x30080041, new String[] {"Ion Control Point DeliverySequence", "SQ"});
    dict.put(0x30080042, new String[] {"Specified Meterset", "DS"});
    dict.put(0x30080044, new String[] {"Delivered Meterset", "DS"});
    dict.put(0x30080045, new String[] {"Meterset Rate Set", "FL"});
    dict.put(0x30080046, new String[] {"Meterset Rate Delivered", "FL"});
    dict.put(0x30080047, new String[] {"Scan Spot Metersets Delivered", "FL"});
    dict.put(0x30080048, new String[] {"Dose Rate Delivered", "DS"});
    dict.put(0x30080050,
             new String[] {"Treatment Summary CalculatedDose Reference Sequence", "SQ"});
    dict.put(0x30080052, new String[] {"Cumulative Dose to DoseReference", "DS"});
    dict.put(0x30080054, new String[] {"First Treatment Date", "DA"});
    dict.put(0x30080056, new String[] {"Most Recent Treatment Date", "DA"});
    dict.put(0x3008005A, new String[] {"Number of Fractions Delivered", "IS"});
    dict.put(0x30080060, new String[] {"Override Sequence", "SQ"});
    dict.put(0x30080061, new String[] {"Parameter Sequence Pointer", "AT"});
    dict.put(0x30080062, new String[] {"Override Parameter Pointer", "AT"});
    dict.put(0x30080063, new String[] {"Parameter Item Index", "IS"});
    dict.put(0x30080064, new String[] {"Measured Dose Reference Number", "IS"});
    dict.put(0x30080065, new String[] {"Parameter Pointer", "AT"});
    dict.put(0x30080066, new String[] {"Override Reason", "ST"});
    dict.put(0x30080068, new String[] {"Corrected Parameter Sequence", "SQ"});
    dict.put(0x3008006A, new String[] {"Correction Value", "FL"});
    dict.put(0x30080070, new String[] {"Calculated Dose ReferenceSequence", "SQ"});
    dict.put(0x30080072, new String[] {"Calculated Dose ReferenceNumber", "IS"});
    dict.put(0x30080074, new String[] {"Calculated Dose ReferenceDescription", "ST"});
    dict.put(0x30080076, new String[] {"Calculated Dose Reference DoseValue", "DS"});
    dict.put(0x30080078, new String[] {"Start Meterset", "DS"});
    dict.put(0x3008007A, new String[] {"End Meterset", "DS"});
    dict.put(0x30080080, new String[] {"Referenced Measured DoseReference Sequence", "SQ"});
    dict.put(0x30080082, new String[] {"Referenced Measured DoseReference Number", "IS"});
    dict.put(0x30080090, new String[] {"Referenced Calculated DoseReference Sequence", "SQ"});
    dict.put(0x30080092, new String[] {"Referenced Calculated DoseReference Number", "IS"});
    dict.put(0x300800A0, new String[] {"Beam Limiting Device Leaf PairsSequence", "SQ"});
    dict.put(0x300800B0, new String[] {"Recorded Wedge Sequence", "SQ"});
    dict.put(0x300800C0, new String[] {"Recorded Compensator Sequence", "SQ"});
    dict.put(0x300800D0, new String[] {"Recorded Block Sequence", "SQ"});
    dict.put(0x300800E0, new String[] {"Treatment Summary MeasuredDose Reference Sequence", "SQ"});
    dict.put(0x300800F0, new String[] {"Recorded Snout Sequence", "SQ"});
    dict.put(0x300800F2, new String[] {"Recorded Range Shifter Sequence", "SQ"});
    dict.put(0x300800F4, new String[] {"Recorded Lateral SpreadingDevice Sequence", "SQ"});
    dict.put(0x300800F6, new String[] {"Recorded Range ModulatorSequence", "SQ"});
    dict.put(0x30080100, new String[] {"Recorded Source Sequence", "SQ"});
    dict.put(0x30080105, new String[] {"Source Serial Number", "LO"});
    dict.put(0x30080110, new String[] {"Treatment Session ApplicationSetup Sequence", "SQ"});
    dict.put(0x30080116, new String[] {"Application Setup Check", "CS"});
    dict.put(0x30080120, new String[] {"Recorded Brachy AccessoryDevice Sequence", "SQ"});
    dict.put(0x30080122, new String[] {"Referenced Brachy AccessoryDevice Number", "IS"});
    dict.put(0x30080130, new String[] {"Recorded Channel Sequence", "SQ"});
    dict.put(0x30080132, new String[] {"Specified Channel Total Time", "DS"});
    dict.put(0x30080134, new String[] {"Delivered Channel Total Time", "DS"});
    dict.put(0x30080136, new String[] {"Specified Number of Pulses", "IS"});
    dict.put(0x30080138, new String[] {"Delivered Number of Pulses", "IS"});
    dict.put(0x3008013A, new String[] {"Specified Pulse Repetition Interval", "DS"});
    dict.put(0x3008013C, new String[] {"Delivered Pulse Repetition Interval", "DS"});
    dict.put(0x30080140, new String[] {"Recorded Source ApplicatorSequence", "SQ"});
    dict.put(0x30080142, new String[] {"Referenced Source ApplicatorNumber", "IS"});
    dict.put(0x30080150, new String[] {"Recorded Channel ShieldSequence", "SQ"});
    dict.put(0x30080152, new String[] {"Referenced Channel ShieldNumber", "IS"});
    dict.put(0x30080160, new String[] {"Brachy Control Point DeliveredSequence", "SQ"});
    dict.put(0x30080162, new String[] {"Safe Position Exit Date", "DA"});
    dict.put(0x30080164, new String[] {"Safe Position Exit Time", "TM"});
    dict.put(0x30080166, new String[] {"Safe Position Return Date", "DA"});
    dict.put(0x30080168, new String[] {"Safe Position Return Time", "TM"});
    dict.put(0x30080171,
             new String[] {"Pulse Specific Brachy Control PointDelivered Sequence", "SQ"});
    dict.put(0x30080172, new String[] {"Pulse Number", "US"});
    dict.put(0x30080173, new String[] {"Brachy Pulse Control PointDelivered Sequence", "SQ"});
    dict.put(0x30080200, new String[] {"Current Treatment Status", "CS"});
    dict.put(0x30080202, new String[] {"Treatment Status Comment", "ST"});
    dict.put(0x30080220, new String[] {"Fraction Group SummarySequence", "SQ"});
    dict.put(0x30080223, new String[] {"Referenced Fraction Number", "IS"});
    dict.put(0x30080224, new String[] {"Fraction Group Type", "CS"});
    dict.put(0x30080230, new String[] {"Beam Stopper Position", "CS"});
    dict.put(0x30080240, new String[] {"Fraction Status SummarySequence", "SQ"});
    dict.put(0x30080250, new String[] {"Treatment Date", "DA"});
    dict.put(0x30080251, new String[] {"Treatment Time", "TM"});
  }

  /**
   * Adds attributes of group 0x300A.
   */
  private static void addAttributeGroup300A(Hashtable<Integer, String[]> dict) {
    dict.put(0x300A0002, new String[] {"RT Plan Label", "SH"});
    dict.put(0x300A0003, new String[] {"RT Plan Name", "LO"});
    dict.put(0x300A0004, new String[] {"RT Plan Description", "ST"});
    dict.put(0x300A0006, new String[] {"RT Plan Date", "DA"});
    dict.put(0x300A0007, new String[] {"RT Plan Time", "TM"});
    dict.put(0x300A0009, new String[] {"Treatment Protocols", "LO"});
    dict.put(0x300A000A, new String[] {"Plan Intent", "CS"});
    dict.put(0x300A000B, new String[] {"Treatment Sites", "LO"});
    dict.put(0x300A000C, new String[] {"RT Plan Geometry", "CS"});
    dict.put(0x300A000E, new String[] {"Prescription Description", "ST"});
    dict.put(0x300A0010, new String[] {"Dose Reference Sequence", "SQ"});
    dict.put(0x300A0012, new String[] {"Dose Reference Number", "IS"});
    dict.put(0x300A0013, new String[] {"Dose Reference UID", "UI"});
    dict.put(0x300A0014, new String[] {"Dose Reference Structure Type", "CS"});
    dict.put(0x300A0015, new String[] {"Nominal Beam Energy Unit", "CS"});
    dict.put(0x300A0016, new String[] {"Dose Reference Description", "LO"});
    dict.put(0x300A0018, new String[] {"Dose Reference Point Coordinates", "DS"});
    dict.put(0x300A001A, new String[] {"Nominal Prior Dose", "DS"});
    dict.put(0x300A0020, new String[] {"Dose Reference Type", "CS"});
    dict.put(0x300A0021, new String[] {"Constraint Weight", "DS"});
    dict.put(0x300A0022, new String[] {"Delivery Warning Dose", "DS"});
    dict.put(0x300A0023, new String[] {"Delivery Maximum Dose", "DS"});
    dict.put(0x300A0025, new String[] {"Target Minimum Dose", "DS"});
    dict.put(0x300A0026, new String[] {"Target Prescription Dose", "DS"});
    dict.put(0x300A0027, new String[] {"Target Maximum Dose", "DS"});
    dict.put(0x300A0028, new String[] {"Target Underdose Volume Fraction", "DS"});
    dict.put(0x300A002A, new String[] {"Organ at Risk Full-volume Dose", "DS"});
    dict.put(0x300A002B, new String[] {"Organ at Risk Limit Dose", "DS"});
    dict.put(0x300A002C, new String[] {"Organ at Risk Maximum Dose", "DS"});
    dict.put(0x300A002D, new String[] {"Organ at Risk Overdose VolumeFraction", "DS"});
    dict.put(0x300A0040, new String[] {"Tolerance Table Sequence", "SQ"});
    dict.put(0x300A0042, new String[] {"Tolerance Table Number", "IS"});
    dict.put(0x300A0043, new String[] {"Tolerance Table Label", "SH"});
    dict.put(0x300A0044, new String[] {"Gantry Angle Tolerance", "DS"});
    dict.put(0x300A0046, new String[] {"Beam Limiting Device AngleTolerance", "DS"});
    dict.put(0x300A0048, new String[] {"Beam Limiting Device ToleranceSequence", "SQ"});
    dict.put(0x300A004A, new String[] {"Beam Limiting Device PositionTolerance", "DS"});
    dict.put(0x300A004B, new String[] {"Snout Position Tolerance", "FL"});
    dict.put(0x300A004C, new String[] {"Patient Support Angle Tolerance", "DS"});
    dict.put(0x300A004E, new String[] {"Table Top Eccentric AngleTolerance", "DS"});
    dict.put(0x300A004F, new String[] {"Table Top Pitch Angle Tolerance", "FL"});
    dict.put(0x300A0050, new String[] {"Table Top Roll Angle Tolerance", "FL"});
    dict.put(0x300A0051, new String[] {"Table Top Vertical PositionTolerance", "DS"});
    dict.put(0x300A0052, new String[] {"Table Top Longitudinal PositionTolerance", "DS"});
    dict.put(0x300A0053, new String[] {"Table Top Lateral PositionTolerance", "DS"});
    dict.put(0x300A0055, new String[] {"RT Plan Relationship", "CS"});
    dict.put(0x300A0070, new String[] {"Fraction Group Sequence", "SQ"});
    dict.put(0x300A0071, new String[] {"Fraction Group Number", "IS"});
    dict.put(0x300A0072, new String[] {"Fraction Group Description", "LO"});
    dict.put(0x300A0078, new String[] {"Number of Fractions Planned", "IS"});
    dict.put(0x300A0079, new String[] {"Number of Fraction Pattern DigitsPer Day", "IS"});
    dict.put(0x300A007A, new String[] {"Repeat Fraction Cycle Length", "IS"});
    dict.put(0x300A007B, new String[] {"Fraction Pattern", "LT"});
    dict.put(0x300A0080, new String[] {"Number of Beams", "IS"});
    dict.put(0x300A0082, new String[] {"Beam Dose Specification Point", "DS"});
    dict.put(0x300A0084, new String[] {"Beam Dose", "DS"});
    dict.put(0x300A0086, new String[] {"Beam Meterset", "DS"});
    dict.put(0x300A0088, new String[] {"Beam Dose Point Depth", "FL"}); // Retired
    dict.put(0x300A0089, new String[] {"Beam Dose Point Equivalent Depth", "FL"}); // Retired
    dict.put(0x300A008A, new String[] {"Beam Dose Point SSD", "FL"}); // Retired
    dict.put(0x300A008B, new String[] {"Beam Dose Meaning", "CS"});
    dict.put(0x300A008C, new String[] {"Beam Dose Verification ControlPoint Sequence", "SQ"});
    dict.put(0x300A008D, new String[] {"Average Beam Dose Point Depth", "FL"});
    dict.put(0x300A008E, new String[] {"Average Beam Dose PointEquivalent Depth", "FL"});
    dict.put(0x300A008F, new String[] {"Average Beam Dose Point SSD", "FL"});
    dict.put(0x300A00A0, new String[] {"Number of Brachy ApplicationSetups", "IS"});
    dict.put(0x300A00A2, new String[] {"Brachy Application Setup DoseSpecification Point", "DS"});
    dict.put(0x300A00A4, new String[] {"Brachy Application Setup Dose", "DS"});
    dict.put(0x300A00B0, new String[] {"Beam Sequence", "SQ"});
    dict.put(0x300A00B2, new String[] {"Treatment Machine Name", "SH"});
    dict.put(0x300A00B3, new String[] {"Primary Dosimeter Unit", "CS"});
    dict.put(0x300A00B4, new String[] {"Source-Axis Distance", "DS"});
    dict.put(0x300A00B6, new String[] {"Beam Limiting Device Sequence", "SQ"});
    dict.put(0x300A00B8, new String[] {"RT Beam Limiting Device Type", "CS"});
    dict.put(0x300A00BA, new String[] {"Source to Beam Limiting DeviceDistance", "DS"});
    dict.put(0x300A00BB, new String[] {"Isocenter to Beam Limiting DeviceDistance", "FL"});
    dict.put(0x300A00BC, new String[] {"Number of Leaf/Jaw Pairs", "IS"});
    dict.put(0x300A00BE, new String[] {"Leaf Position Boundaries", "DS"});
    dict.put(0x300A00C0, new String[] {"Beam Number", "IS"});
    dict.put(0x300A00C2, new String[] {"Beam Name", "LO"});
    dict.put(0x300A00C3, new String[] {"Beam Description", "ST"});
    dict.put(0x300A00C4, new String[] {"Beam Type", "CS"});
    dict.put(0x300A00C5, new String[] {"Beam Delivery Duration Limit", "FD"});
    dict.put(0x300A00C6, new String[] {"Radiation Type", "CS"});
    dict.put(0x300A00C7, new String[] {"High-Dose Technique Type", "CS"});
    dict.put(0x300A00C8, new String[] {"Reference Image Number", "IS"});
    dict.put(0x300A00CA, new String[] {"Planned Verification ImageSequence", "SQ"});
    dict.put(0x300A00CC, new String[] {"Imaging Device-SpecificAcquisition Parameters", "LO"});
    dict.put(0x300A00CE, new String[] {"Treatment Delivery Type", "CS"});
    dict.put(0x300A00D0, new String[] {"Number of Wedges", "IS"});
    dict.put(0x300A00D1, new String[] {"Wedge Sequence", "SQ"});
    dict.put(0x300A00D2, new String[] {"Wedge Number", "IS"});
    dict.put(0x300A00D3, new String[] {"Wedge Type", "CS"});
    dict.put(0x300A00D4, new String[] {"Wedge ID", "SH"});
    dict.put(0x300A00D5, new String[] {"Wedge Angle", "IS"});
    dict.put(0x300A00D6, new String[] {"Wedge Factor", "DS"});
    dict.put(0x300A00D7, new String[] {"Total Wedge TrayWater-Equivalent Thickness", "FL"});
    dict.put(0x300A00D8, new String[] {"Wedge Orientation", "DS"});
    dict.put(0x300A00D9, new String[] {"Isocenter to Wedge Tray Distance", "FL"});
    dict.put(0x300A00DA, new String[] {"Source to Wedge Tray Distance", "DS"});
    dict.put(0x300A00DB, new String[] {"Wedge Thin Edge Position", "FL"});
    dict.put(0x300A00DC, new String[] {"Bolus ID", "SH"});
    dict.put(0x300A00DD, new String[] {"Bolus Description", "ST"});
    dict.put(0x300A00DE, new String[] {"Effective Wedge Angle", "DS"});
    dict.put(0x300A00E0, new String[] {"Number of Compensators", "IS"});
    dict.put(0x300A00E1, new String[] {"Material ID", "SH"});
    dict.put(0x300A00E2, new String[] {"Total Compensator Tray Factor", "DS"});
    dict.put(0x300A00E3, new String[] {"Compensator Sequence", "SQ"});
    dict.put(0x300A00E4, new String[] {"Compensator Number", "IS"});
    dict.put(0x300A00E5, new String[] {"Compensator ID", "SH"});
    dict.put(0x300A00E6, new String[] {"Source to Compensator TrayDistance", "DS"});
    dict.put(0x300A00E7, new String[] {"Compensator Rows", "IS"});
    dict.put(0x300A00E8, new String[] {"Compensator Columns", "IS"});
    dict.put(0x300A00E9, new String[] {"Compensator Pixel Spacing", "DS"});
    dict.put(0x300A00EA, new String[] {"Compensator Position", "DS"});
    dict.put(0x300A00EB, new String[] {"Compensator Transmission Data", "DS"});
    dict.put(0x300A00EC, new String[] {"Compensator Thickness Data", "DS"});
    dict.put(0x300A00ED, new String[] {"Number of Boli", "IS"});
    dict.put(0x300A00EE, new String[] {"Compensator Type", "CS"});
    dict.put(0x300A00EF, new String[] {"Compensator Tray ID", "SH"});
    dict.put(0x300A00F0, new String[] {"Number of Blocks", "IS"});
    dict.put(0x300A00F2, new String[] {"Total Block Tray Factor", "DS"});
    dict.put(0x300A00F3, new String[] {"Total Block Tray Water-EquivalentThickness", "FL"});
    dict.put(0x300A00F4, new String[] {"Block Sequence", "SQ"});
    dict.put(0x300A00F5, new String[] {"Block Tray ID", "SH"});
    dict.put(0x300A00F6, new String[] {"Source to Block Tray Distance", "DS"});
    dict.put(0x300A00F7, new String[] {"Isocenter to Block Tray Distance", "FL"});
    dict.put(0x300A00F8, new String[] {"Block Type", "CS"});
    dict.put(0x300A00F9, new String[] {"Accessory Code", "LO"});
    dict.put(0x300A00FA, new String[] {"Block Divergence", "CS"});
    dict.put(0x300A00FB, new String[] {"Block Mounting Position", "CS"});
    dict.put(0x300A00FC, new String[] {"Block Number", "IS"});
    dict.put(0x300A00FE, new String[] {"Block Name", "LO"});
    dict.put(0x300A0100, new String[] {"Block Thickness", "DS"});
    dict.put(0x300A0102, new String[] {"Block Transmission", "DS"});
    dict.put(0x300A0104, new String[] {"Block Number of Points", "IS"});
    dict.put(0x300A0106, new String[] {"Block Data", "DS"});
    dict.put(0x300A0107, new String[] {"Applicator Sequence", "SQ"});
    dict.put(0x300A0108, new String[] {"Applicator ID", "SH"});
    dict.put(0x300A0109, new String[] {"Applicator Type", "CS"});
    dict.put(0x300A010A, new String[] {"Applicator Description", "LO"});
    dict.put(0x300A010C, new String[] {"Cumulative Dose ReferenceCoefficient", "DS"});
    dict.put(0x300A010E, new String[] {"Final Cumulative Meterset Weight", "DS"});
    dict.put(0x300A0110, new String[] {"Number of Control Points", "IS"});
    dict.put(0x300A0111, new String[] {"Control Point Sequence", "SQ"});
    dict.put(0x300A0112, new String[] {"Control Point Index", "IS"});
    dict.put(0x300A0114, new String[] {"Nominal Beam Energy", "DS"});
    dict.put(0x300A0115, new String[] {"Dose Rate Set", "DS"});
    dict.put(0x300A0116, new String[] {"Wedge Position Sequence", "SQ"});
    dict.put(0x300A0118, new String[] {"Wedge Position", "CS"});
    dict.put(0x300A011A, new String[] {"Beam Limiting Device PositionSequence", "SQ"});
    dict.put(0x300A011C, new String[] {"Leaf/Jaw Positions", "DS"});
    dict.put(0x300A011E, new String[] {"Gantry Angle", "DS"});
    dict.put(0x300A011F, new String[] {"Gantry Rotation Direction", "CS"});
    dict.put(0x300A0120, new String[] {"Beam Limiting Device Angle", "DS"});
    dict.put(0x300A0121, new String[] {"Beam Limiting Device RotationDirection", "CS"});
    dict.put(0x300A0122, new String[] {"Patient Support Angle", "DS"});
    dict.put(0x300A0123, new String[] {"Patient Support Rotation Direction", "CS"});
    dict.put(0x300A0124, new String[] {"Table Top Eccentric Axis Distance", "DS"});
    dict.put(0x300A0125, new String[] {"Table Top Eccentric Angle", "DS"});
    dict.put(0x300A0126, new String[] {"Table Top Eccentric RotationDirection", "CS"});
    dict.put(0x300A0128, new String[] {"Table Top Vertical Position", "DS"});
    dict.put(0x300A0129, new String[] {"Table Top Longitudinal Position", "DS"});
    dict.put(0x300A012A, new String[] {"Table Top Lateral Position", "DS"});
    dict.put(0x300A012C, new String[] {"Isocenter Position", "DS"});
    dict.put(0x300A012E, new String[] {"Surface Entry Point", "DS"});
    dict.put(0x300A0130, new String[] {"Source to Surface Distance", "DS"});
    dict.put(0x300A0131,
             new String[] {"Average Beam Dose Point Sourceto External Contour SurfaceDistance",
                           "FL"});
    dict.put(0x300A0132, new String[] {"Source to External ContourDistance", "FL"});
    dict.put(0x300A0133, new String[] {"External Contour Entry Point", "FL"});
    dict.put(0x300A0134, new String[] {"Cumulative Meterset Weight", "DS"});
    dict.put(0x300A0140, new String[] {"Table Top Pitch Angle", "FL"});
    dict.put(0x300A0142, new String[] {"Table Top Pitch Rotation Direction", "CS"});
    dict.put(0x300A0144, new String[] {"Table Top Roll Angle", "FL"});
    dict.put(0x300A0146, new String[] {"Table Top Roll Rotation Direction", "CS"});
    dict.put(0x300A0148, new String[] {"Head Fixation Angle", "FL"});
    dict.put(0x300A014A, new String[] {"Gantry Pitch Angle", "FL"});
    dict.put(0x300A014C, new String[] {"Gantry Pitch Rotation Direction", "CS"});
    dict.put(0x300A014E, new String[] {"Gantry Pitch Angle Tolerance", "FL"});
    dict.put(0x300A0180, new String[] {"Patient Setup Sequence", "SQ"});
    dict.put(0x300A0182, new String[] {"Patient Setup Number", "IS"});
    dict.put(0x300A0183, new String[] {"Patient Setup Label", "LO"});
    dict.put(0x300A0184, new String[] {"Patient Additional Position", "LO"});
    dict.put(0x300A0190, new String[] {"Fixation Device Sequence", "SQ"});
    dict.put(0x300A0192, new String[] {"Fixation Device Type", "CS"});
    dict.put(0x300A0194, new String[] {"Fixation Device Label", "SH"});
    dict.put(0x300A0196, new String[] {"Fixation Device Description", "ST"});
    dict.put(0x300A0198, new String[] {"Fixation Device Position", "SH"});
    dict.put(0x300A0199, new String[] {"Fixation Device Pitch Angle", "FL"});
    dict.put(0x300A019A, new String[] {"Fixation Device Roll Angle", "FL"});
    dict.put(0x300A01A0, new String[] {"Shielding Device Sequence", "SQ"});
    dict.put(0x300A01A2, new String[] {"Shielding Device Type", "CS"});
    dict.put(0x300A01A4, new String[] {"Shielding Device Label", "SH"});
    dict.put(0x300A01A6, new String[] {"Shielding Device Description", "ST"});
    dict.put(0x300A01A8, new String[] {"Shielding Device Position", "SH"});
    dict.put(0x300A01B0, new String[] {"Setup Technique", "CS"});
    dict.put(0x300A01B2, new String[] {"Setup Technique Description", "ST"});
    dict.put(0x300A01B4, new String[] {"Setup Device Sequence", "SQ"});
    dict.put(0x300A01B6, new String[] {"Setup Device Type", "CS"});
    dict.put(0x300A01B8, new String[] {"Setup Device Label", "SH"});
    dict.put(0x300A01BA, new String[] {"Setup Device Description", "ST"});
    dict.put(0x300A01BC, new String[] {"Setup Device Parameter", "DS"});
    dict.put(0x300A01D0, new String[] {"Setup Reference Description", "ST"});
    dict.put(0x300A01D2, new String[] {"Table Top Vertical SetupDisplacement", "DS"});
    dict.put(0x300A01D4, new String[] {"Table Top Longitudinal SetupDisplacement", "DS"});
    dict.put(0x300A01D6, new String[] {"Table Top Lateral SetupDisplacement", "DS"});
    dict.put(0x300A0200, new String[] {"Brachy Treatment Technique", "CS"});
    dict.put(0x300A0202, new String[] {"Brachy Treatment Type", "CS"});
    dict.put(0x300A0206, new String[] {"Treatment Machine Sequence", "SQ"});
    dict.put(0x300A0210, new String[] {"Source Sequence", "SQ"});
    dict.put(0x300A0212, new String[] {"Source Number", "IS"});
    dict.put(0x300A0214, new String[] {"Source Type", "CS"});
    dict.put(0x300A0216, new String[] {"Source Manufacturer", "LO"});
    dict.put(0x300A0218, new String[] {"Active Source Diameter", "DS"});
    dict.put(0x300A021A, new String[] {"Active Source Length", "DS"});
    dict.put(0x300A021B, new String[] {"Source Model ID", "SH"});
    dict.put(0x300A021C, new String[] {"Source Description", "LO"});
    dict.put(0x300A0222, new String[] {"Source Encapsulation NominalThickness", "DS"});
    dict.put(0x300A0224, new String[] {"Source Encapsulation NominalTransmission", "DS"});
    dict.put(0x300A0226, new String[] {"Source Isotope Name", "LO"});
    dict.put(0x300A0228, new String[] {"Source Isotope Half Life", "DS"});
    dict.put(0x300A0229, new String[] {"Source Strength Units", "CS"});
    dict.put(0x300A022A, new String[] {"Reference Air Kerma Rate", "DS"});
    dict.put(0x300A022B, new String[] {"Source Strength", "DS"});
    dict.put(0x300A022C, new String[] {"Source Strength Reference Date", "DA"});
    dict.put(0x300A022E, new String[] {"Source Strength Reference Time", "TM"});
    dict.put(0x300A0230, new String[] {"Application Setup Sequence", "SQ"});
    dict.put(0x300A0232, new String[] {"Application Setup Type", "CS"});
    dict.put(0x300A0234, new String[] {"Application Setup Number", "IS"});
    dict.put(0x300A0236, new String[] {"Application Setup Name", "LO"});
    dict.put(0x300A0238, new String[] {"Application Setup Manufacturer", "LO"});
    dict.put(0x300A0240, new String[] {"Template Number", "IS"});
    dict.put(0x300A0242, new String[] {"Template Type", "SH"});
    dict.put(0x300A0244, new String[] {"Template Name", "LO"});
    dict.put(0x300A0250, new String[] {"Total Reference Air Kerma", "DS"});
    dict.put(0x300A0260, new String[] {"Brachy Accessory DeviceSequence", "SQ"});
    dict.put(0x300A0262, new String[] {"Brachy Accessory Device Number", "IS"});
    dict.put(0x300A0263, new String[] {"Brachy Accessory Device ID", "SH"});
    dict.put(0x300A0264, new String[] {"Brachy Accessory Device Type", "CS"});
    dict.put(0x300A0266, new String[] {"Brachy Accessory Device Name", "LO"});
    dict.put(0x300A026A, new String[] {"Brachy Accessory Device NominalThickness", "DS"});
    dict.put(0x300A026C, new String[] {"Brachy Accessory Device NominalTransmission", "DS"});
    dict.put(0x300A0280, new String[] {"Channel Sequence", "SQ"});
    dict.put(0x300A0282, new String[] {"Channel Number", "IS"});
    dict.put(0x300A0284, new String[] {"Channel Length", "DS"});
    dict.put(0x300A0286, new String[] {"Channel Total Time", "DS"});
    dict.put(0x300A0288, new String[] {"Source Movement Type", "CS"});
    dict.put(0x300A028A, new String[] {"Number of Pulses", "IS"});
    dict.put(0x300A028C, new String[] {"Pulse Repetition Interval", "DS"});
    dict.put(0x300A0290, new String[] {"Source Applicator Number", "IS"});
    dict.put(0x300A0291, new String[] {"Source Applicator ID", "SH"});
    dict.put(0x300A0292, new String[] {"Source Applicator Type", "CS"});
    dict.put(0x300A0294, new String[] {"Source Applicator Name", "LO"});
    dict.put(0x300A0296, new String[] {"Source Applicator Length", "DS"});
    dict.put(0x300A0298, new String[] {"Source Applicator Manufacturer", "LO"});
    dict.put(0x300A029C, new String[] {"Source Applicator Wall NominalThickness", "DS"});
    dict.put(0x300A029E, new String[] {"Source Applicator Wall NominalTransmission", "DS"});
    dict.put(0x300A02A0, new String[] {"Source Applicator Step Size", "DS"});
    dict.put(0x300A02A2, new String[] {"Transfer Tube Number", "IS"});
    dict.put(0x300A02A4, new String[] {"Transfer Tube Length", "DS"});
    dict.put(0x300A02B0, new String[] {"Channel Shield Sequence", "SQ"});
    dict.put(0x300A02B2, new String[] {"Channel Shield Number", "IS"});
    dict.put(0x300A02B3, new String[] {"Channel Shield ID", "SH"});
    dict.put(0x300A02B4, new String[] {"Channel Shield Name", "LO"});
    dict.put(0x300A02B8, new String[] {"Channel Shield Nominal Thickness", "DS"});
    dict.put(0x300A02BA, new String[] {"Channel Shield NominalTransmission", "DS"});
    dict.put(0x300A02C8, new String[] {"Final Cumulative Time Weight", "DS"});
    dict.put(0x300A02D0, new String[] {"Brachy Control Point Sequence", "SQ"});
    dict.put(0x300A02D2, new String[] {"Control Point Relative Position", "DS"});
    dict.put(0x300A02D4, new String[] {"Control Point 3D Position", "DS"});
    dict.put(0x300A02D6, new String[] {"Cumulative Time Weight", "DS"});
    dict.put(0x300A02E0, new String[] {"Compensator Divergence", "CS"});
    dict.put(0x300A02E1, new String[] {"Compensator Mounting Position", "CS"});
    dict.put(0x300A02E2, new String[] {"Source to Compensator Distance", "DS"});
    dict.put(0x300A02E3, new String[] {"Total Compensator TrayWater-Equivalent Thickness", "FL"});
    dict.put(0x300A02E4, new String[] {"Isocenter to Compensator TrayDistance", "FL"});
    dict.put(0x300A02E5, new String[] {"Compensator Column Offset", "FL"});
    dict.put(0x300A02E6, new String[] {"Isocenter to CompensatorDistances", "FL"});
    dict.put(0x300A02E7, new String[] {"Compensator Relative StoppingPower Ratio", "FL"});
    dict.put(0x300A02E8, new String[] {"Compensator Milling Tool Diameter", "FL"});
    dict.put(0x300A02EA, new String[] {"Ion Range Compensator Sequence", "SQ"});
    dict.put(0x300A02EB, new String[] {"Compensator Description", "LT"});
    dict.put(0x300A0302, new String[] {"Radiation Mass Number", "IS"});
    dict.put(0x300A0304, new String[] {"Radiation Atomic Number", "IS"});
    dict.put(0x300A0306, new String[] {"Radiation Charge State", "SS"});
    dict.put(0x300A0308, new String[] {"Scan Mode", "CS"});
    dict.put(0x300A030A, new String[] {"Virtual Source-Axis Distances", "FL"});
    dict.put(0x300A030C, new String[] {"Snout Sequence", "SQ"});
    dict.put(0x300A030D, new String[] {"Snout Position", "FL"});
    dict.put(0x300A030F, new String[] {"Snout ID", "SH"});
    dict.put(0x300A0312, new String[] {"Number of Range Shifters", "IS"});
    dict.put(0x300A0314, new String[] {"Range Shifter Sequence", "SQ"});
    dict.put(0x300A0316, new String[] {"Range Shifter Number", "IS"});
    dict.put(0x300A0318, new String[] {"Range Shifter ID", "SH"});
    dict.put(0x300A0320, new String[] {"Range Shifter Type", "CS"});
    dict.put(0x300A0322, new String[] {"Range Shifter Description", "LO"});
    dict.put(0x300A0330, new String[] {"Number of Lateral SpreadingDevices", "IS"});
    dict.put(0x300A0332, new String[] {"Lateral Spreading DeviceSequence", "SQ"});
    dict.put(0x300A0334, new String[] {"Lateral Spreading Device Number", "IS"});
    dict.put(0x300A0336, new String[] {"Lateral Spreading Device ID", "SH"});
    dict.put(0x300A0338, new String[] {"Lateral Spreading Device Type", "CS"});
    dict.put(0x300A033A, new String[] {"Lateral Spreading DeviceDescription", "LO"});
    dict.put(0x300A033C,
             new String[] {"Lateral Spreading Device WaterEquivalent Thickness", "FL"});
    dict.put(0x300A0340, new String[] {"Number of Range Modulators", "IS"});
    dict.put(0x300A0342, new String[] {"Range Modulator Sequence", "SQ"});
    dict.put(0x300A0344, new String[] {"Range Modulator Number", "IS"});
    dict.put(0x300A0346, new String[] {"Range Modulator ID", "SH"});
    dict.put(0x300A0348, new String[] {"Range Modulator Type", "CS"});
    dict.put(0x300A034A, new String[] {"Range Modulator Description", "LO"});
    dict.put(0x300A034C, new String[] {"Beam Current Modulation ID", "SH"});
    dict.put(0x300A0350, new String[] {"Patient Support Type", "CS"});
    dict.put(0x300A0352, new String[] {"Patient Support ID", "SH"});
    dict.put(0x300A0354, new String[] {"Patient Support Accessory Code", "LO"});
    dict.put(0x300A0356, new String[] {"Fixation Light Azimuthal Angle", "FL"});
    dict.put(0x300A0358, new String[] {"Fixation Light Polar Angle", "FL"});
    dict.put(0x300A035A, new String[] {"Meterset Rate", "FL"});
    dict.put(0x300A0360, new String[] {"Range Shifter Settings Sequence", "SQ"});
    dict.put(0x300A0362, new String[] {"Range Shifter Setting", "LO"});
    dict.put(0x300A0364, new String[] {"Isocenter to Range Shifter Distance", "FL"});
    dict.put(0x300A0366, new String[] {"Range Shifter Water EquivalentThickness", "FL"});
    dict.put(0x300A0370, new String[] {"Lateral Spreading Device SettingsSequence", "SQ"});
    dict.put(0x300A0372, new String[] {"Lateral Spreading Device Setting", "LO"});
    dict.put(0x300A0374, new String[] {"Isocenter to Lateral SpreadingDevice Distance", "FL"});
    dict.put(0x300A0380, new String[] {"Range Modulator SettingsSequence", "SQ"});
    dict.put(0x300A0382, new String[] {"Range Modulator Gating StartValue", "FL"});
    dict.put(0x300A0384, new String[] {"Range Modulator Gating StopValue", "FL"});
    dict.put(0x300A0386,
             new String[] {"Range Modulator Gating StartWater Equivalent Thickness", "FL"});
    dict.put(0x300A0388,
             new String[] {"Range Modulator Gating StopWater Equivalent Thickness", "FL"});
    dict.put(0x300A038A, new String[] {"Isocenter to Range ModulatorDistance", "FL"});
    dict.put(0x300A0390, new String[] {"Scan Spot Tune ID", "SH"});
    dict.put(0x300A0392, new String[] {"Number of Scan Spot Positions", "IS"});
    dict.put(0x300A0394, new String[] {"Scan Spot Position Map", "FL"});
    dict.put(0x300A0396, new String[] {"Scan Spot Meterset Weights", "FL"});
    dict.put(0x300A0398, new String[] {"Scanning Spot Size", "FL"});
    dict.put(0x300A039A, new String[] {"Number of Paintings", "IS"});
    dict.put(0x300A03A0, new String[] {"Ion Tolerance Table Sequence", "SQ"});
    dict.put(0x300A03A2, new String[] {"Ion Beam Sequence", "SQ"});
    dict.put(0x300A03A4, new String[] {"Ion Beam Limiting DeviceSequence", "SQ"});
    dict.put(0x300A03A6, new String[] {"Ion Block Sequence", "SQ"});
    dict.put(0x300A03A8, new String[] {"Ion Control Point Sequence", "SQ"});
    dict.put(0x300A03AA, new String[] {"Ion Wedge Sequence", "SQ"});
    dict.put(0x300A03AC, new String[] {"Ion Wedge Position Sequence", "SQ"});
    dict.put(0x300A0401, new String[] {"Referenced Setup ImageSequence", "SQ"});
    dict.put(0x300A0402, new String[] {"Setup Image Comment", "ST"});
    dict.put(0x300A0410, new String[] {"Motion Synchronization Sequence", "SQ"});
    dict.put(0x300A0412, new String[] {"Control Point Orientation", "FL"});
    dict.put(0x300A0420, new String[] {"General Accessory Sequence", "SQ"});
    dict.put(0x300A0421, new String[] {"General Accessory ID", "SH"});
    dict.put(0x300A0422, new String[] {"General Accessory Description", "ST"});
    dict.put(0x300A0423, new String[] {"General Accessory Type", "CS"});
    dict.put(0x300A0424, new String[] {"General Accessory Number", "IS"});
    dict.put(0x300A0425, new String[] {"Source to General AccessoryDistance", "FL"});
    dict.put(0x300A0431, new String[] {"Applicator Geometry Sequence", "SQ"});
    dict.put(0x300A0432, new String[] {"Applicator Aperture Shape", "CS"});
    dict.put(0x300A0433, new String[] {"Applicator Opening", "FL"});
    dict.put(0x300A0434, new String[] {"Applicator Opening X", "FL"});
    dict.put(0x300A0435, new String[] {"Applicator Opening Y", "FL"});
    dict.put(0x300A0436, new String[] {"Source to Applicator MountingPosition Distance", "FL"});
    dict.put(0x300A0440, new String[] {"Number of Block Slab Items", "IS"});
    dict.put(0x300A0441, new String[] {"Block Slab Sequence", "SQ"});
    dict.put(0x300A0442, new String[] {"Block Slab Thickness", "DS"});
    dict.put(0x300A0443, new String[] {"Block Slab Number", "US"});
    dict.put(0x300A0450, new String[] {"Device Motion Control Sequence", "SQ"});
    dict.put(0x300A0451, new String[] {"Device Motion Execution Mode", "CS"});
    dict.put(0x300A0452, new String[] {"Device Motion Observation Mode", "CS"});
    dict.put(0x300A0453, new String[] {"Device Motion Parameter CodeSequence", "SQ"});
  }

  /**
   * Adds attributes of group 0x300C.
   */
  private static void addAttributeGroup300C(Hashtable<Integer, String[]> dict) {
    dict.put(0x300C0002, new String[] {"Referenced RT Plan Sequence", "SQ"});
    dict.put(0x300C0004, new String[] {"Referenced Beam Sequence", "SQ"});
    dict.put(0x300C0006, new String[] {"Referenced Beam Number", "IS"});
    dict.put(0x300C0007, new String[] {"Referenced Reference ImageNumber", "IS"});
    dict.put(0x300C0008, new String[] {"Start Cumulative Meterset Weight", "DS"});
    dict.put(0x300C0009, new String[] {"End Cumulative Meterset Weight", "DS"});
    dict.put(0x300C000A, new String[] {"Referenced Brachy ApplicationSetup Sequence", "SQ"});
    dict.put(0x300C000C, new String[] {"Referenced Brachy ApplicationSetup Number", "IS"});
    dict.put(0x300C000E, new String[] {"Referenced Source Number", "IS"});
    dict.put(0x300C0020, new String[] {"Referenced Fraction GroupSequence", "SQ"});
    dict.put(0x300C0022, new String[] {"Referenced Fraction GroupNumber", "IS"});
    dict.put(0x300C0040, new String[] {"Referenced Verification ImageSequence", "SQ"});
    dict.put(0x300C0042, new String[] {"Referenced Reference ImageSequence", "SQ"});
    dict.put(0x300C0050, new String[] {"Referenced Dose ReferenceSequence", "SQ"});
    dict.put(0x300C0051, new String[] {"Referenced Dose ReferenceNumber", "IS"});
    dict.put(0x300C0055, new String[] {"Brachy Referenced DoseReference Sequence", "SQ"});
    dict.put(0x300C0060, new String[] {"Referenced Structure SetSequence", "SQ"});
    dict.put(0x300C006A, new String[] {"Referenced Patient Setup Number", "IS"});
    dict.put(0x300C0080, new String[] {"Referenced Dose Sequence", "SQ"});
    dict.put(0x300C00A0, new String[] {"Referenced Tolerance TableNumber", "IS"});
    dict.put(0x300C00B0, new String[] {"Referenced Bolus Sequence", "SQ"});
    dict.put(0x300C00C0, new String[] {"Referenced Wedge Number", "IS"});
    dict.put(0x300C00D0, new String[] {"Referenced Compensator Number", "IS"});
    dict.put(0x300C00E0, new String[] {"Referenced Block Number", "IS"});
    dict.put(0x300C00F0, new String[] {"Referenced Control Point Index", "IS"});
    dict.put(0x300C00F2, new String[] {"Referenced Control PointSequence", "SQ"});
    dict.put(0x300C00F4, new String[] {"Referenced Start Control PointIndex", "IS"});
    dict.put(0x300C00F6, new String[] {"Referenced Stop Control PointIndex", "IS"});
    dict.put(0x300C0100, new String[] {"Referenced Range Shifter Number", "IS"});
    dict.put(0x300C0102, new String[] {"Referenced Lateral SpreadingDevice Number", "IS"});
    dict.put(0x300C0104, new String[] {"Referenced Range ModulatorNumber", "IS"});
    dict.put(0x300C0111, new String[] {"Omitted Beam Task Sequence", "SQ"});
    dict.put(0x300C0112, new String[] {"Reason for Omission", "CS"});
    dict.put(0x300C0113, new String[] {"Reason for Omission Description", "LO"});
  }

  /**
   * Adds attributes of group 0x300E.
   */
  private static void addAttributeGroup300E(Hashtable<Integer, String[]> dict) {
    dict.put(0x300E0002, new String[] {"Approval Status", "CS"});
    dict.put(0x300E0004, new String[] {"Review Date", "DA"});
    dict.put(0x300E0005, new String[] {"Review Time", "TM"});
    dict.put(0x300E0008, new String[] {"Reviewer Name", "PN"});
  }

  /**
   * Adds attributes of group 0x4000.
   */
  private static void addAttributeGroup4000(Hashtable<Integer, String[]> dict) {
    dict.put(0x40000010, new String[] {"Arbitrary", "LT"}); // Retired
    dict.put(0x40004000, new String[] {"Text Comments", "LT"}); // Retired
  }

  /**
   * Adds attributes of group 0x4008.
   */
  private static void addAttributeGroup4008(Hashtable<Integer, String[]> dict) {
    dict.put(0x40080040, new String[] {"Results ID", "SH"}); // Retired
    dict.put(0x40080042, new String[] {"Results ID Issuer", "LO"}); // Retired
    dict.put(0x40080050, new String[] {"Referenced InterpretationSequence", "SQ"}); // Retired
    dict.put(0x400800FF, new String[] {"Report Production Status (Trial)", "CS"}); // Retired
    dict.put(0x40080100, new String[] {"Interpretation Recorded Date", "DA"}); // Retired
    dict.put(0x40080101, new String[] {"Interpretation Recorded Time", "TM"}); // Retired
    dict.put(0x40080102, new String[] {"Interpretation Recorder", "PN"}); // Retired
    dict.put(0x40080103, new String[] {"Reference to Recorded Sound", "LO"}); // Retired
    dict.put(0x40080108, new String[] {"Interpretation Transcription Date", "DA"}); // Retired
    dict.put(0x40080109, new String[] {"Interpretation Transcription Time", "TM"}); // Retired
    dict.put(0x4008010A, new String[] {"Interpretation Transcriber", "PN"}); // Retired
    dict.put(0x4008010B, new String[] {"Interpretation Text", "ST"}); // Retired
    dict.put(0x4008010C, new String[] {"Interpretation Author", "PN"}); // Retired
    dict.put(0x40080111, new String[] {"Interpretation Approver Sequence", "SQ"}); // Retired
    dict.put(0x40080112, new String[] {"Interpretation Approval Date", "DA"}); // Retired
    dict.put(0x40080113, new String[] {"Interpretation Approval Time", "TM"}); // Retired
    dict.put(0x40080114, new String[] {"Physician Approving Interpretation", "PN"}); // Retired
    dict.put(0x40080115, new String[] {"Interpretation DiagnosisDescription", "LT"}); // Retired
    dict.put(0x40080117, new String[] {"Interpretation Diagnosis CodeSequence", "SQ"}); // Retired
    dict.put(0x40080118, new String[] {"Results Distribution List Sequence", "SQ"}); // Retired
    dict.put(0x40080119, new String[] {"Distribution Name", "PN"}); // Retired
    dict.put(0x4008011A, new String[] {"Distribution Address", "LO"}); // Retired
    dict.put(0x40080200, new String[] {"Interpretation ID", "SH"}); // Retired
    dict.put(0x40080202, new String[] {"Interpretation ID Issuer", "LO"}); // Retired
    dict.put(0x40080210, new String[] {"Interpretation Type ID", "CS"}); // Retired
    dict.put(0x40080212, new String[] {"Interpretation Status ID", "CS"}); // Retired
    dict.put(0x40080300, new String[] {"Impressions", "ST"}); // Retired
    dict.put(0x40084000, new String[] {"Results Comments", "ST"}); // Retired
  }

  /**
   * Adds attributes of group 0x4010.
   */
  private static void addAttributeGroup4010(Hashtable<Integer, String[]> dict) {
    dict.put(0x40100001, new String[] {"Low Energy Detectors", "CS"}); // DICOS
    dict.put(0x40100002, new String[] {"High Energy Detectors", "CS"}); // DICOS
    dict.put(0x40100004, new String[] {"Detector Geometry Sequence", "SQ"}); // DICOS
    dict.put(0x40101001, new String[] {"Threat ROI Voxel Sequence", "SQ"}); // DICOS
    dict.put(0x40101004, new String[] {"Threat ROI Base", "FL"}); // DICOS
    dict.put(0x40101005, new String[] {"Threat ROI Extents", "FL"}); // DICOS
    dict.put(0x40101006, new String[] {"Threat ROI Bitmap", "OB"}); // DICOS
    dict.put(0x40101007, new String[] {"Route Segment ID", "SH"}); // DICOS
    dict.put(0x40101008, new String[] {"Gantry Type", "CS"}); // DICOS
    dict.put(0x40101009, new String[] {"OOI Owner Type", "CS"}); // DICOS
    dict.put(0x4010100A, new String[] {"Route Segment Sequence", "SQ"}); // DICOS
    dict.put(0x40101010, new String[] {"Potential Threat Object ID", "US"}); // DICOS
    dict.put(0x40101011, new String[] {"Threat Sequence", "SQ"}); // DICOS
    dict.put(0x40101012, new String[] {"Threat Category", "CS"}); // DICOS
    dict.put(0x40101013, new String[] {"Threat Category Description", "LT"}); // DICOS
    dict.put(0x40101014, new String[] {"ATD Ability Assessment", "CS"}); // DICOS
    dict.put(0x40101015, new String[] {"ATD Assessment Flag", "CS"}); // DICOS
    dict.put(0x40101016, new String[] {"ATD Assessment Probability", "FL"}); // DICOS
    dict.put(0x40101017, new String[] {"Mass", "FL"}); // DICOS
    dict.put(0x40101018, new String[] {"Density", "FL"}); // DICOS
    dict.put(0x40101019, new String[] {"Z Effective", "FL"}); // DICOS
    dict.put(0x4010101A, new String[] {"Boarding Pass ID", "SH"}); // DICOS
    dict.put(0x4010101B, new String[] {"Center of Mass", "FL"}); // DICOS
    dict.put(0x4010101C, new String[] {"Center of PTO", "FL"}); // DICOS
    dict.put(0x4010101D, new String[] {"Bounding Polygon", "FL"}); // DICOS
    dict.put(0x4010101E, new String[] {"Route Segment Start Location ID", "SH"}); // DICOS
    dict.put(0x4010101F, new String[] {"Route Segment End Location ID", "SH"}); // DICOS
    dict.put(0x40101020, new String[] {"Route Segment Location ID Type", "CS"}); // DICOS
    dict.put(0x40101021, new String[] {"Abort Reason", "CS"}); // DICOS
    dict.put(0x40101023, new String[] {"Volume of PTO", "FL"}); // DICOS
    dict.put(0x40101024, new String[] {"Abort Flag", "CS"}); // DICOS
    dict.put(0x40101025, new String[] {"Route Segment Start Time", "DT"}); // DICOS
    dict.put(0x40101026, new String[] {"Route Segment End Time", "DT"}); // DICOS
    dict.put(0x40101027, new String[] {"TDR Type", "CS"}); // DICOS
    dict.put(0x40101028, new String[] {"International Route Segment", "CS"}); // DICOS
    dict.put(0x40101029, new String[] {"Threat Detection Algorithm andVersion", "LO"}); // DICOS
    dict.put(0x4010102A, new String[] {"Assigned Location", "SH"}); // DICOS
    dict.put(0x4010102B, new String[] {"Alarm Decision Time", "DT"}); // DICOS
    dict.put(0x40101031, new String[] {"Alarm Decision", "CS"}); // DICOS
    dict.put(0x40101033, new String[] {"Number of Total Objects", "US"}); // DICOS
    dict.put(0x40101034, new String[] {"Number of Alarm Objects", "US"}); // DICOS
    dict.put(0x40101037, new String[] {"PTO Representation Sequence", "SQ"}); // DICOS
    dict.put(0x40101038, new String[] {"ATD Assessment Sequence", "SQ"}); // DICOS1
    dict.put(0x40101039, new String[] {"TIP Type", "CS"}); // DICOS
    dict.put(0x4010103A, new String[] {"DICOS Version", "CS"}); // DICOS
    dict.put(0x40101041, new String[] {"OOI Owner Creation Time", "DT"}); // DICOS
    dict.put(0x40101042, new String[] {"OOI Type", "CS"}); // DICOS
    dict.put(0x40101043, new String[] {"OOI Size", "FL"}); // DICOS
    dict.put(0x40101044, new String[] {"Acquisition Status", "CS"}); // DICOS
    dict.put(0x40101045, new String[] {"Basis Materials Code Sequence", "SQ"}); // DICOS
    dict.put(0x40101046, new String[] {"Phantom Type", "CS"}); // DICOS
    dict.put(0x40101047, new String[] {"OOI Owner Sequence", "SQ"}); // DICOS
    dict.put(0x40101048, new String[] {"Scan Type", "CS"}); // DICOS
    dict.put(0x40101051, new String[] {"Itinerary ID", "LO"}); // DICOS
    dict.put(0x40101052, new String[] {"Itinerary ID Type", "SH"}); // DICOS
    dict.put(0x40101053, new String[] {"Itinerary ID Assigning Authority", "LO"}); // DICOS
    dict.put(0x40101054, new String[] {"Route ID", "SH"}); // DICOS
    dict.put(0x40101055, new String[] {"Route ID Assigning Authority", "SH"}); // DICOS
    dict.put(0x40101056, new String[] {"Inbound Arrival Type", "CS"}); // DICOS
    dict.put(0x40101058, new String[] {"Carrier ID", "SH"}); // DICOS
    dict.put(0x40101059, new String[] {"Carrier ID Assigning Authority", "CS"}); // DICOS
    dict.put(0x40101060, new String[] {"Source Orientation", "FL"}); // DICOS
    dict.put(0x40101061, new String[] {"Source Position", "FL"}); // DICOS
    dict.put(0x40101062, new String[] {"Belt Height", "FL"}); // DICOS
    dict.put(0x40101064, new String[] {"Algorithm Routing Code Sequence", "SQ"}); // DICOS
    dict.put(0x40101067, new String[] {"Transport Classification", "CS"}); // DICOS
    dict.put(0x40101068, new String[] {"OOI Type Descriptor", "LT"}); // DICOS
    dict.put(0x40101069, new String[] {"Total Processing Time", "FL"}); // DICOS
    dict.put(0x4010106C, new String[] {"Detector Calibration Data", "OB"}); // DICOS
    dict.put(0x4010106D, new String[] {"Additional Screening Performed", "CS"}); // DICOS
    dict.put(0x4010106E, new String[] {"Additional Inspection SelectionCriteria", "CS"}); // DICOS
    dict.put(0x4010106F, new String[] {"Additional Inspection MethodSequence", "SQ"}); // DICOS
    dict.put(0x40101070, new String[] {"AIT Device Type", "CS"}); // DICOS
    dict.put(0x40101071, new String[] {"QR Measurements Sequence", "SQ"}); // DICOS
    dict.put(0x40101072, new String[] {"Target Material Sequence", "SQ"}); // DICOS
    dict.put(0x40101073, new String[] {"SNR Threshold", "FD"}); // DICOS
    dict.put(0x40101075, new String[] {"Image Scale Representation", "DS"}); // DICOS
    dict.put(0x40101076, new String[] {"Referenced PTO Sequence", "SQ"}); // DICOS
    dict.put(0x40101077, new String[] {"Referenced TDR InstanceSequence", "SQ"}); // DICOS
    dict.put(0x40101078, new String[] {"PTO Location Description", "ST"}); // DICOS
    dict.put(0x40101079, new String[] {"Anomaly Locator IndicatorSequence", "SQ"}); // DICOS
    dict.put(0x4010107A, new String[] {"Anomaly Locator Indicator", "FL"}); // DICOS
    dict.put(0x4010107B, new String[] {"PTO Region Sequence", "SQ"}); // DICOS
    dict.put(0x4010107C, new String[] {"Inspection Selection Criteria", "CS"}); // DICOS
    dict.put(0x4010107D, new String[] {"Secondary Inspection MethodSequence", "SQ"}); // DICOS
    dict.put(0x4010107E, new String[] {"PRCS to RCS Orientation", "DS"}); // DICOS
  }

  /**
   * Adds attributes of group 0x4FFE.
   */
  private static void addAttributeGroup4FFE(Hashtable<Integer, String[]> dict) {
    dict.put(0x4FFE0001, new String[] {"MAC Parameters Sequence", "SQ"});
  }

  /**
   * Adds attributes of group 0x5200.
   */
  private static void addAttributeGroup5200(Hashtable<Integer, String[]> dict) {
    dict.put(0x52009229, new String[] {"Shared Functional GroupsSequence", "SQ"});
    dict.put(0x52009230, new String[] {"Per-frame Functional GroupsSequence", "SQ"});
  }

  /**
   * Adds attributes of group 0x5400.
   */
  private static void addAttributeGroup5400(Hashtable<Integer, String[]> dict) {
    dict.put(0x54000100, new String[] {"Waveform Sequence", "SQ"});
    // dict.put(0x54000110, new String[] {"Channel Minimum Value", "OB or OW"});
    // dict.put(0x54000112, new String[] {"Channel Maximum Value", "OB or OW"});
    dict.put(0x54001004, new String[] {"Waveform Bits Allocated", "US"});
    dict.put(0x54001006, new String[] {"Waveform Sample Interpretation", "CS"});
    // dict.put(0x5400100A, new String[] {"Waveform Padding Value", "OB or OW"});
    // dict.put(0x54001010, new String[] {"Waveform Data", "OB or OW"});
  }

  /**
   * Adds attributes of group 0x5600.
   */
  private static void addAttributeGroup5600(Hashtable<Integer, String[]> dict) {
    dict.put(0x56000010, new String[] {"First Order Phase Correction Angle", "OF"});
    dict.put(0x56000020, new String[] {"Spectroscopy Data", "OF"});
  }

  /**
   * Adds attributes of group 0x7FE0.
   */
  private static void addAttributeGroup7FE0(Hashtable<Integer, String[]> dict) {
    dict.put(0x7FE00008, new String[] {"Float Pixel Data", "OF"});
    dict.put(0x7FE00009, new String[] {"Double Float Pixel Data", "OD"});
    // dict.put(0x7FE00010, new String[] {"Pixel Data", "OB or OW"});
    dict.put(0x7FE00020, new String[] {"Coefficients SDVN", "OW"}); // Retired
    dict.put(0x7FE00030, new String[] {"Coefficients SDHN", "OW"}); // Retired
    dict.put(0x7FE00040, new String[] {"Coefficients SDDN", "OW"}); // Retired
  }

  /**
   * Adds attributes of group 0xFFFA.
   */
  private static void addAttributeGroupFFFA(Hashtable<Integer, String[]> dict) {
    dict.put(0xFFFAFFFA, new String[] {"Digital Signatures Sequence", "SQ"});
  }


	// -- Nested Classes --

	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

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

		private Vector<String> companionFiles = new Vector<>();

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
		public ColorTable getColorTable(final int imageIndex,
			final long planeIndex)
		{
			final int pixelType = get(0).getPixelType();

			switch (pixelType) {
				case FormatTools.INT8:
				case FormatTools.UINT8:
					if (lut != null && lut8 == null) lut8 = new ColorTable8(lut);
					return lut8;
				case FormatTools.INT16:
				case FormatTools.UINT16:
					if (shortLut != null && lut16 == null) lut16 = new ColorTable16(
						shortLut);
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
				get(i).setAxisTypes(Axes.X, Axes.Y);
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
						final Metadata m = p.parse(fileList.get(keys[i]).get(0),
							new SCIFIOConfig().groupableSetGroupFiles(false));
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

	public static class Checker extends AbstractChecker {

		// -- Constants --

		private static final String[] DICOM_SUFFIXES = { "dic", "dcm", "dicom",
			"j2ki", "j2kr" };

		// -- Checker API Methods --

		@Override
		public boolean suffixNecessary() {
			return false;
		}

		@Override
		public boolean suffixSufficient() {
			return false;
		}

		@Override
		public boolean isFormat(final String name, final SCIFIOConfig config) {
			// extension is sufficient as long as it is DIC, DCM, DICOM, J2KI, or J2KR
			if (FormatTools.checkSuffix(name, DICOM_SUFFIXES)) return true;
			return super.isFormat(name, config);
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

	public static class Parser extends AbstractParser<Metadata> {

		// -- Constants --

		private static final int PIXEL_REPRESENTATION = 0x00280103;
		private static final int PIXEL_SIGN = 0x00281041;
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

		@Parameter
		private CodecService codecService;

		// -- Parser API Methods --

		@Override
		public int fileGroupOption(final String id) throws FormatException,
			IOException
		{
			return FormatTools.CAN_GROUP;
		}

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
				FormatException
		{
			meta.createImageMetadata(1);

			stream.order(true);

			final ImageMetadata iMeta = meta.get(0);

			// look for companion files
			final Vector<String> companionFiles = new Vector<>();
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
			final MetadataLevel level = config.parserGetLevel();

			getSource().seek(128);
			if (getSource().readString(4).equals("DICM")) {
				if (level != MetadataLevel.MINIMUM) {
					// header exists, so we'll read it
					getSource().seek(0);
					meta.getTable().put("Header information", getSource().readString(
						128));
					getSource().skipBytes(4);
				}
				location = 128;
			}
			else getSource().seek(0);

			log().info("Reading tags");

			long baseOffset = 0;

			boolean decodingTags = true;
			boolean signed = false;

			while (decodingTags) {
				if (getSource().getFilePointer() + 4 >= getSource().length()) {
					break;
				}
				log().debug("Reading tag from " + getSource().getFilePointer());
				final DICOMTag tag = DICOMUtils.getNextTag(getSource(),
					bigEndianTransferSyntax, oddLocations);
				iMeta.setLittleEndian(tag.isLittleEndian());

				if (tag.getElementLength() <= 0) continue;

				oddLocations = (location & 1) != 0;

				log().debug("  tag=" + tag.get() + " len=" + tag.getElementLength() +
					" fp=" + getSource().getFilePointer());

				String s = null;
				switch (tag.get()) {
					case TRANSFER_SYNTAX_UID:
						// this tag can indicate which compression scheme is used
						s = getSource().readString(tag.getElementLength());
						addInfo(meta, tag, s);
						if (s.startsWith("1.2.840.10008.1.2.4.9")) isJP2K = true;
						else if (s.startsWith("1.2.840.10008.1.2.4")) isJPEG = true;
						else if (s.startsWith("1.2.840.10008.1.2.5")) isRLE = true;
						else if (s.equals("1.2.8.10008.1.2.1.99")) isDeflate = true;
						else if (s.contains("1.2.4") || s.contains("1.2.5")) {
							throw new UnsupportedCompressionException(
								"Sorry, compression type " + s + " not supported");
						}
						if (s.contains("1.2.840.10008.1.2.2")) {
							bigEndianTransferSyntax = true;
						}
						break;
					case NUMBER_OF_FRAMES:
						s = getSource().readString(tag.getElementLength());
						addInfo(meta, tag, s);
						final double frames = Double.parseDouble(s);
						if (frames > 1.0) imagesPerFile = (int) frames;
						break;
					case SAMPLES_PER_PIXEL:
						addInfo(meta, tag, getSource().readShort());
						break;
					case PLANAR_CONFIGURATION:
						final int configuration = getSource().readShort();
						interleaved = configuration == 0;
						if (interleaved) {
							iMeta.setAxisTypes(Axes.CHANNEL, Axes.X, Axes.Y);
							iMeta.setPlanarAxisCount(3);
						}
						addInfo(meta, tag, configuration);
						break;
					case ROWS:
						if (sizeY == 0) {
							sizeY = getSource().readShort();
							iMeta.addAxis(Axes.Y, sizeY);
						}
						else getSource().skipBytes(2);
						addInfo(meta, tag, sizeY);
						break;
					case COLUMNS:
						if (sizeX == 0) {
							sizeX = getSource().readShort();
							iMeta.addAxis(Axes.X, sizeX);
						}
						else getSource().skipBytes(2);
						addInfo(meta, tag, sizeX);
						break;
					case PHOTOMETRIC_INTERPRETATION:
					case PIXEL_SPACING:
					case SLICE_SPACING:
					case RESCALE_INTERCEPT:
					case WINDOW_CENTER:
					case RESCALE_SLOPE:
						addInfo(meta, tag, getSource().readString(tag.getElementLength()));
						break;
					case BITS_ALLOCATED:
						if (bitsPerPixel == 0) bitsPerPixel = getSource().readShort();
						else getSource().skipBytes(2);
						addInfo(meta, tag, bitsPerPixel);
						break;
					case PIXEL_REPRESENTATION:
					case PIXEL_SIGN:
						final short ss = getSource().readShort();
						signed = ss == 1;
						addInfo(meta, tag, ss);
						break;
					case 537262910:
					case WINDOW_WIDTH:
						final String t = getSource().readString(tag.getElementLength());
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
							baseOffset = getSource().getFilePointer();
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
						getSource().skipBytes(tag.getElementLength());
						break;
					case 0:
						getSource().seek(getSource().getFilePointer() - 4);
						break;
					default:
						final long oldfp = getSource().getFilePointer();
						addInfo(meta, tag, s);
						getSource().seek(oldfp + tag.getElementLength());
				}
				if (getSource().getFilePointer() >= (getSource().length() - 4)) {
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

			final int pixelType = FormatTools.pixelTypeFromBytes(bitsPerPixel / 8,
				signed, false);

			iMeta.setBitsPerPixel(bpp);
			iMeta.setPixelType(pixelType);

			final int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);

			final int planeSize = sizeX * sizeY * (int) (meta.getColorTable(0,
				0) == null ? meta.get(0).getAxisLength(Axes.CHANNEL) : 1) *
				bytesPerPixel;

			meta.setJP2K(isJP2K);
			meta.setJPEG(isJPEG);
			meta.setImagesPerFile(imagesPerFile);
			meta.setRLE(isRLE);
			meta.setDeflate(isDeflate);
			meta.setMaxPixelValue(maxPixelValue);
			meta.setOddLocations(oddLocations);

			log().info("Calculating image offsets");

			// calculate the offset to each plane

			getSource().seek(baseOffset - 12);
			final int len = getSource().readInt();
			if (len >= 0 && len + getSource().getFilePointer() < getSource()
				.length())
			{
				getSource().skipBytes(len);
				final int check = getSource().readShort() & 0xffff;
				if (check == 0xfffe) {
					baseOffset = getSource().getFilePointer() + 2;
				}
			}

			offsets = new long[imagesPerFile];
			meta.setOffsets(offsets);

			for (int i = 0; i < imagesPerFile; i++) {
				if (isRLE) {
					if (i == 0) getSource().seek(baseOffset);
					else {
						getSource().seek(offsets[i - 1]);
						final CodecOptions options = new CodecOptions();
						options.maxBytes = planeSize / bytesPerPixel;
						for (int q = 0; q < bytesPerPixel; q++) {
							final PackbitsCodec codec = codecService.getCodec(
								PackbitsCodec.class);
							codec.decompress(getSource(), options);
							while (getSource().read() == 0) { /* Read to non-0 data */}
							getSource().seek(getSource().getFilePointer() - 1);
						}
					}
					getSource().skipBytes(i == 0 ? 64 : 53);
					while (getSource().read() == 0) { /* Read to non-0 data */}
					offsets[i] = getSource().getFilePointer() - 1;
				}
				else if (isJPEG || isJP2K) {
					// scan for next JPEG magic byte sequence
					if (i == 0) offsets[i] = baseOffset;
					else offsets[i] = offsets[i - 1] + 3;

					final byte secondCheck = isJPEG ? (byte) 0xd8 : (byte) 0x4f;

					getSource().seek(offsets[i]);
					final byte[] buf = new byte[8192];
					int n = getSource().read(buf);
					boolean found = false;
					while (!found) {
						for (int q = 0; q < n - 2; q++) {
							if (buf[q] == (byte) 0xff && buf[q + 1] == secondCheck && buf[q +
								2] == (byte) 0xff)
							{
								if (isJPEG || (isJP2K && buf[q + 3] == 0x51)) {
									found = true;
									offsets[i] = getSource().getFilePointer() + q - n;
									break;
								}
							}
						}
						if (!found) {
							for (int q = 0; q < 4; q++) {
								buf[q] = buf[buf.length + q - 4];
							}
							n = getSource().read(buf, 4, buf.length - 4) + 4;
						}
					}
				}
				else offsets[i] = baseOffset + planeSize * i;
			}

			makeFileList(config);
		}

		@Override
		public String[] getImageUsedFiles(final int imageIndex,
			final boolean noPixels)
		{
			FormatTools.assertId(getSource(), true, 1);
			if (noPixels || getMetadata().getFileList() == null) return null;
			final Integer[] keys = getMetadata().getFileList().keySet().toArray(
				new Integer[0]);
			Arrays.sort(keys);
			final Vector<String> files = getMetadata().getFileList().get(
				keys[imageIndex]);
			for (final String f : getMetadata().getCompanionFiles()) {
				files.add(f);
			}
			return files == null ? null : files.toArray(new String[files.size()]);
		}

		// -- Helper methods --

		private void makeFileList(final SCIFIOConfig config) throws FormatException,
			IOException
		{
			log().info("Building file list");

			if (getMetadata().getFileList() == null && getMetadata()
				.getOriginalInstance() != null && getMetadata()
					.getOriginalDate() != null && getMetadata()
						.getOriginalTime() != null && config.groupableIsGroupFiles())
			{
				final Hashtable<Integer, Vector<String>> fileList =
					new Hashtable<>();
				final Integer s = new Integer(getMetadata().getOriginalSeries());
				fileList.put(s, new Vector<String>());

				final int instanceNumber = Integer.parseInt(getMetadata()
					.getOriginalInstance()) - 1;
				if (instanceNumber == 0) fileList.get(s).add(getSource().getFileName());
				else {
					while (instanceNumber > fileList.get(s).size()) {
						fileList.get(s).add(null);
					}
					fileList.get(s).add(getSource().getFileName());
				}

				// look for matching files in the current directory
				final Location currentFile = new Location(getContext(), getSource()
					.getFileName()).getAbsoluteFile();
				Location directory = currentFile.getParentFile();
				scanDirectory(fileList, directory, false);

				// move up a directory and look for other directories that
				// could contain matching files

				directory = directory.getParentFile();
				final String[] subdirs = directory.list(true);
				if (subdirs != null) {
					for (final String subdir : subdirs) {
						final Location f = new Location(getContext(), directory, subdir)
							.getAbsoluteFile();
						if (!f.isDirectory()) continue;
						scanDirectory(fileList, f, true);
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

				getMetadata().setFileList(fileList);
			}
			else if (getMetadata().getFileList() == null) {
				final Hashtable<Integer, Vector<String>> fileList =
					new Hashtable<>();
				fileList.put(0, new Vector<String>());
				fileList.get(0).add(getSource().getFileName());
				getMetadata().setFileList(fileList);
			}
		}

		/**
		 * DICOM datasets produced by:
		 * http://www.ct-imaging.de/index.php/en/ct-systeme-e/mikro-ct-e.html
		 * contain a bunch of extra metadata and log files. We do not parse these
		 * extra files, but do locate and attach them to the DICOM file(s).
		 */
		private void attachCompanionFiles(final Vector<String> companionFiles) {
			final Location parent = new Location(getContext(), getSource()
				.getFileName()).getAbsoluteFile().getParentFile();
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
		private void scanDirectory(
			final Hashtable<Integer, Vector<String>> fileList, final Location dir,
			final boolean checkSeries) throws FormatException, IOException
		{
			final Location currentFile = new Location(getContext(), getSource()
				.getFileName()).getAbsoluteFile();
			final FilePattern pattern = new FilePattern(getContext(), currentFile
				.getName(), dir.getAbsolutePath());
			String[] patternFiles = pattern.getFiles();
			if (patternFiles == null) patternFiles = new String[0];
			Arrays.sort(patternFiles);
			final String[] files = dir.list(true);
			if (files == null) return;
			Arrays.sort(files);
			for (final String f : files) {
				final String file = new Location(getContext(), dir, f)
					.getAbsolutePath();
				log().debug("Checking file " + file);
				if (!f.equals(getSource().getFileName()) && !file.equals(getSource()
					.getFileName()) && getFormat().createChecker().isFormat(file) &&
					Arrays.binarySearch(patternFiles, file) >= 0)
				{
					addFileToList(fileList, file, checkSeries);
				}
			}
		}

		/**
		 * Determine if the given file belongs in the same dataset as this file.
		 */

		private void addFileToList(
			final Hashtable<Integer, Vector<String>> fileList, final String file,
			final boolean checkSeries) throws FormatException, IOException
		{
			final RandomAccessInputStream stream = new RandomAccessInputStream(
				getContext(), file);
			if (!getFormat().createChecker().isFormat(stream)) {
				stream.close();
				return;
			}
			stream.order(true);

			stream.seek(128);
			if (!stream.readString(4).equals("DICM")) stream.seek(0);

			int fileSeries = -1;

			String date = null, time = null, instance = null;
			while (date == null || time == null || instance == null || (checkSeries &&
				fileSeries < 0))
			{
				final long fp = stream.getFilePointer();
				if (fp + 4 >= stream.length() || fp < 0) break;
				final DICOMTag tag = DICOMUtils.getNextTag(stream);
				String[] attribute = TYPES.get(new Integer(tag.get()));
				final String key = (attribute != null) ? attribute[0] : null;
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
					fileSeries = Integer.parseInt(stream.readString(tag
						.getElementLength()).trim());
				}
				else stream.skipBytes(tag.getElementLength());
			}
			stream.close();

			if (date == null || time == null || instance == null || (checkSeries &&
				fileSeries == getMetadata().getOriginalSeries()))
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
				timestamp = Integer.parseInt(getMetadata().getOriginalTime());
			}
			catch (final NumberFormatException e) {}

			if (date.equals(getMetadata().getOriginalDate()) && (Math.abs(stamp -
				timestamp) < 150))
			{
				int position = Integer.parseInt(instance) - 1;
				if (position < 0) position = 0;
				if (fileList.get(fileSeries) == null) {
					fileList.put(fileSeries, new Vector<String>());
				}
				if (position < fileList.get(fileSeries).size()) {
					while (position < fileList.get(fileSeries).size() && fileList.get(
						fileSeries).get(position) != null)
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

				String[] attribute = TYPES.get(tag.get());
				String key = (attribute != null) ? attribute[0] : null;
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
				else if (key.contains("Palette Color LUT Data")) {
					final String color = key.substring(0, key.indexOf(" ")).trim();
					final int ndx = color.equals("Red") ? 0 : color.equals("Green") ? 1
						: 2;
					final long fp = getSource().getFilePointer();
					getSource().seek(getSource().getFilePointer() - tag
						.getElementLength() + 1);
					meta.shortLut[ndx] = new short[tag.getElementLength() / 2];
					meta.lut[ndx] = new byte[tag.getElementLength() / 2];
					for (int i = 0; i < meta.lut[ndx].length; i++) {
						meta.shortLut[ndx][i] = getSource().readShort();
						meta.lut[ndx][i] = (byte) (meta.shortLut[ndx][i] & 0xff);
					}
					getSource().seek(fp);
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
			if (tag.get() == ITEM_DELIMINATION || tag
				.get() == SEQUENCE_DELIMINATION)
			{
				tag.setInSequence(false);
			}

			String[] attribute = TYPES.get(new Integer(tag.get()));
			String id = (attribute != null) ? attribute[0] : null;
			int vr = tag.getVR();

			if (id != null) {
				if (vr == DICOMUtils.IMPLICIT_VR) {
					String vrName = attribute[1];
					vr = (vrName.charAt(0) << 8) + vrName.charAt(1);
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
					// Cannot fix element length to 4, because AT value representation is
					// always
					// 4 bytes long (DICOM specs PS3.5 par.6.2), but value multiplicity is
					// 1-n
					final byte[] bytes = new byte[tag.getElementLength()];
					// Read from stream
					getSource().readFully(bytes);
					// If little endian, swap bytes to get a string with a user friendly
					// representation of tag group and tag element
					if (tag.littleEndian) {
						for (int i = 0; i < bytes.length / 2; ++i) {
							final byte t = bytes[2 * i];
							bytes[2 * i] = bytes[2 * i + 1];
							bytes[2 * i + 1] = t;
						}
					}
					// Convert the bytes to a string
					value = DataTools.bytesToHex(bytes);
					break;
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
					value = getSource().readString(tag.getElementLength());
					break;
				case DICOMUtils.US:
					if (tag.getElementLength() == 2) value = Integer.toString(getSource()
						.readShort());
					else {
						value = "";
						final int n = tag.getElementLength() / 2;
						for (int i = 0; i < n; i++) {
							value += Integer.toString(getSource().readShort()) + " ";
						}
					}
					break;
				case DICOMUtils.IMPLICIT_VR:
					value = getSource().readString(tag.getElementLength());
					if (tag.getElementLength() <= 4 || tag.getElementLength() > 44)
						value = null;
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
				if (getSource().getFilePointer() + skipCount <= getSource().length()) {
					getSource().skipBytes((int) skipCount);
				}
				tag.addLocation(tag.getElementLength());
				value = "";
			}

			if (value != null && id == null && !value.equals("")) return value;
			else if (id == null) return null;
			else return value;
		}
	}

	public static class Reader extends ByteArrayReader<Metadata> {

		@Parameter
		private InitializeService initializeService;

		@Parameter
		private CodecService codecService;

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.MEDICAL_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public boolean hasCompanionFiles() {
			return true;
		}

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, long planeIndex,
			final ByteArrayPlane plane, final long[] planeMin, final long[] planeMax,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			plane.setColorTable(meta.getColorTable(imageIndex, planeIndex));
			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, plane
				.getData().length, planeMin, planeMax);

			final int xAxis = meta.get(imageIndex).getAxisIndex(Axes.X);
			final int yAxis = meta.get(imageIndex).getAxisIndex(Axes.Y);
			final int x = (int) planeMin[xAxis], y = (int) planeMin[yAxis], w =
				(int) planeMax[xAxis], h = (int) planeMax[yAxis];

			final Hashtable<Integer, Vector<String>> fileList = meta.getFileList();

			final Integer[] keys = fileList.keySet().toArray(new Integer[0]);
			Arrays.sort(keys);
			if (fileList.get(keys[imageIndex]).size() > 1) {
				final int fileNumber = (int) (planeIndex / meta.getImagesPerFile());
				planeIndex = planeIndex % meta.getImagesPerFile();
				final String file = fileList.get(keys[imageIndex]).get(fileNumber);
				final io.scif.Reader r = initializeService.initializeReader(file);
				return (ByteArrayPlane) r.openPlane(imageIndex, planeIndex, plane,
					planeMin, planeMax, config);
			}

			final int ec = meta.get(0).isIndexed() ? 1 : (int) meta.get(imageIndex)
				.getAxisLength(Axes.CHANNEL);
			final int bpp = FormatTools.getBytesPerPixel(meta.get(imageIndex)
				.getPixelType());
			final int bytes = (int) (meta.get(imageIndex).getAxisLength(Axes.X) * meta
				.get(imageIndex).getAxisLength(Axes.Y) * bpp * ec);
			getStream().seek(meta.getOffsets()[(int) planeIndex]);

			if (meta.isRLE()) {
				// plane is compressed using run-length encoding
				final CodecOptions options = new CodecOptions();
				options.maxBytes = (int) (meta.get(imageIndex).getAxisLength(Axes.X) *
					meta.get(imageIndex).getAxisLength(Axes.Y));
				final PackbitsCodec codec = codecService.getCodec(PackbitsCodec.class);
				for (int c = 0; c < ec; c++) {
					byte[] t = null;

					if (bpp > 1) {
						// TODO unused int planeSize = bytes / (bpp * ec);
						final byte[][] tmp = new byte[bpp][];
						for (int i = 0; i < bpp; i++) {
							tmp[i] = codec.decompress(getStream(), options);
							if (planeIndex < meta.getImagesPerFile() - 1 || i < bpp - 1) {
								while (getStream().read() == 0) { /* Read to non-0 data */}
								getStream().seek(getStream().getFilePointer() - 1);
							}
						}
						t = new byte[bytes / ec];
						for (int i = 0; i < planeIndex; i++) {
							for (int j = 0; j < bpp; j++) {
								final int byteIndex = meta.get(imageIndex).isLittleEndian()
									? bpp - j - 1 : j;
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
							while (getStream().read() == 0) { /* Read to non-0 data */}
							getStream().seek(getStream().getFilePointer() - 1);
						}
					}

					final int rowLen = w * bpp;
					final int srcRowLen = (int) meta.get(imageIndex).getAxisLength(
						Axes.X) * bpp;

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
				final long end = planeIndex < meta.getOffsets().length - 1 ? meta
					.getOffsets()[(int) planeIndex + 1] : getStream().length();
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

				final CodecOptions options = new CodecOptions();
				options.littleEndian = meta.get(imageIndex).isLittleEndian();
				options.interleaved = meta.get(imageIndex)
					.getInterleavedAxisCount() > 0;
				final Codec codec = meta.isJPEG() ? //
					codecService.getCodec(JPEGCodec.class) : //
					codecService.getCodec(JPEG2000Codec.class);
				b = codec.decompress(b, options);

				final int rowLen = w * bpp;
				final int srcRowLen = (int) meta.get(imageIndex).getAxisLength(Axes.X) *
					bpp;

				final int srcPlane = (int) meta.get(imageIndex).getAxisLength(Axes.Y) *
					srcRowLen;

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
						final short s = DataTools.bytesToShort(plane.getBytes(), i, 2,
							little);
						DataTools.unpackBytes(meta.getMaxPixelValue() - s, plane.getBytes(),
							i, 2, little);
					}
				}
			}

			// NB: do *not* apply the rescale function

			return plane;
		}
	}

	// -- DICOM Helper Classes --

	private static class DICOMUtils {

		private static final int AE = 0x4145, AS = 0x4153, AT = 0x4154, CS = 0x4353;
		private static final int DA = 0x4441, DS = 0x4453, DT = 0x4454, FD = 0x4644;
		private static final int FL = 0x464C, IS = 0x4953, LO = 0x4C4F, LT = 0x4C54;
		private static final int PN = 0x504E, SH = 0x5348, SL = 0x534C, SS = 0x5353;
		private static final int ST = 0x5354, TM = 0x544D, UI = 0x5549, UL = 0x554C;
		private static final int US = 0x5553, UT = 0x5554, OB = 0x4F42, OW = 0x4F57;
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
					throw new FormatException("Invalid tag length " + diTag
						.getElementLength());
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

			if (diTag.getElementLength() == 0 && (groupWord == 0x7fe0 ||
				tag == 0x291014))
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
					if (n1 < 0 || n1 + stream.getFilePointer() > stream.length())
						return n2;
					if (n2 < 0 || n2 + stream.getFilePointer() > stream.length())
						return n1;
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
