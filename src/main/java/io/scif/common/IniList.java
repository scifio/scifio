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

package io.scif.common;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A data structure containing a parsed list of INI key/value tables.
 *
 * @author Curtis Rueden
 */
public class IniList extends ArrayList<IniTable> {

	// -- IniList methods --

	/** Gets the table with the given name (header). */
	public IniTable getTable(final String tableName) {
		for (final IniTable table : this) {
			final String header = table.get(IniTable.HEADER_KEY);
			if (tableName.equals(header)) return table;
		}
		return null;
	}

	/**
	 * Flattens all of the INI tables into a single HashMap whose keys are of the
	 * format "[table name] table key".
	 */
	public HashMap<String, String> flattenIntoHashMap() {
		final HashMap<String, String> h = new HashMap<>();
		for (final IniTable table : this) {
			final String tableName = table.get(IniTable.HEADER_KEY);
			for (final String key : table.keySet()) {
				if (!key.equals(IniTable.HEADER_KEY)) {
					h.put("[" + tableName + "] " + key, table.get(key));
				}
			}
		}
		return h;
	}

}
