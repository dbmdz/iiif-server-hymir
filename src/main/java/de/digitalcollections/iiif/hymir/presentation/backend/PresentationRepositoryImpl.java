package de.digitalcollections.iiif.hymir.presentation.backend;

import de.digitalcollections.commons.file.business.impl.resolved.ResolvedFileResourceServiceImpl;
import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.presentation.backend.api.PresentationRepository;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.sharedcanvas.AnnotationList;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import org.apache.commons.io.IOUtils;
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

  @Autowired private IiifObjectMapper objectMapper;

  @Autowired private ResolvedFileResourceServiceImpl resourceService;

  @Override
  public AnnotationList getAnnotationList(String identifier, String name, String canvasId)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    String annotationListName = name + "-" + identifier + "_" + canvasId;
    FileResource resource;
    try {
      resource = resourceService.find(annotationListName, MimeType.MIME_APPLICATION_JSON);
    } catch (ResourceIOException ex) {
      LOGGER.error("Error getting annotation list for name {}", annotationListName, ex);
      throw new ResolvingException("No annotation list for name " + annotationListName);
    }
    try {
      return objectMapper.readValue(getResourceJson(resource.getUri()), AnnotationList.class);
    } catch (IOException ex) {
      LOGGER.error("Could not retrieve annotation list {}", annotationListName, ex);
      throw new InvalidDataException(
          "Annotation list " + annotationListName + " can not be parsed", ex);
    }
  }

  @Override
  public Collection getCollection(String name)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    // to get a regex resolvable pattern we add a static prefix for collections
    String collectionName = COLLECTION_PREFIX + name;
    FileResource resource;
    try {
      resource = resourceService.find(collectionName, MimeType.MIME_APPLICATION_JSON);
    } catch (ResourceIOException ex) {
      LOGGER.error("Error getting manifest for collection {}", name, ex);
      throw new ResolvingException("No collection for name " + name);
    }
    try {
      return objectMapper.readValue(getResourceJson(resource.getUri()), Collection.class);
    } catch (IOException ex) {
      LOGGER.info("Could not retrieve collection {}", collectionName, ex);
      throw new InvalidDataException(
          "Collection for name " + collectionName + " can not be parsed", ex);
    }
  }

  @Override
  public Manifest getManifest(String identifier)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    FileResource resource;
    try {
      resource = resourceService.find(identifier, MimeType.MIME_APPLICATION_JSON);
    } catch (ResourceIOException ex) {
      LOGGER.error("Error getting manifest for identifier {}", identifier, ex);
      throw new ResolvingException("No manifest for identifier " + identifier);
    }
    try {
      return objectMapper.readValue(getResourceJson(resource.getUri()), Manifest.class);
    } catch (IOException ex) {
      LOGGER.error("Manifest {} can not be parsed", identifier, ex);
      throw new InvalidDataException("Manifest " + identifier + " can not be parsed", ex);
    }
  }

  @Override
  public Instant getManifestModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    return getResourceModificationDate(identifier);
  }

  @Override
  public Instant getCollectionModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    return getResourceModificationDate(identifier);
  }

  private Instant getResourceModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    try {
      FileResource resource = resourceService.find(identifier, MimeType.MIME_APPLICATION_JSON);
      return resource.getLastModified().toInstant(ZoneOffset.UTC);
    } catch (ResourceIOException ex) {
      LOGGER.error(
          "Error getting resource for identifier '{}', message '{}'", identifier, ex.getMessage());
      throw new ResolvingException("No manifest for identifier " + identifier);
    }
  }

  protected String getResourceJson(URI resourceUri)
      throws ResolvingException, ResourceNotFoundException {
    try (InputStream is = resourceService.getInputStream(resourceUri)) {
      return IOUtils.toString(is, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new ResolvingException(e);
    }
  }
}
