package de.digitalcollections.iiif.hymir.presentation.business.api.v2;

import de.digitalcollections.iiif.hymir.model.api.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.model.sharedcanvas.Canvas;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Range;
import de.digitalcollections.iiif.model.sharedcanvas.Sequence;
import java.net.URI;

/**
 * Service for IIIF Presentation API functionality.
 */
public interface PresentationService {

  /**
   * @param name unique name of collection
   * @return Collection specified by name
   * @throws ResolvingException if no collection found or access disallowed
   * @throws InvalidDataException if data is corrupted
   */
  Collection getCollection(String name) throws ResolvingException, InvalidDataException;

  /**
   * @param identifier unique id for IIIF resource
   * @return Manifest specifying presentation for IIIF resource
   * @throws ResolvingException if no manifest found or access disallowed
   * @throws InvalidDataException if data is corrupted
   */
  Manifest getManifest(String identifier) throws ResolvingException, InvalidDataException;

  default Canvas getCanvas(String manifestId, String canvasUri) throws ResolvingException, InvalidDataException {
    return getCanvas(manifestId, URI.create(canvasUri));
  }

  Canvas getCanvas(String manifestId, URI canvasUri) throws ResolvingException, InvalidDataException;

  default Range getRange(String manifestId, String rangeUri) throws ResolvingException, InvalidDataException {
    return getRange(manifestId, URI.create(rangeUri));
  }

  Range getRange(String manifestId, URI rangeUri) throws ResolvingException, InvalidDataException;

  default Sequence getSequence(String manifestId, String sequenceUri) throws ResolvingException, InvalidDataException {
    return getSequence(manifestId, URI.create(sequenceUri));
  }

  Sequence getSequence(String manifestId, URI sequenceUri) throws ResolvingException, InvalidDataException;
}
