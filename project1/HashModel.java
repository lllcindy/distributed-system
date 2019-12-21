package cmu.edu.xindilan;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * @author Xindi Lan
 * @date 19/09/2019
 *
 * This part models the business logic for the web application.
 * In this case, the business logic involves transferring the
 * text into bytes for a given Hash type. And return the
 * transferred result.
 */

public class HashModel {
    /**
     *
     * @param input The input text given by users.
     * @param type The hash type chosen by users.
     * @return transferred result of byte
     */

    public byte[] getHash(String input, String type) {

        try {
            // Get the corresponding instance of specific hash type
            MessageDigest md = MessageDigest.getInstance(type);
            return md.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown"
                    + " for incorrect algorithm: " + e);

            return null;
        }
    }
}
