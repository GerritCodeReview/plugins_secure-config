// Copyright (C) 2016 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.secureconfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

@Singleton
public class PBECodec implements Codec {
  private static final Logger log = LoggerFactory.getLogger(PBECodec.class);
  byte[] salt = new byte[] {0x7d, 0x60, 0x43, 0x5f, 0x02, (byte) 0xe9,
      (byte) 0xe0, (byte) 0xae};
  int iterationCount = 2048;
  private SecureConfigSettings config;

  @Inject
  public PBECodec(SecureConfigSettings config) {
    Provider provider = Security.getProvider(config.getJCEProvider());
    Security.addProvider(provider);
    this.config = config;
  }

  @Override
  public String encode(String s) {
    try {
      Key sKey = generateKey();
      Cipher encoder = getCipher();

      encoder.init(Cipher.ENCRYPT_MODE, sKey, getCipherParameterSpec());
      return new String(Base64.getEncoder().encodeToString(
          encoder.doFinal(s.getBytes(config.getEncoding()))));

    } catch (Exception e) {
      log.error("encode() failed", e);
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public String decode(String s) {
    try {
      Cipher encoder = getCipher();
      Key sKey = generateKey();

      encoder.init(Cipher.DECRYPT_MODE, sKey, getCipherParameterSpec());
      return new String(encoder.doFinal(Base64.getDecoder().decode(s)),
          config.getEncoding());

    } catch (Exception e) {
      log.error("decode() failed", e);
      throw new IllegalArgumentException(e);
    }
  }

  private PBEParameterSpec getCipherParameterSpec() {
    return new PBEParameterSpec(salt, iterationCount);
  }

  private Cipher getCipher() throws NoSuchAlgorithmException,
      NoSuchProviderException, NoSuchPaddingException {
    Cipher encoder =
        Cipher.getInstance(config.getCipher(), config.getJCEProvider());
    return encoder;
  }

  private Key generateKey() throws NoSuchAlgorithmException,
      NoSuchProviderException, InvalidKeySpecException {
    PBEKeySpec pbeSpec = new PBEKeySpec(config.getPassword());
    SecretKeyFactory keyFact =
        SecretKeyFactory.getInstance(config.getCipher(),
            config.getJCEProvider());
    return keyFact.generateSecret(pbeSpec);
  }
}
