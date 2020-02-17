package de.digitalcollections.iiif.hymir.presentation.backend.api;

import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.model.sharedcanvas.AnnotationList;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceNotFoundException;
import java.time.Instant;

/** Interface to be implemented by project/user of this library. */
public interface PresentationRepository {

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
   * @throws ResolvingException in case Collection does not exist or can not be delivered
   * @throws ResourceNotFoundException if Collection with given name can not be found
   * @throws InvalidDataException if collection contains invalid data
   */
  Collection getCollection(String name)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException;

  /**
   * @param identifier unique id for IIIF resource
   * @return Manifest specifying presentation for IIIF resource
   * @throws ResolvingException in case Manifest does not exist or can not be delivered
   * @throws ResourceNotFoundException if Manifest with given identifier can not be found
   * @throws InvalidDataException if manifest contains invalid data
   */
  Manifest getManifest(String identifier)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException;

  default Instant getManifestModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    return null;
  }

  default Instant getCollectionModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    return null;
  }
}
