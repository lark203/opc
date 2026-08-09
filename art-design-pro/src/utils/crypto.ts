import * as CryptoJS from 'crypto-js';

type WordArray = any;

const generateRandomString = (): string => {
  const array = new Uint8Array(32);
  crypto.getRandomValues(array);
  return Array.from(array, b => b.toString(16).padStart(2, '0'))
    .join('')
    .slice(0, 32);
};

export const generateAesKey = (): WordArray => {
  return CryptoJS.enc.Utf8.parse(generateRandomString());
};

export const encryptBase64 = (str: WordArray): string => {
  return CryptoJS.enc.Base64.stringify(str);
};

export const decryptBase64 = (str: string): WordArray => {
  return CryptoJS.enc.Base64.parse(str);
};

export const encryptWithAes = (message: string, aesKey: WordArray): string => {
  const encrypted = CryptoJS.AES.encrypt(message, aesKey, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  });
  return encrypted.toString();
};

export const decryptWithAes = (message: string, aesKey: WordArray): string => {
  const decrypted = CryptoJS.AES.decrypt(message, aesKey, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  });
  return decrypted.toString(CryptoJS.enc.Utf8);
};