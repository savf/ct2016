package main.java.ch.uzh.csg.p2p;

import java.io.IOException;

import main.java.ch.uzh.csg.p2p.screens.LoginScreen;

public class Main {

	public static void main(String[] args) throws IOException {
		
		//args: 0 = id; 1 = ip
		
//		int id = args.length > 0 ? Integer.parseInt(args[0]) : ((Long)System.currentTimeMillis()).intValue();
//		
//		if(args.length < 2){
//			Node node = new Node(id, null);
//		}else{
//			Node node = new Node(id, args[1]);
//		}
		
		new LoginScreen();
	}

}
