package edu.kpi.testcourse.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import edu.kpi.testcourse.entities.UrlAlias;
import edu.kpi.testcourse.logic.UrlShortenerConfig;
import edu.kpi.testcourse.serialization.JsonToolJacksonImpl;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UrlRepositoryFileImplTest {
  UrlShortenerConfig appConfig;
  UrlRepository urlRepository;

  @BeforeEach
  void setUp() {
    try {
      appConfig = new UrlShortenerConfig(
        Files.createTempDirectory("alias-repository-file-test"));
      Files.write(appConfig.storageRoot().resolve("alias-repository.json"), "{}".getBytes());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    urlRepository = new UrlRepositoryFileImpl(new JsonToolJacksonImpl(), appConfig);
  }

  @AfterEach
  void tearDown() {
    try {
      Files.delete(appConfig.storageRoot().resolve("alias-repository.json"));
      Files.delete(appConfig.storageRoot());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Test
  void shouldCreateAlias() {
    // WHEN
    UrlAlias alias = new UrlAlias("http://r.com/short", "http://g.com/long", "aaa@bbb.com");
    urlRepository.createUrlAlias(alias);

    // THEN
    assertThat(urlRepository.findUrlAlias("http://r.com/short")).isEqualTo(alias);
  }

  @Test
  void shouldNotAllowToCreateSameAliases() {
    // WHEN
    UrlAlias alias1 = new UrlAlias("http://r.com/short", "http://g.com/long1", "aaa@bbb.com");
    urlRepository.createUrlAlias(alias1);

    // THEN
    UrlAlias alias2 = new UrlAlias("http://r.com/short", "http://g.com/long2", "aaa@bbb.com");
    assertThatThrownBy(
      () -> {
        urlRepository.createUrlAlias(alias2);
      })
      .isInstanceOf(UrlRepository.AliasAlreadyExist.class);
  }

  @Test
  void shouldDeleteUserAlias() {
    // WHEN
    UrlAlias alias = new UrlAlias("http://r.com/short", "http://g.com/long", "aaa@bbb.com");
    urlRepository.createUrlAlias(alias);

    // THEN
    urlRepository.deleteUrlAlias("aaa@bbb.com", "http://r.com/short");
    assertThat(urlRepository.findUrlAlias("http://r.com/short")).isNull();
  }

  @Test
  void shouldNotDeleteUserAlias() {
    // WHEN
    UrlAlias alias = new UrlAlias("http://r.com/short", "http://g.com/long", "aaa@bbb.com");
    urlRepository.createUrlAlias(alias);

    // THEN
    assertThatThrownBy(
      () -> {
        urlRepository.deleteUrlAlias("bbb@ccc.com", "http://r.com/short");
      })
      .isInstanceOf(UrlRepository.PermissionDenied.class);
  }

  @Test
  void shouldGetAllUserAliases() {
    // WHEN
    UrlAlias alias1 = new UrlAlias("http://r.com/short1", "http://g.com/long1", "aaa@bbb.com");
    UrlAlias alias2 = new UrlAlias("http://r.com/short2", "http://g.com/long2", "aaa@bbb.com");
    UrlAlias alias3 = new UrlAlias("http://r.com/short3", "http://g.com/long3", "bbb@ccc.com");
    urlRepository.createUrlAlias(alias1);
    urlRepository.createUrlAlias(alias2);
    urlRepository.createUrlAlias(alias3);

    // THEN
    assertThat(urlRepository.getAllAliasesForUser("aaa@bbb.com")).containsExactlyInAnyOrder(alias1, alias2);
  }

  @Test
  void shouldReturnEmptyUserAliases(){
    // WHEN
    UrlAlias alias1 = new UrlAlias("http://r.com/short1", "http://g.com/long1", "aaa@bbb.com");

    // THEN
    assertThat(urlRepository.getAllAliasesForUser("bbb@ccc.com")).isEmpty();
  }
}
