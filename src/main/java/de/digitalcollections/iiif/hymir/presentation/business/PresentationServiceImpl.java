package de.digitalcollections.iiif.hymir.presentation.business;

import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.presentation.backend.api.PresentationRepository;
import de.digitalcollections.iiif.hymir.presentation.business.api.PresentationSecurityService;
import de.digitalcollections.iiif.hymir.presentation.business.api.PresentationService;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PresentationServiceImpl implements PresentationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PresentationServiceImpl.class);

  @Autowired
  private PresentationRepository presentationRepository;

  @Autowired(required = false)
  private PresentationSecurityService presentationSecurityService;

  @Override
  public Collection getCollection(String name) throws ResolvingException, InvalidDataException {
    try {
      return presentationRepository.getCollection(name);
    } catch (ResolvingException ex) {
      LOGGER.debug("Collection for '{}' not found.", name);
      throw ex;
    }
  }

  @Override
  public Manifest getManifest(String identifier) throws ResolvingException, InvalidDataException {
    if (presentationSecurityService != null && !presentationSecurityService.isAccessAllowed(identifier)) {
      LOGGER.info("Access to manifest for object '{}' is not allowed!", identifier);
      throw new ResolvingException(); // TODO maybe throw an explicitely access disallowed exception
    }
    LOGGER.debug("Access to manifest for object '{}' is allowed.", identifier);
    try {
      return presentationRepository.getManifest(identifier);
    } catch (ResolvingException ex) {
      LOGGER.debug("Manifest for '{}' not found.", identifier);
      throw ex;
    }
  }
}
