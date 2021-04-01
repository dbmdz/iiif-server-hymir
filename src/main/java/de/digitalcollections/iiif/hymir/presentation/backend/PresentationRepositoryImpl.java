package de.digitalcollections.iiif.hymir.presentation.backend;

import com.github.dbmdz.pathfinder.Pathfinder;
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
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public PresentationRepositoryImpl(
      IiifObjectMapper objectMapper, Pathfinder pathfinder) {
    this.objectMapper = objectMapper;
    this.pathfinder = pathfinder;
  }


  @Override
  public AnnotationList getAnnotationList(String identifier, String name, String canvasId)
      throws ResolvingException, InvalidDataException {
    String annotationListName = name + "-" + identifier + "_" + canvasId;
    Optional<Path> annoPath = pathfinder.find("anno:" + annotationListName);
    if (annoPath.isEmpty()) {
      LOGGER.error("Error getting annotation list for name {}", annotationListName);
      throw new ResolvingException("No annotation list for name " + annotationListName);
    }
    try {
      return objectMapper.readValue(annoPath.get().toFile(), AnnotationList.class);
    } catch (IOException ex) {
      LOGGER.error("Could not retrieve annotation list {}", annotationListName, ex);
      throw new InvalidDataException(
          "Annotation list " + annotationListName + " can not be parsed", ex);
    }
  }

  @Override
  public Collection getCollection(String name)
      throws ResolvingException, InvalidDataException {
    Optional<Path> collectionPath = pathfinder.find("collection:" + name);
    if (collectionPath.isEmpty()) {
      LOGGER.error("Error getting manifest for collection {}", name);
      throw new ResolvingException("No collection for name " + name);
    }
    try {
      return objectMapper.readValue(collectionPath.get().toFile(), Collection.class);
    } catch (IOException ex) {
      LOGGER.info("Could not retrieve collection {}", name, ex);
      throw new InvalidDataException(
          "Collection for name " + name + " can not be parsed", ex);
    }
  }

  @Override
  public Manifest getManifest(String identifier)
      throws ResolvingException, InvalidDataException {
    Optional<Path> manifestPath = pathfinder.find("manifest:" + identifier);
    if (manifestPath.isEmpty()) {
      LOGGER.error("Error getting manifest for identifier {}", identifier);
      throw new ResolvingException("No manifest for identifier " + identifier);
    }
    try {
      return objectMapper.readValue(manifestPath.get().toFile(), Manifest.class);
    } catch (IOException ex) {
      LOGGER.error("Manifest {} can not be parsed", identifier, ex);
      throw new InvalidDataException("Manifest " + identifier + " can not be parsed", ex);
    }
  }

  @Override
  public Instant getManifestModificationDate(String identifier) throws ResolvingException {
    return getResourceModificationDate("manifest:" + identifier);
  }

  @Override
  public Instant getCollectionModificationDate(String identifier) throws ResolvingException {
    return getResourceModificationDate("collection:" + identifier);
  }

  private Instant getResourceModificationDate(String identifier)
      throws ResolvingException {
    Optional<Path> path = pathfinder.find(identifier);
    if (path.isEmpty()) {
      LOGGER.error("Could not find resource with identifier '{}'", identifier);
      throw new ResolvingException("No resource for identifier " + identifier);
    }
    try {
      return Files.getLastModifiedTime(path.get()).toInstant();
    } catch (IOException e) {
      LOGGER.error("Error getting resource for identifier '{}', message '{}'", identifier);
      throw new ResolvingException("No resource for identifier " + identifier);
    }
  }
}
