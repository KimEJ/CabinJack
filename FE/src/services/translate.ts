import { type FromLanguage, type Language } from '../types'
import axios from 'axios'

function getRandomInt(min: number, max: number) {
  return Math.floor(Math.random() * (max - min)) + min
}

export async function translate ({
  fromLanguage,
  toLanguage,
  text
}: {
  fromLanguage: FromLanguage
  toLanguage: Language
  text: string
}) {
  if (fromLanguage === toLanguage) return text

  const returns = (await axios.get('/translateNmt?text='+text, {
    withCredentials: true
  })).data as string;

  console.log(returns)
  
  const index = {
    start: returns.split("(Noun: ")[1].split(", ")[0],
    end: returns.split(")")[0].split(", ")[1]
  }

  const result = returns.split(" (Noun: ")[0];

  console.log(index.start, index.end, result);

  return text.slice(0, Number(index.start)) + result + text.slice(Number(index.start)+Number(index.end));
}
