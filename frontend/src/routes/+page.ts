import {redirect} from "@sveltejs/kit";

export const load = async (event) => {
    if (!event.url.searchParams.get("page"))
        redirect(302, "?page=1")

}
