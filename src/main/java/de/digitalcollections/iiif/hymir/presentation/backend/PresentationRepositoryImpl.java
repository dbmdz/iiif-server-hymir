package de.digitalcollections.iiif.hymir.presentation.backend;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.digitalcollections.core.business.api.ResourceService;
import de.digitalcollections.core.model.api.MimeType;
import de.digitalcollections.core.model.api.resource.Resource;
import de.digitalcollections.core.model.api.resource.enums.ResourcePersistenceType;
import de.digitalcollections.core.model.api.resource.exceptions.ResourceIOException;
import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.presentation.backend.api.PresentationRepository;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Default implementation trying to get manifest.json from an resolved URI as String and returning Manifest instance.
 */
@Repository
public class PresentationRepositoryImpl implements PresentationRepository {

  private static final String COLLECTION_PREFIX = "collection-";
  private static final Logger LOGGER = LoggerFactory.getLogger(PresentationRepositoryImpl.class);

  private final Cache<String, Object> httpCache;

  @Autowired
  private IiifObjectMapper objectMapper;

  @Autowired
  private ResourceService resourceService;

  public PresentationRepositoryImpl() {
    httpCache = CacheBuilder.newBuilder().maximumSize(32).build();
  }

  @Override
  public Collection getCollection(String name) throws ResolvingException, InvalidDataException {
    // to get a regex resolable pattern we add a static prefix for collections
    String collectionName = COLLECTION_PREFIX + name;

    Resource resource;
    try {
      resource = resourceService.get(collectionName, ResourcePersistenceType.REFERENCED, MimeType.MIME_APPLICATION_JSON);
    } catch (ResourceIOException ex) {
      LOGGER.warn("Error getting collection for name " + collectionName, ex);
      throw new ResolvingException("No collection for name " + collectionName);
    }
    URI uri = resource.getUri();
    return getCollection(uri);
  }

  protected Collection getCollection(URI collectionUri) throws ResolvingException, InvalidDataException {
    String location = collectionUri.toString();
    LOGGER.debug("Trying to get collection from " + location);

    Collection collection;
    String scheme = collectionUri.getScheme();

    // use caching for remote/http resources
    if ("http".equals(scheme)) {
      collection = (Collection) httpCache.getIfPresent(location);
      if (collection != null) {
        LOGGER.debug("HTTP Cache hit for collection " + location);
        return collection;
      }
    }

    String collectionJson = getResourceJson(collectionUri);
    try {
      collection = objectMapper.readValue(collectionJson, Collection.class);
    } catch (IOException e) {
      throw new InvalidDataException("Error reading from JSON", e);
    }

    if ("http".equals(scheme)) {
      httpCache.put(location, collection);
    }

    return collection;
  }

  @Override
  public Manifest getManifest(String identifier) throws ResolvingException, InvalidDataException {
    Resource resource;
    try {
      resource = resourceService.get(identifier, ResourcePersistenceType.REFERENCED, MimeType.MIME_APPLICATION_JSON);
    } catch (ResourceIOException ex) {
      LOGGER.warn("Error getting manifest for identifier " + identifier, ex);
      throw new ResolvingException("No manifest for identifier " + identifier);
    }
    URI uri = resource.getUri();
    return getManifest(uri);
  }

  protected Manifest getManifest(URI manifestUri) throws ResolvingException, InvalidDataException {
    String location = manifestUri.toString();
    LOGGER.debug("Trying to get manifest from " + location);

    Manifest manifest;
    String scheme = manifestUri.getScheme();

    // use caching for remote/http resources
    if ("http".equals(scheme)) {
      manifest = (Manifest) httpCache.getIfPresent(location);
      if (manifest != null) {
        LOGGER.debug("HTTP Cache hit for manifest " + location);
        return manifest;
      }
    }

    String manifestJson = getResourceJson(manifestUri);
    try {
      manifest = objectMapper.readValue(manifestJson, Manifest.class);
    } catch (IOException e) {
      throw new InvalidDataException("Could not read manifest JSON", e);
    }

    if ("http".equals(scheme)) {
      httpCache.put(location, manifest);
    }

    return manifest;
  }

  protected String getResourceJson(URI resourceUri) throws ResolvingException {
    InputStream inputStream;
    try (InputStream is = resourceService.getInputStream(resourceUri)) {
      return IOUtils.toString(is, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new ResolvingException(e);
    }
  }

  private JSONObject getResourceAsJsonObject(URI resourceUri) throws ResolvingException, ParseException {
    String json = getResourceJson(resourceUri);
    JSONParser parser = new JSONParser();
    Object obj = parser.parse(json);
    return (JSONObject) obj;
  }

  private JSONObject getResourceAsJsonObject(String resourceUri) throws ResolvingException, ParseException {
    return this.getResourceAsJsonObject(URI.create(resourceUri));
  }
}
