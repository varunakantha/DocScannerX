package com.dynamsoft.online.docscannerx;

/**
 * Created by Elemen on 2018/5/10.
 */
public class SaveFileModel {
	public String filePath;
	public String fileName;
	public String modifyDate;
	public boolean isPDF = false;

	public SaveFileModel() {
	}

	public SaveFileModel(String filePath, String fileName) {
		this.filePath = filePath;
		this.fileName = fileName;
	}

	public String getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isPDF() {
		return isPDF;
	}

	public void setPDF(boolean PDF) {
		isPDF = PDF;
	}
}
