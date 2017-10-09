package de.digitalcollections.iiif.hymir.image.model.api.enums;

public enum ImageFormat {

  GIF(new String[]{"gif"}, "image/gif"),
  JP2(new String[]{"jp2", "jpeg2000"}, "image/jp2"),
  JPEG(new String[]{"jpg", "jpeg"}, "image/jpeg"),
  PDF(new String[]{"pdf"}, "application/pdf"),
  PNG(new String[]{"png"}, "image/png"),
  TIF(new String[]{"tif", "tiff"}, "image/tif"),
  WEBP(new String[]{"webp"}, "image/webp");

  public static ImageFormat getByExtension(String extension) {
    ImageFormat[] values = ImageFormat.values();
    for (ImageFormat imageFormat : values) {
      final String[] formatExtensions = imageFormat.getExtensions();
      for (String formatExtension : formatExtensions) {
        if (formatExtension.equalsIgnoreCase(extension)) {
          return imageFormat;
        }
      }
    }
    return null;
  }

  private final String[] extensions;

  private final String mimeType;

  private ImageFormat(String[] extensions, String mimeType) {
    this.extensions = extensions;
    this.mimeType = mimeType;
  }

  public String[] getExtensions() {
    return extensions;
  }

  public String getMimeType() {
    return mimeType;
  }

}
