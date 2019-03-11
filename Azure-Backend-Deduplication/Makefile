default: all

all: pack 

pack:
	javac -cp .:lib/* Mydedup.java MyDedupTools.java MyDedupUpload.java \
MyDedupDelete.java MyDedupDownload.java FileRecipeMap.java IndexEntry.java \
AzureInfo.java FileIndexMap.java

clean:
	rm *.class