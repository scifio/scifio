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

package io.scif.formats.dicom;

import java.util.Hashtable;

/**
 * Data dictionary of DICOM types.
 * <p>
 * There are literally thousands of fields defined by the DICOM specifications,
 * so this list may be incomplete.
 * </p>
 *
 * @author Andrea Ballaminut
 * @author Curtis Rueden
 * @author Melissa Linkert
 * @author Mark Hiner
 */
public class DICOMDictionary {

	private final Hashtable<Integer, String[]> table = new Hashtable<>(4000);

	public DICOMDictionary() {
		addAttributes();
	}

	/** Checks whether the given code is in the dictionary. */
	public boolean has(final int code) {
		return table.containsKey(code);
	}

	/** Gets the name for the given code. */
	public String name(final int code) {
		return has(code) ? table.get(code)[0] : null;
	}

	/** Gets the VR for the given code. */
	public String vr(final int code) {
		return has(code) ? table.get(code)[1] : null;
	}

	// -- Helper methods --

	private void addAttributes() {
		addAttributeGroup0002();
		addAttributeGroup0008();
		addAttributeGroup0010();
		addAttributeGroup0012();
		addAttributeGroup0014();
		addAttributeGroup0018();
		addAttributeGroup0020();
		addAttributeGroup0022();
		addAttributeGroup0024();
		addAttributeGroup0028();
		addAttributeGroup0032();
		addAttributeGroup0038();
		addAttributeGroup003A();
		addAttributeGroup0042();
		addAttributeGroup0044();
		addAttributeGroup0046();
		addAttributeGroup0048();
		addAttributeGroup0050();
		addAttributeGroup0052();
		addAttributeGroup0054();
		addAttributeGroup0060();
		addAttributeGroup0062();
		addAttributeGroup0064();
		addAttributeGroup0066();
		addAttributeGroup0068();
		addAttributeGroup0070();
		addAttributeGroup0072();
		addAttributeGroup0074();
		addAttributeGroup0076();
		addAttributeGroup0078();
		addAttributeGroup0080();
		addAttributeGroup0088();
		addAttributeGroup0100();
		addAttributeGroup0400();
		addAttributeGroup2000();
		addAttributeGroup2010();
		addAttributeGroup2020();
		addAttributeGroup2030();
		addAttributeGroup2040();
		addAttributeGroup2050();
		addAttributeGroup2100();
		addAttributeGroup2110();
		addAttributeGroup2120();
		addAttributeGroup2130();
		addAttributeGroup2200();
		addAttributeGroup3002();
		addAttributeGroup3004();
		addAttributeGroup3006();
		addAttributeGroup3008();
		addAttributeGroup300A();
		addAttributeGroup300C();
		addAttributeGroup300E();
		addAttributeGroup4000();
		addAttributeGroup4008();
		addAttributeGroup4010();
		addAttributeGroup4FFE();
		addAttributeGroup5200();
		addAttributeGroup5400();
		addAttributeGroup5600();
		addAttributeGroup7FE0();
		addAttributeGroupFFFA();
	}

	/**
	 * Adds attributes of group 0x0002.
	 */
	private void addAttributeGroup0002() {
		add(0x00020000, "File Meta Information Group Length", "UL");
		add(0x00020001, "File Meta Information Version", "OB");
		add(0x00020002, "Media Storage SOP Class UID", "UI");
		add(0x00020003, "Media Storage SOP Instance UID", "UI");
		add(0x00020010, "Transfer Syntax UID", "UI");
		add(0x00020012, "Implementation Class UID", "UI");
		add(0x00020013, "Implementation Version Name", "SH");
		add(0x00020016, "Source Application Entity Title", "AE");
		add(0x00020017, "Sending Application Entity Title", "AE");
		add(0x00020018, "Receiving Application Entity Title", "AE");
		add(0x00020100, "Private Information Creator UID", "UI");
		add(0x00020102, "Private Information", "OB");
	}

	/**
	 * Adds attributes of group 0x0008.
	 */
	private void addAttributeGroup0008() {
		add(0x00080001, "Length to End", "UL"); // Retired
		add(0x00080005, "Specific Character Set", "CS");
		add(0x00080006, "Language Code Sequence", "SQ");
		add(0x00080008, "Image Type", "CS");
		add(0x00080010, "Recognition Code", "SH"); // Retired
		add(0x00080012, "Instance Creation Date", "DA");
		add(0x00080013, "Instance Creation Time", "TM");
		add(0x00080014, "Instance Creator UID", "UI");
		add(0x00080015, "Instance Coercion DateTime", "DT");
		add(0x00080016, "SOP Class UID", "UI");
		add(0x00080018, "SOP Instance UID", "UI");
		add(0x0008001A, "Related General SOP Class UID", "UI");
		add(0x0008001B, "Original Specialized SOP ClassUID", "UI");
		add(0x00080020, "Study Date", "DA");
		add(0x00080021, "Series Date", "DA");
		add(0x00080022, "Acquisition Date", "DA");
		add(0x00080023, "Content Date", "DA");
		add(0x00080024, "Overlay Date", "DA"); // Retired
		add(0x00080025, "Curve Date", "DA"); // Retired
		add(0x0008002A, "Acquisition DateTime", "DT");
		add(0x00080030, "Study Time", "TM");
		add(0x00080031, "Series Time", "TM");
		add(0x00080032, "Acquisition Time", "TM");
		add(0x00080033, "Content Time", "TM");
		add(0x00080034, "Overlay Time", "TM"); // Retired
		add(0x00080035, "Curve Time", "TM"); // Retired
		add(0x00080040, "Data Set Type", "US"); // Retired
		add(0x00080041, "Data Set Subtype", "LO"); // Retired
		add(0x00080042, "Nuclear Medicine Series Type", "CS"); // Retired
		add(0x00080050, "Accession Number", "SH");
		add(0x00080051, "Issuer of Accession NumberSequence", "SQ");
		add(0x00080052, "Query/Retrieve Level", "CS");
		add(0x00080053, "Query/Retrieve View", "CS");
		add(0x00080054, "Retrieve AE Title", "AE");
		add(0x00080056, "Instance Availability", "CS");
		add(0x00080058, "Failed SOP Instance UID List", "UI");
		add(0x00080060, "Modality", "CS");
		add(0x00080061, "Modalities in Study", "CS");
		add(0x00080062, "SOP Classes in Study", "UI");
		add(0x00080064, "Conversion Type", "CS");
		add(0x00080068, "Presentation Intent Type", "CS");
		add(0x00080070, "Manufacturer", "LO");
		add(0x00080080, "Institution Name", "LO");
		add(0x00080081, "Institution Address", "ST");
		add(0x00080082, "Institution Code Sequence", "SQ");
		add(0x00080090, "Referring Physician's Name", "PN");
		add(0x00080092, "Referring Physician's Address", "ST");
		add(0x00080094, "Referring Physician's TelephoneNumbers", "SH");
		add(0x00080096, "Referring Physician IdentificationSequence", "SQ");
		add(0x0008009C, "Consulting Physician's Name", "PN");
		add(0x0008009D, "Consulting Physician IdentificationSequence", "SQ");
		add(0x00080100, "Code Value", "SH");
		add(0x00080101, "Extended Code Value", "LO"); // DICOS
		add(0x00080102, "Coding Scheme Designator", "SH");
		add(0x00080103, "Coding Scheme Version", "SH");
		add(0x00080104, "Code Meaning", "LO");
		add(0x00080105, "Mapping Resource", "CS");
		add(0x00080106, "Context Group Version", "DT");
		add(0x00080107, "Context Group Local Version", "DT");
		add(0x00080108, "Extended Code Meaning", "LT"); // DICOS
		add(0x0008010B, "Context Group Extension Flag", "CS");
		add(0x0008010C, "Coding Scheme UID", "UI");
		add(0x0008010D, "Context Group Extension CreatorUID", "UI");
		add(0x0008010F, "Context Identifier", "CS");
		add(0x00080110, "Coding Scheme IdentificationSequence", "SQ");
		add(0x00080112, "Coding Scheme Registry", "LO");
		add(0x00080114, "Coding Scheme External ID", "ST");
		add(0x00080115, "Coding Scheme Name", "ST");
		add(0x00080116, "Coding Scheme ResponsibleOrganization", "ST");
		add(0x00080117, "Context UID", "UI");
		add(0x00080118, "Mapping Resource UID", "UI");
		add(0x00080119, "Long Code Value", "UC");
		add(0x00080120, "URN Code Value", "UR");
		add(0x00080121, "Equivalent Code Sequence", "SQ");
		add(0x00080201, "Timezone Offset From UTC", "SH");
		add(0x00080300, "Private Data ElementCharacteristics Sequence", "SQ");
		add(0x00080301, "Private Group Reference", "US");
		add(0x00080302, "Private Creator Reference", "LO");
		add(0x00080303, "Block Identifying Information Status", "CS");
		add(0x00080304, "Nonidentifying Private Elements", "US");
		add(0x00080306, "Identifying Private Elements", "US");
		add(0x00080305, "Deidentification Action Sequence", "SQ");
		add(0x00080307, "Deidentification Action", "CS");
		add(0x00081000, "Network ID", "AE"); // Retired
		add(0x00081010, "Station Name", "SH");
		add(0x00081030, "Study Description", "LO");
		add(0x00081032, "Procedure Code Sequence", "SQ");
		add(0x0008103E, "Series Description", "LO");
		add(0x0008103F, "Series Description Code Sequence", "SQ");
		add(0x00081040, "Institutional Department Name", "LO");
		add(0x00081048, "Physician(s) of Record", "PN");
		add(0x00081049, "Physician(s) of RecordIdentification Sequence", "SQ");
		add(0x00081050, "Performing Physician's Name", "PN");
		add(0x00081052, "Performing Physician IdentificationSequence", "SQ");
		add(0x00081060, "Name of Physician(s) ReadingStudy", "PN");
		add(0x00081062, "Physician(s) Reading StudyIdentification Sequence", "SQ");
		add(0x00081070, "Operators' Name", "PN");
		add(0x00081072, "Operator Identification Sequence", "SQ");
		add(0x00081080, "Admitting Diagnoses Description", "LO");
		add(0x00081084, "Admitting Diagnoses CodeSequence", "SQ");
		add(0x00081090, "Manufacturer's Model Name", "LO");
		add(0x00081100, "Referenced Results Sequence", "SQ"); // Retired
		add(0x00081110, "Referenced Study Sequence", "SQ");
		add(0x00081111, "Referenced Performed ProcedureStep Sequence", "SQ");
		add(0x00081115, "Referenced Series Sequence", "SQ");
		add(0x00081120, "Referenced Patient Sequence", "SQ");
		add(0x00081125, "Referenced Visit Sequence", "SQ");
		add(0x00081130, "Referenced Overlay Sequence", "SQ"); // Retired
		add(0x00081134, "Referenced Stereometric InstanceSequence", "SQ");
		add(0x0008113A, "Referenced Waveform Sequence", "SQ");
		add(0x00081140, "Referenced Image Sequence", "SQ");
		add(0x00081145, "Referenced Curve Sequence", "SQ"); // Retired
		add(0x0008114A, "Referenced Instance Sequence", "SQ");
		add(0x0008114B, "Referenced Real World ValueMapping Instance Sequence",
			"SQ");
		add(0x00081150, "Referenced SOP Class UID", "UI");
		add(0x00081155, "Referenced SOP Instance UID", "UI");
		add(0x0008115A, "SOP Classes Supported", "UI");
		add(0x00081160, "Referenced Frame Number", "IS");
		add(0x00081161, "Simple Frame List", "UL");
		add(0x00081162, "Calculated Frame List", "UL");
		add(0x00081163, "Time Range", "FD");
		add(0x00081164, "Frame Extraction Sequence", "SQ");
		add(0x00081167, "Multi-frame Source SOP InstanceUID", "UI");
		add(0x00081190, "Retrieve URL", "UR");
		add(0x00081195, "Transaction UID", "UI");
		add(0x00081196, "Warning Reason", "US");
		add(0x00081197, "Failure Reason", "US");
		add(0x00081198, "Failed SOP Sequence", "SQ");
		add(0x00081199, "Referenced SOP Sequence", "SQ");
		add(0x00081200, "Studies Containing OtherReferenced Instances Sequence",
			"SQ");
		add(0x00081250, "Related Series Sequence", "SQ");
		add(0x00082110, "Lossy Image Compression(Retired)", "CS"); // Retired
		add(0x00082111, "Derivation Description", "ST");
		add(0x00082112, "Source Image Sequence", "SQ");
		add(0x00082120, "Stage Name", "SH");
		add(0x00082122, "Stage Number", "IS");
		add(0x00082124, "Number of Stages", "IS");
		add(0x00082127, "View Name", "SH");
		add(0x00082128, "View Number", "IS");
		add(0x00082129, "Number of Event Timers", "IS");
		add(0x0008212A, "Number of Views in Stage", "IS");
		add(0x00082130, "Event Elapsed Time(s)", "DS");
		add(0x00082132, "Event Timer Name(s)", "LO");
		add(0x00082133, "Event Timer Sequence", "SQ");
		add(0x00082134, "Event Time Offset", "FD");
		add(0x00082135, "Event Code Sequence", "SQ");
		add(0x00082142, "Start Trim", "IS");
		add(0x00082143, "Stop Trim", "IS");
		add(0x00082144, "Recommended Display FrameRate", "IS");
		add(0x00082200, "Transducer Position", "CS"); // Retired
		add(0x00082204, "Transducer Orientation", "CS"); // Retired
		add(0x00082208, "Anatomic Structure", "CS"); // Retired
		add(0x00082218, "Anatomic Region Sequence", "SQ");
		add(0x00082220, "Anatomic Region ModifierSequence", "SQ");
		add(0x00082228, "Primary Anatomic StructureSequence", "SQ");
		add(0x00082229, "Anatomic Structure, Space orRegion Sequence", "SQ");
		add(0x00082230, "Primary Anatomic StructureModifier Sequence", "SQ");
		add(0x00082240, "Transducer Position Sequence", "SQ"); // Retired
		add(0x00082242, "Transducer Position ModifierSequence", "SQ"); // Retired
		add(0x00082244, "Transducer Orientation Sequence", "SQ"); // Retired
		add(0x00082246, "Transducer Orientation ModifierSequence", "SQ"); // Retired
		add(0x00082251, "Anatomic Structure Space OrRegion Code Sequence (Trial)",
			"SQ"); // Retired
		add(0x00082253, "Anatomic Portal Of Entrance CodeSequence (Trial)", "SQ"); // Retired
		add(0x00082255, "Anatomic Approach Direction CodeSequence (Trial)", "SQ"); // Retired
		add(0x00082256, "Anatomic Perspective Description(Trial)", "ST"); // Retired
		add(0x00082257, "Anatomic Perspective CodeSequence (Trial)", "SQ"); // Retired
		add(0x00082258,
			"Anatomic Location Of ExaminingInstrument Description (Trial)", "ST"); // Retired
		add(0x00082259,
			"Anatomic Location Of ExaminingInstrument Code Sequence (Trial)", "SQ"); // Retired
		add(0x0008225A,
			"Anatomic Structure Space OrRegion Modifier Code Sequence(Trial)", "SQ"); // Retired
		add(0x0008225C,
			"On Axis Background AnatomicStructure Code Sequence (Trial)", "SQ"); // Retired
		add(0x00083001, "Alternate RepresentationSequence", "SQ");
		add(0x00083010, "Irradiation Event UID", "UI");
		add(0x00083011, "Source Irradiation Event Sequence", "SQ");
		add(0x00083012, "RadiopharmaceuticalAdministration Event UID", "UI");
		add(0x00084000, "Identifying Comments", "LT"); // Retired
		add(0x00089007, "Frame Type", "CS");
		add(0x00089092, "Referenced Image EvidenceSequence", "SQ");
		add(0x00089121, "Referenced Raw Data Sequence", "SQ");
		add(0x00089123, "Creator-Version UID", "UI");
		add(0x00089124, "Derivation Image Sequence", "SQ");
		add(0x00089154, "Source Image Evidence Sequence", "SQ");
		add(0x00089205, "Pixel Presentation", "CS");
		add(0x00089206, "Volumetric Properties", "CS");
		add(0x00089207, "Volume Based CalculationTechnique", "CS");
		add(0x00089208, "Complex Image Component", "CS");
		add(0x00089209, "Acquisition Contrast", "CS");
		add(0x00089215, "Derivation Code Sequence", "SQ");
		add(0x00089237, "Referenced Presentation StateSequence", "SQ");
		add(0x00089410, "Referenced Other Plane Sequence", "SQ");
		add(0x00089458, "Frame Display Sequence", "SQ");
		add(0x00089459, "Recommended Display FrameRate in Float", "FL");
		add(0x00089460, "Skip Frame Range Flag", "CS");
	}

	/**
	 * Adds attributes of group 0x0010.
	 */
	private void addAttributeGroup0010() {
		add(0x00100010, "Patient's Name", "PN");
		add(0x00100020, "Patient ID", "LO");
		add(0x00100021, "Issuer of Patient ID", "LO");
		add(0x00100022, "Type of Patient ID", "CS");
		add(0x00100024, "Issuer of Patient ID QualifiersSequence", "SQ");
		add(0x00100030, "Patient's Birth Date", "DA");
		add(0x00100032, "Patient's Birth Time", "TM");
		add(0x00100040, "Patient's Sex", "CS");
		add(0x00100050, "Patient's Insurance Plan CodeSequence", "SQ");
		add(0x00100101, "Patient's Primary Language CodeSequence", "SQ");
		add(0x00100102, "Patient's Primary LanguageModifier Code Sequence", "SQ");
		add(0x00100200, "Quality Control Subject", "CS");
		add(0x00100201, "Quality Control Subject Type CodeSequence", "SQ");
		add(0x00101000, "Other Patient IDs", "LO");
		add(0x00101001, "Other Patient Names", "PN");
		add(0x00101002, "Other Patient IDs Sequence", "SQ");
		add(0x00101005, "Patient's Birth Name", "PN");
		add(0x00101010, "Patient's Age", "AS");
		add(0x00101020, "Patient's Size", "DS");
		add(0x00101021, "Patient's Size Code Sequence", "SQ");
		add(0x00101030, "Patient's Weight", "DS");
		add(0x00101040, "Patient's Address", "LO");
		add(0x00101050, "Insurance Plan Identification", "LO"); // Retired
		add(0x00101060, "Patient's Mother's Birth Name", "PN");
		add(0x00101080, "Military Rank", "LO");
		add(0x00101081, "Branch of Service", "LO");
		add(0x00101090, "Medical Record Locator", "LO");
		add(0x00101100, "Referenced Patient PhotoSequence", "SQ");
		add(0x00102000, "Medical Alerts", "LO");
		add(0x00102110, "Allergies", "LO");
		add(0x00102150, "Country of Residence", "LO");
		add(0x00102152, "Region of Residence", "LO");
		add(0x00102154, "Patient's Telephone Numbers", "SH");
		add(0x00102155, "Patient's Telecom Information", "LT");
		add(0x00102160, "Ethnic Group", "SH");
		add(0x00102180, "Occupation", "SH");
		add(0x001021A0, "Smoking Status", "CS");
		add(0x001021B0, "Additional Patient History", "LT");
		add(0x001021C0, "Pregnancy Status", "US");
		add(0x001021D0, "Last Menstrual Date", "DA");
		add(0x001021F0, "Patient's Religious Preference", "LO");
		add(0x00102201, "Patient Species Description", "LO");
		add(0x00102202, "Patient Species Code Sequence", "SQ");
		add(0x00102203, "Patient's Sex Neutered", "CS");
		add(0x00102210, "Anatomical Orientation Type", "CS");
		add(0x00102292, "Patient Breed Description", "LO");
		add(0x00102293, "Patient Breed Code Sequence", "SQ");
		add(0x00102294, "Breed Registration Sequence", "SQ");
		add(0x00102295, "Breed Registration Number", "LO");
		add(0x00102296, "Breed Registry Code Sequence", "SQ");
		add(0x00102297, "Responsible Person", "PN");
		add(0x00102298, "Responsible Person Role", "CS");
		add(0x00102299, "Responsible Organization", "LO");
		add(0x00104000, "Patient Comments", "LT");
		add(0x00109431, "Examined Body Thickness", "FL");
	}

	/**
	 * Adds attributes of group 0x0012.
	 */
	private void addAttributeGroup0012() {
		add(0x00120010, "Clinical Trial Sponsor Name", "LO");
		add(0x00120020, "Clinical Trial Protocol ID", "LO");
		add(0x00120021, "Clinical Trial Protocol Name", "LO");
		add(0x00120030, "Clinical Trial Site ID", "LO");
		add(0x00120031, "Clinical Trial Site Name", "LO");
		add(0x00120040, "Clinical Trial Subject ID", "LO");
		add(0x00120042, "Clinical Trial Subject Reading ID", "LO");
		add(0x00120050, "Clinical Trial Time Point ID", "LO");
		add(0x00120051, "Clinical Trial Time Point Description", "ST");
		add(0x00120060, "Clinical Trial Coordinating CenterName", "LO");
		add(0x00120062, "Patient Identity Removed", "CS");
		add(0x00120063, "De-identification Method", "LO");
		add(0x00120064, "De-identification Method CodeSequence", "SQ");
		add(0x00120071, "Clinical Trial Series ID", "LO");
		add(0x00120072, "Clinical Trial Series Description", "LO");
		add(0x00120081, "Clinical Trial Protocol EthicsCommittee Name", "LO");
		add(0x00120082, "Clinical Trial Protocol EthicsCommittee Approval Number",
			"LO");
		add(0x00120083, "Consent for Clinical Trial UseSequence", "SQ");
		add(0x00120084, "Distribution Type", "CS");
		add(0x00120085, "Consent for Distribution Flag", "CS");
	}

	/**
	 * Adds attributes of group 0x0014.
	 */
	private void addAttributeGroup0014() {
		add(0x00140023, "CAD File Format", "ST"); // Retired
		add(0x00140024, "Component Reference System", "ST"); // Retired
		add(0x00140025, "Component ManufacturingProcedure", "ST"); // DICONDE
		add(0x00140028, "Component Manufacturer", "ST"); // DICONDE
		add(0x00140030, "Material Thickness", "DS"); // DICONDE
		add(0x00140032, "Material Pipe Diameter", "DS"); // DICONDE
		add(0x00140034, "Material Isolation Diameter", "DS"); // DICONDE
		add(0x00140042, "Material Grade", "ST"); // DICONDE
		add(0x00140044, "Material Properties Description", "ST"); // DICONDE
		add(0x00140045, "Material Properties File Format(Retired)", "ST"); // Retired
		add(0x00140046, "Material Notes", "LT"); // DICONDE
		add(0x00140050, "Component Shape", "CS"); // DICONDE
		add(0x00140052, "Curvature Type", "CS"); // DICONDE
		add(0x00140054, "Outer Diameter", "DS"); // DICONDE
		add(0x00140056, "Inner Diameter", "DS"); // DICONDE
		add(0x00141010, "Actual Environmental Conditions", "ST"); // DICONDE
		add(0x00141020, "Expiry Date", "DA"); // DICONDE
		add(0x00141040, "Environmental Conditions", "ST"); // DICONDE
		add(0x00142002, "Evaluator Sequence", "SQ"); // DICONDE
		add(0x00142004, "Evaluator Number", "IS"); // DICONDE
		add(0x00142006, "Evaluator Name", "PN"); // DICONDE
		add(0x00142008, "Evaluation Attempt", "IS"); // DICONDE
		add(0x00142012, "Indication Sequence", "SQ"); // DICONDE
		add(0x00142014, "Indication Number", "IS"); // DICONDE
		add(0x00142016, "Indication Label", "SH"); // DICONDE
		add(0x00142018, "Indication Description", "ST"); // DICONDE
		add(0x0014201A, "Indication Type", "CS"); // DICONDE
		add(0x0014201C, "Indication Disposition", "CS"); // DICONDE
		add(0x0014201E, "Indication ROI Sequence", "SQ"); // DICONDE
		add(0x00142030, "Indication Physical PropertySequence", "SQ"); // DICONDE
		add(0x00142032, "Property Label", "SH"); // DICONDE
		add(0x00142202, "Coordinate System Number ofAxes", "IS"); // DICONDE
		add(0x00142204, "Coordinate System Axes Sequence", "SQ"); // DICONDE
		add(0x00142206, "Coordinate System AxisDescription", "ST"); // DICONDE
		add(0x00142208, "Coordinate System Data SetMapping", "CS"); // DICONDE
		add(0x0014220A, "Coordinate System Axis Number", "IS"); // DICONDE
		add(0x0014220C, "Coordinate System Axis Type", "CS"); // DICONDE
		add(0x0014220E, "Coordinate System Axis Units", "CS"); // DICONDE
		add(0x00142210, "Coordinate System Axis Values", "OB"); // DICONDE
		add(0x00142220, "Coordinate System TransformSequence", "SQ"); // DICONDE
		add(0x00142222, "Transform Description", "ST"); // DICONDE
		add(0x00142224, "Transform Number of Axes", "IS"); // DICONDE
		add(0x00142226, "Transform Order of Axes", "IS"); // DICONDE
		add(0x00142228, "Transformed Axis Units", "CS"); // DICONDE
		add(0x0014222A, "Coordinate System TransformRotation and Scale Matrix",
			"DS"); // DICONDE
		add(0x0014222C, "Coordinate System TransformTranslation Matrix", "DS"); // DICONDE
		add(0x00143011, "Internal Detector Frame Time", "DS"); // DICONDE
		add(0x00143012, "Number of Frames Integrated", "DS"); // DICONDE
		add(0x00143020, "Detector Temperature Sequence", "SQ"); // DICONDE
		add(0x00143022, "Sensor Name", "ST"); // DICONDE
		add(0x00143024, "Horizontal Offset of Sensor", "DS"); // DICONDE
		add(0x00143026, "Vertical Offset of Sensor", "DS"); // DICONDE
		add(0x00143028, "Sensor Temperature", "DS"); // DICONDE
		add(0x00143040, "Dark Current Sequence", "SQ"); // DICONDE
		// add(0x00143050, "Dark Current Counts", "OB or OW"); //DICONDE
		add(0x00143060, "Gain Correction ReferenceSequence", "SQ"); // DICONDE
		// add(0x00143070, "Air Counts", "OB or OW"); //DICONDE
		add(0x00143071, "KV Used in Gain Calibration", "DS"); // DICONDE
		add(0x00143072, "MA Used in Gain Calibration", "DS"); // DICONDE
		add(0x00143073, "Number of Frames Used forIntegration", "DS"); // DICONDE
		add(0x00143074, "Filter Material Used in GainCalibration", "LO"); // DICONDE
		add(0x00143075, "Filter Thickness Used in GainCalibration", "DS"); // DICONDE
		add(0x00143076, "Date of Gain Calibration", "DA"); // DICONDE
		add(0x00143077, "Time of Gain Calibration", "TM"); // DICONDE
		add(0x00143080, "Bad Pixel Image", "OB"); // DICONDE
		add(0x00143099, "Calibration Notes", "LT"); // DICONDE
		add(0x00144002, "Pulser Equipment Sequence", "SQ"); // DICONDE
		add(0x00144004, "Pulser Type", "CS"); // DICONDE
		add(0x00144006, "Pulser Notes", "LT"); // DICONDE
		add(0x00144008, "Receiver Equipment Sequence", "SQ"); // DICONDE
		add(0x0014400A, "Amplifier Type", "CS"); // DICONDE
		add(0x0014400C, "Receiver Notes", "LT"); // DICONDE
		add(0x0014400E, "Pre-Amplifier Equipment Sequence", "SQ"); // DICONDE
		add(0x0014400F, "Pre-Amplifier Notes", "LT"); // DICONDE
		add(0x00144010, "Transmit Transducer Sequence", "SQ"); // DICONDE
		add(0x00144011, "Receive Transducer Sequence", "SQ"); // DICONDE
		add(0x00144012, "Number of Elements", "US"); // DICONDE
		add(0x00144013, "Element Shape", "CS"); // DICONDE
		add(0x00144014, "Element Dimension A", "DS"); // DICONDE
		add(0x00144015, "Element Dimension B", "DS"); // DICONDE
		add(0x00144016, "Element Pitch A", "DS"); // DICONDE
		add(0x00144017, "Measured Beam Dimension A", "DS"); // DICONDE
		add(0x00144018, "Measured Beam Dimension B", "DS"); // DICONDE
		add(0x00144019, "Location of Measured BeamDiameter", "DS"); // DICONDE
		add(0x0014401A, "Nominal Frequency", "DS"); // DICONDE
		add(0x0014401B, "Measured Center Frequency", "DS"); // DICONDE
		add(0x0014401C, "Measured Bandwidth", "DS"); // DICONDE
		add(0x0014401D, "Element Pitch B", "DS"); // DICONDE
		add(0x00144020, "Pulser Settings Sequence", "SQ"); // DICONDE
		add(0x00144022, "Pulse Width", "DS"); // DICONDE
		add(0x00144024, "Excitation Frequency", "DS"); // DICONDE
		add(0x00144026, "Modulation Type", "CS"); // DICONDE
		add(0x00144028, "Damping", "DS"); // DICONDE
		add(0x00144030, "Receiver Settings Sequence", "SQ"); // DICONDE
		add(0x00144031, "Acquired Soundpath Length", "DS"); // DICONDE
		add(0x00144032, "Acquisition Compression Type", "CS"); // DICONDE
		add(0x00144033, "Acquisition Sample Size", "IS"); // DICONDE
		add(0x00144034, "Rectifier Smoothing", "DS"); // DICONDE
		add(0x00144035, "DAC Sequence", "SQ"); // DICONDE
		add(0x00144036, "DAC Type", "CS"); // DICONDE
		add(0x00144038, "DAC Gain Points", "DS"); // DICONDE
		add(0x0014403A, "DAC Time Points", "DS"); // DICONDE
		add(0x0014403C, "DAC Amplitude", "DS"); // DICONDE
		add(0x00144040, "Pre-Amplifier Settings Sequence", "SQ"); // DICONDE
		add(0x00144050, "Transmit Transducer SettingsSequence", "SQ"); // DICONDE
		add(0x00144051, "Receive Transducer SettingsSequence", "SQ"); // DICONDE
		add(0x00144052, "Incident Angle", "DS"); // DICONDE
		add(0x00144054, "Coupling Technique", "ST"); // DICONDE
		add(0x00144056, "Coupling Medium", "ST"); // DICONDE
		add(0x00144057, "Coupling Velocity", "DS"); // DICONDE
		add(0x00144058, "Probe Center Location X", "DS"); // DICONDE
		add(0x00144059, "Probe Center Location Z", "DS"); // DICONDE
		add(0x0014405A, "Sound Path Length", "DS"); // DICONDE
		add(0x0014405C, "Delay Law Identifier", "ST"); // DICONDE
		add(0x00144060, "Gate Settings Sequence", "SQ"); // DICONDE
		add(0x00144062, "Gate Threshold", "DS"); // DICONDE
		add(0x00144064, "Velocity of Sound", "DS"); // DICONDE
		add(0x00144070, "Calibration Settings Sequence", "SQ"); // DICONDE
		add(0x00144072, "Calibration Procedure", "ST"); // DICONDE
		add(0x00144074, "Procedure Version", "SH"); // DICONDE
		add(0x00144076, "Procedure Creation Date", "DA"); // DICONDE
		add(0x00144078, "Procedure Expiration Date", "DA"); // DICONDE
		add(0x0014407A, "Procedure Last Modified Date", "DA"); // DICONDE
		add(0x0014407C, "Calibration Time", "TM"); // DICONDE
		add(0x0014407E, "Calibration Date", "DA"); // DICONDE
		add(0x00144080, "Probe Drive Equipment Sequence", "SQ"); // DICONDE
		add(0x00144081, "Drive Type", "CS"); // DICONDE
		add(0x00144082, "Probe Drive Notes", "LT"); // DICONDE
		add(0x00144083, "Drive Probe Sequence", "SQ"); // DICONDE
		add(0x00144084, "Probe Inductance", "DS"); // DICONDE
		add(0x00144085, "Probe Resistance", "DS"); // DICONDE
		add(0x00144086, "Receive Probe Sequence", "SQ"); // DICONDE
		add(0x00144087, "Probe Drive Settings Sequence", "SQ"); // DICONDE
		add(0x00144088, "Bridge Resistors", "DS"); // DICONDE
		add(0x00144089, "Probe Orientation Angle", "DS"); // DICONDE
		add(0x0014408B, "User Selected Gain Y", "DS"); // DICONDE
		add(0x0014408C, "User Selected Phase", "DS"); // DICONDE
		add(0x0014408D, "User Selected Offset X", "DS"); // DICONDE
		add(0x0014408E, "User Selected Offset Y", "DS"); // DICONDE
		add(0x00144091, "Channel Settings Sequence", "SQ"); // DICONDE
		add(0x00144092, "Channel Threshold", "DS"); // DICONDE
		add(0x0014409A, "Scanner Settings Sequence", "SQ"); // DICONDE
		add(0x0014409B, "Scan Procedure", "ST"); // DICONDE
		add(0x0014409C, "Translation Rate X", "DS"); // DICONDE
		add(0x0014409D, "Translation Rate Y", "DS"); // DICONDE
		add(0x0014409F, "Channel Overlap", "DS"); // DICONDE
		add(0x001440A0, "Image Quality Indicator Type", "LO"); // DICONDE
		add(0x001440A1, "Image Quality Indicator Material", "LO"); // DICONDE
		add(0x001440A2, "Image Quality Indicator Size", "LO"); // DICONDE
		add(0x00145002, "LINAC Energy", "IS"); // DICONDE
		add(0x00145004, "LINAC Output", "IS"); // DICONDE
		add(0x00145100, "Active Aperture", "US"); // DICONDE
		add(0x00145101, "Total Aperture", "DS"); // DICONDE
		add(0x00145102, "Aperture Elevation", "DS"); // DICONDE
		add(0x00145103, "Main Lobe Angle", "DS"); // DICONDE
		add(0x00145104, "Main Roof Angle", "DS"); // DICONDE
		add(0x00145105, "Connector Type", "CS"); // DICONDE
		add(0x00145106, "Wedge Model Number", "SH"); // DICONDE
		add(0x00145107, "Wedge Angle Float", "DS"); // DICONDE
		add(0x00145108, "Wedge Roof Angle", "DS"); // DICONDE
		add(0x00145109, "Wedge Element 1 Position", "CS"); // DICONDE
		add(0x0014510A, "Wedge Material Velocity", "DS"); // DICONDE
		add(0x0014510B, "Wedge Material", "SH"); // DICONDE
		add(0x0014510C, "Wedge Offset Z", "DS"); // DICONDE
		add(0x0014510D, "Wedge Origin Offset X", "DS"); // DICONDE
		add(0x0014510E, "Wedge Time Delay", "DS"); // DICONDE
		add(0x0014510F, "Wedge Name", "SH"); // DICONDE
		add(0x00145110, "Wedge Manufacturer Name", "SH"); // DICONDE
		add(0x00145111, "Wedge Description", "LO"); // DICONDE
		add(0x00145112, "Nominal Beam Angle", "DS"); // DICONDE
		add(0x00145113, "Wedge Offset X", "DS"); // DICONDE
		add(0x00145114, "Wedge Offset Y", "DS"); // DICONDE
		add(0x00145115, "Wedge Total Length", "DS"); // DICONDE
		add(0x00145116, "Wedge In Contact Length", "DS"); // DICONDE
		add(0x00145117, "Wedge Front Gap", "DS"); // DICONDE
		add(0x00145118, "Wedge Total Height", "DS"); // DICONDE
		add(0x00145119, "Wedge Front Height", "DS"); // DICONDE
		add(0x0014511A, "Wedge Rear Height", "DS"); // DICONDE
		add(0x0014511B, "Wedge Total Width", "DS"); // DICONDE
		add(0x0014511C, "Wedge In Contact Width", "DS"); // DICONDE
		add(0x0014511D, "Wedge Chamfer Height", "DS"); // DICONDE
		add(0x0014511E, "Wedge Curve", "CS"); // DICONDE
		add(0x0014511F, "Radius Along the Wedge", "DS"); // DICONDE
	}

	/**
	 * Adds attributes of group 0x0018.
	 */
	private void addAttributeGroup0018() {
		add(0x00180010, "Contrast/Bolus Agent", "LO");
		add(0x00180012, "Contrast/Bolus Agent Sequence", "SQ");
		add(0x00180013, "Contrast/Bolus T1 Relaxivity", "FL");
		add(0x00180014, "Contrast/Bolus AdministrationRoute Sequence", "SQ");
		add(0x00180015, "Body Part Examined", "CS");
		add(0x00180020, "Scanning Sequence", "CS");
		add(0x00180021, "Sequence Variant", "CS");
		add(0x00180022, "Scan Options", "CS");
		add(0x00180023, "MR Acquisition Type", "CS");
		add(0x00180024, "Sequence Name", "SH");
		add(0x00180025, "Angio Flag", "CS");
		add(0x00180026, "Intervention Drug InformationSequence", "SQ");
		add(0x00180027, "Intervention Drug Stop Time", "TM");
		add(0x00180028, "Intervention Drug Dose", "DS");
		add(0x00180029, "Intervention Drug Code Sequence", "SQ");
		add(0x0018002A, "Additional Drug Sequence", "SQ");
		add(0x00180030, "Radionuclide", "LO"); // Retired
		add(0x00180031, "Radiopharmaceutical", "LO");
		add(0x00180032, "Energy Window Centerline", "DS"); // Retired
		add(0x00180033, "Energy Window Total Width", "DS"); // Retired
		add(0x00180034, "Intervention Drug Name", "LO");
		add(0x00180035, "Intervention Drug Start Time", "TM");
		add(0x00180036, "Intervention Sequence", "SQ");
		add(0x00180037, "Therapy Type", "CS"); // Retired
		add(0x00180038, "Intervention Status", "CS");
		add(0x00180039, "Therapy Description", "CS"); // Retired
		add(0x0018003A, "Intervention Description", "ST");
		add(0x00180040, "Cine Rate", "IS");
		add(0x00180042, "Initial Cine Run State", "CS");
		add(0x00180050, "Slice Thickness", "DS");
		add(0x00180060, "KVP", "DS");
		add(0x00180070, "Counts Accumulated", "IS");
		add(0x00180071, "Acquisition Termination Condition", "CS");
		add(0x00180072, "Effective Duration", "DS");
		add(0x00180073, "Acquisition Start Condition", "CS");
		add(0x00180074, "Acquisition Start Condition Data", "IS");
		add(0x00180075, "Acquisition Termination ConditionData", "IS");
		add(0x00180080, "Repetition Time", "DS");
		add(0x00180081, "Echo Time", "DS");
		add(0x00180082, "Inversion Time", "DS");
		add(0x00180083, "Number of Averages", "DS");
		add(0x00180084, "Imaging Frequency", "DS");
		add(0x00180085, "Imaged Nucleus", "SH");
		add(0x00180086, "Echo Number(s)", "IS");
		add(0x00180087, "Magnetic Field Strength", "DS");
		add(0x00180088, "Spacing Between Slices", "DS");
		add(0x00180089, "Number of Phase Encoding Steps", "IS");
		add(0x00180090, "Data Collection Diameter", "DS");
		add(0x00180091, "Echo Train Length", "IS");
		add(0x00180093, "Percent Sampling", "DS");
		add(0x00180094, "Percent Phase Field of View", "DS");
		add(0x00180095, "Pixel Bandwidth", "DS");
		add(0x00181000, "Device Serial Number", "LO");
		add(0x00181002, "Device UID", "UI");
		add(0x00181003, "Device ID", "LO");
		add(0x00181004, "Plate ID", "LO");
		add(0x00181005, "Generator ID", "LO");
		add(0x00181006, "Grid ID", "LO");
		add(0x00181007, "Cassette ID", "LO");
		add(0x00181008, "Gantry ID", "LO");
		add(0x00181010, "Secondary Capture Device ID", "LO");
		add(0x00181011, "Hardcopy Creation Device ID", "LO"); // Retired
		add(0x00181012, "Date of Secondary Capture", "DA");
		add(0x00181014, "Time of Secondary Capture", "TM");
		add(0x00181016, "Secondary Capture DeviceManufacturer", "LO");
		add(0x00181017, "Hardcopy Device Manufacturer", "LO"); // Retired
		add(0x00181018, "Secondary Capture DeviceManufacturer's Model Name", "LO");
		add(0x00181019, "Secondary Capture DeviceSoftware Versions", "LO");
		add(0x0018101A, "Hardcopy Device Software Version", "LO"); // Retired
		add(0x0018101B, "Hardcopy Device Manufacturer'sModel Name", "LO"); // Retired
		add(0x00181020, "Software Version(s)", "LO");
		add(0x00181022, "Video Image Format Acquired", "SH");
		add(0x00181023, "Digital Image Format Acquired", "LO");
		add(0x00181030, "Protocol Name", "LO");
		add(0x00181040, "Contrast/Bolus Route", "LO");
		add(0x00181041, "Contrast/Bolus Volume", "DS");
		add(0x00181042, "Contrast/Bolus Start Time", "TM");
		add(0x00181043, "Contrast/Bolus Stop Time", "TM");
		add(0x00181044, "Contrast/Bolus Total Dose", "DS");
		add(0x00181045, "Syringe Counts", "IS");
		add(0x00181046, "Contrast Flow Rate", "DS");
		add(0x00181047, "Contrast Flow Duration", "DS");
		add(0x00181048, "Contrast/Bolus Ingredient", "CS");
		add(0x00181049, "Contrast/Bolus IngredientConcentration", "DS");
		add(0x00181050, "Spatial Resolution", "DS");
		add(0x00181060, "Trigger Time", "DS");
		// add(0x00181061, "Trigger Source or Type", "LO");
		add(0x00181062, "Nominal Interval", "IS");
		add(0x00181063, "Frame Time", "DS");
		add(0x00181064, "Cardiac Framing Type", "LO");
		add(0x00181065, "Frame Time Vector", "DS");
		add(0x00181066, "Frame Delay", "DS");
		add(0x00181067, "Image Trigger Delay", "DS");
		add(0x00181068, "Multiplex Group Time Offset", "DS");
		add(0x00181069, "Trigger Time Offset", "DS");
		add(0x0018106A, "Synchronization Trigger", "CS");
		add(0x0018106C, "Synchronization Channel", "US");
		add(0x0018106E, "Trigger Sample Position", "UL");
		add(0x00181070, "Radiopharmaceutical Route", "LO");
		add(0x00181071, "Radiopharmaceutical Volume", "DS");
		add(0x00181072, "Radiopharmaceutical Start Time", "TM");
		add(0x00181073, "Radiopharmaceutical Stop Time", "TM");
		add(0x00181074, "Radionuclide Total Dose", "DS");
		add(0x00181075, "Radionuclide Half Life", "DS");
		add(0x00181076, "Radionuclide Positron Fraction", "DS");
		add(0x00181077, "Radiopharmaceutical SpecificActivity", "DS");
		add(0x00181078, "Radiopharmaceutical StartDateTime", "DT");
		add(0x00181079, "Radiopharmaceutical StopDateTime", "DT");
		add(0x00181080, "Beat Rejection Flag", "CS");
		add(0x00181081, "Low R-R Value", "IS");
		add(0x00181082, "High R-R Value", "IS");
		add(0x00181083, "Intervals Acquired", "IS");
		add(0x00181084, "Intervals Rejected", "IS");
		add(0x00181085, "PVC Rejection", "LO");
		add(0x00181086, "Skip Beats", "IS");
		add(0x00181088, "Heart Rate", "IS");
		add(0x00181090, "Cardiac Number of Images", "IS");
		add(0x00181094, "Trigger Window", "IS");
		add(0x00181100, "Reconstruction Diameter", "DS");
		add(0x00181110, "Distance Source to Detector", "DS");
		add(0x00181111, "Distance Source to Patient", "DS");
		add(0x00181114, "Estimated RadiographicMagnification Factor", "DS");
		add(0x00181120, "Gantry/Detector Tilt", "DS");
		add(0x00181121, "Gantry/Detector Slew", "DS");
		add(0x00181130, "Table Height", "DS");
		add(0x00181131, "Table Traverse", "DS");
		add(0x00181134, "Table Motion", "CS");
		add(0x00181135, "Table Vertical Increment", "DS");
		add(0x00181136, "Table Lateral Increment", "DS");
		add(0x00181137, "Table Longitudinal Increment", "DS");
		add(0x00181138, "Table Angle", "DS");
		add(0x0018113A, "Table Type", "CS");
		add(0x00181140, "Rotation Direction", "CS");
		add(0x00181141, "Angular Position", "DS"); // Retired
		add(0x00181142, "Radial Position", "DS");
		add(0x00181143, "Scan Arc", "DS");
		add(0x00181144, "Angular Step", "DS");
		add(0x00181145, "Center of Rotation Offset", "DS");
		add(0x00181146, "Rotation Offset", "DS"); // Retired
		add(0x00181147, "Field of View Shape", "CS");
		add(0x00181149, "Field of View Dimension(s)", "IS");
		add(0x00181150, "Exposure Time", "IS");
		add(0x00181151, "X-Ray Tube Current", "IS");
		add(0x00181152, "Exposure", "IS");
		add(0x00181153, "Exposure in uAs", "IS");
		add(0x00181154, "Average Pulse Width", "DS");
		add(0x00181155, "Radiation Setting", "CS");
		add(0x00181156, "Rectification Type", "CS");
		add(0x0018115A, "Radiation Mode", "CS");
		add(0x0018115E, "Image and Fluoroscopy Area DoseProduct", "DS");
		add(0x00181160, "Filter Type", "SH");
		add(0x00181161, "Type of Filters", "LO");
		add(0x00181162, "Intensifier Size", "DS");
		add(0x00181164, "Imager Pixel Spacing", "DS");
		add(0x00181166, "Grid", "CS");
		add(0x00181170, "Generator Power", "IS");
		add(0x00181180, "Collimator/grid Name", "SH");
		add(0x00181181, "Collimator Type", "CS");
		add(0x00181182, "Focal Distance", "IS");
		add(0x00181183, "X Focus Center", "DS");
		add(0x00181184, "Y Focus Center", "DS");
		add(0x00181190, "Focal Spot(s)", "DS");
		add(0x00181191, "Anode Target Material", "CS");
		add(0x001811A0, "Body Part Thickness", "DS");
		add(0x001811A2, "Compression Force", "DS");
		add(0x001811A4, "Paddle Description", "LO");
		add(0x00181200, "Date of Last Calibration", "DA");
		add(0x00181201, "Time of Last Calibration", "TM");
		add(0x00181202, "DateTime of Last Calibration", "DT");
		add(0x00181210, "Convolution Kernel", "SH");
		add(0x00181240, "Upper/Lower Pixel Values", "IS"); // Retired
		add(0x00181242, "Actual Frame Duration", "IS");
		add(0x00181243, "Count Rate", "IS");
		add(0x00181244, "Preferred Playback Sequencing", "US");
		add(0x00181250, "Receive Coil Name", "SH");
		add(0x00181251, "Transmit Coil Name", "SH");
		add(0x00181260, "Plate Type", "SH");
		add(0x00181261, "Phosphor Type", "LO");
		add(0x00181300, "Scan Velocity", "DS");
		add(0x00181301, "Whole Body Technique", "CS");
		add(0x00181302, "Scan Length", "IS");
		add(0x00181310, "Acquisition Matrix", "US");
		add(0x00181312, "In-plane Phase Encoding Direction", "CS");
		add(0x00181314, "Flip Angle", "DS");
		add(0x00181315, "Variable Flip Angle Flag", "CS");
		add(0x00181316, "SAR", "DS");
		add(0x00181318, "dB/dt", "DS");
		add(0x00181400, "Acquisition Device ProcessingDescription", "LO");
		add(0x00181401, "Acquisition Device ProcessingCode", "LO");
		add(0x00181402, "Cassette Orientation", "CS");
		add(0x00181403, "Cassette Size", "CS");
		add(0x00181404, "Exposures on Plate", "US");
		add(0x00181405, "Relative X-Ray Exposure", "IS");
		add(0x00181411, "Exposure Index", "DS");
		add(0x00181412, "Target Exposure Index", "DS");
		add(0x00181413, "Deviation Index", "DS");
		add(0x00181450, "Column Angulation", "DS");
		add(0x00181460, "Tomo Layer Height", "DS");
		add(0x00181470, "Tomo Angle", "DS");
		add(0x00181480, "Tomo Time", "DS");
		add(0x00181490, "Tomo Type", "CS");
		add(0x00181491, "Tomo Class", "CS");
		add(0x00181495, "Number of Tomosynthesis SourceImages", "IS");
		add(0x00181500, "Positioner Motion", "CS");
		add(0x00181508, "Positioner Type", "CS");
		add(0x00181510, "Positioner Primary Angle", "DS");
		add(0x00181511, "Positioner Secondary Angle", "DS");
		add(0x00181520, "Positioner Primary Angle Increment", "DS");
		add(0x00181521, "Positioner Secondary AngleIncrement", "DS");
		add(0x00181530, "Detector Primary Angle", "DS");
		add(0x00181531, "Detector Secondary Angle", "DS");
		add(0x00181600, "Shutter Shape", "CS");
		add(0x00181602, "Shutter Left Vertical Edge", "IS");
		add(0x00181604, "Shutter Right Vertical Edge", "IS");
		add(0x00181606, "Shutter Upper Horizontal Edge", "IS");
		add(0x00181608, "Shutter Lower Horizontal Edge", "IS");
		add(0x00181610, "Center of Circular Shutter", "IS");
		add(0x00181612, "Radius of Circular Shutter", "IS");
		add(0x00181620, "Vertices of the Polygonal Shutter", "IS");
		add(0x00181622, "Shutter Presentation Value", "US");
		add(0x00181623, "Shutter Overlay Group", "US");
		add(0x00181624, "Shutter Presentation Color CIELabValue", "US");
		add(0x00181700, "Collimator Shape", "CS");
		add(0x00181702, "Collimator Left Vertical Edge", "IS");
		add(0x00181704, "Collimator Right Vertical Edge", "IS");
		add(0x00181706, "Collimator Upper Horizontal Edge", "IS");
		add(0x00181708, "Collimator Lower Horizontal Edge", "IS");
		add(0x00181710, "Center of Circular Collimator", "IS");
		add(0x00181712, "Radius of Circular Collimator", "IS");
		add(0x00181720, "Vertices of the PolygonalCollimator", "IS");
		add(0x00181800, "Acquisition Time Synchronized", "CS");
		add(0x00181801, "Time Source", "SH");
		add(0x00181802, "Time Distribution Protocol", "CS");
		add(0x00181803, "NTP Source Address", "LO");
		add(0x00182001, "Page Number Vector", "IS");
		add(0x00182002, "Frame Label Vector", "SH");
		add(0x00182003, "Frame Primary Angle Vector", "DS");
		add(0x00182004, "Frame Secondary Angle Vector", "DS");
		add(0x00182005, "Slice Location Vector", "DS");
		add(0x00182006, "Display Window Label Vector", "SH");
		add(0x00182010, "Nominal Scanned Pixel Spacing", "DS");
		add(0x00182020, "Digitizing Device TransportDirection", "CS");
		add(0x00182030, "Rotation of Scanned Film", "DS");
		add(0x00182041, "Biopsy Target Sequence", "SQ");
		add(0x00182042, "Target UID", "UI");
		add(0x00182043, "Localizing Cursor Position", "FL");
		add(0x00182044, "Calculated Target Position", "FL");
		add(0x00182045, "Target Label", "SH");
		add(0x00182046, "Displayed Z Value", "FL");
		add(0x00183100, "IVUS Acquisition", "CS");
		add(0x00183101, "IVUS Pullback Rate", "DS");
		add(0x00183102, "IVUS Gated Rate", "DS");
		add(0x00183103, "IVUS Pullback Start Frame Number", "IS");
		add(0x00183104, "IVUS Pullback Stop Frame Number", "IS");
		add(0x00183105, "Lesion Number", "IS");
		add(0x00184000, "Acquisition Comments", "LT"); // Retired
		add(0x00185000, "Output Power", "SH");
		add(0x00185010, "Transducer Data", "LO");
		add(0x00185012, "Focus Depth", "DS");
		add(0x00185020, "Processing Function", "LO");
		add(0x00185021, "Postprocessing Function", "LO"); // Retired
		add(0x00185022, "Mechanical Index", "DS");
		add(0x00185024, "Bone Thermal Index", "DS");
		add(0x00185026, "Cranial Thermal Index", "DS");
		add(0x00185027, "Soft Tissue Thermal Index", "DS");
		add(0x00185028, "Soft Tissue-focus Thermal Index", "DS");
		add(0x00185029, "Soft Tissue-surface Thermal Index", "DS");
		add(0x00185030, "Dynamic Range", "DS"); // Retired
		add(0x00185040, "Total Gain", "DS"); // Retired
		add(0x00185050, "Depth of Scan Field", "IS");
		add(0x00185100, "Patient Position", "CS");
		add(0x00185101, "View Position", "CS");
		add(0x00185104, "Projection Eponymous Name CodeSequence", "SQ");
		add(0x00185210, "Image Transformation Matrix", "DS"); // Retired
		add(0x00185212, "Image Translation Vector", "DS"); // Retired
		add(0x00186000, "Sensitivity", "DS");
		add(0x00186011, "Sequence of Ultrasound Regions", "SQ");
		add(0x00186012, "Region Spatial Format", "US");
		add(0x00186014, "Region Data Type", "US");
		add(0x00186016, "Region Flags", "UL");
		add(0x00186018, "Region Location Min X0", "UL");
		add(0x0018601A, "Region Location Min Y0", "UL");
		add(0x0018601C, "Region Location Max X1", "UL");
		add(0x0018601E, "Region Location Max Y1", "UL");
		add(0x00186020, "Reference Pixel X0", "SL");
		add(0x00186022, "Reference Pixel Y0", "SL");
		add(0x00186024, "Physical Units X Direction", "US");
		add(0x00186026, "Physical Units Y Direction", "US");
		add(0x00186028, "Reference Pixel Physical Value X", "FD");
		add(0x0018602A, "Reference Pixel Physical Value Y", "FD");
		add(0x0018602C, "Physical Delta X", "FD");
		add(0x0018602E, "Physical Delta Y", "FD");
		add(0x00186030, "Transducer Frequency", "UL");
		add(0x00186031, "Transducer Type", "CS");
		add(0x00186032, "Pulse Repetition Frequency", "UL");
		add(0x00186034, "Doppler Correction Angle", "FD");
		add(0x00186036, "Steering Angle", "FD");
		add(0x00186038, "Doppler Sample Volume X Position(Retired)", "UL"); // Retired
		add(0x00186039, "Doppler Sample Volume X Position", "SL");
		add(0x0018603A, "Doppler Sample Volume Y Position(Retired)", "UL"); // Retired
		add(0x0018603B, "Doppler Sample Volume Y Position", "SL");
		add(0x0018603C, "TM-Line Position X0 (Retired)", "UL"); // Retired
		add(0x0018603D, "TM-Line Position X0", "SL");
		add(0x0018603E, "TM-Line Position Y0 (Retired)", "UL"); // Retired
		add(0x0018603F, "TM-Line Position Y0", "SL");
		add(0x00186040, "TM-Line Position X1 (Retired)", "UL"); // Retired
		add(0x00186041, "TM-Line Position X1", "SL");
		add(0x00186042, "TM-Line Position Y1 (Retired)", "UL"); // Retired
		add(0x00186043, "TM-Line Position Y1", "SL");
		add(0x00186044, "Pixel Component Organization", "US");
		add(0x00186046, "Pixel Component Mask", "UL");
		add(0x00186048, "Pixel Component Range Start", "UL");
		add(0x0018604A, "Pixel Component Range Stop", "UL");
		add(0x0018604C, "Pixel Component Physical Units", "US");
		add(0x0018604E, "Pixel Component Data Type", "US");
		add(0x00186050, "Number of Table Break Points", "UL");
		add(0x00186052, "Table of X Break Points", "UL");
		add(0x00186054, "Table of Y Break Points", "FD");
		add(0x00186056, "Number of Table Entries", "UL");
		add(0x00186058, "Table of Pixel Values", "UL");
		add(0x0018605A, "Table of Parameter Values", "FL");
		add(0x00186060, "R Wave Time Vector", "FL");
		add(0x00187000, "Detector Conditions Nominal Flag", "CS");
		add(0x00187001, "Detector Temperature", "DS");
		add(0x00187004, "Detector Type", "CS");
		add(0x00187005, "Detector Configuration", "CS");
		add(0x00187006, "Detector Description", "LT");
		add(0x00187008, "Detector Mode", "LT");
		add(0x0018700A, "Detector ID", "SH");
		add(0x0018700C, "Date of Last Detector Calibration", "DA");
		add(0x0018700E, "Time of Last Detector Calibration", "TM");
		add(0x00187010, "Exposures on Detector Since LastCalibration", "IS");
		add(0x00187011, "Exposures on Detector SinceManufactured", "IS");
		add(0x00187012, "Detector Time Since Last Exposure", "DS");
		add(0x00187014, "Detector Active Time", "DS");
		add(0x00187016, "Detector Activation Offset FromExposure", "DS");
		add(0x0018701A, "Detector Binning", "DS");
		add(0x00187020, "Detector Element Physical Size", "DS");
		add(0x00187022, "Detector Element Spacing", "DS");
		add(0x00187024, "Detector Active Shape", "CS");
		add(0x00187026, "Detector Active Dimension(s)", "DS");
		add(0x00187028, "Detector Active Origin", "DS");
		add(0x0018702A,
			"Detector Manufacturer Name  DetectorManufacturerName  LO  1 (0018,702B) Detector Manufacturer's ModelName",
			"LO");
		add(0x00187030, "Field of View Origin", "DS");
		add(0x00187032, "Field of View Rotation", "DS");
		add(0x00187034, "Field of View Horizontal Flip", "CS");
		add(0x00187036, "Pixel Data Area Origin Relative ToFOV", "FL");
		add(0x00187038, "Pixel Data Area Rotation AngleRelative To FOV", "FL");
		add(0x00187040, "Grid Absorbing Material", "LT");
		add(0x00187041, "Grid Spacing Material", "LT");
		add(0x00187042, "Grid Thickness", "DS");
		add(0x00187044, "Grid Pitch", "DS");
		add(0x00187046, "Grid Aspect Ratio", "IS");
		add(0x00187048, "Grid Period", "DS");
		add(0x0018704C, "Grid Focal Distance", "DS");
		add(0x00187050, "Filter Material", "CS");
		add(0x00187052, "Filter Thickness Minimum", "DS");
		add(0x00187054, "Filter Thickness Maximum", "DS");
		add(0x00187056, "Filter Beam Path Length Minimum", "FL");
		add(0x00187058, "Filter Beam Path Length Maximum", "FL");
		add(0x00187060, "Exposure Control Mode", "CS");
		add(0x00187062, "Exposure Control Mode Description", "LT");
		add(0x00187064, "Exposure Status", "CS");
		add(0x00187065, "Phototimer Setting", "DS");
		add(0x00188150, "Exposure Time in uS", "DS");
		add(0x00188151, "X-Ray Tube Current in uA", "DS");
		add(0x00189004, "Content Qualification", "CS");
		add(0x00189005, "Pulse Sequence Name", "SH");
		add(0x00189006, "MR Imaging Modifier Sequence", "SQ");
		add(0x00189008, "Echo Pulse Sequence", "CS");
		add(0x00189009, "Inversion Recovery", "CS");
		add(0x00189010, "Flow Compensation", "CS");
		add(0x00189011, "Multiple Spin Echo", "CS");
		add(0x00189012, "Multi-planar Excitation", "CS");
		add(0x00189014, "Phase Contrast", "CS");
		add(0x00189015, "Time of Flight Contrast", "CS");
		add(0x00189016, "Spoiling", "CS");
		add(0x00189017, "Steady State Pulse Sequence", "CS");
		add(0x00189018, "Echo Planar Pulse Sequence", "CS");
		add(0x00189019, "Tag Angle First Axis", "FD");
		add(0x00189020, "Magnetization Transfer", "CS");
		add(0x00189021, "T2 Preparation", "CS");
		add(0x00189022, "Blood Signal Nulling", "CS");
		add(0x00189024, "Saturation Recovery", "CS");
		add(0x00189025, "Spectrally Selected Suppression", "CS");
		add(0x00189026, "Spectrally Selected Excitation", "CS");
		add(0x00189027, "Spatial Pre-saturation", "CS");
		add(0x00189028, "Tagging", "CS");
		add(0x00189029, "Oversampling Phase", "CS");
		add(0x00189030, "Tag Spacing First Dimension", "FD");
		add(0x00189032, "Geometry of k-Space Traversal", "CS");
		add(0x00189033, "Segmented k-Space Traversal", "CS");
		add(0x00189034, "Rectilinear Phase EncodeReordering", "CS");
		add(0x00189035, "Tag Thickness", "FD");
		add(0x00189036, "Partial Fourier Direction", "CS");
		add(0x00189037, "Cardiac Synchronization Technique", "CS");
		add(0x00189041, "Receive Coil Manufacturer Name", "LO");
		add(0x00189042, "MR Receive Coil Sequence", "SQ");
		add(0x00189043, "Receive Coil Type", "CS");
		add(0x00189044, "Quadrature Receive Coil", "CS");
		add(0x00189045, "Multi-Coil Definition Sequence", "SQ");
		add(0x00189046, "Multi-Coil Configuration", "LO");
		add(0x00189047, "Multi-Coil Element Name", "SH");
		add(0x00189048, "Multi-Coil Element Used", "CS");
		add(0x00189049, "MR Transmit Coil Sequence", "SQ");
		add(0x00189050, "Transmit Coil Manufacturer Name", "LO");
		add(0x00189051, "Transmit Coil Type", "CS");
		add(0x00189052, "Spectral Width", "FD");
		add(0x00189053, "Chemical Shift Reference", "FD");
		add(0x00189054, "Volume Localization Technique", "CS");
		add(0x00189058, "MR Acquisition FrequencyEncoding Steps", "US");
		add(0x00189059, "De-coupling", "CS");
		add(0x00189060, "De-coupled Nucleus", "CS");
		add(0x00189061, "De-coupling Frequency", "FD");
		add(0x00189062, "De-coupling Method", "CS");
		add(0x00189063, "De-coupling Chemical ShiftReference", "FD");
		add(0x00189064, "k-space Filtering", "CS");
		add(0x00189065, "Time Domain Filtering", "CS");
		add(0x00189066, "Number of Zero Fills", "US");
		add(0x00189067, "Baseline Correction", "CS");
		add(0x00189069, "Parallel Reduction Factor In-plane", "FD");
		add(0x00189070, "Cardiac R-R Interval Specified", "FD");
		add(0x00189073, "Acquisition Duration", "FD");
		add(0x00189074, "Frame Acquisition DateTime", "DT");
		add(0x00189075, "Diffusion Directionality", "CS");
		add(0x00189076, "Diffusion Gradient DirectionSequence", "SQ");
		add(0x00189077, "Parallel Acquisition", "CS");
		add(0x00189078, "Parallel Acquisition Technique", "CS");
		add(0x00189079, "Inversion Times", "FD");
		add(0x00189080, "Metabolite Map Description", "ST");
		add(0x00189081, "Partial Fourier", "CS");
		add(0x00189082, "Effective Echo Time", "FD");
		add(0x00189083, "Metabolite Map Code Sequence", "SQ");
		add(0x00189084, "Chemical Shift Sequence", "SQ");
		add(0x00189085, "Cardiac Signal Source", "CS");
		add(0x00189087, "Diffusion b-value", "FD");
		add(0x00189089, "Diffusion Gradient Orientation", "FD");
		add(0x00189090, "Velocity Encoding Direction", "FD");
		add(0x00189091, "Velocity Encoding Minimum Value", "FD");
		add(0x00189092, "Velocity Encoding AcquisitionSequence", "SQ");
		add(0x00189093, "Number of k-Space Trajectories", "US");
		add(0x00189094, "Coverage of k-Space", "CS");
		add(0x00189095, "Spectroscopy Acquisition PhaseRows", "UL");
		add(0x00189096, "Parallel Reduction Factor In-plane(Retired)", "FD"); // Retired
		add(0x00189098, "Transmitter Frequency", "FD");
		add(0x00189100, "Resonant Nucleus", "CS");
		add(0x00189101, "Frequency Correction", "CS");
		add(0x00189103, "MR Spectroscopy FOV/GeometrySequence", "SQ");
		add(0x00189104, "Slab Thickness", "FD");
		add(0x00189105, "Slab Orientation", "FD");
		add(0x00189106, "Mid Slab Position", "FD");
		add(0x00189107, "MR Spatial Saturation Sequence", "SQ");
		add(0x00189112, "MR Timing and RelatedParameters Sequence", "SQ");
		add(0x00189114, "MR Echo Sequence", "SQ");
		add(0x00189115, "MR Modifier Sequence", "SQ");
		add(0x00189117, "MR Diffusion Sequence", "SQ");
		add(0x00189118, "Cardiac Synchronization Sequence", "SQ");
		add(0x00189119, "MR Averages Sequence", "SQ");
		add(0x00189125, "MR FOV/Geometry Sequence", "SQ");
		add(0x00189126, "Volume Localization Sequence", "SQ");
		add(0x00189127, "Spectroscopy Acquisition DataColumns", "UL");
		add(0x00189147, "Diffusion Anisotropy Type", "CS");
		add(0x00189151, "Frame Reference DateTime", "DT");
		add(0x00189152, "MR Metabolite Map Sequence", "SQ");
		add(0x00189155, "Parallel Reduction Factorout-of-plane", "FD");
		add(0x00189159, "Spectroscopy AcquisitionOut-of-plane Phase Steps", "UL");
		add(0x00189166, "Bulk Motion Status", "CS"); // Retired
		add(0x00189168, "Parallel Reduction Factor SecondIn-plane", "FD");
		add(0x00189169, "Cardiac Beat Rejection Technique", "CS");
		add(0x00189170, "Respiratory Motion CompensationTechnique", "CS");
		add(0x00189171, "Respiratory Signal Source", "CS");
		add(0x00189172, "Bulk Motion CompensationTechnique", "CS");
		add(0x00189173, "Bulk Motion Signal Source", "CS");
		add(0x00189174, "Applicable Safety Standard Agency", "CS");
		add(0x00189175, "Applicable Safety StandardDescription", "LO");
		add(0x00189176, "Operating Mode Sequence", "SQ");
		add(0x00189177, "Operating Mode Type", "CS");
		add(0x00189178, "Operating Mode", "CS");
		add(0x00189179, "Specific Absorption Rate Definition", "CS");
		add(0x00189180, "Gradient Output Type", "CS");
		add(0x00189181, "Specific Absorption Rate Value", "FD");
		add(0x00189182, "Gradient Output", "FD");
		add(0x00189183, "Flow Compensation Direction", "CS");
		add(0x00189184, "Tagging Delay", "FD");
		add(0x00189185, "Respiratory Motion CompensationTechnique Description",
			"ST");
		add(0x00189186, "Respiratory Signal Source ID", "SH");
		add(0x00189195, "Chemical Shift Minimum IntegrationLimit in Hz", "FD"); // Retired
		add(0x00189196, "Chemical Shift MaximumIntegration Limit in Hz", "FD"); // Retired
		add(0x00189197, "MR Velocity Encoding Sequence", "SQ");
		add(0x00189198, "First Order Phase Correction", "CS");
		add(0x00189199, "Water Referenced PhaseCorrection", "CS");
		add(0x00189200, "MR Spectroscopy Acquisition Type", "CS");
		add(0x00189214, "Respiratory Cycle Position", "CS");
		add(0x00189217, "Velocity Encoding Maximum Value", "FD");
		add(0x00189218, "Tag Spacing Second Dimension", "FD");
		add(0x00189219, "Tag Angle Second Axis", "SS");
		add(0x00189220, "Frame Acquisition Duration", "FD");
		add(0x00189226, "MR Image Frame Type Sequence", "SQ");
		add(0x00189227, "MR Spectroscopy Frame TypeSequence", "SQ");
		add(0x00189231, "MR Acquisition Phase EncodingSteps in-plane", "US");
		add(0x00189232, "MR Acquisition Phase EncodingSteps out-of-plane", "US");
		add(0x00189234, "Spectroscopy Acquisition PhaseColumns", "UL");
		add(0x00189236, "Cardiac Cycle Position", "CS");
		add(0x00189239, "Specific Absorption Rate Sequence", "SQ");
		add(0x00189240, "RF Echo Train Length", "US");
		add(0x00189241, "Gradient Echo Train Length", "US");
		add(0x00189250, "Arterial Spin Labeling Contrast", "CS");
		add(0x00189251, "MR Arterial Spin LabelingSequence", "SQ");
		add(0x00189252, "ASL Technique Description", "LO");
		add(0x00189253, "ASL Slab Number", "US");
		add(0x00189254, "ASL Slab Thickness", "FD");
		add(0x00189255, "ASL Slab Orientation", "FD");
		add(0x00189256, "ASL Mid Slab Position", "FD");
		add(0x00189257, "ASL Context", "CS");
		add(0x00189258, "ASL Pulse Train Duration", "UL");
		add(0x00189259, "ASL Crusher Flag", "CS");
		add(0x0018925A, "ASL Crusher Flow Limit", "FD");
		add(0x0018925B, "ASL Crusher Description", "LO");
		add(0x0018925C, "ASL Bolus Cut-off Flag", "CS");
		add(0x0018925D, "ASL Bolus Cut-off TimingSequence", "SQ");
		add(0x0018925E, "ASL Bolus Cut-off Technique", "LO");
		add(0x0018925F, "ASL Bolus Cut-off Delay Time", "UL");
		add(0x00189260, "ASL Slab Sequence", "SQ");
		add(0x00189295, "Chemical Shift Minimum IntegrationLimit in ppm", "FD");
		add(0x00189296, "Chemical Shift MaximumIntegration Limit in ppm", "FD");
		add(0x00189297, "Water Reference Acquisition", "CS");
		add(0x00189298, "Echo Peak Position", "IS");
		add(0x00189301, "CT Acquisition Type Sequence", "SQ");
		add(0x00189302, "Acquisition Type", "CS");
		add(0x00189303, "Tube Angle", "FD");
		add(0x00189304, "CT Acquisition Details Sequence", "SQ");
		add(0x00189305, "Revolution Time", "FD");
		add(0x00189306, "Single Collimation Width", "FD");
		add(0x00189307, "Total Collimation Width", "FD");
		add(0x00189308, "CT Table Dynamics Sequence", "SQ");
		add(0x00189309, "Table Speed", "FD");
		add(0x00189310, "Table Feed per Rotation", "FD");
		add(0x00189311, "Spiral Pitch Factor", "FD");
		add(0x00189312, "CT Geometry Sequence", "SQ");
		add(0x00189313, "Data Collection Center (Patient)", "FD");
		add(0x00189314, "CT Reconstruction Sequence", "SQ");
		add(0x00189315, "Reconstruction Algorithm", "CS");
		add(0x00189316, "Convolution Kernel Group", "CS");
		add(0x00189317, "Reconstruction Field of View", "FD");
		add(0x00189318, "Reconstruction Target Center(Patient)", "FD");
		add(0x00189319, "Reconstruction Angle", "FD");
		add(0x00189320, "Image Filter", "SH");
		add(0x00189321, "CT Exposure Sequence", "SQ");
		add(0x00189322, "Reconstruction Pixel Spacing", "FD");
		add(0x00189323, "Exposure Modulation Type", "CS");
		add(0x00189324, "Estimated Dose Saving", "FD");
		add(0x00189325, "CT X-Ray Details Sequence", "SQ");
		add(0x00189326, "CT Position Sequence", "SQ");
		add(0x00189327, "Table Position", "FD");
		add(0x00189328, "Exposure Time in ms", "FD");
		add(0x00189329, "CT Image Frame Type Sequence", "SQ");
		add(0x00189330, "X-Ray Tube Current in mA", "FD");
		add(0x00189332, "Exposure in mAs", "FD");
		add(0x00189333, "Constant Volume Flag", "CS");
		add(0x00189334, "Fluoroscopy Flag", "CS");
		add(0x00189335, "Distance Source to Data CollectionCenter", "FD");
		add(0x00189337, "Contrast/Bolus Agent Number", "US");
		add(0x00189338, "Contrast/Bolus Ingredient CodeSequence", "SQ");
		add(0x00189340, "Contrast Administration ProfileSequence", "SQ");
		add(0x00189341, "Contrast/Bolus Usage Sequence", "SQ");
		add(0x00189342, "Contrast/Bolus Agent Administered", "CS");
		add(0x00189343, "Contrast/Bolus Agent Detected", "CS");
		add(0x00189344, "Contrast/Bolus Agent Phase", "CS");
		add(0x00189345, "CTDIvol", "FD");
		add(0x00189346, "CTDI Phantom Type CodeSequence", "SQ");
		add(0x00189351, "Calcium Scoring Mass FactorPatient", "FL");
		add(0x00189352, "Calcium Scoring Mass FactorDevice", "FL");
		add(0x00189353, "Energy Weighting Factor", "FL");
		add(0x00189360, "CT Additional X-Ray SourceSequence", "SQ");
		add(0x00189401, "Projection Pixel CalibrationSequence", "SQ");
		add(0x00189402, "Distance Source to Isocenter", "FL");
		add(0x00189403, "Distance Object to Table Top", "FL");
		add(0x00189404, "Object Pixel Spacing in Center ofBeam", "FL");
		add(0x00189405, "Positioner Position Sequence", "SQ");
		add(0x00189406, "Table Position Sequence", "SQ");
		add(0x00189407, "Collimator Shape Sequence", "SQ");
		add(0x00189410, "Planes in Acquisition", "CS");
		add(0x00189412, "XA/XRF Frame CharacteristicsSequence", "SQ");
		add(0x00189417, "Frame Acquisition Sequence", "SQ");
		add(0x00189420, "X-Ray Receptor Type", "CS");
		add(0x00189423, "Acquisition Protocol Name", "LO");
		add(0x00189424, "Acquisition Protocol Description", "LT");
		add(0x00189425, "Contrast/Bolus Ingredient Opaque", "CS");
		add(0x00189426, "Distance Receptor Plane toDetector Housing", "FL");
		add(0x00189427, "Intensifier Active Shape", "CS");
		add(0x00189428, "Intensifier Active Dimension(s)", "FL");
		add(0x00189429, "Physical Detector Size", "FL");
		add(0x00189430, "Position of Isocenter Projection", "FL");
		add(0x00189432, "Field of View Sequence", "SQ");
		add(0x00189433, "Field of View Description", "LO");
		add(0x00189434, "Exposure Control Sensing RegionsSequence", "SQ");
		add(0x00189435, "Exposure Control Sensing RegionShape", "CS");
		add(0x00189436, "Exposure Control Sensing RegionLeft Vertical Edge", "SS");
		add(0x00189437, "Exposure Control Sensing RegionRight Vertical Edge", "SS");
		add(0x00189438, "Exposure Control Sensing RegionUpper Horizontal Edge",
			"SS");
		add(0x00189439, "Exposure Control Sensing RegionLower Horizontal Edge",
			"SS");
		add(0x00189440, "Center of Circular ExposureControl Sensing Region", "SS");
		add(0x00189441, "Radius of Circular ExposureControl Sensing Region", "US");
		add(0x00189442, "Vertices of the Polygonal ExposureControl Sensing Region",
			"SS");
		add(0x00189445, "Column Angulation (Patient)", "FL");
		add(0x00189449, "Beam Angle", "FL");
		add(0x00189451, "Frame Detector ParametersSequence", "SQ");
		add(0x00189452, "Calculated Anatomy Thickness", "FL");
		add(0x00189455, "Calibration Sequence", "SQ");
		add(0x00189456, "Object Thickness Sequence", "SQ");
		add(0x00189457, "Plane Identification", "CS");
		add(0x00189461, "Field of View Dimension(s) in Float", "FL");
		add(0x00189462, "Isocenter Reference SystemSequence", "SQ");
		add(0x00189463, "Positioner Isocenter Primary Angle", "FL");
		add(0x00189464, "Positioner Isocenter SecondaryAngle", "FL");
		add(0x00189465, "Positioner Isocenter DetectorRotation Angle", "FL");
		add(0x00189466, "Table X Position to Isocenter", "FL");
		add(0x00189467, "Table Y Position to Isocenter", "FL");
		add(0x00189468, "Table Z Position to Isocenter", "FL");
		add(0x00189469, "Table Horizontal Rotation Angle", "FL");
		add(0x00189470, "Table Head Tilt Angle", "FL");
		add(0x00189471, "Table Cradle Tilt Angle", "FL");
		add(0x00189472, "Frame Display Shutter Sequence", "SQ");
		add(0x00189473, "Acquired Image Area Dose Product", "FL");
		add(0x00189474, "C-arm Positioner TabletopRelationship", "CS");
		add(0x00189476, "X-Ray Geometry Sequence", "SQ");
		add(0x00189477, "Irradiation Event IdentificationSequence", "SQ");
		add(0x00189504, "X-Ray 3D Frame Type Sequence", "SQ");
		add(0x00189506, "Contributing Sources Sequence", "SQ");
		add(0x00189507, "X-Ray 3D Acquisition Sequence", "SQ");
		add(0x00189508, "Primary Positioner Scan Arc", "FL");
		add(0x00189509, "Secondary Positioner Scan Arc", "FL");
		add(0x00189510, "Primary Positioner Scan StartAngle", "FL");
		add(0x00189511, "Secondary Positioner Scan StartAngle", "FL");
		add(0x00189514, "Primary Positioner Increment", "FL");
		add(0x00189515, "Secondary Positioner Increment", "FL");
		add(0x00189516, "Start Acquisition DateTime", "DT");
		add(0x00189517, "End Acquisition DateTime", "DT");
		add(0x00189518, "Primary Positioner Increment Sign", "SS");
		add(0x00189519, "Secondary Positioner IncrementSign", "SS");
		add(0x00189524, "Application Name", "LO");
		add(0x00189525, "Application Version", "LO");
		add(0x00189526, "Application Manufacturer", "LO");
		add(0x00189527, "Algorithm Type", "CS");
		add(0x00189528, "Algorithm Description", "LO");
		add(0x00189530, "X-Ray 3D ReconstructionSequence", "SQ");
		add(0x00189531, "Reconstruction Description", "LO");
		add(0x00189538, "Per Projection AcquisitionSequence", "SQ");
		add(0x00189541, "Detector Position Sequence", "SQ");
		add(0x00189542, "X-Ray Acquisition Dose Sequence", "SQ");
		add(0x00189543, "X-Ray Source Isocenter PrimaryAngle", "FD");
		add(0x00189544, "X-Ray Source Isocenter SecondaryAngle", "FD");
		add(0x00189545, "Breast Support Isocenter PrimaryAngle", "FD");
		add(0x00189546, "Breast Support IsocenterSecondary Angle", "FD");
		add(0x00189547, "Breast Support X Position toIsocenter", "FD");
		add(0x00189548, "Breast Support Y Position toIsocenter", "FD");
		add(0x00189549, "Breast Support Z Position toIsocenter", "FD");
		add(0x00189550, "Detector Isocenter Primary Angle", "FD");
		add(0x00189551, "Detector Isocenter SecondaryAngle", "FD");
		add(0x00189552, "Detector X Position to Isocenter", "FD");
		add(0x00189553, "Detector Y Position to Isocenter", "FD");
		add(0x00189554, "Detector Z Position to Isocenter", "FD");
		add(0x00189555, "X-Ray Grid Sequence", "SQ");
		add(0x00189556, "X-Ray Filter Sequence", "SQ");
		add(0x00189557, "Detector Active Area TLHCPosition", "FD");
		add(0x00189558, "Detector Active Area Orientation", "FD");
		add(0x00189559, "Positioner Primary Angle Direction", "CS");
		add(0x00189601, "Diffusion b-matrix Sequence", "SQ");
		add(0x00189602, "Diffusion b-value XX", "FD");
		add(0x00189603, "Diffusion b-value XY", "FD");
		add(0x00189604, "Diffusion b-value XZ", "FD");
		add(0x00189605, "Diffusion b-value YY", "FD");
		add(0x00189606, "Diffusion b-value YZ", "FD");
		add(0x00189607, "Diffusion b-value ZZ", "FD");
		add(0x00189701, "Decay Correction DateTime", "DT");
		add(0x00189715, "Start Density Threshold", "FD");
		add(0x00189716, "Start Relative Density DifferenceThreshold", "FD");
		add(0x00189717, "Start Cardiac Trigger CountThreshold", "FD");
		add(0x00189718, "Start Respiratory Trigger CountThreshold", "FD");
		add(0x00189719, "Termination Counts Threshold", "FD");
		add(0x00189720, "Termination Density Threshold", "FD");
		add(0x00189721, "Termination Relative DensityThreshold", "FD");
		add(0x00189722, "Termination Time Threshold", "FD");
		add(0x00189723, "Termination Cardiac Trigger CountThreshold", "FD");
		add(0x00189724, "Termination Respiratory TriggerCount Threshold", "FD");
		add(0x00189725, "Detector Geometry", "CS");
		add(0x00189726, "Transverse Detector Separation", "FD");
		add(0x00189727, "Axial Detector Dimension", "FD");
		add(0x00189729, "Radiopharmaceutical AgentNumber", "US");
		add(0x00189732, "PET Frame Acquisition Sequence", "SQ");
		add(0x00189733, "PET Detector Motion DetailsSequence", "SQ");
		add(0x00189734, "PET Table Dynamics Sequence", "SQ");
		add(0x00189735, "PET Position Sequence", "SQ");
		add(0x00189736, "PET Frame Correction FactorsSequence", "SQ");
		add(0x00189737, "Radiopharmaceutical UsageSequence", "SQ");
		add(0x00189738, "Attenuation Correction Source", "CS");
		add(0x00189739, "Number of Iterations", "US");
		add(0x00189740, "Number of Subsets", "US");
		add(0x00189749, "PET Reconstruction Sequence", "SQ");
		add(0x00189751, "PET Frame Type Sequence", "SQ");
		add(0x00189755, "Time of Flight Information Used", "CS");
		add(0x00189756, "Reconstruction Type", "CS");
		add(0x00189758, "Decay Corrected", "CS");
		add(0x00189759, "Attenuation Corrected", "CS");
		add(0x00189760, "Scatter Corrected", "CS");
		add(0x00189761, "Dead Time Corrected", "CS");
		add(0x00189762, "Gantry Motion Corrected", "CS");
		add(0x00189763, "Patient Motion Corrected", "CS");
		add(0x00189764, "Count Loss NormalizationCorrected", "CS");
		add(0x00189765, "Randoms Corrected", "CS");
		add(0x00189766, "Non-uniform Radial SamplingCorrected", "CS");
		add(0x00189767, "Sensitivity Calibrated", "CS");
		add(0x00189768, "Detector Normalization Correction", "CS");
		add(0x00189769, "Iterative Reconstruction Method", "CS");
		add(0x00189770, "Attenuation Correction TemporalRelationship", "CS");
		add(0x00189771, "Patient Physiological StateSequence", "SQ");
		add(0x00189772, "Patient Physiological State CodeSequence", "SQ");
		add(0x00189801, "Depth(s) of Focus", "FD");
		add(0x00189803, "Excluded Intervals Sequence", "SQ");
		add(0x00189804, "Exclusion Start DateTime", "DT");
		add(0x00189805, "Exclusion Duration", "FD");
		add(0x00189806, "US Image Description Sequence", "SQ");
		add(0x00189807, "Image Data Type Sequence", "SQ");
		add(0x00189808, "Data Type", "CS");
		add(0x00189809, "Transducer Scan Pattern CodeSequence", "SQ");
		add(0x0018980B, "Aliased Data Type", "CS");
		add(0x0018980C, "Position Measuring Device Used", "CS");
		add(0x0018980D, "Transducer Geometry CodeSequence", "SQ");
		add(0x0018980E, "Transducer Beam Steering CodeSequence", "SQ");
		add(0x0018980F, "Transducer Application CodeSequence", "SQ");
		// add(0x00189810, "Zero Velocity Pixel Value", "US or SS");
		add(0x0018A001, "Contributing Equipment Sequence", "SQ");
		add(0x0018A002, "Contribution DateTime", "DT");
		add(0x0018A003, "Contribution Description", "ST");
	}

	/**
	 * Adds attributes of group 0x0020.
	 */
	private void addAttributeGroup0020() {
		add(0x0020000D, "Study Instance UID", "UI");
		add(0x0020000E, "Series Instance UID", "UI");
		add(0x00200010, "Study ID", "SH");
		add(0x00200011, "Series Number", "IS");
		add(0x00200012, "Acquisition Number", "IS");
		add(0x00200013, "Instance Number", "IS");
		add(0x00200014, "Isotope Number", "IS"); // Retired
		add(0x00200015, "Phase Number", "IS"); // Retired
		add(0x00200016, "Interval Number", "IS"); // Retired
		add(0x00200017, "Time Slot Number", "IS"); // Retired
		add(0x00200018, "Angle Number", "IS"); // Retired
		add(0x00200019, "Item Number", "IS");
		add(0x00200020, "Patient Orientation", "CS");
		add(0x00200022, "Overlay Number", "IS"); // Retired
		add(0x00200024, "Curve Number", "IS"); // Retired
		add(0x00200026, "LUT Number", "IS"); // Retired
		add(0x00200030, "Image Position", "DS"); // Retired
		add(0x00200032, "Image Position (Patient)", "DS");
		add(0x00200035, "Image Orientation", "DS"); // Retired
		add(0x00200037, "Image Orientation (Patient)", "DS");
		add(0x00200050, "Location", "DS"); // Retired
		add(0x00200052, "Frame of Reference UID", "UI");
		add(0x00200060, "Laterality", "CS");
		add(0x00200062, "Image Laterality", "CS");
		add(0x00200070, "Image Geometry Type", "LO"); // Retired
		add(0x00200080, "Masking Image", "CS"); // Retired
		add(0x002000AA, "Report Number", "IS"); // Retired
		add(0x00200100, "Temporal Position Identifier", "IS");
		add(0x00200105, "Number of Temporal Positions", "IS");
		add(0x00200110, "Temporal Resolution", "DS");
		add(0x00200200, "Synchronization Frame ofReference UID", "UI");
		add(0x00200242, "SOP Instance UID ofConcatenation Source", "UI");
		add(0x00201000, "Series in Study", "IS"); // Retired
		add(0x00201001, "Acquisitions in Series", "IS"); // Retired
		add(0x00201002, "Images in Acquisition", "IS");
		add(0x00201003, "Images in Series", "IS"); // Retired
		add(0x00201004, "Acquisitions in Study", "IS"); // Retired
		add(0x00201005, "Images in Study", "IS"); // Retired
		add(0x00201020, "Reference", "LO"); // Retired
		add(0x00201040, "Position Reference Indicator", "LO");
		add(0x00201041, "Slice Location", "DS");
		add(0x00201070, "Other Study Numbers", "IS"); // Retired
		add(0x00201200, "Number of Patient Related Studies", "IS");
		add(0x00201202, "Number of Patient Related Series", "IS");
		add(0x00201204, "Number of Patient RelatedInstances", "IS");
		add(0x00201206, "Number of Study Related Series", "IS");
		add(0x00201208, "Number of Study Related Instances", "IS");
		add(0x00201209, "Number of Series RelatedInstances", "IS");
		add(0x00203401, "Modifying Device ID", "CS"); // Retired
		add(0x00203402, "Modified Image ID", "CS"); // Retired
		add(0x00203403, "Modified Image Date", "DA"); // Retired
		add(0x00203404, "Modifying Device Manufacturer", "LO"); // Retired
		add(0x00203405, "Modified Image Time", "TM"); // Retired
		add(0x00203406, "Modified Image Description", "LO"); // Retired
		add(0x00204000, "Image Comments", "LT");
		add(0x00205000, "Original Image Identification", "AT"); // Retired
		add(0x00205002, "Original Image IdentificationNomenclature", "LO"); // Retired
		add(0x00209056, "Stack ID", "SH");
		add(0x00209057, "In-Stack Position Number", "UL");
		add(0x00209071, "Frame Anatomy Sequence", "SQ");
		add(0x00209072, "Frame Laterality", "CS");
		add(0x00209111, "Frame Content Sequence", "SQ");
		add(0x00209113, "Plane Position Sequence", "SQ");
		add(0x00209116, "Plane Orientation Sequence", "SQ");
		add(0x00209128, "Temporal Position Index", "UL");
		add(0x00209153, "Nominal Cardiac Trigger DelayTime", "FD");
		add(0x00209154, "Nominal Cardiac Trigger Time PriorTo R-Peak", "FL");
		add(0x00209155, "Actual Cardiac Trigger Time PriorTo R-Peak", "FL");
		add(0x00209156, "Frame Acquisition Number", "US");
		add(0x00209157, "Dimension Index Values", "UL");
		add(0x00209158, "Frame Comments", "LT");
		add(0x00209161, "Concatenation UID", "UI");
		add(0x00209162, "In-concatenation Number", "US");
		add(0x00209163, "In-concatenation Total Number", "US");
		add(0x00209164, "Dimension Organization UID", "UI");
		add(0x00209165, "Dimension Index Pointer", "AT");
		add(0x00209167, "Functional Group Pointer", "AT");
		add(0x00209170, "Unassigned Shared ConvertedAttributes Sequence", "SQ");
		add(0x00209171, "Unassigned Per-Frame ConvertedAttributes Sequence", "SQ");
		add(0x00209172, "Conversion Source AttributesSequence", "SQ");
		add(0x00209213, "Dimension Index Private Creator", "LO");
		add(0x00209221, "Dimension Organization Sequence", "SQ");
		add(0x00209222, "Dimension Index Sequence", "SQ");
		add(0x00209228, "Concatenation Frame OffsetNumber", "UL");
		add(0x00209238, "Functional Group Private Creator", "LO");
		add(0x00209241, "Nominal Percentage of CardiacPhase", "FL");
		add(0x00209245, "Nominal Percentage of RespiratoryPhase", "FL");
		add(0x00209246, "Starting Respiratory Amplitude", "FL");
		add(0x00209247, "Starting Respiratory Phase", "CS");
		add(0x00209248, "Ending Respiratory Amplitude", "FL");
		add(0x00209249, "Ending Respiratory Phase", "CS");
		add(0x00209250, "Respiratory Trigger Type", "CS");
		add(0x00209251, "R-R Interval Time Nominal", "FD");
		add(0x00209252, "Actual Cardiac Trigger Delay Time", "FD");
		add(0x00209253, "Respiratory SynchronizationSequence", "SQ");
		add(0x00209254, "Respiratory Interval Time", "FD");
		add(0x00209255, "Nominal Respiratory Trigger DelayTime", "FD");
		add(0x00209256, "Respiratory Trigger DelayThreshold", "FD");
		add(0x00209257, "Actual Respiratory Trigger DelayTime", "FD");
		add(0x00209301, "Image Position (Volume)", "FD");
		add(0x00209302, "Image Orientation (Volume)", "FD");
		add(0x00209307, "Ultrasound Acquisition Geometry", "CS");
		add(0x00209308, "Apex Position", "FD");
		add(0x00209309, "Volume to Transducer MappingMatrix", "FD");
		add(0x0020930A, "Volume to Table Mapping Matrix", "FD");
		add(0x0020930B, "Volume to Transducer Relationship", "CS");
		add(0x0020930C, "Patient Frame of Reference Source", "CS");
		add(0x0020930D, "Temporal Position Time Offset", "FD");
		add(0x0020930E, "Plane Position (Volume) Sequence", "SQ");
		add(0x0020930F, "Plane Orientation (Volume)  Sequence", "SQ");
		add(0x00209310, "Temporal Position Sequence", "SQ");
		add(0x00209311, "Dimension Organization Type", "CS");
		add(0x00209312, "Volume Frame of Reference UID", "UI");
		add(0x00209313, "Table Frame of Reference UID", "UI");
		add(0x00209421, "Dimension Description Label", "LO");
		add(0x00209450, "Patient Orientation in FrameSequence", "SQ");
		add(0x00209453, "Frame Label", "LO");
		add(0x00209518, "Acquisition Index", "US");
		add(0x00209529, "Contributing SOP InstancesReference Sequence", "SQ");
		add(0x00209536, "Reconstruction Index", "US");
	}

	/**
	 * Adds attributes of group 0x0022.
	 */
	private void addAttributeGroup0022() {
		add(0x00220001, "Light Path Filter Pass-ThroughWavelength", "US");
		add(0x00220002, "Light Path Filter Pass Band", "US");
		add(0x00220003, "Image Path Filter Pass-ThroughWavelength", "US");
		add(0x00220004, "Image Path Filter Pass Band", "US");
		add(0x00220005, "Patient Eye MovementCommanded", "CS");
		add(0x00220006, "Patient Eye Movement CommandCode Sequence", "SQ");
		add(0x00220007, "Spherical Lens Power", "FL");
		add(0x00220008, "Cylinder Lens Power", "FL");
		add(0x00220009, "Cylinder Axis", "FL");
		add(0x0022000A, "Emmetropic Magnification", "FL");
		add(0x0022000B, "Intra Ocular Pressure", "FL");
		add(0x0022000C, "Horizontal Field of View", "FL");
		add(0x0022000D, "Pupil Dilated", "CS");
		add(0x0022000E, "Degree of Dilation", "FL");
		add(0x00220010, "Stereo Baseline Angle", "FL");
		add(0x00220011, "Stereo Baseline Displacement", "FL");
		add(0x00220012, "Stereo Horizontal Pixel Offset", "FL");
		add(0x00220013, "Stereo Vertical Pixel Offset", "FL");
		add(0x00220014, "Stereo Rotation", "FL");
		add(0x00220015, "Acquisition Device Type CodeSequence", "SQ");
		add(0x00220016, "Illumination Type Code Sequence", "SQ");
		add(0x00220017, "Light Path Filter Type Stack CodeSequence", "SQ");
		add(0x00220018, "Image Path Filter Type Stack CodeSequence", "SQ");
		add(0x00220019, "Lenses Code Sequence", "SQ");
		add(0x0022001A, "Channel Description CodeSequence", "SQ");
		add(0x0022001B, "Refractive State Sequence", "SQ");
		add(0x0022001C, "Mydriatic Agent Code Sequence", "SQ");
		add(0x0022001D, "Relative Image Position CodeSequence", "SQ");
		add(0x0022001E, "Camera Angle of View", "FL");
		add(0x00220020, "Stereo Pairs Sequence", "SQ");
		add(0x00220021, "Left Image Sequence", "SQ");
		add(0x00220022, "Right Image Sequence", "SQ");
		add(0x00220028, "Stereo Pairs Present", "CS");
		add(0x00220030, "Axial Length of the Eye", "FL");
		add(0x00220031, "Ophthalmic Frame LocationSequence", "SQ");
		add(0x00220032, "Reference Coordinates", "FL");
		add(0x00220035, "Depth Spatial Resolution", "FL");
		add(0x00220036, "Maximum Depth Distortion", "FL");
		add(0x00220037, "Along-scan Spatial Resolution", "FL");
		add(0x00220038, "Maximum Along-scan Distortion", "FL");
		add(0x00220039, "Ophthalmic Image Orientation", "CS");
		add(0x00220041, "Depth of Transverse Image", "FL");
		add(0x00220042, "Mydriatic Agent ConcentrationUnits Sequence", "SQ");
		add(0x00220048, "Across-scan Spatial Resolution", "FL");
		add(0x00220049, "Maximum Across-scan Distortion", "FL");
		add(0x0022004E, "Mydriatic Agent Concentration", "DS");
		add(0x00220055, "Illumination Wave Length", "FL");
		add(0x00220056, "Illumination Power", "FL");
		add(0x00220057, "Illumination Bandwidth", "FL");
		add(0x00220058, "Mydriatic Agent Sequence", "SQ");
		add(0x00221007, "Ophthalmic Axial MeasurementsRight Eye Sequence", "SQ");
		add(0x00221008, "Ophthalmic Axial MeasurementsLeft Eye Sequence", "SQ");
		add(0x00221009, "Ophthalmic Axial MeasurementsDevice Type", "CS");
		add(0x00221010, "Ophthalmic Axial LengthMeasurements Type", "CS");
		add(0x00221012, "Ophthalmic Axial Length Sequence", "SQ");
		add(0x00221019, "Ophthalmic Axial Length", "FL");
		add(0x00221024, "Lens Status Code Sequence", "SQ");
		add(0x00221025, "Vitreous Status Code Sequence", "SQ");
		add(0x00221028, "IOL Formula Code Sequence", "SQ");
		add(0x00221029, "IOL Formula Detail", "LO");
		add(0x00221033, "Keratometer Index", "FL");
		add(0x00221035, "Source of Ophthalmic Axial LengthCode Sequence", "SQ");
		add(0x00221037, "Target Refraction", "FL");
		add(0x00221039, "Refractive Procedure Occurred", "CS");
		add(0x00221040, "Refractive Surgery Type CodeSequence", "SQ");
		add(0x00221044, "Ophthalmic Ultrasound MethodCode Sequence", "SQ");
		add(0x00221050, "Ophthalmic Axial LengthMeasurements Sequence", "SQ");
		add(0x00221053, "IOL Power", "FL");
		add(0x00221054, "Predicted Refractive Error", "FL");
		add(0x00221059, "Ophthalmic Axial Length Velocity", "FL");
		add(0x00221065, "Lens Status Description", "LO");
		add(0x00221066, "Vitreous Status Description", "LO");
		add(0x00221090, "IOL Power Sequence", "SQ");
		add(0x00221092, "Lens Constant Sequence", "SQ");
		add(0x00221093, "IOL Manufacturer", "LO");
		add(0x00221094, "Lens Constant Description", "LO"); // Retired
		add(0x00221095, "Implant Name", "LO");
		add(0x00221096, "Keratometry Measurement TypeCode Sequence", "SQ");
		add(0x00221097, "Implant Part Number", "LO");
		add(0x00221100, "Referenced Ophthalmic AxialMeasurements Sequence", "SQ");
		add(0x00221101,
			"Ophthalmic Axial LengthMeasurements Segment NameCode Sequence", "SQ");
		add(0x00221103, "Refractive Error Before RefractiveSurgery Code Sequence",
			"SQ");
		add(0x00221121, "IOL Power For Exact Emmetropia", "FL");
		add(0x00221122, "IOL Power For Exact TargetRefraction", "FL");
		add(0x00221125, "Anterior Chamber Depth DefinitionCode Sequence", "SQ");
		add(0x00221127, "Lens Thickness Sequence", "SQ");
		add(0x00221128, "Anterior Chamber Depth Sequence", "SQ");
		add(0x00221130, "Lens Thickness", "FL");
		add(0x00221131, "Anterior Chamber Depth", "FL");
		add(0x00221132, "Source of Lens Thickness DataCode Sequence", "SQ");
		add(0x00221133, "Source of Anterior Chamber DepthData Code Sequence", "SQ");
		add(0x00221134, "Source of RefractiveMeasurements Sequence", "SQ");
		add(0x00221135, "Source of RefractiveMeasurements Code Sequence", "SQ");
		add(0x00221140, "Ophthalmic Axial LengthMeasurement Modified", "CS");
		add(0x00221150, "Ophthalmic Axial Length DataSource Code Sequence", "SQ");
		add(0x00221153, "Ophthalmic Axial LengthAcquisition Method CodeSequence",
			"SQ"); // Retired
		add(0x00221155, "Signal to Noise Ratio", "FL");
		add(0x00221159, "Ophthalmic Axial Length DataSource Description", "LO");
		add(0x00221210, "Ophthalmic Axial LengthMeasurements Total LengthSequence",
			"SQ");
		add(0x00221211,
			"Ophthalmic Axial LengthMeasurements Segmental LengthSequence", "SQ");
		add(0x00221212,
			"Ophthalmic Axial LengthMeasurements Length SummationSequence", "SQ");
		add(0x00221220, "Ultrasound Ophthalmic AxialLength Measurements Sequence",
			"SQ");
		add(0x00221225, "Optical Ophthalmic Axial LengthMeasurements Sequence",
			"SQ");
		add(0x00221230, "Ultrasound Selected OphthalmicAxial Length Sequence",
			"SQ");
		add(0x00221250, "Ophthalmic Axial Length SelectionMethod Code Sequence",
			"SQ");
		add(0x00221255, "Optical Selected Ophthalmic AxialLength Sequence", "SQ");
		add(0x00221257, "Selected Segmental OphthalmicAxial Length Sequence", "SQ");
		add(0x00221260, "Selected Total Ophthalmic AxialLength Sequence", "SQ");
		add(0x00221262, "Ophthalmic Axial Length QualityMetric Sequence", "SQ");
		add(0x00221265, "Ophthalmic Axial Length QualityMetric Type Code Sequence",
			"SQ"); // Retired
		add(0x00221273, "Ophthalmic Axial Length QualityMetric Type Description",
			"LO"); // Retired
		add(0x00221300, "Intraocular Lens Calculations RightEye Sequence", "SQ");
		add(0x00221310, "Intraocular Lens Calculations LeftEye Sequence", "SQ");
		add(0x00221330,
			"Referenced Ophthalmic AxialLength Measurement QC ImageSequence", "SQ");
		add(0x00221415, "Ophthalmic Mapping Device Type", "CS");
		add(0x00221420, "Acquisition Method CodeSequence", "SQ");
		add(0x00221423, "Acquisition Method AlgorithmSequence", "SQ");
		add(0x00221436, "Ophthalmic Thickness Map TypeCode Sequence", "SQ");
		add(0x00221443, "Ophthalmic Thickness MappingNormals Sequence", "SQ");
		add(0x00221445, "Retinal Thickness Definition CodeSequence", "SQ");
		add(0x00221450, "Pixel Value Mapping to CodedConcept Sequence", "SQ");
		// add(0x00221452, "Mapped Pixel Value", "US or SS");
		add(0x00221454, "Pixel Value Mapping Explanation", "LO");
		add(0x00221458, "Ophthalmic Thickness Map QualityThreshold Sequence", "SQ");
		add(0x00221460, "Ophthalmic Thickness MapThreshold Quality Rating", "FL");
		add(0x00221463, "Anatomic Structure ReferencePoint", "FL");
		add(0x00221465, "Registration to Localizer Sequence", "SQ");
		add(0x00221466, "Registered Localizer Units", "CS");
		add(0x00221467, "Registered Localizer Top Left HandCorner", "FL");
		add(0x00221468, "Registered Localizer Bottom RightHand Corner", "FL");
		add(0x00221470, "Ophthalmic Thickness Map QualityRating Sequence", "SQ");
		add(0x00221472, "Relevant OPT Attributes Sequence", "SQ");
		add(0x00221512, "Transformation Method CodeSequence", "SQ");
		add(0x00221513, "Transformation AlgorithmSequence", "SQ");
		add(0x00221515, "Ophthalmic Axial Length Method", "CS");
		add(0x00221517, "Ophthalmic FOV", "FL");
		add(0x00221518, "Two Dimensional to ThreeDimensional Map Sequence", "SQ");
		add(0x00221525, "Wide Field OphthalmicPhotography Quality RatingSequence",
			"SQ");
		add(0x00221526,
			"Wide Field OphthalmicPhotography Quality ThresholdSequence", "SQ");
		add(0x00221527, "Wide Field OphthalmicPhotography Threshold QualityRating",
			"FL");
		add(0x00221528, "X Coordinates Center Pixel ViewAngle", "FL");
		add(0x00221529, "Y Coordinates Center Pixel ViewAngle", "FL");
		add(0x00221530, "Number of Map Points", "UL");
		add(0x00221531, "Two Dimensional to ThreeDimensional Map Data", "OF");
	}

	/**
	 * Adds attributes of group 0x0024.
	 */
	private void addAttributeGroup0024() {
		add(0x00240010, "Visual Field Horizontal Extent", "FL");
		add(0x00240011, "Visual Field Vertical Extent", "FL");
		add(0x00240012, "Visual Field Shape", "CS");
		add(0x00240016, "Screening Test Mode CodeSequence", "SQ");
		add(0x00240018, "Maximum Stimulus Luminance", "FL");
		add(0x00240020, "Background Luminance", "FL");
		add(0x00240021, "Stimulus Color Code Sequence", "SQ");
		add(0x00240024, "Background Illumination ColorCode Sequence", "SQ");
		add(0x00240025, "Stimulus Area", "FL");
		add(0x00240028, "Stimulus Presentation Time", "FL");
		add(0x00240032, "Fixation Sequence", "SQ");
		add(0x00240033, "Fixation Monitoring CodeSequence", "SQ");
		add(0x00240034, "Visual Field Catch Trial Sequence", "SQ");
		add(0x00240035, "Fixation Checked Quantity", "US");
		add(0x00240036, "Patient Not Properly FixatedQuantity", "US");
		add(0x00240037, "Presented Visual Stimuli Data Flag", "CS");
		add(0x00240038, "Number of Visual Stimuli", "US");
		add(0x00240039, "Excessive Fixation Losses DataFlag", "CS");
		add(0x00240040, "Excessive Fixation Losses", "CS");
		add(0x00240042, "Stimuli Retesting Quantity", "US");
		add(0x00240044, "Comments on Patient'sPerformance of Visual Field", "LT");
		add(0x00240045, "False Negatives Estimate Flag", "CS");
		add(0x00240046, "False Negatives Estimate", "FL");
		add(0x00240048, "Negative Catch Trials Quantity", "US");
		add(0x00240050, "False Negatives Quantity", "US");
		add(0x00240051, "Excessive False Negatives DataFlag", "CS");
		add(0x00240052, "Excessive False Negatives", "CS");
		add(0x00240053, "False Positives Estimate Flag", "CS");
		add(0x00240054, "False Positives Estimate", "FL");
		add(0x00240055, "Catch Trials Data Flag", "CS");
		add(0x00240056, "Positive Catch Trials Quantity", "US");
		add(0x00240057, "Test Point Normals Data Flag", "CS");
		add(0x00240058, "Test Point Normals Sequence", "SQ");
		add(0x00240059, "Global Deviation ProbabilityNormals Flag", "CS");
		add(0x00240060, "False Positives Quantity", "US");
		add(0x00240061, "Excessive False Positives DataFlag", "CS");
		add(0x00240062, "Excessive False Positives", "CS");
		add(0x00240063, "Visual Field Test Normals Flag", "CS");
		add(0x00240064, "Results Normals Sequence", "SQ");
		add(0x00240065, "Age Corrected Sensitivity DeviationAlgorithm Sequence",
			"SQ");
		add(0x00240066, "Global Deviation From Normal", "FL");
		add(0x00240067,
			"Generalized Defect SensitivityDeviation Algorithm Sequence", "SQ");
		add(0x00240068, "Localized Deviation From Normal", "FL");
		add(0x00240069, "Patient Reliability Indicator", "LO");
		add(0x00240070, "Visual Field Mean Sensitivity", "FL");
		add(0x00240071, "Global Deviation Probability", "FL");
		add(0x00240072, "Local Deviation Probability NormalsFlag", "CS");
		add(0x00240073, "Localized Deviation Probability", "FL");
		add(0x00240074, "Short Term Fluctuation Calculated", "CS");
		add(0x00240075, "Short Term Fluctuation", "FL");
		add(0x00240076, "Short Term Fluctuation ProbabilityCalculated", "CS");
		add(0x00240077, "Short Term Fluctuation Probability", "FL");
		add(0x00240078, "Corrected Localized DeviationFrom Normal Calculated",
			"CS");
		add(0x00240079, "Corrected Localized DeviationFrom Normal", "FL");
		add(0x00240080,
			"Corrected Localized DeviationFrom Normal Probability Calculated", "CS");
		add(0x00240081, "Corrected Localized DeviationFrom Normal Probability",
			"FL");
		add(0x00240083, "Global Deviation ProbabilitySequence", "SQ");
		add(0x00240085, "Localized Deviation ProbabilitySequence", "SQ");
		add(0x00240086, "Foveal Sensitivity Measured", "CS");
		add(0x00240087, "Foveal Sensitivity", "FL");
		add(0x00240088, "Visual Field Test Duration", "FL");
		add(0x00240089, "Visual Field Test Point Sequence", "SQ");
		add(0x00240090, "Visual Field Test PointX-Coordinate", "FL");
		add(0x00240091, "Visual Field Test PointY-Coordinate", "FL");
		add(0x00240092, "Age Corrected Sensitivity DeviationValue", "FL");
		add(0x00240093, "Stimulus Results", "CS");
		add(0x00240094, "Sensitivity Value", "FL");
		add(0x00240095, "Retest Stimulus Seen", "CS");
		add(0x00240096, "Retest Sensitivity Value", "FL");
		add(0x00240097, "Visual Field Test Point NormalsSequence", "SQ");
		add(0x00240098, "Quantified Defect", "FL");
		add(0x00240100, "Age Corrected Sensitivity DeviationProbability Value",
			"FL");
		add(0x00240102, "Generalized Defect CorrectedSensitivity Deviation Flag",
			"CS");
		add(0x00240103, "Generalized Defect CorrectedSensitivity Deviation Value",
			"FL");
		add(0x00240104,
			"Generalized Defect CorrectedSensitivity Deviation ProbabilityValue",
			"FL");
		add(0x00240105, "Minimum Sensitivity Value", "FL");
		add(0x00240106, "Blind Spot Localized", "CS");
		add(0x00240107, "Blind Spot X-Coordinate", "FL");
		add(0x00240108, "Blind Spot Y-Coordinate", "FL");
		add(0x00240110, "Visual Acuity MeasurementSequence", "SQ");
		add(0x00240112, "Refractive Parameters Used onPatient Sequence", "SQ");
		add(0x00240113, "Measurement Laterality", "CS");
		add(0x00240114, "Ophthalmic Patient ClinicalInformation Left Eye Sequence",
			"SQ");
		add(0x00240115, "Ophthalmic Patient ClinicalInformation Right Eye Sequence",
			"SQ");
		add(0x00240117, "Foveal Point Normative Data Flag", "CS");
		add(0x00240118, "Foveal Point Probability Value", "FL");
		add(0x00240120, "Screening Baseline Measured", "CS");
		add(0x00240122, "Screening Baseline MeasuredSequence", "SQ");
		add(0x00240124, "Screening Baseline Type", "CS");
		add(0x00240126, "Screening Baseline Value", "FL");
		add(0x00240202, "Algorithm Source", "LO");
		add(0x00240306, "Data Set Name", "LO");
		add(0x00240307, "Data Set Version", "LO");
		add(0x00240308, "Data Set Source", "LO");
		add(0x00240309, "Data Set Description", "LO");
		add(0x00240317, "Visual Field Test Reliability GlobalIndex Sequence", "SQ");
		add(0x00240320, "Visual Field Global Results IndexSequence", "SQ");
		add(0x00240325, "Data Observation Sequence", "SQ");
		add(0x00240338, "Index Normals Flag", "CS");
		add(0x00240341, "Index Probability", "FL");
		add(0x00240344, "Index Probability Sequence", "SQ");
	}

	/**
	 * Adds attributes of group 0x0028.
	 */
	private void addAttributeGroup0028() {
		add(0x00280002, "Samples per Pixel", "US");
		add(0x00280003, "Samples per Pixel Used", "US");
		add(0x00280004, "Photometric Interpretation", "CS");
		add(0x00280005, "Image Dimensions", "US"); // Retired
		add(0x00280006, "Planar Configuration", "US");
		add(0x00280008, "Number of Frames", "IS");
		add(0x00280009, "Frame Increment Pointer", "AT");
		add(0x0028000A, "Frame Dimension Pointer", "AT");
		add(0x00280010, "Rows", "US");
		add(0x00280011, "Columns", "US");
		add(0x00280012, "Planes", "US"); // Retired
		add(0x00280014, "Ultrasound Color Data Present", "US");
		add(0x00280030, "Pixel Spacing", "DS");
		add(0x00280031, "Zoom Factor", "DS");
		add(0x00280032, "Zoom Center", "DS");
		add(0x00280034, "Pixel Aspect Ratio", "IS");
		add(0x00280040, "Image Format", "CS"); // Retired
		add(0x00280050, "Manipulated Image", "LO"); // Retired
		add(0x00280051, "Corrected Image", "CS");
		add(0x0028005F, "Compression Recognition Code", "LO"); // Retired
		add(0x00280060, "Compression Code", "CS"); // Retired
		add(0x00280061, "Compression Originator", "SH"); // Retired
		add(0x00280062, "Compression Label", "LO"); // Retired
		add(0x00280063, "Compression Description", "SH"); // Retired
		add(0x00280065, "Compression Sequence", "CS"); // Retired
		add(0x00280066, "Compression Step Pointers", "AT"); // Retired
		add(0x00280068, "Repeat Interval", "US"); // Retired
		add(0x00280069, "Bits Grouped", "US"); // Retired
		add(0x00280070, "Perimeter Table", "US"); // Retired
		// add(0x00280071, "Perimeter Value", "US or SS"); //Retired
		add(0x00280080, "Predictor Rows", "US"); // Retired
		add(0x00280081, "Predictor Columns", "US"); // Retired
		add(0x00280082, "Predictor Constants", "US"); // Retired
		add(0x00280090, "Blocked Pixels", "CS"); // Retired
		add(0x00280091, "Block Rows", "US"); // Retired
		add(0x00280092, "Block Columns", "US"); // Retired
		add(0x00280093, "Row Overlap", "US"); // Retired
		add(0x00280094, "Column Overlap", "US"); // Retired
		add(0x00280100, "Bits Allocated", "US");
		add(0x00280101, "Bits Stored", "US");
		add(0x00280102, "High Bit", "US");
		add(0x00280103, "Pixel Representation", "US");
		// add(0x00280104, "Smallest Valid Pixel Value", "US or SS"); //Retired
		// add(0x00280105, "Largest Valid Pixel Value", "US or SS"); //Retired
		// add(0x00280106, "Smallest Image Pixel Value", "US or SS");
		// add(0x00280107, "Largest Image Pixel Value", "US or SS");
		// add(0x00280108, "Smallest Pixel Value in Series", "US or SS");
		// add(0x00280109, "Largest Pixel Value in Series", "US or SS");
		// add(0x00280110, "Smallest Image Pixel Value inPlane", "US or SS1");
		// //Retired
		// add(0x00280111, "Largest Image Pixel Value in Plane", "US or SS");
		// //Retired
		// add(0x00280120, "Pixel Padding Value", "US or SS");
		// add(0x00280121, "Pixel Padding Range Limit", "US or SS");
		add(0x00280122, "Float Pixel Padding Value", "FL");
		add(0x00280123, "Double Float Pixel Padding Value", "FD");
		add(0x00280124, "Float Pixel Padding Range Limit", "FL");
		add(0x00280125, "Double Float Pixel Padding RangeLimit", "FD");
		add(0x00280200, "Image Location", "US"); // Retired
		add(0x00280300, "Quality Control Image", "CS");
		add(0x00280301, "Burned In Annotation", "CS");
		add(0x00280302, "Recognizable Visual Features", "CS");
		add(0x00280303, "Longitudinal Temporal InformationModified", "CS");
		add(0x00280304, "Referenced Color Palette InstanceUID", "UI");
		add(0x00280400, "Transform Label", "LO"); // Retired
		add(0x00280401, "Transform Version Number", "LO"); // Retired
		add(0x00280402, "Number of Transform Steps", "US"); // Retired
		add(0x00280403, "Sequence of Compressed Data", "LO"); // Retired
		add(0x00280404, "Details of Coefficients", "AT"); // Retired
		add(0x00280700, "DCT Label", "LO"); // Retired
		add(0x00280701, "Data Block Description", "CS"); // Retired
		add(0x00280702, "Data Block", "AT"); // Retired
		add(0x00280710, "Normalization Factor Format", "US"); // Retired
		add(0x00280720, "Zonal Map Number Format", "US"); // Retired
		add(0x00280721, "Zonal Map Location", "AT"); // Retired
		add(0x00280722, "Zonal Map Format", "US"); // Retired
		add(0x00280730, "Adaptive Map Format", "US"); // Retired
		add(0x00280740, "Code Number Format", "US"); // Retired
		add(0x00280A02, "Pixel Spacing Calibration Type", "CS");
		add(0x00280A04, "Pixel Spacing CalibrationDescription", "LO");
		add(0x00281040, "Pixel Intensity Relationship", "CS");
		add(0x00281041, "Pixel Intensity Relationship Sign", "SS");
		add(0x00281050, "Window Center", "DS");
		add(0x00281051, "Window Width", "DS");
		add(0x00281052, "Rescale Intercept", "DS");
		add(0x00281053, "Rescale Slope", "DS");
		add(0x00281054, "Rescale Type", "LO");
		add(0x00281055, "Window Center & WidthExplanation", "LO");
		add(0x00281056, "VOI LUT Function", "CS");
		add(0x00281080, "Gray Scale", "CS"); // Retired
		add(0x00281090, "Recommended Viewing Mode", "CS");
		// add(0x00281100, "Gray Lookup Table Descriptor", "US or SS"); //Retired
		// add(0x00281101, "Red Palette Color Lookup TableDescriptor", "US or SS");
		// add(0x00281102, "Green Palette Color Lookup TableDescriptor", "US or
		// SS");
		// add(0x00281103, "Blue Palette Color Lookup TableDescriptor", "US or SS");
		add(0x00281104, "Alpha Palette Color Lookup TableDescriptor", "US");
		// add(0x00281111, "Large Red Palette Color LookupTable Descriptor", "US or
		// SS"); //Retired
		// add(0x00281112, "Large Green Palette Color LookupTable Descriptor", "US
		// or SS"); //Retired
		// add(0x00281113, "Large Blue Palette Color LookupTable Descriptor", "US or
		// SS"); //Retired
		add(0x00281199, "Palette Color Lookup Table UID", "UI");
		// add(0x00281200, "Gray Lookup Table Data", "US or SS or OW"); //Retired
		add(0x00281201, "Red Palette Color Lookup TableData", "OW");
		add(0x00281202, "Green Palette Color Lookup TableData", "OW");
		add(0x00281203, "Blue Palette Color Lookup TableData", "OW");
		add(0x00281204, "Alpha Palette Color Lookup TableData", "OW");
		add(0x00281211, "Large Red Palette Color LookupTable Data", "OW"); // Retired
		add(0x00281212, "Large Green Palette Color LookupTable Data", "OW"); // Retired
		add(0x00281213, "Large Blue Palette Color LookupTable Data", "OW"); // Retired
		add(0x00281214, "Large Palette Color Lookup TableUID", "UI"); // Retired
		add(0x00281221, "Segmented Red Palette ColorLookup Table Data", "OW");
		add(0x00281222, "Segmented Green Palette ColorLookup Table Data", "OW");
		add(0x00281223, "Segmented Blue Palette ColorLookup Table Data", "OW");
		add(0x00281300, "Breast Implant Present", "CS");
		add(0x00281350, "Partial View", "CS");
		add(0x00281351, "Partial View Description", "ST");
		add(0x00281352, "Partial View Code Sequence", "SQ");
		add(0x0028135A, "Spatial Locations Preserved", "CS");
		add(0x00281401, "Data Frame Assignment Sequence", "SQ");
		add(0x00281402, "Data Path Assignment", "CS");
		add(0x00281403, "Bits Mapped to Color Lookup Table", "US");
		add(0x00281404, "Blending LUT 1 Sequence", "SQ");
		add(0x00281405, "Blending LUT 1 Transfer Function", "CS");
		add(0x00281406, "Blending Weight Constant", "FD");
		add(0x00281407, "Blending Lookup Table Descriptor", "US");
		add(0x00281408, "Blending Lookup Table Data", "OW");
		add(0x0028140B, "Enhanced Palette Color LookupTable Sequence", "SQ");
		add(0x0028140C, "Blending LUT 2 Sequence", "SQ");
		add(0x0028140D,
			"Blending LUT 2 Transfer Function  BlendingLUT2TransferFunction  CS  1 (0028,140E) Data Path ID",
			"CS");
		add(0x0028140F, "RGB LUT Transfer Function", "CS");
		add(0x00281410, "Alpha LUT Transfer Function", "CS");
		add(0x00282000, "ICC Profile", "OB");
		add(0x00282110, "Lossy Image Compression", "CS");
		add(0x00282112, "Lossy Image Compression Ratio", "DS");
		add(0x00282114, "Lossy Image Compression Method", "CS");
		add(0x00283000, "Modality LUT Sequence", "SQ");
		// add(0x00283002, "LUT Descriptor", "US or SS");
		add(0x00283003, "LUT Explanation", "LO");
		add(0x00283004, "Modality LUT Type", "LO");
		// add(0x00283006, "LUT Data", "US or OW");
		add(0x00283010, "VOI LUT Sequence", "SQ");
		add(0x00283110, "Softcopy VOI LUT Sequence", "SQ");
		add(0x00284000, "Image Presentation Comments", "LT"); // Retired
		add(0x00285000, "Bi-Plane Acquisition Sequence", "SQ"); // Retired
		add(0x00286010, "Representative Frame Number", "US");
		add(0x00286020, "Frame Numbers of Interest (FOI)", "US");
		add(0x00286022, "Frame of Interest Description", "LO");
		add(0x00286023, "Frame of Interest Type", "CS");
		add(0x00286030, "Mask Pointer(s)", "US"); // Retired
		add(0x00286040, "R Wave Pointer", "US");
		add(0x00286100, "Mask Subtraction Sequence", "SQ");
		add(0x00286101, "Mask Operation", "CS");
		add(0x00286102, "Applicable Frame Range", "US");
		add(0x00286110, "Mask Frame Numbers", "US");
		add(0x00286112, "Contrast Frame Averaging", "US");
		add(0x00286114, "Mask Sub-pixel Shift", "FL");
		add(0x00286120, "TID Offset", "SS");
		add(0x00286190, "Mask Operation Explanation", "ST");
		add(0x00287000, "Equipment Administrator Sequence", "SQ");
		add(0x00287001, "Number of Display Subsystems", "US");
		add(0x00287002, "Current Configuration ID", "US");
		add(0x00287003, "Display Subsystem ID", "US");
		add(0x00287004, "Display Subsystem Name", "SH");
		add(0x00287005, "Display Subsystem Description", "LO");
		add(0x00287006, "System Status", "CS");
		add(0x00287007, "System Status Comment", "LO");
		add(0x00287008, "Target Luminance CharacteristicsSequence", "SQ");
		add(0x00287009, "Luminance Characteristics ID", "US");
		add(0x0028700A, "Display Subsystem ConfigurationSequence", "SQ");
		add(0x0028700B, "Configuration ID", "US");
		add(0x0028700C, "Configuration Name", "SH");
		add(0x0028700D, "Configuration Description", "LO");
		add(0x0028700E, "Referenced Target LuminanceCharacteristics ID", "US");
		add(0x0028700F, "QA Results Sequence", "SQ");
		add(0x00287010, "Display Subsystem QA ResultsSequence", "SQ");
		add(0x00287011, "Configuration QA ResultsSequence", "SQ");
		add(0x00287012, "Measurement EquipmentSequence", "SQ");
		add(0x00287013, "Measurement Functions", "CS");
		add(0x00287014, "Measurement Equipment Type", "CS");
		add(0x00287015, "Visual Evaluation Result Sequence", "SQ");
		add(0x00287016, "Display Calibration ResultSequence", "SQ");
		add(0x00287017, "DDL Value", "US");
		add(0x00287018, "CIExy White Point", "FL");
		add(0x00287019, "Display Function Type", "CS");
		add(0x0028701A, "Gamma Value", "FL");
		add(0x0028701B, "Number of Luminance Points", "US");
		add(0x0028701C, "Luminance Response Sequence", "SQ");
		add(0x0028701D, "Target Minimum Luminance", "FL");
		add(0x0028701E, "Target Maximum Luminance", "FL");
		add(0x0028701F, "Luminance Value", "FL");
		add(0x00287020, "Luminance Response Description", "LO");
		add(0x00287021, "White Point Flag", "CS");
		add(0x00287022, "Display Device Type CodeSequence", "SQ");
		add(0x00287023, "Display Subsystem Sequence", "SQ");
		add(0x00287024, "Luminance Result Sequence", "SQ");
		add(0x00287025, "Ambient Light Value Source", "CS");
		add(0x00287026, "Measured Characteristics", "CS");
		add(0x00287027, "Luminance Uniformity ResultSequence", "SQ");
		add(0x00287028, "Visual Evaluation Test Sequence", "SQ");
		add(0x00287029, "Test Result", "CS");
		add(0x0028702A, "Test Result Comment", "LO");
		add(0x0028702B, "Test Image Validation", "CS");
		add(0x0028702C, "Test Pattern Code Sequence", "SQ");
		add(0x0028702D, "Measurement Pattern CodeSequence", "SQ");
		add(0x0028702E, "Visual Evaluation Method CodeSequence", "SQ");
		add(0x00287FE0, "Pixel Data Provider URL", "UR");
		add(0x00289001, "Data Point Rows", "UL");
		add(0x00289002, "Data Point Columns", "UL");
		add(0x00289003, "Signal Domain Columns", "CS");
		add(0x00289099, "Largest Monochrome Pixel Value", "US"); // Retired
		add(0x00289108, "Data Representation", "CS");
		add(0x00289110, "Pixel Measures Sequence", "SQ");
		add(0x00289132, "Frame VOI LUT Sequence", "SQ");
		add(0x00289145, "Pixel Value TransformationSequence", "SQ");
		add(0x00289235, "Signal Domain Rows", "CS");
		add(0x00289411, "Display Filter Percentage", "FL");
		add(0x00289415, "Frame Pixel Shift Sequence", "SQ");
		add(0x00289416, "Subtraction Item ID", "US");
		add(0x00289422, "Pixel Intensity Relationship LUTSequence", "SQ");
		add(0x00289443, "Frame Pixel Data PropertiesSequence", "SQ");
		add(0x00289444, "Geometrical Properties", "CS");
		add(0x00289445, "Geometric Maximum Distortion", "FL");
		add(0x00289446, "Image Processing Applied", "CS");
		add(0x00289454, "Mask Selection Mode", "CS");
		add(0x00289474, "LUT Function", "CS");
		add(0x00289478, "Mask Visibility Percentage", "FL");
		add(0x00289501, "Pixel Shift Sequence", "SQ");
		add(0x00289502, "Region Pixel Shift Sequence", "SQ");
		add(0x00289503, "Vertices of the Region", "SS");
		add(0x00289505, "Multi-frame Presentation Sequence", "SQ");
		add(0x00289506, "Pixel Shift Frame Range", "US");
		add(0x00289507, "LUT Frame Range", "US");
		add(0x00289520, "Image to Equipment MappingMatrix", "DS");
		add(0x00289537, "Equipment Coordinate SystemIdentification", "CS");
	}

	/**
	 * Adds attributes of group 0x0032.
	 */
	private void addAttributeGroup0032() {
		add(0x0032000A, "Study Status ID", "CS"); // Retired
		add(0x0032000C, "Study Priority ID", "CS"); // Retired
		add(0x00320012, "Study ID Issuer", "LO"); // Retired
		add(0x00320032, "Study Verified Date", "DA"); // Retired
		add(0x00320033, "Study Verified Time", "TM"); // Retired
		add(0x00320034, "Study Read Date", "DA"); // Retired
		add(0x00320035, "Study Read Time", "TM"); // Retired
		add(0x00321000, "Scheduled Study Start Date", "DA"); // Retired
		add(0x00321001, "Scheduled Study Start Time", "TM"); // Retired
		add(0x00321010, "Scheduled Study Stop Date", "DA"); // Retired
		add(0x00321011, "Scheduled Study Stop Time", "TM"); // Retired
		add(0x00321020, "Scheduled Study Location", "LO"); // Retired
		add(0x00321021, "Scheduled Study Location AE Title", "AE"); // Retired
		add(0x00321030, "Reason for Study", "LO"); // Retired
		add(0x00321031, "Requesting Physician IdentificationSequence", "SQ");
		add(0x00321032, "Requesting Physician", "PN");
		add(0x00321033, "Requesting Service", "LO");
		add(0x00321034, "Requesting Service CodeSequence", "SQ");
		add(0x00321040, "Study Arrival Date", "DA"); // Retired
		add(0x00321041, "Study Arrival Time", "TM"); // Retired
		add(0x00321050, "Study Completion Date", "DA"); // Retired
		add(0x00321051, "Study Completion Time", "TM"); // Retired
		add(0x00321055, "Study Component Status ID", "CS"); // Retired
		add(0x00321060, "Requested Procedure Description", "LO");
		add(0x00321064, "Requested Procedure CodeSequence", "SQ");
		add(0x00321070, "Requested Contrast Agent", "LO");
		add(0x00324000, "Study Comments", "LT"); // Retired
	}

	/**
	 * Adds attributes of group 0x0038.
	 */
	private void addAttributeGroup0038() {
		add(0x00380004, "Referenced Patient Alias Sequence", "SQ");
		add(0x00380008, "Visit Status ID", "CS");
		add(0x00380010, "Admission ID", "LO");
		add(0x00380011, "Issuer of Admission ID", "LO"); // Retired
		add(0x00380014, "Issuer of Admission ID Sequence", "SQ");
		add(0x00380016, "Route of Admissions", "LO");
		add(0x0038001A, "Scheduled Admission Date", "DA"); // Retired
		add(0x0038001B, "Scheduled Admission Time", "TM"); // Retired
		add(0x0038001C, "Scheduled Discharge Date", "DA"); // Retired
		add(0x0038001D, "Scheduled Discharge Time", "TM"); // Retired
		add(0x0038001E, "Scheduled Patient InstitutionResidence", "LO"); // Retired
		add(0x00380020, "Admitting Date", "DA");
		add(0x00380021, "Admitting Time", "TM");
		add(0x00380030, "Discharge Date", "DA"); // Retired
		add(0x00380032, "Discharge Time", "TM"); // Retired
		add(0x00380040, "Discharge Diagnosis Description", "LO"); // Retired
		add(0x00380044, "Discharge Diagnosis CodeSequence", "SQ"); // Retired
		add(0x00380050, "Special Needs", "LO");
		add(0x00380060, "Service Episode ID", "LO");
		add(0x00380061, "Issuer of Service Episode ID", "LO"); // Retired
		add(0x00380062, "Service Episode Description", "LO");
		add(0x00380064, "Issuer of Service Episode IDSequence", "SQ");
		add(0x00380100, "Pertinent Documents Sequence", "SQ");
		add(0x00380101, "Pertinent Resources Sequence", "SQ");
		add(0x00380102, "Resource Description", "LO");
		add(0x00380300, "Current Patient Location", "LO");
		add(0x00380400, "Patient's Institution Residence", "LO");
		add(0x00380500, "Patient State", "LO");
		add(0x00380502, "Patient Clinical Trial ParticipationSequence", "SQ");
		add(0x00384000, "Visit Comments", "LT");
	}

	/**
	 * Adds attributes of group 0x003A.
	 */
	private void addAttributeGroup003A() {
		add(0x00400001, "Scheduled Station AE Title", "AE");
		add(0x00400002, "Scheduled Procedure Step StartDate", "DA");
		add(0x00400003, "Scheduled Procedure Step StartTime", "TM");
		add(0x00400004, "Scheduled Procedure Step EndDate", "DA");
		add(0x00400005, "Scheduled Procedure Step EndTime", "TM");
		add(0x00400006, "Scheduled Performing Physician'sName", "PN");
		add(0x00400007, "Scheduled Procedure StepDescription", "LO");
		add(0x00400008, "Scheduled Protocol CodeSequence", "SQ");
		add(0x00400009, "Scheduled Procedure Step ID", "SH");
		add(0x0040000A, "Stage Code Sequence", "SQ");
		add(0x0040000B, "Scheduled Performing PhysicianIdentification Sequence",
			"SQ");
		add(0x00400010, "Scheduled Station Name", "SH");
		add(0x00400011, "Scheduled Procedure StepLocation", "SH");
		add(0x00400012, "Pre-Medication", "LO");
		add(0x00400020, "Scheduled Procedure Step Status", "CS");
		add(0x00400026, "Order Placer Identifier Sequence", "SQ");
		add(0x00400027, "Order Filler Identifier Sequence", "SQ");
		add(0x00400031, "Local Namespace Entity ID", "UT");
		add(0x00400032, "Universal Entity ID", "UT");
		add(0x00400033, "Universal Entity ID Type", "CS");
		add(0x00400035, "Identifier Type Code", "CS");
		add(0x00400036, "Assigning Facility Sequence", "SQ");
		add(0x00400039, "Assigning Jurisdiction CodeSequence", "SQ");
		// add(0x0040003A, "Assigning Agency or DepartmentCode Sequence", "SQ");
		add(0x00400100, "Scheduled Procedure StepSequence", "SQ");
		add(0x00400220, "Referenced Non-Image CompositeSOP Instance Sequence",
			"SQ");
		add(0x00400241, "Performed Station AE Title", "AE");
		add(0x00400242, "Performed Station Name", "SH");
		add(0x00400243, "Performed Location", "SH");
		add(0x00400244, "Performed Procedure Step StartDate", "DA");
		add(0x00400245, "Performed Procedure Step StartTime", "TM");
		add(0x00400250, "Performed Procedure Step EndDate", "DA");
		add(0x00400251, "Performed Procedure Step EndTime", "TM");
		add(0x00400252, "Performed Procedure Step Status", "CS");
		add(0x00400253, "Performed Procedure Step ID", "SH");
		add(0x00400254, "Performed Procedure StepDescription", "LO");
		add(0x00400255, "Performed Procedure TypeDescription", "LO");
		add(0x00400260, "Performed Protocol CodeSequence", "SQ");
		add(0x00400261, "Performed Protocol Type", "CS");
		add(0x00400270, "Scheduled Step AttributesSequence", "SQ");
		add(0x00400275, "Request Attributes Sequence", "SQ");
		add(0x00400280, "Comments on the PerformedProcedure Step", "ST");
		add(0x00400281,
			"Performed Procedure StepDiscontinuation Reason CodeSequence", "SQ");
		add(0x00400293, "Quantity Sequence", "SQ");
		add(0x00400294, "Quantity", "DS");
		add(0x00400295, "Measuring Units Sequence", "SQ");
		add(0x00400296, "Billing Item Sequence", "SQ");
		add(0x00400300, "Total Time of Fluoroscopy", "US");
		add(0x00400301, "Total Number of Exposures", "US");
		add(0x00400302, "Entrance Dose", "US");
		add(0x00400303, "Exposed Area", "US");
		add(0x00400306, "Distance Source to Entrance", "DS");
		add(0x00400307, "Distance Source to Support", "DS"); // Retired
		add(0x0040030E, "Exposure Dose Sequence", "SQ");
		add(0x00400310, "Comments on Radiation Dose", "ST");
		add(0x00400312, "X-Ray Output", "DS");
		add(0x00400314, "Half Value Layer", "DS");
		add(0x00400316, "Organ Dose", "DS");
		add(0x00400318, "Organ Exposed", "CS");
		add(0x00400320, "Billing Procedure Step Sequence", "SQ");
		add(0x00400321, "Film Consumption Sequence", "SQ");
		add(0x00400324, "Billing Supplies and DevicesSequence", "SQ");
		add(0x00400330, "Referenced Procedure StepSequence", "SQ"); // Retired
		add(0x00400340, "Performed Series Sequence", "SQ");
		add(0x00400400, "Comments on the ScheduledProcedure Step", "LT");
		add(0x00400440, "Protocol Context Sequence", "SQ");
		add(0x00400441, "Content Item Modifier Sequence", "SQ");
		add(0x00400500, "Scheduled Specimen Sequence", "SQ");
		add(0x0040050A, "Specimen Accession Number", "LO"); // Retired
		add(0x00400512, "Container Identifier", "LO");
		add(0x00400513, "Issuer of the Container IdentifierSequence", "SQ");
		add(0x00400515, "Alternate Container IdentifierSequence", "SQ");
		add(0x00400518, "Container Type Code Sequence", "SQ");
		add(0x0040051A, "Container Description", "LO");
		add(0x00400520, "Container Component Sequence", "SQ");
		add(0x00400550, "Specimen Sequence", "SQ"); // Retired
		add(0x00400551, "Specimen Identifier", "LO");
		add(0x00400552, "Specimen Description Sequence(Trial)", "SQ"); // Retired
		add(0x00400553, "Specimen Description (Trial)", "ST"); // Retired
		add(0x00400554, "Specimen UID", "UI");
		add(0x00400555, "Acquisition Context Sequence", "SQ");
		add(0x00400556, "Acquisition Context Description", "ST");
		add(0x0040059A, "Specimen Type Code Sequence", "SQ");
		add(0x00400560, "Specimen Description Sequence", "SQ");
		add(0x00400562, "Issuer of the Specimen IdentifierSequence", "SQ");
		add(0x00400600, "Specimen Short Description", "LO");
		add(0x00400602, "Specimen Detailed Description", "UT");
		add(0x00400610, "Specimen Preparation Sequence", "SQ");
		add(0x00400612, "Specimen Preparation StepContent Item Sequence", "SQ");
		add(0x00400620, "Specimen Localization ContentItem Sequence", "SQ");
		add(0x004006FA, "Slide Identifier", "LO"); // Retired
		add(0x0040071A, "Image Center Point CoordinatesSequence", "SQ");
		add(0x0040072A, "X Offset in Slide CoordinateSystem", "DS");
		add(0x0040073A, "Y Offset in Slide CoordinateSystem", "DS");
		add(0x0040074A, "Z Offset in Slide CoordinateSystem", "DS");
		add(0x004008D8, "Pixel Spacing Sequence", "SQ"); // Retired
		add(0x004008DA, "Coordinate System Axis CodeSequence", "SQ"); // Retired
		add(0x004008EA, "Measurement Units CodeSequence", "SQ");
		add(0x004009F8, "Vital Stain Code Sequence (Trial)", "SQ"); // Retired
		add(0x00401001, "Requested Procedure ID", "SH");
		add(0x00401002, "Reason for the RequestedProcedure", "LO");
		add(0x00401003, "Requested Procedure Priority", "SH");
		add(0x00401004, "Patient Transport Arrangements", "LO");
		add(0x00401005, "Requested Procedure Location", "LO");
		add(0x00401006, "Placer Order Number / Procedure", "SH"); // Retired
		add(0x00401007, "Filler Order Number / Procedure", "SH"); // Retired
		add(0x00401008, "Confidentiality Code", "LO");
		add(0x00401009, "Reporting Priority", "SH");
		add(0x0040100A, "Reason for Requested ProcedureCode Sequence", "SQ");
		add(0x00401010, "Names of Intended Recipients ofResults", "PN");
		add(0x00401011, "Intended Recipients of ResultsIdentification Sequence",
			"SQ");
		add(0x00401012, "Reason For Performed ProcedureCode Sequence", "SQ");
		add(0x00401060, "Requested Procedure Description(Trial)", "LO"); // Retired
		add(0x00401101, "Person Identification CodeSequence", "SQ");
		add(0x00401102, "Person's Address", "ST");
		add(0x00401103, "Person's Telephone Numbers", "LO");
		add(0x00401104, "Person's Telecom Information", "LT");
		add(0x00401400, "Requested Procedure Comments", "LT");
		add(0x00402001, "Reason for the Imaging ServiceRequest", "LO"); // Retired
		add(0x00402004, "Issue Date of Imaging ServiceRequest", "DA");
		add(0x00402005, "Issue Time of Imaging ServiceRequest", "TM");
		add(0x00402006, "Placer Order Number / ImagingService Request (Retired)",
			"SH"); // Retired
		add(0x00402007, "Filler Order Number / ImagingService Request (Retired)",
			"SH"); // Retired
		add(0x00402008, "Order Entered By", "PN");
		add(0x00402009, "Order Enterer's Location", "SH");
		add(0x00402010, "Order Callback Phone Number", "SH");
		add(0x00402011, "Order Callback TelecomInformation", "LT");
		add(0x00402016, "Placer Order Number / ImagingService Request", "LO");
		add(0x00402017, "Filler Order Number / ImagingService Request", "LO");
		add(0x00402400, "Imaging Service RequestComments", "LT");
		add(0x00403001, "Confidentiality Constraint onPatient Data Description",
			"LO");
		add(0x00404001, "General Purpose ScheduledProcedure Step Status", "CS"); // Retired
		add(0x00404002, "General Purpose PerformedProcedure Step Status", "CS"); // Retired
		add(0x00404003, "General Purpose ScheduledProcedure Step Priority", "CS"); // Retired
		add(0x00404004, "Scheduled Processing ApplicationsCode Sequence", "SQ"); // Retired
		add(0x00404005, "Scheduled Procedure Step StartDateTime", "DT");
		add(0x00404006, "Multiple Copies Flag", "CS"); // Retired
		add(0x00404007, "Performed Processing ApplicationsCode Sequence", "SQ");
		add(0x00404009, "Human Performer Code Sequence", "SQ");
		add(0x00404010, "Scheduled Procedure StepModification DateTime", "DT");
		add(0x00404011, "Expected Completion DateTime", "DT");
		add(0x00404015,
			"Resulting General PurposePerformed Procedure StepsSequence", "SQ"); // Retired
		add(0x00404016,
			"Referenced General PurposeScheduled Procedure StepSequence", "SQ"); // Retired
		add(0x00404018, "Scheduled Workitem CodeSequence", "SQ");
		add(0x00404019, "Performed Workitem CodeSequence", "SQ");
		add(0x00404020, "Input Availability Flag", "CS");
		add(0x00404021, "Input Information Sequence", "SQ");
		add(0x00404022, "Relevant Information Sequence", "SQ"); // Retired
		add(0x00404023,
			"Referenced General PurposeScheduled Procedure StepTransaction UID",
			"UI"); // Retired
		add(0x00404025, "Scheduled Station Name CodeSequence", "SQ");
		add(0x00404026, "Scheduled Station Class CodeSequence", "SQ");
		add(0x00404027, "Scheduled Station GeographicLocation Code Sequence", "SQ");
		add(0x00404028, "Performed Station Name CodeSequence", "SQ");
		add(0x00404029, "Performed Station Class CodeSequence", "SQ");
		add(0x00404030, "Performed Station GeographicLocation Code Sequence", "SQ");
		add(0x00404031, "Requested Subsequent WorkitemCode Sequence", "SQ"); // Retired
		add(0x00404032, "Non-DICOM Output CodeSequence", "SQ"); // Retired
		add(0x00404033, "Output Information Sequence", "SQ");
		add(0x00404034, "Scheduled Human PerformersSequence", "SQ");
		add(0x00404035, "Actual Human PerformersSequence", "SQ");
		add(0x00404036, "Human Performer's Organization", "LO");
		add(0x00404037, "Human Performer's Name", "PN");
		add(0x00404040, "Raw Data Handling", "CS");
		add(0x00404041, "Input Readiness State", "CS");
		add(0x00404050, "Performed Procedure Step StartDateTime", "DT");
		add(0x00404051, "Performed Procedure Step EndDateTime", "DT");
		add(0x00404052, "Procedure Step CancellationDateTime", "DT");
		add(0x00408302, "Entrance Dose in mGy", "DS");
		add(0x00409092, "Parametric Map Frame TypeSequence", "SQ");
		add(0x00409094, "Referenced Image Real WorldValue Mapping Sequence", "SQ");
		add(0x00409096, "Real World Value MappingSequence", "SQ");
		add(0x00409098, "Pixel Value Mapping CodeSequence", "SQ");
		add(0x00409210, "LUT Label", "SH");
		// add(0x00409211, "Real World Value Last ValueMapped", "US or SS");
		add(0x00409212, "Real World Value LUT Data", "FD");
		// add(0x00409216, "Real World Value First ValueMapped", "US or SS");
		add(0x00409220, "Quantity Definition Sequence", "SQ");
		add(0x00409224, "Real World Value Intercept", "FD");
		add(0x00409225, "Real World Value Slope", "FD");
		add(0x0040A007, "Findings Flag (Trial)", "CS"); // Retired
		add(0x0040A010, "Relationship Type", "CS");
		add(0x0040A020, "Findings Sequence (Trial)", "SQ"); // Retired
		add(0x0040A021, "Findings Group UID (Trial)", "UI"); // Retired
		add(0x0040A022, "Referenced Findings Group UID(Trial)", "UI"); // Retired
		add(0x0040A023, "Findings Group Recording Date(Trial)", "DA"); // Retired
		add(0x0040A024, "Findings Group Recording Time(Trial)", "TM"); // Retired
		add(0x0040A026, "Findings Source Category CodeSequence (Trial)", "SQ"); // Retired
		add(0x0040A027, "Verifying Organization", "LO");
		add(0x0040A028, "Documenting OrganizationIdentifier Code Sequence (Trial)",
			"SQ"); // Retired
		add(0x0040A030, "Verification DateTime", "DT");
		add(0x0040A032, "Observation DateTime", "DT");
		add(0x0040A040, "Value Type", "CS");
		add(0x0040A043, "Concept Name Code Sequence", "SQ");
		add(0x0040A047, "Measurement Precision Description(Trial)", "LO"); // Retired
		add(0x0040A050, "Continuity Of Content", "CS");
		// add(0x0040A057, "Urgency or Priority Alerts (Trial)", "CS"); //Retired
		add(0x0040A060, "Sequencing Indicator (Trial)", "LO"); // Retired
		add(0x0040A066, "Document Identifier CodeSequence (Trial)", "SQ"); // Retired
		add(0x0040A067, "Document Author (Trial)", "PN"); // Retired
		add(0x0040A068, "Document Author Identifier CodeSequence (Trial)", "SQ"); // Retired
		add(0x0040A070, "Identifier Code Sequence (Trial)", "SQ"); // Retired
		add(0x0040A073, "Verifying Observer Sequence", "SQ");
		add(0x0040A074, "Object Binary Identifier (Trial)", "OB"); // Retired
		add(0x0040A075, "Verifying Observer Name", "PN");
		add(0x0040A076, "Documenting Observer IdentifierCode Sequence (Trial)",
			"SQ"); // Retired
		add(0x0040A078, "Author Observer Sequence", "SQ");
		add(0x0040A07A, "Participant Sequence", "SQ");
		add(0x0040A07C, "Custodial Organization Sequence", "SQ");
		add(0x0040A080, "Participation Type", "CS");
		add(0x0040A082, "Participation DateTime", "DT");
		add(0x0040A084, "Observer Type", "CS");
		add(0x0040A085, "Procedure Identifier CodeSequence (Trial)", "SQ"); // Retired
		add(0x0040A088, "Verifying Observer IdentificationCode Sequence", "SQ");
		add(0x0040A089, "Object Directory Binary Identifier(Trial)", "OB"); // Retired
		add(0x0040A090, "Equivalent CDA DocumentSequence", "SQ"); // Retired
		add(0x0040A0B0, "Referenced Waveform Channels", "US");
		// add(0x0040A110, "Date of Document or VerbalTransaction (Trial)", "DA");
		// //Retired
		add(0x0040A112, "Time of Document Creation orVerbal Transaction (Trial)",
			"TM"); // Retired
		add(0x0040A120, "DateTime", "DT");
		add(0x0040A121, "Date", "DA");
		add(0x0040A122, "Time", "TM");
		add(0x0040A123, "Person Name", "PN");
		add(0x0040A124, "UID", "UI");
		add(0x0040A125, "Report Status ID (Trial)", "CS"); // Retired
		add(0x0040A130, "Temporal Range Type", "CS");
		add(0x0040A132, "Referenced Sample Positions", "UL");
		add(0x0040A136, "Referenced Frame Numbers", "US");
		add(0x0040A138, "Referenced Time Offsets", "DS");
		add(0x0040A13A, "Referenced DateTime", "DT");
		add(0x0040A160, "Text Value", "UT");
		add(0x0040A161, "Floating Point Value", "FD");
		add(0x0040A162, "Rational Numerator Value", "SL");
		add(0x0040A163, "Rational Denominator Value", "UL");
		add(0x0040A167, "Observation Category CodeSequence (Trial)", "SQ"); // Retired
		add(0x0040A168, "Concept Code Sequence", "SQ");
		add(0x0040A16A, "Bibliographic Citation (Trial)", "ST"); // Retired
		add(0x0040A170, "Purpose of Reference CodeSequence", "SQ");
		add(0x0040A171, "Observation UID", "UI");
		add(0x0040A172, "Referenced Observation UID (Trial)", "UI"); // Retired
		add(0x0040A173, "Referenced Observation Class(Trial)", "CS"); // Retired
		add(0x0040A174, "Referenced Object ObservationClass (Trial)", "CS"); // Retired
		add(0x0040A180, "Annotation Group Number", "US");
		add(0x0040A192, "Observation Date (Trial)", "DA"); // Retired
		add(0x0040A193, "Observation Time (Trial)", "TM"); // Retired
		add(0x0040A194, "Measurement Automation (Trial)", "CS"); // Retired
		add(0x0040A195, "Modifier Code Sequence", "SQ");
		add(0x0040A224, "Identification Description (Trial)", "ST"); // Retired
		add(0x0040A290, "Coordinates Set Geometric Type(Trial)", "CS"); // Retired
		add(0x0040A296, "Algorithm Code Sequence (Trial)", "SQ"); // Retired
		add(0x0040A297, "Algorithm Description (Trial)", "ST"); // Retired
		add(0x0040A29A, "Pixel Coordinates Set (Trial)", "SL"); // Retired
		add(0x0040A300, "Measured Value Sequence", "SQ");
		add(0x0040A301, "Numeric Value Qualifier CodeSequence", "SQ");
		add(0x0040A307, "Current Observer (Trial)", "PN"); // Retired
		add(0x0040A30A, "Numeric Value", "DS");
		add(0x0040A313, "Referenced Accession Sequence(Trial)", "SQ"); // Retired
		add(0x0040A33A, "Report Status Comment (Trial)", "ST"); // Retired
		add(0x0040A340, "Procedure Context Sequence(Trial)", "SQ"); // Retired
		add(0x0040A352, "Verbal Source (Trial)", "PN"); // Retired
		add(0x0040A353, "Address (Trial)", "ST"); // Retired
		add(0x0040A354, "Telephone Number (Trial)", "LO"); // Retired
		add(0x0040A358, "Verbal Source Identifier CodeSequence (Trial)", "SQ"); // Retired
		add(0x0040A360, "Predecessor Documents Sequence", "SQ");
		add(0x0040A370, "Referenced Request Sequence", "SQ");
		add(0x0040A372, "Performed Procedure CodeSequence", "SQ");
		add(0x0040A375, "Current Requested ProcedureEvidence Sequence", "SQ");
		add(0x0040A380, "Report Detail Sequence (Trial)", "SQ"); // Retired
		add(0x0040A385, "Pertinent Other EvidenceSequence", "SQ");
		add(0x0040A390, "HL7 Structured DocumentReference Sequence", "SQ");
		add(0x0040A402, "Observation Subject UID (Trial)", "UI"); // Retired
		add(0x0040A403, "Observation Subject Class (Trial)", "CS"); // Retired
		add(0x0040A404, "Observation Subject Type CodeSequence (Trial)", "SQ"); // Retired
		add(0x0040A491, "Completion Flag", "CS");
		add(0x0040A492, "Completion Flag Description", "LO");
		add(0x0040A493, "Verification Flag", "CS");
		add(0x0040A494, "Archive Requested", "CS");
		add(0x0040A496, "Preliminary Flag", "CS");
		add(0x0040A504, "Content Template Sequence", "SQ");
		add(0x0040A525, "Identical Documents Sequence", "SQ");
		add(0x0040A600, "Observation Subject Context Flag(Trial)", "CS"); // Retired
		add(0x0040A601, "Observer Context Flag (Trial)", "CS"); // Retired
		add(0x0040A603, "Procedure Context Flag (Trial)", "CS"); // Retired
		add(0x0040A730, "Content Sequence", "SQ");
		add(0x0040A731, "Relationship Sequence (Trial)", "SQ"); // Retired
		add(0x0040A732, "Relationship Type Code Sequence(Trial)", "SQ"); // Retired
		add(0x0040A744, "Language Code Sequence (Trial)", "SQ"); // Retired
		add(0x0040A992, "Uniform Resource Locator (Trial)", "ST"); // Retired
		add(0x0040B020, "Waveform Annotation Sequence", "SQ");
		add(0x0040DB00, "Template Identifier", "CS");
		add(0x0040DB06, "Template Version", "DT"); // Retired
		add(0x0040DB07, "Template Local Version", "DT"); // Retired
		add(0x0040DB0B, "Template Extension Flag", "CS"); // Retired
		add(0x0040DB0C, "Template Extension OrganizationUID", "UI"); // Retired
		add(0x0040DB0D, "Template Extension Creator UID", "UI"); // Retired
		add(0x0040DB73, "Referenced Content Item Identifier", "UL");
		add(0x0040E001, "HL7 Instance Identifier", "ST");
		add(0x0040E004, "HL7 Document Effective Time", "DT");
		add(0x0040E006, "HL7 Document Type CodeSequence", "SQ");
		add(0x0040E008, "Document Class Code Sequence", "SQ");
		add(0x0040E010, "Retrieve URI", "UR");
		add(0x0040E011, "Retrieve Location UID", "UI");
		add(0x0040E020, "Type of Instances", "CS");
		add(0x0040E021, "DICOM Retrieval Sequence", "SQ");
		add(0x0040E022, "DICOM Media Retrieval Sequence", "SQ");
		add(0x0040E023, "WADO Retrieval Sequence", "SQ");
		add(0x0040E024, "XDS Retrieval Sequence", "SQ");
		add(0x0040E025, "WADO-RS Retrieval Sequence", "SQ");
		add(0x0040E030, "Repository Unique ID", "UI");
		add(0x0040E031, "Home Community ID", "UI");
	}

	/**
	 * Adds attributes of group 0x0042.
	 */
	private void addAttributeGroup0042() {
		add(0x00420010, "Document Title", "ST");
		add(0x00420011, "Encapsulated Document", "OB");
		add(0x00420012, "MIME Type of EncapsulatedDocument", "LO");
		add(0x00420013, "Source Instance Sequence", "SQ");
		add(0x00420014, "List of MIME Types", "LO");
	}

	/**
	 * Adds attributes of group 0x0044.
	 */
	private void addAttributeGroup0044() {
		add(0x00440001, "Product Package Identifier", "ST");
		add(0x00440002, "Substance Administration Approval", "CS");
		add(0x00440003, "Approval Status FurtherDescription", "LT");
		add(0x00440004, "Approval Status DateTime", "DT");
		add(0x00440007, "Product Type Code Sequence", "SQ");
		add(0x00440008, "Product Name", "LO");
		add(0x00440009, "Product Description", "LT");
		add(0x0044000A, "Product Lot Identifier", "LO");
		add(0x0044000B, "Product Expiration DateTime", "DT");
		add(0x00440010, "Substance AdministrationDateTime", "DT");
		add(0x00440011, "Substance Administration Notes", "LO");
		add(0x00440012, "Substance Administration DeviceID", "LO");
		add(0x00440013, "Product Parameter Sequence", "SQ");
		add(0x00440019, "Substance AdministrationParameter Sequence", "SQ");
	}

	/**
	 * Adds attributes of group 0x0046.
	 */
	private void addAttributeGroup0046() {
		add(0x00460012, "Lens Description", "LO");
		add(0x00460014, "Right Lens Sequence", "SQ");
		add(0x00460015, "Left Lens Sequence", "SQ");
		add(0x00460016, "Unspecified Laterality LensSequence", "SQ");
		add(0x00460018, "Cylinder Sequence", "SQ");
		add(0x00460028, "Prism Sequence", "SQ");
		add(0x00460030, "Horizontal Prism Power", "FD");
		add(0x00460032, "Horizontal Prism Base", "CS");
		add(0x00460034, "Vertical Prism Power", "FD");
		add(0x00460036, "Vertical Prism Base", "CS");
		add(0x00460038, "Lens Segment Type", "CS");
		add(0x00460040, "Optical Transmittance", "FD");
		add(0x00460042, "Channel Width", "FD");
		add(0x00460044, "Pupil Size", "FD");
		add(0x00460046, "Corneal Size", "FD");
		add(0x00460050, "Autorefraction Right Eye Sequence", "SQ");
		add(0x00460052, "Autorefraction Left Eye Sequence", "SQ");
		add(0x00460060, "Distance Pupillary Distance", "FD");
		add(0x00460062, "Near Pupillary Distance", "FD");
		add(0x00460063, "Intermediate Pupillary Distance", "FD");
		add(0x00460064, "Other Pupillary Distance", "FD");
		add(0x00460070, "Keratometry Right Eye Sequence", "SQ");
		add(0x00460071, "Keratometry Left Eye Sequence", "SQ");
		add(0x00460074, "Steep Keratometric Axis Sequence", "SQ");
		add(0x00460075, "Radius of Curvature", "FD");
		add(0x00460076, "Keratometric Power", "FD");
		add(0x00460077, "Keratometric Axis", "FD");
		add(0x00460080, "Flat Keratometric Axis Sequence", "SQ");
		add(0x00460092, "Background Color", "CS");
		add(0x00460094, "Optotype", "CS");
		add(0x00460095, "Optotype Presentation", "CS");
		add(0x00460097, "Subjective Refraction Right EyeSequence", "SQ");
		add(0x00460098, "Subjective Refraction Left EyeSequence", "SQ");
		add(0x00460100, "Add Near Sequence", "SQ");
		add(0x00460101, "Add Intermediate Sequence", "SQ");
		add(0x00460102, "Add Other Sequence", "SQ");
		add(0x00460104, "Add Power", "FD");
		add(0x00460106, "Viewing Distance", "FD");
		add(0x00460121, "Visual Acuity Type Code Sequence", "SQ");
		add(0x00460122, "Visual Acuity Right Eye Sequence", "SQ");
		add(0x00460123, "Visual Acuity Left Eye Sequence", "SQ");
		add(0x00460124, "Visual Acuity Both Eyes OpenSequence", "SQ");
		add(0x00460125, "Viewing Distance Type", "CS");
		add(0x00460135, "Visual Acuity Modifiers", "SS");
		add(0x00460137, "Decimal Visual Acuity", "FD");
		add(0x00460139, "Optotype Detailed Definition", "LO");
		add(0x00460145, "Referenced RefractiveMeasurements Sequence", "SQ");
		add(0x00460146, "Sphere Power", "FD");
		add(0x00460147, "Cylinder Power", "FD");
		add(0x00460201, "Corneal Topography Surface", "CS");
		add(0x00460202, "Corneal Vertex Location", "FL");
		add(0x00460203, "Pupil Centroid X-Coordinate", "FL");
		add(0x00460204, "Pupil Centroid Y-Coordinate", "FL");
		add(0x00460205, "Equivalent Pupil Radius", "FL");
		add(0x00460207, "Corneal Topography Map TypeCode Sequence", "SQ");
		add(0x00460208, "Vertices of the Outline of Pupil", "IS");
		add(0x00460210, "Corneal Topography MappingNormals Sequence", "SQ");
		add(0x00460211, "Maximum Corneal CurvatureSequence", "SQ");
		add(0x00460212, "Maximum Corneal Curvature", "FL");
		add(0x00460213, "Maximum Corneal CurvatureLocation", "FL");
		add(0x00460215, "Minimum Keratometric Sequence", "SQ");
		add(0x00460218, "Simulated Keratometric CylinderSequence", "SQ");
		add(0x00460220, "Average Corneal Power", "FL");
		add(0x00460224, "Corneal I-S Value", "FL");
		add(0x00460227, "Analyzed Area", "FL");
		add(0x00460230, "Surface Regularity Index", "FL");
		add(0x00460232, "Surface Asymmetry Index", "FL");
		add(0x00460234, "Corneal Eccentricity Index", "FL");
		add(0x00460236, "Keratoconus Prediction Index", "FL");
		add(0x00460238, "Decimal Potential Visual Acuity", "FL");
		add(0x00460242, "Corneal Topography Map QualityEvaluation", "CS");
		add(0x00460244, "Source Image Corneal ProcessedData Sequence", "SQ");
		add(0x00460247, "Corneal Point Location", "FL");
		add(0x00460248, "Corneal Point Estimated", "CS");
		add(0x00460249, "Axial Power", "FL");
		add(0x00460250, "Tangential Power", "FL");
		add(0x00460251, "Refractive Power", "FL");
		add(0x00460252, "Relative Elevation", "FL");
		add(0x00460253, "Corneal Wavefront", "FL");
	}

	/**
	 * Adds attributes of group 0x0048.
	 */
	private void addAttributeGroup0048() {
		add(0x00480001, "Imaged Volume Width", "FL");
		add(0x00480002, "Imaged Volume Height", "FL");
		add(0x00480003, "Imaged Volume Depth", "FL");
		add(0x00480006, "Total Pixel Matrix Columns", "UL");
		add(0x00480007, "Total Pixel Matrix Rows", "UL");
		add(0x00480008, "Total Pixel Matrix Origin Sequence", "SQ");
		add(0x00480010, "Specimen Label in Image", "CS");
		add(0x00480011, "Focus Method", "CS");
		add(0x00480012, "Extended Depth of Field", "CS");
		add(0x00480013, "Number of Focal Planes", "US");
		add(0x00480014, "Distance Between Focal Planes", "FL");
		add(0x00480015, "Recommended Absent PixelCIELab Value", "US");
		add(0x00480100, "Illuminator Type Code Sequence", "SQ");
		add(0x00480102, "Image Orientation (Slide)", "DS");
		add(0x00480105, "Optical Path Sequence", "SQ");
		add(0x00480106, "Optical Path Identifier", "SH");
		add(0x00480107, "Optical Path Description", "ST");
		add(0x00480108, "Illumination Color Code Sequence", "SQ");
		add(0x00480110, "Specimen Reference Sequence", "SQ");
		add(0x00480111, "Condenser Lens Power", "DS");
		add(0x00480112, "Objective Lens Power", "DS");
		add(0x00480113, "Objective Lens Numerical Aperture", "DS");
		add(0x00480120, "Palette Color Lookup TableSequence", "SQ");
		add(0x00480200, "Referenced Image NavigationSequence", "SQ");
		add(0x00480201, "Top Left Hand Corner of LocalizerArea", "US");
		add(0x00480202, "Bottom Right Hand Corner ofLocalizer Area", "US");
		add(0x00480207, "Optical Path IdentificationSequence", "SQ");
		add(0x0048021A, "Plane Position (Slide) Sequence", "SQ");
		add(0x0048021E, "Column Position In Total ImagePixel Matrix", "SL");
		add(0x0048021F, "Row Position In Total Image PixelMatrix", "SL");
		add(0x00480301, "Pixel Origin Interpretation", "CS");
		add(0x00480001, "Imaged Volume Width", "FL");
		add(0x00480002, "Imaged Volume Height", "FL");
		add(0x00480003, "Imaged Volume Depth", "FL");
		add(0x00480006, "Total Pixel Matrix Columns", "UL");
		add(0x00480007, "Total Pixel Matrix Rows", "UL");
		add(0x00480008, "Total Pixel Matrix Origin Sequence", "SQ");
		add(0x00480010, "Specimen Label in Image", "CS");
		add(0x00480011, "Focus Method", "CS");
		add(0x00480012, "Extended Depth of Field", "CS");
		add(0x00480013, "Number of Focal Planes", "US");
		add(0x00480014, "Distance Between Focal Planes", "FL");
		add(0x00480015, "Recommended Absent PixelCIELab Value", "US");
		add(0x00480100, "Illuminator Type Code Sequence", "SQ");
		add(0x00480102, "Image Orientation (Slide)", "DS");
		add(0x00480105, "Optical Path Sequence", "SQ");
		add(0x00480106, "Optical Path Identifier", "SH");
		add(0x00480107, "Optical Path Description", "ST");
		add(0x00480108, "Illumination Color Code Sequence", "SQ");
		add(0x00480110, "Specimen Reference Sequence", "SQ");
		add(0x00480111, "Condenser Lens Power", "DS");
		add(0x00480112, "Objective Lens Power", "DS");
		add(0x00480113, "Objective Lens Numerical Aperture", "DS");
		add(0x00480120, "Palette Color Lookup TableSequence", "SQ");
		add(0x00480200, "Referenced Image NavigationSequence", "SQ");
		add(0x00480201, "Top Left Hand Corner of LocalizerArea", "US");
		add(0x00480202, "Bottom Right Hand Corner ofLocalizer Area", "US");
		add(0x00480207, "Optical Path IdentificationSequence", "SQ");
		add(0x0048021A, "Plane Position (Slide) Sequence", "SQ");
		add(0x0048021E, "Column Position In Total ImagePixel Matrix", "SL");
		add(0x0048021F, "Row Position In Total Image PixelMatrix", "SL");
		add(0x00480301, "Pixel Origin Interpretation", "CS");
	}

	/**
	 * Adds attributes of group 0x0050.
	 */
	private void addAttributeGroup0050() {
		add(0x00500004, "Calibration Image", "CS");
		add(0x00500010, "Device Sequence", "SQ");
		add(0x00500012, "Container Component Type CodeSequence", "SQ");
		add(0x00500013, "Container Component Thickness", "FD");
		add(0x00500014, "Device Length", "DS");
		add(0x00500015, "Container Component Width", "FD");
		add(0x00500016, "Device Diameter", "DS");
		add(0x00500017, "Device Diameter Units", "CS");
		add(0x00500018, "Device Volume", "DS");
		add(0x00500019, "Inter-Marker Distance", "DS");
		add(0x0050001A, "Container Component Material", "CS");
		add(0x0050001B, "Container Component ID", "LO");
		add(0x0050001C, "Container Component Length", "FD");
		add(0x0050001D, "Container Component Diameter", "FD");
		add(0x0050001E, "Container Component Description", "LO");
		add(0x00500020, "Device Description", "LO");
	}

	/**
	 * Adds attributes of group 0x0052.
	 */
	private void addAttributeGroup0052() {
		add(0x00520001, "Contrast/Bolus Ingredient Percentby Volume", "FL");
		add(0x00520002, "OCT Focal Distance", "FD");
		add(0x00520003, "Beam Spot Size", "FD");
		add(0x00520004, "Effective Refractive Index", "FD");
		add(0x00520006, "OCT Acquisition Domain", "CS");
		add(0x00520007, "OCT Optical Center Wavelength", "FD");
		add(0x00520008, "Axial Resolution", "FD");
		add(0x00520009, "Ranging Depth", "FD");
		add(0x00520011, "A-line Rate", "FD");
		add(0x00520012, "A-lines Per Frame", "US");
		add(0x00520013, "Catheter Rotational Rate", "FD");
		add(0x00520014, "A-line Pixel Spacing", "FD");
		add(0x00520016, "Mode of Percutaneous AccessSequence", "SQ");
		add(0x00520025, "Intravascular OCT Frame TypeSequence", "SQ");
		add(0x00520026, "OCT Z Offset Applied", "CS");
		add(0x00520027, "Intravascular Frame ContentSequence", "SQ");
		add(0x00520028, "Intravascular Longitudinal Distance", "FD");
		add(0x00520029, "Intravascular OCT Frame ContentSequence", "SQ");
		add(0x00520030, "OCT Z Offset Correction", "SS");
		add(0x00520031, "Catheter Direction of Rotation", "CS");
		add(0x00520033, "Seam Line Location", "FD");
		add(0x00520034, "First A-line Location", "FD");
		add(0x00520036, "Seam Line Index", "US");
		add(0x00520038, "Number of Padded A-lines", "US");
		add(0x00520039, "Interpolation Type", "CS");
		add(0x0052003A, "Refractive Index Applied", "CS");
	}

	/**
	 * Adds attributes of group 0x0054.
	 */
	private void addAttributeGroup0054() {
		add(0x00540010, "Energy Window Vector", "US");
		add(0x00540011, "Number of Energy Windows", "US");
		add(0x00540012, "Energy Window InformationSequence", "SQ");
		add(0x00540013, "Energy Window Range Sequence", "SQ");
		add(0x00540014, "Energy Window Lower Limit", "DS");
		add(0x00540015, "Energy Window Upper Limit", "DS");
		add(0x00540016, "Radiopharmaceutical InformationSequence", "SQ");
		add(0x00540017, "Residual Syringe Counts", "IS");
		add(0x00540018, "Energy Window Name", "SH");
		add(0x00540020, "Detector Vector", "US");
		add(0x00540021, "Number of Detectors", "US");
		add(0x00540022, "Detector Information Sequence", "SQ");
		add(0x00540030, "Phase Vector", "US");
		add(0x00540031, "Number of Phases", "US");
		add(0x00540032, "Phase Information Sequence", "SQ");
		add(0x00540033, "Number of Frames in Phase", "US");
		add(0x00540036, "Phase Delay", "IS");
		add(0x00540038, "Pause Between Frames", "IS");
		add(0x00540039, "Phase Description", "CS");
		add(0x00540050, "Rotation Vector", "US");
		add(0x00540051, "Number of Rotations", "US");
		add(0x00540052, "Rotation Information Sequence", "SQ");
		add(0x00540053, "Number of Frames in Rotation", "US");
		add(0x00540060, "R-R Interval Vector", "US");
		add(0x00540061, "Number of R-R Intervals", "US");
		add(0x00540062, "Gated Information Sequence", "SQ");
		add(0x00540063, "Data Information Sequence", "SQ");
		add(0x00540070, "Time Slot Vector", "US");
		add(0x00540071, "Number of Time Slots", "US");
		add(0x00540072, "Time Slot Information Sequence", "SQ");
		add(0x00540073, "Time Slot Time", "DS");
		add(0x00540080, "Slice Vector", "US");
		add(0x00540081, "Number of Slices", "US");
		add(0x00540090, "Angular View Vector", "US");
		add(0x00540100, "Time Slice Vector", "US");
		add(0x00540101, "Number of Time Slices", "US");
		add(0x00540200, "Start Angle", "DS");
		add(0x00540202, "Type of Detector Motion", "CS");
		add(0x00540210, "Trigger Vector", "IS");
		add(0x00540211, "Number of Triggers in Phase", "US");
		add(0x00540220, "View Code Sequence", "SQ");
		add(0x00540222, "View Modifier Code Sequence", "SQ");
		add(0x00540300, "Radionuclide Code Sequence", "SQ");
		add(0x00540302, "Administration Route CodeSequence", "SQ");
		add(0x00540304, "Radiopharmaceutical CodeSequence", "SQ");
		add(0x00540306, "Calibration Data Sequence", "SQ");
		add(0x00540308, "Energy Window Number", "US");
		add(0x00540400, "Image ID", "SH");
		add(0x00540410, "Patient Orientation Code Sequence", "SQ");
		add(0x00540412, "Patient Orientation Modifier CodeSequence", "SQ");
		add(0x00540414, "Patient Gantry Relationship CodeSequence", "SQ");
		add(0x00540500, "Slice Progression Direction", "CS");
		add(0x00540501, "Scan Progression Direction", "CS");
		add(0x00541000, "Series Type", "CS");
		add(0x00541001, "Units", "CS");
		add(0x00541002, "Counts Source", "CS");
		add(0x00541004, "Reprojection Method", "CS");
		add(0x00541006, "SUV Type", "CS");
		add(0x00541100, "Randoms Correction Method", "CS");
		add(0x00541101, "Attenuation Correction Method", "LO");
		add(0x00541102, "Decay Correction", "CS");
		add(0x00541103, "Reconstruction Method", "LO");
		add(0x00541104, "Detector Lines of Response Used", "LO");
		add(0x00541105, "Scatter Correction Method", "LO");
		add(0x00541200, "Axial Acceptance", "DS");
		add(0x00541201, "Axial Mash", "IS");
		add(0x00541202, "Transverse Mash", "IS");
		add(0x00541203, "Detector Element Size", "DS");
		add(0x00541210, "Coincidence Window Width", "DS");
		add(0x00541220, "Secondary Counts Type", "CS");
		add(0x00541300, "Frame Reference Time", "DS");
		add(0x00541310, "Primary (Prompts) CountsAccumulated", "IS");
		add(0x00541311, "Secondary Counts Accumulated", "IS");
		add(0x00541320, "Slice Sensitivity Factor", "DS");
		add(0x00541321, "Decay Factor", "DS");
		add(0x00541322, "Dose Calibration Factor", "DS");
		add(0x00541323, "Scatter Fraction Factor", "DS");
		add(0x00541324, "Dead Time Factor", "DS");
		add(0x00541330, "Image Index", "US");
		add(0x00541400, "Counts Included", "CS"); // Retired
		add(0x00541401, "Dead Time Correction Flag", "CS"); // Retired
	}

	/**
	 * Adds attributes of group 0x0060.
	 */
	private void addAttributeGroup0060() {
		add(0x00603000, "Histogram Sequence", "SQ");
		add(0x00603002, "Histogram Number of Bins", "US");
		// add(0x00603004, "Histogram First Bin Value", "US or SS");
		// add(0x00603006, "Histogram Last Bin Value", "US or SS");
		add(0x00603008, "Histogram Bin Width", "US");
		add(0x00603010, "Histogram Explanation", "LO");
		add(0x00603020, "Histogram Data", "UL");
	}

	/**
	 * Adds attributes of group 0x0062.
	 */
	private void addAttributeGroup0062() {
		add(0x00620001, "Segmentation Type", "CS");
		add(0x00620002, "Segment Sequence", "SQ");
		add(0x00620003, "Segmented Property CategoryCode Sequence", "SQ");
		add(0x00620004, "Segment Number", "US");
		add(0x00620005, "Segment Label", "LO");
		add(0x00620006, "Segment Description", "ST");
		add(0x00620008, "Segment Algorithm Type", "CS");
		add(0x00620009, "Segment Algorithm Name", "LO");
		add(0x0062000A, "Segment Identification Sequence", "SQ");
		add(0x0062000B, "Referenced Segment Number", "US");
		add(0x0062000C, "Recommended Display GrayscaleValue", "US");
		add(0x0062000D, "Recommended Display CIELabValue", "US");
		add(0x0062000E, "Maximum Fractional Value", "US");
		add(0x0062000F, "Segmented Property Type CodeSequence", "SQ");
		add(0x00620010, "Segmentation Fractional Type", "CS");
		add(0x00620011, "Segmented Property Type ModifierCode Sequence", "SQ");
		add(0x00620012, "Used Segments Sequence", "SQ");
	}

	/**
	 * Adds attributes of group 0x0064.
	 */
	private void addAttributeGroup0064() {
		add(0x00640002, "Deformable Registration Sequence", "SQ");
		add(0x00640003, "Source Frame of Reference UID", "UI");
		add(0x00640005, "Deformable Registration GridSequence", "SQ");
		add(0x00640007, "Grid Dimensions", "UL");
		add(0x00640008, "Grid Resolution", "FD");
		add(0x00640009, "Vector Grid Data", "OF");
		add(0x0064000F, "Pre Deformation MatrixRegistration Sequence", "SQ");
		add(0x00640010, "Post Deformation MatrixRegistration Sequence", "SQ");
	}

	/**
	 * Adds attributes of group 0x0066.
	 */
	private void addAttributeGroup0066() {
		add(0x00660001, "Number of Surfaces", "UL");
		add(0x00660002, "Surface Sequence", "SQ");
		add(0x00660003, "Surface Number", "UL");
		add(0x00660004, "Surface Comments", "LT");
		add(0x00660009, "Surface Processing", "CS");
		add(0x0066000A, "Surface Processing Ratio", "FL");
		add(0x0066000B, "Surface Processing Description", "LO");
		add(0x0066000C, "Recommended PresentationOpacity", "FL");
		add(0x0066000D, "Recommended Presentation Type", "CS");
		add(0x0066000E, "Finite Volume", "CS");
		add(0x00660010, "Manifold", "CS");
		add(0x00660011, "Surface Points Sequence", "SQ");
		add(0x00660012, "Surface Points Normals Sequence", "SQ");
		add(0x00660013, "Surface Mesh Primitives Sequence", "SQ");
		add(0x00660015, "Number of Surface Points", "UL");
		add(0x00660016, "Point Coordinates Data", "OF");
		add(0x00660017, "Point Position Accuracy", "FL");
		add(0x00660018, "Mean Point Distance", "FL");
		add(0x00660019, "Maximum Point Distance", "FL");
		add(0x0066001A, "Points Bounding Box Coordinates", "FL");
		add(0x0066001B, "Axis of Rotation", "FL");
		add(0x0066001C, "Center of Rotation", "FL");
		add(0x0066001E, "Number of Vectors", "UL");
		add(0x0066001F, "Vector Dimensionality", "US");
		add(0x00660020, "Vector Accuracy", "FL");
		add(0x00660021, "Vector Coordinate Data", "OF");
		add(0x00660023, "Triangle Point Index List", "OW");
		add(0x00660024, "Edge Point Index List", "OW");
		add(0x00660025, "Vertex Point Index List", "OW");
		add(0x00660026, "Triangle Strip Sequence", "SQ");
		add(0x00660027, "Triangle Fan Sequence", "SQ");
		add(0x00660028, "Line Sequence", "SQ");
		add(0x00660029, "Primitive Point Index List", "OW");
		add(0x0066002A, "Surface Count", "UL");
		add(0x0066002B, "Referenced Surface Sequence", "SQ");
		add(0x0066002C, "Referenced Surface Number", "UL");
		add(0x0066002D,
			"Segment Surface GenerationAlgorithm Identification Sequence", "SQ");
		add(0x0066002E, "Segment Surface Source InstanceSequence", "SQ");
		add(0x0066002F, "Algorithm Family Code Sequence", "SQ");
		add(0x00660030, "Algorithm Name Code Sequence", "SQ");
		add(0x00660031, "Algorithm Version", "LO");
		add(0x00660032, "Algorithm Parameters", "LT");
		add(0x00660034, "Facet Sequence", "SQ");
		add(0x00660035, "Surface Processing AlgorithmIdentification Sequence",
			"SQ");
		add(0x00660036, "Algorithm Name", "LO");
		add(0x00660037, "Recommended Point Radius", "FL");
		add(0x00660038, "Recommended Line Thickness", "FL");
		add(0x00660040, "Long Primitive Point Index List", "UL");
		add(0x00660041, "Long Triangle Point Index List", "UL");
		add(0x00660042, "Long Edge Point Index List", "UL");
		add(0x00660043, "Long Vertex Point Index List", "UL");
	}

	/**
	 * Adds attributes of group 0x0068.
	 */
	private void addAttributeGroup0068() {
		add(0x00686210, "Implant Size", "LO");
		add(0x00686221, "Implant Template Version", "LO");
		add(0x00686222, "Replaced Implant TemplateSequence", "SQ");
		add(0x00686223, "Implant Type", "CS");
		add(0x00686224, "Derivation Implant TemplateSequence", "SQ");
		add(0x00686225, "Original Implant TemplateSequence", "SQ");
		add(0x00686226, "Effective DateTime", "DT");
		add(0x00686230, "Implant Target Anatomy Sequence", "SQ");
		add(0x00686260, "Information From ManufacturerSequence", "SQ");
		add(0x00686265, "Notification From ManufacturerSequence", "SQ");
		add(0x00686270, "Information Issue DateTime", "DT");
		add(0x00686280, "Information Summary", "ST");
		add(0x006862A0, "Implant Regulatory DisapprovalCode Sequence", "SQ");
		add(0x006862A5, "Overall Template Spatial Tolerance", "FD");
		add(0x006862C0, "HPGL Document Sequence", "SQ");
		add(0x006862D0, "HPGL Document ID", "US");
		add(0x006862D5, "HPGL Document Label", "LO");
		add(0x006862E0, "View Orientation Code Sequence", "SQ");
		add(0x006862F0, "View Orientation Modifier", "FD");
		add(0x006862F2, "HPGL Document Scaling", "FD");
		add(0x00686300, "HPGL Document", "OB");
		add(0x00686310, "HPGL Contour Pen Number", "US");
		add(0x00686320, "HPGL Pen Sequence", "SQ");
		add(0x00686330, "HPGL Pen Number", "US");
		add(0x00686340, "HPGL Pen Label", "LO");
		add(0x00686345, "HPGL Pen Description", "ST");
		add(0x00686346, "Recommended Rotation Point", "FD");
		add(0x00686347, "Bounding Rectangle", "FD");
		add(0x00686350, "Implant Template 3D ModelSurface Number", "US");
		add(0x00686360, "Surface Model DescriptionSequence", "SQ");
		add(0x00686380, "Surface Model Label", "LO");
		add(0x00686390, "Surface Model Scaling Factor", "FD");
		add(0x006863A0, "Materials Code Sequence", "SQ");
		add(0x006863A4, "Coating Materials Code Sequence", "SQ");
		add(0x006863A8, "Implant Type Code Sequence", "SQ");
		add(0x006863AC, "Fixation Method Code Sequence", "SQ");
		add(0x006863B0, "Mating Feature Sets Sequence", "SQ");
		add(0x006863C0, "Mating Feature Set ID", "US");
		add(0x006863D0, "Mating Feature Set Label", "LO");
		add(0x006863E0, "Mating Feature Sequence", "SQ");
		add(0x006863F0, "Mating Feature ID", "US");
		add(0x00686400, "Mating Feature Degree of FreedomSequence", "SQ");
		add(0x00686410, "Degree of Freedom ID", "US");
		add(0x00686420, "Degree of Freedom Type", "CS");
		add(0x00686430, "2D Mating Feature CoordinatesSequence", "SQ");
		add(0x00686440, "Referenced HPGL Document ID", "US");
		add(0x00686450, "2D Mating Point", "FD");
		add(0x00686460, "2D Mating Axes", "FD");
		add(0x00686470, "2D Degree of Freedom Sequence", "SQ");
		add(0x00686490, "3D Degree of Freedom Axis", "FD");
		add(0x006864A0, "Range of Freedom", "FD");
		add(0x006864C0, "3D Mating Point", "FD");
		add(0x006864D0, "3D Mating Axes", "FD");
		add(0x006864F0, "2D Degree of Freedom Axis", "FD");
		add(0x00686500, "Planning Landmark PointSequence", "SQ");
		add(0x00686510, "Planning Landmark Line Sequence", "SQ");
		add(0x00686520, "Planning Landmark PlaneSequence", "SQ");
		add(0x00686530, "Planning Landmark ID", "US");
		add(0x00686540, "Planning Landmark Description", "LO");
		add(0x00686545, "Planning Landmark IdentificationCode Sequence", "SQ");
		add(0x00686550, "2D Point Coordinates Sequence", "SQ");
		add(0x00686560, "2D Point Coordinates", "FD");
		add(0x00686590, "3D Point Coordinates", "FD");
		add(0x006865A0, "2D Line Coordinates Sequence", "SQ");
		add(0x006865B0, "2D Line Coordinates", "FD");
		add(0x006865D0, "3D Line Coordinates", "FD");
		add(0x006865E0, "2D Plane Coordinates Sequence", "SQ");
		add(0x006865F0, "2D Plane Intersection", "FD");
		add(0x00686610, "3D Plane Origin", "FD");
		add(0x00686620, "3D Plane Normal", "FD");
	}

	/**
	 * Adds attributes of group 0x0070.
	 */
	private void addAttributeGroup0070() {
		add(0x00700001, "Graphic Annotation Sequence", "SQ");
		add(0x00700002, "Graphic Layer", "CS");
		add(0x00700003, "Bounding Box Annotation Units", "CS");
		add(0x00700004, "Anchor Point Annotation Units", "CS");
		add(0x00700005, "Graphic Annotation Units", "CS");
		add(0x00700006, "Unformatted Text Value", "ST");
		add(0x00700008, "Text Object Sequence", "SQ");
		add(0x00700009, "Graphic Object Sequence", "SQ");
		add(0x00700010, "Bounding Box Top Left HandCorner", "FL");
		add(0x00700011, "Bounding Box Bottom Right HandCorner", "FL");
		add(0x00700012, "Bounding Box Text HorizontalJustification", "CS");
		add(0x00700014, "Anchor Point", "FL");
		add(0x00700015, "Anchor Point Visibility", "CS");
		add(0x00700020, "Graphic Dimensions", "US");
		add(0x00700021, "Number of Graphic Points", "US");
		add(0x00700022, "Graphic Data", "FL");
		add(0x00700023, "Graphic Type", "CS");
		add(0x00700024, "Graphic Filled", "CS");
		add(0x00700040, "Image Rotation (Retired)", "IS"); // Retired
		add(0x00700041, "Image Horizontal Flip", "CS");
		add(0x00700042, "Image Rotation", "US");
		add(0x00700050, "Displayed Area Top Left HandCorner (Trial)", "US"); // Retired
		add(0x00700051, "Displayed Area Bottom Right HandCorner (Trial)", "US"); // Retired
		add(0x00700052, "Displayed Area Top Left HandCorner", "SL");
		add(0x00700053, "Displayed Area Bottom Right HandCorner", "SL");
		add(0x0070005A, "Displayed Area SelectionSequence", "SQ");
		add(0x00700060, "Graphic Layer Sequence", "SQ");
		add(0x00700062, "Graphic Layer Order", "IS");
		add(0x00700066, "Graphic Layer RecommendedDisplay Grayscale Value", "US");
		add(0x00700067, "Graphic Layer RecommendedDisplay RGB Value", "US"); // Retired
		add(0x00700068, "Graphic Layer Description", "LO");
		add(0x00700080, "Content Label", "CS");
		add(0x00700081, "Content Description", "LO");
		add(0x00700082, "Presentation Creation Date", "DA");
		add(0x00700083, "Presentation Creation Time", "TM");
		add(0x00700084, "Content Creator's Name", "PN");
		add(0x00700086, "Content Creator's IdentificationCode Sequence", "SQ");
		add(0x00700087, "Alternate Content DescriptionSequence", "SQ");
		add(0x00700100, "Presentation Size Mode", "CS");
		add(0x00700101, "Presentation Pixel Spacing", "DS");
		add(0x00700102, "Presentation Pixel Aspect Ratio", "IS");
		add(0x00700103, "Presentation Pixel MagnificationRatio", "FL");
		add(0x00700207, "Graphic Group Label", "LO");
		add(0x00700208, "Graphic Group Description", "ST");
		add(0x00700209, "Compound Graphic Sequence", "SQ");
		add(0x00700226, "Compound Graphic Instance ID", "UL");
		add(0x00700227, "Font Name", "LO");
		add(0x00700228, "Font Name Type", "CS");
		add(0x00700229, "CSS Font Name", "LO");
		add(0x00700230, "Rotation Angle", "FD");
		add(0x00700231, "Text Style Sequence", "SQ");
		add(0x00700232, "Line Style Sequence", "SQ");
		add(0x00700233, "Fill Style Sequence", "SQ");
		add(0x00700234, "Graphic Group Sequence", "SQ");
		add(0x00700241, "Text Color CIELab Value", "US");
		add(0x00700242, "Horizontal Alignment", "CS");
		add(0x00700243, "Vertical Alignment", "CS");
		add(0x00700244, "Shadow Style", "CS");
		add(0x00700245, "Shadow Offset X", "FL");
		add(0x00700246, "Shadow Offset Y", "FL");
		add(0x00700247, "Shadow Color CIELab Value", "US");
		add(0x00700248, "Underlined", "CS");
		add(0x00700249, "Bold", "CS");
		add(0x00700250, "Italic", "CS");
		add(0x00700251, "Pattern On Color CIELab Value", "US");
		add(0x00700252, "Pattern Off Color CIELab Value", "US");
		add(0x00700253, "Line Thickness", "FL");
		add(0x00700254, "Line Dashing Style", "CS");
		add(0x00700255, "Line Pattern", "UL");
		add(0x00700256, "Fill Pattern", "OB");
		add(0x00700257, "Fill Mode", "CS");
		add(0x00700258, "Shadow Opacity", "FL");
		add(0x00700261, "Gap Length", "FL");
		add(0x00700262, "Diameter of Visibility", "FL");
		add(0x00700273, "Rotation Point", "FL");
		add(0x00700274, "Tick Alignment", "CS");
		add(0x00700278, "Show Tick Label", "CS");
		add(0x00700279, "Tick Label Alignment", "CS");
		add(0x00700282, "Compound Graphic Units", "CS");
		add(0x00700284, "Pattern On Opacity", "FL");
		add(0x00700285, "Pattern Off Opacity", "FL");
		add(0x00700287, "Major Ticks Sequence", "SQ");
		add(0x00700288, "Tick Position", "FL");
		add(0x00700289, "Tick Label", "SH");
		add(0x00700294, "Compound Graphic Type", "CS");
		add(0x00700295, "Graphic Group ID", "UL");
		add(0x00700306, "Shape Type", "CS");
		add(0x00700308, "Registration Sequence", "SQ");
		add(0x00700309, "Matrix Registration Sequence", "SQ");
		add(0x0070030A, "Matrix Sequence", "SQ");
		add(0x0070030C, "Frame of ReferenceTransformation Matrix Type", "CS");
		add(0x0070030D, "Registration Type Code Sequence", "SQ");
		add(0x0070030F, "Fiducial Description", "ST");
		add(0x00700310, "Fiducial Identifier", "SH");
		add(0x00700311, "Fiducial Identifier Code Sequence", "SQ");
		add(0x00700312, "Contour Uncertainty Radius", "FD");
		add(0x00700314, "Used Fiducials Sequence", "SQ");
		add(0x00700318, "Graphic Coordinates DataSequence", "SQ");
		add(0x0070031A, "Fiducial UID", "UI");
		add(0x0070031C, "Fiducial Set Sequence", "SQ");
		add(0x0070031E, "Fiducial Sequence", "SQ");
		add(0x00700401, "Graphic Layer RecommendedDisplay CIELab Value", "US");
		add(0x00700402, "Blending Sequence", "SQ");
		add(0x00700403, "Relative Opacity", "FL");
		add(0x00700404, "Referenced Spatial RegistrationSequence", "SQ");
		add(0x00700405, "Blending Position", "CS");
	}

	/**
	 * Adds attributes of group 0x0072.
	 */
	private void addAttributeGroup0072() {
		add(0x00720002, "Hanging Protocol Name", "SH");
		add(0x00720004, "Hanging Protocol Description", "LO");
		add(0x00720006, "Hanging Protocol Level", "CS");
		add(0x00720008, "Hanging Protocol Creator", "LO");
		add(0x0072000A, "Hanging Protocol CreationDateTime", "DT");
		add(0x0072000C, "Hanging Protocol DefinitionSequence", "SQ");
		add(0x0072000E, "Hanging Protocol UserIdentification Code Sequence", "SQ");
		add(0x00720010, "Hanging Protocol User GroupName", "LO");
		add(0x00720012, "Source Hanging ProtocolSequence", "SQ");
		add(0x00720014, "Number of Priors Referenced", "US");
		add(0x00720020, "Image Sets Sequence", "SQ");
		add(0x00720022, "Image Set Selector Sequence", "SQ");
		add(0x00720024, "Image Set Selector Usage Flag", "CS");
		add(0x00720026, "Selector Attribute", "AT");
		add(0x00720028, "Selector Value Number", "US");
		add(0x00720030, "Time Based Image Sets Sequence", "SQ");
		add(0x00720032, "Image Set Number", "US");
		add(0x00720034, "Image Set Selector Category", "CS");
		add(0x00720038, "Relative Time", "US");
		add(0x0072003A, "Relative Time Units", "CS");
		add(0x0072003C, "Abstract Prior Value", "SS");
		add(0x0072003E, "Abstract Prior Code Sequence", "SQ");
		add(0x00720040, "Image Set Label", "LO");
		add(0x00720050, "Selector Attribute VR", "CS");
		add(0x00720052, "Selector Sequence Pointer", "AT");
		add(0x00720054, "Selector Sequence Pointer PrivateCreator", "LO");
		add(0x00720056, "Selector Attribute Private Creator", "LO");
		add(0x00720060, "Selector AT Value", "AT");
		add(0x00720062, "Selector CS Value", "CS");
		add(0x00720064, "Selector IS Value", "IS");
		add(0x00720066, "Selector LO Value", "LO");
		add(0x00720068, "Selector LT Value", "LT");
		add(0x0072006A, "Selector PN Value", "PN");
		add(0x0072006C, "Selector SH Value", "SH");
		add(0x0072006E, "Selector ST Value", "ST");
		add(0x00720070, "Selector UT Value", "UT");
		add(0x00720072, "Selector DS Value", "DS");
		add(0x00720074, "Selector FD Value", "FD");
		add(0x00720076, "Selector FL Value", "FL");
		add(0x00720078, "Selector UL Value", "UL");
		add(0x0072007A, "Selector US Value", "US");
		add(0x0072007C, "Selector SL Value", "SL");
		add(0x0072007E, "Selector SS Value", "SS");
		add(0x0072007F, "Selector UI Value", "UI");
		add(0x00720080, "Selector Code Sequence Value", "SQ");
		add(0x00720100, "Number of Screens", "US");
		add(0x00720102, "Nominal Screen DefinitionSequence", "SQ");
		add(0x00720104, "Number of Vertical Pixels", "US");
		add(0x00720106, "Number of Horizontal Pixels", "US");
		add(0x00720108, "Display Environment SpatialPosition", "FD");
		add(0x0072010A, "Screen Minimum Grayscale BitDepth", "US");
		add(0x0072010C, "Screen Minimum Color Bit Depth", "US");
		add(0x0072010E, "Application Maximum Repaint Time", "US");
		add(0x00720200, "Display Sets Sequence", "SQ");
		add(0x00720202, "Display Set Number", "US");
		add(0x00720203, "Display Set Label", "LO");
		add(0x00720204, "Display Set Presentation Group", "US");
		add(0x00720206, "Display Set Presentation GroupDescription", "LO");
		add(0x00720208, "Partial Data Display Handling", "CS");
		add(0x00720210, "Synchronized Scrolling Sequence", "SQ");
		add(0x00720212, "Display Set Scrolling Group", "US");
		add(0x00720214, "Navigation Indicator Sequence", "SQ");
		add(0x00720216, "Navigation Display Set", "US");
		add(0x00720218, "Reference Display Sets", "US");
		add(0x00720300, "Image Boxes Sequence", "SQ");
		add(0x00720302, "Image Box Number", "US");
		add(0x00720304, "Image Box Layout Type", "CS");
		add(0x00720306, "Image Box Tile HorizontalDimension", "US");
		add(0x00720308, "Image Box Tile Vertical Dimension", "US");
		add(0x00720310, "Image Box Scroll Direction", "CS");
		add(0x00720312, "Image Box Small Scroll Type", "CS");
		add(0x00720314, "Image Box Small Scroll Amount", "US");
		add(0x00720316, "Image Box Large Scroll Type", "CS");
		add(0x00720318, "Image Box Large Scroll Amount", "US");
		add(0x00720320, "Image Box Overlap Priority", "US");
		add(0x00720330, "Cine Relative to Real-Time", "FD");
		add(0x00720400, "Filter Operations Sequence", "SQ");
		add(0x00720402, "Filter-by Category", "CS");
		add(0x00720404, "Filter-by Attribute Presence", "CS");
		add(0x00720406, "Filter-by Operator", "CS");
		add(0x00720420, "Structured Display BackgroundCIELab Value", "US");
		add(0x00720421, "Empty Image Box CIELab Value", "US");
		add(0x00720422, "Structured Display Image BoxSequence", "SQ");
		add(0x00720424, "Structured Display Text BoxSequence", "SQ");
		add(0x00720427, "Referenced First Frame Sequence", "SQ");
		add(0x00720430, "Image Box SynchronizationSequence", "SQ");
		add(0x00720432, "Synchronized Image Box List", "US");
		add(0x00720434, "Type of Synchronization", "CS");
		add(0x00720500, "Blending Operation Type", "CS");
		add(0x00720510, "Reformatting Operation Type", "CS");
		add(0x00720512, "Reformatting Thickness", "FD");
		add(0x00720514, "Reformatting Interval", "FD");
		add(0x00720516, "Reformatting Operation Initial ViewDirection", "CS");
		add(0x00720520, "3D Rendering Type", "CS");
		add(0x00720600, "Sorting Operations Sequence", "SQ");
		add(0x00720602, "Sort-by Category", "CS");
		add(0x00720604, "Sorting Direction", "CS");
		add(0x00720700, "Display Set Patient Orientation", "CS");
		add(0x00720702, "VOI Type", "CS");
		add(0x00720704, "Pseudo-Color Type", "CS");
		add(0x00720705, "Pseudo-Color Palette InstanceReference Sequence", "SQ");
		add(0x00720706, "Show Grayscale Inverted", "CS");
		add(0x00720710, "Show Image True Size Flag", "CS");
		add(0x00720712, "Show Graphic Annotation Flag", "CS");
		add(0x00720714, "Show Patient Demographics Flag", "CS");
		add(0x00720716, "Show Acquisition Techniques Flag", "CS");
		add(0x00720717, "Display Set Horizontal Justification", "CS");
		add(0x00720718, "Display Set Vertical Justification", "CS");
	}

	/**
	 * Adds attributes of group 0x0074.
	 */
	private void addAttributeGroup0074() {
		add(0x00740120, "Continuation Start Meterset", "FD");
		add(0x00740121, "Continuation End Meterset", "FD");
		add(0x00741000, "Procedure Step State", "CS");
		add(0x00741002, "Procedure Step ProgressInformation Sequence", "SQ");
		add(0x00741004, "Procedure Step Progress", "DS");
		add(0x00741006, "Procedure Step ProgressDescription", "ST");
		add(0x00741008, "Procedure Step CommunicationsURI Sequence", "SQ");
		add(0x0074100A, "Contact URI", "UR");
		add(0x0074100C, "Contact Display Name", "LO");
		add(0x0074100E, "Procedure Step DiscontinuationReason Code Sequence", "SQ");
		add(0x00741020, "Beam Task Sequence", "SQ");
		add(0x00741022, "Beam Task Type", "CS");
		add(0x00741024, "Beam Order Index (Trial)", "IS"); // Retired
		add(0x00741025, "Autosequence Flag", "CS");
		add(0x00741026, "Table Top Vertical AdjustedPosition", "FD");
		add(0x00741027, "Table Top Longitudinal AdjustedPosition", "FD");
		add(0x00741028, "Table Top Lateral AdjustedPosition", "FD");
		add(0x0074102A, "Patient Support Adjusted Angle", "FD");
		add(0x0074102B, "Table Top Eccentric AdjustedAngle", "FD");
		add(0x0074102C, "Table Top Pitch Adjusted Angle", "FD");
		add(0x0074102D, "Table Top Roll Adjusted Angle", "FD");
		add(0x00741030, "Delivery Verification ImageSequence", "SQ");
		add(0x00741032, "Verification Image Timing", "CS");
		add(0x00741034, "Double Exposure Flag", "CS");
		add(0x00741036, "Double Exposure Ordering", "CS");
		add(0x00741038, "Double Exposure Meterset (Trial)", "DS"); // Retired
		add(0x0074103A, "Double Exposure Field Delta (Trial)", "DS"); // Retired
		add(0x00741040, "Related Reference RT ImageSequence", "SQ");
		add(0x00741042, "General Machine VerificationSequence", "SQ");
		add(0x00741044, "Conventional Machine VerificationSequence", "SQ");
		add(0x00741046, "Ion Machine Verification Sequence", "SQ");
		add(0x00741048, "Failed Attributes Sequence", "SQ");
		add(0x0074104A, "Overridden Attributes Sequence", "SQ");
		add(0x0074104C, "Conventional Control PointVerification Sequence", "SQ");
		add(0x0074104E, "Ion Control Point VerificationSequence", "SQ");
		add(0x00741050, "Attribute Occurrence Sequence", "SQ");
		add(0x00741052, "Attribute Occurrence Pointer", "AT");
		add(0x00741054, "Attribute Item Selector", "UL");
		add(0x00741056, "Attribute Occurrence PrivateCreator", "LO");
		add(0x00741057, "Selector Sequence Pointer Items", "IS");
		add(0x00741200, "Scheduled Procedure Step Priority", "CS");
		add(0x00741202, "Worklist Label", "LO");
		add(0x00741204, "Procedure Step Label", "LO");
		add(0x00741210, "Scheduled Processing ParametersSequence", "SQ");
		add(0x00741212, "Performed Processing ParametersSequence", "SQ");
		add(0x00741216, "Unified Procedure Step PerformedProcedure Sequence", "SQ");
		add(0x00741220, "Related Procedure Step Sequence", "SQ"); // Retired
		add(0x00741222, "Procedure Step Relationship Type", "LO"); // Retired
		add(0x00741224, "Replaced Procedure StepSequence", "SQ");
		add(0x00741230, "Deletion Lock", "LO");
		add(0x00741234, "Receiving AE", "AE");
		add(0x00741236, "Requesting AE", "AE");
		add(0x00741238, "Reason for Cancellation", "LT");
		add(0x00741242, "SCP Status", "CS");
		add(0x00741244, "Subscription List Status", "CS");
		add(0x00741246, "Unified Procedure Step List Status", "CS");
		add(0x00741324, "Beam Order Index", "UL");
		add(0x00741338, "Double Exposure Meterset", "FD");
		add(0x0074133A, "Double Exposure Field Delta", "FD");
	}

	/**
	 * Adds attributes of group 0x0076.
	 */
	private void addAttributeGroup0076() {
		add(0x00760001, "Implant Assembly Template Name", "LO");
		add(0x00760003, "Implant Assembly Template Issuer", "LO");
		add(0x00760006, "Implant Assembly TemplateVersion", "LO");
		add(0x00760008, "Replaced Implant AssemblyTemplate Sequence", "SQ");
		add(0x0076000A, "Implant Assembly Template Type", "CS");
		add(0x0076000C, "Original Implant AssemblyTemplate Sequence", "SQ");
		add(0x0076000E, "Derivation Implant AssemblyTemplate Sequence", "SQ");
		add(0x00760010, "Implant Assembly Template TargetAnatomy Sequence", "SQ");
		add(0x00760020, "Procedure Type Code Sequence", "SQ");
		add(0x00760030, "Surgical Technique", "LO");
		add(0x00760032, "Component Types Sequence", "SQ");
		add(0x00760034, "Component Type Code Sequence", "CS");
		add(0x00760036, "Exclusive Component Type", "CS");
		add(0x00760038, "Mandatory Component Type", "CS");
		add(0x00760040, "Component Sequence", "SQ");
		add(0x00760055, "Component ID", "US");
		add(0x00760060, "Component Assembly Sequence", "SQ");
		add(0x00760070, "Component 1 Referenced ID", "US");
		add(0x00760080, "Component 1 Referenced MatingFeature Set ID", "US");
		add(0x00760090, "Component 1 Referenced MatingFeature ID", "US");
		add(0x007600A0, "Component 2 Referenced ID", "US");
		add(0x007600B0, "Component 2 Referenced MatingFeature Set ID", "US");
		add(0x007600C0, "Component 2 Referenced MatingFeature ID", "US");
	}

	/**
	 * Adds attributes of group 0x0078.
	 */
	private void addAttributeGroup0078() {
		add(0x00780001, "Implant Template Group Name", "LO");
		add(0x00780010, "Implant Template GroupDescription", "ST");
		add(0x00780020, "Implant Template Group Issuer", "LO");
		add(0x00780024, "Implant Template Group Version", "LO");
		add(0x00780026, "Replaced Implant Template GroupSequence", "SQ");
		add(0x00780028, "Implant Template Group TargetAnatomy Sequence", "SQ");
		add(0x0078002A, "Implant Template Group MembersSequence", "SQ");
		add(0x0078002E, "Implant Template Group MemberID", "US");
		add(0x00780050, "3D Implant Template GroupMember Matching Point", "FD");
		add(0x00780060, "3D Implant Template GroupMember Matching Axes", "FD");
		add(0x00780070,
			"Implant Template Group MemberMatching 2D CoordinatesSequence", "SQ");
		add(0x00780090, "2D Implant Template GroupMember Matching Point", "FD");
		add(0x007800A0, "2D Implant Template GroupMember Matching Axes", "FD");
		add(0x007800B0, "Implant Template Group VariationDimension Sequence", "SQ");
		add(0x007800B2, "Implant Template Group VariationDimension Name", "LO");
		add(0x007800B4, "Implant Template Group VariationDimension Rank Sequence",
			"SQ");
		add(0x007800B6, "Referenced Implant TemplateGroup Member ID", "US");
		add(0x007800B8, "Implant Template Group VariationDimension Rank", "US");
	}

	/**
	 * Adds attributes of group 0x0080.
	 */
	private void addAttributeGroup0080() {
		add(0x00800001, "Surface Scan Acquisition TypeCode Sequence", "SQ");
		add(0x00800002, "Surface Scan Mode CodeSequence", "SQ");
		add(0x00800003, "Registration Method CodeSequence", "SQ");
		add(0x00800004, "Shot Duration Time", "FD");
		add(0x00800005, "Shot Offset Time", "FD");
		add(0x00800006, "Surface Point Presentation ValueData", "US");
		add(0x00800007, "Surface Point Color CIELab ValueData", "US");
		add(0x00800008, "UV Mapping Sequence", "SQ");
		add(0x00800009, "Texture Label", "SH");
		add(0x00800010, "U Value Data", "OF");
		add(0x00800011, "V Value Data", "OF");
		add(0x00800012, "Referenced Texture Sequence", "SQ");
		add(0x00800013, "Referenced Surface DataSequence", "SQ");
	}

	/**
	 * Adds attributes of group 0x0088.
	 */
	private void addAttributeGroup0088() {
		add(0x00880130, "Storage Media File-set ID", "SH");
		add(0x00880140, "Storage Media File-set UID", "UI");
		add(0x00880200, "Icon Image Sequence", "SQ");
		add(0x00880904, "Topic Title", "LO"); // Retired
		add(0x00880906, "Topic Subject", "ST"); // Retired
		add(0x00880910, "Topic Author", "LO"); // Retired
		add(0x00880912, "Topic Keywords", "LO"); // Retired
	}

	/**
	 * Adds attributes of group 0x0100.
	 */
	private void addAttributeGroup0100() {
		add(0x01000410, "SOP Instance Status", "CS");
		add(0x01000420, "SOP Authorization DateTime", "DT");
		add(0x01000424, "SOP Authorization Comment", "LT");
		add(0x01000426, "Authorization EquipmentCertification Number", "LO");
	}

	/**
	 * Adds attributes of group 0x0400.
	 */
	private void addAttributeGroup0400() {
		add(0x04000005, "MAC ID Number", "US");
		add(0x04000010, "MAC Calculation Transfer SyntaxUID", "UI");
		add(0x04000015, "MAC Algorithm", "CS");
		add(0x04000020, "Data Elements Signed", "AT");
		add(0x04000100, "Digital Signature UID", "UI");
		add(0x04000105, "Digital Signature DateTime", "DT");
		add(0x04000110, "Certificate Type", "CS");
		add(0x04000115, "Certificate of Signer", "OB");
		add(0x04000120, "Signature", "OB");
		add(0x04000305, "Certified Timestamp Type", "CS");
		add(0x04000310, "Certified Timestamp", "OB");
		add(0x04000401, "Digital Signature Purpose CodeSequence", "SQ");
		add(0x04000402, "Referenced Digital SignatureSequence", "SQ");
		add(0x04000403, "Referenced SOP Instance MACSequence", "SQ");
		add(0x04000404, "MAC", "OB");
		add(0x04000500, "Encrypted Attributes Sequence", "SQ");
		add(0x04000510, "Encrypted Content Transfer SyntaxUID", "UI");
		add(0x04000520, "Encrypted Content", "OB");
		add(0x04000550, "Modified Attributes Sequence", "SQ");
		add(0x04000561, "Original Attributes Sequence", "SQ");
		add(0x04000562, "Attribute Modification DateTime", "DT");
		add(0x04000563, "Modifying System", "LO");
		add(0x04000564, "Source of Previous Values", "LO");
		add(0x04000565, "Reason for the AttributeModification", "CS");
	}

	/**
	 * Adds attributes of group 0x2000.
	 */
	private void addAttributeGroup2000() {
		add(0x20000010, "Number of Copies", "IS");
		add(0x2000001E, "Printer Configuration Sequence", "SQ");
		add(0x20000020, "Print Priority", "CS");
		add(0x20000030, "Medium Type", "CS");
		add(0x20000040, "Film Destination", "CS");
		add(0x20000050, "Film Session Label", "LO");
		add(0x20000060, "Memory Allocation", "IS");
		add(0x20000061, "Maximum Memory Allocation", "IS");
		add(0x20000062, "Color Image Printing Flag", "CS"); // Retired
		add(0x20000063, "Collation Flag", "CS"); // Retired
		add(0x20000065, "Annotation Flag", "CS"); // Retired
		add(0x20000067, "Image Overlay Flag", "CS"); // Retired
		add(0x20000069, "Presentation LUT Flag", "CS"); // Retired
		add(0x2000006A, "Image Box Presentation LUT Flag", "CS"); // Retired
		add(0x200000A0, "Memory Bit Depth", "US");
		add(0x200000A1, "Printing Bit Depth", "US");
		add(0x200000A2, "Media Installed Sequence", "SQ");
		add(0x200000A4, "Other Media Available Sequence", "SQ");
		add(0x200000A8, "Supported Image Display FormatsSequence", "SQ");
		add(0x20000500, "Referenced Film Box Sequence", "SQ");
		add(0x20000510, "Referenced Stored Print Sequence", "SQ"); // Retired
	}

	/**
	 * Adds attributes of group 0x2010.
	 */
	private void addAttributeGroup2010() {
		add(0x20100010, "Image Display Format", "ST");
		add(0x20100030, "Annotation Display Format ID", "CS");
		add(0x20100040, "Film Orientation", "CS");
		add(0x20100050, "Film Size ID", "CS");
		add(0x20100052, "Printer Resolution ID", "CS");
		add(0x20100054, "Default Printer Resolution ID", "CS");
		add(0x20100060, "Magnification Type", "CS");
		add(0x20100080, "Smoothing Type", "CS");
		add(0x201000A6, "Default Magnification Type", "CS");
		add(0x201000A7, "Other Magnification TypesAvailable", "CS");
		add(0x201000A8, "Default Smoothing Type", "CS");
		add(0x201000A9, "Other Smoothing Types Available", "CS");
		add(0x20100100, "Border Density", "CS");
		add(0x20100110, "Empty Image Density", "CS");
		add(0x20100120, "Min Density", "US");
		add(0x20100130, "Max Density", "US");
		add(0x20100140, "Trim", "CS");
		add(0x20100150, "Configuration Information", "ST");
		add(0x20100152, "Configuration InformationDescription", "LT");
		add(0x20100154, "Maximum Collated Films", "IS");
		add(0x2010015E, "Illumination", "US");
		add(0x20100160, "Reflected Ambient Light", "US");
		add(0x20100376, "Printer Pixel Spacing", "DS");
		add(0x20100500, "Referenced Film SessionSequence", "SQ");
		add(0x20100510, "Referenced Image Box Sequence", "SQ");
		add(0x20100520, "Referenced Basic Annotation BoxSequence", "SQ");
	}

	/**
	 * Adds attributes of group 0x2020.
	 */
	private void addAttributeGroup2020() {
		add(0x20200010, "Image Box Position", "US");
		add(0x20200020, "Polarity", "CS");
		add(0x20200030, "Requested Image Size", "DS");
		add(0x20200040, "Requested Decimate/CropBehavior", "CS");
		add(0x20200050, "Requested Resolution ID", "CS");
		add(0x202000A0, "Requested Image Size Flag", "CS");
		add(0x202000A2, "Decimate/Crop Result", "CS");
		add(0x20200110, "Basic Grayscale Image Sequence", "SQ");
		add(0x20200111, "Basic Color Image Sequence", "SQ");
		add(0x20200130, "Referenced Image Overlay BoxSequence", "SQ"); // Retired
		add(0x20200140, "Referenced VOI LUT BoxSequence", "SQ"); // Retired1
	}

	/**
	 * Adds attributes of group 0x2030.
	 */
	private void addAttributeGroup2030() {
		add(0x20300010, "Annotation Position", "US");
		add(0x20300020, "Text String", "LO");
	}

	/**
	 * Adds attributes of group 0x2040.
	 */
	private void addAttributeGroup2040() {
		add(0x20400010, "Referenced Overlay PlaneSequence", "SQ"); // Retired
		add(0x20400011, "Referenced Overlay Plane Groups", "US"); // Retired
		add(0x20400020, "Overlay Pixel Data Sequence", "SQ"); // Retired
		add(0x20400060, "Overlay Magnification Type", "CS"); // Retired
		add(0x20400070, "Overlay Smoothing Type", "CS"); // Retired
		// add(0x20400072, "Overlay or Image Magnification", "CS"); //Retired
		add(0x20400074, "Magnify to Number of Columns", "US"); // Retired
		add(0x20400080, "Overlay Foreground Density", "CS"); // Retired
		add(0x20400082, "Overlay Background Density", "CS"); // Retired
		add(0x20400090, "Overlay Mode", "CS"); // Retired
		add(0x20400100, "Threshold Density", "CS"); // Retired
		add(0x20400500, "Referenced Image Box Sequence(Retired)", "SQ"); // Retired
	}

	/**
	 * Adds attributes of group 0x2050.
	 */
	private void addAttributeGroup2050() {
		add(0x20500010, "Presentation LUT Sequence", "SQ");
		add(0x20500020, "Presentation LUT Shape", "CS");
		add(0x20500500, "Referenced Presentation LUTSequence", "SQ");
	}

	/**
	 * Adds attributes of group 0x2100.
	 */
	private void addAttributeGroup2100() {
		add(0x21000010, "Print Job ID", "SH"); // Retired
		add(0x21000020, "Execution Status", "CS");
		add(0x21000030, "Execution Status Info", "CS");
		add(0x21000040, "Creation Date", "DA");
		add(0x21000050, "Creation Time", "TM");
		add(0x21000070, "Originator", "AE");
		add(0x21000140, "Destination AE", "AE"); // Retired
		add(0x21000160, "Owner ID", "SH");
		add(0x21000170, "Number of Films", "IS");
		add(0x21000500, "Referenced Print Job Sequence(Pull Stored Print)", "SQ"); // Retired
	}

	/**
	 * Adds attributes of group 0x2110.
	 */
	private void addAttributeGroup2110() {
		add(0x21100010, "Printer Status", "CS");
		add(0x21100020, "Printer Status Info", "CS");
		add(0x21100030, "Printer Name", "LO");
		add(0x21100099, "Print Queue ID", "SH"); // Retired
	}

	/**
	 * Adds attributes of group 0x2120.
	 */
	private void addAttributeGroup2120() {
		add(0x21200010, "Queue Status", "CS"); // Retired
		add(0x21200050, "Print Job Description Sequence", "SQ"); // Retired
		add(0x21200070, "Referenced Print Job Sequence", "SQ"); // Retired
	}

	/**
	 * Adds attributes of group 0x2130.
	 */
	private void addAttributeGroup2130() {
		add(0x21300010, "Print Management CapabilitiesSequence", "SQ"); // Retired
		add(0x21300015, "Printer Characteristics Sequence", "SQ"); // Retired
		add(0x21300030, "Film Box Content Sequence", "SQ"); // Retired
		add(0x21300040, "Image Box Content Sequence", "SQ"); // Retired
		add(0x21300050, "Annotation Content Sequence", "SQ"); // Retired
		add(0x21300060, "Image Overlay Box ContentSequence", "SQ"); // Retired
		add(0x21300080, "Presentation LUT ContentSequence", "SQ"); // Retired
		add(0x213000A0, "Proposed Study Sequence", "SQ"); // Retired
		add(0x213000C0, "Original Image Sequence", "SQ"); // Retired
	}

	/**
	 * Adds attributes of group 0x2200.
	 */
	private void addAttributeGroup2200() {
		add(0x22000001, "Label Using Information ExtractedFrom Instances", "CS");
		add(0x22000002, "Label Text", "UT");
		add(0x22000003, "Label Style Selection", "CS");
		add(0x22000004, "Media Disposition", "LT");
		add(0x22000005, "Barcode Value", "LT");
		add(0x22000006, "Barcode Symbology", "CS");
		add(0x22000007, "Allow Media Splitting", "CS");
		add(0x22000008, "Include Non-DICOM Objects", "CS");
		add(0x22000009, "Include Display Application", "CS");
		add(0x2200000A, "Preserve Composite InstancesAfter Media Creation", "CS");
		add(0x2200000B, "Total Number of Pieces of MediaCreated", "US");
		add(0x2200000C, "Requested Media ApplicationProfile", "LO");
		add(0x2200000D, "Referenced Storage MediaSequence", "SQ");
		add(0x2200000E,
			"Failure Attributes  FailureAttributes AT  1-n(2200,000F)  Allow Lossy Compression",
			"CS");
		add(0x22000020, "Request Priority", "CS");
	}

	/**
	 * Adds attributes of group 0x3002.
	 */
	private void addAttributeGroup3002() {
		add(0x30020002, "RT Image Label", "SH");
		add(0x30020003, "RT Image Name", "LO");
		add(0x30020004, "RT Image Description", "ST");
		add(0x3002000A, "Reported Values Origin", "CS");
		add(0x3002000C, "RT Image Plane", "CS");
		add(0x3002000D, "X-Ray Image Receptor Translation", "DS");
		add(0x3002000E, "X-Ray Image Receptor Angle", "DS");
		add(0x30020010, "RT Image Orientation", "DS");
		add(0x30020011, "Image Plane Pixel Spacing", "DS");
		add(0x30020012, "RT Image Position", "DS");
		add(0x30020020, "Radiation Machine Name", "SH");
		add(0x30020022, "Radiation Machine SAD", "DS");
		add(0x30020024, "Radiation Machine SSD", "DS");
		add(0x30020026, "RT Image SID", "DS");
		add(0x30020028, "Source to Reference ObjectDistance", "DS");
		add(0x30020029, "Fraction Number", "IS");
		add(0x30020030, "Exposure Sequence", "SQ");
		add(0x30020032, "Meterset Exposure", "DS");
		add(0x30020034, "Diaphragm Position", "DS");
		add(0x30020040, "Fluence Map Sequence", "SQ");
		add(0x30020041, "Fluence Data Source", "CS");
		add(0x30020042, "Fluence Data Scale", "DS");
		add(0x30020050, "Primary Fluence Mode Sequence", "SQ");
		add(0x30020051, "Fluence Mode", "CS");
		add(0x30020052, "Fluence Mode ID", "SH");
	}

	/**
	 * Adds attributes of group 0x3004.
	 */
	private void addAttributeGroup3004() {
		add(0x30040001, "DVH Type", "CS");
		add(0x30040002, "Dose Units", "CS");
		add(0x30040004, "Dose Type", "CS");
		add(0x30040005, "Spatial Transform of Dose", "CS");
		add(0x30040006, "Dose Comment", "LO");
		add(0x30040008, "Normalization Point", "DS");
		add(0x3004000A, "Dose Summation Type", "CS");
		add(0x3004000C, "Grid Frame Offset Vector", "DS");
		add(0x3004000E, "Dose Grid Scaling", "DS");
		add(0x30040010, "RT Dose ROI Sequence", "SQ");
		add(0x30040012, "Dose Value", "DS");
		add(0x30040014, "Tissue Heterogeneity Correction", "CS");
		add(0x30040040, "DVH Normalization Point", "DS");
		add(0x30040042, "DVH Normalization Dose Value", "DS");
		add(0x30040050, "DVH Sequence", "SQ");
		add(0x30040052, "DVH Dose Scaling", "DS");
		add(0x30040054, "DVH Volume Units", "CS");
		add(0x30040056, "DVH Number of Bins", "IS");
		add(0x30040058, "DVH Data", "DS");
		add(0x30040060, "DVH Referenced ROI Sequence", "SQ");
		add(0x30040062, "DVH ROI Contribution Type", "CS");
		add(0x30040070, "DVH Minimum Dose", "DS");
		add(0x30040072, "DVH Maximum Dose", "DS");
		add(0x30040074, "DVH Mean Dose", "DS");
	}

	/**
	 * Adds attributes of group 0x3006.
	 */
	private void addAttributeGroup3006() {
		add(0x30060002, "Structure Set Label", "SH");
		add(0x30060004, "Structure Set Name", "LO");
		add(0x30060006, "Structure Set Description", "ST");
		add(0x30060008, "Structure Set Date", "DA");
		add(0x30060009, "Structure Set Time", "TM");
		add(0x30060010, "Referenced Frame of ReferenceSequence", "SQ");
		add(0x30060012, "RT Referenced Study Sequence", "SQ");
		add(0x30060014, "RT Referenced Series Sequence", "SQ");
		add(0x30060016, "Contour Image Sequence", "SQ");
		add(0x30060018, "Predecessor Structure SetSequence", "SQ");
		add(0x30060020, "Structure Set ROI Sequence", "SQ");
		add(0x30060022, "ROI Number", "IS");
		add(0x30060024, "Referenced Frame of ReferenceUID", "UI");
		add(0x30060026, "ROI Name", "LO");
		add(0x30060028, "ROI Description", "ST");
		add(0x3006002A, "ROI Display Color", "IS");
		add(0x3006002C, "ROI Volume", "DS");
		add(0x30060030, "RT Related ROI Sequence", "SQ");
		add(0x30060033, "RT ROI Relationship", "CS");
		add(0x30060036, "ROI Generation Algorithm", "CS");
		add(0x30060038, "ROI Generation Description", "LO");
		add(0x30060039, "ROI Contour Sequence", "SQ");
		add(0x30060040, "Contour Sequence", "SQ");
		add(0x30060042, "Contour Geometric Type", "CS");
		add(0x30060044, "Contour Slab Thickness", "DS");
		add(0x30060045, "Contour Offset Vector", "DS");
		add(0x30060046, "Number of Contour Points", "IS");
		add(0x30060048, "Contour Number", "IS");
		add(0x30060049, "Attached Contours", "IS");
		add(0x30060050, "Contour Data", "DS");
		add(0x30060080, "RT ROI Observations Sequence", "SQ");
		add(0x30060082, "Observation Number", "IS");
		add(0x30060084, "Referenced ROI Number", "IS");
		add(0x30060085, "ROI Observation Label", "SH");
		add(0x30060086, "RT ROI Identification CodeSequence", "SQ");
		add(0x30060088, "ROI Observation Description", "ST");
		add(0x300600A0, "Related RT ROI ObservationsSequence", "SQ");
		add(0x300600A4, "RT ROI Interpreted Type", "CS");
		add(0x300600A6, "ROI Interpreter", "PN");
		add(0x300600B0, "ROI Physical Properties Sequence", "SQ");
		add(0x300600B2, "ROI Physical Property", "CS");
		add(0x300600B4, "ROI Physical Property Value", "DS");
		add(0x300600B6, "ROI Elemental CompositionSequence", "SQ");
		add(0x300600B7, "ROI Elemental Composition AtomicNumber", "US");
		add(0x300600B8, "ROI Elemental Composition AtomicMass Fraction", "FL");
		add(0x300600B9, "Additional RT ROI IdentificationCode Sequence", "SQ");
		add(0x300600C0, "Frame of Reference RelationshipSequence", "SQ"); // Retired
		add(0x300600C2, "Related Frame of Reference UID", "UI"); // Retired
		add(0x300600C4, "Frame of ReferenceTransformation Type", "CS"); // Retired
		add(0x300600C6, "Frame of ReferenceTransformation Matrix", "DS");
		add(0x300600C8, "Frame of ReferenceTransformation Comment", "LO");
	}

	/**
	 * Adds attributes of group 0x3008.
	 */
	private void addAttributeGroup3008() {
		add(0x30080010, "Measured Dose ReferenceSequence", "SQ");
		add(0x30080012, "Measured Dose Description", "ST");
		add(0x30080014, "Measured Dose Type", "CS");
		add(0x30080016, "Measured Dose Value", "DS");
		add(0x30080020, "Treatment Session BeamSequence", "SQ");
		add(0x30080021, "Treatment Session Ion BeamSequence", "SQ");
		add(0x30080022, "Current Fraction Number", "IS");
		add(0x30080024, "Treatment Control Point Date", "DA");
		add(0x30080025, "Treatment Control Point Time", "TM");
		add(0x3008002A, "Treatment Termination Status", "CS");
		add(0x3008002B, "Treatment Termination Code", "SH");
		add(0x3008002C, "Treatment Verification Status", "CS");
		add(0x30080030, "Referenced Treatment RecordSequence", "SQ");
		add(0x30080032, "Specified Primary Meterset", "DS");
		add(0x30080033, "Specified Secondary Meterset", "DS");
		add(0x30080036, "Delivered Primary Meterset", "DS");
		add(0x30080037, "Delivered Secondary Meterset", "DS");
		add(0x3008003A, "Specified Treatment Time", "DS");
		add(0x3008003B, "Delivered Treatment Time", "DS");
		add(0x30080040, "Control Point Delivery Sequence", "SQ");
		add(0x30080041, "Ion Control Point DeliverySequence", "SQ");
		add(0x30080042, "Specified Meterset", "DS");
		add(0x30080044, "Delivered Meterset", "DS");
		add(0x30080045, "Meterset Rate Set", "FL");
		add(0x30080046, "Meterset Rate Delivered", "FL");
		add(0x30080047, "Scan Spot Metersets Delivered", "FL");
		add(0x30080048, "Dose Rate Delivered", "DS");
		add(0x30080050, "Treatment Summary CalculatedDose Reference Sequence",
			"SQ");
		add(0x30080052, "Cumulative Dose to DoseReference", "DS");
		add(0x30080054, "First Treatment Date", "DA");
		add(0x30080056, "Most Recent Treatment Date", "DA");
		add(0x3008005A, "Number of Fractions Delivered", "IS");
		add(0x30080060, "Override Sequence", "SQ");
		add(0x30080061, "Parameter Sequence Pointer", "AT");
		add(0x30080062, "Override Parameter Pointer", "AT");
		add(0x30080063, "Parameter Item Index", "IS");
		add(0x30080064, "Measured Dose Reference Number", "IS");
		add(0x30080065, "Parameter Pointer", "AT");
		add(0x30080066, "Override Reason", "ST");
		add(0x30080068, "Corrected Parameter Sequence", "SQ");
		add(0x3008006A, "Correction Value", "FL");
		add(0x30080070, "Calculated Dose ReferenceSequence", "SQ");
		add(0x30080072, "Calculated Dose ReferenceNumber", "IS");
		add(0x30080074, "Calculated Dose ReferenceDescription", "ST");
		add(0x30080076, "Calculated Dose Reference DoseValue", "DS");
		add(0x30080078, "Start Meterset", "DS");
		add(0x3008007A, "End Meterset", "DS");
		add(0x30080080, "Referenced Measured DoseReference Sequence", "SQ");
		add(0x30080082, "Referenced Measured DoseReference Number", "IS");
		add(0x30080090, "Referenced Calculated DoseReference Sequence", "SQ");
		add(0x30080092, "Referenced Calculated DoseReference Number", "IS");
		add(0x300800A0, "Beam Limiting Device Leaf PairsSequence", "SQ");
		add(0x300800B0, "Recorded Wedge Sequence", "SQ");
		add(0x300800C0, "Recorded Compensator Sequence", "SQ");
		add(0x300800D0, "Recorded Block Sequence", "SQ");
		add(0x300800E0, "Treatment Summary MeasuredDose Reference Sequence", "SQ");
		add(0x300800F0, "Recorded Snout Sequence", "SQ");
		add(0x300800F2, "Recorded Range Shifter Sequence", "SQ");
		add(0x300800F4, "Recorded Lateral SpreadingDevice Sequence", "SQ");
		add(0x300800F6, "Recorded Range ModulatorSequence", "SQ");
		add(0x30080100, "Recorded Source Sequence", "SQ");
		add(0x30080105, "Source Serial Number", "LO");
		add(0x30080110, "Treatment Session ApplicationSetup Sequence", "SQ");
		add(0x30080116, "Application Setup Check", "CS");
		add(0x30080120, "Recorded Brachy AccessoryDevice Sequence", "SQ");
		add(0x30080122, "Referenced Brachy AccessoryDevice Number", "IS");
		add(0x30080130, "Recorded Channel Sequence", "SQ");
		add(0x30080132, "Specified Channel Total Time", "DS");
		add(0x30080134, "Delivered Channel Total Time", "DS");
		add(0x30080136, "Specified Number of Pulses", "IS");
		add(0x30080138, "Delivered Number of Pulses", "IS");
		add(0x3008013A, "Specified Pulse Repetition Interval", "DS");
		add(0x3008013C, "Delivered Pulse Repetition Interval", "DS");
		add(0x30080140, "Recorded Source ApplicatorSequence", "SQ");
		add(0x30080142, "Referenced Source ApplicatorNumber", "IS");
		add(0x30080150, "Recorded Channel ShieldSequence", "SQ");
		add(0x30080152, "Referenced Channel ShieldNumber", "IS");
		add(0x30080160, "Brachy Control Point DeliveredSequence", "SQ");
		add(0x30080162, "Safe Position Exit Date", "DA");
		add(0x30080164, "Safe Position Exit Time", "TM");
		add(0x30080166, "Safe Position Return Date", "DA");
		add(0x30080168, "Safe Position Return Time", "TM");
		add(0x30080171, "Pulse Specific Brachy Control PointDelivered Sequence",
			"SQ");
		add(0x30080172, "Pulse Number", "US");
		add(0x30080173, "Brachy Pulse Control PointDelivered Sequence", "SQ");
		add(0x30080200, "Current Treatment Status", "CS");
		add(0x30080202, "Treatment Status Comment", "ST");
		add(0x30080220, "Fraction Group SummarySequence", "SQ");
		add(0x30080223, "Referenced Fraction Number", "IS");
		add(0x30080224, "Fraction Group Type", "CS");
		add(0x30080230, "Beam Stopper Position", "CS");
		add(0x30080240, "Fraction Status SummarySequence", "SQ");
		add(0x30080250, "Treatment Date", "DA");
		add(0x30080251, "Treatment Time", "TM");
	}

	/**
	 * Adds attributes of group 0x300A.
	 */
	private void addAttributeGroup300A() {
		add(0x300A0002, "RT Plan Label", "SH");
		add(0x300A0003, "RT Plan Name", "LO");
		add(0x300A0004, "RT Plan Description", "ST");
		add(0x300A0006, "RT Plan Date", "DA");
		add(0x300A0007, "RT Plan Time", "TM");
		add(0x300A0009, "Treatment Protocols", "LO");
		add(0x300A000A, "Plan Intent", "CS");
		add(0x300A000B, "Treatment Sites", "LO");
		add(0x300A000C, "RT Plan Geometry", "CS");
		add(0x300A000E, "Prescription Description", "ST");
		add(0x300A0010, "Dose Reference Sequence", "SQ");
		add(0x300A0012, "Dose Reference Number", "IS");
		add(0x300A0013, "Dose Reference UID", "UI");
		add(0x300A0014, "Dose Reference Structure Type", "CS");
		add(0x300A0015, "Nominal Beam Energy Unit", "CS");
		add(0x300A0016, "Dose Reference Description", "LO");
		add(0x300A0018, "Dose Reference Point Coordinates", "DS");
		add(0x300A001A, "Nominal Prior Dose", "DS");
		add(0x300A0020, "Dose Reference Type", "CS");
		add(0x300A0021, "Constraint Weight", "DS");
		add(0x300A0022, "Delivery Warning Dose", "DS");
		add(0x300A0023, "Delivery Maximum Dose", "DS");
		add(0x300A0025, "Target Minimum Dose", "DS");
		add(0x300A0026, "Target Prescription Dose", "DS");
		add(0x300A0027, "Target Maximum Dose", "DS");
		add(0x300A0028, "Target Underdose Volume Fraction", "DS");
		add(0x300A002A, "Organ at Risk Full-volume Dose", "DS");
		add(0x300A002B, "Organ at Risk Limit Dose", "DS");
		add(0x300A002C, "Organ at Risk Maximum Dose", "DS");
		add(0x300A002D, "Organ at Risk Overdose VolumeFraction", "DS");
		add(0x300A0040, "Tolerance Table Sequence", "SQ");
		add(0x300A0042, "Tolerance Table Number", "IS");
		add(0x300A0043, "Tolerance Table Label", "SH");
		add(0x300A0044, "Gantry Angle Tolerance", "DS");
		add(0x300A0046, "Beam Limiting Device AngleTolerance", "DS");
		add(0x300A0048, "Beam Limiting Device ToleranceSequence", "SQ");
		add(0x300A004A, "Beam Limiting Device PositionTolerance", "DS");
		add(0x300A004B, "Snout Position Tolerance", "FL");
		add(0x300A004C, "Patient Support Angle Tolerance", "DS");
		add(0x300A004E, "Table Top Eccentric AngleTolerance", "DS");
		add(0x300A004F, "Table Top Pitch Angle Tolerance", "FL");
		add(0x300A0050, "Table Top Roll Angle Tolerance", "FL");
		add(0x300A0051, "Table Top Vertical PositionTolerance", "DS");
		add(0x300A0052, "Table Top Longitudinal PositionTolerance", "DS");
		add(0x300A0053, "Table Top Lateral PositionTolerance", "DS");
		add(0x300A0055, "RT Plan Relationship", "CS");
		add(0x300A0070, "Fraction Group Sequence", "SQ");
		add(0x300A0071, "Fraction Group Number", "IS");
		add(0x300A0072, "Fraction Group Description", "LO");
		add(0x300A0078, "Number of Fractions Planned", "IS");
		add(0x300A0079, "Number of Fraction Pattern DigitsPer Day", "IS");
		add(0x300A007A, "Repeat Fraction Cycle Length", "IS");
		add(0x300A007B, "Fraction Pattern", "LT");
		add(0x300A0080, "Number of Beams", "IS");
		add(0x300A0082, "Beam Dose Specification Point", "DS");
		add(0x300A0084, "Beam Dose", "DS");
		add(0x300A0086, "Beam Meterset", "DS");
		add(0x300A0088, "Beam Dose Point Depth", "FL"); // Retired
		add(0x300A0089, "Beam Dose Point Equivalent Depth", "FL"); // Retired
		add(0x300A008A, "Beam Dose Point SSD", "FL"); // Retired
		add(0x300A008B, "Beam Dose Meaning", "CS");
		add(0x300A008C, "Beam Dose Verification ControlPoint Sequence", "SQ");
		add(0x300A008D, "Average Beam Dose Point Depth", "FL");
		add(0x300A008E, "Average Beam Dose PointEquivalent Depth", "FL");
		add(0x300A008F, "Average Beam Dose Point SSD", "FL");
		add(0x300A00A0, "Number of Brachy ApplicationSetups", "IS");
		add(0x300A00A2, "Brachy Application Setup DoseSpecification Point", "DS");
		add(0x300A00A4, "Brachy Application Setup Dose", "DS");
		add(0x300A00B0, "Beam Sequence", "SQ");
		add(0x300A00B2, "Treatment Machine Name", "SH");
		add(0x300A00B3, "Primary Dosimeter Unit", "CS");
		add(0x300A00B4, "Source-Axis Distance", "DS");
		add(0x300A00B6, "Beam Limiting Device Sequence", "SQ");
		add(0x300A00B8, "RT Beam Limiting Device Type", "CS");
		add(0x300A00BA, "Source to Beam Limiting DeviceDistance", "DS");
		add(0x300A00BB, "Isocenter to Beam Limiting DeviceDistance", "FL");
		add(0x300A00BC, "Number of Leaf/Jaw Pairs", "IS");
		add(0x300A00BE, "Leaf Position Boundaries", "DS");
		add(0x300A00C0, "Beam Number", "IS");
		add(0x300A00C2, "Beam Name", "LO");
		add(0x300A00C3, "Beam Description", "ST");
		add(0x300A00C4, "Beam Type", "CS");
		add(0x300A00C5, "Beam Delivery Duration Limit", "FD");
		add(0x300A00C6, "Radiation Type", "CS");
		add(0x300A00C7, "High-Dose Technique Type", "CS");
		add(0x300A00C8, "Reference Image Number", "IS");
		add(0x300A00CA, "Planned Verification ImageSequence", "SQ");
		add(0x300A00CC, "Imaging Device-SpecificAcquisition Parameters", "LO");
		add(0x300A00CE, "Treatment Delivery Type", "CS");
		add(0x300A00D0, "Number of Wedges", "IS");
		add(0x300A00D1, "Wedge Sequence", "SQ");
		add(0x300A00D2, "Wedge Number", "IS");
		add(0x300A00D3, "Wedge Type", "CS");
		add(0x300A00D4, "Wedge ID", "SH");
		add(0x300A00D5, "Wedge Angle", "IS");
		add(0x300A00D6, "Wedge Factor", "DS");
		add(0x300A00D7, "Total Wedge TrayWater-Equivalent Thickness", "FL");
		add(0x300A00D8, "Wedge Orientation", "DS");
		add(0x300A00D9, "Isocenter to Wedge Tray Distance", "FL");
		add(0x300A00DA, "Source to Wedge Tray Distance", "DS");
		add(0x300A00DB, "Wedge Thin Edge Position", "FL");
		add(0x300A00DC, "Bolus ID", "SH");
		add(0x300A00DD, "Bolus Description", "ST");
		add(0x300A00DE, "Effective Wedge Angle", "DS");
		add(0x300A00E0, "Number of Compensators", "IS");
		add(0x300A00E1, "Material ID", "SH");
		add(0x300A00E2, "Total Compensator Tray Factor", "DS");
		add(0x300A00E3, "Compensator Sequence", "SQ");
		add(0x300A00E4, "Compensator Number", "IS");
		add(0x300A00E5, "Compensator ID", "SH");
		add(0x300A00E6, "Source to Compensator TrayDistance", "DS");
		add(0x300A00E7, "Compensator Rows", "IS");
		add(0x300A00E8, "Compensator Columns", "IS");
		add(0x300A00E9, "Compensator Pixel Spacing", "DS");
		add(0x300A00EA, "Compensator Position", "DS");
		add(0x300A00EB, "Compensator Transmission Data", "DS");
		add(0x300A00EC, "Compensator Thickness Data", "DS");
		add(0x300A00ED, "Number of Boli", "IS");
		add(0x300A00EE, "Compensator Type", "CS");
		add(0x300A00EF, "Compensator Tray ID", "SH");
		add(0x300A00F0, "Number of Blocks", "IS");
		add(0x300A00F2, "Total Block Tray Factor", "DS");
		add(0x300A00F3, "Total Block Tray Water-EquivalentThickness", "FL");
		add(0x300A00F4, "Block Sequence", "SQ");
		add(0x300A00F5, "Block Tray ID", "SH");
		add(0x300A00F6, "Source to Block Tray Distance", "DS");
		add(0x300A00F7, "Isocenter to Block Tray Distance", "FL");
		add(0x300A00F8, "Block Type", "CS");
		add(0x300A00F9, "Accessory Code", "LO");
		add(0x300A00FA, "Block Divergence", "CS");
		add(0x300A00FB, "Block Mounting Position", "CS");
		add(0x300A00FC, "Block Number", "IS");
		add(0x300A00FE, "Block Name", "LO");
		add(0x300A0100, "Block Thickness", "DS");
		add(0x300A0102, "Block Transmission", "DS");
		add(0x300A0104, "Block Number of Points", "IS");
		add(0x300A0106, "Block Data", "DS");
		add(0x300A0107, "Applicator Sequence", "SQ");
		add(0x300A0108, "Applicator ID", "SH");
		add(0x300A0109, "Applicator Type", "CS");
		add(0x300A010A, "Applicator Description", "LO");
		add(0x300A010C, "Cumulative Dose ReferenceCoefficient", "DS");
		add(0x300A010E, "Final Cumulative Meterset Weight", "DS");
		add(0x300A0110, "Number of Control Points", "IS");
		add(0x300A0111, "Control Point Sequence", "SQ");
		add(0x300A0112, "Control Point Index", "IS");
		add(0x300A0114, "Nominal Beam Energy", "DS");
		add(0x300A0115, "Dose Rate Set", "DS");
		add(0x300A0116, "Wedge Position Sequence", "SQ");
		add(0x300A0118, "Wedge Position", "CS");
		add(0x300A011A, "Beam Limiting Device PositionSequence", "SQ");
		add(0x300A011C, "Leaf/Jaw Positions", "DS");
		add(0x300A011E, "Gantry Angle", "DS");
		add(0x300A011F, "Gantry Rotation Direction", "CS");
		add(0x300A0120, "Beam Limiting Device Angle", "DS");
		add(0x300A0121, "Beam Limiting Device RotationDirection", "CS");
		add(0x300A0122, "Patient Support Angle", "DS");
		add(0x300A0123, "Patient Support Rotation Direction", "CS");
		add(0x300A0124, "Table Top Eccentric Axis Distance", "DS");
		add(0x300A0125, "Table Top Eccentric Angle", "DS");
		add(0x300A0126, "Table Top Eccentric RotationDirection", "CS");
		add(0x300A0128, "Table Top Vertical Position", "DS");
		add(0x300A0129, "Table Top Longitudinal Position", "DS");
		add(0x300A012A, "Table Top Lateral Position", "DS");
		add(0x300A012C, "Isocenter Position", "DS");
		add(0x300A012E, "Surface Entry Point", "DS");
		add(0x300A0130, "Source to Surface Distance", "DS");
		add(0x300A0131,
			"Average Beam Dose Point Sourceto External Contour SurfaceDistance",
			"FL");
		add(0x300A0132, "Source to External ContourDistance", "FL");
		add(0x300A0133, "External Contour Entry Point", "FL");
		add(0x300A0134, "Cumulative Meterset Weight", "DS");
		add(0x300A0140, "Table Top Pitch Angle", "FL");
		add(0x300A0142, "Table Top Pitch Rotation Direction", "CS");
		add(0x300A0144, "Table Top Roll Angle", "FL");
		add(0x300A0146, "Table Top Roll Rotation Direction", "CS");
		add(0x300A0148, "Head Fixation Angle", "FL");
		add(0x300A014A, "Gantry Pitch Angle", "FL");
		add(0x300A014C, "Gantry Pitch Rotation Direction", "CS");
		add(0x300A014E, "Gantry Pitch Angle Tolerance", "FL");
		add(0x300A0180, "Patient Setup Sequence", "SQ");
		add(0x300A0182, "Patient Setup Number", "IS");
		add(0x300A0183, "Patient Setup Label", "LO");
		add(0x300A0184, "Patient Additional Position", "LO");
		add(0x300A0190, "Fixation Device Sequence", "SQ");
		add(0x300A0192, "Fixation Device Type", "CS");
		add(0x300A0194, "Fixation Device Label", "SH");
		add(0x300A0196, "Fixation Device Description", "ST");
		add(0x300A0198, "Fixation Device Position", "SH");
		add(0x300A0199, "Fixation Device Pitch Angle", "FL");
		add(0x300A019A, "Fixation Device Roll Angle", "FL");
		add(0x300A01A0, "Shielding Device Sequence", "SQ");
		add(0x300A01A2, "Shielding Device Type", "CS");
		add(0x300A01A4, "Shielding Device Label", "SH");
		add(0x300A01A6, "Shielding Device Description", "ST");
		add(0x300A01A8, "Shielding Device Position", "SH");
		add(0x300A01B0, "Setup Technique", "CS");
		add(0x300A01B2, "Setup Technique Description", "ST");
		add(0x300A01B4, "Setup Device Sequence", "SQ");
		add(0x300A01B6, "Setup Device Type", "CS");
		add(0x300A01B8, "Setup Device Label", "SH");
		add(0x300A01BA, "Setup Device Description", "ST");
		add(0x300A01BC, "Setup Device Parameter", "DS");
		add(0x300A01D0, "Setup Reference Description", "ST");
		add(0x300A01D2, "Table Top Vertical SetupDisplacement", "DS");
		add(0x300A01D4, "Table Top Longitudinal SetupDisplacement", "DS");
		add(0x300A01D6, "Table Top Lateral SetupDisplacement", "DS");
		add(0x300A0200, "Brachy Treatment Technique", "CS");
		add(0x300A0202, "Brachy Treatment Type", "CS");
		add(0x300A0206, "Treatment Machine Sequence", "SQ");
		add(0x300A0210, "Source Sequence", "SQ");
		add(0x300A0212, "Source Number", "IS");
		add(0x300A0214, "Source Type", "CS");
		add(0x300A0216, "Source Manufacturer", "LO");
		add(0x300A0218, "Active Source Diameter", "DS");
		add(0x300A021A, "Active Source Length", "DS");
		add(0x300A021B, "Source Model ID", "SH");
		add(0x300A021C, "Source Description", "LO");
		add(0x300A0222, "Source Encapsulation NominalThickness", "DS");
		add(0x300A0224, "Source Encapsulation NominalTransmission", "DS");
		add(0x300A0226, "Source Isotope Name", "LO");
		add(0x300A0228, "Source Isotope Half Life", "DS");
		add(0x300A0229, "Source Strength Units", "CS");
		add(0x300A022A, "Reference Air Kerma Rate", "DS");
		add(0x300A022B, "Source Strength", "DS");
		add(0x300A022C, "Source Strength Reference Date", "DA");
		add(0x300A022E, "Source Strength Reference Time", "TM");
		add(0x300A0230, "Application Setup Sequence", "SQ");
		add(0x300A0232, "Application Setup Type", "CS");
		add(0x300A0234, "Application Setup Number", "IS");
		add(0x300A0236, "Application Setup Name", "LO");
		add(0x300A0238, "Application Setup Manufacturer", "LO");
		add(0x300A0240, "Template Number", "IS");
		add(0x300A0242, "Template Type", "SH");
		add(0x300A0244, "Template Name", "LO");
		add(0x300A0250, "Total Reference Air Kerma", "DS");
		add(0x300A0260, "Brachy Accessory DeviceSequence", "SQ");
		add(0x300A0262, "Brachy Accessory Device Number", "IS");
		add(0x300A0263, "Brachy Accessory Device ID", "SH");
		add(0x300A0264, "Brachy Accessory Device Type", "CS");
		add(0x300A0266, "Brachy Accessory Device Name", "LO");
		add(0x300A026A, "Brachy Accessory Device NominalThickness", "DS");
		add(0x300A026C, "Brachy Accessory Device NominalTransmission", "DS");
		add(0x300A0280, "Channel Sequence", "SQ");
		add(0x300A0282, "Channel Number", "IS");
		add(0x300A0284, "Channel Length", "DS");
		add(0x300A0286, "Channel Total Time", "DS");
		add(0x300A0288, "Source Movement Type", "CS");
		add(0x300A028A, "Number of Pulses", "IS");
		add(0x300A028C, "Pulse Repetition Interval", "DS");
		add(0x300A0290, "Source Applicator Number", "IS");
		add(0x300A0291, "Source Applicator ID", "SH");
		add(0x300A0292, "Source Applicator Type", "CS");
		add(0x300A0294, "Source Applicator Name", "LO");
		add(0x300A0296, "Source Applicator Length", "DS");
		add(0x300A0298, "Source Applicator Manufacturer", "LO");
		add(0x300A029C, "Source Applicator Wall NominalThickness", "DS");
		add(0x300A029E, "Source Applicator Wall NominalTransmission", "DS");
		add(0x300A02A0, "Source Applicator Step Size", "DS");
		add(0x300A02A2, "Transfer Tube Number", "IS");
		add(0x300A02A4, "Transfer Tube Length", "DS");
		add(0x300A02B0, "Channel Shield Sequence", "SQ");
		add(0x300A02B2, "Channel Shield Number", "IS");
		add(0x300A02B3, "Channel Shield ID", "SH");
		add(0x300A02B4, "Channel Shield Name", "LO");
		add(0x300A02B8, "Channel Shield Nominal Thickness", "DS");
		add(0x300A02BA, "Channel Shield NominalTransmission", "DS");
		add(0x300A02C8, "Final Cumulative Time Weight", "DS");
		add(0x300A02D0, "Brachy Control Point Sequence", "SQ");
		add(0x300A02D2, "Control Point Relative Position", "DS");
		add(0x300A02D4, "Control Point 3D Position", "DS");
		add(0x300A02D6, "Cumulative Time Weight", "DS");
		add(0x300A02E0, "Compensator Divergence", "CS");
		add(0x300A02E1, "Compensator Mounting Position", "CS");
		add(0x300A02E2, "Source to Compensator Distance", "DS");
		add(0x300A02E3, "Total Compensator TrayWater-Equivalent Thickness", "FL");
		add(0x300A02E4, "Isocenter to Compensator TrayDistance", "FL");
		add(0x300A02E5, "Compensator Column Offset", "FL");
		add(0x300A02E6, "Isocenter to CompensatorDistances", "FL");
		add(0x300A02E7, "Compensator Relative StoppingPower Ratio", "FL");
		add(0x300A02E8, "Compensator Milling Tool Diameter", "FL");
		add(0x300A02EA, "Ion Range Compensator Sequence", "SQ");
		add(0x300A02EB, "Compensator Description", "LT");
		add(0x300A0302, "Radiation Mass Number", "IS");
		add(0x300A0304, "Radiation Atomic Number", "IS");
		add(0x300A0306, "Radiation Charge State", "SS");
		add(0x300A0308, "Scan Mode", "CS");
		add(0x300A030A, "Virtual Source-Axis Distances", "FL");
		add(0x300A030C, "Snout Sequence", "SQ");
		add(0x300A030D, "Snout Position", "FL");
		add(0x300A030F, "Snout ID", "SH");
		add(0x300A0312, "Number of Range Shifters", "IS");
		add(0x300A0314, "Range Shifter Sequence", "SQ");
		add(0x300A0316, "Range Shifter Number", "IS");
		add(0x300A0318, "Range Shifter ID", "SH");
		add(0x300A0320, "Range Shifter Type", "CS");
		add(0x300A0322, "Range Shifter Description", "LO");
		add(0x300A0330, "Number of Lateral SpreadingDevices", "IS");
		add(0x300A0332, "Lateral Spreading DeviceSequence", "SQ");
		add(0x300A0334, "Lateral Spreading Device Number", "IS");
		add(0x300A0336, "Lateral Spreading Device ID", "SH");
		add(0x300A0338, "Lateral Spreading Device Type", "CS");
		add(0x300A033A, "Lateral Spreading DeviceDescription", "LO");
		add(0x300A033C, "Lateral Spreading Device WaterEquivalent Thickness", "FL");
		add(0x300A0340, "Number of Range Modulators", "IS");
		add(0x300A0342, "Range Modulator Sequence", "SQ");
		add(0x300A0344, "Range Modulator Number", "IS");
		add(0x300A0346, "Range Modulator ID", "SH");
		add(0x300A0348, "Range Modulator Type", "CS");
		add(0x300A034A, "Range Modulator Description", "LO");
		add(0x300A034C, "Beam Current Modulation ID", "SH");
		add(0x300A0350, "Patient Support Type", "CS");
		add(0x300A0352, "Patient Support ID", "SH");
		add(0x300A0354, "Patient Support Accessory Code", "LO");
		add(0x300A0356, "Fixation Light Azimuthal Angle", "FL");
		add(0x300A0358, "Fixation Light Polar Angle", "FL");
		add(0x300A035A, "Meterset Rate", "FL");
		add(0x300A0360, "Range Shifter Settings Sequence", "SQ");
		add(0x300A0362, "Range Shifter Setting", "LO");
		add(0x300A0364, "Isocenter to Range Shifter Distance", "FL");
		add(0x300A0366, "Range Shifter Water EquivalentThickness", "FL");
		add(0x300A0370, "Lateral Spreading Device SettingsSequence", "SQ");
		add(0x300A0372, "Lateral Spreading Device Setting", "LO");
		add(0x300A0374, "Isocenter to Lateral SpreadingDevice Distance", "FL");
		add(0x300A0380, "Range Modulator SettingsSequence", "SQ");
		add(0x300A0382, "Range Modulator Gating StartValue", "FL");
		add(0x300A0384, "Range Modulator Gating StopValue", "FL");
		add(0x300A0386, "Range Modulator Gating StartWater Equivalent Thickness",
			"FL");
		add(0x300A0388, "Range Modulator Gating StopWater Equivalent Thickness",
			"FL");
		add(0x300A038A, "Isocenter to Range ModulatorDistance", "FL");
		add(0x300A0390, "Scan Spot Tune ID", "SH");
		add(0x300A0392, "Number of Scan Spot Positions", "IS");
		add(0x300A0394, "Scan Spot Position Map", "FL");
		add(0x300A0396, "Scan Spot Meterset Weights", "FL");
		add(0x300A0398, "Scanning Spot Size", "FL");
		add(0x300A039A, "Number of Paintings", "IS");
		add(0x300A03A0, "Ion Tolerance Table Sequence", "SQ");
		add(0x300A03A2, "Ion Beam Sequence", "SQ");
		add(0x300A03A4, "Ion Beam Limiting DeviceSequence", "SQ");
		add(0x300A03A6, "Ion Block Sequence", "SQ");
		add(0x300A03A8, "Ion Control Point Sequence", "SQ");
		add(0x300A03AA, "Ion Wedge Sequence", "SQ");
		add(0x300A03AC, "Ion Wedge Position Sequence", "SQ");
		add(0x300A0401, "Referenced Setup ImageSequence", "SQ");
		add(0x300A0402, "Setup Image Comment", "ST");
		add(0x300A0410, "Motion Synchronization Sequence", "SQ");
		add(0x300A0412, "Control Point Orientation", "FL");
		add(0x300A0420, "General Accessory Sequence", "SQ");
		add(0x300A0421, "General Accessory ID", "SH");
		add(0x300A0422, "General Accessory Description", "ST");
		add(0x300A0423, "General Accessory Type", "CS");
		add(0x300A0424, "General Accessory Number", "IS");
		add(0x300A0425, "Source to General AccessoryDistance", "FL");
		add(0x300A0431, "Applicator Geometry Sequence", "SQ");
		add(0x300A0432, "Applicator Aperture Shape", "CS");
		add(0x300A0433, "Applicator Opening", "FL");
		add(0x300A0434, "Applicator Opening X", "FL");
		add(0x300A0435, "Applicator Opening Y", "FL");
		add(0x300A0436, "Source to Applicator MountingPosition Distance", "FL");
		add(0x300A0440, "Number of Block Slab Items", "IS");
		add(0x300A0441, "Block Slab Sequence", "SQ");
		add(0x300A0442, "Block Slab Thickness", "DS");
		add(0x300A0443, "Block Slab Number", "US");
		add(0x300A0450, "Device Motion Control Sequence", "SQ");
		add(0x300A0451, "Device Motion Execution Mode", "CS");
		add(0x300A0452, "Device Motion Observation Mode", "CS");
		add(0x300A0453, "Device Motion Parameter CodeSequence", "SQ");
	}

	/**
	 * Adds attributes of group 0x300C.
	 */
	private void addAttributeGroup300C() {
		add(0x300C0002, "Referenced RT Plan Sequence", "SQ");
		add(0x300C0004, "Referenced Beam Sequence", "SQ");
		add(0x300C0006, "Referenced Beam Number", "IS");
		add(0x300C0007, "Referenced Reference ImageNumber", "IS");
		add(0x300C0008, "Start Cumulative Meterset Weight", "DS");
		add(0x300C0009, "End Cumulative Meterset Weight", "DS");
		add(0x300C000A, "Referenced Brachy ApplicationSetup Sequence", "SQ");
		add(0x300C000C, "Referenced Brachy ApplicationSetup Number", "IS");
		add(0x300C000E, "Referenced Source Number", "IS");
		add(0x300C0020, "Referenced Fraction GroupSequence", "SQ");
		add(0x300C0022, "Referenced Fraction GroupNumber", "IS");
		add(0x300C0040, "Referenced Verification ImageSequence", "SQ");
		add(0x300C0042, "Referenced Reference ImageSequence", "SQ");
		add(0x300C0050, "Referenced Dose ReferenceSequence", "SQ");
		add(0x300C0051, "Referenced Dose ReferenceNumber", "IS");
		add(0x300C0055, "Brachy Referenced DoseReference Sequence", "SQ");
		add(0x300C0060, "Referenced Structure SetSequence", "SQ");
		add(0x300C006A, "Referenced Patient Setup Number", "IS");
		add(0x300C0080, "Referenced Dose Sequence", "SQ");
		add(0x300C00A0, "Referenced Tolerance TableNumber", "IS");
		add(0x300C00B0, "Referenced Bolus Sequence", "SQ");
		add(0x300C00C0, "Referenced Wedge Number", "IS");
		add(0x300C00D0, "Referenced Compensator Number", "IS");
		add(0x300C00E0, "Referenced Block Number", "IS");
		add(0x300C00F0, "Referenced Control Point Index", "IS");
		add(0x300C00F2, "Referenced Control PointSequence", "SQ");
		add(0x300C00F4, "Referenced Start Control PointIndex", "IS");
		add(0x300C00F6, "Referenced Stop Control PointIndex", "IS");
		add(0x300C0100, "Referenced Range Shifter Number", "IS");
		add(0x300C0102, "Referenced Lateral SpreadingDevice Number", "IS");
		add(0x300C0104, "Referenced Range ModulatorNumber", "IS");
		add(0x300C0111, "Omitted Beam Task Sequence", "SQ");
		add(0x300C0112, "Reason for Omission", "CS");
		add(0x300C0113, "Reason for Omission Description", "LO");
	}

	/**
	 * Adds attributes of group 0x300E.
	 */
	private void addAttributeGroup300E() {
		add(0x300E0002, "Approval Status", "CS");
		add(0x300E0004, "Review Date", "DA");
		add(0x300E0005, "Review Time", "TM");
		add(0x300E0008, "Reviewer Name", "PN");
	}

	/**
	 * Adds attributes of group 0x4000.
	 */
	private void addAttributeGroup4000() {
		add(0x40000010, "Arbitrary", "LT"); // Retired
		add(0x40004000, "Text Comments", "LT"); // Retired
	}

	/**
	 * Adds attributes of group 0x4008.
	 */
	private void addAttributeGroup4008() {
		add(0x40080040, "Results ID", "SH"); // Retired
		add(0x40080042, "Results ID Issuer", "LO"); // Retired
		add(0x40080050, "Referenced InterpretationSequence", "SQ"); // Retired
		add(0x400800FF, "Report Production Status (Trial)", "CS"); // Retired
		add(0x40080100, "Interpretation Recorded Date", "DA"); // Retired
		add(0x40080101, "Interpretation Recorded Time", "TM"); // Retired
		add(0x40080102, "Interpretation Recorder", "PN"); // Retired
		add(0x40080103, "Reference to Recorded Sound", "LO"); // Retired
		add(0x40080108, "Interpretation Transcription Date", "DA"); // Retired
		add(0x40080109, "Interpretation Transcription Time", "TM"); // Retired
		add(0x4008010A, "Interpretation Transcriber", "PN"); // Retired
		add(0x4008010B, "Interpretation Text", "ST"); // Retired
		add(0x4008010C, "Interpretation Author", "PN"); // Retired
		add(0x40080111, "Interpretation Approver Sequence", "SQ"); // Retired
		add(0x40080112, "Interpretation Approval Date", "DA"); // Retired
		add(0x40080113, "Interpretation Approval Time", "TM"); // Retired
		add(0x40080114, "Physician Approving Interpretation", "PN"); // Retired
		add(0x40080115, "Interpretation DiagnosisDescription", "LT"); // Retired
		add(0x40080117, "Interpretation Diagnosis CodeSequence", "SQ"); // Retired
		add(0x40080118, "Results Distribution List Sequence", "SQ"); // Retired
		add(0x40080119, "Distribution Name", "PN"); // Retired
		add(0x4008011A, "Distribution Address", "LO"); // Retired
		add(0x40080200, "Interpretation ID", "SH"); // Retired
		add(0x40080202, "Interpretation ID Issuer", "LO"); // Retired
		add(0x40080210, "Interpretation Type ID", "CS"); // Retired
		add(0x40080212, "Interpretation Status ID", "CS"); // Retired
		add(0x40080300, "Impressions", "ST"); // Retired
		add(0x40084000, "Results Comments", "ST"); // Retired
	}

	/**
	 * Adds attributes of group 0x4010.
	 */
	private void addAttributeGroup4010() {
		add(0x40100001, "Low Energy Detectors", "CS"); // DICOS
		add(0x40100002, "High Energy Detectors", "CS"); // DICOS
		add(0x40100004, "Detector Geometry Sequence", "SQ"); // DICOS
		add(0x40101001, "Threat ROI Voxel Sequence", "SQ"); // DICOS
		add(0x40101004, "Threat ROI Base", "FL"); // DICOS
		add(0x40101005, "Threat ROI Extents", "FL"); // DICOS
		add(0x40101006, "Threat ROI Bitmap", "OB"); // DICOS
		add(0x40101007, "Route Segment ID", "SH"); // DICOS
		add(0x40101008, "Gantry Type", "CS"); // DICOS
		add(0x40101009, "OOI Owner Type", "CS"); // DICOS
		add(0x4010100A, "Route Segment Sequence", "SQ"); // DICOS
		add(0x40101010, "Potential Threat Object ID", "US"); // DICOS
		add(0x40101011, "Threat Sequence", "SQ"); // DICOS
		add(0x40101012, "Threat Category", "CS"); // DICOS
		add(0x40101013, "Threat Category Description", "LT"); // DICOS
		add(0x40101014, "ATD Ability Assessment", "CS"); // DICOS
		add(0x40101015, "ATD Assessment Flag", "CS"); // DICOS
		add(0x40101016, "ATD Assessment Probability", "FL"); // DICOS
		add(0x40101017, "Mass", "FL"); // DICOS
		add(0x40101018, "Density", "FL"); // DICOS
		add(0x40101019, "Z Effective", "FL"); // DICOS
		add(0x4010101A, "Boarding Pass ID", "SH"); // DICOS
		add(0x4010101B, "Center of Mass", "FL"); // DICOS
		add(0x4010101C, "Center of PTO", "FL"); // DICOS
		add(0x4010101D, "Bounding Polygon", "FL"); // DICOS
		add(0x4010101E, "Route Segment Start Location ID", "SH"); // DICOS
		add(0x4010101F, "Route Segment End Location ID", "SH"); // DICOS
		add(0x40101020, "Route Segment Location ID Type", "CS"); // DICOS
		add(0x40101021, "Abort Reason", "CS"); // DICOS
		add(0x40101023, "Volume of PTO", "FL"); // DICOS
		add(0x40101024, "Abort Flag", "CS"); // DICOS
		add(0x40101025, "Route Segment Start Time", "DT"); // DICOS
		add(0x40101026, "Route Segment End Time", "DT"); // DICOS
		add(0x40101027, "TDR Type", "CS"); // DICOS
		add(0x40101028, "International Route Segment", "CS"); // DICOS
		add(0x40101029, "Threat Detection Algorithm andVersion", "LO"); // DICOS
		add(0x4010102A, "Assigned Location", "SH"); // DICOS
		add(0x4010102B, "Alarm Decision Time", "DT"); // DICOS
		add(0x40101031, "Alarm Decision", "CS"); // DICOS
		add(0x40101033, "Number of Total Objects", "US"); // DICOS
		add(0x40101034, "Number of Alarm Objects", "US"); // DICOS
		add(0x40101037, "PTO Representation Sequence", "SQ"); // DICOS
		add(0x40101038, "ATD Assessment Sequence", "SQ"); // DICOS1
		add(0x40101039, "TIP Type", "CS"); // DICOS
		add(0x4010103A, "DICOS Version", "CS"); // DICOS
		add(0x40101041, "OOI Owner Creation Time", "DT"); // DICOS
		add(0x40101042, "OOI Type", "CS"); // DICOS
		add(0x40101043, "OOI Size", "FL"); // DICOS
		add(0x40101044, "Acquisition Status", "CS"); // DICOS
		add(0x40101045, "Basis Materials Code Sequence", "SQ"); // DICOS
		add(0x40101046, "Phantom Type", "CS"); // DICOS
		add(0x40101047, "OOI Owner Sequence", "SQ"); // DICOS
		add(0x40101048, "Scan Type", "CS"); // DICOS
		add(0x40101051, "Itinerary ID", "LO"); // DICOS
		add(0x40101052, "Itinerary ID Type", "SH"); // DICOS
		add(0x40101053, "Itinerary ID Assigning Authority", "LO"); // DICOS
		add(0x40101054, "Route ID", "SH"); // DICOS
		add(0x40101055, "Route ID Assigning Authority", "SH"); // DICOS
		add(0x40101056, "Inbound Arrival Type", "CS"); // DICOS
		add(0x40101058, "Carrier ID", "SH"); // DICOS
		add(0x40101059, "Carrier ID Assigning Authority", "CS"); // DICOS
		add(0x40101060, "Source Orientation", "FL"); // DICOS
		add(0x40101061, "Source Position", "FL"); // DICOS
		add(0x40101062, "Belt Height", "FL"); // DICOS
		add(0x40101064, "Algorithm Routing Code Sequence", "SQ"); // DICOS
		add(0x40101067, "Transport Classification", "CS"); // DICOS
		add(0x40101068, "OOI Type Descriptor", "LT"); // DICOS
		add(0x40101069, "Total Processing Time", "FL"); // DICOS
		add(0x4010106C, "Detector Calibration Data", "OB"); // DICOS
		add(0x4010106D, "Additional Screening Performed", "CS"); // DICOS
		add(0x4010106E, "Additional Inspection SelectionCriteria", "CS"); // DICOS
		add(0x4010106F, "Additional Inspection MethodSequence", "SQ"); // DICOS
		add(0x40101070, "AIT Device Type", "CS"); // DICOS
		add(0x40101071, "QR Measurements Sequence", "SQ"); // DICOS
		add(0x40101072, "Target Material Sequence", "SQ"); // DICOS
		add(0x40101073, "SNR Threshold", "FD"); // DICOS
		add(0x40101075, "Image Scale Representation", "DS"); // DICOS
		add(0x40101076, "Referenced PTO Sequence", "SQ"); // DICOS
		add(0x40101077, "Referenced TDR InstanceSequence", "SQ"); // DICOS
		add(0x40101078, "PTO Location Description", "ST"); // DICOS
		add(0x40101079, "Anomaly Locator IndicatorSequence", "SQ"); // DICOS
		add(0x4010107A, "Anomaly Locator Indicator", "FL"); // DICOS
		add(0x4010107B, "PTO Region Sequence", "SQ"); // DICOS
		add(0x4010107C, "Inspection Selection Criteria", "CS"); // DICOS
		add(0x4010107D, "Secondary Inspection MethodSequence", "SQ"); // DICOS
		add(0x4010107E, "PRCS to RCS Orientation", "DS"); // DICOS
	}

	/**
	 * Adds attributes of group 0x4FFE.
	 */
	private void addAttributeGroup4FFE() {
		add(0x4FFE0001, "MAC Parameters Sequence", "SQ");
	}

	/**
	 * Adds attributes of group 0x5200.
	 */
	private void addAttributeGroup5200() {
		add(0x52009229, "Shared Functional GroupsSequence", "SQ");
		add(0x52009230, "Per-frame Functional GroupsSequence", "SQ");
	}

	/**
	 * Adds attributes of group 0x5400.
	 */
	private void addAttributeGroup5400() {
		add(0x54000100, "Waveform Sequence", "SQ");
		// add(0x54000110, "Channel Minimum Value", "OB or OW");
		// add(0x54000112, "Channel Maximum Value", "OB or OW");
		add(0x54001004, "Waveform Bits Allocated", "US");
		add(0x54001006, "Waveform Sample Interpretation", "CS");
		// add(0x5400100A, "Waveform Padding Value", "OB or OW");
		// add(0x54001010, "Waveform Data", "OB or OW");
	}

	/**
	 * Adds attributes of group 0x5600.
	 */
	private void addAttributeGroup5600() {
		add(0x56000010, "First Order Phase Correction Angle", "OF");
		add(0x56000020, "Spectroscopy Data", "OF");
	}

	/**
	 * Adds attributes of group 0x7FE0.
	 */
	private void addAttributeGroup7FE0() {
		add(0x7FE00008, "Float Pixel Data", "OF");
		add(0x7FE00009, "Double Float Pixel Data", "OD");
		// add(0x7FE00010, "Pixel Data", "OB or OW");
		add(0x7FE00020, "Coefficients SDVN", "OW"); // Retired
		add(0x7FE00030, "Coefficients SDHN", "OW"); // Retired
		add(0x7FE00040, "Coefficients SDDN", "OW"); // Retired
	}

	/**
	 * Adds attributes of group 0xFFFA.
	 */
	private void addAttributeGroupFFFA() {
		add(0xFFFAFFFA, "Digital Signatures Sequence", "SQ");
	}

	private void add(final int code, final String name, final String vr) {
		table.put(code, new String[] { name, vr });
	}
}
