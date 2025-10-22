import { r as redirect } from "../../chunks/index.js";
const load = async (event) => {
  if (!event.url.searchParams.get("page"))
    redirect(302, "?page=1");
};
export {
  load
};
