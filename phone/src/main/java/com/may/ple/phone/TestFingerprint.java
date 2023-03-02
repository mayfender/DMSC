package com.may.ple.phone;

import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;

public class TestFingerprint {
	public static void main(String[] args) {
		String originalString = "MIICFTCCAX4CCQDK388aJNK5kjANBgkqhkiG9w0BAQsFADBPMQswCQYDVQQGEwJC\r\n" +
				"SzELMAkGA1UECAwCQksxCzAJBgNVBAcMAkJLMQswCQYDVQQKDAJCSzELMAkGA1UE\r\n" +
				"CwwCQksxDDAKBgNVBAMMA01UQzAeFw0yMjEwMjgyMjI0MDFaFw0yMjExMjcyMjI0\r\n" +
				"MDFaME8xCzAJBgNVBAYTAkJLMQswCQYDVQQIDAJCSzELMAkGA1UEBwwCQksxCzAJ\r\n" +
				"BgNVBAoMAkJLMQswCQYDVQQLDAJCSzEMMAoGA1UEAwwDTVRDMIGfMA0GCSqGSIb3\r\n" +
				"DQEBAQUAA4GNADCBiQKBgQDPSXhZrKIwvELEnp0OP/2nDR8OX5nWyCMKRtSzpOTv\r\n" +
				"OBv1iT5WKD6hQ0ApQ4G5w36+rcQTBLI/xgIpGhVr0fwisB1zI3vH8jUNenr1K3n8\r\n" +
				"7GMVPF5zlZ6SWl8cNQCks1eg7KiCAXymZPDSDRzuWDQEqC980PIkctsllMsnlJLi\r\n" +
				"xwIDAQABMA0GCSqGSIb3DQEBCwUAA4GBAHOb1Q38doRsOELm4VGRO90U5gIrg0AG\r\n" +
				"BOaiTf+Q5la6GAVoPnA/+sn7l4hLXsb3UoXlNmGuqobGpAZr6pTiogH55vlx0nlh\r\n" +
				"y4ip/577L4kD73Ft0LTxZc79zJ/5KayLvkXdBZ4rg5f55BWBQypr34sFYCjDzbkN\r\n" +
				"Tcn1SMf1iNvA\r\n" +
				"";
//		String sha256hex = DigestUtils.sha256Hex(originalString);
//		System.out.println(sha256hex);


		String sha256hex = Hashing.sha256()
				  .hashString(originalString, StandardCharsets.UTF_8)
				  .toString();
		System.out.println(sha256hex);
	}

}
