package com.sap.oss.phosphor.fosstars.advice.oss.github;

import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.FUZZED_IN_OSS_FUZZ;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.HAS_SECURITY_POLICY;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.LANGUAGES;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.USES_ADDRESS_SANITIZER;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.USES_FIND_SEC_BUGS;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.USES_LGTM_CHECKS;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.USES_MEMORY_SANITIZER;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.USES_UNDEFINED_BEHAVIOR_SANITIZER;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.WORST_LGTM_GRADE;
import static com.sap.oss.phosphor.fosstars.model.other.Utils.allUnknown;
import static com.sap.oss.phosphor.fosstars.model.value.Language.C;
import static com.sap.oss.phosphor.fosstars.model.value.LgtmGrade.B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sap.oss.phosphor.fosstars.advice.Advice;
import com.sap.oss.phosphor.fosstars.advice.Link;
import com.sap.oss.phosphor.fosstars.model.Rating;
import com.sap.oss.phosphor.fosstars.model.RatingRepository;
import com.sap.oss.phosphor.fosstars.model.ValueSet;
import com.sap.oss.phosphor.fosstars.model.rating.oss.OssSecurityRating;
import com.sap.oss.phosphor.fosstars.model.subject.oss.GitHubProject;
import com.sap.oss.phosphor.fosstars.model.value.Languages;
import com.sap.oss.phosphor.fosstars.model.value.ValueHashSet;
import java.util.List;
import org.junit.Test;

public class OssSecurityGithubAdvisorTest {

  @Test
  public void testBasics() {
    OssSecurityGithubAdvisor advisor = new OssSecurityGithubAdvisor();

    GitHubProject project = new GitHubProject("org", "test");

    // no advices if no rating value is set
    assertTrue(advisor.adviseFor(project).isEmpty());

    Rating rating = RatingRepository.INSTANCE.rating(OssSecurityRating.class);
    ValueSet values = new ValueHashSet();

    // no advices for an unknown values
    values.update(allUnknown(rating.score().allFeatures()));
    assertTrue(advisor.adviseFor(project).isEmpty());

    // expect an advice if the LGTM checks are not enabled
    values.update(USES_LGTM_CHECKS.value(false));
    project.set(rating.calculate(values));
    assertEquals(1, advisor.adviseFor(project).size());

    // expect an advice if the LGTM grade is not the best
    values.update(WORST_LGTM_GRADE.value(B));
    project.set(rating.calculate(values));
    assertEquals(2, advisor.adviseFor(project).size());
  }

  @Test
  public void testAdviceForLgtmGrade() {
    final OssSecurityGithubAdvisor advisor = new OssSecurityGithubAdvisor();
    final GitHubProject project = new GitHubProject("org", "test");

    Rating rating = RatingRepository.INSTANCE.rating(OssSecurityRating.class);
    ValueSet values = new ValueHashSet();
    values.update(allUnknown(rating.score().allFeatures()));
    values.update(WORST_LGTM_GRADE.value(B));
    project.set(rating.calculate(values));

    // expect an advice if the LGTM grade is not the best
    List<Advice> advices = advisor.adviseFor(project);
    assertEquals(1, advices.size());
    Advice advice = advices.get(0);
    assertFalse(advice.content().text().isEmpty());
    assertFalse(advice.content().links().isEmpty());
    Link link = advice.content().links().get(0);
    assertFalse(link.name.isEmpty());
    assertEquals("https://lgtm.com/projects/g/org/test", link.url.toString());
  }

  @Test
  public void testAdvicesForSecurityPolicy() {
    final OssSecurityGithubAdvisor advisor = new OssSecurityGithubAdvisor();
    final GitHubProject project = new GitHubProject("org", "test");
    final Rating rating = RatingRepository.INSTANCE.rating(OssSecurityRating.class);
    final ValueSet values = new ValueHashSet();
    values.update(allUnknown(rating.score().allFeatures()));

    // expect an advice if the project doesn't have a security policy
    values.update(HAS_SECURITY_POLICY.value(false));
    project.set(rating.calculate(values));
    List<Advice> advices = advisor.adviseFor(project);
    assertEquals(1, advices.size());
    Advice advice = advices.get(0);
    assertFalse(advice.content().text().isEmpty());
    assertFalse(advice.content().links().isEmpty());
    boolean foundLinkForSuggestingSecurityPolicy = advice.content().links().stream().anyMatch(
        link -> "https://github.com/org/test/security/policy".equals(link.url.toString()));
    assertTrue(foundLinkForSuggestingSecurityPolicy);
  }

  @Test
  public void testAdvicesForFindSecBugs() {
    final OssSecurityGithubAdvisor advisor = new OssSecurityGithubAdvisor();
    final GitHubProject project = new GitHubProject("org", "test");
    final Rating rating = RatingRepository.INSTANCE.rating(OssSecurityRating.class);
    final ValueSet values = new ValueHashSet();
    values.update(allUnknown(rating.score().allFeatures()));

    // expect an advice if the project doesn't use FindSecBugs
    values.update(USES_FIND_SEC_BUGS.value(false));
    project.set(rating.calculate(values));
    List<Advice> advices = advisor.adviseFor(project);
    assertEquals(1, advices.size());
    Advice advice = advices.get(0);
    assertFalse(advice.content().text().isEmpty());
    assertFalse(advice.content().links().isEmpty());
    boolean foundLinkForSuggestingSecurityPolicy = advice.content().links().stream().anyMatch(
        link -> "https://find-sec-bugs.github.io/".equals(link.url.toString()));
    assertTrue(foundLinkForSuggestingSecurityPolicy);
  }

  @Test
  public void testAdvicesForSanitizers() {
    final OssSecurityGithubAdvisor advisor = new OssSecurityGithubAdvisor();
    final GitHubProject project = new GitHubProject("org", "test");
    final Rating rating = RatingRepository.INSTANCE.rating(OssSecurityRating.class);
    final ValueSet values = new ValueHashSet();
    values.update(allUnknown(rating.score().allFeatures()));

    // expect an advice if the project doesn't use sanitizers
    values.update(USES_ADDRESS_SANITIZER.value(false));
    values.update(USES_MEMORY_SANITIZER.value(false));
    values.update(USES_UNDEFINED_BEHAVIOR_SANITIZER.value(false));
    project.set(rating.calculate(values));
    List<Advice> advices = advisor.adviseFor(project);
    assertEquals(3, advices.size());
    for (Advice advice : advices) {
      assertFalse(advice.content().text().isEmpty());
      assertFalse(advice.content().links().isEmpty());
    }
  }

  @Test
  public void testAdvicesForOssFuzz() {
    final OssSecurityGithubAdvisor advisor = new OssSecurityGithubAdvisor();
    final GitHubProject project = new GitHubProject("org", "test");
    final Rating rating = RatingRepository.INSTANCE.rating(OssSecurityRating.class);
    final ValueSet values = new ValueHashSet();
    values.update(allUnknown(rating.score().allFeatures()));

    // expect an advice if the project is not fuzzed in OSS-Fuzz
    values.update(FUZZED_IN_OSS_FUZZ.value(false));
    values.update(LANGUAGES.value(Languages.of(C)));
    project.set(rating.calculate(values));
    List<Advice> advices = advisor.adviseFor(project);
    Advice advice = advices.get(0);
    assertFalse(advice.content().text().isEmpty());
    assertFalse(advice.content().links().isEmpty());
  }
}