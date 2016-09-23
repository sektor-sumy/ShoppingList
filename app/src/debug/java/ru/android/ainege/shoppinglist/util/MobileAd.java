package ru.android.ainege.shoppinglist.util;

import com.google.android.gms.ads.AdRequest;

public class MobileAd extends GeneralMobileAd {

	@Override
	public AdRequest getRequest() {
		return new AdRequest.Builder()
				.addTestDevice("39A3252EB274EA99795AF3C37E80170A") // sony
				.addTestDevice("1AD1397024CA4ECB4EF3FB26AAEC4D7D") // htc desire
				.addTestDevice("3183CA0E1B4EE2A19DC952DB972B57B8") // htc one
				.build();
	}
}
