package se.sundsvall.teamssender.service;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.FileReader;
import java.io.Reader;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class PemUtils {

    public static PrivateKey readPrivateKey(String filepath) throws Exception {
        try (Reader reader = new FileReader(filepath);
             PEMParser pemParser = new PEMParser(reader)) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (object instanceof org.bouncycastle.openssl.PEMKeyPair) {
                return converter.getPrivateKey(((org.bouncycastle.openssl.PEMKeyPair) object).getPrivateKeyInfo());
            } else if (object instanceof org.bouncycastle.openssl.PEMEncryptedKeyPair) {
                throw new UnsupportedOperationException("Krypterade PEM-nycklar stöds inte i detta exempel");
            } else {
                throw new IllegalArgumentException("Ogiltigt PEM-format för privat nyckel");
            }
        }
    }

    public static X509Certificate readCertificate(String filepath) throws Exception {
        try (FileReader reader = new FileReader(filepath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(new java.io.FileInputStream(filepath));
        }
    }
}