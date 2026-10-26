export function BackendTagToFrontendConverter(tag: string | string[] | undefined): string[] | undefined {
  if (Array.isArray(tag)) return tag.length > 0 ? tag : undefined;
  if (!tag || typeof tag !== "string") return undefined;
  const arr = tag
    .split(",")
    .map((t) => t.trim())
    .filter((t) => t.length > 0);
  return arr.length > 0 ? arr : undefined;
}

export function FrontendTagToBackendConverter(tag: string[] | undefined): string | undefined {
  if (!tag || !Array.isArray(tag) || tag.length === 0) return "";
  return tag
    .map((t) => t.trim())
    .filter(Boolean)
    .join(",");
}
