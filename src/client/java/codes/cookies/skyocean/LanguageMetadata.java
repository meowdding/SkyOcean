package codes.cookies.skyocean;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public record LanguageMetadata(
    boolean allowMultiline,
    boolean allowTags,
    String prefix,
    boolean objectsAsKeys,
    boolean allowReferences,
    Map<String, JsonElement> references
) {
    public static LanguageMetadata fromJson(JsonObject json) {
        return new LanguageMetadata(
            json.has("allowMultiline") && json.get("allowMultiline").getAsBoolean(),
            json.has("allowTags") && json.get("allowTags").getAsBoolean(),
            json.has("prefix") ? json.get("prefix").getAsString() : null,
            json.has("objectAsKeys") && json.get("objectAsKeys").getAsBoolean(),
            json.has("allowReferences") && json.get("allowReferences").getAsBoolean(),
            json.has("@references") ? json.get("@references").getAsJsonObject().asMap() : null
        );
    }

}
