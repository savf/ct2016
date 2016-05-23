package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.FriendshipStatus;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.UserInfo;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.FutureGetListener;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.tomp2p.connection.PeerBean;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.message.DataMap;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

public class FriendlistPaneController {

	private Node node;
	private MainWindowController mainWindowController;
	private FriendlistHelper friendlistHelper;
	private ListChangeListener<Friend> listChangeListener;
	private List<Friend> friendList;
	public Map<String, FriendlistItemController> friendlistItemControllerList;

	@FXML
	private TextField friendSearchText;
	@FXML
	private VBox friendlistVBox;
	@FXML
	private VBox searchResultList;

	private Label currentChatLabel;

	public FriendlistPaneController(Node node, MainWindowController mainWindowController) {
		this.node = node;
		this.mainWindowController = mainWindowController;
		currentChatLabel = null;
		this.friendlistHelper = new FriendlistHelper(this.node);
		friendlistItemControllerList = new HashMap<String, FriendlistItemController>();
		this.friendList = new ArrayList<Friend>();
		listChangeListener = new ListChangeListener<Friend>() {
			public void onChanged(ListChangeListener.Change change) {
				initializeFriendlist(node);
			}
		};
		node.registerForFriendListUpdates(listChangeListener);
	}

	public void sendFriendRequest(UserInfo user, Node node) {
	    Friend friend1 = new Friend(user.getPeerAddress(), user.getUserName());
	    friend1.setFriendshipStatus(FriendshipStatus.WAITING);
	    friendlistHelper.storeFriend(friend1, node.getUser().getUsername());
	    Friend friend2 = new Friend(node.getPeer().peerAddress(), node.getUser().getUsername());
	    friend2.setFriendshipStatus(FriendshipStatus.WAITING);
	    friendlistHelper.storeFriend(friend2, user.getUserName());
	    
		FriendRequest request = new FriendRequest(node.getUser().getPeerAddress(),
				node.getUser().getUsername(), user.getUserName(), RequestType.SEND);
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
					FutureGetListener<UserInfo> requestListener = new FutureGetListener<UserInfo>(node) {
						@Override
						public void operationComplete(FutureGet futureGet) throws Exception {
							if (futureGet != null && futureGet.isSuccess()
									&& futureGet.data() != null) {
							    Object o = new UserInfo();
							    try{
							    Iterator<Data> i= futureGet.dataMap().values().iterator();
	                            o = i.next().object();
							    }
							    catch(Exception e){
							      e.printStackTrace();
							    }
							    final UserInfo user = (UserInfo) o;
							  
								//final User user = (User) futureGet.data().object();
								// Only show user in search results, if it's not myself
								if (user.getPeerAddress() != null && !user.getPeerAddress().equals(node.getPeer().peerAddress())) {
									final HBox hBox = new HBox();
									hBox.setSpacing(40);

									Label label = new Label(user.getUserName());
									label.getStyleClass().add("label");
									hBox.getChildren().add(label);
									boolean alreadyFriend = true;
									alreadyFriend =
											friendlistHelper.checkAlreadyFriend(user.getUserName());
									Button button = new Button("Send friend request");
									button.getStyleClass().add("btn");
									button.getStyleClass().add("friendRequestBtn");
									button.setOnAction(new EventHandler<ActionEvent>() {

										public void handle(ActionEvent event) {
											sendFriendRequest(user, node);
											searchResultList.getChildren().clear();
											mainWindowController.showNotificationPane();
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
					if (!friendSearchText.getText().equals("")) {
						LoginHelper.retrieveUserInfo(friendSearchText.getText(), node, requestListener);
					} else {
						mainWindowController.alertWidthHeight();
					}

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
					if (!friendList.contains(f) && f.getFriendshipStatus().equals(FriendshipStatus.ACCEPTED)) {
						friendList.add(f);
						addUserToFriendList(f);
					}
				}
			}
		});
	}

	public void addUserToFriendList(Friend friend) {
	  if(!friendlistItemControllerList.containsKey(friend.getName())){
		final Friend f = friend;
		Platform.runLater(new Runnable() {
			public void run() {
				FXMLLoader loader;
				loader = new FXMLLoader(MainWindow.class.getResource("FriendlistItem.fxml"));

				FriendlistItemController friendlistItemController =
						new FriendlistItemController(mainWindowController);
				loader.setController(friendlistItemController);

				AnchorPane friendlistItem = new AnchorPane();
				try {
					friendlistItem = loader.load();
				} catch (IOException e) {
					e.printStackTrace();
				}
				friendlistItemController.setName(f.getName());
				friendlistItemController.setOnline();

				friendlistVBox.getChildren().add(friendlistItem);
				f.addObserver(friendlistItemController);
				friendlistItemControllerList.put(f.getName(), friendlistItemController);
			}
		});
	  }
	}

	protected void rejectFriendship(FriendRequest r) {
		final FriendRequest req = r;
		Platform.runLater(new Runnable() {
			public void run() {
				FriendRequest request = new FriendRequest(node.getPeer().peerAddress(),
						node.getUser().getUsername(), req.getSenderName(), RequestType.SEND);
				request.setReceiverAddress(req.getSenderPeerAddress());
				request.setStatus(RequestStatus.REJECTED);
				
				Friend myFriend = new Friend(req.getSenderPeerAddress(), req.getSenderName());
                myFriend.setFriendshipStatus(FriendshipStatus.REJECTED);
                friendlistHelper.removeFriend(myFriend, req.getReceiverName());
                Friend hisFriend = new Friend(req.getReceiverAddress(), req.getReceiverName());
                hisFriend.setFriendshipStatus(FriendshipStatus.REJECTED);
                friendlistHelper.storeFriend(hisFriend, req.getSenderName());
				
				RequestHandler.handleRequest(request, node);
			}
		});
	}

	protected void acceptFriendship(FriendRequest r) {
		final FriendRequest req = r;
		Platform.runLater(new Runnable() {
			public void run() {
				FriendRequest request = new FriendRequest(node.getPeer().peerAddress(),
						node.getUser().getUsername(), req.getSenderName(), RequestType.SEND);
				request.setReceiverAddress(req.getSenderPeerAddress());
				request.setStatus(RequestStatus.ACCEPTED);
				
				Friend myFriend = new Friend(req.getSenderPeerAddress(), req.getSenderName());
				myFriend.setFriendshipStatus(FriendshipStatus.ACCEPTED);
				friendlistHelper.storeFriend(myFriend, req.getReceiverName());
				Friend hisFriend = new Friend(req.getReceiverAddress(), req.getReceiverName());
				hisFriend.setFriendshipStatus(FriendshipStatus.ACCEPTED);
				friendlistHelper.storeFriend(hisFriend, req.getSenderName());
				
				node.addFriend(myFriend);
				RequestHandler.handleRequest(request, node);
			}
		});
	}

	public void friendshipRejected(Friend f) {
		Platform.runLater(new Runnable() {
			public void run() {
			  if(node.getFriend(f.getName())!= null){
			    node.removeFriend(f);
			  }
			  friendlistHelper.removeFriend(f, node.getUser().getUsername());
			  
				mainWindowController.setRightTopPane(null);
			}
		});
		mainWindowController.friendshipRejected(f.getName());
	}

	public void friendshipAccepted(Friend f) {
		final Friend friend = f;
		Platform.runLater(new Runnable() {
			public void run() {
			  friend.setFriendshipStatus(FriendshipStatus.ACCEPTED);
				friendlistHelper.storeFriend(friend, node.getUser().getUsername());
				node.addFriend(friend);
			}
		});
		mainWindowController.friendshipAccepted(f.getName());
	}

	@FXML
	public void leaveChatHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.showNotificationPane();
	}

}
