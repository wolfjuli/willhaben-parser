function _layout($$payload, $$props) {
  const { children } = $$props;
  children($$payload);
  $$payload.out += `<!---->`;
}
export {
  _layout as default
};
