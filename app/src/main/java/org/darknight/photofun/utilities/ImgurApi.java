package org.darknight.photofun.utilities;

import org.darknight.photofun.share.imgur.ImgurPicUploadReq;
import org.darknight.photofun.share.imgur.ImgurPicUploadResp;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ImgurApi {

  @POST("/image")
  Call<ImgurPicUploadResp> uploadImageToImgur(
      @Header("Authorization") String authorization, @Body ImgurPicUploadReq body);
}
