package ch.uzh.csg.p2p.controller;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestListener;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import net.tomp2p.dht.FutureGet;

public class FriendlistPaneController {

	private Node node;
	private MainWindowController mainWindowController;
	private FriendlistHelper friendlistHelper;
	private ListChangeListener<Friend> listChangeListener;
	private List<Friend> friendList;
	private Map<String, FriendController> controllerList;

	@FXML
	private TextField friendSearchText;
	@FXML
	private VBox friendlist;
	@FXML
	private VBox searchResultList;
  
  private Label currentChatLabel;

  public FriendlistPaneController(Node node, MainWindowController mainWindowController) {
    this.node = node;
    this.mainWindowController = mainWindowController;
    currentChatLabel = null;
    this.friendlistHelper = new FriendlistHelper(this.node);
    controllerList = new HashMap<String, FriendController>();
    this.friendList = new ArrayList<Friend>();
    listChangeListener = new ListChangeListener<Friend>() {
        public void onChanged(ListChangeListener.Change change) {
            initializeFriendlist(node);
        }
    };
    node.registerForFriendListUpdates(listChangeListener);
  }

  public void sendFriendRequest(User user, Node node) {
    FriendRequest request = new FriendRequest(node.getUser().getPeerAddress(),
            node.getUser().getUsername(), user.getUsername(), RequestType.SEND);
    RequestHandler.handleRequest(request, node);
}

  @FXML
  public void searchFriendHandler()
          throws ClassNotFoundException, IOException, LineUnavailableException {
      Platform.runLater(new Runnable() {
          public void run() {
              mainWindowController.showFriendSearchResultPane();
              searchResultList.getChildren().clear();
              try {
                  RequestListener<User> requestListener = new RequestListener<User>(node){
                      @Override
                      public void operationComplete(FutureGet futureGet) throws Exception {
                          if(futureGet != null && futureGet.isSuccess() && futureGet.data() != null) {
                              final User user = (User) futureGet.data().object();
                              //  Only show user in search results, if it's not myself
                              if(!user.getPeerAddress().equals(node.getPeer().peerAddress())) {
                                  final HBox hBox = new HBox();
                                  hBox.setSpacing(40);
  
                                  Label label = new Label(user.getUsername());
                                  label.getStyleClass().add("label");
                                  hBox.getChildren().add(label);
                                  boolean alreadyFriend = true;
                                  alreadyFriend = friendlistHelper.checkAlreadyFriend(user.getUsername());
                                  Button button = new Button("Send friend request");
                                  button.getStyleClass().add("btn");
                                  button.getStyleClass().add("friendRequestBtn");
                                  button.setOnAction(new EventHandler<ActionEvent>() {
  
                                      public void handle(ActionEvent event) {
                                          sendFriendRequest(user, node);
                                          searchResultList.getChildren().clear();
                                          mainWindowController.showInfoPane();
                                      }
                                  });
                                  if (alreadyFriend) {
                                      button.setDisable(true);
                                      button.setVisible(false);
                                  }
                                  hBox.getChildren().add(button);
                                  Platform.runLater(new Runnable() {

                                      public void run() {
                                          searchResultList.getChildren().add(hBox);
                                      }
                                      
                                  });
                                  
                              }
                              friendSearchText.setText("");
                          }
                      }
                  };
                  
                  LoginHelper.retrieveUser(friendSearchText.getText(), node, requestListener);
                  
              } catch (LineUnavailableException e1) {
                  e1.printStackTrace();
              }
          }
      });
  }
  
  public void initializeFriendlist(final Node node) {
    Platform.runLater(new Runnable() {
        public void run() {
            for (Friend f : node.getFriendList()) {
                if(!friendList.contains(f)) {
                    friendList.add(f);
                    addUserToFriendList(f);
                }
            }
            // TODO: add asynchronous task to look for status changes
            
        }
    }); 
}

	public void addUserToFriendList(Friend friend) {
		final Friend f = friend;
		Platform.runLater(new Runnable() {
			public void run() {
			    FXMLLoader loader;
			    loader = new FXMLLoader(MainWindow.class.getResource("FriendPane.fxml"));
			    
			    FriendController controller = new FriendController(mainWindowController);
			    loader.setController(controller);
			    
			    AnchorPane friend = new AnchorPane();			   
			    try {
			      friend = loader.load();
              } catch (IOException e) {
                  e.printStackTrace();
              }
				controller.setName(f.getName());
				// TODO: Task erstellen, der immer wieder online status abfragt
				controller.setOnline();
				
				friend.getStyleClass().add("friendlistLabel");
				//TODO: set width automatically
				controller.friendName.setMinWidth(255.0);
				friendlist.getChildren().add(friend);
				f.addObserver(controller);
				controllerList.put(f.getName(), controller);
			}
		});
	}

	protected void rejectFriendship(FriendRequest r) {
      final FriendRequest req = r;
      Platform.runLater(new Runnable() {
          public void run() {
              mainWindowController.setMainPaneTop(null);
              FriendRequest request = new FriendRequest(node.getPeer().peerAddress(),
                      node.getUser().getUsername(), req.getSenderName(), RequestType.SEND);
              request.setStatus(RequestStatus.REJECTED);
              RequestHandler.handleRequest(request, node);
          }
      });
  }

	protected void acceptFriendship(FriendRequest r) {
      final FriendRequest req = r;
      Platform.runLater(new Runnable() {
          public void run() {
              mainWindowController.setMainPaneTop(null);
              FriendRequest request = new FriendRequest(node.getPeer().peerAddress(),
                      node.getUser().getUsername(), req.getSenderName(), RequestType.SEND);
              request.setReceiverAddress(req.getSenderPeerAddress());
              request.setStatus(RequestStatus.ACCEPTED);
              RequestHandler.handleRequest(request, node);
              Friend friend = new Friend(req.getSenderPeerAddress(), req.getSenderName());
              friendlistHelper.storeFriend(friend);
              node.addFriend(friend);
          }
      });
  }

	public void friendshipRejected(Friend f) {
		Platform.runLater(new Runnable() {
			public void run() {
				mainWindowController.setRightPaneTop(null);
			}
		});
		mainWindowController.friendshipRejected(f.getName());
	}

	public void friendshipAccepted(Friend f) {
		final Friend friend = f;
		Platform.runLater(new Runnable() {
			public void run() {
				mainWindowController.setMainPaneTop(null);
				friendlistHelper.storeFriend(friend);
				node.addFriend(friend);
			}
		});
		mainWindowController.friendshipAccepted(f.getName());
	}

	@FXML
	public void leaveChatHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.showInfoPane();
	}

}
