package de.digitalcollections.iiif.hymir.presentation.business.impl.service.v2;

import de.digitalcollections.iiif.hymir.model.api.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.presentation.backend.api.repository.v2.PresentationRepository;
import de.digitalcollections.iiif.hymir.presentation.business.api.PresentationSecurityService;
import de.digitalcollections.iiif.hymir.presentation.business.api.v2.PresentationService;
import de.digitalcollections.iiif.model.sharedcanvas.Canvas;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Range;
import de.digitalcollections.iiif.model.sharedcanvas.Sequence;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "PresentationServiceImpl-v2.0.0")
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

  @Override
  public Canvas getCanvas(String manifestId, URI canvasUri) throws ResolvingException, InvalidDataException {
    Manifest manifest = getManifest(manifestId);
    return manifest.getSequences().stream()
            .flatMap(seq -> seq.getCanvases().stream())
            .filter(canv -> canv.getIdentifier().equals(canvasUri))
            .findFirst().orElseThrow(ResolvingException::new);
  }

  @Override
  public Range getRange(String manifestId, URI rangeUri) throws ResolvingException, InvalidDataException {
    Manifest manifest = getManifest(manifestId);
    return manifest.getRanges().stream()
            .filter(range -> range.getIdentifier().equals(rangeUri))
            .findFirst().orElseThrow(ResolvingException::new);
  }

  @Override
  public Sequence getSequence(String manifestId, URI sequenceUri) throws ResolvingException, InvalidDataException {
    Manifest manifest = getManifest(manifestId);
    return manifest.getSequences().stream()
            .filter(seq -> seq.getIdentifier().equals(sequenceUri))
            .findFirst().orElseThrow(ResolvingException::new);
  }
}
