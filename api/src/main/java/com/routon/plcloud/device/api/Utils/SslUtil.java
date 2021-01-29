package com.routon.plcloud.device.api.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;

/***
 * SSL连接工具类
 * @author wangzhao
 *
 */
public class SslUtil {
    /**
     * 读取项目外的证书文件
     */
    public static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile,
                                                    final String password) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        PEMReader reader = new PEMReader(
                new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
        X509Certificate caCert = (X509Certificate) reader.readObject();
        reader.close();

        // load client certificate
        reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(crtFile)))));
        X509Certificate cert = (X509Certificate) reader.readObject();
        reader.close();

        // load client private key
        reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(keyFile)))),
                new PasswordFinder() {
                    @Override
                    public char[] getPassword() {
                        return password.toCharArray();
                    }
                });

        KeyPair key = (KeyPair) reader.readObject();
        reader.close();

        // finally, create SSL socket factory
        SSLContext context = getSslContext(caCert, cert, key, password);

        return context.getSocketFactory();
    }

    /**
     * 读取项目jar包内的证书文件
     */
    public static SSLSocketFactory getSocketFactoryInJar(final String caCrtFile, final String crtFile, final String keyFile,
                                                         final String password) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        InputStream caCrtFileIns = SslUtil.class.getResourceAsStream(caCrtFile);
        //byte[] caCrtFileBytes = new byte[caCrtFileIns.available()];
        PEMReader reader = new PEMReader(new InputStreamReader(caCrtFileIns));
        X509Certificate caCert = (X509Certificate) reader.readObject();
        reader.close();

        // load client certificate
        InputStream crtFileIns = SslUtil.class.getResourceAsStream(crtFile);
        //byte[] crtFileBytes = new byte[crtFileIns.available()];
        reader = new PEMReader(new InputStreamReader(crtFileIns));
        X509Certificate cert = (X509Certificate) reader.readObject();
        reader.close();

        // load client private key
        InputStream keyFileIns = SslUtil.class.getResourceAsStream(keyFile);
        //byte[] keyFileBytes = new byte[keyFileIns.available()];
        reader = new PEMReader(new InputStreamReader(keyFileIns),
                new PasswordFinder() {
                    @Override
                    public char[] getPassword() {
                        return password.toCharArray();
                    }
                });
        KeyPair key = (KeyPair) reader.readObject();
        reader.close();

        // finally, create SSL socket factory
        SSLContext context = getSslContext(caCert, cert, key, password);

        return context.getSocketFactory();
    }

    /**
     * 读取证书文件内容公共方法
     */
    private static SSLContext getSslContext(X509Certificate caCert, X509Certificate cert, KeyPair key, String password) throws Exception {
        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate
        // us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                new java.security.cert.Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context;
    }
}