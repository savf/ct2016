package ch.uzh.csg.p2p;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;

import javax.sound.sampled.LineUnavailableException;

import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.AudioInfo;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.FriendshipStatus;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.OnlineStatus;
import ch.uzh.csg.p2p.model.OnlineStatusTask;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.UserInfo;
import ch.uzh.csg.p2p.model.VideoInfo;
import ch.uzh.csg.p2p.model.request.AudioRequest;
import ch.uzh.csg.p2p.model.request.BootstrapRequest;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.OnlineStatusRequest;
import ch.uzh.csg.p2p.model.request.Request;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.UserInfoRequest;
import ch.uzh.csg.p2p.model.request.VideoRequest;

public class Node extends Observable {

	protected static final int MAX_TRIES = 0;
	protected static final long TRY_AGAIN_TIME_WINDOW = 7000;
	protected final String BOOTSTRAPNODE = "Bootstrapnode";
	private final int DEFAULTPORT = 54000;
	private final long TIMERDELAY = 10000;
	private boolean running = true;

	private Logger log;
	private User user;
	private Timer onlineStatusTaskTimer;
	protected PeerDHT peer;

	public Node(int nodeId, String ip, String username, String password, boolean bootstrapNode,
			Observer nodeReadyObserver) throws IOException, LineUnavailableException,
			ClassNotFoundException {
		log = LoggerFactory.getLogger("Node of user " + username);
		user = new User(username, password, null);
		int id = ((Long) System.currentTimeMillis()).intValue();

		if (nodeReadyObserver != null) {
			addObserver(nodeReadyObserver);
		}
		// if not a BootstrapNode
		createPeerAndInitiateUser(id, username, password, bootstrapNode, ip);
	}

	private void nodeReady() {
		try {
			storeUpdatedUserInfo();
			loadStoredDataFromDHT();
		} catch (UnsupportedEncodingException e) {

		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers(this);
		startOnlineStatusTask();
	}

	private void storeUpdatedUserInfo() throws LineUnavailableException {
		UserInfo info = new UserInfo(peer.peerAddress(), user.getUsername(), user.getPassword());
		UserInfoRequest request = new UserInfoRequest(info, RequestType.STORE);
		final long time = System.currentTimeMillis();
		BaseFutureListener<FuturePut> futurePutListener = new BaseFutureListener<FuturePut>() {
			@Override
			public void operationComplete(FuturePut future) throws Exception {
				if (!future.isSuccess()) {
					long timeNow = System.currentTimeMillis();
					if (timeNow - time < TRY_AGAIN_TIME_WINDOW) {
						tryAgain(request, this);
					}
				}
			}

			@Override
			public void exceptionCaught(Throwable t) throws Exception {
				log.error(t.getMessage());
			}

		};
		RequestHandler.handleRequest(request, this, futurePutListener);
	}

	public void loadStoredDataFromDHT() throws UnsupportedEncodingException,
			LineUnavailableException {
		loadFriendlistFromDHTAndUpdatePeerAddresses();
	}

	private void loadMessagesFromDHT() throws LineUnavailableException {
		final long time = System.currentTimeMillis();
		final Node node = this;
		MessageRequest messageRequest = new MessageRequest();
		messageRequest.setType(RequestType.RETRIEVE);
		messageRequest.setSenderName(user.getUsername());

		BaseFutureListener<FutureGet> messageRetrieveListener =
				new BaseFutureListener<FutureGet>() {

					@Override
					public void operationComplete(FutureGet futureGet) throws Exception {
						if (futureGet != null && futureGet.isSuccess()) {
							Iterator<Data> iterator = futureGet.dataMap().values().iterator();
							while (iterator.hasNext()) {
								ChatMessage chatMessage = (ChatMessage) iterator.next().object();
								user.addChatMessage(chatMessage);
							}
						}
					}

					@Override
					public void exceptionCaught(Throwable t) throws Exception {}

				};
		RequestHandler.handleRequest(messageRequest, this, messageRetrieveListener);
	}

	private void loadAudioCallsFromDHT() throws LineUnavailableException {
		final long time = System.currentTimeMillis();
		final Node node = this;
		AudioRequest audioRequest = new AudioRequest();
		audioRequest.setType(RequestType.RETRIEVE);
		audioRequest.setSenderName(user.getUsername());

		BaseFutureListener<FutureGet> audioRetrieveListener = new BaseFutureListener<FutureGet>() {

			@Override
			public void operationComplete(FutureGet futureGet) throws Exception {
				if (futureGet != null && futureGet.isSuccess()) {
					Iterator<Data> iterator = futureGet.dataMap().values().iterator();
					while (iterator.hasNext()) {
						AudioInfo audioInfo = (AudioInfo) iterator.next().object();
						user.addAudioInfo(audioInfo);
					}
				}
			}

			@Override
			public void exceptionCaught(Throwable t) throws Exception {}

		};
		RequestHandler.handleRequest(audioRequest, this, audioRetrieveListener);
	}

	private void loadVideoCallsFromDHT() throws LineUnavailableException {
		final long time = System.currentTimeMillis();
		final Node node = this;
		VideoRequest videoRequest = new VideoRequest();
		videoRequest.setType(RequestType.RETRIEVE);
		videoRequest.setSenderName(user.getUsername());

		BaseFutureListener<FutureGet> videoRetrieveListener = new BaseFutureListener<FutureGet>() {

			@Override
			public void operationComplete(FutureGet futureGet) throws Exception {
				if (futureGet != null && futureGet.isSuccess()) {
					Iterator<Data> iterator = futureGet.dataMap().values().iterator();
					while (iterator.hasNext()) {
						VideoInfo videoInfo = (VideoInfo) iterator.next().object();
						user.addVideoInfo(videoInfo);
					}
				}
			}

			@Override
			public void exceptionCaught(Throwable t) throws Exception {}

		};
		RequestHandler.handleRequest(videoRequest, this, videoRetrieveListener);
	}

	private void initiateUser(final String username, final String password)
			throws ClassNotFoundException, LineUnavailableException, IOException {
		Node node = this;
		final long time = System.currentTimeMillis();
		BaseFutureListener<FutureGet> userExistsListener = new BaseFutureListener<FutureGet>() {
			@Override
			public void operationComplete(FutureGet futureGet) throws Exception {
				if (futureGet != null && futureGet.isSuccess() && futureGet.data() != null) {
					// user already exist --> only update address
					LoginHelper.updatePeerAddress(node, username);
				} else {
					long timeNow = System.currentTimeMillis();
					if (timeNow - time < TRY_AGAIN_TIME_WINDOW) {
						tryAgainRetrieveUserInfo(username, node, this);
					} else {
						// user does not exist --> add user
						LoginHelper.saveUsernamePassword(node, username, password);
						UserInfo newUser =
								new UserInfo(node.getPeer().peerAddress(), username, password);
						node.setUserInfo(newUser);
					}
				}
				nodeReady();
			}

			@Override
			public void exceptionCaught(Throwable t) throws Exception {}
		};

		LoginHelper.retrieveUserInfo(username, this, userExistsListener);
	}

	protected void tryAgainRetrieveUserInfo(String username, Node node,
			BaseFutureListener<FutureGet> baseFutureListener) throws LineUnavailableException,
			InterruptedException {
		// TODO Auto-generated method stub
		Thread.sleep(500);
		log.debug("Node had unsuccessful UserRetrieve Request, Try again...");
		log.debug(" Search for: " + username);
		LoginHelper.retrieveUserInfo(username, node, baseFutureListener);
	}

	private void loadFriendlistFromDHTAndUpdatePeerAddresses() throws UnsupportedEncodingException,
			LineUnavailableException {
		FriendRequest request = new FriendRequest();
		request.setType(RequestType.RETRIEVE);
		request.setSenderName(user.getUsername());
		final Node node = this;
		final long time = System.currentTimeMillis();
		BaseFutureListener<FutureGet> listener = new BaseFutureListener<FutureGet>() {

			@Override
			public void operationComplete(FutureGet future) throws Exception {
				if (future != null && future.isSuccess()) {
					Iterator<Data> i = future.dataMap().values().iterator();
					while (i.hasNext()) {
						Friend f = (Friend) i.next().object();
						if (f != null) {
							if (f.getFriendshipStatus().equals(FriendshipStatus.ACCEPTED)) {
								UserInfo info = new UserInfo(f.getPeerAddress(), f.getName(), null);
								UserInfoRequest request =
										new UserInfoRequest(info, RequestType.RETRIEVE);
								BaseFutureListener<FutureGet> listener =
										new BaseFutureListener<FutureGet>() {
											@Override
											public void operationComplete(FutureGet future)
													throws Exception {
												if (future != null && future.isSuccess()) {
													UserInfo info =
															(UserInfo) future.data().object();
													if (info != null
															&& info.getPeerAddress() != null) {
														f.setPeerAddress(info.getPeerAddress());
													}
													user.addFriend(f);
												}
											}

											@Override
											public void exceptionCaught(Throwable t)
													throws Exception {}
										};
								RequestHandler.handleRequest(request, node, listener);
							} else if (f.getFriendshipStatus().equals(FriendshipStatus.REJECTED)
									|| f.getFriendshipStatus().equals(FriendshipStatus.ABORTED)) {
								getRejected(f);
							} else if (f.getFriendshipStatus().equals(FriendshipStatus.WAITING)) {
								addFriendRequestWhileAway(f);
							}
						}
					}
					announceChangedToOnlineStatus();
					// Load missed messages and calls after initiating the friend list
					loadMessagesFromDHT();
					loadAudioCallsFromDHT();
					loadVideoCallsFromDHT();
				} else {
					long timeNow = System.currentTimeMillis();
					if (timeNow - time < TRY_AGAIN_TIME_WINDOW) {
						tryAgain(request, this);
					}
				}
			}

			@Override
			public void exceptionCaught(Throwable t) throws Exception {
				log.error(t.getMessage());
			}

		};
		RequestHandler.handleRequest(request, this, listener);
	}

	protected void addFriendRequestWhileAway(Friend f) {
		FriendRequest request = new FriendRequest();
		request.setReceiverName(user.getUsername());
		request.setReceiverAddress(peer.peerAddress());
		request.setSenderName(f.getName());
		request.setSenderPeerAddress(f.getPeerAddress());
		request.setStatus(RequestStatus.WAITING);
		request.setType(RequestType.RECEIVE);
		user.addFriendRequest(request);
	}

	protected void getRejected(Friend f) {
		FriendlistHelper helper = new FriendlistHelper(this);
		helper.removeFriend(f, user.getUsername());

		FriendRequest req = new FriendRequest();
		req.setReceiverName(user.getUsername());
		req.setReceiverAddress(peer.peerAddress());
		req.setSenderName(f.getName());
		req.setSenderPeerAddress(f.getPeerAddress());
		req.setStatus(RequestStatus.REJECTED);
		req.setType(RequestType.RECEIVE);
		RequestHandler.handleRequest(req, this);
	}

	protected void startOnlineStatusTask() {
		OnlineStatusTask onlineStatusTask = new OnlineStatusTask(this);
		onlineStatusTaskTimer = new Timer();
		onlineStatusTaskTimer.scheduleAtFixedRate(onlineStatusTask, 0, TIMERDELAY);
	}

	protected void stopOnlineStatusTask() {
		if (onlineStatusTaskTimer != null) {
			onlineStatusTaskTimer.cancel();
		}
	}

	protected void createPeerAndInitiateUser(int nodeId, String username, String password,
			boolean bootstrapNode, String ip) throws IOException, ClassNotFoundException,
			LineUnavailableException {
		Bindings b = new Bindings().listenAny();
		peer =
				new PeerBuilderDHT(new PeerBuilder(new Number160(nodeId)).ports(getPort())
						.bindings(b).start()).start();

		if (bootstrapNode) {
			// Bootstrapnode
			BootstrapRequest request = new BootstrapRequest(RequestType.STORE);
			request.setBootstrapNodeIP(ip);
			final long time = System.currentTimeMillis();
			BaseFutureListener<FuturePut> futurePutListener = new BaseFutureListener<FuturePut>() {
				@Override
				public void operationComplete(FuturePut future) throws Exception {
					if (future.isSuccess()) {
						peer.peer().objectDataReply(new ObjectDataReply() {
							public Object reply(PeerAddress peerAddress, Object object)
									throws Exception {
								return handleReceivedData(peerAddress, object);
							}
						});
						// TODO: Indirect Replication or direct? Replication factor?
						new IndirectReplication(peer).autoReplication(true).replicationFactor(3)
								.start();

						initiateUser(username, password);
					} else {
						long timeNow = System.currentTimeMillis();
						if (timeNow - time < TRY_AGAIN_TIME_WINDOW) {
							tryAgain(request, this);
						}
					}
				}

				@Override
				public void exceptionCaught(Throwable t) throws Exception {
					log.error(t.getMessage());
				}

			};
			RequestHandler.handleRequest(request, this, futurePutListener);
		} else {
			peer.peer().objectDataReply(new ObjectDataReply() {
				public Object reply(PeerAddress peerAddress, Object object) throws Exception {
					return handleReceivedData(peerAddress, object);
				}
			});
			new IndirectReplication(peer).autoReplication(true).replicationFactor(3).start();

			connectToNode(ip, username, password);
		}
	}

	protected Object handleReceivedData(PeerAddress peerAddress, Object object) throws IOException,
			LineUnavailableException, ClassNotFoundException {

		log.info("Received message: " + object.toString() + " from: " + peerAddress.toString());

		if (object instanceof Message) {
			MessageRequest messageRequest = new MessageRequest();
			messageRequest.setType(RequestType.RECEIVE);
			messageRequest.setMessage((Message) object);
			RequestHandler.handleRequest(messageRequest, this);
		} else if (object instanceof Request) {
			Request r = (Request) object;
			r.setType(RequestType.RECEIVE);
			RequestHandler.handleRequest(r, this);
		} else {
			System.out.println("else");
		}

		return 0;
	}

	private void connectToNode(String knownIP, String username, String password)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		BootstrapRequest request =
				new BootstrapRequest(RequestType.SEND, user.getPeerAddress(), knownIP);
		final long time = System.currentTimeMillis();
		BaseFutureListener<FutureBootstrap> futureBootstrapListener =
				new BaseFutureListener<FutureBootstrap>() {

					@Override
					public void operationComplete(FutureBootstrap future) throws Exception {

						log.info(getUser().getUsername() + " knows: "
								+ getPeer().peerBean().peerMap().all() + " unverified: "
								+ getPeer().peerBean().peerMap().allOverflow());
						log.info("Waiting for maintenance ping");
						if (future.isSuccess()) {
							initiateUser(username, password);
						} else {
							long timeNow = System.currentTimeMillis();
							if (timeNow - time < TRY_AGAIN_TIME_WINDOW) {
								tryAgain(request, this);
							}
						}
					}

					@Override
					public void exceptionCaught(Throwable t) throws Exception {
						log.error(t.getMessage());
					}

				};
		RequestHandler.handleRequest(request, this, futureBootstrapListener);
	}

	private int getPort() {
		int port = DEFAULTPORT;
		while (portIsOpen("127.0.0.1", port, 200)) {
			port++;
		}
		log.info("Free port found, port is: " + port);
		return port;
	}

	private boolean portIsOpen(String ip, int port, int timeout) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), timeout);
			socket.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public PeerDHT getPeer() {
		return peer;
	}

	public User getUser() {
		return user;
	}

	public void setUserInfo(UserInfo info) {
		if (user != null && info != null) {
			user.setUsername(info.getUserName());
			user.setPassword(info.getPassword());
			user.setPeerAddress(info.getPeerAddress());
		} else if (info != null) {
			User newUser = new User(info.getUserName(), info.getPassword(), info.getPeerAddress());
			this.user = newUser;
		}
	}

	public void shutdown() {
		log.info("Shutting down gracefully.");
		running = false;
		if (peer != null) {
			stopOnlineStatusTask();
			try {
				announceChangedToOfflineStatus();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
	}

	public void announceChangedToOnlineStatus() throws LineUnavailableException {
		for (Friend f : user.getFriendList()) {
			OnlineStatusRequest request =
					new OnlineStatusRequest(f.getPeerAddress(), peer.peerAddress(),
							user.getUsername(), f.getName(), RequestType.SEND);
			request.setChangedPeerAddress(true);
			request.setOnlineStatus(OnlineStatus.ONLINE);
			// send new OnlineRequest, act like this is an answer to a request, and send a positive
			// answer
			// so status goes online
			request.setStatus(RequestStatus.ACCEPTED);
			final long time = System.currentTimeMillis();
			BaseFutureListener<FutureDirect> futureDirectListener =
					new BaseFutureListener<FutureDirect>() {
						@Override
						public void operationComplete(FutureDirect future) throws Exception {
							if (future != null && future.isSuccess()) {
								f.setStatus(OnlineStatus.ONLINE);
							} else {
								long timeNow = System.currentTimeMillis();
								if (timeNow - time < (TRY_AGAIN_TIME_WINDOW * 3)) {
									// this Message is very important, so it tries to send it more
									// than the normal amount
									tryAgain(request, this);
								}
							}
						}

						@Override
						public void exceptionCaught(Throwable t) throws Exception {
							log.error(t.getMessage());
						}
					};
			RequestHandler.handleRequest(request, this, futureDirectListener);
		}
	}

	private void announceChangedToOfflineStatus() throws LineUnavailableException {
		if (user.getFriendList().isEmpty()) {
			peer.shutdown();
		} else {
			int i = 1;
			for (Friend f : user.getFriendList()) {
				OnlineStatusRequest request =
						new OnlineStatusRequest(f.getPeerAddress(), peer.peerAddress(),
								user.getUsername(), f.getName(), RequestType.SEND);
				// act like we received an OnlineStatusRequest and answer with Aborted so Status
				// goes to
				// offline
				request.setOnlineStatus(OnlineStatus.OFFLINE);
				request.setStatus(RequestStatus.ABORTED);
				final boolean lastInLine = (i == user.getFriendList().size());
				final long time = System.currentTimeMillis();
				BaseFutureListener<FutureDirect> futureDirectListener =
						new BaseFutureListener<FutureDirect>() {
							@Override
							public void operationComplete(FutureDirect future) throws Exception {
								if (future.isSuccess()) {
									if (lastInLine) {
										peer.shutdown();
									}
								} else {
									long timeNow = System.currentTimeMillis();
									if (timeNow - time < TRY_AGAIN_TIME_WINDOW) {
										tryAgain(request, this);
									}
								}
							}

							@Override
							public void exceptionCaught(Throwable t) throws Exception {
								log.error(t.getMessage());
							}
						};
				RequestHandler.handleRequest(request, this, futureDirectListener);
				i++;
			}
		}
	}

	protected void tryAgain(Request request, BaseFutureListener baseFutureListener)
			throws LineUnavailableException, InterruptedException {
		Thread.sleep(500);
		log.debug("Node had unsuccessful Request, Try again...");
		log.debug("Sender: " + request.getSenderName() + ", Receiver: " + request.getReceiverName());
		log.debug(" Request: " + request.getClass() + " Type: " + request.getType());
		RequestHandler.tryAgain(request, this, baseFutureListener);
	}

}
