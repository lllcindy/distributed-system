import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/*
 *
 * @author Xindi Lan
 * @date 29/09/2019
 *
 * The TCPClient class runs as a client by using TCP to
 * sent request to ask server to do either add, subtract
 * or view to a value, and receive the message returned by
 * server. And it can also use hash to encrypt and sign
 * the message.
 *
 */

public class TCPClient {
    public static void main(String args[]) throws Exception{
        // To inform the client is running
        System.out.println("Client Running!");
        Socket clientSocket = null;
        try {
            // Determine the address and port number
            String addr = "localhost";
            int serverPort = 6789;
            String m = "";
            System.out.println("Please input the operation(add, subtract or view) and value (if you ");
            System.out.println("choose add or subtract)(divided by comma). If you want exit, input quit!: ");
            Operate(addr, serverPort, clientSocket);
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
        } finally {
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
     * This method is to connect client to server and pass
     * the message to server. It also encrypt and sign the message.
     *
     * @param Addr the host name of server
     * @param serverPort the port number that will be used
     * @param clientSocket the socket used to connect to server
     */
    private static void Operate(String Addr, int serverPort, Socket clientSocket)throws Exception {
        BigInteger n; // n is the modulus for both the private and public keys
        BigInteger e; // e is the exponent of the public key
        BigInteger d; // d is the exponent of the private key

        Random rnd = new Random();

        // Generate two large random primes.
        // We use 400 bits here, but best practice for security is 2048 bits.
        // Change 400 to 2048, recompile, and run the program again and you will
        // notice it takes much longer to do the math with that many bits.
        BigInteger p = new BigInteger(400,100,rnd);
        BigInteger q = new BigInteger(400,100,rnd);

        // Compute n by the equation n = p * q.
        n = p.multiply(q);

        // Compute phi(n) = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        // Select a small odd integer e that is relatively prime to phi(n).
        // By convention the prime 65537 is used as the public exponent.
        e = new BigInteger ("65537");

        // Compute d as the multiplicative inverse of e modulo phi(n).
        d = e.modInverse(phi);

        //Use e+n to compute hash value as id for users
        String input = e.toString()+n.toString();
        String id = convertToHex(getHash(input));

        // Create the connection with server and use bufferedreader to get the input from user, and printwriter to write to server
        clientSocket = new Socket(Addr, serverPort);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
        BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));
        String nextLine;
        while ((nextLine = typed.readLine()) != null) {
            //If the input is not formatted as "," or quit!, ask user to input again
            if((!nextLine.contains(",")) && (!nextLine.equals("quit!")) && (!nextLine.equals("view"))){
                System.out.println("Please input in a correct format:");
                continue;
            }

            // If the input is quit, the break the loop and end the connection
            if (nextLine.equals("quit!")) {
                out.println(nextLine);
                out.flush();
                System.out.println("Client is Quitting.");
                break;
            } else {
                if(nextLine.split(",").length==2){
                    //If the operation inputted is not a add, subtract or view, ask user to input again
                    if (!nextLine.split(",")[0].equals("add") && !nextLine.split(",")[0].equals("subtract") && !nextLine.split(",")[0].equals("view")){
                        System.out.println("The operation can only be add subtract or view. Please input the ID, operation and value again:");
                        continue;
                    }
                    //If the there is a operand and the operand inputted is not a number, ask user to input again
                    try {
                        Integer.parseInt(nextLine.split(",")[1]);
                    } catch (NumberFormatException e1) {
                        System.out.println("The operand is not a number. Please input the ID, operation and value again:");
                        continue;
                    }
                }

                String sig = id+","+nextLine;
                // Generate signature using Sign method
                String signature = Sign(sig, d, n);
                // Combine the id, operation, operand, e, n, signature into one string
                String request  = sig + "," + e.toString() + "," +n.toString() + "," +signature;
                out.println(request);
                // Pass to server
                out.flush();
                String data = in.readLine(); // read a line of data from the stream
                System.out.println("Received: " + data);
                System.out.println("Please input the operation(add, subtract or view) and value (if you ");
                System.out.println("choose add or subtract)(divided by comma). If you want exit, input quit!: ");
            }
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
        return r.substring(r.length()-20); //Get the last 20 bytes as id
//        return buf.toString(); //Get the last 20 bytes as id
    }

    /**
     * This method is used to sign the massage and get the signature
     *
     * @param message the message string be signed
     * @param d the exponent of the private key
     * @param n the modulus for both the private and public keys
     * @return the String of signature
     */
    public static String Sign(String message, BigInteger d, BigInteger n) throws Exception {

        // compute the digest with SHA-256
        byte[] bytesOfMessage = message.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bigDigest = md.digest(bytesOfMessage);

        // we only want two bytes of the hash for BabySign
        // we add a 0 byte as the most significant byte to keep
        // the value to be signed non-negative.
        byte[] messageDigest = new byte[3];
        messageDigest[0] = 0;   // most significant set to 0
        messageDigest[1] = bigDigest[0]; // take a byte from SHA-256
        messageDigest[2] = bigDigest[1]; // take a byte from SHA-256 The message digest now has three bytes. Two from SHA-256 and one is 0

        // From the digest, create a BigInteger
        BigInteger m = new BigInteger(messageDigest);

        // encrypt the digest with the private key
        BigInteger c = m.modPow(d, n);

        // return this big integer string as signature
        return c.toString();
    }
}
