const BaseAttributesStore = { value: void 0 };
const CustomAttributesStore = { value: void 0 };
function transform(a) {
  return { ...a, label: a.label ?? a.normalized };
}
fetch("/api/rest/v1/attributes").then((r) => r.json()).then((d) => BaseAttributesStore.value = d.map(transform));
fetch("/api/rest/v1/custom_attributes").then((r) => r.json()).then((d) => CustomAttributesStore.value = d.map(transform));
function mergedAttributes() {
  const custom = CustomAttributesStore.value?.map((a) => a.normalized) ?? [];
  const baseAttr = BaseAttributesStore.value?.filter((a) => !custom.find((c) => c === a.normalized)) ?? [];
  return {
    value: [
      ...baseAttr,
      ...CustomAttributesStore.value ?? []
    ]
  };
}
function filteredAttributes(normalized) {
  return normalized?.map((f) => mergedAttributes().value?.find((a) => a.normalized === f)).filter(Boolean) ?? [];
}
export {
  CustomAttributesStore as C,
  filteredAttributes as f,
  mergedAttributes as m
};
