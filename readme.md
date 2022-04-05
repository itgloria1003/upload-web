
## /inbox 
https://www.baeldung.com/java-aws-s3-reactive

-> AWS IAM create the user and grant the S3FullAccess 
-> obtain the Access key/secret and put them in application.yml


## /upload-and-validate 
- Controller to consume the multipart file into Flux FilePart
- Converting the file parts into Flux of String using dataBuffer
- Collect all the data parts string and process them
- Check validity using Java Stream API and regex