const BASE_36 = 36;
export const getRandomString = (): string => (Math.random() + 1).toString(BASE_36).substring(2);
export const getRandomNumber = (min: number, max: number): number => Math.floor(Math.random() * (max - min + 1) + min);
