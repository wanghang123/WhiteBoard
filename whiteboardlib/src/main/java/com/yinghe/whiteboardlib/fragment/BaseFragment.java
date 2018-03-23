package com.yinghe.whiteboardlib.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.protruly.permissions.EasyPermissions;
import com.yinghe.whiteboardlib.R;
import com.yinghe.whiteboardlib.utils.T;

import java.lang.reflect.Field;
import java.util.List;


@SuppressLint("NewApi")
public class BaseFragment extends Fragment implements EasyPermissions.PermissionCallbacks {
	private final static String TAG = BaseFragment.class.getSimpleName();

	/**
	 * Activity
	 */
	protected Activity activity;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getActivity();
	}
	
	/** 通过Class跳转界面 **/
	protected void startActivity(Class<?> cls) {
		startActivity(cls, null);
	}

	/** 含有Bundle通过Class跳转界面 **/
	protected void startActivity(Class<?> cls, Bundle bundle) {
		Intent intent = new Intent();
		intent.setClass(getActivity(), cls);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		startActivity(intent);
	}

	/** 通过Action跳转界面 **/
	protected void startActivity(String action) {
		startActivity(action, null);
	}

	/** 含有Bundle通过Action跳转界面 **/
	protected void startActivity(String action, Bundle bundle) {
		Intent intent = new Intent();
		intent.setAction(action);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		startActivity(intent);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		try {
			Field childFragmentManager =
					Fragment.class.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 吐司
	 * 
	 * @param message
	 */
	protected void showShort(String message) {
		T.showShort(getActivity(), message);
	}

	protected void showLong(String message) {
		T.showLong(getActivity(), message);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// EasyPermissions handles the request result.
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@Override
	public void onPermissionsGranted(int requestCode, List<String> perms) {
		Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
	}

	@Override
	public void onPermissionsDenied(int requestCode, List<String> perms) {
		Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

		// (Optional) Check whether the user denied permissions and checked NEVER ASK AGAIN.
		// This will display a dialog directing them to enable the permission in app settings.
		EasyPermissions.checkDeniedPermissionsNeverAskAgain(this,
				getString(R.string.rationale_ask_again),
				R.string.setting, R.string.cancel, perms);
	}
}
