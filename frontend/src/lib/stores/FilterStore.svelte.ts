import { SingletonState} from "$lib/stores/AsSingleton";
import type {Listing} from "$lib/types/listing";

export const FilterFnStore = SingletonState<(e: Listing) => boolean>(() => true)
