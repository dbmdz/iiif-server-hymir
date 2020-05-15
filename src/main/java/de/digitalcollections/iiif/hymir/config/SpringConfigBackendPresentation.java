package de.digitalcollections.iiif.hymir.config;

import de.digitalcollections.commons.file.config.SpringConfigCommonsFile;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Backend configuration. */
@Configuration
@Import(SpringConfigCommonsFile.class)
public class SpringConfigBackendPresentation {}
