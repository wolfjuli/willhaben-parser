import {type Configuration} from '../types/configuration';


function transform(data: Configuration[]) {
    return data[0]
}


export const ConfigurationStore = $state<{ value: Configuration | undefined }>({})

fetch("/api/rest/v1/configuration")
    .then((response) => response.json())
    .then((data) => transform(data))
    .then((data) => {
        ConfigurationStore.value = data;
        return data
    })


