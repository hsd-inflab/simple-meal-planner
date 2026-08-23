package hsd.inflab.smp.dto.response;

/**
 * Represents a single enum option for the frontend.
 *
 * @param value the raw enum name (stable lookup key, e.g. "MEAT")
 * @param displayName the localized label to render (e.g. "Fleisch")
 */
public record EnumOptionDto(String value, String displayName) {}
