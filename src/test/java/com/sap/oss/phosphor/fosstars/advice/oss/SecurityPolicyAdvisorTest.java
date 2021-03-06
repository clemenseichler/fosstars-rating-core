package com.sap.oss.phosphor.fosstars.advice.oss;

import static com.sap.oss.phosphor.fosstars.advice.oss.AbstractOssAdvisor.OssAdviceContextFactory.WITH_EMPTY_CONTEXT;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.HAS_SECURITY_POLICY;
import static com.sap.oss.phosphor.fosstars.model.other.Utils.allUnknown;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sap.oss.phosphor.fosstars.advice.Advice;
import com.sap.oss.phosphor.fosstars.model.Rating;
import com.sap.oss.phosphor.fosstars.model.RatingRepository;
import com.sap.oss.phosphor.fosstars.model.ValueSet;
import com.sap.oss.phosphor.fosstars.model.rating.oss.OssSecurityRating;
import com.sap.oss.phosphor.fosstars.model.subject.oss.GitHubProject;
import com.sap.oss.phosphor.fosstars.model.value.ValueHashSet;
import java.util.List;
import org.junit.Test;

public class SecurityPolicyAdvisorTest {

  @Test
  public void testAdvicesForSecurityPolicy() {
    SecurityPolicyAdvisor advisor = new SecurityPolicyAdvisor(WITH_EMPTY_CONTEXT);
    GitHubProject project = new GitHubProject("org", "test");

    // no advices if no rating value is set
    assertTrue(advisor.adviseFor(project).isEmpty());

    Rating rating = RatingRepository.INSTANCE.rating(OssSecurityRating.class);
    ValueSet values = new ValueHashSet();

    // no advices for an unknown values
    values.update(allUnknown(rating.score().allFeatures()));
    assertTrue(advisor.adviseFor(project).isEmpty());

    // no advices if the project has a security policy
    values.update(HAS_SECURITY_POLICY.value(true));
    project.set(rating.calculate(values));
    assertTrue(advisor.adviseFor(project).isEmpty());

    // expect an advice if the project doesn't have a security policy
    values.update(HAS_SECURITY_POLICY.value(false));
    project.set(rating.calculate(values));
    List<Advice> advices = advisor.adviseFor(project);
    assertEquals(1, advices.size());
    Advice advice = advices.get(0);
    assertFalse(advice.content().text().isEmpty());
    assertFalse(advice.content().links().isEmpty());
  }
}