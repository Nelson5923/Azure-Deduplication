public class Mydedup {

    static {
        System.setProperty("https.proxyHost", "proxy.cse.cuhk.edu.hk");
        System.setProperty("https.proxyPort", "8000");
        System.setProperty("http.proxyHost", "proxy.cse.cuhk.edu.hk");
        System.setProperty("http.proxyPort", "8000");
    }    
    
    public static void main(String[] args) {
             
        if (args.length < 1) {
            MyDedupTools.usage();
            System.exit(1);
        }
        
        String fileToProcess, storageBackend;
                
        switch (args[0]) {
            
            case "upload":
                
                if (args.length != 7) {
                    
                    MyDedupTools.usage();
                    System.exit(1);
                    
                } else {
                    
                    int minChunk = 0, avgChunk = 0, maxChunk = 0, d = 0;

                    try {
                        
                        minChunk = Integer.parseInt(args[1]);
                        avgChunk = Integer.parseInt(args[2]);
                        maxChunk = Integer.parseInt(args[3]);
                        d = Integer.parseInt(args[4]);
                        
                    } catch (NumberFormatException e) {
                        
                        MyDedupTools.usage();
                        System.exit(1);
                        
                    }
                    
                    fileToProcess = args[5];
                    storageBackend = args[6];
          
                    if (!(storageBackend.equals("local") || storageBackend.equals("azure"))) {
                        
                        MyDedupTools.usage();
                        System.exit(1);
                        
                    } else {
                        
                        if(storageBackend.equals("local"))
                            MyDedupUpload.LocalUpload(minChunk, avgChunk, maxChunk, d, fileToProcess);
                        if(storageBackend.equals("azure")){
                            
                            AzureInfo ai = new AzureInfo();
                            if(ai.setup() == false)
                                System.exit(1);
                            MyDedupUpload.AzureUpload(minChunk, avgChunk, maxChunk, d, fileToProcess, ai);
                            
                        }
                        else
                            System.exit(1);

                    }
                    
                }
                
                break;
                
            case "download":
                
                if (args.length != 3) {
                    
                    System.exit(1);
                    
                } else {
                    
                    fileToProcess = args[1];
                    storageBackend = args[2];
                    
                    if (!(storageBackend.equals("local") || storageBackend.equals("azure"))) {
                        
                        MyDedupTools.usage();
                        System.exit(1);
                        
                    } 
                    else {
                        
                        if(storageBackend.equals("local"))
                            MyDedupDownload.LocalDownload(fileToProcess);
                        if(storageBackend.equals("azure")){
                            
                            AzureInfo ai = new AzureInfo();
                            if(ai.setup() == false)
                                System.exit(1);
                            MyDedupDownload.AzureDownload(fileToProcess, ai);
                            
                        }
                        else
                            System.exit(1);
                        
                    }
                    
                }
                
                break;
                
            case "delete":
                
                if (args.length != 3) {
                    
                    MyDedupTools.usage();
                    System.exit(1);
                    
                } else {
                    
                    fileToProcess = args[1];
                    storageBackend = args[2];
                    
                    if (!(storageBackend.equals("local") || storageBackend.equals("azure"))) {
                        MyDedupTools.usage();
                        System.exit(1);
                    } 
                    else {
                        
                        if(storageBackend.equals("local"))
                            MyDedupDelete.LocalDelete(fileToProcess);
                        if(storageBackend.equals("azure")){
                            
                            AzureInfo ai = new AzureInfo();
                            if(ai.setup() == false)
                                System.exit(1);
                            MyDedupDelete.AzureDelete(fileToProcess, ai);
                            
                        }
                        else
                            System.exit(1);
                        
                    }
                    
                }
                
                break;
                
            default:
                
                MyDedupTools.usage();
                System.exit(1);
                
        }
    }
}
    
