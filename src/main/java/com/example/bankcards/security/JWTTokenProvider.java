package com.example.bankcards.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JWTTokenProvider {
    private final JWEEncrypter jweEncrypter;

    private final ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor;

    @Value("${security.jwtSecretExpiration}")
    private long jwtSecretExpiration;

    @Value("${security.jwtRefreshExpiration}")
    private long refreshExpiration;

    private Payload getPayload(String subject, String otherInfo) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .claim("other_info", otherInfo)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + jwtSecretExpiration))
                .build();

        return new Payload(claims.toJSONObject());
    }

    private JWEHeader getHeader() {
        return new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256);
    }

    public String generateRefreshToken(UserDetails user) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .claim("type", "refresh")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + refreshExpiration))
                .build();

        Payload payload = new Payload(claims.toJSONObject());
        JWEObject jweObject = new JWEObject(getHeader(), payload);
        jweObject.encrypt(jweEncrypter);
        return jweObject.serialize();
    }


    public String generateToken(UserDetails user) throws JOSEException {
        JWEObject jweObject = new JWEObject(
                getHeader(),
                getPayload(
                        user.getUsername(),
                        "additional user info"
                ));

        jweObject.encrypt(jweEncrypter);
        return jweObject.serialize();
    }

    public String getSubject(String token) throws BadJOSEException, ParseException, JOSEException {
        JWTClaimsSet claims = extractClaims(token);
        return claims.getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) throws BadJOSEException, ParseException, JOSEException {
        String userName = getSubject(token);
        Date expiration = extractClaims(token).getExpirationTime();
        return userName.equals(userDetails.getUsername()) && expiration.after(new Date());
    }

    private JWTClaimsSet extractClaims(String token) throws BadJOSEException, ParseException, JOSEException {
        return jwtProcessor.process(token, null);
    }
}
