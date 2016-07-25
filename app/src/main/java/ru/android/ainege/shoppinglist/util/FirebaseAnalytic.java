package ru.android.ainege.shoppinglist.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalytic {
	//events
	public static final String UPDATE_LIST_IN_SL = "update_list_in_SL";
	public static final String DELETE_LIST_IN_SL = "delete_list_in_SL";
	public static final String COLLAPSE_CATEGORY = "collapse_category";
	public static final String NEW_ITEM = "new_item";
	public static final String TRANSITION_TO_SETTINGS_IN_ITEM = "transition_to_settings";
	public static final String ADD_CATALOG_IN_ITEM = "add_catalog_in_item";
	public static final String ADD_UNIT = "add_unit";
	public static final String ADD_CATEGORY = "add_category";
	public static final String ADD_CURRENCY = "add_currency";

	//params
	public static final String CONTENT_TYPE = FirebaseAnalytics.Param.CONTENT_TYPE;
	public static final String NAME = "name";
	public static final String COLLAPSE = "collapse";
	public static final String EXTEND = "extend";
	public static final String UNIT = "unit";
	public static final String CATEGORY = "category";

	//user properties
	public static final String IS_OPEN_LAST_LIST = "is_open_last_list";
	public static final String IS_BOUGHT_END = "is_bought_end";
	public static final String USE_CATEGORY = "use_category";
	public static final String AUTO_COMPLETE_DATA = "auto_complete_data";
	public static final String TRANSITION_TO_SETTINGS = "transition_to_settings";
	public static final String FAST_EDIT_CATALOGS = "fast_edit_catalogs";

	private Bundle mBundle;
	private String mName;

	private FirebaseAnalytics mFirebaseAnalytics;

	public static FirebaseAnalytic getInstance(Context context, String name) {
		return new FirebaseAnalytic(context, name);
	}

	private FirebaseAnalytic(Context context, String name) {
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
		mBundle = new Bundle();
		mName = name;
	}

	public FirebaseAnalytic putString(String name, String val) {
		mBundle.putString(name, val);
		return this;
	}

	public void addEvent() {
		mFirebaseAnalytics.logEvent(mName, mBundle);
	}

	public FirebaseAnalytic setUserProperty(String name, String value) {
		mFirebaseAnalytics.setUserProperty(name, value);
		return this;
	}
}
