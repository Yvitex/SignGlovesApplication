package data;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.example.signglovesapplication.R;
import com.example.signglovesapplication.StartMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class WordProcessor {

    private TextToSpeech textToSpeech;
    private Context context;
    private MediaPlayer mediaPlayer;
    public String[] english = {"Hi", "See you later", "Cat", "Yes", "No", "Help", "Please",
    "Thank You", "Want", "What?", "Again", "Eat"};
    public String[] japanese = {"こんにちは", "また後で会いましょう", "猫", "はい", "いいえ", "助けて", "お願いします",
            "ありがとうございます", "欲しい", "何？", "もう一度", "食べる"};
    public String[] visaya = {"hi", "magkita_ta_unya", "ako", "oo", "dili", "tabang", "palihug",
            "salamat", "sama_sa", "unsa", "iro", "iring"};
    public String[] tagalog = {"hi", "kita_tayo_mamaya", "ako", "oo", "hindi", "tulong", "pakiusap",
            "salamat", "gusto", "ano?", "aso", "pusa"};
    public String[] chinese = {"你好", "回头见", "我", "是", "不", "帮助", "请",
            "谢谢", "想要", "什么?", "狗", "猫"};
    public String[] italian = {"Ciao", "A più tardi", "Io", "Sì", "No", "Aiuto", "Per favore",
            "Grazie", "Volere", "Cosa?", "Cane", "Gatto"};

    public JSONObject externalEnglish;

    private int statusCode;
    private Locale locale;
    private String mainDirectory = "GlovesApp";

    private File wordPath = new File(Environment.getExternalStorageDirectory(), mainDirectory + "/WordsDictionary.txt");

    public WordProcessor(Context context, Locale locale){
        this.context = context;
        this.locale = locale;
        this.mediaPlayer = new MediaPlayer();
        this.externalEnglish = readJSON(wordPath);
        initializeSpeaker();
    }

    public JSONObject getCustomWords() {
        return this.externalEnglish;
    }

    public JSONObject readJSON(File filename) {
        try {
            FileInputStream inputStream = new FileInputStream(filename);

            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            return jsonObject;

        } catch (IOException | JSONException e) {
            Log.e("GlovesApp", "readJSON: " + e.toString());
            return new JSONObject();
        }
    }

    public int getLengthEnglish() {
        return this.english.length;
    }

    public int getLengthCustom() {
        return this.externalEnglish.length();
    }

    public String wordCorrector(String word) {
        String[] parts = word.split("_");

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!part.isEmpty()) {
                if (i != 0) {
                    sb.append(" ");
                }
                sb.append(Character.toUpperCase(part.charAt(0))); // Capitalize first letter
                if (part.length() > 1) { // If there are more characters, append them
                    sb.append(part.substring(1)); // Append remaining characters
                }
            }
        }

        // Convert StringBuilder to String and return
        return sb.toString();
    }


    public void setLocale(Locale locale) {
        this.locale = locale;
        initializeSpeaker();
    }

    public void playSounds(String data) {
        int id = context.getResources().getIdentifier(data, "raw", context.getPackageName());

        Log.d("Locale", "playSounds: " + id);
                if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            try {
                mediaPlayer = MediaPlayer.create(context, id);
                mediaPlayer.start();
            } catch (Exception e) {
                Toast.makeText(context, "Something Gone Wrong In Player" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeSpeaker(){
        Toast.makeText(context, "Locale: " + String.valueOf(locale), Toast.LENGTH_LONG).show();
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    statusCode = textToSpeech.setLanguage(locale);
                    Toast.makeText(context, "Initialized Language " + String.valueOf(locale), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Failed to initialize Text to Speech", Toast.LENGTH_LONG).show();
                }

                if (statusCode == TextToSpeech.LANG_MISSING_DATA || statusCode == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, locale.getDisplayName() + " language not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public String speak(String data) {
        String text = data.trim();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        return data;
    }

    public String speak(int num) {
        String text;
        String language = String.valueOf(locale);
        switch (language) {
            case "en": text = english[num].trim();
            break;
            case "ja": text = japanese[num].trim();
            break;
            case "zh": text = chinese[num].trim();
            break;
            case "it": text = italian[num].trim();
            break;
            case "filipino": text = tagalog[num].trim();
            break;
            case "visaya": text = visaya[num].trim();
            break;
            default: text = null;
            break;
        }
        if (text != null) {
            if (!language.equals("filipino") && !language.equals("visaya")) {
                Log.d("Locale", "speak: Executed Non F");
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                Log.d("Locale", "speak: Executed Non P");
                playSounds(text);
            }
            return this.wordCorrector(text);
        } else {
            return "Null";
        }

    }
}
