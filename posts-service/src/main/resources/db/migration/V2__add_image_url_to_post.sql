-- Post.imageUrl: the Cloudinary URL returned by uploader-service when a post
-- carries an image. Nullable — posts without an image stay text-only.
ALTER TABLE post ADD COLUMN IF NOT EXISTS image_url varchar(255);
