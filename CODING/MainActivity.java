package com.example.voicerecognition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements
		TextToSpeech.OnInitListener {
	
	public static final String TAG = "TTS";

	private static final int REQUEST_CODE = 1234;
	private static final int MY_DATA_CHECK_CODE = 4321;
	
	private static String source, target;
	private static String[] sourceLangCodes, targetLangCodes;
	private static String text;
	private static String translatedText = "";
	private static Spinner sourceSpinner, targetSpinner;
	private ArrayAdapter<CharSequence> sourceAdapter, targetAdapter;

	public static TextToSpeech tts;	
	
	private static ListView dialog_list;	
	private static TextView output;
	private static EditText input_txt;
	private static Button speakButton;
	private static Button speakOutButton;
	private static Button translateButton;

	/**
	 * Called with the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_recog);

		speakButton = (Button) findViewById(R.id.speakButton);
		speakOutButton = (Button) findViewById(R.id.speakOutButton);
		input_txt = (EditText) findViewById(R.id.input_txt_box);
		translateButton = (Button) findViewById(R.id.translate_btn);
		output = (TextView) findViewById(R.id.translated_text);
				
		source = "English";
		target = "French";
		
		sourceLangCodes = getResources().getStringArray(R.array.input_language_code);
		targetLangCodes = getResources().getStringArray(R.array.output_language_code);
		
		initSpinners();


		// Disable button if no recognition service is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			speakButton.setEnabled(false);
			speakButton.setText("Recognizer not present");
		}
		
		speakButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}			
		});
		
		translateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String text = input_txt.getText().toString();
				
				if(target.equalsIgnoreCase("tamil") || target.equalsIgnoreCase("hindi")) {
					TranslatorBing.translate(text, source, target, output, true);
					Log.d("TTS", "Translation over...");
					Log.d(TAG, "Starting TTS event...");				
					Intent checkIntent = new Intent();
					checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
					startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
				} else {
					TranslatorBing.translate(text, source, target, output, false);
					Log.d("TTS", "Translation over...");
					Log.d(TAG, "Starting TTS event...");				
					Intent checkIntent = new Intent();
					checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
					startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);					
				}
				

				
			}
		});

		speakOutButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(init)
				{
					Log.d("TTS","Speak out clicked...");
					Log.d("TTS",translatedText);
					translatedText = output.getText().toString();
					Log.d("TTS",translatedText);
					tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		});

	}
	
	public void initSpinners() {
		Log.d(TAG, "Initiallizing spinners");
		sourceSpinner = (Spinner)findViewById(R.id.spinner1);
		sourceAdapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.languages_input, android.R.layout.simple_spinner_item);
		sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sourceSpinner.setAdapter(sourceAdapter);
		sourceSpinner.setOnItemSelectedListener(new SourceLanguageOnItemSelectedListener());
		sourceSpinner.setSelection(sourceAdapter.getPosition(source));
		
		targetSpinner = (Spinner)findViewById(R.id.spinner2);
		targetAdapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.languages_output, android.R.layout.simple_spinner_item);
		targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		targetSpinner.setAdapter(targetAdapter);
		targetSpinner.setOnItemSelectedListener(new TargetLanguageOnItemSelectedListener());
		targetSpinner.setSelection(targetAdapter.getPosition(target));
		Log.d(TAG, "Spinner initialized");
	}

	/**
	 * Fire an intent to start the voice recognition activity.
	 */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		String sourceCode = sourceLangCodes[sourceSpinner.getSelectedItemPosition()];
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceCode);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"Voice recognition Demo...");
		startActivityForResult(intent, REQUEST_CODE);
	}
	
	/**
	 * Handle the results from the voice recognition activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				tts = new TextToSpeech(this, MainActivity.this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}

		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			// Populate the wordsList with the String values the recognition
			// engine thought it heard
			matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			removeDialog(0);
			showDialog(0);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public ArrayList<String> matches;
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(id) {
		case 0:
			// Get the layout inflater
			LayoutInflater inflater = getLayoutInflater();
			LinearLayout layout = new LinearLayout(this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(lp);
			layout.setOrientation(LinearLayout.VERTICAL);
			
			
			dialog_list = new ListView(this);
			dialog_list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, matches));
			dialog_list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					text = ((TextView) view).getText().toString();
					
					input_txt.setText(text);
					

				}
			});
			
			layout.addView(dialog_list);
			
			builder.setView(layout);
			
		}	
		return builder.create();			      
	}

	boolean init = false;
	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		int result;
		if (status == TextToSpeech.SUCCESS) {
			result = -1;
			String targetLanguage = targetSpinner.getSelectedItem().toString();

			String targetCode = targetLangCodes[targetSpinner.getSelectedItemPosition()];
			Locale locale = new Locale(targetCode);
			result = tts.setLanguage(locale);
			Log.d(TAG, "target = " +targetCode);
			Toast.makeText(MainActivity.this, "TTS language set to : " + target +", code: " +targetCode, Toast.LENGTH_SHORT).show();				  
			
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
				Toast.makeText(getApplicationContext(), "Language not supported", Toast.LENGTH_SHORT).show();
				
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			} else {
				Log.d("TTS", "init set true...");
				speakOutButton.setVisibility(View.VISIBLE);
				speakOutButton.setEnabled(true);
				init = true;
				// speakOut();
			}

		} else {
			Log.e("TTS", "Initilization Failed!");
		}
	}
	
	
	  public class SourceLanguageOnItemSelectedListener implements OnItemSelectedListener {
		  
		  @Override
		  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			  
		      source = (String) sourceSpinner.getItemAtPosition(pos);
		      Log.d(TAG, "Source language selected as :" +source);
		      Toast.makeText(MainActivity.this, "Voice input language: " +source, Toast.LENGTH_SHORT).show();
		         
		  }

		  @Override
		  public void onNothingSelected(AdapterView<?> arg0) {
		      // Do nothing
		  }
	  }
		
	  public class TargetLanguageOnItemSelectedListener implements OnItemSelectedListener {
		  @Override
		  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			  // Save the target language preference
			  target = (String) targetSpinner.getItemAtPosition(pos);
			  
		  }

		  @Override
		  public void onNothingSelected(AdapterView<?> arg0) {
			  // Do nothing
		  }
	  }
	  
	  private class GetTamilTranslation extends AsyncTask<String, Void, String> {
			
			@Override
			protected String doInBackground(String... urls) {
				String response = "";
				for (String url : urls) {
					
					DefaultHttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost(url); 

					try {
						
						Log.d(TAG, "Starting DbSearch...");
						
						List<NameValuePair> form=new ArrayList<NameValuePair>();
						form.add(new BasicNameValuePair("english", input_txt.getText().toString().toLowerCase()));
						form.add(new BasicNameValuePair("language", target));
						post.setEntity(new UrlEncodedFormEntity(form, HTTP.UTF_8));

						HttpResponse resp = client.execute(post);
						
						String data = new BasicResponseHandler().handleResponse(resp);
						
						response = data;
						
//						response = Jsoup.parse(data).text();

					} catch (Exception e) {
						e.printStackTrace();
						Log.d(TAG, e.toString());
						response = "unable_to_download";
					}
				}
				return response;
			}

			@Override
			protected void onPostExecute(String result) {
				
				output.setText(result);
				
			}
		}
	  
}