import { SingletonState} from "$lib/stores/AsSingleton";
import type {Listing} from "$lib/types/Listing";

export const FilterStore = SingletonState<(e: Listing) => boolean>(() => true)
