import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Scanner;

/*
 *
 * @author Xindi Lan
 * @date 29/09/2019
 *
 * The TCPServer class runs as a server by using TCP to
 * receive request from client. First it verify the
 * signature and the hash value. Once that done, it will
 * do add, subtract or view a to a value based on the
 * request, and then return back the value to client.
 *
 */

public class TCPServer {
    public static void main(String args[]) throws Exception{
        System.out.println("Server Running!");
        Socket clientSocket = null;
        try{
            // the server port we are using
            int serverPort = 6789;
            HashMap<String, Integer> map = new HashMap<>();
            // Create a new server socket
            ServerSocket listenSocket = new ServerSocket(serverPort);
            Add(clientSocket, listenSocket, map);
            // Handle exceptions
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
            // If quitting (typically by you sending quit signal) clean up sockets
        }finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }

    /**
     * This method is to connect client to server.It also
     * verify the message and can do the corresponding
     * computation. Finally, it will return the value back
     * to client.
     *
     * @param clientSocket the socket used to connect to server
     * @param listenSocket the socket used for server to accept the connection from client
     * @param map stores the pairs of id and values
     */
    private static void Add(Socket clientSocket, ServerSocket listenSocket, HashMap<String, Integer> map) throws Exception{
        //To let server always run. After client closed, it will launch a new loop and create a new listencosket
        while (true) {
            try {
                // Create the connection and use scanner to read the request from client, and printwriter to write to client
                clientSocket = listenSocket.accept();
                Scanner in = new Scanner(clientSocket.getInputStream());
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
                while(true) {
                    String data = in.nextLine();
                    //If the request is quit!, server will ignore that and start a new listensocket
                    if (data.equals("quit!")) {
                        break;
                    } else {
                        //separate the request into id, operation, operand, e, n, signature
                        String[] re = data.split(",");
                        String idt = "";
                        String id = "";
                        boolean verif = false;
                        //If the operation is view, the index will be slightly different
                        if (re[1].equals("view")){
                            //sig is the string before signed
                            String sig = re[0] + "," + re[1];
                            String signature = re[4];
                            BigInteger e = new BigInteger(re[2]);
                            BigInteger n = new BigInteger(re[3]);
                            //Compute hash again and compare with the hash passed from client
                            String input = e.toString() + n.toString();
                            idt = convertToHex(getHash(input));
                            id = re[0].trim();
                            //Use verify method to find whether the message is well signed
                            verif = Verify(sig, signature, e, n);
                        }
                        else {
                            String sig = re[0] + "," + re[1] + "," + re[2];
                            String signature = re[5];
                            BigInteger e = new BigInteger(re[3]);
                            BigInteger n = new BigInteger(re[4]);
                            String input = e.toString() + n.toString();
                            idt = convertToHex(getHash(input));
                            id = re[0].trim();
                            verif = Verify(sig, signature, e, n);
                        }
                        //If the hash is correct and message is well signed, then do the computation
                        if (idt.equals(id) && verif) {
                            String replies = "";
                            //If the operation is add, then add the operand and return ok
                            if (re[1].trim().equals("add")) {
                                //If id does not exist, first set the value to 0
                                if (!map.containsKey(id)) {
                                    int v = Integer.parseInt(re[2].trim());
                                    map.put(id, v);
                                    replies = "OK";
                                } else {
                                    int v = Integer.parseInt(re[2].trim());
                                    int num = map.get(id) + v;
                                    map.put(id, num);
                                    replies = "OK";
                                }
                            //If the operation is subtract, then subtract the operand and return ok
                            } else if (re[1].trim().equals("subtract")) {
                                //If id does not exist, first set the value to 0
                                if (!map.containsKey(id)) {
                                    int v = Integer.parseInt(re[2].trim());
                                    int num = 0 - v;
                                    map.put(id, num);
                                    replies = "OK";
                                } else {
                                    int v = Integer.parseInt(re[2].trim());
                                    int num = map.get(id) - v;
                                    map.put(id, num);
                                    replies = "OK";
                                }
                            //If the operation is view, then return the value
                            } else if (re[1].trim().equals("view")) {
                                //If id does not exist, first set the value to 0
                                if (!map.containsKey(id)) {
                                    map.put(id, 0);
                                }
                                replies = String.valueOf(map.get(id));
                            }
                            System.out.println("Echoing: The number " + id + " item has the value of " + map.get(id));
                            //Pass the reply to client
                            out.println(replies);
                            out.flush();
                        }
                        // If the hash is not correct or message is not well signed, then return the error message
                        else{
                            String reply = "Echoing: Error in Request.";
                            System.out.println(reply);
                            out.println(reply);
                            out.flush();
                        }
                    }
                }
            } catch (IOException e) {
                clientSocket.close();
            }
        }
    }

    /**
     * This method is used to verify whether the message sent is well signed by client
     *
     * @param messageToCheck the message that needs to be verified
     * @param encryptedHashStr the signature
     * @param e the exponent of the public key
     * @param n the modulus for both the private and public keys
     * @return a boolean value to represent whether the message is well signed
     */
    public static boolean Verify(String messageToCheck, String encryptedHashStr, BigInteger e, BigInteger n)throws Exception  {

        // Take the encrypted string and make it a big integer
        BigInteger encryptedHash = new BigInteger(encryptedHashStr);
        // Decrypt it
        BigInteger decryptedHash = encryptedHash.modPow(e, n);

        // Get the bytes from messageToCheck
        byte[] bytesOfMessageToCheck = messageToCheck.getBytes("UTF-8");

        // compute the digest of the message with SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] messageToCheckDigest = md.digest(bytesOfMessageToCheck);

        // messageToCheckDigest is a full SHA-256 digest
        // take two bytes from SHA-256 and add a zero byte
        byte[] extraByte = new byte[3];
        extraByte[0] = 0;
        extraByte[1] = messageToCheckDigest[0];
        extraByte[2] = messageToCheckDigest[1];

        // Make it a big int
        BigInteger bigIntegerToCheck = new BigInteger(extraByte);

        // Compare the two value. If they are equal, then return true. If not, return false
        if(bigIntegerToCheck.compareTo(decryptedHash) == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * This method can convert the plain text into hash
     * value and return array of bytes
     *
     * @param text the String need to be converted into hash
     * @return the array of byte converted from text
     */
    private static byte[] getHash(String text){
        try {
            // Get the corresponding instance of specific hash type
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(text.getBytes());
//            byte[] by = new byte[20];
//            System.arraycopy(b, b.length-20, by, 0, 20);
            return b;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown"
                    + " for incorrect algorithm: " + e);
            return null;
        }
    }

    /**
     * This method convert the result of getHash into string based message
     *
     * @param data the array of byte returned from getHash method
     * @return a String converted from the array of byte
     */
    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        String r = buf.toString();
        return r.substring(r.length()-20);
    }
}

