package ch.uzh.csg.p2p.model.request;

import ch.uzh.csg.p2p.model.Message;

public class MessageRequest extends Request {

  private static final long serialVersionUID = 1L;
  private Message message;

  public MessageRequest() {
    super();
    setMessage(null);
  }

  public MessageRequest(Message message, RequestType type) {
    super(type);
    setMessage(message);
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }



}
