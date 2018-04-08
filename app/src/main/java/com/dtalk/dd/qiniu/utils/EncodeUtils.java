package com.dtalk.dd.qiniu.utils;

import android.util.Base64;

public class EncodeUtils {

	public static byte[] urlsafeEncodeBytes(byte[] src) {
		if (src.length % 3 == 0) {
			return encodeBase64Ex(src);
		}

		byte[] b = encodeBase64Ex(src);
		if (b.length % 4 == 0) {
			return b;
		}

		int pad = 4 - b.length % 4;
		byte[] b2 = new byte[b.length + pad];
		System.arraycopy(b, 0, b2, 0, b.length);
		b2[b.length] = '=';
		if (pad > 1) {
			b2[b.length + 1] = '=';
		}
		return b2;
	}
	
	public static byte[] urlsafeBase64Decode(String encoded){
		byte[] rawbs = encoded.getBytes();
		for(int i=0;i<rawbs.length;i++){
			if(rawbs[i] == '_'){
				rawbs[i] = '/';
			}else if(rawbs[i] == '-'){
				rawbs[i] = '+';
			}
		}
		return Base64.decode(rawbs, Base64.DEFAULT);
	}
	
	public static String urlsafeEncodeString(byte[] src) {
		return new String(urlsafeEncodeBytes(src));
	}

	public static String urlsafeEncode(String text) {
		return new String(urlsafeEncodeBytes(text.getBytes()));
	}

	// replace '/' with '_', '+" with '-'
	private static byte[] encodeBase64Ex(byte[] src) {
		// urlsafe version is not supported in version 1.4 or lower.
		byte[] b64 = Base64.encode(src, Base64.DEFAULT);

		for (int i = 0; i < b64.length; i++) {
			if (b64[i] == '/') {
				b64[i] = '_';
			} else if (b64[i] == '+') {
				b64[i] = '-';
			}
		}
		return b64;
	}

}
