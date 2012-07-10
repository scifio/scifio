package ome.scifio.ics;

import java.io.IOException;

import ome.scifio.io.Location;
import ome.scifio.AbstractParser;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.io.RandomAccessInputStream;

/**
 * SCIFIO file format Parser for ICS images. 
 *
 */
public class ICSParser extends AbstractParser<ICSMetadata> {

  // -- Constructor --

  public ICSParser() {
    this(null);
  }

  public ICSParser(final SCIFIO ctx) {
    super(ctx, ICSMetadata.class);
  }

  // -- Parser API Methods --

  /* @see Parser#parse(RandomAccessInputStream) */
  @Override
  public ICSMetadata parse(final RandomAccessInputStream stream)
    throws IOException, FormatException
  {
    super.parse(stream);

    if (stream.getFileName() != null) findCompanion(stream.getFileName());

    final RandomAccessInputStream reader =
      new RandomAccessInputStream(metadata.getIcsId());

    reader.seek(0);
    reader.readString(ICSUtils.NL);
    String line = reader.readString(ICSUtils.NL);

    // Extracts the keys, value pairs from each line and inserts them into the ICSMetadata object
    while (line != null && !line.trim().equals("end") &&
      reader.getFilePointer() < reader.length() - 1)
    {
      line = line.trim();
      final String[] tokens = line.split("[ \t]");
      final StringBuffer key = new StringBuffer();
      for (int q = 0; q < tokens.length; q++) {
        tokens[q] = tokens[q].trim();
        if (tokens[q].length() == 0) continue;

        boolean foundValue = true;
        for (int i = 0; i < ICSUtils.CATEGORIES.length; i++) {
          if (tokens[q].matches(ICSUtils.CATEGORIES[i])) {
            foundValue = false;
            break;
          }
        }
        if (!foundValue) {
          key.append(tokens[q]);
          key.append(" ");
          continue;
        }

        final StringBuffer value = new StringBuffer(tokens[q++]);
        for (; q < tokens.length; q++) {
          value.append(" ");
          value.append(tokens[q].trim());
        }
        final String k = key.toString().trim().replaceAll("\t", " ");
        final String v = value.toString().trim();
        addGlobalMeta(k, v);
        metadata.keyValPairs.put(k.toLowerCase(), v);
      }
      line = reader.readString(ICSUtils.NL);
    }
    reader.close();

    if (metadata.versionTwo) {
      String s = in.readString(ICSUtils.NL);
      while (!s.trim().equals("end"))
        s = in.readString(ICSUtils.NL);
    }

    metadata.offset = in.getFilePointer();

    metadata.hasInstrumentData =
      nullKeyCheck(new String[] {
          "history cube emm nm", "history cube exc nm", "history objective NA",
          "history stage xyzum", "history objective magnification",
          "history objective mag", "history objective WorkingDistance",
          "history objective type", "history objective",
          "history objective immersion"});

    return metadata;
  }

  // -- Helper Methods --

  /* Returns true if any of the keys in testKeys has a non-null value */
  private boolean nullKeyCheck(final String[] testKeys) {
    for (final String key : testKeys) {
      if (metadata.get(key) != null) {
        return true;
      }
    }
    return false;
  }

  /* Finds the companion file (ICS and IDS are companions) */
  private void findCompanion(final String id)
    throws IOException, FormatException
  {
    metadata.icsId = id;
    metadata.idsId = id;
    String icsId = id, idsId = id;
    final int dot = id.lastIndexOf(".");
    final String ext = dot < 0 ? "" : id.substring(dot + 1).toLowerCase();
    if (ext.equals("ics")) {
      // convert C to D regardless of case
      final char[] c = idsId.toCharArray();
      c[c.length - 2]++;
      idsId = new String(c);
    }
    else if (ext.equals("ids")) {
      // convert D to C regardless of case
      final char[] c = icsId.toCharArray();
      c[c.length - 2]--;
      icsId = new String(c);
    }

    if (icsId == null) throw new FormatException("No ICS file found.");
    final Location icsFile = new Location(icsId);
    if (!icsFile.exists()) throw new FormatException("ICS file not found.");

    // check if we have a v2 ICS file - means there is no companion IDS file
    final RandomAccessInputStream f = new RandomAccessInputStream(icsId);
    if (f.readString(17).trim().equals("ics_version\t2.0")) {
      in = new RandomAccessInputStream(icsId);
      metadata.versionTwo = true;
    }
    else {
      if (idsId == null) throw new FormatException("No IDS file found.");
      final Location idsFile = new Location(idsId);
      if (!idsFile.exists()) throw new FormatException("IDS file not found.");
      //currentIdsId = idsId;
      metadata.idsId = idsId;
      in = new RandomAccessInputStream(idsId);
    }
    f.close();
  }
}
