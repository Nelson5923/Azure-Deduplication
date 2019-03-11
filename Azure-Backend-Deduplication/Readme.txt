##### Test Local #####

/* Check Duplicate File */

cp test.txt test-dup.txt
java -cp .:./lib/* Mydedup upload 4 8 16 10 test.txt local
java -cp .:./lib/* Mydedup upload 4 8 16 10 test-dup.txt local

/* Check Correct Byte */

wc test.txt

/* Check Download File */

java -cp .:./lib/* Mydedup download test.txt local
java -cp .:./lib/* Mydedup download test-dup.txt local
cat test.txt.download
cat test-dup.txt.download

/* Check Delete File */

java -cp .:./lib/* Mydedup delete test.txt local
ls ./data/
java -cp .:./lib/* Mydedup delete test-dup.txt local
ls ./data

/* Check Zero Run */

dd if=/dev/zero of=./test-zero.txt count=1 bs=20

java -cp .:./lib/* Mydedup upload 10 12 15 10 test-zero.txt local
java -cp .:./lib/* Mydedup download test-zero.txt local
wc test-zero.txt.download
java -cp .:./lib/* Mydedup delete test-zero.txt local
ls ./data

Parameter: min_chunk = 10, max_chunk =15, upload file1 which contains 20 bytes of zeros. 
Total number of logical chunks in storage: 1
A logical chunk of zero run.
Number of unique physical chunks in storage: 0
No physical chunk in backend store
Number of bytes in storage with deduplication: 0
Number of bytes in storage without deduplication: 20
Space saving: 1.0

/* Test Large File */

java -cp .:./lib/* Mydedup upload 1024 2048 4096 10 10mb.txt azure
java -cp .:./lib/* Mydedup download 10mb.txt azure
java -cp .:./lib/* Mydedup delete 10mb.txt azure

/* Test Zero Run */ # ZeroRun after FingerPrint

# https://stackoverflow.com/questions/28242813/how-to-convert-a-text-file-to-binary-file-using-linux-commands/28243029#28243029

echo "0000 4865 6c6c 6f20 776f 0000 0000 0000 0000 726c 6421 0000 0000" > text_dump
xxd -r -p text_dump > test-mixture.txt
xxd test-mixture.txt

java -cp .:./lib/* Mydedup upload 2 4 8 10 test-mixture.txt local
java -cp .:./lib/* Mydedup download test-mixture.txt local
less test-mixture.txt.download
java -cp .:./lib/* Mydedup delete test-mixture.txt local
ls ./data

------- Test Azure: Need to run in CSE Server & Set the Container in AzureInfo.java -------

##https://portal.azure.com/blade/Microsoft_Azure_Storage/ContainerMenuBlade/overview/storageAccountId

/* Check Duplicate File */

cp test.txt test-dup.txt
java -cp .:./lib/* Mydedup upload 4 8 16 10 test.txt azure
java -cp .:./lib/* Mydedup upload 4 8 16 10 test-dup.txt azure

/* Check Correct Byte */

wc test.txt

/* Check Download File */

java -cp .:./lib/* Mydedup download test.txt azure
java -cp .:./lib/* Mydedup download test-dup.txt azure
cat test.txt.download
cat test-dup.txt.download

/* Check Delete File */

java -cp .:./lib/* Mydedup delete test.txt azure
java -cp .:./lib/* Mydedup delete test-dup.txt azure

/* Check Zero Run */

dd if=/dev/zero of=./test-zero.txt count=1 bs=20

java -cp .:./lib/* Mydedup upload 10 12 15 10 test-zero.txt azure
java -cp .:./lib/* Mydedup download test-zero.txt azure
wc test-zero.txt.download
java -cp .:./lib/* Mydedup delete test-zero.txt azure

Parameter: min_chunk = 10, max_chunk =15, upload file1 which contains 20 bytes of zeros. 
Total number of logical chunks in storage: 1
A logical chunk of zero run.
Number of unique physical chunks in storage: 0
No physical chunk in backend store
Number of bytes in storage with deduplication: 0
Number of bytes in storage without deduplication: 20
Space saving: 1.0

/* Test Large File */

java -cp .:./lib/* Mydedup upload 1024 2048 4096 10 10mb.txt azure
java -cp .:./lib/* Mydedup download 10mb.txt azure
java -cp .:./lib/* Mydedup delete 10mb.txt azure

/* Test Zero Run */ # ZeroRun after FingerPrint

# https://stackoverflow.com/questions/28242813/how-to-convert-a-text-file-to-binary-file-using-linux-commands/28243029#28243029

echo "0000 4865 6c6c 6f20 776f 0000 0000 0000 0000 726c 6421 0000 0000" > text_dump
xxd -r -p text_dump > test-mixture.txt
xxd test-mixture.txt

java -cp .:./lib/* Mydedup upload 2 4 8 10 test-mixture.txt azure
java -cp .:./lib/* Mydedup download test-mixture.txt azure
less test-mixture.txt.download
java -cp .:./lib/* Mydedup delete test-mixture.txt azure

/* Test Upload Duplicate File */

cp test-mixture.txt test-mixture-dup.txt
java -cp .:./lib/* Mydedup upload 2 4 8 10 test-mixture-dup.txt azure
java -cp .:./lib/* Mydedup download test-mixture-dup.txt azure
less test-mixture-dup.txt.download
java -cp .:./lib/* Mydedup delete test-mixture-dup.txt azure

