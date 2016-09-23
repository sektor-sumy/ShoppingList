package ru.android.ainege.shoppinglist.util;

import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public abstract class GeneralMobileAd {
	public abstract AdRequest getRequest();

	public void adsInitialize(Context context, final AdView adView) {
		MobileAds.initialize(context, "ca-app-pub-3804902313328755~1215266223");
		adView.loadAd(getRequest());
		adView.setAdListener(new AdListener() {

			@Override
			public void onAdFailedToLoad(int i) {
				super.onAdFailedToLoad(i);

				if (i == AdRequest.ERROR_CODE_NETWORK_ERROR) {
					adView.destroy();
					adView.setVisibility(View.GONE);
				}
			}

			@Override
			public void onAdLoaded() {
				super.onAdLoaded();
				adView.setVisibility(View.VISIBLE);
			}
		});
	}
}
