import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.util.*;

public class MyDedupDownload {
    
    public static void LocalDownload(String fileToDownload) {

        try {

            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            FileInputStream fis = null;
            ObjectInputStream ois = null;
           
            /* Create a Directory */
            
            File dir = new File("data");

            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    System.err.println("Directory does not exist.");
                    return;
                }
            }
            
            if (!dir.isDirectory()) {
                System.err.println("Not a direcetory");
                return;
            }
            
            /* Create a File Recipe */
            
            File recipef = new File(dir.getName() + "/" + "mydedup.recipe");
            Boolean isNewRecipesFile = recipef.createNewFile();
            FileRecipeMap RecipeMap = new FileRecipeMap();
            
            if (!isNewRecipesFile) {
                
                fis = new FileInputStream(recipef.getAbsolutePath());
                ois = new ObjectInputStream(fis);
                RecipeMap = (FileRecipeMap) ois.readObject();
                ois.close();
                fis.close();
                
            }
            
            /* Merge the File */

            if (!RecipeMap.containsKey(fileToDownload)) {
                System.err.println("Error: File not exists. (RecipeMap)");
                return;
            }
            
            fos = new FileOutputStream(new File(fileToDownload + ".download"));
            byte[] fb;
            List<String> FileRecipe = RecipeMap.getRecipe(fileToDownload);
                    
            for (String FileChunk : FileRecipe) {
            
                if (FileChunk.matches("ZeroChunk:[0-9].*")){
                    
                    StringTokenizer stk = new StringTokenizer(FileChunk,":");
                    stk.nextToken();
                    int ChunkSize = Integer.parseInt(stk.nextToken());
                    fb = new byte[(int) ChunkSize];
                    fos.write(fb);
                    fos.flush();
                    
                }
                else{
                    
                    File f = new File(dir.getName() + "/" + FileChunk);
                    fis = new FileInputStream(f);
                    fb = new byte[(int) f.length()];
                    fis.read(fb, 0, (int) f.length());
                    fos.write(fb);
                    fos.flush();
                    fis.close();
                    
                }
            }
            
            fos.close();
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
    }
    
    public static void AzureDownload(String fileToDownload, AzureInfo ai) {

        try {

            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            FileInputStream fis = null;
            ObjectInputStream ois = null;
           
            /* Create a Directory */
            
            File dir = new File("data");

            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    System.err.println("Directory does not exist.");
                    return;
                }
            }
            
            if (!dir.isDirectory()) {
                System.err.println("Not a direcetory");
                return;
            }
            
            /* Create a File Recipe */
            
            ai.download("mydedup.recipe",dir.getName());
            File recipef = new File(dir.getName() + "/" + "mydedup.recipe");
            Boolean isNewRecipesFile = recipef.createNewFile();
            FileRecipeMap RecipeMap = new FileRecipeMap();
            
            if (!isNewRecipesFile) {
                
                fis = new FileInputStream(recipef.getAbsolutePath());
                ois = new ObjectInputStream(fis);
                RecipeMap = (FileRecipeMap) ois.readObject();
                ois.close();
                fis.close();
                
            }
            
            /* Merge the File */
            
            if (!RecipeMap.containsKey(fileToDownload)) {
                System.err.println("Error: File not exists. (RecipeMap)");
                return;
            }

            fos = new FileOutputStream(new File(fileToDownload + ".download"));
            byte[] fb;
            List<String> FileRecipe = RecipeMap.getRecipe(fileToDownload);
                    
            for (String FileChunk : FileRecipe) {

                if (FileChunk.matches("ZeroChunk:[0-9].*")){
                    
                    StringTokenizer stk = new StringTokenizer(FileChunk,":");
                    stk.nextToken();
                    int ChunkSize = Integer.parseInt(stk.nextToken());
                    fb = new byte[(int) ChunkSize];
                    fos.write(fb);
                    fos.flush();
                    
                }
                else{
                    
                    ai.download(FileChunk, dir.getName());
                    File f = new File(dir.getName() + "/" + FileChunk);
                    fis = new FileInputStream(f);
                    fb = new byte[(int) f.length()];
                    fis.read(fb, 0, (int) f.length());
                    fos.write(fb);
                    fos.flush();
                    fis.close();
                    
                }
                
            }
            
            fos.close();            
            MyDedupTools.removeAll(dir);

            
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
    }
    
}
