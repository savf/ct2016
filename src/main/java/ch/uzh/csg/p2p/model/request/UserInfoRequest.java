package ch.uzh.csg.p2p.model.request;

import ch.uzh.csg.p2p.model.UserInfo;

public class UserInfoRequest extends Request {

  private static final long serialVersionUID = 1L;
  private UserInfo userInfo;

  public UserInfoRequest() {
    super();
    setUserInfo(new UserInfo(null, "", ""));
  }

  public void setUserInfo(UserInfo user) {
    this.userInfo = user;
  }

  public UserInfo getUserInfo() {
    return userInfo;
  }

  public UserInfoRequest(UserInfo user, RequestType type) {
    super(type);
    setUserInfo(user);
  }

}
