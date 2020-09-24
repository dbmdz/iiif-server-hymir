package de.digitalcollections.iiif.hymir.presentation.backend;

import com.github.dbmdz.pathfinder.Pathfinder;
import de.digitalcollections.commons.springmvc.exceptions.ResourceNotFoundException;
import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.presentation.backend.api.PresentationRepository;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.sharedcanvas.AnnotationList;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Default implementation trying to get manifest.json from an resolved URI as String and returning
 * Manifest instance.
 */
@Repository
public class PresentationRepositoryImpl implements PresentationRepository {

  private static final String COLLECTION_PREFIX = "collection-";
  private static final Logger LOGGER = LoggerFactory.getLogger(PresentationRepositoryImpl.class);

  private final IiifObjectMapper objectMapper;
  private final Pathfinder pathfinder;

  @Autowired
  public PresentationRepositoryImpl(IiifObjectMapper objectMapper, Pathfinder pathfinder) {
    this.objectMapper = objectMapper;
    this.pathfinder = pathfinder;
  }

  @Override
  public AnnotationList getAnnotationList(String identifier, String name, String canvasId)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    String annotationListName = name + "-" + identifier + "_" + canvasId;
    Path path =
        pathfinder
            .findExisting("iiifAnno:" + annotationListName)
            .orElseThrow(
                () -> new ResolvingException("No annotation list for name " + annotationListName));
    try {
      return objectMapper.readValue(Files.newInputStream(path), AnnotationList.class);
    } catch (IOException ex) {
      throw new InvalidDataException(
          "Annotation list " + annotationListName + " can not be parsed", ex);
    }
  }

  @Override
  public Collection getCollection(String name)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    // to get a regex resolvable pattern we add a static prefix for collections
    String collectionName = COLLECTION_PREFIX + name;
    Path path =
        pathfinder
            .findExisting("collection:" + collectionName)
            .orElseThrow(() -> new ResolvingException("No collection for name " + name));
    try {
      return objectMapper.readValue(Files.newInputStream(path), Collection.class);
    } catch (IOException ex) {
      throw new InvalidDataException(
          "Collection for name " + collectionName + " can not be parsed", ex);
    }
  }

  @Override
  public Manifest getManifest(String identifier)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    Path path =
        pathfinder
            .findExisting("manifest:" + identifier)
            .orElseThrow(() -> new ResolvingException("No manifest for identifier " + identifier));
    try {
      return objectMapper.readValue(Files.newInputStream(path), Manifest.class);
    } catch (IOException ex) {
      throw new InvalidDataException("Manifest " + identifier + " can not be parsed", ex);
    }
  }

  @Override
  public Instant getManifestModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    try {
      return Files.getLastModifiedTime(
              pathfinder
                  .findExisting("manifest:" + identifier)
                  .orElseThrow(ResolvingException::new))
          .toInstant();
    } catch (IOException e) {
      throw new ResolvingException("Failed to resolve '" + identifier + "' to a readable file");
    }
  }

  @Override
  public Instant getCollectionModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    try {
      return Files.getLastModifiedTime(
              pathfinder
                  .findExisting("collection:" + identifier)
                  .orElseThrow(ResolvingException::new))
          .toInstant();
    } catch (IOException e) {
      throw new ResolvingException("Failed to resolve '" + identifier + "' to a readable file");
    }
  }
}
