package de.digitalcollections.iiif.hymir.presentation.backend.api.repository.v2;

import de.digitalcollections.iiif.hymir.model.api.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.net.URI;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * Interface to be implemented by project/user of this library.
 */
public interface PresentationRepository {

  /**
   * @param name unique name of collection
   * @return Collection specified by name
   * @throws ResolvingException in case Collection does not exist or can not be delivered
   * @throws InvalidDataException if manifest contains invalid data
   */
  public Collection getCollection(String name) throws ResolvingException, InvalidDataException;

  public Collection getCollection(URI collectionUri) throws ResolvingException, InvalidDataException;

  /**
   * @param identifier unique id for IIIF resource
   * @return Manifest specifying presentation for IIIF resource
   * @throws ResolvingException in case Manifest does not exist or can not be delivered
   * @throws InvalidDataException if manifest contains invalid data
   */
  Manifest getManifest(String identifier) throws ResolvingException, InvalidDataException;

  public Manifest getManifest(URI manifestUri) throws ResolvingException, InvalidDataException;

  public String getResourceJson(URI resourceUri) throws ResolvingException;

  public JSONObject getResourceAsJsonObject(URI resourceUri) throws ResolvingException, ParseException;

  public JSONObject getResourceAsJsonObject(String resourceUri) throws ResolvingException, ParseException;
}
