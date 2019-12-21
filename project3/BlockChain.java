import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Date;

/*
 *
 * @author Xindi Lan
 * @date 15/10/2019
 *
 * The BlockChain class holds the blockchain object.
 * It has the block array and the current hash. It
 * can add block, get chain size, hashes per second,
 * check whether the block chain is valid. If not,
 * repair the block chain.
 *
 */

public class BlockChain {
    public ArrayList<Block> blockchain;
    public String Hash = "";

    public BlockChain() {
        blockchain = new ArrayList<Block>();
        Hash = "";
    }

    /**
     * This method is to add new block into the block
     * chain and calculate the hash for the block.
     *
     * @param newBlock the block need to be added
     */
    public void addBlock(Block newBlock) {
        blockchain.add(newBlock);
        newBlock.setPreviousHash(Hash);
        Hash = newBlock.proofOfWork();
    }

    public int getChainSize() {
        return blockchain.size();
    }

    public Block getLatestBlock() {
        return blockchain.get(blockchain.size() - 1);
    }

    public Timestamp getTime() {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        ;
        return timestamp;
    }

    /**
     * This method is to calculate how many hashes can the machine compute in a second
     *
     * @return the number of hashes
     */
    public int hashesPerSecond() {
        long t = System.currentTimeMillis();
        long end = t + 1000;
        int count = 0;
        // set a loop that stand 1000 milliseconds
        while (System.currentTimeMillis() < end) {
            // repeat to calculate the hash for "00000000".
            String h = getHash("00000000");
            // count the number of hashes
            count++;
        }
        return count;
    }

    /**
     * This method is to computes the hash of the block and checks that
     * the hash has the requisite number of leftmost 0's (proof of work)
     * as specified in the difficulty field. It also checks that the chain
     * hash is equal to this computed hash. If either check fails, return false.
     * Otherwise, return true. If the chain has more blocks than one, begin
     * checking from block one. Continue checking until you have validated the
     * entire chain.
     *
     * @return boolean refers to whther the block chain is correct
     */
    public boolean isChainValid() {
        int c = 0;
        // First check the first block
        Block b = blockchain.get(0);
        String h = b.calculateHash();
        int count = 0;
        // check whether the hash of the block has the correct number of zero at the beginning. If not, break the loop
        for (int i = 0; i < h.length(); i++) {
            //If the block has the zero in the correct position, add the count by 1
            if (h.charAt(i) == '0') {
                count++;
            } else {
                break;
            }
        }
        // If the size is more than one, we need to also check the chain
        // (whether the the block has the previous hash equals to the previous
        // block's hash)
        if (count == b.getDifficulty()) {
            c++;
        }
        if (blockchain.size() > 1) {
            for (int i = 1; i < blockchain.size(); i++) {
                b = blockchain.get(i - 1);
                Block bb = blockchain.get(i);
                h = bb.calculateHash();
                count = 0;
                // First as the for loop above, check whether the current block
                // has the correct number of zero in the correct position
                for (int j = 0; j < h.length(); j++) {
                    if (h.charAt(j) == '0') {
                        count++;
                    } else {
                        break;
                    }
                }
                // if the blocks has the correct hash and the previous hash
                // ais equal to the hash of previous block, add the count
                if ((count == bb.getDifficulty()) && (bb.getPreviousHash().equals(b.calculateHash()))) {
                    c++;
                }
            }
        }
        // If the count is equal to the block chain size, which menas all the
        // blocks and chains are correct, return true
        if (c == blockchain.size()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method is to repair the chain. It checks the hashes of each
     * block and ensures that any illegal hashes are recomputed. If
     * there exists the wrong hash, it computes new proof of work based
     * on the difficulty specified in the Block.
     *
     */
    public void repairChain() {
        int index = 0;
        // check the hash of each block just as what isValid() do
        for (int i = 0; i < blockchain.size(); i++) {
            Block b = blockchain.get(i);
            String h = b.calculateHash();
            int count = 0;
            for (int j = 0; j < h.length(); j++) {
                if (h.charAt(i) == '0') {
                    count++;
                } else {
                    break;
                }
            }
            // If the cont of zero is not equal to difficulty, get the index of the block
            if (count != b.getDifficulty()) {
                index = i;
            }
            break;
        }
        // from the block that has wrong hash, compute all the hashes after that.
        for (int i = index; i < blockchain.size(); i++) {
            // recompute the hash for the current block
            String rehash = blockchain.get(i).proofOfWork();
            // is the block is not the last one, set this hash to the previoushash
            if (i != blockchain.size() - 1) {
                blockchain.get(i + 1).setPreviousHash(rehash);
            }
            // If the block is the last block, set "hash" equals to the hash
            else {
                Hash = rehash;
            }
        }
    }

    /**
     * This method can convert the plain text into hash
     * value and return hex string of the hash
     *
     * @param text the String need to be converted into hash
     * @return the hex string of the hash
     */
    private static String getHash(String text) {
        try {
            // Get the corresponding instance of specific hash type
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(text.getBytes());
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                int halfbyte = (b[i] >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    if ((0 <= halfbyte) && (halfbyte <= 9))
                        buf.append((char) ('0' + halfbyte));
                    else
                        buf.append((char) ('a' + (halfbyte - 10)));
                    halfbyte = b[i] & 0x0F;
                } while (two_halfs++ < 1);
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown"
                    + " for incorrect algorithm: " + e);
            return null;
        }
    }
}
