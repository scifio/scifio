/*
 * #%L
 * OME-XML Java library for working with OME-XML metadata structures.
 * %%
 * Copyright (C) 2006 - 2013 Open Microscopy Environment:
 *   - Massachusetts Institute of Technology
 *   - National Institutes of Health
 *   - University of Dundee
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
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

/*-----------------------------------------------------------------------------
 *
 * THIS IS AUTOMATICALLY GENERATED CODE.  DO NOT MODIFY.
 * Created by temp via xsd-fu on 2012-09-20 09:42:39.286138
 *
 *-----------------------------------------------------------------------------
 */

package loci.formats.meta;

import ome.xml.model.*;
import ome.xml.model.enums.*;
import ome.xml.model.primitives.*;

/**
 * A legacy delegator class for ome.xml.meta.DummyMetadata.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/meta/DummyMetadata.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/meta/DummyMetadata.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Chris Allan callan at blackcat.ca
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Mark hiner hiner at wisc.edu
 */
public class DummyMetadata implements IMetadata
{

  // -- Fields --

  private ome.xml.meta.DummyMetadata meta;

  // -- Constructor --

  public DummyMetadata() {
    meta = new ome.xml.meta.DummyMetadata();
  }

	// -- MetadataStore API methods --

	public void createRoot()
	{
    meta.createRoot();
	}

	public Object getRoot()
	{
		return meta.getRoot();
	}

	public void setRoot(Object root)
	{
    meta.setRoot(root);
	}

	// -- Entity counting (manual definitions) --

  public int getBooleanAnnotationAnnotationCount(int booleanAnnotationIndex) {
    return meta.getBooleanAnnotationAnnotationCount(booleanAnnotationIndex);
  }

  public int getCommentAnnotationAnnotationCount(int commentAnnotationIndex) {
    return meta.getCommentAnnotationAnnotationCount(commentAnnotationIndex);
  }

  public int getDoubleAnnotationAnnotationCount(int doubleAnnotationIndex) {
    return meta.getDoubleAnnotationAnnotationCount(doubleAnnotationIndex);
  }

  public int getFileAnnotationAnnotationCount(int fileAnnotationIndex) {
    return meta.getFileAnnotationAnnotationCount(fileAnnotationIndex);
  }

  public int getListAnnotationAnnotationCount(int listAnnotationIndex) {
    return meta.getListAnnotationAnnotationCount(listAnnotationIndex);
  }

  public int getLongAnnotationAnnotationCount(int longAnnotationIndex) {
    return meta.getLongAnnotationAnnotationCount(longAnnotationIndex);
  }

  public int getTagAnnotationAnnotationCount(int tagAnnotationIndex) {
    return meta.getTagAnnotationAnnotationCount(tagAnnotationIndex);
  }

  public int getTermAnnotationAnnotationCount(int termAnnotationIndex) {
    return meta.getTermAnnotationAnnotationCount(termAnnotationIndex);
  }

  public int getTimestampAnnotationAnnotationCount(int timestampAnnotationIndex)
  {
    return meta.getTimestampAnnotationAnnotationCount(timestampAnnotationIndex);
  }

  public int getXMLAnnotationAnnotationCount(int xmlAnnotationIndex) {
    return meta.getXMLAnnotationAnnotationCount(xmlAnnotationIndex);
  }

	public int getPixelsBinDataCount(int imageIndex)
	{
		return meta.getPixelsBinDataCount(imageIndex);
	}

	public String getLightSourceType(int instrumentIndex, int lightSourceIndex)
	{
		return meta.getLightSourceType(instrumentIndex, lightSourceIndex);
	}

  public String getShapeType(int roiIndex, int shapeIndex)
  {
    return meta.getShapeType(roiIndex, shapeIndex);
  }

	// -- Entity counting (code generated definitions) --

	// AnnotationRef entity counting
	public int getROIAnnotationRefCount(int ROIIndex)
	{
    return meta.getROIAnnotationRefCount(ROIIndex);
	}

	public int getPlateAcquisitionAnnotationRefCount(int plateIndex, int plateAcquisitionIndex)
	{
    return meta.getPlateAcquisitionAnnotationRefCount(plateIndex, plateAcquisitionIndex);
	}

	public int getPlateAnnotationRefCount(int plateIndex)
	{
    return meta.getPlateAnnotationRefCount(plateIndex);
	}

	public int getExperimenterGroupAnnotationRefCount(int experimenterGroupIndex)
	{
    return meta.getExperimenterGroupAnnotationRefCount(experimenterGroupIndex);
	}

	public int getImageAnnotationRefCount(int imageIndex)
	{
    return meta.getImageAnnotationRefCount(imageIndex);
	}

	public int getScreenAnnotationRefCount(int screenIndex)
	{
    return meta.getScreenAnnotationRefCount(screenIndex);
	}

	public int getWellAnnotationRefCount(int plateIndex, int wellIndex)
	{
    return meta.getWellAnnotationRefCount(plateIndex, wellIndex);
	}

	public int getDatasetAnnotationRefCount(int datasetIndex)
	{
    return meta.getDatasetAnnotationRefCount(datasetIndex);
	}

	public int getProjectAnnotationRefCount(int projectIndex)
	{
    return meta.getProjectAnnotationRefCount(projectIndex);
	}

	public int getReagentAnnotationRefCount(int screenIndex, int reagentIndex)
	{
    return meta.getReagentAnnotationRefCount(screenIndex, reagentIndex);
	}

	public int getPlaneAnnotationRefCount(int imageIndex, int planeIndex)
	{
    return meta.getPlaneAnnotationRefCount(imageIndex, planeIndex);
	}

	public int getExperimenterAnnotationRefCount(int experimenterIndex)
	{
    return meta.getExperimenterAnnotationRefCount(experimenterIndex);
	}

	public int getWellSampleAnnotationRefCount(int plateIndex, int wellIndex, int wellSampleIndex)
	{
    return meta.getWellSampleAnnotationRefCount(plateIndex, wellIndex, wellSampleIndex);
	}

	public int getPixelsAnnotationRefCount(int imageIndex)
	{
    return meta.getPixelsAnnotationRefCount(imageIndex);
	}

	public int getChannelAnnotationRefCount(int imageIndex, int channelIndex)
	{
    return meta.getChannelAnnotationRefCount(imageIndex, channelIndex);
	}

	// Arc entity counting
	// BinaryFile entity counting
	// BinaryOnly entity counting
	// BooleanAnnotation entity counting
	public int getBooleanAnnotationCount()
	{
    return meta.getBooleanAnnotationCount();
	}

	// Channel entity counting
	public int getChannelCount(int imageIndex)
	{
    return meta.getChannelCount(imageIndex);
	}

	// CommentAnnotation entity counting
	public int getCommentAnnotationCount()
	{
    return meta.getCommentAnnotationCount();
	}

	// Dataset entity counting
	public int getDatasetCount()
	{
    return meta.getDatasetCount();
	}

	// DatasetRef entity counting
	public int getDatasetRefCount(int projectIndex)
	{
    return meta.getDatasetRefCount(projectIndex);
	}

	// Detector entity counting
	public int getDetectorCount(int instrumentIndex)
	{
    return meta.getDetectorCount(instrumentIndex);
	}

	// DetectorSettings entity counting
	// Dichroic entity counting
	public int getDichroicCount(int instrumentIndex)
	{
    return meta.getDichroicCount(instrumentIndex);
	}

	// DichroicRef entity counting
	// DoubleAnnotation entity counting
	public int getDoubleAnnotationCount()
	{
    return meta.getDoubleAnnotationCount();
	}

	// Ellipse entity counting
	// EmissionFilterRef entity counting
	public int getLightPathEmissionFilterRefCount(int imageIndex, int channelIndex)
	{
    return meta.getLightPathEmissionFilterRefCount(imageIndex, channelIndex);
	}

	public int getFilterSetEmissionFilterRefCount(int instrumentIndex, int filterSetIndex)
	{
    return meta.getFilterSetEmissionFilterRefCount(instrumentIndex, filterSetIndex);
	}

	// ExcitationFilterRef entity counting
	public int getLightPathExcitationFilterRefCount(int imageIndex, int channelIndex)
	{
    return meta.getLightPathExcitationFilterRefCount(imageIndex, channelIndex);
	}

	public int getFilterSetExcitationFilterRefCount(int instrumentIndex, int filterSetIndex)
	{
    return meta.getFilterSetExcitationFilterRefCount(instrumentIndex, filterSetIndex);
	}

	// Experiment entity counting
	public int getExperimentCount()
	{
    return meta.getExperimentCount();
	}

	// ExperimentRef entity counting
	// Experimenter entity counting
	public int getExperimenterCount()
	{
    return meta.getExperimenterCount();
	}

	// ExperimenterGroup entity counting
	public int getExperimenterGroupCount()
	{
    return meta.getExperimenterGroupCount();
	}

	// ExperimenterGroupRef entity counting
	// ExperimenterRef entity counting
	public int getExperimenterGroupExperimenterRefCount(int experimenterGroupIndex)
	{
    return meta.getExperimenterGroupExperimenterRefCount(experimenterGroupIndex);
	}

	// Filament entity counting
	// FileAnnotation entity counting
	public int getFileAnnotationCount()
	{
    return meta.getFileAnnotationCount();
	}

	// Filter entity counting
	public int getFilterCount(int instrumentIndex)
	{
    return meta.getFilterCount(instrumentIndex);
	}

	// FilterSet entity counting
	public int getFilterSetCount(int instrumentIndex)
	{
    return meta.getFilterSetCount(instrumentIndex);
	}

	// FilterSetRef entity counting
	// Image entity counting
	public int getImageCount()
	{
    return meta.getImageCount();
	}

	// ImageRef entity counting
	public int getDatasetImageRefCount(int datasetIndex)
	{
    return meta.getDatasetImageRefCount(datasetIndex);
	}

	// ImagingEnvironment entity counting
	// Instrument entity counting
	public int getInstrumentCount()
	{
    return meta.getInstrumentCount();
	}

	// InstrumentRef entity counting
	// Label entity counting
	// Laser entity counting
	// Leader entity counting
	public int getLeaderCount(int experimenterGroupIndex)
	{
    return meta.getLeaderCount(experimenterGroupIndex);
	}

	// LightEmittingDiode entity counting
	// LightPath entity counting
	// LightSource entity counting
	public int getLightSourceCount(int instrumentIndex)
	{
    return meta.getLightSourceCount(instrumentIndex);
	}

	// LightSourceSettings entity counting
	public int getMicrobeamManipulationLightSourceSettingsCount(int experimentIndex, int microbeamManipulationIndex)
	{
    return meta.getMicrobeamManipulationLightSourceSettingsCount(experimentIndex, microbeamManipulationIndex);
	}

	// Line entity counting
	// ListAnnotation entity counting
	public int getListAnnotationCount()
	{
    return meta.getListAnnotationCount();
	}

	// LongAnnotation entity counting
	public int getLongAnnotationCount()
	{
    return meta.getLongAnnotationCount();
	}

	// Mask entity counting
	// MetadataOnly entity counting
	// MicrobeamManipulation entity counting
	public int getMicrobeamManipulationCount(int experimentIndex)
	{
    return meta.getMicrobeamManipulationCount(experimentIndex);
	}

	// MicrobeamManipulationRef entity counting
	public int getMicrobeamManipulationRefCount(int imageIndex)
	{
    return meta.getMicrobeamManipulationRefCount(imageIndex);
	}

	// Microscope entity counting
	// Objective entity counting
	public int getObjectiveCount(int instrumentIndex)
	{
    return meta.getObjectiveCount(instrumentIndex);
	}

	// ObjectiveSettings entity counting
	// Pixels entity counting
	// Plane entity counting
	public int getPlaneCount(int imageIndex)
	{
    return meta.getPlaneCount(imageIndex);
	}

	// Plate entity counting
	public int getPlateCount()
	{
    return meta.getPlateCount();
	}

	// PlateAcquisition entity counting
	public int getPlateAcquisitionCount(int plateIndex)
	{
    return meta.getPlateAcquisitionCount(plateIndex);
	}

	// PlateRef entity counting
	public int getPlateRefCount(int screenIndex)
	{
    return meta.getPlateRefCount(screenIndex);
	}

	// Point entity counting
	// Polygon entity counting
	// Polyline entity counting
	// Project entity counting
	public int getProjectCount()
	{
    return meta.getProjectCount();
	}

	// Pump entity counting
	// ROI entity counting
	public int getROICount()
	{
    return meta.getROICount();
	}

	// ROIRef entity counting
	public int getImageROIRefCount(int imageIndex)
	{
    return meta.getImageROIRefCount(imageIndex);
	}

	public int getMicrobeamManipulationROIRefCount(int experimentIndex, int microbeamManipulationIndex)
	{
    return meta.getMicrobeamManipulationROIRefCount(experimentIndex, microbeamManipulationIndex);
	}

	// Reagent entity counting
	public int getReagentCount(int screenIndex)
	{
    return meta.getReagentCount(screenIndex);
	}

	// ReagentRef entity counting
	// Rectangle entity counting
	// Screen entity counting
	public int getScreenCount()
	{
    return meta.getScreenCount();
	}

	// Shape entity counting
	public int getShapeCount(int ROIIndex)
	{
    return meta.getShapeCount(ROIIndex);
	}

	// StageLabel entity counting
	// StructuredAnnotations entity counting
	// TagAnnotation entity counting
	public int getTagAnnotationCount()
	{
    return meta.getTagAnnotationCount();
	}

	// TermAnnotation entity counting
	public int getTermAnnotationCount()
	{
    return meta.getTermAnnotationCount();
	}

	// TiffData entity counting
	public int getTiffDataCount(int imageIndex)
	{
    return meta.getTiffDataCount(imageIndex);
	}

	// TimestampAnnotation entity counting
	public int getTimestampAnnotationCount()
	{
    return meta.getTimestampAnnotationCount();
	}

	// TransmittanceRange entity counting
	// Element's text data
	// {u'TiffData': [u'int imageIndex', u'int tiffDataIndex']}
	public void setUUIDValue(String value, int imageIndex, int tiffDataIndex)
	{
    meta.setUUIDValue(value, imageIndex, tiffDataIndex);
	}

	public String getUUIDValue(int imageIndex, int tiffDataIndex)
	{
    return meta.getUUIDValue(imageIndex, tiffDataIndex);
	}

	// UUID entity counting
	// Union entity counting
	// Well entity counting
	public int getWellCount(int plateIndex)
	{
    return meta.getWellCount(plateIndex);
	}

	// WellSample entity counting
	public int getWellSampleCount(int plateIndex, int wellIndex)
	{
    return meta.getWellSampleCount(plateIndex, wellIndex);
	}

	// WellSampleRef entity counting
	public int getWellSampleRefCount(int plateIndex, int plateAcquisitionIndex)
	{
    return meta.getWellSampleRefCount(plateIndex, plateAcquisitionIndex);
	}

	// XMLAnnotation entity counting
	public int getXMLAnnotationCount()
	{
    return meta.getXMLAnnotationCount();
	}


	// -- Entity retrieval (manual definitions) --

	public Boolean getPixelsBinDataBigEndian(int imageIndex, int binDataIndex)
	{
		return meta.getPixelsBinDataBigEndian(imageIndex, binDataIndex);
	}

	/** Gets the UUID associated with this collection of metadata. */
	public String getUUID()
	{
		return meta.getUUID();
	}

	// -- Entity retrieval (code generated definitions) --

	//
	// AnnotationRef property storage
	//
	// Indexes: {u'ROI': [u'int ROIIndex', u'int annotationRefIndex'], u'Reagent': [u'int screenIndex', u'int reagentIndex', u'int annotationRefIndex'], u'Plate': [u'int plateIndex', u'int annotationRefIndex'], u'ExperimenterGroup': [u'int experimenterGroupIndex', u'int annotationRefIndex'], u'Image': [u'int imageIndex', u'int annotationRefIndex'], u'Well': [u'int plateIndex', u'int wellIndex', u'int annotationRefIndex'], u'Pixels': [u'int imageIndex', u'int annotationRefIndex'], u'Dataset': [u'int datasetIndex', u'int annotationRefIndex'], u'Project': [u'int projectIndex', u'int annotationRefIndex'], u'PlateAcquisition': [u'int plateIndex', u'int plateAcquisitionIndex', u'int annotationRefIndex'], u'Plane': [u'int imageIndex', u'int planeIndex', u'int annotationRefIndex'], u'Experimenter': [u'int experimenterIndex', u'int annotationRefIndex'], u'Annotation': [u'int annotationRefIndex'], u'WellSample': [u'int plateIndex', u'int wellIndex', u'int wellSampleIndex', u'int annotationRefIndex'], u'Screen': [u'int screenIndex', u'int annotationRefIndex'], u'Channel': [u'int imageIndex', u'int channelIndex', u'int annotationRefIndex']}
	// {u'ROI': {u'OME': None}, u'PlateAcquisition': {u'Plate': {u'OME': None}}, u'Plate': {u'OME': None}, u'ExperimenterGroup': {u'OME': None}, u'Image': {u'OME': None}, u'Screen': {u'OME': None}, u'Well': {u'Plate': {u'OME': None}}, u'Dataset': {u'OME': None}, u'Project': {u'OME': None}, u'Reagent': {u'Screen': {u'OME': None}}, u'Plane': {u'Pixels': {u'Image': {u'OME': None}}}, u'Experimenter': {u'OME': None}, u'Annotation': None, u'WellSample': {u'Well': {u'Plate': {u'OME': None}}}, u'Pixels': {u'Image': {u'OME': None}}, u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference AnnotationRef

	//
	// Arc property storage
	//
	// Indexes: {u'LightSource': [u'int instrumentIndex', u'int lightSourceIndex']}
	// {u'LightSource': {u'Instrument': {u'OME': None}}}
	// Is multi path? False

	// Ignoring Arc of parent abstract type
	// Ignoring Filament of parent abstract type
	// ID accessor from parent LightSource
	public String getArcID(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getArcID(instrumentIndex, lightSourceIndex);
	}

	// Ignoring Laser of parent abstract type
	// Ignoring LightEmittingDiode of parent abstract type
	// LotNumber accessor from parent LightSource
	public String getArcLotNumber(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getArcLotNumber(instrumentIndex, lightSourceIndex);
	}

	// Manufacturer accessor from parent LightSource
	public String getArcManufacturer(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getArcManufacturer(instrumentIndex, lightSourceIndex);
	}

	// Model accessor from parent LightSource
	public String getArcModel(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getArcModel(instrumentIndex, lightSourceIndex);
	}

	// Power accessor from parent LightSource
	public Double getArcPower(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getArcPower(instrumentIndex, lightSourceIndex);
	}

	// SerialNumber accessor from parent LightSource
	public String getArcSerialNumber(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getArcSerialNumber(instrumentIndex, lightSourceIndex);
	}

	public ArcType getArcType(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getArcType(instrumentIndex, lightSourceIndex);
	}

	//
	// BinaryFile property storage
	//
	// Indexes: {u'FileAnnotation': [u'int fileAnnotationIndex']}
	// {u'FileAnnotation': {u'StructuredAnnotations': {u'OME': None}}}
	// Is multi path? False

	// Ignoring BinData element, complex property
	// Ignoring External element, complex property
	public String getBinaryFileFileName(int fileAnnotationIndex)
	{
    return meta.getBinaryFileFileName(fileAnnotationIndex);
	}

	public String getBinaryFileMIMEType(int fileAnnotationIndex)
	{
    return meta.getBinaryFileMIMEType(fileAnnotationIndex);
	}

	public NonNegativeLong getBinaryFileSize(int fileAnnotationIndex)
	{
    return meta.getBinaryFileSize(fileAnnotationIndex);
	}

	//
	// BinaryOnly property storage
	//
	// Indexes: {u'OME': []}
	// {u'OME': None}
	// Is multi path? False

	public String getBinaryOnlyMetadataFile(int metadataFileIndex)
	{
    return meta.getBinaryOnlyMetadataFile(metadataFileIndex);
	}

	public String getBinaryOnlyUUID(int UUIDIndex)
	{
    return meta.getBinaryOnlyUUID(UUIDIndex);
	}

	//
	// BooleanAnnotation property storage
	//
	// Indexes: {u'StructuredAnnotations': [u'int booleanAnnotationIndex']}
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public String getBooleanAnnotationAnnotationRef(int booleanAnnotationIndex, int annotationRefIndex)
	{
    return meta.getBooleanAnnotationAnnotationRef(booleanAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public String getBooleanAnnotationDescription(int booleanAnnotationIndex)
	{
    return meta.getBooleanAnnotationDescription(booleanAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public String getBooleanAnnotationID(int booleanAnnotationIndex)
	{
    return meta.getBooleanAnnotationID(booleanAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getBooleanAnnotationNamespace(int booleanAnnotationIndex)
	{
    return meta.getBooleanAnnotationNamespace(booleanAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public Boolean getBooleanAnnotationValue(int booleanAnnotationIndex)
	{
    return meta.getBooleanAnnotationValue(booleanAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// Channel property storage
	//
	// Indexes: {u'Pixels': [u'int imageIndex', u'int channelIndex']}
	// {u'Pixels': {u'Image': {u'OME': None}}}
	// Is multi path? False

	public AcquisitionMode getChannelAcquisitionMode(int imageIndex, int channelIndex)
	{
    return meta.getChannelAcquisitionMode(imageIndex, channelIndex);
	}

	public String getChannelAnnotationRef(int imageIndex, int channelIndex, int annotationRefIndex)
	{
    return meta.getChannelAnnotationRef(imageIndex, channelIndex, annotationRefIndex);
	}

	public Color getChannelColor(int imageIndex, int channelIndex)
	{
    return meta.getChannelColor(imageIndex, channelIndex);
	}

	public ContrastMethod getChannelContrastMethod(int imageIndex, int channelIndex)
	{
    return meta.getChannelContrastMethod(imageIndex, channelIndex);
	}

	// Ignoring DetectorSettings element, complex property
	public PositiveInteger getChannelEmissionWavelength(int imageIndex, int channelIndex)
	{
    return meta.getChannelEmissionWavelength(imageIndex, channelIndex);
	}

	public PositiveInteger getChannelExcitationWavelength(int imageIndex, int channelIndex)
	{
    return meta.getChannelExcitationWavelength(imageIndex, channelIndex);
	}

	public String getChannelFilterSetRef(int imageIndex, int channelIndex)
	{
    return meta.getChannelFilterSetRef(imageIndex, channelIndex);
	}

	public String getChannelFluor(int imageIndex, int channelIndex)
	{
    return meta.getChannelFluor(imageIndex, channelIndex);
	}

	public String getChannelID(int imageIndex, int channelIndex)
	{
    return meta.getChannelID(imageIndex, channelIndex);
	}

	public IlluminationType getChannelIlluminationType(int imageIndex, int channelIndex)
	{
    return meta.getChannelIlluminationType(imageIndex, channelIndex);
	}

	// Ignoring LightPath element, complex property
	// Ignoring LightSourceSettings element, complex property
	public Double getChannelNDFilter(int imageIndex, int channelIndex)
	{
    return meta.getChannelNDFilter(imageIndex, channelIndex);
	}

	public String getChannelName(int imageIndex, int channelIndex)
	{
    return meta.getChannelName(imageIndex, channelIndex);
	}

	public Double getChannelPinholeSize(int imageIndex, int channelIndex)
	{
    return meta.getChannelPinholeSize(imageIndex, channelIndex);
	}

	// Ignoring Pixels_BackReference back reference
	public Integer getChannelPockelCellSetting(int imageIndex, int channelIndex)
	{
    return meta.getChannelPockelCellSetting(imageIndex, channelIndex);
	}

	public PositiveInteger getChannelSamplesPerPixel(int imageIndex, int channelIndex)
	{
    return meta.getChannelSamplesPerPixel(imageIndex, channelIndex);
	}

	//
	// CommentAnnotation property storage
	//
	// Indexes: {u'StructuredAnnotations': [u'int commentAnnotationIndex']}
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public String getCommentAnnotationAnnotationRef(int commentAnnotationIndex, int annotationRefIndex)
	{
    return meta.getCommentAnnotationAnnotationRef(commentAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public String getCommentAnnotationDescription(int commentAnnotationIndex)
	{
    return meta.getCommentAnnotationDescription(commentAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public String getCommentAnnotationID(int commentAnnotationIndex)
	{
    return meta.getCommentAnnotationID(commentAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getCommentAnnotationNamespace(int commentAnnotationIndex)
	{
    return meta.getCommentAnnotationNamespace(commentAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public String getCommentAnnotationValue(int commentAnnotationIndex)
	{
    return meta.getCommentAnnotationValue(commentAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// Dataset property storage
	//
	// Indexes: {u'OME': [u'int datasetIndex']}
	// {u'OME': None}
	// Is multi path? False

	public String getDatasetAnnotationRef(int datasetIndex, int annotationRefIndex)
	{
    return meta.getDatasetAnnotationRef(datasetIndex, annotationRefIndex);
	}

	public String getDatasetDescription(int datasetIndex)
	{
    return meta.getDatasetDescription(datasetIndex);
	}

	public String getDatasetExperimenterGroupRef(int datasetIndex)
	{
    return meta.getDatasetExperimenterGroupRef(datasetIndex);
	}

	public String getDatasetExperimenterRef(int datasetIndex)
	{
    return meta.getDatasetExperimenterRef(datasetIndex);
	}

	public String getDatasetID(int datasetIndex)
	{
    return meta.getDatasetID(datasetIndex);
	}

	public String getDatasetImageRef(int datasetIndex, int imageRefIndex)
	{
    return meta.getDatasetImageRef(datasetIndex, imageRefIndex);
	}

	public String getDatasetName(int datasetIndex)
	{
    return meta.getDatasetName(datasetIndex);
	}

	// Ignoring Project_BackReference back reference
	//
	// DatasetRef property storage
	//
	// Indexes: {u'Project': [u'int projectIndex', u'int datasetRefIndex']}
	// {u'Project': {u'OME': None}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference DatasetRef

	//
	// Detector property storage
	//
	// Indexes: {u'Instrument': [u'int instrumentIndex', u'int detectorIndex']}
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	public Double getDetectorAmplificationGain(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorAmplificationGain(instrumentIndex, detectorIndex);
	}

	public Double getDetectorGain(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorGain(instrumentIndex, detectorIndex);
	}

	public String getDetectorID(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorID(instrumentIndex, detectorIndex);
	}

	// Ignoring Instrument_BackReference back reference
	public String getDetectorLotNumber(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorLotNumber(instrumentIndex, detectorIndex);
	}

	public String getDetectorManufacturer(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorManufacturer(instrumentIndex, detectorIndex);
	}

	public String getDetectorModel(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorModel(instrumentIndex, detectorIndex);
	}

	public Double getDetectorOffset(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorOffset(instrumentIndex, detectorIndex);
	}

	public String getDetectorSerialNumber(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorSerialNumber(instrumentIndex, detectorIndex);
	}

	public DetectorType getDetectorType(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorType(instrumentIndex, detectorIndex);
	}

	public Double getDetectorVoltage(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorVoltage(instrumentIndex, detectorIndex);
	}

	public Double getDetectorZoom(int instrumentIndex, int detectorIndex)
	{
    return meta.getDetectorZoom(instrumentIndex, detectorIndex);
	}

	//
	// DetectorSettings property storage
	//
	// Indexes: {u'Channel': [u'int imageIndex', u'int channelIndex']}
	// {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}
	// Is multi path? False

	public Binning getDetectorSettingsBinning(int imageIndex, int channelIndex)
	{
    return meta.getDetectorSettingsBinning(imageIndex, channelIndex);
	}

	// Ignoring DetectorRef back reference
	public Double getDetectorSettingsGain(int imageIndex, int channelIndex)
	{
    return meta.getDetectorSettingsGain(imageIndex, channelIndex);
	}

	public String getDetectorSettingsID(int imageIndex, int channelIndex)
	{
    return meta.getDetectorSettingsID(imageIndex, channelIndex);
	}

	public Double getDetectorSettingsOffset(int imageIndex, int channelIndex)
	{
    return meta.getDetectorSettingsOffset(imageIndex, channelIndex);
	}

	public Double getDetectorSettingsReadOutRate(int imageIndex, int channelIndex)
	{
    return meta.getDetectorSettingsReadOutRate(imageIndex, channelIndex);
	}

	public Double getDetectorSettingsVoltage(int imageIndex, int channelIndex)
	{
    return meta.getDetectorSettingsVoltage(imageIndex, channelIndex);
	}

	//
	// Dichroic property storage
	//
	// Indexes: {u'Instrument': [u'int instrumentIndex', u'int dichroicIndex']}
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	// Ignoring FilterSet_BackReference back reference
	public String getDichroicID(int instrumentIndex, int dichroicIndex)
	{
    return meta.getDichroicID(instrumentIndex, dichroicIndex);
	}

	// Ignoring Instrument_BackReference back reference
	// Ignoring LightPath_BackReference back reference
	public String getDichroicLotNumber(int instrumentIndex, int dichroicIndex)
	{
    return meta.getDichroicLotNumber(instrumentIndex, dichroicIndex);
	}

	public String getDichroicManufacturer(int instrumentIndex, int dichroicIndex)
	{
    return meta.getDichroicManufacturer(instrumentIndex, dichroicIndex);
	}

	public String getDichroicModel(int instrumentIndex, int dichroicIndex)
	{
    return meta.getDichroicModel(instrumentIndex, dichroicIndex);
	}

	public String getDichroicSerialNumber(int instrumentIndex, int dichroicIndex)
	{
    return meta.getDichroicSerialNumber(instrumentIndex, dichroicIndex);
	}

	//
	// DichroicRef property storage
	//
	// Indexes: {u'LightPath': [u'int imageIndex', u'int channelIndex'], u'FilterSet': [u'int instrumentIndex', u'int filterSetIndex']}
	// {u'LightPath': {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}, u'FilterSet': {u'Instrument': {u'OME': None}}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference DichroicRef

	//
	// DoubleAnnotation property storage
	//
	// Indexes: {u'StructuredAnnotations': [u'int doubleAnnotationIndex']}
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public String getDoubleAnnotationAnnotationRef(int doubleAnnotationIndex, int annotationRefIndex)
	{
    return meta.getDoubleAnnotationAnnotationRef(doubleAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public String getDoubleAnnotationDescription(int doubleAnnotationIndex)
	{
    return meta.getDoubleAnnotationDescription(doubleAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public String getDoubleAnnotationID(int doubleAnnotationIndex)
	{
    return meta.getDoubleAnnotationID(doubleAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getDoubleAnnotationNamespace(int doubleAnnotationIndex)
	{
    return meta.getDoubleAnnotationNamespace(doubleAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public Double getDoubleAnnotationValue(int doubleAnnotationIndex)
	{
    return meta.getDoubleAnnotationValue(doubleAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// Ellipse property storage
	//
	// Indexes: {u'Shape': [u'int ROIIndex', u'int shapeIndex']}
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public Color getEllipseFillColor(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseFillColor(ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public FillRule getEllipseFillRule(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseFillRule(ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public FontFamily getEllipseFontFamily(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseFontFamily(ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public NonNegativeInteger getEllipseFontSize(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseFontSize(ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public FontStyle getEllipseFontStyle(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseFontStyle(ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public String getEllipseID(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseID(ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public LineCap getEllipseLineCap(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseLineCap(ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public Boolean getEllipseLocked(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseLocked(ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public Color getEllipseStrokeColor(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseStrokeColor(ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public String getEllipseStrokeDashArray(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseStrokeDashArray(ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public Double getEllipseStrokeWidth(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseStrokeWidth(ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public String getEllipseText(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseText(ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public NonNegativeInteger getEllipseTheC(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseTheC(ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public NonNegativeInteger getEllipseTheT(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseTheT(ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public NonNegativeInteger getEllipseTheZ(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseTheZ(ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public AffineTransform getEllipseTransform(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseTransform(ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public Boolean getEllipseVisible(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseVisible(ROIIndex, shapeIndex);
	}

	public Double getEllipseRadiusX(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseRadiusX(ROIIndex, shapeIndex);
	}

	public Double getEllipseRadiusY(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseRadiusY(ROIIndex, shapeIndex);
	}

	public Double getEllipseX(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseX(ROIIndex, shapeIndex);
	}

	public Double getEllipseY(int ROIIndex, int shapeIndex)
	{
    return meta.getEllipseY(ROIIndex, shapeIndex);
	}

	//
	// EmissionFilterRef property storage
	//
	// Indexes: {u'LightPath': [u'int imageIndex', u'int channelIndex', u'int emissionFilterRefIndex'], u'FilterSet': [u'int instrumentIndex', u'int filterSetIndex', u'int emissionFilterRefIndex']}
	// {u'LightPath': {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}, u'FilterSet': {u'Instrument': {u'OME': None}}}
	// Is multi path? True

	//
	// ExcitationFilterRef property storage
	//
	// Indexes: {u'LightPath': [u'int imageIndex', u'int channelIndex', u'int excitationFilterRefIndex'], u'FilterSet': [u'int instrumentIndex', u'int filterSetIndex', u'int excitationFilterRefIndex']}
	// {u'LightPath': {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}, u'FilterSet': {u'Instrument': {u'OME': None}}}
	// Is multi path? True

	//
	// Experiment property storage
	//
	// Indexes: {u'OME': [u'int experimentIndex']}
	// {u'OME': None}
	// Is multi path? False

	public String getExperimentDescription(int experimentIndex)
	{
    return meta.getExperimentDescription(experimentIndex);
	}

	public String getExperimentExperimenterRef(int experimentIndex)
	{
    return meta.getExperimentExperimenterRef(experimentIndex);
	}

	public String getExperimentID(int experimentIndex)
	{
    return meta.getExperimentID(experimentIndex);
	}

	// Ignoring Image_BackReference back reference
	// Ignoring MicrobeamManipulation element, complex property
	public ExperimentType getExperimentType(int experimentIndex)
	{
    return meta.getExperimentType(experimentIndex);
	}

	//
	// ExperimentRef property storage
	//
	// Indexes: {u'Image': [u'int imageIndex']}
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference ExperimentRef

	//
	// Experimenter property storage
	//
	// Indexes: {u'OME': [u'int experimenterIndex']}
	// {u'OME': None}
	// Is multi path? False

	public String getExperimenterAnnotationRef(int experimenterIndex, int annotationRefIndex)
	{
    return meta.getExperimenterAnnotationRef(experimenterIndex, annotationRefIndex);
	}

	// Ignoring Dataset_BackReference back reference
	public String getExperimenterEmail(int experimenterIndex)
	{
    return meta.getExperimenterEmail(experimenterIndex);
	}

	// Ignoring Experiment_BackReference back reference
	// Ignoring ExperimenterGroup_BackReference back reference
	public String getExperimenterFirstName(int experimenterIndex)
	{
    return meta.getExperimenterFirstName(experimenterIndex);
	}

	public String getExperimenterID(int experimenterIndex)
	{
    return meta.getExperimenterID(experimenterIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getExperimenterInstitution(int experimenterIndex)
	{
    return meta.getExperimenterInstitution(experimenterIndex);
	}

	public String getExperimenterLastName(int experimenterIndex)
	{
    return meta.getExperimenterLastName(experimenterIndex);
	}

	// Ignoring MicrobeamManipulation_BackReference back reference
	public String getExperimenterMiddleName(int experimenterIndex)
	{
    return meta.getExperimenterMiddleName(experimenterIndex);
	}

	// Ignoring Project_BackReference back reference
	public String getExperimenterUserName(int experimenterIndex)
	{
    return meta.getExperimenterUserName(experimenterIndex);
	}

	//
	// ExperimenterGroup property storage
	//
	// Indexes: {u'OME': [u'int experimenterGroupIndex']}
	// {u'OME': None}
	// Is multi path? False

	public String getExperimenterGroupAnnotationRef(int experimenterGroupIndex, int annotationRefIndex)
	{
    return meta.getExperimenterGroupAnnotationRef(experimenterGroupIndex, annotationRefIndex);
	}

	// Ignoring Dataset_BackReference back reference
	public String getExperimenterGroupDescription(int experimenterGroupIndex)
	{
    return meta.getExperimenterGroupDescription(experimenterGroupIndex);
	}

	public String getExperimenterGroupExperimenterRef(int experimenterGroupIndex, int experimenterRefIndex)
	{
    return meta.getExperimenterGroupExperimenterRef(experimenterGroupIndex, experimenterRefIndex);
	}

	public String getExperimenterGroupID(int experimenterGroupIndex)
	{
    return meta.getExperimenterGroupID(experimenterGroupIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getExperimenterGroupLeader(int experimenterGroupIndex, int leaderIndex)
	{
    return meta.getExperimenterGroupLeader(experimenterGroupIndex, leaderIndex);
	}

	public String getExperimenterGroupName(int experimenterGroupIndex)
	{
    return meta.getExperimenterGroupName(experimenterGroupIndex);
	}

	// Ignoring Project_BackReference back reference
	//
	// ExperimenterGroupRef property storage
	//
	// Indexes: {u'Project': [u'int projectIndex'], u'Image': [u'int imageIndex'], u'Dataset': [u'int datasetIndex']}
	// {u'Project': {u'OME': None}, u'Image': {u'OME': None}, u'Dataset': {u'OME': None}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference ExperimenterGroupRef

	//
	// ExperimenterRef property storage
	//
	// Indexes: {u'ExperimenterGroup': [u'int experimenterGroupIndex', u'int experimenterRefIndex'], u'Image': [u'int imageIndex'], u'Dataset': [u'int datasetIndex'], u'Project': [u'int projectIndex'], u'Experiment': [u'int experimentIndex'], u'MicrobeamManipulation': [u'int experimentIndex', u'int microbeamManipulationIndex']}
	// {u'ExperimenterGroup': {u'OME': None}, u'Image': {u'OME': None}, u'Dataset': {u'OME': None}, u'Project': {u'OME': None}, u'Experiment': {u'OME': None}, u'MicrobeamManipulation': {u'Experiment': {u'OME': None}}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference ExperimenterRef

	//
	// Filament property storage
	//
	// Indexes: {u'LightSource': [u'int instrumentIndex', u'int lightSourceIndex']}
	// {u'LightSource': {u'Instrument': {u'OME': None}}}
	// Is multi path? False

	// Ignoring Arc of parent abstract type
	// Ignoring Filament of parent abstract type
	// ID accessor from parent LightSource
	public String getFilamentID(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getFilamentID(instrumentIndex, lightSourceIndex);
	}

	// Ignoring Laser of parent abstract type
	// Ignoring LightEmittingDiode of parent abstract type
	// LotNumber accessor from parent LightSource
	public String getFilamentLotNumber(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getFilamentLotNumber(instrumentIndex, lightSourceIndex);
	}

	// Manufacturer accessor from parent LightSource
	public String getFilamentManufacturer(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getFilamentManufacturer(instrumentIndex, lightSourceIndex);
	}

	// Model accessor from parent LightSource
	public String getFilamentModel(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getFilamentModel(instrumentIndex, lightSourceIndex);
	}

	// Power accessor from parent LightSource
	public Double getFilamentPower(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getFilamentPower(instrumentIndex, lightSourceIndex);
	}

	// SerialNumber accessor from parent LightSource
	public String getFilamentSerialNumber(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getFilamentSerialNumber(instrumentIndex, lightSourceIndex);
	}

	public FilamentType getFilamentType(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getFilamentType(instrumentIndex, lightSourceIndex);
	}

	//
	// FileAnnotation property storage
	//
	// Indexes: {u'StructuredAnnotations': [u'int fileAnnotationIndex']}
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public String getFileAnnotationAnnotationRef(int fileAnnotationIndex, int annotationRefIndex)
	{
    return meta.getFileAnnotationAnnotationRef(fileAnnotationIndex, annotationRefIndex);
	}

	// Ignoring BinaryFile element, complex property
	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public String getFileAnnotationDescription(int fileAnnotationIndex)
	{
    return meta.getFileAnnotationDescription(fileAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public String getFileAnnotationID(int fileAnnotationIndex)
	{
    return meta.getFileAnnotationID(fileAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getFileAnnotationNamespace(int fileAnnotationIndex)
	{
    return meta.getFileAnnotationNamespace(fileAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// Filter property storage
	//
	// Indexes: {u'Instrument': [u'int instrumentIndex', u'int filterIndex']}
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	// Ignoring FilterSet_BackReference back reference
	// Ignoring FilterSet_BackReference back reference
	public String getFilterFilterWheel(int instrumentIndex, int filterIndex)
	{
    return meta.getFilterFilterWheel(instrumentIndex, filterIndex);
	}

	public String getFilterID(int instrumentIndex, int filterIndex)
	{
    return meta.getFilterID(instrumentIndex, filterIndex);
	}

	// Ignoring Instrument_BackReference back reference
	// Ignoring LightPath_BackReference back reference
	// Ignoring LightPath_BackReference back reference
	public String getFilterLotNumber(int instrumentIndex, int filterIndex)
	{
    return meta.getFilterLotNumber(instrumentIndex, filterIndex);
	}

	public String getFilterManufacturer(int instrumentIndex, int filterIndex)
	{
    return meta.getFilterManufacturer(instrumentIndex, filterIndex);
	}

	public String getFilterModel(int instrumentIndex, int filterIndex)
	{
    return meta.getFilterModel(instrumentIndex, filterIndex);
	}

	public String getFilterSerialNumber(int instrumentIndex, int filterIndex)
	{
    return meta.getFilterSerialNumber(instrumentIndex, filterIndex);
	}

	// Ignoring TransmittanceRange element, complex property
	public FilterType getFilterType(int instrumentIndex, int filterIndex)
	{
    return meta.getFilterType(instrumentIndex, filterIndex);
	}

	//
	// FilterSet property storage
	//
	// Indexes: {u'Instrument': [u'int instrumentIndex', u'int filterSetIndex']}
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	// Ignoring Channel_BackReference back reference
	public String getFilterSetDichroicRef(int instrumentIndex, int filterSetIndex)
	{
    return meta.getFilterSetDichroicRef(instrumentIndex, filterSetIndex);
	}

	public String getFilterSetEmissionFilterRef(int instrumentIndex, int filterSetIndex, int emissionFilterRefIndex)
	{
    return meta.getFilterSetEmissionFilterRef(instrumentIndex, filterSetIndex, emissionFilterRefIndex);
	}

	public String getFilterSetExcitationFilterRef(int instrumentIndex, int filterSetIndex, int excitationFilterRefIndex)
	{
    return meta.getFilterSetExcitationFilterRef(instrumentIndex, filterSetIndex, excitationFilterRefIndex);
	}

	public String getFilterSetID(int instrumentIndex, int filterSetIndex)
	{
    return meta.getFilterSetID(instrumentIndex, filterSetIndex);
	}

	// Ignoring Instrument_BackReference back reference
	public String getFilterSetLotNumber(int instrumentIndex, int filterSetIndex)
	{
    return meta.getFilterSetLotNumber(instrumentIndex, filterSetIndex);
	}

	public String getFilterSetManufacturer(int instrumentIndex, int filterSetIndex)
	{
    return meta.getFilterSetManufacturer(instrumentIndex, filterSetIndex);
	}

	public String getFilterSetModel(int instrumentIndex, int filterSetIndex)
	{
    return meta.getFilterSetModel(instrumentIndex, filterSetIndex);
	}

	public String getFilterSetSerialNumber(int instrumentIndex, int filterSetIndex)
	{
    return meta.getFilterSetSerialNumber(instrumentIndex, filterSetIndex);
	}

	//
	// FilterSetRef property storage
	//
	// Indexes: {u'Channel': [u'int imageIndex', u'int channelIndex']}
	// {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference FilterSetRef

	//
	// Image property storage
	//
	// Indexes: {u'OME': [u'int imageIndex']}
	// {u'OME': None}
	// Is multi path? False

	public Timestamp getImageAcquisitionDate(int imageIndex)
	{
    return meta.getImageAcquisitionDate(imageIndex);
	}

	public String getImageAnnotationRef(int imageIndex, int annotationRefIndex)
	{
    return meta.getImageAnnotationRef(imageIndex, annotationRefIndex);
	}

	// Ignoring Dataset_BackReference back reference
	public String getImageDescription(int imageIndex)
	{
    return meta.getImageDescription(imageIndex);
	}

	public String getImageExperimentRef(int imageIndex)
	{
    return meta.getImageExperimentRef(imageIndex);
	}

	public String getImageExperimenterGroupRef(int imageIndex)
	{
    return meta.getImageExperimenterGroupRef(imageIndex);
	}

	public String getImageExperimenterRef(int imageIndex)
	{
    return meta.getImageExperimenterRef(imageIndex);
	}

	public String getImageID(int imageIndex)
	{
    return meta.getImageID(imageIndex);
	}

	// Ignoring ImagingEnvironment element, complex property
	public String getImageInstrumentRef(int imageIndex)
	{
    return meta.getImageInstrumentRef(imageIndex);
	}

	public String getImageMicrobeamManipulationRef(int imageIndex, int microbeamManipulationRefIndex)
	{
    return meta.getImageMicrobeamManipulationRef(imageIndex, microbeamManipulationRefIndex);
	}

	public String getImageName(int imageIndex)
	{
    return meta.getImageName(imageIndex);
	}

	// Ignoring ObjectiveSettings element, complex property
	// Ignoring Pixels element, complex property
	public String getImageROIRef(int imageIndex, int ROIRefIndex)
	{
    return meta.getImageROIRef(imageIndex, ROIRefIndex);
	}

	// Ignoring StageLabel element, complex property
	// Ignoring WellSample_BackReference back reference
	//
	// ImageRef property storage
	//
	// Indexes: {u'WellSample': [u'int plateIndex', u'int wellIndex', u'int wellSampleIndex'], u'Dataset': [u'int datasetIndex', u'int imageRefIndex']}
	// {u'WellSample': {u'Well': {u'Plate': {u'OME': None}}}, u'Dataset': {u'OME': None}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference ImageRef

	//
	// ImagingEnvironment property storage
	//
	// Indexes: {u'Image': [u'int imageIndex']}
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	public Double getImagingEnvironmentAirPressure(int imageIndex)
	{
    return meta.getImagingEnvironmentAirPressure(imageIndex);
	}

	public PercentFraction getImagingEnvironmentCO2Percent(int imageIndex)
	{
    return meta.getImagingEnvironmentCO2Percent(imageIndex);
	}

	public PercentFraction getImagingEnvironmentHumidity(int imageIndex)
	{
    return meta.getImagingEnvironmentHumidity(imageIndex);
	}

	public Double getImagingEnvironmentTemperature(int imageIndex)
	{
    return meta.getImagingEnvironmentTemperature(imageIndex);
	}

	//
	// Instrument property storage
	//
	// Indexes: {u'OME': [u'int instrumentIndex']}
	// {u'OME': None}
	// Is multi path? False

	// Ignoring Detector element, complex property
	// Ignoring Dichroic element, complex property
	// Ignoring Filter element, complex property
	// Ignoring FilterSet element, complex property
	public String getInstrumentID(int instrumentIndex)
	{
    return meta.getInstrumentID(instrumentIndex);
	}

	// Ignoring Image_BackReference back reference
	// Ignoring LightSource element, complex property
	// Ignoring Microscope element, complex property
	// Ignoring Objective element, complex property
	//
	// InstrumentRef property storage
	//
	// Indexes: {u'Image': [u'int imageIndex']}
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference InstrumentRef

	//
	// Label property storage
	//
	// Indexes: {u'Shape': [u'int ROIIndex', u'int shapeIndex']}
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public Color getLabelFillColor(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelFillColor(ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public FillRule getLabelFillRule(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelFillRule(ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public FontFamily getLabelFontFamily(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelFontFamily(ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public NonNegativeInteger getLabelFontSize(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelFontSize(ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public FontStyle getLabelFontStyle(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelFontStyle(ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public String getLabelID(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelID(ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public LineCap getLabelLineCap(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelLineCap(ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public Boolean getLabelLocked(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelLocked(ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public Color getLabelStrokeColor(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelStrokeColor(ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public String getLabelStrokeDashArray(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelStrokeDashArray(ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public Double getLabelStrokeWidth(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelStrokeWidth(ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public String getLabelText(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelText(ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public NonNegativeInteger getLabelTheC(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelTheC(ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public NonNegativeInteger getLabelTheT(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelTheT(ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public NonNegativeInteger getLabelTheZ(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelTheZ(ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public AffineTransform getLabelTransform(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelTransform(ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public Boolean getLabelVisible(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelVisible(ROIIndex, shapeIndex);
	}

	public Double getLabelX(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelX(ROIIndex, shapeIndex);
	}

	public Double getLabelY(int ROIIndex, int shapeIndex)
	{
    return meta.getLabelY(ROIIndex, shapeIndex);
	}

	//
	// Laser property storage
	//
	// Indexes: {u'LightSource': [u'int instrumentIndex', u'int lightSourceIndex']}
	// {u'LightSource': {u'Instrument': {u'OME': None}}}
	// Is multi path? False

	// Ignoring Arc of parent abstract type
	// Ignoring Filament of parent abstract type
	// ID accessor from parent LightSource
	public String getLaserID(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserID(instrumentIndex, lightSourceIndex);
	}

	// Ignoring Laser of parent abstract type
	// Ignoring LightEmittingDiode of parent abstract type
	// LotNumber accessor from parent LightSource
	public String getLaserLotNumber(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserLotNumber(instrumentIndex, lightSourceIndex);
	}

	// Manufacturer accessor from parent LightSource
	public String getLaserManufacturer(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserManufacturer(instrumentIndex, lightSourceIndex);
	}

	// Model accessor from parent LightSource
	public String getLaserModel(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserModel(instrumentIndex, lightSourceIndex);
	}

	// Power accessor from parent LightSource
	public Double getLaserPower(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserPower(instrumentIndex, lightSourceIndex);
	}

	// SerialNumber accessor from parent LightSource
	public String getLaserSerialNumber(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserSerialNumber(instrumentIndex, lightSourceIndex);
	}

	public PositiveInteger getLaserFrequencyMultiplication(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserFrequencyMultiplication(instrumentIndex, lightSourceIndex);
	}

	public LaserMedium getLaserLaserMedium(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserLaserMedium(instrumentIndex, lightSourceIndex);
	}

	public Boolean getLaserPockelCell(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserPockelCell(instrumentIndex, lightSourceIndex);
	}

	public Pulse getLaserPulse(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserPulse(instrumentIndex, lightSourceIndex);
	}

	public String getLaserPump(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserPump(instrumentIndex, lightSourceIndex);
	}

	public Double getLaserRepetitionRate(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserRepetitionRate(instrumentIndex, lightSourceIndex);
	}

	public Boolean getLaserTuneable(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserTuneable(instrumentIndex, lightSourceIndex);
	}

	public LaserType getLaserType(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserType(instrumentIndex, lightSourceIndex);
	}

	public PositiveInteger getLaserWavelength(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLaserWavelength(instrumentIndex, lightSourceIndex);
	}

	//
	// Leader property storage
	//
	// Indexes: {u'ExperimenterGroup': [u'int experimenterGroupIndex', u'int leaderIndex']}
	// {u'ExperimenterGroup': {u'OME': None}}
	// Is multi path? False

	// 0:9999
	// Is multi path? False
	// Ignoring ExperimenterGroup_BackReference property of reference Leader

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference Leader

	//
	// LightEmittingDiode property storage
	//
	// Indexes: {u'LightSource': [u'int instrumentIndex', u'int lightSourceIndex']}
	// {u'LightSource': {u'Instrument': {u'OME': None}}}
	// Is multi path? False

	// Ignoring Arc of parent abstract type
	// Ignoring Filament of parent abstract type
	// ID accessor from parent LightSource
	public String getLightEmittingDiodeID(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLightEmittingDiodeID(instrumentIndex, lightSourceIndex);
	}

	// Ignoring Laser of parent abstract type
	// Ignoring LightEmittingDiode of parent abstract type
	// LotNumber accessor from parent LightSource
	public String getLightEmittingDiodeLotNumber(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLightEmittingDiodeLotNumber(instrumentIndex, lightSourceIndex);
	}

	// Manufacturer accessor from parent LightSource
	public String getLightEmittingDiodeManufacturer(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLightEmittingDiodeManufacturer(instrumentIndex, lightSourceIndex);
	}

	// Model accessor from parent LightSource
	public String getLightEmittingDiodeModel(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLightEmittingDiodeModel(instrumentIndex, lightSourceIndex);
	}

	// Power accessor from parent LightSource
	public Double getLightEmittingDiodePower(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLightEmittingDiodePower(instrumentIndex, lightSourceIndex);
	}

	// SerialNumber accessor from parent LightSource
	public String getLightEmittingDiodeSerialNumber(int instrumentIndex, int lightSourceIndex)
	{
    return meta.getLightEmittingDiodeSerialNumber(instrumentIndex, lightSourceIndex);
	}

	//
	// LightPath property storage
	//
	// Indexes: {u'Channel': [u'int imageIndex', u'int channelIndex']}
	// {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}
	// Is multi path? False

	public String getLightPathDichroicRef(int imageIndex, int channelIndex)
	{
    return meta.getLightPathDichroicRef(imageIndex, channelIndex);
	}

	public String getLightPathEmissionFilterRef(int imageIndex, int channelIndex, int emissionFilterRefIndex)
	{
    return meta.getLightPathEmissionFilterRef(imageIndex, channelIndex, emissionFilterRefIndex);
	}

	public String getLightPathExcitationFilterRef(int imageIndex, int channelIndex, int excitationFilterRefIndex)
	{
    return meta.getLightPathExcitationFilterRef(imageIndex, channelIndex, excitationFilterRefIndex);
	}

	//
	// LightSourceSettings property storage
	//
	// Indexes: {u'Channel': [u'int imageIndex', u'int channelIndex'], u'MicrobeamManipulation': [u'int experimentIndex', u'int microbeamManipulationIndex', u'int lightSourceSettingsIndex']}
	// {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}, u'MicrobeamManipulation': {u'Experiment': {u'OME': None}}}
	// Is multi path? True

	public PercentFraction getChannelLightSourceSettingsAttenuation(int imageIndex, int channelIndex)
	{
    return meta.getChannelLightSourceSettingsAttenuation(imageIndex, channelIndex);
	}

	public PercentFraction getMicrobeamManipulationLightSourceSettingsAttenuation(int experimentIndex, int microbeamManipulationIndex, int lightSourceSettingsIndex)
	{
    return meta.getMicrobeamManipulationLightSourceSettingsAttenuation(experimentIndex, microbeamManipulationIndex, lightSourceSettingsIndex);
	}

	public String getChannelLightSourceSettingsID(int imageIndex, int channelIndex)
	{
    return meta.getChannelLightSourceSettingsID(imageIndex, channelIndex);
	}

	public String getMicrobeamManipulationLightSourceSettingsID(int experimentIndex, int microbeamManipulationIndex, int lightSourceSettingsIndex)
	{
    return meta.getMicrobeamManipulationLightSourceSettingsID(experimentIndex, microbeamManipulationIndex, lightSourceSettingsIndex);
	}

	// Ignoring LightSourceRef back reference
	// Ignoring MicrobeamManipulation_BackReference back reference
	public PositiveInteger getChannelLightSourceSettingsWavelength(int imageIndex, int channelIndex)
	{
    return meta.getChannelLightSourceSettingsWavelength(imageIndex, channelIndex);
	}

	public PositiveInteger getMicrobeamManipulationLightSourceSettingsWavelength(int experimentIndex, int microbeamManipulationIndex, int lightSourceSettingsIndex)
	{
    return meta.getMicrobeamManipulationLightSourceSettingsWavelength(experimentIndex, microbeamManipulationIndex, lightSourceSettingsIndex);
	}

	//
	// Line property storage
	//
	// Indexes: {u'Shape': [u'int ROIIndex', u'int shapeIndex']}
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public Color getLineFillColor(int ROIIndex, int shapeIndex)
	{
    return meta.getLineFillColor(ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public FillRule getLineFillRule(int ROIIndex, int shapeIndex)
	{
    return meta.getLineFillRule(ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public FontFamily getLineFontFamily(int ROIIndex, int shapeIndex)
	{
    return meta.getLineFontFamily(ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public NonNegativeInteger getLineFontSize(int ROIIndex, int shapeIndex)
	{
    return meta.getLineFontSize(ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public FontStyle getLineFontStyle(int ROIIndex, int shapeIndex)
	{
    return meta.getLineFontStyle(ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public String getLineID(int ROIIndex, int shapeIndex)
	{
    return meta.getLineID(ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public LineCap getLineLineCap(int ROIIndex, int shapeIndex)
	{
    return meta.getLineLineCap(ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public Boolean getLineLocked(int ROIIndex, int shapeIndex)
	{
    return meta.getLineLocked(ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public Color getLineStrokeColor(int ROIIndex, int shapeIndex)
	{
    return meta.getLineStrokeColor(ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public String getLineStrokeDashArray(int ROIIndex, int shapeIndex)
	{
    return meta.getLineStrokeDashArray(ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public Double getLineStrokeWidth(int ROIIndex, int shapeIndex)
	{
    return meta.getLineStrokeWidth(ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public String getLineText(int ROIIndex, int shapeIndex)
	{
    return meta.getLineText(ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public NonNegativeInteger getLineTheC(int ROIIndex, int shapeIndex)
	{
    return meta.getLineTheC(ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public NonNegativeInteger getLineTheT(int ROIIndex, int shapeIndex)
	{
    return meta.getLineTheT(ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public NonNegativeInteger getLineTheZ(int ROIIndex, int shapeIndex)
	{
    return meta.getLineTheZ(ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public AffineTransform getLineTransform(int ROIIndex, int shapeIndex)
	{
    return meta.getLineTransform(ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public Boolean getLineVisible(int ROIIndex, int shapeIndex)
	{
    return meta.getLineVisible(ROIIndex, shapeIndex);
	}

	public Marker getLineMarkerEnd(int ROIIndex, int shapeIndex)
	{
    return meta.getLineMarkerEnd(ROIIndex, shapeIndex);
	}

	public Marker getLineMarkerStart(int ROIIndex, int shapeIndex)
	{
    return meta.getLineMarkerStart(ROIIndex, shapeIndex);
	}

	public Double getLineX1(int ROIIndex, int shapeIndex)
	{
    return meta.getLineX1(ROIIndex, shapeIndex);
	}

	public Double getLineX2(int ROIIndex, int shapeIndex)
	{
    return meta.getLineX2(ROIIndex, shapeIndex);
	}

	public Double getLineY1(int ROIIndex, int shapeIndex)
	{
    return meta.getLineY1(ROIIndex, shapeIndex);
	}

	public Double getLineY2(int ROIIndex, int shapeIndex)
	{
    return meta.getLineY2(ROIIndex, shapeIndex);
	}

	//
	// ListAnnotation property storage
	//
	// Indexes: {u'StructuredAnnotations': [u'int listAnnotationIndex']}
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public String getListAnnotationAnnotationRef(int listAnnotationIndex, int annotationRefIndex)
	{
    return meta.getListAnnotationAnnotationRef(listAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public String getListAnnotationDescription(int listAnnotationIndex)
	{
    return meta.getListAnnotationDescription(listAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public String getListAnnotationID(int listAnnotationIndex)
	{
    return meta.getListAnnotationID(listAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getListAnnotationNamespace(int listAnnotationIndex)
	{
    return meta.getListAnnotationNamespace(listAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// LongAnnotation property storage
	//
	// Indexes: {u'StructuredAnnotations': [u'int longAnnotationIndex']}
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public String getLongAnnotationAnnotationRef(int longAnnotationIndex, int annotationRefIndex)
	{
    return meta.getLongAnnotationAnnotationRef(longAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public String getLongAnnotationDescription(int longAnnotationIndex)
	{
    return meta.getLongAnnotationDescription(longAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public String getLongAnnotationID(int longAnnotationIndex)
	{
    return meta.getLongAnnotationID(longAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getLongAnnotationNamespace(int longAnnotationIndex)
	{
    return meta.getLongAnnotationNamespace(longAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public Long getLongAnnotationValue(int longAnnotationIndex)
	{
    return meta.getLongAnnotationValue(longAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// Mask property storage
	//
	// Indexes: {u'Shape': [u'int ROIIndex', u'int shapeIndex']}
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public Color getMaskFillColor(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskFillColor(ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public FillRule getMaskFillRule(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskFillRule(ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public FontFamily getMaskFontFamily(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskFontFamily(ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public NonNegativeInteger getMaskFontSize(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskFontSize(ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public FontStyle getMaskFontStyle(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskFontStyle(ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public String getMaskID(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskID(ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public LineCap getMaskLineCap(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskLineCap(ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public Boolean getMaskLocked(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskLocked(ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public Color getMaskStrokeColor(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskStrokeColor(ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public String getMaskStrokeDashArray(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskStrokeDashArray(ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public Double getMaskStrokeWidth(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskStrokeWidth(ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public String getMaskText(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskText(ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public NonNegativeInteger getMaskTheC(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskTheC(ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public NonNegativeInteger getMaskTheT(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskTheT(ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public NonNegativeInteger getMaskTheZ(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskTheZ(ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public AffineTransform getMaskTransform(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskTransform(ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public Boolean getMaskVisible(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskVisible(ROIIndex, shapeIndex);
	}

	// Ignoring BinData element, complex property
	public Double getMaskHeight(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskHeight(ROIIndex, shapeIndex);
	}

	public Double getMaskWidth(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskWidth(ROIIndex, shapeIndex);
	}

	public Double getMaskX(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskX(ROIIndex, shapeIndex);
	}

	public Double getMaskY(int ROIIndex, int shapeIndex)
	{
    return meta.getMaskY(ROIIndex, shapeIndex);
	}

	//
	// MetadataOnly property storage
	//
	// Indexes: {u'Pixels': [u'int imageIndex']}
	// {u'Pixels': {u'Image': {u'OME': None}}}
	// Is multi path? False

	//
	// MicrobeamManipulation property storage
	//
	// Indexes: {u'Experiment': [u'int experimentIndex', u'int microbeamManipulationIndex']}
	// {u'Experiment': {u'OME': None}}
	// Is multi path? False

	public String getMicrobeamManipulationDescription(int experimentIndex, int microbeamManipulationIndex)
	{
    return meta.getMicrobeamManipulationDescription(experimentIndex, microbeamManipulationIndex);
	}

	// Ignoring Experiment_BackReference back reference
	public String getMicrobeamManipulationExperimenterRef(int experimentIndex, int microbeamManipulationIndex)
	{
    return meta.getMicrobeamManipulationExperimenterRef(experimentIndex, microbeamManipulationIndex);
	}

	public String getMicrobeamManipulationID(int experimentIndex, int microbeamManipulationIndex)
	{
    return meta.getMicrobeamManipulationID(experimentIndex, microbeamManipulationIndex);
	}

	// Ignoring Image_BackReference back reference
	// Ignoring LightSourceSettings element, complex property
	public String getMicrobeamManipulationROIRef(int experimentIndex, int microbeamManipulationIndex, int ROIRefIndex)
	{
    return meta.getMicrobeamManipulationROIRef(experimentIndex, microbeamManipulationIndex, ROIRefIndex);
	}

	public MicrobeamManipulationType getMicrobeamManipulationType(int experimentIndex, int microbeamManipulationIndex)
	{
    return meta.getMicrobeamManipulationType(experimentIndex, microbeamManipulationIndex);
	}

	//
	// MicrobeamManipulationRef property storage
	//
	// Indexes: {u'Image': [u'int imageIndex', u'int microbeamManipulationRefIndex']}
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference MicrobeamManipulationRef

	//
	// Microscope property storage
	//
	// Indexes: {u'Instrument': [u'int instrumentIndex']}
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	public String getMicroscopeLotNumber(int instrumentIndex)
	{
    return meta.getMicroscopeLotNumber(instrumentIndex);
	}

	public String getMicroscopeManufacturer(int instrumentIndex)
	{
    return meta.getMicroscopeManufacturer(instrumentIndex);
	}

	public String getMicroscopeModel(int instrumentIndex)
	{
    return meta.getMicroscopeModel(instrumentIndex);
	}

	public String getMicroscopeSerialNumber(int instrumentIndex)
	{
    return meta.getMicroscopeSerialNumber(instrumentIndex);
	}

	public MicroscopeType getMicroscopeType(int instrumentIndex)
	{
    return meta.getMicroscopeType(instrumentIndex);
	}

	//
	// Objective property storage
	//
	// Indexes: {u'Instrument': [u'int instrumentIndex', u'int objectiveIndex']}
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	public Double getObjectiveCalibratedMagnification(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveCalibratedMagnification(instrumentIndex, objectiveIndex);
	}

	public Correction getObjectiveCorrection(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveCorrection(instrumentIndex, objectiveIndex);
	}

	public String getObjectiveID(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveID(instrumentIndex, objectiveIndex);
	}

	public Immersion getObjectiveImmersion(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveImmersion(instrumentIndex, objectiveIndex);
	}

	// Ignoring Instrument_BackReference back reference
	public Boolean getObjectiveIris(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveIris(instrumentIndex, objectiveIndex);
	}

	public Double getObjectiveLensNA(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveLensNA(instrumentIndex, objectiveIndex);
	}

	public String getObjectiveLotNumber(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveLotNumber(instrumentIndex, objectiveIndex);
	}

	public String getObjectiveManufacturer(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveManufacturer(instrumentIndex, objectiveIndex);
	}

	public String getObjectiveModel(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveModel(instrumentIndex, objectiveIndex);
	}

	public PositiveInteger getObjectiveNominalMagnification(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveNominalMagnification(instrumentIndex, objectiveIndex);
	}

	public String getObjectiveSerialNumber(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveSerialNumber(instrumentIndex, objectiveIndex);
	}

	public Double getObjectiveWorkingDistance(int instrumentIndex, int objectiveIndex)
	{
    return meta.getObjectiveWorkingDistance(instrumentIndex, objectiveIndex);
	}

	//
	// ObjectiveSettings property storage
	//
	// Indexes: {u'Image': [u'int imageIndex']}
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	public Double getObjectiveSettingsCorrectionCollar(int imageIndex)
	{
    return meta.getObjectiveSettingsCorrectionCollar(imageIndex);
	}

	public String getObjectiveSettingsID(int imageIndex)
	{
    return meta.getObjectiveSettingsID(imageIndex);
	}

	public Medium getObjectiveSettingsMedium(int imageIndex)
	{
    return meta.getObjectiveSettingsMedium(imageIndex);
	}

	// Ignoring ObjectiveRef back reference
	public Double getObjectiveSettingsRefractiveIndex(int imageIndex)
	{
    return meta.getObjectiveSettingsRefractiveIndex(imageIndex);
	}

	//
	// Pixels property storage
	//
	// Indexes: {u'Image': [u'int imageIndex']}
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	public String getPixelsAnnotationRef(int imageIndex, int annotationRefIndex)
	{
    return meta.getPixelsAnnotationRef(imageIndex, annotationRefIndex);
	}

	// Ignoring BinData element, complex property
	// Ignoring Channel element, complex property
	public DimensionOrder getPixelsDimensionOrder(int imageIndex)
	{
    return meta.getPixelsDimensionOrder(imageIndex);
	}

	public String getPixelsID(int imageIndex)
	{
    return meta.getPixelsID(imageIndex);
	}

	// Ignoring MetadataOnly element, complex property
	public PositiveFloat getPixelsPhysicalSizeX(int imageIndex)
	{
    return meta.getPixelsPhysicalSizeX(imageIndex);
	}

	public PositiveFloat getPixelsPhysicalSizeY(int imageIndex)
	{
    return meta.getPixelsPhysicalSizeY(imageIndex);
	}

	public PositiveFloat getPixelsPhysicalSizeZ(int imageIndex)
	{
    return meta.getPixelsPhysicalSizeZ(imageIndex);
	}

	// Ignoring Plane element, complex property
	public PositiveInteger getPixelsSizeC(int imageIndex)
	{
    return meta.getPixelsSizeC(imageIndex);
	}

	public PositiveInteger getPixelsSizeT(int imageIndex)
	{
    return meta.getPixelsSizeT(imageIndex);
	}

	public PositiveInteger getPixelsSizeX(int imageIndex)
	{
    return meta.getPixelsSizeX(imageIndex);
	}

	public PositiveInteger getPixelsSizeY(int imageIndex)
	{
    return meta.getPixelsSizeY(imageIndex);
	}

	public PositiveInteger getPixelsSizeZ(int imageIndex)
	{
    return meta.getPixelsSizeZ(imageIndex);
	}

	// Ignoring TiffData element, complex property
	public Double getPixelsTimeIncrement(int imageIndex)
	{
    return meta.getPixelsTimeIncrement(imageIndex);
	}

	public PixelType getPixelsType(int imageIndex)
	{
    return meta.getPixelsType(imageIndex);
	}

	//
	// Plane property storage
	//
	// Indexes: {u'Pixels': [u'int imageIndex', u'int planeIndex']}
	// {u'Pixels': {u'Image': {u'OME': None}}}
	// Is multi path? False

	public String getPlaneAnnotationRef(int imageIndex, int planeIndex, int annotationRefIndex)
	{
    return meta.getPlaneAnnotationRef(imageIndex, planeIndex, annotationRefIndex);
	}

	public Double getPlaneDeltaT(int imageIndex, int planeIndex)
	{
    return meta.getPlaneDeltaT(imageIndex, planeIndex);
	}

	public Double getPlaneExposureTime(int imageIndex, int planeIndex)
	{
    return meta.getPlaneExposureTime(imageIndex, planeIndex);
	}

	public String getPlaneHashSHA1(int imageIndex, int planeIndex)
	{
    return meta.getPlaneHashSHA1(imageIndex, planeIndex);
	}

	// Ignoring Pixels_BackReference back reference
	public Double getPlanePositionX(int imageIndex, int planeIndex)
	{
    return meta.getPlanePositionX(imageIndex, planeIndex);
	}

	public Double getPlanePositionY(int imageIndex, int planeIndex)
	{
    return meta.getPlanePositionY(imageIndex, planeIndex);
	}

	public Double getPlanePositionZ(int imageIndex, int planeIndex)
	{
    return meta.getPlanePositionZ(imageIndex, planeIndex);
	}

	public NonNegativeInteger getPlaneTheC(int imageIndex, int planeIndex)
	{
    return meta.getPlaneTheC(imageIndex, planeIndex);
	}

	public NonNegativeInteger getPlaneTheT(int imageIndex, int planeIndex)
	{
    return meta.getPlaneTheT(imageIndex, planeIndex);
	}

	public NonNegativeInteger getPlaneTheZ(int imageIndex, int planeIndex)
	{
    return meta.getPlaneTheZ(imageIndex, planeIndex);
	}

	//
	// Plate property storage
	//
	// Indexes: {u'OME': [u'int plateIndex']}
	// {u'OME': None}
	// Is multi path? False

	public String getPlateAnnotationRef(int plateIndex, int annotationRefIndex)
	{
    return meta.getPlateAnnotationRef(plateIndex, annotationRefIndex);
	}

	public NamingConvention getPlateColumnNamingConvention(int plateIndex)
	{
    return meta.getPlateColumnNamingConvention(plateIndex);
	}

	public PositiveInteger getPlateColumns(int plateIndex)
	{
    return meta.getPlateColumns(plateIndex);
	}

	public String getPlateDescription(int plateIndex)
	{
    return meta.getPlateDescription(plateIndex);
	}

	public String getPlateExternalIdentifier(int plateIndex)
	{
    return meta.getPlateExternalIdentifier(plateIndex);
	}

	public NonNegativeInteger getPlateFieldIndex(int plateIndex)
	{
    return meta.getPlateFieldIndex(plateIndex);
	}

	public String getPlateID(int plateIndex)
	{
    return meta.getPlateID(plateIndex);
	}

	public String getPlateName(int plateIndex)
	{
    return meta.getPlateName(plateIndex);
	}

	// Ignoring PlateAcquisition element, complex property
	public NamingConvention getPlateRowNamingConvention(int plateIndex)
	{
    return meta.getPlateRowNamingConvention(plateIndex);
	}

	public PositiveInteger getPlateRows(int plateIndex)
	{
    return meta.getPlateRows(plateIndex);
	}

	// Ignoring Screen_BackReference back reference
	public String getPlateStatus(int plateIndex)
	{
    return meta.getPlateStatus(plateIndex);
	}

	// Ignoring Well element, complex property
	public Double getPlateWellOriginX(int plateIndex)
	{
    return meta.getPlateWellOriginX(plateIndex);
	}

	public Double getPlateWellOriginY(int plateIndex)
	{
    return meta.getPlateWellOriginY(plateIndex);
	}

	//
	// PlateAcquisition property storage
	//
	// Indexes: {u'Plate': [u'int plateIndex', u'int plateAcquisitionIndex']}
	// {u'Plate': {u'OME': None}}
	// Is multi path? False

	public String getPlateAcquisitionAnnotationRef(int plateIndex, int plateAcquisitionIndex, int annotationRefIndex)
	{
    return meta.getPlateAcquisitionAnnotationRef(plateIndex, plateAcquisitionIndex, annotationRefIndex);
	}

	public String getPlateAcquisitionDescription(int plateIndex, int plateAcquisitionIndex)
	{
    return meta.getPlateAcquisitionDescription(plateIndex, plateAcquisitionIndex);
	}

	public Timestamp getPlateAcquisitionEndTime(int plateIndex, int plateAcquisitionIndex)
	{
    return meta.getPlateAcquisitionEndTime(plateIndex, plateAcquisitionIndex);
	}

	public String getPlateAcquisitionID(int plateIndex, int plateAcquisitionIndex)
	{
    return meta.getPlateAcquisitionID(plateIndex, plateAcquisitionIndex);
	}

	public PositiveInteger getPlateAcquisitionMaximumFieldCount(int plateIndex, int plateAcquisitionIndex)
	{
    return meta.getPlateAcquisitionMaximumFieldCount(plateIndex, plateAcquisitionIndex);
	}

	public String getPlateAcquisitionName(int plateIndex, int plateAcquisitionIndex)
	{
    return meta.getPlateAcquisitionName(plateIndex, plateAcquisitionIndex);
	}

	// Ignoring Plate_BackReference back reference
	public Timestamp getPlateAcquisitionStartTime(int plateIndex, int plateAcquisitionIndex)
	{
    return meta.getPlateAcquisitionStartTime(plateIndex, plateAcquisitionIndex);
	}

	public String getPlateAcquisitionWellSampleRef(int plateIndex, int plateAcquisitionIndex, int wellSampleRefIndex)
	{
    return meta.getPlateAcquisitionWellSampleRef(plateIndex, plateAcquisitionIndex, wellSampleRefIndex);
	}

	//
	// PlateRef property storage
	//
	// Indexes: {u'Screen': [u'int screenIndex', u'int plateRefIndex']}
	// {u'Screen': {u'OME': None}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference PlateRef

	//
	// Point property storage
	//
	// Indexes: {u'Shape': [u'int ROIIndex', u'int shapeIndex']}
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public Color getPointFillColor(int ROIIndex, int shapeIndex)
	{
    return meta.getPointFillColor(ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public FillRule getPointFillRule(int ROIIndex, int shapeIndex)
	{
    return meta.getPointFillRule(ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public FontFamily getPointFontFamily(int ROIIndex, int shapeIndex)
	{
    return meta.getPointFontFamily(ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public NonNegativeInteger getPointFontSize(int ROIIndex, int shapeIndex)
	{
    return meta.getPointFontSize(ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public FontStyle getPointFontStyle(int ROIIndex, int shapeIndex)
	{
    return meta.getPointFontStyle(ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public String getPointID(int ROIIndex, int shapeIndex)
	{
    return meta.getPointID(ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public LineCap getPointLineCap(int ROIIndex, int shapeIndex)
	{
    return meta.getPointLineCap(ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public Boolean getPointLocked(int ROIIndex, int shapeIndex)
	{
    return meta.getPointLocked(ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public Color getPointStrokeColor(int ROIIndex, int shapeIndex)
	{
    return meta.getPointStrokeColor(ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public String getPointStrokeDashArray(int ROIIndex, int shapeIndex)
	{
    return meta.getPointStrokeDashArray(ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public Double getPointStrokeWidth(int ROIIndex, int shapeIndex)
	{
    return meta.getPointStrokeWidth(ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public String getPointText(int ROIIndex, int shapeIndex)
	{
    return meta.getPointText(ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public NonNegativeInteger getPointTheC(int ROIIndex, int shapeIndex)
	{
    return meta.getPointTheC(ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public NonNegativeInteger getPointTheT(int ROIIndex, int shapeIndex)
	{
    return meta.getPointTheT(ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public NonNegativeInteger getPointTheZ(int ROIIndex, int shapeIndex)
	{
    return meta.getPointTheZ(ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public AffineTransform getPointTransform(int ROIIndex, int shapeIndex)
	{
    return meta.getPointTransform(ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public Boolean getPointVisible(int ROIIndex, int shapeIndex)
	{
    return meta.getPointVisible(ROIIndex, shapeIndex);
	}

	public Double getPointX(int ROIIndex, int shapeIndex)
	{
    return meta.getPointX(ROIIndex, shapeIndex);
	}

	public Double getPointY(int ROIIndex, int shapeIndex)
	{
    return meta.getPointY(ROIIndex, shapeIndex);
	}

	//
	// Polygon property storage
	//
	// Indexes: {u'Shape': [u'int ROIIndex', u'int shapeIndex']}
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public Color getPolygonFillColor(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonFillColor(ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public FillRule getPolygonFillRule(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonFillRule(ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public FontFamily getPolygonFontFamily(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonFontFamily(ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public NonNegativeInteger getPolygonFontSize(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonFontSize(ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public FontStyle getPolygonFontStyle(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonFontStyle(ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public String getPolygonID(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonID(ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public LineCap getPolygonLineCap(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonLineCap(ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public Boolean getPolygonLocked(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonLocked(ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public Color getPolygonStrokeColor(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonStrokeColor(ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public String getPolygonStrokeDashArray(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonStrokeDashArray(ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public Double getPolygonStrokeWidth(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonStrokeWidth(ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public String getPolygonText(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonText(ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public NonNegativeInteger getPolygonTheC(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonTheC(ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public NonNegativeInteger getPolygonTheT(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonTheT(ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public NonNegativeInteger getPolygonTheZ(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonTheZ(ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public AffineTransform getPolygonTransform(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonTransform(ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public Boolean getPolygonVisible(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonVisible(ROIIndex, shapeIndex);
	}

	public String getPolygonPoints(int ROIIndex, int shapeIndex)
	{
    return meta.getPolygonPoints(ROIIndex, shapeIndex);
	}

	//
	// Polyline property storage
	//
	// Indexes: {u'Shape': [u'int ROIIndex', u'int shapeIndex']}
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public Color getPolylineFillColor(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineFillColor(ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public FillRule getPolylineFillRule(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineFillRule(ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public FontFamily getPolylineFontFamily(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineFontFamily(ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public NonNegativeInteger getPolylineFontSize(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineFontSize(ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public FontStyle getPolylineFontStyle(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineFontStyle(ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public String getPolylineID(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineID(ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public LineCap getPolylineLineCap(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineLineCap(ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public Boolean getPolylineLocked(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineLocked(ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public Color getPolylineStrokeColor(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineStrokeColor(ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public String getPolylineStrokeDashArray(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineStrokeDashArray(ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public Double getPolylineStrokeWidth(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineStrokeWidth(ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public String getPolylineText(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineText(ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public NonNegativeInteger getPolylineTheC(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineTheC(ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public NonNegativeInteger getPolylineTheT(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineTheT(ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public NonNegativeInteger getPolylineTheZ(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineTheZ(ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public AffineTransform getPolylineTransform(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineTransform(ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public Boolean getPolylineVisible(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineVisible(ROIIndex, shapeIndex);
	}

	public Marker getPolylineMarkerEnd(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineMarkerEnd(ROIIndex, shapeIndex);
	}

	public Marker getPolylineMarkerStart(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylineMarkerStart(ROIIndex, shapeIndex);
	}

	public String getPolylinePoints(int ROIIndex, int shapeIndex)
	{
    return meta.getPolylinePoints(ROIIndex, shapeIndex);
	}

	//
	// Project property storage
	//
	// Indexes: {u'OME': [u'int projectIndex']}
	// {u'OME': None}
	// Is multi path? False

	public String getProjectAnnotationRef(int projectIndex, int annotationRefIndex)
	{
    return meta.getProjectAnnotationRef(projectIndex, annotationRefIndex);
	}

	public String getProjectDatasetRef(int projectIndex, int datasetRefIndex)
	{
    return meta.getProjectDatasetRef(projectIndex, datasetRefIndex);
	}

	public String getProjectDescription(int projectIndex)
	{
    return meta.getProjectDescription(projectIndex);
	}

	public String getProjectExperimenterGroupRef(int projectIndex)
	{
    return meta.getProjectExperimenterGroupRef(projectIndex);
	}

	public String getProjectExperimenterRef(int projectIndex)
	{
    return meta.getProjectExperimenterRef(projectIndex);
	}

	public String getProjectID(int projectIndex)
	{
    return meta.getProjectID(projectIndex);
	}

	public String getProjectName(int projectIndex)
	{
    return meta.getProjectName(projectIndex);
	}

	//
	// Pump property storage
	//
	// Indexes: {u'Laser': [u'int instrumentIndex', u'int lightSourceIndex']}
	// {u'Laser': {u'LightSource': {u'Instrument': {u'OME': None}}}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference Pump

	// 0:9999
	// Is multi path? False
	// Ignoring Laser_BackReference property of reference Pump

	//
	// ROI property storage
	//
	// Indexes: {u'OME': [u'int ROIIndex']}
	// {u'OME': None}
	// Is multi path? False

	public String getROIAnnotationRef(int ROIIndex, int annotationRefIndex)
	{
    return meta.getROIAnnotationRef(ROIIndex, annotationRefIndex);
	}

	public String getROIDescription(int ROIIndex)
	{
    return meta.getROIDescription(ROIIndex);
	}

	public String getROIID(int ROIIndex)
	{
    return meta.getROIID(ROIIndex);
	}

	// Ignoring Image_BackReference back reference
	// Ignoring MicrobeamManipulation_BackReference back reference
	public String getROIName(int ROIIndex)
	{
    return meta.getROIName(ROIIndex);
	}

	public String getROINamespace(int ROIIndex)
	{
    return meta.getROINamespace(ROIIndex);
	}

	// Ignoring Union element, complex property
	//
	// ROIRef property storage
	//
	// Indexes: {u'Image': [u'int imageIndex', u'int ROIRefIndex'], u'MicrobeamManipulation': [u'int experimentIndex', u'int microbeamManipulationIndex', u'int ROIRefIndex']}
	// {u'Image': {u'OME': None}, u'MicrobeamManipulation': {u'Experiment': {u'OME': None}}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference ROIRef

	//
	// Reagent property storage
	//
	// Indexes: {u'Screen': [u'int screenIndex', u'int reagentIndex']}
	// {u'Screen': {u'OME': None}}
	// Is multi path? False

	public String getReagentAnnotationRef(int screenIndex, int reagentIndex, int annotationRefIndex)
	{
    return meta.getReagentAnnotationRef(screenIndex, reagentIndex, annotationRefIndex);
	}

	public String getReagentDescription(int screenIndex, int reagentIndex)
	{
    return meta.getReagentDescription(screenIndex, reagentIndex);
	}

	public String getReagentID(int screenIndex, int reagentIndex)
	{
    return meta.getReagentID(screenIndex, reagentIndex);
	}

	public String getReagentName(int screenIndex, int reagentIndex)
	{
    return meta.getReagentName(screenIndex, reagentIndex);
	}

	public String getReagentReagentIdentifier(int screenIndex, int reagentIndex)
	{
    return meta.getReagentReagentIdentifier(screenIndex, reagentIndex);
	}

	// Ignoring Screen_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// ReagentRef property storage
	//
	// Indexes: {u'Well': [u'int plateIndex', u'int wellIndex']}
	// {u'Well': {u'Plate': {u'OME': None}}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference ReagentRef

	//
	// Rectangle property storage
	//
	// Indexes: {u'Shape': [u'int ROIIndex', u'int shapeIndex']}
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public Color getRectangleFillColor(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleFillColor(ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public FillRule getRectangleFillRule(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleFillRule(ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public FontFamily getRectangleFontFamily(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleFontFamily(ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public NonNegativeInteger getRectangleFontSize(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleFontSize(ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public FontStyle getRectangleFontStyle(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleFontStyle(ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public String getRectangleID(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleID(ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public LineCap getRectangleLineCap(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleLineCap(ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public Boolean getRectangleLocked(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleLocked(ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public Color getRectangleStrokeColor(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleStrokeColor(ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public String getRectangleStrokeDashArray(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleStrokeDashArray(ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public Double getRectangleStrokeWidth(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleStrokeWidth(ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public String getRectangleText(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleText(ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public NonNegativeInteger getRectangleTheC(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleTheC(ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public NonNegativeInteger getRectangleTheT(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleTheT(ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public NonNegativeInteger getRectangleTheZ(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleTheZ(ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public AffineTransform getRectangleTransform(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleTransform(ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public Boolean getRectangleVisible(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleVisible(ROIIndex, shapeIndex);
	}

	public Double getRectangleHeight(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleHeight(ROIIndex, shapeIndex);
	}

	public Double getRectangleWidth(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleWidth(ROIIndex, shapeIndex);
	}

	public Double getRectangleX(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleX(ROIIndex, shapeIndex);
	}

	public Double getRectangleY(int ROIIndex, int shapeIndex)
	{
    return meta.getRectangleY(ROIIndex, shapeIndex);
	}

	//
	// Screen property storage
	//
	// Indexes: {u'OME': [u'int screenIndex']}
	// {u'OME': None}
	// Is multi path? False

	public String getScreenAnnotationRef(int screenIndex, int annotationRefIndex)
	{
    return meta.getScreenAnnotationRef(screenIndex, annotationRefIndex);
	}

	public String getScreenDescription(int screenIndex)
	{
    return meta.getScreenDescription(screenIndex);
	}

	public String getScreenID(int screenIndex)
	{
    return meta.getScreenID(screenIndex);
	}

	public String getScreenName(int screenIndex)
	{
    return meta.getScreenName(screenIndex);
	}

	public String getScreenPlateRef(int screenIndex, int plateRefIndex)
	{
    return meta.getScreenPlateRef(screenIndex, plateRefIndex);
	}

	public String getScreenProtocolDescription(int screenIndex)
	{
    return meta.getScreenProtocolDescription(screenIndex);
	}

	public String getScreenProtocolIdentifier(int screenIndex)
	{
    return meta.getScreenProtocolIdentifier(screenIndex);
	}

	// Ignoring Reagent element, complex property
	public String getScreenReagentSetDescription(int screenIndex)
	{
    return meta.getScreenReagentSetDescription(screenIndex);
	}

	public String getScreenReagentSetIdentifier(int screenIndex)
	{
    return meta.getScreenReagentSetIdentifier(screenIndex);
	}

	public String getScreenType(int screenIndex)
	{
    return meta.getScreenType(screenIndex);
	}

	//
	// StageLabel property storage
	//
	// Indexes: {u'Image': [u'int imageIndex']}
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	public String getStageLabelName(int imageIndex)
	{
    return meta.getStageLabelName(imageIndex);
	}

	public Double getStageLabelX(int imageIndex)
	{
    return meta.getStageLabelX(imageIndex);
	}

	public Double getStageLabelY(int imageIndex)
	{
    return meta.getStageLabelY(imageIndex);
	}

	public Double getStageLabelZ(int imageIndex)
	{
    return meta.getStageLabelZ(imageIndex);
	}

	//
	// StructuredAnnotations property storage
	//
	// Indexes: {u'OME': []}
	// {u'OME': None}
	// Is multi path? False

	// Ignoring BooleanAnnotation element, complex property
	// Ignoring CommentAnnotation element, complex property
	// Ignoring DoubleAnnotation element, complex property
	// Ignoring FileAnnotation element, complex property
	// Ignoring ListAnnotation element, complex property
	// Ignoring LongAnnotation element, complex property
	// Ignoring TagAnnotation element, complex property
	// Ignoring TermAnnotation element, complex property
	// Ignoring TimestampAnnotation element, complex property
	// Ignoring XMLAnnotation element, complex property
	//
	// TagAnnotation property storage
	//
	// Indexes: {u'StructuredAnnotations': [u'int tagAnnotationIndex']}
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public String getTagAnnotationAnnotationRef(int tagAnnotationIndex, int annotationRefIndex)
	{
    return meta.getTagAnnotationAnnotationRef(tagAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public String getTagAnnotationDescription(int tagAnnotationIndex)
	{
    return meta.getTagAnnotationDescription(tagAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public String getTagAnnotationID(int tagAnnotationIndex)
	{
    return meta.getTagAnnotationID(tagAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getTagAnnotationNamespace(int tagAnnotationIndex)
	{
    return meta.getTagAnnotationNamespace(tagAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public String getTagAnnotationValue(int tagAnnotationIndex)
	{
    return meta.getTagAnnotationValue(tagAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// TermAnnotation property storage
	//
	// Indexes: {u'StructuredAnnotations': [u'int termAnnotationIndex']}
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public String getTermAnnotationAnnotationRef(int termAnnotationIndex, int annotationRefIndex)
	{
    return meta.getTermAnnotationAnnotationRef(termAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public String getTermAnnotationDescription(int termAnnotationIndex)
	{
    return meta.getTermAnnotationDescription(termAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public String getTermAnnotationID(int termAnnotationIndex)
	{
    return meta.getTermAnnotationID(termAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getTermAnnotationNamespace(int termAnnotationIndex)
	{
    return meta.getTermAnnotationNamespace(termAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public String getTermAnnotationValue(int termAnnotationIndex)
	{
    return meta.getTermAnnotationValue(termAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// TiffData property storage
	//
	// Indexes: {u'Pixels': [u'int imageIndex', u'int tiffDataIndex']}
	// {u'Pixels': {u'Image': {u'OME': None}}}
	// Is multi path? False

	public NonNegativeInteger getTiffDataFirstC(int imageIndex, int tiffDataIndex)
	{
    return meta.getTiffDataFirstC(imageIndex, tiffDataIndex);
	}

	public NonNegativeInteger getTiffDataFirstT(int imageIndex, int tiffDataIndex)
	{
    return meta.getTiffDataFirstT(imageIndex, tiffDataIndex);
	}

	public NonNegativeInteger getTiffDataFirstZ(int imageIndex, int tiffDataIndex)
	{
    return meta.getTiffDataFirstZ(imageIndex, tiffDataIndex);
	}

	public NonNegativeInteger getTiffDataIFD(int imageIndex, int tiffDataIndex)
	{
    return meta.getTiffDataIFD(imageIndex, tiffDataIndex);
	}

	// Ignoring Pixels_BackReference back reference
	public NonNegativeInteger getTiffDataPlaneCount(int imageIndex, int tiffDataIndex)
	{
    return meta.getTiffDataPlaneCount(imageIndex, tiffDataIndex);
	}

	// Ignoring UUID element, complex property
	//
	// TimestampAnnotation property storage
	//
	// Indexes: {u'StructuredAnnotations': [u'int timestampAnnotationIndex']}
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public String getTimestampAnnotationAnnotationRef(int timestampAnnotationIndex, int annotationRefIndex)
	{
    return meta.getTimestampAnnotationAnnotationRef(timestampAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public String getTimestampAnnotationDescription(int timestampAnnotationIndex)
	{
    return meta.getTimestampAnnotationDescription(timestampAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public String getTimestampAnnotationID(int timestampAnnotationIndex)
	{
    return meta.getTimestampAnnotationID(timestampAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getTimestampAnnotationNamespace(int timestampAnnotationIndex)
	{
    return meta.getTimestampAnnotationNamespace(timestampAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public Timestamp getTimestampAnnotationValue(int timestampAnnotationIndex)
	{
    return meta.getTimestampAnnotationValue(timestampAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// TransmittanceRange property storage
	//
	// Indexes: {u'Filter': [u'int instrumentIndex', u'int filterIndex']}
	// {u'Filter': {u'Instrument': {u'OME': None}}}
	// Is multi path? False

	public PositiveInteger getTransmittanceRangeCutIn(int instrumentIndex, int filterIndex)
	{
    return meta.getTransmittanceRangeCutIn(instrumentIndex, filterIndex);
	}

	public NonNegativeInteger getTransmittanceRangeCutInTolerance(int instrumentIndex, int filterIndex)
	{
    return meta.getTransmittanceRangeCutInTolerance(instrumentIndex, filterIndex);
	}

	public PositiveInteger getTransmittanceRangeCutOut(int instrumentIndex, int filterIndex)
	{
    return meta.getTransmittanceRangeCutOut(instrumentIndex, filterIndex);
	}

	public NonNegativeInteger getTransmittanceRangeCutOutTolerance(int instrumentIndex, int filterIndex)
	{
    return meta.getTransmittanceRangeCutOutTolerance(instrumentIndex, filterIndex);
	}

	public PercentFraction getTransmittanceRangeTransmittance(int instrumentIndex, int filterIndex)
	{
    return meta.getTransmittanceRangeTransmittance(instrumentIndex, filterIndex);
	}

	//
	// UUID property storage
	//
	// Indexes: {u'TiffData': [u'int imageIndex', u'int tiffDataIndex']}
	// {u'TiffData': {u'Pixels': {u'Image': {u'OME': None}}}}
	// Is multi path? False

	public String getUUIDFileName(int imageIndex, int tiffDataIndex)
	{
    return meta.getUUIDFileName(imageIndex, tiffDataIndex);
	}

	//
	// Union property storage
	//
	// Indexes: {u'ROI': [u'int ROIIndex']}
	// {u'ROI': {u'OME': None}}
	// Is multi path? False

	// Ignoring Shape element, complex property
	//
	// Well property storage
	//
	// Indexes: {u'Plate': [u'int plateIndex', u'int wellIndex']}
	// {u'Plate': {u'OME': None}}
	// Is multi path? False

	public String getWellAnnotationRef(int plateIndex, int wellIndex, int annotationRefIndex)
	{
    return meta.getWellAnnotationRef(plateIndex, wellIndex, annotationRefIndex);
	}

	public Color getWellColor(int plateIndex, int wellIndex)
	{
    return meta.getWellColor(plateIndex, wellIndex);
	}

	public NonNegativeInteger getWellColumn(int plateIndex, int wellIndex)
	{
    return meta.getWellColumn(plateIndex, wellIndex);
	}

	public String getWellExternalDescription(int plateIndex, int wellIndex)
	{
    return meta.getWellExternalDescription(plateIndex, wellIndex);
	}

	public String getWellExternalIdentifier(int plateIndex, int wellIndex)
	{
    return meta.getWellExternalIdentifier(plateIndex, wellIndex);
	}

	public String getWellID(int plateIndex, int wellIndex)
	{
    return meta.getWellID(plateIndex, wellIndex);
	}

	// Ignoring Plate_BackReference back reference
	public String getWellReagentRef(int plateIndex, int wellIndex)
	{
    return meta.getWellReagentRef(plateIndex, wellIndex);
	}

	public NonNegativeInteger getWellRow(int plateIndex, int wellIndex)
	{
    return meta.getWellRow(plateIndex, wellIndex);
	}

	public String getWellType(int plateIndex, int wellIndex)
	{
    return meta.getWellType(plateIndex, wellIndex);
	}

	// Ignoring WellSample element, complex property
	//
	// WellSample property storage
	//
	// Indexes: {u'Well': [u'int plateIndex', u'int wellIndex', u'int wellSampleIndex']}
	// {u'Well': {u'Plate': {u'OME': None}}}
	// Is multi path? False

	public String getWellSampleAnnotationRef(int plateIndex, int wellIndex, int wellSampleIndex, int annotationRefIndex)
	{
    return meta.getWellSampleAnnotationRef(plateIndex, wellIndex, wellSampleIndex, annotationRefIndex);
	}

	public String getWellSampleID(int plateIndex, int wellIndex, int wellSampleIndex)
	{
    return meta.getWellSampleID(plateIndex, wellIndex, wellSampleIndex);
	}

	public String getWellSampleImageRef(int plateIndex, int wellIndex, int wellSampleIndex)
	{
    return meta.getWellSampleImageRef(plateIndex, wellIndex, wellSampleIndex);
	}

	public NonNegativeInteger getWellSampleIndex(int plateIndex, int wellIndex, int wellSampleIndex)
	{
    return meta.getWellSampleIndex(plateIndex, wellIndex, wellSampleIndex);
	}

	// Ignoring PlateAcquisition_BackReference back reference
	public Double getWellSamplePositionX(int plateIndex, int wellIndex, int wellSampleIndex)
	{
    return meta.getWellSamplePositionX(plateIndex, wellIndex, wellSampleIndex);
	}

	public Double getWellSamplePositionY(int plateIndex, int wellIndex, int wellSampleIndex)
	{
    return meta.getWellSamplePositionY(plateIndex, wellIndex, wellSampleIndex);
	}

	public Timestamp getWellSampleTimepoint(int plateIndex, int wellIndex, int wellSampleIndex)
	{
    return meta.getWellSampleTimepoint(plateIndex, wellIndex, wellSampleIndex);
	}

	// Ignoring Well_BackReference back reference
	//
	// WellSampleRef property storage
	//
	// Indexes: {u'PlateAcquisition': [u'int plateIndex', u'int plateAcquisitionIndex', u'int wellSampleRefIndex']}
	// {u'PlateAcquisition': {u'Plate': {u'OME': None}}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference WellSampleRef

	//
	// XMLAnnotation property storage
	//
	// Indexes: {u'StructuredAnnotations': [u'int XMLAnnotationIndex']}
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public String getXMLAnnotationAnnotationRef(int XMLAnnotationIndex, int annotationRefIndex)
	{
    return meta.getXMLAnnotationAnnotationRef(XMLAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public String getXMLAnnotationDescription(int XMLAnnotationIndex)
	{
    return meta.getXMLAnnotationDescription(XMLAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public String getXMLAnnotationID(int XMLAnnotationIndex)
	{
    return meta.getXMLAnnotationID(XMLAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public String getXMLAnnotationNamespace(int XMLAnnotationIndex)
	{
    return meta.getXMLAnnotationNamespace(XMLAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public String getXMLAnnotationValue(int XMLAnnotationIndex)
	{
    return meta.getXMLAnnotationValue(XMLAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference

	// -- Entity storage (manual definitions) --

	public void setPixelsBinDataBigEndian(Boolean bigEndian, int imageIndex, int binDataIndex)
	{
    meta.setPixelsBinDataBigEndian(bigEndian, imageIndex, binDataIndex);
	}

	public void setMaskBinData(byte[] binData, int ROIIndex, int shapeIndex)
	{
    meta.setMaskBinData(binData, ROIIndex, shapeIndex);
	}

	// -- Entity storage (code generated definitions) --

	/** Sets the UUID associated with this collection of metadata. */
	public void setUUID(String uuid)
	{
    meta.setUUID(uuid);
	}

	//
	// AnnotationRef property storage
	//
	// {u'ROI': {u'OME': None}, u'PlateAcquisition': {u'Plate': {u'OME': None}}, u'Plate': {u'OME': None}, u'ExperimenterGroup': {u'OME': None}, u'Image': {u'OME': None}, u'Screen': {u'OME': None}, u'Well': {u'Plate': {u'OME': None}}, u'Dataset': {u'OME': None}, u'Project': {u'OME': None}, u'Reagent': {u'Screen': {u'OME': None}}, u'Plane': {u'Pixels': {u'Image': {u'OME': None}}}, u'Experimenter': {u'OME': None}, u'Annotation': None, u'WellSample': {u'Well': {u'Plate': {u'OME': None}}}, u'Pixels': {u'Image': {u'OME': None}}, u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference AnnotationRef

	//
	// Arc property storage
	//
	// {u'LightSource': {u'Instrument': {u'OME': None}}}
	// Is multi path? False

	// Ignoring Arc of parent abstract type
	// Ignoring Filament of parent abstract type
	// ID accessor from parent LightSource
	public void setArcID(String id, int instrumentIndex, int lightSourceIndex)
	{
    meta.setArcID(id, instrumentIndex, lightSourceIndex);
	}

	// Instrument_BackReference accessor from parent LightSource
	public void setArcInstrument_BackReference(String instrument_BackReference, int instrumentIndex, int lightSourceIndex)
	{
    meta.setArcInstrument_BackReference(instrument_BackReference, instrumentIndex, lightSourceIndex);
	}

	// Ignoring Laser of parent abstract type
	// Ignoring LightEmittingDiode of parent abstract type
	// LotNumber accessor from parent LightSource
	public void setArcLotNumber(String lotNumber, int instrumentIndex, int lightSourceIndex)
	{
    meta.setArcLotNumber(lotNumber, instrumentIndex, lightSourceIndex);
	}

	// Manufacturer accessor from parent LightSource
	public void setArcManufacturer(String manufacturer, int instrumentIndex, int lightSourceIndex)
	{
    meta.setArcManufacturer(manufacturer, instrumentIndex, lightSourceIndex);
	}

	// Model accessor from parent LightSource
	public void setArcModel(String model, int instrumentIndex, int lightSourceIndex)
	{
    meta.setArcModel(model, instrumentIndex, lightSourceIndex);
	}

	// Power accessor from parent LightSource
	public void setArcPower(Double power, int instrumentIndex, int lightSourceIndex)
	{
    meta.setArcPower(power, instrumentIndex, lightSourceIndex);
	}

	// SerialNumber accessor from parent LightSource
	public void setArcSerialNumber(String serialNumber, int instrumentIndex, int lightSourceIndex)
	{
    meta.setArcSerialNumber(serialNumber, instrumentIndex, lightSourceIndex);
	}

	public void setArcType(ArcType type, int instrumentIndex, int lightSourceIndex)
	{
    meta.setArcType(type, instrumentIndex, lightSourceIndex);
	}

	//
	// BinaryFile property storage
	//
	// {u'FileAnnotation': {u'StructuredAnnotations': {u'OME': None}}}
	// Is multi path? False

	// Ignoring BinData element, complex property
	// Ignoring External element, complex property
	public void setBinaryFileFileName(String fileName, int fileAnnotationIndex)
	{
    meta.setBinaryFileFileName(fileName, fileAnnotationIndex);
	}

	public void setBinaryFileMIMEType(String mimeType, int fileAnnotationIndex)
	{
    meta.setBinaryFileMIMEType(mimeType, fileAnnotationIndex);
	}

	public void setBinaryFileSize(NonNegativeLong size, int fileAnnotationIndex)
	{
    meta.setBinaryFileSize(size, fileAnnotationIndex);
	}

	//
	// BinaryOnly property storage
	//
	// {u'OME': None}
	// Is multi path? False

	public void setBinaryOnlyMetadataFile(String metadataFile)
	{
    meta.setBinaryOnlyMetadataFile(metadataFile);
	}

	public void setBinaryOnlyUUID(String uuid)
	{
    meta.setBinaryOnlyUUID(uuid);
	}

	//
	// BooleanAnnotation property storage
	//
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public void setBooleanAnnotationAnnotationRef(String annotation, int booleanAnnotationIndex, int annotationRefIndex)
	{
    meta.setBooleanAnnotationAnnotationRef(annotation, booleanAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public void setBooleanAnnotationDescription(String description, int booleanAnnotationIndex)
	{
    meta.setBooleanAnnotationDescription(description, booleanAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public void setBooleanAnnotationID(String id, int booleanAnnotationIndex)
	{
    meta.setBooleanAnnotationID(id, booleanAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setBooleanAnnotationNamespace(String namespace, int booleanAnnotationIndex)
	{
    meta.setBooleanAnnotationNamespace(namespace, booleanAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public void setBooleanAnnotationValue(Boolean value, int booleanAnnotationIndex)
	{
    meta.setBooleanAnnotationValue(value, booleanAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// Channel property storage
	//
	// {u'Pixels': {u'Image': {u'OME': None}}}
	// Is multi path? False

	public void setChannelAcquisitionMode(AcquisitionMode acquisitionMode, int imageIndex, int channelIndex)
	{
    meta.setChannelAcquisitionMode(acquisitionMode, imageIndex, channelIndex);
	}

	public void setChannelAnnotationRef(String annotation, int imageIndex, int channelIndex, int annotationRefIndex)
	{
    meta.setChannelAnnotationRef(annotation, imageIndex, channelIndex, annotationRefIndex);
	}

	public void setChannelColor(Color color, int imageIndex, int channelIndex)
	{
    meta.setChannelColor(color, imageIndex, channelIndex);
	}

	public void setChannelContrastMethod(ContrastMethod contrastMethod, int imageIndex, int channelIndex)
	{
    meta.setChannelContrastMethod(contrastMethod, imageIndex, channelIndex);
	}

	// Ignoring DetectorSettings element, complex property
	public void setChannelEmissionWavelength(PositiveInteger emissionWavelength, int imageIndex, int channelIndex)
	{
    meta.setChannelEmissionWavelength(emissionWavelength, imageIndex, channelIndex);
	}

	public void setChannelExcitationWavelength(PositiveInteger excitationWavelength, int imageIndex, int channelIndex)
	{
    meta.setChannelExcitationWavelength(excitationWavelength, imageIndex, channelIndex);
	}

	public void setChannelFilterSetRef(String filterSet, int imageIndex, int channelIndex)
	{
    meta.setChannelFilterSetRef(filterSet, imageIndex, channelIndex);
	}

	public void setChannelFluor(String fluor, int imageIndex, int channelIndex)
	{
    meta.setChannelFluor(fluor, imageIndex, channelIndex);
	}

	public void setChannelID(String id, int imageIndex, int channelIndex)
	{
    meta.setChannelID(id, imageIndex, channelIndex);
	}

	public void setChannelIlluminationType(IlluminationType illuminationType, int imageIndex, int channelIndex)
	{
    meta.setChannelIlluminationType(illuminationType, imageIndex, channelIndex);
	}

	// Ignoring LightPath element, complex property
	// Ignoring LightSourceSettings element, complex property
	public void setChannelNDFilter(Double ndFilter, int imageIndex, int channelIndex)
	{
    meta.setChannelNDFilter(ndFilter, imageIndex, channelIndex);
	}

	public void setChannelName(String name, int imageIndex, int channelIndex)
	{
    meta.setChannelName(name, imageIndex, channelIndex);
	}

	public void setChannelPinholeSize(Double pinholeSize, int imageIndex, int channelIndex)
	{
    meta.setChannelPinholeSize(pinholeSize, imageIndex, channelIndex);
	}

	// Ignoring Pixels_BackReference back reference
	public void setChannelPockelCellSetting(Integer pockelCellSetting, int imageIndex, int channelIndex)
	{
    meta.setChannelPockelCellSetting(pockelCellSetting, imageIndex, channelIndex);
	}

	public void setChannelSamplesPerPixel(PositiveInteger samplesPerPixel, int imageIndex, int channelIndex)
	{
    meta.setChannelSamplesPerPixel(samplesPerPixel, imageIndex, channelIndex);
	}

	//
	// CommentAnnotation property storage
	//
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public void setCommentAnnotationAnnotationRef(String annotation, int commentAnnotationIndex, int annotationRefIndex)
	{
    meta.setCommentAnnotationAnnotationRef(annotation, commentAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public void setCommentAnnotationDescription(String description, int commentAnnotationIndex)
	{
    meta.setCommentAnnotationDescription(description, commentAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public void setCommentAnnotationID(String id, int commentAnnotationIndex)
	{
    meta.setCommentAnnotationID(id, commentAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setCommentAnnotationNamespace(String namespace, int commentAnnotationIndex)
	{
    meta.setCommentAnnotationNamespace(namespace, commentAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public void setCommentAnnotationValue(String value, int commentAnnotationIndex)
	{
    meta.setCommentAnnotationValue(value, commentAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// Dataset property storage
	//
	// {u'OME': None}
	// Is multi path? False

	public void setDatasetAnnotationRef(String annotation, int datasetIndex, int annotationRefIndex)
	{
    meta.setDatasetAnnotationRef(annotation, datasetIndex, annotationRefIndex);
	}

	public void setDatasetDescription(String description, int datasetIndex)
	{
    meta.setDatasetDescription(description, datasetIndex);
	}

	public void setDatasetExperimenterGroupRef(String experimenterGroup, int datasetIndex)
	{
    meta.setDatasetExperimenterGroupRef(experimenterGroup, datasetIndex);
	}

	public void setDatasetExperimenterRef(String experimenter, int datasetIndex)
	{
    meta.setDatasetExperimenterRef(experimenter, datasetIndex);
	}

	public void setDatasetID(String id, int datasetIndex)
	{
    meta.setDatasetID(id, datasetIndex);
	}

	public void setDatasetImageRef(String image, int datasetIndex, int imageRefIndex)
	{
    meta.setDatasetImageRef(image, datasetIndex, imageRefIndex);
	}

	public void setDatasetName(String name, int datasetIndex)
	{
    meta.setDatasetName(name, datasetIndex);
	}

	// Ignoring Project_BackReference back reference
	//
	// DatasetRef property storage
	//
	// {u'Project': {u'OME': None}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference DatasetRef

	//
	// Detector property storage
	//
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	public void setDetectorAmplificationGain(Double amplificationGain, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorAmplificationGain(amplificationGain, instrumentIndex, detectorIndex);
	}

	public void setDetectorGain(Double gain, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorGain(gain, instrumentIndex, detectorIndex);
	}

	public void setDetectorID(String id, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorID(id, instrumentIndex, detectorIndex);
	}

	// Ignoring Instrument_BackReference back reference
	public void setDetectorLotNumber(String lotNumber, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorLotNumber(lotNumber, instrumentIndex, detectorIndex);
	}

	public void setDetectorManufacturer(String manufacturer, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorManufacturer(manufacturer, instrumentIndex, detectorIndex);
	}

	public void setDetectorModel(String model, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorModel(model, instrumentIndex, detectorIndex);
	}

	public void setDetectorOffset(Double offset, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorOffset(offset, instrumentIndex, detectorIndex);
	}

	public void setDetectorSerialNumber(String serialNumber, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorSerialNumber(serialNumber, instrumentIndex, detectorIndex);
	}

	public void setDetectorType(DetectorType type, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorType(type, instrumentIndex, detectorIndex);
	}

	public void setDetectorVoltage(Double voltage, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorVoltage(voltage, instrumentIndex, detectorIndex);
	}

	public void setDetectorZoom(Double zoom, int instrumentIndex, int detectorIndex)
	{
    meta.setDetectorZoom(zoom, instrumentIndex, detectorIndex);
	}

	//
	// DetectorSettings property storage
	//
	// {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}
	// Is multi path? False

	public void setDetectorSettingsBinning(Binning binning, int imageIndex, int channelIndex)
	{
    meta.setDetectorSettingsBinning(binning, imageIndex, channelIndex);
	}

	// Ignoring DetectorRef back reference
	public void setDetectorSettingsGain(Double gain, int imageIndex, int channelIndex)
	{
    meta.setDetectorSettingsGain(gain, imageIndex, channelIndex);
	}

	public void setDetectorSettingsID(String id, int imageIndex, int channelIndex)
	{
    meta.setDetectorSettingsID(id, imageIndex, channelIndex);
	}

	public void setDetectorSettingsOffset(Double offset, int imageIndex, int channelIndex)
	{
    meta.setDetectorSettingsOffset(offset, imageIndex, channelIndex);
	}

	public void setDetectorSettingsReadOutRate(Double readOutRate, int imageIndex, int channelIndex)
	{
    meta.setDetectorSettingsReadOutRate(readOutRate, imageIndex, channelIndex);
	}

	public void setDetectorSettingsVoltage(Double voltage, int imageIndex, int channelIndex)
	{
    meta.setDetectorSettingsVoltage(voltage, imageIndex, channelIndex);
	}

	//
	// Dichroic property storage
	//
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	// Ignoring FilterSet_BackReference back reference
	public void setDichroicID(String id, int instrumentIndex, int dichroicIndex)
	{
    meta.setDichroicID(id, instrumentIndex, dichroicIndex);
	}

	// Ignoring Instrument_BackReference back reference
	// Ignoring LightPath_BackReference back reference
	public void setDichroicLotNumber(String lotNumber, int instrumentIndex, int dichroicIndex)
	{
    meta.setDichroicLotNumber(lotNumber, instrumentIndex, dichroicIndex);
	}

	public void setDichroicManufacturer(String manufacturer, int instrumentIndex, int dichroicIndex)
	{
    meta.setDichroicManufacturer(manufacturer, instrumentIndex, dichroicIndex);
	}

	public void setDichroicModel(String model, int instrumentIndex, int dichroicIndex)
	{
    meta.setDichroicModel(model, instrumentIndex, dichroicIndex);
	}

	public void setDichroicSerialNumber(String serialNumber, int instrumentIndex, int dichroicIndex)
	{
    meta.setDichroicSerialNumber(serialNumber, instrumentIndex, dichroicIndex);
	}

	//
	// DichroicRef property storage
	//
	// {u'LightPath': {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}, u'FilterSet': {u'Instrument': {u'OME': None}}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference DichroicRef

	//
	// DoubleAnnotation property storage
	//
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public void setDoubleAnnotationAnnotationRef(String annotation, int doubleAnnotationIndex, int annotationRefIndex)
	{
    meta.setDoubleAnnotationAnnotationRef(annotation, doubleAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public void setDoubleAnnotationDescription(String description, int doubleAnnotationIndex)
	{
    meta.setDoubleAnnotationDescription(description, doubleAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public void setDoubleAnnotationID(String id, int doubleAnnotationIndex)
	{
    meta.setDoubleAnnotationID(id, doubleAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setDoubleAnnotationNamespace(String namespace, int doubleAnnotationIndex)
	{
    meta.setDoubleAnnotationNamespace(namespace, doubleAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public void setDoubleAnnotationValue(Double value, int doubleAnnotationIndex)
	{
    meta.setDoubleAnnotationValue(value, doubleAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// Ellipse property storage
	//
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public void setEllipseFillColor(Color fillColor, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseFillColor(fillColor, ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public void setEllipseFillRule(FillRule fillRule, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseFillRule(fillRule, ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public void setEllipseFontFamily(FontFamily fontFamily, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseFontFamily(fontFamily, ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public void setEllipseFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseFontSize(fontSize, ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public void setEllipseFontStyle(FontStyle fontStyle, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseFontStyle(fontStyle, ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public void setEllipseID(String id, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseID(id, ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public void setEllipseLineCap(LineCap lineCap, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseLineCap(lineCap, ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public void setEllipseLocked(Boolean locked, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseLocked(locked, ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public void setEllipseStrokeColor(Color strokeColor, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseStrokeColor(strokeColor, ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public void setEllipseStrokeDashArray(String strokeDashArray, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseStrokeDashArray(strokeDashArray, ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public void setEllipseStrokeWidth(Double strokeWidth, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseStrokeWidth(strokeWidth, ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public void setEllipseText(String text, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseText(text, ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public void setEllipseTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseTheC(theC, ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public void setEllipseTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseTheT(theT, ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public void setEllipseTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseTheZ(theZ, ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public void setEllipseTransform(AffineTransform transform, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseTransform(transform, ROIIndex, shapeIndex);
	}

	// Union_BackReference accessor from parent Shape
	public void setEllipseUnion_BackReference(String union_BackReference, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseUnion_BackReference(union_BackReference, ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public void setEllipseVisible(Boolean visible, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseVisible(visible, ROIIndex, shapeIndex);
	}

	public void setEllipseRadiusX(Double radiusX, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseRadiusX(radiusX, ROIIndex, shapeIndex);
	}

	public void setEllipseRadiusY(Double radiusY, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseRadiusY(radiusY, ROIIndex, shapeIndex);
	}

	public void setEllipseX(Double x, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseX(x, ROIIndex, shapeIndex);
	}

	public void setEllipseY(Double y, int ROIIndex, int shapeIndex)
	{
    meta.setEllipseY(y, ROIIndex, shapeIndex);
	}

	//
	// EmissionFilterRef property storage
	//
	// {u'LightPath': {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}, u'FilterSet': {u'Instrument': {u'OME': None}}}
	// Is multi path? True

	//
	// ExcitationFilterRef property storage
	//
	// {u'LightPath': {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}, u'FilterSet': {u'Instrument': {u'OME': None}}}
	// Is multi path? True

	//
	// Experiment property storage
	//
	// {u'OME': None}
	// Is multi path? False

	public void setExperimentDescription(String description, int experimentIndex)
	{
    meta.setExperimentDescription(description, experimentIndex);
	}

	public void setExperimentExperimenterRef(String experimenter, int experimentIndex)
	{
    meta.setExperimentExperimenterRef(experimenter, experimentIndex);
	}

	public void setExperimentID(String id, int experimentIndex)
	{
    meta.setExperimentID(id, experimentIndex);
	}

	// Ignoring Image_BackReference back reference
	// Ignoring MicrobeamManipulation element, complex property
	public void setExperimentType(ExperimentType type, int experimentIndex)
	{
    meta.setExperimentType(type, experimentIndex);
	}

	//
	// ExperimentRef property storage
	//
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference ExperimentRef

	//
	// Experimenter property storage
	//
	// {u'OME': None}
	// Is multi path? False

	public void setExperimenterAnnotationRef(String annotation, int experimenterIndex, int annotationRefIndex)
	{
    meta.setExperimenterAnnotationRef(annotation, experimenterIndex, annotationRefIndex);
	}

	// Ignoring Dataset_BackReference back reference
	public void setExperimenterEmail(String email, int experimenterIndex)
	{
    meta.setExperimenterEmail(email, experimenterIndex);
	}

	// Ignoring Experiment_BackReference back reference
	// Ignoring ExperimenterGroup_BackReference back reference
	public void setExperimenterFirstName(String firstName, int experimenterIndex)
	{
    meta.setExperimenterFirstName(firstName, experimenterIndex);
	}

	public void setExperimenterID(String id, int experimenterIndex)
	{
    meta.setExperimenterID(id, experimenterIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setExperimenterInstitution(String institution, int experimenterIndex)
	{
    meta.setExperimenterInstitution(institution, experimenterIndex);
	}

	public void setExperimenterLastName(String lastName, int experimenterIndex)
	{
    meta.setExperimenterLastName(lastName, experimenterIndex);
	}

	// Ignoring MicrobeamManipulation_BackReference back reference
	public void setExperimenterMiddleName(String middleName, int experimenterIndex)
	{
    meta.setExperimenterMiddleName(middleName, experimenterIndex);
	}

	// Ignoring Project_BackReference back reference
	public void setExperimenterUserName(String userName, int experimenterIndex)
	{
    meta.setExperimenterUserName(userName, experimenterIndex);
	}

	//
	// ExperimenterGroup property storage
	//
	// {u'OME': None}
	// Is multi path? False

	public void setExperimenterGroupAnnotationRef(String annotation, int experimenterGroupIndex, int annotationRefIndex)
	{
    meta.setExperimenterGroupAnnotationRef(annotation, experimenterGroupIndex, annotationRefIndex);
	}

	// Ignoring Dataset_BackReference back reference
	public void setExperimenterGroupDescription(String description, int experimenterGroupIndex)
	{
    meta.setExperimenterGroupDescription(description, experimenterGroupIndex);
	}

	public void setExperimenterGroupExperimenterRef(String experimenter, int experimenterGroupIndex, int experimenterRefIndex)
	{
    meta.setExperimenterGroupExperimenterRef(experimenter, experimenterGroupIndex, experimenterRefIndex);
	}

	public void setExperimenterGroupID(String id, int experimenterGroupIndex)
	{
    meta.setExperimenterGroupID(id, experimenterGroupIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setExperimenterGroupLeader(String leader, int experimenterGroupIndex, int leaderIndex)
	{
    meta.setExperimenterGroupLeader(leader, experimenterGroupIndex, leaderIndex);
	}

	public void setExperimenterGroupName(String name, int experimenterGroupIndex)
	{
    meta.setExperimenterGroupName(name, experimenterGroupIndex);
	}

	// Ignoring Project_BackReference back reference
	//
	// ExperimenterGroupRef property storage
	//
	// {u'Project': {u'OME': None}, u'Image': {u'OME': None}, u'Dataset': {u'OME': None}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference ExperimenterGroupRef

	//
	// ExperimenterRef property storage
	//
	// {u'ExperimenterGroup': {u'OME': None}, u'Image': {u'OME': None}, u'Dataset': {u'OME': None}, u'Project': {u'OME': None}, u'Experiment': {u'OME': None}, u'MicrobeamManipulation': {u'Experiment': {u'OME': None}}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference ExperimenterRef

	//
	// Filament property storage
	//
	// {u'LightSource': {u'Instrument': {u'OME': None}}}
	// Is multi path? False

	// Ignoring Arc of parent abstract type
	// Ignoring Filament of parent abstract type
	// ID accessor from parent LightSource
	public void setFilamentID(String id, int instrumentIndex, int lightSourceIndex)
	{
    meta.setFilamentID(id, instrumentIndex, lightSourceIndex);
	}

	// Instrument_BackReference accessor from parent LightSource
	public void setFilamentInstrument_BackReference(String instrument_BackReference, int instrumentIndex, int lightSourceIndex)
	{
    meta.setFilamentInstrument_BackReference(instrument_BackReference, instrumentIndex, lightSourceIndex);
	}

	// Ignoring Laser of parent abstract type
	// Ignoring LightEmittingDiode of parent abstract type
	// LotNumber accessor from parent LightSource
	public void setFilamentLotNumber(String lotNumber, int instrumentIndex, int lightSourceIndex)
	{
    meta.setFilamentLotNumber(lotNumber, instrumentIndex, lightSourceIndex);
	}

	// Manufacturer accessor from parent LightSource
	public void setFilamentManufacturer(String manufacturer, int instrumentIndex, int lightSourceIndex)
	{
    meta.setFilamentManufacturer(manufacturer, instrumentIndex, lightSourceIndex);
	}

	// Model accessor from parent LightSource
	public void setFilamentModel(String model, int instrumentIndex, int lightSourceIndex)
	{
    meta.setFilamentModel(model, instrumentIndex, lightSourceIndex);
	}

	// Power accessor from parent LightSource
	public void setFilamentPower(Double power, int instrumentIndex, int lightSourceIndex)
	{
    meta.setFilamentPower(power, instrumentIndex, lightSourceIndex);
	}

	// SerialNumber accessor from parent LightSource
	public void setFilamentSerialNumber(String serialNumber, int instrumentIndex, int lightSourceIndex)
	{
    meta.setFilamentSerialNumber(serialNumber, instrumentIndex, lightSourceIndex);
	}

	public void setFilamentType(FilamentType type, int instrumentIndex, int lightSourceIndex)
	{
    meta.setFilamentType(type, instrumentIndex, lightSourceIndex);
	}

	//
	// FileAnnotation property storage
	//
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public void setFileAnnotationAnnotationRef(String annotation, int fileAnnotationIndex, int annotationRefIndex)
	{
    meta.setFileAnnotationAnnotationRef(annotation, fileAnnotationIndex, annotationRefIndex);
	}

	// Ignoring BinaryFile element, complex property
	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public void setFileAnnotationDescription(String description, int fileAnnotationIndex)
	{
    meta.setFileAnnotationDescription(description, fileAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public void setFileAnnotationID(String id, int fileAnnotationIndex)
	{
    meta.setFileAnnotationID(id, fileAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setFileAnnotationNamespace(String namespace, int fileAnnotationIndex)
	{
    meta.setFileAnnotationNamespace(namespace, fileAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// Filter property storage
	//
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	// Ignoring FilterSet_BackReference back reference
	// Ignoring FilterSet_BackReference back reference
	public void setFilterFilterWheel(String filterWheel, int instrumentIndex, int filterIndex)
	{
    meta.setFilterFilterWheel(filterWheel, instrumentIndex, filterIndex);
	}

	public void setFilterID(String id, int instrumentIndex, int filterIndex)
	{
    meta.setFilterID(id, instrumentIndex, filterIndex);
	}

	// Ignoring Instrument_BackReference back reference
	// Ignoring LightPath_BackReference back reference
	// Ignoring LightPath_BackReference back reference
	public void setFilterLotNumber(String lotNumber, int instrumentIndex, int filterIndex)
	{
    meta.setFilterLotNumber(lotNumber, instrumentIndex, filterIndex);
	}

	public void setFilterManufacturer(String manufacturer, int instrumentIndex, int filterIndex)
	{
    meta.setFilterManufacturer(manufacturer, instrumentIndex, filterIndex);
	}

	public void setFilterModel(String model, int instrumentIndex, int filterIndex)
	{
    meta.setFilterModel(model, instrumentIndex, filterIndex);
	}

	public void setFilterSerialNumber(String serialNumber, int instrumentIndex, int filterIndex)
	{
    meta.setFilterSerialNumber(serialNumber, instrumentIndex, filterIndex);
	}

	// Ignoring TransmittanceRange element, complex property
	public void setFilterType(FilterType type, int instrumentIndex, int filterIndex)
	{
    meta.setFilterType(type, instrumentIndex, filterIndex);
	}

	//
	// FilterSet property storage
	//
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	// Ignoring Channel_BackReference back reference
	public void setFilterSetDichroicRef(String dichroic, int instrumentIndex, int filterSetIndex)
	{
    meta.setFilterSetDichroicRef(dichroic, instrumentIndex, filterSetIndex);
	}

	public void setFilterSetEmissionFilterRef(String emissionFilter, int instrumentIndex, int filterSetIndex, int emissionFilterRefIndex)
	{
    meta.setFilterSetEmissionFilterRef(emissionFilter, instrumentIndex, filterSetIndex, emissionFilterRefIndex);
	}

	public void setFilterSetExcitationFilterRef(String excitationFilter, int instrumentIndex, int filterSetIndex, int excitationFilterRefIndex)
	{
    meta.setFilterSetExcitationFilterRef(excitationFilter, instrumentIndex, filterSetIndex, excitationFilterRefIndex);
	}

	public void setFilterSetID(String id, int instrumentIndex, int filterSetIndex)
	{
    meta.setFilterSetID(id, instrumentIndex, filterSetIndex);
	}

	// Ignoring Instrument_BackReference back reference
	public void setFilterSetLotNumber(String lotNumber, int instrumentIndex, int filterSetIndex)
	{
    meta.setFilterSetLotNumber(lotNumber, instrumentIndex, filterSetIndex);
	}

	public void setFilterSetManufacturer(String manufacturer, int instrumentIndex, int filterSetIndex)
	{
    meta.setFilterSetManufacturer(manufacturer, instrumentIndex, filterSetIndex);
	}

	public void setFilterSetModel(String model, int instrumentIndex, int filterSetIndex)
	{
    meta.setFilterSetModel(model, instrumentIndex, filterSetIndex);
	}

	public void setFilterSetSerialNumber(String serialNumber, int instrumentIndex, int filterSetIndex)
	{
    meta.setFilterSetSerialNumber(serialNumber, instrumentIndex, filterSetIndex);
	}

	//
	// FilterSetRef property storage
	//
	// {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference FilterSetRef

	//
	// Image property storage
	//
	// {u'OME': None}
	// Is multi path? False

	public void setImageAcquisitionDate(Timestamp acquisitionDate, int imageIndex)
	{
    meta.setImageAcquisitionDate(acquisitionDate, imageIndex);
	}

	public void setImageAnnotationRef(String annotation, int imageIndex, int annotationRefIndex)
	{
    meta.setImageAnnotationRef(annotation, imageIndex, annotationRefIndex);
	}

	// Ignoring Dataset_BackReference back reference
	public void setImageDescription(String description, int imageIndex)
	{
    meta.setImageDescription(description, imageIndex);
	}

	public void setImageExperimentRef(String experiment, int imageIndex)
	{
    meta.setImageExperimentRef(experiment, imageIndex);
	}

	public void setImageExperimenterGroupRef(String experimenterGroup, int imageIndex)
	{
    meta.setImageExperimenterGroupRef(experimenterGroup, imageIndex);
	}

	public void setImageExperimenterRef(String experimenter, int imageIndex)
	{
    meta.setImageExperimenterRef(experimenter, imageIndex);
	}

	public void setImageID(String id, int imageIndex)
	{
    meta.setImageID(id, imageIndex);
	}

	// Ignoring ImagingEnvironment element, complex property
	public void setImageInstrumentRef(String instrument, int imageIndex)
	{
    meta.setImageInstrumentRef(instrument, imageIndex);
	}

	public void setImageMicrobeamManipulationRef(String microbeamManipulation, int imageIndex, int microbeamManipulationRefIndex)
	{
    meta.setImageMicrobeamManipulationRef(microbeamManipulation, imageIndex, microbeamManipulationRefIndex);
	}

	public void setImageName(String name, int imageIndex)
	{
    meta.setImageName(name, imageIndex);
	}

	// Ignoring ObjectiveSettings element, complex property
	// Ignoring Pixels element, complex property
	public void setImageROIRef(String roi, int imageIndex, int ROIRefIndex)
	{
    meta.setImageROIRef(roi, imageIndex, ROIRefIndex);
	}

	// Ignoring StageLabel element, complex property
	// Ignoring WellSample_BackReference back reference
	//
	// ImageRef property storage
	//
	// {u'WellSample': {u'Well': {u'Plate': {u'OME': None}}}, u'Dataset': {u'OME': None}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference ImageRef

	//
	// ImagingEnvironment property storage
	//
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	public void setImagingEnvironmentAirPressure(Double airPressure, int imageIndex)
	{
    meta.setImagingEnvironmentAirPressure(airPressure, imageIndex);
	}

	public void setImagingEnvironmentCO2Percent(PercentFraction co2Percent, int imageIndex)
	{
    meta.setImagingEnvironmentCO2Percent(co2Percent, imageIndex);
	}

	public void setImagingEnvironmentHumidity(PercentFraction humidity, int imageIndex)
	{
    meta.setImagingEnvironmentHumidity(humidity, imageIndex);
	}

	public void setImagingEnvironmentTemperature(Double temperature, int imageIndex)
	{
    meta.setImagingEnvironmentTemperature(temperature, imageIndex);
	}

	//
	// Instrument property storage
	//
	// {u'OME': None}
	// Is multi path? False

	// Ignoring Detector element, complex property
	// Ignoring Dichroic element, complex property
	// Ignoring Filter element, complex property
	// Ignoring FilterSet element, complex property
	public void setInstrumentID(String id, int instrumentIndex)
	{
    meta.setInstrumentID(id, instrumentIndex);
	}

	// Ignoring Image_BackReference back reference
	// Ignoring LightSource element, complex property
	// Ignoring Microscope element, complex property
	// Ignoring Objective element, complex property
	//
	// InstrumentRef property storage
	//
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference InstrumentRef

	//
	// Label property storage
	//
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public void setLabelFillColor(Color fillColor, int ROIIndex, int shapeIndex)
	{
    meta.setLabelFillColor(fillColor, ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public void setLabelFillRule(FillRule fillRule, int ROIIndex, int shapeIndex)
	{
    meta.setLabelFillRule(fillRule, ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public void setLabelFontFamily(FontFamily fontFamily, int ROIIndex, int shapeIndex)
	{
    meta.setLabelFontFamily(fontFamily, ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public void setLabelFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
	{
    meta.setLabelFontSize(fontSize, ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public void setLabelFontStyle(FontStyle fontStyle, int ROIIndex, int shapeIndex)
	{
    meta.setLabelFontStyle(fontStyle, ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public void setLabelID(String id, int ROIIndex, int shapeIndex)
	{
    meta.setLabelID(id, ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public void setLabelLineCap(LineCap lineCap, int ROIIndex, int shapeIndex)
	{
    meta.setLabelLineCap(lineCap, ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public void setLabelLocked(Boolean locked, int ROIIndex, int shapeIndex)
	{
    meta.setLabelLocked(locked, ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public void setLabelStrokeColor(Color strokeColor, int ROIIndex, int shapeIndex)
	{
    meta.setLabelStrokeColor(strokeColor, ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public void setLabelStrokeDashArray(String strokeDashArray, int ROIIndex, int shapeIndex)
	{
    meta.setLabelStrokeDashArray(strokeDashArray, ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public void setLabelStrokeWidth(Double strokeWidth, int ROIIndex, int shapeIndex)
	{
    meta.setLabelStrokeWidth(strokeWidth, ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public void setLabelText(String text, int ROIIndex, int shapeIndex)
	{
    meta.setLabelText(text, ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public void setLabelTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
	{
    meta.setLabelTheC(theC, ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public void setLabelTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
	{
    meta.setLabelTheT(theT, ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public void setLabelTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
	{
    meta.setLabelTheZ(theZ, ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public void setLabelTransform(AffineTransform transform, int ROIIndex, int shapeIndex)
	{
    meta.setLabelTransform(transform, ROIIndex, shapeIndex);
	}

	// Union_BackReference accessor from parent Shape
	public void setLabelUnion_BackReference(String union_BackReference, int ROIIndex, int shapeIndex)
	{
    meta.setLabelUnion_BackReference(union_BackReference, ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public void setLabelVisible(Boolean visible, int ROIIndex, int shapeIndex)
	{
    meta.setLabelVisible(visible, ROIIndex, shapeIndex);
	}

	public void setLabelX(Double x, int ROIIndex, int shapeIndex)
	{
    meta.setLabelX(x, ROIIndex, shapeIndex);
	}

	public void setLabelY(Double y, int ROIIndex, int shapeIndex)
	{
    meta.setLabelY(y, ROIIndex, shapeIndex);
	}

	//
	// Laser property storage
	//
	// {u'LightSource': {u'Instrument': {u'OME': None}}}
	// Is multi path? False

	// Ignoring Arc of parent abstract type
	// Ignoring Filament of parent abstract type
	// ID accessor from parent LightSource
	public void setLaserID(String id, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserID(id, instrumentIndex, lightSourceIndex);
	}

	// Instrument_BackReference accessor from parent LightSource
	public void setLaserInstrument_BackReference(String instrument_BackReference, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserInstrument_BackReference(instrument_BackReference, instrumentIndex, lightSourceIndex);
	}

	// Ignoring Laser of parent abstract type
	// Ignoring LightEmittingDiode of parent abstract type
	// LotNumber accessor from parent LightSource
	public void setLaserLotNumber(String lotNumber, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserLotNumber(lotNumber, instrumentIndex, lightSourceIndex);
	}

	// Manufacturer accessor from parent LightSource
	public void setLaserManufacturer(String manufacturer, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserManufacturer(manufacturer, instrumentIndex, lightSourceIndex);
	}

	// Model accessor from parent LightSource
	public void setLaserModel(String model, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserModel(model, instrumentIndex, lightSourceIndex);
	}

	// Power accessor from parent LightSource
	public void setLaserPower(Double power, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserPower(power, instrumentIndex, lightSourceIndex);
	}

	// SerialNumber accessor from parent LightSource
	public void setLaserSerialNumber(String serialNumber, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserSerialNumber(serialNumber, instrumentIndex, lightSourceIndex);
	}

	public void setLaserFrequencyMultiplication(PositiveInteger frequencyMultiplication, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserFrequencyMultiplication(frequencyMultiplication, instrumentIndex, lightSourceIndex);
	}

	public void setLaserLaserMedium(LaserMedium laserMedium, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserLaserMedium(laserMedium, instrumentIndex, lightSourceIndex);
	}

	public void setLaserPockelCell(Boolean pockelCell, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserPockelCell(pockelCell, instrumentIndex, lightSourceIndex);
	}

	public void setLaserPulse(Pulse pulse, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserPulse(pulse, instrumentIndex, lightSourceIndex);
	}

	public void setLaserPump(String pump, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserPump(pump, instrumentIndex, lightSourceIndex);
	}

	public void setLaserRepetitionRate(Double repetitionRate, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserRepetitionRate(repetitionRate, instrumentIndex, lightSourceIndex);
	}

	public void setLaserTuneable(Boolean tuneable, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserTuneable(tuneable, instrumentIndex, lightSourceIndex);
	}

	public void setLaserType(LaserType type, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserType(type, instrumentIndex, lightSourceIndex);
	}

	public void setLaserWavelength(PositiveInteger wavelength, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLaserWavelength(wavelength, instrumentIndex, lightSourceIndex);
	}

	//
	// Leader property storage
	//
	// {u'ExperimenterGroup': {u'OME': None}}
	// Is multi path? False

	// 0:9999
	// Is multi path? False
	// Ignoring ExperimenterGroup_BackReference property of reference Leader

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference Leader

	//
	// LightEmittingDiode property storage
	//
	// {u'LightSource': {u'Instrument': {u'OME': None}}}
	// Is multi path? False

	// Ignoring Arc of parent abstract type
	// Ignoring Filament of parent abstract type
	// ID accessor from parent LightSource
	public void setLightEmittingDiodeID(String id, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLightEmittingDiodeID(id, instrumentIndex, lightSourceIndex);
	}

	// Instrument_BackReference accessor from parent LightSource
	public void setLightEmittingDiodeInstrument_BackReference(String instrument_BackReference, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLightEmittingDiodeInstrument_BackReference(instrument_BackReference, instrumentIndex, lightSourceIndex);
	}

	// Ignoring Laser of parent abstract type
	// Ignoring LightEmittingDiode of parent abstract type
	// LotNumber accessor from parent LightSource
	public void setLightEmittingDiodeLotNumber(String lotNumber, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLightEmittingDiodeLotNumber(lotNumber, instrumentIndex, lightSourceIndex);
	}

	// Manufacturer accessor from parent LightSource
	public void setLightEmittingDiodeManufacturer(String manufacturer, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLightEmittingDiodeManufacturer(manufacturer, instrumentIndex, lightSourceIndex);
	}

	// Model accessor from parent LightSource
	public void setLightEmittingDiodeModel(String model, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLightEmittingDiodeModel(model, instrumentIndex, lightSourceIndex);
	}

	// Power accessor from parent LightSource
	public void setLightEmittingDiodePower(Double power, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLightEmittingDiodePower(power, instrumentIndex, lightSourceIndex);
	}

	// SerialNumber accessor from parent LightSource
	public void setLightEmittingDiodeSerialNumber(String serialNumber, int instrumentIndex, int lightSourceIndex)
	{
    meta.setLightEmittingDiodeSerialNumber(serialNumber, instrumentIndex, lightSourceIndex);
	}

	//
	// LightPath property storage
	//
	// {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}}
	// Is multi path? False

	public void setLightPathDichroicRef(String dichroic, int imageIndex, int channelIndex)
	{
    meta.setLightPathDichroicRef(dichroic, imageIndex, channelIndex);
	}

	public void setLightPathEmissionFilterRef(String emissionFilter, int imageIndex, int channelIndex, int emissionFilterRefIndex)
	{
    meta.setLightPathEmissionFilterRef(emissionFilter, imageIndex, channelIndex, emissionFilterRefIndex);
	}

	public void setLightPathExcitationFilterRef(String excitationFilter, int imageIndex, int channelIndex, int excitationFilterRefIndex)
	{
    meta.setLightPathExcitationFilterRef(excitationFilter, imageIndex, channelIndex, excitationFilterRefIndex);
	}

	//
	// LightSourceSettings property storage
	//
	// {u'Channel': {u'Pixels': {u'Image': {u'OME': None}}}, u'MicrobeamManipulation': {u'Experiment': {u'OME': None}}}
	// Is multi path? True

	public void setChannelLightSourceSettingsAttenuation(PercentFraction attenuation, int imageIndex, int channelIndex)
	{
    meta.setChannelLightSourceSettingsAttenuation(attenuation, imageIndex, channelIndex);
	}

	public void setMicrobeamManipulationLightSourceSettingsAttenuation(PercentFraction attenuation, int experimentIndex, int microbeamManipulationIndex, int lightSourceSettingsIndex)
	{
    meta.setMicrobeamManipulationLightSourceSettingsAttenuation(attenuation, experimentIndex, microbeamManipulationIndex, lightSourceSettingsIndex);
	}

	public void setChannelLightSourceSettingsID(String id, int imageIndex, int channelIndex)
	{
    meta.setChannelLightSourceSettingsID(id, imageIndex, channelIndex);
	}

	public void setMicrobeamManipulationLightSourceSettingsID(String id, int experimentIndex, int microbeamManipulationIndex, int lightSourceSettingsIndex)
	{
    meta.setMicrobeamManipulationLightSourceSettingsID(id, experimentIndex, microbeamManipulationIndex, lightSourceSettingsIndex);
	}

	// Ignoring LightSourceRef back reference
	// Ignoring MicrobeamManipulation_BackReference back reference
	public void setChannelLightSourceSettingsWavelength(PositiveInteger wavelength, int imageIndex, int channelIndex)
	{
    meta.setChannelLightSourceSettingsWavelength(wavelength, imageIndex, channelIndex);
	}

	public void setMicrobeamManipulationLightSourceSettingsWavelength(PositiveInteger wavelength, int experimentIndex, int microbeamManipulationIndex, int lightSourceSettingsIndex)
	{
    meta.setMicrobeamManipulationLightSourceSettingsWavelength(wavelength, experimentIndex, microbeamManipulationIndex, lightSourceSettingsIndex);
	}

	//
	// Line property storage
	//
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public void setLineFillColor(Color fillColor, int ROIIndex, int shapeIndex)
	{
    meta.setLineFillColor(fillColor, ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public void setLineFillRule(FillRule fillRule, int ROIIndex, int shapeIndex)
	{
    meta.setLineFillRule(fillRule, ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public void setLineFontFamily(FontFamily fontFamily, int ROIIndex, int shapeIndex)
	{
    meta.setLineFontFamily(fontFamily, ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public void setLineFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
	{
    meta.setLineFontSize(fontSize, ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public void setLineFontStyle(FontStyle fontStyle, int ROIIndex, int shapeIndex)
	{
    meta.setLineFontStyle(fontStyle, ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public void setLineID(String id, int ROIIndex, int shapeIndex)
	{
    meta.setLineID(id, ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public void setLineLineCap(LineCap lineCap, int ROIIndex, int shapeIndex)
	{
    meta.setLineLineCap(lineCap, ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public void setLineLocked(Boolean locked, int ROIIndex, int shapeIndex)
	{
    meta.setLineLocked(locked, ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public void setLineStrokeColor(Color strokeColor, int ROIIndex, int shapeIndex)
	{
    meta.setLineStrokeColor(strokeColor, ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public void setLineStrokeDashArray(String strokeDashArray, int ROIIndex, int shapeIndex)
	{
    meta.setLineStrokeDashArray(strokeDashArray, ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public void setLineStrokeWidth(Double strokeWidth, int ROIIndex, int shapeIndex)
	{
    meta.setLineStrokeWidth(strokeWidth, ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public void setLineText(String text, int ROIIndex, int shapeIndex)
	{
    meta.setLineText(text, ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public void setLineTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
	{
    meta.setLineTheC(theC, ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public void setLineTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
	{
    meta.setLineTheT(theT, ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public void setLineTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
	{
    meta.setLineTheZ(theZ, ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public void setLineTransform(AffineTransform transform, int ROIIndex, int shapeIndex)
	{
    meta.setLineTransform(transform, ROIIndex, shapeIndex);
	}

	// Union_BackReference accessor from parent Shape
	public void setLineUnion_BackReference(String union_BackReference, int ROIIndex, int shapeIndex)
	{
    meta.setLineUnion_BackReference(union_BackReference, ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public void setLineVisible(Boolean visible, int ROIIndex, int shapeIndex)
	{
    meta.setLineVisible(visible, ROIIndex, shapeIndex);
	}

	public void setLineMarkerEnd(Marker markerEnd, int ROIIndex, int shapeIndex)
	{
    meta.setLineMarkerEnd(markerEnd, ROIIndex, shapeIndex);
	}

	public void setLineMarkerStart(Marker markerStart, int ROIIndex, int shapeIndex)
	{
    meta.setLineMarkerStart(markerStart, ROIIndex, shapeIndex);
	}

	public void setLineX1(Double x1, int ROIIndex, int shapeIndex)
	{
    meta.setLineX1(x1, ROIIndex, shapeIndex);
	}

	public void setLineX2(Double x2, int ROIIndex, int shapeIndex)
	{
    meta.setLineX2(x2, ROIIndex, shapeIndex);
	}

	public void setLineY1(Double y1, int ROIIndex, int shapeIndex)
	{
    meta.setLineY1(y1, ROIIndex, shapeIndex);
	}

	public void setLineY2(Double y2, int ROIIndex, int shapeIndex)
	{
    meta.setLineY2(y2, ROIIndex, shapeIndex);
	}

	//
	// ListAnnotation property storage
	//
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public void setListAnnotationAnnotationRef(String annotation, int listAnnotationIndex, int annotationRefIndex)
	{
    meta.setListAnnotationAnnotationRef(annotation, listAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public void setListAnnotationDescription(String description, int listAnnotationIndex)
	{
    meta.setListAnnotationDescription(description, listAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public void setListAnnotationID(String id, int listAnnotationIndex)
	{
    meta.setListAnnotationID(id, listAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setListAnnotationNamespace(String namespace, int listAnnotationIndex)
	{
    meta.setListAnnotationNamespace(namespace, listAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// LongAnnotation property storage
	//
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public void setLongAnnotationAnnotationRef(String annotation, int longAnnotationIndex, int annotationRefIndex)
	{
    meta.setLongAnnotationAnnotationRef(annotation, longAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public void setLongAnnotationDescription(String description, int longAnnotationIndex)
	{
    meta.setLongAnnotationDescription(description, longAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public void setLongAnnotationID(String id, int longAnnotationIndex)
	{
    meta.setLongAnnotationID(id, longAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setLongAnnotationNamespace(String namespace, int longAnnotationIndex)
	{
    meta.setLongAnnotationNamespace(namespace, longAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public void setLongAnnotationValue(Long value, int longAnnotationIndex)
	{
    meta.setLongAnnotationValue(value, longAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// Mask property storage
	//
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public void setMaskFillColor(Color fillColor, int ROIIndex, int shapeIndex)
	{
    meta.setMaskFillColor(fillColor, ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public void setMaskFillRule(FillRule fillRule, int ROIIndex, int shapeIndex)
	{
    meta.setMaskFillRule(fillRule, ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public void setMaskFontFamily(FontFamily fontFamily, int ROIIndex, int shapeIndex)
	{
    meta.setMaskFontFamily(fontFamily, ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public void setMaskFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
	{
    meta.setMaskFontSize(fontSize, ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public void setMaskFontStyle(FontStyle fontStyle, int ROIIndex, int shapeIndex)
	{
    meta.setMaskFontStyle(fontStyle, ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public void setMaskID(String id, int ROIIndex, int shapeIndex)
	{
    meta.setMaskID(id, ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public void setMaskLineCap(LineCap lineCap, int ROIIndex, int shapeIndex)
	{
    meta.setMaskLineCap(lineCap, ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public void setMaskLocked(Boolean locked, int ROIIndex, int shapeIndex)
	{
    meta.setMaskLocked(locked, ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public void setMaskStrokeColor(Color strokeColor, int ROIIndex, int shapeIndex)
	{
    meta.setMaskStrokeColor(strokeColor, ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public void setMaskStrokeDashArray(String strokeDashArray, int ROIIndex, int shapeIndex)
	{
    meta.setMaskStrokeDashArray(strokeDashArray, ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public void setMaskStrokeWidth(Double strokeWidth, int ROIIndex, int shapeIndex)
	{
    meta.setMaskStrokeWidth(strokeWidth, ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public void setMaskText(String text, int ROIIndex, int shapeIndex)
	{
    meta.setMaskText(text, ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public void setMaskTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
	{
    meta.setMaskTheC(theC, ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public void setMaskTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
	{
    meta.setMaskTheT(theT, ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public void setMaskTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
	{
    meta.setMaskTheZ(theZ, ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public void setMaskTransform(AffineTransform transform, int ROIIndex, int shapeIndex)
	{
    meta.setMaskTransform(transform, ROIIndex, shapeIndex);
	}

	// Union_BackReference accessor from parent Shape
	public void setMaskUnion_BackReference(String union_BackReference, int ROIIndex, int shapeIndex)
	{
    meta.setMaskUnion_BackReference(union_BackReference, ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public void setMaskVisible(Boolean visible, int ROIIndex, int shapeIndex)
	{
    meta.setMaskVisible(visible, ROIIndex, shapeIndex);
	}

	// Ignoring BinData element, complex property
	public void setMaskHeight(Double height, int ROIIndex, int shapeIndex)
	{
    meta.setMaskHeight(height, ROIIndex, shapeIndex);
	}

	public void setMaskWidth(Double width, int ROIIndex, int shapeIndex)
	{
    meta.setMaskWidth(width, ROIIndex, shapeIndex);
	}

	public void setMaskX(Double x, int ROIIndex, int shapeIndex)
	{
    meta.setMaskX(x, ROIIndex, shapeIndex);
	}

	public void setMaskY(Double y, int ROIIndex, int shapeIndex)
	{
    meta.setMaskY(y, ROIIndex, shapeIndex);
	}

	//
	// MetadataOnly property storage
	//
	// {u'Pixels': {u'Image': {u'OME': None}}}
	// Is multi path? False

	//
	// MicrobeamManipulation property storage
	//
	// {u'Experiment': {u'OME': None}}
	// Is multi path? False

	public void setMicrobeamManipulationDescription(String description, int experimentIndex, int microbeamManipulationIndex)
	{
    meta.setMicrobeamManipulationDescription(description, experimentIndex, microbeamManipulationIndex);
	}

	// Ignoring Experiment_BackReference back reference
	public void setMicrobeamManipulationExperimenterRef(String experimenter, int experimentIndex, int microbeamManipulationIndex)
	{
    meta.setMicrobeamManipulationExperimenterRef(experimenter, experimentIndex, microbeamManipulationIndex);
	}

	public void setMicrobeamManipulationID(String id, int experimentIndex, int microbeamManipulationIndex)
	{
    meta.setMicrobeamManipulationID(id, experimentIndex, microbeamManipulationIndex);
	}

	// Ignoring Image_BackReference back reference
	// Ignoring LightSourceSettings element, complex property
	public void setMicrobeamManipulationROIRef(String roi, int experimentIndex, int microbeamManipulationIndex, int ROIRefIndex)
	{
    meta.setMicrobeamManipulationROIRef(roi, experimentIndex, microbeamManipulationIndex, ROIRefIndex);
	}

	public void setMicrobeamManipulationType(MicrobeamManipulationType type, int experimentIndex, int microbeamManipulationIndex)
	{
    meta.setMicrobeamManipulationType(type, experimentIndex, microbeamManipulationIndex);
	}

	//
	// MicrobeamManipulationRef property storage
	//
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference MicrobeamManipulationRef

	//
	// Microscope property storage
	//
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	public void setMicroscopeLotNumber(String lotNumber, int instrumentIndex)
	{
    meta.setMicroscopeLotNumber(lotNumber, instrumentIndex);
	}

	public void setMicroscopeManufacturer(String manufacturer, int instrumentIndex)
	{
    meta.setMicroscopeManufacturer(manufacturer, instrumentIndex);
	}

	public void setMicroscopeModel(String model, int instrumentIndex)
	{
    meta.setMicroscopeModel(model, instrumentIndex);
	}

	public void setMicroscopeSerialNumber(String serialNumber, int instrumentIndex)
	{
    meta.setMicroscopeSerialNumber(serialNumber, instrumentIndex);
	}

	public void setMicroscopeType(MicroscopeType type, int instrumentIndex)
	{
    meta.setMicroscopeType(type, instrumentIndex);
	}

	//
	// Objective property storage
	//
	// {u'Instrument': {u'OME': None}}
	// Is multi path? False

	public void setObjectiveCalibratedMagnification(Double calibratedMagnification, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveCalibratedMagnification(calibratedMagnification, instrumentIndex, objectiveIndex);
	}

	public void setObjectiveCorrection(Correction correction, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveCorrection(correction, instrumentIndex, objectiveIndex);
	}

	public void setObjectiveID(String id, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveID(id, instrumentIndex, objectiveIndex);
	}

	public void setObjectiveImmersion(Immersion immersion, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveImmersion(immersion, instrumentIndex, objectiveIndex);
	}

	// Ignoring Instrument_BackReference back reference
	public void setObjectiveIris(Boolean iris, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveIris(iris, instrumentIndex, objectiveIndex);
	}

	public void setObjectiveLensNA(Double lensNA, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveLensNA(lensNA, instrumentIndex, objectiveIndex);
	}

	public void setObjectiveLotNumber(String lotNumber, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveLotNumber(lotNumber, instrumentIndex, objectiveIndex);
	}

	public void setObjectiveManufacturer(String manufacturer, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveManufacturer(manufacturer, instrumentIndex, objectiveIndex);
	}

	public void setObjectiveModel(String model, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveModel(model, instrumentIndex, objectiveIndex);
	}

	public void setObjectiveNominalMagnification(PositiveInteger nominalMagnification, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveNominalMagnification(nominalMagnification, instrumentIndex, objectiveIndex);
	}

	public void setObjectiveSerialNumber(String serialNumber, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveSerialNumber(serialNumber, instrumentIndex, objectiveIndex);
	}

	public void setObjectiveWorkingDistance(Double workingDistance, int instrumentIndex, int objectiveIndex)
	{
    meta.setObjectiveWorkingDistance(workingDistance, instrumentIndex, objectiveIndex);
	}

	//
	// ObjectiveSettings property storage
	//
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	public void setObjectiveSettingsCorrectionCollar(Double correctionCollar, int imageIndex)
	{
    meta.setObjectiveSettingsCorrectionCollar(correctionCollar, imageIndex);
	}

	public void setObjectiveSettingsID(String id, int imageIndex)
	{
    meta.setObjectiveSettingsID(id, imageIndex);
	}

	public void setObjectiveSettingsMedium(Medium medium, int imageIndex)
	{
    meta.setObjectiveSettingsMedium(medium, imageIndex);
	}

	// Ignoring ObjectiveRef back reference
	public void setObjectiveSettingsRefractiveIndex(Double refractiveIndex, int imageIndex)
	{
    meta.setObjectiveSettingsRefractiveIndex(refractiveIndex, imageIndex);
	}

	//
	// Pixels property storage
	//
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	public void setPixelsAnnotationRef(String annotation, int imageIndex, int annotationRefIndex)
	{
    meta.setPixelsAnnotationRef(annotation, imageIndex, annotationRefIndex);
	}

	// Ignoring BinData element, complex property
	// Ignoring Channel element, complex property
	public void setPixelsDimensionOrder(DimensionOrder dimensionOrder, int imageIndex)
	{
    meta.setPixelsDimensionOrder(dimensionOrder, imageIndex);
	}

	public void setPixelsID(String id, int imageIndex)
	{
    meta.setPixelsID(id, imageIndex);
	}

	// Ignoring MetadataOnly element, complex property
	public void setPixelsPhysicalSizeX(PositiveFloat physicalSizeX, int imageIndex)
	{
    meta.setPixelsPhysicalSizeX(physicalSizeX, imageIndex);
	}

	public void setPixelsPhysicalSizeY(PositiveFloat physicalSizeY, int imageIndex)
	{
    meta.setPixelsPhysicalSizeY(physicalSizeY, imageIndex);
	}

	public void setPixelsPhysicalSizeZ(PositiveFloat physicalSizeZ, int imageIndex)
	{
    meta.setPixelsPhysicalSizeZ(physicalSizeZ, imageIndex);
	}

	// Ignoring Plane element, complex property
	public void setPixelsSizeC(PositiveInteger sizeC, int imageIndex)
	{
    meta.setPixelsSizeC(sizeC, imageIndex);
	}

	public void setPixelsSizeT(PositiveInteger sizeT, int imageIndex)
	{
    meta.setPixelsSizeT(sizeT, imageIndex);
	}

	public void setPixelsSizeX(PositiveInteger sizeX, int imageIndex)
	{
    meta.setPixelsSizeX(sizeX, imageIndex);
	}

	public void setPixelsSizeY(PositiveInteger sizeY, int imageIndex)
	{
    meta.setPixelsSizeY(sizeY, imageIndex);
	}

	public void setPixelsSizeZ(PositiveInteger sizeZ, int imageIndex)
	{
    meta.setPixelsSizeZ(sizeZ, imageIndex);
	}

	// Ignoring TiffData element, complex property
	public void setPixelsTimeIncrement(Double timeIncrement, int imageIndex)
	{
    meta.setPixelsTimeIncrement(timeIncrement, imageIndex);
	}

	public void setPixelsType(PixelType type, int imageIndex)
	{
    meta.setPixelsType(type, imageIndex);
	}

	//
	// Plane property storage
	//
	// {u'Pixels': {u'Image': {u'OME': None}}}
	// Is multi path? False

	public void setPlaneAnnotationRef(String annotation, int imageIndex, int planeIndex, int annotationRefIndex)
	{
    meta.setPlaneAnnotationRef(annotation, imageIndex, planeIndex, annotationRefIndex);
	}

	public void setPlaneDeltaT(Double deltaT, int imageIndex, int planeIndex)
	{
    meta.setPlaneDeltaT(deltaT, imageIndex, planeIndex);
	}

	public void setPlaneExposureTime(Double exposureTime, int imageIndex, int planeIndex)
	{
    meta.setPlaneExposureTime(exposureTime, imageIndex, planeIndex);
	}

	public void setPlaneHashSHA1(String hashSHA1, int imageIndex, int planeIndex)
	{
    meta.setPlaneHashSHA1(hashSHA1, imageIndex, planeIndex);
	}

	// Ignoring Pixels_BackReference back reference
	public void setPlanePositionX(Double positionX, int imageIndex, int planeIndex)
	{
    meta.setPlanePositionX(positionX, imageIndex, planeIndex);
	}

	public void setPlanePositionY(Double positionY, int imageIndex, int planeIndex)
	{
    meta.setPlanePositionY(positionY, imageIndex, planeIndex);
	}

	public void setPlanePositionZ(Double positionZ, int imageIndex, int planeIndex)
	{
    meta.setPlanePositionZ(positionZ, imageIndex, planeIndex);
	}

	public void setPlaneTheC(NonNegativeInteger theC, int imageIndex, int planeIndex)
	{
    meta.setPlaneTheC(theC, imageIndex, planeIndex);
	}

	public void setPlaneTheT(NonNegativeInteger theT, int imageIndex, int planeIndex)
	{
    meta.setPlaneTheT(theT, imageIndex, planeIndex);
	}

	public void setPlaneTheZ(NonNegativeInteger theZ, int imageIndex, int planeIndex)
	{
    meta.setPlaneTheZ(theZ, imageIndex, planeIndex);
	}

	//
	// Plate property storage
	//
	// {u'OME': None}
	// Is multi path? False

	public void setPlateAnnotationRef(String annotation, int plateIndex, int annotationRefIndex)
	{
    meta.setPlateAnnotationRef(annotation, plateIndex, annotationRefIndex);
	}

	public void setPlateColumnNamingConvention(NamingConvention columnNamingConvention, int plateIndex)
	{
    meta.setPlateColumnNamingConvention(columnNamingConvention, plateIndex);
	}

	public void setPlateColumns(PositiveInteger columns, int plateIndex)
	{
    meta.setPlateColumns(columns, plateIndex);
	}

	public void setPlateDescription(String description, int plateIndex)
	{
    meta.setPlateDescription(description, plateIndex);
	}

	public void setPlateExternalIdentifier(String externalIdentifier, int plateIndex)
	{
    meta.setPlateExternalIdentifier(externalIdentifier, plateIndex);
	}

	public void setPlateFieldIndex(NonNegativeInteger fieldIndex, int plateIndex)
	{
    meta.setPlateFieldIndex(fieldIndex, plateIndex);
	}

	public void setPlateID(String id, int plateIndex)
	{
    meta.setPlateID(id, plateIndex);
	}

	public void setPlateName(String name, int plateIndex)
	{
    meta.setPlateName(name, plateIndex);
	}

	// Ignoring PlateAcquisition element, complex property
	public void setPlateRowNamingConvention(NamingConvention rowNamingConvention, int plateIndex)
	{
    meta.setPlateRowNamingConvention(rowNamingConvention, plateIndex);
	}

	public void setPlateRows(PositiveInteger rows, int plateIndex)
	{
    meta.setPlateRows(rows, plateIndex);
	}

	// Ignoring Screen_BackReference back reference
	public void setPlateStatus(String status, int plateIndex)
	{
    meta.setPlateStatus(status, plateIndex);
	}

	// Ignoring Well element, complex property
	public void setPlateWellOriginX(Double wellOriginX, int plateIndex)
	{
    meta.setPlateWellOriginX(wellOriginX, plateIndex);
	}

	public void setPlateWellOriginY(Double wellOriginY, int plateIndex)
	{
    meta.setPlateWellOriginY(wellOriginY, plateIndex);
	}

	//
	// PlateAcquisition property storage
	//
	// {u'Plate': {u'OME': None}}
	// Is multi path? False

	public void setPlateAcquisitionAnnotationRef(String annotation, int plateIndex, int plateAcquisitionIndex, int annotationRefIndex)
	{
    meta.setPlateAcquisitionAnnotationRef(annotation, plateIndex, plateAcquisitionIndex, annotationRefIndex);
	}

	public void setPlateAcquisitionDescription(String description, int plateIndex, int plateAcquisitionIndex)
	{
    meta.setPlateAcquisitionDescription(description, plateIndex, plateAcquisitionIndex);
	}

	public void setPlateAcquisitionEndTime(Timestamp endTime, int plateIndex, int plateAcquisitionIndex)
	{
    meta.setPlateAcquisitionEndTime(endTime, plateIndex, plateAcquisitionIndex);
	}

	public void setPlateAcquisitionID(String id, int plateIndex, int plateAcquisitionIndex)
	{
    meta.setPlateAcquisitionID(id, plateIndex, plateAcquisitionIndex);
	}

	public void setPlateAcquisitionMaximumFieldCount(PositiveInteger maximumFieldCount, int plateIndex, int plateAcquisitionIndex)
	{
    meta.setPlateAcquisitionMaximumFieldCount(maximumFieldCount, plateIndex, plateAcquisitionIndex);
	}

	public void setPlateAcquisitionName(String name, int plateIndex, int plateAcquisitionIndex)
	{
    meta.setPlateAcquisitionName(name, plateIndex, plateAcquisitionIndex);
	}

	// Ignoring Plate_BackReference back reference
	public void setPlateAcquisitionStartTime(Timestamp startTime, int plateIndex, int plateAcquisitionIndex)
	{
    meta.setPlateAcquisitionStartTime(startTime, plateIndex, plateAcquisitionIndex);
	}

	public void setPlateAcquisitionWellSampleRef(String wellSample, int plateIndex, int plateAcquisitionIndex, int wellSampleRefIndex)
	{
    meta.setPlateAcquisitionWellSampleRef(wellSample, plateIndex, plateAcquisitionIndex, wellSampleRefIndex);
	}

	//
	// PlateRef property storage
	//
	// {u'Screen': {u'OME': None}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference PlateRef

	//
	// Point property storage
	//
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public void setPointFillColor(Color fillColor, int ROIIndex, int shapeIndex)
	{
    meta.setPointFillColor(fillColor, ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public void setPointFillRule(FillRule fillRule, int ROIIndex, int shapeIndex)
	{
    meta.setPointFillRule(fillRule, ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public void setPointFontFamily(FontFamily fontFamily, int ROIIndex, int shapeIndex)
	{
    meta.setPointFontFamily(fontFamily, ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public void setPointFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
	{
    meta.setPointFontSize(fontSize, ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public void setPointFontStyle(FontStyle fontStyle, int ROIIndex, int shapeIndex)
	{
    meta.setPointFontStyle(fontStyle, ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public void setPointID(String id, int ROIIndex, int shapeIndex)
	{
    meta.setPointID(id, ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public void setPointLineCap(LineCap lineCap, int ROIIndex, int shapeIndex)
	{
    meta.setPointLineCap(lineCap, ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public void setPointLocked(Boolean locked, int ROIIndex, int shapeIndex)
	{
    meta.setPointLocked(locked, ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public void setPointStrokeColor(Color strokeColor, int ROIIndex, int shapeIndex)
	{
    meta.setPointStrokeColor(strokeColor, ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public void setPointStrokeDashArray(String strokeDashArray, int ROIIndex, int shapeIndex)
	{
    meta.setPointStrokeDashArray(strokeDashArray, ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public void setPointStrokeWidth(Double strokeWidth, int ROIIndex, int shapeIndex)
	{
    meta.setPointStrokeWidth(strokeWidth, ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public void setPointText(String text, int ROIIndex, int shapeIndex)
	{
    meta.setPointText(text, ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public void setPointTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
	{
    meta.setPointTheC(theC, ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public void setPointTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
	{
    meta.setPointTheT(theT, ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public void setPointTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
	{
    meta.setPointTheZ(theZ, ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public void setPointTransform(AffineTransform transform, int ROIIndex, int shapeIndex)
	{
    meta.setPointTransform(transform, ROIIndex, shapeIndex);
	}

	// Union_BackReference accessor from parent Shape
	public void setPointUnion_BackReference(String union_BackReference, int ROIIndex, int shapeIndex)
	{
    meta.setPointUnion_BackReference(union_BackReference, ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public void setPointVisible(Boolean visible, int ROIIndex, int shapeIndex)
	{
    meta.setPointVisible(visible, ROIIndex, shapeIndex);
	}

	public void setPointX(Double x, int ROIIndex, int shapeIndex)
	{
    meta.setPointX(x, ROIIndex, shapeIndex);
	}

	public void setPointY(Double y, int ROIIndex, int shapeIndex)
	{
    meta.setPointY(y, ROIIndex, shapeIndex);
	}

	//
	// Polygon property storage
	//
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public void setPolygonFillColor(Color fillColor, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonFillColor(fillColor, ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public void setPolygonFillRule(FillRule fillRule, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonFillRule(fillRule, ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public void setPolygonFontFamily(FontFamily fontFamily, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonFontFamily(fontFamily, ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public void setPolygonFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonFontSize(fontSize, ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public void setPolygonFontStyle(FontStyle fontStyle, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonFontStyle(fontStyle, ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public void setPolygonID(String id, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonID(id, ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public void setPolygonLineCap(LineCap lineCap, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonLineCap(lineCap, ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public void setPolygonLocked(Boolean locked, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonLocked(locked, ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public void setPolygonStrokeColor(Color strokeColor, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonStrokeColor(strokeColor, ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public void setPolygonStrokeDashArray(String strokeDashArray, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonStrokeDashArray(strokeDashArray, ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public void setPolygonStrokeWidth(Double strokeWidth, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonStrokeWidth(strokeWidth, ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public void setPolygonText(String text, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonText(text, ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public void setPolygonTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonTheC(theC, ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public void setPolygonTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonTheT(theT, ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public void setPolygonTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonTheZ(theZ, ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public void setPolygonTransform(AffineTransform transform, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonTransform(transform, ROIIndex, shapeIndex);
	}

	// Union_BackReference accessor from parent Shape
	public void setPolygonUnion_BackReference(String union_BackReference, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonUnion_BackReference(union_BackReference, ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public void setPolygonVisible(Boolean visible, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonVisible(visible, ROIIndex, shapeIndex);
	}

	public void setPolygonPoints(String points, int ROIIndex, int shapeIndex)
	{
    meta.setPolygonPoints(points, ROIIndex, shapeIndex);
	}

	//
	// Polyline property storage
	//
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public void setPolylineFillColor(Color fillColor, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineFillColor(fillColor, ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public void setPolylineFillRule(FillRule fillRule, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineFillRule(fillRule, ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public void setPolylineFontFamily(FontFamily fontFamily, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineFontFamily(fontFamily, ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public void setPolylineFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineFontSize(fontSize, ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public void setPolylineFontStyle(FontStyle fontStyle, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineFontStyle(fontStyle, ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public void setPolylineID(String id, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineID(id, ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public void setPolylineLineCap(LineCap lineCap, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineLineCap(lineCap, ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public void setPolylineLocked(Boolean locked, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineLocked(locked, ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public void setPolylineStrokeColor(Color strokeColor, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineStrokeColor(strokeColor, ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public void setPolylineStrokeDashArray(String strokeDashArray, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineStrokeDashArray(strokeDashArray, ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public void setPolylineStrokeWidth(Double strokeWidth, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineStrokeWidth(strokeWidth, ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public void setPolylineText(String text, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineText(text, ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public void setPolylineTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineTheC(theC, ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public void setPolylineTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineTheT(theT, ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public void setPolylineTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineTheZ(theZ, ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public void setPolylineTransform(AffineTransform transform, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineTransform(transform, ROIIndex, shapeIndex);
	}

	// Union_BackReference accessor from parent Shape
	public void setPolylineUnion_BackReference(String union_BackReference, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineUnion_BackReference(union_BackReference, ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public void setPolylineVisible(Boolean visible, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineVisible(visible, ROIIndex, shapeIndex);
	}

	public void setPolylineMarkerEnd(Marker markerEnd, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineMarkerEnd(markerEnd, ROIIndex, shapeIndex);
	}

	public void setPolylineMarkerStart(Marker markerStart, int ROIIndex, int shapeIndex)
	{
    meta.setPolylineMarkerStart(markerStart, ROIIndex, shapeIndex);
	}

	public void setPolylinePoints(String points, int ROIIndex, int shapeIndex)
	{
    meta.setPolylinePoints(points, ROIIndex, shapeIndex);
	}

	//
	// Project property storage
	//
	// {u'OME': None}
	// Is multi path? False

	public void setProjectAnnotationRef(String annotation, int projectIndex, int annotationRefIndex)
	{
    meta.setProjectAnnotationRef(annotation, projectIndex, annotationRefIndex);
	}

	public void setProjectDatasetRef(String dataset, int projectIndex, int datasetRefIndex)
	{
    meta.setProjectDatasetRef(dataset, projectIndex, datasetRefIndex);
	}

	public void setProjectDescription(String description, int projectIndex)
	{
    meta.setProjectDescription(description, projectIndex);
	}

	public void setProjectExperimenterGroupRef(String experimenterGroup, int projectIndex)
	{
    meta.setProjectExperimenterGroupRef(experimenterGroup, projectIndex);
	}

	public void setProjectExperimenterRef(String experimenter, int projectIndex)
	{
    meta.setProjectExperimenterRef(experimenter, projectIndex);
	}

	public void setProjectID(String id, int projectIndex)
	{
    meta.setProjectID(id, projectIndex);
	}

	public void setProjectName(String name, int projectIndex)
	{
    meta.setProjectName(name, projectIndex);
	}

	//
	// Pump property storage
	//
	// {u'Laser': {u'LightSource': {u'Instrument': {u'OME': None}}}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference Pump

	// 0:9999
	// Is multi path? False
	// Ignoring Laser_BackReference property of reference Pump

	//
	// ROI property storage
	//
	// {u'OME': None}
	// Is multi path? False

	public void setROIAnnotationRef(String annotation, int ROIIndex, int annotationRefIndex)
	{
    meta.setROIAnnotationRef(annotation, ROIIndex, annotationRefIndex);
	}

	public void setROIDescription(String description, int ROIIndex)
	{
    meta.setROIDescription(description, ROIIndex);
	}

	public void setROIID(String id, int ROIIndex)
	{
    meta.setROIID(id, ROIIndex);
	}

	// Ignoring Image_BackReference back reference
	// Ignoring MicrobeamManipulation_BackReference back reference
	public void setROIName(String name, int ROIIndex)
	{
    meta.setROIName(name, ROIIndex);
	}

	public void setROINamespace(String namespace, int ROIIndex)
	{
    meta.setROINamespace(namespace, ROIIndex);
	}

	// Ignoring Union element, complex property
	//
	// ROIRef property storage
	//
	// {u'Image': {u'OME': None}, u'MicrobeamManipulation': {u'Experiment': {u'OME': None}}}
	// Is multi path? True

	// 1:1
	// Is multi path? True
	// Ignoring ID property of reference ROIRef

	//
	// Reagent property storage
	//
	// {u'Screen': {u'OME': None}}
	// Is multi path? False

	public void setReagentAnnotationRef(String annotation, int screenIndex, int reagentIndex, int annotationRefIndex)
	{
    meta.setReagentAnnotationRef(annotation, screenIndex, reagentIndex, annotationRefIndex);
	}

	public void setReagentDescription(String description, int screenIndex, int reagentIndex)
	{
    meta.setReagentDescription(description, screenIndex, reagentIndex);
	}

	public void setReagentID(String id, int screenIndex, int reagentIndex)
	{
    meta.setReagentID(id, screenIndex, reagentIndex);
	}

	public void setReagentName(String name, int screenIndex, int reagentIndex)
	{
    meta.setReagentName(name, screenIndex, reagentIndex);
	}

	public void setReagentReagentIdentifier(String reagentIdentifier, int screenIndex, int reagentIndex)
	{
    meta.setReagentReagentIdentifier(reagentIdentifier, screenIndex, reagentIndex);
	}

	// Ignoring Screen_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// ReagentRef property storage
	//
	// {u'Well': {u'Plate': {u'OME': None}}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference ReagentRef

	//
	// Rectangle property storage
	//
	// {u'Shape': {u'Union': {u'ROI': {u'OME': None}}}}
	// Is multi path? False

	// Ignoring Ellipse of parent abstract type
	// FillColor accessor from parent Shape
	public void setRectangleFillColor(Color fillColor, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleFillColor(fillColor, ROIIndex, shapeIndex);
	}

	// FillRule accessor from parent Shape
	public void setRectangleFillRule(FillRule fillRule, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleFillRule(fillRule, ROIIndex, shapeIndex);
	}

	// FontFamily accessor from parent Shape
	public void setRectangleFontFamily(FontFamily fontFamily, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleFontFamily(fontFamily, ROIIndex, shapeIndex);
	}

	// FontSize accessor from parent Shape
	public void setRectangleFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleFontSize(fontSize, ROIIndex, shapeIndex);
	}

	// FontStyle accessor from parent Shape
	public void setRectangleFontStyle(FontStyle fontStyle, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleFontStyle(fontStyle, ROIIndex, shapeIndex);
	}

	// ID accessor from parent Shape
	public void setRectangleID(String id, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleID(id, ROIIndex, shapeIndex);
	}

	// Ignoring Label of parent abstract type
	// Ignoring Line of parent abstract type
	// LineCap accessor from parent Shape
	public void setRectangleLineCap(LineCap lineCap, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleLineCap(lineCap, ROIIndex, shapeIndex);
	}

	// Locked accessor from parent Shape
	public void setRectangleLocked(Boolean locked, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleLocked(locked, ROIIndex, shapeIndex);
	}

	// Ignoring Mask of parent abstract type
	// Ignoring Point of parent abstract type
	// Ignoring Polygon of parent abstract type
	// Ignoring Polyline of parent abstract type
	// Ignoring Rectangle of parent abstract type
	// StrokeColor accessor from parent Shape
	public void setRectangleStrokeColor(Color strokeColor, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleStrokeColor(strokeColor, ROIIndex, shapeIndex);
	}

	// StrokeDashArray accessor from parent Shape
	public void setRectangleStrokeDashArray(String strokeDashArray, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleStrokeDashArray(strokeDashArray, ROIIndex, shapeIndex);
	}

	// StrokeWidth accessor from parent Shape
	public void setRectangleStrokeWidth(Double strokeWidth, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleStrokeWidth(strokeWidth, ROIIndex, shapeIndex);
	}

	// Text accessor from parent Shape
	public void setRectangleText(String text, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleText(text, ROIIndex, shapeIndex);
	}

	// TheC accessor from parent Shape
	public void setRectangleTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleTheC(theC, ROIIndex, shapeIndex);
	}

	// TheT accessor from parent Shape
	public void setRectangleTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleTheT(theT, ROIIndex, shapeIndex);
	}

	// TheZ accessor from parent Shape
	public void setRectangleTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleTheZ(theZ, ROIIndex, shapeIndex);
	}

	// Transform accessor from parent Shape
	public void setRectangleTransform(AffineTransform transform, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleTransform(transform, ROIIndex, shapeIndex);
	}

	// Union_BackReference accessor from parent Shape
	public void setRectangleUnion_BackReference(String union_BackReference, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleUnion_BackReference(union_BackReference, ROIIndex, shapeIndex);
	}

	// Visible accessor from parent Shape
	public void setRectangleVisible(Boolean visible, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleVisible(visible, ROIIndex, shapeIndex);
	}

	public void setRectangleHeight(Double height, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleHeight(height, ROIIndex, shapeIndex);
	}

	public void setRectangleWidth(Double width, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleWidth(width, ROIIndex, shapeIndex);
	}

	public void setRectangleX(Double x, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleX(x, ROIIndex, shapeIndex);
	}

	public void setRectangleY(Double y, int ROIIndex, int shapeIndex)
	{
    meta.setRectangleY(y, ROIIndex, shapeIndex);
	}

	//
	// Screen property storage
	//
	// {u'OME': None}
	// Is multi path? False

	public void setScreenAnnotationRef(String annotation, int screenIndex, int annotationRefIndex)
	{
    meta.setScreenAnnotationRef(annotation, screenIndex, annotationRefIndex);
	}

	public void setScreenDescription(String description, int screenIndex)
	{
    meta.setScreenDescription(description, screenIndex);
	}

	public void setScreenID(String id, int screenIndex)
	{
    meta.setScreenID(id, screenIndex);
	}

	public void setScreenName(String name, int screenIndex)
	{
    meta.setScreenName(name, screenIndex);
	}

	public void setScreenPlateRef(String plate, int screenIndex, int plateRefIndex)
	{
    meta.setScreenPlateRef(plate, screenIndex, plateRefIndex);
	}

	public void setScreenProtocolDescription(String protocolDescription, int screenIndex)
	{
    meta.setScreenProtocolDescription(protocolDescription, screenIndex);
	}

	public void setScreenProtocolIdentifier(String protocolIdentifier, int screenIndex)
	{
    meta.setScreenProtocolIdentifier(protocolIdentifier, screenIndex);
	}

	// Ignoring Reagent element, complex property
	public void setScreenReagentSetDescription(String reagentSetDescription, int screenIndex)
	{
    meta.setScreenReagentSetDescription(reagentSetDescription, screenIndex);
	}

	public void setScreenReagentSetIdentifier(String reagentSetIdentifier, int screenIndex)
	{
    meta.setScreenReagentSetIdentifier(reagentSetIdentifier, screenIndex);
	}

	public void setScreenType(String type, int screenIndex)
	{
    meta.setScreenType(type, screenIndex);
	}

	//
	// StageLabel property storage
	//
	// {u'Image': {u'OME': None}}
	// Is multi path? False

	public void setStageLabelName(String name, int imageIndex)
	{
    meta.setStageLabelName(name, imageIndex);
	}

	public void setStageLabelX(Double x, int imageIndex)
	{
    meta.setStageLabelX(x, imageIndex);
	}

	public void setStageLabelY(Double y, int imageIndex)
	{
    meta.setStageLabelY(y, imageIndex);
	}

	public void setStageLabelZ(Double z, int imageIndex)
	{
    meta.setStageLabelZ(z, imageIndex);
	}

	//
	// StructuredAnnotations property storage
	//
	// {u'OME': None}
	// Is multi path? False

	// Ignoring BooleanAnnotation element, complex property
	// Ignoring CommentAnnotation element, complex property
	// Ignoring DoubleAnnotation element, complex property
	// Ignoring FileAnnotation element, complex property
	// Ignoring ListAnnotation element, complex property
	// Ignoring LongAnnotation element, complex property
	// Ignoring TagAnnotation element, complex property
	// Ignoring TermAnnotation element, complex property
	// Ignoring TimestampAnnotation element, complex property
	// Ignoring XMLAnnotation element, complex property
	//
	// TagAnnotation property storage
	//
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public void setTagAnnotationAnnotationRef(String annotation, int tagAnnotationIndex, int annotationRefIndex)
	{
    meta.setTagAnnotationAnnotationRef(annotation, tagAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public void setTagAnnotationDescription(String description, int tagAnnotationIndex)
	{
    meta.setTagAnnotationDescription(description, tagAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public void setTagAnnotationID(String id, int tagAnnotationIndex)
	{
    meta.setTagAnnotationID(id, tagAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setTagAnnotationNamespace(String namespace, int tagAnnotationIndex)
	{
    meta.setTagAnnotationNamespace(namespace, tagAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public void setTagAnnotationValue(String value, int tagAnnotationIndex)
	{
    meta.setTagAnnotationValue(value, tagAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// TermAnnotation property storage
	//
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public void setTermAnnotationAnnotationRef(String annotation, int termAnnotationIndex, int annotationRefIndex)
	{
    meta.setTermAnnotationAnnotationRef(annotation, termAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public void setTermAnnotationDescription(String description, int termAnnotationIndex)
	{
    meta.setTermAnnotationDescription(description, termAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public void setTermAnnotationID(String id, int termAnnotationIndex)
	{
    meta.setTermAnnotationID(id, termAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setTermAnnotationNamespace(String namespace, int termAnnotationIndex)
	{
    meta.setTermAnnotationNamespace(namespace, termAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public void setTermAnnotationValue(String value, int termAnnotationIndex)
	{
    meta.setTermAnnotationValue(value, termAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// TiffData property storage
	//
	// {u'Pixels': {u'Image': {u'OME': None}}}
	// Is multi path? False

	public void setTiffDataFirstC(NonNegativeInteger firstC, int imageIndex, int tiffDataIndex)
	{
    meta.setTiffDataFirstC(firstC, imageIndex, tiffDataIndex);
	}

	public void setTiffDataFirstT(NonNegativeInteger firstT, int imageIndex, int tiffDataIndex)
	{
    meta.setTiffDataFirstT(firstT, imageIndex, tiffDataIndex);
	}

	public void setTiffDataFirstZ(NonNegativeInteger firstZ, int imageIndex, int tiffDataIndex)
	{
    meta.setTiffDataFirstZ(firstZ, imageIndex, tiffDataIndex);
	}

	public void setTiffDataIFD(NonNegativeInteger ifd, int imageIndex, int tiffDataIndex)
	{
    meta.setTiffDataIFD(ifd, imageIndex, tiffDataIndex);
	}

	// Ignoring Pixels_BackReference back reference
	public void setTiffDataPlaneCount(NonNegativeInteger planeCount, int imageIndex, int tiffDataIndex)
	{
    meta.setTiffDataPlaneCount(planeCount, imageIndex, tiffDataIndex);
	}

	// Ignoring UUID element, complex property
	//
	// TimestampAnnotation property storage
	//
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public void setTimestampAnnotationAnnotationRef(String annotation, int timestampAnnotationIndex, int annotationRefIndex)
	{
    meta.setTimestampAnnotationAnnotationRef(annotation, timestampAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public void setTimestampAnnotationDescription(String description, int timestampAnnotationIndex)
	{
    meta.setTimestampAnnotationDescription(description, timestampAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public void setTimestampAnnotationID(String id, int timestampAnnotationIndex)
	{
    meta.setTimestampAnnotationID(id, timestampAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setTimestampAnnotationNamespace(String namespace, int timestampAnnotationIndex)
	{
    meta.setTimestampAnnotationNamespace(namespace, timestampAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public void setTimestampAnnotationValue(Timestamp value, int timestampAnnotationIndex)
	{
    meta.setTimestampAnnotationValue(value, timestampAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
	//
	// TransmittanceRange property storage
	//
	// {u'Filter': {u'Instrument': {u'OME': None}}}
	// Is multi path? False

	public void setTransmittanceRangeCutIn(PositiveInteger cutIn, int instrumentIndex, int filterIndex)
	{
    meta.setTransmittanceRangeCutIn(cutIn, instrumentIndex, filterIndex);
	}

	public void setTransmittanceRangeCutInTolerance(NonNegativeInteger cutInTolerance, int instrumentIndex, int filterIndex)
	{
    meta.setTransmittanceRangeCutInTolerance(cutInTolerance, instrumentIndex, filterIndex);
	}

	public void setTransmittanceRangeCutOut(PositiveInteger cutOut, int instrumentIndex, int filterIndex)
	{
    meta.setTransmittanceRangeCutOut(cutOut, instrumentIndex, filterIndex);
	}

	public void setTransmittanceRangeCutOutTolerance(NonNegativeInteger cutOutTolerance, int instrumentIndex, int filterIndex)
	{
    meta.setTransmittanceRangeCutOutTolerance(cutOutTolerance, instrumentIndex, filterIndex);
	}

	public void setTransmittanceRangeTransmittance(PercentFraction transmittance, int instrumentIndex, int filterIndex)
	{
    meta.setTransmittanceRangeTransmittance(transmittance, instrumentIndex, filterIndex);
	}

	//
	// UUID property storage
	//
	// {u'TiffData': {u'Pixels': {u'Image': {u'OME': None}}}}
	// Is multi path? False

	public void setUUIDFileName(String fileName, int imageIndex, int tiffDataIndex)
	{
    meta.setUUIDFileName(fileName, imageIndex, tiffDataIndex);
	}

	//
	// Union property storage
	//
	// {u'ROI': {u'OME': None}}
	// Is multi path? False

	// Ignoring Shape element, complex property
	//
	// Well property storage
	//
	// {u'Plate': {u'OME': None}}
	// Is multi path? False

	public void setWellAnnotationRef(String annotation, int plateIndex, int wellIndex, int annotationRefIndex)
	{
    meta.setWellAnnotationRef(annotation, plateIndex, wellIndex, annotationRefIndex);
	}

	public void setWellColor(Color color, int plateIndex, int wellIndex)
	{
    meta.setWellColor(color, plateIndex, wellIndex);
	}

	public void setWellColumn(NonNegativeInteger column, int plateIndex, int wellIndex)
	{
    meta.setWellColumn(column, plateIndex, wellIndex);
	}

	public void setWellExternalDescription(String externalDescription, int plateIndex, int wellIndex)
	{
    meta.setWellExternalDescription(externalDescription, plateIndex, wellIndex);
	}

	public void setWellExternalIdentifier(String externalIdentifier, int plateIndex, int wellIndex)
	{
    meta.setWellExternalIdentifier(externalIdentifier, plateIndex, wellIndex);
	}

	public void setWellID(String id, int plateIndex, int wellIndex)
	{
    meta.setWellID(id, plateIndex, wellIndex);
	}

	// Ignoring Plate_BackReference back reference
	public void setWellReagentRef(String reagent, int plateIndex, int wellIndex)
	{
    meta.setWellReagentRef(reagent, plateIndex, wellIndex);
	}

	public void setWellRow(NonNegativeInteger row, int plateIndex, int wellIndex)
	{
    meta.setWellRow(row, plateIndex, wellIndex);
	}

	public void setWellType(String type, int plateIndex, int wellIndex)
	{
    meta.setWellType(type, plateIndex, wellIndex);
	}

	// Ignoring WellSample element, complex property
	//
	// WellSample property storage
	//
	// {u'Well': {u'Plate': {u'OME': None}}}
	// Is multi path? False

	public void setWellSampleAnnotationRef(String annotation, int plateIndex, int wellIndex, int wellSampleIndex, int annotationRefIndex)
	{
    meta.setWellSampleAnnotationRef(annotation, plateIndex, wellIndex, wellSampleIndex, annotationRefIndex);
	}

	public void setWellSampleID(String id, int plateIndex, int wellIndex, int wellSampleIndex)
	{
    meta.setWellSampleID(id, plateIndex, wellIndex, wellSampleIndex);
	}

	public void setWellSampleImageRef(String image, int plateIndex, int wellIndex, int wellSampleIndex)
	{
    meta.setWellSampleImageRef(image, plateIndex, wellIndex, wellSampleIndex);
	}

	public void setWellSampleIndex(NonNegativeInteger index, int plateIndex, int wellIndex, int wellSampleIndex)
	{
    meta.setWellSampleIndex(index, plateIndex, wellIndex, wellSampleIndex);
	}

	// Ignoring PlateAcquisition_BackReference back reference
	public void setWellSamplePositionX(Double positionX, int plateIndex, int wellIndex, int wellSampleIndex)
	{
    meta.setWellSamplePositionX(positionX, plateIndex, wellIndex, wellSampleIndex);
	}

	public void setWellSamplePositionY(Double positionY, int plateIndex, int wellIndex, int wellSampleIndex)
	{
    meta.setWellSamplePositionY(positionY, plateIndex, wellIndex, wellSampleIndex);
	}

	public void setWellSampleTimepoint(Timestamp timepoint, int plateIndex, int wellIndex, int wellSampleIndex)
	{
    meta.setWellSampleTimepoint(timepoint, plateIndex, wellIndex, wellSampleIndex);
	}

	// Ignoring Well_BackReference back reference
	//
	// WellSampleRef property storage
	//
	// {u'PlateAcquisition': {u'Plate': {u'OME': None}}}
	// Is multi path? False

	// 1:1
	// Is multi path? False
	// Ignoring ID property of reference WellSampleRef

	//
	// XMLAnnotation property storage
	//
	// {u'StructuredAnnotations': {u'OME': None}}
	// Is multi path? False

	public void setXMLAnnotationAnnotationRef(String annotation, int XMLAnnotationIndex, int annotationRefIndex)
	{
    meta.setXMLAnnotationAnnotationRef(annotation, XMLAnnotationIndex, annotationRefIndex);
	}

	// Ignoring Channel_BackReference back reference
	// Ignoring Dataset_BackReference back reference
	public void setXMLAnnotationDescription(String description, int XMLAnnotationIndex)
	{
    meta.setXMLAnnotationDescription(description, XMLAnnotationIndex);
	}

	// Ignoring ExperimenterGroup_BackReference back reference
	// Ignoring Experimenter_BackReference back reference
	public void setXMLAnnotationID(String id, int XMLAnnotationIndex)
	{
    meta.setXMLAnnotationID(id, XMLAnnotationIndex);
	}

	// Ignoring Image_BackReference back reference
	public void setXMLAnnotationNamespace(String namespace, int XMLAnnotationIndex)
	{
    meta.setXMLAnnotationNamespace(namespace, XMLAnnotationIndex);
	}

	// Ignoring Pixels_BackReference back reference
	// Ignoring Plane_BackReference back reference
	// Ignoring PlateAcquisition_BackReference back reference
	// Ignoring Plate_BackReference back reference
	// Ignoring Project_BackReference back reference
	// Ignoring ROI_BackReference back reference
	// Ignoring Reagent_BackReference back reference
	// Ignoring Screen_BackReference back reference
	// Ignoring StructuredAnnotations_BackReference back reference
	public void setXMLAnnotationValue(String value, int XMLAnnotationIndex)
	{
    meta.setXMLAnnotationValue(value, XMLAnnotationIndex);
	}

	// Ignoring WellSample_BackReference back reference
	// Ignoring Well_BackReference back reference
}
