package ch.uzh.csg.p2p.model.request;

import ch.uzh.csg.p2p.model.Friend;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureDirect;

public class StatusListener<Type> implements BaseFutureListener<FutureDirect>{
  
  private Type variableToSet;
  /*private Friend friend;
  
  public StatusListener(Friend friend) {
    this.friend = friend;
  } */
  public StatusListener(){
    
  }
  
  public StatusListener(Type variableToSet){
    this.variableToSet = variableToSet;
  }
  
  @Override
  public void operationComplete(FutureDirect future) throws Exception {
    if(future != null && future.isSuccess()) {
      this.variableToSet = null;
  }
  else {
      this.variableToSet = null;
  }
  }

  @Override
  public void exceptionCaught(Throwable t) throws Exception {
    // TODO Auto-generated method stub
    
  }

}
