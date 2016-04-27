package ch.uzh.csg.p2p.model.request;

public class RequestHandler {
  
  public RequestHandler(){
  }
  
  public static void handleRequest(Request request){
    switch(request.getType()){
      case RECEIVE:
        handleReceiveMessage(request);
        break;
      case RETRIEVE:
        handleRetrieveMessage(request);
        break;
      case SEND:
        handleSendMessage(request);
        break;
      case STORE:
        handleStoreMessage(request);
        break;
      default:
        break;
    }
  }

  private static void handleStoreMessage(Request request) {
    // TODO Auto-generated method stub
    
  }

  private static void handleSendMessage(Request request) {
    // TODO Auto-generated method stub
    
  }

  private static void handleRetrieveMessage(Request request) {
    // TODO Auto-generated method stub
    
  }

  private static void handleReceiveMessage(Request request) {
    // TODO Auto-generated method stub
    
  }
  
}
