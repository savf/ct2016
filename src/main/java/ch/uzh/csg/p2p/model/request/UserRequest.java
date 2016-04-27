package ch.uzh.csg.p2p.model.request;

public class UserRequest extends Request {
private String name;
private String password;

public UserRequest(){
  super();
  setName("test");
  setPassword("1234");
}

public UserRequest(String name, String password, REQUEST_TYPE type){
  super(type);
  setName(name);
  setPassword(password);
}

public String getName() {
  return name;
}
public void setName(String name) {
  this.name = name;
}
public String getPassword() {
  return password;
}
public void setPassword(String password) {
  this.password = password;
}



}
