package org.shanoir.uploader.action.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.LoginConfigurationPanel;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;

public class LoginPanelActionListener implements ActionListener {
	
	private static Logger logger = Logger.getLogger(LoginPanelActionListener.class);

	private LoginConfigurationPanel loginPanel;

	private StartupStateContext sSC;

	public LoginPanelActionListener(LoginConfigurationPanel loginPanel, StartupStateContext sSC) {
		this.loginPanel = loginPanel;
		this.sSC = sSC;
	}

	public void actionPerformed(ActionEvent e) {
		String username = this.loginPanel.loginText.getText();
		String password = String.valueOf(this.loginPanel.passwordText.getPassword());
		ShanoirUploaderServiceClient shanoirUploaderServiceClient = ShUpOnloadConfig.getShanoirUploaderServiceClient();
		String token;
		try {
			token = shanoirUploaderServiceClient.loginWithKeycloakForToken(username, password);
			if (token != null) {
				ShUpOnloadConfig.setTokenString(token);
				sSC.getShUpStartupDialog().updateStartupText(
				"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.success"));
				sSC.setState(new PacsConfigurationState());
			} else {
				sSC.getShUpStartupDialog().updateStartupText(
						"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
				sSC.setState(new AuthenticationManualConfigurationState());
			}
		} catch (JSONException e1) {
			logger.error(e1.getMessage(), e1);
			sSC.getShUpStartupDialog().updateStartupText(
					"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
			sSC.setState(new AuthenticationManualConfigurationState());
		}
		sSC.nextState();
	}

}
