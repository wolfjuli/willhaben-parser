export class Listing {

    constructor(
        public id: string,
        public name: string,
        public calculatedScore: number,
        public userScore: number,
        public calculatedPrice: number
    ) {
    }


}