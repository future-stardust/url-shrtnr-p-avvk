package edu.kpi.testcourse.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import edu.kpi.testcourse.entities.UrlAlias;
import org.junit.jupiter.api.Test;

class UrlRepositoryFakeImplTest {

  @Test
  void shouldCreateAlias() {
    //GIVEN
    UrlRepository repo = new UrlRepositoryFakeImpl();

    //WHEN
    UrlAlias alias = new UrlAlias("http://r.com/short", "http://g.com/long", "aaa@bbb.com");
    repo.createUrlAlias(alias);

    //THEN
    assertThat(repo.findUrlAlias("http://r.com/short")).isEqualTo(alias);
  }

  @Test
  void shouldNotAllowToCreateSameAliases() {
    //GIVEN
    UrlRepository repo = new UrlRepositoryFakeImpl();

    //WHEN
    UrlAlias alias1 = new UrlAlias("http://r.com/short", "http://g.com/long1", "aaa@bbb.com");
    repo.createUrlAlias(alias1);

    //THEN
    UrlAlias alias2 = new UrlAlias("http://r.com/short", "http://g.com/long2", "aaa@bbb.com");
    assertThatThrownBy(() -> {
      repo.createUrlAlias(alias2);
    }).isInstanceOf(UrlRepository.AliasAlreadyExist.class);
  }

  @Test
  void shouldDeleteUserAlias() {
    // GIVEN
    UrlRepository repo = new UrlRepositoryFakeImpl();
    // WHEN
    UrlAlias alias = new UrlAlias("http://r.com/short", "http://g.com/long", "aaa@bbb.com");
    repo.createUrlAlias(alias);
    // THEN
    repo.deleteUrlAlias("aaa@bbb.com", "http://r.com/short");
    assertThat(repo.findUrlAlias("http://r.com/short")).isNull();
  }

  @Test
  void shouldNotDeleteUserAlias() {
    // GIVEN
    UrlRepository repo = new UrlRepositoryFakeImpl();

    // WHEN
    UrlAlias alias = new UrlAlias("http://r.com/short", "http://g.com/long", "aaa@bbb.com");
    repo.createUrlAlias(alias);
    // THEN
    repo.deleteUrlAlias("bbb@ccc.com", "http://r.com/short");
    repo.deleteUrlAlias("aaa@bbb.com", "http://r.com/anothershort");
    assertThat(repo.findUrlAlias("http://r.com/short")).isEqualTo(alias);
  }

  @Test
  void shouldGetAllUserAliases() {
    // GIVEN
    UrlRepository repo = new UrlRepositoryFakeImpl();
    // WHEN
    UrlAlias alias1 = new UrlAlias("http://r.com/short1", "http://g.com/long1", "aaa@bbb.com");
    UrlAlias alias2 = new UrlAlias("http://r.com/short2", "http://g.com/long2", "aaa@bbb.com");
    UrlAlias alias3 = new UrlAlias("http://r.com/short3", "http://g.com/long3", "bbb@ccc.com");
    repo.createUrlAlias(alias1);
    repo.createUrlAlias(alias2);
    repo.createUrlAlias(alias3);
    // THEN
    assertThat(repo.getAllAliasesForUser("aaa@bbb.com")).containsExactlyInAnyOrder(alias1, alias2);
  }


}
