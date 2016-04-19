package com.anysoftkeyboard.dictionarycreator;

public interface UI {
	void showErrorMessage(String message);
	void updateProgressState(String message, int precentage);
	
	void onEnded();
}
