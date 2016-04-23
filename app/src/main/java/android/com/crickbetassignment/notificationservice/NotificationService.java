package android.com.crickbetassignment.notificationservice;

import android.annotation.TargetApi;
import android.app.Service;
import android.com.crickbetassignment.R;
import android.com.crickbetassignment.app.AppController;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Duke on 4/23/2016.
 */
public class NotificationService extends Service {


    private static final String SCORE_URL = "http://staging.matchupcricket.com/cric/api/match/scores/";

    private WindowManager windowManager;


    private String currentScore = " ";

    private LayoutInflater li;
    private View view;


    private String _currentScore = " ";
    private TextView _scoreShowBoardTV;


    private WindowManager.LayoutParams _params;
    Handler handler;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String data = intent.getStringExtra("data");
        if (data.contentEquals("start")) {
            regularCheckScore();
        } else {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        super.onCreate();
        createView();
    }


    private void createView() {
        li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        view = new View(this);
        _params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        _params.gravity = Gravity.TOP | Gravity.LEFT;
        _params.x = 0;
        _params.y = 100;
        view = li.inflate(R.layout.rowitem, null);
        _scoreShowBoardTV = (TextView) view.findViewById(R.id.textViewId_updatedScore);
        _scoreShowBoardTV.setText(getCurrentScore());
        windowManager.addView(view, _params);
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = _params.x;
                        initialY = _params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        _params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        _params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(view, _params);
                        return true;
                }

                return false;
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Toast.makeText(NotificationService.this, "Image Circle touched", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(NotificationService.this, "Image Circle touched", Toast.LENGTH_SHORT).show();

            }
        });

    }


    public void regularCheckScore() {

        handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            new PerformBackgroundTask().execute();

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.d("handler error", e.getMessage());
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 50000 ms
    }


    private class PerformBackgroundTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String string_req = "String_data_request_commentRead";
            StringRequest strReq = new StringRequest(Request.Method.GET,
                    SCORE_URL, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {

                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        int len = jsonArray.length();
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        currentScore = jsonObject.getString("current_score");
                        _scoreShowBoardTV.setText(currentScore);

                    } catch (JSONException e) {
                        e.printStackTrace();

                        Log.d("Error in Parsing", e.getMessage());


                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Error Volley", error.getMessage());
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("match_id", "iplt20_2014_g20_classic");
                    params.put("solution_id", "2");
                    return params;
                }

            };

            AppController.getInstance().addToRequestQueue(strReq, string_req);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public void setCurrentScore(String currentScore) {
        this._currentScore = currentScore;
    }

    public String getCurrentScore() {
        return this._currentScore;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        windowManager.removeViewImmediate(view);
    }
}
