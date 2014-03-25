package com.example.newdemo;

import java.io.IOException;
import java.io.Serializable;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppInfo implements Serializable{
		private String appLabel;    //Ӧ�ó����ǩ  
	    private Drawable appIcon ;  //Ӧ�ó���ͼ��  
	    private Intent intent ;     //����Ӧ�ó����Intent ��һ����ActionΪMain��CategoryΪLancher��Activity  
	    private String pkgName ;    //Ӧ�ó������Ӧ�İ���  
	      
	    public AppInfo(){}  
	      
	    public String getAppLabel() {  
	        return appLabel;  
	    }  
	    public void setAppLabel(String appName) {  
	        this.appLabel = appName;  
	    }  
	    public Drawable getAppIcon() {  
	        return appIcon;  
	    }  
	    public void setAppIcon(Drawable appIcon) {  
	        this.appIcon = appIcon;  
	    }  
	    public Intent getIntent() {  
	        return intent;  
	    }  
	    public void setIntent(Intent intent) {  
	        this.intent = intent;  
	    }  
	    public String getPkgName(){  
	        return pkgName ;  
	    }  
	    public void setPkgName(String pkgName){  
	        this.pkgName=pkgName ;  
	    }  	
	    
	    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	        out.defaultWriteObject();
	        out.writeObject(appLabel);
	        out.writeObject(appIcon);
	        out.writeObject(intent);
	        out.writeObject(pkgName);
	        
	    }

	    private void readObject(java.io.ObjectInputStream in) throws IOException,
	            ClassNotFoundException {
	        in.defaultReadObject();
	        appLabel=(String) in.readObject();
	        appIcon=(Drawable) in.readObject();
	        intent=(Intent) in.readObject();
	        pkgName=(String) in.readObject();      
	    }

}
