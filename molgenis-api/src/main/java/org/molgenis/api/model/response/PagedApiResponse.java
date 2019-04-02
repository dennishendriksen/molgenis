package org.molgenis.api.model.response;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_PagedApiResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class PagedApiResponse {
  @Nullable
  public abstract PageResponse getPage();

  @Nullable
  public abstract LinksResponse getLinks();

  public abstract Object getData();

  public static PagedApiResponse create(PageResponse page, LinksResponse links, Object data) {
    return new AutoValue_PagedApiResponse(page, links, data);
  }
}