package org.darknight.photofun.base;

import android.os.Bundle;
import org.darknight.photofun.MyApplication;
import org.darknight.photofun.gallery.data.Album;
import org.darknight.photofun.gallery.data.HandlingAlbums;

/** Created by dnld on 03/08/16. */
public class SharedMediaActivity extends ThemedActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  public static HandlingAlbums getAlbums() {
    return ((MyApplication) MyApplication.applicationContext).getAlbums();
  }

  public Album getAlbum() {
    return ((MyApplication) getApplicationContext()).getAlbum();
  }
}
