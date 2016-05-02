package ch.uzh.csg.p2p.model;

import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class VideoMessage extends Message {

  private static final long serialVersionUID = 5905024972095013097L;
  private List<BufferedImage> data;
  
  public VideoMessage(String sender, String receiver, Date date, List<BufferedImage> data){
    super(sender, receiver, date);
    setData(data);
  }
  
  public void setData(List<BufferedImage> data){
    this.data = data;
  }
  
  @Override
  public List<BufferedImage> getData() {
    return data;
  }
  
  @Override
  public String toString(){
    return "[sender="+getSenderID()+", receiver="+getReceiverID()+", date="+getDate().toString()+", videoData="+getData().toString()+"]";
  }

}
