package ch.uzh.csg.p2p.model;

import java.io.Serializable;

public class AudioInfo implements Serializable {

  private static final long serialVersionUID = 7580302456484382160L;
  private String sendername;
  private int count;

  public AudioInfo() {
    sendername = "";
    count = 0;
  }

  public AudioInfo(String sendername) {
    setSendername(sendername);
    count = 0;
  }

  public AudioInfo(String sendername, int count) {
    setSendername(sendername);
    setCount(count);
  }

  public String getSendername() {
    return sendername;
  }

  public void setSendername(String sendername) {
    this.sendername = sendername;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
