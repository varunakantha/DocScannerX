package com.dynamsoft.online.docscannerx;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dynamsoft.camerasdk.exception.DcsCameraNotAuthorizedException;
import com.dynamsoft.camerasdk.exception.DcsException;
import com.dynamsoft.camerasdk.exception.DcsSelectModeNotEnabledException;
import com.dynamsoft.camerasdk.exception.DcsValueOutOfRangeException;
import com.dynamsoft.camerasdk.io.DcsCache;
import com.dynamsoft.camerasdk.io.DcsEncodeParameter;
import com.dynamsoft.camerasdk.io.DcsHttpUploadConfig;
import com.dynamsoft.camerasdk.io.DcsJPEGEncodeParameter;
import com.dynamsoft.camerasdk.io.DcsPDFEncodeParameter;
import com.dynamsoft.camerasdk.io.DcsPNGEncodeParameter;
import com.dynamsoft.camerasdk.io.ISave;
import com.dynamsoft.camerasdk.io.IUpload;
import com.dynamsoft.camerasdk.model.DcsDocument;
import com.dynamsoft.camerasdk.model.DcsImage;
import com.dynamsoft.camerasdk.view.DcsImageGalleryView;
import com.dynamsoft.camerasdk.view.DcsImageGalleryViewListener;
import com.dynamsoft.camerasdk.view.DcsVideoView;
import com.dynamsoft.camerasdk.view.DcsVideoViewListener;
import com.dynamsoft.camerasdk.view.DcsView;
import com.dynamsoft.camerasdk.view.DcsViewListener;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import cn.bingoogolapple.baseadapter.BGAOnRVItemClickListener;
import me.shaohui.bottomdialog.BottomDialog;
import q.rorbin.badgeview.QBadgeView;


public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, DcsViewListener {

	private static final int VIEW_STATE_HOME = 1;
	private static final int VIEW_STATE_SORT = 2;
	private static final int VIEW_STATE_SELECT = 3;
	private static final int VIEW_STATE_SIGNEL = 4;
	private static final int CAMERA_OK = 10;
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE"};
	private DcsView dcsView;
	private AppBarLayout barTitle;
	private FloatingActionButton mFab;
	//private Toolbar mActionBar;
	private Toolbar mToolbar;
	private DrawerLayout navigationDrawer;
	private ActionBarDrawerToggle mToggle;
	private Menu mMenu;
	private LinearLayout bottomMenu;
	private ImageView camThumb;
	private LinearLayout mShare;
	private LinearLayout mTrash;
	private LinearLayout mUpload;
	private LinearLayout mSave;
	private RecyclerView saveList;
	private WebView mUploadList;
	private WebView mDevCenter;
	private LinearLayout emptyView;
	private QBadgeView badgeView;
	private ImageView mTick;
	private NavigationView navigationView;
	private SavelistAdapter savelistAdapter;
	private int mGalleryEnterMode = VIEW_STATE_HOME;
	private ShareUtil shareUtil;
	private DcsEncodeParameter encodeParameter;
	private String userId;
	private DcsCache cache;
	private int photoCount = 0;
	private int snackBarTop = 0;
	private MaterialDialog progressDialog;
	String mCurrentPhotoPath;
	Uri imageURI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		DcsView.setLicense(getApplicationContext(), "Your license number");
		initSplashPage();
		initUI();
		requestPermissions();

	}

	private void initSplashPage() {
		SplashView.showSplashView(this, 2, R.drawable.splash_ico, new SplashView.OnSplashViewActionListener() {
			@Override
			public void onSplashImageClick(String actionUrl) {
				Log.d("SplashView", "img clicked. actionUrl: " + actionUrl);
			}

			@Override
			public void onSplashViewDismiss(boolean initiativeDismiss) {
				Log.d("SplashView", "dismissed, initiativeDismiss: " + initiativeDismiss);
			}
		});
	}

	private void initDcsView() {
		dcsView = findViewById(R.id.dcsview_id);
		dcsView.setLogLevel(DcsView.DLLE_OFF);
		dcsView.setCurrentView(DcsView.DVE_IMAGEGALLERYVIEW);
		badgeView = new QBadgeView(MainActivity.this);
		badgeView.bindTarget(camThumb)
				.setBadgeTextColor(Color.parseColor("#ffffff"))
				.setBadgeBackgroundColor(Color.parseColor("#3894E2"))
				.setBadgeGravity(Gravity.TOP | Gravity.END)
				.setShowShadow(false);
		try {
			dcsView.getVideoView().setMode(DcsView.DME_DOCUMENT);
		} catch (DcsValueOutOfRangeException e) {
			e.printStackTrace();
		}
		dcsView.setListener(this);

		dcsView.getImageGalleryView().enterManualSortMode();
		dcsView.getImageGalleryView().setListener(new DcsImageGalleryViewListener() {
			@Override
			public void onSingleTap(DcsImageGalleryView dcsImageGalleryView, int i) {
				if (dcsImageGalleryView.getImageGalleryViewmode() == DcsImageGalleryView.DIVME_SINGLE) {
					updateToolbar(VIEW_STATE_SIGNEL);
				} else {
					if (mGalleryEnterMode != VIEW_STATE_SELECT) {
						updateToolbar(VIEW_STATE_HOME);
					}
				}
			}

			@Override
			public void onLongPress(DcsImageGalleryView dcsImageGalleryView, int i) {
				if (mGalleryEnterMode == VIEW_STATE_HOME) {
					updateToolbar(VIEW_STATE_SELECT);
					int[] indices = new int[1];
					indices[0] = i;
					try {
						dcsImageGalleryView.setSelectedIndices(indices);
					} catch (DcsSelectModeNotEnabledException e) {
						e.printStackTrace();
					} catch (DcsValueOutOfRangeException e) {
						e.printStackTrace();
					}
					String strTitle = dcsImageGalleryView.getSelectedIndices().length + " Selected";
					mToolbar.setTitle(strTitle);
				}
			}

			@Override
			public void onSelectChanged(DcsImageGalleryView dcsImageGalleryView, int[] ints) {
				if (mGalleryEnterMode == VIEW_STATE_SELECT) {
					String strTitle = dcsView.getImageGalleryView().getSelectedIndices().length + " Selected";
					mToolbar.setTitle(strTitle);
					if (dcsView.getImageGalleryView().getSelectedIndices().length == dcsView.getBuffer().getCount()) {
						mMenu.getItem(1).getSubMenu().getItem().setVisible(true).setTitle("Unselect All");
					} else {
						mMenu.getItem(1).getSubMenu().getItem().setVisible(true).setTitle("Select All");
					}
				}
			}
		});

		dcsView.getVideoView().setListener(new DcsVideoViewListener() {
			@Override
			public boolean onPreCapture(DcsVideoView sender) {
				return true;
			}

			@Override
			public void onCaptureFailure(DcsVideoView sender, DcsException exception) {

			}

			@Override
			public void onPostCapture(DcsVideoView sender, DcsImage image) {

				if (sender.getVisibility() == View.VISIBLE) {
					if (camThumb.getVisibility() != View.VISIBLE) {
						camThumb.setVisibility(View.VISIBLE);
					}
					savePicture(image.getImage());
					camThumb.setImageBitmap(image.getImage());
					badgeView.setBadgeNumber(++photoCount);
					dcsView.getVideoView().setShowCancelToolItem(false);
					mTick.setVisibility(View.VISIBLE);
				}


			}

			@Override
			public void onCancelTapped(DcsVideoView sender) {
				if (dcsView.getBuffer().getCount() == 0) {
					emptyView.setVisibility(View.VISIBLE);
				} else {
					emptyView.setVisibility(View.GONE);
				}
				if (badgeView != null) {
					badgeView.hide(false);
				}
			}

			@Override
			public void onCaptureTapped(DcsVideoView sender) {

			}

			@Override
			public void onDocumentDetected(DcsVideoView sender, DcsDocument document) {

			}
		});

	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		}
		if (dcsView.getCurrentView() == DcsView.DVE_EDITORVIEW) {
			if (dcsView.getDocumentEditorView().getVisibility() == View.VISIBLE) {
				dcsView.getDocumentEditorView().discard();
				dcsView.setCurrentView(dcsView.getDocumentEditorView().getNextViewAfterCancel());
			} else if (dcsView.getImageEditorView().getVisibility() == View.VISIBLE) {
				dcsView.getImageEditorView().discard();
				dcsView.setCurrentView(dcsView.getImageEditorView().getNextViewAfterCancel());
			}
		} else if (dcsView.getCurrentView() == DcsView.DVE_VIDEOVIEW) {
			dcsView.setCurrentView(DcsView.DVE_IMAGEGALLERYVIEW);
		} else if (dcsView.getCurrentView() == DcsView.DVE_IMAGEGALLERYVIEW &&
				dcsView.getImageGalleryView().getImageGalleryViewmode() == DcsImageGalleryView.DIVME_SINGLE) {
			try {
				dcsView.getImageGalleryView().setImageGalleryViewmode(DcsImageGalleryView.DIVME_MULTIPLE);
			} catch (DcsValueOutOfRangeException e) {
				e.printStackTrace();
			}
			updateToolbar(VIEW_STATE_HOME);

		} else if (dcsView.getCurrentView() == DcsView.DVE_IMAGEGALLERYVIEW && (mGalleryEnterMode == VIEW_STATE_SORT || mGalleryEnterMode == VIEW_STATE_SELECT)) {
			updateToolbar(VIEW_STATE_HOME);
		} else {
			AlertDialog.Builder isEixtDlg = new AlertDialog.Builder(this);
			isEixtDlg.setTitle("Exit Warning");
			isEixtDlg.setMessage("Are you sure you want to quit Doc-Scanner-X?");
			isEixtDlg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			isEixtDlg.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			isEixtDlg.show();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		mMenu = menu;
		SubMenu subMenu = menu.addSubMenu(0, 991100, 100, "");
		getMenuInflater().inflate(R.menu.main, subMenu);

		subMenu.getItem()
				.setIcon(R.mipmap.icn_more)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		//select view menu
		menu.addSubMenu(0, R.id.action_select_all, 101, "Select All").getItem()
				.setVisible(false)

				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);


		//edit & trash menu on sigle mode
		menu.addSubMenu(0, R.id.action_edit, 101, "").getItem()
				.setIcon(R.mipmap.icn_edit)
				.setVisible(false)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.addSubMenu(0, R.id.action_trash, 101, "").getItem()
				.setIcon(R.mipmap.icn_trash)
				.setVisible(false)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		switch (dcsView.getCurrentView()) {
			case DcsView.DVE_IMAGEGALLERYVIEW:
				if (DcsImageGalleryView.DIVME_SINGLE == dcsView.getImageGalleryView().getImageGalleryViewmode()) {
					if (mToolbar != null) {
						mToolbar.setTitle("");
						mToolbar.setNavigationIcon(R.mipmap.icn_back);
						mToolbar.setBackgroundColor(Color.parseColor("#000000"));
						updateToolbar(VIEW_STATE_SIGNEL);
					}
				}
				break;
			default:
				break;
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
			case R.id.action_select:
				updateToolbar(VIEW_STATE_SELECT);
				break;
			case R.id.action_sort:
				updateToolbar(VIEW_STATE_SORT);
				break;
			case R.id.action_edit:
				dcsView.setCurrentView(DcsView.DVE_EDITORVIEW);
				break;
			case R.id.action_trash:
				if (dcsView.getBuffer().getCurrentIndex() != -1) {
					AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).
							setTitle("Delete This Image").setMessage("Are you sure you want to delete this page?")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									try {
										dcsView.getBuffer().delete(dcsView.getBuffer().getCurrentIndex());
									} catch (DcsValueOutOfRangeException e) {
										e.printStackTrace();
									}
									dialog.dismiss();
								}
							}).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).create();
					dialog.show();
				}
				break;
			case R.id.action_select_all:
				if (item.getTitle().toString().equalsIgnoreCase("Unselect All")) {
					try {
						dcsView.getImageGalleryView().setSelectedIndices(null);
					} catch (DcsSelectModeNotEnabledException e) {
						e.printStackTrace();
					} catch (DcsValueOutOfRangeException e) {
						e.printStackTrace();
					}
					item.setTitle("Select All");
				} else {
					int count = dcsView.getBuffer().getCount();
					int[] itemAll = new int[count];
					for (int i = 0; i < count; i++) {
						itemAll[i] = i;
					}
					try {
						dcsView.getImageGalleryView().setSelectedIndices(itemAll);
					} catch (DcsSelectModeNotEnabledException e) {
						e.printStackTrace();
					} catch (DcsValueOutOfRangeException e) {
						e.printStackTrace();
					}
					item.setTitle("Unselect All");
				}
				break;
			default:
				break;

		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();
		if (id == R.id.nav_home) {
			mToolbar.setTitle(R.string.view_home);
			mToolbar.getMenu().getItem(0).setVisible(true);
			dcsView.setVisibility(View.VISIBLE);
			saveList.setVisibility(View.GONE);
			saveList.setAdapter(null);
			if (mGalleryEnterMode == VIEW_STATE_HOME)
				mFab.setVisibility(View.VISIBLE);
			mUploadList.setVisibility(View.GONE);
			mDevCenter.setVisibility(View.GONE);
		} else if (id == R.id.nav_saved) {
			openSavedFiles();
		} else if (id == R.id.nav_uploaded) {
			openUploadedFiles();
		} else if (id == R.id.nav_center) {
			mToolbar.getMenu().getItem(0).setVisible(false);
			mToolbar.getMenu().setGroupVisible(0, false);
			mToolbar.setTitle(R.string.developer_center);
			dcsView.setVisibility(View.GONE);
			saveList.setVisibility(View.GONE);
			saveList.setAdapter(null);
			mUploadList.setVisibility(View.GONE);
			mFab.setVisibility(View.GONE);
			mDevCenter.setVisibility(View.VISIBLE);
			mDevCenter.getSettings().setJavaScriptEnabled(true);
			mDevCenter.loadUrl("https://developer.dynamsoft.com/dws/android-edition");


		}
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	private void openSavedFiles() {
		mToolbar.setTitle(R.string.saved_files);
		mToolbar.getMenu().getItem(0).setVisible(false);
		saveList.setVisibility(View.VISIBLE);
		dcsView.setVisibility(View.GONE);
		savelistAdapter.setData(getImagePathFromSD());
		saveList.setAdapter(savelistAdapter);
		mFab.setVisibility(View.GONE);
		mUploadList.setVisibility(View.GONE);
		mDevCenter.setVisibility(View.GONE);
	}

	private void openUploadedFiles() {
		mToolbar.getMenu().getItem(0).setVisible(false);
		mToolbar.getMenu().setGroupVisible(0, false);
		mToolbar.setTitle(R.string.uploaded_files);
		dcsView.setVisibility(View.GONE);
		saveList.setVisibility(View.GONE);
		saveList.setAdapter(null);
		mUploadList.setVisibility(View.VISIBLE);
		mFab.setVisibility(View.GONE);
		mDevCenter.setVisibility(View.GONE);
		loadUploadList();
	}

	private void loadUploadList() {
		mUploadList.getSettings().setJavaScriptEnabled(true);
		mUploadList.getSettings().setUserAgentString("com.dynamsoft.dcs.android");
		mUploadList.loadUrl("https://demo.dynamsoft.com/DCS_Mobile/filesList.html?userId=" + userId);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (dcsView.getCurrentView() == DcsView.DVE_VIDEOVIEW) {
			try {
				dcsView.getVideoView().preview();
			} catch (DcsCameraNotAuthorizedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		dcsView.getVideoView().stopPreview();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dcsView.getVideoView().destroyCamera();
	}

	@Override
	public void onCurrentViewChanged(DcsView dcsView, int lastView, int currentView) {
		if (currentView == DcsView.DVE_IMAGEGALLERYVIEW) {
			navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		} else {
			navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}

		if (currentView == DcsView.DVE_IMAGEGALLERYVIEW) {
			barTitle.setVisibility(View.VISIBLE);
			mFab.setVisibility(View.VISIBLE);
			camThumb.setVisibility(View.GONE);
			if (dcsView.getBuffer().getCount() == 0) {
				emptyView.setVisibility(View.VISIBLE);
			} else {
				emptyView.setVisibility(View.GONE);
			}

		} else if (currentView == DcsView.DVE_VIDEOVIEW) {
			barTitle.setVisibility(View.GONE);
			mFab.setVisibility(View.GONE);
		} else {
			barTitle.setVisibility(View.GONE);
			mFab.setVisibility(View.GONE);
		}
		if (lastView == DcsView.DVE_VIDEOVIEW && currentView != lastView) {
			mTick.setVisibility(View.GONE);
			if (badgeView != null) {
				badgeView.hide(false);
			}
			camThumb.setVisibility(View.GONE);
			dcsView.getVideoView().setShowCancelToolItem(true);
		}

	}

	private void updateToolbar(int viewState) {

		if (viewState == VIEW_STATE_HOME) {
			navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		} else {
			navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}

		switch (viewState) {
			case VIEW_STATE_HOME: {
				mGalleryEnterMode = VIEW_STATE_HOME;
				mToolbar.setTitle(R.string.view_home);
				mToolbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
				mToggle = new ActionBarDrawerToggle(
						this, navigationDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
				navigationDrawer.addDrawerListener(mToggle);
				mToggle.syncState();
				mFab.setVisibility(View.VISIBLE);
				bottomMenu.setVisibility(View.GONE);
				dcsView.getImageGalleryView().enterNormalMode();
				if (mMenu != null) {
					mMenu.getItem(0).getSubMenu().getItem().setVisible(true);
					mMenu.getItem(1).getSubMenu().getItem().setVisible(false);
					mMenu.getItem(2).getSubMenu().getItem().setVisible(false);
					mMenu.getItem(3).getSubMenu().getItem().setVisible(false);
				}
				if (dcsView.getBuffer().getCount() == 0) {
					emptyView.setVisibility(View.VISIBLE);
				} else {
					emptyView.setVisibility(View.GONE);
				}
			}
			break;
			case VIEW_STATE_SORT: {
				mGalleryEnterMode = VIEW_STATE_SORT;
				mToolbar.setTitle(R.string.view_sort);
				mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						updateToolbar(VIEW_STATE_HOME);
					}
				});
				mToolbar.setNavigationIcon(R.mipmap.icn_back);
				dcsView.getImageGalleryView().enterManualSortMode();
				if (mMenu != null) {
					mMenu.getItem(0).getSubMenu().getItem().setVisible(true);
					mMenu.getItem(1).getSubMenu().getItem().setVisible(false);
					mMenu.getItem(2).getSubMenu().getItem().setVisible(false);
					mMenu.getItem(3).getSubMenu().getItem().setVisible(false);
				}
			}
			break;
			case VIEW_STATE_SELECT: {
				mGalleryEnterMode = VIEW_STATE_SELECT;
				String strTitle = dcsView.getImageGalleryView().getSelectedIndices().length + " Selected";
				mToolbar.setTitle(strTitle);
				mFab.setVisibility(View.GONE);
				mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						updateToolbar(VIEW_STATE_HOME);
					}
				});
				mToolbar.setNavigationIcon(R.mipmap.icn_back);
				dcsView.getImageGalleryView().enterSelectMode();

				boolean bSelectedAll = false;
				int[] selectIndices = dcsView.getImageGalleryView().getSelectedIndices();
				List<Integer> list = new ArrayList<Integer>();
				for (int i = 0; i < selectIndices.length; i++) {
					if (!list.contains(selectIndices[i])) {
						list.add(selectIndices[i]);
					}
				}

				if (list.size() == dcsView.getBuffer().getCount()) {
					bSelectedAll = true;
				}

				if (mMenu != null) {
					mMenu.getItem(0).getSubMenu().getItem().setVisible(false);
					if (bSelectedAll) {
						mMenu.getItem(1).getSubMenu().getItem().setVisible(true).setTitle("Unselect All");
					} else {
						mMenu.getItem(1).getSubMenu().getItem().setVisible(true).setTitle("Select All");
					}
					mMenu.getItem(2).getSubMenu().getItem().setVisible(false);
					mMenu.getItem(3).getSubMenu().getItem().setVisible(false);
				}


				bottomMenu.setVisibility(View.VISIBLE);
			}
			break;
			case VIEW_STATE_SIGNEL: {
				mGalleryEnterMode = VIEW_STATE_SIGNEL;
				mToolbar.setTitle("");
				mFab.setVisibility(View.GONE);
				mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							dcsView.getImageGalleryView().setImageGalleryViewmode(DcsImageGalleryView.DIVME_MULTIPLE);
						} catch (DcsValueOutOfRangeException e) {
							e.printStackTrace();
						}
						updateToolbar(VIEW_STATE_HOME);
					}
				});
				mToolbar.setNavigationIcon(R.mipmap.icn_back);
				if (mMenu != null) {
					mMenu.getItem(0).getSubMenu().getItem().setVisible(false);
					mMenu.getItem(1).getSubMenu().getItem().setVisible(false);
					mMenu.getItem(2).getSubMenu().getItem().setVisible(true);
					mMenu.getItem(3).getSubMenu().getItem().setVisible(true);
				}
			}
			break;
			default:
				break;
		}
	}

	private List<SaveFileModel> getImagePathFromSD() {
		List<SaveFileModel> imagePathList = new ArrayList<>();
		String dcsPath = Environment.getExternalStorageDirectory() + "/DcsPhotos/";
		File dir = new File(dcsPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String filePath = dcsPath + getApplicationContext().getPackageName() + "/";
		dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File fileAll = new File(filePath);

		File[] files = fileAll.listFiles();
		List<File> listFile = Arrays.asList(files) ;
		Comparator<File> comparator = new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return (int) ((o2.lastModified()/1000)-(o1.lastModified()/1000));
			}
		};

		Collections.sort(listFile,comparator);
		SaveFileModel fileModel;
		for (int i = 0; i < listFile.size(); i++) {
			File file = listFile.get(i);
			fileModel = new SaveFileModel();
			if (checkIsImageFile(file.getPath(), fileModel)) {
				long time = file.lastModified();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				fileModel.modifyDate = formatter.format(time);
				fileModel.filePath = file.getPath();
				imagePathList.add(fileModel);
			}
		}
		return imagePathList;
	}

	private boolean checkIsImageFile(String fName, SaveFileModel model) {
		boolean isImageFile;
		String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
				fName.length()).toLowerCase();
		int start = fName.lastIndexOf("/");
		int end = fName.lastIndexOf(".");
		if (start != -1 && end != -1) {
			fName = fName.substring(start + 1, end);
		} else {
			fName = "";
		}
		if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("jpeg") ||
				FileEnd.equals("pdf")) {
			isImageFile = true;
			if (FileEnd.equals("pdf")) {
				model.isPDF = true;
			}
			model.fileName = fName;
		} else {
			isImageFile = false;
		}
		return isImageFile;
	}

	private void uploadDoc() {
		final DcsHttpUploadConfig config = new DcsHttpUploadConfig();
		config.Url = "https://demo.dynamsoft.com/DCS_Mobile/upload.ashx";
		config.Name =  "fileBinary";
		HashMap<String, String> map = new HashMap<>();
		map.put("userId", userId);
		map.put("filePureName",System.currentTimeMillis() + "");
		config.FormField = map;
		int[] array = dcsView.getImageGalleryView().getSelectedIndices();
		progressDialog.show();
		final int sCount = (encodeParameter instanceof DcsPDFEncodeParameter)? 1:array.length;
		final int []nUploadCount = new int[2];
		dcsView.getIO().uploadAsync(array, config, encodeParameter, new IUpload() {
			@Override
			public boolean onUploadProgress(int progress) {
				//	Log.d("upload progress", "progress" + progress);
				progressDialog.setProgress(progress);
				return true;
			}

			@Override
			public void onUploadSuccess(byte[] data) {
				nUploadCount[0]++;
				if (data != null) {
					try {
						Log.d("upload success", "succeed : " + new String(data, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					if(nUploadCount[0]+nUploadCount[1] == sCount) {

						progressDialog.dismiss();
						progressDialog.setProgress(0);
						SnackbarManager.show(Snackbar.with(MainActivity.this)
								.text("Doc uploaded")
								.actionLabel("VIEW")
								.actionColor(Color.parseColor("#3894E2")).margin(0, ScreenUtil.dp2px(MainActivity.this, 55))
								.actionListener(new ActionClickListener() {
									@Override
									public void onActionClicked(Snackbar snackbar) {
										updateToolbar(VIEW_STATE_HOME);

										mToolbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
										mToggle = new ActionBarDrawerToggle(
												MainActivity.this, navigationDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
										navigationDrawer.addDrawerListener(mToggle);
										mToggle.syncState();
										mFab.setVisibility(View.VISIBLE);
										bottomMenu.setVisibility(View.GONE);
										dcsView.getImageGalleryView().enterNormalMode();
										mToolbar.getMenu().getItem(0).setVisible(false);
										mToolbar.setTitle(R.string.uploaded_files);
										dcsView.setVisibility(View.GONE);
										saveList.setVisibility(View.GONE);
										saveList.setAdapter(null);
										mUploadList.setVisibility(View.VISIBLE);
										mFab.setVisibility(View.GONE);
										mToolbar.getMenu().setGroupVisible(0, false);
										navigationView.getMenu().getItem(2).setChecked(true);
										loadUploadList();
									}
								})
						);
					}
				}
			}

			@Override
			public void onUploadFailure(DcsException exp) {
				nUploadCount[1]++;
				Log.d("upload error", "" + exp.getMessage());
				if(nUploadCount[0]+nUploadCount[1] == sCount) {
					progressDialog.dismiss();
					progressDialog.setProgress(0);
					if(nUploadCount[0]>0){
						SnackbarManager.show(Snackbar.with(MainActivity.this)
								.text("Doc uploaded")
								.actionLabel("VIEW")
								.actionColor(Color.parseColor("#3894E2")).margin(0, ScreenUtil.dp2px(MainActivity.this, 55))
								.actionListener(new ActionClickListener() {
									@Override
									public void onActionClicked(Snackbar snackbar) {
										updateToolbar(VIEW_STATE_HOME);

										mToolbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
										mToggle = new ActionBarDrawerToggle(
												MainActivity.this, navigationDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
										navigationDrawer.addDrawerListener(mToggle);
										mToggle.syncState();
										mFab.setVisibility(View.VISIBLE);
										bottomMenu.setVisibility(View.GONE);
										dcsView.getImageGalleryView().enterNormalMode();
										mToolbar.getMenu().getItem(0).setVisible(false);
										mToolbar.setTitle(R.string.uploaded_files);
										dcsView.setVisibility(View.GONE);
										saveList.setVisibility(View.GONE);
										saveList.setAdapter(null);
										mUploadList.setVisibility(View.VISIBLE);
										mFab.setVisibility(View.GONE);
										mToolbar.getMenu().setGroupVisible(0, false);
										navigationView.getMenu().getItem(2).setChecked(true);
										loadUploadList();
									}
								})
						);
					}else{
						Toast.makeText(MainActivity.this, "Upload error : " + exp.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
				//
			}
		});
	}

	private void initUI() {
		cache = DcsCache.get(this);
		userId = cache.getAsString("userId");
		if (userId == null) {
			userId = UUID.randomUUID().toString();
			cache.put("userId", userId);
		}
		//snackBarTop
		shareUtil = new ShareUtil(this);
		emptyView = findViewById(R.id.emptyview);
		mUploadList = findViewById(R.id.wv_upload_list);
		mDevCenter = findViewById(R.id.wv_center_id);
		barTitle = findViewById(R.id.title_bar_id);
		mToolbar = findViewById(R.id.toolbar);
		bottomMenu = findViewById(R.id.bottom_menu);
		mFab = findViewById(R.id.fab);
		camThumb = findViewById(R.id.iv_thumb);
		mToolbar.setTitle("Home");
		setSupportActionBar(mToolbar);
		mShare = findViewById(R.id.ll_share);
		mTrash = findViewById(R.id.ll_trash);
		mUpload = findViewById(R.id.ll_upload);
		mSave = findViewById(R.id.ll_save);
		saveList = findViewById(R.id.rl_savelist);
		mTick = findViewById(R.id.iv_tick);
		progressDialog = new MaterialDialog.Builder(MainActivity.this)
				.backgroundColor(Color.parseColor("#00ffffff"))
				.contentGravity(GravityEnum.CENTER)
				.progress(false, 100, true)
				.content("Uploading document...")
				.contentColor(Color.parseColor("#ffffff"))
				.canceledOnTouchOutside(false)
				.build();
		savelistAdapter = new SavelistAdapter(saveList);
		savelistAdapter.setOnRVItemClickListener(new BGAOnRVItemClickListener() {
			@Override
			public void onRVItemClick(ViewGroup parent, View itemView, int position) {
				Intent intent = new Intent(MainActivity.this, SavedImgDetailActivity.class);
				intent.putExtra("imgPath", getImagePathFromSD().get(position).filePath);
				intent.putExtra("imgName", getImagePathFromSD().get(position).fileName);
				intent.putExtra("isPDF", getImagePathFromSD().get(position).isPDF);
				startActivity(intent);
			}
		});
		saveList.setLayoutManager(new LinearLayoutManager(this));
		saveList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
		initDcsView();
		mSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String dcsPath = Environment.getExternalStorageDirectory() + "/DcsPhotos/";
				File file = new File(dcsPath);
				if (!file.exists()) {
					file.mkdirs();
				}
				final String path = dcsPath + getApplicationContext().getPackageName() + "/";
				file = new File(path);
				if (!file.exists()) {
					file.mkdirs();
				}
				if (dcsView.getImageGalleryView().getSelectedIndices() == null || dcsView.getImageGalleryView().getSelectedIndices().length == 0) {
					return;
				}
				final BottomDialog dialog = BottomDialog.create(getSupportFragmentManager());
				dialog.setViewListener(new BottomDialog.ViewListener() {
					@Override
					public void bindView(View v) {
						TextView title = v.findViewById(R.id.tv_dialog_title);
						title.setText("Save to local file ");
						v.findViewById(R.id.ll_png).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Log.d("bottom menu", "click");
								int[] array = dcsView.getImageGalleryView().getSelectedIndices();
								dcsView.getIO().saveAsync(array, path + System.currentTimeMillis() + ".png", new DcsPNGEncodeParameter(), new ISave() {
									@Override
									public boolean onSaveProgress(int progress) {
										return true;
									}

									@Override
									public void onSaveSuccess(Object result) {
										dialog.dismiss();
										SnackbarManager.show(Snackbar.with(MainActivity.this)
												.text("Doc saved")
												.actionLabel("VIEW")
												.actionColor(Color.parseColor("#3894E2")).margin(0, ScreenUtil.dp2px(MainActivity.this, 55))
												.actionListener(new ActionClickListener() {
													@Override
													public void onActionClicked(Snackbar snackbar) {
														mToolbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
														mToggle = new ActionBarDrawerToggle(
																MainActivity.this, navigationDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
														navigationDrawer.addDrawerListener(mToggle);
														mToggle.syncState();
														updateToolbar(VIEW_STATE_HOME);
														openSavedFiles();

													}
												})
										);


										//Toast.makeText(MainActivity.this, "Save successfully", Toast.LENGTH_SHORT).show();
									}

									@Override
									public void onSaveFailure(Object result, DcsException exp) {

									}
								});
								dialog.dismiss();
							}
						});
						v.findViewById(R.id.ll_jpg).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								int[] array = dcsView.getImageGalleryView().getSelectedIndices();
								dcsView.getIO().saveAsync(array, path + System.currentTimeMillis() + ".jpeg", new DcsJPEGEncodeParameter(), new ISave() {
									@Override
									public boolean onSaveProgress(int progress) {
										return true;
									}

									@Override
									public void onSaveSuccess(Object result) {
										dialog.dismiss();
										SnackbarManager.show(Snackbar.with(MainActivity.this)
												.text("Doc saved")
												.actionLabel("VIEW")
												.actionColor(Color.parseColor("#3894E2")).margin(0, ScreenUtil.dp2px(MainActivity.this, 55))
												.actionListener(new ActionClickListener() {
													@Override
													public void onActionClicked(Snackbar snackbar) {
														mToolbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
														mToggle = new ActionBarDrawerToggle(
																MainActivity.this, navigationDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
														navigationDrawer.addDrawerListener(mToggle);
														mToggle.syncState();
														updateToolbar(VIEW_STATE_HOME);
														openSavedFiles();

													}
												})
										);
									}

									@Override
									public void onSaveFailure(Object result, DcsException exp) {

									}
								});
								dialog.dismiss();
							}
						});
						v.findViewById(R.id.ll_pdf).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								int[] array = dcsView.getImageGalleryView().getSelectedIndices();
								dcsView.getIO().saveAsync(array, path + System.currentTimeMillis() + ".pdf", new DcsPDFEncodeParameter(), new ISave() {
									@Override
									public boolean onSaveProgress(int progress) {
										return true;
									}

									@Override
									public void onSaveSuccess(Object result) {
										dialog.dismiss();
										SnackbarManager.show(Snackbar.with(MainActivity.this)
												.text("Doc saved")
												.actionLabel("VIEW")
												.actionColor(Color.parseColor("#3894E2")).margin(0, ScreenUtil.dp2px(MainActivity.this, 55))
												.actionListener(new ActionClickListener() {
													@Override
													public void onActionClicked(Snackbar snackbar) {
														mToolbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
														mToggle = new ActionBarDrawerToggle(
																MainActivity.this, navigationDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
														navigationDrawer.addDrawerListener(mToggle);
														mToggle.syncState();
														updateToolbar(VIEW_STATE_HOME);
														openSavedFiles();

													}
												})
										);
									}

									@Override
									public void onSaveFailure(Object result, DcsException exp) {

									}
								});
								dialog.dismiss();
							}
						});
					}
				})
						.setLayoutRes(R.layout.dialog_bottom)
						.show();
			}
		});
		mUpload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dcsView.getImageGalleryView().getSelectedIndices().length == 0) {
					return;
				}

				final BottomDialog dialog = BottomDialog.create(getSupportFragmentManager());
				dialog.setViewListener(new BottomDialog.ViewListener() {
					@Override
					public void bindView(View v) {
						TextView title = v.findViewById(R.id.tv_dialog_title);
						title.setText("Upload to Server ");
						v.findViewById(R.id.ll_png).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
								encodeParameter = new DcsPNGEncodeParameter();
								uploadDoc();
							}
						});
						v.findViewById(R.id.ll_jpg).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								progressDialog.show();
								encodeParameter = new DcsJPEGEncodeParameter();
								dialog.dismiss();
								uploadDoc();
							}
						});
						v.findViewById(R.id.ll_pdf).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								progressDialog.show();
								encodeParameter = new DcsPDFEncodeParameter();
								dialog.dismiss();
								uploadDoc();
							}
						});
					}
				})
						.setLayoutRes(R.layout.dialog_bottom)
						.show();
			}
		});
		mTrash.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int size = dcsView.getImageGalleryView().getSelectedIndices().length;
				if (size == 0) {
					return;
				}
				String strTitle, strContent;
				if (size == 1) {
					strTitle = "Delete This Image";
					strContent = "Are you sure you want to delete this page?";
				} else {
					strTitle = "Delete These Images";
					strContent = "Are you sure you want to delete these pages?";
				}
				AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).
						setTitle(strTitle).setMessage(strContent)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								while (dcsView.getImageGalleryView().getSelectedIndices().length != 0) {
									try {
										dcsView.getBuffer().delete(dcsView.getImageGalleryView().getSelectedIndices()[0]);
									} catch (DcsValueOutOfRangeException e) {
										e.printStackTrace();
									}
								}
								dialog.dismiss();
							}
						}).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).create();
				dialog.show();
			}
		});
		mShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dcsView.getImageGalleryView().getSelectedIndices().length == 0) {
					return;
				}
				int[] selecindices = dcsView.getImageGalleryView().getSelectedIndices();
				ArrayList<Uri> imageUris = new ArrayList<>();
				for (int idx : selecindices) {
					Bitmap bmp = null;
					try {
						bmp = dcsView.getBuffer().get(idx).getImage();
					} catch (DcsValueOutOfRangeException e) {
						e.printStackTrace();
					}
					Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bmp, null, null));
					imageUris.add(uri);
				}
				shareUtil.shareMultiImages(imageUris, MainActivity.this);

				//shareUtil.shareText(null, null, "share content", "title", "subject");
			}
		});
		camThumb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dcsView.setCurrentView(DcsView.DVE_IMAGEGALLERYVIEW);
				try {
					dcsView.getImageGalleryView().setImageGalleryViewmode(DcsImageGalleryView.DIVME_SINGLE);
					invalidateOptionsMenu();
				} catch (DcsValueOutOfRangeException e) {
					e.printStackTrace();
				}
			}
		});
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				photoCount = 0;
				dcsView.setCurrentView(DcsView.DVE_VIDEOVIEW);
			}
		});

		navigationDrawer = findViewById(R.id.drawer_layout);

		updateToolbar(VIEW_STATE_HOME);

		navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		navigationView.getMenu().getItem(0).setChecked(true);

		mTick.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dcsView.getVideoView().setShowCancelToolItem(true);
				dcsView.setCurrentView(DcsView.DVE_IMAGEGALLERYVIEW);
				try {
					if (dcsView.getVideoView().getFlashMode() == DcsVideoView.DFME_TORCH) {
						dcsView.getVideoView().setFlashMode(DcsVideoView.DFME_AUTO);
					}
					dcsView.getImageGalleryView().setImageGalleryViewmode(DcsImageGalleryView.DIVME_MULTIPLE);
					invalidateOptionsMenu();
				} catch (DcsValueOutOfRangeException e) {
					e.printStackTrace();
				}
			}
		});
	}


	private void requestPermissions() {
		if (Build.VERSION.SDK_INT > 22) {
			try {
				if (ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
				}
				if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_OK);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		DcsView.setLicense(getApplicationContext(), "Your license number");
	}

	private File createImageFile(Bitmap documentImage) throws IOException {

		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_TEST_";
		File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
		);

		// Convert bitmap to Image
		mCurrentPhotoPath = image.getAbsolutePath();
		File file = new File(mCurrentPhotoPath);
		OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
		documentImage.compress(Bitmap.CompressFormat.JPEG, 100, os);
		os.close();
       /* pdfName = file.getName();
        new ProcessDocumentTask(imageURI).execute(documentImage);*/

		return image;
	}

	private void savePicture(Bitmap documentImage) {

		// Create the File where the photo should go
		File photoFile = null;

		try {
			photoFile = createImageFile(documentImage);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Continue only if the File was successfully created
		if (photoFile != null) {
			imageURI = FileProvider.getUriForFile(this,
					"com.dynamsoft.online.docscannerx",
					photoFile);


		}

	}

}
