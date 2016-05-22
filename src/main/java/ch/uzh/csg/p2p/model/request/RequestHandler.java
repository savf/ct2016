package ch.uzh.csg.p2p.model.request;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.controller.MainWindowController;
import ch.uzh.csg.p2p.helper.AudioUtils;
import ch.uzh.csg.p2p.helper.EncoderUtils;
import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.helper.VideoUtils;
import ch.uzh.csg.p2p.model.AudioMessage;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.OnlineStatus;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.VideoMessage;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;


public class RequestHandler {

	private static Logger log = LoggerFactory.getLogger("RequestHandler");
	private static MainWindowController mainWindowController;
	private final static int DEFAULTPORT = 54000;
	private final static String BOOTSTRAPNODE = "Bootstrapnode";
	private static final String FRIEND_PREFIX = "Friend";
	private static String USER_PREFIX = "user_";
	private final static String ADDRESS_PREFIX = "address_";

	public RequestHandler() {}

	public static Object handleRequest(Request request, Node node) {
		try {
			return handleRequest(request, node, null, null);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object handleRequest(Request request, Node node,
			FutureGetListener requestListener) throws LineUnavailableException {
		try{
		  return handleRequest(request, node, requestListener, null);
		}
		catch (LineUnavailableException e) {
          e.printStackTrace();
      }
      return null;
	}
	
	public static Object handleRequest(Request request, Node node,
        FutureGetListener requestListener, BaseFutureListener genericListener) throws LineUnavailableException {
    switch (request.getType()) {
        case RECEIVE:
            try {
                return handleReceive(request, node);
            } catch (ClassNotFoundException e2) {
                e2.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            } catch (LineUnavailableException e2) {
                e2.printStackTrace();
            }
        case RETRIEVE:
            try {
                return handleRetrieve(request, node, requestListener);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        case SEND:
            try {
                return handleSend(request, node, genericListener);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        case STORE:
            try {
                return handleStore(request, node, genericListener);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        default:
            return null;
    }
}

	private static Boolean handleStore(Request request, Node node, BaseFutureListener genericListener)
			throws IOException, ClassNotFoundException, InterruptedException {
		if (request instanceof UserRequest) {
			UserRequest r = (UserRequest) request;
			final User user = r.getUser();

			// TODO: Direct replication if wanted.. should look somewhat like this
			/*
			 * PutBuilder putBuilder =
			 * node.getPeer().put(Number160.createHash(LoginHelper.USER_PREFIX +
			 * r.getUser().getUsername())).data(new Data(user)); JobScheduler replication = new
			 * JobScheduler(node.getPeer().peer()); Shutdown shutdown =
			 * replication.start(putBuilder, 1000, 3, new AutomaticFuture() { public void
			 * futureCreated(BaseFuture future) { System.out.println("put again..."); } });
			 * 
			 * shutdown.shutdown();
			 * 
			 * replication.shutdown().awaitUninterruptibly();
			 */

			FuturePut future = node.getPeer()
					.put(Number160.createHash(USER_PREFIX + r.getUser().getUsername()))
					.data(new Data(user)).start();
			future.addListener(new BaseFutureAdapter<FuturePut>() {
	          public void operationComplete(FuturePut future) throws Exception {
	              log.info("User " + user.getUsername() + " put into DHT, with success: " + future.isSuccess());
	          }
	      });
			return true;
		}
		if (request instanceof MessageRequest) {
			MessageRequest r = (MessageRequest) request;
		}
		if (request instanceof FriendRequest) {
			final FriendRequest r = (FriendRequest) request;
			final Node node1 = node;
			User user = node.getUser();
			final String username = user.getUsername();
			user.addFriend(r.getSenderName());
			// TODO: necessary?
			FutureRemove futureRemove =
					node.getPeer()
							.remove(Number160
									.createHash(USER_PREFIX + user.getUsername()))
							.start();
			futureRemove.addListener(new BaseFutureListener<FutureRemove>(){

        @Override
        public void operationComplete(FutureRemove future) throws Exception {
          FuturePut futurePut = node.getPeer()
              .put(Number160.createHash(USER_PREFIX + user.getUsername()))
              .data(new Data(user)).start();
      futurePut.addListener(new BaseFutureAdapter<FuturePut>() {
          public void operationComplete(FuturePut future) throws Exception {
              log.info("User " + username + " put " + r.getSenderName() + " into DHT as friend, with success: " + future.isSuccess());
          }
      });
          
        }

        @Override
        public void exceptionCaught(Throwable t) throws Exception {
          log.error(t.getMessage()); 
        }		  
			});
		}
		if (request instanceof BootstrapRequest) {
		  BootstrapRequest req = (BootstrapRequest) request;
			User user = new User(req.getSenderName(), null, node.getPeer().peerAddress());
			FuturePut future = node.getPeer().put(Number160.createHash(BOOTSTRAPNODE)).data(new Data(req.getBootstrapNodeIP())).start();
			//Attention, this listener could be assigned wrongly
			// TODO: assure that only FuturePut listener gets assigned
			future.addListener(genericListener);
			log.info("BootstrapNode created from user: " + node.getUser().getUsername());
			return true;
		}
		return false;
	}

	private static Boolean handleSend(Request request, Node node, BaseFutureListener genericListener)
			throws ClassNotFoundException, IOException, InterruptedException, LineUnavailableException {
		final Node n = node;
		if (request instanceof MessageRequest) {
			final MessageRequest r = (MessageRequest) request;
			PeerAddress peerAddress = null;
			if (r.getMessage() != null && r.getMessage().getReceiverAddress() != null) {
				peerAddress = r.getMessage().getReceiverAddress();
				
				n.getPeer().peer().sendDirect(peerAddress).object(r.getMessage()).start();
			} else {
				FutureGetListener<User> requestListener = new FutureGetListener<User>(node) {
					@Override
					public void operationComplete(FutureGet futureGet) throws Exception {
						if (futureGet != null && futureGet.isSuccess()
								&& futureGet.data() != null) {
							User user = (User) futureGet.data().object();
							log.info(r.getMessage().getSenderID() + "sent Message of type "
									+ r.getMessage().getClass() + " " + r.getType().toString()
									+ "to: " + user.getPeerAddress().toString());
							node.getPeer().peer().sendDirect(user.getPeerAddress()).object(r.getMessage())
									.start();
						} else {
							log.error("FutureGet for user {} was successful, but data is null!",
									r.getMessage().getReceiverID());
						}
					}
				};
				LoginHelper.retrieveUser(r.getMessage().getReceiverID(), node, requestListener);
			}

		}

		if (request instanceof BootstrapRequest) {
			BootstrapRequest r = (BootstrapRequest) request;

			InetAddress address = Inet4Address.getByName(r.getBootstrapNodeIP());
			FutureDiscover futureDiscover =
					n.getPeer().peer().discover().inetAddress(address).ports(DEFAULTPORT).start();
			futureDiscover.addListener(new BaseFutureListener<FutureDiscover>(){

        @Override
        public void operationComplete(FutureDiscover future) throws Exception {
          PeerAddress master = future.peerAddress();
          System.out.println(future.reporter());
          FutureBootstrap futureBootstrap =
              n.getPeer().peer().bootstrap().peerAddress(master).ports(DEFAULTPORT).start();
          // Attention, the listener could be wrongly assigned to the future object
          // TODO: assure that the genericListener is a FutureBootstrap listener
          futureBootstrap.addListener(genericListener);
        }

        @Override
        public void exceptionCaught(Throwable t) throws Exception {
          log.error(t.getMessage());
        }
			  
			});
		}
		if (request instanceof AudioRequest) {
			final AudioRequest r = (AudioRequest) request;
			PeerAddress peerAddress = null;
			if (r.getReceiverAddress() != null) {
				peerAddress = r.getReceiverAddress();
				n.getPeer().peer().sendDirect(peerAddress).object(r).start();
			} else {

				FutureGetListener<User> requestListener = new FutureGetListener<User>(node) {
					@Override
					public void operationComplete(FutureGet futureGet) throws Exception {
						if (futureGet != null && futureGet.isSuccess()
								&& futureGet.data() != null) {
							User user = (User) futureGet.data().object();
							node.getPeer().peer().sendDirect(user.getPeerAddress()).object(r)
									.start();
						}
					}
				};
				LoginHelper.retrieveUser(r.getReceiverName(), node, requestListener);
			}

			return true;
		}
		if (request instanceof VideoRequest) {
			final VideoRequest videoRequest = (VideoRequest) request;
			PeerAddress peerAddress = null;
			if (videoRequest.getReceiverAddress() != null) {
				peerAddress = videoRequest.getReceiverAddress();
				n.getPeer().peer().sendDirect(peerAddress).object(videoRequest).start();
			} else {
				FutureGetListener<User> requestListener = new FutureGetListener<User>(node) {
					@Override
					public void operationComplete(FutureGet futureGet) throws Exception {
						if (futureGet != null && futureGet.isSuccess()
								&& futureGet.data() != null) {
							User user = (User) futureGet.data().object();
							node.getPeer().peer().sendDirect(user.getPeerAddress())
									.object(videoRequest).start();
						}
					}
				};

				LoginHelper.retrieveUser(videoRequest.getReceiverName(), node, requestListener);
			}

			n.getPeer().peer().sendDirect(peerAddress).object(videoRequest).start();
			return true;
		}

		if (request instanceof FriendRequest) {
			final FriendRequest r = (FriendRequest) request;
			PeerAddress peerAddress = null;
			if (r.getReceiverAddress() != null) {
				peerAddress = r.getReceiverAddress();
				n.getPeer().peer().sendDirect(peerAddress).object(r).start();
			} else {
				FutureGetListener<User> requestListener = new FutureGetListener<User>(node){
					@Override
					public void operationComplete(FutureGet futureGet) throws Exception {
						if(futureGet != null && futureGet.isSuccess() && futureGet.data() != null) {
							User user = (User) futureGet.data().object();
							node.getPeer().peer().sendDirect(user.getPeerAddress()).object(r).start();
						}
					}
				};
				
				LoginHelper.retrieveUser(r.getReceiverName(), node, requestListener);
			}
			
			return true;
		}
		if(request instanceof OnlineStatusRequest){
		  final OnlineStatusRequest r = (OnlineStatusRequest) request;
		  FutureDirect future = n.getPeer().peer().sendDirect(r.getReceiverAddress()).object(r).start();
		  if(genericListener != null){
		    // Attention, this could assign a wrong BaseFutureListener to the future object. 
		    // TODO: Assure the listener has type FutureDirect
		  future.addListener(genericListener);
		  }
		}
		return true;
	}

	private static Object handleRetrieve(Request request, Node node,
			FutureGetListener requestListener)
			throws ClassNotFoundException, IOException, InterruptedException {
		if (request instanceof UserRequest) {
			UserRequest r = (UserRequest) request;
			retrieveUser(r.getUser(), node, requestListener);
		}
		if (request instanceof MessageRequest) {
			MessageRequest r = (MessageRequest) request;
		}
		if (request instanceof FriendRequest) {
			FriendRequest r = (FriendRequest) request;
			retrieveFriend(r.getSenderName(), node, requestListener);
		}
		if (request instanceof BootstrapRequest) {
			FutureGet futureGet = node.getPeer().get(Number160.createHash(BOOTSTRAPNODE)).start();
			futureGet.addListener(requestListener);
		}
		return null;
	}

	private static void retrieveFriend(String senderName, Node node,
			FutureGetListener<Friend> requestListener) throws ClassNotFoundException, IOException {
		FutureGet futureGet =
				node.getPeer().get(Number160.createHash(FRIEND_PREFIX + senderName)).start();
		futureGet.addListener(requestListener);
	}

	private static Message handleReceive(Request request, Node node)
			throws IOException, LineUnavailableException, ClassNotFoundException {

		if (request instanceof MessageRequest) {
			MessageRequest r = (MessageRequest) request;
			Message message = r.getMessage();
			if (message instanceof AudioMessage) {
				AudioMessage audioMessage = (AudioMessage) message;
				try {
					AudioUtils
							.playAudio(EncoderUtils.byteArrayToByteBuffer(audioMessage.getData()));
				} catch (LineUnavailableException e) {
					log.error("AudioMessage could not be played: " + e);
				}
			} else if (message instanceof ChatMessage) {
				ChatMessage chatMessage = (ChatMessage) message;
				mainWindowController.chatPaneController
						.addReceivedMessage(chatMessage.getSenderID(), chatMessage.getData());
			} else if (message instanceof VideoMessage) {
				log.info("handleReceive VideoMessage");
				VideoMessage videoMessage = (VideoMessage) message;
				VideoUtils.playVideo(videoMessage.getData());
			}
			log.info(message.getReceiverID() + " received message: " + message.toString()
					+ " from: " + message.getSenderID().toString());
			return message;
		}

		if (request instanceof BootstrapRequest) {
			/*
			 * BootstrapRequest r = (BootstrapRequest) request; log.info(
			 * "BootstrapNode received Bootstrap Request from: " +
			 * r.getSenderPeerAddress().toString()); log.info("new node"); FutureBootstrap
			 * futureBootstrap =
			 * node.getPeer().peer().bootstrap().peerAddress(r.getSenderPeerAddress()).start();
			 */
		  BootstrapRequest r = (BootstrapRequest) request;
		}

		if (request instanceof AudioRequest) {
			AudioRequest audioRequest = (AudioRequest) request;
			switch (audioRequest.getStatus()) {
				case WAITING:
					mainWindowController.audioPaneController.askAudioCall(audioRequest);
					break;
				case ACCEPTED:
					mainWindowController.audioPaneController.startAudioCall();
					break;
				case REJECTED:
					mainWindowController.audioPaneController.audioCallRejected(audioRequest);
					break;
				case ABORTED:
					mainWindowController.audioPaneController.audioCallAborted();
					break;
				default:
					break;
			}
		}

		if (request instanceof FriendRequest) {
			FriendRequest friendRequest = (FriendRequest) request;
			Friend f =
					new Friend(friendRequest.getSenderPeerAddress(), friendRequest.getSenderName());
			switch (friendRequest.getStatus()) {
				case WAITING:
					mainWindowController.askFriend(friendRequest);
					break;
				case ACCEPTED:
					mainWindowController.friendlistPaneController.friendshipAccepted(f);
					break;
				case REJECTED:
					mainWindowController.friendlistPaneController.friendshipRejected(f);
					break;
				case ABORTED:
					mainWindowController.friendlistPaneController.friendshipRejected(f);
					break;
				default:
					break;
			}
		}

		if (request instanceof VideoRequest) {
			VideoRequest videoRequest = (VideoRequest) request;
			switch (videoRequest.getStatus()) {
				case WAITING:
					mainWindowController.videoPaneController.askVideoCall(videoRequest);
					break;
				case ACCEPTED:
					mainWindowController.videoPaneController.startVideoCall();
					break;
				case REJECTED:
					mainWindowController.videoPaneController.videoCallRejected(videoRequest);
					break;
				case ABORTED:
					mainWindowController.videoPaneController.videoCallAborted();
					break;
				default:
					break;
			}
		}
		if(request instanceof OnlineStatusRequest){
		  OnlineStatusRequest r = (OnlineStatusRequest) request;
		  switch (r.getStatus()){
		    case WAITING:
		      OnlineStatusRequest req;
		      if(!r.getReceiverName().equals(node.getUser().getUsername())){
		        // then, the peerAddress of the friend has changed and been reassigned to this node
		        req = new OnlineStatusRequest(r.getSenderAddress(), r.getReceiverName(), r.getSenderName(), RequestType.SEND);
		        req.setOnlineStatus(OnlineStatus.ONLINE);
		        req.setStatus(RequestStatus.REJECTED);		        
		      }
		      else{
		      node.getFriend(r.getSenderName()).setStatus(OnlineStatus.ONLINE);
		      req = new OnlineStatusRequest(r.getSenderAddress(), r.getReceiverName(), r.getSenderName(), RequestType.SEND);
		      req.setOnlineStatus(OnlineStatus.ONLINE);
		      req.setStatus(RequestStatus.ACCEPTED);
		      }
		      handleRequest(req, node);
		      break;
		    case ACCEPTED:
		      if(node.getFriend(r.getSenderName())!= null){
		        if(r.hasChangedPeerAddress()){
		          node.getFriend(r.getSenderName()).setPeerAddress(r.getSenderAddress());
		        }
		      node.getFriend(r.getSenderName()).setStatus(r.getOnlineStatus());
		      }
		      break;
		    case REJECTED:
		      if(node.getFriend(r.getSenderName())!= null){
		      node.getFriend(r.getSenderName()).setStatus(OnlineStatus.OFFLINE);
		      }
		      break;
		    case ABORTED:
		      if(node.getFriend(r.getSenderName())!= null){
		      node.getFriend(r.getSenderName()).setStatus(OnlineStatus.OFFLINE);
		      }
		      break;
		    default:
		      break;
		  }
		}

		return null;
	}

	private static void retrieveUser(User user, Node node, FutureGetListener<User> requestListener)
			throws ClassNotFoundException, IOException {
	  // try out with a not fast get
		FutureGet futureGet = node.getPeer()
				.get(Number160.createHash(USER_PREFIX + user.getUsername())).start();
		futureGet.addListener(requestListener);
	}

	public static void setMainWindowController(MainWindowController mWC) {
		mainWindowController = mWC;
	}

}
