package ch.uzh.csg.p2p.model.request;

import java.io.UnsupportedEncodingException;

import ch.uzh.csg.p2p.model.User;

public class UserRequest extends Request {
private User user;

public UserRequest(){
  super();
  setUser(new User("test", "1234", null));
}

public void setUser(User user) {
  this.user = user;
}

public User getUser(){
  return user;
}

public UserRequest(User user, RequestType type){
  super(type);
  setUser(user);
}

}
