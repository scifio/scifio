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
 * Created by temp via xsd-fu on 2012-09-20 09:08:52.526388
 *
 *-----------------------------------------------------------------------------
 */

package loci.formats.meta;

import io.scif.common.DataTools;

import ome.xml.model.*;
import ome.xml.model.enums.*;
import ome.xml.model.primitives.*;

/**
 * A legacy delegator class for ome.xml.meta.FilterMetadata
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/meta/FilterMetadata.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/meta/FilterMetadata.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Mark Hiner hiner at wisc.edu
 */
public class FilterMetadata implements MetadataStore
{
	// -- Fields --

	private ome.xml.meta.FilterMetadata meta;

	// -- Constructor --

	public FilterMetadata(MetadataStore store, boolean filter)
	{
		meta = new ome.xml.meta.FilterMetadata(store, filter);
	}

	// -- MetadataStore API methods --

	/* @see MetadataStore#createRoot() */
	public void createRoot()
	{
		meta.createRoot();
	}

	/* @see MetadataStore#getRoot() */
	public Object getRoot()
	{
		return meta.getRoot();
	}

	/* @see MetadataStore#setRoot(Object) */
	public void setRoot(Object root)
	{
		meta.setRoot(root);
	}

	/* @see MetadataStore#setUUID(String) */
	public void setUUID(String uuid)
	{
		meta.setUUID(uuid);
	}

	// -- AggregateMetadata API methods --

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

	// Element's text data
	// {u'TiffData': [u'int imageIndex', u'int tiffDataIndex']}
	public void setUUIDValue(String value, int imageIndex, int tiffDataIndex)
	{
		meta.setUUIDValue(value, imageIndex, tiffDataIndex);
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
