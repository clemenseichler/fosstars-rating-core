package com.sap.sgs.phosphor.fosstars.data.github;

import static com.sap.sgs.phosphor.fosstars.model.feature.oss.OssFeatures.USES_FIND_SEC_BUGS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sap.sgs.phosphor.fosstars.model.Value;
import com.sap.sgs.phosphor.fosstars.model.ValueSet;
import com.sap.sgs.phosphor.fosstars.model.value.ValueHashSet;
import com.sap.sgs.phosphor.fosstars.tool.github.GitHubProject;
import com.sap.sgs.phosphor.fosstars.tool.github.GitHubProjectValueCache;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.junit.Test;

public class UsesFindSecBugsTest extends TestGitHubDataFetcherHolder {

  @Test
  public void testMavenWithFindSecBugs() throws IOException {
    try (InputStream is = getClass().getResourceAsStream("MavenWithFindSecBugs.xml")) {
      checkValue(createProvider(is, "pom.xml"), true);
    }
  }

  @Test
  public void testMavenWithoutFindSecBugs() throws IOException {
    try (InputStream is = getClass().getResourceAsStream("MavenWithoutFindSecBugs.xml")) {
      checkValue(createProvider(is, "pom.xml"), false);
    }
  }

  private UsesFindSecBugs createProvider(InputStream is, String filename) throws IOException {
    final LocalRepository repository = mock(LocalRepository.class);
    when(repository.read(filename)).thenReturn(Optional.of(is));

    GitHubProject project = new GitHubProject("org", "test");
    fetcher.addForTesting(project, repository);

    UsesFindSecBugs provider = new UsesFindSecBugs(fetcher);
    provider.set(new GitHubProjectValueCache());

    return provider;
  }

  private static void checkValue(UsesFindSecBugs provider, boolean expectedValue)
      throws IOException {

    GitHubProject project = new GitHubProject("org", "test");

    ValueSet values = new ValueHashSet();
    provider.update(project, values);

    assertEquals(1, values.size());
    assertTrue(values.has(USES_FIND_SEC_BUGS));
    assertTrue(values.of(USES_FIND_SEC_BUGS).isPresent());

    Value<Boolean> value = values.of(USES_FIND_SEC_BUGS).get();
    assertEquals(expectedValue, value.get());
  }
}