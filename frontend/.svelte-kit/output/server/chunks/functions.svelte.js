const transform = (data) => data.reduce(
  (acc, curr) => {
    acc[curr.id] = curr;
    return acc;
  },
  {}
);
const FunctionsStore = { value: {} };
fetch("/api/rest/v1/functions").then((response) => response.json()).then((data) => transform(data)).then((data) => {
  FunctionsStore.value = data;
});
export {
  FunctionsStore as F
};
