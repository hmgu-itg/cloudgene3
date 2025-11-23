package cloudgene.mapred.util;

import java.util.Base64;

import org.apache.commons.codec.binary.Base32;
import java.security.SecureRandom;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.awt.image.BufferedImage;

public class OTPUtil {
    
    public static String createQR(String ga_url,int height,int width) throws IOException, WriterException {
        BitMatrix matrix = new MultiFormatWriter().encode(ga_url,BarcodeFormat.QR_CODE,width,height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix,"png",baos);
        return new String(Base64.getEncoder().encode(baos.toByteArray()));
    }

    public static String getGoogleAuthenticatorURL(String secretKey, String account, String issuer) throws IllegalStateException {
        try {
            return "otpauth://totp/"
                + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20")
                + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
		+ "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

}
