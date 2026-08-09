import { JSEncrypt } from 'jsencrypt';

const publicKey = import.meta.env.VITE_APP_RSA_PUBLIC_KEY;

const privateKey = import.meta.env.VITE_APP_RSA_PRIVATE_KEY;

export const encrypt = (txt: string): string | false => {
  const encryptor = new JSEncrypt();
  encryptor.setPublicKey(publicKey);
  return encryptor.encrypt(txt);
};

export const decrypt = (txt: string): string | false => {
  const encryptor = new JSEncrypt();
  encryptor.setPrivateKey(privateKey);
  return encryptor.decrypt(txt);
};