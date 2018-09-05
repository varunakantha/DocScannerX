package com.dynamsoft.online.docscannerx;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

/**
 * Created by Elemen on 2018/5/22.
 */
public class SavedImgDetailActivity extends AppCompatActivity {
	private ImageView back;
	private ImageView imgDetail;
	private TextView imgName;
	private PDFView pdfView;

	private String name;
	private String path;
	private boolean isPDF;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_savedimgdetail);
		back = findViewById(R.id.iv_back);
		imgName = findViewById(R.id.tv_img_name);
		imgDetail = findViewById(R.id.iv_detail);
		pdfView = findViewById(R.id.PDFview);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		path = getIntent().getStringExtra("imgPath");
		name = getIntent().getStringExtra("imgName");
		isPDF = getIntent().getBooleanExtra("isPDF", false);
		imgName.setText(name);
		if (!isPDF) {
			pdfView.setVisibility(View.GONE);
			imgDetail.setVisibility(View.VISIBLE);
			Glide.with(this).load(path).into(imgDetail);
		} else {
			pdfView.setVisibility(View.VISIBLE);
			imgDetail.setVisibility(View.GONE);
			pdfView.fromFile(new File(path)).load();
		}
	}
}
