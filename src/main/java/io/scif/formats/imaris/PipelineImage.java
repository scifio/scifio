package io.scif.formats.imaris;

/**
 *
 * @author henrypinkard
 */
public class PipelineImage {
   
   public int channel, slice, frame;
   public Object pixels;
   public long[][] histograms;
   public String dateAndtime;
   
   public PipelineImage(Object pix, int chnl, int slce, int frm, String dnt) {
      channel = chnl;
      slice = slce;
      frame = frm;
      pixels = pix;
      dateAndtime = dnt;
   }
}
