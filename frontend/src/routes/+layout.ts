
export const ssr = false

export const load = async (event) => {
    const configuration = await fetch("/api/v1/rest/configuration")
        .then((response) => response.json())
        .then((data) => data[0])

    return {
        configuration
    }
}
