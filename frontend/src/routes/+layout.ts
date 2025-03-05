import {redirect} from "@sveltejs/kit";

export const ssr = false

export const load = (event) => {
    if (!event.url.searchParams.get("page"))
        redirect(302, "?page=1")
}
