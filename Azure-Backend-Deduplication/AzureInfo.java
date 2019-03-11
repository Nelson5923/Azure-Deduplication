import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public class AzureInfo {
    
        public CloudStorageAccount Account;
        public CloudBlobClient Client;
        public CloudBlobContainer Container;
        public String ContainerName;
        String ConnectionName;
                
        public AzureInfo() {
            
            this.ContainerName = "container39";
            this.ConnectionName = "DefaultEndpointsProtocol=https;AccountName=csci4180storage39;"
                    + "AccountKey=V3e9odmjWi0u9cmmRdXNYfmV5gg0JwhVADc5LGF1BWeQLfPhwLtwanUXW/FQrgGm"
                    + "RWdkcaFOEyrWfznTg/liPg==;EndpointSuffix=core.windows.net";
     
        }
        
        public Boolean setup(){
            
            try{
                
                Account = CloudStorageAccount.parse(ConnectionName);
                Client = Account.createCloudBlobClient();
                Container = Client.getContainerReference(ContainerName);
                return true;
                
            }
            catch(Exception e){
                
                System.err.printf(e.toString());
                return false;
                
            }
            
        }
        
        public void download(String s, String dir){
            try{
                CloudBlockBlob blockBlobReference = Container.getBlockBlobReference(s);
                if (blockBlobReference.exists()) {
                    blockBlobReference.downloadToFile(dir + "/" + s);
                }
            }
            catch(Exception e){
                System.err.println(e.toString());
            }
        }
        
        public void uploadFromByteArray(String source, byte[] fb, Integer start, Integer end){
            try{
                CloudBlockBlob blockBlobReference = Container.getBlockBlobReference(source);
                blockBlobReference.uploadFromByteArray(fb,start,end);
            }
            catch(Exception e){
                System.err.println(e.toString());
            }
        }
        
        public void uploadFromFile(String source, String destination){
            try{
                CloudBlockBlob blockBlobReference = Container.getBlockBlobReference(destination);
                blockBlobReference.uploadFromFile(source);
            }
            catch(Exception e){
                System.err.println(e.toString());
            }
        }
        
        public void delete(String source){
            try{
            CloudBlockBlob blockBlobReference = Container.getBlockBlobReference(source);
            blockBlobReference.deleteIfExists();
            }
            catch(Exception e){
                System.err.println(e.toString());
            }
        }
    
}