package ru.android.ainege.shoppinglist.util;

import com.google.android.gms.ads.AdRequest;

public class MobileAd extends GeneralMobileAd {

	@Override
	public AdRequest getRequest() {
		return new AdRequest.Builder().build();
	}
}
