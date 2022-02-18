package de.digitalcollections.iiif.hymir.presentation.business;

import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException;
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException;
import de.digitalcollections.iiif.hymir.presentation.backend.api.PresentationRepository;
import de.digitalcollections.iiif.hymir.presentation.business.api.PresentationSecurityService;
import de.digitalcollections.iiif.hymir.presentation.business.api.PresentationService;
import de.digitalcollections.iiif.model.sharedcanvas.AnnotationList;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Deprecated(forRemoval = true)  // Will be gone with the next major version
public class PresentationServiceImpl implements PresentationService {

  private final PresentationRepository presentationRepository;

  private final PresentationSecurityService presentationSecurityService;

  // FIXME: Yes, this is incredibly nasty and violates "separation of concerns", but it's the
  //        only way to implement ACL based on user-supplied data without refactoring a significant
  //        part of the API and breaking implementers left and right.
  //        This should be done properly with the next major release that introduces API breakage
  //        anyway
  @Autowired private HttpServletRequest currentRequest;

  @Autowired
  public PresentationServiceImpl(
      PresentationRepository presentationRepository,
      @Autowired(required = false) PresentationSecurityService presentationSecurityService) {
    this.presentationRepository = presentationRepository;
    this.presentationSecurityService = presentationSecurityService;
  }

  @Override
  public AnnotationList getAnnotationList(String identifier, String name, String canvasId)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    return presentationRepository.getAnnotationList(identifier, name, canvasId);
  }

  @Override
  public Collection getCollection(String name)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    return presentationRepository.getCollection(name);
  }

  @Override
  public Manifest getManifest(String identifier)
      throws ResolvingException, ResourceNotFoundException, InvalidDataException {
    if (presentationSecurityService != null
        && !presentationSecurityService.isAccessAllowed(identifier, currentRequest)) {
      throw new ResolvingException(); // TODO maybe throw an explicit 'access disallowed' exception
    }
    return presentationRepository.getManifest(identifier);
  }

  @Override
  public Instant getManifestModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    return presentationRepository.getManifestModificationDate(identifier);
  }

  @Override
  public Instant getCollectionModificationDate(String identifier)
      throws ResolvingException, ResourceNotFoundException {
    return presentationRepository.getCollectionModificationDate(identifier);
  }
}
