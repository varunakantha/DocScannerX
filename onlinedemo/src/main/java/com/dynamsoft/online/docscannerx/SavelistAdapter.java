package com.dynamsoft.online.docscannerx;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

import cn.bingoogolapple.baseadapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.baseadapter.BGAViewHolderHelper;

/**
 * Created by Elemen on 2018/5/10.
 */
public class SavelistAdapter extends BGARecyclerViewAdapter<SaveFileModel> {


	public SavelistAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.save_list_item);
	}


	@Override
	protected void fillData(BGAViewHolderHelper helper, final int position, SaveFileModel saveFileModel) {
		ImageView imageView = helper.getImageView(R.id.iv_save_thumb);
		if (!saveFileModel.isPDF) {
			imageView.setVisibility(View.VISIBLE);
			helper.getView(R.id.iv_pdf).setVisibility(View.GONE);
			Glide.with(mContext).load(saveFileModel.filePath).placeholder(R.drawable.ic_holder_dark).into(imageView);
			if(saveFileModel.filePath.endsWith(".png"))
				helper.setText(R.id.tv_save_name, saveFileModel.fileName+".png");
			else
				helper.setText(R.id.tv_save_name, saveFileModel.fileName+".jpg");
			helper.setText(R.id.tv_save_date, saveFileModel.modifyDate);
		} else {
			imageView.setVisibility(View.GONE);
			helper.getView(R.id.iv_pdf).setVisibility(View.VISIBLE);
			((PDFView) helper.getView(R.id.iv_pdf)).fromFile(new File(saveFileModel.filePath)).enableDoubletap(false).pages(0).enableSwipe(false).load();
			helper.setText(R.id.tv_save_name, saveFileModel.fileName+".pdf");
			helper.setText(R.id.tv_save_date, saveFileModel.modifyDate);

		}
		helper.getView(R.id.iv_trash).setTag(saveFileModel);
		helper.getView(R.id.iv_trash).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SaveFileModel sfpositon = (SaveFileModel) v.getTag();
				SavelistAdapter.this.removeItem(sfpositon);
				File file = new File(sfpositon.filePath);
				if(file.exists()){
					file.delete();
				}
			}
		});
	}

}
