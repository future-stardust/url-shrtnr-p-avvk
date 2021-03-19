package edu.kpi.testcourse.logic;

import edu.kpi.testcourse.entities.UrlAlias;
import edu.kpi.testcourse.entities.User;
import edu.kpi.testcourse.storage.UrlRepository.AliasAlreadyExist;
import edu.kpi.testcourse.storage.UrlRepositoryFakeImpl;
import edu.kpi.testcourse.storage.UserRepositoryFakeImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class LogicTest {

  Logic createLogic() {
    return new Logic(new UserRepositoryFakeImpl(), new UrlRepositoryFakeImpl());
  }

  Logic createLogic(UserRepositoryFakeImpl users) {
    return new Logic(users, new UrlRepositoryFakeImpl());
  }

  Logic createLogic(UrlRepositoryFakeImpl urls) {
    return new Logic(new UserRepositoryFakeImpl(), urls);
  }

  @Test
  void shouldSuccessfullyCreateANewUser() throws Logic.UserIsAlreadyCreated {
    // GIVEN
    UserRepositoryFakeImpl users = new UserRepositoryFakeImpl();
    Logic logic = createLogic(users);

    // WHEN
    logic.createNewUser("aaa@bbb.com", "password");

    // THEN
    assertThat(users.findUser("aaa@bbb.com")).isNotNull();
  }

  @Test
  void shouldNotAllowUserCreationIfEmailIsUsed() {
    // GIVEN
    UserRepositoryFakeImpl users = new UserRepositoryFakeImpl();
    users.createUser(new User("aaa@bbb.com", "hash"));
    Logic logic = createLogic(users);

    assertThatThrownBy(
      () -> {
        // WHEN
        logic.createNewUser("aaa@bbb.com", "password");
      })
      // THEN
      .isInstanceOf(Logic.UserIsAlreadyCreated.class);
  }

  @Test
  void shouldAuthorizeUser() throws Logic.UserIsAlreadyCreated {
    // GIVEN
    Logic logic = createLogic();

    // WHEN
    logic.createNewUser("aaa@bbb.com", "password");

    // THEN
    assertThat(logic.isUserValid("aaa@bbb.com", "password")).isTrue();
  }

  @Test
  void shouldCreateShortVersionOfUrl() {
    // GIVEN
    UrlRepositoryFakeImpl urls = new UrlRepositoryFakeImpl();
    Logic logic = createLogic(urls);

    // WHEN
    var shortUrl = logic.createNewAlias("aaa@bbb.com", "http://g.com/loooong_url", "short");

    // THEN
    assertThat(shortUrl).isEqualTo("short");
    assertThat(logic.findFullUrl("short")).isEqualTo("http://g.com/loooong_url");
  }

  @Test
  void shouldCreateRandomVersionOfUrl() {
    // GIVEN
    UrlRepositoryFakeImpl urls = new UrlRepositoryFakeImpl();
    Logic logic = createLogic(urls);

    // WHEN
    var shortUrl = logic.createNewAlias("aaa@bbb.com", "http://g.com/loooong_url", null);

    // THEN
    assertThat(logic.findFullUrl(shortUrl)).isEqualTo("http://g.com/loooong_url");
  }

  @Test
  void shouldNotAllowToCreateSameAliasTwice() {
    // GIVEN
    Logic logic = createLogic();

    // WHEN
    var shortUrl = logic.createNewAlias("aaa@bbb.com", "http://g.com/loooong_url", "short");

    // THEN
    assertThatThrownBy(
      () -> {
        logic.createNewAlias("ddd@bbb.com", "http://d.com/laaaang_url", "short");
      })
      .isInstanceOf(AliasAlreadyExist.class);
  }

  @Test
  void shouldDeleteUserAlias() {
    // GIVEN
    Logic logic = createLogic();

    // WHEN
    var shortUrl = logic.createNewAlias("aaa@bbb.com", "http://g.com/loooong_url", "short");

    // THEN
    assertThat(logic.deleteUserAlias("aaa@bbb.com", "short")).isTrue();
    assertThat(logic.findFullUrl("short")).isNull();
  }

  @Test
  void shouldNotDeleteUserAlias() {
    // GIVEN
    Logic logic = createLogic();

    // WHEN
    var shortUrl = logic.createNewAlias("aaa@bbb.com", "http://g.com/loooong_url", "short");

    // THEN
    assertThat(logic.deleteUserAlias("bbb@ccc.com", "short")).isFalse();
    assertThat(logic.deleteUserAlias("aaa@bbb.com", "wrongShort")).isFalse();
  }

  @Test
  void shouldReturnUserAliases() {
    // GIVEN
    Logic logic = createLogic();

    // WHEN
    UrlAlias alias1 = new UrlAlias("short1", "http://g.com/long1", "aaa@bbb.com");
    UrlAlias alias2 = new UrlAlias("short2", "http://g.com/long2", "aaa@bbb.com");
    UrlAlias alias3 = new UrlAlias("short3", "http://g.com/long3", "bbb@ccc.com");
    logic.createNewAlias("aaa@bbb.com", "http://g.com/long1", "short1");
    logic.createNewAlias("aaa@bbb.com", "http://g.com/long2", "short2");
    logic.createNewAlias("bbb@ccc.com", "http://g.com/long3", "short3");

    // THEN
    Assertions
      .assertThat(logic.getUserAliases("aaa@bbb.com")).containsExactlyInAnyOrder(alias1, alias2);
    Assertions.assertThat(logic.getUserAliases("bbb@ccc.com")).containsExactlyInAnyOrder(alias3);
    Assertions.assertThat(logic.getUserAliases("ccc@ddd.com")).isEmpty();
  }
}
