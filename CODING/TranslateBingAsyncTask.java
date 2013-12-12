package com.example.voicerecognition;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public final class TranslateBingAsyncTask extends AsyncTask<String, Void, Boolean> {

  private static final String API_KEY = "FE55328FE94D3809B4C6F458F1C5E4E655FE47FF";

  private Language sourceLanguage;
  private Language targetLanguage;
  private String text;
  private String translatedText;
  private TextView textView;
  private boolean indianLanguage;
  private String targetIndianLanguage;

  public TranslateBingAsyncTask(String text, Language sourceLanguage, Language targetLanguage, TextView textView, boolean indianLanguage, String targetIndianLanguage) {
    this.sourceLanguage = sourceLanguage;
    this.targetLanguage = targetLanguage;
    this.text = text;
    this.textView = textView;
    this.indianLanguage = indianLanguage;
    this.targetIndianLanguage = targetIndianLanguage;
  }
  
  @Override
  protected synchronized void onPreExecute() {
    super.onPreExecute();
    
    textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL);
    textView.setTextSize(14);
    textView.setText("Translating...");
  }
  
  @Override
  protected synchronized Boolean doInBackground(String... arg0) {
    Translate.setKey(API_KEY);
    try {
      // Request translation
      translatedText = Translate.execute(text, sourceLanguage, targetLanguage);
      if(indianLanguage){
    	  DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://speakindia-saveetha.appspot.com/WordSearch"); 

			try {			
				
				List<NameValuePair> form=new ArrayList<NameValuePair>();
				form.add(new BasicNameValuePair("english", translatedText.toLowerCase()));
				form.add(new BasicNameValuePair("language", targetIndianLanguage));
				post.setEntity(new UrlEncodedFormEntity(form, HTTP.UTF_8));

				HttpResponse resp = client.execute(post);
				
				String data = new BasicResponseHandler().handleResponse(resp);
				
				translatedText = data;
				
			} catch (Exception e) {
				e.printStackTrace();
				translatedText = "unable_to_download";
			}

    	  
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  protected synchronized void onPostExecute(Boolean result) {
    super.onPostExecute(result);
    
    if (result) {
      // Reset the text formatting
      if (textView != null) {
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL);
      }
      
      // Put the translation into the textview
      textView.setText(translatedText);

      // Crudely scale betweeen 22 and 32 -- bigger font for shorter text
      int scaledSize = Math.max(22, 32 - translatedText.length() / 4);
      textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);

    } else {
      textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
      textView.setText("Unavailable");
    }
  }
}
