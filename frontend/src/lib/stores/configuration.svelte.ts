import {type Configuration} from '../types/Configuration';


function transform(data: Configuration[]) {
    return data[0]
}


export const ConfigurationStore = $state<{ value: Configuration | undefined }>({value: undefined})

fetch("/api/rest/v1/fe_configuration")
    .then((response) => response.json())
    .then((data) => transform(data))
    .then((data) => {
        ConfigurationStore.value = data;
        return data
    })


