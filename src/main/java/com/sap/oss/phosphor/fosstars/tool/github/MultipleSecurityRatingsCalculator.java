package com.sap.oss.phosphor.fosstars.tool.github;

import com.sap.oss.phosphor.fosstars.model.subject.oss.GitHubProject;
import com.sap.oss.phosphor.fosstars.model.value.RatingValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The class calculates security ratings for multiple open-source projects.
 */
class MultipleSecurityRatingsCalculator {

  /**
   * A logger.
   */
  private static final Logger LOGGER
      = LogManager.getLogger(MultipleSecurityRatingsCalculator.class);

  /**
   * A calculator that calculate a rating for a single project.
   */
  private final SingleSecurityRatingCalculator calculator;

  /**
   * A cache of processed projects.
   */
  private GitHubProjectCache projectCache = GitHubProjectCache.empty();

  /**
   * A filename where the cache of projects should be stored.
   */
  private String projectCacheFile;

  /**
   * A list of projects for which a rating couldn't be calculated.
   */
  private final List<GitHubProject> failedProjects = new ArrayList<>();

  /**
   * Initializes a new calculator that calculates ratings for multiple projects.
   *
   * @param calculator A calculator that calculate a rating for a single project.
   */
  MultipleSecurityRatingsCalculator(SingleSecurityRatingCalculator calculator) {
    Objects.requireNonNull(calculator, "Oh no! Calculator is null!");
    this.calculator = calculator;
  }

  /**
   * Set a cache of processed projects.
   *
   * @param projectCache The cache.
   * @return The same {@link MultipleSecurityRatingsCalculator}.
   */
  MultipleSecurityRatingsCalculator set(GitHubProjectCache projectCache) {
    this.projectCache = Objects.requireNonNull(projectCache, "Oh no! Project cache can't be null!");
    return this;
  }

  /**
   * Sets a file where the cache of processed projects should be stored.
   *
   * @param filename The file.
   * @return The same {@link MultipleSecurityRatingsCalculator}.
   */
  MultipleSecurityRatingsCalculator storeProjectCacheTo(String filename) {
    Objects.requireNonNull(filename, "Hey! Filename can't be null!");
    projectCacheFile = filename;
    return this;
  }

  /**
   * Calculate a rating for a project.
   *
   * @param project The project.
   * @return The same {@link MultipleSecurityRatingsCalculator}.
   * @throws IOException If something went wrong.
   */
  private MultipleSecurityRatingsCalculator calculateFor(GitHubProject project) throws IOException {
    Optional<RatingValue> cachedRatingValue = projectCache.cachedRatingValueFor(project);
    if (cachedRatingValue.isPresent()) {
      project.set(cachedRatingValue.get());
      LOGGER.info("Found a cached rating for {}", project);
      return this;
    }

    calculator.calculateFor(project);
    projectCache.add(project);

    return this;
  }

  /**
   * Calculates ratings for multiple projects.
   * First, the method checks if a rating value for a project is already available in cache.
   *
   * @param projects The projects.
   * @return The same calculator.
   */
  MultipleSecurityRatingsCalculator calculateFor(List<GitHubProject> projects) {
    failedProjects.clear();

    for (GitHubProject project : projects) {
      try {
        calculateFor(project);

        if (projectCacheFile != null) {
          LOGGER.info("Storing the project cache to {}", projectCacheFile);
          projectCache.store(projectCacheFile);
        }
      } catch (Exception e) {
        LOGGER.warn("Oh no! Could not calculate a rating for {}", project.scm());
        LOGGER.warn(e);
        failedProjects.add(project);
      }
    }

    return this;
  }

  /**
   * Returns a list of projects for which ratings couldn't be calculated.
   *
   * @return The list of projects.
   */
  List<GitHubProject> failedProjects() {
    return new ArrayList<>(failedProjects);
  }

}
