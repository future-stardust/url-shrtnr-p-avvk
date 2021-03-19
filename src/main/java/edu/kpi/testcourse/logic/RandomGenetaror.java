package edu.kpi.testcourse.logic;
import java.util.Random;

public class RandomGenetaror {

  int targetStringLength = 8;

  String randomString() {
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    Random random = new Random();

    String generatedString = random.ints(leftLimit, rightLimit + 1)
      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
      .limit(targetStringLength)
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .toString();

    return generatedString;
  }

  public String generate(int len) {
    targetStringLength = len;
    return randomString();
  }

  public String generate() {
    return randomString();
  }
}
