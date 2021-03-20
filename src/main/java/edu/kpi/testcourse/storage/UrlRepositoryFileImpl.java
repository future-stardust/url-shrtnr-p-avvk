package edu.kpi.testcourse.storage;


import com.google.gson.reflect.TypeToken;
import edu.kpi.testcourse.entities.UrlAlias;
import edu.kpi.testcourse.logic.UrlShortenerConfig;
import edu.kpi.testcourse.serialization.JsonTool;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A file-backed implementation of {@link UrlRepository} suitable for use in production.
 */
public class UrlRepositoryFileImpl implements UrlRepository{
  // UrlAliases, keyed by aliases.
  private final Map<String, UrlAlias> aliases;

  private final JsonTool jsonTool;
  private final UrlShortenerConfig appConfig;

  /**
   * Creates an instance.
   */
  @Inject
  public UrlRepositoryFileImpl(JsonTool jsonTool, UrlShortenerConfig appConfig) {
    this.jsonTool = jsonTool;
    this.appConfig = appConfig;
    this.aliases = readAliasesFromJsonDatabaseFile(jsonTool, makeJsonFilePath(appConfig.storageRoot()));
  }

  @Override
  public void createUrlAlias(UrlAlias urlAlias) throws AliasAlreadyExist {
    if (aliases.containsKey(urlAlias.alias())) {
      throw new UrlRepository.AliasAlreadyExist();
    }

    aliases.put(urlAlias.alias(), urlAlias);
    writeAliasesToJsonDatabaseFile(jsonTool, aliases, makeJsonFilePath(appConfig.storageRoot()));
  }

  @Nullable
  @Override
  public UrlAlias findUrlAlias(String alias) {
    return aliases.get(alias);
  }

  @Override
  public void deleteUrlAlias(String email, String alias) throws PermissionDenied {
    UrlAlias savedAlias = aliases.get(alias);
    if (savedAlias != null && savedAlias.email().equals(email)) {
      aliases.remove(alias);
      writeAliasesToJsonDatabaseFile(jsonTool, aliases, makeJsonFilePath(appConfig.storageRoot()));
    }
    else {
      throw new PermissionDenied();
    }
  }

  @Override
  public List<UrlAlias> getAllAliasesForUser(String userEmail) {
    var userAliases = new ArrayList<UrlAlias>();
    for (UrlAlias alias : aliases.values()) {
      if (alias.email().equals(userEmail)) {
        userAliases.add(alias);
      }
    }
    return userAliases;
  }

  private static Path makeJsonFilePath(Path storageRoot) {
    return storageRoot.resolve("alias-repository.json");
  }

  private static Map<String, UrlAlias> readAliasesFromJsonDatabaseFile(
    JsonTool jsonTool, Path sourceFilePath
  ) {
    String json;
    try {
      json = Files.readString(sourceFilePath, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Type type = new TypeToken<HashMap<String, UrlAlias>>(){}.getType();
    Map<String, UrlAlias> result = jsonTool.fromJson(json, type);
    if (result == null) {
      throw new RuntimeException("Could not deserialize the aliases repository");
    }
    return result;
  }

  private static void writeAliasesToJsonDatabaseFile(
    JsonTool jsonTool, Map<String, UrlAlias> aliases, Path destinationFilePath
  ) {
    String json = jsonTool.toJson(aliases);
    try {
      Files.writeString(destinationFilePath, json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
