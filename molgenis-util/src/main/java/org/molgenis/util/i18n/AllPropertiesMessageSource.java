package org.molgenis.util.i18n;

import static com.google.common.collect.Sets.newTreeSet;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.SetMultimap;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import org.springframework.context.support.ResourceBundleMessageSource;

/** A {@link ResourceBundleMessageSource} that can be queried for keys per basename. */
public class AllPropertiesMessageSource extends ResourceBundleMessageSource {
  public AllPropertiesMessageSource() {
    setAlwaysUseMessageFormat(false);
    setFallbackToSystemLocale(false);
    setUseCodeAsDefaultMessage(false);
    setDefaultEncoding("UTF-8");
  }

  public void addMolgenisNamespaces(String... namespaces) {
    addBasenames(
        Arrays.stream(namespaces)
            .map(namespace -> "l10n." + namespace.trim().toLowerCase())
            .toArray(String[]::new));
  }

  public SetMultimap<String, String> getAllMessageIds() {
    ImmutableSetMultimap.Builder<String, String> result = ImmutableSetMultimap.builder();
    getBasenameSet()
        .forEach(basename -> result.putAll(basename.substring(5), getMessageIds(basename)));
    return result.build();
  }

  private Set<String> getMessageIds(String basename) {
    Set<String> result = newTreeSet();
    for (String languageCode : LanguageService.getLanguageCodes().collect(toList())) {
      Locale locale = new Locale(languageCode);
      ResourceBundle resourceBundle = getResourceBundle(basename, locale);
      if (resourceBundle != null) {
        Enumeration<String> keys = resourceBundle.getKeys();
        Iterators.forEnumeration(keys).forEachRemaining(result::add);
      }
    }
    return result;
  }

  @Override
  // Overridden to make the visibility public
  public String resolveCodeWithoutArguments(String code, Locale locale) {
    return super.resolveCodeWithoutArguments(code, locale);
  }
}
