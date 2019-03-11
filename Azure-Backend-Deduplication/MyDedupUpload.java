import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.util.*;

public class MyDedupUpload {
    
    public static void LocalUpload(int MinChunkSize, int AvgChunkSize, int MaxChunkSize, int d, String fileToUpload){
        
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
            
            /* Create a Index File */

            File indexf = new File(dir.getName() + "/" + "mydedup.index");
            Boolean isNewIndexFile = indexf.createNewFile();
            FileIndexMap IndexMap = new FileIndexMap();
            
            if (!isNewIndexFile) {
                fis = new FileInputStream(indexf.getAbsolutePath());
                ois = new ObjectInputStream(fis);
                IndexMap = (FileIndexMap) ois.readObject();
                ois.close();
                fis.close();
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

            if (RecipeMap.containsKey(fileToUpload)) {
                System.err.println("Error: File already exists. (RecipeMap)");
                return;
            }
            
            /* Chunks the File */
                 
            File f = new File(fileToUpload);
            fis = new FileInputStream(f);
            
            long FileSize = f.length();
            int MaxBufferSize = (int) Runtime.getRuntime().freeMemory();
            int Iteration = (int) Math.ceil((double)FileSize/(double)MaxBufferSize);      
            int LastChunkSize = (int) (FileSize % MaxBufferSize);
            List<String> FileRecipe = new ArrayList<String>();
            int TotalByte;

            for (int n = 0; n < Iteration; n++) {
                
                /* Allocate memory to a buffer */
                
                byte[] fb;
                
                if (n == Iteration - 1) {
                    fb = new byte[LastChunkSize];
                } else {
                    fb = new byte[MaxBufferSize];
                }
                
                TotalByte = fis.read(fb);
                System.out.println("Logging: Read Total Byte: " + TotalByte);
                
                int TotalBufferSize = fb.length;
                int m = MinChunkSize;
                int q = AvgChunkSize;
                int s = 0;
                int currentChunkSize = 0;
                int currentPos = 0;
                int fp = 0;
                List<Integer> offset = new ArrayList<Integer>();
                List<Integer> chunkSize = new ArrayList<Integer>();
                List<Boolean> isZeroChunk = new ArrayList<Boolean>();
                Boolean ZeroRun = false;
                Boolean PrevZero = false;

                /* While TotalChunkSize does not exceed TotalBufferSize */
                
                while ((currentPos + currentChunkSize <= TotalBufferSize) && (currentPos + m <= TotalBufferSize)) {
                    
                    if (s == 0) {
                        
                        /* Handle Zero Run */

                        for(int i = 1; i <= m; i++){
                          
                            if (fb[currentPos + i - 1] != 0) {
                                ZeroRun = false;
                                PrevZero = false;
                                break;
                            }
                            
                            ZeroRun = true;
                            PrevZero = true;
                            currentChunkSize = m;
                            fp = 0;
                            
                        }              
                        
                        /* Initialize the Fingerprint, Calculate the Fingerprint with MinChunks */
                        
                        if(!ZeroRun){
                                        
                            fp = 0;
                            
                            for (int i = 1; i <= m; i++) {
                                
                                //System.out.println(String.format("0x%04X", fb[currentPos + i - 1]));
                                
                                fp = (fp + ((int) (fb[currentPos + i - 1] & 0xff) * 
                                        MyDedupTools.FastMod(d, m - i, q))) % q;
                                
                            }
                            
                            currentChunkSize = m;
                            
                        }
                        
                    } else {
    
                        if(!ZeroRun){
                            
                            fp = (d * (fp - MyDedupTools.FastMod(d, m - 1, q) * (int) (fb[currentPos + s - 1] & 0xff)) 
                                    + fb[currentPos + s + m - 1]) % q;

                            while (fp < 0) {
                                fp += q;
                            }

                            currentChunkSize++;
                        
                        }else{
                        
                            if(fb[currentPos + m - 1 + s] != 0){
                                
                                ZeroRun = false;
                                
                            }else{
                            
                                currentChunkSize++;
                                
                            }
                            
                        }
                        
                    }
                        
                    /* Stop Condition for CurrentChunks */

                    if(ZeroRun && (currentPos + currentChunkSize >= TotalBufferSize)){
                        
                        offset.add(currentPos);
                        chunkSize.add(currentChunkSize);
                        isZeroChunk.add(true);
                        
                        currentPos += currentChunkSize;
                        s = 0;
                        currentChunkSize = 0;
                        
                        System.out.println("Logging: Enter Condition 1 (ZeroRun till EOF)");
                        
                    }
                    else if ((!ZeroRun) && (!PrevZero) && ((fp & 0xFF) == 0 
                            || currentPos + currentChunkSize >= TotalBufferSize || currentChunkSize >= MaxChunkSize)) {
                        
                        if(currentChunkSize >= MaxChunkSize)
                            currentChunkSize = MaxChunkSize;
                        
                        offset.add(currentPos);
                        chunkSize.add(currentChunkSize);
                        isZeroChunk.add(false);
                        
                        currentPos += currentChunkSize;
                        s = 0;
                        currentChunkSize = 0;
                        
                        System.out.println("Logging: Enter Condition 2 (FP = 0 & EOF * ReachMax)");

                    }
                    else if((!ZeroRun) && PrevZero){
                        
                        offset.add(currentPos);
                        chunkSize.add(currentChunkSize);
                        isZeroChunk.add(true);
                        
                        currentPos += currentChunkSize;
                        s = 0;
                        currentChunkSize = 0;
                        
                        PrevZero = false;                        
                        System.out.println("Logging: Enter Condition 3 (ZeroRunEnd)");
                    
                    }
                    else{

                        /* s for Shift */
                            
                        s++;
                        
                    }
                    
                }
                
                /* Insert the remaining Chunks */
                                        
                if (currentPos < TotalBufferSize) {

                    for(int i = 0; i < TotalBufferSize - currentPos; i++){
                        if (fb[currentPos + i] != 0) {
                            ZeroRun = false;
                            break;
                        }
                        ZeroRun = true;
                    }
                    
                    offset.add(currentPos);
                    chunkSize.add(TotalBufferSize - currentPos);
                    
                    if(ZeroRun)
                       isZeroChunk.add(true);
                    else
                       isZeroChunk.add(false);
                    
                    System.out.println("Logging: Enter Condition 4 (Not Enough for MinChunks)");
                    
                }
                
                /* Write the Chunks */
                
                if (offset.size() == chunkSize.size() && offset.size() > 0) {
                    
                    int numOfChunks = offset.size();          
            
                    for (int i = 0; i < numOfChunks; i++) {
                        
                        if(isZeroChunk.get(i)){
                            
                            String ZeroChunkID = "ZeroChunk" + ":" + chunkSize.get(i);
                            
                            if (IndexMap.containsKey(ZeroChunkID)) {
                                IndexMap.getEntry(ZeroChunkID).addRef();
                                FileRecipe.add(ZeroChunkID);
                            }
                            else{
                                IndexMap.putEntry(ZeroChunkID, new IndexEntry(chunkSize.get(i), 1));
                                FileRecipe.add(ZeroChunkID);
                            }
                                
                            System.out.println("Logging: ZeroChunk discover: " + chunkSize.get(i));
                            
                        }
                        else{
                        
                            String sha256Hex = DigestUtils.sha256Hex
                            (Arrays.copyOfRange(fb, offset.get(i), offset.get(i) + chunkSize.get(i)));

                            /* Reuse the Chunks if exists */

                            if (IndexMap.containsKey(sha256Hex)) {

                                IndexMap.getEntry(sha256Hex).addRef();
                                FileRecipe.add(sha256Hex);

                                System.out.println("Logging: IndexMap contains the Chunk (IndexMap)");

                            } else { 

                                /* Write the Chunks if it doesn't exist */

                                File newChunk = new File(dir.getAbsolutePath() + "/" + sha256Hex);

                                if (newChunk.createNewFile()) {

                                    fos = new FileOutputStream(newChunk);
                                    fos.write(fb, offset.get(i), chunkSize.get(i));
                                    fos.close();

                                    IndexMap.putEntry(sha256Hex, new IndexEntry(chunkSize.get(i), 1));
                                    FileRecipe.add(sha256Hex);

                                    System.out.println("Logging: Written the Chunk (newChunk)");

                                } else {

                                    System.err.println("Error: Chunks already exists (newChunk)");
                                    fis.close();
                                    return;

                                }
                                
                            }
                            
                        }
                        
                    }     
                }            
            }
            
            fis.close();
            
            long TotalLogicalChunks = 0;
            long TotalUniqueChunk = 0;
            long TotalLogicalFileBytes = 0;
            long TotalUniqueFileBytes = 0;
            double SpaceSaving = 0;

            for (String key : IndexMap.getKey()) {
                
                if (key.matches("ZeroChunk:[0-9].*")){
                    
                    int refCount = IndexMap.getEntry(key).getRefCount();
                    int chunkSize = IndexMap.getEntry(key).getChunkSize();
                    TotalLogicalChunks += refCount;
                    TotalLogicalFileBytes += refCount * chunkSize;                
                    
                }else{
                    
                    int refCount = IndexMap.getEntry(key).getRefCount();
                    int chunkSize = IndexMap.getEntry(key).getChunkSize();
                    TotalLogicalChunks += refCount;
                    TotalUniqueChunk++;
                    TotalLogicalFileBytes += refCount * chunkSize;
                    TotalUniqueFileBytes += chunkSize;
                    
                }
                
            }

            SpaceSaving  = 1 - (double)TotalUniqueChunk / (double)TotalLogicalChunks;
            System.out.println("Total number of chunks in storage: " + TotalLogicalChunks);
            System.out.println("Number of unique chunks in storage: " + TotalUniqueChunk);
            System.out.println("Number of bytes in storage with deduplication: " + TotalUniqueFileBytes);
            System.out.println("Number of bytes in storage without deduplication: " + TotalLogicalFileBytes);
            System.out.println("Space saving: " + SpaceSaving);

            RecipeMap.putRecipe(fileToUpload, FileRecipe);
            
            FileOutputStream indexfos = new FileOutputStream(indexf.getAbsolutePath());
            ObjectOutputStream indexoos = new ObjectOutputStream(indexfos);
            indexoos.writeObject(IndexMap);
            indexoos.close();
            indexfos.close();
            
            FileOutputStream recipefos = new FileOutputStream(recipef.getAbsolutePath());
            ObjectOutputStream recipeoos =  new ObjectOutputStream(recipefos);
            
            recipeoos.writeObject(RecipeMap);
            recipeoos.close();
            recipefos.close();
           
        }catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static void AzureUpload(int MinChunkSize, int AvgChunkSize, int MaxChunkSize, int d, String fileToUpload,
            AzureInfo ai){

        try{
                        
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
                   
             /* Create a Index File */

            ai.download("mydedup.index",dir.getName());
            File indexf = new File(dir.getName() + "/" + "mydedup.index");
            Boolean isNewIndexFile = indexf.createNewFile();
            FileIndexMap IndexMap = new FileIndexMap();
            
            if (!isNewIndexFile) {
                fis = new FileInputStream(indexf.getAbsolutePath());
                ois = new ObjectInputStream(fis);
                IndexMap = (FileIndexMap) ois.readObject();
                ois.close();
                fis.close();
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

            if (RecipeMap.containsKey(fileToUpload)) {
                System.err.println("Error: File already exists.");
                return;
            }
            
            /* Chunks the File */
                 
            File f = new File(fileToUpload);
            fis = new FileInputStream(f);
            
            long FileSize = f.length();
            int MaxBufferSize = (int) Runtime.getRuntime().freeMemory();
            int Iteration = (int) Math.ceil((double)FileSize/(double)MaxBufferSize);      
            int LastChunkSize = (int) (FileSize % MaxBufferSize);
            List<String> FileRecipe = new ArrayList<String>();
            int TotalByte;

            for (int n = 0; n < Iteration; n++) {
                
                /* Allocate memory to a buffer */
                
                byte[] fb;
                
                if (n == Iteration - 1) {
                    fb = new byte[LastChunkSize];
                } else {
                    fb = new byte[MaxBufferSize];
                }
                
                TotalByte = fis.read(fb);
                System.out.println("Logging: Read Total Byte: " + TotalByte);
                
                int TotalBufferSize = fb.length;
                int m = MinChunkSize;
                int q = AvgChunkSize;
                int s = 0;
                int currentChunkSize = 0;
                int currentPos = 0;
                int fp = 0;
                List<Integer> offset = new ArrayList<Integer>();
                List<Integer> chunkSize = new ArrayList<Integer>();
                List<Boolean> isZeroChunk = new ArrayList<Boolean>();
                Boolean ZeroRun = false;
                Boolean PrevZero = false;

                /* While TotalChunkSize does not exceed TotalBufferSize */
                
                while ((currentPos + currentChunkSize <= TotalBufferSize) && (currentPos + m <= TotalBufferSize)) {
                    
                    if (s == 0) {
                        
                        /* Handle Zero Run */

                        for(int i = 1; i <= m; i++){
                          
                            if (fb[currentPos + i - 1] != 0) {
                                ZeroRun = false;
                                PrevZero = false;
                                break;
                            }
                            
                            ZeroRun = true;
                            PrevZero = true;
                            currentChunkSize = m;
                            fp = 0;
                            
                        }              
                        
                        /* Initialize the Fingerprint, Calculate the Fingerprint with MinChunks */
                        
                        if(!ZeroRun){
                                        
                            fp = 0;
                            
                            for (int i = 1; i <= m; i++) {
                                
                                //System.out.println(String.format("0x%04X", fb[currentPos + i - 1]));
                                
                                fp = (fp + ((int) (fb[currentPos + i - 1] & 0xff) * 
                                        MyDedupTools.FastMod(d, m - i, q))) % q;
                                
                            }
                            
                            currentChunkSize = m;
                            
                        }
                        
                    } else {
    
                        if(!ZeroRun){
                            
                            fp = (d * (fp - MyDedupTools.FastMod(d, m - 1, q) * (int) (fb[currentPos + s - 1] & 0xff)) 
                                    + fb[currentPos + s + m - 1]) % q;

                            while (fp < 0) {
                                fp += q;
                            }

                            currentChunkSize++;
                        
                        }else{
                        
                            if(fb[currentPos + m - 1 + s] != 0){
                                
                                ZeroRun = false;
                                
                            }else{
                            
                                currentChunkSize++;
                                
                            }
                            
                        }
                        
                    }
                        
                    /* Stop Condition for CurrentChunks */

                    if(ZeroRun && (currentPos + currentChunkSize >= TotalBufferSize)){
                        
                        offset.add(currentPos);
                        chunkSize.add(currentChunkSize);
                        isZeroChunk.add(true);
                        
                        currentPos += currentChunkSize;
                        s = 0;
                        currentChunkSize = 0;
                        
                        System.out.println("Logging: Enter Condition 1 (ZeroRun till EOF)");
                        
                    }
                    else if ((!ZeroRun) && (!PrevZero) && ((fp & 0xFF) == 0 
                            || currentPos + currentChunkSize >= TotalBufferSize || currentChunkSize >= MaxChunkSize)) {
                        
                        if(currentChunkSize >= MaxChunkSize)
                            currentChunkSize = MaxChunkSize;
                        
                        offset.add(currentPos);
                        chunkSize.add(currentChunkSize);
                        isZeroChunk.add(false);
                        
                        currentPos += currentChunkSize;
                        s = 0;
                        currentChunkSize = 0;
                        
                        System.out.println("Logging: Enter Condition 2 (FP = 0 & EOF * ReachMax)");

                    }
                    else if((!ZeroRun) && PrevZero){
                        
                        offset.add(currentPos);
                        chunkSize.add(currentChunkSize);
                        isZeroChunk.add(true);
                        
                        currentPos += currentChunkSize;
                        s = 0;
                        currentChunkSize = 0;
                        
                        PrevZero = false;                        
                        System.out.println("Logging: Enter Condition 3 (ZeroRunEnd)");
                    
                    }
                    else{

                        /* s for Shift */
                            
                        s++;
                        
                    }
                    
                }
                
                /* Insert the remaining Chunks */
                                        
                if (currentPos < TotalBufferSize) {

                    for(int i = 0; i < TotalBufferSize - currentPos; i++){
                        if (fb[currentPos + i] != 0) {
                            ZeroRun = false;
                            break;
                        }
                        ZeroRun = true;
                    }
                    
                    offset.add(currentPos);
                    chunkSize.add(TotalBufferSize - currentPos);
                    
                    if(ZeroRun)
                       isZeroChunk.add(true);
                    else
                       isZeroChunk.add(false);
                    
                    System.out.println("Logging: Enter Condition 4 (Not Enough for MinChunks)");
                    
                }
                
                /* Write the Chunks */
                
                if (offset.size() == chunkSize.size() && offset.size() > 0) {
                    
                    int numOfChunks = offset.size();          
            
                    for (int i = 0; i < numOfChunks; i++) {
                        
                        if(isZeroChunk.get(i)){
                            
                            String ZeroChunkID = "ZeroChunk" + ":" + chunkSize.get(i);
                            
                            if (IndexMap.containsKey(ZeroChunkID)) {
                                IndexMap.getEntry(ZeroChunkID).addRef();
                                FileRecipe.add(ZeroChunkID);
                            }
                            else{
                                IndexMap.putEntry(ZeroChunkID, new IndexEntry(chunkSize.get(i), 1));
                                FileRecipe.add(ZeroChunkID);
                            }
                                
                            System.out.println("Logging: ZeroChunk discover: " + chunkSize.get(i));
                            
                        }
                        else{
                        
                            String sha256Hex = DigestUtils.sha256Hex
                            (Arrays.copyOfRange(fb, offset.get(i), offset.get(i) + chunkSize.get(i)));

                            /* Reuse the Chunks if exists */

                            if (IndexMap.containsKey(sha256Hex)) {

                                IndexMap.getEntry(sha256Hex).addRef();
                                FileRecipe.add(sha256Hex);

                                System.out.println("Logging: IndexMap contains the Chunk (IndexMap)");

                            } else { 

                                /* Write the Chunks if it doesn't exist */

                                ai.uploadFromByteArray(sha256Hex,fb,offset.get(i), chunkSize.get(i));
                                IndexMap.putEntry(sha256Hex, new IndexEntry(chunkSize.get(i), 1));
                                FileRecipe.add(sha256Hex);

                                System.out.println("Logging: Written the Chunk (newChunk)");
                                
                            }
                            
                        }
                        
                    }     
                }            
            }
            
            long TotalLogicalChunks = 0;
            long TotalUniqueChunk = 0;
            long TotalLogicalFileBytes = 0;
            long TotalUniqueFileBytes = 0;
            double SpaceSaving = 0;

            for (String key : IndexMap.getKey()) {
                
                if (key.matches("ZeroChunk:[0-9].*")){
                    
                    int refCount = IndexMap.getEntry(key).getRefCount();
                    int chunkSize = IndexMap.getEntry(key).getChunkSize();
                    TotalLogicalChunks += refCount;
                    TotalLogicalFileBytes += refCount * chunkSize;                
                    
                }else{
                    
                    int refCount = IndexMap.getEntry(key).getRefCount();
                    int chunkSize = IndexMap.getEntry(key).getChunkSize();
                    TotalLogicalChunks += refCount;
                    TotalUniqueChunk++;
                    TotalLogicalFileBytes += refCount * chunkSize;
                    TotalUniqueFileBytes += chunkSize;
                    
                }
                
            }

            SpaceSaving  = 1 - (double)TotalUniqueChunk / (double)TotalLogicalChunks;
            System.out.println("Total number of chunks in storage: " + TotalLogicalChunks);
            System.out.println("Number of unique chunks in storage: " + TotalUniqueChunk);
            System.out.println("Number of bytes in storage with deduplication: " + TotalUniqueFileBytes);
            System.out.println("Number of bytes in storage without deduplication: " + TotalLogicalFileBytes);
            System.out.println("Space saving: " + SpaceSaving);

            RecipeMap.putRecipe(fileToUpload, FileRecipe);
            
            FileOutputStream indexfos = new FileOutputStream(indexf.getAbsolutePath());
            ObjectOutputStream indexoos = new ObjectOutputStream(indexfos);
            
            indexoos.writeObject(IndexMap);
            indexoos.close();
            indexfos.close();
            
            FileOutputStream recipefos = new FileOutputStream(recipef.getAbsolutePath());
            ObjectOutputStream recipeoos =  new ObjectOutputStream(recipefos);
            
            recipeoos.writeObject(RecipeMap);
            recipeoos.close();
            recipefos.close();
           
            File source = new File(dir.getName() + "/" + "mydedup.index");
            ai.uploadFromFile(source.getAbsolutePath(), "mydedup.index");
            source = new File(dir.getName() + "/" + "mydedup.recipe");
            ai.uploadFromFile(source.getAbsolutePath(), "mydedup.recipe");
            MyDedupTools.removeAll(dir);
            
        }
        catch(Exception e){
            System.err.println(e.toString());
            return;
        }
        
    }
    
}
