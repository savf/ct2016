package ch.uzh.csg.p2p.model.request;

public abstract class Request {
private RequestType type;

public Request(){
  type = null;
}

public Request(RequestType type){
  setType(type);
}

public RequestType getType() {
  return type;
}

public void setType(RequestType type) {
  this.type = type;
}

}
