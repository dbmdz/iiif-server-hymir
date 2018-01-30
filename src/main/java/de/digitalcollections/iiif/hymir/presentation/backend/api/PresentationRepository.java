package de.digitalcollections.iiif.hymir.presentation.backend.api;

import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.time.Instant;

/**
 * Interface to be implemented by project/user of this library.
 */
public interface PresentationRepository {
  /**
   * @param name unique name of collection
   * @return Collection specified by name
   * @throws ResolvingException in case Collection does not exist or can not be delivered
   * @throws InvalidDataException if collection contains invalid data
   */
  Collection getCollection(String name) throws ResolvingException, InvalidDataException;

  /**
   * @param identifier unique id for IIIF resource
   * @return Manifest specifying presentation for IIIF resource
   * @throws ResolvingException in case Manifest does not exist or can not be delivered
   * @throws InvalidDataException if manifest contains invalid data
   */
  Manifest getManifest(String identifier) throws ResolvingException, InvalidDataException;

  default Instant getManifestModificationDate(String identifier) throws ResolvingException {
    return null;
  }

  default Instant getCollectionModificationDate(String identifier) throws ResolvingException {
    return null;
  }
}
