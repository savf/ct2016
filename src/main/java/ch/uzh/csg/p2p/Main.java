package ch.uzh.csg.p2p;

import java.io.IOException;

import ch.uzh.csg.p2p.screens.LoginWindow;

public class Main {

  public static void main(String[] args) throws IOException {
    LoginWindow loginWindow = new LoginWindow();
    loginWindow.launch(LoginWindow.class, args);
  }
}
