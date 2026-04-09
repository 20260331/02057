function base64ToArrayBuffer(base64: string): ArrayBuffer {
  const binaryString = atob(base64)
  const bytes = new Uint8Array(binaryString.length)
  for (let i = 0; i < binaryString.length; i += 1) {
    bytes[i] = binaryString.charCodeAt(i)
  }
  return bytes.buffer
}

function arrayBufferToBase64(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer)
  let binary = ''
  for (let i = 0; i < bytes.byteLength; i += 1) {
    binary += String.fromCharCode(bytes[i])
  }
  return btoa(binary)
}

export async function rsaEncryptPasswordOAEP(publicKeyBase64: string, password: string): Promise<string> {
  if (!window.crypto?.subtle) {
    throw new Error('当前环境不支持 WebCrypto，请使用 HTTPS 或 localhost 访问系统')
  }
  const spkiDer = base64ToArrayBuffer(publicKeyBase64)
  const cryptoKey = await window.crypto.subtle.importKey(
    'spki',
    spkiDer,
    {
      name: 'RSA-OAEP',
      hash: 'SHA-256'
    },
    false,
    ['encrypt']
  )

  const encrypted = await window.crypto.subtle.encrypt(
    { name: 'RSA-OAEP' },
    cryptoKey,
    new TextEncoder().encode(password)
  )

  return arrayBufferToBase64(encrypted)
}
