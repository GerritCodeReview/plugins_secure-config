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

import com.google.inject.Singleton;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

@Singleton
public class Codec {

  char[] password = "password".toCharArray();
  byte[] salt = new byte[] {0x7d, 0x60, 0x43, 0x5f, 0x02, (byte) 0xe9,
      (byte) 0xe0, (byte) 0xae};
  int iterationCount = 2048;

  public Codec() {
    Security
        .addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
  }

  public String encode(String s) {
    try {
      PBEKeySpec pbeSpec = new PBEKeySpec(password);
      SecretKeyFactory keyFact =
          SecretKeyFactory.getInstance("PBEWithMD5AndDES", "BC");

      Cipher encoder = Cipher.getInstance("PBEWithMD5AndDES", "BC");
      Key sKey = keyFact.generateSecret(pbeSpec);

      encoder.init(Cipher.ENCRYPT_MODE, sKey, new PBEParameterSpec(salt,
          iterationCount));
      byte[] out = encoder.doFinal(s.getBytes());
      return new String(out);
    } catch (NoSuchAlgorithmException | NoSuchProviderException
        | NoSuchPaddingException | InvalidKeyException
        | InvalidAlgorithmParameterException | IllegalBlockSizeException
        | BadPaddingException | InvalidKeySpecException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    }
  }

  public String decode(String s) {
    PBEKeySpec pbeSpec = new PBEKeySpec(password);
    SecretKeyFactory keyFact;
    try {
      keyFact =
          SecretKeyFactory.getInstance("PBEWithMD5AndDES", "BC");

      Cipher encoder = Cipher.getInstance("PBEWithMD5AndDES", "BC");
      Key sKey = keyFact.generateSecret(pbeSpec);

      encoder.init(Cipher.DECRYPT_MODE, sKey, new PBEParameterSpec(salt,
          iterationCount));
      byte[] out = encoder.doFinal(s.getBytes());
      return new String(out);
    } catch (NoSuchAlgorithmException | NoSuchProviderException
        | NoSuchPaddingException | InvalidKeyException
        | InvalidAlgorithmParameterException | IllegalBlockSizeException
        | BadPaddingException | InvalidKeySpecException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    }

  }
}
