package com.haidev.kanibis.ui.searchList.view;

import android.app.Activity;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.haidev.kanibis.shared.views.IAnibisFragmentCallback;

public abstract class BaseDialogWrapper implements
		IAnibisFragmentCallback {
	Activity _activity;
	protected String _title;
	protected AlertDialog _dialog;

	public BaseDialogWrapper(Activity activity) {
		_activity = activity;
	}

	public Activity getActivity() {
		return _activity;
	}

	protected AlertDialog getDialog() {
		return _dialog;
	}

	protected void setDialog(AlertDialog d) {
		_dialog = d;
	}

	@Override
	public void openWebLink(String url) {
	}

	@Override
	public void openMemberLink(final int id, final String url) {
	}

	@Override
	public void openMemberAdvertLink(final int id, final String url) {
	}

	@Override
	public boolean isLargeLayout() {
		return false;
	}

	protected void setDefaultSoftInputMode(AlertDialog dialog) {
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}
	protected Resources getResources() {
		return getActivity().getResources();
	}

	public void setTitle(String title) {
		_title = title;
	}

	public String getTitle() {
		return _title;
	}

	public void showDialog() {
		_dialog = createDialog();
		setDialog(_dialog);
		_dialog.show();
	}

	public abstract AlertDialog createDialog();

	public void dismissDialog() {
		if (_dialog != null) {
			_dialog.dismiss();
		}
	}
}
