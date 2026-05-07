package com.example.bankcards.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Base64;

import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;

@Configuration
public class JwtConfig {

    @Value("${security.jwtSecret}")
    private String jwtSecret;

    @Bean
    public JWEEncrypter jweEncrypter() throws JOSEException {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return new DirectEncrypter(keyBytes);
    }

    @Bean
    public ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        OctetSequenceKey jwk = new OctetSequenceKey.Builder(keyBytes).build();
        JWKSet jwkSet = new JWKSet(jwk);
        JWKSource<SimpleSecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.HS256;
        JWSKeySelector<SimpleSecurityContext> keySelector =
                new JWSVerificationKeySelector<>(expectedJWSAlg, jwkSource);

        DefaultJWTProcessor<SimpleSecurityContext> processor = new DefaultJWTProcessor<>();

        processor.setJWSKeySelector(keySelector);

        Set<String> requiredClaims = new HashSet<>(Arrays.asList("sub", "exp"));

        JWTClaimsSet expectedClaims = new JWTClaimsSet.Builder().build();

        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                expectedClaims,
                requiredClaims
        ));

        return processor;
    }
}
