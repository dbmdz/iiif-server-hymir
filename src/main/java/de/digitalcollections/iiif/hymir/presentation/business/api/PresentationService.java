package de.digitalcollections.iiif.hymir.presentation.business.api;

import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.model.sharedcanvas.AnnotationList;
import de.digitalcollections.iiif.model.sharedcanvas.Canvas;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Range;
import de.digitalcollections.iiif.model.sharedcanvas.Resource;
import de.digitalcollections.iiif.model.sharedcanvas.Sequence;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import java.net.URI;
import java.time.Instant;

/** Service for IIIF Presentation API functionality. */
@Deprecated(forRemoval = true)  // Will be gone with the next major version
public interface PresentationService {

  /**
   * @param identifier unique identifier of the corresponding manifest
   * @param name unique name of annotation list
   * @param canvasId name of the corresponding canvas
   * @return AnnotationList specified by name
   * @throws ResolvingException if no annotation list found
   * @throws ResourceNotFoundException if annotation list with given name can not be found
   * @throws InvalidDataException if data is corrupted
   */
  AnnotationList getAnnotationList(String identifier, String name, String canvasId)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException;

  /**
   * @param name unique name of collection
   * @return Collection specified by name
   * @throws ResolvingException if no collection found or access disallowed
   * @throws ResourceNotFoundException if collection with given name can not be found
   * @throws InvalidDataException if data is corrupted
   */
  Collection getCollection(String name)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException;

  /**
   * @param identifier unique id for IIIF resource
   * @return Manifest specifying presentation for IIIF resource
   * @throws ResolvingException if no manifest found or access disallowed
   * @throws ResourceNotFoundException if Manifest with given identifier can not be found
   * @throws InvalidDataException if data is corrupted
   */
  Manifest getManifest(String identifier)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException;

  default Instant getManifestModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    return Instant.now();
  }

  default Instant getCollectionModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    return Instant.now();
  }

  default Canvas getCanvas(String manifestId, String canvasUri)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    return getCanvas(manifestId, URI.create(canvasUri));
  }

  default Canvas getCanvas(String manifestId, URI canvasUri)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    Manifest manifest = getManifest(manifestId);
    return manifest.getSequences().stream()
        .flatMap(seq -> seq.getCanvases().stream())
        .filter(canv -> canv.getIdentifier().equals(canvasUri))
        .map(canv -> this.copyAttributionInfo(manifest, canv))
        .findFirst()
        .orElseThrow(ResolvingException::new);
  }

  default Range getRange(String manifestId, String rangeUri)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    return getRange(manifestId, URI.create(rangeUri));
  }

  default Range getRange(String manifestId, URI rangeUri)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    Manifest manifest = getManifest(manifestId);
    return manifest.getRanges().stream()
        .filter(r -> r.getIdentifier().equals(rangeUri))
        .map(r -> this.copyAttributionInfo(manifest, r))
        .findFirst()
        .orElseThrow(ResolvingException::new);
  }

  default Sequence getSequence(String manifestId, String sequenceUri)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    return getSequence(manifestId, URI.create(sequenceUri));
  }

  default Sequence getSequence(String manifestId, URI sequenceUri)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    Manifest manifest = getManifest(manifestId);
    return manifest.getSequences().stream()
        .filter(s -> s.getIdentifier().equals(sequenceUri))
        .map(s -> this.copyAttributionInfo(manifest, s))
        .findFirst()
        .orElseThrow(ResolvingException::new);
  }

  default <T extends Resource> T copyAttributionInfo(Manifest manifest, T res) {
    res.setLogos(manifest.getLogos());
    res.setAttribution(manifest.getAttribution());
    res.setLicenses(manifest.getLicenses());
    return res;
  }
}
