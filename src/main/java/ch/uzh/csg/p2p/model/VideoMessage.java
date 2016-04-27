package ch.uzh.csg.p2p.model;

import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.List;

public class VideoMessage extends Message {

  private static final long serialVersionUID = 5905024972095013097L;
  private List<BufferedImage> data;
  
  public VideoMessage(String sender, String receiver, Timestamp timestamp, List<BufferedImage> data){
    super(sender, receiver, timestamp);
    setData(data);
  }
  
  public void setData(List<BufferedImage> data){
    this.data = data;
  }
  
  @Override
  public List<BufferedImage> getData() {
    return data;
  }

}
