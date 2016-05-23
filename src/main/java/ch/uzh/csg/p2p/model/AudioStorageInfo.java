package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AudioStorageInfo implements Serializable {

  private static final long serialVersionUID = -6117505294470998163L;
  private String username;
  private List<AudioInfo> audioinfolist;

  public AudioStorageInfo() {
    username = "";
    audioinfolist = new ArrayList<AudioInfo>();
  }

  public AudioStorageInfo(String username) {
    setUsername(username);
    audioinfolist = new ArrayList<AudioInfo>();
  }

  public AudioStorageInfo(String username, List<AudioInfo> list) {
    setUsername(username);
    setAudioinfolist(list);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<AudioInfo> getAudioinfolist() {
    return audioinfolist;
  }

  public void setAudioinfolist(List<AudioInfo> audioinfolist) {
    this.audioinfolist = audioinfolist;
  }

  public void addAudioInfo(AudioInfo info) {
    audioinfolist.add(info);
  }

}
