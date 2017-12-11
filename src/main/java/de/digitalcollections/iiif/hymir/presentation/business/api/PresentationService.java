package de.digitalcollections.iiif.hymir.presentation.business.api;

import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.model.sharedcanvas.Canvas;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Range;
import de.digitalcollections.iiif.model.sharedcanvas.Sequence;
import java.net.URI;
import java.time.Instant;

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

  default Instant getManifestModificationDate(String identifier) throws ResolvingException {
    return null;
  }

  default Instant getCollectionModificationDate(String identifier) throws ResolvingException {
    return null;
  }

  default Canvas getCanvas(String manifestId, String canvasUri) throws ResolvingException, InvalidDataException {
    return getCanvas(manifestId, URI.create(canvasUri));
  }

  default Canvas getCanvas(String manifestId, URI canvasUri) throws ResolvingException, InvalidDataException {
    return getManifest(manifestId).getSequences().stream()
        .flatMap(seq -> seq.getCanvases().stream())
        .filter(canv -> canv.getIdentifier().equals(canvasUri))
        .findFirst().orElseThrow(ResolvingException::new);
  };

  default Range getRange(String manifestId, String rangeUri) throws ResolvingException, InvalidDataException {
    return getRange(manifestId, URI.create(rangeUri));
  }

  default Range getRange(String manifestId, URI rangeUri) throws ResolvingException, InvalidDataException {
    return getManifest(manifestId).getRanges().stream()
        .filter(range -> range.getIdentifier().equals(rangeUri))
        .findFirst().orElseThrow(ResolvingException::new);
  };

  default Sequence getSequence(String manifestId, String sequenceUri) throws ResolvingException, InvalidDataException {
    return getSequence(manifestId, URI.create(sequenceUri));
  }

  default Sequence getSequence(String manifestId, URI sequenceUri) throws ResolvingException, InvalidDataException {
    return getManifest(manifestId).getSequences().stream()
        .filter(seq -> seq.getIdentifier().equals(sequenceUri))
        .findFirst().orElseThrow(ResolvingException::new);

  };
}
