Have you ever wanted to use Square's [Picasso](http://square.github.io/picasso/) Android library to download images stored on the Amazon S3 and secured by IAM? It's simple - just set up your `AmazonS3Client`, create the new `S3Downloader` instance and pass it to the `Picasso.Builder` like so:
```java
final AmazonS3Client client = new AmazonS3Client(getCredentialsProvider());

new Picasso.Builder(getApplicationContext())
    .downloader(new S3Downloader(
        s3client,
        context,
        "your-image-bucket"
    ))
    .build()
    .load(getImagePath())
    .into(image);

```
That's it.

Fixes, PRs and such are highly appreciated.