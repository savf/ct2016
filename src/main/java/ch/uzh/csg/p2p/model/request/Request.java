package ch.uzh.csg.p2p.model.request;

public abstract class Request {
private REQUEST_TYPE type;

public Request(){
  type = null;
}

public Request(REQUEST_TYPE type){
  setType(type);
}

public REQUEST_TYPE getType() {
  return type;
}

public void setType(REQUEST_TYPE type) {
  this.type = type;
}

}
