
package io.scif.filters;

import io.scif.ImageMetadata;

public class FileStitcherMetadata extends AbstractMetadataWrapper {

	private ImageMetadata imgMeta;

	@Override
	public Class<? extends Filter> filterType() {
		return FileStitcher.class;
	}

	@Override
	public int getImageCount() {
		return 1;
	}

	@Override
	public ImageMetadata get(final int imageIndex) {
		if (imageIndex != 0) {
			throw new IllegalArgumentException(
				"Filestitcher merges the files to one large Image!");
		}
		return imgMeta;
	}

	public void setImgMeta(final ImageMetadata imgMeta) {
		this.imgMeta = imgMeta;
	}

	@Override
	public long getDatasetSize() {
		return imgMeta.getSize();
	}
}
