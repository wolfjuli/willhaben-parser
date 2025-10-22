const ssr = false;
const load = async (event) => {
  const configuration = await fetch("/api/rest/v1/fe_configuration").then((response) => response.json()).then((data) => data[0]);
  return {
    configuration
  };
};
export {
  load,
  ssr
};
