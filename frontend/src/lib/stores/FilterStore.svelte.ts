import { SingletonState} from "$lib/stores/AsSingleton";
import type {Listing} from "$lib/types/Listing";

export const FilterFnStore = SingletonState<(e: Listing) => boolean>(() => true)
