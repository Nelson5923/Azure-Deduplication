import java.io.*;
import java.util.*;

public class MyDedupTools {

    public static void usage() {
        System.out.println("Usage: ");
        System.out.println("    java -cp .:./lib/* Mydedup upload <min_chunk> <avg_chunk> <max_chunk> <d> <file_to_upload> <local|azure>");
        System.out.println("    java -cp .:./lib/* Mydedup download <file_to_download> <local|azure>");
        System.out.println("    java -cp .:./lib/* Mydedup delete <file_to_delete> <local|azure>");
    }

    public static int FastMod(int base, int exp, int modulus) {
        base %= modulus;
        int result = 1;
        while (exp > 0) {
            if ((exp & 1) != 0) result = (result * base) % modulus;
            base = (base * base) % modulus;
            exp >>= 1;
        }
        return result;
    }
    
    public static void removeAll(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                removeAll(f);
            }
        }
        file.delete();
    }
    
}
